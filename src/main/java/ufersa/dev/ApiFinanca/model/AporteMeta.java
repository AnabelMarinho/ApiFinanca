package ufersa.dev.ApiFinanca.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "aporte_metas")
public class AporteMeta {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private UUID id;

    private BigDecimal valor;
    private LocalDate data;

    @ManyToOne
    @JoinColumn(name = "meta_id", nullable = false)
    private Meta meta;

    public AporteMeta() {
    }

    public AporteMeta(BigDecimal valor, UUID id, LocalDate data, Meta meta) {
        this.valor = valor;
        this.id = id;
        this.data = data;
        this.meta = meta;
    }

    public AporteMeta(BigDecimal valor, Meta meta) {
        this.valor = valor;
        this.meta = meta;
        this.data = LocalDate.now();
    }
}
