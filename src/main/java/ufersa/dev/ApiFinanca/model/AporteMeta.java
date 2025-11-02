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
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private BigDecimal valor;
    private LocalDate data;

    @ManyToOne
    @JoinColumn(name = "meta_id", nullable = false)
    private Meta meta;

    public AporteMeta(UUID id, BigDecimal valor, LocalDate data) {
        this.id = id;
        this.valor = valor;
        this.data = data;
    }

    public AporteMeta() {
        this.id = UUID.randomUUID();
    }

}
