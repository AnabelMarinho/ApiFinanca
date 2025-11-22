package ufersa.dev.ApiFinanca.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ufersa.dev.ApiFinanca.dto.UpdatePerfilRequest;
import ufersa.dev.ApiFinanca.dto.UpdateSenhaRequest;
import ufersa.dev.ApiFinanca.model.Usuario;
import ufersa.dev.ApiFinanca.model.TransacaoRecorrente;
import ufersa.dev.ApiFinanca.repository.CategoriaRepository;
import ufersa.dev.ApiFinanca.repository.ConfiguracaoUsuarioRepository;
import ufersa.dev.ApiFinanca.repository.TransacaoRecorrenteExecucaoRepository;
import ufersa.dev.ApiFinanca.repository.TransacaoRecorrenteRepository;
import ufersa.dev.ApiFinanca.repository.TransacaoRepository;
import ufersa.dev.ApiFinanca.repository.UsuarioRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PerfilService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransacaoRepository transacaoRepository;
    private final TransacaoRecorrenteRepository transacaoRecorrenteRepository;
    private final TransacaoRecorrenteExecucaoRepository transacaoRecorrenteExecucaoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ConfiguracaoUsuarioRepository configuracaoUsuarioRepository;

    public Usuario atualizarPerfil(UUID userId, UpdatePerfilRequest request) {

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (request.getNome() != null) {
            usuario.setNome(request.getNome());
        }

        if (request.getEmail() != null) {
            usuario.setEmail(request.getEmail());
        }

        if (request.getFaixaSalario() != null) {
            usuario.setFaixaSalario(request.getFaixaSalario());
        }

        return usuarioRepository.save(usuario);
    }

    public void atualizarSenha(UUID userId, UpdateSenhaRequest request) {

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        // Valida senha atual
        if (!passwordEncoder.matches(request.getSenhaAtual(), usuario.getSenha())) {
            throw new RuntimeException("Senha atual incorreta.");
        }

        // Valida se a nova senha e confirmação são iguais
        if (!request.getNovaSenha().equals(request.getConfirmarNovaSenha())) {
            throw new RuntimeException("A nova senha e a confirmação não coincidem.");
        }

        // Valida se a nova senha é diferente da senha atual
        if (passwordEncoder.matches(request.getNovaSenha(), usuario.getSenha())) {
            throw new RuntimeException("A nova senha deve ser diferente da senha atual.");
        }

        usuario.setSenha(passwordEncoder.encode(request.getNovaSenha()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void excluirConta(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        // Deletar transações do usuário
        transacaoRepository.findByUser(usuario).forEach(transacaoRepository::delete);

        // Deletar transações recorrentes do usuário (e suas execuções)
        for (TransacaoRecorrente recorrente : transacaoRecorrenteRepository.findByUser(usuario)) {
            // Deletar execuções da transação recorrente
            transacaoRecorrenteExecucaoRepository.findByTransacaoRecorrente(recorrente)
                    .forEach(transacaoRecorrenteExecucaoRepository::delete);
            // Deletar a transação recorrente
            transacaoRecorrenteRepository.delete(recorrente);
        }

        // Deletar categorias do usuário (apenas as que pertencem ao usuário, não as padrões)
        categoriaRepository.findByUser(usuario).forEach(categoriaRepository::delete);

        // Deletar configurações do usuário (se houver)
        configuracaoUsuarioRepository.findByUsuario_Id(usuarioId).ifPresent(configuracaoUsuarioRepository::delete);

        // Metas já serão deletadas automaticamente devido ao cascade = CascadeType.ALL no relacionamento

        // Por fim, deletar o usuário
        usuarioRepository.delete(usuario);
    }

}
