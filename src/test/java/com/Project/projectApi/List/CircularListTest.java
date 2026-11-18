package com.Project.projectApi.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CircularListTest {

    @Test
    void funcionaComoCola() {
        CircularList<String> lista = new CircularList<>();
        lista.add("a");
        lista.add("b");
        lista.add("c");

        assertEquals("a", lista.getFirst());
        assertEquals("c", lista.getLast());
        assertEquals("a", lista.removeFirst());
        assertEquals("b", lista.removeFirst());
        assertEquals("c", lista.removeFirst());
        assertTrue(lista.isEmpty());
    }

    @Test
    void conCapacidadDescartaElMasViejo() {
        CircularList<Integer> lista = new CircularList<>(3);
        lista.add(1);
        lista.add(2);
        lista.add(3);
        assertEquals(1, lista.add(4));

        StringBuilder sb = new StringBuilder();
        for (int x : lista) {
            sb.append(x);
        }
        assertEquals("234", sb.toString());
        assertEquals(3, lista.size());
    }

    @Test
    void rotarYRecorrerUnaVuelta() {
        CircularList<Integer> lista = new CircularList<>();
        lista.add(1);
        lista.add(2);
        lista.add(3);
        lista.rotate();

        StringBuilder sb = new StringBuilder();
        for (int x : lista) {
            sb.append(x);
        }
        assertEquals("231", sb.toString());
    }

    @Test
    void unSoloElemento() {
        CircularList<String> lista = new CircularList<>();
        lista.add("solo");
        lista.rotate();
        assertEquals("solo", lista.getFirst());
        assertEquals("solo", lista.removeFirst());
        assertThrows(RuntimeException.class, lista::removeFirst);
    }
}
