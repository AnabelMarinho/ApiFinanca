package ufersa.dev.ApiFinanca.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ufersa.dev.ApiFinanca.dto.OnboardingRequest;
import ufersa.dev.ApiFinanca.dto.TransacaoRecorrenteOnboardingRequest;
import ufersa.dev.ApiFinanca.model.PreferenciaAlerta;
import ufersa.dev.ApiFinanca.model.TipoAlerta;
import ufersa.dev.ApiFinanca.model.TipoTransacao;
import ufersa.dev.ApiFinanca.model.Usuario;
import ufersa.dev.ApiFinanca.repository.PreferenciaAlertaRepository;
import ufersa.dev.ApiFinanca.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OnboardingService {

    private final UsuarioRepository usuarioRepository;
    private final TransacaoRecorrenteService transacaoRecorrenteService;
    private final PreferenciaAlertaRepository preferenciaAlertaRepository;

    public OnboardingService(UsuarioRepository usuarioRepository,
                             TransacaoRecorrenteService transacaoRecorrenteService,
                             PreferenciaAlertaRepository preferenciaAlertaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.transacaoRecorrenteService = transacaoRecorrenteService;
        this.preferenciaAlertaRepository = preferenciaAlertaRepository;
    }

    @Transactional
    public Usuario concluirOnboarding(String email, OnboardingRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o email informado."));

        return concluirOnboardingParaUsuario(usuario, request);
    }

    public boolean statusOnboarding(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o email informado."));

        return statusOnboarding(usuario.getId());
    }

    @Transactional
    public Usuario concluirOnboarding(UUID usuarioId, OnboardingRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID informado."));
        return concluirOnboardingParaUsuario(usuario, request);
    }

    private Usuario concluirOnboardingParaUsuario(Usuario usuario, OnboardingRequest request) {

        if (Boolean.FALSE.equals(usuario.getPrimeiroAcesso())) {
            throw new IllegalStateException("O onboarding já foi concluído para este usuário.");
        }

        if (request.getDataInicioControle() == null) {
            throw new IllegalArgumentException("A data de início do controle financeiro é obrigatória.");
        }
        int diaDoMes = request.getDataInicioControle().getDayOfMonth();
        if (diaDoMes > 28) {
            throw new IllegalArgumentException("O dia de início do controle deve estar entre 1 e 28.");
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

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        // Inicializar preferências de alertas padrão para o usuário
        inicializarPreferenciasAlertasPadrao(usuarioSalvo);

        return usuarioSalvo;
    }

    /**
     * Inicializa as preferências de alertas com os valores padrão para um novo usuário
     */
    private void inicializarPreferenciasAlertasPadrao(Usuario usuario) {
        // Verificar se já existem preferências
        List<PreferenciaAlerta> preferenciasExistentes = preferenciaAlertaRepository.findByUsuario_Id(usuario.getId());
        
        if (!preferenciasExistentes.isEmpty()) {
            return; // Já tem preferências configuradas
        }

        // Criar preferências padrão para todos os tipos de alertas
        List<PreferenciaAlerta> preferencias = new ArrayList<>();
        
        for (TipoAlerta tipo : TipoAlerta.values()) {
            PreferenciaAlerta preferencia = new PreferenciaAlerta(
                usuario,
                tipo,
                tipo.isAtivoPorPadrao()
            );
            preferencias.add(preferencia);
        }

        preferenciaAlertaRepository.saveAll(preferencias);
    }

    public boolean statusOnboarding(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID informado."));

        return !Boolean.TRUE.equals(usuario.getPrimeiroAcesso());
    }

}
