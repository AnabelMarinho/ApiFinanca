package ufersa.dev.ApiFinanca.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ufersa.dev.ApiFinanca.dto.VerificacaoAlertasResponse;
import ufersa.dev.ApiFinanca.model.*;
import ufersa.dev.ApiFinanca.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service para verificação manual de alertas
 * Permite executar a verificação de todos os usuários sob demanda via endpoint
 */
@Service
public class AlertaVerificacaoManualService {

    private static final Logger logger = LoggerFactory.getLogger(AlertaVerificacaoManualService.class);

    private final UsuarioRepository usuarioRepository;
    private final TransacaoRepository transacaoRepository;
    private final TransacaoRecorrenteRepository transacaoRecorrenteRepository;
    private final ConfiguracaoUsuarioRepository configuracaoUsuarioRepository;
    private final AlertaGerenciamentoService alertaGerenciamentoService;

    public AlertaVerificacaoManualService(
            UsuarioRepository usuarioRepository,
            TransacaoRepository transacaoRepository,
            TransacaoRecorrenteRepository transacaoRecorrenteRepository,
            ConfiguracaoUsuarioRepository configuracaoUsuarioRepository,
            AlertaGerenciamentoService alertaGerenciamentoService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.transacaoRepository = transacaoRepository;
        this.transacaoRecorrenteRepository = transacaoRecorrenteRepository;
        this.configuracaoUsuarioRepository = configuracaoUsuarioRepository;
        this.alertaGerenciamentoService = alertaGerenciamentoService;
    }

    /**
     * Verifica alertas para todos os usuários e retorna estatísticas
     */
    @Transactional
    public VerificacaoAlertasResponse verificarTodosUsuarios() {
        logger.info("Iniciando verificação manual de alertas para todos os usuários");
        
        VerificacaoAlertasResponse response = new VerificacaoAlertasResponse(
            LocalDateTime.now(), 
            0, 
            0
        );

        List<Usuario> usuarios = usuarioRepository.findAll();
        response.setTotalUsuariosVerificados(usuarios.size());

        for (Usuario usuario : usuarios) {
            try {
                // Forçar carregamento de metas
                if (usuario.getMetas() != null) {
                    usuario.getMetas().size();
                }

                // Verificar cada tipo de alerta
                verificarMetasProximasVencimento(usuario, response);
                verificarGastosAcimaMedia(usuario, response);
                verificarProjecaoSaldoNegativo(usuario, response);
                verificarProgressoMetas(usuario, response);
                verificarEconomiaPositiva(usuario, response);
                verificarMetasAlcancadas(usuario, response);

            } catch (Exception e) {
                logger.error("Erro ao verificar alertas do usuário {}: {}", usuario.getId(), e.getMessage());
            }
        }

        logger.info("Verificação manual concluída: {} alertas criados para {} usuários", 
                    response.getTotalAlertasCriados(), response.getTotalUsuariosVerificados());

        return response;
    }

    /**
     * Verifica metas próximas do vencimento para um usuário
     */
    private void verificarMetasProximasVencimento(Usuario usuario, VerificacaoAlertasResponse response) {
        LocalDate hoje = LocalDate.now();
        
        if (usuario.getMetas() == null) return;

        for (Meta meta : usuario.getMetas()) {
            if (meta.getDataAlvo() == null) continue;
            
            // Verificar se meta já foi alcançada
            if (meta.getValorAtual() != null && meta.getValorAlvo() != null &&
                meta.getValorAtual().compareTo(meta.getValorAlvo()) >= 0) {
                continue;
            }
            
            long diasRestantes = ChronoUnit.DAYS.between(hoje, meta.getDataAlvo());
            
            // Alerta quando faltam 7 dias
            if (diasRestantes == 7) {
                criarAlertaMetaProximaVencimento(usuario, meta, diasRestantes, response);
            }
            
            // Alerta no dia anterior
            if (diasRestantes == 1) {
                criarAlertaMetaProximaVencimento(usuario, meta, diasRestantes, response);
            }
        }
    }

