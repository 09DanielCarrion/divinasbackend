package com.upc.divinasbackend.finanzas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Casos del TP (Prueba 1, Prueba 2, ejemplo 5.2.1) y casos de borde.
 * Los valores de la Prueba 2 son los CORRECTOS (el informe tenía copiados los de la Prueba 1).
 */
class CalculadoraFinancieraTest {

    // ---------- ayudas ----------
    private static BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }

    // Dinero: se compara a 2 decimales
    private static void assertDinero(String esperado, BigDecimal real, String detalle) {
        assertNotNull(real, detalle + ": llegó null");
        assertEquals(new BigDecimal(esperado), real.setScale(2, RoundingMode.HALF_UP), detalle);
    }

    // Tasas: se compara a 7 decimales (mínimo que pide el enunciado)
    private static void assertTasa(String esperado, BigDecimal real, String detalle) {
        assertNotNull(real, detalle + ": llegó null");
        assertEquals(new BigDecimal(esperado), real.setScale(7, RoundingMode.HALF_UP), detalle);
    }

    private static void assertFila(FilaCuota fila, int numero, LocalDate fecha, String saldoIni, String cuota,
                                   String interes, String amort, String saldoFin) {
        String f = "cuota " + numero;
        assertEquals(numero, fila.numero(), f + ": número");
        assertEquals(fecha, fila.fechaVencimiento(), f + ": fecha de vencimiento");
        assertDinero(saldoIni, fila.saldoInicial(), f + ": saldo inicial");
        assertDinero(cuota, fila.cuota(), f + ": cuota");
        assertDinero(interes, fila.interes(), f + ": interés");
        assertDinero(amort, fila.amortizacion(), f + ": amortización");
        assertDinero(saldoFin, fila.saldoFinal(), f + ": saldo final");
    }

    // ---------- 1. Tasas ----------
    @Nested
    @DisplayName("1. Conversión de tasas")
    class Tasas {

        @Test
        void periodosPorAnioDeCadaCapitalizacion() {
            assertEquals(360, CalculadoraFinanciera.periodosPorAnio("Diaria"));
            assertEquals(12, CalculadoraFinanciera.periodosPorAnio("Mensual"));
            assertEquals(6, CalculadoraFinanciera.periodosPorAnio("Bimestral"));
            assertEquals(4, CalculadoraFinanciera.periodosPorAnio("Trimestral"));
            assertEquals(2, CalculadoraFinanciera.periodosPorAnio("Semestral"));
            assertEquals(1, CalculadoraFinanciera.periodosPorAnio("Anual"));
        }

        @Test
        void capitalizacionDesconocidaLanzaError() {
            assertThrows(IllegalArgumentException.class, () -> CalculadoraFinanciera.periodosPorAnio("Semanal"));
        }

        @Test
        void teaDeTasaEfectivaEsLaMismaTasa() {
            assertTasa("0.3000000", CalculadoraFinanciera.tasaEfectivaAnual("Efectiva", bd("30"), null), "TEA efectiva 30%");
        }

        @Test
        void teaDeTasaNominalSegunCapitalizacion() {
            assertTasa("0.3448888", CalculadoraFinanciera.tasaEfectivaAnual("Nominal", bd("30"), "Mensual"), "TN 30% mensual");
            assertTasa("0.3496902", CalculadoraFinanciera.tasaEfectivaAnual("Nominal", bd("30"), "Diaria"), "TN 30% diaria");
            assertTasa("0.3354691", CalculadoraFinanciera.tasaEfectivaAnual("Nominal", bd("30"), "Trimestral"), "TN 30% trimestral");
            assertTasa("0.3000000", CalculadoraFinanciera.tasaEfectivaAnual("Nominal", bd("30"), "Anual"), "TN 30% anual");
        }

        @Test
        void tasaNominalSinCapitalizacionLanzaError() {
            assertThrows(IllegalArgumentException.class,
                    () -> CalculadoraFinanciera.tasaEfectivaAnual("Nominal", bd("30"), null));
        }

        @Test
        void tipoDeTasaDesconocidoLanzaError() {
            assertThrows(IllegalArgumentException.class,
                    () -> CalculadoraFinanciera.tasaEfectivaAnual("TEA", bd("30"), null));
        }

        @Test
        void temDeTea30() {
            assertTasa("0.0221045", CalculadoraFinanciera.tasaEfectivaMensual(bd("0.30")), "TEM de TEA 30%");
        }

        @Test
        void temDeUnaNominalMensualEsExactamenteLaTasaMensual() {
            // TN 30% mensual => 2.5% por mes. Si se redondea la TEA antes de convertir, esto no da exacto.
            BigDecimal tea = CalculadoraFinanciera.tasaEfectivaAnual("Nominal", bd("30"), "Mensual");
            assertTasa("0.0250000", CalculadoraFinanciera.tasaEfectivaMensual(tea), "TEM de TN 30% mensual");
        }
    }

    // ---------- 2. Fechas ----------
    @Nested
    @DisplayName("2. Fecha de fin de gracia y días de gracia")
    class Fechas {

        @Test
        void compraAntesDelCorteVaAlPagoDelMismoMes() {
            assertEquals(LocalDate.of(2026, 10, 26), CalculadoraFinanciera.fechaFinGracia(LocalDate.of(2026, 10, 15), 20, 26));
            assertEquals(LocalDate.of(2026, 10, 26), CalculadoraFinanciera.fechaFinGracia(LocalDate.of(2026, 10, 5), 20, 26));
        }

        @Test
        void compraEnElDiaDeCorteTodavíaEsDelMismoMes() {
            assertEquals(LocalDate.of(2026, 10, 26), CalculadoraFinanciera.fechaFinGracia(LocalDate.of(2026, 10, 20), 20, 26));
        }

        @Test
        void compraDespuesDelCorteVaAlPagoDelMesSiguiente() {
            assertEquals(LocalDate.of(2026, 11, 26), CalculadoraFinanciera.fechaFinGracia(LocalDate.of(2026, 10, 21), 20, 26));
        }

        @Test
        void compraDeDiciembreDespuesDelCortePasaAlAnioSiguiente() {
            assertEquals(LocalDate.of(2027, 1, 26), CalculadoraFinanciera.fechaFinGracia(LocalDate.of(2026, 12, 25), 20, 26));
        }

        @Test
        void fechaDePago31EnMesDe30DiasUsaElUltimoDia() {
            assertEquals(LocalDate.of(2026, 4, 30), CalculadoraFinanciera.fechaFinGracia(LocalDate.of(2026, 4, 5), 20, 31));
        }

        @Test
        void fechaDePago31EnFebreroUsaElUltimoDia() {
            assertEquals(LocalDate.of(2027, 2, 28), CalculadoraFinanciera.fechaFinGracia(LocalDate.of(2027, 2, 10), 20, 31));
            assertEquals(LocalDate.of(2028, 2, 29), CalculadoraFinanciera.fechaFinGracia(LocalDate.of(2028, 2, 10), 20, 31));
        }

        @Test
        void diasDeGracia() {
            assertEquals(11, CalculadoraFinanciera.diasGracia(LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 26)));
            assertEquals(21, CalculadoraFinanciera.diasGracia(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 26)));
            assertEquals(0, CalculadoraFinanciera.diasGracia(LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 15)));
        }
    }

    // ---------- 3. Capital y cuota ----------
    @Nested
    @DisplayName("3. Capital capitalizado y cuota fija")
    class CapitalYCuota {

        @Test
        void capitalCapitalizado() {
            assertDinero("1008.05", CalculadoraFinanciera.capitalCapitalizado(bd("1000"), bd("0.30"), 11), "Prueba 1");
            assertDinero("507.71", CalculadoraFinanciera.capitalCapitalizado(bd("500"), bd("0.30"), 21), "Prueba 2");
            assertDinero("320.34", CalculadoraFinanciera.capitalCapitalizado(bd("300"), bd("0.30"), 90), "ejemplo 5.2.2 (90 días)");
            assertDinero("300.00", CalculadoraFinanciera.capitalCapitalizado(bd("300"), bd("0.30"), 0), "sin gracia");
        }

        @Test
        void cuotaFija() {
            BigDecimal tem = CalculadoraFinanciera.tasaEfectivaMensual(bd("0.30"));
            assertDinero("350.98", CalculadoraFinanciera.cuotaFija(bd("1008.05"), tem, 3), "Prueba 1");
            assertDinero("262.30", CalculadoraFinanciera.cuotaFija(bd("507.71"), tem, 2), "Prueba 2");
            assertDinero("104.45", CalculadoraFinanciera.cuotaFija(bd("300.00"), tem, 3), "ejemplo 5.2.1");
        }
    }

    // ---------- 4. TIR y TCEA ----------
    @Nested
    @DisplayName("4. TIR y TCEA")
    class TirTcea {

        @Test
        void prueba1() {
            BigDecimal tir = CalculadoraFinanciera.tir(bd("1000"), List.of(bd("350.98"), bd("350.98"), bd("350.98")));
            assertTasa("0.0262434", tir, "TIR Prueba 1 (2.6243% mensual)");
            assertTasa("0.3645976", CalculadoraFinanciera.tcea(tir), "TCEA Prueba 1 (36.4598%)");
        }

        @Test
        void prueba2() {
            BigDecimal tir = CalculadoraFinanciera.tir(bd("500"), List.of(bd("262.30"), bd("262.30")));
            assertTasa("0.0326254", tir, "TIR Prueba 2");
            assertTasa("0.4699883", CalculadoraFinanciera.tcea(tir), "TCEA Prueba 2 (46.9988%)");
        }

        @Test
        void ejemplo300() {
            BigDecimal tir = CalculadoraFinanciera.tir(bd("300"), List.of(bd("104.45"), bd("104.45"), bd("104.46")));
            assertTasa("0.0221052", tir, "TIR ejemplo 300");
            assertTasa("0.3000116", CalculadoraFinanciera.tcea(tir), "TCEA ejemplo 300 (≈30.00%)");
        }
    }

    // ---------- 5. Plan completo ----------
    @Nested
    @DisplayName("5. Plan de pagos completo")
    class Plan {

        @Test
        @DisplayName("TP Prueba 1: 1,000.00 en 3 meses, compra 15/10/2026")
        void prueba1() {
            PlanDePagos p = CalculadoraFinanciera.generarPlan(LocalDate.of(2026, 10, 15), bd("1000"), "En meses", 3,
                    "Efectiva", bd("30"), null, 20, 26);

            assertEquals(LocalDate.of(2026, 10, 26), p.fechaFinGracia());
            assertEquals(11, p.diasGracia());
            assertTasa("0.3000000", p.tasaEfectivaAnual(), "TEA");
            assertTasa("0.0221045", p.tasaEfectivaMensual(), "TEM");
            assertDinero("1008.05", p.capitalCapitalizado(), "capital capitalizado");
            assertDinero("8.05", p.interesGracia(), "interés de gracia");
            assertDinero("350.98", p.cuotaFija(), "cuota fija");

            assertEquals(3, p.cuotas().size());
            // La primera cuota vence UN MES DESPUÉS del fin de la gracia (26/11, 26/12, 26/01)
            assertFila(p.cuotas().get(0), 1, LocalDate.of(2026, 11, 26), "1008.05", "350.98", "22.28", "328.70", "679.35");
            assertFila(p.cuotas().get(1), 2, LocalDate.of(2026, 12, 26), "679.35", "350.98", "15.02", "335.96", "343.39");
            assertFila(p.cuotas().get(2), 3, LocalDate.of(2027, 1, 26), "343.39", "350.98", "7.59", "343.39", "0.00");

            assertDinero("1052.94", p.totalCuotas(), "total de cuotas (= precio a crédito del TP)");
            assertDinero("44.89", p.totalIntereses(), "intereses de las cuotas");
            assertDinero("52.94", p.interesGracia().add(p.totalIntereses()), "interés compensatorio total (TP: 52.94)");
            assertTasa("0.0262434", p.tir(), "TIR");
            assertTasa("0.3645976", p.tcea(), "TCEA");
        }

        @Test
        @DisplayName("TP Prueba 2 (valores corregidos): 500.00 en 2 meses, compra 05/10/2026")
        void prueba2() {
            PlanDePagos p = CalculadoraFinanciera.generarPlan(LocalDate.of(2026, 10, 5), bd("500"), "En meses", 2,
                    "Efectiva", bd("30"), null, 20, 26);

            assertEquals(LocalDate.of(2026, 10, 26), p.fechaFinGracia());
            assertEquals(21, p.diasGracia());
            assertDinero("507.71", p.capitalCapitalizado(), "capital capitalizado");
            assertDinero("7.71", p.interesGracia(), "interés de gracia");
            assertDinero("262.30", p.cuotaFija(), "cuota fija");

            assertEquals(2, p.cuotas().size());
            assertFila(p.cuotas().get(0), 1, LocalDate.of(2026, 11, 26), "507.71", "262.30", "11.22", "251.08", "256.63");
            assertFila(p.cuotas().get(1), 2, LocalDate.of(2026, 12, 26), "256.63", "262.30", "5.67", "256.63", "0.00");

            assertDinero("524.60", p.totalCuotas(), "total de cuotas");
            assertDinero("16.89", p.totalIntereses(), "intereses de las cuotas");
            assertTasa("0.0326254", p.tir(), "TIR");
            assertTasa("0.4699883", p.tcea(), "TCEA");
        }

        @Test
        @DisplayName("TP 5.2.1: 300.00 en 3 meses sin gracia; la última cuota absorbe el centavo")
        void ejemplo300SinGracia() {
            // compra el día 15, corte 20, pago 15  =>  0 días de gracia
            PlanDePagos p = CalculadoraFinanciera.generarPlan(LocalDate.of(2026, 10, 15), bd("300"), "En meses", 3,
                    "Efectiva", bd("30"), null, 20, 15);

            assertEquals(0, p.diasGracia());
            assertDinero("300.00", p.capitalCapitalizado(), "capital capitalizado");
            assertDinero("104.45", p.cuotaFija(), "cuota fija");

            assertFila(p.cuotas().get(0), 1, LocalDate.of(2026, 11, 15), "300.00", "104.45", "6.63", "97.82", "202.18");
            assertFila(p.cuotas().get(1), 2, LocalDate.of(2026, 12, 15), "202.18", "104.45", "4.47", "99.98", "102.20");
            // Ajuste de la última cuota: amortización = saldo (102.20), cuota = 102.20 + 2.26 = 104.46
            assertFila(p.cuotas().get(2), 3, LocalDate.of(2027, 1, 15), "102.20", "104.46", "2.26", "102.20", "0.00");

            assertDinero("313.36", p.totalCuotas(), "total de cuotas");
            assertTasa("0.3000116", p.tcea(), "TCEA");
        }

        @Test
        @DisplayName("Tasa nominal 30% con capitalización mensual")
        void tasaNominal() {
            PlanDePagos p = CalculadoraFinanciera.generarPlan(LocalDate.of(2026, 10, 15), bd("1000"), "En meses", 3,
                    "Nominal", bd("30"), "Mensual", 20, 26);

            assertTasa("0.3448888", p.tasaEfectivaAnual(), "TEA");
            assertTasa("0.0250000", p.tasaEfectivaMensual(), "TEM");
            assertDinero("1009.10", p.capitalCapitalizado(), "capital capitalizado");
            assertDinero("353.32", p.cuotaFija(), "cuota fija");
        }

        @Test
        @DisplayName("Pago único: una sola cuota con el capital capitalizado")
        void pagoUnico() {
            PlanDePagos p = CalculadoraFinanciera.generarPlan(LocalDate.of(2026, 10, 15), bd("1000"), "Unico pago", null,
                    "Efectiva", bd("30"), null, 20, 26);

            assertEquals(LocalDate.of(2026, 10, 26), p.fechaFinGracia());
            assertDinero("1008.05", p.capitalCapitalizado(), "capital capitalizado");
            assertDinero("8.05", p.interesGracia(), "interés de gracia");
            assertEquals(1, p.cuotas().size());
            assertFila(p.cuotas().get(0), 1, LocalDate.of(2026, 10, 26), "1008.05", "1008.05", "0.00", "1008.05", "0.00");
            assertDinero("1008.05", p.totalCuotas(), "total de cuotas");
            assertDinero("0.00", p.totalIntereses(), "intereses de las filas");
            assertNull(p.tir(), "en pago único no hay TIR");
            assertTasa("0.3000000", p.tcea(), "en pago único la TCEA es la TEA");
        }

        @Test
        @DisplayName("Fecha de pago 31: cada cuota cae en el último día de su mes")
        void fechaDePago31() {
            PlanDePagos p = CalculadoraFinanciera.generarPlan(LocalDate.of(2027, 1, 10), bd("1000"), "En meses", 3,
                    "Efectiva", bd("30"), null, 20, 31);

            assertEquals(LocalDate.of(2027, 1, 31), p.fechaFinGracia());
            assertEquals(LocalDate.of(2027, 2, 28), p.cuotas().get(0).fechaVencimiento());
            assertEquals(LocalDate.of(2027, 3, 31), p.cuotas().get(1).fechaVencimiento());
            assertEquals(LocalDate.of(2027, 4, 30), p.cuotas().get(2).fechaVencimiento());
        }

        @Test
        @DisplayName("Invariantes: el saldo encadena, cierra en 0.00 y las amortizaciones suman el capital")
        void invariantes() {
            List<PlanDePagos> planes = List.of(
                    CalculadoraFinanciera.generarPlan(LocalDate.of(2026, 10, 15), bd("1000"), "En meses", 3, "Efectiva", bd("30"), null, 20, 26),
                    CalculadoraFinanciera.generarPlan(LocalDate.of(2026, 10, 5), bd("500"), "En meses", 2, "Efectiva", bd("30"), null, 20, 26),
                    CalculadoraFinanciera.generarPlan(LocalDate.of(2026, 10, 15), bd("300"), "En meses", 3, "Efectiva", bd("30"), null, 20, 15),
                    CalculadoraFinanciera.generarPlan(LocalDate.of(2026, 3, 12), bd("1234.56"), "En meses", 7, "Nominal", bd("45.5"), "Trimestral", 20, 26),
                    CalculadoraFinanciera.generarPlan(LocalDate.of(2026, 3, 25), bd("89.90"), "En meses", 12, "Efectiva", bd("200"), null, 10, 5));

            for (PlanDePagos p : planes) {
                BigDecimal sumaAmortizaciones = BigDecimal.ZERO;
                BigDecimal saldoEsperado = p.capitalCapitalizado();
                for (FilaCuota f : p.cuotas()) {
                    assertEquals(saldoEsperado.setScale(2, RoundingMode.HALF_UP), f.saldoInicial().setScale(2, RoundingMode.HALF_UP),
                            "el saldo inicial de la cuota " + f.numero() + " debe ser el saldo final de la anterior");
                    assertEquals(f.saldoInicial().subtract(f.amortizacion()).setScale(2, RoundingMode.HALF_UP),
                            f.saldoFinal().setScale(2, RoundingMode.HALF_UP), "saldo final = saldo inicial - amortización");
                    assertEquals(f.interes().add(f.amortizacion()).setScale(2, RoundingMode.HALF_UP),
                            f.cuota().setScale(2, RoundingMode.HALF_UP), "cuota = interés + amortización");
                    sumaAmortizaciones = sumaAmortizaciones.add(f.amortizacion());
                    saldoEsperado = f.saldoFinal();
                }
                assertDinero("0.00", p.cuotas().get(p.cuotas().size() - 1).saldoFinal(), "el saldo final debe ser 0.00");
                assertEquals(p.capitalCapitalizado().setScale(2, RoundingMode.HALF_UP), sumaAmortizaciones.setScale(2, RoundingMode.HALF_UP),
                        "las amortizaciones deben sumar el capital capitalizado");
                // La última cuota difiere de la fija como máximo en unos pocos centavos
                BigDecimal ultima = p.cuotas().get(p.cuotas().size() - 1).cuota();
                assertTrue(ultima.subtract(p.cuotaFija()).abs().compareTo(bd("0.10")) < 0,
                        "la última cuota no debe alejarse más de 0.10 de la cuota fija");
            }
        }
    }

    // ---------- 6. Validaciones ----------
    @Nested
    @DisplayName("6. Datos inválidos")
    class Validaciones {

        private PlanDePagos plan(BigDecimal monto, String forma, Integer meses, String tipo, String cap, int corte, int pago) {
            return CalculadoraFinanciera.generarPlan(LocalDate.of(2026, 10, 15), monto, forma, meses, tipo, bd("30"), cap, corte, pago);
        }

        @Test
        void montoCeroONegativo() {
            assertThrows(IllegalArgumentException.class, () -> plan(bd("0"), "En meses", 3, "Efectiva", null, 20, 26));
            assertThrows(IllegalArgumentException.class, () -> plan(bd("-5"), "En meses", 3, "Efectiva", null, 20, 26));
        }

        @Test
        void numeroDeMesesInvalidoEnCuotas() {
            assertThrows(IllegalArgumentException.class, () -> plan(bd("100"), "En meses", 0, "Efectiva", null, 20, 26));
            assertThrows(IllegalArgumentException.class, () -> plan(bd("100"), "En meses", null, "Efectiva", null, 20, 26));
        }

        @Test
        void formaDePagoDesconocida() {
            assertThrows(IllegalArgumentException.class, () -> plan(bd("100"), "A plazos", 3, "Efectiva", null, 20, 26));
        }

        @Test
        void fechasFueraDeRango() {
            assertThrows(IllegalArgumentException.class, () -> plan(bd("100"), "En meses", 3, "Efectiva", null, 32, 26));
            assertThrows(IllegalArgumentException.class, () -> plan(bd("100"), "En meses", 3, "Efectiva", null, 20, 0));
        }

        @Test
        void nominalSinCapitalizacion() {
            assertThrows(IllegalArgumentException.class, () -> plan(bd("100"), "En meses", 3, "Nominal", null, 20, 26));
        }
    }
}
