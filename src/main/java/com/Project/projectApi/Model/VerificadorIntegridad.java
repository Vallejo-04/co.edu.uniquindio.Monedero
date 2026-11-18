package com.Project.projectApi.Model;

import com.Project.projectApi.Enum.TipoTransaccion;
import com.Project.projectApi.Nodes.Node;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class VerificadorIntegridad {

    public static final String HUELLA_INICIAL = "0000000000000000";
    private static final double TOLERANCIA = 0.01;

    public static String calcularHuella(Transaccion t) {
        String contenido = t.getId() + "|" + t.getTipo() + "|" + t.getMonto() + "|" + t.getComision()
                + "|" + t.getIdCliente() + "|" + t.getIdMonederoOrigen() + "|" + t.getIdMonederoDestino()
                + "|" + t.getPuntosGenerados() + "|" + t.getFecha() + "|" + t.getHuellaAnterior();
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(contenido.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                hex.append(String.format("%02x", hash[i]));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("La JVM no tiene SHA-256", e);
        }
    }

    public ReporteIntegridad verificar(MonederoSystem sistema) {
        long inicio = System.nanoTime();
        ReporteIntegridad reporte = new ReporteIntegridad();
        Node<Transaccion> primera = sistema.getLibroMayor().getHead();
        int totalTransacciones = sistema.getLibroMayor().size();

        int antes = reporte.getProblemas().size();
        revisarCadena(primera, HUELLA_INICIAL, reporte);
        reporte.agregarRevision("Cadena de huellas",
                "Cada transacción está enlazada con la anterior y nadie la modificó",
                reporte.getProblemas().size() == antes, totalTransacciones);

        antes = reporte.getProblemas().size();
        revisarReglas(primera, reporte);
        reporte.agregarRevision("Reglas de cada transacción",
                "Montos positivos, puntos según el enunciado y saldos antes/después coherentes",
                reporte.getProblemas().size() == antes, totalTransacciones);

        antes = reporte.getProblemas().size();
        int monederos = 0;
        for (Cliente cliente : sistema.getClientes()) {
            for (Monedero monedero : cliente.getMonederos()) {
                monederos++;
                double esperado = ReglasNegocio.redondear(saldoSegunLibro(primera, monedero.getId()));
                if (Math.abs(esperado - monedero.getSaldo()) > TOLERANCIA) {
                    reporte.agregarProblema("El monedero #" + monedero.getId() + " de " + cliente.getNombre()
                            + " tiene " + ReglasNegocio.formatoDinero(monedero.getSaldo())
                            + " pero según el libro mayor debería tener " + ReglasNegocio.formatoDinero(esperado));
                }
            }
        }
        reporte.agregarRevision("Saldos de los monederos",
                "El saldo de cada monedero se reconstruye sumando sus transacciones",
                reporte.getProblemas().size() == antes, monederos);

        antes = reporte.getProblemas().size();
        for (Cliente cliente : sistema.getClientes()) {
            Node<MovimientoPuntos> movimientos = cliente.getHistorialPuntos().getHead();
            int acumulados = sumarAcumulados(movimientos);
            int disponibles = sumarDisponibles(movimientos);

            if (acumulados != cliente.getPuntos() || disponibles != cliente.getPuntosDisponibles()) {
                reporte.agregarProblema(cliente.getNombre() + " tiene " + cliente.getPuntos() + "/"
                        + cliente.getPuntosDisponibles() + " puntos (acumulados/disponibles) pero su historial suma "
                        + acumulados + "/" + disponibles);
            }
        }
        reporte.agregarRevision("Puntos de los clientes",
                "Los puntos se reconstruyen sumando el historial de puntos",
                reporte.getProblemas().size() == antes, sistema.getClientes().size());

        antes = reporte.getProblemas().size();
        GestorPuntos gestor = sistema.getGestorPuntos();

        if (!gestor.getArbolPuntos().esArbolDeBusquedaValido()) {
            reporte.agregarProblema("El árbol de puntos perdió el orden de árbol de búsqueda");
        }
        if (!gestor.getArbolPuntos().estaBalanceado()) {
            reporte.agregarProblema("El árbol de puntos dejó de estar balanceado (AVL)");
        }
        if (gestor.getArbolPuntos().size() != sistema.getClientes().size()) {
            reporte.agregarProblema("El árbol tiene " + gestor.getArbolPuntos().size() + " clientes y el sistema "
                    + sistema.getClientes().size());
        }
        for (Cliente cliente : sistema.getClientes()) {
            PuntajeCliente puntaje = gestor.puntajeDe(cliente.getId());
            if (puntaje == null || puntaje.puntos() != cliente.getPuntos()
                    || !gestor.getArbolPuntos().contains(puntaje)) {
                reporte.agregarProblema("El árbol/tabla de puntos no coincide con los puntos de " + cliente.getNombre());
            }
            if (sistema.buscarCliente(cliente.getId()) != cliente) {
                reporte.agregarProblema("La tabla hash no encuentra al cliente " + cliente.getId());
            }
        }
        reporte.agregarRevision("Árbol AVL de puntos y tabla hash",
                "Orden BST, balance AVL y coincidencia con cada cliente",
                reporte.getProblemas().size() == antes, gestor.getArbolPuntos().size());

        reporte.setDuracionMicros((System.nanoTime() - inicio) / 1000);
        return reporte;
    }

    private void revisarCadena(Node<Transaccion> nodo, String huellaAnteriorEsperada, ReporteIntegridad reporte) {
        if (nodo == null) {
            return;
        }

        Transaccion t = nodo.getData();

        if (!huellaAnteriorEsperada.equals(t.getHuellaAnterior())) {
            reporte.agregarProblema("La transacción #" + t.getId() + " no está enlazada con la anterior");
        }
        if (!calcularHuella(t).equals(t.getHuella())) {
            reporte.agregarProblema("La transacción #" + t.getId() + " fue modificada después de registrarse");
        }

        revisarCadena(nodo.getNext(), t.getHuella(), reporte);
    }

    private void revisarReglas(Node<Transaccion> nodo, ReporteIntegridad reporte) {
        if (nodo == null) {
            return;
        }

        Transaccion t = nodo.getData();
        String id = "La transacción #" + t.getId();

        if (!(t.getMonto() > 0)) {
            reporte.agregarProblema(id + " tiene un monto que no es positivo");
        }
        if (t.getComision() < 0 || t.getComision() > t.getMonto() * ReglasNegocio.COMISION_TRANSFERENCIA + TOLERANCIA) {
            reporte.agregarProblema(id + " tiene una comisión fuera de lo permitido");
        }

        boolean daPuntos = t.isReversible() && !t.esInterna();
        int baseEsperada = daPuntos ? ReglasNegocio.calcularPuntos(t.getTipo(), t.getMonto()) : 0;
        if (t.getPuntosBase() != baseEsperada || t.getPuntosGenerados() != t.getPuntosBase() + t.getPuntosExtra()) {
            reporte.agregarProblema(id + " no tiene los puntos que le corresponden");
        }

        double saldoEsperado = t.getTipo() == TipoTransaccion.DEPOSITO
                ? t.getSaldoAntes() + t.getMonto()
                : t.getSaldoAntes() - t.getTotalDebitado();
        if (Math.abs(saldoEsperado - t.getSaldoDespues()) > TOLERANCIA) {
            reporte.agregarProblema(id + " no cuadra: saldo antes y después no corresponden al monto");
        }

        revisarReglas(nodo.getNext(), reporte);
    }

    double saldoSegunLibro(Node<Transaccion> nodo, int idMonedero) {
        if (nodo == null) {
            return 0;
        }

        Transaccion t = nodo.getData();
        double efecto = 0;

        if (t.estaActiva()) {
            if (t.getIdMonederoDestino() == idMonedero) {
                efecto += t.getMonto();
            }
            if (t.getIdMonederoOrigen() == idMonedero) {
                efecto -= t.getTotalDebitado();
            }
        }

        return efecto + saldoSegunLibro(nodo.getNext(), idMonedero);
    }

    private int sumarAcumulados(Node<MovimientoPuntos> nodo) {
        return nodo == null ? 0 : nodo.getData().getCambioAcumulados() + sumarAcumulados(nodo.getNext());
    }

    private int sumarDisponibles(Node<MovimientoPuntos> nodo) {
        return nodo == null ? 0 : nodo.getData().getCambioDisponibles() + sumarDisponibles(nodo.getNext());
    }
}
