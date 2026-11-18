package com.Project.projectApi.Model;

import com.Project.projectApi.Enum.*;
import com.Project.projectApi.Excepcion.OperacionInvalidaException;
import com.Project.projectApi.List.LinkedList;
import com.Project.projectApi.Nodes.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FuncionalidadesAvanzadasTest {

    private MonederoSystem sistema;
    private int ahorrosAna;
    private int gastosAna;
    private int principalLuis;
    private int principalEva;

    @BeforeEach
    void preparar() {
        sistema = new MonederoSystem();
        sistema.agregarCliente(new Cliente(1, "Ana", "ana@x.com"));
        sistema.agregarCliente(new Cliente(2, "Luis", "luis@x.com"));
        sistema.agregarCliente(new Cliente(3, "Eva", "eva@x.com"));
        ahorrosAna = sistema.crearMonedero(1, "Ahorros").getId();
        gastosAna = sistema.crearMonedero(1, "Gastos").getId();
        principalLuis = sistema.crearMonedero(2, "Principal").getId();
        principalEva = sistema.crearMonedero(3, "Principal").getId();
    }

    private Cliente ana() {
        return sistema.buscarCliente(1);
    }

    @Test
    void elRangoDaPuntosExtra() {
        sistema.depositar(1, ahorrosAna, 60_000, "");
        Transaccion t = sistema.depositar(1, ahorrosAna, 10_000, "");

        assertEquals(100, t.getPuntosBase());
        assertEquals(5, t.getPuntosExtra());
        assertEquals(705, ana().getPuntos());
    }

    @Test
    void rankingYConsultaPorRangoSalenDelArbol() {
        sistema.depositar(1, ahorrosAna, 200_000, "");
        sistema.depositar(2, principalLuis, 70_000, "");
        sistema.depositar(3, principalEva, 80_000, "");

        LinkedList<PuntajeCliente> top = sistema.ranking(2);
        assertEquals(1, top.get(0).idCliente());
        assertEquals(3, top.get(1).idCliente());

        LinkedList<PuntajeCliente> plata = sistema.clientesDelRango(RangoCliente.PLATA);
        assertEquals(2, plata.size());
        assertEquals(3, plata.get(0).idCliente());
        assertEquals(1, sistema.getGestorPuntos().posicionDe(1));
        assertTrue(sistema.getGestorPuntos().getArbolPuntos().estaBalanceado());
    }

    @Test
    void cambioDeRangoGeneraNotificacion() {
        sistema.depositar(1, ahorrosAna, 60_000, "");
        boolean avisado = false;
        for (Notificacion n : ana().getNotificaciones()) {
            avisado |= n.getTipo() == TipoNotificacion.CAMBIO_DE_RANGO;
        }
        assertTrue(avisado);
    }

    @Test
    void canjeDescuentaDisponiblesPeroNoBajaElRango() {
        sistema.depositar(1, ahorrosAna, 60_000, "");
        sistema.canjearBeneficio(1, TipoBeneficio.RETIROS_SIN_CARGO, null);

        assertEquals(600, ana().getPuntos());
        assertEquals(100, ana().getPuntosDisponibles());
        assertEquals(RangoCliente.PLATA, ana().getRango());

        Transaccion retiro = sistema.retirar(1, ahorrosAna, 1_000, CategoriaGasto.OTROS, "");
        assertEquals(0, retiro.getComision());
    }

    @Test
    void sinPuntosSuficientesNoSeCanjea() {
        sistema.depositar(1, ahorrosAna, 5_000, "");
        OperacionInvalidaException e = assertThrows(OperacionInvalidaException.class,
                () -> sistema.canjearBeneficio(1, TipoBeneficio.DESCUENTO_TRANSFERENCIAS, null));
        assertTrue(e.getMessage().contains("50"));
    }

    @Test
    void noSePuedeCanjearDosVecesElMismoBeneficioVigente() {
        sistema.depositar(1, ahorrosAna, 50_000, "");
        sistema.canjearBeneficio(1, TipoBeneficio.DESCUENTO_TRANSFERENCIAS, null);
        assertThrows(OperacionInvalidaException.class,
                () -> sistema.canjearBeneficio(1, TipoBeneficio.DESCUENTO_TRANSFERENCIAS, null));
    }

    @Test
    void descuentoDeTransferenciaSeAplica() {
        sistema.depositar(1, ahorrosAna, 30_000, "");
        sistema.canjearBeneficio(1, TipoBeneficio.DESCUENTO_TRANSFERENCIAS, null);
        Transaccion t = sistema.transferir(1, ahorrosAna, principalLuis, 1_000, CategoriaGasto.OTROS, "");
        assertEquals(9.0, t.getComision());
    }

    @Test
    void bonoDeSaldoEntraAlMonederoYNoSeRevierte() {
        sistema.depositar(1, ahorrosAna, 100_000, "");
        sistema.canjearBeneficio(1, TipoBeneficio.BONO_SALDO, gastosAna);

        assertEquals(50, sistema.buscarMonedero(gastosAna).getSaldo());
        assertEquals(0, ana().getPuntosDisponibles());
        assertEquals(TipoTransaccion.DEPOSITO, sistema.verUltimaReversible(1).getTipo());
        assertEquals(100_000, sistema.verUltimaReversible(1).getMonto());
    }

    @Test
    void revertirDespuesDeCanjearNoDejaPuntosNegativos() {
        sistema.depositar(1, ahorrosAna, 10_000, "");
        sistema.canjearBeneficio(1, TipoBeneficio.DESCUENTO_TRANSFERENCIAS, null);
        sistema.revertirUltimaTransaccion(1);

        assertEquals(0, ana().getPuntos());
        assertEquals(0, ana().getPuntosDisponibles());
    }

    @Test
    void programadasSeEjecutanEnOrdenDeFecha() {
        sistema.depositar(1, ahorrosAna, 10_000, "");
        LocalDateTime base = LocalDateTime.now().plusMinutes(5);

        sistema.programar(1, TipoTransaccion.TRANSFERENCIA, ahorrosAna, principalLuis, 100,
                CategoriaGasto.OTROS, "segunda", base.plusHours(2), Frecuencia.UNA_VEZ, 1);
        sistema.programar(1, TipoTransaccion.TRANSFERENCIA, ahorrosAna, principalEva, 100,
                CategoriaGasto.OTROS, "primera", base.plusHours(1), Frecuencia.UNA_VEZ, 1);

        assertEquals("primera", sistema.getPlanificador().proxima().getDescripcion());

        assertEquals(0, sistema.procesarProgramadas(base));
        assertEquals(2, sistema.procesarProgramadas(base.plusHours(3)));
        assertEquals(0, sistema.getPlanificador().pendientes());

        LinkedList<Transaccion> historial = ana().getHistorialTransacciones();
        assertEquals("primera", historial.get(1).getDescripcion());
        assertEquals("segunda", historial.get(2).getDescripcion());
        assertTrue(historial.get(1).esAutomatica());
    }

    @Test
    void programadaRecurrenteVuelveALaColaHastaAgotarse() {
        LocalDateTime inicio = LocalDateTime.now().plusMinutes(5);
        sistema.programar(1, TipoTransaccion.DEPOSITO, 0, ahorrosAna, 1_000, null,
                "ahorro", inicio, Frecuencia.SEMANAL, 3);

        sistema.procesarProgramadas(inicio.plusWeeks(5));

        assertEquals(3_000, sistema.buscarMonedero(ahorrosAna).getSaldo());
        assertEquals(0, sistema.getPlanificador().pendientes());
    }

    @Test
    void programadaQueFallaAvisaAlCliente() {
        LocalDateTime cuando = LocalDateTime.now().plusMinutes(5);
        sistema.programar(1, TipoTransaccion.RETIRO, ahorrosAna, 0, 5_000, CategoriaGasto.OTROS,
                "sin plata", cuando, Frecuencia.UNA_VEZ, 1);

        sistema.procesarProgramadas(cuando.plusMinutes(1));

        boolean avisoFallo = false;
        for (Notificacion n : ana().getNotificaciones()) {
            avisoFallo |= n.getTipo() == TipoNotificacion.PROGRAMADA_FALLIDA;
        }
        assertTrue(avisoFallo);
    }

    @Test
    void recordatorioParaLoQueSeEjecutaPronto() {
        LocalDateTime ahora = LocalDateTime.now();
        sistema.programar(1, TipoTransaccion.DEPOSITO, 0, ahorrosAna, 1_000, null,
                "pronto", ahora.plusHours(3), Frecuencia.UNA_VEZ, 1);
        sistema.programar(1, TipoTransaccion.DEPOSITO, 0, ahorrosAna, 1_000, null,
                "lejos", ahora.plusDays(5), Frecuencia.UNA_VEZ, 1);

        sistema.procesarProgramadas(ahora);

        int recordatorios = 0;
        for (Notificacion n : ana().getNotificaciones()) {
            if (n.getTipo() == TipoNotificacion.RECORDATORIO_PROGRAMADA) {
                recordatorios++;
                assertTrue(n.getMensaje().contains("pronto"));
            }
        }
        assertEquals(1, recordatorios);
    }

    @Test
    void cancelarProgramada() {
        TransaccionProgramada p = sistema.programar(1, TipoTransaccion.DEPOSITO, 0, ahorrosAna, 1_000, null,
                "x", LocalDateTime.now().plusDays(1), Frecuencia.DIARIA, 0);
        assertThrows(OperacionInvalidaException.class, () -> sistema.cancelarProgramada(2, p.getId()));
        sistema.cancelarProgramada(1, p.getId());
        assertEquals(0, sistema.getPlanificador().pendientes());
    }

    @Test
    void noSeProgramaEnElPasadoNiConMonederoAjeno() {
        assertThrows(OperacionInvalidaException.class, () -> sistema.programar(1, TipoTransaccion.DEPOSITO, 0,
                ahorrosAna, 100, null, "", LocalDateTime.now().minusDays(1), Frecuencia.UNA_VEZ, 1));
        assertThrows(OperacionInvalidaException.class, () -> sistema.programar(1, TipoTransaccion.RETIRO,
                principalLuis, 0, 100, null, "", LocalDateTime.now().plusDays(1), Frecuencia.UNA_VEZ, 1));
    }

    @Test
    void saldoBajoSoloAvisaAlCruzarElUmbral() {
        sistema.depositar(1, ahorrosAna, 12_000, "");
        sistema.retirar(1, ahorrosAna, 3_000, CategoriaGasto.OTROS, "");
        sistema.retirar(1, ahorrosAna, 1_000, CategoriaGasto.OTROS, "");

        int avisos = 0;
        for (Notificacion n : ana().getNotificaciones()) {
            if (n.getTipo() == TipoNotificacion.SALDO_BAJO) {
                avisos++;
            }
        }
        assertEquals(1, avisos);
    }

    @Test
    void notificacionesSeGuardanEnListaCircularConTope() {
        sistema.depositar(1, ahorrosAna, 1_000_000, "");
        for (int i = 0; i < 40; i++) {
            sistema.transferir(1, ahorrosAna, principalLuis, 10, CategoriaGasto.OTROS, "t" + i);
        }
        assertEquals(ReglasNegocio.MAX_NOTIFICACIONES_POR_CLIENTE, sistema.buscarCliente(2).getNotificaciones().size());
        assertTrue(sistema.buscarCliente(2).getNotificaciones().getLast().getMensaje().contains("Ana"));
        assertEquals(0, sistema.getCentroNotificaciones().pendientes());
    }

    @Test
    void reporteGeneralDetectaCicloDeTransferencias() {
        sistema.depositar(1, ahorrosAna, 10_000, "");
        sistema.depositar(2, principalLuis, 10_000, "");
        sistema.depositar(3, principalEva, 10_000, "");
        sistema.transferir(1, ahorrosAna, principalLuis, 1_000, CategoriaGasto.OTROS, "");
        sistema.transferir(2, principalLuis, principalEva, 1_000, CategoriaGasto.OTROS, "");

        AnalizadorGastos analizador = new AnalizadorGastos(sistema);
        assertTrue(analizador.reporteGeneral().cicloDetectado().isEmpty());

        sistema.transferir(3, principalEva, ahorrosAna, 1_000, CategoriaGasto.OTROS, "");
        assertEquals(3, analizador.reporteGeneral().cicloDetectado().size());

        sistema.revertirUltimaTransaccion(3);
        assertTrue(analizador.reporteGeneral().cicloDetectado().isEmpty());
    }

    @Test
    void analisisDelClienteAgrupaGastosYHabitos() {
        sistema.depositar(1, ahorrosAna, 50_000, "");
        sistema.retirar(1, ahorrosAna, 4_000, CategoriaGasto.ALIMENTACION, "a");
        sistema.retirar(1, ahorrosAna, 1_000, CategoriaGasto.TRANSPORTE, "b");
        sistema.retirar(1, ahorrosAna, 3_000, CategoriaGasto.ALIMENTACION, "c");
        sistema.retirar(1, ahorrosAna, 1_000, CategoriaGasto.TRANSPORTE, "d");
        sistema.transferir(1, ahorrosAna, gastosAna, 5_000, CategoriaGasto.AHORRO, "interna");

        AnalizadorGastos.ReporteCliente r = new AnalizadorGastos(sistema).reporteDe(1);

        assertEquals(9_000, r.gastoTotal());
        assertEquals(CategoriaGasto.ALIMENTACION, r.categoriaPrincipal().categoria());
        assertEquals(77.8, r.categoriaPrincipal().porcentaje());
        AnalizadorGastos.Flujo masFrecuente = r.habitosEntreCategorias().get(0);
        assertEquals("ALIMENTACION", masFrecuente.origen());
        assertEquals("TRANSPORTE", masFrecuente.destino());
        assertEquals(2, masFrecuente.cantidad());
        assertFalse(r.flujoEntreMonederos().isEmpty());
    }

    @Test
    void sistemaSanoPasaTodasLasRevisiones() {
        sistema.depositar(1, ahorrosAna, 100_000, "");
        sistema.transferir(1, ahorrosAna, principalLuis, 5_000, CategoriaGasto.OTROS, "");
        sistema.retirar(2, principalLuis, 1_000, CategoriaGasto.OTROS, "");
        sistema.canjearBeneficio(1, TipoBeneficio.BONO_SALDO, gastosAna);
        sistema.revertirUltimaTransaccion(2);

        ReporteIntegridad r = new VerificadorIntegridad().verificar(sistema);
        assertTrue(r.isTodoCorrecto(), () -> r.getProblemas().toString());
        assertEquals(5, r.getRevisiones().size());
    }

    @Test
    void detectaUnaTransaccionAlterada() {
        sistema.depositar(1, ahorrosAna, 1_000, "");
        sistema.depositar(1, ahorrosAna, 2_000, "");

        Node<Transaccion> primera = sistema.getLibroMayor().getHead();
        primera.getData().setMonto(9_999);

        ReporteIntegridad r = new VerificadorIntegridad().verificar(sistema);
        assertFalse(r.isTodoCorrecto());
        assertTrue(r.getProblemas().toString().contains("#1 fue modificada"));
    }

    @Test
    void detectaSaldoQueNoCuadraConElLibro() {
        sistema.depositar(1, ahorrosAna, 1_000, "");
        sistema.buscarMonedero(ahorrosAna).setSaldo(5_000);

        ReporteIntegridad r = new VerificadorIntegridad().verificar(sistema);
        assertFalse(r.isTodoCorrecto());
        assertTrue(r.getProblemas().get(0).contains("según el libro mayor"));
    }
}
