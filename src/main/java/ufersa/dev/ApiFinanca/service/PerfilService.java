package ufersa.dev.ApiFinanca.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ufersa.dev.ApiFinanca.dto.UpdatePerfilRequest;
import ufersa.dev.ApiFinanca.dto.UpdateSenhaRequest;
import ufersa.dev.ApiFinanca.model.Usuario;
import ufersa.dev.ApiFinanca.repository.UsuarioRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PerfilService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public Usuario atualizarPerfil(UUID userId, UpdatePerfilRequest request) {

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (request.getNome() != null) {
            usuario.setNome(request.getNome());
        }

        if (request.getEmail() != null) {
            usuario.setEmail(request.getEmail());
        }

        if (request.getFaixaSalario() != null) {
            usuario.setFaixaSalario(request.getFaixaSalario());
        }

        return usuarioRepository.save(usuario);
    }

    public void atualizarSenha(UUID userId, UpdateSenhaRequest request) {

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (!passwordEncoder.matches(request.getSenhaAtual(), usuario.getSenha())) {
            throw new RuntimeException("Senha atual incorreta.");
        }

        usuario.setSenha(passwordEncoder.encode(request.getNovaSenha()));
        usuarioRepository.save(usuario);
    }
}
