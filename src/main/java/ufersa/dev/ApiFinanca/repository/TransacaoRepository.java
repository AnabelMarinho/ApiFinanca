package ufersa.dev.ApiFinanca.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
import java.util.UUID;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, UUID> {

    // Buscar transações por usuário
    List<Transacao> findByUser(Usuario user);

    // Buscar transações por usuário ordenadas por data decrescente
    List<Transacao> findByUserOrderByDataDesc(Usuario user);

    // Buscar transações por usuário e tipo
    List<Transacao> findByUserAndTipo(Usuario user, TipoTransacao tipo);

    // Buscar transações por usuário e categoria
    List<Transacao> findByUserAndCategoria(Usuario user, Categoria categoria);

    // Buscar transações por usuário em um período específico (compara apenas a parte da data)
    @Query("SELECT t FROM Transacao t WHERE t.user = :user AND FUNCTION('DATE', t.data) BETWEEN :dataInicio AND :dataFim")
    List<Transacao> findByUserAndDataBetween(@Param("user") Usuario user, @Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    // Buscar transações por usuário, tipo e período (compara apenas a parte da data)
    @Query("SELECT t FROM Transacao t WHERE t.user = :user AND t.tipo = :tipo AND FUNCTION('DATE', t.data) BETWEEN :dataInicio AND :dataFim")
    List<Transacao> findByUserAndTipoAndDataBetween(@Param("user") Usuario user, @Param("tipo") TipoTransacao tipo, @Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    // Buscar transações por usuário e categoria em um período (compara apenas a parte da data)
    @Query("SELECT t FROM Transacao t WHERE t.user = :user AND t.categoria = :categoria AND FUNCTION('DATE', t.data) BETWEEN :dataInicio AND :dataFim")
    List<Transacao> findByUserAndCategoriaAndDataBetween(@Param("user") Usuario user, @Param("categoria") Categoria categoria, @Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    // Calcular soma total de transações por usuário e tipo em um período (compara apenas a parte da data)
    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t WHERE t.user = :user AND t.tipo = :tipo AND FUNCTION('DATE', t.data) BETWEEN :dataInicio AND :dataFim")
    BigDecimal calcularTotalPorTipoEPeriodo(@Param("user") Usuario user, @Param("tipo") TipoTransacao tipo, @Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    // Calcular soma total de transações por usuário e categoria em um período (compara apenas a parte da data)
    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t WHERE t.user = :user AND t.categoria = :categoria AND FUNCTION('DATE', t.data) BETWEEN :dataInicio AND :dataFim")
    BigDecimal calcularTotalPorCategoriaEPeriodo(@Param("user") Usuario user, @Param("categoria") Categoria categoria, @Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    // Buscar transações mais recentes do usuário (limitado)
    List<Transacao> findTop10ByUserOrderByDataDesc(Usuario user);

    // Calcular soma total de receitas do usuário
    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t WHERE t.user = :user AND t.tipo = :tipo")
    BigDecimal calcularTotalReceitas(@Param("user") Usuario user, @Param("tipo") TipoTransacao tipo);
    
    default BigDecimal calcularTotalReceitas(Usuario user) {
        return calcularTotalReceitas(user, TipoTransacao.RECEITA);
    }

    // Calcular soma total de despesas do usuário
    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t WHERE t.user = :user AND t.tipo = :tipo")
    BigDecimal calcularTotalDespesas(@Param("user") Usuario user, @Param("tipo") TipoTransacao tipo);
    
    default BigDecimal calcularTotalDespesas(Usuario user) {
        return calcularTotalDespesas(user, TipoTransacao.DESPESA);
    }

    // Deletar todas as transações de um usuário (query nativa para garantir execução)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM transacoes WHERE user_id = :userId", nativeQuery = true)
    void deleteAllByUserId(@Param("userId") UUID userId);

    // Verificar se já existe transação com características similares criada hoje
    // Usado para evitar duplicação em race conditions ao processar recorrências
    @Query("SELECT COUNT(t) > 0 FROM Transacao t WHERE t.user = :user AND t.categoria = :categoria " +
           "AND t.tipo = :tipo AND t.valor = :valor AND t.descricao = :descricao " +
           "AND FUNCTION('DATE', t.data) = :data")
    boolean existsTransacaoSimilar(@Param("user") Usuario user, 
                                   @Param("categoria") Categoria categoria,
                                   @Param("tipo") TipoTransacao tipo,
                                   @Param("valor") BigDecimal valor,
                                   @Param("descricao") String descricao,
                                   @Param("data") LocalDate data);

}

