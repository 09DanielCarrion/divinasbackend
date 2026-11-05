# Errores del informe por corregir (DivinasPay)

Revisión del informe `TP_FINANZAS_G4.pdf` (38 páginas) contra el backend implementado y probado.
Las páginas citadas son las del PDF. Los números se comprobaron con cálculo independiente y coinciden con las 32 pruebas automáticas del motor financiero.

> **Alcance:** esta revisión compara el informe del grupo con el sistema construido y con el **enunciado del trabajo final** (`InformarcionGeneral/SI642_Enunciado del Trabajo Final 2026-20 (1).pdf`). El enunciado **no menciona** TREA, duración, convexidad ni precio del bono: ver el punto 6. Las diferencias entre el sistema y el enunciado están en el punto 12.

## Resumen

| # | Problema | Gravedad | Páginas |
|---|---|---|---|
| 1 | Tabla de la Prueba 1 con fechas corridas y un interés de gracia incorrecto | Alta | 25–26 |
| 2 | Tabla de la Prueba 2 copiada de la Prueba 1 (valores y resultados incorrectos) | Alta | 27–28 |
| 3 | Mezcla de **método francés** con **método americano (bullet)** | Alta | 14, 15, 34, 36–37 |
| 4 | Ejemplo 5.2.1 con saldo final de 0.01 y total de 313.35 | Media | 12 |
| 5 | Afirmación falsa sobre TCEA = TEA | Media | 13 |
| 6 | Secciones que dicen que el sistema calcula cosas que no calcula | Media | 2, 13–14 |
| 7 | Datos de entrada que no coinciden con el sistema | Media | 2, 15, 16–19, 24–28 |
| 8 | Pseudocódigo incompleto | Media | 28–29 |
| 9 | Script SQL de la sección 8 desactualizado | Alta | 31–34 |
| 10 | Faltan seguridad y API en el informe | Media | — |
| 11 | Tabla de uso de IA y bibliografía | **Alta** (el enunciado castiga la omisión con nota 0) | 35, 38 |
| 12 | Diferencias entre el sistema y el enunciado | Alta | — |

---

## 1. Prueba 1 (págs. 25–26)

Los **totales están bien** y no hay que tocarlos: cuotas 1,052.94, intereses 44.89, amortización 1,008.05, interés compensatorio 52.94, TIR 2.6243 % mensual, TCEA 36.4598 %.

Lo que está mal en la tabla:

| Error | Dice | Debe decir |
|---|---|---|
| Fecha de compra (período 0) | 15/09/2026 | **15/10/2026** |
| Interés de los días de gracia | S/ 7.71 | **S/ 8.05** (1,000.00 → 1,008.05). El 7.71 pertenece a la Prueba 2 |
| Fecha de la cuota 1 | 26/10/2026 | **26/11/2026** |
| Fecha de la cuota 2 | 26/11/2026 | **26/12/2026** |
| Fecha de la cuota 3 | 26/12/2026 | **26/01/2027** |

La primera cuota vence **un mes después** del fin de la gracia, porque paga el interés del período 26/10 → 26/11. El propio pseudocódigo lo dice (`fecha_fin_gracia + mes`, con `mes` desde 1). Además, conviene indicar que los días de gracia son **11**.

**Tabla corregida:**

| Período | Fecha de pago | Saldo inicial | Cuota fija | Interés | Amortización | Saldo final |
|---|---|---|---|---|---|---|
| 0 | 15/10/2026 (compra) | 1,000.00 | – | – | – | 1,000.00 |
| Gracia (11 días) | 26/10/2026 | 1,000.00 | – | 8.05 | – | 1,008.05 |
| 1 | 26/11/2026 | 1,008.05 | 350.98 | 22.28 | 328.70 | 679.35 |
| 2 | 26/12/2026 | 679.35 | 350.98 | 15.02 | 335.96 | 343.39 |
| 3 | 26/01/2027 | 343.39 | 350.98 | 7.59 | 343.39 | 0.00 |
| **Total** | | | **1,052.94** | **44.89** | **1,008.05** | |

