package com.Project.projectApi.Model;

import com.Project.projectApi.Enum.CategoriaGasto;
import com.Project.projectApi.Enum.EstadoTransaccion;
import com.Project.projectApi.Enum.Frecuencia;
import com.Project.projectApi.Enum.RangoCliente;
import com.Project.projectApi.Enum.TipoBeneficio;
import com.Project.projectApi.Enum.TipoNotificacion;
import com.Project.projectApi.Enum.TipoTransaccion;
import com.Project.projectApi.Excepcion.OperacionInvalidaException;
import com.Project.projectApi.Hash.HashTable;
import com.Project.projectApi.List.LinkedList;
import com.Project.projectApi.Stack.Stack;

import java.time.LocalDateTime;

public class MonederoSystem {

    private static MonederoSystem instance;

    private final LinkedList<Cliente> clientes = new LinkedList<>();
    private final HashTable<Integer, Cliente> clientesPorId = new HashTable<>();

    private final HashTable<Integer, Monedero> monederosPorId = new HashTable<>();

    private final LinkedList<Beneficio> beneficios = new LinkedList<>();

    private final LinkedList<Transaccion> libroMayor = new LinkedList<>();
    private String ultimaHuella = VerificadorIntegridad.HUELLA_INICIAL;

    private final CentroNotificaciones centroNotificaciones;
    private final GestorPuntos gestorPuntos;
    private final PlanificadorTransacciones planificador;

    private int siguienteIdMonedero = 1;
    private int siguienteIdTransaccion = 1;
    private int siguienteIdBeneficio = 1;

    MonederoSystem() {
        centroNotificaciones = new CentroNotificaciones(this);
        gestorPuntos = new GestorPuntos(centroNotificaciones);
        planificador = new PlanificadorTransacciones(this, centroNotificaciones);

        for (TipoBeneficio tipo : TipoBeneficio.values()) {
            beneficios.add(Beneficio.delCatalogo(siguienteIdBeneficio++, tipo));
        }
    }

    public static MonederoSystem getInstance() {
        if (instance == null) {
            instance = new MonederoSystem();
        }
        return instance;
    }

    public boolean agregarCliente(Cliente cliente) {
        if (clientesPorId.containsKey(cliente.getId())) {
            return false;
        }
        clientes.add(cliente);
        clientesPorId.put(cliente.getId(), cliente);
        gestorPuntos.registrarCliente(cliente);
        return true;
    }

    public Cliente buscarCliente(int id) {
        return clientesPorId.get(id);
    }

    public boolean actualizarCliente(int id, String nombre, String correo) {
        Cliente cliente = buscarCliente(id);
        if (cliente == null) {
            return false;
        }
        cliente.setNombre(nombre);
        cliente.setCorreo(correo);
        return true;
    }

    public boolean eliminarCliente(int id) {
        Cliente cliente = buscarCliente(id);
        if (cliente == null) {
            return false;
        }

        for (Monedero monedero : cliente.getMonederos()) {
            monederosPorId.remove(monedero.getId());
        }
        planificador.cancelarTodasDe(id);
        gestorPuntos.quitarCliente(id);
        clientesPorId.remove(id);
        return clientes.remove(cliente);
    }

    public Monedero crearMonedero(int idCliente, String nombre) {
        Cliente cliente = clienteObligatorio(idCliente);

        if (nombre == null || nombre.isBlank()) {
            throw new OperacionInvalidaException("Ponle un nombre al monedero (por ejemplo «Ahorros»)");
        }

        Monedero monedero = new Monedero(siguienteIdMonedero, nombre.trim(), 0);
        agregarMonedero(cliente.getId(), monedero);
        return monedero;
    }

    public boolean agregarMonedero(int idCliente, Monedero monedero) {
        Cliente cliente = buscarCliente(idCliente);
        if (cliente == null || monederosPorId.containsKey(monedero.getId())) {
            return false;
        }

        monedero.setIdCliente(idCliente);
        cliente.agregarMonedero(monedero);
        monederosPorId.put(monedero.getId(), monedero);

        if (monedero.getId() >= siguienteIdMonedero) {
            siguienteIdMonedero = monedero.getId() + 1;
        }
        return true;
    }

