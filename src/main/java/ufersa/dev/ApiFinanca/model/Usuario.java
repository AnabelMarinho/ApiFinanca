package ufersa.dev.ApiFinanca.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String email;
    private String senha;
    private BigDecimal faixaSalario;

    public Usuario(Long id, String nome, String email, String senha, BigDecimal faixaSalario) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.faixaSalario = faixaSalario;
    }

}

