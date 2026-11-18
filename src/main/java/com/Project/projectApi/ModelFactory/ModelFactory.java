package com.Project.projectApi.ModelFactory;

import com.Project.projectApi.DTO.*;
import com.Project.projectApi.Enum.CategoriaGasto;
import com.Project.projectApi.Enum.Frecuencia;
import com.Project.projectApi.Enum.RangoCliente;
import com.Project.projectApi.Enum.TipoBeneficio;
import com.Project.projectApi.Enum.TipoTransaccion;
import com.Project.projectApi.List.LinkedList;
import com.Project.projectApi.Model.*;

import java.time.LocalDateTime;

public class ModelFactory {

    private static ModelFactory instance;

    private final MonederoSystem monederoSystem;
    private final MonederoMapper mapper;

    private ModelFactory() {
        monederoSystem = MonederoSystem.getInstance();
        mapper = new MonederoMapper();
        DatosDePrueba.cargar(monederoSystem);
    }

    public static synchronized ModelFactory getInstance() {
        if (instance == null) {
            instance = new ModelFactory();
        }
        return instance;
    }

    public MonederoSystem getMonederoSystem() {
        return monederoSystem;
    }

    public MonederoMapper getMapper() {
        return mapper;
    }

    public synchronized boolean crearCliente(int id, ClienteDto dto) {
        return monederoSystem.agregarCliente(mapper.dtoToCliente(dto, id));
    }

    public synchronized boolean eliminarCliente(int id) {
        return monederoSystem.eliminarCliente(id);
    }

    public synchronized boolean actualizarCliente(int id, ClienteDto dto) {
        return monederoSystem.actualizarCliente(id, dto.nombre(), dto.correo());
    }

    public synchronized ClienteDto obtenerCliente(int id) {
        Cliente cliente = monederoSystem.buscarCliente(id);
        if (cliente == null) {
            return null;
        }
        return mapper.clienteToDto(cliente, monederoSystem.getGestorPuntos().posicionDe(id));
    }

    public synchronized LinkedList<ClienteDto> obtenerClientes() {
        return mapper.mapList(monederoSystem.getClientes(), mapper::clienteToDto);
    }

    public synchronized LinkedList<MonederoDto> obtenerMonederos(int idCliente) {
        Cliente cliente = monederoSystem.buscarCliente(idCliente);
        return cliente == null ? new LinkedList<>() : mapper.mapList(cliente.getMonederos(), mapper::monederoToDto);
    }

    public synchronized LinkedList<TransaccionDto> obtenerTransacciones(int idCliente) {
        Cliente cliente = monederoSystem.buscarCliente(idCliente);
        if (cliente == null) {
            return new LinkedList<>();
        }
        return mapper.mapListInvertida(cliente.getHistorialTransacciones(),
                t -> mapper.transaccionToDto(t, idCliente));
    }

    public synchronized double obtenerSaldoTotal(int idCliente) {
        Cliente cliente = monederoSystem.buscarCliente(idCliente);
        return cliente == null ? 0 : cliente.getSaldoTotal();
    }

    public synchronized LinkedList<MonederoDto> obtenerMonederosDestino() {
        LinkedList<MonederoDto> destinos = new LinkedList<>();
        for (Cliente cliente : monederoSystem.getClientes()) {
            for (Monedero m : cliente.getMonederos()) {
                destinos.add(new MonederoDto(m.getId(), m.getNombre(), m.getSaldo(), cliente.getId(), cliente.getNombre()));
            }
        }
        return destinos;
    }

    public synchronized TransaccionDto obtenerUltimaReversible(int idCliente) {
        if (monederoSystem.buscarCliente(idCliente) == null) {
            return null;
        }
        Transaccion t = monederoSystem.verUltimaReversible(idCliente);
        return t == null ? null : mapper.transaccionToDto(t, idCliente);
    }

