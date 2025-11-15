package ufersa.dev.ApiFinanca.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ufersa.dev.ApiFinanca.dto.TransacaoRequest;
import ufersa.dev.ApiFinanca.model.Transacao;
import ufersa.dev.ApiFinanca.service.TransacaoService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transacao")
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @GetMapping
    public List<Transacao> getAll() {
        return transacaoService.getAll();
    }

    @GetMapping("/{id}")
    public Transacao getById(@PathVariable UUID id) {
        return transacaoService.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transação não encontrada"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public Transacao create(@RequestBody TransacaoRequest request) {
        try {
            return transacaoService.save(request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @PutMapping("/{id}")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public Transacao update(@PathVariable UUID id, @RequestBody TransacaoRequest request) {
        try {
            return transacaoService.update(id, request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public void delete(@PathVariable UUID id) {
        try {
            transacaoService.delete(id);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @GetMapping("/buscar-por-usuario/{usuarioId}")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public List<Transacao> buscarPorUsuario(@PathVariable UUID usuarioId) {
        try {
            return transacaoService.getByUser(usuarioId);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }
}

