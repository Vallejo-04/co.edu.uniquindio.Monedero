# Monedero Virtual con sistema de puntos — Guía de funcionamiento

Esta guía explica cómo arrancar el proyecto, qué hace cada parte, qué estructura de datos resuelve cada requisito del enunciado y cómo usar la aplicación desde la web (administrador y cliente) o desde la API REST.

---

## 1. Cómo ejecutarlo

Requisitos: **Java 24** (lo pide el `pom.xml`) y conexión a internet la primera vez para que Maven baje las dependencias.

```bash
./mvnw spring-boot:run
```

En Windows:

```bash
mvnw.cmd spring-boot:run
```

Luego abre <http://localhost:8080>.

| Dirección | Qué hay |
|---|---|
| `http://localhost:8080/` | Inicio: eliges si entras como administrador o como cliente |
| `http://localhost:8080/admin` | Consola del administrador |
| `http://localhost:8080/cliente` | Acceso del cliente (pide la identificación) |
| `http://localhost:8080/api/...` | API REST en JSON (sección 7) |

Para correr las pruebas automáticas:

```bash
./mvnw test
```

### Datos de ejemplo

Al arrancar se cargan cinco clientes con movimientos reales (`ModelFactory/DatosDePrueba.java`). Puedes usar estas identificaciones para entrar como cliente:

| Identificación | Cliente | Rango | Qué tiene de interesante |
|---|---|---|---|
| 456789 | Santiago Vallejo | PLATINO | Dos beneficios canjeados (retiros sin cargo y bono de saldo) |
| 123456 | Juan Pérez | ORO | Descuento en transferencias activo y un depósito automático mensual |
| 987654 | María González | PLATA | Transferencia semanal programada |
| 369258 | Laura Torres | BRONCE | Le transfirió a Carlos |
| 741852 | Carlos Ramírez | BRONCE | Alerta de saldo bajo y un retiro programado que fallará por falta de saldo |

Juan → María → Santiago → Juan forman a propósito un **ciclo de transferencias**, para que el reporte del grafo lo detecte.

Si no quieres datos de ejemplo, borra la línea `DatosDePrueba.cargar(monederoSystem);` en `ModelFactory`.

---

## 2. Cómo está organizado

```
com.Project.projectApi
├── Nodes          Node, NodeTree, NodeHash, NodeVertex, NodeEdge   ← los "eslabones"
├── List           LinkedList, CircularList
├── Stack          Stack
├── Queue          Queue, PriorityQueue
├── Tree           Tree (BST), AVLTree
├── Hash           HashTable
├── Graph          Graph, Arista
├── Enum           RangoCliente, TipoTransaccion, TipoBeneficio, CategoriaGasto, Frecuencia, ...
├── Excepcion      OperacionInvalidaException
├── Model          Cliente, Monedero, Transaccion, Beneficio, Notificacion, TransaccionProgramada,
│                  MonederoSystem, GestorPuntos, PlanificadorTransacciones, CentroNotificaciones,
│                  AnalizadorGastos, VerificadorIntegridad, ReglasNegocio, ...
├── DTO            records que viajan hacia las vistas y la API + MonederoMapper
├── ModelFactory   ModelFactory (fachada), DatosDePrueba, TareasProgramadas (reloj)
└── Controller     MainController, AdminController, ClienteController, ApiController
```

El camino de una petición siempre es el mismo:

```
Vista web / API  →  Controller  →  ModelFactory  →  MonederoSystem  →  estructuras de datos
                    (recibe DTOs)   (traduce a DTO)  (reglas de negocio)
```

- Los **controladores** nunca tocan un `Cliente` o un `Monedero`: reciben DTOs.
- **`ModelFactory`** es la única puerta al modelo. Sus métodos son `synchronized` porque el reloj de las transacciones programadas corre en otro hilo.
- **`MonederoSystem`** maneja clientes, monederos y dinero, y delega lo demás en cuatro ayudantes:

