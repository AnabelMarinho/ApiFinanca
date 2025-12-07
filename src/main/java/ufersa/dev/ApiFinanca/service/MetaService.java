package ufersa.dev.ApiFinanca.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ufersa.dev.ApiFinanca.dto.AporteRequest;
import ufersa.dev.ApiFinanca.dto.AporteResponse;
import ufersa.dev.ApiFinanca.dto.MetaRequest;
import ufersa.dev.ApiFinanca.dto.MetaResponse;
import ufersa.dev.ApiFinanca.model.AporteMeta;
import ufersa.dev.ApiFinanca.model.Meta;
import ufersa.dev.ApiFinanca.model.Usuario;
import ufersa.dev.ApiFinanca.repository.AporteMetaRepository;
import ufersa.dev.ApiFinanca.repository.MetaRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class MetaService {

    private final MetaRepository metaRepository;
    private final AporteMetaRepository aporteMetaRepository;

    public MetaResponse criarMeta(MetaRequest request, Usuario usuario) {
        Meta meta = new Meta(
                request.getNome(),
                request.getValorAlvo(),
                request.getDataAlvo(),
                usuario
        );
        Meta metaSalva = metaRepository.save(meta);
        return mapToMetaResponse(metaSalva);
    }

    public List<MetaResponse> listarMetas(Usuario usuario) {
        return metaRepository.findByUsuarioId(usuario.getId())
                .stream()
                .map(this::mapToMetaResponse)
                .toList();
    }

    public MetaResponse buscarPorId(UUID id, Usuario usuario) {
        Meta meta = metaRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Meta não encontrada"));
        return mapToMetaResponse(meta);
    }

    public MetaResponse atualizarMeta(UUID id, MetaRequest request, Usuario usuario) {
        Meta meta = metaRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Meta não encontrada"));
        meta.setNome(request.getNome());
        meta.setValorAlvo(request.getValorAlvo());
        meta.setDataAlvo(request.getDataAlvo());
        Meta metaAtualizada = metaRepository.save(meta);
        return mapToMetaResponse(metaAtualizada);
    }

    public void excluirMeta(UUID id, Usuario usuario) {
        Meta meta = metaRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Meta não encontrada"));
        metaRepository.delete(meta);
    }

    public MetaResponse adicionarAporte(UUID metaId, AporteRequest request, Usuario usuario) {
        Meta meta = metaRepository.findByIdAndUsuario(metaId, usuario)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Meta não encontrada"));
        AporteMeta aporte = new AporteMeta();
        aporte.setValor(request.valor());
        aporte.setMeta(meta);
        aporte.setData(request.data());
        aporteMetaRepository.save(aporte);
        meta.setValorAtual(meta.getValorAtual().add(request.valor()));
        metaRepository.save(meta);
        return mapToMetaResponse(meta);
    }

    private MetaResponse mapToMetaResponse(Meta meta) {
        BigDecimal progresso = calcularProgresso(meta);
        List<AporteResponse> aportesResponse = aporteMetaRepository.findByMetaId(meta.getId())
                .stream()
                .map(a -> new AporteResponse(a.getValor(), a.getData()))
                .toList();
        boolean concluida = meta.getValorAtual().compareTo(meta.getValorAlvo()) >= 0;
        BigDecimal aporteMensalSugerido = calcularAporteMensal(meta, 12);

        return new MetaResponse(
                meta.getId(),
                meta.getNome(),
                meta.getValorAlvo(),
                meta.getValorAtual(),
                meta.getDataAlvo(),
                progresso,
                aportesResponse,
                concluida,
                aporteMensalSugerido
        );
    }


    private BigDecimal calcularProgresso(Meta meta) {
        if (meta.getValorAlvo() == null || meta.getValorAlvo().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal progresso = meta.getValorAtual()
                .divide(meta.getValorAlvo(), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        return progresso.min(new BigDecimal("100"));
    }

    public BigDecimal calcularAporteMensal(Meta meta, int mesesRestantes) {
        if (mesesRestantes <= 0) return BigDecimal.ZERO;
        BigDecimal restante = meta.getValorAlvo().subtract(meta.getValorAtual());
        if (restante.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return restante.divide(new BigDecimal(mesesRestantes), 2, RoundingMode.HALF_UP);
    }

    public MetaResponse buscarMetaComAportes(UUID id, Usuario usuario) {
        Meta meta = metaRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Meta não encontrada"));
        return mapToMetaResponse(meta);
    }
}
