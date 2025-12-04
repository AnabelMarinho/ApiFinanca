package ufersa.dev.ApiFinanca.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "preferencias_alerta", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"usuario_id", "tipo_alerta"})
})
public class PreferenciaAlerta {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_alerta", nullable = false)
    private TipoAlerta tipoAlerta;

    @Column(nullable = false)
    private Boolean ativo;

    public PreferenciaAlerta() {
    }

    public PreferenciaAlerta(Usuario usuario, TipoAlerta tipoAlerta, Boolean ativo) {
        this.usuario = usuario;
        this.tipoAlerta = tipoAlerta;
        this.ativo = ativo;
    }
}

