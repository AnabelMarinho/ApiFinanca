package ufersa.dev.ApiFinanca.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ufersa.dev.ApiFinanca.model.TransacaoRecorrente;
import ufersa.dev.ApiFinanca.model.TransacaoRecorrenteExecucao;

import java.time.LocalDate;
import java.util.UUID;

import java.util.List;

@Repository
public interface TransacaoRecorrenteExecucaoRepository extends JpaRepository<TransacaoRecorrenteExecucao, UUID> {

    boolean existsByTransacaoRecorrenteAndDataExecucao(TransacaoRecorrente transacaoRecorrente, LocalDate dataExecucao);
    
    List<TransacaoRecorrenteExecucao> findByTransacaoRecorrente(TransacaoRecorrente transacaoRecorrente);
}


