package ufersa.dev.ApiFinanca.service;

import ufersa.dev.ApiFinanca.model.Usuario;
import ufersa.dev.ApiFinanca.repository.UsuarioRepository;

import java.util.List;

public class UsuarioService {

    private UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Listar
    public List<Usuario> getAll() {return usuarioRepository.findAll();}

    // Criar
    public Usuario save(Usuario usuario) {return usuarioRepository.save(usuario);}

}
