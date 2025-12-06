package ufersa.dev.ApiFinanca.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record MetaResponse(
        UUID id,
        String nome,
        BigDecimal valorAlvo,
        BigDecimal valorAtual,
        LocalDate dataAlvo,
        BigDecimal progresso,
        List<AporteResponse> aportes,
        boolean concluida,
        BigDecimal aporteMensalSugerido
) {
}
