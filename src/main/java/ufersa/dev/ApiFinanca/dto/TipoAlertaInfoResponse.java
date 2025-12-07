package ufersa.dev.ApiFinanca.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ufersa.dev.ApiFinanca.model.TipoAlerta;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoAlertaInfoResponse {
    private TipoAlerta tipo;
    private String nome;
    private String descricao;
    private Boolean ativo;
    private Boolean ativoPorPadrao;
}

