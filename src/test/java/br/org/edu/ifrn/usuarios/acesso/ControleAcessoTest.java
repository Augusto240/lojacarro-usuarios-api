package br.org.edu.ifrn.usuarios.acesso;

import br.org.edu.ifrn.usuarios.model.Cargo;
import br.org.edu.ifrn.usuarios.model.Usuario;
import br.org.edu.ifrn.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControleAcessoTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ControleAcesso controleAcesso;

    @AfterEach
    void limparMdc() {
        MDC.clear();
    }

    @Test
    void semHeaderDa401() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controleAcesso.exigirGerente(null, "cadastrar usuários"));

        assertEquals(401, ex.getStatusCode().value());
    }

    @Test
    void usuarioQueNaoExisteDa401() {
        when(usuarioRepository.findById(7L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controleAcesso.exigirGerente(7L, "cadastrar usuários"));

        assertEquals(401, ex.getStatusCode().value());
    }

    @Test
    void vendedorDa403() {
        Usuario caio = new Usuario("Caio", Cargo.VENDEDOR);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(caio));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controleAcesso.exigirGerente(2L, "excluir usuários"));

        assertEquals(403, ex.getStatusCode().value());
        assertEquals("O cargo VENDEDOR não pode excluir usuários", ex.getReason());
    }

    @Test
    void gerentePassaEVaiProMdc() {
        Usuario marta = new Usuario("Marta", Cargo.GERENTE);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(marta));

        assertSame(marta, controleAcesso.exigirGerente(1L, "cadastrar usuários"));
        assertEquals("Marta", MDC.get("usuario"));
    }
}
