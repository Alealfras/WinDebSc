package com.windebsc;

import com.windebsc.core.ScriptRunner;

import java.io.File;
import java.util.Scanner;

public class Main {

    private static final ScriptRunner runner = new ScriptRunner();

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("                   WinDebSc                       ");
        System.out.println("==================================================");

        // Si se pasa una ruta como argumento, se ejecuta directamente
        if (args.length > 0) {
            executeScript(new File(args[0]));
            return;
        }

        // Modo interactivo en consola
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("Selecciona una opcion:");
            System.out.println("1. Crear punto de restauracion (restore-point.ps1)");
            System.out.println("2. Desactivar telemetria completa (disable-telemetry.ps1)");
            System.out.println("0. Salir");
            System.out.print("> ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    executeScript(new File("scripts/restore-point.ps1"));
                    break;
                case "2":
                    executeScript(new File("scripts/disable-telemetry.ps1"));
                    break;
                case "0":
                    System.out.println("Saliendo de WinDebSc...");
                    running = false;
                    break;
                default:
                    System.out.println("Opcion no valida. Introduce un numero.");
                    break;
            }
        }
    }

    private static void executeScript(File script) {
        if (!script.exists()) {
            System.err.println("No se encontro el script: " + script.getAbsolutePath());
            return;
        }

        System.out.println();
        System.out.println("Ejecutando: " + script.getName() + "...");
        System.out.println("--------------------------------------------------");

        try {
            int exitCode = runner.runScript(script, line -> {
                System.out.println("  " + line);
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
