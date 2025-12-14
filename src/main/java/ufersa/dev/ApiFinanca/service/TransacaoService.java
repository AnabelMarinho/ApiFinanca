package ufersa.dev.ApiFinanca.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ufersa.dev.ApiFinanca.dto.TransacaoRequest;
import ufersa.dev.ApiFinanca.model.Categoria;
import ufersa.dev.ApiFinanca.model.TipoTransacao;
import ufersa.dev.ApiFinanca.model.Transacao;
import ufersa.dev.ApiFinanca.model.Usuario;
import ufersa.dev.ApiFinanca.repository.CategoriaRepository;
import ufersa.dev.ApiFinanca.repository.TransacaoRepository;
import ufersa.dev.ApiFinanca.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;

    public TransacaoService(TransacaoRepository transacaoRepository,
                            UsuarioRepository usuarioRepository,
                            CategoriaRepository categoriaRepository) {
        this.transacaoRepository = transacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<Transacao> getAll() {
        return transacaoRepository.findAll();
    }

    public Optional<Transacao> getById(UUID id) {
        return transacaoRepository.findById(id);
    }

    @Transactional
    public Transacao save(TransacaoRequest request) {
        Transacao transacao = new Transacao();
        applyRequestToEntity(request, transacao);
        Transacao saved = transacaoRepository.save(transacao);
        transacaoRepository.flush(); // Garante que a transação seja persistida antes de recalcular o saldo
        atualizarSaldoUsuario(saved.getUser());
        return saved;
    }

    @Transactional
    public Transacao update(UUID id, TransacaoRequest request) {
        Transacao transacao = transacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transação não encontrada com o ID informado."));
        
        // Verifica se a transação é de uma categoria de meta (não pode ser atualizada)
        if (isCategoriaMeta(transacao.getCategoria())) {
            throw new IllegalArgumentException("Transações de aporte ou saque de meta não podem ser atualizadas.");
        }
        
        Usuario usuarioAntigo = transacao.getUser();
        applyRequestToEntity(request, transacao);
        
        // Verifica se a nova categoria também é de meta
        UUID categoriaId = request.getCategoriaId();
        if (categoriaId != null) {
            Categoria novaCategoria = categoriaRepository.findById(categoriaId)
                    .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com o ID informado."));
            if (isCategoriaMeta(novaCategoria)) {
                throw new IllegalArgumentException("Não é permitido alterar uma transação para categoria de meta.");
            }
        }
        
        Transacao saved = transacaoRepository.save(transacao);
        transacaoRepository.flush(); // Garante que a transação seja persistida antes de recalcular o saldo
        
        // Atualiza o saldo do usuário (pode ter mudado se o valor, tipo ou userId mudou)
        atualizarSaldoUsuario(saved.getUser());
        
        // Se o usuário mudou, também atualiza o usuário antigo
        if (!usuarioAntigo.getId().equals(saved.getUser().getId())) {
            atualizarSaldoUsuario(usuarioAntigo);
        }
        
        return saved;
    }

    @Transactional
    public void delete(UUID id) {
        Transacao transacao = transacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transação não encontrada com o ID informado."));
        
        // Verifica se a transação é de uma categoria de meta (não pode ser deletada)
        if (isCategoriaMeta(transacao.getCategoria())) {
            throw new IllegalArgumentException("Transações de aporte ou saque de meta não podem ser deletadas.");
        }
        
        Usuario usuario = transacao.getUser();
        transacaoRepository.deleteById(id);
        transacaoRepository.flush(); // Garante que a transação seja deletada antes de recalcular o saldo
        atualizarSaldoUsuario(usuario);
    }

    public List<Transacao> getByUser(Usuario usuario) {
        return transacaoRepository.findByUser(usuario);
    }

    public List<Transacao> getByUser(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID informado."));
        return getByUser(usuario);
    }

    public List<Transacao> getByUserOrderByDataDesc(Usuario usuario) {
        return transacaoRepository.findByUserOrderByDataDesc(usuario);
    }

    public List<Transacao> getByUserAndTipo(Usuario usuario, TipoTransacao tipo) {
        return transacaoRepository.findByUserAndTipo(usuario, tipo);
    }

    public List<Transacao> getByUserAndCategoria(Usuario usuario, Categoria categoria) {
        return transacaoRepository.findByUserAndCategoria(usuario, categoria);
    }

    public List<Transacao> getByUserAndPeriodo(Usuario usuario, LocalDate dataInicio, LocalDate dataFim) {
        return transacaoRepository.findByUserAndDataBetween(usuario, dataInicio, dataFim);
    }

    public List<Transacao> getByUserTipoEPeriodo(Usuario usuario, TipoTransacao tipo, LocalDate dataInicio, LocalDate dataFim) {
        return transacaoRepository.findByUserAndTipoAndDataBetween(usuario, tipo, dataInicio, dataFim);
    }

    public List<Transacao> getByUserCategoriaEPeriodo(Usuario usuario, Categoria categoria, LocalDate dataInicio, LocalDate dataFim) {
        return transacaoRepository.findByUserAndCategoriaAndDataBetween(usuario, categoria, dataInicio, dataFim);
    }

    public BigDecimal calcularTotalPorTipoEPeriodo(Usuario usuario, TipoTransacao tipo, LocalDate dataInicio, LocalDate dataFim) {
        return transacaoRepository.calcularTotalPorTipoEPeriodo(usuario, tipo, dataInicio, dataFim);
    }

    public BigDecimal calcularTotalPorCategoriaEPeriodo(Usuario usuario, Categoria categoria, LocalDate dataInicio, LocalDate dataFim) {
        return transacaoRepository.calcularTotalPorCategoriaEPeriodo(usuario, categoria, dataInicio, dataFim);
    }

    public List<Transacao> getTop10ByUser(Usuario usuario) {
        return transacaoRepository.findTop10ByUserOrderByDataDesc(usuario);
    }

    private void applyRequestToEntity(TransacaoRequest request, Transacao transacao) {
        if (request.getTipo() == null) {
            throw new IllegalArgumentException("O tipo da transação é obrigatório (RECEITA ou DESPESA).");
        }
        if (request.getValor() == null) {
            throw new IllegalArgumentException("O valor da transação é obrigatório.");
        }
        if (request.getData() == null) {
            throw new IllegalArgumentException("A data da transação é obrigatória.");
        }

        transacao.setTipo(request.getTipo());
        transacao.setValor(request.getValor());
        transacao.setData(request.getData());
        transacao.setDescricao(request.getDescricao());

        UUID userId = request.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("O ID do usuário é obrigatório.");
        }
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID informado."));
        transacao.setUser(usuario);

        UUID categoriaId = request.getCategoriaId();
        if (categoriaId == null) {
            throw new IllegalArgumentException("O ID da categoria é obrigatório.");
        }
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com o ID informado."));
        transacao.setCategoria(categoria);
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

    /**
     * Verifica se uma categoria é uma das categorias de meta (Aporte de Meta ou Saque de Meta).
     * Essas categorias são protegidas e suas transações não podem ser atualizadas ou deletadas.
     */
    private boolean isCategoriaMeta(Categoria categoria) {
        if (categoria == null || categoria.getNome() == null) {
            return false;
        }
        String nomeCategoria = categoria.getNome();
        return "Aporte de Meta".equals(nomeCategoria) || "Saque de Meta".equals(nomeCategoria);
    }
}

