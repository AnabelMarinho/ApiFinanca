package ufersa.dev.ApiFinanca.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ufersa.dev.ApiFinanca.dto.MetaRequest;
import ufersa.dev.ApiFinanca.dto.MetaResponse;
import ufersa.dev.ApiFinanca.model.Meta;
import ufersa.dev.ApiFinanca.model.Usuario;
import ufersa.dev.ApiFinanca.repository.MetaRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MetaService {

    private final MetaRepository metaRepository;

    public MetaResponse criarMeta(MetaRequest request, Usuario usuario) {
        Meta meta = new Meta(
                request.getNome(),
                request.getValorAlvo(),
                request.getDataAlvo(),
                usuario
        );

        Meta metaSalva = metaRepository.save(meta);

        return new MetaResponse(
                metaSalva.getId(),
                metaSalva.getNome(),
                metaSalva.getValorAlvo(),
                metaSalva.getValorAtual(),
                metaSalva.getDataAlvo(),
                null // progresso entra depois na #240
        );
    }

    public List<MetaResponse> listarMetas(UUID usuarioId) {
        return metaRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(meta -> new MetaResponse(
                        meta.getId(),
                        meta.getNome(),
                        meta.getValorAlvo(),
                        meta.getValorAtual(),
                        meta.getDataAlvo(),
                        null
                ))
                .toList();
    }

    public MetaResponse buscarPorId(UUID id) {
        Meta meta = metaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meta não encontrada"));

        return new MetaResponse(
                meta.getId(),
                meta.getNome(),
                meta.getValorAlvo(),
                meta.getValorAtual(),
                meta.getDataAlvo(),
                null
        );
    }

    public MetaResponse atualizarMeta(UUID id, MetaRequest request) {
        Meta meta = metaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meta não encontrada"));

        meta.setNome(request.getNome());
        meta.setValorAlvo(request.getValorAlvo());
        meta.setDataAlvo(request.getDataAlvo());

        Meta metaAtualizada = metaRepository.save(meta);

        return new MetaResponse(
                metaAtualizada.getId(),
                metaAtualizada.getNome(),
                metaAtualizada.getValorAlvo(),
                metaAtualizada.getValorAtual(),
                metaAtualizada.getDataAlvo(),
                null
        );
    }

    public void excluirMeta(UUID id) {
        Meta meta = metaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meta não encontrada"));

        metaRepository.delete(meta);
    }
}

