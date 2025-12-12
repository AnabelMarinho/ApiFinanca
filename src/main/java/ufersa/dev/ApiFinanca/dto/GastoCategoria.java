package ufersa.dev.ApiFinanca.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class GastoCategoria {
    private String categoria;
    private BigDecimal valor;
    private BigDecimal porcentagem;

    public GastoCategoria(String key, BigDecimal value) {
        this.categoria = key;
        this.valor = value;
    }
}
