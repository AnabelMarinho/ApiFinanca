package ufersa.dev.ApiFinanca.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class OnboardingRequest {

    @NotNull(message = "A data de início do controle é obrigatória.")
    private LocalDate dataInicioControle;

    /**
     * Permite enviar explicitamente o usuário alvo do onboarding quando necessário (ex.: fluxos administrativos).
     * Se não for informado, o controlador utilizará o usuário autenticado via JWT.
     */
    private UUID usuarioId;

    @Valid
    @Size(max = 20, message = "O número máximo de transações recorrentes é 20.")
    private List<TransacaoRecorrenteOnboardingRequest> transacoesRecorrentes;
}
