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

    @Transactional
    public TransacaoRecorrente save(TransacaoRecorrenteRequest request) {
        TransacaoRecorrente transacaoRecorrente = new TransacaoRecorrente();
        applyRequestToEntity(request, transacaoRecorrente);
        TransacaoRecorrente salva = transacaoRecorrenteRepository.save(transacaoRecorrente);
        
        // Se a transação recorrente criada é para o dia atual e está ativa, processa imediatamente
        if (salva.isAtiva()) {
            LocalDate hoje = LocalDate.now();
            if (deveExecutarHoje(salva, hoje)) {
                processarRecorrencia(salva, hoje);
            }
        }
        
        return salva;
    }

    public TransacaoRecorrente update(UUID id, TransacaoRecorrenteRequest request) {
        TransacaoRecorrente transacaoRecorrente = transacaoRecorrenteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transação recorrente não encontrada com o ID informado."));
        applyRequestToEntity(request, transacaoRecorrente);
        return transacaoRecorrenteRepository.save(transacaoRecorrente);
    }

    public void delete(UUID id) {
        if (!transacaoRecorrenteRepository.existsById(id)) {
            throw new EntityNotFoundException("Transação recorrente não encontrada com o ID informado.");
        }
        transacaoRecorrenteRepository.deleteById(id);
    }

    public List<TransacaoRecorrente> getByUser(Usuario usuario) {
        return transacaoRecorrenteRepository.findByUser(usuario);
    }

    public List<TransacaoRecorrente> getByUser(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID informado."));
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

    /**
     * Processa recorrências pendentes de um usuário específico para o dia atual.
     * Útil para verificar e executar transações recorrentes quando o usuário faz login,
     * completa onboarding ou cria uma nova transação recorrente.
     * 
     * @param usuario O usuário para verificar recorrências pendentes
     */
    @Transactional
    public void processarRecorrenciasPendentesDoUsuario(Usuario usuario) {
        LocalDate hoje = LocalDate.now();
        List<TransacaoRecorrente> recorrentes = transacaoRecorrenteRepository.findByUserAndAtivaTrue(usuario);
        
        for (TransacaoRecorrente recorrente : recorrentes) {
            if (deveExecutarHoje(recorrente, hoje)) {
                processarRecorrencia(recorrente, hoje);
            }
        }
    }

    private void processarRecorrencia(TransacaoRecorrente recorrente, LocalDate dataReferencia) {
        // PRIMEIRA VERIFICAÇÃO: Verifica se já foi processada hoje (otimização rápida)
        if (transacaoRecorrenteExecucaoRepository
                .existsByTransacaoRecorrenteAndDataExecucao(recorrente, dataReferencia)) {
            log.debug("Recorrência {} já processada em {}", recorrente.getId(), dataReferencia);
            return;
        }

        try {
            // SEGUNDA VERIFICAÇÃO: Verifica novamente antes de processar (proteção contra race condition)
            // Isso garante que mesmo se duas threads passarem pela primeira verificação,
            // apenas uma vai processar
            if (transacaoRecorrenteExecucaoRepository
                    .existsByTransacaoRecorrenteAndDataExecucao(recorrente, dataReferencia)) {
                log.debug("Recorrência {} já processada em {} (verificação dupla)", 
                        recorrente.getId(), dataReferencia);
                return;
            }
            
            // Cria a transação automatica (com verificação interna de duplicação)
            criarTransacaoAutomatica(recorrente, dataReferencia);
            
            // TERCEIRA VERIFICAÇÃO: Verifica uma última vez antes de registrar execução
            // Se outra thread processou entre criar a transação e registrar execução
            if (transacaoRecorrenteExecucaoRepository
                    .existsByTransacaoRecorrenteAndDataExecucao(recorrente, dataReferencia)) {
                log.debug("Execução já registrada para recorrência {} em {} (outra thread processou)", 
                        recorrente.getId(), dataReferencia);
                return;
            }
            
            // Registra a execução com sucesso
            // A constraint única no banco garante que não haverá duplicação mesmo em race conditions
            registrarExecucao(recorrente, dataReferencia, TransacaoRecorrenteExecucaoStatus.SUCESSO, null);
            log.info("Transação recorrente {} executada com sucesso em {}", recorrente.getId(), dataReferencia);
            
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            // Constraint unique violation ao registrar execução - outra thread já processou
            log.debug("Execução já registrada para recorrência {} em {} (constraint violation - outra thread processou)", 
                    recorrente.getId(), dataReferencia);
            // Não precisa fazer nada, já foi processada por outra thread
        } catch (Exception ex) {
            log.error("Falha ao processar transação recorrente {} em {}", recorrente.getId(), dataReferencia, ex);
            // Tenta registrar como falha, mas se já existir execução (outra thread processou), ignora
            try {
                // Verifica novamente antes de registrar falha
                if (!transacaoRecorrenteExecucaoRepository
                        .existsByTransacaoRecorrenteAndDataExecucao(recorrente, dataReferencia)) {
                    registrarExecucao(recorrente, dataReferencia, TransacaoRecorrenteExecucaoStatus.FALHA, ex.getMessage());
                } else {
                    log.debug("Execução já registrada para recorrência {} em {} (outra thread processou)", 
                            recorrente.getId(), dataReferencia);
                }
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                // Já existe execução (provavelmente outra thread processou com sucesso)
                log.debug("Execução já registrada para recorrência {} em {} (constraint violation)", 
                        recorrente.getId(), dataReferencia);
            }
        }
    }

    private void criarTransacaoAutomatica(TransacaoRecorrente recorrente, LocalDate dataExecucao) {
        // Verificação adicional para evitar duplicação em race conditions
        // Verifica se já existe uma transação com as mesmas características criada hoje
        boolean jaExiste = transacaoRepository.existsTransacaoSimilar(
            recorrente.getUser(),
            recorrente.getCategoria(),
            recorrente.getTipo(),
            recorrente.getValor(),
            recorrente.getDescricao(),
            dataExecucao
        );
        
        if (jaExiste) {
            log.debug("Transação similar já existe para recorrência {} em {}. Pulando criação.", 
                    recorrente.getId(), dataExecucao);
            return;
        }
        
        Transacao transacao = new Transacao();
        transacao.setUser(recorrente.getUser());
        transacao.setCategoria(recorrente.getCategoria());
        transacao.setTipo(recorrente.getTipo());
        transacao.setValor(recorrente.getValor());
        transacao.setDescricao(recorrente.getDescricao());
        // Converte LocalDate para LocalDateTime (início do dia)
        transacao.setData(dataExecucao.atStartOfDay());
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
            throw new IllegalArgumentException("O tipo da transação recorrente é obrigatório (RECEITA ou DESPESA).");
        }
        if (request.getValor() == null) {
            throw new IllegalArgumentException("O valor da transação recorrente é obrigatório.");
        }

        entity.setTipo(request.getTipo());
        entity.setValor(request.getValor());
        entity.setDescricao(request.getDescricao());

        Integer diaRecorrencia = request.getDiaRecorrencia();
        if (diaRecorrencia == null) {
            throw new IllegalArgumentException("O dia de recorrência é obrigatório (deve estar entre 1 e 28).");
        }
        entity.setDiaRecorrencia(diaRecorrencia);

        if (request.getAtiva() != null) {
            entity.setAtiva(request.getAtiva());
        }

        UUID userId = request.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("O ID do usuário é obrigatório.");
        }
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID informado."));
        entity.setUser(usuario);

        UUID categoriaId = request.getCategoriaId();
        if (categoriaId == null) {
            throw new IllegalArgumentException("O ID da categoria é obrigatório.");
        }
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com o ID informado."));
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
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com o ID informado."));

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

