package ufersa.dev.ApiFinanca.service;

import jakarta.transaction.Transactional;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ufersa.dev.ApiFinanca.dto.AlertaResponse;
import ufersa.dev.ApiFinanca.model.*;
import ufersa.dev.ApiFinanca.repository.AlertaRepository;
import ufersa.dev.ApiFinanca.repository.PreferenciaAlertaRepository;
import ufersa.dev.ApiFinanca.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service para gerenciar alertas persistidos (criar, marcar como visto, listar)
 */
@Service
public class AlertaGerenciamentoService {

    private final AlertaRepository alertaRepository;
    private final PreferenciaAlertaRepository preferenciaAlertaRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String ALERTA_WEBHOOK_URL = "https://webhookworkflow.vulpesflow.com/webhook/0b0e0c4c-0023-4283-b9bd-065652767bdc";

    public AlertaGerenciamentoService(
            AlertaRepository alertaRepository,
            PreferenciaAlertaRepository preferenciaAlertaRepository
    ) {
        this.alertaRepository = alertaRepository;
        this.preferenciaAlertaRepository = preferenciaAlertaRepository;
    }

    /**
     * Lista alertas não vistos do usuário
     */
    @Transactional
    public List<AlertaResponse> listarAlertasNaoVistos(UUID usuarioId) {
        return alertaRepository.findByUsuario_IdAndVistoFalseOrderByDataCriacaoDesc(usuarioId)
                .stream()
                .map(AlertaResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lista todos os alertas do usuário (vistos e não vistos)
     */
    @Transactional
    public List<AlertaResponse> listarTodosAlertas(UUID usuarioId) {
        return alertaRepository.findByUsuario_IdOrderByDataCriacaoDesc(usuarioId)
                .stream()
                .map(AlertaResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Marca um alerta como visto
     */
    @Transactional
    public void marcarComoVisto(UUID alertaId, UUID usuarioId) {
        Alerta alerta = alertaRepository.findById(alertaId)
                .orElseThrow(() -> new IllegalArgumentException("Alerta não encontrado"));

        if (!alerta.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("Este alerta não pertence ao usuário");
        }

        alerta.marcarComoVisto();
        alertaRepository.save(alerta);
    }

    /**
     * Deleta um alerta
     */
    @Transactional
    public void deletarAlerta(UUID alertaId, UUID usuarioId) {
        Alerta alerta = alertaRepository.findById(alertaId)
                .orElseThrow(() -> new IllegalArgumentException("Alerta não encontrado"));

        if (!alerta.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("Este alerta não pertence ao usuário");
        }

        alertaRepository.delete(alerta);
    }

    /**
     * Conta alertas não vistos
     */
    public long contarAlertasNaoVistos(UUID usuarioId) {
        return alertaRepository.countByUsuario_IdAndVistoFalse(usuarioId);
    }

    /**
     * Cria um alerta se as regras de recorrência permitirem
     */
    @Transactional
    public void criarAlertaSeNecessario(
            Usuario usuario,
            TipoAlerta tipoAlerta,
            String mensagem,
            SeveridadeAlerta severidade,
            UUID metaId,
            UUID categoriaId
    ) {
        // Verificar se o usuário tem essa preferência ativa
        if (!usuarioTemPreferenciaAtiva(usuario, tipoAlerta)) {
            return;
        }

        // Verificar regras de recorrência específicas por tipo
        if (podecriarAlerta(usuario, tipoAlerta, metaId, categoriaId)) {
            Alerta alerta = new Alerta(usuario, tipoAlerta, mensagem, severidade);
            alerta.setMetaId(metaId);
            alerta.setCategoriaId(categoriaId);
            
            // Definir referência de período para alguns tipos
            if (tipoUsaPeriodoMensal(tipoAlerta)) {
                alerta.setReferenciaPeriodo(obterPeriodoAtual());
            }
            
            Alerta alertaSalvo = alertaRepository.save(alerta);
            enviarWebhookAlerta(alertaSalvo);
        }
    }

    /**
     * Verifica se o usuário tem a preferência ativa para este tipo de alerta
     */
    private boolean usuarioTemPreferenciaAtiva(Usuario usuario, TipoAlerta tipoAlerta) {
        return preferenciaAlertaRepository.findByUsuarioAndTipoAlerta(usuario, tipoAlerta)
                .map(PreferenciaAlerta::getAtivo)
                .orElse(tipoAlerta.isAtivoPorPadrao());
    }

    /**
     * Verifica se pode criar um alerta baseado nas regras de recorrência
     */
    private boolean podecriarAlerta(Usuario usuario, TipoAlerta tipoAlerta, UUID metaId, UUID categoriaId) {
        switch (tipoAlerta) {
            case GASTO_ACIMA_MEDIA:
                // Máximo 1 por semana
                return !existeAlertaRecente(usuario, tipoAlerta, 7);
                
            case META_PROXIMA_VENCIMENTO:
                // Verifica por meta específica (1 por dia para cada meta)
                if (metaId != null) {
                    return !existeAlertaRecenteMeta(usuario, tipoAlerta, metaId, 1);
                }
                return false;
                
            case PROJECAO_SALDO_NEGATIVO:
                // 1 por mês (usa referenciaPeriodo)
                return !existeAlertaNoPeriodo(usuario, tipoAlerta);
                
            case PROGRESSO_META_LENTO:
                // 1 por meta a cada 7 dias
                if (metaId != null) {
                    return !existeAlertaRecenteMeta(usuario, tipoAlerta, metaId, 7);
                }
                return false;
                
            case META_ALCANCADA:
                // 1 única vez por meta
                if (metaId != null) {
                    return !existeAlertaRecenteMeta(usuario, tipoAlerta, metaId, 365); // 1 ano
                }
                return false;
                
            case GASTO_CATEGORIA_ELEVADO:
                // 1 por categoria a cada 3 dias
                if (categoriaId != null) {
                    return !existeAlertaRecenteCategoria(usuario, tipoAlerta, categoriaId, 3);
                }
                return false;
                
            case ECONOMIA_POSITIVA:
                // 1 por mês (usa referenciaPeriodo)
                return !existeAlertaNoPeriodo(usuario, tipoAlerta);
                
            default:
                return true;
        }
    }

    /**
     * Verifica se existe alerta recente (últimos X dias)
     */
    private boolean existeAlertaRecente(Usuario usuario, TipoAlerta tipoAlerta, int dias) {
        LocalDateTime dataMinima = LocalDateTime.now().minusDays(dias);
        return alertaRepository.findRecentByUsuarioAndTipo(usuario, tipoAlerta, dataMinima).isPresent();
    }

    /**
     * Verifica se existe alerta recente para meta específica
     */
    private boolean existeAlertaRecenteMeta(Usuario usuario, TipoAlerta tipoAlerta, UUID metaId, int dias) {
        LocalDateTime dataMinima = LocalDateTime.now().minusDays(dias);
        return alertaRepository.findRecentByUsuarioAndTipoAndMeta(usuario, tipoAlerta, metaId, dataMinima).isPresent();
    }

    /**
     * Verifica se existe alerta recente para categoria específica
     */
    private boolean existeAlertaRecenteCategoria(Usuario usuario, TipoAlerta tipoAlerta, UUID categoriaId, int dias) {
        LocalDateTime dataMinima = LocalDateTime.now().minusDays(dias);
        return alertaRepository.findRecentByUsuarioAndTipoAndCategoria(usuario, tipoAlerta, categoriaId, dataMinima).isPresent();
    }

    /**
     * Verifica se já existe alerta no período atual
     */
    private boolean existeAlertaNoPeriodo(Usuario usuario, TipoAlerta tipoAlerta) {
        String periodo = obterPeriodoAtual();
        return alertaRepository.findByUsuarioAndTipoAlertaAndReferenciaPeriodo(usuario, tipoAlerta, periodo).isPresent();
    }

    /**
     * Obtém o período atual no formato YYYY-MM
     */
    private String obterPeriodoAtual() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    /**
     * Verifica se o tipo de alerta usa controle por período mensal
     */
    private boolean tipoUsaPeriodoMensal(TipoAlerta tipoAlerta) {
        return tipoAlerta == TipoAlerta.PROJECAO_SALDO_NEGATIVO ||
               tipoAlerta == TipoAlerta.ECONOMIA_POSITIVA;
    }

    /**
     * Limpa alertas antigos (mais de 90 dias)
     */
    @Transactional
    public void limparAlertasAntigos() {
        LocalDateTime dataLimite = LocalDateTime.now().minusDays(90);
        alertaRepository.deleteByDataCriacaoBefore(dataLimite);
    }

    private void enviarWebhookAlerta(Alerta alerta) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", alerta.getId());
        payload.put("usuarioId", alerta.getUsuario().getId());
        payload.put("tipo", alerta.getTipoAlerta());
        payload.put("mensagem", alerta.getMensagem());
        payload.put("severidade", alerta.getSeveridade());
        payload.put("dataCriacao", alerta.getDataCriacao());
        payload.put("visto", alerta.getVisto());
        payload.put("metaId", alerta.getMetaId());
        payload.put("categoriaId", alerta.getCategoriaId());
        payload.put("referenciaPeriodo", alerta.getReferenciaPeriodo());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(ALERTA_WEBHOOK_URL, entity, Void.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return;
            }
        } catch (RestClientException ex) {
            return;
        }
    }
}

