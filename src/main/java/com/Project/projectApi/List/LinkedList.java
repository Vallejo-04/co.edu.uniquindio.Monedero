package com.Project.projectApi.List;

import com.Project.projectApi.Nodes.Node;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class LinkedList<T> implements Iterable<T> {

    private Node<T> head;
    private int size;

    public LinkedList() {
        this.head = null;
        this.size = 0;
    }

    public boolean isEmpty() {
        return head == null;
    }

    public int size() {
        return size;
    }

    public void add(T data) {

        Node<T> newNode = new Node<>(data);

        if (isEmpty()) {
            head = newNode;
        } else {

            Node<T> current = head;

            while (current.getNext() != null) {
                current = current.getNext();
            }

            current.setNext(newNode);
        }

        size++;
    }

    public void addFirst(T data) {

        Node<T> newNode = new Node<>(data);

        newNode.setNext(head);
        head = newNode;

        size++;
    }

    public T get(int index) {

        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Índice fuera de rango");
        }

        Node<T> current = head;

        for (int i = 0; i < index; i++) {
            current = current.getNext();
        }

        return current.getData();
    }

    public boolean contains(T data) {

        Node<T> current = head;

        while (current != null) {

            if (current.getData().equals(data)) {
                return true;
            }

            current = current.getNext();
        }

        return false;
    }

    public boolean remove(T data) {

        if (isEmpty()) {
            return false;
        }

        if (head.getData().equals(data)) {
            head = head.getNext();
            size--;
            return true;
        }

        Node<T> current = head;

        while (current.getNext() != null) {

            if (current.getNext().getData().equals(data)) {

                current.setNext(current.getNext().getNext());

                size--;
                return true;
            }

            current = current.getNext();
        }

        return false;
    }

    public T find(Predicate<T> condicion) {

        Node<T> current = head;

        while (current != null) {

            if (condicion.test(current.getData())) {
                return current.getData();
            }

            current = current.getNext();
        }

        return null;
    }

    @Override
    public Iterator<T> iterator() {

        return new Iterator<T>() {

            private Node<T> current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {

                if (current == null) {
                    throw new NoSuchElementException();
                }

                T data = current.getData();
                current = current.getNext();
                return data;
            }
        };
    }

    public void clear() {

        head = null;
        size = 0;
    }

    public Node<T> getHead() {
        return head;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        Node<T> current = head;

        sb.append("[");

        while (current != null) {

            sb.append(current.getData());

            if (current.getNext() != null) {
                sb.append(", ");
            }

            current = current.getNext();
        }

        sb.append("]");

        return sb.toString();
    }
}