    public Monedero buscarMonedero(int idMonedero) {
        return monederosPorId.get(idMonedero);
    }

    public Monedero buscarMonedero(int idCliente, int idMonedero) {
        Monedero monedero = monederosPorId.get(idMonedero);
        return (monedero != null && monedero.getIdCliente() == idCliente) ? monedero : null;
    }

    public boolean actualizarMonedero(int idCliente, int idMonedero, String nombre) {
        Monedero monedero = buscarMonedero(idCliente, idMonedero);
        if (monedero == null) {
            return false;
        }
        monedero.setNombre(nombre);
        return true;
    }

    public boolean eliminarMonedero(int idCliente, int idMonedero) {
        Cliente cliente = buscarCliente(idCliente);
        Monedero monedero = buscarMonedero(idCliente, idMonedero);
        if (cliente == null || monedero == null) {
            return false;
        }

        if (monedero.getSaldo() > 0) {
            throw new OperacionInvalidaException("El monedero #" + idMonedero + " todavía tiene "
                    + ReglasNegocio.formatoDinero(monedero.getSaldo())
                    + ". Muévelo a otro monedero antes de eliminarlo.");
        }

        monederosPorId.remove(idMonedero);
        return cliente.getMonederos().remove(monedero);
    }

    public Transaccion depositar(int idCliente, int idMonedero, double monto, String descripcion) {
        return depositar(idCliente, idMonedero, monto, descripcion, 0);
    }

    Transaccion depositar(int idCliente, int idMonedero, double monto, String descripcion, int idProgramada) {
        Cliente cliente = clienteObligatorio(idCliente);
        Monedero monedero = monederoDelCliente(cliente, idMonedero);
        validarMonto(monto);

        Transaccion t = nuevaTransaccion(TipoTransaccion.DEPOSITO, monto, descripcion, cliente, idProgramada);
        t.setIdMonederoDestino(monedero.getId());
        t.setIdClienteDestino(cliente.getId());

        t.setSaldoAntes(monedero.getSaldo());
        monedero.depositar(t.getMonto());
        t.setSaldoDespues(monedero.getSaldo());

        darPuntos(cliente, t);
        registrar(cliente, t);
        centroNotificaciones.despachar();
        return t;
    }

    public Transaccion retirar(int idCliente, int idMonedero, double monto,
                               CategoriaGasto categoria, String descripcion) {
        return retirar(idCliente, idMonedero, monto, categoria, descripcion, 0);
    }

    Transaccion retirar(int idCliente, int idMonedero, double monto,
                        CategoriaGasto categoria, String descripcion, int idProgramada) {
        Cliente cliente = clienteObligatorio(idCliente);
        Monedero monedero = monederoDelCliente(cliente, idMonedero);
        validarMonto(monto);

        double comision = comisionPara(cliente, TipoTransaccion.RETIRO, monto);
        exigirSaldo(monedero, monto, comision);

        Transaccion t = nuevaTransaccion(TipoTransaccion.RETIRO, monto, descripcion, cliente, idProgramada);
        t.setIdMonederoOrigen(monedero.getId());
        t.setComision(comision);
        t.setCategoria(categoria == null ? CategoriaGasto.OTROS : categoria);

        double saldoAntes = monedero.getSaldo();
        t.setSaldoAntes(saldoAntes);
        monedero.retirar(t.getTotalDebitado());
        t.setSaldoDespues(monedero.getSaldo());

        darPuntos(cliente, t);
        registrar(cliente, t);
        centroNotificaciones.revisarSaldoBajo(cliente, monedero, saldoAntes);
        centroNotificaciones.despachar();
        return t;
    }

    public Transaccion transferir(int idCliente, int idMonederoOrigen, int idMonederoDestino,
                                  double monto, CategoriaGasto categoria, String descripcion) {
        return transferir(idCliente, idMonederoOrigen, idMonederoDestino, monto, categoria, descripcion, 0);
    }

