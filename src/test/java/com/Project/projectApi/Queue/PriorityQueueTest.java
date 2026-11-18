package com.Project.projectApi.Queue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PriorityQueueTest {

    record Tarea(int prioridad, String nombre) implements Comparable<Tarea> {
        public int compareTo(Tarea otra) {
            return Integer.compare(prioridad, otra.prioridad);
        }
    }

    @Test
    void saleSiempreElMenor() {
        PriorityQueue<Integer> cola = new PriorityQueue<>();
        for (int v : new int[]{5, 1, 4, 2, 3}) {
            cola.enqueue(v);
        }

        assertEquals(1, cola.peek());
        StringBuilder orden = new StringBuilder();
        while (!cola.isEmpty()) {
            orden.append(cola.dequeue());
        }
        assertEquals("12345", orden.toString());
    }

    @Test
    void empatesRespetanElOrdenDeLlegada() {
        PriorityQueue<Tarea> cola = new PriorityQueue<>();
        cola.enqueue(new Tarea(2, "b1"));
        cola.enqueue(new Tarea(1, "a"));
        cola.enqueue(new Tarea(2, "b2"));
        cola.enqueue(new Tarea(2, "b3"));

        assertEquals("a", cola.dequeue().nombre());
        assertEquals("b1", cola.dequeue().nombre());
        assertEquals("b2", cola.dequeue().nombre());
        assertEquals("b3", cola.dequeue().nombre());
    }

    @Test
    void quitarPorCondicion() {
        PriorityQueue<Integer> cola = new PriorityQueue<>();
        cola.enqueue(3);
        cola.enqueue(1);
        cola.enqueue(2);

        assertEquals(2, cola.removeFirst(x -> x == 2));
        assertNull(cola.removeFirst(x -> x == 99));
        assertEquals(2, cola.size());
        assertEquals("[1 -> 3]", cola.toString());
    }

    @Test
    void vaciaLanzaExcepcion() {
        assertThrows(RuntimeException.class, () -> new PriorityQueue<Integer>().dequeue());
    }
}
