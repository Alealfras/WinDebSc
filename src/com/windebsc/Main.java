package com.windebsc;

import com.windebsc.core.ScriptRunner;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("        WinDebSc - Suite de Optimizacion Windows  ");
        System.out.println("==================================================");
        System.out.println();

        File script = new File("scripts/restore-point.ps1");
        if (!script.exists()) {
            System.err.println("[ERROR] No se encontro el script: " + script.getAbsolutePath());
            return;
        }

        ScriptRunner runner = new ScriptRunner();
        System.out.println("[JAVA] Ejecutando: " + script.getName() + "...");
        System.out.println("--------------------------------------------------");

        try {
            int exitCode = runner.runScript(script, line -> {
                System.out.println("  [PS] " + line);
            });

            System.out.println("--------------------------------------------------");
            System.out.println("[JAVA] Proceso finalizado con codigo de salida: " + exitCode);
            if (exitCode == 0) {
                System.out.println("[JAVA] Operacion completada con exito.");
            } else {
                System.out.println("[JAVA] Advertencia o error durante la ejecucion (codigo != 0).");
            }
        } catch (Exception e) {
            System.err.println("[JAVA ERROR] Fallo critico al ejecutar el script: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
