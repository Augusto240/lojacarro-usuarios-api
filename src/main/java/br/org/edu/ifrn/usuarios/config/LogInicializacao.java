package br.org.edu.ifrn.usuarios.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class LogInicializacao {

    private static final Logger log = LoggerFactory.getLogger(LogInicializacao.class);

    private final Environment environment;

    public LogInicializacao(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void aplicacaoNoAr() {
        log.info("API de usuarios no ar na porta {}",
                environment.getProperty("local.server.port", environment.getProperty("server.port", "8081")));
    }

    @EventListener(ContextClosedEvent.class)
    public void aplicacaoDesligando() {
        log.info("API de usuarios desligando");
    }
}