    Transaccion transferir(int idCliente, int idMonederoOrigen, int idMonederoDestino, double monto,
                           CategoriaGasto categoria, String descripcion, int idProgramada) {
        Cliente cliente = clienteObligatorio(idCliente);
        Monedero origen = monederoDelCliente(cliente, idMonederoOrigen);
        validarMonto(monto);

        if (idMonederoOrigen == idMonederoDestino) {
            throw new OperacionInvalidaException("El monedero de origen y el de destino tienen que ser distintos");
        }

        Monedero destino = buscarMonedero(idMonederoDestino);
        if (destino == null) {
            throw new OperacionInvalidaException("No existe el monedero destino #" + idMonederoDestino);
        }

        Cliente receptor = clienteObligatorio(destino.getIdCliente());
        boolean entreSusMonederos = receptor == cliente;

        double comision = entreSusMonederos ? 0 : comisionPara(cliente, TipoTransaccion.TRANSFERENCIA, monto);
        exigirSaldo(origen, monto, comision);

        Transaccion t = nuevaTransaccion(TipoTransaccion.TRANSFERENCIA, monto, descripcion, cliente, idProgramada);
        t.setIdMonederoOrigen(origen.getId());
        t.setIdMonederoDestino(destino.getId());
        t.setIdClienteDestino(receptor.getId());
        t.setComision(comision);
        t.setCategoria(categoria == null ? CategoriaGasto.OTROS : categoria);

        double saldoAntes = origen.getSaldo();
        t.setSaldoAntes(saldoAntes);
        origen.retirar(t.getTotalDebitado());
        destino.depositar(t.getMonto());
        t.setSaldoDespues(origen.getSaldo());

        if (!entreSusMonederos) {
            darPuntos(cliente, t);
        }
        registrar(cliente, t);

        if (!entreSusMonederos) {
            receptor.agregarTransaccion(t);
            centroNotificaciones.enviar(receptor.getId(), TipoNotificacion.TRANSFERENCIA_RECIBIDA,
                    cliente.getNombre() + " te transfirió " + ReglasNegocio.formatoDinero(t.getMonto())
                            + " a tu monedero «" + destino.getNombre() + "».");
        }

        centroNotificaciones.revisarSaldoBajo(cliente, origen, saldoAntes);
        centroNotificaciones.despachar();
        return t;
    }

    public Transaccion revertirUltimaTransaccion(int idCliente) {
        Cliente cliente = clienteObligatorio(idCliente);
        Stack<Transaccion> pila = cliente.getTransaccionesReversibles();

        if (pila.isEmpty()) {
            throw new OperacionInvalidaException("No hay transacciones para revertir");
        }

        Transaccion t = pila.peek();

        switch (t.getTipo()) {
            case DEPOSITO -> {
                Monedero monedero = monederoQueTodaviaExiste(t.getIdMonederoDestino());
                exigirSaldoParaRevertir(monedero, t.getMonto());
                monedero.retirar(t.getMonto());
            }
            case RETIRO -> monederoQueTodaviaExiste(t.getIdMonederoOrigen()).depositar(t.getTotalDebitado());
            case TRANSFERENCIA -> {
                Monedero origen = monederoQueTodaviaExiste(t.getIdMonederoOrigen());
                Monedero destino = monederoQueTodaviaExiste(t.getIdMonederoDestino());
                exigirSaldoParaRevertir(destino, t.getMonto());
                destino.retirar(t.getMonto());
                origen.depositar(t.getTotalDebitado());

                if (!t.esInterna()) {
                    centroNotificaciones.enviar(t.getIdClienteDestino(), TipoNotificacion.REVERSION,
                            cliente.getNombre() + " revirtió la transferencia de "
                                    + ReglasNegocio.formatoDinero(t.getMonto()) + " que te había hecho.");
                }
            }
        }

        pila.pop();
        t.setEstado(EstadoTransaccion.REVERTIDA);
        gestorPuntos.restarPorReversion(cliente, t.getPuntosGenerados(),
                "Reversión de la transacción #" + t.getId(), t.getId());
        centroNotificaciones.despachar();
        return t;
    }

    public Transaccion verUltimaReversible(int idCliente) {
        Stack<Transaccion> pila = clienteObligatorio(idCliente).getTransaccionesReversibles();
        return pila.isEmpty() ? null : pila.peek();
    }

