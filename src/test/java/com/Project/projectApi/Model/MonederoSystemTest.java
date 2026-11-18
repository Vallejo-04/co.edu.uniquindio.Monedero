package com.Project.projectApi.Model;

import com.Project.projectApi.Enum.CategoriaGasto;
import com.Project.projectApi.Enum.EstadoTransaccion;
import com.Project.projectApi.Enum.RangoCliente;
import com.Project.projectApi.Excepcion.OperacionInvalidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MonederoSystemTest {

    private MonederoSystem sistema;
    private int ahorrosAna;
    private int gastosAna;
    private int principalLuis;

    @BeforeEach
    void preparar() {
        sistema = new MonederoSystem();
        sistema.agregarCliente(new Cliente(1, "Ana", "ana@x.com"));
        sistema.agregarCliente(new Cliente(2, "Luis", "luis@x.com"));
        ahorrosAna = sistema.crearMonedero(1, "Ahorros").getId();
        gastosAna = sistema.crearMonedero(1, "Gastos").getId();
        principalLuis = sistema.crearMonedero(2, "Principal").getId();
    }

    private Cliente ana() {
        return sistema.buscarCliente(1);
    }

    @Test
    void depositoSumaSaldoYUnPuntoPorCada100() {
        Transaccion t = sistema.depositar(1, ahorrosAna, 1_050, "Pago");

        assertEquals(1_050, sistema.buscarMonedero(ahorrosAna).getSaldo());
        assertEquals(10, t.getPuntosGenerados());
        assertEquals(10, ana().getPuntos());
        assertEquals(10, ana().getPuntosDisponibles());
        assertEquals(1, ana().getHistorialTransacciones().size());
    }

    @Test
    void retiroCobraComisionYDaDosPuntosPorCada100() {
        sistema.depositar(1, ahorrosAna, 10_000, "");
        Transaccion t = sistema.retirar(1, ahorrosAna, 2_000, CategoriaGasto.ALIMENTACION, "");

        assertEquals(10.0, t.getComision());
        assertEquals(7_990, sistema.buscarMonedero(ahorrosAna).getSaldo());
        assertEquals(40, t.getPuntosGenerados());
        assertEquals(100 + 40, ana().getPuntos());
    }

    @Test
    void retiroSinSaldoSuficienteNoCambiaNada() {
        sistema.depositar(1, ahorrosAna, 1_000, "");

        assertThrows(OperacionInvalidaException.class,
                () -> sistema.retirar(1, ahorrosAna, 1_000, CategoriaGasto.OTROS, ""));

        assertEquals(1_000, sistema.buscarMonedero(ahorrosAna).getSaldo());
        assertEquals(1, ana().getHistorialTransacciones().size());
        assertEquals(10, ana().getPuntos());
    }

    @Test
    void transferenciaAOtroClienteCobraComisionYDaTresPuntos() {
        sistema.depositar(1, ahorrosAna, 10_000, "");
        Transaccion t = sistema.transferir(1, ahorrosAna, principalLuis, 1_000, CategoriaGasto.OTROS, "");

        assertEquals(10.0, t.getComision());
        assertEquals(8_990, sistema.buscarMonedero(ahorrosAna).getSaldo());
        assertEquals(1_000, sistema.buscarMonedero(principalLuis).getSaldo());
        assertEquals(30, t.getPuntosGenerados());
        assertEquals(1, sistema.buscarCliente(2).getHistorialTransacciones().size());
        assertEquals(0, sistema.buscarCliente(2).getPuntos());
    }

    @Test
    void transferenciaEntreMonederosPropiosNoCobraNiDaPuntos() {
        sistema.depositar(1, ahorrosAna, 1_000, "");
        Transaccion t = sistema.transferir(1, ahorrosAna, gastosAna, 400, CategoriaGasto.AHORRO, "");

        assertTrue(t.esInterna());
        assertEquals(0, t.getComision());
        assertEquals(0, t.getPuntosGenerados());
        assertEquals(600, sistema.buscarMonedero(ahorrosAna).getSaldo());
        assertEquals(400, sistema.buscarMonedero(gastosAna).getSaldo());
    }

    @Test
    void noSePuedeOperarConMonederoAjeno() {
        assertThrows(OperacionInvalidaException.class,
                () -> sistema.depositar(1, principalLuis, 100, ""));
    }

    @Test
    void rangoSubeConLosPuntosYLaComisionBaja() {
        sistema.depositar(1, ahorrosAna, 110_000, "");
        assertEquals(RangoCliente.ORO, ana().getRango());

        Transaccion t = sistema.transferir(1, ahorrosAna, principalLuis, 1_000, CategoriaGasto.OTROS, "");
        assertEquals(7.5, t.getComision());
    }

    @Test
    void revertirRetiroDevuelveDineroYQuitaPuntos() {
        sistema.depositar(1, ahorrosAna, 10_000, "");
        sistema.retirar(1, ahorrosAna, 2_000, CategoriaGasto.OTROS, "");

        Transaccion revertida = sistema.revertirUltimaTransaccion(1);

        assertEquals(EstadoTransaccion.REVERTIDA, revertida.getEstado());
        assertEquals(10_000, sistema.buscarMonedero(ahorrosAna).getSaldo());
        assertEquals(100, ana().getPuntos());
        assertEquals(2, ana().getHistorialTransacciones().size());
    }

    @Test
    void revertirEsLifoYRecalculaElRango() {
        sistema.depositar(1, ahorrosAna, 30_000, "");
        sistema.depositar(1, ahorrosAna, 30_000, "");
        assertEquals(RangoCliente.PLATA, ana().getRango());

        sistema.revertirUltimaTransaccion(1);
        assertEquals(30_000, sistema.buscarMonedero(ahorrosAna).getSaldo());
        assertEquals(RangoCliente.BRONCE, ana().getRango());

        sistema.revertirUltimaTransaccion(1);
        assertEquals(0, sistema.buscarMonedero(ahorrosAna).getSaldo());
        assertEquals(0, ana().getPuntos());

        assertThrows(OperacionInvalidaException.class, () -> sistema.revertirUltimaTransaccion(1));
    }

    @Test
    void noSeRevierteTransferenciaSiElDestinoYaGastoElDinero() {
        sistema.depositar(1, ahorrosAna, 10_000, "");
        sistema.transferir(1, ahorrosAna, principalLuis, 5_000, CategoriaGasto.OTROS, "");
        sistema.retirar(2, principalLuis, 4_000, CategoriaGasto.OTROS, "");

        assertThrows(OperacionInvalidaException.class, () -> sistema.revertirUltimaTransaccion(1));

        assertEquals(4_950, sistema.buscarMonedero(ahorrosAna).getSaldo());
        assertEquals(EstadoTransaccion.ACTIVA, sistema.verUltimaReversible(1).getEstado());
    }

    @Test
    void eliminarClienteLiberaSusMonederos() {
        assertTrue(sistema.eliminarCliente(2));
        assertNull(sistema.buscarCliente(2));
        assertNull(sistema.buscarMonedero(principalLuis));
        assertThrows(OperacionInvalidaException.class,
                () -> sistema.transferir(1, ahorrosAna, principalLuis, 1, CategoriaGasto.OTROS, ""));
    }

    @Test
    void noSeEliminaMonederoConSaldo() {
        sistema.depositar(1, gastosAna, 100, "");
        assertThrows(OperacionInvalidaException.class, () -> sistema.eliminarMonedero(1, gastosAna));
        sistema.transferir(1, gastosAna, ahorrosAna, 100, CategoriaGasto.OTROS, "");
        assertTrue(sistema.eliminarMonedero(1, gastosAna));
    }
}
