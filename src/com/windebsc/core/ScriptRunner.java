package com.windebsc.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Gestor de ejecucion de scripts de PowerShell desde la JVM.
 * Utiliza ProcessBuilder para invocar el interprete nativo con politicas seguras.
 */
public class ScriptRunner {

    /**
     * Resuelve la ruta de un script tanto en ejecucion desde IDE/CLI como desde un .exe empaquetado (jpackage).
     */
    public static File resolveScript(String relativePath) {
        File direct = new File(relativePath);
        if (direct.exists()) {
            return direct;
        }

        try {
            File codeLocation = new File(ScriptRunner.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File baseDir = codeLocation.isFile() ? codeLocation.getParentFile() : codeLocation;

            // Caso 1: scripts/ junto al .jar o carpeta bin/
            File besideJar = new File(baseDir, relativePath);
            if (besideJar.exists()) {
                return besideJar;
            }

            // Caso 2: jpackage app-image (el .jar esta en /app/ y scripts/ junto al .exe en el nivel superior)
            if (baseDir.getParentFile() != null) {
                File besideExe = new File(baseDir.getParentFile(), relativePath);
                if (besideExe.exists()) {
                    return besideExe;
                }
            }
        } catch (Exception ignored) {
            // Fallback al archivo directo
        }

        return direct;
    }

    /**
     * Ejecuta un script de PowerShell ubicado en el disco sin argumentos adicionales.
     */
    public int runScript(File scriptFile, Consumer<String> outputListener) throws IOException, InterruptedException {
        return runScript(scriptFile, new String[0], outputListener);
    }

    /**
     * Ejecuta un script de PowerShell ubicado en el disco pasando argumentos opcionales.
     *
     * @param scriptFile Archivo .ps1 a ejecutar.
     * @param args Argumentos de linea de comandos para el script.
     * @param outputListener Callback funcional para procesar la salida en tiempo real.
     * @return Codigo de salida del proceso (0 indica exito).
     * @throws IOException Si ocurre un fallo de E/S al arrancar el proceso.
     * @throws InterruptedException Si el hilo es interrumpido mientras espera al proceso.
     */
    public int runScript(File scriptFile, String[] args, Consumer<String> outputListener) throws IOException, InterruptedException {
        if (!scriptFile.exists()) {
            throw new IllegalArgumentException("El archivo de script no existe: " + scriptFile.getAbsolutePath());
        }

        // Construccion dinamica del comando de PowerShell
        java.util.List<String> command = new java.util.ArrayList<>();
        command.add("powershell.exe");
        command.add("-NoProfile");
        command.add("-ExecutionPolicy");
        command.add("Bypass");
        command.add("-File");
        command.add(scriptFile.getAbsolutePath());

        if (args != null) {
            for (String arg : args) {
                if (arg != null && !arg.isEmpty()) {
                    command.add(arg);
                }
            }
        }

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        // Redirige los errores al mismo flujo de salida estandar para facilitar la lectura unificada
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        // Lectura continua de la salida estandar de PowerShell en tiempo real
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (outputListener != null) {
                    outputListener.accept(line);
                }
            }
        }

        return process.waitFor();
    }
}
