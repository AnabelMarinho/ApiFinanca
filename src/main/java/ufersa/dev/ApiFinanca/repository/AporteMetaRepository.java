package ufersa.dev.ApiFinanca.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ufersa.dev.ApiFinanca.model.AporteMeta;

import java.util.List;
import java.util.UUID;

@Repository
public interface AporteMetaRepository extends JpaRepository<AporteMeta,UUID> {
    List<AporteMeta> findByUsuarioId(UUID id);
}
