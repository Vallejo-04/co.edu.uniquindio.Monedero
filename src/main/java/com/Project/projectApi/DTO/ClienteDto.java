package com.Project.projectApi.DTO;

public record ClienteDto(
        int id,
        String nombre,
        String correo,
        int puntos,
        int puntosDisponibles,
        String rango,
        String siguienteRango,
        int puntosParaSubir,
        int posicionRanking,
        int notificacionesSinLeer
) {

    public ClienteDto(int id, String nombre, String correo) {
        this(id, nombre, correo, 0, 0, "BRONCE", "PLATA", 501, 0, 0);
    }
}
