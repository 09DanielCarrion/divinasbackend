# DivinasPay — Guía del backend y cómo integrar el frontend

Backend de **DivinasPay**, la cuenta corriente de crédito de *Divinas Salón & Spa* (curso SI642 Finanzas e Ingeniería Económica).
Esta guía explica cómo funciona el backend y cómo conectarlo desde el frontend.

> Para ver y probar todos los endpoints con ejemplos: `http://localhost:8080/swagger-ui.html`

---

## 1. Resumen

| Tema | Detalle |
|---|---|
| Tecnología | Java 21, Spring Boot 4.1.2, Maven, PostgreSQL 17 |
| Arquitectura | Controller → Service (interfaz + implementación) → Repository → PostgreSQL |
| Seguridad | Login con **token JWT** (HS256, dura 120 min) y tres roles: `ADMIN_SISTEMA`, `ADMIN_TIENDA` y `CLIENTE` |
| Método financiero | **Método francés vencido** (cuotas fijas), año de 360 días, mes de 30 días |
| URL base local | `http://localhost:8080` |
| Documentación viva | `http://localhost:8080/swagger-ui.html` (tiene el botón *Authorize* para pegar el token) |

### Qué hace el sistema
1. El **administrador del sistema** da de alta (y de baja) a las tiendas, con las credenciales de su administrador.
2. El **administrador de la tienda** entra al sistema y crea sus **clientes**, cada uno con su tasa de interés, límite de crédito, máximo de meses y fechas de corte y pago.
3. El administrador crea el **catálogo** de productos y servicios (precio al contado y precio de lista).
4. Se registra una **compra** a crédito: el sistema calcula el plan de pagos (cuotas, intereses, TIR y TCEA) y guarda las cuotas.
5. El administrador **registra los pagos** de las cuotas; el cliente consulta sus compras, cuotas y saldo.

---

## 2. Cómo levantar el backend

1. Instalar **Java 21** y **PostgreSQL**.
2. Crear la base: `CREATE DATABASE divinasdb;`
3. Conectarse a `divinasdb` y ejecutar el script `database/crear_base_divinasdb.sql`.
4. Crear un archivo `.env` en la carpeta del proyecto (no se sube a GitHub). Hay una plantilla en `.env.example`:
   ```
   DB_USER=tu_usuario_de_postgres
   DB_PASSWORD=tu_contraseña_de_postgres
   JWT_SECRET=una_frase_larga_y_aleatoria_de_al_menos_32_caracteres
   SISTEMA_USUARIO=usuario_del_administrador_del_sistema
   SISTEMA_PASSWORD=contraseña_del_administrador_del_sistema
   ```
   `SISTEMA_USUARIO` y `SISTEMA_PASSWORD` son las credenciales del **administrador del sistema**, que no se guardan en la base de datos. Si faltan, ese login queda deshabilitado.
5. Ejecutar: `./mvnw spring-boot:run` (o el botón Run de IntelliJ). El servidor queda en el puerto **8080**.
6. Pruebas automáticas del motor financiero: `./mvnw test`.

> La aplicación usa `ddl-auto=validate`: **no crea tablas**, solo comprueba que coincidan con el script SQL. Si falla al arrancar, revisa que ejecutaste el script.

El frontend puede correr en `http://localhost:3000` (React), `http://localhost:5173` (Vite) o `http://localhost:4200` (Angular): esos orígenes ya están permitidos por CORS. Si usan otro puerto, hay que agregarlo en `config/CorsConfig.java`.

---

## 3. Convenciones de la API

- **Formato:** JSON (`Content-Type: application/json`). Los nombres de los campos van en `camelCase`.
- **Fechas:** formato ISO `yyyy-MM-dd` (por ejemplo `"2026-10-15"`). El frontend debe mostrarlas como `dd/mm/aaaa`.
- **Dinero:** número con 2 decimales (por ejemplo `1008.05`). Siempre en soles.
- **Tasas:**
  - Al **enviar** una tasa (cliente), va en porcentaje: `30` significa 30 %.
  - Al **recibir** TEA, TEM, TIR y TCEA, vienen en **tanto por uno** con 10 decimales: `0.3645975747` significa 36.45976 %. Para mostrarlas, multiplicar por 100.
