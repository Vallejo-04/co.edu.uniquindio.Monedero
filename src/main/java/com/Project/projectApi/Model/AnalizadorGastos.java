package com.Project.projectApi.Model;

import com.Project.projectApi.Enum.CategoriaGasto;
import com.Project.projectApi.Enum.TipoTransaccion;
import com.Project.projectApi.Graph.Arista;
import com.Project.projectApi.Graph.Graph;
import com.Project.projectApi.Hash.HashTable;
import com.Project.projectApi.List.LinkedList;
import com.Project.projectApi.Queue.PriorityQueue;

public class AnalizadorGastos {

    public record GastoCategoria(CategoriaGasto categoria, double total, int cantidad, double porcentaje)
            implements Comparable<GastoCategoria> {
        @Override
        public int compareTo(GastoCategoria otro) {
            return Double.compare(otro.total, total);
        }
    }

    public record Flujo(String origen, String destino, double total, int cantidad)
            implements Comparable<Flujo> {
        @Override
        public int compareTo(Flujo otro) {
            int porCantidad = Integer.compare(otro.cantidad, cantidad);
            return porCantidad != 0 ? porCantidad : Double.compare(otro.total, total);
        }
    }

    public record MontoCliente(int idCliente, String nombre, double total) implements Comparable<MontoCliente> {
        @Override
        public int compareTo(MontoCliente otro) {
            return Double.compare(otro.total, total);
        }
    }

    public record ReporteCliente(
            int idCliente,
            int transaccionesActivas,
            int transaccionesRevertidas,
            double totalDepositado,
            double totalRetirado,
            double totalEnviado,
            double totalRecibido,
            double totalComisiones,
            double gastoTotal,
            double mayorGasto,
            String descripcionMayorGasto,
            LinkedList<GastoCategoria> gastosPorCategoria,
            LinkedList<Flujo> habitosEntreCategorias,
            LinkedList<Flujo> flujoEntreMonederos,
            LinkedList<String> alcanceDeSuDinero) {

        public GastoCategoria categoriaPrincipal() {
            return gastosPorCategoria.isEmpty() ? null : gastosPorCategoria.get(0);
        }
    }

    public record ReporteGeneral(
            int clientes,
            int monederos,
            int transaccionesActivas,
            double dineroEnElSistema,
            double volumenTransferido,
            double comisionesCobradas,
            LinkedList<Flujo> transferenciasEntreClientes,
            LinkedList<MontoCliente> quienesMasEnvian,
            LinkedList<MontoCliente> quienesMasReciben,
            LinkedList<String> cicloDetectado,
            LinkedList<GastoCategoria> gastosPorCategoria,
            LinkedList<Flujo> habitosEntreCategorias) {
    }

    private static class Acumulado {
        double total;
        int cantidad;
    }

    private final MonederoSystem sistema;

    public AnalizadorGastos(MonederoSystem sistema) {
        this.sistema = sistema;
    }

    public ReporteCliente reporteDe(int idCliente) {
        Cliente cliente = sistema.clienteObligatorio(idCliente);

        int activas = 0;
        int revertidas = 0;
        double depositado = 0;
        double retirado = 0;
        double enviado = 0;
        double recibido = 0;
        double comisiones = 0;
        double mayorGasto = 0;
        String descripcionMayorGasto = null;

        HashTable<CategoriaGasto, Acumulado> porCategoria = new HashTable<>();
        Graph<CategoriaGasto> habitos = new Graph<>();
        Graph<String> monederos = new Graph<>();
        CategoriaGasto categoriaAnterior = null;

        for (Transaccion t : cliente.getHistorialTransacciones()) {
            if (!t.estaActiva()) {
                revertidas++;
                continue;
            }
            activas++;

            boolean laHizoEl = t.getIdCliente() == idCliente;
            registrarFlujoDeMonederos(monederos, t, idCliente);

            switch (t.getTipo()) {
                case DEPOSITO -> depositado += t.getMonto();
                case RETIRO -> retirado += t.getMonto();
                case TRANSFERENCIA -> {
                    if (t.esInterna()) {
                        continue;
                    }
                    if (laHizoEl) {
                        enviado += t.getMonto();
                    } else {
                        recibido += t.getMonto();
                        continue;
                    }
                }
            }

            if (!laHizoEl) {
                continue;
            }
            comisiones += t.getComision();

            if (t.getTipo() == TipoTransaccion.DEPOSITO) {
                continue;
            }

            sumarCategoria(porCategoria, t.getCategoria(), t.getMonto());

            if (categoriaAnterior != null) {
                habitos.addEdge(categoriaAnterior, t.getCategoria(), t.getMonto());
            }
            categoriaAnterior = t.getCategoria();

            if (t.getMonto() > mayorGasto) {
                mayorGasto = t.getMonto();
                descripcionMayorGasto = t.getDescripcion() + " (" + t.getCategoria() + ")";
            }
        }

        double gastoTotal = retirado + enviado;

        LinkedList<String> alcance = new LinkedList<>();
        for (Integer idAlcanzado : grafoDeTransferencias().alcanzablesDesde(idCliente)) {
            alcance.add(nombreDe(idAlcanzado));
        }

        return new ReporteCliente(idCliente, activas, revertidas,
                redondear(depositado), redondear(retirado), redondear(enviado), redondear(recibido),
                redondear(comisiones), redondear(gastoTotal), mayorGasto, descripcionMayorGasto,
                ordenarCategorias(porCategoria, gastoTotal),
                aristasDeCategorias(habitos),
                aristasDeTexto(monederos),
                alcance);
    }

