package ufersa.dev.ApiFinanca.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ufersa.dev.ApiFinanca.dto.AlertaResponse;
import ufersa.dev.ApiFinanca.model.*;
import ufersa.dev.ApiFinanca.repository.ConfiguracaoUsuarioRepository;
import ufersa.dev.ApiFinanca.repository.TransacaoRecorrenteRepository;
import ufersa.dev.ApiFinanca.repository.TransacaoRepository;
import ufersa.dev.ApiFinanca.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AlertaService {

    private final TransacaoRepository transacaoRepository;
    private final TransacaoRecorrenteRepository transacaoRecorrenteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConfiguracaoUsuarioRepository configuracaoUsuarioRepository;

    public AlertaService(
            TransacaoRepository transacaoRepository,
            TransacaoRecorrenteRepository transacaoRecorrenteRepository,
            UsuarioRepository usuarioRepository,
            ConfiguracaoUsuarioRepository configuracaoUsuarioRepository
    ) {
        this.transacaoRepository = transacaoRepository;
        this.transacaoRecorrenteRepository = transacaoRecorrenteRepository;
        this.usuarioRepository = usuarioRepository;
        this.configuracaoUsuarioRepository = configuracaoUsuarioRepository;
    }

    /**
     * Retorna lista de alertas ativos para o usuário
     */
    @Transactional
    public List<AlertaResponse> obterAlertasAtivos(UUID userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com o ID informado."));

        // Forçar carregamento das metas (lazy loading)
        if (usuario.getMetas() != null) {
            usuario.getMetas().size(); // Força o carregamento
        }

        List<AlertaResponse> alertas = new ArrayList<>();

        // 1. Detectar gasto 20%+ acima da média em categoria
        alertas.addAll(detectarGastoAcimaMedia(usuario));

        // 2. Detectar meta próxima do vencimento (faltam 7 dias)
        alertas.addAll(detectarMetaProximaVencimento(usuario));

        // 3. Detectar projeção de saldo negativo
        alertas.addAll(detectarProjecaoSaldoNegativo(usuario));

        return alertas;
    }

    /**
     * #35: Implementar lógica: detectar gasto 20%+ acima da média em categoria
     */
    private List<AlertaResponse> detectarGastoAcimaMedia(Usuario usuario) {
        List<AlertaResponse> alertas = new ArrayList<>();

        // Obter período atual
        LocalDate[] periodoAtual = obterPeriodoAtual(usuario.getId());
        LocalDate inicioAtual = periodoAtual[0];
        LocalDate fimAtual = periodoAtual[1];

        // Buscar todas as categorias de despesa do usuário no período atual
        List<Categoria> categoriasDespesa = transacaoRepository
                .findByUserAndTipoAndDataBetween(usuario, TipoTransacao.DESPESA, inicioAtual, fimAtual)
                .stream()
                .map(Transacao::getCategoria)
                .distinct()
                .collect(Collectors.toList());

        for (Categoria categoria : categoriasDespesa) {
            // Calcular gasto atual do mês na categoria
            BigDecimal gastoAtual = transacaoRepository
                    .calcularTotalPorCategoriaEPeriodo(usuario, categoria, inicioAtual, fimAtual);

            if (gastoAtual.compareTo(BigDecimal.ZERO) <= 0) {
                continue; // Não há gasto atual, não precisa alertar
            }

            // Calcular média dos últimos 3 meses (calculando cada mês separadamente)
            BigDecimal somaMeses = BigDecimal.ZERO;
            int mesesComDados = 0;

            for (int i = 1; i <= 3; i++) {
                LocalDate inicioMes = inicioAtual.minusMonths(i);
                LocalDate fimMes = inicioMes.plusMonths(1).minusDays(1);
                
                BigDecimal gastoMes = transacaoRepository
                        .calcularTotalPorCategoriaEPeriodo(usuario, categoria, inicioMes, fimMes);
                
                if (gastoMes.compareTo(BigDecimal.ZERO) > 0) {
                    somaMeses = somaMeses.add(gastoMes);
                    mesesComDados++;
                }
            }

            // Se não há histórico suficiente (menos de 1 mês), não gera alerta
            if (mesesComDados == 0) {
                continue;
            }

            // Calcular média mensal
            BigDecimal media = somaMeses.divide(BigDecimal.valueOf(mesesComDados), 2, RoundingMode.HALF_UP);

            // Verificar se o gasto atual está 20% ou mais acima da média
            BigDecimal limite = media.multiply(BigDecimal.valueOf(1.2)); // média * 1.2 = 20% acima

            if (gastoAtual.compareTo(limite) > 0) {
                BigDecimal percentualAcima = gastoAtual.subtract(media)
                        .divide(media, 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

                String mensagem = String.format(
                        "Gasto na categoria '%s' está %.0f%% acima da média histórica (R$ %.2f vs média de R$ %.2f)",
                        categoria.getNome(),
                        percentualAcima.doubleValue(),
                        gastoAtual.doubleValue(),
                        media.doubleValue()
                );

                SeveridadeAlerta severidade = percentualAcima.compareTo(BigDecimal.valueOf(50)) > 0
                        ? SeveridadeAlerta.ALTA
                        : SeveridadeAlerta.MEDIA;

                alertas.add(new AlertaResponse(
                        TipoAlerta.GASTO_ACIMA_MEDIA,
                        mensagem,
                        severidade
                ));
            }
        }

        return alertas;
    }

    /**
     * #36: Implementar lógica: detectar meta próxima do vencimento (faltam 7 dias)
     */
    private List<AlertaResponse> detectarMetaProximaVencimento(Usuario usuario) {
        List<AlertaResponse> alertas = new ArrayList<>();

        LocalDate hoje = LocalDate.now();

        for (Meta meta : usuario.getMetas()) {
            if (meta.getDataAlvo() == null) {
                continue;
            }

            // Verificar se a meta ainda não foi alcançada
            if (meta.getValorAtual() != null && meta.getValorAlvo() != null) {
                if (meta.getValorAtual().compareTo(meta.getValorAlvo()) >= 0) {
                    continue; // Meta já alcançada
                }
            }

            // Calcular dias restantes
            long diasRestantes = ChronoUnit.DAYS.between(hoje, meta.getDataAlvo());

            // Se faltam 7 dias ou menos e a meta ainda não foi alcançada
            if (diasRestantes >= 0 && diasRestantes <= 7) {
                BigDecimal valorFaltante = meta.getValorAlvo().subtract(
                        meta.getValorAtual() != null ? meta.getValorAtual() : BigDecimal.ZERO
                );

                String mensagem;
                SeveridadeAlerta severidade;

                if (diasRestantes == 0) {
                    mensagem = String.format(
                            "Meta '%s' vence hoje! Faltam R$ %.2f para alcançar o valor alvo de R$ %.2f",
                            meta.getNome(),
                            valorFaltante.doubleValue(),
                            meta.getValorAlvo().doubleValue()
                    );
                    severidade = SeveridadeAlerta.ALTA;
                } else if (diasRestantes <= 3) {
                    mensagem = String.format(
                            "Meta '%s' vence em %d dia(s)! Faltam R$ %.2f para alcançar o valor alvo de R$ %.2f",
                            meta.getNome(),
                            diasRestantes,
                            valorFaltante.doubleValue(),
                            meta.getValorAlvo().doubleValue()
                    );
                    severidade = SeveridadeAlerta.ALTA;
                } else {
                    mensagem = String.format(
                            "Meta '%s' vence em %d dias. Faltam R$ %.2f para alcançar o valor alvo de R$ %.2f",
                            meta.getNome(),
                            diasRestantes,
                            valorFaltante.doubleValue(),
                            meta.getValorAlvo().doubleValue()
                    );
                    severidade = SeveridadeAlerta.MEDIA;
                }

                alertas.add(new AlertaResponse(
                        TipoAlerta.META_PROXIMA_VENCIMENTO,
                        mensagem,
                        severidade
                ));
            }
        }

        return alertas;
    }

    /**
     * #37: Implementar lógica: detectar projeção de saldo negativo
     */
    private List<AlertaResponse> detectarProjecaoSaldoNegativo(Usuario usuario) {
        List<AlertaResponse> alertas = new ArrayList<>();

        BigDecimal saldoAtual = usuario.getSaldoAtual() != null ? usuario.getSaldoAtual() : BigDecimal.ZERO;

        // Obter período atual
        LocalDate[] periodoAtual = obterPeriodoAtual(usuario.getId());
        LocalDate inicioAtual = periodoAtual[0];
        LocalDate fimAtual = periodoAtual[1];

        // Calcular receitas e despesas esperadas do mês atual
        BigDecimal receitasEsperadas = calcularReceitasEsperadas(usuario, inicioAtual, fimAtual);
        BigDecimal despesasEsperadas = calcularDespesasEsperadas(usuario, inicioAtual, fimAtual);

        // Calcular projeção de saldo ao final do mês
        BigDecimal projecaoSaldo = saldoAtual.add(receitasEsperadas).subtract(despesasEsperadas);

        // Se a projeção for negativa, gerar alerta
        if (projecaoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            String mensagem = String.format(
                    "Projeção indica saldo negativo de R$ %.2f ao final do mês. Receitas esperadas: R$ %.2f, Despesas esperadas: R$ %.2f",
                    projecaoSaldo.abs().doubleValue(),
                    receitasEsperadas.doubleValue(),
                    despesasEsperadas.doubleValue()
            );

            SeveridadeAlerta severidade = projecaoSaldo.abs().compareTo(saldoAtual.abs()) > 0
                    ? SeveridadeAlerta.ALTA
                    : SeveridadeAlerta.MEDIA;

            alertas.add(new AlertaResponse(
                    TipoAlerta.PROJECAO_SALDO_NEGATIVO,
                    mensagem,
                    severidade
            ));
        }

        return alertas;
    }

    /**
     * Calcula receitas esperadas para o período (transações já realizadas + recorrentes que ainda serão executadas)
     */
    private BigDecimal calcularReceitasEsperadas(Usuario usuario, LocalDate inicio, LocalDate fim) {
        // Receitas já realizadas no período
        BigDecimal receitasRealizadas = transacaoRepository
                .calcularTotalPorTipoEPeriodo(usuario, TipoTransacao.RECEITA, inicio, fim);

        // Receitas recorrentes ativas que ainda serão executadas no restante do período
        // (considerando que as recorrentes já executadas já estão em receitasRealizadas)
        LocalDate hoje = LocalDate.now();
        BigDecimal receitasRecorrentesFuturas = BigDecimal.ZERO;
        
        if (hoje.isBefore(fim) || hoje.isEqual(fim)) {
            // Considerar apenas as recorrentes que ainda serão executadas
            // Para simplificar, vamos considerar todas as recorrentes ativas como esperadas
            // (mesmo que algumas já tenham sido executadas, isso dá uma estimativa conservadora)
            receitasRecorrentesFuturas = transacaoRecorrenteRepository
                    .findByUserAndTipoAndAtivaTrue(usuario, TipoTransacao.RECEITA)
                    .stream()
                    .map(TransacaoRecorrente::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        return receitasRealizadas.add(receitasRecorrentesFuturas);
    }

    /**
     * Calcula despesas esperadas para o período (transações já realizadas + recorrentes que ainda serão executadas)
     */
    private BigDecimal calcularDespesasEsperadas(Usuario usuario, LocalDate inicio, LocalDate fim) {
        // Despesas já realizadas no período
        BigDecimal despesasRealizadas = transacaoRepository
                .calcularTotalPorTipoEPeriodo(usuario, TipoTransacao.DESPESA, inicio, fim);

        // Despesas recorrentes ativas que ainda serão executadas no restante do período
        LocalDate hoje = LocalDate.now();
        BigDecimal despesasRecorrentesFuturas = BigDecimal.ZERO;
        
        if (hoje.isBefore(fim) || hoje.isEqual(fim)) {
            // Considerar apenas as recorrentes que ainda serão executadas
            // Para simplificar, vamos considerar todas as recorrentes ativas como esperadas
            // (mesmo que algumas já tenham sido executadas, isso dá uma estimativa conservadora)
            despesasRecorrentesFuturas = transacaoRecorrenteRepository
                    .findByUserAndTipoAndAtivaTrue(usuario, TipoTransacao.DESPESA)
                    .stream()
                    .map(TransacaoRecorrente::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        return despesasRealizadas.add(despesasRecorrentesFuturas);
    }

    /**
     * Obtém o período atual baseado na configuração do usuário (dia de virada do mês)
     */
    private LocalDate[] obterPeriodoAtual(UUID userId) {
        int diaVirada = configuracaoUsuarioRepository.findByUsuario_Id(userId)
                .map(ConfiguracaoUsuario::getDiaViradaMes)
                .orElse(1); // Valor padrão: dia 1 do mês

        LocalDate hoje = LocalDate.now();
        LocalDate inicio;
        LocalDate fim;

        if (hoje.getDayOfMonth() >= diaVirada) {
            inicio = LocalDate.of(hoje.getYear(), hoje.getMonth(), diaVirada);
            fim = inicio.plusMonths(1).minusDays(1);
        } else {
            LocalDate mesAnterior = hoje.minusMonths(1);
            inicio = LocalDate.of(mesAnterior.getYear(), mesAnterior.getMonth(), diaVirada);
            fim = inicio.plusMonths(1).minusDays(1);
        }

        return new LocalDate[]{inicio, fim};
    }
}

