package ufersa.dev.ApiFinanca.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ufersa.dev.ApiFinanca.dto.UsuarioResponse;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UsuarioResponse usuario;
    private Boolean primeiroAcesso;
    private LocalDate dataInicioControle;
}