Otros ajustes en los datos de esta prueba:
- `tipo_tasa = TEA` → debe ser **`Efectiva`** (la tabla 6.1 solo admite `Nominal` o `Efectiva`).
- `usuario_login` → debe llamarse **`usuario_cliente`** (como en 6.1 y en el SQL).
- `tasa_interes_moratorio 15 %` no existe en el modelo de datos ni en el sistema (la mora no se implementó, ver punto 6). Quitarla o aclarar que no se usa.
- `precio_credito S/ 1,052.94` y `monto S/ 1,000.00`: el enunciado pide el **precio de lista (crédito)** en el producto, y el sistema financia ese precio. Entonces `monto` debe ser igual a `precio_credito`. O se define el producto con `precio_contado` menor y `precio_credito = 1,000.00` (monto de la prueba), o se acepta que el monto sería 1,052.94 y todos los resultados cambian. Ver el punto 7. El valor 1,052.94 es el **total de cuotas** que resulta de la compra.

## 2. Prueba 2 (págs. 27–28)

La tabla de amortización quedó **copiada de la Prueba 1**: tiene saldos de 679.35 y 343.39 que no corresponden a una compra de 500, y los resultados de salida (interés compensatorio, TIR y TCEA) también son los de la Prueba 1.

Lo que está bien: 21 días de gracia, interés de gracia 7.71 y capital capitalizado 507.71.

**Tabla corregida:**

| Período | Fecha de pago | Saldo inicial | Cuota fija | Interés | Amortización | Saldo final |
|---|---|---|---|---|---|---|
| 0 | 05/10/2026 (compra) | 500.00 | – | – | – | 500.00 |
| Gracia (21 días) | 26/10/2026 | 500.00 | – | 7.71 | – | 507.71 |
| 1 | 26/11/2026 | 507.71 | 262.30 | 11.22 | 251.08 | 256.63 |
| 2 | 26/12/2026 | 256.63 | 262.30 | 5.67 | 256.63 | 0.00 |
| **Total** | | | **524.60** | **16.89** | **507.71** | |

**Datos de salida corregidos:**

| Campo | Dice | Debe decir |
|---|---|---|
| Amortización total | 250.89 + 256.82 = 507.71 | **251.08 + 256.63 = 507.71** |
| Total de cuotas | 524.98 | **524.60** |
| Total de intereses | 24.99 | **16.89** |
| Interés compensatorio | 52.94 | **24.60** (524.60 − 500.00) |
| TIR | 2.6243 % mensual | **3.2625 % mensual** |
| TCEA | 36.4598 % anual | **46.9988 % anual** |

Otro problema: las dos pruebas usan el **mismo nombre de tienda** ("Divinas Salón & Spa"), pero ese campo es único. Si se cargan en la misma base, la segunda falla. Aclarar que son escenarios independientes, o usar otra tienda.

## 3. Método francés vs. método americano (el error más serio de coherencia)

El informe dice **francés** en la introducción (pág. 2), en 5.2.1 (pág. 12), en 6.3 y en el algoritmo (págs. 28–29). Lo implementado y probado es **francés**: cuotas fijas con interés y amortización.

Pero hay partes que describen el **americano (bullet)**, donde solo se pagan intereses y el capital va al final. Es incompatible con todo lo demás:

| Dónde | Qué dice | Qué hacer |
|---|---|---|
| 5.2.4 (pág. 14) | "En un modelo de amortización americano ... cuota bullet ..." | Reescribir sobre el método francés, o quitar la sección |
| 5.2.5 (pág. 14) | "En el esquema de amortización americano utilizado por el salón ..." | Ídem |
| 5.2.6, párrafo final (pág. 15) | "Bajo el sistema de amortización americano (bullet) implementado para Divinas ..." | **Eliminar** o cambiar a método francés |
| 8.2, entidad `Cuota` (pág. 34) | "intereses en cuotas intermedias y capital en la cuota final bajo el sistema americano" | Cambiar por: *monto de cada cuota fija; los intereses y amortizaciones se calculan con el método francés* |
| Anexos, figuras 1 y 2 (págs. 36–37) | Páginas de BanBif sobre estructura bullet / método americano | Quitar, o reemplazar por una fuente del método francés |
| Bibliografía (pág. 38) | Fuentes de bonos corporativos (Fabozzi, Tuckman, Cedeño, Restrepo...) | Quitar las que solo respaldaban lo americano |

