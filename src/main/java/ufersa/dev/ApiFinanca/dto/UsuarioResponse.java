package ufersa.dev.ApiFinanca.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class UsuarioResponse {
    private Long id;
    private String nome;
    private String email;
    private BigDecimal faixaSalario;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
}
