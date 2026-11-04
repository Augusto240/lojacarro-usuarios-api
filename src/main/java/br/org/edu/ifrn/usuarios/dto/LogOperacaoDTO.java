package br.org.edu.ifrn.usuarios.dto;

import java.time.LocalDateTime;

public record LogOperacaoDTO(
        String modulo,
        String operacao,
        LocalDateTime dataHora,
        Long usuarioId,
        String usuarioNome,
        Long registroId,
        String descricao,
        String requestId) {
}