**Posible origen** (no verificado): el propio informe (pág. 8) comenta que se encontraron "secciones que aún correspondían al trabajo anterior". Estas partes parecen restos de ese trabajo previo, que usaba bonos. Conviene confirmarlo con el grupo.

## 4. Ejemplo del laceado de 300 (5.2.1, pág. 12)

La tabla del informe termina con saldo **0.01**, no 0.00: en la cuota 3, 102.20 − 102.19 = 0.01. Con redondeo a centavos, la última cuota absorbe la diferencia.

| N.° Cuota | Cuota | Interés | Amortización | Saldo pendiente |
|---|---|---|---|---|
| 0 | – | – | – | 300.00 |
| 1 | 104.45 | 6.63 | 97.82 | 202.18 |
| 2 | 104.45 | 4.47 | 99.98 | 102.20 |
| 3 | **104.46** | 2.26 | **102.20** | 0.00 |

- Total pagado: **S/ 313.36** (no 313.35); intereses: **S/ 13.36** (no 13.35).
- "las tres cuotas serán del mismo monto" → "las cuotas son iguales, salvo la última, que se ajusta en un centavo para dejar el saldo en cero".
- Siguen bien: i = 2.2104 %, cuota fija 104.45, valores presentes 102.19 + 99.98 + 97.82 ≈ 300.00, y el ejemplo 5.2.2 (S = 320.34).

## 5. TCEA y TEA (5.2.3, pág. 13)

El informe dice: *"Cuando no hay cargos adicionales, la TCEA coincide con la TEA pactada."* Eso **solo es cierto si no hay días de gracia**.

- Ejemplo de 300 sin gracia: TCEA ≈ 30.00 % = TEA. ✅
- Prueba 1 (11 días de gracia): TEA 30 % pero **TCEA 36.46 %**, porque el interés de la gracia se suma al capital y el cliente solo recibe 1,000.
- Pago único: TCEA = TEA.

Texto sugerido: *"Si la primera cuota se paga al terminar el período de gracia sin intereses de gracia, la TCEA coincide con la TEA; si hay días de gracia, la TCEA es mayor, porque los intereses de ese período también forman parte del costo."*

El ejemplo de S/ 3.00 por cuota (cuota 107.45 y TCEA 54.30 %) es numéricamente correcto, pero **el sistema no maneja comisiones ni gastos**.

## 6. Cosas que el informe dice que el sistema hace y no hace

| Afirmación | Dónde | Estado |
|---|---|---|
| TREA (rendimiento del negocio) | Intro, 5.2.3 | **No implementada** (la TCEA sí) |
| Duración de Macaulay, duración modificada y convexidad: "el sistema utiliza el cálculo..." | 5.2.4 | **No implementado** |
| Precio del bono | 5.2.5 | **No implementado** |
| TCEA con comisiones y gastos operativos | Intro, 5.2.3 | **No implementado** (solo interés y gracia) |
| Interés moratorio | 4.8, 6.3, pruebas | **No implementado.** El enunciado lo describe (puntos 5 y 6, Anexo A), pero en clase el profesor dijo que no es obligatorio |
| Seguro de desgravamen | 6.3 | **No implementado** |
| Estado de cuenta en cada fecha de corte | 4.8 | **Parcial**: existe el listado de pagos por cliente con cuota a pagar, saldo y próximo pago |
| Variable `saldo_acumulado_ciclo` | 6.1 (datos intermedios) | **No implementada** |

El **enunciado no pide** TREA, duración, convexidad ni precio del bono. Para cada una: reescribirla como *teoría de apoyo (no implementada)* o quitarla. Lo que sí pide son la TCEA y el plan de pagos.

## 7. Datos de entrada, salida e intermedios que no coinciden (págs. 2, 15–19)

