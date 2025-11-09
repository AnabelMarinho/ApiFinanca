package ufersa.dev.ApiFinanca.dto;

import ufersa.dev.ApiFinanca.model.TipoTransacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class TransacaoRequest {

    private TipoTransacao tipo;
    private BigDecimal valor;
    private LocalDate data;
    private String descricao;
    private UUID userId;
    private Long categoriaId;

    public TipoTransacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoTransacao tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }
}


