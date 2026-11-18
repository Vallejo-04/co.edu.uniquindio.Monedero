package com.Project.projectApi.Enum;

import java.time.LocalDateTime;

public enum Frecuencia {

    UNA_VEZ,
    DIARIA,
    SEMANAL,
    MENSUAL;

    public LocalDateTime siguiente(LocalDateTime desde) {
        return switch (this) {
            case UNA_VEZ -> null;
            case DIARIA -> desde.plusDays(1);
            case SEMANAL -> desde.plusWeeks(1);
            case MENSUAL -> desde.plusMonths(1);
        };
    }
}
