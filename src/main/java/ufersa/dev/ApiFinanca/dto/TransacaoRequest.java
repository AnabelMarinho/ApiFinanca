package ufersa.dev.ApiFinanca.dto;

import ufersa.dev.ApiFinanca.model.TipoTransacao;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransacaoRequest {

    private TipoTransacao tipo;
    private BigDecimal valor;
    private LocalDate data;
    private String descricao;
    private Long userId;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }
}


