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
import ufersa.dev.ApiFinanca.dto.DashboardResponse;
import ufersa.dev.ApiFinanca.enums.PeriodoDashboard;
import ufersa.dev.ApiFinanca.service.DashboardService;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public DashboardResponse getDashboard(
            @RequestParam(value = "usuarioId", required = false) UUID usuarioId,
            @RequestParam(value = "periodo", required = false, defaultValue = "TODA_UTILIZACAO") PeriodoDashboard periodo,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            UUID authenticatedId = userDetails != null ? UUID.fromString(userDetails.getUsername()) : null;
            UUID userId;
            if (usuarioId != null) {
                if (authenticatedId == null) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
                }
                if (!usuarioId.equals(authenticatedId)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado");
                }
                userId = usuarioId;
            } else if (authenticatedId != null) {
                userId = authenticatedId;
            } else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
            }
            return dashboardService.getDashboard(userId, periodo);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID de usuário inválido", ex);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }
}

