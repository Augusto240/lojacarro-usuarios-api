package br.org.edu.ifrn.usuarios.rabbitmq;

import br.org.edu.ifrn.usuarios.dto.LogOperacaoDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(OutputCaptureExtension.class)
class EnviarTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void dataVaiNoJsonComoTextoParaOModuloDeLog() {
        String json = jsonMapper.writeValueAsString(new LogOperacaoDTO("USUARIO", "USUARIO_CRIADO",
                LocalDateTime.of(2026, 11, 4, 10, 0, 0), 1L, "Administrador", 2L, "Usuario{id=2}", "abc"));

        assertThat(json).contains("\"dataHora\":\"2026-11-04T10:00:00\"").contains("\"modulo\":\"USUARIO\"");
    }

    @Test
    void rabbitForaDoArNaoDerrubaOCadastro(CapturedOutput output) {
        Enviar enviar = new Enviar(jsonMapper, "localhost", 1, "aluno", "senha-teste", "augusto_erick");

        assertDoesNotThrow(() -> enviar.enviarMensagem(new LogOperacaoDTO("USUARIO", "USUARIO_CRIADO",
                LocalDateTime.now(), 1L, "Administrador", 2L, "Usuario{id=2}", "abc")));

        assertThat(output).contains("Nao consegui mandar a mensagem para o RabbitMQ");
    }
}
