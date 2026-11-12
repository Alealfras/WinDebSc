package com.windebsc;

import com.windebsc.core.ScriptRunner;
import com.windebsc.ui.MainWindow;

import javax.swing.*;
import java.io.File;
import java.util.Scanner;

public class Main {

    private static final ScriptRunner runner = new ScriptRunner();

    public static void main(String[] args) {
        // Si se pasa el argumento --cli, se utiliza el modo consola tradicional
        if (args.length > 0 && "--cli".equalsIgnoreCase(args[0])) {
            runConsoleMode();
            return;
        }

        // Si se pasa la ruta de un script directamente como argumento
        if (args.length > 0 && !args[0].startsWith("-")) {
            executeScript(new File(args[0]));
            return;
        }

        // Por defecto: Iniciar la interfaz grafica (GUI)
        launchGui();
    }

    private static void launchGui() {
        // Establecer el aspecto nativo del sistema operativo (Windows)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Si falla, se utiliza el aspecto estandar de Java
        }

        // Iniciar en el hilo de eventos de Swing (EDT)
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }

    private static void runConsoleMode() {
        System.out.println("==================================================");
        System.out.println("                   WinDebSc                       ");
        System.out.println("==================================================");

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("Selecciona una opcion:");
            System.out.println("1. Crear punto de restauracion (restore-point.ps1)");
            System.out.println("2. Desactivar telemetria completa (disable-telemetry.ps1)");
            System.out.println("3. Eliminar bloatware profundo (remove-bloatware.ps1)");
            System.out.println("4. Optimizar servicios del sistema (optimize-services.ps1)");
            System.out.println("5. Desactivar Bing, Copilot y Widgets (disable-bing-copilot.ps1)");
            System.out.println("6. Limpiar temporales y cache del sistema (clean-temp-cache.ps1)");
            System.out.println("7. Optimizar red y ancho de banda (optimize-network.ps1)");
            System.out.println("8. Gestionar Microsoft Edge (manage-edge.ps1)");
            System.out.println("9. Optimizar rendimiento visual y gaming (optimize-performance-visuals.ps1)");
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
                case "3":
                    executeScript(new File("scripts/remove-bloatware.ps1"));
                    break;
                case "4":
                    executeScript(new File("scripts/optimize-services.ps1"));
                    break;
                case "5":
                    executeScript(new File("scripts/disable-bing-copilot.ps1"));
                    break;
                case "6":
                    executeScript(new File("scripts/clean-temp-cache.ps1"));
                    break;
                case "7":
                    executeScript(new File("scripts/optimize-network.ps1"));
                    break;
                case "8":
                    executeScript(new File("scripts/manage-edge.ps1"));
                    break;
                case "9":
                    executeScript(new File("scripts/optimize-performance-visuals.ps1"));
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
