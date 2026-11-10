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
     * Ejecuta un script de PowerShell ubicado en el disco.
     *
     * @param scriptFile Archivo .ps1 a ejecutar.
     * @param outputListener Callback funcional para procesar la salida en tiempo real.
     * @return Codigo de salida del proceso (0 indica exito).
     * @throws IOException Si ocurre un fallo de E/S al arrancar el proceso.
     * @throws InterruptedException Si el hilo es interrumpido mientras espera al proceso.
     */
    public int runScript(File scriptFile, Consumer<String> outputListener) throws IOException, InterruptedException {
        if (!scriptFile.exists()) {
            throw new IllegalArgumentException("El archivo de script no existe: " + scriptFile.getAbsolutePath());
        }

        // Construccion del comando de PowerShell:
        // -NoProfile: no carga los perfiles del usuario (arranque mucho mas rapido)
        // -ExecutionPolicy Bypass: permite ejecutar el script sin alterar la politica global del sistema
        // -File: ruta del script a ejecutar
        ProcessBuilder processBuilder = new ProcessBuilder(
                "powershell.exe",
                "-NoProfile",
                "-ExecutionPolicy", "Bypass",
                "-File", scriptFile.getAbsolutePath()
        );

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
