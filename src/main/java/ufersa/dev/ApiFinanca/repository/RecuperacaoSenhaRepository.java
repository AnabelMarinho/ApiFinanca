package ufersa.dev.ApiFinanca.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ufersa.dev.ApiFinanca.model.RecuperacaoSenha;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecuperacaoSenhaRepository extends JpaRepository<RecuperacaoSenha, UUID> {
    Optional<RecuperacaoSenha> findByEmailAndCodigoAndUsadoFalse(String email, String codigo);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM RecuperacaoSenha r WHERE r.email = :email")
    void deleteByEmail(@Param("email") String email);
}
