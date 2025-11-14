package ufersa.dev.ApiFinanca.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ufersa.dev.ApiFinanca.dto.OnboardingRequest;
import ufersa.dev.ApiFinanca.model.Usuario;
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
    public ResponseEntity<Map<String, Boolean>> getStatusOnboarding(@RequestParam UUID usuarioId) {
        boolean status = onboardingService.statusOnboarding(usuarioId);
        return ResponseEntity.ok(Map.of("onboardingConcluido", status));
    }

    @PostMapping("/onboarding")
    public ResponseEntity<?> realizarOnboarding(
            @AuthenticationPrincipal Usuario usuario,
            @RequestBody OnboardingRequest request
    ) {

        if (usuario == null) {
            return ResponseEntity.status(401).body("Usuário não autenticado");
        }

        onboardingService.concluirOnboarding(usuario.getId(), request);

        return ResponseEntity.ok("Onboarding concluído com sucesso!");
    }

}
