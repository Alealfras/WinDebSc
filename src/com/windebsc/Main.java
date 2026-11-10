package com.windebsc;
import com.windebsc.core.ScriptRunner;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("                   WinDebSc                       ");
        System.out.println("==================================================");
        System.out.println();

        File script = new File("scripts/restore-point.ps1");
        if (!script.exists()) {
            System.err.println("No se encontro el script: " + script.getAbsolutePath());
            return;
        }

        ScriptRunner runner = new ScriptRunner();
        System.out.println("Ejecutando: " + script.getName() + "...");
        System.out.println("--------------------------------------------------");

        try {
            int exitCode = runner.runScript(script, line -> {
                System.out.println("  [PS] " + line);
            });

            System.out.println("--------------------------------------------------");
            System.out.println("Proceso finalizado con codigo de salida: " + exitCode);
            if (exitCode == 0) {
                System.out.println("Operacion completada con exito.");
            } else {
                System.out.println("Advertencia o error durante la ejecucion (codigo != 0).");
            }
        } catch (Exception e) {
            System.err.println("Error critico al ejecutar el script: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