| Clase | Se encarga de | Estructuras |
|---|---|---|
| `GestorPuntos` | puntos, rangos y ranking | `AVLTree` + `HashTable` |
| `PlanificadorTransacciones` | transacciones programadas | `PriorityQueue` |
| `CentroNotificaciones` | alertas y avisos | `Queue` + `CircularList` |
| `AnalizadorGastos` | reportes de patrones de gasto | `Graph` |
| `VerificadorIntegridad` | que todo cuadre | recursión sobre `Node` |

---

## 3. Estructuras de datos

Todas son **genéricas** y están hechas con **nodos enlazados**. No se usa ningún contenedor de `java.util` (`ArrayList`, `HashMap`, `PriorityQueue`...). Todas implementan `Iterable`, así se recorren con `for-each` y con `th:each` en Thymeleaf.

| Requisito del enunciado | Clase | Dónde se usa | Operaciones clave |
|---|---|---|---|
| Listas: transacciones por cliente | `LinkedList<T>` | `Cliente.historialTransacciones`, `historialPuntos`, `monederos`, y el libro mayor de `MonederoSystem` | `add` O(1) al final, `find` O(n) |
| Pilas: transacciones reversibles | `Stack<T>` | `Cliente.transaccionesReversibles` | `push` / `pop` / `peek` O(1) |
| Colas: notificaciones | `Queue<T>` | `CentroNotificaciones.porEntregar` (bandeja de salida) | `enqueue` / `dequeue` O(1) |
| Colas de prioridad: programadas | `PriorityQueue<T>` | `PlanificadorTransacciones.agenda` | `enqueue` O(n), `dequeue`/`peek` O(1) |
| Listas circulares: notificaciones | `CircularList<T>` | `Cliente.notificaciones` (guarda las últimas 30) | `add` / `removeFirst` O(1) |
| Árbol binario de búsqueda: puntos | `Tree<T>` | base de `AVLTree` | `insert` / `remove` / `search` O(altura) |
| Árbol balanceado: rangos | `AVLTree<T>` | `GestorPuntos.arbolPuntos` | todo en O(log n); ranking y consulta por rango O(log n + k) |
| Tabla hash: puntos por cliente | `HashTable<K,V>` | `clientesPorId`, `monederosPorId`, `puntajePorCliente` | `put` / `get` / `remove` O(1) promedio |
| Grafos dirigidos | `Graph<T>` | `AnalizadorGastos` (clientes, categorías, monederos) | `addEdge` O(grado), BFS y DFS O(V + E) |

### Detalles que vale la pena explicar en el informe

**`PriorityQueue` como lista enlazada ordenada.** Insertar recorre la lista hasta encontrar el lugar (O(n)), pero el siguiente a ejecutar siempre está en la cabeza (O(1)). El planificador pregunta "¿ya toca?" cada 30 segundos y solo programa algo nuevo de vez en cuando, así que conviene tener el frente listo. Si dos programadas tienen la misma fecha, sale primero la que se programó antes (es estable).

**`CircularList` con capacidad.** El último nodo apunta al primero y solo se guarda `tail`, así que la cabeza es `tail.next`: agregar al final y quitar del inicio cuestan O(1). Cuando llega a su capacidad (30 notificaciones), lo nuevo saca a lo más viejo. Así la memoria por cliente no crece sin control.

**`Tree` y `AVLTree`.** `Tree` es un BST completo: insertar, eliminar (con sucesor inorden), buscar, mínimo, máximo, inorden, `mayores(k)` y `rango(desde, hasta)`. `AVLTree` hereda todo y solo sobrescribe `reequilibrar(nodo)`, que se llama en cada nodo al volver de la recursión y aplica las rotaciones simples o dobles. La prueba `AVLTreeTest` inserta 1.000 números en orden: el BST termina con altura 1.000 y el AVL con altura 15 o menos.

**`HashTable` con encadenamiento.** Es un arreglo de cubetas en el que cada cubeta es una cadena de `NodeHash`. El índice se calcula con `(hashCode & 0x7fffffff) % capacidad`. Cuando la ocupación supera el 75 %, la capacidad se duplica y los mismos nodos se reubican (rehash). El arreglo es lo que da el O(1); el estilo enlazado está en las cadenas.

