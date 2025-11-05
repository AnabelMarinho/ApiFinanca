package ufersa.dev.ApiFinanca.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ufersa.dev.ApiFinanca.model.Categoria;
import ufersa.dev.ApiFinanca.model.TipoTransacao;
import ufersa.dev.ApiFinanca.model.Transacao;
import ufersa.dev.ApiFinanca.model.Usuario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    // Buscar transações por usuário
    List<Transacao> findByUser(Usuario user);

    // Buscar transações por usuário ordenadas por data decrescente
    List<Transacao> findByUserOrderByDataDesc(Usuario user);

    // Buscar transações por usuário e tipo
    List<Transacao> findByUserAndTipo(Usuario user, TipoTransacao tipo);

    // Buscar transações por usuário e categoria
    List<Transacao> findByUserAndCategoria(Usuario user, Categoria categoria);

    // Buscar transações por usuário em um período específico
    List<Transacao> findByUserAndDataBetween(Usuario user, LocalDate dataInicio, LocalDate dataFim);

    // Buscar transações por usuário, tipo e período
    List<Transacao> findByUserAndTipoAndDataBetween(Usuario user, TipoTransacao tipo, LocalDate dataInicio, LocalDate dataFim);

    // Buscar transações por usuário e categoria em um período
    List<Transacao> findByUserAndCategoriaAndDataBetween(Usuario user, Categoria categoria, LocalDate dataInicio, LocalDate dataFim);

    // Calcular soma total de transações por usuário e tipo em um período
    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t WHERE t.user = :user AND t.tipo = :tipo AND t.data BETWEEN :dataInicio AND :dataFim")
    BigDecimal calcularTotalPorTipoEPeriodo(@Param("user") Usuario user, @Param("tipo") TipoTransacao tipo, @Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    // Calcular soma total de transações por usuário e categoria em um período
    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t WHERE t.user = :user AND t.categoria = :categoria AND t.data BETWEEN :dataInicio AND :dataFim")
    BigDecimal calcularTotalPorCategoriaEPeriodo(@Param("user") Usuario user, @Param("categoria") Categoria categoria, @Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    // Buscar transações mais recentes do usuário (limitado)
    List<Transacao> findTop10ByUserOrderByDataDesc(Usuario user);

}

