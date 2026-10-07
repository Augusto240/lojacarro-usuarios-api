package br.org.edu.ifrn.usuarios.service;

import br.org.edu.ifrn.usuarios.dto.UsuarioRequest;
import br.org.edu.ifrn.usuarios.model.Cargo;
import br.org.edu.ifrn.usuarios.model.Usuario;
import br.org.edu.ifrn.usuarios.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class UsuarioService {

    static final int TAMANHO_MAXIMO_NOME = 100;

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);
    private static final Logger auditoria = LoggerFactory.getLogger("AUDITORIA");
    private static final String HTML = "(?i)(<[^>]*>|javascript:|onerror=|onload=)";

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listar() {
        List<Usuario> usuarios = usuarioRepository.findAllByOrderByNomeAsc();
        log.info("Listagem devolveu {} usuario(s)", usuarios.size());
        return usuarios;
    }

    public Usuario buscar(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> {
            log.warn("Usuario {} nao encontrado", id);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário " + id + " não encontrado");
        });
    }

    public String buscarNome(Long id) {
        String nome = buscar(id).getNome();
        log.info("Nome do usuario {} consultado: {}", id, nome);
        return nome;
    }

    public Usuario criar(UsuarioRequest request, Usuario operador) {
        String nome = validarNome(request.nome());
        Cargo cargo = converterCargo(request.cargo());

        Usuario novo = usuarioRepository.save(new Usuario(nome, cargo));

        log.info("Usuario '{}' cadastrado com id {} e cargo {} por {}", novo.getNome(), novo.getId(), cargo,
                operador.getNome());
        auditoria.info("acao=USUARIO_CRIADO por=\"{}\" usuario=\"{}\"", operador.getNome(), novo);
        return novo;
    }

    public Usuario atualizar(Long id, UsuarioRequest request, Usuario operador) {
        Usuario usuario = buscar(id);
        String nome = validarNome(request.nome());
        Cargo novoCargo = converterCargo(request.cargo());

        boolean rebaixandoGerente = usuario.getCargo() == Cargo.GERENTE && novoCargo != Cargo.GERENTE;
        if (rebaixandoGerente && usuarioRepository.countByCargo(Cargo.GERENTE) <= 1) {
            log.warn("{} tentou tirar o cargo do ultimo gerente ({})", operador.getNome(), usuario.getNome());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não é possível tirar o cargo do último gerente");
        }

        String antes = usuario.toString();
        usuario.setNome(nome);
        usuario.setCargo(novoCargo);
        Usuario salvo = usuarioRepository.save(usuario);

        log.info("Usuario {} atualizado por {}: {} -> {}", id, operador.getNome(), antes, salvo);
        auditoria.info("acao=USUARIO_ATUALIZADO por=\"{}\" antes=\"{}\" depois=\"{}\"", operador.getNome(), antes, salvo);
        return salvo;
    }

    public void excluir(Long id, Usuario operador) {
        Usuario usuario = buscar(id);

        if (usuario.getId().equals(operador.getId())) {
            log.warn("{} tentou excluir o proprio usuario", operador.getNome());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Você não pode excluir o seu próprio usuário");
        }

        usuarioRepository.delete(usuario);

        log.info("Usuario {} excluido por {}", usuario, operador.getNome());
        auditoria.info("acao=USUARIO_EXCLUIDO por=\"{}\" usuario=\"{}\"", operador.getNome(), usuario);
    }

    private String validarNome(String nomeRecebido) {
        String nome = nomeRecebido == null ? "" : nomeRecebido.replaceAll(HTML, "").trim();

        if (nome.isEmpty()) {
            log.warn("Nome vazio recebido");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome é obrigatório");
        }
        if (nome.length() > TAMANHO_MAXIMO_NOME) {
            log.warn("Nome recusado por ter {} caracteres", nome.length());
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "Nome deve ter no máximo " + TAMANHO_MAXIMO_NOME + " caracteres");
        }
        if (!nome.equals(nomeRecebido.trim())) {
            log.warn("HTML removido do nome recebido (possivel XSS)");
        }
        return nome;
    }

    private Cargo converterCargo(String cargo) {
        if (cargo == null || cargo.isBlank()) {
            log.warn("Cargo vazio recebido");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cargo é obrigatório");
        }
        try {
            return Cargo.valueOf(cargo.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            log.warn("Cargo invalido recebido: '{}'", cargo);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cargo inválido. Use GERENTE ou VENDEDOR");
        }
    }
}
