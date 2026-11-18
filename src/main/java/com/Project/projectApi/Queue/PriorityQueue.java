package com.Project.projectApi.Queue;

import com.Project.projectApi.Nodes.Node;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class PriorityQueue<T extends Comparable<T>> implements Iterable<T> {

    private Node<T> front;
    private int size;

    public PriorityQueue() {
        front = null;
        size = 0;
    }

    public void enqueue(T data) {
        Node<T> newNode = new Node<>(data);

        if (front == null || data.compareTo(front.getData()) < 0) {
            newNode.setNext(front);
            front = newNode;
            size++;
            return;
        }

        Node<T> current = front;
        while (current.getNext() != null && current.getNext().getData().compareTo(data) <= 0) {
            current = current.getNext();
        }

        newNode.setNext(current.getNext());
        current.setNext(newNode);
        size++;
    }

    public T dequeue() {
        if (isEmpty()) {
            throw new RuntimeException("La cola de prioridad está vacía");
        }

        T data = front.getData();
        front = front.getNext();
        size--;
        return data;
    }

    public T peek() {
        if (isEmpty()) {
            throw new RuntimeException("La cola de prioridad está vacía");
        }
        return front.getData();
    }

    public T removeFirst(Predicate<T> condicion) {
        Node<T> anterior = null;
        Node<T> current = front;

        while (current != null) {
            if (condicion.test(current.getData())) {
                if (anterior == null) {
                    front = current.getNext();
                } else {
                    anterior.setNext(current.getNext());
                }
                size--;
                return current.getData();
            }
            anterior = current;
            current = current.getNext();
        }

        return null;
    }

    public boolean isEmpty() {
        return front == null;
    }

    public int size() {
        return size;
    }

    public void clear() {
        front = null;
        size = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node<T> current = front;

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (Node<T> n = front; n != null; n = n.getNext()) {
            sb.append(n.getData());
            if (n.getNext() != null) {
                sb.append(" -> ");
            }
        }
        return sb.append("]").toString();
    }
}