    private void registrarFlujoDeMonederos(Graph<String> grafo, Transaccion t, int idCliente) {
        switch (t.getTipo()) {
            case DEPOSITO -> grafo.addEdge("Ingresos", nombreMonedero(t.getIdMonederoDestino(), idCliente), t.getMonto());
            case RETIRO -> grafo.addEdge(nombreMonedero(t.getIdMonederoOrigen(), idCliente),
                    "Retiros · " + t.getCategoria(), t.getMonto());
            case TRANSFERENCIA -> grafo.addEdge(nombreMonedero(t.getIdMonederoOrigen(), idCliente),
                    nombreMonedero(t.getIdMonederoDestino(), idCliente), t.getMonto());
        }
    }

    public ReporteGeneral reporteGeneral() {
        int monederos = 0;
        double dinero = 0;
        for (Cliente c : sistema.getClientes()) {
            monederos += c.getMonederos().size();
            dinero += c.getSaldoTotal();
        }

        int activas = 0;
        double volumenTransferido = 0;
        double comisiones = 0;
        HashTable<CategoriaGasto, Acumulado> porCategoria = new HashTable<>();
        double gastoTotal = 0;

        for (Transaccion t : sistema.getLibroMayor()) {
            if (!t.estaActiva()) {
                continue;
            }
            activas++;
            comisiones += t.getComision();

            boolean esGasto = t.getTipo() == TipoTransaccion.RETIRO
                    || (t.getTipo() == TipoTransaccion.TRANSFERENCIA && !t.esInterna());

            if (t.getTipo() == TipoTransaccion.TRANSFERENCIA && !t.esInterna()) {
                volumenTransferido += t.getMonto();
            }
            if (esGasto) {
                sumarCategoria(porCategoria, t.getCategoria(), t.getMonto());
                gastoTotal += t.getMonto();
            }
        }

        Graph<Integer> transferencias = grafoDeTransferencias();

        Graph<CategoriaGasto> habitos = new Graph<>();
        for (Cliente c : sistema.getClientes()) {
            CategoriaGasto anterior = null;
            for (Transaccion t : c.getHistorialTransacciones()) {
                boolean esGastoSuyo = t.estaActiva() && t.getIdCliente() == c.getId()
                        && (t.getTipo() == TipoTransaccion.RETIRO
                        || (t.getTipo() == TipoTransaccion.TRANSFERENCIA && !t.esInterna()));
                if (esGastoSuyo) {
                    if (anterior != null) {
                        habitos.addEdge(anterior, t.getCategoria(), t.getMonto());
                    }
                    anterior = t.getCategoria();
                }
            }
        }

        PriorityQueue<MontoCliente> envian = new PriorityQueue<>();
        PriorityQueue<MontoCliente> reciben = new PriorityQueue<>();
        for (Integer id : transferencias.vertices()) {
            double salida = transferencias.pesoSalida(id);
            double entrada = transferencias.pesoEntrada(id);
            if (salida > 0) {
                envian.enqueue(new MontoCliente(id, nombreDe(id), redondear(salida)));
            }
            if (entrada > 0) {
                reciben.enqueue(new MontoCliente(id, nombreDe(id), redondear(entrada)));
            }
        }

        LinkedList<String> ciclo = new LinkedList<>();
        for (Integer id : transferencias.buscarCiclo()) {
            ciclo.add(nombreDe(id));
        }

        LinkedList<Flujo> flujos = new LinkedList<>();
        PriorityQueue<Flujo> ordenados = new PriorityQueue<>();
        for (Arista<Integer> a : transferencias.edges()) {
            ordenados.enqueue(new Flujo(nombreDe(a.origen()), nombreDe(a.destino()), redondear(a.peso()), a.cantidad()));
        }
        while (!ordenados.isEmpty()) {
            flujos.add(ordenados.dequeue());
        }

        return new ReporteGeneral(sistema.getClientes().size(), monederos, activas,
                redondear(dinero), redondear(volumenTransferido), redondear(comisiones),
                flujos, primeros(envian, 5), primeros(reciben, 5), ciclo,
                ordenarCategorias(porCategoria, gastoTotal), aristasDeCategorias(habitos));
    }