- **Ids:** enteros. Las rutas anidan la tienda y el cliente: `/api/tiendas/{idTienda}/clientes/{idCliente}/...`
- **Baja lógica:** los `DELETE` no borran: ponen `estado = false` y responden `204 No Content`.
- **Autorización:** todas las rutas, salvo las públicas, requieren el encabezado `Authorization: Bearer <token>`.

### Formato de los errores
Todos los errores usan la misma estructura:
```json
{
  "codigo": "VALIDACION",
  "mensaje": "Hay campos inválidos.",
  "detalles": { "telefono": "El teléfono debe tener exactamente 9 dígitos." }
}
```
`detalles` solo trae contenido en errores de validación (campo → mensaje); en los demás va vacío.

| HTTP | `codigo` | Cuándo ocurre |
|---|---|---|
| 400 | `VALIDACION` | Algún campo no cumple las reglas (mira `detalles`) |
| 400 | `SOLICITUD_INVALIDA` | Regla de negocio (límite de crédito superado, tienda dada de baja, etc.) |
| 400 | `JSON_INVALIDO` | El cuerpo no es un JSON válido |
| 401 | `CREDENCIALES_INVALIDAS` | Usuario o contraseña incorrectos en el login |
| 401 | `NO_AUTENTICADO` | Falta el token, es inválido o venció |
| 403 | `SIN_PERMISO` | El token es válido pero no puede acceder a ese recurso |
| 404 | `NO_ENCONTRADO` | El recurso no existe (o no pertenece a esa tienda o cliente) |
| 409 | `CONFLICTO` | Dato repetido, o una operación que no se puede hacer en ese estado |
| 500 | `ERROR_INTERNO` | Error inesperado del servidor |

---

## 4. Autenticación y roles

### 4.1 Cómo funciona
1. El usuario hace login y recibe un **token**.
2. El frontend guarda el token y lo envía en cada petición.
3. Dentro del token, firmado por el servidor, van el **rol**, el `idTienda` y, si es cliente, el `idCliente`. El servidor los compara con la ruta pedida: un cliente no puede ver datos de otro cambiando el número en la URL (recibe `403`).

### 4.2 Login — `POST /api/auth/login` (público)

Administrador del sistema (sus datos están en el `.env`):
```json
{ "tipo": "SISTEMA", "usuario": "...", "password": "..." }
```

