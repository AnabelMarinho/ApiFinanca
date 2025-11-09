package ufersa.dev.ApiFinanca.dto;

import java.math.BigDecimal;

public class UsuarioRequest {

    private String nome;
    private String email;
    private String senha;
    private BigDecimal faixaSalario;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public BigDecimal getFaixaSalario() {
        return faixaSalario;
    }

    public void setFaixaSalario(BigDecimal faixaSalario) {
        this.faixaSalario = faixaSalario;
    }
}