    public Beneficio canjearBeneficio(int idCliente, TipoBeneficio tipo, Integer idMonederoBono) {
        Cliente cliente = clienteObligatorio(idCliente);
        LocalDateTime ahora = LocalDateTime.now();

        if (tipo == null) {
            throw new OperacionInvalidaException("Elige qué beneficio quieres canjear");
        }

        Beneficio yaActivo = cliente.beneficioVigente(tipo, ahora);
        if (yaActivo != null) {
            throw new OperacionInvalidaException("Ya tienes «" + tipo.getNombre() + "» activo hasta el "
                    + yaActivo.getFechaVencimiento().toLocalDate() + ".");
        }

        Monedero monederoBono = null;
        if (tipo == TipoBeneficio.BONO_SALDO) {
            if (idMonederoBono == null) {
                throw new OperacionInvalidaException("Elige el monedero donde quieres recibir el bono");
            }
            monederoBono = monederoDelCliente(cliente, idMonederoBono);
        }

        gestorPuntos.cobrarCanje(cliente, tipo);

        Beneficio canjeado = Beneficio.canjeado(siguienteIdBeneficio++, tipo, ahora);
        cliente.agregarBeneficio(canjeado);

        if (monederoBono != null) {
            acreditarBono(cliente, monederoBono);
        }

        centroNotificaciones.enviar(cliente.getId(), TipoNotificacion.BENEFICIO,
                "Canjeaste «" + tipo.getNombre() + "» por " + tipo.getCostoPuntos() + " puntos."
                        + (canjeado.getFechaVencimiento() != null
                        ? " Vigente hasta el " + canjeado.getFechaVencimiento().toLocalDate() + "." : ""));
        centroNotificaciones.despachar();
        return canjeado;
    }

    private void acreditarBono(Cliente cliente, Monedero monedero) {
        Transaccion bono = nuevaTransaccion(TipoTransaccion.DEPOSITO, ReglasNegocio.VALOR_BONO_SALDO,
                "Bono por canje de puntos", cliente, 0);
        bono.setIdMonederoDestino(monedero.getId());
        bono.setIdClienteDestino(cliente.getId());
        bono.setReversible(false);

        bono.setSaldoAntes(monedero.getSaldo());
        monedero.depositar(bono.getMonto());
        bono.setSaldoDespues(monedero.getSaldo());

        registrar(cliente, bono);
    }

    public TransaccionProgramada programar(int idCliente, TipoTransaccion tipo, int idMonederoOrigen,
                                           int idMonederoDestino, double monto, CategoriaGasto categoria,
                                           String descripcion, LocalDateTime primeraEjecucion,
                                           Frecuencia frecuencia, int repeticiones) {
        return planificador.programar(idCliente, tipo, idMonederoOrigen, idMonederoDestino, monto,
                categoria, descripcion, primeraEjecucion, frecuencia, repeticiones);
    }

    public TransaccionProgramada cancelarProgramada(int idCliente, int idProgramada) {
        return planificador.cancelar(idCliente, idProgramada);
    }

    public int procesarProgramadas(LocalDateTime ahora) {
        return planificador.procesarPendientes(ahora);
    }

    public LinkedList<PuntajeCliente> ranking(int cantidad) {
        return gestorPuntos.ranking(cantidad);
    }

    public LinkedList<PuntajeCliente> clientesDelRango(RangoCliente rango) {
        return gestorPuntos.clientesDelRango(rango);
    }

    public LinkedList<Cliente> getClientes() {
        return clientes;
    }

    public LinkedList<Beneficio> getBeneficios() {
        return beneficios;
    }

    public LinkedList<Transaccion> getLibroMayor() {
        return libroMayor;
    }

    public GestorPuntos getGestorPuntos() {
        return gestorPuntos;
    }

    public PlanificadorTransacciones getPlanificador() {
        return planificador;
    }

    public CentroNotificaciones getCentroNotificaciones() {
        return centroNotificaciones;
    }

    public HashTable<Integer, Cliente> getClientesPorId() {
        return clientesPorId;
    }

    public HashTable<Integer, Monedero> getMonederosPorId() {
        return monederosPorId;
    }

    Cliente clienteObligatorio(int idCliente) {
        Cliente cliente = buscarCliente(idCliente);
        if (cliente == null) {
            throw new OperacionInvalidaException("No existe el cliente " + idCliente);
        }
        return cliente;
    }

