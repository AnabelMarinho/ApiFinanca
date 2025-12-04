package ufersa.dev.ApiFinanca.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ufersa.dev.ApiFinanca.model.Alerta;
import ufersa.dev.ApiFinanca.model.TipoAlerta;
import ufersa.dev.ApiFinanca.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, UUID> {
    
    // Buscar alertas não vistos do usuário
    List<Alerta> findByUsuario_IdAndVistoFalseOrderByDataCriacaoDesc(UUID usuarioId);
    
    // Buscar todos os alertas do usuário
    List<Alerta> findByUsuario_IdOrderByDataCriacaoDesc(UUID usuarioId);
    
    // Verificar se já existe alerta do tipo para o usuário em determinado período
    @Query("SELECT a FROM Alerta a WHERE a.usuario = :usuario AND a.tipoAlerta = :tipo " +
           "AND a.referenciaPeriodo = :periodo")
    Optional<Alerta> findByUsuarioAndTipoAlertaAndReferenciaPeriodo(
            @Param("usuario") Usuario usuario,
            @Param("tipo") TipoAlerta tipo,
            @Param("periodo") String periodo
    );
    
    // Verificar se já existe alerta do tipo para meta específica
    @Query("SELECT a FROM Alerta a WHERE a.usuario = :usuario AND a.tipoAlerta = :tipo " +
           "AND a.metaId = :metaId AND a.dataCriacao > :dataMinima")
    Optional<Alerta> findRecentByUsuarioAndTipoAndMeta(
            @Param("usuario") Usuario usuario,
            @Param("tipo") TipoAlerta tipo,
            @Param("metaId") UUID metaId,
            @Param("dataMinima") LocalDateTime dataMinima
    );
    
    // Verificar se já existe alerta do tipo para categoria específica
    @Query("SELECT a FROM Alerta a WHERE a.usuario = :usuario AND a.tipoAlerta = :tipo " +
           "AND a.categoriaId = :categoriaId AND a.dataCriacao > :dataMinima")
    Optional<Alerta> findRecentByUsuarioAndTipoAndCategoria(
            @Param("usuario") Usuario usuario,
            @Param("tipo") TipoAlerta tipo,
            @Param("categoriaId") UUID categoriaId,
            @Param("dataMinima") LocalDateTime dataMinima
    );
    
    // Verificar se já existe alerta recente do tipo (últimos X dias)
    @Query("SELECT a FROM Alerta a WHERE a.usuario = :usuario AND a.tipoAlerta = :tipo " +
           "AND a.dataCriacao > :dataMinima")
    Optional<Alerta> findRecentByUsuarioAndTipo(
            @Param("usuario") Usuario usuario,
            @Param("tipo") TipoAlerta tipo,
            @Param("dataMinima") LocalDateTime dataMinima
    );
    
    // Contar alertas não vistos
    long countByUsuario_IdAndVistoFalse(UUID usuarioId);
    
    // Deletar alertas antigos (limpeza)
    void deleteByDataCriacaoBefore(LocalDateTime data);
}

