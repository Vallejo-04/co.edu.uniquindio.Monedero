package com.Project.projectApi.List;

import com.Project.projectApi.Nodes.Node;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CircularList<T> implements Iterable<T> {

    private Node<T> tail;
    private int size;
    private final int capacidad;

    public CircularList() {
        this(0);
    }

    public CircularList(int capacidad) {
        if (capacidad < 0) {
            throw new IllegalArgumentException("La capacidad no puede ser negativa");
        }
        this.capacidad = capacidad;
        this.tail = null;
        this.size = 0;
    }

    public T add(T data) {
        T descartado = null;
        if (estaLlena()) {
            descartado = removeFirst();
        }

        Node<T> newNode = new Node<>(data);

        if (tail == null) {
            newNode.setNext(newNode);
        } else {
            newNode.setNext(tail.getNext());
            tail.setNext(newNode);
        }

        tail = newNode;
        size++;
        return descartado;
    }

    public T removeFirst() {
        if (isEmpty()) {
            throw new RuntimeException("La lista circular está vacía");
        }

        Node<T> head = tail.getNext();

        if (head == tail) {
            tail = null;
        } else {
            tail.setNext(head.getNext());
        }

        size--;
        return head.getData();
    }

    public T getFirst() {
        if (isEmpty()) {
            throw new RuntimeException("La lista circular está vacía");
        }
        return tail.getNext().getData();
    }

    public T getLast() {
        if (isEmpty()) {
            throw new RuntimeException("La lista circular está vacía");
        }
        return tail.getData();
    }

    public void rotate() {
        if (tail != null) {
            tail = tail.getNext();
        }
    }

    public boolean estaLlena() {
        return capacidad > 0 && size >= capacidad;
    }

    public int capacidad() {
        return capacidad;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return tail == null;
    }

    public void clear() {
        tail = null;
        size = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node<T> current = tail == null ? null : tail.getNext();
            private int restantes = size;

            @Override
            public boolean hasNext() {
                return restantes > 0;
            }

            @Override
            public T next() {
                if (restantes == 0) {
                    throw new NoSuchElementException();
                }
                T data = current.getData();
                current = current.getNext();
                restantes--;
                return data;
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        int i = 0;
        for (T dato : this) {
            sb.append(dato);
            if (++i < size) {
                sb.append(" -> ");
            }
        }
        return sb.append(size > 0 ? " -> ...)" : ")").toString();
    }
}
