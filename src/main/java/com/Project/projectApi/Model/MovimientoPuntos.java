package com.Project.projectApi.Model;

import com.Project.projectApi.Enum.TipoMovimientoPuntos;

import java.time.LocalDateTime;

public class MovimientoPuntos {

    private final LocalDateTime fecha;
    private final TipoMovimientoPuntos tipo;
    private final int cambioAcumulados;
    private final int cambioDisponibles;
    private final String motivo;
    private final int idTransaccion;
    private final int acumuladosDespues;
    private final int disponiblesDespues;

    public MovimientoPuntos(TipoMovimientoPuntos tipo, int cambioAcumulados, int cambioDisponibles,
                            String motivo, int idTransaccion, int acumuladosDespues, int disponiblesDespues) {
        this.fecha = LocalDateTime.now();
        this.tipo = tipo;
        this.cambioAcumulados = cambioAcumulados;
        this.cambioDisponibles = cambioDisponibles;
        this.motivo = motivo;
        this.idTransaccion = idTransaccion;
        this.acumuladosDespues = acumuladosDespues;
        this.disponiblesDespues = disponiblesDespues;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public TipoMovimientoPuntos getTipo() {
        return tipo;
    }

    public int getCambioAcumulados() {
        return cambioAcumulados;
    }

    public int getCambioDisponibles() {
        return cambioDisponibles;
    }

    public String getMotivo() {
        return motivo;
    }

    public int getIdTransaccion() {
        return idTransaccion;
    }

    public int getAcumuladosDespues() {
        return acumuladosDespues;
    }

    public int getDisponiblesDespues() {
        return disponiblesDespues;
    }
}
