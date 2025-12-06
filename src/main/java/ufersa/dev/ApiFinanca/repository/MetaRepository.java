package ufersa.dev.ApiFinanca.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ufersa.dev.ApiFinanca.model.Meta;
import ufersa.dev.ApiFinanca.model.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MetaRepository extends JpaRepository<Meta, UUID> {
    List<Meta> findByUsuarioId(UUID id);
    Optional<Meta> findByIdAndUsuario(UUID id, Usuario usuario);
}
