package com.Project.projectApi.Model;

import com.Project.projectApi.Enum.TipoNotificacion;

import java.time.LocalDateTime;

public class Notificacion {

    private int id;
    private int idCliente;
    private TipoNotificacion tipo;
    private String mensaje;
    private LocalDateTime fecha;
    private boolean leida;

    public Notificacion(int id, String mensaje) {
        this.id = id;
        this.mensaje = mensaje;
        this.fecha = LocalDateTime.now();
        this.leida = false;
    }

    public Notificacion(int id, int idCliente, TipoNotificacion tipo, String mensaje) {
        this(id, mensaje);
        this.idCliente = idCliente;
        this.tipo = tipo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public TipoNotificacion getTipo() {
        return tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }

    public void marcarComoLeida() {
        this.leida = true;
    }

    @Override
    public String toString() {
        return "Notificacion{" +
                "id=" + id +
                ", tipo=" + tipo +
                ", mensaje='" + mensaje + '\'' +
                ", leida=" + leida +
                '}';
    }
}
