package ufersa.dev.ApiFinanca.service;

import ufersa.dev.ApiFinanca.dto.RelatorioMensalResponse;

import java.time.LocalDate;
import java.util.UUID;

public interface RelatorioService {

    RelatorioMensalResponse gerarRelatorioMensal(UUID userId, int mes, int ano);

    RelatorioMensalResponse gerarRelatorioPorPeriodo(
            UUID userId,
            LocalDate dataInicio,
            LocalDate dataFim
    );
}
