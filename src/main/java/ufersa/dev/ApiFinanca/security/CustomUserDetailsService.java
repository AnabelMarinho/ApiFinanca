package ufersa.dev.ApiFinanca.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ufersa.dev.ApiFinanca.model.Usuario;
import ufersa.dev.ApiFinanca.repository.UsuarioRepository;

import java.util.UUID;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario;
        
        // Tenta primeiro como UUID (para validação de token JWT)
        try {
            UUID userId = UUID.fromString(username);
            usuario = usuarioRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado para o ID " + username));
        } catch (IllegalArgumentException ex) {
            // Se não for UUID válido, tenta buscar por email (para login)
            usuario = usuarioRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado para o email " + username));
        }
        
        // Sempre retornamos o UUID como username no UserDetails para manter consistência
        // Isso garante que o token JWT sempre use UUID
        return User.withUsername(usuario.getId().toString())
                .password(usuario.getSenha())
                .authorities("USER")
                .build();
    }
}

