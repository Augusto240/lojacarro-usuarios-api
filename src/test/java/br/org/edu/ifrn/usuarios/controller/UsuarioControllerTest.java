package br.org.edu.ifrn.usuarios.controller;

import br.org.edu.ifrn.usuarios.model.Cargo;
import br.org.edu.ifrn.usuarios.model.Usuario;
import br.org.edu.ifrn.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@ExtendWith(OutputCaptureExtension.class)
class UsuarioControllerTest {

    private static final String HEADER = "X-Usuario-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario marta;
    private Usuario caio;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        marta = usuarioRepository.save(new Usuario("Marta", Cargo.GERENTE));
        caio = usuarioRepository.save(new Usuario("Caio", Cargo.VENDEDOR));
    }

    @Test
    void crudCompletoFeitoPorGerente() throws Exception {
        String resposta = mockMvc.perform(post("/usuarios").header(HEADER, marta.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Beatriz\",\"cargo\":\"VENDEDOR\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.nome").value("Beatriz"))
                .andReturn().getResponse().getContentAsString();
        String id = resposta.replaceAll(".*\"id\":(\\d+).*", "$1");

        mockMvc.perform(put("/usuarios/" + id).header(HEADER, marta.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Beatriz Souza\",\"cargo\":\"GERENTE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Beatriz Souza"))
                .andExpect(jsonPath("$.cargo").value("GERENTE"));

        mockMvc.perform(delete("/usuarios/" + id).header(HEADER, marta.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/usuarios/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarEmOrdemAlfabetica() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Caio"))
                .andExpect(jsonPath("$[1].nome").value("Marta"));
    }

    @Test
    void rotaDoNomeDevolveSoONome() throws Exception {
        mockMvc.perform(get("/usuarios/" + caio.getId() + "/nome"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Caio"))
                .andExpect(jsonPath("$.cargo").doesNotExist())
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void nomeDeUsuarioQueNaoExisteDa404() throws Exception {
        mockMvc.perform(get("/usuarios/999999/nome"))
                .andExpect(status().isNotFound());
    }

    @Test
    void vendedorNaoAlteraNada() throws Exception {
        mockMvc.perform(post("/usuarios").header(HEADER, caio.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ze\",\"cargo\":\"VENDEDOR\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("O cargo VENDEDOR não pode cadastrar usuários"));

        mockMvc.perform(delete("/usuarios/" + marta.getId()).header(HEADER, caio.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void semHeaderDa401() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ze\",\"cargo\":\"VENDEDOR\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void erroDeFormatoViram400E404E405() throws Exception {
        mockMvc.perform(delete("/usuarios/1").header(HEADER, "abc"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/usuarios").header(HEADER, marta.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{json quebrado"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/nada/aqui"))
                .andExpect(status().isNotFound());
        mockMvc.perform(patch("/usuarios"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void requestIdQueVemDaLojaCarroApareceNoLog(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/usuarios/" + caio.getId() + "/nome").header("X-Request-Id", "web-teste123"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", "web-teste123"));

        assertThat(output)
                .contains("[web-teste123]")
                .contains("--> GET /usuarios/" + caio.getId() + "/nome")
                .contains("Nome do usuario " + caio.getId() + " consultado: Caio")
                .contains("respondeu 200");
    }

    @Test
    void requestIdInvalidoEhTrocado() throws Exception {
        String gerado = mockMvc.perform(get("/usuarios").header("X-Request-Id", "id com espaco"))
                .andReturn().getResponse().getHeader("X-Request-Id");

        assertThat(gerado).hasSize(8).doesNotContain(" ");
    }
}
