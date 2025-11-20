package ufersa.dev.ApiFinanca.service;

import org.springframework.stereotype.Service;
import ufersa.dev.ApiFinanca.dto.DashboardResponse;
import ufersa.dev.ApiFinanca.dto.GastoCategoria;
import ufersa.dev.ApiFinanca.dto.TransacaoResponse;
import ufersa.dev.ApiFinanca.model.*;
import ufersa.dev.ApiFinanca.repository.ConfiguracaoUsuarioRepository;
import ufersa.dev.ApiFinanca.repository.TransacaoRepository;
import ufersa.dev.ApiFinanca.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final TransacaoRepository transacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConfiguracaoUsuarioRepository configuracaoUsuarioRepository;

    public DashboardService(TransacaoRepository transacaoRepository,
                            UsuarioRepository usuarioRepository,
                            ConfiguracaoUsuarioRepository configuracaoUsuarioRepository) {
        this.transacaoRepository = transacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.configuracaoUsuarioRepository = configuracaoUsuarioRepository;
    }

    public DashboardResponse getDashboard(UUID userId) {

        Usuario user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        ConfiguracaoUsuario config = configuracaoUsuarioRepository.findByUsuario_Id(userId)
                .orElseThrow(() -> new RuntimeException("Configuração do usuário não encontrada"));

        int diaVirada = config.getDiaViradaMes();
        LocalDate hoje = LocalDate.now();
        LocalDate inicio;
        LocalDate fim;

        if (hoje.getDayOfMonth() >= diaVirada) {
            inicio = LocalDate.of(hoje.getYear(), hoje.getMonth(), diaVirada);
            fim = inicio.plusMonths(1).minusDays(1);
        } else {
            LocalDate mesAnterior = hoje.minusMonths(1);
            inicio = LocalDate.of(mesAnterior.getYear(), mesAnterior.getMonth(), diaVirada);
            fim = inicio.plusMonths(1).minusDays(1);
        }

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
