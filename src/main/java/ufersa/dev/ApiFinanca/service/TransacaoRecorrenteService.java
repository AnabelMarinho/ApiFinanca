package ufersa.dev.ApiFinanca.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import ufersa.dev.ApiFinanca.dto.TransacaoRecorrenteRequest;
import ufersa.dev.ApiFinanca.model.Categoria;
import ufersa.dev.ApiFinanca.model.TipoTransacao;
import ufersa.dev.ApiFinanca.model.TransacaoRecorrente;
import ufersa.dev.ApiFinanca.model.Usuario;
import ufersa.dev.ApiFinanca.repository.CategoriaRepository;
import ufersa.dev.ApiFinanca.repository.TransacaoRecorrenteRepository;
import ufersa.dev.ApiFinanca.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransacaoRecorrenteService {

    private final TransacaoRecorrenteRepository transacaoRecorrenteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;

    public TransacaoRecorrenteService(TransacaoRecorrenteRepository transacaoRecorrenteRepository,
                                      UsuarioRepository usuarioRepository,
                                      CategoriaRepository categoriaRepository) {
        this.transacaoRecorrenteRepository = transacaoRecorrenteRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<TransacaoRecorrente> getAll() {
        return transacaoRecorrenteRepository.findAll();
    }

    public Optional<TransacaoRecorrente> getById(Long id) {
        return transacaoRecorrenteRepository.findById(id);
    }

    public TransacaoRecorrente save(TransacaoRecorrenteRequest request) {
        TransacaoRecorrente transacaoRecorrente = new TransacaoRecorrente();
        applyRequestToEntity(request, transacaoRecorrente);
        return transacaoRecorrenteRepository.save(transacaoRecorrente);
    }

    public TransacaoRecorrente update(Long id, TransacaoRecorrenteRequest request) {
        TransacaoRecorrente transacaoRecorrente = transacaoRecorrenteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transação recorrente não encontrada para o id " + id));
        applyRequestToEntity(request, transacaoRecorrente);
        return transacaoRecorrenteRepository.save(transacaoRecorrente);
    }

    public void delete(Long id) {
        if (!transacaoRecorrenteRepository.existsById(id)) {
            throw new EntityNotFoundException("Transação recorrente não encontrada para o id " + id);
        }
        transacaoRecorrenteRepository.deleteById(id);
    }

    public List<TransacaoRecorrente> getByUser(Usuario usuario) {
        return transacaoRecorrenteRepository.findByUser(usuario);
    }

    public List<TransacaoRecorrente> getAtivasByUser(Usuario usuario) {
        return transacaoRecorrenteRepository.findByUserAndAtivaTrue(usuario);
    }

    public List<TransacaoRecorrente> getInativasByUser(Usuario usuario) {
        return transacaoRecorrenteRepository.findByUserAndAtivaFalse(usuario);
    }

    public List<TransacaoRecorrente> getByUserAndTipo(Usuario usuario, TipoTransacao tipo) {
        return transacaoRecorrenteRepository.findByUserAndTipo(usuario, tipo);
    }

    public List<TransacaoRecorrente> getAtivasByUserAndTipo(Usuario usuario, TipoTransacao tipo) {
        return transacaoRecorrenteRepository.findByUserAndTipoAndAtivaTrue(usuario, tipo);
    }

    public List<TransacaoRecorrente> getByUserAndCategoria(Usuario usuario, Categoria categoria) {
        return transacaoRecorrenteRepository.findByUserAndCategoria(usuario, categoria);
    }

    public List<TransacaoRecorrente> getAtivasByUserAndCategoria(Usuario usuario, Categoria categoria) {
        return transacaoRecorrenteRepository.findByUserAndCategoriaAndAtivaTrue(usuario, categoria);
    }

    public List<TransacaoRecorrente> getByUserAndDiaRecorrencia(Usuario usuario, int diaRecorrencia) {
        return transacaoRecorrenteRepository.findByUserAndDiaRecorrencia(usuario, diaRecorrencia);
    }

    public List<TransacaoRecorrente> getAtivas() {
        return transacaoRecorrenteRepository.findByAtivaTrue();
    }

    public List<TransacaoRecorrente> getAtivasPorDiaRecorrencia(int diaRecorrencia) {
        return transacaoRecorrenteRepository.findByAtivaTrueAndDiaRecorrencia(diaRecorrencia);
    }

    private void applyRequestToEntity(TransacaoRecorrenteRequest request, TransacaoRecorrente entity) {
        if (request.getTipo() == null) {
            throw new IllegalArgumentException("tipo é obrigatório");
        }
        if (request.getValor() == null) {
            throw new IllegalArgumentException("valor é obrigatório");
        }

        entity.setTipo(request.getTipo());
        entity.setValor(request.getValor());
        entity.setDescricao(request.getDescricao());

        Integer diaRecorrencia = request.getDiaRecorrencia();
        if (diaRecorrencia == null) {
            throw new IllegalArgumentException("diaRecorrencia é obrigatório");
        }
        entity.setDiaRecorrencia(diaRecorrencia);

        if (request.getAtiva() != null) {
            entity.setAtiva(request.getAtiva());
        }

        UUID userId = request.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("userId é obrigatório");
        }
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado para o id " + userId));
        entity.setUser(usuario);

        Long categoriaId = request.getCategoriaId();
        if (categoriaId == null) {
            throw new IllegalArgumentException("categoriaId é obrigatório");
        }
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada para o id " + categoriaId));
        entity.setCategoria(categoria);
    }
}