    public synchronized int[] obtenerVentajasDelRango(int idCliente) {
        Cliente cliente = monederoSystem.buscarCliente(idCliente);
        RangoCliente rango = cliente == null ? RangoCliente.BRONCE : cliente.getRango();
        return new int[]{
                (int) Math.round(ReglasNegocio.descuentoPorRango(rango) * 100),
                (int) Math.round(ReglasNegocio.bonoPuntosPorRango(rango) * 100)
        };
    }

    public synchronized LinkedList<MovimientoPuntosDto> obtenerHistorialPuntos(int idCliente) {
        Cliente cliente = monederoSystem.buscarCliente(idCliente);
        return cliente == null ? new LinkedList<>()
                : mapper.mapListInvertida(cliente.getHistorialPuntos(), mapper::movimientoToDto);
    }

    public synchronized LinkedList<BeneficioDto> obtenerBeneficios(int idCliente) {
        Cliente cliente = monederoSystem.buscarCliente(idCliente);
        return cliente == null ? new LinkedList<>()
                : mapper.mapListInvertida(cliente.getBeneficios(), mapper::beneficioToDto);
    }

    public synchronized LinkedList<BeneficioDto> obtenerCatalogoBeneficios() {
        return mapper.mapList(monederoSystem.getBeneficios(), mapper::beneficioToDto);
    }

    public synchronized BeneficioDto canjearBeneficio(int idCliente, TipoBeneficio tipo, Integer idMonedero) {
        return mapper.beneficioToDto(monederoSystem.canjearBeneficio(idCliente, tipo, idMonedero));
    }

    public synchronized LinkedList<PosicionRankingDto> obtenerRanking(int cantidad) {
        return aPosiciones(monederoSystem.ranking(cantidad));
    }

    public synchronized LinkedList<PosicionRankingDto> obtenerClientesDelRango(RangoCliente rango) {
        LinkedList<PosicionRankingDto> resultado = new LinkedList<>();
        for (PuntajeCliente p : monederoSystem.clientesDelRango(rango)) {
            Cliente c = monederoSystem.buscarCliente(p.idCliente());
            resultado.add(new PosicionRankingDto(monederoSystem.getGestorPuntos().posicionDe(p.idCliente()),
                    p.idCliente(), c.getNombre(), p.puntos(), c.getRango().name()));
        }
        return resultado;
    }

    private LinkedList<PosicionRankingDto> aPosiciones(LinkedList<PuntajeCliente> puntajes) {
        LinkedList<PosicionRankingDto> resultado = new LinkedList<>();
        int posicion = 1;
        for (PuntajeCliente p : puntajes) {
            Cliente c = monederoSystem.buscarCliente(p.idCliente());
            resultado.add(new PosicionRankingDto(posicion++, p.idCliente(), c.getNombre(), p.puntos(), c.getRango().name()));
        }
        return resultado;
    }

    public synchronized LinkedList<NotificacionDto> obtenerNotificaciones(int idCliente) {
        Cliente cliente = monederoSystem.buscarCliente(idCliente);
        return cliente == null ? new LinkedList<>()
                : mapper.mapListInvertida(cliente.getNotificaciones(), mapper::notificacionToDto);
    }

    public synchronized void marcarNotificacionesLeidas(int idCliente) {
        Cliente cliente = monederoSystem.buscarCliente(idCliente);
        if (cliente != null) {
            cliente.marcarNotificacionesLeidas();
        }
    }

    public synchronized MonederoDto crearMonedero(int idCliente, String nombre) {
        return mapper.monederoToDto(monederoSystem.crearMonedero(idCliente, nombre));
    }

    public synchronized boolean eliminarMonedero(int idCliente, int idMonedero) {
        return monederoSystem.eliminarMonedero(idCliente, idMonedero);
    }

    public synchronized TransaccionDto depositar(int idCliente, int idMonedero, double monto, String descripcion) {
        return mapper.transaccionToDto(monederoSystem.depositar(idCliente, idMonedero, monto, descripcion));
    }

    public synchronized TransaccionDto retirar(int idCliente, int idMonedero, double monto,
                                               CategoriaGasto categoria, String descripcion) {
        return mapper.transaccionToDto(monederoSystem.retirar(idCliente, idMonedero, monto, categoria, descripcion));
    }

