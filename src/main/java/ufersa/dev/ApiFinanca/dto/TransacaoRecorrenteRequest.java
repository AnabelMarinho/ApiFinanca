package ufersa.dev.ApiFinanca.dto;

import ufersa.dev.ApiFinanca.model.TipoTransacao;

import java.math.BigDecimal;

public class TransacaoRecorrenteRequest {

    private TipoTransacao tipo;
    private BigDecimal valor;
    private String descricao;
    private Integer diaRecorrencia;
    private Boolean ativa;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getDiaRecorrencia() {
        return diaRecorrencia;
    }

    public void setDiaRecorrencia(Integer diaRecorrencia) {
        this.diaRecorrencia = diaRecorrencia;
    }

    public Boolean getAtiva() {
        return ativa;
    }

    public void setAtiva(Boolean ativa) {
        this.ativa = ativa;
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


