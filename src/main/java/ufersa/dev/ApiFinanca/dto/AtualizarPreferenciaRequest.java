package ufersa.dev.ApiFinanca.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ufersa.dev.ApiFinanca.model.TipoAlerta;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarPreferenciaRequest {
    
    @NotNull(message = "O tipo de alerta é obrigatório")
    private TipoAlerta tipo;
    
    @NotNull(message = "O status ativo é obrigatório")
    private Boolean ativo;
}

