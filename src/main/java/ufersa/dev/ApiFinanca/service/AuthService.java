package ufersa.dev.ApiFinanca.service;

import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ufersa.dev.ApiFinanca.dto.UsuarioResponse;
import ufersa.dev.ApiFinanca.dto.auth.*;
import ufersa.dev.ApiFinanca.model.RecuperacaoSenha;
import ufersa.dev.ApiFinanca.model.Usuario;
import ufersa.dev.ApiFinanca.repository.RecuperacaoSenhaRepository;
import ufersa.dev.ApiFinanca.repository.UsuarioRepository;
import ufersa.dev.ApiFinanca.security.JwtService;
import ufersa.dev.ApiFinanca.service.TransacaoRecorrenteService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TransacaoRecorrenteService transacaoRecorrenteService;
    private final EmailService emailService;
    private final RecuperacaoSenhaRepository recuperacaoSenhaRepository;
    private final Random random = new Random();

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            TransacaoRecorrenteService transacaoRecorrenteService,
            EmailService emailService,
            RecuperacaoSenhaRepository recuperacaoSenhaRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.transacaoRecorrenteService = transacaoRecorrenteService;
        this.emailService = emailService;
        this.recuperacaoSenhaRepository = recuperacaoSenhaRepository;
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

            // Verifica e processa transações recorrentes pendentes do usuário para o dia atual
            try {
                transacaoRecorrenteService.processarRecorrenciasPendentesDoUsuario(usuario);
            } catch (Exception ex) {
                // Log do erro mas não impede o login
                // A transação recorrente será processada no próximo scheduler
            }

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

    @Transactional
    public void solicitarRecuperacaoSenha(EsqueciSenhaRequest request) {
        // Verifica se o email existe (mas não revela se existe ou não por segurança)
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(request.getEmail());
        
        // Sempre gera um código, mesmo se o email não existir (por segurança)
        String codigo = gerarCodigoRecuperacao();
        
        // Remove códigos anteriores para este email
        recuperacaoSenhaRepository.deleteByEmail(request.getEmail());
        
        // Cria novo código de recuperação
        RecuperacaoSenha recuperacaoSenha = new RecuperacaoSenha(request.getEmail(), codigo);
        recuperacaoSenhaRepository.save(recuperacaoSenha);
        
        // Envia email apenas se o usuário existir
        if (usuarioOpt.isPresent()) {
            String mensagem = String.format(
                "Olá,\n\n" +
                "Recebemos uma solicitação de recuperação de senha para sua conta.\n\n" +
                "Seu código de recuperação é: %s\n\n" +
                "Este código expira em 5 minutos.\n\n" +
                "Se você não solicitou esta recuperação de senha, pode ignorar este email com segurança.\n\n" +
                "Atenciosamente,\n" +
                "Equipe Finança+",
                codigo
            );
            
            emailService.enviarEmail(
                request.getEmail(),
                "Recuperação de senha",
                mensagem
            );
        }
        // Se o email não existir, não envia nada (por segurança, não revela se o email existe)
    }

    public ValidarCodigoResponse validarCodigoRecuperacao(ValidarCodigoRequest request) {
        Optional<RecuperacaoSenha> recuperacaoOpt = recuperacaoSenhaRepository
                .findByEmailAndCodigoAndUsadoFalse(request.getEmail(), request.getCodigo());
        
        if (recuperacaoOpt.isEmpty()) {
            throw new IllegalArgumentException("Código inválido ou expirado.");
        }
        
        RecuperacaoSenha recuperacao = recuperacaoOpt.get();
        
        if (!recuperacao.isValid()) {
            throw new IllegalArgumentException("Código inválido ou expirado.");
        }
        
        // Marca o código como usado
        recuperacao.setUsado(true);
        recuperacaoSenhaRepository.save(recuperacao);
        
        // Busca o usuário pelo email
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        
        // Gera um token JWT para redefinição de senha
        String token = jwtService.generateToken(usuario.getId().toString());
        
        return new ValidarCodigoResponse(token, "Código validado com sucesso.");
    }

    @Transactional
    public void redefinirSenha(String token, RedefinirSenhaRequest request) {
        // Valida e extrai o userId do token
        String userIdString;
        try {
            userIdString = jwtService.extractUsername(token);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Token inválido ou expirado.");
        }
        
        UUID userId;
        try {
            userId = UUID.fromString(userIdString);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Token inválido.");
        }
        
        // Valida se o token é válido
        if (!jwtService.isTokenValid(token, userIdString)) {
            throw new IllegalArgumentException("Token inválido ou expirado.");
        }
        
        // Busca o usuário
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        
        // Atualiza a senha
        usuario.setSenha(passwordEncoder.encode(request.getNovaSenha()));
        usuario.setDataAtualizacao(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    private String gerarCodigoRecuperacao() {
        // Gera código de 5 dígitos (1-9, podem se repetir)
        StringBuilder codigo = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            codigo.append(random.nextInt(9) + 1); // 1 a 9
        }
        return codigo.toString();
    }

}

