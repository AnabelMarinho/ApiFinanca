package ufersa.dev.ApiFinanca.service.impl;

import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelatorioServiceImpl implements RelatorioService {

    private static final Logger logger = LoggerFactory.getLogger(RelatorioServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
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

    @Override
    public ByteArrayInputStream exportarTransacoesParaCSV(UUID userId) {
        logger.info("Exportando todas as transações para CSV - userId: {}", userId);
        
        Usuario user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<Transacao> transacoes = transacaoRepository.findByUserOrderByDataDesc(user);
        logger.info("Total de transações encontradas: {}", transacoes.size());

        return gerarCSV(transacoes);
    }

    @Override
    public ByteArrayInputStream exportarTransacoesMensalParaCSV(UUID userId, int mes, int ano) {
        logger.info("Exportando transações mensais para CSV - userId: {}, mes: {}, ano: {}", userId, mes, ano);
        
        YearMonth yearMonth = YearMonth.of(ano, mes);
        LocalDate dataInicio = yearMonth.atDay(1);
        LocalDate dataFim = yearMonth.atEndOfMonth();

        Usuario user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<Transacao> transacoes = transacaoRepository.findByUserAndDataBetween(user, dataInicio, dataFim);
        logger.info("Total de transações encontradas para o período: {}", transacoes.size());

        return gerarCSV(transacoes);
    }

    private ByteArrayInputStream gerarCSV(List<Transacao> transacoes) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            // Adiciona BOM UTF-8 para compatibilidade com Excel
            outputStream.write(0xEF);
            outputStream.write(0xBB);
            outputStream.write(0xBF);
            
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            CSVWriter csvWriter = new CSVWriter(writer);

            // Cabeçalho do CSV
            String[] header = {"Data", "Tipo", "Categoria", "Valor", "Descrição"};
            csvWriter.writeNext(header);

            // Dados das transações
            for (Transacao transacao : transacoes) {
                String[] linha = {
                    transacao.getData().format(DATE_FORMATTER),
                    transacao.getTipo().toString(),
                    transacao.getCategoria() != null ? transacao.getCategoria().getNome() : "",
                    transacao.getValor().toString(),
                    transacao.getDescricao() != null ? transacao.getDescricao() : ""
                };
                csvWriter.writeNext(linha);
            }

            csvWriter.close();
            writer.close();

            byte[] csvBytes = outputStream.toByteArray();
            logger.info("CSV gerado com sucesso - Tamanho: {} bytes", csvBytes.length);
            
            return new ByteArrayInputStream(csvBytes);
        } catch (IOException e) {
            logger.error("Erro ao gerar CSV: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar arquivo CSV", e);
        }
    }
}
