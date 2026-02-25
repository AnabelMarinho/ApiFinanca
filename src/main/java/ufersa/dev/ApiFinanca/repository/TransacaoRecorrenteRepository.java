package ufersa.dev.ApiFinanca.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ufersa.dev.ApiFinanca.model.Categoria;
import ufersa.dev.ApiFinanca.model.TipoTransacao;
import ufersa.dev.ApiFinanca.model.TransacaoRecorrente;
import ufersa.dev.ApiFinanca.model.Usuario;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransacaoRecorrenteRepository extends JpaRepository<TransacaoRecorrente, UUID> {

    // Buscar transações recorrentes por usuário
    List<TransacaoRecorrente> findByUser(Usuario user);

    // Buscar transações recorrentes ativas por usuário
    List<TransacaoRecorrente> findByUserAndAtivaTrue(Usuario user);

    // Buscar transações recorrentes inativas por usuário
    List<TransacaoRecorrente> findByUserAndAtivaFalse(Usuario user);

    // Buscar transações recorrentes por usuário e tipo
    List<TransacaoRecorrente> findByUserAndTipo(Usuario user, TipoTransacao tipo);

    // Buscar transações recorrentes ativas por usuário e tipo
    List<TransacaoRecorrente> findByUserAndTipoAndAtivaTrue(Usuario user, TipoTransacao tipo);

    // Buscar transações recorrentes por usuário e categoria
    List<TransacaoRecorrente> findByUserAndCategoria(Usuario user, Categoria categoria);

    // Buscar transações recorrentes ativas por categoria
    List<TransacaoRecorrente> findByUserAndCategoriaAndAtivaTrue(Usuario user, Categoria categoria);

    // Buscar transações recorrentes por dia de recorrência
    List<TransacaoRecorrente> findByUserAndDiaRecorrencia(Usuario user, int diaRecorrencia);

    // Buscar todas as transações recorrentes ativas (para job futuro)
    List<TransacaoRecorrente> findByAtivaTrue();

    // Buscar todas as transações recorrentes ativas por dia de recorrência (para job futuro)
    List<TransacaoRecorrente> findByAtivaTrueAndDiaRecorrencia(int diaRecorrencia);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE TransacaoRecorrente t SET t.categoria = :novaCategoria WHERE t.categoria = :categoria AND t.tipo = :tipo")
    void atualizarCategoriaPorTipo(@Param("categoria") Categoria categoria,
                                   @Param("novaCategoria") Categoria novaCategoria,
                                   @Param("tipo") TipoTransacao tipo);

}

