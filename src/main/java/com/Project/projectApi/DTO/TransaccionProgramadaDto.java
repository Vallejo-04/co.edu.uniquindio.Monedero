package com.Project.projectApi.DTO;

import com.Project.projectApi.Enum.CategoriaGasto;
import com.Project.projectApi.Enum.Frecuencia;
import com.Project.projectApi.Enum.TipoTransaccion;

import java.time.LocalDateTime;

public record TransaccionProgramadaDto(
        int id,
        int idCliente,
        String nombreCliente,
        TipoTransaccion tipo,
        int idMonederoOrigen,
        int idMonederoDestino,
        double monto,
        CategoriaGasto categoria,
        String descripcion,
        Frecuencia frecuencia,
        LocalDateTime proximaEjecucion,
        int ejecucionesRestantes,
        int vecesEjecutada,
        int vecesFallida,
        String ultimoResultado
) {

    public boolean sinLimite() {
        return ejecucionesRestantes < 0;
    }
}
