package ufersa.dev.ApiFinanca.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ufersa.dev.ApiFinanca.model.PreferenciaAlerta;
import ufersa.dev.ApiFinanca.model.TipoAlerta;
import ufersa.dev.ApiFinanca.model.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PreferenciaAlertaRepository extends JpaRepository<PreferenciaAlerta, UUID> {
    
    List<PreferenciaAlerta> findByUsuario_Id(UUID usuarioId);
    
    Optional<PreferenciaAlerta> findByUsuarioAndTipoAlerta(Usuario usuario, TipoAlerta tipoAlerta);
    
    List<PreferenciaAlerta> findByUsuarioAndAtivoTrue(Usuario usuario);
    
    void deleteByUsuario(Usuario usuario);
}

