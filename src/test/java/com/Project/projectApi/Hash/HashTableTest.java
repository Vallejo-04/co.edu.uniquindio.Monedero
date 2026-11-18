package com.Project.projectApi.Hash;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HashTableTest {

    @Test
    void putYGet() {
        HashTable<Integer, String> tabla = new HashTable<>();
        assertNull(tabla.put(1, "uno"));
        assertNull(tabla.put(2, "dos"));

        assertEquals("uno", tabla.get(1));
        assertEquals("dos", tabla.get(2));
        assertNull(tabla.get(3));
        assertEquals(2, tabla.size());
    }

    @Test
    void putReemplazaSinCambiarTamano() {
        HashTable<Integer, String> tabla = new HashTable<>();
        tabla.put(1, "uno");
        assertEquals("uno", tabla.put(1, "UNO"));
        assertEquals("UNO", tabla.get(1));
        assertEquals(1, tabla.size());
    }

    @Test
    void colisionesEnLaMismaCubeta() {
        HashTable<Integer, String> tabla = new HashTable<>(4);
        tabla.put(1, "a");
        tabla.put(5, "b");
        tabla.put(9, "c");

        assertEquals("a", tabla.get(1));
        assertEquals("b", tabla.get(5));
        assertEquals("c", tabla.get(9));

        assertEquals("b", tabla.remove(5));
        assertNull(tabla.get(5));
        assertEquals("a", tabla.get(1));
        assertEquals("c", tabla.get(9));
        assertEquals(2, tabla.size());
    }

    @Test
    void removeInexistente() {
        HashTable<Integer, String> tabla = new HashTable<>();
        assertNull(tabla.remove(99));
        assertEquals(0, tabla.size());
    }

    @Test
    void rehashConservaTodosLosDatos() {
        HashTable<Integer, Integer> tabla = new HashTable<>(4);

        for (int i = 0; i < 1000; i++) {
            tabla.put(i, i * 10);
        }

        assertTrue(tabla.capacidad() > 4);
        assertEquals(1000, tabla.size());

        for (int i = 0; i < 1000; i++) {
            assertEquals(i * 10, tabla.get(i));
        }
    }

    @Test
    void clavesNegativasYStrings() {
        HashTable<Integer, String> numeros = new HashTable<>();
        numeros.put(-7, "negativo");
        assertEquals("negativo", numeros.get(-7));

        HashTable<String, Integer> textos = new HashTable<>();
        textos.put("Aa", 1);
        textos.put("BB", 2);
        assertEquals(1, textos.get("Aa"));
        assertEquals(2, textos.get("BB"));
    }
}
