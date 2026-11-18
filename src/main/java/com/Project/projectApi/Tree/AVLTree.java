package com.Project.projectApi.Tree;

import com.Project.projectApi.Nodes.NodeTree;

public class AVLTree<T extends Comparable<T>> extends Tree<T> {

    @Override
    protected NodeTree<T> reequilibrar(NodeTree<T> node) {
        actualizarAltura(node);
        int balance = factorDeBalance(node);
        if (balance > 1) {
            if (factorDeBalance(node.getLeft()) < 0) {
                node.setLeft(rotarIzquierda(node.getLeft()));
            }
            return rotarDerecha(node);
        }
        if (balance < -1) {
            if (factorDeBalance(node.getRight()) > 0) {
                node.setRight(rotarDerecha(node.getRight()));
            }
            return rotarIzquierda(node);
        }
        return node;
    }

    private int factorDeBalance(NodeTree<T> node) {
        return node == null ? 0 : altura(node.getLeft()) - altura(node.getRight());
    }

    private NodeTree<T> rotarDerecha(NodeTree<T> y) {
        NodeTree<T> x = y.getLeft();
        NodeTree<T> b = x.getRight();
        x.setRight(y);
        y.setLeft(b);
        actualizarAltura(y);
        actualizarAltura(x);
        return x;
    }

    private NodeTree<T> rotarIzquierda(NodeTree<T> x) {
        NodeTree<T> y = x.getRight();
        NodeTree<T> b = y.getLeft();
        y.setLeft(x);
        x.setRight(b);
        actualizarAltura(x);
        actualizarAltura(y);
        return y;
    }

    public boolean estaBalanceado() {
        return alturaSiEstaBalanceado(getRoot()) >= 0;
    }

    private int alturaSiEstaBalanceado(NodeTree<T> node) {
        if (node == null) {
            return 0;
        }
        int izquierda = alturaSiEstaBalanceado(node.getLeft());
        int derecha = alturaSiEstaBalanceado(node.getRight());
        if (izquierda < 0 || derecha < 0 || Math.abs(izquierda - derecha) > 1) {
            return -1;
        }
        int alturaReal = 1 + Math.max(izquierda, derecha);
        return alturaReal == node.getHeight() ? alturaReal : -1;
    }
}
