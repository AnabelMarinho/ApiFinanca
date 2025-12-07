package ufersa.dev.ApiFinanca.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class DashboardResponse {
    private BigDecimal saldoAtual;
    private BigDecimal totalReceitas;
    private BigDecimal totalDespesas;
    private List<TransacaoResponse> transacoesRecentes;
    private List<GastoCategoria> gastosPorCategoria;
    private List<MetaDashboard> metas;
}
