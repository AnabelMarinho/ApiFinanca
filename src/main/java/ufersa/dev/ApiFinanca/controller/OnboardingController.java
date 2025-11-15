package ufersa.dev.ApiFinanca.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ufersa.dev.ApiFinanca.dto.OnboardingRequest;
import ufersa.dev.ApiFinanca.service.OnboardingService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @GetMapping("/status")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<Map<String, Boolean>> getStatusOnboarding(
            @RequestParam(value = "usuarioId", required = false) UUID usuarioId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        boolean status;
        if (usuarioId != null) {
            status = onboardingService.statusOnboarding(usuarioId);
        } else if (userDetails != null) {
            status = onboardingService.statusOnboarding(userDetails.getUsername());
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }

        return ResponseEntity.ok(Map.of("onboardingConcluido", status));
    }

    @PostMapping("/onboarding")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<?> realizarOnboarding(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "usuarioId", required = false) UUID usuarioIdHeader,
            @Valid @RequestBody OnboardingRequest request
    ) {
        UUID usuarioId = usuarioIdHeader != null ? usuarioIdHeader : request.getUsuarioId();

        if (usuarioId != null) {
            onboardingService.concluirOnboarding(usuarioId, request);
        } else if (userDetails != null) {
            onboardingService.concluirOnboarding(userDetails.getUsername(), request);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }

        return ResponseEntity.ok("Onboarding concluído com sucesso!");
    }

}
