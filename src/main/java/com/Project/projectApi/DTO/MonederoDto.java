package com.Project.projectApi.DTO;

public record MonederoDto(
        int id,
        String nombre,
        double saldo,
        int idCliente,
        String titular
) {

    public MonederoDto(String nombre, double saldo) {
        this(0, nombre, saldo, 0, null);
    }
}