**`Graph` con listas de adyacencia.** Los vértices forman una lista enlazada y cada vértice tiene su propia lista de aristas (`NodeEdge`). Una `HashTable` (dato → vértice) evita recorrer la lista para encontrar un vértice. Si una arista se repite, se acumula el peso y se cuenta la repetición en vez de duplicarla. Algoritmos:
- `alcanzablesDesde(v)`: recorrido en anchura (BFS) con nuestra `Queue`.
- `buscarCiclo()`: búsqueda en profundidad (DFS) recursiva con colores blanco, gris y negro.
- `pesoSalida` / `pesoEntrada`: total que sale o llega a un vértice.

---

## 4. Reglas de negocio

Todas están en `Model/ReglasNegocio.java` y `Enum/`, para cambiarlas en un solo lugar.

### Puntos (enunciado)

| Transacción | Puntos base |
|---|---|
| Depósito | 1 por cada 100 |
| Retiro | 2 por cada 100 |
| Transferencia a otro cliente | 3 por cada 100 |
| Transferencia entre monederos propios | 0 (solo está reorganizando su plata) |

Cada cliente tiene dos contadores:
- **Acumulados**: todo lo ganado. Definen el **rango**. Solo bajan si se revierte una transacción.
- **Disponibles**: lo que puede canjear. Bajan al canjear, y el rango no cambia.

### Rangos (enunciado)

| Rango | Puntos | Descuento en comisiones | Puntos extra por transacción |
|---|---|---|---|
| Bronce | 0 – 500 | 0 % | 0 % |
| Plata | 501 – 1000 | 10 % | +5 % |
| Oro | 1001 – 5000 | 25 % | +10 % |
| Platino | más de 5000 | 50 % | +20 % |

El enunciado deja abiertos los beneficios de cada rango; los porcentajes de descuento y de puntos extra son una propuesta.

### Comisiones

| Operación | Comisión |
|---|---|
| Depósito | 0 |
| Retiro | 0,5 % del monto (0 si tiene «Retiros sin cargo» vigente) |
| Transferencia a otro cliente | 1 % del monto (10 % menos con «Descuento en transferencias») |
| Transferencia entre monederos propios | 0 |

La comisión se descuenta del monedero de origen además del monto, y se le aplica el descuento del rango.

### Beneficios canjeables (enunciado)

| Puntos | Beneficio | Efecto |
|---|---|---|
| 100 | Descuento en transferencias | 10 % menos de comisión en transferencias durante 30 días |
| 500 | Retiros sin cargo | Un mes sin comisión por retiros |
| 1000 | Bono de saldo | 50 unidades a un monedero elegido (no se puede revertir) |

No se puede canjear dos veces un beneficio que sigue vigente.

### Alertas

- **Saldo bajo**: cuando un monedero queda por debajo de 10.000. Solo se avisa al cruzar el umbral, no en cada compra.
- **Recordatorio**: 24 horas antes de que se ejecute una transacción programada.
- Además se avisa de: transferencia recibida, programada ejecutada o fallida, cambio de rango, beneficio canjeado y transferencia revertida.

---

## 5. Qué pasa por dentro en cada operación

### Un depósito, paso a paso

1. `ClienteController` (web) o `ApiController` (API) recibe la petición y llama a `ModelFactory.depositar`.
2. `MonederoSystem` busca al cliente y al monedero en sus **tablas hash** (O(1)) y valida el monto.
3. Actualiza el saldo y calcula los puntos: base del enunciado más el extra del rango.
4. `GestorPuntos` suma los puntos, anota el movimiento en el historial de puntos (**lista**) y reubica al cliente en el **árbol AVL** (saca el nodo viejo y mete el nuevo). Si cambió de rango, envía una notificación.
5. La transacción se **sella** con su huella SHA-256 encadenada a la anterior y se guarda en tres lugares: el libro mayor (**lista**), el historial del cliente (**lista**) y su **pila** de reversibles.
6. `CentroNotificaciones` vacía su **cola** y entrega cada aviso en la **lista circular** de su dueño.

### Revertir

Se toma la cima de la **pila** del cliente, es decir, lo último que hizo:
- se devuelve el dinero, incluida la comisión;
- se descuentan los puntos que dio esa transacción y se recalcula el rango;
- la transacción queda en el historial marcada como `REVERTIDA`.

