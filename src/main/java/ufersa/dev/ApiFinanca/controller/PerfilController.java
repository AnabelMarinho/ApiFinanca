package ufersa.dev.ApiFinanca.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ufersa.dev.ApiFinanca.dto.UpdatePerfilRequest;
import ufersa.dev.ApiFinanca.dto.UpdateSenhaRequest;
import ufersa.dev.ApiFinanca.model.Usuario;
import ufersa.dev.ApiFinanca.service.PerfilService;

import java.util.UUID;

@RestController
@RequestMapping("/api/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService perfilService;

    @PutMapping
    public ResponseEntity<Usuario> atualizarPerfil(
            @RequestParam UUID userId,
            @RequestBody UpdatePerfilRequest request) {

        Usuario atualizado = perfilService.atualizarPerfil(userId, request);
        return ResponseEntity.ok(atualizado);
    }

    @PutMapping("/senha")
    public ResponseEntity<String> atualizarSenha(
            @RequestParam UUID userId,
            @RequestBody UpdateSenhaRequest request) {

        perfilService.atualizarSenha(userId, request);
        return ResponseEntity.ok("Senha atualizada com sucesso.");
    }

    @DeleteMapping
    public ResponseEntity<String> excluirConta(@RequestParam UUID userId) {

        perfilService.excluirConta(userId);
        return ResponseEntity.ok("Conta excluída com sucesso.");
    }
}
