package com.Project.projectApi.DTO;

import com.Project.projectApi.Enum.TipoNotificacion;

import java.time.LocalDateTime;

public record NotificacionDto(
        int id,
        String mensaje,
        TipoNotificacion tipo,
        LocalDateTime fecha,
        boolean leida
) {

    public NotificacionDto(String mensaje) {
        this(0, mensaje, null, null, false);
    }
}
