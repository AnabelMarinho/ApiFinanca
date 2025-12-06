package ufersa.dev.ApiFinanca.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ufersa.dev.ApiFinanca.dto.AporteRequest;
import ufersa.dev.ApiFinanca.dto.MetaRequest;
import ufersa.dev.ApiFinanca.dto.MetaResponse;
import ufersa.dev.ApiFinanca.model.Usuario;
import ufersa.dev.ApiFinanca.service.AuthService;
import ufersa.dev.ApiFinanca.service.MetaService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/metas")
public class MetaController {

    private final MetaService metaService;
    private final AuthService authService;

    // Construtor para injeção de dependências
    public MetaController(MetaService metaService, AuthService authService) {
        this.metaService = metaService;
        this.authService = authService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public MetaResponse criarMeta(@Valid @RequestBody MetaRequest request) {
        try {
            Usuario usuarioLogado = authService.getCurrentUserAsEntity();
            return metaService.criarMeta(request, usuarioLogado);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @GetMapping
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public List<MetaResponse> listarMetas() {
        Usuario usuarioLogado = authService.getCurrentUserAsEntity();
        return metaService.listarMetas(usuarioLogado);
    }

    @GetMapping("/{id}")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public MetaResponse getMetaPorId(@PathVariable UUID id) {
        Usuario usuarioLogado = authService.getCurrentUserAsEntity();
        return metaService.buscarMetaComAportes(id, usuarioLogado);
    }

    @PutMapping("/{id}")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public MetaResponse atualizarMeta(
            @PathVariable UUID id,
            @Valid @RequestBody MetaRequest request) {
        Usuario usuarioLogado = authService.getCurrentUserAsEntity();
        return metaService.atualizarMeta(id, request, usuarioLogado);
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public void excluirMeta(@PathVariable UUID id) {
        Usuario usuarioLogado = authService.getCurrentUserAsEntity();
        metaService.excluirMeta(id, usuarioLogado);
    }

    @PostMapping("/{id}/aportes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public MetaResponse adicionarAporte(
            @PathVariable UUID id,
            @Valid @RequestBody AporteRequest request) {
        Usuario usuarioLogado = authService.getCurrentUserAsEntity();
        return metaService.adicionarAporte(id, request, usuarioLogado);
    }

}