    private void criarAlertaMetaProximaVencimento(Usuario usuario, Meta meta, long diasRestantes, VerificacaoAlertasResponse response) {
        BigDecimal valorFaltante = meta.getValorAlvo().subtract(
            meta.getValorAtual() != null ? meta.getValorAtual() : BigDecimal.ZERO
        );
        
        String mensagem;
        SeveridadeAlerta severidade;
        
        if (diasRestantes == 1) {
            mensagem = String.format(
                "⚠️ Meta '%s' vence amanhã! Faltam R$ %.2f para alcançar o valor alvo de R$ %.2f",
                meta.getNome(),
                valorFaltante.doubleValue(),
                meta.getValorAlvo().doubleValue()
            );
            severidade = SeveridadeAlerta.ALTA;
        } else {
            mensagem = String.format(
                "Meta '%s' vence em %d dias! Faltam R$ %.2f para alcançar o valor alvo de R$ %.2f",
                meta.getNome(),
                diasRestantes,
                valorFaltante.doubleValue(),
                meta.getValorAlvo().doubleValue()
            );
            severidade = SeveridadeAlerta.MEDIA;
        }
        
        alertaGerenciamentoService.criarAlertaSeNecessario(
            usuario,
            TipoAlerta.META_PROXIMA_VENCIMENTO,
            mensagem,
            severidade,
            meta.getId(),
            null
        );
        
        response.setTotalAlertasCriados(response.getTotalAlertasCriados() + 1);
        response.adicionarAlertaPorTipo("META_PROXIMA_VENCIMENTO");
    }

