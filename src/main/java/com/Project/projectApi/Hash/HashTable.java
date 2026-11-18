package com.Project.projectApi.Hash;

import com.Project.projectApi.List.LinkedList;
import com.Project.projectApi.Nodes.NodeHash;

public class HashTable<K, V> {

    private static final int CAPACIDAD_INICIAL = 16;
    private static final double FACTOR_CARGA_MAX = 0.75;

    private NodeHash<K, V>[] cubetas;
    private int size;

    public HashTable() {
        this(CAPACIDAD_INICIAL);
    }

    @SuppressWarnings("unchecked")
    public HashTable(int capacidad) {

        if (capacidad < 1) {
            throw new IllegalArgumentException("La capacidad debe ser mayor que 0");
        }

        cubetas = (NodeHash<K, V>[]) new NodeHash[capacidad];
        size = 0;
    }

    public V put(K key, V value) {

        validarClave(key);

        int indice = indice(key, cubetas.length);
        NodeHash<K, V> current = cubetas[indice];

        while (current != null) {

            if (current.getKey().equals(key)) {
                V anterior = current.getValue();
                current.setValue(value);
                return anterior;
            }

            current = current.getNext();
        }

        NodeHash<K, V> newNode = new NodeHash<>(key, value);
        newNode.setNext(cubetas[indice]);
        cubetas[indice] = newNode;
        size++;

        if ((double) size / cubetas.length > FACTOR_CARGA_MAX) {
            rehash();
        }

        return null;
    }

    public V get(K key) {

        validarClave(key);

        NodeHash<K, V> current = cubetas[indice(key, cubetas.length)];

        while (current != null) {

            if (current.getKey().equals(key)) {
                return current.getValue();
            }

            current = current.getNext();
        }

        return null;
    }

    public boolean containsKey(K key) {

        validarClave(key);

        NodeHash<K, V> current = cubetas[indice(key, cubetas.length)];

        while (current != null) {

            if (current.getKey().equals(key)) {
                return true;
            }

            current = current.getNext();
        }

        return false;
    }

    public V remove(K key) {

        validarClave(key);

        int indice = indice(key, cubetas.length);
        NodeHash<K, V> current = cubetas[indice];
        NodeHash<K, V> previous = null;

        while (current != null) {

            if (current.getKey().equals(key)) {

                if (previous == null) {
                    cubetas[indice] = current.getNext();
                } else {
                    previous.setNext(current.getNext());
                }

                size--;
                return current.getValue();
            }

            previous = current;
            current = current.getNext();
        }

        return null;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int capacidad() {
        return cubetas.length;
    }

    public LinkedList<K> keys() {
        LinkedList<K> resultado = new LinkedList<>();
        for (NodeHash<K, V> cubeta : cubetas) {
            for (NodeHash<K, V> n = cubeta; n != null; n = n.getNext()) {
                resultado.add(n.getKey());
            }
        }
        return resultado;
    }

    public LinkedList<V> values() {
        LinkedList<V> resultado = new LinkedList<>();
        for (NodeHash<K, V> cubeta : cubetas) {
            for (NodeHash<K, V> n = cubeta; n != null; n = n.getNext()) {
                resultado.add(n.getValue());
            }
        }
        return resultado;
    }

    public int cadenaMasLarga() {
        int maximo = 0;
        for (NodeHash<K, V> cubeta : cubetas) {
            int largo = 0;
            for (NodeHash<K, V> n = cubeta; n != null; n = n.getNext()) {
                largo++;
            }
            maximo = Math.max(maximo, largo);
        }
        return maximo;
    }

    @SuppressWarnings("unchecked")
    public void clear() {
        cubetas = (NodeHash<K, V>[]) new NodeHash[CAPACIDAD_INICIAL];
        size = 0;
    }

    @SuppressWarnings("unchecked")
    private void rehash() {

        NodeHash<K, V>[] nuevas = (NodeHash<K, V>[]) new NodeHash[cubetas.length * 2];

        for (int i = 0; i < cubetas.length; i++) {

            NodeHash<K, V> current = cubetas[i];

            while (current != null) {

                NodeHash<K, V> siguiente = current.getNext();
                int nuevoIndice = indice(current.getKey(), nuevas.length);

                current.setNext(nuevas[nuevoIndice]);
                nuevas[nuevoIndice] = current;

                current = siguiente;
            }
        }

        cubetas = nuevas;
    }

    private int indice(K key, int capacidad) {
        return (key.hashCode() & 0x7fffffff) % capacidad;
    }

    private void validarClave(K key) {
        if (key == null) {
            throw new IllegalArgumentException("La clave no puede ser null");
        }
    }
}