Si no se puede revertir (por ejemplo, porque quien recibió una transferencia ya se gastó el dinero), no cambia nada y la transacción sigue en la pila.

### Transacciones programadas

Hay dos formas de que se procesen:
- `TareasProgramadas` (con `@Scheduled`) llama al planificador cada 30 segundos. El intervalo se configura en `monedero.programadas.intervalo-ms` dentro de `application.properties`.
- El administrador las procesa en el momento con «Procesar ahora».

En cada ronda:
1. Mientras el frente de la **cola de prioridad** ya tenga fecha cumplida, se saca y se ejecuta con las mismas operaciones de siempre. Esas transacciones quedan marcadas como `AUTO` en el historial.
2. Si es recurrente (diaria, semanal o mensual) y le quedan repeticiones, vuelve a la cola con su siguiente fecha.
3. Si falla, por ejemplo por saldo insuficiente, se avisa al cliente y la recurrencia sigue.
4. Se recorre la cola desde el frente enviando recordatorios para lo que vence en 24 horas. Como está ordenada, se corta apenas aparece una fecha más lejana.

### Análisis de gastos (grafos)

Los grafos se arman en el momento a partir de las transacciones **activas**. Así el reporte nunca muestra algo que ya se revirtió.

| Grafo | Vértices → aristas | Para qué |
|---|---|---|
| Transferencias | cliente → cliente | Quién envía y quién recibe más, detección de ciclos (DFS), hasta dónde llega el dinero de alguien (BFS) |
| Hábitos | categoría → categoría | "Después de gastar en TRANSPORTE suele gastar en SERVICIOS" |
| Monederos | Ingresos / monedero → monedero / Retiros | Cómo se mueve la plata entre los monederos de un cliente (multimonedero) |

Los resultados se ordenan pasando por la `PriorityQueue`.

### Verificación de integridad (recursiva)

`VerificadorIntegridad` hace cinco revisiones. Todas avanzan **recursivamente** nodo por nodo:

1. **Cadena de huellas**: cada transacción guarda la huella de la anterior. Si alguien altera una transacción vieja, su huella deja de coincidir.
2. **Reglas de cada transacción**: monto positivo, comisión dentro del límite, puntos según el enunciado, saldo antes/después coherente.
3. **Saldos**: el saldo de cada monedero se reconstruye sumando el libro mayor y se compara con el real.
4. **Puntos**: los puntos de cada cliente se reconstruyen sumando su historial de puntos.
5. **Árbol y tabla hash**: el árbol sigue siendo un BST válido, sigue balanceado (AVL) y coincide con los puntos de cada cliente.

Las pruebas `detectaUnaTransaccionAlterada` y `detectaSaldoQueNoCuadraConElLibro` muestran que la verificación detecta manipulaciones.

---

## 6. La aplicación web: dos perspectivas

La navegación es por clic: menú superior, lista de clientes y pestañas. La información de un cliente está repartida en siete ventanas, una por pestaña, para no amontonar todo en una sola:

| Pestaña | Qué muestra |
|---|---|
| Resumen | Saldo, puntos, rango, datos del cliente y sus monederos |
| Operaciones | Crear monedero, depositar, retirar, transferir y revertir la última |
| Historial | Todas sus transacciones |
| Puntos | Canje de beneficios, beneficios canjeados y movimientos de puntos |
| Programadas | Programar una transacción y ver las pendientes |
| Notificaciones | Sus avisos (saldo bajo, recordatorios, etc.) |
| Gastos | Gasto por categoría, patrones de gasto y movimiento entre monederos (grafos) |

### Administrador (`/admin`)

| Ventana | Qué permite |
|---|---|
| **Consola** `/admin` | Lista de clientes con buscador y botón para crear uno. Sin cliente seleccionado muestra un resumen general y los clientes con más puntos. |
| **Cliente** `/admin/cliente/{id}/{pestaña}` | Las siete pestañas del cliente, más el botón para eliminarlo. |
| **Programadas** `/admin/programadas` | Todas las transacciones programadas en orden de ejecución y el botón «Procesar ahora». |
| **Reportes** `/admin/reportes` | Ranking, clientes por rango, transferencias entre clientes (con ciclos), gasto por categoría y patrones. |
| **Integridad** `/admin/integridad` | Resultado de las cinco revisiones recursivas. |

