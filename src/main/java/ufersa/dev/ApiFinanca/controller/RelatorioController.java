package ufersa.dev.ApiFinanca.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ufersa.dev.ApiFinanca.dto.RelatorioMensalResponse;
import ufersa.dev.ApiFinanca.service.RelatorioService;

import java.util.UUID;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/mensal")
    public ResponseEntity<RelatorioMensalResponse> getRelatorioMensal(
            @RequestParam UUID userId,
            @RequestParam int mes,
            @RequestParam int ano
    ) {
        RelatorioMensalResponse response =
                relatorioService.gerarRelatorioMensal(userId, mes, ano);

        return ResponseEntity.ok(response);
    }
}
