package ufersa.dev.ApiFinanca.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "alertas", indexes = {
        @Index(name = "idx_alertas_usuario_visto", columnList = "usuario_id, visto"),
        @Index(name = "idx_alertas_usuario_tipo", columnList = "usuario_id, tipo_alerta"),
        @Index(name = "idx_alertas_data_criacao", columnList = "data_criacao")
})
public class Alerta {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_alerta", nullable = false)
    private TipoAlerta tipoAlerta;

    @Column(nullable = false, length = 1000)
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeveridadeAlerta severidade;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(nullable = false)
    private Boolean visto = false;

    @Column(name = "data_visto")
    private LocalDateTime dataVisto;

    // Metadados opcionais para controle de recorrência
    @Column(name = "meta_id")
    private UUID metaId; // Para alertas relacionados a metas específicas

    @Column(name = "categoria_id")
    private UUID categoriaId; // Para alertas relacionados a categorias específicas

    @Column(name = "referencia_periodo")
    private String referenciaPeriodo; // Ex: "2024-12" para identificar o mês

    public Alerta() {
        this.dataCriacao = LocalDateTime.now();
        this.visto = false;
    }

    public Alerta(Usuario usuario, TipoAlerta tipoAlerta, String mensagem, SeveridadeAlerta severidade) {
        this();
        this.usuario = usuario;
        this.tipoAlerta = tipoAlerta;
        this.mensagem = mensagem;
        this.severidade = severidade;
    }

    public void marcarComoVisto() {
        this.visto = true;
        this.dataVisto = LocalDateTime.now();
    }
}

