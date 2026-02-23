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
    private BigDecimal economia;
    private BigDecimal projecao;
    private List<TendenciaDashboard> tendencia;
    private List<TransacaoResponse> transacoesRecentes;
    private List<GastoCategoria> gastosPorCategoria;
    private List<MetaDashboard> metas;
    private InsightsDashboard insights;

    @Getter
    @Setter
    public static class TendenciaDashboard {
        private String mes;
        private BigDecimal receitas;
        private BigDecimal despesas;
    }

    @Getter
    @Setter
    public static class InsightsDashboard {
        private MaiorGastoPeriodo maiorGastoPeriodo;
        private CategoriaMaiorCrescimento categoriaMaiorCrescimento;
        private MetaMaisProximaDeConcluir metaMaisProximaDeConcluir;
    }

    @Getter
    @Setter
    public static class MaiorGastoPeriodo {
        private String categoria;
        private BigDecimal valor;
    }

    @Getter
    @Setter
    public static class CategoriaMaiorCrescimento {
        private String categoria;
        private BigDecimal percentualCrescimento;
    }

    @Getter
    @Setter
    public static class MetaMaisProximaDeConcluir {
        private String nome;
        private BigDecimal porcentagem;
    }
}
