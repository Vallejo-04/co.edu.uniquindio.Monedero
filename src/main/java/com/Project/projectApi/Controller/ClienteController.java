package com.Project.projectApi.Controller;

import com.Project.projectApi.DTO.BeneficioDto;
import com.Project.projectApi.DTO.ClienteDto;
import com.Project.projectApi.DTO.MonederoDto;
import com.Project.projectApi.DTO.TransaccionDto;
import com.Project.projectApi.DTO.TransaccionProgramadaDto;
import com.Project.projectApi.Enum.CategoriaGasto;
import com.Project.projectApi.Enum.Frecuencia;
import com.Project.projectApi.Enum.TipoBeneficio;
import com.Project.projectApi.Enum.TipoTransaccion;
import com.Project.projectApi.Excepcion.OperacionInvalidaException;
import com.Project.projectApi.Model.ReglasNegocio;
import com.Project.projectApi.ModelFactory.ModelFactory;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm");

    private final ModelFactory fabrica = ModelFactory.getInstance();

    @GetMapping
    public String mostrarAcceso() {
        return "cliente-acceso";
    }

    @GetMapping("/perfil")
    public String buscarPerfil(@RequestParam int id, RedirectAttributes flash) {
        if (fabrica.obtenerCliente(id) == null) {
            flash.addFlashAttribute("error", "No encontramos ningún cliente con la identificación " + id);
            return "redirect:/cliente";
        }
        return "redirect:/cliente/" + id;
    }

    @GetMapping("/registro")
    public String mostrarFormularioRegistro() {
        return "RegistroCliente";
    }

    @PostMapping("/guardar")
    public String guardarCliente(@RequestParam int id,
                                 @RequestParam String nombre,
                                 @RequestParam String correo,
                                 RedirectAttributes flash) {
        ClienteDto dto = new ClienteDto(id, nombre.trim(), correo.trim());

        if (!fabrica.crearCliente(id, dto)) {
            flash.addFlashAttribute("error", "Ya existe un cliente con la identificación " + id);
            return "redirect:/cliente";
        }

        flash.addFlashAttribute("mensaje", "¡Bienvenido, " + dto.nombre() + "! Empieza creando tu primer monedero.");
        return "redirect:/cliente/" + id;
    }

    @GetMapping("/{id}")
    public String mostrarPanel(@PathVariable int id, Model model, RedirectAttributes flash) {
        return mostrarSeccion(id, "resumen", model, flash);
    }

    @GetMapping("/{id}/{seccion}")
    public String mostrarSeccion(@PathVariable int id, @PathVariable String seccion,
                                 Model model, RedirectAttributes flash) {
        if (!DatosVistaCliente.cargar(model, fabrica, id, DatosVistaCliente.seccionValida(seccion))) {
            flash.addFlashAttribute("error", "No encontramos ningún cliente con la identificación " + id);
            return "redirect:/cliente";
        }
        return "cliente-panel";
    }

    @PostMapping("/{id}/monedero")
    public String crearMonedero(@PathVariable int id,
                                @RequestParam String nombre,
                                @RequestParam(defaultValue = "cliente") String volver,
                                RedirectAttributes flash) {
        return intentar(flash, volver, id, "operaciones", () -> {
            MonederoDto m = fabrica.crearMonedero(id, nombre);
            return "Monedero «" + m.nombre() + "» creado con el número #" + m.id();
        });
    }

    @PostMapping("/{id}/monedero/eliminar/{idMonedero}")
    public String eliminarMonedero(@PathVariable int id,
                                   @PathVariable int idMonedero,
                                   @RequestParam(defaultValue = "cliente") String volver,
                                   RedirectAttributes flash) {
        return intentar(flash, volver, id, "resumen", () -> {
            if (!fabrica.eliminarMonedero(id, idMonedero)) {
                throw new OperacionInvalidaException("El monedero #" + idMonedero + " no es de este cliente");
            }
            return "Monedero #" + idMonedero + " eliminado";
        });
    }

    @PostMapping("/{id}/deposito")
    public String depositar(@PathVariable int id,
                            @RequestParam int idMonedero,
                            @RequestParam double monto,
                            @RequestParam(defaultValue = "") String descripcion,
                            @RequestParam(defaultValue = "cliente") String volver,
                            RedirectAttributes flash) {
        return intentar(flash, volver, id, "operaciones", () -> {
            TransaccionDto t = fabrica.depositar(id, idMonedero, monto, descripcion);
            return "Depósito de " + dinero(t.monto()) + " realizado" + textoPuntos(t);
        });
    }

    @PostMapping("/{id}/retiro")
    public String retirar(@PathVariable int id,
                          @RequestParam int idMonedero,
                          @RequestParam double monto,
                          @RequestParam(defaultValue = "OTROS") CategoriaGasto categoria,
                          @RequestParam(defaultValue = "") String descripcion,
                          @RequestParam(defaultValue = "cliente") String volver,
                          RedirectAttributes flash) {
        return intentar(flash, volver, id, "operaciones", () -> {
            TransaccionDto t = fabrica.retirar(id, idMonedero, monto, categoria, descripcion);
            return "Retiro de " + dinero(t.monto()) + " realizado"
                    + (t.comision() > 0 ? " (comisión " + dinero(t.comision()) + ")" : " sin comisión")
                    + textoPuntos(t);
        });
    }

    @PostMapping("/{id}/transferencia")
    public String transferir(@PathVariable int id,
                             @RequestParam int idMonederoOrigen,
                             @RequestParam int idMonederoDestino,
                             @RequestParam double monto,
                             @RequestParam(defaultValue = "OTROS") CategoriaGasto categoria,
                             @RequestParam(defaultValue = "") String descripcion,
                             @RequestParam(defaultValue = "cliente") String volver,
                             RedirectAttributes flash) {
        return intentar(flash, volver, id, "operaciones", () -> {
            TransaccionDto t = fabrica.transferir(id, idMonederoOrigen, idMonederoDestino, monto, categoria, descripcion);
            return "Transferiste " + dinero(t.monto()) + " al monedero #" + t.idMonederoDestino()
                    + (t.comision() > 0 ? " (comisión " + dinero(t.comision()) + ")" : "")
                    + textoPuntos(t);
        });
    }

    @PostMapping("/{id}/revertir")
    public String revertir(@PathVariable int id,
                           @RequestParam(defaultValue = "cliente") String volver,
                           RedirectAttributes flash) {
        return intentar(flash, volver, id, "operaciones", () -> {
            TransaccionDto t = fabrica.revertirUltimaTransaccion(id);
            return "Se revirtió la transacción #" + t.id() + " (" + t.tipo().name().toLowerCase() + " de "
                    + dinero(t.monto()) + ")"
                    + (t.puntosGenerados() > 0 ? " y se descontaron " + t.puntosGenerados() + " puntos" : "");
        });
    }

    @PostMapping("/{id}/canjear")
    public String canjear(@PathVariable int id,
                          @RequestParam TipoBeneficio tipo,
                          @RequestParam(required = false) Integer idMonedero,
                          @RequestParam(defaultValue = "cliente") String volver,
                          RedirectAttributes flash) {
        return intentar(flash, volver, id, "puntos", () -> {
            BeneficioDto b = fabrica.canjearBeneficio(id, tipo, idMonedero);
            return "Canjeaste «" + b.nombre() + "» por " + b.costoPuntos() + " puntos"
                    + (b.fechaVencimiento() != null ? ". Vigente hasta el " + b.fechaVencimiento().toLocalDate() : "");
        });
    }

    @PostMapping("/{id}/programar")
    public String programar(@PathVariable int id,
                            @RequestParam TipoTransaccion tipo,
                            @RequestParam(defaultValue = "0") int idMonederoOrigen,
                            @RequestParam(defaultValue = "0") int idMonederoDestino,
                            @RequestParam double monto,
                            @RequestParam(defaultValue = "OTROS") CategoriaGasto categoria,
                            @RequestParam(defaultValue = "") String descripcion,
                            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime fecha,
                            @RequestParam(defaultValue = "UNA_VEZ") Frecuencia frecuencia,
                            @RequestParam(defaultValue = "0") int repeticiones,
                            @RequestParam(defaultValue = "cliente") String volver,
                            RedirectAttributes flash) {
        return intentar(flash, volver, id, "programadas", () -> {
            TransaccionProgramadaDto p = fabrica.programar(id, tipo, idMonederoOrigen, idMonederoDestino,
                    monto, categoria, descripcion, fecha, frecuencia, repeticiones);
            return "Quedó programado «" + p.descripcion() + "» para el " + p.proximaEjecucion().format(FECHA)
                    + (frecuencia == Frecuencia.UNA_VEZ ? "" : " (" + frecuencia.name().toLowerCase().replace('_', ' ') + ")");
        });
    }

    @PostMapping("/{id}/programada/cancelar/{idProgramada}")
    public String cancelarProgramada(@PathVariable int id,
                                     @PathVariable int idProgramada,
                                     @RequestParam(defaultValue = "cliente") String volver,
                                     RedirectAttributes flash) {
        return intentar(flash, volver, id, "programadas", () -> {
            TransaccionProgramadaDto p = fabrica.cancelarProgramada(id, idProgramada);
            return "Cancelaste «" + p.descripcion() + "»";
        });
    }

    @PostMapping("/{id}/notificaciones/leidas")
    public String marcarLeidas(@PathVariable int id,
                               @RequestParam(defaultValue = "cliente") String volver) {
        fabrica.marcarNotificacionesLeidas(id);
        return volverA(volver, id, "notificaciones");
    }

    private String intentar(RedirectAttributes flash, String volver, int idCliente, String seccion,
                            Supplier<String> operacion) {
        try {
            flash.addFlashAttribute("mensaje", operacion.get());
        } catch (OperacionInvalidaException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return volverA(volver, idCliente, seccion);
    }

    private String volverA(String volver, int idCliente, String seccion) {
        String base = "admin".equals(volver) ? "redirect:/admin/cliente/" : "redirect:/cliente/";
        return base + idCliente + "/" + seccion;
    }

    private String textoPuntos(TransaccionDto t) {
        if (t.puntosGenerados() <= 0) {
            return "";
        }
        return ". +" + t.puntosGenerados() + " puntos"
                + (t.puntosExtra() > 0 ? " (incluye " + t.puntosExtra() + " extra por tu rango)" : "");
    }

    private static String dinero(double valor) {
        return ReglasNegocio.formatoDinero(valor);
    }
}
