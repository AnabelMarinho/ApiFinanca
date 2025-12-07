package ufersa.dev.ApiFinanca.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MetaDashboard {
    private String nome;
    private BigDecimal porcentagem;

    public MetaDashboard(String nome, BigDecimal porcentagem) {
        this.nome = nome;
        this.porcentagem = porcentagem;
    }
}

