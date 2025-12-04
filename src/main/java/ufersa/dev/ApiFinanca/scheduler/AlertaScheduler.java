package ufersa.dev.ApiFinanca.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ufersa.dev.ApiFinanca.model.*;
import ufersa.dev.ApiFinanca.repository.*;
import ufersa.dev.ApiFinanca.service.AlertaGerenciamentoService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Scheduler que verifica periodicamente as condições e cria alertas para os usuários
 */
@Component
public class AlertaScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AlertaScheduler.class);

    private final UsuarioRepository usuarioRepository;
    private final ConfiguracaoUsuarioRepository configuracaoUsuarioRepository;
    private final AlertaGerenciamentoService alertaGerenciamentoService;

    public AlertaScheduler(
            UsuarioRepository usuarioRepository,
            ConfiguracaoUsuarioRepository configuracaoUsuarioRepository,
            AlertaGerenciamentoService alertaGerenciamentoService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.configuracaoUsuarioRepository = configuracaoUsuarioRepository;
        this.alertaGerenciamentoService = alertaGerenciamentoService;
    }

    /**
     * Roda diariamente às 8h para verificar metas próximas do vencimento
     */
    @Scheduled(cron = "0 0 8 * * *") // 8h da manhã todos os dias
    public void verificarMetasProximasVencimento() {
        logger.info("Iniciando verificação de metas próximas do vencimento");
        
        List<Usuario> usuarios = usuarioRepository.findAll();
        LocalDate hoje = LocalDate.now();
        
        for (Usuario usuario : usuarios) {
            if (usuario.getMetas() == null) continue;
            
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
                    BigDecimal valorFaltante = meta.getValorAlvo().subtract(
                        meta.getValorAtual() != null ? meta.getValorAtual() : BigDecimal.ZERO
                    );
                    
                    String mensagem = String.format(
                        "Meta '%s' vence em 7 dias! Faltam R$ %.2f para alcançar o valor alvo de R$ %.2f",
                        meta.getNome(),
                        valorFaltante.doubleValue(),
                        meta.getValorAlvo().doubleValue()
                    );
                    
                    alertaGerenciamentoService.criarAlertaSeNecessario(
                        usuario,
                        TipoAlerta.META_PROXIMA_VENCIMENTO,
                        mensagem,
                        SeveridadeAlerta.MEDIA,
                        meta.getId(),
                        null
                    );
                }
                
                // Alerta no dia anterior ao vencimento
                if (diasRestantes == 1) {
                    BigDecimal valorFaltante = meta.getValorAlvo().subtract(
                        meta.getValorAtual() != null ? meta.getValorAtual() : BigDecimal.ZERO
                    );
                    
                    String mensagem = String.format(
                        "⚠️ Meta '%s' vence amanhã! Faltam R$ %.2f para alcançar o valor alvo de R$ %.2f",
                        meta.getNome(),
                        valorFaltante.doubleValue(),
                        meta.getValorAlvo().doubleValue()
                    );
                    
                    alertaGerenciamentoService.criarAlertaSeNecessario(
                        usuario,
                        TipoAlerta.META_PROXIMA_VENCIMENTO,
                        mensagem,
                        SeveridadeAlerta.ALTA,
                        meta.getId(),
                        null
                    );
                }
            }
        }
        
        logger.info("Verificação de metas próximas do vencimento concluída");
    }

    /**
     * Roda semanalmente (domingo às 9h) para verificar gastos acima da média
     */
    @Scheduled(cron = "0 0 9 * * SUN") // Domingo às 9h
    public void verificarGastosAcimaMedia() {
        logger.info("Iniciando verificação de gastos acima da média");
        
        List<Usuario> usuarios = usuarioRepository.findAll();
        
        for (Usuario usuario : usuarios) {
            // Implementar lógica de gasto acima da média
            // (Similar ao método detectarGastoAcimaMedia do AlertaService, mas criando alertas persistidos)
            verificarGastoAcimaMediaParaUsuario(usuario);
        }
        
        logger.info("Verificação de gastos acima da média concluída");
    }

    /**
     * Roda no dia de virada do mês de cada usuário para verificar projeção e economia
     */
    @Scheduled(cron = "0 0 10 * * *") // 10h todos os dias (verifica se é dia de virada)
    public void verificarAlertasMensais() {
        logger.info("Iniciando verificação de alertas mensais");
        
        List<Usuario> usuarios = usuarioRepository.findAll();
        LocalDate hoje = LocalDate.now();
        
        for (Usuario usuario : usuarios) {
            int diaVirada = configuracaoUsuarioRepository.findByUsuario_Id(usuario.getId())
                    .map(ConfiguracaoUsuario::getDiaViradaMes)
                    .orElse(1);
            
            // Verifica se hoje é o dia de virada do usuário
            if (hoje.getDayOfMonth() == diaVirada) {
                verificarProjecaoSaldoNegativoParaUsuario(usuario);
                verificarEconomiaPositivaParaUsuario(usuario);
            }
        }
        
        logger.info("Verificação de alertas mensais concluída");
    }

    /**
     * Roda diariamente às 10h para verificar progresso de metas
     */
    @Scheduled(cron = "0 0 10 * * *") // 10h todos os dias
    public void verificarProgressoMetas() {
        logger.info("Iniciando verificação de progresso de metas");
        
        List<Usuario> usuarios = usuarioRepository.findAll();
        
        for (Usuario usuario : usuarios) {
            verificarProgressoMetasParaUsuario(usuario);
        }
        
        logger.info("Verificação de progresso de metas concluída");
    }

    /**
     * Limpa alertas antigos mensalmente
     */
    @Scheduled(cron = "0 0 3 1 * *") // 3h do dia 1 de cada mês
    public void limparAlertasAntigos() {
        logger.info("Iniciando limpeza de alertas antigos");
        alertaGerenciamentoService.limparAlertasAntigos();
        logger.info("Limpeza de alertas antigos concluída");
    }

    // Métodos auxiliares que implementam a lógica de detecção
    // (Nota: Estes métodos precisam ser implementados de forma similar aos do AlertaService,
    //  mas chamando alertaGerenciamentoService.criarAlertaSeNecessario em vez de retornar lista)
    
    private void verificarGastoAcimaMediaParaUsuario(Usuario usuario) {
        // TODO: Implementar lógica completa
        // Similar ao detectarGastoAcimaMedia do AlertaService
    }
    
    private void verificarProjecaoSaldoNegativoParaUsuario(Usuario usuario) {
        // TODO: Implementar lógica completa
    }
    
    private void verificarEconomiaPositivaParaUsuario(Usuario usuario) {
        // TODO: Implementar lógica completa
    }
    
    private void verificarProgressoMetasParaUsuario(Usuario usuario) {
        // TODO: Implementar lógica completa
    }
}

