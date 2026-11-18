package com.Project.projectApi.ModelFactory;

import com.Project.projectApi.Enum.CategoriaGasto;
import com.Project.projectApi.Enum.Frecuencia;
import com.Project.projectApi.Enum.TipoBeneficio;
import com.Project.projectApi.Enum.TipoTransaccion;
import com.Project.projectApi.Model.Cliente;
import com.Project.projectApi.Model.MonederoSystem;

import java.time.LocalDateTime;

final class DatosDePrueba {

    private DatosDePrueba() {
    }

    static void cargar(MonederoSystem s) {
        if (!s.getClientes().isEmpty()) {
            return;
        }

        s.agregarCliente(new Cliente(123456, "Juan Pérez", "juan@gmail.com"));
        s.agregarCliente(new Cliente(987654, "María González", "maria@gmail.com"));
        s.agregarCliente(new Cliente(456789, "Santiago Vallejo", "santiago@gmail.com"));
        s.agregarCliente(new Cliente(741852, "Carlos Ramírez", "carlos@gmail.com"));
        s.agregarCliente(new Cliente(369258, "Laura Torres", "laura@gmail.com"));

        int ahorrosJuan = s.crearMonedero(123456, "Ahorros").getId();
        int gastosJuan = s.crearMonedero(123456, "Gastos diarios").getId();
        int principalMaria = s.crearMonedero(987654, "Principal").getId();
        s.crearMonedero(987654, "Vacaciones");
        int inversionSantiago = s.crearMonedero(456789, "Inversión").getId();
        int gastosSantiago = s.crearMonedero(456789, "Gastos").getId();
        int principalCarlos = s.crearMonedero(741852, "Principal").getId();
        int principalLaura = s.crearMonedero(369258, "Principal").getId();

        s.depositar(123456, ahorrosJuan, 60_000, "Nómina");
        s.depositar(123456, gastosJuan, 20_000, "Recarga");
        s.transferir(123456, ahorrosJuan, gastosJuan, 10_000, CategoriaGasto.AHORRO, "Plata para el mes");
        s.retirar(123456, gastosJuan, 8_000, CategoriaGasto.ALIMENTACION, "Mercado");
        s.retirar(123456, gastosJuan, 3_500, CategoriaGasto.TRANSPORTE, "Taxi");
        s.transferir(123456, ahorrosJuan, principalMaria, 15_000, CategoriaGasto.SERVICIOS, "Arriendo compartido");
        s.retirar(123456, gastosJuan, 4_000, CategoriaGasto.ALIMENTACION, "Restaurante");

        s.depositar(987654, principalMaria, 40_000, "Salario");
        s.retirar(987654, principalMaria, 5_000, CategoriaGasto.TRANSPORTE, "Gasolina");
        s.transferir(987654, principalMaria, inversionSantiago, 7_000, CategoriaGasto.EDUCACION, "Clases de inglés");
        s.retirar(987654, principalMaria, 3_000, CategoriaGasto.ENTRETENIMIENTO, "Cine");

        s.depositar(456789, inversionSantiago, 600_000, "Venta de acciones");
        s.transferir(456789, inversionSantiago, gastosSantiago, 50_000, CategoriaGasto.AHORRO, "Presupuesto del mes");
        s.retirar(456789, gastosSantiago, 12_000, CategoriaGasto.SALUD, "Medicina prepagada");
        s.transferir(456789, gastosSantiago, gastosJuan, 5_000, CategoriaGasto.OTROS, "Devolución de préstamo");
        s.retirar(456789, gastosSantiago, 9_000, CategoriaGasto.ALIMENTACION, "Mercado");

        s.depositar(741852, principalCarlos, 12_000, "Primer ahorro");
        s.retirar(741852, principalCarlos, 3_000, CategoriaGasto.SERVICIOS, "Factura de la luz");

        s.depositar(369258, principalLaura, 25_000, "Mesada");
        s.transferir(369258, principalLaura, principalCarlos, 2_000, CategoriaGasto.SERVICIOS, "Internet compartido");

        s.canjearBeneficio(123456, TipoBeneficio.DESCUENTO_TRANSFERENCIAS, null);
        s.canjearBeneficio(456789, TipoBeneficio.RETIROS_SIN_CARGO, null);
        s.canjearBeneficio(456789, TipoBeneficio.BONO_SALDO, gastosSantiago);

        LocalDateTime ahora = LocalDateTime.now().withSecond(0).withNano(0);
        s.programar(123456, TipoTransaccion.DEPOSITO, 0, ahorrosJuan, 5_000, null,
                "Ahorro automático", ahora.plusHours(2), Frecuencia.MENSUAL, 0);
        s.programar(987654, TipoTransaccion.TRANSFERENCIA, principalMaria, gastosJuan, 2_000,
                CategoriaGasto.ALIMENTACION, "Aporte para el mercado", ahora.plusDays(1).withHour(9).withMinute(0),
                Frecuencia.SEMANAL, 4);
        s.programar(741852, TipoTransaccion.RETIRO, principalCarlos, 0, 30_000, CategoriaGasto.ENTRETENIMIENTO,
                "Pago del gimnasio", ahora.plusDays(3), Frecuencia.UNA_VEZ, 1);

        s.procesarProgramadas(LocalDateTime.now());
    }
}
