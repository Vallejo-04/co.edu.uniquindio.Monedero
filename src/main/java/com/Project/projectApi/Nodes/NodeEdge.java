package com.Project.projectApi.Nodes;

public class NodeEdge<T> {

    private final NodeVertex<T> destino;
    private double peso;
    private int cantidad;
    private NodeEdge<T> next;

    public NodeEdge(NodeVertex<T> destino, double peso) {
        this.destino = destino;
        this.peso = peso;
        this.cantidad = 1;
    }

    public void acumular(double pesoExtra) {
        peso += pesoExtra;
        cantidad++;
    }

    public NodeVertex<T> getDestino() {
        return destino;
    }

    public double getPeso() {
        return peso;
    }

    public int getCantidad() {
        return cantidad;
    }

    public NodeEdge<T> getNext() {
        return next;
    }

    public void setNext(NodeEdge<T> next) {
        this.next = next;
    }
}
