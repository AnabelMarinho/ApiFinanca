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
import ufersa.dev.ApiFinanca.dto.auth.AuthLoginRequest;
import ufersa.dev.ApiFinanca.dto.auth.AuthRegisterRequest;
import ufersa.dev.ApiFinanca.dto.auth.AuthResponse;
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
}