    /**
     * Verifica gastos acima da média
     */
    private void verificarGastosAcimaMedia(Usuario usuario, VerificacaoAlertasResponse response) {
        LocalDate[] periodoAtual = obterPeriodoAtual(usuario.getId());
        LocalDate inicioAtual = periodoAtual[0];
        LocalDate fimAtual = periodoAtual[1];

        List<Categoria> categoriasDespesa = transacaoRepository
                .findByUserAndTipoAndDataBetween(usuario, TipoTransacao.DESPESA, inicioAtual, fimAtual)
                .stream()
                .map(Transacao::getCategoria)
                .distinct()
                .collect(Collectors.toList());

        for (Categoria categoria : categoriasDespesa) {
            BigDecimal gastoAtual = transacaoRepository
                    .calcularTotalPorCategoriaEPeriodo(usuario, categoria, inicioAtual, fimAtual);

            if (gastoAtual.compareTo(BigDecimal.ZERO) <= 0) continue;

            // Calcular média dos últimos 3 meses
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

            if (mesesComDados == 0) continue;

            BigDecimal media = somaMeses.divide(BigDecimal.valueOf(mesesComDados), 2, RoundingMode.HALF_UP);
            BigDecimal limite = media.multiply(BigDecimal.valueOf(1.2));

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

                alertaGerenciamentoService.criarAlertaSeNecessario(
                    usuario,
                    TipoAlerta.GASTO_ACIMA_MEDIA,
                    mensagem,
                    severidade,
                    null,
                    categoria.getId()
                );
                
                response.setTotalAlertasCriados(response.getTotalAlertasCriados() + 1);
                response.adicionarAlertaPorTipo("GASTO_ACIMA_MEDIA");
            }
        }
    }

    /**
     * Verifica projeção de saldo negativo
     */
    private void verificarProjecaoSaldoNegativo(Usuario usuario, VerificacaoAlertasResponse response) {
        BigDecimal saldoAtual = usuario.getSaldoAtual() != null ? usuario.getSaldoAtual() : BigDecimal.ZERO;

        LocalDate[] periodoAtual = obterPeriodoAtual(usuario.getId());
        LocalDate inicioAtual = periodoAtual[0];
        LocalDate fimAtual = periodoAtual[1];

        BigDecimal receitasEsperadas = calcularReceitasEsperadas(usuario, inicioAtual, fimAtual);
        BigDecimal despesasEsperadas = calcularDespesasEsperadas(usuario, inicioAtual, fimAtual);

        BigDecimal projecaoSaldo = saldoAtual.add(receitasEsperadas).subtract(despesasEsperadas);

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

            alertaGerenciamentoService.criarAlertaSeNecessario(
                usuario,
                TipoAlerta.PROJECAO_SALDO_NEGATIVO,
                mensagem,
                severidade,
                null,
                null
            );
            
            response.setTotalAlertasCriados(response.getTotalAlertasCriados() + 1);
            response.adicionarAlertaPorTipo("PROJECAO_SALDO_NEGATIVO");
        }
    }

    /**
     * Verifica progresso de metas
     */
    private void verificarProgressoMetas(Usuario usuario, VerificacaoAlertasResponse response) {
        LocalDate hoje = LocalDate.now();

        if (usuario.getMetas() == null) return;

        for (Meta meta : usuario.getMetas()) {
            if (meta.getDataAlvo() == null || meta.getDataCriacao() == null) continue;

            if (meta.getValorAtual() != null && meta.getValorAlvo() != null &&
                meta.getValorAtual().compareTo(meta.getValorAlvo()) >= 0) {
                continue;
            }

            LocalDate dataCriacao = meta.getDataCriacao().toLocalDate();
            LocalDate dataAlvo = meta.getDataAlvo();

            long tempoTotalDias = ChronoUnit.DAYS.between(dataCriacao, dataAlvo);
            long tempoDecorridoDias = ChronoUnit.DAYS.between(dataCriacao, hoje);

            if (tempoDecorridoDias < (tempoTotalDias / 2)) continue;
            if (hoje.isAfter(dataAlvo)) continue;

            BigDecimal valorAtual = meta.getValorAtual() != null ? meta.getValorAtual() : BigDecimal.ZERO;
            BigDecimal percentualProgresso = valorAtual
                    .divide(meta.getValorAlvo(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            BigDecimal percentualTempo = BigDecimal.valueOf(tempoDecorridoDias)
                    .divide(BigDecimal.valueOf(tempoTotalDias), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            if (percentualProgresso.add(BigDecimal.valueOf(20)).compareTo(percentualTempo) < 0) {
                BigDecimal valorEsperado = meta.getValorAlvo()
                        .multiply(percentualTempo)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                String mensagem = String.format(
                        "Meta '%s': Progresso abaixo do esperado. Decorreram %.0f%% do tempo, mas apenas %.0f%% foi alcançado. " +
                        "Esperado: R$ %.2f, Atual: R$ %.2f",
                        meta.getNome(),
                        percentualTempo.doubleValue(),
                        percentualProgresso.doubleValue(),
                        valorEsperado.doubleValue(),
                        valorAtual.doubleValue()
                );

                SeveridadeAlerta severidade = percentualProgresso.add(BigDecimal.valueOf(40))
                        .compareTo(percentualTempo) < 0
                        ? SeveridadeAlerta.ALTA
                        : SeveridadeAlerta.MEDIA;

                alertaGerenciamentoService.criarAlertaSeNecessario(
                    usuario,
                    TipoAlerta.PROGRESSO_META_LENTO,
                    mensagem,
                    severidade,
                    meta.getId(),
                    null
                );
                
                response.setTotalAlertasCriados(response.getTotalAlertasCriados() + 1);
                response.adicionarAlertaPorTipo("PROGRESSO_META_LENTO");
            }
        }
    }

    /**
     * Verifica economia positiva
     */
    private void verificarEconomiaPositiva(Usuario usuario, VerificacaoAlertasResponse response) {
        LocalDate[] periodoAtual = obterPeriodoAtual(usuario.getId());
        LocalDate inicioAtual = periodoAtual[0];
        LocalDate fimAtual = periodoAtual[1];

        BigDecimal despesasAtual = transacaoRepository
                .calcularTotalPorTipoEPeriodo(usuario, TipoTransacao.DESPESA, inicioAtual, fimAtual);

        BigDecimal somaDespesas = BigDecimal.ZERO;
        int mesesComDados = 0;

        for (int i = 1; i <= 3; i++) {
            LocalDate inicioMes = inicioAtual.minusMonths(i);
            LocalDate fimMes = inicioMes.plusMonths(1).minusDays(1);

            BigDecimal despesasMes = transacaoRepository
                    .calcularTotalPorTipoEPeriodo(usuario, TipoTransacao.DESPESA, inicioMes, fimMes);

            if (despesasMes.compareTo(BigDecimal.ZERO) > 0) {
                somaDespesas = somaDespesas.add(despesasMes);
                mesesComDados++;
            }
        }

        if (mesesComDados == 0) return;

        BigDecimal mediaDespesas = somaDespesas.divide(BigDecimal.valueOf(mesesComDados), 2, RoundingMode.HALF_UP);
        BigDecimal economia = mediaDespesas.subtract(despesasAtual);

        if (economia.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal percentualEconomia = economia
                    .divide(mediaDespesas, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            if (percentualEconomia.compareTo(BigDecimal.valueOf(15)) > 0) {
                String mensagem = String.format(
                        "💰 Ótimo trabalho! Você economizou %.0f%% em relação à média (R$ %.2f). " +
                        "Despesas: R$ %.2f vs Média: R$ %.2f",
                        percentualEconomia.doubleValue(),
                        economia.doubleValue(),
                        despesasAtual.doubleValue(),
                        mediaDespesas.doubleValue()
                );

                alertaGerenciamentoService.criarAlertaSeNecessario(
                    usuario,
                    TipoAlerta.ECONOMIA_POSITIVA,
                    mensagem,
                    SeveridadeAlerta.BAIXA,
                    null,
                    null
                );
                
                response.setTotalAlertasCriados(response.getTotalAlertasCriados() + 1);
                response.adicionarAlertaPorTipo("ECONOMIA_POSITIVA");
            }
        }
    }

    /**
     * Verifica metas alcançadas
     */
    private void verificarMetasAlcancadas(Usuario usuario, VerificacaoAlertasResponse response) {
        if (usuario.getMetas() == null) return;

        for (Meta meta : usuario.getMetas()) {
            if (meta.getValorAtual() == null || meta.getValorAlvo() == null) continue;

            if (meta.getValorAtual().compareTo(meta.getValorAlvo()) >= 0) {
                BigDecimal percentualAlcancado = meta.getValorAtual()
                        .divide(meta.getValorAlvo(), 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

                String mensagem = String.format(
                        "🎉 Parabéns! Meta '%s' alcançada! Você conquistou %.0f%% do objetivo (R$ %.2f de R$ %.2f)",
                        meta.getNome(),
                        percentualAlcancado.doubleValue(),
                        meta.getValorAtual().doubleValue(),
                        meta.getValorAlvo().doubleValue()
                );

                alertaGerenciamentoService.criarAlertaSeNecessario(
                    usuario,
                    TipoAlerta.META_ALCANCADA,
                    mensagem,
                    SeveridadeAlerta.BAIXA,
                    meta.getId(),
                    null
                );
                
                response.setTotalAlertasCriados(response.getTotalAlertasCriados() + 1);
                response.adicionarAlertaPorTipo("META_ALCANCADA");
            }
        }
    }

    // Métodos auxiliares
    
    private LocalDate[] obterPeriodoAtual(java.util.UUID userId) {
        int diaVirada = configuracaoUsuarioRepository.findByUsuario_Id(userId)
                .map(ConfiguracaoUsuario::getDiaViradaMes)
                .orElse(1);

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

    private BigDecimal calcularReceitasEsperadas(Usuario usuario, LocalDate inicio, LocalDate fim) {
        BigDecimal receitasRealizadas = transacaoRepository
                .calcularTotalPorTipoEPeriodo(usuario, TipoTransacao.RECEITA, inicio, fim);

        LocalDate hoje = LocalDate.now();
        BigDecimal receitasRecorrentesFuturas = BigDecimal.ZERO;
        
        if (hoje.isBefore(fim) || hoje.isEqual(fim)) {
            receitasRecorrentesFuturas = transacaoRecorrenteRepository
                    .findByUserAndTipoAndAtivaTrue(usuario, TipoTransacao.RECEITA)
                    .stream()
                    .map(TransacaoRecorrente::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        return receitasRealizadas.add(receitasRecorrentesFuturas);
    }

    private BigDecimal calcularDespesasEsperadas(Usuario usuario, LocalDate inicio, LocalDate fim) {
        BigDecimal despesasRealizadas = transacaoRepository
                .calcularTotalPorTipoEPeriodo(usuario, TipoTransacao.DESPESA, inicio, fim);

        LocalDate hoje = LocalDate.now();
        BigDecimal despesasRecorrentesFuturas = BigDecimal.ZERO;
        
        if (hoje.isBefore(fim) || hoje.isEqual(fim)) {
            despesasRecorrentesFuturas = transacaoRecorrenteRepository
                    .findByUserAndTipoAndAtivaTrue(usuario, TipoTransacao.DESPESA)
                    .stream()
                    .map(TransacaoRecorrente::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        return despesasRealizadas.add(despesasRecorrentesFuturas);
    }
}

