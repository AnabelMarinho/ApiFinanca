package ufersa.dev.ApiFinanca.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import ufersa.dev.ApiFinanca.dto.CategoriaRequest;
import ufersa.dev.ApiFinanca.model.Categoria;
import ufersa.dev.ApiFinanca.model.TipoTransacao;
import ufersa.dev.ApiFinanca.model.Usuario;
import ufersa.dev.ApiFinanca.repository.CategoriaRepository;
import ufersa.dev.ApiFinanca.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, UsuarioRepository usuarioRepository) {
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Categoria> getAll() {
        return categoriaRepository.findAll();
    }

    public Optional<Categoria> getById(Long id) {
        return categoriaRepository.findById(id);
    }

    public Categoria save(CategoriaRequest request) {
        Categoria categoria = new Categoria();
        applyRequestToEntity(request, categoria);
        return categoriaRepository.save(categoria);
    }

    public Categoria update(Long id, CategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada para o id " + id));
        applyRequestToEntity(request, categoria);
        return categoriaRepository.save(categoria);
    }

    public void delete(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new EntityNotFoundException("Categoria não encontrada para o id " + id);
        }
        categoriaRepository.deleteById(id);
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

    public List<Categoria> getDisponiveisParaUsuarioPorTipo(Usuario usuario, TipoTransacao tipo) {
        return categoriaRepository.findCategoriasDisponiveisParaUsuarioPorTipo(usuario, tipo);
    }

    public Optional<Categoria> getByNomeTipoEUsuario(String nome, TipoTransacao tipo, Usuario usuario) {
        return categoriaRepository.findByNomeAndTipoAndUser(nome, tipo, usuario);
    }

    private void applyRequestToEntity(CategoriaRequest request, Categoria categoria) {
        if (request.getNome() == null || request.getNome().isBlank()) {
            throw new IllegalArgumentException("nome é obrigatório");
        }
        if (request.getTipo() == null) {
            throw new IllegalArgumentException("tipo é obrigatório");
        }

        categoria.setNome(request.getNome());
        categoria.setTipo(request.getTipo());

        UUID userId = request.getUserId();
        if (userId == null) {
            categoria.setUser(null);
            return;
        }

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado para o id " + userId));
        categoria.setUser(usuario);
    }
}

