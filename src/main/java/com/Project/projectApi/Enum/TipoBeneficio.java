package com.Project.projectApi.Enum;

public enum TipoBeneficio {

    DESCUENTO_TRANSFERENCIAS(100, "Descuento en transferencias",
            "Reducción del 10 % en la comisión de tus transferencias durante 30 días", 30),

    RETIROS_SIN_CARGO(500, "Retiros sin cargo",
            "Un mes sin cargos por retiros", 30),

    BONO_SALDO(1000, "Bono de saldo",
            "Bono de 50 unidades directo a uno de tus monederos", 0);

    private final int costoPuntos;
    private final String nombre;
    private final String descripcion;
    private final int diasDeVigencia;

    TipoBeneficio(int costoPuntos, String nombre, String descripcion, int diasDeVigencia) {
        this.costoPuntos = costoPuntos;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.diasDeVigencia = diasDeVigencia;
    }

    public int getCostoPuntos() {
        return costoPuntos;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getDiasDeVigencia() {
        return diasDeVigencia;
    }

    public boolean esInmediato() {
        return diasDeVigencia == 0;
    }
}
