package ufersa.dev.ApiFinanca.service;

import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ufersa.dev.ApiFinanca.dto.UsuarioResponse;
import ufersa.dev.ApiFinanca.dto.auth.AuthLoginRequest;
import ufersa.dev.ApiFinanca.dto.auth.AuthRegisterRequest;
import ufersa.dev.ApiFinanca.dto.auth.AuthResponse;
import ufersa.dev.ApiFinanca.model.Usuario;
import ufersa.dev.ApiFinanca.repository.UsuarioRepository;
import ufersa.dev.ApiFinanca.security.JwtService;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(AuthRegisterRequest request) {
        Optional<Usuario> existingUser = usuarioRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Este email já está cadastrado. Tente fazer login ou use outro email.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail());
        usuario.setSenha(passwordEncoder.encode(request.getSenha()));

        Usuario savedUser = usuarioRepository.save(usuario);
        String token = jwtService.generateToken(savedUser.getId().toString());

        return new AuthResponse(token, mapToUsuarioResponse(savedUser),
                savedUser.getPrimeiroAcesso(), savedUser.getDataInicioControle());
    }

    public AuthResponse login(AuthLoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
            );

            // Após autenticação, o authentication.getName() retorna o UUID (do UserDetails)
            UUID userId = UUID.fromString(authentication.getName());
            Usuario usuario = usuarioRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Email ou senha incorretos"));

            String token = jwtService.generateToken(usuario.getId().toString());
            return new AuthResponse(token, mapToUsuarioResponse(usuario),
                    usuario.getPrimeiroAcesso(), usuario.getDataInicioControle());
        } catch (org.springframework.security.authentication.BadCredentialsException ex) {
            throw new IllegalArgumentException("Email ou senha incorretos");
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new IllegalArgumentException("Falha na autenticação: " + ex.getMessage());
        }
    }

    public UsuarioResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("Usuário não autenticado. Faça login novamente.");
        }

        String userIdString = authentication.getName();
        UUID userId;
        try {
            userId = UUID.fromString(userIdString);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Token inválido. Faça login novamente.");
        }
        
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado. Faça login novamente."));

        return mapToUsuarioResponse(usuario);
    }

    private UsuarioResponse mapToUsuarioResponse(Usuario usuario) {
        UsuarioResponse response = new UsuarioResponse();
        response.setId(usuario.getId());
        response.setNome(usuario.getNome());
        response.setEmail(usuario.getEmail());
        response.setFaixaSalario(usuario.getFaixaSalario());
        response.setDataCriacao(usuario.getDataCriacao());
        response.setDataAtualizacao(usuario.getDataAtualizacao());
        response.setDataInicioControle(usuario.getDataInicioControle());
        response.setPrimeiroAcesso(usuario.getPrimeiroAcesso());
        response.setSaldoInicial(usuario.getSaldoInicial());
        return response;
    }

    public Usuario getCurrentUserAsEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("Usuário não autenticado. Faça login novamente.");
        }

        String userIdString = authentication.getName();
        UUID userId;
        try {
            userId = UUID.fromString(userIdString);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Token inválido. Faça login novamente.");
        }

        return usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado. Faça login novamente."));
    }


}

