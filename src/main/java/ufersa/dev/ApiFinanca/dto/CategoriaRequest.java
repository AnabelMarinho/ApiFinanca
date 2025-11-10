package ufersa.dev.ApiFinanca.dto;

import ufersa.dev.ApiFinanca.model.TipoTransacao;


public class CategoriaRequest {

    private String nome;
    private TipoTransacao tipo;
    private Long userId;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoTransacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoTransacao tipo) {
        this.tipo = tipo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}


