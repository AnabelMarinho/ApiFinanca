package ufersa.dev.ApiFinanca.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ufersa.dev.ApiFinanca.model.ConfiguracaoUsuario;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfiguracaoUsuarioRepository extends JpaRepository<ConfiguracaoUsuario, UUID> {
    Optional<ConfiguracaoUsuario> findByUsuario_Id(UUID usuarioId);
}
