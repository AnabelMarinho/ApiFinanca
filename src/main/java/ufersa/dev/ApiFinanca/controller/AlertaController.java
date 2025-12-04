package ufersa.dev.ApiFinanca.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ufersa.dev.ApiFinanca.dto.AlertaResponse;
import ufersa.dev.ApiFinanca.dto.AtualizarPreferenciaRequest;
import ufersa.dev.ApiFinanca.dto.TipoAlertaInfoResponse;
import ufersa.dev.ApiFinanca.dto.VerificacaoAlertasResponse;
import ufersa.dev.ApiFinanca.service.AlertaGerenciamentoService;
import ufersa.dev.ApiFinanca.service.AlertaService;
import ufersa.dev.ApiFinanca.service.AlertaVerificacaoManualService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alertas")
@Tag(name = "Alertas", description = "Gerenciamento de alertas do usuário")
public class AlertaController {

    private final AlertaService alertaService;
    private final AlertaGerenciamentoService alertaGerenciamentoService;
    private final AlertaVerificacaoManualService alertaVerificacaoManualService;

    public AlertaController(
            AlertaService alertaService, 
            AlertaGerenciamentoService alertaGerenciamentoService,
            AlertaVerificacaoManualService alertaVerificacaoManualService
    ) {
        this.alertaService = alertaService;
        this.alertaGerenciamentoService = alertaGerenciamentoService;
        this.alertaVerificacaoManualService = alertaVerificacaoManualService;
    }

    @GetMapping
    @Operation(
            summary = "Listar alertas não vistos",
            description = "Retorna todos os alertas não vistos do usuário autenticado",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    public List<AlertaResponse> listarAlertasNaoVistos(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            UUID userId = UUID.fromString(userDetails.getUsername());
            return alertaGerenciamentoService.listarAlertasNaoVistos(userId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @GetMapping("/todos")
    @Operation(
            summary = "Listar todos os alertas",
            description = "Retorna todos os alertas do usuário (vistos e não vistos)",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    public List<AlertaResponse> listarTodosAlertas(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            UUID userId = UUID.fromString(userDetails.getUsername());
            return alertaGerenciamentoService.listarTodosAlertas(userId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @PutMapping("/{alertaId}/marcar-visto")
    @Operation(
            summary = "Marcar alerta como visto",
            description = "Marca um alerta específico como visto",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarComoVisto(
            @PathVariable UUID alertaId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            UUID userId = UUID.fromString(userDetails.getUsername());
            alertaGerenciamentoService.marcarComoVisto(alertaId, userId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @DeleteMapping("/{alertaId}")
    @Operation(
            summary = "Deletar alerta",
            description = "Remove um alerta específico",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletarAlerta(
            @PathVariable UUID alertaId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            UUID userId = UUID.fromString(userDetails.getUsername());
            alertaGerenciamentoService.deletarAlerta(alertaId, userId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @GetMapping("/contador-nao-vistos")
    @Operation(
            summary = "Contar alertas não vistos",
            description = "Retorna a quantidade de alertas não vistos do usuário",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    public long contarAlertasNaoVistos(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return alertaGerenciamentoService.contarAlertasNaoVistos(userId);
    }

    @GetMapping("/preferencias")
    @Operation(
            summary = "Listar tipos de alertas disponíveis",
            description = "Retorna todos os tipos de alertas disponíveis com suas configurações e preferências do usuário",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    public List<TipoAlertaInfoResponse> listarTiposAlertas(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            UUID userId = UUID.fromString(userDetails.getUsername());
            return alertaService.listarTiposAlertas(userId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @PutMapping("/preferencias")
    @Operation(
            summary = "Atualizar preferência de alerta",
            description = "Permite ao usuário ativar ou desativar um tipo específico de alerta",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizarPreferenciaAlerta(
            @Valid @RequestBody AtualizarPreferenciaRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            UUID userId = UUID.fromString(userDetails.getUsername());
            alertaService.atualizarPreferenciaAlerta(userId, request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @PostMapping("/verificar-todos")
    @Operation(
            summary = "Verificar alertas de todos os usuários (Manual)",
            description = "Executa manualmente a verificação de alertas para todos os usuários do sistema. " +
                         "Útil para testes e para forçar verificação imediata sem esperar os schedulers.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    public VerificacaoAlertasResponse verificarAlertasTodosUsuarios(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            return alertaVerificacaoManualService.verificarTodosUsuarios();
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Erro ao verificar alertas: " + ex.getMessage(), ex);
        }
    }
}