**Capitalización (págs. 2, 14–15):** el texto habla de capitalización *diaria, semanal, quincenal, mensual* (con m = 51.43 y 24). La tabla 6.1 y el sistema usan **Diaria (360), Mensual (12), Bimestral (6), Trimestral (4), Semestral (2) y Anual (1)**. Unificar el texto con la tabla.

**Tabla 6.1 (págs. 16–19):**
- `precio_credito`: el enunciado lo pide en el producto como **precio de lista (crédito)**. Se mantiene, con la regla `precio_credito > precio_contado`. En el sistema es el **monto que se financia**.
- `COMPRA.monto`: en el sistema **no se escribe**; es el `precio_credito` (precio de lista) del producto elegido.
- `PRODUCTO.forma_venta`: el enunciado dice que un producto se vende "en un solo pago al fin de mes (o fecha de corte) o en meses". Los valores del sistema son `"Al contado"` (equivale a **un solo pago**) y `"En meses"`. Un producto `"Al contado"` solo se compra con forma de pago `"Unico pago"`. Conviene renombrar la etiqueta en el informe ("Un solo pago") o aclarar que "Al contado" significa pago único a la fecha de pago.
- `COMPRA.forma_pago` (`"Unico pago"` / `"En meses"`) **falta en la tabla** y sí está en el SQL.
- `COMPRA.num_meses`: dice "aplicable si `forma_venta` = En meses". Debe decir **"obligatorio si `forma_pago` = En meses"**; la `forma_venta` pertenece al producto. Si la compra es de pago único se guarda 1.
- `COMPRA.fecha_compra`: "Fecha presente obligatoria". El sistema usa hoy por defecto y **acepta cualquier fecha**, porque las pruebas usan fechas pasadas.
- **Valores por defecto** (límite S/ 500, máximo 3 meses, corte 20, pago 26): el sistema los **exige** como obligatorios y no los aplica. Cambiar la tabla o el sistema.
- `fecha_pago` "debe estar en el ciclo de la siguiente fecha_corte": **no se valida** (basta que esté entre 1 y 31).
- `password_*`: ✅ el sistema valida de 8 a 72 caracteres y las guarda cifradas (BCrypt, 60 caracteres).
- `tipo_tasa`, `capitalizacion`, `tasa_interes` (0 < x ≤ 200), `moneda`, `usuario_cliente` único por tienda: ✅ coinciden.

**Datos de salida (pág. 19):** `proximos_vencimientos` está descrito como "fecha de vencimiento de productos en tienda... filtro de productos activos". Lo que devuelve el sistema son las **10 próximas cuotas pendientes** de la tienda, ordenadas por fecha de vencimiento. Corregir la redacción.

## 8. Pseudocódigo (págs. 28–29)

El algoritmo del informe es coherente con lo implementado en lo esencial, pero está incompleto:
- **No divide la tasa entre 100** (4.3 dice que ingresa en tanto por uno).
- **No maneja diciembre** (`Mes + 1` = 13) **ni meses cortos** (pago el 31 en abril).
- **No calcula** interés, amortización, saldo, TIR ni TCEA, solo las cuotas.
- **No ajusta la última cuota.**
- **No valida** el monto, el límite de crédito ni el máximo de meses.
- La descripción dice "interés compuesto **diario**"; en realidad es `(1 + TEA)^(días/360)`.

**Versión corregida (para pegar en PSeInt):**

