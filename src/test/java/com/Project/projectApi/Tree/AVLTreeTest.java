package com.Project.projectApi.Tree;

import com.Project.projectApi.List.LinkedList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AVLTreeTest {

    @Test
    void conDatosOrdenadosElBstSeEstiraYElAvlNo() {
        Tree<Integer> bst = new Tree<>();
        AVLTree<Integer> avl = new AVLTree<>();

        for (int i = 1; i <= 1000; i++) {
            bst.insert(i);
            avl.insert(i);
        }

        assertEquals(1000, bst.height());
        assertTrue(avl.height() <= 15);
        assertTrue(avl.estaBalanceado());
        assertTrue(avl.esArbolDeBusquedaValido());
    }

    @Test
    void insertarEliminarYBuscar() {
        AVLTree<Integer> avl = new AVLTree<>();
        for (int v : new int[]{50, 20, 70, 10, 30, 60, 80, 25, 35}) {
            avl.insert(v);
        }

        assertFalse(avl.insert(30));
        assertEquals(9, avl.size());

        assertTrue(avl.remove(20));
        assertTrue(avl.remove(10));
        assertFalse(avl.remove(999));
        assertEquals(7, avl.size());
        assertFalse(avl.contains(20));
        assertTrue(avl.contains(25));
        assertTrue(avl.estaBalanceado());
        assertEquals("[25, 30, 35, 50, 60, 70, 80]", avl.inOrder().toString());
    }

    @Test
    void eliminarMuchosSigueBalanceado() {
        AVLTree<Integer> avl = new AVLTree<>();
        for (int i = 0; i < 500; i++) {
            avl.insert(i);
        }
        for (int i = 0; i < 500; i += 2) {
            avl.remove(i);
        }
        assertEquals(250, avl.size());
        assertTrue(avl.estaBalanceado());
        assertTrue(avl.esArbolDeBusquedaValido());
        assertEquals(1, avl.min());
        assertEquals(499, avl.max());
    }

    @Test
    void mayoresYRango() {
        AVLTree<Integer> avl = new AVLTree<>();
        for (int v : new int[]{5, 1, 9, 3, 7, 2, 8}) {
            avl.insert(v);
        }

        LinkedList<Integer> top3 = avl.mayores(3);
        assertEquals("[9, 8, 7]", top3.toString());

        assertEquals("[3, 5, 7]", avl.rango(3, 7).toString());
        assertEquals("[]", avl.rango(10, 20).toString());
    }
}
