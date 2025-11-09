package ufersa.dev.ApiFinanca.controller;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ufersa.dev.ApiFinanca.dto.CategoriaRequest;
import ufersa.dev.ApiFinanca.model.Categoria;
import ufersa.dev.ApiFinanca.service.CategoriaService;

import java.util.List;

@RestController
@RequestMapping("/categoria")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<Categoria> getAll() {
        return categoriaService.getAll();
    }

    @GetMapping("/{id}")
    public Categoria getById(@PathVariable Long id) {
        return categoriaService.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Categoria create(@RequestBody CategoriaRequest request) {
        try {
            return categoriaService.save(request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @PutMapping("/{id}")
    public Categoria update(@PathVariable Long id, @RequestBody CategoriaRequest request) {
        try {
            return categoriaService.update(id, request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        try {
            categoriaService.delete(id);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }
}

