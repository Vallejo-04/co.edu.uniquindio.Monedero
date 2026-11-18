package com.Project.projectApi.Graph;

import com.Project.projectApi.Hash.HashTable;
import com.Project.projectApi.List.LinkedList;
import com.Project.projectApi.Nodes.NodeEdge;
import com.Project.projectApi.Nodes.NodeVertex;
import com.Project.projectApi.Queue.Queue;

public class Graph<T> {

    private NodeVertex<T> firstVertex;
    private NodeVertex<T> lastVertex;
    private final HashTable<T, NodeVertex<T>> indice;
    private int vertexCount;
    private int edgeCount;

    public Graph() {
        indice = new HashTable<>();
    }

    public boolean addVertex(T data) {
        if (indice.containsKey(data)) {
            return false;
        }

        NodeVertex<T> vertice = new NodeVertex<>(data);

        if (firstVertex == null) {
            firstVertex = vertice;
        } else {
            lastVertex.setNext(vertice);
        }
        lastVertex = vertice;

        indice.put(data, vertice);
        vertexCount++;
        return true;
    }

    public void addEdge(T origen, T destino, double peso) {
        addVertex(origen);
        addVertex(destino);

        NodeVertex<T> desde = indice.get(origen);
        NodeVertex<T> hasta = indice.get(destino);

        NodeEdge<T> existente = buscarArista(desde, hasta);
        if (existente != null) {
            existente.acumular(peso);
            return;
        }

        NodeEdge<T> nueva = new NodeEdge<>(hasta, peso);
        nueva.setNext(desde.getFirstEdge());
        desde.setFirstEdge(nueva);
        edgeCount++;
    }

    private NodeEdge<T> buscarArista(NodeVertex<T> desde, NodeVertex<T> hasta) {
        for (NodeEdge<T> e = desde.getFirstEdge(); e != null; e = e.getNext()) {
            if (e.getDestino() == hasta) {
                return e;
            }
        }
        return null;
    }

    public boolean containsVertex(T data) {
        return indice.containsKey(data);
    }

    public boolean containsEdge(T origen, T destino) {
        NodeVertex<T> desde = indice.get(origen);
        NodeVertex<T> hasta = indice.get(destino);
        return desde != null && hasta != null && buscarArista(desde, hasta) != null;
    }

    public int vertexCount() {
        return vertexCount;
    }

    public int edgeCount() {
        return edgeCount;
    }

    public boolean isEmpty() {
        return vertexCount == 0;
    }

    public LinkedList<T> vertices() {
        LinkedList<T> resultado = new LinkedList<>();
        for (NodeVertex<T> v = firstVertex; v != null; v = v.getNext()) {
            resultado.add(v.getData());
        }
        return resultado;
    }

    public LinkedList<Arista<T>> edgesFrom(T origen) {
        LinkedList<Arista<T>> resultado = new LinkedList<>();
        NodeVertex<T> desde = indice.get(origen);

        if (desde != null) {
            for (NodeEdge<T> e = desde.getFirstEdge(); e != null; e = e.getNext()) {
                resultado.add(new Arista<>(origen, e.getDestino().getData(), e.getPeso(), e.getCantidad()));
            }
        }
        return resultado;
    }

    public LinkedList<Arista<T>> edges() {
        LinkedList<Arista<T>> resultado = new LinkedList<>();
        for (NodeVertex<T> v = firstVertex; v != null; v = v.getNext()) {
            for (NodeEdge<T> e = v.getFirstEdge(); e != null; e = e.getNext()) {
                resultado.add(new Arista<>(v.getData(), e.getDestino().getData(), e.getPeso(), e.getCantidad()));
            }
        }
        return resultado;
    }

    public double pesoSalida(T data) {
        double total = 0;
        NodeVertex<T> v = indice.get(data);
        if (v != null) {
            for (NodeEdge<T> e = v.getFirstEdge(); e != null; e = e.getNext()) {
                total += e.getPeso();
            }
        }
        return total;
    }

    public double pesoEntrada(T data) {
        double total = 0;
        NodeVertex<T> objetivo = indice.get(data);
        if (objetivo == null) {
            return 0;
        }
        for (NodeVertex<T> v = firstVertex; v != null; v = v.getNext()) {
            for (NodeEdge<T> e = v.getFirstEdge(); e != null; e = e.getNext()) {
                if (e.getDestino() == objetivo) {
                    total += e.getPeso();
                }
            }
        }
        return total;
    }

    public LinkedList<T> alcanzablesDesde(T inicio) {
        LinkedList<T> resultado = new LinkedList<>();
        NodeVertex<T> origen = indice.get(inicio);
        if (origen == null) {
            return resultado;
        }

        HashTable<T, Boolean> visitados = new HashTable<>();
        Queue<NodeVertex<T>> pendientes = new Queue<>();

        visitados.put(inicio, true);
        pendientes.enqueue(origen);

        while (!pendientes.isEmpty()) {
            NodeVertex<T> actual = pendientes.dequeue();

            for (NodeEdge<T> e = actual.getFirstEdge(); e != null; e = e.getNext()) {
                T vecino = e.getDestino().getData();

                if (!visitados.containsKey(vecino)) {
                    visitados.put(vecino, true);
                    resultado.add(vecino);
                    pendientes.enqueue(e.getDestino());
                }
            }
        }
        return resultado;
    }

    public LinkedList<T> buscarCiclo() {
        HashTable<T, Integer> estado = new HashTable<>();
        HashTable<T, T> padre = new HashTable<>();
        LinkedList<T> ciclo = new LinkedList<>();

        for (NodeVertex<T> v = firstVertex; v != null; v = v.getNext()) {
            if (!estado.containsKey(v.getData()) && dfsCiclo(v, estado, padre, ciclo)) {
                break;
            }
        }
        return ciclo;
    }

    private boolean dfsCiclo(NodeVertex<T> actual, HashTable<T, Integer> estado,
                             HashTable<T, T> padre, LinkedList<T> ciclo) {
        T dato = actual.getData();
        estado.put(dato, 1);

        for (NodeEdge<T> e = actual.getFirstEdge(); e != null; e = e.getNext()) {
            T vecino = e.getDestino().getData();
            Integer estadoVecino = estado.get(vecino);

            if (estadoVecino != null && estadoVecino == 1) {
                for (T paso = dato; ; paso = padre.get(paso)) {
                    ciclo.addFirst(paso);
                    if (paso.equals(vecino)) {
                        break;
                    }
                }
                return true;
            }

            if (estadoVecino == null) {
                padre.put(vecino, dato);
                if (dfsCiclo(e.getDestino(), estado, padre, ciclo)) {
                    return true;
                }
            }
        }

        estado.put(dato, 2);
        return false;
    }
}
