package com.Project.projectApi.DTO;

import com.Project.projectApi.Enum.CategoriaGasto;
import com.Project.projectApi.Enum.Frecuencia;
import com.Project.projectApi.Enum.TipoTransaccion;

import java.time.LocalDateTime;

public record SolicitudProgramacionDto(
        TipoTransaccion tipo,
        Integer idMonederoOrigen,
        Integer idMonederoDestino,
        Double monto,
        CategoriaGasto categoria,
        String descripcion,
        LocalDateTime fechaEjecucion,
        Frecuencia frecuencia,
        Integer repeticiones
) {
}