    public synchronized TransaccionDto transferir(int idCliente, int idMonederoOrigen, int idMonederoDestino,
                                                  double monto, CategoriaGasto categoria, String descripcion) {
        return mapper.transaccionToDto(monederoSystem.transferir(idCliente, idMonederoOrigen, idMonederoDestino,
                monto, categoria, descripcion));
    }

    public synchronized TransaccionDto revertirUltimaTransaccion(int idCliente) {
        return mapper.transaccionToDto(monederoSystem.revertirUltimaTransaccion(idCliente));
    }

    public synchronized TransaccionProgramadaDto programar(int idCliente, TipoTransaccion tipo, int idMonederoOrigen,
                                                           int idMonederoDestino, double monto, CategoriaGasto categoria,
                                                           String descripcion, LocalDateTime fecha,
                                                           Frecuencia frecuencia, int repeticiones) {
        TransaccionProgramada p = monederoSystem.programar(idCliente, tipo, idMonederoOrigen, idMonederoDestino,
                monto, categoria, descripcion, fecha, frecuencia, repeticiones);
        return mapper.programadaToDto(p, nombreDe(idCliente));
    }

    public synchronized TransaccionProgramadaDto cancelarProgramada(int idCliente, int idProgramada) {
        return mapper.programadaToDto(monederoSystem.cancelarProgramada(idCliente, idProgramada), nombreDe(idCliente));
    }

    public synchronized LinkedList<TransaccionProgramadaDto> obtenerProgramadas(int idCliente) {
        return mapper.mapList(monederoSystem.getPlanificador().deCliente(idCliente),
                p -> mapper.programadaToDto(p, nombreDe(idCliente)));
    }

    public synchronized LinkedList<TransaccionProgramadaDto> obtenerTodasLasProgramadas() {
        return mapper.mapList(monederoSystem.getPlanificador().todas(),
                p -> mapper.programadaToDto(p, nombreDe(p.getIdCliente())));
    }

    public synchronized int procesarProgramadas() {
        return monederoSystem.procesarProgramadas(LocalDateTime.now());
    }

    public synchronized AnalizadorGastos.ReporteCliente obtenerAnalisisCliente(int idCliente) {
        return new AnalizadorGastos(monederoSystem).reporteDe(idCliente);
    }

    public synchronized AnalizadorGastos.ReporteGeneral obtenerReporteGeneral() {
        return new AnalizadorGastos(monederoSystem).reporteGeneral();
    }

    public synchronized ReporteIntegridad verificarIntegridad() {
        return new VerificadorIntegridad().verificar(monederoSystem);
    }

    public synchronized EstadoSistemaDto obtenerEstadoSistema() {
        int monederos = 0;
        double dinero = 0;
        for (Cliente c : monederoSystem.getClientes()) {
            monederos += c.getMonederos().size();
            dinero += c.getSaldoTotal();
        }

        TransaccionProgramada proxima = monederoSystem.getPlanificador().proxima();
        GestorPuntos puntos = monederoSystem.getGestorPuntos();

        return new EstadoSistemaDto(
                monederoSystem.getClientes().size(),
                monederos,
                monederoSystem.getLibroMayor().size(),
                ReglasNegocio.redondear(dinero),
                monederoSystem.getPlanificador().pendientes(),
                proxima == null ? null : mapper.programadaToDto(proxima, nombreDe(proxima.getIdCliente())),
                puntos.getArbolPuntos().height(),
                puntos.getArbolPuntos().estaBalanceado(),
                monederoSystem.getClientesPorId().capacidad(),
                monederoSystem.getClientesPorId().cadenaMasLarga(),
                monederoSystem.getCentroNotificaciones().getTotalEntregadas());
    }

    private String nombreDe(int idCliente) {
        Cliente c = monederoSystem.buscarCliente(idCliente);
        return c == null ? "Cliente " + idCliente : c.getNombre();
    }
}
