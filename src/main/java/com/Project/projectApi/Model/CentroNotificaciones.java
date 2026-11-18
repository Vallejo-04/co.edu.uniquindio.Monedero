package com.Project.projectApi.Model;

import com.Project.projectApi.Enum.TipoNotificacion;
import com.Project.projectApi.Queue.Queue;

public class CentroNotificaciones {

    private final Queue<Notificacion> porEntregar = new Queue<>();
    private final MonederoSystem sistema;
    private int siguienteId = 1;
    private int totalEntregadas;

    public CentroNotificaciones(MonederoSystem sistema) {
        this.sistema = sistema;
    }

    public void enviar(int idCliente, TipoNotificacion tipo, String mensaje) {
        porEntregar.enqueue(new Notificacion(siguienteId++, idCliente, tipo, mensaje));
    }

    public int despachar() {
        int entregadas = 0;

        while (!porEntregar.isEmpty()) {
            Notificacion notificacion = porEntregar.dequeue();
            Cliente destinatario = sistema.buscarCliente(notificacion.getIdCliente());

            if (destinatario != null) {
                destinatario.recibirNotificacion(notificacion);
                entregadas++;
            }
        }

        totalEntregadas += entregadas;
        return entregadas;
    }

    public void revisarSaldoBajo(Cliente cliente, Monedero monedero, double saldoAntes) {
        double umbral = ReglasNegocio.UMBRAL_SALDO_BAJO;

        if (saldoAntes >= umbral && monedero.getSaldo() < umbral) {
            enviar(cliente.getId(), TipoNotificacion.SALDO_BAJO,
                    "Tu monedero «" + monedero.getNombre() + "» (#" + monedero.getId() + ") quedó con "
                            + ReglasNegocio.formatoDinero(monedero.getSaldo())
                            + ", por debajo de " + ReglasNegocio.formatoDinero(umbral) + ".");
        }
    }

    public int pendientes() {
        return porEntregar.size();
    }

    public int getTotalEntregadas() {
        return totalEntregadas;
    }
}
