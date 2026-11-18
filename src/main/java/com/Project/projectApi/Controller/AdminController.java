package com.Project.projectApi.Controller;

import com.Project.projectApi.DTO.ClienteDto;
import com.Project.projectApi.Enum.RangoCliente;
import com.Project.projectApi.ModelFactory.ModelFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ModelFactory fabrica = ModelFactory.getInstance();

    @GetMapping
    public String mostrarDashboard(Model model) {
        model.addAttribute("clientes", fabrica.obtenerClientes());
        model.addAttribute("estado", fabrica.obtenerEstadoSistema());
        model.addAttribute("ranking", fabrica.obtenerRanking(5));
        return "admin";
    }

    @GetMapping("/cliente/{id}")
    public String seleccionarCliente(@PathVariable int id, Model model) {
        return verCliente(id, "resumen", model);
    }

    @GetMapping("/cliente/{id}/{seccion}")
    public String verCliente(@PathVariable int id, @PathVariable String seccion, Model model) {
        model.addAttribute("clientes", fabrica.obtenerClientes());

        if (!DatosVistaCliente.cargar(model, fabrica, id, DatosVistaCliente.seccionValida(seccion))) {
            model.addAttribute("error", "No existe un cliente con identificación " + id);
            model.addAttribute("estado", fabrica.obtenerEstadoSistema());
            model.addAttribute("ranking", fabrica.obtenerRanking(5));
        }
        return "admin";
    }

    @PostMapping("/cliente")
    public String crearCliente(@RequestParam int id,
                               @RequestParam String nombre,
                               @RequestParam String correo,
                               RedirectAttributes flash) {
        ClienteDto dto = new ClienteDto(id, nombre.trim(), correo.trim());

        if (!fabrica.crearCliente(id, dto)) {
            flash.addFlashAttribute("error", "Ya existe un cliente con identificación " + id);
            return "redirect:/admin";
        }

        flash.addFlashAttribute("mensaje", "Listo, " + dto.nombre() + " ya es cliente. Créale su primer monedero.");
        return "redirect:/admin/cliente/" + id;
    }

    @PostMapping("/cliente/eliminar/{id}")
    public String eliminarCliente(@PathVariable int id, RedirectAttributes flash) {
        if (fabrica.eliminarCliente(id)) {
            flash.addFlashAttribute("mensaje", "Cliente " + id + " eliminado junto con sus monederos y programaciones");
        } else {
            flash.addFlashAttribute("error", "No existe un cliente con identificación " + id);
        }
        return "redirect:/admin";
    }

    @GetMapping("/reportes")
    public String reportes(Model model) {
        model.addAttribute("reporte", fabrica.obtenerReporteGeneral());
        model.addAttribute("ranking", fabrica.obtenerRanking(10));
        model.addAttribute("bronce", fabrica.obtenerClientesDelRango(RangoCliente.BRONCE));
        model.addAttribute("plata", fabrica.obtenerClientesDelRango(RangoCliente.PLATA));
        model.addAttribute("oro", fabrica.obtenerClientesDelRango(RangoCliente.ORO));
        model.addAttribute("platino", fabrica.obtenerClientesDelRango(RangoCliente.PLATINO));
        return "reportes";
    }

    @GetMapping("/programadas")
    public String programadas(Model model) {
        model.addAttribute("programadas", fabrica.obtenerTodasLasProgramadas());
        model.addAttribute("estado", fabrica.obtenerEstadoSistema());
        return "programadas";
    }

    @PostMapping("/programadas/procesar")
    public String procesarProgramadas(RedirectAttributes flash) {
        int ejecutadas = fabrica.procesarProgramadas();
        flash.addFlashAttribute("mensaje", ejecutadas == 0
                ? "No había nada pendiente por ejecutar. Los recordatorios quedaron al día."
                : "Se ejecutaron " + ejecutadas + " transacciones programadas.");
        return "redirect:/admin/programadas";
    }

    @GetMapping("/integridad")
    public String integridad(Model model) {
        model.addAttribute("reporte", fabrica.verificarIntegridad());
        model.addAttribute("estado", fabrica.obtenerEstadoSistema());
        return "integridad";
    }
}
