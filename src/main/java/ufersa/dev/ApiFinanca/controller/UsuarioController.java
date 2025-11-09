package ufersa.dev.ApiFinanca.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ufersa.dev.ApiFinanca.dto.UsuarioRequest;
import ufersa.dev.ApiFinanca.model.Usuario;
import ufersa.dev.ApiFinanca.service.UsuarioService;

import java.util.List;

@RestController
@RequestMapping ("/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<Usuario> getAll() {return usuarioService.getAll();}

    @PostMapping
    public Usuario create(@RequestBody UsuarioRequest request) {
        try {
            return usuarioService.save(request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

}
