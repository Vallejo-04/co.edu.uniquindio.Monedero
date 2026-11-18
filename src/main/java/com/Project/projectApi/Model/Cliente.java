package com.Project.projectApi.Model;

import com.Project.projectApi.Enum.RangoCliente;
import com.Project.projectApi.Enum.TipoBeneficio;
import com.Project.projectApi.List.CircularList;
import com.Project.projectApi.List.LinkedList;
import com.Project.projectApi.Stack.Stack;

import java.time.LocalDateTime;

public class Cliente {

    private int id;
    private String nombre;
    private String correo;

    private int puntos;
    private int puntosDisponibles;
    private RangoCliente rango;

    private LinkedList<Monedero> monederos;
    private LinkedList<Transaccion> historialTransacciones;
    private LinkedList<MovimientoPuntos> historialPuntos;
    private LinkedList<Beneficio> beneficios;

    private Stack<Transaccion> transaccionesReversibles;

    private CircularList<Notificacion> notificaciones;

    public Cliente(int id, String nombre, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.puntos = 0;
        this.puntosDisponibles = 0;
        this.rango = RangoCliente.BRONCE;

        this.monederos = new LinkedList<>();
        this.historialTransacciones = new LinkedList<>();
        this.historialPuntos = new LinkedList<>();
        this.beneficios = new LinkedList<>();
        this.transaccionesReversibles = new Stack<>();
        this.notificaciones = new CircularList<>(ReglasNegocio.MAX_NOTIFICACIONES_POR_CLIENTE);
    }

    void ajustarPuntos(int cambioAcumulados, int cambioDisponibles) {
        puntos = Math.max(0, puntos + cambioAcumulados);
        puntosDisponibles = Math.max(0, puntosDisponibles + cambioDisponibles);
        rango = RangoCliente.desdePuntos(puntos);
    }

    void registrarMovimiento(MovimientoPuntos movimiento) {
        historialPuntos.add(movimiento);
    }

    public int getPuntosParaSiguienteRango() {
        return rango.puntosParaSubir(puntos);
    }

    public Beneficio beneficioVigente(TipoBeneficio tipo, LocalDateTime fecha) {
        return beneficios.find(b -> b.getTipo() == tipo && b.estaVigente(fecha));
    }

    public boolean tieneVigente(TipoBeneficio tipo, LocalDateTime fecha) {
        return beneficioVigente(tipo, fecha) != null;
    }

    public void agregarBeneficio(Beneficio beneficio) {
        beneficios.add(beneficio);
    }

    void recibirNotificacion(Notificacion notificacion) {
        notificaciones.add(notificacion);
    }

    public int contarNoLeidas() {
        int total = 0;
        for (Notificacion n : notificaciones) {
            if (!n.isLeida()) {
                total++;
            }
        }
        return total;
    }

    public void marcarNotificacionesLeidas() {
        for (Notificacion n : notificaciones) {
            n.marcarComoLeida();
        }
    }

    public double getSaldoTotal() {
        double total = 0;
        for (Monedero monedero : monederos) {
            total += monedero.getSaldo();
        }
        return ReglasNegocio.redondear(total);
    }

    public void agregarMonedero(Monedero monedero) {
        monederos.add(monedero);
    }

    public void agregarTransaccion(Transaccion transaccion) {
        historialTransacciones.add(transaccion);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public int getPuntos() {
        return puntos;
    }

    public int getPuntosDisponibles() {
        return puntosDisponibles;
    }

    public RangoCliente getRango() {
        return rango;
    }

    public LinkedList<Monedero> getMonederos() {
        return monederos;
    }

    public LinkedList<Transaccion> getHistorialTransacciones() {
        return historialTransacciones;
    }

    public LinkedList<MovimientoPuntos> getHistorialPuntos() {
        return historialPuntos;
    }

    public LinkedList<Beneficio> getBeneficios() {
        return beneficios;
    }

    public Stack<Transaccion> getTransaccionesReversibles() {
        return transaccionesReversibles;
    }

    public CircularList<Notificacion> getNotificaciones() {
        return notificaciones;
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", puntos=" + puntos +
                ", disponibles=" + puntosDisponibles +
                ", rango=" + rango +
                '}';
    }
}
