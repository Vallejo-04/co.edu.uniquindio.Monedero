package com.Project.projectApi.Nodes;

public class NodeTree<T extends Comparable<T>> {

    private T data;
    private NodeTree<T> left;
    private NodeTree<T> right;
    private int height;

    public NodeTree(T data) {
        this.data = data;
        this.left = null;
        this.right = null;
        this.height = 1;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public NodeTree<T> getLeft() {
        return left;
    }

    public void setLeft(NodeTree<T> left) {
        this.left = left;
    }

    public NodeTree<T> getRight() {
        return right;
    }

    public void setRight(NodeTree<T> right) {
        this.right = right;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
