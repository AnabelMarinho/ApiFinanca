package ufersa.dev.ApiFinanca.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String nome;
    private BigDecimal valorAlvo;
    private BigDecimal valorAtual;
    private LocalDate dataAlvo;
    private LocalDateTime dataCriacao;
    private UUID userId;

    @OneToMany(mappedBy = "meta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AporteMeta> aporteMetas = new ArrayList<>();

    public Meta() {
        this.id = UUID.randomUUID();
        this.valorAtual = BigDecimal.ZERO;
        this.dataCriacao = LocalDateTime.now();
        this.aporteMetas = new ArrayList<>();
    }

    public Meta(String nome, BigDecimal valorAlvo, LocalDate dataAlvo, UUID userId) {
        this();
        this.nome = nome;
        this.valorAlvo = valorAlvo;
        this.dataAlvo = dataAlvo;
        this.userId = userId;
    }
}
