package com.Project.projectApi.Enum;

public enum RangoCliente {

    BRONCE(0, 500),
    PLATA(501, 1000),
    ORO(1001, 5000),
    PLATINO(5001, Integer.MAX_VALUE);

    private final int minimo;
    private final int maximo;

    RangoCliente(int minimo, int maximo) {
        this.minimo = minimo;
        this.maximo = maximo;
    }

    public int getMinimo() {
        return minimo;
    }

    public int getMaximo() {
        return maximo;
    }

    public static RangoCliente desdePuntos(int puntos) {
        for (RangoCliente rango : values()) {
            if (puntos >= rango.minimo && puntos <= rango.maximo) {
                return rango;
            }
        }
        return BRONCE;
    }

    public int puntosParaSubir(int puntosActuales) {
        return this == PLATINO ? 0 : Math.max(0, maximo + 1 - puntosActuales);
    }

    public RangoCliente siguiente() {
        return this == PLATINO ? null : values()[ordinal() + 1];
    }
}
