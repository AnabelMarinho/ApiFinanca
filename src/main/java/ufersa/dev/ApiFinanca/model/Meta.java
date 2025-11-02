package ufersa.dev.ApiFinanca.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "metas")
public class Meta {

    @Id
    @GeneratedValue
    private UUID id;
    private String nome;
    private BigDecimal valorAlvo;
    private BigDecimal valorAtual;
    private LocalDate dataAlvo;
    private LocalDateTime dataCriacao;
    private UUID userId;

    public Meta(UUID id, String nome, BigDecimal valorAlvo, BigDecimal valorAtual, LocalDate dataAlvo, LocalDateTime dataCriacao, UUID userId) {
        this.id = id;
        this.nome = nome;
        this.valorAlvo = valorAlvo;
        this.valorAtual = valorAtual;
        this.dataAlvo = dataAlvo;
        this.dataCriacao = dataCriacao;
        this.userId = userId;
    }
}
