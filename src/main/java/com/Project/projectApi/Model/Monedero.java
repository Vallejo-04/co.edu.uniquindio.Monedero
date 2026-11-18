package com.Project.projectApi.Model;

public class Monedero {
    private int id;
    private String nombre;
    private double saldo;
    private int idCliente;
    public Monedero(int id, String nombre, double saldo) {
        this.id = id;
        this.nombre = nombre;
        this.saldo = saldo;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public double getSaldo() {
        return saldo;
    }
    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }
    public void depositar(double monto) {
        if (monto > 0) {
            saldo = ReglasNegocio.redondear(saldo + monto);
        }
    }
    public boolean retirar(double monto) {

        if (monto > 0 && saldo >= monto) {
            saldo = ReglasNegocio.redondear(saldo - monto);
            return true;
        }

        return false;
    }
    @Override
    public String toString() {
        return "Monedero{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", saldo=" + saldo +
                '}';
    }
}
