package ufersa.dev.ApiFinanca.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ufersa.dev.ApiFinanca.model.TipoAlerta;
import ufersa.dev.ApiFinanca.model.SeveridadeAlerta;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertaResponse {
    private TipoAlerta tipo;
    private String mensagem;
    private SeveridadeAlerta severidade;
}

