package com.Project.projectApi.Model;

import com.Project.projectApi.Enum.TipoBeneficio;

import java.time.LocalDateTime;

public class Beneficio {

    private int id;
    private String nombre;
    private int costoPuntos;
    private String descripcion;

    private TipoBeneficio tipo;
    private LocalDateTime fechaCanje;
    private LocalDateTime fechaVencimiento;

    public Beneficio(int id,
                     String nombre,
                     int costoPuntos,
                     String descripcion) {

        this.id = id;
        this.nombre = nombre;
        this.costoPuntos = costoPuntos;
        this.descripcion = descripcion;
    }

    public static Beneficio delCatalogo(int id, TipoBeneficio tipo) {
        Beneficio beneficio = new Beneficio(id, tipo.getNombre(), tipo.getCostoPuntos(), tipo.getDescripcion());
        beneficio.tipo = tipo;
        return beneficio;
    }

    public static Beneficio canjeado(int id, TipoBeneficio tipo, LocalDateTime cuando) {
        Beneficio beneficio = delCatalogo(id, tipo);
        beneficio.fechaCanje = cuando;
        beneficio.fechaVencimiento = tipo.esInmediato() ? null : cuando.plusDays(tipo.getDiasDeVigencia());
        return beneficio;
    }

    public boolean estaVigente(LocalDateTime fecha) {
        return fechaVencimiento != null && !fecha.isAfter(fechaVencimiento);
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

    public int getCostoPuntos() {
        return costoPuntos;
    }

    public void setCostoPuntos(int costoPuntos) {
        this.costoPuntos = costoPuntos;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public TipoBeneficio getTipo() {
        return tipo;
    }

    public LocalDateTime getFechaCanje() {
        return fechaCanje;
    }

    public LocalDateTime getFechaVencimiento() {
        return fechaVencimiento;
    }

    @Override
    public String toString() {
        return "Beneficio{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", costoPuntos=" + costoPuntos +
                ", vence=" + fechaVencimiento +
                '}';
    }
}
