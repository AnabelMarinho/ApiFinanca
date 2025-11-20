package ufersa.dev.ApiFinanca.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class TransacaoResponse {

    private UUID id;
    private String tipo;
    private BigDecimal valor;
    private LocalDate data;
    private String descricao;
    private String categoria;
}
