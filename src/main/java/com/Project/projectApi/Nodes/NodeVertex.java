package com.Project.projectApi.Nodes;

public class NodeVertex<T> {

    private final T data;
    private NodeVertex<T> next;
    private NodeEdge<T> firstEdge;

    public NodeVertex(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public NodeVertex<T> getNext() {
        return next;
    }

    public void setNext(NodeVertex<T> next) {
        this.next = next;
    }

    public NodeEdge<T> getFirstEdge() {
        return firstEdge;
    }

    public void setFirstEdge(NodeEdge<T> firstEdge) {
        this.firstEdge = firstEdge;
    }
}
