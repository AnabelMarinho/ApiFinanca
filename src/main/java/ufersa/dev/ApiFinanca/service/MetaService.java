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
import ufersa.dev.ApiFinanca.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class MetaService {

    private final MetaRepository metaRepository;
    private final AporteMetaRepository aporteMetaRepository;
    private final UsuarioRepository usuarioRepository;

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

    @Transactional
    public void excluirMeta(UUID id, Usuario usuario) {
        // Buscar usuário completo do banco para garantir que temos os dados atualizados
        Usuario usuarioCompleto = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuário não encontrado"));
        
        // Buscar meta
        Meta meta = metaRepository.findByIdAndUsuario(id, usuarioCompleto)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Meta não encontrada"));
        
        // Se a meta tiver valorAtual, transferir para o saldoAtual do usuário
        if (meta.getValorAtual() != null && meta.getValorAtual().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal valorAtualMeta = meta.getValorAtual();
            BigDecimal saldoAtualUsuario = usuarioCompleto.getSaldoAtual() != null 
                    ? usuarioCompleto.getSaldoAtual() 
                    : BigDecimal.ZERO;
            
            // Adicionar o valor da meta ao saldo do usuário
            usuarioCompleto.setSaldoAtual(saldoAtualUsuario.add(valorAtualMeta));
            usuarioRepository.save(usuarioCompleto);
        }
        
        // Excluir a meta
        metaRepository.delete(meta);
    }

    @Transactional
    public MetaResponse adicionarAporte(UUID metaId, AporteRequest request, Usuario usuario) {
        // Validar valor do aporte
        if (request.valor() == null || request.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O valor do aporte deve ser maior que zero");
        }
        
        // Buscar usuário completo do banco
        Usuario usuarioCompleto = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuário não encontrado"));
        
        // Buscar meta
        Meta meta = metaRepository.findByIdAndUsuario(metaId, usuarioCompleto)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Meta não encontrada"));
        
        // Criar e salvar aporte
        AporteMeta aporte = new AporteMeta();
        aporte.setValor(request.valor());
        aporte.setMeta(meta);
        aporte.setData(request.data());
        aporteMetaRepository.save(aporte);
        
        // Atualizar valor atual da meta
        meta.setValorAtual(meta.getValorAtual().add(request.valor()));
        metaRepository.save(meta);
        
        // Subtrair do saldo do usuário
        usuarioCompleto.setSaldoAtual(usuarioCompleto.getSaldoAtual().subtract(request.valor()));
        usuarioRepository.save(usuarioCompleto);
        
        return mapToMetaResponse(meta);
    }

    @Transactional
    public MetaResponse removerAporte(UUID metaId, AporteRequest request, Usuario usuario ) {
        // Validar valor da remoção
        if (request.valor() == null || request.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O valor da remoção deve ser maior que zero");
        }
        
        // Buscar usuário completo do banco
        Usuario usuarioCompleto = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuário não encontrado"));
        
        // Buscar meta
        Meta meta = metaRepository.findByIdAndUsuario(metaId, usuarioCompleto)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Meta não encontrada"));
        
        // Verificar se a meta tem saldo suficiente para remoção
        if (meta.getValorAtual().compareTo(request.valor()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Valor insuficiente na meta. Valor atual da meta: " + meta.getValorAtual() + 
                ", valor solicitado para remoção: " + request.valor());
        }
        
        // Criar e salvar aporte negativo (para histórico)
        AporteMeta aporteReverso = new AporteMeta();
        aporteReverso.setValor(request.valor().negate()); // Valor negativo
        aporteReverso.setMeta(meta);
        aporteReverso.setData(request.data());
        aporteMetaRepository.save(aporteReverso);
        
        // Subtrair do valor atual da meta
        meta.setValorAtual(meta.getValorAtual().subtract(request.valor()));
        metaRepository.save(meta);
        
        // Adicionar ao saldo do usuário
        usuarioCompleto.setSaldoAtual(usuarioCompleto.getSaldoAtual().add(request.valor()));
        usuarioRepository.save(usuarioCompleto);
        
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