Administrador de tienda:
```json
{ "tipo": "ADMIN", "usuario": "admin_divinas", "password": "Clave12345" }
```
Cliente (**obligatorio** enviar `idTienda`, porque un mismo usuario puede existir en dos tiendas):
```json
{ "tipo": "CLIENTE", "idTienda": 1, "usuario": "carla_sisoy", "password": "Clave12345" }
```
Respuesta `200`:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipoToken": "Bearer",
  "rol": "CLIENTE",
  "idTienda": 1,
  "idCliente": 1,
  "nombre": "Carla",
  "expiraEnSegundos": 7200
}
```
Para el administrador de tienda, `idCliente` es `null`; para el administrador del sistema, `idTienda` e `idCliente` son `null`. Una tienda o un cliente dados de baja no pueden entrar (`401`, con el mismo mensaje que una clave incorrecta).

### 4.3 Lista de tiendas — `GET /api/auth/tiendas` (público)
Devuelve solo `idTienda` y `nombreTienda` de las tiendas activas. Sirve para que **el cliente elija su tienda antes de iniciar sesión**.

### 4.4 Qué puede hacer cada rol

| Operación | Sin sesión | `CLIENTE` | `ADMIN_TIENDA` (de su tienda) | `ADMIN_SISTEMA` |
|---|:-:|:-:|:-:|:-:|
| Login, lista de tiendas (`/api/auth/tiendas`), Swagger | ✅ | ✅ | ✅ | ✅ |
| Alta de tienda, lista completa y baja de tienda | ❌ | ❌ | ❌ | ✅ |
| Ver o modificar los datos de una tienda | ❌ | ✅ solo ver (la suya) | ✅ la suya | ✅ cualquiera |
| Sus compras, pagos y perfil (GET) | ❌ | ✅ solo los suyos | ✅ | ❌ |
| Simular una compra | ❌ | ✅ solo la suya | ✅ | ❌ |
| Ver catálogo de su tienda (GET) | ❌ | ✅ | ✅ | ❌ |
| Crear, editar o dar de baja clientes y productos | ❌ | ❌ | ✅ | ❌ |
| Registrar compras y pagar cuotas | ❌ | ❌ | ✅ | ❌ |
| Resumen de la tienda (dashboard) | ❌ | ❌ | ✅ | ❌ |
| Listado de pago en la fecha de corte | ❌ | ✅ solo el suyo | ✅ | ❌ |
| Registro de operaciones de su tienda | ❌ | ❌ | ✅ | ❌ |
| Registro de operaciones de todas las tiendas | ❌ | ❌ | ❌ | ✅ |
| Datos de **otra** tienda u **otro** cliente | ❌ | ❌ | ❌ | ❌ |

> El administrador del sistema solo gestiona las **tiendas**: no ve clientes, productos ni compras.

---

## 5. Referencia de endpoints

`{t}` = `idTienda`, `{c}` = `idCliente`. "Admin" = rol `ADMIN_TIENDA` de esa tienda.

### 5.1 Tiendas
| Método y ruta | Quién | Descripción |
|---|---|---|
| `POST /api/tiendas` | Administrador del sistema | Dar de alta una tienda con las credenciales de su administrador |
| `GET /api/tiendas` | Administrador del sistema | Listar todas las tiendas |
| `GET /api/tiendas/{t}` | Sistema, admin de la tienda o cliente de la tienda | Datos de la tienda |
| `PUT /api/tiendas/{t}` | Sistema o admin de la tienda | Modificar tienda |
| `DELETE /api/tiendas/{t}` | Administrador del sistema | Dar de baja (204) |

Cuerpo de `POST` (la contraseña tiene entre 8 y 72 caracteres):
```json
{
  "nombreTienda": "Divinas Salón & Spa",
  "direccion": "Av. Surco 1167",
  "telefono": "955988231",
  "usuarioAdmin": "admin_divinas",
  "passwordAdmin": "Clave12345"
}
```
- `telefono`: exactamente 9 dígitos. `nombreTienda` y `usuarioAdmin` son únicos (si se repiten: `409`).
- `PUT` lleva los mismos campos, **sin** `passwordAdmin`.
- Respuesta: `idTienda`, `nombreTienda`, `direccion`, `telefono`, `usuarioAdmin`, `estado`. Nunca devuelve contraseñas.

### 5.2 Clientes
| Método y ruta | Quién | Descripción |
|---|---|---|
| `POST /api/tiendas/{t}/clientes` | Admin | Crear cliente |
| `GET /api/tiendas/{t}/clientes` | Admin | Listar clientes de la tienda |
| `GET /api/tiendas/{t}/clientes/{c}` | Admin o ese cliente | Perfil |
| `PUT /api/tiendas/{t}/clientes/{c}` | Admin | Modificar |
| `DELETE /api/tiendas/{t}/clientes/{c}` | Admin | Dar de baja (204) |

Cuerpo de `POST`:
```json
{
  "nombreCliente": "Carla",
  "tipoTasa": "Efectiva",
  "tasaInteres": 30,
  "capitalizacion": null,
  "moneda": "Soles",
  "limiteMaxCredito": 1500,
  "maxMesesCredito": 7,
  "fechaCorte": 20,
  "fechaPago": 26,
  "usuarioCliente": "carla_sisoy",
  "passwordCliente": "Clave12345"
}
```
Reglas:
- `tipoTasa`: `"Efectiva"` o `"Nominal"`. Si es `"Nominal"`, `capitalizacion` es **obligatoria**: `Diaria`, `Mensual`, `Bimestral`, `Trimestral`, `Semestral` o `Anual`. Si es `"Efectiva"`, se ignora.
- `tasaInteres`: mayor que 0 y hasta 200, con máximo 4 decimales.
- `moneda`: solo `"Soles"`. `fechaCorte` y `fechaPago`: de 1 a 31. `passwordCliente`: de 8 a 72 caracteres.
- `usuarioCliente` es único **dentro de la tienda**.
- `PUT` lleva los mismos campos, **sin** `passwordCliente` (no existe endpoint para cambiar contraseñas).
- ⚠️ **La tasa, la capitalización y las fechas de corte y pago no se pueden modificar.** Si se envía un valor distinto, responde `409`. Reenviar los mismos valores sí es válido, así que un formulario de edición puede mandar todos los campos. Esto garantiza que el cronograma de una compra antigua no cambie.

### 5.3 Productos y servicios
| Método y ruta | Quién | Descripción |
|---|---|---|
| `POST /api/tiendas/{t}/productos` | Admin | Crear |
| `GET /api/tiendas/{t}/productos?soloActivos=true` | Admin o cliente de la tienda | Listar (para el catálogo del cliente usar `soloActivos=true`) |
| `GET /api/tiendas/{t}/productos/{p}` | Admin o cliente de la tienda | Detalle |
| `PUT /api/tiendas/{t}/productos/{p}` | Admin | Modificar |
| `DELETE /api/tiendas/{t}/productos/{p}` | Admin | Dar de baja (204) |

```json
{
  "imagen": "https://...",
  "proveedor": "no aplica",
  "marca": "no aplica",
  "descripcion": "Laceado",
  "unidadMedida": "Servicio",
  "precioContado": 900,
  "precioCredito": 1000,
  "formaVenta": "En meses"
}
```
- `descripcion`, `unidadMedida`, `precioContado` y `precioCredito` son obligatorios.
- **`precioContado`** es el precio si se paga en efectivo. **`precioCredito`** es el **precio de lista**, con el recargo por vender a crédito, y debe ser **mayor** que el precio al contado (si no: `400`). El precio de lista es el **monto que se financia** en una compra.
- `formaVenta`: `"En meses"` (por defecto) o `"Al contado"`. Un producto `"En meses"` se puede comprar en cuotas o en un solo pago al fin de mes. Un producto `"Al contado"` solo se vende en **un solo pago** (`"Unico pago"`): pedirlo en meses da `400`.
- El costo real de una compra a crédito, con intereses, es el campo `totalCuotas` de la respuesta de la compra.

### 5.4 Simular una compra — `POST /api/tiendas/{t}/clientes/{c}/compras/simular`
Calcula el plan de pagos **sin guardar nada**. Úsalo para la pantalla "¿cuánto pagaré?".
```json
{ "fechaCompra": "2026-10-15", "monto": 1000, "formaPago": "En meses", "numMeses": 3 }
```
- `fechaCompra` es opcional (por defecto, hoy). `formaPago`: `"En meses"` o `"Unico pago"`. `numMeses` es obligatorio solo si es `"En meses"`.
- Se rechaza con `400` si el monto supera el **crédito disponible** del cliente (su límite menos lo que aún debe en cuotas pendientes) o si los meses superan su máximo.

### 5.5 Registrar una compra — `POST /api/tiendas/{t}/clientes/{c}/compras` (solo admin)
```json
{ "idProductoServicio": 1, "fechaCompra": "2026-10-15", "formaPago": "En meses", "numMeses": 3 }
```
- El **monto no se envía**: sale del `precioCredito` (precio de lista) del producto.
- Valida: que el producto sea de esa tienda y esté activo, que su forma de venta permita la forma de pago pedida, y el crédito disponible y los meses máximos del cliente.
- Responde `201` con la compra y su plan de pagos (ver sección 6).

### 5.6 Consultar compras
| Método y ruta | Quién | Descripción |
|---|---|---|
| `GET /api/tiendas/{t}/clientes/{c}/compras` | Admin o ese cliente | Compras del cliente, cada una con su plan |
| `GET /api/tiendas/{t}/clientes/{c}/compras/{id}` | Admin o ese cliente | Una compra |

### 5.7 Pagar una cuota — `PUT /api/tiendas/{t}/clientes/{c}/compras/{idCompra}/cuotas/{numeroCuota}/pagar` (solo admin)
Cuerpo **opcional**: `{ "fechaPago": "2026-11-20" }` (por defecto, hoy). Responde `200` con la compra actualizada.
- Las cuotas se pagan **en orden**: pagar la cuota 2 sin la 1 da `409`. Pagar una cuota ya pagada también da `409`.
- La fecha de pago no puede ser anterior a la fecha de la compra (`400`).

### 5.8 Listado de pagos del cliente — `GET /api/tiendas/{t}/clientes/{c}/pagos` (admin o ese cliente)
```json
{
  "idCliente": 1,
  "nombreCliente": "Carla",
  "limiteMaxCredito": 1500.0,
  "limiteDisponible": 798.04,
  "saldoPendienteTotal": 701.96,
  "comprasEnCurso": 1,
  "compras": [
    {
      "idCompra": 1,
      "descripcionProducto": "Laceado",
      "fechaCompra": "2026-10-15",
      "monto": 1000.0,
      "formaPago": "En meses",
      "cuotasPagadas": 1,
      "cuotasTotales": 3,
      "cuotaAPagar": 350.98,
      "fechaProximoPago": "2026-12-26",
      "saldoPendiente": 701.96
    }
  ]
}
```
`cuotaAPagar` y `fechaProximoPago` son `null` cuando la compra ya está pagada por completo. `saldoPendiente` es la suma de las cuotas aún no pagadas. `limiteDisponible` es el límite del cliente menos su saldo pendiente total: es lo máximo que puede comprar hoy.

### 5.9 Resumen de la tienda (dashboard) — `GET /api/tiendas/{t}/resumen` (solo admin)
```json
{
  "totalClientesActivos": 3,
  "totalServiciosRegistrados": 5,
  "comprasEnCurso": 2,
  "proximosVencimientos": [
    { "idCompra": 1, "idCliente": 1, "nombreCliente": "Carla", "numeroCuota": 2,
      "fechaVencimiento": "2026-12-26", "montoCuota": 350.98 }
  ]
}
```
`proximosVencimientos` trae las 10 próximas cuotas pendientes de toda la tienda, ordenadas por fecha. `comprasEnCurso` cuenta las compras con al menos una cuota pendiente.

### 5.10 Listado de pago en la fecha de corte
Es el listado de lo que el cliente debe pagar en una fecha de pago: las compras ordenadas por fecha, con los intereses acumulados de cada una.

| Método y ruta | Quién | Descripción |
|---|---|---|
| `GET /api/tiendas/{t}/clientes/{c}/listado-pago?fechaPago=2026-11-26` | Admin o ese cliente | Listado de un cliente |
| `GET /api/tiendas/{t}/listado-pago?fechaPago=2026-11-26` | Admin | Listados de todos los clientes de la tienda con cuotas ese día |

`fechaPago` es opcional: por defecto se usa la **próxima fecha de pago** (el vencimiento pendiente más cercano). El listado incluye las cuotas **pendientes** que vencen ese día.
```json
{
  "idCliente": 1,
  "nombreCliente": "Carla",
  "fechaCorte": 20,
  "fechaPago": "2026-11-26",
  "totalInteresesAcumulados": 30.33,
  "totalAPagar": 350.98,
  "detalle": [
    {
      "idCompra": 1,
      "descripcionProducto": "Laceado",
      "fechaCompra": "2026-10-15",
      "montoCompra": 1000.0,
      "formaPago": "En meses",
      "numeroCuota": 1,
      "cuotasTotales": 3,
      "diasGracia": 11,
      "interesGracia": 8.05,
      "interesCuota": 22.28,
      "montoCuota": 350.98
    }
  ]
}
```
- `detalle` va ordenado por **fecha de compra**.
- `interesGracia` y `diasGracia` son los de los días entre la compra y la fecha de pago; solo aparecen en la **primera cuota** de cada compra (en las demás valen 0).
- `interesCuota` es el interés propio de esa cuota (en un pago único es 0).
- `totalInteresesAcumulados` = suma de `interesGracia` + `interesCuota`. `totalAPagar` = suma de `montoCuota`.
- Si no hay cuotas pendientes, `fechaPago` es `null` y `detalle` viene vacío.
- No incluye intereses por mora (la mora no está implementada).

### 5.11 Registro de operaciones (bitácora)
El sistema guarda automáticamente quién hizo cada operación que modifica datos y cada inicio de sesión.

| Método y ruta | Quién | Descripción |
|---|---|---|
| `GET /api/tiendas/{t}/operaciones?limite=100` | Admin de la tienda | Operaciones de su tienda, las más recientes primero |
| `GET /api/operaciones?idTienda=1&limite=100` | Administrador del sistema | Operaciones de todas las tiendas (`idTienda` es opcional) |

`limite` va de 1 a 500 (por defecto 100).
```json
{
  "idOperacion": 12,
  "fechaHora": "2026-11-04T16:41:07.523",
  "usuario": "admin_divinas",
  "rol": "ADMIN_TIENDA",
  "idTienda": 1,
  "accion": "Registro de compra",
  "metodo": "POST",
  "ruta": "/api/tiendas/1/clientes/1/compras",
  "codigoHttp": 201
}
```
Se registran: inicio de sesión (correcto y fallido), alta, modificación y baja de tiendas, clientes y productos, registro de compras y pago de cuotas. Cada registro lleva el usuario, su rol, la tienda, la fecha y hora, la ruta y el código de respuesta (por eso también quedan las operaciones rechazadas por reglas de negocio, como un `409`).
**No se registran:** las consultas (GET), las simulaciones, ni los intentos bloqueados antes de llegar al sistema (sin token o sin permiso). Nunca se guardan contraseñas ni el contenido de las peticiones.

---

## 6. La respuesta de una compra (registro y consulta)

```json
{
  "idCompra": 1,
  "idCliente": 1,
  "idProductoServicio": 1,
  "descripcionProducto": "Laceado",
  "fechaCompra": "2026-10-15",
  "monto": 1000.0,
  "formaPago": "En meses",
  "numMeses": 3,
  "fechaFinGracia": "2026-10-26",
  "diasGracia": 11,
  "tasaEfectivaAnual": 0.3,
  "tasaEfectivaMensual": 0.0221044506,
  "capitalCapitalizado": 1008.05,
  "interesGracia": 8.05,
  "cuotaFija": 350.98,
  "cuotas": [
    { "idCuota": 1, "numero": 1, "fechaVencimiento": "2026-11-26", "saldoInicial": 1008.05,
      "cuota": 350.98, "interes": 22.28, "amortizacion": 328.7, "saldoFinal": 679.35,
      "estado": "Pendiente", "fechaPagoReal": null }
  ],
  "totalCuotas": 1052.94,
  "totalIntereses": 44.89,
  "tir": 0.0262434138,
  "tcea": 0.3645975747
}
```

Cómo leerla (para mostrarla en las pantallas):

| Campo | Qué es |
|---|---|
| `monto` | Precio de lista (crédito) del producto: el monto que se financia |
| `fechaFinGracia`, `diasGracia` | Desde la compra hasta la primera fecha de pago, según la fecha de corte del cliente |
| `capitalCapitalizado` | `monto` + intereses de los días de gracia: la deuda real al inicio del cronograma |
| `interesGracia` | `capitalCapitalizado − monto` |
| `cuotas[].estado` | `"Pendiente"` o `"Pagado"` |
| `totalCuotas` | Todo lo que paga el cliente en cuotas (capital más intereses) |
| `totalIntereses` | Suma de los intereses de las cuotas (sin el de gracia) |
| `tir` | Tasa mensual real del crédito. `null` en pago único |
| `tcea` | Costo efectivo anual. En pago único es igual a la TEA |

Interés total pagado por el cliente = `totalCuotas − monto` (en el ejemplo, 52.94 = 8.05 de gracia + 44.89 de cuotas).

**Pago único:** una sola cuota que vence en `fechaFinGracia`, por el `capitalCapitalizado`. No hay `tir`.

---

## 7. Reglas financieras (para que el frontend entienda los números)

1. **Año de 360 días y mes de 30 días.**
2. **TEA:** si la tasa del cliente es *Efectiva*, la TEA es esa tasa. Si es *Nominal*: `TEA = (1 + TN/m)^m − 1`, con `m` = 360 (Diaria), 12 (Mensual), 6 (Bimestral), 4 (Trimestral), 2 (Semestral) o 1 (Anual).
3. **TEM** (tasa mensual): `(1 + TEA)^(30/360) − 1`.
4. **Fin de la gracia:** si el día de la compra es ≤ la fecha de corte, se paga el día `fechaPago` de **ese mes**; si no, del **mes siguiente**. Si el mes no tiene ese día (por ejemplo, 31 en abril), se usa el último día del mes.
5. **Capital capitalizado:** `monto × (1 + TEA)^(díasGracia / 360)`.
6. **Cuota fija (método francés):** `C × TEM × (1+TEM)^n / ((1+TEM)^n − 1)`. La primera cuota vence **un mes después** del fin de la gracia, y las demás cada mes.
7. **Cada fila:** interés = saldo inicial × TEM; amortización = cuota − interés. En la **última** cuota la amortización es el saldo restante, así que puede diferir un centavo de la cuota fija y el saldo final queda en `0.00`.
8. **TIR:** la tasa mensual que iguala el `monto` con el valor presente de las cuotas. **TCEA** = `(1 + TIR)^12 − 1`. Los montos se redondean a 2 decimales y las tasas no se redondean al calcular.

---

## 8. Guía para el frontend

### 8.1 Flujo por pantalla (según el diseño de Figma)

| Pantalla | Quién | Endpoints |
|---|---|---|
| **Alta de tiendas** (el *Register* del diseño) | Administrador del sistema | `POST /api/tiendas`, `GET /api/tiendas`, `DELETE /api/tiendas/{t}` |
| **Login** | Público | Cliente: `GET /api/auth/tiendas` para elegir tienda, luego `POST /api/auth/login`. Administradores: `POST /api/auth/login` |
| **Dashboard administrativo** | Admin | `GET /api/tiendas/{t}/resumen`, `GET .../clientes`, `GET .../productos` |
| **Gestión de clientes** | Admin | `POST`, `PUT`, `DELETE` sobre `.../clientes` |
| **Catálogo / productos** | Admin y cliente | `GET .../productos?soloActivos=true`; el admin además crea y edita |
| **Panel detalle de crédito (administrador)** | Admin | `GET .../clientes/{c}/compras`, `GET .../pagos`, `POST .../compras`, `PUT .../cuotas/{n}/pagar` |
| **Panel detalle de crédito (cliente)** | Cliente | `GET .../compras`, `GET .../pagos` con su propio `idCliente` |
| **Simulador "¿cuánto pagaré?"** | Admin y cliente | `POST .../compras/simular` |
| **Perfil** | Admin y cliente | Cliente: `GET .../clientes/{c}`. Admin: `GET /api/tiendas/{t}` |

> Según el enunciado, las tiendas las da de alta el **administrador del sistema**, y los clientes los crea el administrador de cada tienda desde su dashboard. El backend no tiene registro público de tiendas ni de clientes. Si el diseño de Figma muestra un *Register* público, hay que ajustarlo.

### 8.2 Guardar el token y enviarlo (ejemplo con axios)

```js
import axios from 'axios';

