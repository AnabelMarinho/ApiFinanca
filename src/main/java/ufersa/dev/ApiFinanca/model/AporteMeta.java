package ufersa.dev.ApiFinanca.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    @GeneratedValue
    private UUID id;
    private BigDecimal valor;
    private LocalDate data;
    private UUID metaId;

    public AporteMeta(UUID id, BigDecimal valor, LocalDate data, UUID metaId) {
        this.id = id;
        this.valor = valor;
        this.data = data;
        this.metaId = metaId;
    }
}
