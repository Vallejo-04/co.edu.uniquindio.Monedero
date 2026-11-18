package com.Project.projectApi.Model;

import com.Project.projectApi.List.LinkedList;

import java.time.LocalDateTime;

public class ReporteIntegridad {

    public record Revision(String nombre, String descripcion, boolean aprobada, int elementosRevisados) {
    }

    private final LocalDateTime fecha = LocalDateTime.now();
    private final LinkedList<Revision> revisiones = new LinkedList<>();
    private final LinkedList<String> problemas = new LinkedList<>();
    private long duracionMicros;

    void agregarRevision(String nombre, String descripcion, boolean aprobada, int elementos) {
        revisiones.add(new Revision(nombre, descripcion, aprobada, elementos));
    }

    void agregarProblema(String problema) {
        problemas.add(problema);
    }

    void setDuracionMicros(long duracionMicros) {
        this.duracionMicros = duracionMicros;
    }

    public boolean isTodoCorrecto() {
        return problemas.isEmpty();
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public LinkedList<Revision> getRevisiones() {
        return revisiones;
    }

    public LinkedList<String> getProblemas() {
        return problemas;
    }

    public long getDuracionMicros() {
        return duracionMicros;
    }
}
