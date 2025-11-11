package ufersa.dev.ApiFinanca.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ufersa.dev.ApiFinanca.model.Categoria;
import ufersa.dev.ApiFinanca.model.TipoTransacao;
import ufersa.dev.ApiFinanca.model.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {

    // Buscar categorias padrão do sistema (user_id = null)
    List<Categoria> findByUserIsNull();

    // Verificar se categoria padrão existe
    boolean existsByNomeAndTipoAndUserIsNull(String nome, TipoTransacao tipo);

    // Buscar categorias por usuário
    List<Categoria> findByUser(Usuario user);

    // Buscar categorias por usuário e tipo
    List<Categoria> findByUserAndTipo(Usuario user, TipoTransacao tipo);

    // Buscar categorias por tipo (incluindo padrões)
    List<Categoria> findByTipo(TipoTransacao tipo);

    // Buscar todas as categorias disponíveis para um usuário (padrões + próprias)
    @Query("SELECT c FROM Categoria c WHERE c.user IS NULL OR c.user = :user")
    List<Categoria> findCategoriasDisponiveisParaUsuario(@Param("user") Usuario user);

    // Buscar todas as categorias disponíveis para um usuário por tipo
    @Query("SELECT c FROM Categoria c WHERE (c.user IS NULL OR c.user = :user) AND c.tipo = :tipo")
    List<Categoria> findCategoriasDisponiveisParaUsuarioPorTipo(@Param("user") Usuario user, @Param("tipo") TipoTransacao tipo);

    // Buscar categoria por nome e tipo para um usuário específico
    Optional<Categoria> findByNomeAndTipoAndUser(String nome, TipoTransacao tipo, Usuario user);

}