### Cliente (`/cliente`)

| Ventana | Qué permite |
|---|---|
| **Acceso** `/cliente` | Entrar con la identificación o crear cuenta. |
| **Mi monedero** `/cliente/{id}/{pestaña}` | Las mismas siete pestañas, solo con su información. |

El cliente no puede eliminar clientes ni ver la información de otros; el administrador ve y opera sobre todos. Los formularios son los mismos en las dos vistas: el campo oculto `volver` decide si se regresa a la consola o al panel del cliente, y cada operación regresa a la pestaña donde está su formulario.

### Rutas web

| Método | Ruta | Acción |
|---|---|---|
| GET | `/` | Inicio |
| GET | `/admin` | Consola |
| GET | `/admin/cliente/{id}` | Consola con un cliente (pestaña Resumen) |
| GET | `/admin/cliente/{id}/{pestaña}` | Otra pestaña del cliente |
| POST | `/admin/cliente` | Crear cliente (`id`, `nombre`, `correo`) |
| POST | `/admin/cliente/eliminar/{id}` | Eliminar cliente |
| GET | `/admin/reportes` | Reportes |
| GET | `/admin/programadas` | Agenda |
| POST | `/admin/programadas/procesar` | Ejecutar lo pendiente |
| GET | `/admin/integridad` | Verificación de integridad |
| GET | `/cliente` | Acceso del cliente |
| GET | `/cliente/perfil?id=` | Entrar |
| GET | `/cliente/registro` · POST `/cliente/guardar` | Registro |
| GET | `/cliente/{id}` | Panel del cliente (pestaña Resumen) |
| GET | `/cliente/{id}/{pestaña}` | resumen, operaciones, historial, puntos, programadas, notificaciones o gastos |
| POST | `/cliente/{id}/monedero` | Crear monedero (`nombre`) |
| POST | `/cliente/{id}/monedero/eliminar/{idMonedero}` | Eliminar monedero vacío |
| POST | `/cliente/{id}/deposito` | `idMonedero`, `monto`, `descripcion` |
| POST | `/cliente/{id}/retiro` | `idMonedero`, `monto`, `categoria`, `descripcion` |
| POST | `/cliente/{id}/transferencia` | `idMonederoOrigen`, `idMonederoDestino`, `monto`, `categoria`, `descripcion` |
| POST | `/cliente/{id}/revertir` | Revertir la última |
| POST | `/cliente/{id}/canjear` | `tipo` (y `idMonedero` para el bono) |
| POST | `/cliente/{id}/programar` | `tipo`, monederos, `monto`, `categoria`, `fecha`, `frecuencia`, `repeticiones` |
| POST | `/cliente/{id}/programada/cancelar/{idProgramada}` | Cancelar programación |
| POST | `/cliente/{id}/notificaciones/leidas` | Marcar como leídas |

Todos los POST de operaciones aceptan `volver=admin` para regresar a la consola.

---

## 7. API REST

Base: `http://localhost:8080/api`. Envía y recibe **JSON**. Las fechas usan el formato ISO `2026-11-20T09:00:00`.

### Respuestas

| Código | Cuándo |
|---|---|
| 200 | Consulta correcta |
| 201 | Se creó algo (cliente, monedero, transacción, canje, programación) |
| 204 | Se eliminó o se marcó, sin cuerpo |
| 400 | La operación no se puede hacer: `{"error": "Saldo insuficiente en «Ahorros»: necesitas ..."}` |
| 404 | El cliente no existe: `{"error": "No existe el cliente 99"}` |

### Valores permitidos

