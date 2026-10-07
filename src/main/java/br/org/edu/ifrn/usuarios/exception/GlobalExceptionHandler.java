package br.org.edu.ifrn.usuarios.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> tratarResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        log.warn("Requisicao recusada com {}: {}", status.value(), ex.getReason());
        return erro(ex.getReason(), status);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> tratarCorpoInvalido() {
        log.warn("Corpo da requisicao com JSON invalido");
        return erro("Corpo da requisição inválido", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> tratarTipoErrado(MethodArgumentTypeMismatchException ex) {
        log.warn("Parametro '{}' com valor em formato errado", ex.getName());
        return erro("Valor inválido para " + ex.getName(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> tratarRotaInexistente(NoResourceFoundException ex) {
        log.warn("Rota inexistente: /{}", ex.getResourcePath());
        return erro("Não encontrado", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> tratarMetodoErrado(HttpRequestMethodNotSupportedException ex) {
        log.warn("Metodo {} nao suportado nessa rota", ex.getMethod());
        return erro("Método não permitido", HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> tratarErroInesperado(Exception ex) {
        log.error("Erro inesperado: {}", ex.getMessage(), ex);
        return erro("Erro interno", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Map<String, String>> erro(String mensagem, HttpStatus status) {
        return ResponseEntity.status(status).body(Map.of("error", mensagem == null ? "" : mensagem));
    }
}
