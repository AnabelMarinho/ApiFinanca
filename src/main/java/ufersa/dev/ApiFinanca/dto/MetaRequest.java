package ufersa.dev.ApiFinanca.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class MetaRequest {

    private String nome;
    private BigDecimal valorAlvo;
    private LocalDate dataAlvo;

}