    Monedero monederoDelCliente(Cliente cliente, int idMonedero) {
        Monedero monedero = buscarMonedero(cliente.getId(), idMonedero);
        if (monedero == null) {
            throw new OperacionInvalidaException("El monedero #" + idMonedero + " no es de " + cliente.getNombre());
        }
        return monedero;
    }

    private Transaccion nuevaTransaccion(TipoTransaccion tipo, double monto, String descripcion,
                                         Cliente cliente, int idProgramada) {
        String texto = (descripcion == null || descripcion.isBlank())
                ? tipo.name().charAt(0) + tipo.name().substring(1).toLowerCase()
                : descripcion.trim();

        Transaccion t = new Transaccion(siguienteIdTransaccion++, tipo, ReglasNegocio.redondear(monto),
                LocalDateTime.now(), texto);
        t.setIdCliente(cliente.getId());
        t.setIdProgramada(idProgramada);
        return t;
    }

    private double comisionPara(Cliente cliente, TipoTransaccion tipo, double monto) {
        LocalDateTime ahora = LocalDateTime.now();
        return ReglasNegocio.calcularComision(tipo, monto, cliente.getRango(),
                cliente.tieneVigente(TipoBeneficio.DESCUENTO_TRANSFERENCIAS, ahora),
                cliente.tieneVigente(TipoBeneficio.RETIROS_SIN_CARGO, ahora));
    }

    private void darPuntos(Cliente cliente, Transaccion t) {
        int base = ReglasNegocio.calcularPuntos(t.getTipo(), t.getMonto());
        int extra = ReglasNegocio.puntosExtraPorRango(cliente.getRango(), base);

        t.setPuntosBase(base);
        t.setPuntosExtra(extra);
        t.setPuntosGenerados(base + extra);

        gestorPuntos.sumar(cliente, base + extra,
                etiqueta(t.getTipo()) + " #" + t.getId(), t.getId());
    }

    private void registrar(Cliente cliente, Transaccion t) {
        t.setHuellaAnterior(ultimaHuella);
        t.setHuella(VerificadorIntegridad.calcularHuella(t));
        ultimaHuella = t.getHuella();

        libroMayor.add(t);
        cliente.agregarTransaccion(t);

        if (t.isReversible()) {
            cliente.getTransaccionesReversibles().push(t);
        }
    }

    private Monedero monederoQueTodaviaExiste(int idMonedero) {
        Monedero monedero = buscarMonedero(idMonedero);
        if (monedero == null) {
            throw new OperacionInvalidaException("No se puede revertir: el monedero #" + idMonedero + " ya no existe");
        }
        return monedero;
    }

    private void validarMonto(double monto) {
        if (!(monto > 0)) {
            throw new OperacionInvalidaException("El monto tiene que ser mayor que 0");
        }
    }

    private void exigirSaldo(Monedero monedero, double monto, double comision) {
        double total = ReglasNegocio.redondear(monto + comision);
        if (monedero.getSaldo() < total) {
            throw new OperacionInvalidaException("Saldo insuficiente en «" + monedero.getNombre() + "»: necesitas "
                    + ReglasNegocio.formatoDinero(total) + " (" + ReglasNegocio.formatoDinero(monto)
                    + " + " + ReglasNegocio.formatoDinero(comision) + " de comisión) y tienes "
                    + ReglasNegocio.formatoDinero(monedero.getSaldo()) + ".");
        }
    }

    private void exigirSaldoParaRevertir(Monedero monedero, double monto) {
        if (monedero.getSaldo() < monto) {
            throw new OperacionInvalidaException("No se puede revertir: el monedero #" + monedero.getId()
                    + " ya no tiene los " + ReglasNegocio.formatoDinero(monto)
                    + " (le quedan " + ReglasNegocio.formatoDinero(monedero.getSaldo()) + ").");
        }
    }

    private static String etiqueta(TipoTransaccion tipo) {
        return switch (tipo) {
            case DEPOSITO -> "Depósito";
            case RETIRO -> "Retiro";
            case TRANSFERENCIA -> "Transferencia";
        };
    }
}
