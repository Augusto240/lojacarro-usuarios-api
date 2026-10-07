package br.org.edu.ifrn.usuarios.acesso;

import br.org.edu.ifrn.usuarios.model.Cargo;
import br.org.edu.ifrn.usuarios.model.Usuario;
import br.org.edu.ifrn.usuarios.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ControleAcesso {

    public static final String HEADER_USUARIO = "X-Usuario-Id";

    private static final Logger log = LoggerFactory.getLogger(ControleAcesso.class);
    private static final Logger auditoria = LoggerFactory.getLogger("AUDITORIA");

    private final UsuarioRepository usuarioRepository;

    public ControleAcesso(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario exigirGerente(Long operadorId, String acao) {
        if (operadorId == null) {
            log.warn("Tentativa de {} sem o header {}", acao, HEADER_USUARIO);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Header " + HEADER_USUARIO + " não informado");
        }

        Usuario operador = usuarioRepository.findById(operadorId).orElseThrow(() -> {
            log.warn("Header {} veio com o usuario {} que nao existe", HEADER_USUARIO, operadorId);
            return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário " + operadorId + " não existe");
        });

        MDC.put("usuario", operador.getNome());

        if (operador.getCargo() != Cargo.GERENTE) {
            log.warn("Acesso negado: {} ({}) tentou {}", operador.getNome(), operador.getCargo(), acao);
            auditoria.info("acao=ACESSO_NEGADO por=\"{}\" tentou=\"{}\"", operador.getNome(), acao);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "O cargo " + operador.getCargo() + " não pode " + acao);
        }

        log.debug("{} liberado para {}", operador.getNome(), acao);
        return operador;
    }
}
