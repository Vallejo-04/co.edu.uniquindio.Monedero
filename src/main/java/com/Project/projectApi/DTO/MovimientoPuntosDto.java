package com.Project.projectApi.DTO;

import com.Project.projectApi.Enum.TipoMovimientoPuntos;

import java.time.LocalDateTime;

public record MovimientoPuntosDto(
        LocalDateTime fecha,
        TipoMovimientoPuntos tipo,
        int cambioAcumulados,
        int cambioDisponibles,
        String motivo,
        int idTransaccion,
        int acumuladosDespues,
        int disponiblesDespues
) {
}
