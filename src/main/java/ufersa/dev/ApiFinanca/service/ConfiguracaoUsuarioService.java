package ufersa.dev.ApiFinanca.service;

import org.springframework.stereotype.Service;
import ufersa.dev.ApiFinanca.model.ConfiguracaoUsuario;
import ufersa.dev.ApiFinanca.repository.ConfiguracaoUsuarioRepository;

import java.util.Optional;
import java.util.UUID;

@Service
public class ConfiguracaoUsuarioService {

    private final ConfiguracaoUsuarioRepository configuracaoRepo;

    public ConfiguracaoUsuarioService(ConfiguracaoUsuarioRepository configuracaoRepo) {
        this.configuracaoRepo = configuracaoRepo;
    }

    public ConfiguracaoUsuario salvarConfiguracao(ConfiguracaoUsuario config) {
        return configuracaoRepo.save(config);
    }

    public Optional<ConfiguracaoUsuario> getConfiguracaoUsuario(UUID usuarioId) {
        return configuracaoRepo.findByUsuario_Id(usuarioId);
    }
}
