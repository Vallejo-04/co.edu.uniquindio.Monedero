package com.Project.projectApi.Tree;

import com.Project.projectApi.List.LinkedList;
import com.Project.projectApi.Nodes.NodeTree;

import java.util.Iterator;

public class Tree<T extends Comparable<T>> implements Iterable<T> {

    private NodeTree<T> root;
    private int size;

    private boolean huboCambio;

    public Tree() {
        root = null;
        size = 0;
    }

    public NodeTree<T> getRoot() {
        return root;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public int height() {
        return altura(root);
    }

    public boolean insert(T data) {
        huboCambio = false;
        root = insertRecursive(root, data);

        if (huboCambio) {
            size++;
        }
        return huboCambio;
    }

    private NodeTree<T> insertRecursive(NodeTree<T> current, T data) {
        if (current == null) {
            huboCambio = true;
            return new NodeTree<>(data);
        }

        int comparacion = data.compareTo(current.getData());

        if (comparacion < 0) {
            current.setLeft(insertRecursive(current.getLeft(), data));
        } else if (comparacion > 0) {
            current.setRight(insertRecursive(current.getRight(), data));
        } else {
            return current;
        }

        return reequilibrar(current);
    }

    public boolean remove(T data) {
        huboCambio = false;
        root = removeRecursive(root, data);

        if (huboCambio) {
            size--;
        }
        return huboCambio;
    }

    private NodeTree<T> removeRecursive(NodeTree<T> current, T data) {
        if (current == null) {
            return null;
        }

        int comparacion = data.compareTo(current.getData());

        if (comparacion < 0) {
            current.setLeft(removeRecursive(current.getLeft(), data));
        } else if (comparacion > 0) {
            current.setRight(removeRecursive(current.getRight(), data));
        } else {
            huboCambio = true;

            if (current.getLeft() == null) {
                return current.getRight();
            }
            if (current.getRight() == null) {
                return current.getLeft();
            }

            T sucesor = minimo(current.getRight()).getData();
            current.setData(sucesor);
            current.setRight(removeRecursive(current.getRight(), sucesor));
        }

        return reequilibrar(current);
    }

    protected NodeTree<T> reequilibrar(NodeTree<T> node) {
        actualizarAltura(node);
        return node;
    }

    protected void actualizarAltura(NodeTree<T> node) {
        node.setHeight(1 + Math.max(altura(node.getLeft()), altura(node.getRight())));
    }

    protected int altura(NodeTree<T> node) {
        return node == null ? 0 : node.getHeight();
    }

    public boolean contains(T data) {
        return search(data) != null;
    }

    public T search(T data) {
        NodeTree<T> current = root;

        while (current != null) {
            int comparacion = data.compareTo(current.getData());

            if (comparacion == 0) {
                return current.getData();
            }
            current = comparacion < 0 ? current.getLeft() : current.getRight();
        }

        return null;
    }

    public T min() {
        return root == null ? null : minimo(root).getData();
    }

    public T max() {
        NodeTree<T> current = root;
        if (current == null) {
            return null;
        }
        while (current.getRight() != null) {
            current = current.getRight();
        }
        return current.getData();
    }

    private NodeTree<T> minimo(NodeTree<T> node) {
        while (node.getLeft() != null) {
            node = node.getLeft();
        }
        return node;
    }

    public LinkedList<T> inOrder() {
        LinkedList<T> resultado = new LinkedList<>();
        inOrder(root, resultado);
        return resultado;
    }

    private void inOrder(NodeTree<T> node, LinkedList<T> resultado) {
        if (node == null) {
            return;
        }
        inOrder(node.getLeft(), resultado);
        resultado.add(node.getData());
        inOrder(node.getRight(), resultado);
    }

    public LinkedList<T> mayores(int cantidad) {
        LinkedList<T> resultado = new LinkedList<>();
        mayores(root, cantidad, resultado);
        return resultado;
    }

    private void mayores(NodeTree<T> node, int cantidad, LinkedList<T> resultado) {
        if (node == null || resultado.size() >= cantidad) {
            return;
        }
        mayores(node.getRight(), cantidad, resultado);

        if (resultado.size() < cantidad) {
            resultado.add(node.getData());
        }
        mayores(node.getLeft(), cantidad, resultado);
    }

    public LinkedList<T> rango(T desde, T hasta) {
        LinkedList<T> resultado = new LinkedList<>();
        rango(root, desde, hasta, resultado);
        return resultado;
    }

    private void rango(NodeTree<T> node, T desde, T hasta, LinkedList<T> resultado) {
        if (node == null) {
            return;
        }

        T dato = node.getData();

        if (desde.compareTo(dato) < 0) {
            rango(node.getLeft(), desde, hasta, resultado);
        }
        if (desde.compareTo(dato) <= 0 && hasta.compareTo(dato) >= 0) {
            resultado.add(dato);
        }
        if (hasta.compareTo(dato) > 0) {
            rango(node.getRight(), desde, hasta, resultado);
        }
    }

    public boolean esArbolDeBusquedaValido() {
        return esValido(root, null, null);
    }

    private boolean esValido(NodeTree<T> node, T minimo, T maximo) {
        if (node == null) {
            return true;
        }

        T dato = node.getData();

        if (minimo != null && dato.compareTo(minimo) <= 0) {
            return false;
        }
        if (maximo != null && dato.compareTo(maximo) >= 0) {
            return false;
        }

        return esValido(node.getLeft(), minimo, dato) && esValido(node.getRight(), dato, maximo);
    }

    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return inOrder().iterator();
    }

    @Override
    public String toString() {
        return inOrder().toString();
    }
}
