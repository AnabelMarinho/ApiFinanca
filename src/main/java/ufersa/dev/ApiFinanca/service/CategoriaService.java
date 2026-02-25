package ufersa.dev.ApiFinanca.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ufersa.dev.ApiFinanca.dto.CategoriaRequest;
import ufersa.dev.ApiFinanca.model.Categoria;
import ufersa.dev.ApiFinanca.model.TipoTransacao;
import ufersa.dev.ApiFinanca.model.Usuario;
import ufersa.dev.ApiFinanca.repository.CategoriaRepository;
import ufersa.dev.ApiFinanca.repository.TransacaoRecorrenteRepository;
import ufersa.dev.ApiFinanca.repository.TransacaoRepository;
import ufersa.dev.ApiFinanca.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TransacaoRepository transacaoRepository;
    private final TransacaoRecorrenteRepository transacaoRecorrenteRepository;

    public CategoriaService(
            CategoriaRepository categoriaRepository,
            UsuarioRepository usuarioRepository,
            TransacaoRepository transacaoRepository,
            TransacaoRecorrenteRepository transacaoRecorrenteRepository
    ) {
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.transacaoRepository = transacaoRepository;
        this.transacaoRecorrenteRepository = transacaoRecorrenteRepository;
    }

    public List<Categoria> getAll() {
        return categoriaRepository.findAll();
    }

    public Optional<Categoria> getById(UUID id) {
        return categoriaRepository.findById(id);
    }

    public Categoria save(CategoriaRequest request) {
        Categoria categoria = new Categoria();
        applyRequestToEntity(request, categoria);
        return categoriaRepository.save(categoria);
    }

    public Categoria update(UUID id, CategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com o ID informado."));
        applyRequestToEntity(request, categoria);
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void delete(UUID id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com o ID informado."));
        if (categoria.getUser() == null) {
            throw new IllegalArgumentException("Não é permitido excluir categoria padrão.");
        }

        Categoria categoriaPadraoReceita = obterOuCriarCategoriaOutros(TipoTransacao.RECEITA);
        Categoria categoriaPadraoDespesa = obterOuCriarCategoriaOutros(TipoTransacao.DESPESA);

        transacaoRepository.atualizarCategoriaPorTipo(categoria, categoriaPadraoReceita, TipoTransacao.RECEITA);
        transacaoRepository.atualizarCategoriaPorTipo(categoria, categoriaPadraoDespesa, TipoTransacao.DESPESA);
        transacaoRecorrenteRepository.atualizarCategoriaPorTipo(categoria, categoriaPadraoReceita, TipoTransacao.RECEITA);
        transacaoRecorrenteRepository.atualizarCategoriaPorTipo(categoria, categoriaPadraoDespesa, TipoTransacao.DESPESA);
        categoriaRepository.delete(categoria);
    }

    public List<Categoria> getPadroes() {
        return categoriaRepository.findByUserIsNull();
    }

    public List<Categoria> getByUser(Usuario usuario) {
        return categoriaRepository.findByUser(usuario);
    }

    public List<Categoria> getByUserAndTipo(Usuario usuario, TipoTransacao tipo) {
        return categoriaRepository.findByUserAndTipo(usuario, tipo);
    }

    public List<Categoria> getDisponiveisParaUsuario(Usuario usuario) {
        return categoriaRepository.findCategoriasDisponiveisParaUsuario(usuario);
    }

    public List<Categoria> getDisponiveisParaUsuario(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID informado."));
        return getDisponiveisParaUsuario(usuario);
    }

    public List<Categoria> getDisponiveisParaUsuarioPorTipo(Usuario usuario, TipoTransacao tipo) {
        return categoriaRepository.findCategoriasDisponiveisParaUsuarioPorTipo(usuario, tipo);
    }

    public Optional<Categoria> getByNomeTipoEUsuario(String nome, TipoTransacao tipo, Usuario usuario) {
        return categoriaRepository.findByNomeAndTipoAndUser(nome, tipo, usuario);
    }

    private Categoria obterOuCriarCategoriaOutros(TipoTransacao tipo) {
        return categoriaRepository.findByNomeAndTipoAndUserIsNull("Outros", tipo)
                .orElseGet(() -> {
                    Categoria novaCategoria = new Categoria();
                    novaCategoria.setNome("Outros");
                    novaCategoria.setTipo(tipo);
                    novaCategoria.setUser(null);
                    return categoriaRepository.save(novaCategoria);
                });
    }

    private void applyRequestToEntity(CategoriaRequest request, Categoria categoria) {
        if (request.getNome() == null || request.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome da categoria é obrigatório.");
        }
        if (request.getTipo() == null) {
            throw new IllegalArgumentException("O tipo da categoria é obrigatório (RECEITA ou DESPESA).");
        }

        categoria.setNome(request.getNome());
        categoria.setTipo(request.getTipo());

        UUID userId = request.getUserId();
        if (userId == null) {
            categoria.setUser(null);
            return;
        }

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID informado."));
        categoria.setUser(usuario);
    }
}

