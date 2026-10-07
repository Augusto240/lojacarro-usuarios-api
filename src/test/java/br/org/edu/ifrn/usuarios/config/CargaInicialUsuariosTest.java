package br.org.edu.ifrn.usuarios.config;

import br.org.edu.ifrn.usuarios.model.Cargo;
import br.org.edu.ifrn.usuarios.model.Usuario;
import br.org.edu.ifrn.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CargaInicialUsuariosTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CargaInicialUsuarios cargaInicial;

    @Test
    void bancoVazioGanhaUmGerente() {
        when(usuarioRepository.count()).thenReturn(0L);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        cargaInicial.run(null);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals(CargaInicialUsuarios.NOME_GERENTE_PADRAO, captor.getValue().getNome());
        assertEquals(Cargo.GERENTE, captor.getValue().getCargo());
    }

    @Test
    void bancoComUsuarioNaoCriaNada() {
        when(usuarioRepository.count()).thenReturn(2L);

        cargaInicial.run(null);

        verify(usuarioRepository, never()).save(any());
    }
}
