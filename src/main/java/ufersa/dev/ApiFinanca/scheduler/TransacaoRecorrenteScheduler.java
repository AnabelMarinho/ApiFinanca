package ufersa.dev.ApiFinanca.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ufersa.dev.ApiFinanca.service.TransacaoRecorrenteService;

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransacaoRecorrenteScheduler {

    private final TransacaoRecorrenteService transacaoRecorrenteService;

    /**
     * Processa as transações recorrentes diariamente às 00:05 (horário local).
     * A expressão pode ser ajustada via propriedade app.transacoes-recorrentes.cron se necessário.
     */
    @Scheduled(cron = "${app.transacoes-recorrentes.cron:0 5 0 * * *}")
    public void processarRecorrenciasDiarias() {
        LocalDate hoje = LocalDate.now(ZoneId.systemDefault());
        log.info("Iniciando processamento de transações recorrentes para {}", hoje);
        transacaoRecorrenteService.processarRecorrenciasDoDia(hoje);
    }
}


