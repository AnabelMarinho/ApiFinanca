package ufersa.dev.ApiFinanca.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ufersa.dev.ApiFinanca.model.TipoTransacao;

import java.math.BigDecimal;

@Getter
@Setter
public class TransacaoRecorrenteOnboardingRequest {

    @NotNull
    private TipoTransacao tipo;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal valor;

    private String descricao;

    @NotNull
    @Min(1)
    @Max(31)
    private Integer diaRecorrencia;

    private Long categoriaId;
}
