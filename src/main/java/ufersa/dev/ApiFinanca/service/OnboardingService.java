package ufersa.dev.ApiFinanca.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ufersa.dev.ApiFinanca.dto.OnboardingRequest;
import ufersa.dev.ApiFinanca.dto.TransacaoRecorrenteOnboardingRequest;
import ufersa.dev.ApiFinanca.model.TipoTransacao;
import ufersa.dev.ApiFinanca.model.Usuario;
import ufersa.dev.ApiFinanca.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OnboardingService {

    private final UsuarioRepository usuarioRepository;
    private final TransacaoRecorrenteService transacaoRecorrenteService;

    public OnboardingService(UsuarioRepository usuarioRepository,
                             TransacaoRecorrenteService transacaoRecorrenteService) {
        this.usuarioRepository = usuarioRepository;
        this.transacaoRecorrenteService = transacaoRecorrenteService;
    }

    @Transactional
    public Usuario concluirOnboarding(String email, OnboardingRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado para email: " + email));

        return concluirOnboardingParaUsuario(usuario, request);
    }

    public boolean statusOnboarding(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado para email: " + email));

        return statusOnboarding(usuario.getId());
    }

    @Transactional
    public Usuario concluirOnboarding(UUID usuarioId, OnboardingRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + usuarioId));
        return concluirOnboardingParaUsuario(usuario, request);
    }

    private Usuario concluirOnboardingParaUsuario(Usuario usuario, OnboardingRequest request) {

        if (Boolean.FALSE.equals(usuario.getPrimeiroAcesso())) {
            throw new IllegalStateException("Onboarding já concluído para este usuário.");
        }

        if (request.getDataInicioControle() == null) {
            throw new IllegalArgumentException("A data de início do controle é obrigatória.");
        }
        int diaDoMes = request.getDataInicioControle().getDayOfMonth();
        if (diaDoMes > 28) {
            throw new IllegalArgumentException("O dia de início deve estar entre 1 e 28.");
        }

        List<TransacaoRecorrenteOnboardingRequest> lista = request.getTransacoesRecorrentes();
        if (lista != null && !lista.isEmpty()) {
            for (TransacaoRecorrenteOnboardingRequest t : lista) {

                if (t.getTipo() == null) throw new IllegalArgumentException("Tipo da transação é obrigatório.");
                if (t.getValor() == null) throw new IllegalArgumentException("Valor da transação é obrigatório.");

                TipoTransacao tipo = t.getTipo();
                int diaRecorrencia = (t.getDiaRecorrencia() != null) ? t.getDiaRecorrencia() : diaDoMes;

                UUID categoriaId = t.getCategoriaId();

                transacaoRecorrenteService.saveFromOnboarding(
                        usuario,
                        tipo,
                        t.getValor(),
                        categoriaId,
                        t.getDescricao(),
                        diaRecorrencia
                );
            }
        }

        usuario.setDataInicioControle(request.getDataInicioControle());
        usuario.setPrimeiroAcesso(false);
        usuario.setDataAtualizacao(LocalDateTime.now());
        
        // Salva o saldo inicial e atual se informado, caso contrário mantém null ou zero
        BigDecimal saldoInicial = request.getSaldoAtual() != null ? request.getSaldoAtual() : BigDecimal.ZERO;
        usuario.setSaldoInicial(saldoInicial);
        usuario.setSaldoAtual(saldoInicial);

        return usuarioRepository.save(usuario);
    }

    public boolean statusOnboarding(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + usuarioId));

        return !Boolean.TRUE.equals(usuario.getPrimeiroAcesso());
    }

}
