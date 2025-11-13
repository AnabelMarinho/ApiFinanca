package ufersa.dev.ApiFinanca.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "usuarios")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Usuario {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private UUID id;

    private String nome;
    private String email;
    private String senha;
    private BigDecimal faixaSalario;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;

    @Column(name = "data_inicio_controle")
    private LocalDate dataInicioControle;

    @Column(name = "primeiro_acesso", nullable = false)
    private Boolean primeiroAcesso = true;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Meta> metas = new ArrayList<>();

    public Usuario() {
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
        this.primeiroAcesso = true;
    }

    public Usuario(UUID id, String nome, String email, String senha, BigDecimal faixaSalario,
                   LocalDateTime dataCriacao, LocalDateTime dataAtualizacao,
                   LocalDate dataInicioControle, Boolean primeiroAcesso, List<Meta> metas) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.faixaSalario = faixaSalario;
        this.dataCriacao = dataCriacao != null ? dataCriacao : LocalDateTime.now();
        this.dataAtualizacao = dataAtualizacao != null ? dataAtualizacao : LocalDateTime.now();
        this.dataInicioControle = dataInicioControle;
        this.primeiroAcesso = primeiroAcesso != null ? primeiroAcesso : true;
        this.metas = metas != null ? metas : new ArrayList<>();
    }

    @PreUpdate
    public void atualizarDataAtualizacao() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void setDataInicioControle(LocalDate dataInicioControle) {
        this.dataInicioControle = dataInicioControle;
        atualizarDataAtualizacao();
    }

    public void setPrimeiroAcesso(Boolean primeiroAcesso) {
        this.primeiroAcesso = primeiroAcesso;
        atualizarDataAtualizacao();
    }
}
