package com.Project.projectApi.DTO;

import com.Project.projectApi.Model.Beneficio;
import com.Project.projectApi.Model.Cliente;
import com.Project.projectApi.Model.Monedero;
import com.Project.projectApi.Model.MovimientoPuntos;
import com.Project.projectApi.Model.Notificacion;
import com.Project.projectApi.Model.Transaccion;
import com.Project.projectApi.Model.TransaccionProgramada;
import com.Project.projectApi.Enum.RangoCliente;
import com.Project.projectApi.List.LinkedList;

import java.time.LocalDateTime;
import java.util.function.Function;

public class MonederoMapper {

    public Cliente dtoToCliente(ClienteDto dto, int id) {
        return new Cliente(id, dto.nombre(), dto.correo());
    }

    public ClienteDto clienteToDto(Cliente cliente) {
        return clienteToDto(cliente, 0);
    }

    public ClienteDto clienteToDto(Cliente cliente, int posicionRanking) {
        RangoCliente siguiente = cliente.getRango().siguiente();

        return new ClienteDto(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getCorreo(),
                cliente.getPuntos(),
                cliente.getPuntosDisponibles(),
                cliente.getRango().name(),
                siguiente == null ? null : siguiente.name(),
                cliente.getPuntosParaSiguienteRango(),
                posicionRanking,
                cliente.contarNoLeidas()
        );
    }

    public Monedero dtoToMonedero(MonederoDto dto, int id) {
        return new Monedero(id, dto.nombre(), dto.saldo());
    }

    public MonederoDto monederoToDto(Monedero monedero) {
        return new MonederoDto(monedero.getId(), monedero.getNombre(), monedero.getSaldo(),
                monedero.getIdCliente(), null);
    }

    public Transaccion dtoToTransaccion(TransaccionDto dto, int id) {
        return new Transaccion(id, dto.tipo(), dto.monto(), LocalDateTime.now(), dto.descripcion());
    }

    public TransaccionDto transaccionToDto(Transaccion transaccion) {
        return transaccionToDto(transaccion, transaccion.getIdCliente());
    }

    public TransaccionDto transaccionToDto(Transaccion t, int idClienteQueMira) {
        return new TransaccionDto(
                t.getId(), t.getTipo(), t.getMonto(), t.getFecha(), t.getDescripcion(),
                t.getComision(), t.getPuntosGenerados(), t.getPuntosExtra(), t.getCategoria(), t.getEstado(),
                t.getIdMonederoOrigen(), t.getIdMonederoDestino(),
                direccion(t, idClienteQueMira),
                t.esAutomatica(), t.isReversible(), t.getHuella());
    }

    private String direccion(Transaccion t, int idClienteQueMira) {
        return switch (t.getTipo()) {
            case DEPOSITO -> "ENTRADA";
            case RETIRO -> "SALIDA";
            case TRANSFERENCIA -> {
                if (t.esInterna()) {
                    yield "INTERNA";
                }
                yield t.getIdCliente() == idClienteQueMira ? "SALIDA" : "ENTRADA";
            }
        };
    }

    public TransaccionProgramadaDto programadaToDto(TransaccionProgramada p, String nombreCliente) {
        return new TransaccionProgramadaDto(p.getId(), p.getIdCliente(), nombreCliente, p.getTipo(),
                p.getIdMonederoOrigen(), p.getIdMonederoDestino(), p.getMonto(), p.getCategoria(),
                p.getDescripcion(), p.getFrecuencia(), p.getProximaEjecucion(), p.getEjecucionesRestantes(),
                p.getVecesEjecutada(), p.getVecesFallida(), p.getUltimoResultado());
    }

    public Beneficio dtoToBeneficio(BeneficioDto dto, int id) {
        return new Beneficio(id, dto.nombre(), dto.costoPuntos(), dto.descripcion());
    }

    public BeneficioDto beneficioToDto(Beneficio b) {
        return new BeneficioDto(b.getId(), b.getNombre(), b.getCostoPuntos(), b.getDescripcion(), b.getTipo(),
                b.getFechaCanje(), b.getFechaVencimiento(), b.estaVigente(LocalDateTime.now()));
    }

    public MovimientoPuntosDto movimientoToDto(MovimientoPuntos m) {
        return new MovimientoPuntosDto(m.getFecha(), m.getTipo(), m.getCambioAcumulados(),
                m.getCambioDisponibles(), m.getMotivo(), m.getIdTransaccion(),
                m.getAcumuladosDespues(), m.getDisponiblesDespues());
    }

    public Notificacion dtoToNotificacion(NotificacionDto dto, int id) {
        return new Notificacion(id, dto.mensaje());
    }

    public NotificacionDto notificacionToDto(Notificacion n) {
        return new NotificacionDto(n.getId(), n.getMensaje(), n.getTipo(), n.getFecha(), n.isLeida());
    }

    public <M, D> LinkedList<D> mapList(Iterable<M> origen, Function<M, D> conversor) {
        LinkedList<D> destino = new LinkedList<>();
        for (M elemento : origen) {
            destino.add(conversor.apply(elemento));
        }
        return destino;
    }

    public <M, D> LinkedList<D> mapListInvertida(Iterable<M> origen, Function<M, D> conversor) {
        LinkedList<D> destino = new LinkedList<>();
        for (M elemento : origen) {
            destino.addFirst(conversor.apply(elemento));
        }
        return destino;
    }
}
