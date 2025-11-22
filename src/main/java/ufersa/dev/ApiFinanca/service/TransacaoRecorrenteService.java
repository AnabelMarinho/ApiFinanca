package ufersa.dev.ApiFinanca.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ufersa.dev.ApiFinanca.dto.TransacaoRecorrenteRequest;
import ufersa.dev.ApiFinanca.model.*;
import ufersa.dev.ApiFinanca.repository.CategoriaRepository;
import ufersa.dev.ApiFinanca.repository.TransacaoRecorrenteExecucaoRepository;
import ufersa.dev.ApiFinanca.repository.TransacaoRecorrenteRepository;
import ufersa.dev.ApiFinanca.repository.TransacaoRepository;
import ufersa.dev.ApiFinanca.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class TransacaoRecorrenteService {

    private final TransacaoRecorrenteRepository transacaoRecorrenteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final TransacaoRepository transacaoRepository;
    private final TransacaoRecorrenteExecucaoRepository transacaoRecorrenteExecucaoRepository;

    public TransacaoRecorrenteService(TransacaoRecorrenteRepository transacaoRecorrenteRepository,
                                      UsuarioRepository usuarioRepository,
                                      CategoriaRepository categoriaRepository,
                                      TransacaoRepository transacaoRepository,
                                      TransacaoRecorrenteExecucaoRepository transacaoRecorrenteExecucaoRepository) {
        this.transacaoRecorrenteRepository = transacaoRecorrenteRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.transacaoRepository = transacaoRepository;
        this.transacaoRecorrenteExecucaoRepository = transacaoRecorrenteExecucaoRepository;
    }

    public List<TransacaoRecorrente> getAll() {
        return transacaoRecorrenteRepository.findAll();
    }

    public Optional<TransacaoRecorrente> getById(UUID id) {
        return transacaoRecorrenteRepository.findById(id);
    }

    public TransacaoRecorrente save(TransacaoRecorrenteRequest request) {
        TransacaoRecorrente transacaoRecorrente = new TransacaoRecorrente();
        applyRequestToEntity(request, transacaoRecorrente);
        return transacaoRecorrenteRepository.save(transacaoRecorrente);
    }

    public TransacaoRecorrente update(UUID id, TransacaoRecorrenteRequest request) {
        TransacaoRecorrente transacaoRecorrente = transacaoRecorrenteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transação recorrente não encontrada para o id " + id));
        applyRequestToEntity(request, transacaoRecorrente);
        return transacaoRecorrenteRepository.save(transacaoRecorrente);
    }

    public void delete(UUID id) {
        if (!transacaoRecorrenteRepository.existsById(id)) {
            throw new EntityNotFoundException("Transação recorrente não encontrada para o id " + id);
        }
        transacaoRecorrenteRepository.deleteById(id);
    }

    public List<TransacaoRecorrente> getByUser(Usuario usuario) {
        return transacaoRecorrenteRepository.findByUser(usuario);
    }

    public List<TransacaoRecorrente> getByUser(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado para o id " + usuarioId));
        return getByUser(usuario);
    }

    public List<TransacaoRecorrente> getAtivasByUser(Usuario usuario) {
        return transacaoRecorrenteRepository.findByUserAndAtivaTrue(usuario);
    }

    public List<TransacaoRecorrente> getInativasByUser(Usuario usuario) {
        return transacaoRecorrenteRepository.findByUserAndAtivaFalse(usuario);
    }

    public List<TransacaoRecorrente> getByUserAndTipo(Usuario usuario, TipoTransacao tipo) {
        return transacaoRecorrenteRepository.findByUserAndTipo(usuario, tipo);
    }

    public List<TransacaoRecorrente> getAtivasByUserAndTipo(Usuario usuario, TipoTransacao tipo) {
        return transacaoRecorrenteRepository.findByUserAndTipoAndAtivaTrue(usuario, tipo);
    }

    public List<TransacaoRecorrente> getByUserAndCategoria(Usuario usuario, Categoria categoria) {
        return transacaoRecorrenteRepository.findByUserAndCategoria(usuario, categoria);
    }

    public List<TransacaoRecorrente> getAtivasByUserAndCategoria(Usuario usuario, Categoria categoria) {
        return transacaoRecorrenteRepository.findByUserAndCategoriaAndAtivaTrue(usuario, categoria);
    }

    public List<TransacaoRecorrente> getByUserAndDiaRecorrencia(Usuario usuario, int diaRecorrencia) {
        return transacaoRecorrenteRepository.findByUserAndDiaRecorrencia(usuario, diaRecorrencia);
    }

    public List<TransacaoRecorrente> getAtivas() {
        return transacaoRecorrenteRepository.findByAtivaTrue();
    }

    public List<TransacaoRecorrente> getAtivasPorDiaRecorrencia(int diaRecorrencia) {
        return transacaoRecorrenteRepository.findByAtivaTrueAndDiaRecorrencia(diaRecorrencia);
    }

    @Transactional
    public void processarRecorrenciasDoDia(LocalDate dataReferencia) {
        List<TransacaoRecorrente> recorrentes = transacaoRecorrenteRepository.findByAtivaTrue();
        for (TransacaoRecorrente recorrente : recorrentes) {
            if (!deveExecutarHoje(recorrente, dataReferencia)) {
                continue;
            }
            processarRecorrencia(recorrente, dataReferencia);
        }
    }

    private void processarRecorrencia(TransacaoRecorrente recorrente, LocalDate dataReferencia) {
        if (transacaoRecorrenteExecucaoRepository
                .existsByTransacaoRecorrenteAndDataExecucao(recorrente, dataReferencia)) {
            log.debug("Recorrência {} já processada em {}", recorrente.getId(), dataReferencia);
            return;
        }

        try {
            criarTransacaoAutomatica(recorrente, dataReferencia);
            registrarExecucao(recorrente, dataReferencia, TransacaoRecorrenteExecucaoStatus.SUCESSO, null);
            log.info("Transação recorrente {} executada com sucesso em {}", recorrente.getId(), dataReferencia);
        } catch (Exception ex) {
            log.error("Falha ao processar transação recorrente {} em {}", recorrente.getId(), dataReferencia, ex);
            registrarExecucao(recorrente, dataReferencia, TransacaoRecorrenteExecucaoStatus.FALHA, ex.getMessage());
        }
    }

    private void criarTransacaoAutomatica(TransacaoRecorrente recorrente, LocalDate dataExecucao) {
        Transacao transacao = new Transacao();
        transacao.setUser(recorrente.getUser());
        transacao.setCategoria(recorrente.getCategoria());
        transacao.setTipo(recorrente.getTipo());
        transacao.setValor(recorrente.getValor());
        transacao.setDescricao(recorrente.getDescricao());
        transacao.setData(dataExecucao);
        transacaoRepository.save(transacao);
        // Atualiza o saldo do usuário após criar a transação
        atualizarSaldoUsuario(recorrente.getUser());
    }
    
    /**
     * Atualiza o saldo atual do usuário baseado no saldo inicial + todas as receitas - todas as despesas.
     * O saldo inicial é mantido separado e nunca muda após o onboarding.
     */
    private void atualizarSaldoUsuario(Usuario usuario) {
        // Busca o usuário completo do banco para ter o saldo inicial
        Usuario usuarioCompleto = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        
        // O saldo inicial é o valor salvo no onboarding e nunca muda
        BigDecimal saldoInicial = usuarioCompleto.getSaldoInicial() != null 
                ? usuarioCompleto.getSaldoInicial() 
                : BigDecimal.ZERO;
        
        BigDecimal totalReceitas = transacaoRepository.calcularTotalReceitas(usuarioCompleto);
        BigDecimal totalDespesas = transacaoRepository.calcularTotalDespesas(usuarioCompleto);
        
        // Recalcula o novo saldo: saldo inicial + receitas - despesas
        BigDecimal novoSaldo = saldoInicial.add(totalReceitas).subtract(totalDespesas);
        
        usuarioCompleto.setSaldoAtual(novoSaldo);
        usuarioRepository.save(usuarioCompleto);
    }

    private void registrarExecucao(TransacaoRecorrente recorrente,
                                   LocalDate dataReferencia,
                                   TransacaoRecorrenteExecucaoStatus status,
                                   String mensagemErro) {
        TransacaoRecorrenteExecucao execucao = new TransacaoRecorrenteExecucao();
        execucao.setTransacaoRecorrente(recorrente);
        execucao.setDataExecucao(dataReferencia);
        execucao.setStatus(status);
        execucao.setMensagemErro(mensagemErro);
        transacaoRecorrenteExecucaoRepository.save(execucao);
    }

    private boolean deveExecutarHoje(TransacaoRecorrente recorrente, LocalDate dataReferencia) {
        int diaEfetivo = obterDiaEfetivoParaMes(recorrente.getDiaRecorrencia(), dataReferencia);
        return dataReferencia.getDayOfMonth() == diaEfetivo;
    }

    private int obterDiaEfetivoParaMes(int diaRecorrencia, LocalDate dataReferencia) {
        int ultimoDiaDoMes = dataReferencia.lengthOfMonth();
        return Math.min(diaRecorrencia, ultimoDiaDoMes);
    }

    private void applyRequestToEntity(TransacaoRecorrenteRequest request, TransacaoRecorrente entity) {
        if (request.getTipo() == null) {
            throw new IllegalArgumentException("tipo é obrigatório");
        }
        if (request.getValor() == null) {
            throw new IllegalArgumentException("valor é obrigatório");
        }

        entity.setTipo(request.getTipo());
        entity.setValor(request.getValor());
        entity.setDescricao(request.getDescricao());

        Integer diaRecorrencia = request.getDiaRecorrencia();
        if (diaRecorrencia == null) {
            throw new IllegalArgumentException("diaRecorrencia é obrigatório");
        }
        entity.setDiaRecorrencia(diaRecorrencia);

        if (request.getAtiva() != null) {
            entity.setAtiva(request.getAtiva());
        }

        UUID userId = request.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("userId é obrigatório");
        }
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado para o id " + userId));
        entity.setUser(usuario);

        UUID categoriaId = request.getCategoriaId();
        if (categoriaId == null) {
            throw new IllegalArgumentException("categoriaId é obrigatório");
        }
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada para o id " + categoriaId));
        entity.setCategoria(categoria);
    }

    public void saveFromOnboarding(
            Usuario usuario,
            TipoTransacao tipo,
            @NotNull @DecimalMin("0.01") BigDecimal valor,
            UUID categoriaId,
            String descricao,
            int diaRecorrencia
    ) {

        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada para o id " + categoriaId));

        TransacaoRecorrente recorrente = new TransacaoRecorrente();
        recorrente.setUser(usuario);
        recorrente.setTipo(tipo);
        recorrente.setValor(valor);
        recorrente.setDescricao(descricao);
        recorrente.setCategoria(categoria);
        recorrente.setDiaRecorrencia(diaRecorrencia);
        recorrente.setAtiva(true);

        transacaoRecorrenteRepository.save(recorrente);
    }



}

