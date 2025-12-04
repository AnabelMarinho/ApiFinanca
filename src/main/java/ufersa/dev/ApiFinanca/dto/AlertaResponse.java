package ufersa.dev.ApiFinanca.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ufersa.dev.ApiFinanca.model.Alerta;
import ufersa.dev.ApiFinanca.model.TipoAlerta;
import ufersa.dev.ApiFinanca.model.SeveridadeAlerta;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertaResponse {
    private UUID id;
    private TipoAlerta tipo;
    private String mensagem;
    private SeveridadeAlerta severidade;
    private LocalDateTime dataCriacao;
    private Boolean visto;
    private LocalDateTime dataVisto;
    
    // Construtor para criar a partir da entidade
    public static AlertaResponse fromEntity(Alerta alerta) {
        AlertaResponse response = new AlertaResponse();
        response.setId(alerta.getId());
        response.setTipo(alerta.getTipoAlerta());
        response.setMensagem(alerta.getMensagem());
        response.setSeveridade(alerta.getSeveridade());
        response.setDataCriacao(alerta.getDataCriacao());
        response.setVisto(alerta.getVisto());
        response.setDataVisto(alerta.getDataVisto());
        return response;
    }
    
    // Construtor legado (sem ID) para compatibilidade
    public AlertaResponse(TipoAlerta tipo, String mensagem, SeveridadeAlerta severidade) {
        this.tipo = tipo;
        this.mensagem = mensagem;
        this.severidade = severidade;
    }
}