    public Graph<Integer> grafoDeTransferencias() {
        Graph<Integer> grafo = new Graph<>();
        for (Cliente c : sistema.getClientes()) {
            grafo.addVertex(c.getId());
        }
        for (Transaccion t : sistema.getLibroMayor()) {
            if (t.estaActiva() && t.getTipo() == TipoTransaccion.TRANSFERENCIA && !t.esInterna()) {
                grafo.addEdge(t.getIdCliente(), t.getIdClienteDestino(), t.getMonto());
            }
        }
        return grafo;
    }

    private static void sumarCategoria(HashTable<CategoriaGasto, Acumulado> tabla, CategoriaGasto categoria, double monto) {
        Acumulado acumulado = tabla.get(categoria);
        if (acumulado == null) {
            acumulado = new Acumulado();
            tabla.put(categoria, acumulado);
        }
        acumulado.total += monto;
        acumulado.cantidad++;
    }

    private static LinkedList<GastoCategoria> ordenarCategorias(HashTable<CategoriaGasto, Acumulado> tabla, double total) {
        PriorityQueue<GastoCategoria> cola = new PriorityQueue<>();
        for (CategoriaGasto categoria : tabla.keys()) {
            Acumulado a = tabla.get(categoria);
            double porcentaje = total > 0 ? Math.round(a.total * 1000 / total) / 10.0 : 0;
            cola.enqueue(new GastoCategoria(categoria, redondear(a.total), a.cantidad, porcentaje));
        }
        return primeros(cola, Integer.MAX_VALUE);
    }

    private static LinkedList<Flujo> aristasDeCategorias(Graph<CategoriaGasto> grafo) {
        PriorityQueue<Flujo> cola = new PriorityQueue<>();
        for (Arista<CategoriaGasto> a : grafo.edges()) {
            cola.enqueue(new Flujo(a.origen().name(), a.destino().name(), redondear(a.peso()), a.cantidad()));
        }
        return primeros(cola, 10);
    }

    private static LinkedList<Flujo> aristasDeTexto(Graph<String> grafo) {
        PriorityQueue<Flujo> cola = new PriorityQueue<>();
        for (Arista<String> a : grafo.edges()) {
            cola.enqueue(new Flujo(a.origen(), a.destino(), redondear(a.peso()), a.cantidad()));
        }
        return primeros(cola, Integer.MAX_VALUE);
    }

    private static <T extends Comparable<T>> LinkedList<T> primeros(PriorityQueue<T> cola, int cuantos) {
        LinkedList<T> resultado = new LinkedList<>();
        while (!cola.isEmpty() && resultado.size() < cuantos) {
            resultado.add(cola.dequeue());
        }
        return resultado;
    }

    private String nombreDe(int idCliente) {
        Cliente c = sistema.buscarCliente(idCliente);
        return c == null ? "Cliente " + idCliente + " (eliminado)" : c.getNombre();
    }

    private String nombreMonedero(int idMonedero, int idClienteQueMira) {
        Monedero m = sistema.buscarMonedero(idMonedero);
        if (m == null) {
            return "#" + idMonedero + " (eliminado)";
        }
        String texto = "#" + m.getId() + " " + m.getNombre();
        return m.getIdCliente() == idClienteQueMira ? texto : texto + " · " + nombreDe(m.getIdCliente());
    }

    private static double redondear(double valor) {
        return ReglasNegocio.redondear(valor);
    }
}
