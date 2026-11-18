package com.Project.projectApi.Model;

import com.Project.projectApi.Enum.CategoriaGasto;
import com.Project.projectApi.Enum.EstadoTransaccion;
import com.Project.projectApi.Enum.TipoTransaccion;

import java.time.LocalDateTime;

public class Transaccion {

    private int id;

    private TipoTransaccion tipo;

    private double monto;

    private LocalDateTime fecha;

    private String descripcion;

    private int idCliente;
    private int idMonederoOrigen;
    private int idClienteDestino;
    private int idMonederoDestino;

    private double comision;
    private int puntosGenerados;
    private CategoriaGasto categoria;
    private EstadoTransaccion estado;

    private double saldoAntes;
    private double saldoDespues;

    private int puntosBase;
    private int puntosExtra;
    private boolean reversible;
    private int idProgramada;

    private String huella;
    private String huellaAnterior;

    public Transaccion(int id,
                       TipoTransaccion tipo,
                       double monto,
                       LocalDateTime fecha,
                       String descripcion) {

        this.id = id;
        this.tipo = tipo;
        this.monto = monto;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.estado = EstadoTransaccion.ACTIVA;
        this.categoria = CategoriaGasto.OTROS;
        this.reversible = true;
    }

    public boolean esAutomatica() {
        return idProgramada != 0;
    }

    public boolean esInterna() {
        return tipo == TipoTransaccion.TRANSFERENCIA && idCliente == idClienteDestino;
    }

    public boolean estaActiva() {
        return estado == EstadoTransaccion.ACTIVA;
    }

    public double getTotalDebitado() {
        return ReglasNegocio.redondear(monto + comision);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TipoTransaccion getTipo() {
        return tipo;
    }

    public void setTipo(TipoTransaccion tipo) {
        this.tipo = tipo;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdMonederoOrigen() {
        return idMonederoOrigen;
    }

    public void setIdMonederoOrigen(int idMonederoOrigen) {
        this.idMonederoOrigen = idMonederoOrigen;
    }

    public int getIdClienteDestino() {
        return idClienteDestino;
    }

    public void setIdClienteDestino(int idClienteDestino) {
        this.idClienteDestino = idClienteDestino;
    }

    public int getIdMonederoDestino() {
        return idMonederoDestino;
    }

    public void setIdMonederoDestino(int idMonederoDestino) {
        this.idMonederoDestino = idMonederoDestino;
    }

    public double getComision() {
        return comision;
    }

    public void setComision(double comision) {
        this.comision = comision;
    }

    public int getPuntosGenerados() {
        return puntosGenerados;
    }

    public void setPuntosGenerados(int puntosGenerados) {
        this.puntosGenerados = puntosGenerados;
    }

    public CategoriaGasto getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaGasto categoria) {
        this.categoria = categoria;
    }

    public EstadoTransaccion getEstado() {
        return estado;
    }

    public void setEstado(EstadoTransaccion estado) {
        this.estado = estado;
    }

    public double getSaldoAntes() {
        return saldoAntes;
    }

    public void setSaldoAntes(double saldoAntes) {
        this.saldoAntes = saldoAntes;
    }

    public double getSaldoDespues() {
        return saldoDespues;
    }

    public void setSaldoDespues(double saldoDespues) {
        this.saldoDespues = saldoDespues;
    }

    public int getPuntosBase() {
        return puntosBase;
    }

    public void setPuntosBase(int puntosBase) {
        this.puntosBase = puntosBase;
    }

    public int getPuntosExtra() {
        return puntosExtra;
    }

    public void setPuntosExtra(int puntosExtra) {
        this.puntosExtra = puntosExtra;
    }

    public boolean isReversible() {
        return reversible;
    }

    public void setReversible(boolean reversible) {
        this.reversible = reversible;
    }

    public int getIdProgramada() {
        return idProgramada;
    }

    public void setIdProgramada(int idProgramada) {
        this.idProgramada = idProgramada;
    }

    public String getHuella() {
        return huella;
    }

    public void setHuella(String huella) {
        this.huella = huella;
    }

    public String getHuellaAnterior() {
        return huellaAnterior;
    }

    public void setHuellaAnterior(String huellaAnterior) {
        this.huellaAnterior = huellaAnterior;
    }

    @Override
    public String toString() {
        return "Transaccion{" +
                "id=" + id +
                ", tipo=" + tipo +
                ", monto=" + monto +
                ", comision=" + comision +
                ", puntos=" + puntosGenerados +
                ", estado=" + estado +
                ", fecha=" + fecha +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }

}
