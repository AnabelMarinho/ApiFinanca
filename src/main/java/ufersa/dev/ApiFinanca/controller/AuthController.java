package ufersa.dev.ApiFinanca.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ufersa.dev.ApiFinanca.dto.UsuarioResponse;
import ufersa.dev.ApiFinanca.dto.auth.*;
import ufersa.dev.ApiFinanca.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthLoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<UsuarioResponse> getCurrentUser() {
        UsuarioResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/esqueci-senha")
    @Operation(summary = "Solicita recuperação de senha")
    public ResponseEntity<String> esqueciSenha(@Valid @RequestBody EsqueciSenhaRequest request) {
        authService.solicitarRecuperacaoSenha(request);
        return ResponseEntity.ok("Se o email estiver cadastrado, você receberá um código de recuperação.");
    }

    @PostMapping("/validar-codigo")
    @Operation(summary = "Valida código de recuperação de senha")
    public ResponseEntity<ValidarCodigoResponse> validarCodigo(@Valid @RequestBody ValidarCodigoRequest request) {
        ValidarCodigoResponse response = authService.validarCodigoRecuperacao(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/redefinir-senha")
    @Operation(
            summary = "Redefine a senha usando o token de recuperação",
            description = "Use o token retornado pelo endpoint /auth/validar-codigo. Envie o token no campo 'token' do body."
    )
    public ResponseEntity<String> redefinirSenha(@Valid @RequestBody RedefinirSenhaRequest request) {
        
        if (request.getToken() == null || request.getToken().isBlank()) {
            throw new IllegalArgumentException("Token de autorização é obrigatório. Envie o token no campo 'token' do body.");
        }
        
        String token = request.getToken();
        // Remove "Bearer " se presente
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        authService.redefinirSenha(token, request);
        return ResponseEntity.ok("Senha redefinida com sucesso.");
    }
}