- `categoria`: `ALIMENTACION`, `TRANSPORTE`, `SERVICIOS`, `ENTRETENIMIENTO`, `SALUD`, `EDUCACION`, `AHORRO`, `OTROS` (si no se envía: `OTROS`)
- `tipo` de transacción: `DEPOSITO`, `RETIRO`, `TRANSFERENCIA`
- `frecuencia`: `UNA_VEZ`, `DIARIA`, `SEMANAL`, `MENSUAL`
- `tipo` de beneficio: `DESCUENTO_TRANSFERENCIAS`, `RETIROS_SIN_CARGO`, `BONO_SALDO`
- `rango`: `BRONCE`, `PLATA`, `ORO`, `PLATINO`

### Endpoints

| Método | Ruta | Cuerpo | Respuesta |
|---|---|---|---|
| GET | `/clientes` | | Lista de clientes |
| GET | `/clientes/{id}` | | Cliente (puntos, rango, posición, notificaciones sin leer) |
| POST | `/clientes` | `{"id", "nombre", "correo"}` | 201 + cliente |
| DELETE | `/clientes/{id}` | | 204 |
| GET | `/clientes/{id}/monederos` | | Monederos |
| POST | `/clientes/{id}/monederos` | `{"nombre"}` | 201 + monedero |
| DELETE | `/clientes/{id}/monederos/{idMonedero}` | | 204 (solo si está vacío) |
| GET | `/clientes/{id}/saldo` | | Saldo total y por monedero |
| GET | `/clientes/{id}/transacciones` | | Historial, la más reciente primero |
| POST | `/clientes/{id}/depositos` | `{"idMonedero", "monto", "descripcion"}` | 201 + transacción |
| POST | `/clientes/{id}/retiros` | `{"idMonedero", "monto", "categoria", "descripcion"}` | 201 + transacción |
| POST | `/clientes/{id}/transferencias` | `{"idMonederoOrigen", "idMonederoDestino", "monto", "categoria", "descripcion"}` | 201 + transacción |
| POST | `/clientes/{id}/revertir` | | La transacción revertida |
| GET | `/clientes/{id}/puntos` | | Cliente, ventajas del rango y movimientos de puntos |
| GET | `/beneficios` | | Catálogo |
| GET | `/clientes/{id}/beneficios` | | Beneficios canjeados |
| POST | `/clientes/{id}/canjes` | `{"tipo", "idMonedero"}` | 201 + beneficio |
| GET | `/ranking?top=10` | | Los N con más puntos |
| GET | `/rangos/{rango}` | | Clientes de ese rango |
| GET | `/clientes/{id}/programadas` | | Sus programaciones en orden de ejecución |
| POST | `/clientes/{id}/programadas` | ver ejemplo | 201 + programación |
| DELETE | `/clientes/{id}/programadas/{idProgramada}` | | La programación cancelada |
| GET | `/programadas` | | Toda la cola de prioridad |
| POST | `/programadas/procesar` | | `{"ejecutadas": n}` |
| GET | `/clientes/{id}/notificaciones` | | Notificaciones, la más reciente primero |
| POST | `/clientes/{id}/notificaciones/leidas` | | 204 |
| GET | `/clientes/{id}/analisis` | | Análisis de gastos con grafos |
| GET | `/reportes/general` | | Reporte general (transferencias, ciclos, categorías) |
| GET | `/integridad` | | Resultado de la verificación |
| GET | `/sistema` | | Estado de las estructuras |

### Ejemplos con curl

Crear un cliente:

```bash
curl -X POST http://localhost:8080/api/clientes -H "Content-Type: application/json" -d '{"id": 1001, "nombre": "Ana Ruiz", "correo": "ana@correo.com"}'
```

Crearle un monedero (la respuesta trae su `id`, por ejemplo 9):

```bash
curl -X POST http://localhost:8080/api/clientes/1001/monederos -H "Content-Type: application/json" -d '{"nombre": "Ahorros"}'
```

Depositar:

```bash
curl -X POST http://localhost:8080/api/clientes/1001/depositos -H "Content-Type: application/json" -d '{"idMonedero": 9, "monto": 20000, "descripcion": "Sueldo"}'
```

Respuesta (resumida; los números de id y la huella cambian en cada ejecución):

```json
{
  "id": 22, "tipo": "DEPOSITO", "monto": 20000.0, "comision": 0.0,
  "puntosGenerados": 200, "puntosExtra": 0, "estado": "ACTIVA",
  "idMonederoDestino": 9, "direccion": "ENTRADA", "automatica": false, "huella": "3f9c0a1b7d2e4c55"
}
```

