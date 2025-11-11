package ufersa.dev.ApiFinanca.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ufersa.dev.ApiFinanca.dto.TransacaoRecorrenteRequest;
import ufersa.dev.ApiFinanca.model.TransacaoRecorrente;
import ufersa.dev.ApiFinanca.service.TransacaoRecorrenteService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transacao-recorrente")
public class TransacaoRecorrenteController {

    private final TransacaoRecorrenteService transacaoRecorrenteService;

    public TransacaoRecorrenteController(TransacaoRecorrenteService transacaoRecorrenteService) {
        this.transacaoRecorrenteService = transacaoRecorrenteService;
    }

    @GetMapping
    public List<TransacaoRecorrente> getAll() {
        return transacaoRecorrenteService.getAll();
    }

    @GetMapping("/{id}")
    public TransacaoRecorrente getById(@PathVariable UUID id) {
        return transacaoRecorrenteService.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transação recorrente não encontrada"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public TransacaoRecorrente create(@RequestBody TransacaoRecorrenteRequest request) {
        try {
            return transacaoRecorrenteService.save(request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @PutMapping("/{id}")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public TransacaoRecorrente update(@PathVariable UUID id, @RequestBody TransacaoRecorrenteRequest request) {
        try {
            return transacaoRecorrenteService.update(id, request);
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
            transacaoRecorrenteService.delete(id);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }
}

