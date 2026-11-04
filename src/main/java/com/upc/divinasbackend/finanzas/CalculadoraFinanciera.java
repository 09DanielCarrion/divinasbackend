package com.upc.divinasbackend.finanzas;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/*
    Reglas:
    - Año comercial de 360 días y mes comercial de 30 días.
    - Las tasas se manejan en tanto por uno (0.30 = 30%) y no se redondean mientras se calcula.
    - El dinero se redondea a 2 decimales cuando el metodo devuelve un monto.
 */

public class CalculadoraFinanciera {

    private static final MathContext MC = new MathContext(20);
    private static final BigDecimal CIEN = new BigDecimal("100");

    // Metodo para redondear
    private static BigDecimal dinero(BigDecimal x) {
        return x.setScale(2, RoundingMode.HALF_UP);
    }


    private static BigDecimal potencia(BigDecimal base, BigDecimal exponente) {
        return new BigDecimal(Math.pow(base.doubleValue(), exponente.doubleValue()), MC);
    }

    /*
        Períodos de capitalización por año: Diaria 360, Mensual 12, Bimestral 6,
        Trimestral 4, Semestral 2, Anual 1.
        Cualquier otro valor lanza IllegalArgumentException como error.
     */
    public static int periodosPorAnio(String capitalizacion) {
        if (capitalizacion == null) {
            throw new IllegalArgumentException("La capitalización es obligatoria");
        }
        return switch (capitalizacion) {
            case "Diaria" -> 360;
            case "Mensual" -> 12;
            case "Bimestral" -> 6;
            case "Trimestral" -> 4;
            case "Semestral" -> 2;
            case "Anual" -> 1;
            default -> throw new IllegalArgumentException("Capitalización desconocida: " + capitalizacion);
        };
    }

    /*
        Tasa efectiva anual (tanto por uno) a partir de la tasa del cliente.
            "Efectiva": tasaPorcentaje / 100
            "Nominal":  (1 + TN/m)^m - 1, con TN = tasaPorcentaje/100 y m = periodosPorAnio(capitalizacion)
        Lanza IllegalArgumentException si el tipo no es Nominal/Efectiva, o si es Nominal sin capitalización.
     */
    public static BigDecimal tasaEfectivaAnual(String tipoTasa, BigDecimal tasaPorcentaje, String capitalizacion) {
        BigDecimal tasa = tasaPorcentaje.divide(CIEN, MC);
        if ("Efectiva".equals(tipoTasa)) {
            return tasa;
        }
        if ("Nominal".equals(tipoTasa)) {
            int m = periodosPorAnio(capitalizacion);
            BigDecimal tasaPorPeriodo = tasa.divide(BigDecimal.valueOf(m), MC);
            // aqui estoy aplicando la formula para convertir la tasa nominal a efectiva nomas
            return BigDecimal.ONE.add(tasaPorPeriodo).pow(m, MC).subtract(BigDecimal.ONE);
        }
        throw new IllegalArgumentException("Tipo de tasa desconocido: " + tipoTasa);
    }

    public static BigDecimal tasaEfectivaMensual(BigDecimal tea) {
        if (tea == null) {
            throw new IllegalArgumentException("La tea es obligatoria");
        }
        double base = BigDecimal.ONE.add(tea).doubleValue();
        double potencia = Math.pow(base, 30.0 / 360.0);
        return new BigDecimal(potencia, MC).subtract(BigDecimal.ONE);
    }

    /*
        Fecha en que termina el periodo de gracia:
         - si el día de la compra es <= fechaCorte, es el día fechaPago del mismo mes;
         - sino, es el día fechaPago del siguinte mes.
        Si el mes no tiene ese día, se usa el último día del mes.
     */
    public static LocalDate fechaFinGracia(LocalDate fechaCompra, int fechaCorte, int fechaPago) {
        YearMonth mes = YearMonth.from(fechaCompra);
        if (fechaCompra.getDayOfMonth() > fechaCorte) {
            mes = mes.plusMonths(1);
        }
        int dia = Math.min(fechaPago, mes.lengthOfMonth());
        return mes.atDay(dia);
    }

    /*
        Días calendario entre la compra y el fin de la gracia.
     */
    public static int diasGracia(LocalDate fechaCompra, LocalDate fechaFinGracia) {
        return (int) ChronoUnit.DAYS.between(fechaCompra, fechaFinGracia);
    }

    /*
        Capital capitalizado: monto * (1 + TEA)^(diasGracia / 360), con redondeo de 2 dec.
     */
    public static BigDecimal capitalCapitalizado(BigDecimal monto, BigDecimal tea, int diasGracia) {
        BigDecimal exponente = new BigDecimal(diasGracia).divide(new BigDecimal(360), MC);
        BigDecimal factor = potencia(BigDecimal.ONE.add(tea), exponente);
        return dinero(monto.multiply(factor, MC));
    }

