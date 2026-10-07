package br.org.edu.ifrn.usuarios.config;

import br.org.edu.ifrn.usuarios.model.Cargo;
import br.org.edu.ifrn.usuarios.model.Usuario;
import br.org.edu.ifrn.usuarios.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CargaInicialUsuarios implements ApplicationRunner {

    static final String NOME_GERENTE_PADRAO = "Administrador";

    private static final Logger log = LoggerFactory.getLogger(CargaInicialUsuarios.class);

    private final UsuarioRepository usuarioRepository;

    public CargaInicialUsuarios(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        long total = usuarioRepository.count();
        if (total > 0) {
            log.info("Banco ja tem {} usuario(s)", total);
            return;
        }
        Usuario gerente = usuarioRepository.save(new Usuario(NOME_GERENTE_PADRAO, Cargo.GERENTE));
        log.warn("Nenhum usuario cadastrado. Criei o gerente padrao '{}' com id {}", gerente.getNome(), gerente.getId());
    }
}
