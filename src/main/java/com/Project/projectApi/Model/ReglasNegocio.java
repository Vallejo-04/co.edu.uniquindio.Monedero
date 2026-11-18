package com.Project.projectApi.Model;

import com.Project.projectApi.Enum.RangoCliente;
import com.Project.projectApi.Enum.TipoTransaccion;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class ReglasNegocio {

    public static final double COMISION_TRANSFERENCIA = 0.01;
    public static final double COMISION_RETIRO = 0.005;

    public static final double DESCUENTO_BENEFICIO_TRANSFERENCIA = 0.10;
    public static final double VALOR_BONO_SALDO = 50;

    public static final double UMBRAL_SALDO_BAJO = 10_000;
    public static final int MAX_NOTIFICACIONES_POR_CLIENTE = 30;

    private ReglasNegocio() {
    }

    public static int calcularPuntos(TipoTransaccion tipo, double monto) {
        int bloquesDe100 = (int) (monto / 100);

        return switch (tipo) {
            case DEPOSITO -> bloquesDe100;
            case RETIRO -> bloquesDe100 * 2;
            case TRANSFERENCIA -> bloquesDe100 * 3;
        };
    }

    public static double descuentoPorRango(RangoCliente rango) {
        return switch (rango) {
            case BRONCE -> 0.0;
            case PLATA -> 0.10;
            case ORO -> 0.25;
            case PLATINO -> 0.50;
        };
    }

    public static double bonoPuntosPorRango(RangoCliente rango) {
        return switch (rango) {
            case BRONCE -> 0.0;
            case PLATA -> 0.05;
            case ORO -> 0.10;
            case PLATINO -> 0.20;
        };
    }

    public static int puntosExtraPorRango(RangoCliente rango, int puntosBase) {
        return (int) Math.round(puntosBase * bonoPuntosPorRango(rango));
    }

    public static double calcularComision(TipoTransaccion tipo, double monto, RangoCliente rango,
                                          boolean conDescuentoTransferencia, boolean retiroSinCargo) {
        double comision = switch (tipo) {
            case DEPOSITO -> 0;
            case RETIRO -> retiroSinCargo ? 0 : monto * COMISION_RETIRO;
            case TRANSFERENCIA -> monto * COMISION_TRANSFERENCIA;
        };

        comision *= 1 - descuentoPorRango(rango);

        if (tipo == TipoTransaccion.TRANSFERENCIA && conDescuentoTransferencia) {
            comision *= 1 - DESCUENTO_BENEFICIO_TRANSFERENCIA;
        }

        return redondear(comision);
    }

    public static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    public static String formatoDinero(double valor) {
        DecimalFormat formato = new DecimalFormat(
                "#,##0.##", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-CO")));
        return "$" + formato.format(valor);
    }
}
