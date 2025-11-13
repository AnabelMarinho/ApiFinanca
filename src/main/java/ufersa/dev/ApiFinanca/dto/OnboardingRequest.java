package ufersa.dev.ApiFinanca.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class OnboardingRequest {

    @NotNull(message = "A data de início do controle é obrigatória.")
    private LocalDate dataInicioControle;

    @Valid
    @Size(max = 20, message = "O número máximo de transações recorrentes é 20.")
    private List<TransacaoRecorrenteOnboardingRequest> transacoesRecorrentes;
}
