package br.org.edu.ifrn.usuarios.service;

import br.org.edu.ifrn.usuarios.dto.UsuarioRequest;
import br.org.edu.ifrn.usuarios.model.Cargo;
import br.org.edu.ifrn.usuarios.model.Usuario;
import br.org.edu.ifrn.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario gerente;

    @BeforeEach
    void setUp() {
        gerente = usuario(1L, "Marta", Cargo.GERENTE);
    }

    @Test
    void criarDeveSalvarComNomeLimpoECargoCerto() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario salvo = invocation.getArgument(0);
            salvo.setId(10L);
            return salvo;
        });

        Usuario criado = usuarioService.criar(new UsuarioRequest("  <b>Joao</b> Pedro ", "vendedor"), gerente);

        assertEquals(10L, criado.getId());
        assertEquals("Joao Pedro", criado.getNome());
        assertEquals(Cargo.VENDEDOR, criado.getCargo());
    }

    @Test
    void criarSemNomeDaBadRequest() {
        UsuarioRequest request = new UsuarioRequest("   ", "GERENTE");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> usuarioService.criar(request, gerente));

        assertEquals(400, ex.getStatusCode().value());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void criarComNomeNuloDaBadRequest() {
        UsuarioRequest request = new UsuarioRequest(null, "GERENTE");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> usuarioService.criar(request, gerente));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void criarComNomeGrandeDemaisDa422() {
        UsuarioRequest request = new UsuarioRequest("a".repeat(UsuarioService.TAMANHO_MAXIMO_NOME + 1), "GERENTE");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> usuarioService.criar(request, gerente));

        assertEquals(422, ex.getStatusCode().value());
    }

    @Test
    void criarComCargoInvalidoDaBadRequest() {
        UsuarioRequest request = new UsuarioRequest("Carlos", "DIRETOR");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> usuarioService.criar(request, gerente));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void criarSemCargoDaBadRequest() {
        UsuarioRequest request = new UsuarioRequest("Carlos", " ");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> usuarioService.criar(request, gerente));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void listarDevolveOQueVeioDoBanco() {
        List<Usuario> usuarios = List.of(gerente);
        when(usuarioRepository.findAllByOrderByNomeAsc()).thenReturn(usuarios);

        assertSame(usuarios, usuarioService.listar());
    }

    @Test
    void buscarNomeDevolveSoONome() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(gerente));

        assertEquals("Marta", usuarioService.buscarNome(1L));
    }

    @Test
    void buscarQueNaoExisteDa404() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> usuarioService.buscarNome(99L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void atualizarTrocaNomeECargo() {
        Usuario caio = usuario(2L, "Caio", Cargo.VENDEDOR);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(caio));
        when(usuarioRepository.save(caio)).thenReturn(caio);

        Usuario atualizado = usuarioService.atualizar(2L, new UsuarioRequest("Caio Lima", "GERENTE"), gerente);

        assertEquals("Caio Lima", atualizado.getNome());
        assertEquals(Cargo.GERENTE, atualizado.getCargo());
    }

    @Test
    void naoTiraOCargoDoUltimoGerente() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(gerente));
        when(usuarioRepository.countByCargo(Cargo.GERENTE)).thenReturn(1L);
        UsuarioRequest request = new UsuarioRequest("Marta", "VENDEDOR");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> usuarioService.atualizar(1L, request, gerente));

        assertEquals(409, ex.getStatusCode().value());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rebaixaGerenteQuandoTemOutro() {
        Usuario bruno = usuario(3L, "Bruno", Cargo.GERENTE);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(bruno));
        when(usuarioRepository.countByCargo(Cargo.GERENTE)).thenReturn(2L);
        when(usuarioRepository.save(bruno)).thenReturn(bruno);

        assertEquals(Cargo.VENDEDOR, usuarioService.atualizar(3L, new UsuarioRequest("Bruno", "VENDEDOR"), gerente).getCargo());
    }

    @Test
    void excluirApagaDoBanco() {
        Usuario caio = usuario(2L, "Caio", Cargo.VENDEDOR);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(caio));

        usuarioService.excluir(2L, gerente);

        verify(usuarioRepository).delete(caio);
    }

    @Test
    void naoExcluiOProprioUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(gerente));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> usuarioService.excluir(1L, gerente));

        assertEquals(409, ex.getStatusCode().value());
        verify(usuarioRepository, never()).delete(any());
    }

    private Usuario usuario(Long id, String nome, Cargo cargo) {
        Usuario usuario = new Usuario(nome, cargo);
        usuario.setId(id);
        return usuario;
    }
}
