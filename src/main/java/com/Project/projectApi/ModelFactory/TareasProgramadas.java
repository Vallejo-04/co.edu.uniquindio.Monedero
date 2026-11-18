package com.Project.projectApi.ModelFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TareasProgramadas {

    private static final Logger log = LoggerFactory.getLogger(TareasProgramadas.class);

    @Scheduled(fixedDelayString = "${monedero.programadas.intervalo-ms:30000}", initialDelay = 10_000)
    public void revisarAgenda() {
        int ejecutadas = ModelFactory.getInstance().procesarProgramadas();
        if (ejecutadas > 0) {
            log.info("Se ejecutaron {} transacciones programadas", ejecutadas);
        }
    }
}
