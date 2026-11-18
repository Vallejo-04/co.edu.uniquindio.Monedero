package com.Project.projectApi.Model;

import com.Project.projectApi.Enum.CategoriaGasto;
import com.Project.projectApi.Enum.Frecuencia;
import com.Project.projectApi.Enum.TipoNotificacion;
import com.Project.projectApi.Enum.TipoTransaccion;
import com.Project.projectApi.Excepcion.OperacionInvalidaException;
import com.Project.projectApi.List.LinkedList;
import com.Project.projectApi.Queue.PriorityQueue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PlanificadorTransacciones {

    private static final int MAXIMO_POR_RONDA = 500;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PriorityQueue<TransaccionProgramada> agenda = new PriorityQueue<>();
    private final MonederoSystem sistema;
    private final CentroNotificaciones notificaciones;
    private int siguienteId = 1;

    public PlanificadorTransacciones(MonederoSystem sistema, CentroNotificaciones notificaciones) {
        this.sistema = sistema;
        this.notificaciones = notificaciones;
    }

    public TransaccionProgramada programar(int idCliente, TipoTransaccion tipo, int idMonederoOrigen,
                                           int idMonederoDestino, double monto, CategoriaGasto categoria,
                                           String descripcion, LocalDateTime primeraEjecucion,
                                           Frecuencia frecuencia, int repeticiones) {
        Cliente cliente = sistema.clienteObligatorio(idCliente);

        if (!(monto > 0)) {
            throw new OperacionInvalidaException("El monto debe ser mayor que 0");
        }
        if (primeraEjecucion == null || primeraEjecucion.isBefore(LocalDateTime.now().minusMinutes(1))) {
            throw new OperacionInvalidaException("La fecha de ejecución debe ser futura");
        }
        if (tipo == null || frecuencia == null) {
            throw new OperacionInvalidaException("Falta el tipo de transacción o la frecuencia");
        }

        switch (tipo) {
            case DEPOSITO -> sistema.monederoDelCliente(cliente, idMonederoDestino);
            case RETIRO -> sistema.monederoDelCliente(cliente, idMonederoOrigen);
            case TRANSFERENCIA -> {
                sistema.monederoDelCliente(cliente, idMonederoOrigen);
                if (sistema.buscarMonedero(idMonederoDestino) == null) {
                    throw new OperacionInvalidaException("No existe el monedero destino #" + idMonederoDestino);
                }
                if (idMonederoOrigen == idMonederoDestino) {
                    throw new OperacionInvalidaException("El monedero origen y el destino deben ser distintos");
                }
            }
        }

        String texto = (descripcion == null || descripcion.isBlank())
                ? "Programada: " + tipo.name().toLowerCase()
                : descripcion.trim();

        TransaccionProgramada programada = new TransaccionProgramada(
                siguienteId++, idCliente, tipo,
                tipo == TipoTransaccion.DEPOSITO ? 0 : idMonederoOrigen,
                tipo == TipoTransaccion.RETIRO ? 0 : idMonederoDestino,
                ReglasNegocio.redondear(monto),
                categoria == null ? CategoriaGasto.OTROS : categoria,
                texto, primeraEjecucion, frecuencia, repeticiones);

        agenda.enqueue(programada);
        return programada;
    }

    public TransaccionProgramada cancelar(int idCliente, int idProgramada) {
        TransaccionProgramada cancelada = agenda.removeFirst(
                p -> p.getId() == idProgramada && p.getIdCliente() == idCliente);

        if (cancelada == null) {
            throw new OperacionInvalidaException("No encontramos la programación #" + idProgramada + " de este cliente");
        }
        return cancelada;
    }

    public void cancelarTodasDe(int idCliente) {
        while (agenda.removeFirst(p -> p.getIdCliente() == idCliente) != null) {
        }
    }

    public int procesarPendientes(LocalDateTime ahora) {
        int ejecutadas = 0;

        while (!agenda.isEmpty() && agenda.peek().tocaEjecutar(ahora) && ejecutadas < MAXIMO_POR_RONDA) {
            TransaccionProgramada programada = agenda.dequeue();
            ejecutar(programada);
            ejecutadas++;

            if (programada.avanzarASiguiente()) {
                agenda.enqueue(programada);
            }
        }

        enviarRecordatorios(ahora);
        notificaciones.despachar();
        return ejecutadas;
    }

    private void ejecutar(TransaccionProgramada p) {
        try {
            Transaccion hecha = switch (p.getTipo()) {
                case DEPOSITO -> sistema.depositar(p.getIdCliente(), p.getIdMonederoDestino(),
                        p.getMonto(), p.getDescripcion(), p.getId());
                case RETIRO -> sistema.retirar(p.getIdCliente(), p.getIdMonederoOrigen(),
                        p.getMonto(), p.getCategoria(), p.getDescripcion(), p.getId());
                case TRANSFERENCIA -> sistema.transferir(p.getIdCliente(), p.getIdMonederoOrigen(),
                        p.getIdMonederoDestino(), p.getMonto(), p.getCategoria(), p.getDescripcion(), p.getId());
            };

            p.registrarExito("Ejecutada como transacción #" + hecha.getId());
            notificaciones.enviar(p.getIdCliente(), TipoNotificacion.PROGRAMADA_EJECUTADA,
                    "Se ejecutó tu " + p.getTipo().name().toLowerCase() + " programado de "
                            + ReglasNegocio.formatoDinero(p.getMonto()) + " («" + p.getDescripcion() + "»).");

        } catch (OperacionInvalidaException e) {
            p.registrarFallo(e.getMessage());
            notificaciones.enviar(p.getIdCliente(), TipoNotificacion.PROGRAMADA_FALLIDA,
                    "No se pudo ejecutar «" + p.getDescripcion() + "»: " + e.getMessage());
        }
    }

    private void enviarRecordatorios(LocalDateTime ahora) {
        LocalDateTime limite = ahora.plusHours(24);

        for (TransaccionProgramada p : agenda) {
            if (p.getProximaEjecucion().isAfter(limite)) {
                break;
            }
            if (!p.isRecordatorioEnviado()) {
                notificaciones.enviar(p.getIdCliente(), TipoNotificacion.RECORDATORIO_PROGRAMADA,
                        "Recordatorio: «" + p.getDescripcion() + "» por " + ReglasNegocio.formatoDinero(p.getMonto())
                                + " se ejecutará el " + p.getProximaEjecucion().format(FORMATO_FECHA) + ".");
                p.marcarRecordatorioEnviado();
            }
        }
    }

    public LinkedList<TransaccionProgramada> deCliente(int idCliente) {
        LinkedList<TransaccionProgramada> suyas = new LinkedList<>();
        for (TransaccionProgramada p : agenda) {
            if (p.getIdCliente() == idCliente) {
                suyas.add(p);
            }
        }
        return suyas;
    }

    public LinkedList<TransaccionProgramada> todas() {
        LinkedList<TransaccionProgramada> lista = new LinkedList<>();
        for (TransaccionProgramada p : agenda) {
            lista.add(p);
        }
        return lista;
    }

    public TransaccionProgramada proxima() {
        return agenda.isEmpty() ? null : agenda.peek();
    }

    public int pendientes() {
        return agenda.size();
    }
}
