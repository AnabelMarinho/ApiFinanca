package ufersa.dev.ApiFinanca.dto;

import ufersa.dev.ApiFinanca.model.TipoTransacao;

import java.util.UUID;

public class CategoriaRequest {

    private String nome;
    private TipoTransacao tipo;
    private UUID userId;

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

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}


