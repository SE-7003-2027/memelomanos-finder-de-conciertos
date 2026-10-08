package com.memelomanos.finderconciertos.config;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.util.LinkedHashMap;
import java.util.Map;

public class PuntajeTestListener implements TestExecutionListener {

    private Map<String, int[]> puntajesPorCategoria = new LinkedHashMap<>();
    private int totalPruebas = 0;
    private int totalExitosas = 0;
    private TestPlan currentTestPlan;

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        this.currentTestPlan = testPlan;
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (testIdentifier.isTest()) {
            String categoria = "Desconocido";
            if (currentTestPlan != null) {
                TestIdentifier parent = currentTestPlan.getParent(testIdentifier).orElse(null);
                if (parent != null) {
                    categoria = parent.getDisplayName(); // Usa el @DisplayName si existe
                }
            }

            puntajesPorCategoria.putIfAbsent(categoria, new int[]{0, 0});
            puntajesPorCategoria.get(categoria)[0]++; // Suma total
            totalPruebas++;
            
            if (testExecutionResult.getStatus() == TestExecutionResult.Status.SUCCESSFUL) {
                puntajesPorCategoria.get(categoria)[1]++; // Suma éxito
                totalExitosas++;
            }
        }
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        if (totalPruebas == 0) return;
        
        System.out.println("\n===================================================================");
        System.out.println("   RESUMEN DE LOS TESTS - BY MEMELOMANOS: FINDER DE CONCIERTOS   ");
        System.out.println("===================================================================");
        
        for (Map.Entry<String, int[]> entry : puntajesPorCategoria.entrySet()) {
            String categoria = entry.getKey();
            int total = entry.getValue()[0];
            int exitos = entry.getValue()[1];
            int porcentaje = (int) Math.round((double) exitos / total * 100);
            
            String icono = porcentaje == 100 ? "\u001B[32m Completo\u001B[0m" : (porcentaje >= 50 ? "\u001B[33m Completitud parcial\u001B[0m" : "\u001B[31m Incompleto\u001B[0m");
            
            System.out.printf(" %s %-45s : %d/%d (%d%%)%n", icono, categoria, exitos, total, porcentaje);
        }
        
        System.out.println("-------------------------------------------------------------------");
        int porcentajeTotal = (int) Math.round((double) totalExitosas / totalPruebas * 100);
        String iconoTotal = porcentajeTotal == 100 ? "\u001B[32m Completo\u001B[0m" : (porcentajeTotal >= 70 ? "\u001B[33m Completitud parcial\u001B[0m" : "\u001B[31m Incompleto\u001B[0m");
        System.out.printf(" %s PUNTAJE TOTAL DE LA APLICACIÓN                       : %d/%d (%d%%)%n", iconoTotal, totalExitosas, totalPruebas, porcentajeTotal);
        System.out.println("===================================================================\n");
    }
}
