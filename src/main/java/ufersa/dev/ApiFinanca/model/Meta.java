package ufersa.dev.ApiFinanca.model;

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
@Table(name = "metas")
public class Meta {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private UUID id;

    private String nome;
    private BigDecimal valorAlvo;
    private BigDecimal valorAtual;
    private LocalDate dataAlvo;
    private LocalDateTime dataCriacao;

    @OneToMany(mappedBy = "meta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AporteMeta> aporteMetas = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;


    public Meta() {
        this.valorAtual = BigDecimal.ZERO;
        this.dataCriacao = LocalDateTime.now();
        this.aporteMetas = new ArrayList<>();
    }

    public Meta(String nome, BigDecimal valorAlvo, LocalDate dataAlvo, Usuario usuario) {
        this();
        this.nome = nome;
        this.valorAlvo = valorAlvo;
        this.dataAlvo = dataAlvo;
        this.usuario = usuario;
    }

}
