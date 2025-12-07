package ufersa.dev.ApiFinanca.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificacaoAlertasResponse {
    private LocalDateTime dataVerificacao;
    private Integer totalUsuariosVerificados;
    private Integer totalAlertasCriados;
    private Map<String, Integer> alertasPorTipo;
    private String mensagem;
    
    public VerificacaoAlertasResponse(LocalDateTime dataVerificacao, Integer totalUsuariosVerificados, Integer totalAlertasCriados) {
        this.dataVerificacao = dataVerificacao;
        this.totalUsuariosVerificados = totalUsuariosVerificados;
        this.totalAlertasCriados = totalAlertasCriados;
        this.alertasPorTipo = new HashMap<>();
        this.mensagem = String.format("Verificação concluída: %d alertas criados para %d usuários", 
                                      totalAlertasCriados, totalUsuariosVerificados);
    }
    
    public void adicionarAlertaPorTipo(String tipo) {
        alertasPorTipo.put(tipo, alertasPorTipo.getOrDefault(tipo, 0) + 1);
    }
}

