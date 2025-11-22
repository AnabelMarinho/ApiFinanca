package ufersa.dev.ApiFinanca.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdatePerfilRequest {

    private String nome;

    @Email(message = "Email inválido.")
    private String email;

    private BigDecimal faixaSalario;
}
