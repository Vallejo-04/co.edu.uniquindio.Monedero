package com.Project.projectApi.DTO;

import com.Project.projectApi.Enum.CategoriaGasto;
import com.Project.projectApi.Enum.EstadoTransaccion;
import com.Project.projectApi.Enum.TipoTransaccion;

import java.time.LocalDateTime;

public record TransaccionDto(
        int id,
        TipoTransaccion tipo,
        double monto,
        LocalDateTime fecha,
        String descripcion,
        double comision,
        int puntosGenerados,
        int puntosExtra,
        CategoriaGasto categoria,
        EstadoTransaccion estado,
        int idMonederoOrigen,
        int idMonederoDestino,
        String direccion,
        boolean automatica,
        boolean reversible,
        String huella
) {

    public TransaccionDto(TipoTransaccion tipo, double monto, String descripcion) {
        this(0, tipo, monto, null, descripcion, 0, 0, 0, CategoriaGasto.OTROS,
                EstadoTransaccion.ACTIVA, 0, 0, null, false, true, null);
    }

    public boolean esSalida() {
        return "SALIDA".equals(direccion);
    }

    public boolean esEntrada() {
        return "ENTRADA".equals(direccion);
    }

    public boolean esRevertida() {
        return estado == EstadoTransaccion.REVERTIDA;
    }
}
