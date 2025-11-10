package ufersa.dev.ApiFinanca.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "aporte_metas")
public class AporteMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal valor;
    private LocalDate data;

    @ManyToOne
    @JoinColumn(name = "meta_id", nullable = false)
    private Meta meta;

    public AporteMeta(BigDecimal valor, Long id, LocalDate data, Meta meta) {
        this.valor = valor;
        this.id = id;
        this.data = data;
        this.meta = meta;
    }
    public AporteMeta() {
    }

}
