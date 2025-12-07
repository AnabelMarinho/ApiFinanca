package ufersa.dev.ApiFinanca.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ufersa.dev.ApiFinanca.dto.GastoCategoria;
import ufersa.dev.ApiFinanca.dto.RelatorioMensalResponse;
import ufersa.dev.ApiFinanca.dto.TransacaoResponse;
import ufersa.dev.ApiFinanca.model.TipoTransacao;
import ufersa.dev.ApiFinanca.model.Transacao;
import ufersa.dev.ApiFinanca.model.Usuario;
import ufersa.dev.ApiFinanca.repository.TransacaoRepository;
import ufersa.dev.ApiFinanca.repository.UsuarioRepository;
import ufersa.dev.ApiFinanca.service.RelatorioService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelatorioServiceImpl implements RelatorioService {

    private final TransacaoRepository transacaoRepository;
    private final UsuarioRepository usuarioRepository;

    // ✅ RELATÓRIO MENSAL
    @Override
    public RelatorioMensalResponse gerarRelatorioMensal(UUID userId, int mes, int ano) {

        YearMonth yearMonth = YearMonth.of(ano, mes);
        LocalDate dataInicio = yearMonth.atDay(1);
        LocalDate dataFim = yearMonth.atEndOfMonth();

        return gerarRelatorioBase(userId, dataInicio, dataFim);
    }

    @Override
    public RelatorioMensalResponse gerarRelatorioPorPeriodo(
            UUID userId,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
        return gerarRelatorioBase(userId, dataInicio, dataFim);
    }

    private RelatorioMensalResponse gerarRelatorioBase(
            UUID userId,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {

        Usuario user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<Transacao> transacoes = transacaoRepository
                .findByUserAndDataBetween(user, dataInicio, dataFim);

        BigDecimal totalReceitas = transacaoRepository
                .calcularTotalPorTipoEPeriodo(user, TipoTransacao.RECEITA, dataInicio, dataFim);

        BigDecimal totalDespesas = transacaoRepository
                .calcularTotalPorTipoEPeriodo(user, TipoTransacao.DESPESA, dataInicio, dataFim);

        BigDecimal saldo = totalReceitas.subtract(totalDespesas);

        List<TransacaoResponse> transacaoResponses = transacoes.stream()
                .map(TransacaoResponse::new)
                .toList();

        Map<String, BigDecimal> mapaCategorias = transacoes.stream()
                .filter(t -> t.getTipo() == TipoTransacao.DESPESA)
                .collect(Collectors.groupingBy(
                        t -> t.getCategoria().getNome(),
                        Collectors.reducing(BigDecimal.ZERO, Transacao::getValor, BigDecimal::add)
                ));

        List<GastoCategoria> gastosPorCategoria = mapaCategorias.entrySet().stream()
                .map(e -> new GastoCategoria(e.getKey(), e.getValue()))
                .toList();

        RelatorioMensalResponse response = new RelatorioMensalResponse();
        response.setTotalReceitas(totalReceitas);
        response.setTotalDespesas(totalDespesas);
        response.setSaldo(saldo);
        response.setTransacoes(transacaoResponses);
        response.setGastosPorCategoria(gastosPorCategoria);

        return response;
    }
}