```
Algoritmo GenerarPlanDePagos
    // Datos: monto, forma_pago, num_meses, tipo_tasa, tasa_interes (en %), capitalizacion,
    //        fecha_compra, fecha_corte, fecha_pago, limite_max_credito, max_meses_credito

    // 1. Validaciones
    Si monto <= 0 O monto > limite_max_credito Entonces Error
    Si forma_pago = "En meses" Y (num_meses < 1 O num_meses > max_meses_credito) Entonces Error

    // 2. Fecha de fin de gracia
    anio <- Anio(fecha_compra)
    mes  <- Mes(fecha_compra)
    Si Dia(fecha_compra) > fecha_corte Entonces
        mes <- mes + 1
        Si mes > 12 Entonces
            mes <- 1
            anio <- anio + 1
        FinSi
    FinSi
    dia <- Minimo(fecha_pago, UltimoDiaDelMes(anio, mes))
    fecha_fin_gracia <- ConstruirFecha(anio, mes, dia)
    dias_gracia <- fecha_fin_gracia - fecha_compra

    // 3. Tasas (sin redondear)
    tasa <- tasa_interes / 100
    Si tipo_tasa = "Nominal" Entonces
        m <- PeriodosPorAno(capitalizacion)        // Diaria 360, Mensual 12, Bimestral 6,
        tea <- (1 + tasa / m) ^ m - 1              // Trimestral 4, Semestral 2, Anual 1
    Sino
        tea <- tasa
    FinSi
    tem <- (1 + tea) ^ (30 / 360) - 1

    // 4. Capital capitalizado hasta el fin de la gracia (2 decimales)
    capital <- Redondear(monto * (1 + tea) ^ (dias_gracia / 360), 2)

    Si forma_pago = "Unico pago" Entonces
        GenerarCuota(1, fecha_fin_gracia, capital)
        tir <- Nulo
        tcea <- tea
    Sino
        // 5. Cuota fija, método francés vencido
        cuota_fija <- Redondear(capital * tem * (1 + tem) ^ num_meses / ((1 + tem) ^ num_meses - 1), 2)
        saldo <- capital
        Para k <- 1 Hasta num_meses Hacer
            interes <- Redondear(saldo * tem, 2)
            Si k = num_meses Entonces
                amortizacion <- saldo                     // la última cuota liquida el saldo
            Sino
                amortizacion <- cuota_fija - interes
            FinSi
            cuota <- amortizacion + interes
            saldo_final <- saldo - amortizacion
            GenerarCuota(k, fecha_fin_gracia + k meses, cuota, interes, amortizacion, saldo_final)
            saldo <- saldo_final
        FinPara

        // 6. TIR por bisección y TCEA
        tir <- tasa r que cumple: monto = suma de cuota_k / (1 + r) ^ k, con k = 1..num_meses
        tcea <- (1 + tir) ^ 12 - 1
    FinSi
FinAlgoritmo
```

Notas: la TIR usa el **monto original** (lo que recibe el cliente) y no el capital capitalizado. En 6.3, la fórmula de la cuota usa `VN` (valor nominal del préstamo): aclarar que se aplica sobre el **capital capitalizado** al fin de la gracia.

## 9. Sección 8: Modelo de base de datos (págs. 31–34)

El script del informe es de SQL Server y no corresponde a lo que corre en PostgreSQL:
- Usa `GO` (no existe en PostgreSQL).
- Las claves primarias no son autoincrementales (`INTEGER NOT NULL`), así que los `INSERT` fallan.
- Usa `FLOAT` para dinero y tasas; el sistema usa `NUMERIC`.
- No tiene las restricciones `UNIQUE` y `CHECK` que el informe pide en 6.1.

**Acción:** reemplazarlo por el contenido de **`database/crear_base_divinasdb.sql`** del repositorio (es el script real y probado). `precio_credito` se mantiene en el modelo.

**Documentación de entidades (8.2, pág. 33–34):**
- `Compra`: los campos *Función, Relaciones y Funcionamiento* están **vacíos**. Texto sugerido: *"Función: registra cada compra a crédito de un cliente. Relaciones: pertenece a un Cliente y a un Producto_Servicio, y tiene una o más Cuotas. Funcionamiento: guarda la fecha, el monto financiado (precio de lista del producto), la forma de pago (Unico pago o En meses) y el número de meses."*
- `Cuota`: quitar la mención al sistema americano (ver punto 3).
- `Producto_Servicio`: mantener "precio al contado y precio de lista (crédito)"; aclarar que el precio de lista es el monto que se financia.
- `Tienda` / `Cliente`: agregar que las contraseñas se guardan cifradas con BCrypt.

## 10. Lo que falta agregar al informe: seguridad y API

