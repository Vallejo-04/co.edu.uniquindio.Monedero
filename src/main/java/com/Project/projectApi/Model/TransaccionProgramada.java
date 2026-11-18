package com.Project.projectApi.Model;

import com.Project.projectApi.Enum.CategoriaGasto;
import com.Project.projectApi.Enum.Frecuencia;
import com.Project.projectApi.Enum.TipoTransaccion;

import java.time.LocalDateTime;

public class TransaccionProgramada implements Comparable<TransaccionProgramada> {

    public static final int SIN_LIMITE = -1;

    private final int id;
    private final int idCliente;
    private final TipoTransaccion tipo;
    private final int idMonederoOrigen;
    private final int idMonederoDestino;
    private final double monto;
    private final CategoriaGasto categoria;
    private final String descripcion;
    private final Frecuencia frecuencia;

    private LocalDateTime proximaEjecucion;
    private int ejecucionesRestantes;
    private int vecesEjecutada;
    private int vecesFallida;
    private boolean recordatorioEnviado;
    private String ultimoResultado;

    public TransaccionProgramada(int id, int idCliente, TipoTransaccion tipo,
                                 int idMonederoOrigen, int idMonederoDestino, double monto,
                                 CategoriaGasto categoria, String descripcion,
                                 LocalDateTime primeraEjecucion, Frecuencia frecuencia, int repeticiones) {
        this.id = id;
        this.idCliente = idCliente;
        this.tipo = tipo;
        this.idMonederoOrigen = idMonederoOrigen;
        this.idMonederoDestino = idMonederoDestino;
        this.monto = monto;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.frecuencia = frecuencia;
        this.proximaEjecucion = primeraEjecucion;

        if (frecuencia == Frecuencia.UNA_VEZ) {
            this.ejecucionesRestantes = 1;
        } else {
            this.ejecucionesRestantes = repeticiones > 0 ? repeticiones : SIN_LIMITE;
        }
        this.ultimoResultado = "Pendiente";
    }

    @Override
    public int compareTo(TransaccionProgramada otra) {
        int porFecha = proximaEjecucion.compareTo(otra.proximaEjecucion);
        return porFecha != 0 ? porFecha : Integer.compare(id, otra.id);
    }

    public boolean tocaEjecutar(LocalDateTime ahora) {
        return !proximaEjecucion.isAfter(ahora);
    }

    public void registrarExito(String resultado) {
        vecesEjecutada++;
        ultimoResultado = resultado;
        descontarRepeticion();
    }

    public void registrarFallo(String motivo) {
        vecesFallida++;
        ultimoResultado = "Falló: " + motivo;
        descontarRepeticion();
    }

    private void descontarRepeticion() {
        if (ejecucionesRestantes > 0) {
            ejecucionesRestantes--;
        }
    }

    public boolean avanzarASiguiente() {
        if (frecuencia == Frecuencia.UNA_VEZ || ejecucionesRestantes == 0) {
            return false;
        }
        proximaEjecucion = frecuencia.siguiente(proximaEjecucion);
        recordatorioEnviado = false;
        return true;
    }

    public void marcarRecordatorioEnviado() {
        recordatorioEnviado = true;
    }

    public int getId() {
        return id;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public TipoTransaccion getTipo() {
        return tipo;
    }

    public int getIdMonederoOrigen() {
        return idMonederoOrigen;
    }

    public int getIdMonederoDestino() {
        return idMonederoDestino;
    }

    public double getMonto() {
        return monto;
    }

    public CategoriaGasto getCategoria() {
        return categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Frecuencia getFrecuencia() {
        return frecuencia;
    }

    public LocalDateTime getProximaEjecucion() {
        return proximaEjecucion;
    }

    public int getEjecucionesRestantes() {
        return ejecucionesRestantes;
    }

    public int getVecesEjecutada() {
        return vecesEjecutada;
    }

    public int getVecesFallida() {
        return vecesFallida;
    }

    public boolean isRecordatorioEnviado() {
        return recordatorioEnviado;
    }

    public String getUltimoResultado() {
        return ultimoResultado;
    }

    @Override
    public String toString() {
        return "Programada#" + id + "(" + tipo + " " + monto + " el " + proximaEjecucion + ")";
    }
}
