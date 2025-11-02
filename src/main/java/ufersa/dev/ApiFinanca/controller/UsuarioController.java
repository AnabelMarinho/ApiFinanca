package ufersa.dev.ApiFinanca.controller;
import org.springframework.web.bind.annotation.*;
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
    public Usuario create(@RequestBody Usuario usuario) {return usuarioService.save(usuario);}

}
