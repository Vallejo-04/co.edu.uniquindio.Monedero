package com.Project.projectApi.Graph;

import com.Project.projectApi.List.LinkedList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GraphTest {

    @Test
    void aristasRepetidasSeAcumulan() {
        Graph<String> g = new Graph<>();
        g.addEdge("Juan", "María", 100);
        g.addEdge("Juan", "María", 50);
        g.addEdge("Juan", "Carlos", 30);

        assertEquals(3, g.vertexCount());
        assertEquals(2, g.edgeCount());
        assertEquals(180, g.pesoSalida("Juan"));
        assertEquals(150, g.pesoEntrada("María"));

        Arista<String> aMaria = null;
        for (Arista<String> a : g.edgesFrom("Juan")) {
            if (a.destino().equals("María")) {
                aMaria = a;
            }
        }
        assertNotNull(aMaria);
        assertEquals(2, aMaria.cantidad());
    }

    @Test
    void alcanzablesConBfs() {
        Graph<Integer> g = new Graph<>();
        g.addEdge(1, 2, 1);
        g.addEdge(2, 3, 1);
        g.addEdge(1, 4, 1);
        g.addVertex(5);

        LinkedList<Integer> desde1 = g.alcanzablesDesde(1);
        assertEquals(3, desde1.size());
        assertTrue(desde1.contains(3));
        assertFalse(desde1.contains(5));
        assertEquals(0, g.alcanzablesDesde(5).size());
    }

    @Test
    void detectaCiclos() {
        Graph<String> sinCiclo = new Graph<>();
        sinCiclo.addEdge("A", "B", 1);
        sinCiclo.addEdge("B", "C", 1);
        sinCiclo.addEdge("A", "C", 1);
        assertTrue(sinCiclo.buscarCiclo().isEmpty());

        Graph<String> conCiclo = new Graph<>();
        conCiclo.addEdge("A", "B", 1);
        conCiclo.addEdge("B", "C", 1);
        conCiclo.addEdge("C", "A", 1);
        conCiclo.addEdge("C", "D", 1);
        assertEquals("[A, B, C]", conCiclo.buscarCiclo().toString());
    }
}
