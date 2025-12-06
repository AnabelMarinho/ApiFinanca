package ufersa.dev.ApiFinanca.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class RelatorioMensalResponse {

    private BigDecimal totalReceitas;
    private BigDecimal totalDespesas;
    private BigDecimal saldo;
    private List<TransacaoResponse> transacoes;
    private List<GastoCategoria> gastosPorCategoria;

}
