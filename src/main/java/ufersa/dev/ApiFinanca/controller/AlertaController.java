package ufersa.dev.ApiFinanca.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ufersa.dev.ApiFinanca.dto.AlertaResponse;
import ufersa.dev.ApiFinanca.service.AlertaService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public List<AlertaResponse> obterAlertasAtivos(
            @RequestParam(value = "usuarioId", required = false) UUID usuarioId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            UUID userId;
            if (usuarioId != null) {
                userId = usuarioId;
            } else if (userDetails != null) {
                userId = UUID.fromString(userDetails.getUsername());
            } else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
            }
            return alertaService.obterAlertasAtivos(userId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }
}

