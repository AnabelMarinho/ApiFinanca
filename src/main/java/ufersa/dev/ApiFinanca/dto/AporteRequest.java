package ufersa.dev.ApiFinanca.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AporteRequest(
        BigDecimal valor,
        LocalDate data
) {
}
