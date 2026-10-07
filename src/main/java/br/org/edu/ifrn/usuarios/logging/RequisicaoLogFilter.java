package br.org.edu.ifrn.usuarios.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequisicaoLogFilter extends OncePerRequestFilter {

    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    private static final Logger log = LoggerFactory.getLogger(RequisicaoLogFilter.class);
    private static final Pattern ID_VALIDO = Pattern.compile("[A-Za-z0-9-]{1,64}");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long inicio = System.currentTimeMillis();
        String recebido = request.getHeader(HEADER_REQUEST_ID);
        String requestId = recebido != null && ID_VALIDO.matcher(recebido).matches()
                ? recebido
                : UUID.randomUUID().toString().substring(0, 8);
        String rota = request.getMethod() + " " + request.getRequestURI();

        MDC.put("requestId", requestId);
        response.setHeader(HEADER_REQUEST_ID, requestId);
        log.info("--> {} de {}", rota, request.getRemoteAddr());

        try {
            filterChain.doFilter(request, response);
        } finally {
            int status = response.getStatus();
            long duracao = System.currentTimeMillis() - inicio;
            if (status >= 500) {
                log.error("<-- {} respondeu {} em {} ms", rota, status, duracao);
            } else if (status >= 400) {
                log.warn("<-- {} respondeu {} em {} ms", rota, status, duracao);
            } else {
                log.info("<-- {} respondeu {} em {} ms", rota, status, duracao);
            }
            MDC.clear();
        }
    }
}
