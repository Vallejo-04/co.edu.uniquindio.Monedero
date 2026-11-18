package com.Project.projectApi.Controller;

import com.Project.projectApi.DTO.ClienteDto;
import com.Project.projectApi.Enum.CategoriaGasto;
import com.Project.projectApi.Enum.Frecuencia;
import com.Project.projectApi.Enum.TipoTransaccion;
import com.Project.projectApi.ModelFactory.ModelFactory;
import org.springframework.ui.Model;

final class DatosVistaCliente {

    private DatosVistaCliente() {
    }

    static String seccionValida(String seccion) {
        return switch (seccion) {
            case "resumen", "operaciones", "historial", "puntos",
                 "programadas", "notificaciones", "gastos" -> seccion;
            default -> "resumen";
        };
    }

    static boolean cargar(Model model, ModelFactory fabrica, int idCliente, String seccion) {
        ClienteDto cliente = fabrica.obtenerCliente(idCliente);
        if (cliente == null) {
            return false;
        }

        model.addAttribute("cliente", cliente);
        model.addAttribute("seccion", seccion);
        model.addAttribute("monederos", fabrica.obtenerMonederos(idCliente));
        model.addAttribute("saldoTotal", fabrica.obtenerSaldoTotal(idCliente));

        int[] ventajas = fabrica.obtenerVentajasDelRango(idCliente);
        model.addAttribute("descuentoRango", ventajas[0]);
        model.addAttribute("bonoPuntosRango", ventajas[1]);

        switch (seccion) {
            case "operaciones" -> {
                model.addAttribute("destinos", fabrica.obtenerMonederosDestino());
                model.addAttribute("ultimaReversible", fabrica.obtenerUltimaReversible(idCliente));
                model.addAttribute("categorias", CategoriaGasto.values());
            }
            case "historial" -> model.addAttribute("transacciones", fabrica.obtenerTransacciones(idCliente));
            case "puntos" -> {
                model.addAttribute("catalogo", fabrica.obtenerCatalogoBeneficios());
                model.addAttribute("beneficios", fabrica.obtenerBeneficios(idCliente));
                model.addAttribute("historialPuntos", fabrica.obtenerHistorialPuntos(idCliente));
            }
            case "programadas" -> {
                model.addAttribute("programadas", fabrica.obtenerProgramadas(idCliente));
                model.addAttribute("destinos", fabrica.obtenerMonederosDestino());
                model.addAttribute("categorias", CategoriaGasto.values());
                model.addAttribute("frecuencias", Frecuencia.values());
                model.addAttribute("tiposTransaccion", TipoTransaccion.values());
            }
            case "notificaciones" -> model.addAttribute("notificaciones", fabrica.obtenerNotificaciones(idCliente));
            case "gastos" -> model.addAttribute("analisis", fabrica.obtenerAnalisisCliente(idCliente));
            default -> {
            }
        }
        return true;
    }
}
