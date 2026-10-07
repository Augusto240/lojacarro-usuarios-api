package br.org.edu.ifrn.usuarios.controller;

import br.org.edu.ifrn.usuarios.acesso.ControleAcesso;
import br.org.edu.ifrn.usuarios.dto.NomeResponse;
import br.org.edu.ifrn.usuarios.dto.UsuarioRequest;
import br.org.edu.ifrn.usuarios.dto.UsuarioResponse;
import br.org.edu.ifrn.usuarios.model.Usuario;
import br.org.edu.ifrn.usuarios.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);

    private final UsuarioService usuarioService;
    private final ControleAcesso controleAcesso;

    public UsuarioController(UsuarioService usuarioService, ControleAcesso controleAcesso) {
        this.usuarioService = usuarioService;
        this.controleAcesso = controleAcesso;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listar().stream()
                .map(UsuarioResponse::de)
                .toList();
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscar(@PathVariable Long id) {
        return UsuarioResponse.de(usuarioService.buscar(id));
    }

    @GetMapping("/{id}/nome")
    public NomeResponse buscarNome(@PathVariable Long id) {
        log.debug("Pedido do nome do usuario {}", id);
        return new NomeResponse(usuarioService.buscarNome(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@RequestBody UsuarioRequest request,
            @RequestHeader(value = ControleAcesso.HEADER_USUARIO, required = false) Long operadorId) {
        log.info("Pedido para cadastrar usuario '{}' com cargo {}", request.nome(), request.cargo());
        Usuario operador = controleAcesso.exigirGerente(operadorId, "cadastrar usuários");

        Usuario criado = usuarioService.criar(request, operador);
        return ResponseEntity.created(URI.create("/usuarios/" + criado.getId())).body(UsuarioResponse.de(criado));
    }

    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable Long id, @RequestBody UsuarioRequest request,
            @RequestHeader(value = ControleAcesso.HEADER_USUARIO, required = false) Long operadorId) {
        log.info("Pedido para atualizar usuario {} para nome='{}' cargo={}", id, request.nome(), request.cargo());
        Usuario operador = controleAcesso.exigirGerente(operadorId, "editar usuários");

        return UsuarioResponse.de(usuarioService.atualizar(id, request, operador));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id,
            @RequestHeader(value = ControleAcesso.HEADER_USUARIO, required = false) Long operadorId) {
        log.info("Pedido para excluir usuario {}", id);
        Usuario operador = controleAcesso.exigirGerente(operadorId, "excluir usuários");

        usuarioService.excluir(id, operador);
        return ResponseEntity.noContent().build();
    }
}
