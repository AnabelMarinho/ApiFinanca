package ufersa.dev.ApiFinanca.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "transacao_recorrente_execucoes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"transacao_recorrente_id", "data_execucao"}))
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TransacaoRecorrenteExecucao {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transacao_recorrente_id", nullable = false)
    private TransacaoRecorrente transacaoRecorrente;

    @Column(name = "data_execucao", nullable = false)
    private LocalDate dataExecucao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransacaoRecorrenteExecucaoStatus status;

    @Column(name = "mensagem_erro", length = 1024)
    private String mensagemErro;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    public void onCreate() {
        this.criadoEm = LocalDateTime.now();
    }
}


