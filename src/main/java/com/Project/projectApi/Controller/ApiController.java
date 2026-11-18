package com.Project.projectApi.Controller;

import com.Project.projectApi.DTO.*;
import com.Project.projectApi.Enum.CategoriaGasto;
import com.Project.projectApi.Enum.Frecuencia;
import com.Project.projectApi.Enum.RangoCliente;
import com.Project.projectApi.Excepcion.OperacionInvalidaException;
import com.Project.projectApi.List.LinkedList;
import com.Project.projectApi.Model.AnalizadorGastos;
import com.Project.projectApi.Model.ReporteIntegridad;
import com.Project.projectApi.ModelFactory.ModelFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class ApiController {

    public record Error(String error) {
    }

    public record Saldo(int idCliente, double saldoTotal, LinkedList<MonederoDto> monederos) {
    }

    public record Puntos(ClienteDto cliente, int descuentoComisionesPorRango, int puntosExtraPorRango,
                         LinkedList<MovimientoPuntosDto> historial) {
    }

    public record Procesadas(int ejecutadas) {
    }

    private final ModelFactory fabrica = ModelFactory.getInstance();

    @GetMapping("/clientes")
    public LinkedList<ClienteDto> clientes() {
        return fabrica.obtenerClientes();
    }

    @GetMapping("/clientes/{id}")
    public ClienteDto cliente(@PathVariable int id) {
        return clienteQueExista(id);
    }

    @PostMapping("/clientes")
    public ResponseEntity<ClienteDto> crearCliente(@RequestBody ClienteDto datos) {
        if (datos.nombre() == null || datos.nombre().isBlank() || datos.correo() == null || datos.id() <= 0) {
            throw new OperacionInvalidaException("Envía id (mayor que 0), nombre y correo");
        }
        if (!fabrica.crearCliente(datos.id(), new ClienteDto(datos.id(), datos.nombre().trim(), datos.correo().trim()))) {
            throw new OperacionInvalidaException("Ya existe un cliente con identificación " + datos.id());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(fabrica.obtenerCliente(datos.id()));
    }

    @DeleteMapping("/clientes/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable int id) {
        clienteQueExista(id);
        fabrica.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/clientes/{id}/monederos")
    public LinkedList<MonederoDto> monederos(@PathVariable int id) {
        clienteQueExista(id);
        return fabrica.obtenerMonederos(id);
    }

    @PostMapping("/clientes/{id}/monederos")
    public ResponseEntity<MonederoDto> crearMonedero(@PathVariable int id, @RequestBody SolicitudMonederoDto datos) {
        clienteQueExista(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(fabrica.crearMonedero(id, datos.nombre()));
    }

    @DeleteMapping("/clientes/{id}/monederos/{idMonedero}")
    public ResponseEntity<Void> eliminarMonedero(@PathVariable int id, @PathVariable int idMonedero) {
        clienteQueExista(id);
        if (!fabrica.eliminarMonedero(id, idMonedero)) {
            throw new OperacionInvalidaException("El monedero #" + idMonedero + " no es de este cliente");
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/clientes/{id}/saldo")
    public Saldo saldo(@PathVariable int id) {
        clienteQueExista(id);
        return new Saldo(id, fabrica.obtenerSaldoTotal(id), fabrica.obtenerMonederos(id));
    }

    @GetMapping("/clientes/{id}/transacciones")
    public LinkedList<TransaccionDto> transacciones(@PathVariable int id) {
        clienteQueExista(id);
        return fabrica.obtenerTransacciones(id);
    }

    @PostMapping("/clientes/{id}/depositos")
    public ResponseEntity<TransaccionDto> depositar(@PathVariable int id, @RequestBody SolicitudOperacionDto datos) {
        clienteQueExista(id);
        TransaccionDto t = fabrica.depositar(id, requerido(datos.idMonedero(), "idMonedero"),
                requerido(datos.monto(), "monto"), datos.descripcion());
        return ResponseEntity.status(HttpStatus.CREATED).body(t);
    }

    @PostMapping("/clientes/{id}/retiros")
    public ResponseEntity<TransaccionDto> retirar(@PathVariable int id, @RequestBody SolicitudOperacionDto datos) {
        clienteQueExista(id);
        TransaccionDto t = fabrica.retirar(id, requerido(datos.idMonedero(), "idMonedero"),
                requerido(datos.monto(), "monto"), categoria(datos.categoria()), datos.descripcion());
        return ResponseEntity.status(HttpStatus.CREATED).body(t);
    }

    @PostMapping("/clientes/{id}/transferencias")
    public ResponseEntity<TransaccionDto> transferir(@PathVariable int id, @RequestBody SolicitudOperacionDto datos) {
        clienteQueExista(id);
        TransaccionDto t = fabrica.transferir(id,
                requerido(datos.idMonederoOrigen(), "idMonederoOrigen"),
                requerido(datos.idMonederoDestino(), "idMonederoDestino"),
                requerido(datos.monto(), "monto"), categoria(datos.categoria()), datos.descripcion());
        return ResponseEntity.status(HttpStatus.CREATED).body(t);
    }

    @PostMapping("/clientes/{id}/revertir")
    public TransaccionDto revertir(@PathVariable int id) {
        clienteQueExista(id);
        return fabrica.revertirUltimaTransaccion(id);
    }

    @GetMapping("/clientes/{id}/puntos")
    public Puntos puntos(@PathVariable int id) {
        ClienteDto cliente = clienteQueExista(id);
        int[] ventajas = fabrica.obtenerVentajasDelRango(id);
        return new Puntos(cliente, ventajas[0], ventajas[1], fabrica.obtenerHistorialPuntos(id));
    }

    @GetMapping("/beneficios")
    public LinkedList<BeneficioDto> catalogo() {
        return fabrica.obtenerCatalogoBeneficios();
    }

    @GetMapping("/clientes/{id}/beneficios")
    public LinkedList<BeneficioDto> beneficiosDelCliente(@PathVariable int id) {
        clienteQueExista(id);
        return fabrica.obtenerBeneficios(id);
    }

    @PostMapping("/clientes/{id}/canjes")
    public ResponseEntity<BeneficioDto> canjear(@PathVariable int id, @RequestBody SolicitudCanjeDto datos) {
        clienteQueExista(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fabrica.canjearBeneficio(id, datos.tipo(), datos.idMonedero()));
    }

    @GetMapping("/ranking")
    public LinkedList<PosicionRankingDto> ranking(@RequestParam(defaultValue = "10") int top) {
        return fabrica.obtenerRanking(Math.max(1, top));
    }

    @GetMapping("/rangos/{rango}")
    public LinkedList<PosicionRankingDto> clientesDelRango(@PathVariable RangoCliente rango) {
        return fabrica.obtenerClientesDelRango(rango);
    }

    @GetMapping("/clientes/{id}/programadas")
    public LinkedList<TransaccionProgramadaDto> programadas(@PathVariable int id) {
        clienteQueExista(id);
        return fabrica.obtenerProgramadas(id);
    }

    @PostMapping("/clientes/{id}/programadas")
    public ResponseEntity<TransaccionProgramadaDto> programar(@PathVariable int id,
                                                              @RequestBody SolicitudProgramacionDto datos) {
        clienteQueExista(id);
        TransaccionProgramadaDto p = fabrica.programar(id, datos.tipo(),
                datos.idMonederoOrigen() == null ? 0 : datos.idMonederoOrigen(),
                datos.idMonederoDestino() == null ? 0 : datos.idMonederoDestino(),
                requerido(datos.monto(), "monto"), categoria(datos.categoria()), datos.descripcion(),
                datos.fechaEjecucion(),
                datos.frecuencia() == null ? Frecuencia.UNA_VEZ : datos.frecuencia(),
                datos.repeticiones() == null ? 0 : datos.repeticiones());
        return ResponseEntity.status(HttpStatus.CREATED).body(p);
    }

    @DeleteMapping("/clientes/{id}/programadas/{idProgramada}")
    public TransaccionProgramadaDto cancelarProgramada(@PathVariable int id, @PathVariable int idProgramada) {
        clienteQueExista(id);
        return fabrica.cancelarProgramada(id, idProgramada);
    }

    @GetMapping("/programadas")
    public LinkedList<TransaccionProgramadaDto> todasLasProgramadas() {
        return fabrica.obtenerTodasLasProgramadas();
    }

    @PostMapping("/programadas/procesar")
    public Procesadas procesar() {
        return new Procesadas(fabrica.procesarProgramadas());
    }

    @GetMapping("/clientes/{id}/notificaciones")
    public LinkedList<NotificacionDto> notificaciones(@PathVariable int id) {
        clienteQueExista(id);
        return fabrica.obtenerNotificaciones(id);
    }

    @PostMapping("/clientes/{id}/notificaciones/leidas")
    public ResponseEntity<Void> marcarLeidas(@PathVariable int id) {
        clienteQueExista(id);
        fabrica.marcarNotificacionesLeidas(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/clientes/{id}/analisis")
    public AnalizadorGastos.ReporteCliente analisis(@PathVariable int id) {
        clienteQueExista(id);
        return fabrica.obtenerAnalisisCliente(id);
    }

    @GetMapping("/reportes/general")
    public AnalizadorGastos.ReporteGeneral reporteGeneral() {
        return fabrica.obtenerReporteGeneral();
    }

    @GetMapping("/integridad")
    public ReporteIntegridad integridad() {
        return fabrica.verificarIntegridad();
    }

    @GetMapping("/sistema")
    public EstadoSistemaDto estado() {
        return fabrica.obtenerEstadoSistema();
    }

    @ExceptionHandler(OperacionInvalidaException.class)
    public ResponseEntity<Error> operacionInvalida(OperacionInvalidaException e) {
        return ResponseEntity.badRequest().body(new Error(e.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Error> noEncontrado(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(new Error(e.getReason()));
    }

    private ClienteDto clienteQueExista(int id) {
        ClienteDto cliente = fabrica.obtenerCliente(id);
        if (cliente == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el cliente " + id);
        }
        return cliente;
    }

    private static <T> T requerido(T valor, String campo) {
        if (valor == null) {
            throw new OperacionInvalidaException("Falta el campo \"" + campo + "\"");
        }
        return valor;
    }

    private static CategoriaGasto categoria(CategoriaGasto categoria) {
        return categoria == null ? CategoriaGasto.OTROS : categoria;
    }
}
