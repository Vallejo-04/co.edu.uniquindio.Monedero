package com.Project.projectApi.Model;

public record PuntajeCliente(int puntos, int idCliente) implements Comparable<PuntajeCliente> {

    @Override
    public int compareTo(PuntajeCliente otro) {
        int porPuntos = Integer.compare(puntos, otro.puntos);
        return porPuntos != 0 ? porPuntos : Integer.compare(idCliente, otro.idCliente);
    }
}
