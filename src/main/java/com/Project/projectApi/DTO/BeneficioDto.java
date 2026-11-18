package com.Project.projectApi.DTO;

import com.Project.projectApi.Enum.TipoBeneficio;

import java.time.LocalDateTime;

public record BeneficioDto(
        int id,
        String nombre,
        int costoPuntos,
        String descripcion,
        TipoBeneficio tipo,
        LocalDateTime fechaCanje,
        LocalDateTime fechaVencimiento,
        boolean vigente
) {

    public BeneficioDto(String nombre, int costoPuntos, String descripcion) {
        this(0, nombre, costoPuntos, descripcion, null, null, null, false);
    }
}
