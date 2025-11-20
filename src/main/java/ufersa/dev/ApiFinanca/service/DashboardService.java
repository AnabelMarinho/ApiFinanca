package ufersa.dev.ApiFinanca.service;

import org.springframework.stereotype.Service;
import ufersa.dev.ApiFinanca.dto.DashboardResponse;
import ufersa.dev.ApiFinanca.dto.GastoCategoria;
import ufersa.dev.ApiFinanca.dto.TransacaoResponse;
import ufersa.dev.ApiFinanca.model.*;
import ufersa.dev.ApiFinanca.repository.TransacaoRepository;
import ufersa.dev.ApiFinanca.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final TransacaoRepository transacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public DashboardService(TransacaoRepository transacaoRepository,
                            UsuarioRepository usuarioRepository) {
        this.transacaoRepository = transacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public DashboardResponse getDashboard(UUID userId) {

        Usuario user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        YearMonth mesAtual = YearMonth.now();
        LocalDate inicio = mesAtual.atDay(1);
        LocalDate fim = mesAtual.atEndOfMonth();

        BigDecimal totalReceitas = transacaoRepository
                .calcularTotalPorTipoEPeriodo(user, TipoTransacao.RECEITA, inicio, fim);


        BigDecimal totalDespesas = transacaoRepository
                .calcularTotalPorTipoEPeriodo(user, TipoTransacao.DESPESA, inicio, fim);


        BigDecimal saldoAtual = totalReceitas.subtract(totalDespesas);

        List<TransacaoResponse> transacoesRecentes =
                transacaoRepository.findTop10ByUserOrderByDataDesc(user)
                        .stream()
                        .map(TransacaoResponse::new)
                        .toList();

        List<Transacao> todasTransacoesMes = transacaoRepository
                .findByUserAndTipoAndDataBetween(user, TipoTransacao.DESPESA, inicio, fim);

        List<GastoCategoria> gastosPorCategoria = todasTransacoesMes.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategoria().getNome(),
                        Collectors.reducing(BigDecimal.ZERO, Transacao::getValor, BigDecimal::add)
                ))
                .entrySet()
                .stream()
                .map(e -> new GastoCategoria(e.getKey(), e.getValue()))
                .toList();

        DashboardResponse response = new DashboardResponse();
        response.setSaldoAtual(saldoAtual);
        response.setTotalReceitas(totalReceitas);
        response.setTotalDespesas(totalDespesas);
        response.setTransacoesRecentes(transacoesRecentes);
        response.setGastosPorCategoria(gastosPorCategoria);

        return response;
    }

}

