package ufersa.dev.ApiFinanca.dto;

import lombok.Data;
import ufersa.dev.ApiFinanca.model.Transacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransacaoResponse {

    private UUID id;
    private String tipo;
    private BigDecimal valor;
    private LocalDateTime data;
    private String descricao;
    private String categoria;

    public TransacaoResponse(Transacao t) {
        this.id = t.getId();
        this.valor = t.getValor();
        this.data = t.getData();
        this.descricao = t.getDescricao();
        this.categoria = t.getCategoria().getNome();
        this.tipo = t.getTipo().name();
    }
}