El backend incluye algo que el informe no describe:
- **Login con token JWT** (dura 120 minutos) y **tres roles**: `ADMIN_SISTEMA` (da de alta y de baja a las tiendas), `ADMIN_TIENDA` y `CLIENTE`. Cada tienda está aislada de las demás, y un cliente solo ve sus propios datos.
- **Endpoints REST** documentados con Swagger.
- Cifrado de contraseñas con BCrypt (ya mencionado en 5.1 d).

Se puede tomar el contenido de `GUIA_BACKEND_Y_FRONTEND.md` (secciones 4 y 5) para redactarlo.

## 11. Uso de IA y bibliografía

**Qué dice el enunciado sobre la IA (sección 10):** hay que detallar los puntos del avance, informe o sistema donde la IA formó parte del grupo, con el **porcentaje de ayuda**, el **nombre de la IA**, el **alumno que fue el puente**, y enumerar **qué se le pidió, a qué herramienta, qué se aceptó, qué se corrigió y por qué**. Si falta, o el profesor detecta ayuda no declarada, **todos los integrantes reciben cero** en la presentación. En clase sugirió declarar un porcentaje razonable (como 40 %), nunca "la IA hizo todo sin declarar".

**Tabla de uso de IA (pág. 35):** solo cubre las secciones 4 a 8 del informe. Si el informe final incluye el backend, las pruebas, la guía o estas revisiones, la tabla debe reflejar **honestamente** el uso de IA en esas partes. Es una declaración académica del grupo.

**Bibliografía (pág. 38):**
- Hay **referencias repetidas** (Cedeño y Gamarra, Fabozzi, Madura, Raimondi et al., Tuckman y Serrat aparecen dos veces).
- Se cita la **Resolución SBS N.º 8181-2012**, pero 5.1 dice que fue reemplazada por la **3274-2017**. Citar la vigente.
- No pude verificar que existan los artículos de *Cedeño y Gamarra (2022)*, *Raimondi et al. (2021)*, *Restrepo y Rivera (2023)* ni *Vásquez y Rojas (2020)*. El grupo debe confirmar que existen y que dicen lo que se les atribuye.
- Las fuentes de bonos y del método americano pueden sobrar al corregir el punto 3.

## 12. Diferencias entre el sistema y el enunciado

Revisado contra `SI642_Enunciado del Trabajo Final 2026-20 (1).pdf`.

| Punto del enunciado | Qué pide | Estado del sistema |
|---|---|---|
| 1 | El **administrador del sistema** da de alta y de baja a las tiendas, con sus credenciales y datos | ✅ Rol `ADMIN_SISTEMA` (credenciales en el `.env`). La tienda no se registra sola |
| 1 | Login con usuario y clave para el cliente y el administrador del negocio | ✅ |
| 2 | Productos con proveedor, marca, descripción, unidad de medida, imagen, **precio contado, precio de lista (crédito)** y venta en un solo pago al fin de mes o en meses | ✅ Con `precio_credito` y `forma_venta` |
| 3 | Clientes con tasa nominal o efectiva, moneda, límite máximo, máximo de meses, fecha de corte y fecha de pago | ✅ |
| 4 | Método francés vencido simple ordinario, meses de 30 días, gracia capitalizada, primera cuota el mes siguiente | ✅ (el ejemplo del enunciado: compra el 15/09, pago el 26, 11 días de gracia, cuotas 26/10, 26/11 y 26/12) |
| 5 | Intereses por los días entre la compra y la fecha de pago | ✅ |
| 5 | Intereses por **mora** | ❌ No implementado. En clase el profesor dijo que no es obligatorio |
| 5 | No permitir compras que excedan el monto permitido | ✅ Se compara con el **crédito disponible**: el límite menos la deuda pendiente |
| 5 | No permitir pagos parciales | ✅ La cuota se paga completa |
| 6 | Listado de pago generado en la **fecha de corte**, con las compras ordenadas por fecha e intereses acumulados | ✅ `GET .../listado-pago` (por cliente y por tienda). Se genera al consultarlo; no hay un proceso automático |
| 9 | Registrar todas las operaciones en una base de datos | ✅ Tabla `operacion` (bitácora de inicios de sesión, altas, bajas, modificaciones, compras y pagos) |
| Anexo A | Base 360, mes de 30 días, conversión explícita de tasas, dinero a 2 decimales y tasas con al menos 7 | ✅ (tasas con 10 decimales) |
| Anexo A | Orden de imputación de pagos: mora, interés compensatorio y capital | ❌ Solo aplica con mora o pagos parciales |

