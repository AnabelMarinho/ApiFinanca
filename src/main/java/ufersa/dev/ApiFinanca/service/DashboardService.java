package ufersa.dev.ApiFinanca.service;

import org.springframework.stereotype.Service;
import ufersa.dev.ApiFinanca.dto.DashboardResponse;
import ufersa.dev.ApiFinanca.dto.GastoCategoria;
import ufersa.dev.ApiFinanca.dto.MetaDashboard;
import ufersa.dev.ApiFinanca.dto.TransacaoResponse;
import ufersa.dev.ApiFinanca.enums.PeriodoDashboard;
import ufersa.dev.ApiFinanca.model.*;
import ufersa.dev.ApiFinanca.repository.ConfiguracaoUsuarioRepository;
import ufersa.dev.ApiFinanca.repository.MetaRepository;
import ufersa.dev.ApiFinanca.repository.TransacaoRepository;
import ufersa.dev.ApiFinanca.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final TransacaoRepository transacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConfiguracaoUsuarioRepository configuracaoUsuarioRepository;
    private final MetaRepository metaRepository;

    public DashboardService(TransacaoRepository transacaoRepository,
                            UsuarioRepository usuarioRepository,
                            ConfiguracaoUsuarioRepository configuracaoUsuarioRepository,
                            MetaRepository metaRepository) {
        this.transacaoRepository = transacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.configuracaoUsuarioRepository = configuracaoUsuarioRepository;
        this.metaRepository = metaRepository;
    }

    public DashboardResponse getDashboard(UUID userId, PeriodoDashboard periodo) {

        Usuario user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com o ID informado."));

        // Calcula o período baseado no enum
        LocalDate[] periodoCalculado = calcularPeriodo(userId, periodo);
        LocalDate inicio = periodoCalculado[0];
        LocalDate fim = periodoCalculado[1];

        // Calcula totais do período
        final BigDecimal totalReceitas;
        final BigDecimal totalDespesas;

        if (inicio != null && fim != null) {
            totalReceitas = transacaoRepository
                    .calcularTotalPorTipoEPeriodo(user, TipoTransacao.RECEITA, inicio, fim);
            totalDespesas = transacaoRepository
                    .calcularTotalPorTipoEPeriodo(user, TipoTransacao.DESPESA, inicio, fim);
        } else {
            // Para TODA_UTILIZACAO, busca sem filtro de data
            totalReceitas = transacaoRepository.calcularTotalReceitas(user, TipoTransacao.RECEITA);
            totalDespesas = transacaoRepository.calcularTotalDespesas(user, TipoTransacao.DESPESA);
        }

        // Saldo atual sempre é o mesmo, independente do período
        BigDecimal saldoAtual = user.getSaldoAtual() != null ? user.getSaldoAtual() : BigDecimal.ZERO;
        BigDecimal economia = totalReceitas.subtract(totalDespesas);
        BigDecimal projecao = calcularProjecao(periodo, inicio, fim, economia, saldoAtual);

        // Busca transações do período
        List<Transacao> transacoesPeriodo;
        if (inicio != null && fim != null) {
            transacoesPeriodo = transacaoRepository.findByUserAndDataBetween(user, inicio, fim);
        } else {
            transacoesPeriodo = transacaoRepository.findByUserOrderByDataDesc(user);
        }

        // Ordena por data decrescente e pega as 10 mais recentes
        List<TransacaoResponse> transacoesRecentes = transacoesPeriodo.stream()
                .sorted((t1, t2) -> t2.getData().compareTo(t1.getData()))
                .limit(10)
                .map(t -> {
                    TransacaoResponse response = new TransacaoResponse(t);
                    // Calcula porcentagem baseada no tipo da transação
                    BigDecimal total = t.getTipo() == TipoTransacao.RECEITA ? totalReceitas : totalDespesas;
                    BigDecimal porcentagem = calcularPorcentagem(t.getValor(), total);
                    response.setPorcentagem(porcentagem);
                    return response;
                })
                .toList();

        // Busca gastos por categoria do período
        List<Transacao> despesasPeriodo;
        if (inicio != null && fim != null) {
            despesasPeriodo = transacaoRepository
                    .findByUserAndTipoAndDataBetween(user, TipoTransacao.DESPESA, inicio, fim);
        } else {
            despesasPeriodo = transacaoRepository.findByUserAndTipo(user, TipoTransacao.DESPESA);
        }

        List<GastoCategoria> gastosPorCategoria = despesasPeriodo.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategoria().getNome(),
                        Collectors.reducing(BigDecimal.ZERO, Transacao::getValor, BigDecimal::add)
                ))
                .entrySet()
                .stream()
                .map(e -> {
                    GastoCategoria gasto = new GastoCategoria(e.getKey(), e.getValue());
                    BigDecimal porcentagem = calcularPorcentagem(e.getValue(), totalDespesas);
                    gasto.setPorcentagem(porcentagem);
                    return gasto;
                })
                .toList();

        List<DashboardResponse.TendenciaDashboard> tendencia = calcularTendencia(transacoesPeriodo, inicio, fim);

        // Busca metas do período
        List<Meta> todasMetas = metaRepository.findByUsuarioId(userId);
        List<MetaDashboard> metas = todasMetas.stream()
                .filter(meta -> {
                    if (inicio == null || fim == null) {
                        // Para TODA_UTILIZACAO, inclui todas as metas
                        return true;
                    }
                    // Verifica se dataCriacao ou dataAlvo estão dentro do período
                    LocalDate dataCriacao = meta.getDataCriacao() != null 
                            ? meta.getDataCriacao().toLocalDate() 
                            : null;
                    LocalDate dataAlvo = meta.getDataAlvo();
                    
                    boolean dataCriacaoNoPeriodo = dataCriacao != null 
                            && !dataCriacao.isBefore(inicio) 
                            && !dataCriacao.isAfter(fim);
                    boolean dataAlvoNoPeriodo = dataAlvo != null 
                            && !dataAlvo.isBefore(inicio) 
                            && !dataAlvo.isAfter(fim);
                    
                    return dataCriacaoNoPeriodo || dataAlvoNoPeriodo;
                })
                .map(meta -> {
                    BigDecimal porcentagem = calcularPorcentagem(
                            meta.getValorAtual() != null ? meta.getValorAtual() : BigDecimal.ZERO,
                            meta.getValorAlvo() != null ? meta.getValorAlvo() : BigDecimal.ONE
                    );
                    return new MetaDashboard(meta.getNome(), porcentagem);
                })
                .toList();

        DashboardResponse.InsightsDashboard insights = new DashboardResponse.InsightsDashboard();
        insights.setMaiorGastoPeriodo(calcularMaiorGastoPeriodo(gastosPorCategoria));
        insights.setCategoriaMaiorCrescimento(calcularCategoriaMaiorCrescimento(periodo, user, inicio, fim, despesasPeriodo));
        insights.setMetaMaisProximaDeConcluir(calcularMetaMaisProxima(metas));

        DashboardResponse response = new DashboardResponse();
        response.setSaldoAtual(saldoAtual);
        response.setTotalReceitas(totalReceitas);
        response.setTotalDespesas(totalDespesas);
        response.setEconomia(economia);
        response.setProjecao(projecao);
        response.setTendencia(tendencia);
        response.setTransacoesRecentes(transacoesRecentes);
        response.setGastosPorCategoria(gastosPorCategoria);
        response.setMetas(metas);
        response.setInsights(insights);

        return response;
    }

    private LocalDate[] calcularPeriodo(UUID userId, PeriodoDashboard periodo) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = null;
        LocalDate fim = null;

        switch (periodo) {
            case MES_ATUAL:
                int diaVirada = configuracaoUsuarioRepository.findByUsuario_Id(userId)
                        .map(ConfiguracaoUsuario::getDiaViradaMes)
                        .orElse(1);
                
                if (hoje.getDayOfMonth() >= diaVirada) {
                    inicio = LocalDate.of(hoje.getYear(), hoje.getMonth(), diaVirada);
                    fim = inicio.plusMonths(1).minusDays(1);
                } else {
                    LocalDate mesAnterior = hoje.minusMonths(1);
                    inicio = LocalDate.of(mesAnterior.getYear(), mesAnterior.getMonth(), diaVirada);
                    fim = inicio.plusMonths(1).minusDays(1);
                }
                break;
            case ULTIMOS_3_MESES:
                fim = hoje;
                inicio = hoje.minusMonths(3);
                break;
            case ULTIMOS_6_MESES:
                fim = hoje;
                inicio = hoje.minusMonths(6);
                break;
            case ULTIMOS_12_MESES:
                fim = hoje;
                inicio = hoje.minusMonths(12);
                break;
            case TODA_UTILIZACAO:
                // inicio e fim permanecem null para indicar que não há filtro de período
                break;
        }

        return new LocalDate[]{inicio, fim};
    }

    private BigDecimal calcularPorcentagem(BigDecimal valor, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (valor == null) {
            return BigDecimal.ZERO;
        }
        return valor.multiply(new BigDecimal("100"))
                .divide(total, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularProjecao(PeriodoDashboard periodo, LocalDate inicio, LocalDate fim, BigDecimal economia, BigDecimal saldoAtual) {
        if (periodo != PeriodoDashboard.MES_ATUAL && periodo != PeriodoDashboard.ULTIMOS_3_MESES) {
            return saldoAtual;
        }
        if (inicio == null || fim == null) {
            return saldoAtual;
        }
        if (economia == null || economia.compareTo(BigDecimal.ZERO) == 0) {
            return saldoAtual;
        }
        long diasDecorridos = ChronoUnit.DAYS.between(inicio, LocalDate.now()) + 1;
        long totalDias = ChronoUnit.DAYS.between(inicio, fim) + 1;
        if (diasDecorridos <= 0 || totalDias <= 0) {
            return saldoAtual;
        }
        BigDecimal mediaDiaria = economia.divide(BigDecimal.valueOf(diasDecorridos), 6, RoundingMode.HALF_UP);
        return mediaDiaria.multiply(BigDecimal.valueOf(totalDias)).setScale(2, RoundingMode.HALF_UP);
    }

    private List<DashboardResponse.TendenciaDashboard> calcularTendencia(List<Transacao> transacoesPeriodo, LocalDate inicio, LocalDate fim) {
        if (transacoesPeriodo.isEmpty() && (inicio == null || fim == null)) {
            return List.of();
        }

        LocalDate inicioPeriodo = inicio;
        LocalDate fimPeriodo = fim;

        if (inicioPeriodo == null || fimPeriodo == null) {
            Optional<LocalDate> minData = transacoesPeriodo.stream()
                    .map(t -> t.getData().toLocalDate())
                    .min(LocalDate::compareTo);
            Optional<LocalDate> maxData = transacoesPeriodo.stream()
                    .map(t -> t.getData().toLocalDate())
                    .max(LocalDate::compareTo);

            if (minData.isEmpty() || maxData.isEmpty()) {
                return List.of();
            }

            inicioPeriodo = minData.get();
            fimPeriodo = maxData.get();
        }

        Map<YearMonth, BigDecimal> receitasPorMes = new HashMap<>();
        Map<YearMonth, BigDecimal> despesasPorMes = new HashMap<>();

        for (Transacao transacao : transacoesPeriodo) {
            YearMonth mes = YearMonth.from(transacao.getData());
            if (transacao.getTipo() == TipoTransacao.RECEITA) {
                receitasPorMes.merge(mes, transacao.getValor(), BigDecimal::add);
            } else if (transacao.getTipo() == TipoTransacao.DESPESA) {
                despesasPorMes.merge(mes, transacao.getValor(), BigDecimal::add);
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
        List<DashboardResponse.TendenciaDashboard> tendencia = new ArrayList<>();
        YearMonth atual = YearMonth.from(inicioPeriodo);
        YearMonth fimMes = YearMonth.from(fimPeriodo);

        while (!atual.isAfter(fimMes)) {
            DashboardResponse.TendenciaDashboard item = new DashboardResponse.TendenciaDashboard();
            item.setMes(atual.format(formatter));
            item.setReceitas(receitasPorMes.getOrDefault(atual, BigDecimal.ZERO));
            item.setDespesas(despesasPorMes.getOrDefault(atual, BigDecimal.ZERO));
            tendencia.add(item);
            atual = atual.plusMonths(1);
        }

        return tendencia;
    }

    private DashboardResponse.MaiorGastoPeriodo calcularMaiorGastoPeriodo(List<GastoCategoria> gastosPorCategoria) {
        DashboardResponse.MaiorGastoPeriodo maiorGasto = new DashboardResponse.MaiorGastoPeriodo();
        if (gastosPorCategoria == null || gastosPorCategoria.isEmpty()) {
            maiorGasto.setCategoria("");
            maiorGasto.setValor(BigDecimal.ZERO);
            return maiorGasto;
        }

        GastoCategoria maior = gastosPorCategoria.stream()
                .max(Comparator.comparing(GastoCategoria::getValor))
                .orElse(null);

        if (maior == null) {
            maiorGasto.setCategoria("");
            maiorGasto.setValor(BigDecimal.ZERO);
            return maiorGasto;
        }

        maiorGasto.setCategoria(maior.getCategoria());
        maiorGasto.setValor(maior.getValor());
        return maiorGasto;
    }

    private DashboardResponse.CategoriaMaiorCrescimento calcularCategoriaMaiorCrescimento(
            PeriodoDashboard periodo,
            Usuario user,
            LocalDate inicio,
            LocalDate fim,
            List<Transacao> despesasPeriodo
    ) {
        DashboardResponse.CategoriaMaiorCrescimento resultado = new DashboardResponse.CategoriaMaiorCrescimento();
        resultado.setCategoria("");
        resultado.setPercentualCrescimento(BigDecimal.ZERO);

        if (periodo == PeriodoDashboard.TODA_UTILIZACAO || inicio == null || fim == null) {
            return resultado;
        }

        Map<String, BigDecimal> atualPorCategoria = despesasPeriodo.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategoria().getNome(),
                        Collectors.reducing(BigDecimal.ZERO, Transacao::getValor, BigDecimal::add)
                ));

        long totalDias = ChronoUnit.DAYS.between(inicio, fim) + 1;
        LocalDate fimAnterior = inicio.minusDays(1);
        LocalDate inicioAnterior = fimAnterior.minusDays(totalDias - 1);

        List<Transacao> despesasPeriodoAnterior = transacaoRepository
                .findByUserAndTipoAndDataBetween(user, TipoTransacao.DESPESA, inicioAnterior, fimAnterior);

        Map<String, BigDecimal> anteriorPorCategoria = despesasPeriodoAnterior.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategoria().getNome(),
                        Collectors.reducing(BigDecimal.ZERO, Transacao::getValor, BigDecimal::add)
                ));

        BigDecimal maiorPercentual = BigDecimal.ZERO;
        String categoriaMaior = "";

        for (Map.Entry<String, BigDecimal> entry : atualPorCategoria.entrySet()) {
            BigDecimal anterior = anteriorPorCategoria.get(entry.getKey());
            if (anterior == null || anterior.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            BigDecimal crescimento = entry.getValue().subtract(anterior)
                    .divide(anterior, 6, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));

            if (crescimento.compareTo(maiorPercentual) > 0) {
                maiorPercentual = crescimento;
                categoriaMaior = entry.getKey();
            }
        }

        resultado.setCategoria(categoriaMaior);
        resultado.setPercentualCrescimento(maiorPercentual.setScale(2, RoundingMode.HALF_UP));
        return resultado;
    }

    private DashboardResponse.MetaMaisProximaDeConcluir calcularMetaMaisProxima(List<MetaDashboard> metas) {
        if (metas == null || metas.isEmpty()) {
            return null;
        }

        BigDecimal limiteConcluida = new BigDecimal("100");

        Optional<MetaDashboard> metaMaisProxima = metas.stream()
                .filter(meta -> meta.getPorcentagem() != null && meta.getPorcentagem().compareTo(limiteConcluida) < 0)
                .max(Comparator.comparing(MetaDashboard::getPorcentagem));

        if (metaMaisProxima.isEmpty()) {
            return null;
        }

        DashboardResponse.MetaMaisProximaDeConcluir resultado = new DashboardResponse.MetaMaisProximaDeConcluir();
        resultado.setNome(metaMaisProxima.get().getNome());
        resultado.setPorcentagem(metaMaisProxima.get().getPorcentagem());
        return resultado;
    }
}
