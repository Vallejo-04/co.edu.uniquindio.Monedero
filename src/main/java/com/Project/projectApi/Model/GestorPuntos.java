package com.Project.projectApi.Model;

import com.Project.projectApi.Enum.RangoCliente;
import com.Project.projectApi.Enum.TipoBeneficio;
import com.Project.projectApi.Enum.TipoMovimientoPuntos;
import com.Project.projectApi.Enum.TipoNotificacion;
import com.Project.projectApi.Excepcion.OperacionInvalidaException;
import com.Project.projectApi.Hash.HashTable;
import com.Project.projectApi.List.LinkedList;
import com.Project.projectApi.Tree.AVLTree;

public class GestorPuntos {

    private final AVLTree<PuntajeCliente> arbolPuntos = new AVLTree<>();
    private final HashTable<Integer, PuntajeCliente> puntajePorCliente = new HashTable<>();
    private final CentroNotificaciones notificaciones;

    public GestorPuntos(CentroNotificaciones notificaciones) {
        this.notificaciones = notificaciones;
    }

    public void registrarCliente(Cliente cliente) {
        PuntajeCliente puntaje = new PuntajeCliente(cliente.getPuntos(), cliente.getId());
        arbolPuntos.insert(puntaje);
        puntajePorCliente.put(cliente.getId(), puntaje);
    }

    public void quitarCliente(int idCliente) {
        PuntajeCliente puntaje = puntajePorCliente.remove(idCliente);
        if (puntaje != null) {
            arbolPuntos.remove(puntaje);
        }
    }

    public void sumar(Cliente cliente, int puntos, String motivo, int idTransaccion) {
        if (puntos > 0) {
            aplicar(cliente, TipoMovimientoPuntos.GANADOS, puntos, puntos, motivo, idTransaccion);
        }
    }

    public void restarPorReversion(Cliente cliente, int puntos, String motivo, int idTransaccion) {
        if (puntos <= 0) {
            return;
        }
        int deAcumulados = Math.min(puntos, cliente.getPuntos());
        int deDisponibles = Math.min(puntos, cliente.getPuntosDisponibles());

        aplicar(cliente, TipoMovimientoPuntos.REVERTIDOS, -deAcumulados, -deDisponibles, motivo, idTransaccion);
    }

    public void cobrarCanje(Cliente cliente, TipoBeneficio beneficio) {
        int costo = beneficio.getCostoPuntos();

        if (cliente.getPuntosDisponibles() < costo) {
            throw new OperacionInvalidaException("Te faltan " + (costo - cliente.getPuntosDisponibles())
                    + " puntos para canjear «" + beneficio.getNombre() + "»");
        }

        aplicar(cliente, TipoMovimientoPuntos.CANJEADOS, 0, -costo, "Canje: " + beneficio.getNombre(), 0);
    }

    private void aplicar(Cliente cliente, TipoMovimientoPuntos tipo, int cambioAcumulados,
                         int cambioDisponibles, String motivo, int idTransaccion) {
        RangoCliente rangoAnterior = cliente.getRango();

        cliente.ajustarPuntos(cambioAcumulados, cambioDisponibles);
        cliente.registrarMovimiento(new MovimientoPuntos(tipo, cambioAcumulados, cambioDisponibles, motivo,
                idTransaccion, cliente.getPuntos(), cliente.getPuntosDisponibles()));

        if (cambioAcumulados != 0) {
            reubicarEnElArbol(cliente);
        }

        if (cliente.getRango() != rangoAnterior) {
            avisarCambioDeRango(cliente, rangoAnterior);
        }
    }

    private void reubicarEnElArbol(Cliente cliente) {
        PuntajeCliente anterior = puntajePorCliente.get(cliente.getId());
        if (anterior != null) {
            arbolPuntos.remove(anterior);
        }

        PuntajeCliente actual = new PuntajeCliente(cliente.getPuntos(), cliente.getId());
        arbolPuntos.insert(actual);
        puntajePorCliente.put(cliente.getId(), actual);
    }

    private void avisarCambioDeRango(Cliente cliente, RangoCliente anterior) {
        boolean subio = cliente.getRango().ordinal() > anterior.ordinal();
        String mensaje = subio
                ? "¡Felicidades! Subiste de " + anterior + " a " + cliente.getRango()
                + ". Ahora pagas menos comisión y ganas más puntos."
                : "Tu rango bajó de " + anterior + " a " + cliente.getRango() + " por una transacción revertida.";

        notificaciones.enviar(cliente.getId(), TipoNotificacion.CAMBIO_DE_RANGO, mensaje);
    }

    public LinkedList<PuntajeCliente> ranking(int cantidad) {
        return arbolPuntos.mayores(cantidad);
    }

    public LinkedList<PuntajeCliente> clientesDelRango(RangoCliente rango) {
        LinkedList<PuntajeCliente> ascendente = arbolPuntos.rango(
                new PuntajeCliente(rango.getMinimo(), Integer.MIN_VALUE),
                new PuntajeCliente(rango.getMaximo(), Integer.MAX_VALUE));

        LinkedList<PuntajeCliente> descendente = new LinkedList<>();
        for (PuntajeCliente p : ascendente) {
            descendente.addFirst(p);
        }
        return descendente;
    }

    public int posicionDe(int idCliente) {
        PuntajeCliente suyo = puntajePorCliente.get(idCliente);
        if (suyo == null) {
            return 0;
        }
        PuntajeCliente maximo = new PuntajeCliente(Integer.MAX_VALUE, Integer.MAX_VALUE);
        return arbolPuntos.rango(suyo, maximo).size();
    }

    public PuntajeCliente puntajeDe(int idCliente) {
        return puntajePorCliente.get(idCliente);
    }

    public AVLTree<PuntajeCliente> getArbolPuntos() {
        return arbolPuntos;
    }

    public HashTable<Integer, PuntajeCliente> getPuntajePorCliente() {
        return puntajePorCliente;
    }
}
