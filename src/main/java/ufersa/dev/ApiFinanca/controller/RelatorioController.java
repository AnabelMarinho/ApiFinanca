package ufersa.dev.ApiFinanca.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ufersa.dev.ApiFinanca.dto.RelatorioMensalResponse;
import ufersa.dev.ApiFinanca.service.RelatorioService;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private static final Logger logger = LoggerFactory.getLogger(RelatorioController.class);
    private final RelatorioService relatorioService;

    @GetMapping("/mensal")
    @Operation(
            summary = "Gera relatório mensal de transações",
            description = "Retorna um relatório completo com receitas, despesas, saldo e gastos por categoria para um mês específico",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ResponseEntity<RelatorioMensalResponse> getRelatorioMensal(
            @RequestParam UUID userId,
            @RequestParam int mes,
            @RequestParam int ano
    ) {
        logger.info("=== INÍCIO: getRelatorioMensal ===");
        logger.info("Parâmetros recebidos - userId: {}, mes: {}, ano: {}", userId, mes, ano);
        
        // Verifica autenticação
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            logger.warn("⚠️ AUTENTICAÇÃO NULA - SecurityContext não possui autenticação");
        } else if (!authentication.isAuthenticated()) {
            logger.warn("⚠️ AUTENTICAÇÃO NÃO AUTENTICADA - authenticated: {}", authentication.isAuthenticated());
        } else {
            logger.info("✅ Autenticação presente - Principal: {}, Authorities: {}", 
                    authentication.getPrincipal(), authentication.getAuthorities());
        }
        
        try {
            logger.info("Chamando relatorioService.gerarRelatorioMensal...");
            RelatorioMensalResponse response = relatorioService.gerarRelatorioMensal(userId, mes, ano);
            logger.info("Relatório gerado com sucesso - Total Receitas: {}, Total Despesas: {}, Saldo: {}", 
                    response.getTotalReceitas(), response.getTotalDespesas(), response.getSaldo());
            logger.info("=== FIM: getRelatorioMensal (SUCESSO) ===");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ ERRO ao gerar relatório mensal: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/exportacao/transacoes")
    @Operation(
            summary = "Exporta todas as transações do usuário para CSV",
            description = "Gera um arquivo CSV com todas as transações do usuário contendo: data, tipo, categoria, valor e descrição",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ResponseEntity<InputStreamResource> exportarTransacoes(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "csv") String formato
    ) {
        logger.info("=== INÍCIO: exportarTransacoes ===");
        logger.info("Parâmetros recebidos - userId: {}, formato: {}", userId, formato);

        if (!"csv".equalsIgnoreCase(formato)) {
            logger.warn("Formato não suportado: {}. Apenas 'csv' é suportado.", formato);
            return ResponseEntity.badRequest().build();
        }

        try {
            ByteArrayInputStream csvStream = relatorioService.exportarTransacoesParaCSV(userId);
            
            String nomeArquivo = String.format("transacoes_%s_%s.csv", 
                    userId.toString().substring(0, 8),
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");

            logger.info("CSV gerado com sucesso - Nome do arquivo: {}", nomeArquivo);
            logger.info("=== FIM: exportarTransacoes (SUCESSO) ===");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(new InputStreamResource(csvStream));
        } catch (Exception e) {
            logger.error("❌ ERRO ao exportar transações: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/exportacao/transacoes/mensal")
    @Operation(
            summary = "Exporta transações mensais do usuário para CSV",
            description = "Gera um arquivo CSV com as transações do usuário de um mês específico contendo: data, tipo, categoria, valor e descrição",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ResponseEntity<InputStreamResource> exportarTransacoesMensal(
            @RequestParam UUID userId,
            @RequestParam int mes,
            @RequestParam int ano,
            @RequestParam(defaultValue = "csv") String formato
    ) {
        logger.info("=== INÍCIO: exportarTransacoesMensal ===");
        logger.info("Parâmetros recebidos - userId: {}, mes: {}, ano: {}, formato: {}", userId, mes, ano, formato);

        if (!"csv".equalsIgnoreCase(formato)) {
            logger.warn("Formato não suportado: {}. Apenas 'csv' é suportado.", formato);
            return ResponseEntity.badRequest().build();
        }

        try {
            ByteArrayInputStream csvStream = relatorioService.exportarTransacoesMensalParaCSV(userId, mes, ano);
            
            String nomeArquivo = String.format("transacoes_%s_%02d_%d.csv", 
                    userId.toString().substring(0, 8),
                    mes,
                    ano);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");

            logger.info("CSV gerado com sucesso - Nome do arquivo: {}", nomeArquivo);
            logger.info("=== FIM: exportarTransacoesMensal (SUCESSO) ===");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(new InputStreamResource(csvStream));
        } catch (Exception e) {
            logger.error("❌ ERRO ao exportar transações mensais: {}", e.getMessage(), e);
            throw e;
        }
    }
}