const api = axios.create({ baseURL: 'http://localhost:8080' });

// Agrega el token a cada petición
api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Si el token venció o falta, volver al login (sin confundirlo con una clave incorrecta)
api.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error.response?.data?.codigo === 'NO_AUTENTICADO') {
      sessionStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export async function login(datos) {
  const { data } = await api.post('/api/auth/login', datos);
  sessionStorage.setItem('token', data.token);
  sessionStorage.setItem('sesion', JSON.stringify({
    rol: data.rol, idTienda: data.idTienda, idCliente: data.idCliente, nombre: data.nombre,
  }));
  return data;
}
```

Recomendaciones:
- Guardar la sesión que devuelve el login (`rol`, `idTienda`, `idCliente`) y usarla para armar las rutas: no hace falta decodificar el token.
- `sessionStorage` se borra al cerrar la pestaña; `localStorage` se mantiene. Cualquiera de los dos sirve para este proyecto.
- Redirigir según `rol`: `ADMIN_TIENDA` al dashboard, `CLIENTE` a su panel de crédito.
- **Cerrar sesión** = borrar el token del navegador (el backend no guarda sesiones).
- El token dura **120 minutos** y no hay renovación: al vencer, el usuario debe entrar de nuevo.

### 8.3 Mostrar los errores
- `VALIDACION`: recorrer `error.response.data.detalles` (objeto campo → mensaje) y mostrar cada mensaje junto a su campo del formulario.
- Los demás errores traen un `mensaje` listo para mostrar al usuario.
- `403 SIN_PERMISO`: la pantalla no debería ofrecer esa acción a ese rol; si ocurre, mostrar el mensaje.

### 8.4 Formato de números y fechas

```js
const soles = (n) => new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' }).format(n);
const porcentaje = (t) => (t * 100).toFixed(4) + ' %';       // 0.3645975747 → "36.4598 %"
const fecha = (iso) => iso.split('-').reverse().join('/');    // "2026-10-15" → "15/10/2026"
```
Para enviar un monto, usar un número (`1000`), no un texto con símbolo ni comas. Las tasas del cliente se envían en porcentaje (`30`).

### 8.5 Detalles a tener en cuenta
- **Selector de tienda en el login del cliente:** cargar `GET /api/auth/tiendas` antes de mostrar el formulario.
- **Formulario de edición de cliente:** mostrar tasa, capitalización y fechas como **solo lectura**; el backend las rechaza si cambian.
- **Registrar una compra:** el administrador elige el producto (no escribe el monto). Antes de confirmar, se puede llamar a `simular` con el `precioCredito` del producto para mostrar el cronograma. Se registra con `POST .../compras`.
- **Pagos:** el botón "Pagar" solo debe habilitarse en la **primera cuota pendiente**; el backend rechaza las demás con `409`.
- **Compras vacías:** un cliente sin compras recibe una lista vacía y `saldoPendienteTotal` en `0.00`.

---

## 9. Datos de prueba con resultados esperados

Para reproducir las pruebas, crear el producto con un `precioCredito` igual al monto de la prueba y un `precioContado` menor (por ejemplo, contado 900 y crédito 1000 para la Prueba 1), porque el monto que se financia es el precio de lista.

Cliente de los ejemplos: tasa **Efectiva 30 %**, fecha de corte **20**, fecha de pago **26**, límite 1500, máximo 7 meses.

| Prueba | Entrada | Resultado esperado |
|---|---|---|
| **1** | monto 1000, compra 15/10/2026, 3 meses | Gracia 11 días (fin 26/10/2026), capital **1008.05**, cuota **350.98**, total **1052.94**, intereses 44.89, TIR **0.0262434**, TCEA **0.3645976** |
| **2** | monto 500, compra 05/10/2026, 2 meses | Gracia 21 días, capital **507.71**, cuota **262.30**, total **524.60**, TIR **0.0326254**, TCEA **0.4699883** |
| **Pago único** | monto 1000, compra 15/10/2026 | Una cuota de **1008.05**, `tir` nulo, TCEA **0.3** |
| **Sin gracia** | monto 300, 3 meses, cliente con corte 26 y pago 26, compra el día 26 (0 días de gracia) | Cuotas 104.45, 104.45, 104.46; TCEA **0.3000116** |
| **Nominal** | Nominal 30 %, capitalización Mensual | TEA **0.3448888**, TEM **0.0250000** |

Tabla de la Prueba 1:

| N.° | Vence | Saldo inicial | Cuota | Interés | Amortización | Saldo final |
|---|---|---|---|---|---|---|
| 1 | 26/11/2026 | 1008.05 | 350.98 | 22.28 | 328.70 | 679.35 |
| 2 | 26/12/2026 | 679.35 | 350.98 | 15.02 | 335.96 | 343.39 |
| 3 | 26/01/2027 | 343.39 | 350.98 | 7.59 | 343.39 | 0.00 |

---

## 10. Estructura del proyecto

```
src/main/java/com/upc/divinasbackend/
├── controllers/        Endpoints REST
├── serviceinterfaces/  Interfaces de los servicios
├── ServiceImplements/  Lógica de negocio
├── repositories/       Acceso a la base de datos (Spring Data JPA)
├── entities/           Tablas: Tienda, Cliente, ProductoServicio, Compra, Cuota
├── dtos/               Objetos de entrada y salida de la API
├── finanzas/           CalculadoraFinanciera (el motor de cálculo), PlanDePagos, FilaCuota
├── config/             Seguridad (JWT), CORS, Swagger, cifrado de contraseñas
└── exceptions/         Errores y manejador global
database/crear_base_divinasdb.sql   Script de la base de datos
```

## 11. Límites conocidos
- **Mora** (interés moratorio) y orden de imputación de pagos: no implementados. En clase se indicó que la mora no es obligatoria, aunque el enunciado la describe en los puntos 5 y 6 y en el Anexo A.
- **Crédito disponible:** se calcula como el límite menos la suma de las cuotas pendientes (capital e intereses). La compra que se está registrando se compara por su monto financiado.
- **Listado de pago:** se genera cuando se consulta (no hay un proceso automático que lo emita el día del corte). Solo incluye cuotas pendientes con vencimiento en la fecha pedida.
- **Bitácora:** no guarda las consultas (GET) ni los intentos bloqueados por falta de token o de permiso.
- No hay renovación de token ni cambio de contraseña, y no hay comisiones ni seguros: la TCEA considera solo el interés y el período de gracia.
