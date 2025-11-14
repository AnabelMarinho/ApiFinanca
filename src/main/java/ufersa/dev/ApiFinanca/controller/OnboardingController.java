package ufersa.dev.ApiFinanca.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
}