Retirar con categoría:

```bash
curl -X POST http://localhost:8080/api/clientes/1001/retiros -H "Content-Type: application/json" -d '{"idMonedero": 9, "monto": 5000, "categoria": "ALIMENTACION", "descripcion": "Mercado"}'
```

Transferir al monedero #1 (de Juan):

```bash
curl -X POST http://localhost:8080/api/clientes/1001/transferencias -H "Content-Type: application/json" -d '{"idMonederoOrigen": 9, "idMonederoDestino": 1, "monto": 3000, "categoria": "OTROS"}'
```

Error por saldo insuficiente (400):

```json
{"error": "Saldo insuficiente en «Ahorros»: necesitas $1.004.999 ($999.999 + $5.000 de comisión) y tienes $20.000."}
```

Revertir la última transacción:

```bash
curl -X POST http://localhost:8080/api/clientes/1001/revertir
```

Canjear el bono de saldo hacia el monedero 9:

```bash
curl -X POST http://localhost:8080/api/clientes/1001/canjes -H "Content-Type: application/json" -d '{"tipo": "BONO_SALDO", "idMonedero": 9}'
```

Programar un ahorro semanal, 4 veces:

```bash
curl -X POST http://localhost:8080/api/clientes/1001/programadas -H "Content-Type: application/json" -d '{"tipo": "DEPOSITO", "idMonederoDestino": 9, "monto": 5000, "descripcion": "Ahorro semanal", "fechaEjecucion": "2026-12-01T08:00:00", "frecuencia": "SEMANAL", "repeticiones": 4}'
```

`repeticiones` vacío o 0 significa que se repite hasta que la cancelen. Para `RETIRO` se envía `idMonederoOrigen`, y para `TRANSFERENCIA` los dos monederos.

Ranking, clientes Oro, reporte general e integridad:

```bash
curl http://localhost:8080/api/ranking?top=5
```

```bash
curl http://localhost:8080/api/rangos/ORO
```

```bash
curl http://localhost:8080/api/reportes/general
```

```bash
curl http://localhost:8080/api/integridad
```

### Nota sobre JSON y las estructuras propias

Jackson no conoce nuestra `LinkedList`. `Controller/JsonConfig.java` registra un serializador que la escribe como un arreglo JSON normal, así que la API devuelve `[...]` aunque por dentro todo sea una lista enlazada propia.

---

## 8. Pruebas automáticas

```bash
./mvnw test
```

| Archivo | Qué prueba |
|---|---|
| `HashTableTest` | inserción, reemplazo, colisiones en la misma cubeta, eliminación en medio de la cadena, rehash con 1.000 claves |
| `AVLTreeTest` | balance con datos ordenados (BST vs AVL), eliminación con dos hijos, `mayores` y `rango` |
| `PriorityQueueTest` | orden, estabilidad en empates, cancelación por condición |
| `CircularListTest` | comportamiento de cola, capacidad que descarta lo más viejo, rotación |
| `GraphTest` | acumulación de aristas, BFS, detección de ciclos |
| `MonederoSystemTest` | depósitos, retiros, transferencias, comisiones, reversión LIFO, rangos |
| `FuncionalidadesAvanzadasTest` | puntos extra, ranking, canjes, programadas (orden, recurrencia, fallos, recordatorios), notificaciones, grafos e integridad (incluida la detección de manipulaciones) |

---

## 9. Cosas que se pueden ajustar

| Qué | Dónde |
|---|---|
| Porcentajes de comisión, umbral de saldo bajo, valor del bono, máximo de notificaciones | `Model/ReglasNegocio.java` |
| Límites de cada rango | `Enum/RangoCliente.java` |
| Costo y duración de los beneficios | `Enum/TipoBeneficio.java` |
| Cada cuánto se procesan las programadas | `monedero.programadas.intervalo-ms` en `application.properties` |
| Datos de ejemplo | `ModelFactory/DatosDePrueba.java` |

Los datos viven en memoria: al reiniciar la aplicación se vuelven a cargar los de ejemplo.
