package ufersa.dev.ApiFinanca.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdatePerfilRequest {

    @NotBlank(message = "O nome não pode ser vazio.")
    private String nome;

    @NotBlank(message = "O email não pode ser vazio.")
    @Email(message ="Email inválido.")
    private String email;

    @NotBlank(message = "A faixa salarial não pode ser nula.")
    private BigDecimal faixaSalario;
}