**Estructura del informe final (sección "Estructura del Informe" del enunciado):** son 12 secciones. Respecto al informe actual (11), se agrega la **9. Sistema de información** (conectividad, base de datos, código fuente, ayuda y asistencia técnica, validación y pruebas), y el uso de IA pasa a ser la sección 10. La interfaz debe incluir ayuda sobre cómo llenar cada campo.

**Entrega del hito 2:** presentación en PowerPoint (herramientas, lenguajes, base de datos, servicio de alojamiento, dirección web si está desplegada, y requisitos de hardware), informe en Word, anexos, código fuente y video de hasta 20 minutos. En la exposición, "en esa parte me ayudaron" no es una respuesta válida: cada integrante debe saber explicar todo.

---

## Checklist de correcciones

**Prioridad alta**
- [ ] Corregir la tabla de la Prueba 1 (fechas, interés de gracia 8.05, días de gracia 11)
- [ ] Rehacer la tabla y los resultados de la Prueba 2
- [ ] Eliminar o reescribir todo lo "americano" (5.2.4, 5.2.5, 5.2.6, 8.2, anexos y bibliografía)
- [ ] Reemplazar el script SQL por `database/crear_base_divinasdb.sql` y actualizar el diagrama ER
- [ ] Mantener `precio_credito` (precio de lista) y redefinir los datos de producto de las pruebas (`monto` = `precio_credito`)

**Prioridad media**
- [ ] Corregir el ejemplo 5.2.1 (cuota 3 de 104.46, total 313.36, intereses 13.36)
- [ ] Corregir la frase "TCEA = TEA sin cargos"
- [ ] Quitar o declarar como teoría no implementada: TREA, duración, convexidad, precio del bono y comisiones (el enunciado no los pide)
- [ ] Agregar la sección 9 del informe final (Sistema de información: conectividad, base de datos, código fuente, ayuda y asistencia técnica, validación y pruebas)
- [ ] Describir el rol de administrador del sistema (alta y baja de tiendas) en el análisis y la interfaz
- [ ] Agregar la entidad `Operacion` (bitácora) al modelo de datos y al diagrama ER, y el crédito disponible y el listado de pago a las fórmulas y datos de salida
- [ ] Unificar los períodos de capitalización (texto vs. tabla 6.1)
- [ ] Ajustar la tabla 6.1: `forma_pago`, `num_meses`, `monto`, valores por defecto y `fecha_pago`
- [ ] Reemplazar el pseudocódigo por la versión corregida
- [ ] En las pruebas: `tipo_tasa` = `Efectiva`, `usuario_cliente`, quitar la tasa moratoria y no repetir el nombre de tienda
- [ ] Completar la entidad `Compra` en 8.2
- [ ] Agregar la sección de seguridad y API

**Prioridad baja**
- [ ] Redactar bien `proximos_vencimientos` en los datos de salida
- [ ] Quitar las referencias duplicadas de la bibliografía y citar la SBS vigente
- [ ] **Actualizar la tabla de uso de IA con el detalle que exige el enunciado (ver el punto 11)**
- [ ] Revisar que el título y el video ("TB1 ...") correspondan a esta entrega

## Decisiones que debe tomar el grupo
1. **TREA, duración, convexidad y precio del bono:** el enunciado no las pide; ¿se quitan del informe o se dejan como teoría de apoyo?
2. **Mora:** en clase se dijo que no es obligatoria. ¿La dejan fuera del informe también, o la documentan como mejora futura?
3. **Valores por defecto de 6.1:** ¿se agregan al sistema o se quitan de la tabla?
4. **Prueba 2:** ¿otra tienda, o aclarar que es un escenario independiente?