    /*
        Cuota fija del metodo francés vencido, con redondeo de 2 dec.
        C * i * (1+i)^n / ((1+i)^n - 1), con i = tasa mensual.
     */
    public static BigDecimal cuotaFija(BigDecimal capital, BigDecimal tasaMensual, int numMeses) {
        // (1+i)^n
        BigDecimal factor = BigDecimal.ONE.add(tasaMensual).pow(numMeses, MC);
        // C * i * (1+i)^n
        BigDecimal numerador = capital.multiply(tasaMensual, MC).multiply(factor, MC);
        //((1+i)^n - 1)
        BigDecimal denominador = factor.subtract(BigDecimal.ONE);
        return dinero(numerador.divide(denominador, MC));
    }

    /*
        TIR mensual: la tasa r que cumple  monto = suma de cuota_k / (1+r)^k, con k = 1..n.
        Se usa metodo de bisección.
     */
    public static BigDecimal tir(BigDecimal monto, List<BigDecimal> cuotas) {
        BigDecimal bajo = new BigDecimal("0.0000001");
        BigDecimal alto = new BigDecimal("0.9");
        BigDecimal medio = bajo;
        for (int iteracion = 0; iteracion < 200; iteracion++) {
            medio = bajo.add(alto).divide(new BigDecimal(2), MC);
            BigDecimal valorPresente = BigDecimal.ZERO;
            for (int k = 0; k < cuotas.size(); k++) {
                BigDecimal descuento = BigDecimal.ONE.add(medio).pow(k + 1, MC);
                valorPresente = valorPresente.add(cuotas.get(k).divide(descuento, MC));
            }
            // Si el valor presente supera el monto, la tasa es muy baja se sube, sino se baja.
            if (valorPresente.compareTo(monto) > 0) {
                bajo = medio;
            } else {
                alto = medio;
            }
        }
        return medio;
    }

    /*
        TCEA: (1 + TIR mensual)^12 - 1
     */
    public static BigDecimal tcea(BigDecimal tirMensual) {
        return BigDecimal.ONE.add(tirMensual).pow(12, MC).subtract(BigDecimal.ONE);
    }

    /*
        Funcion para armar el plan de pagos completo
     */
    public static PlanDePagos generarPlan(LocalDate fechaCompra, BigDecimal monto, String formaPago, Integer numMeses,
                                          String tipoTasa, BigDecimal tasaPorcentaje, String capitalizacion,
                                          int fechaCorte, int fechaPago) {
        // Algunas validaciones
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }
        if (fechaCorte < 1 || fechaCorte > 31 || fechaPago < 1 || fechaPago > 31) {
            throw new IllegalArgumentException("Las fechas de corte y pago deben estar entre 1 y 31");
        }
        boolean pagoUnico = "Unico pago".equals(formaPago);
        if (!pagoUnico && !"En meses".equals(formaPago)) {
            throw new IllegalArgumentException("Forma de pago inválida: " + formaPago);
        }
        if (!pagoUnico && (numMeses == null || numMeses < 1)) {
            throw new IllegalArgumentException("El número de meses debe ser al menos 1");
        }

        // calculo de gracia y las tasas
        LocalDate fin = fechaFinGracia(fechaCompra, fechaCorte, fechaPago);
        int dias = diasGracia(fechaCompra, fin);
        BigDecimal tea = tasaEfectivaAnual(tipoTasa, tasaPorcentaje, capitalizacion);
        BigDecimal tem = tasaEfectivaMensual(tea);
        BigDecimal capital = capitalCapitalizado(monto, tea, dias);
        BigDecimal interesGracia = capital.subtract(dinero(monto));

        // Si el pago es unico, solo se genera una cuota
        if (pagoUnico) {
            BigDecimal cero = dinero(BigDecimal.ZERO);
            FilaCuota fila = new FilaCuota(1, fin, capital, capital, cero, capital, cero);
            return new PlanDePagos(fin, dias, tea, tem, capital, interesGracia, capital,
                    List.of(fila), capital, cero, null, tea);
        }

        // Si el pago es mas de una cuota, entonces aca recien se aplica metodo frances vencido
        BigDecimal cuotaFija = cuotaFija(capital, tem, numMeses);
        List<FilaCuota> filas = new ArrayList<>();
        List<BigDecimal> cuotas = new ArrayList<>();
        BigDecimal saldo = capital;
        BigDecimal totalCuotas = BigDecimal.ZERO;
        BigDecimal totalIntereses = BigDecimal.ZERO;

        for (int k = 1; k <= numMeses; k++) {
            BigDecimal interes = dinero(saldo.multiply(tem, MC));
            // la ultima fila toma los centavos para que el saldo final sea cero
            BigDecimal amortizacion;
            if (k == numMeses) {
                amortizacion = saldo;
            } else {
                amortizacion = cuotaFija.subtract(interes);
            }
            BigDecimal cuota = amortizacion.add(interes);
            BigDecimal saldoFinal = saldo.subtract(amortizacion);

            filas.add(new FilaCuota(k, fin.plusMonths(k), saldo, cuota, interes, amortizacion, saldoFinal));
            cuotas.add(cuota);
            totalCuotas = totalCuotas.add(cuota);
            totalIntereses = totalIntereses.add(interes);
            saldo = saldoFinal;
        }

        BigDecimal tir = tir(dinero(monto), cuotas);
        return new PlanDePagos(fin, dias, tea, tem, capital, interesGracia, cuotaFija,
                filas, totalCuotas, totalIntereses, tir, tcea(tir));
    }
}
