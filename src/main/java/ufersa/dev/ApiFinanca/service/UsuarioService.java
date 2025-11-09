package ufersa.dev.ApiFinanca.service;

import org.springframework.stereotype.Service;
import ufersa.dev.ApiFinanca.dto.UsuarioRequest;
import ufersa.dev.ApiFinanca.model.Usuario;
import ufersa.dev.ApiFinanca.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Listar
    public List<Usuario> getAll() {
        return usuarioRepository.findAll();
    }

    // Criar
    public Usuario save(UsuarioRequest request) {
        if (request.getNome() == null || request.getNome().isBlank()) {
            throw new IllegalArgumentException("nome é obrigatório");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("email é obrigatório");
        }
        if (request.getSenha() == null || request.getSenha().isBlank()) {
            throw new IllegalArgumentException("senha é obrigatória");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail());
        usuario.setSenha(request.getSenha());
        usuario.setFaixaSalario(request.getFaixaSalario());
        usuario.setDataCriacao(LocalDateTime.now());
        usuario.setDataAtualizacao(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

}
