package com.windebsc.ui;

import com.windebsc.core.ScriptRunner;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana principal de la aplicacion WinDebSc.
 * Organiza las funciones en dos secciones: Debloat/Optimizacion y Configuracion/Utilidades.
 * Utiliza ejecucion asincrona para mantener la interfaz receptiva durante la ejecucion de scripts.
 */
public class MainWindow extends JFrame {

    private final ScriptRunner runner = new ScriptRunner();
    private JTextArea consoleArea;

    // Seccion 1: Debloat y Optimizacion
    private JButton btnRestorePoint;
    private JButton btnDisableTelemetry;
    private JButton btnRemoveBloatware;
    private JButton btnOptimizeServices;
    private JButton btnDisableBingCopilot;
    private JButton btnCleanTempCache;
    private JButton btnOptimizeNetwork;
    private JButton btnOptimizeVisuals;
    private JButton btnManageEdge;

    // Seccion 2: Configuracion y Utilidades
    private JButton btnConfigureExplorer;
    private JButton btnConfigurePower;
    private JButton btnConfigureWindowsUpdate;
    private JToggleButton switchDarkMode;

    // Control global
    private JButton btnClear;

    public MainWindow() {
        setTitle("WinDebSc - Suite de Optimizacion y Configuracion Windows");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 660);
        setMinimumSize(new Dimension(750, 500));
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        // Panel principal con borde y distribucion BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // 1. Cabecera (Titulo y descripcion)
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("WinDebSc");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel subtitleLabel = new JLabel("Herramienta de automatizacion, privacidad, optimizacion y configuracion de Windows");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.DARK_GRAY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. Panel lateral con pestanas (Debloat/Optimizacion vs Configuracion/Utilidades)
        JPanel leftPanel = new JPanel(new BorderLayout(0, 8));
        leftPanel.setPreferredSize(new Dimension(270, 0));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // --- PESTANA 1: DEBLOAT Y OPTIMIZACION ---
        JPanel debloatPanel = new JPanel();
        debloatPanel.setLayout(new BoxLayout(debloatPanel, BoxLayout.Y_AXIS));
        debloatPanel.setBorder(new EmptyBorder(10, 8, 10, 8));

        btnRestorePoint = createActionButton("Crear Punto Restauracion", "scripts/restore-point.ps1");
        btnDisableTelemetry = createActionButton("Desactivar Telemetria", "scripts/disable-telemetry.ps1");
        btnRemoveBloatware = createActionButton("Eliminar Bloatware UWP", "scripts/remove-bloatware.ps1");
        btnOptimizeServices = createActionButton("Optimizar Servicios", "scripts/optimize-services.ps1");
        btnDisableBingCopilot = createActionButton("Desactivar Bing / Copilot", "scripts/disable-bing-copilot.ps1");
        btnCleanTempCache = createActionButton("Limpiar Temporales y Cache", "scripts/clean-temp-cache.ps1");
        btnOptimizeNetwork = createActionButton("Optimizar Red / QoS", "scripts/optimize-network.ps1");
        btnOptimizeVisuals = createActionButton("Rendimiento Visual y Gaming", "scripts/optimize-performance-visuals.ps1");

        btnManageEdge = new JButton("Gestionar Microsoft Edge");
        btnManageEdge.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnManageEdge.setMaximumSize(new Dimension(240, 34));
        btnManageEdge.setFocusPainted(false);
        btnManageEdge.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnManageEdge.addActionListener(e -> promptEdgeAction());

        debloatPanel.add(btnRestorePoint);
        debloatPanel.add(Box.createVerticalStrut(6));
        debloatPanel.add(btnDisableTelemetry);
        debloatPanel.add(Box.createVerticalStrut(6));
        debloatPanel.add(btnRemoveBloatware);
        debloatPanel.add(Box.createVerticalStrut(6));
        debloatPanel.add(btnOptimizeServices);
        debloatPanel.add(Box.createVerticalStrut(6));
        debloatPanel.add(btnDisableBingCopilot);
        debloatPanel.add(Box.createVerticalStrut(6));
        debloatPanel.add(btnCleanTempCache);
        debloatPanel.add(Box.createVerticalStrut(6));
        debloatPanel.add(btnOptimizeNetwork);
        debloatPanel.add(Box.createVerticalStrut(6));
        debloatPanel.add(btnOptimizeVisuals);
        debloatPanel.add(Box.createVerticalStrut(6));
        debloatPanel.add(btnManageEdge);
        debloatPanel.add(Box.createVerticalGlue());

        JScrollPane debloatScroll = new JScrollPane(debloatPanel);
        debloatScroll.setBorder(null);
        debloatScroll.getVerticalScrollBar().setUnitIncrement(12);

        tabbedPane.addTab("Debloat y Optimizacion", debloatScroll);

        // --- PESTANA 2: CONFIGURACION Y UTILIDADES ---
        JPanel configPanel = new JPanel();
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        configPanel.setBorder(new EmptyBorder(12, 8, 12, 8));

        btnConfigureExplorer = createActionButton("Explorador de Archivos", "scripts/configure-explorer.ps1");
        btnConfigurePower = createActionButton("Energia y Rendimiento", "scripts/configure-power-plan.ps1");

        btnConfigureWindowsUpdate = new JButton("Configurar Windows Update...");
        btnConfigureWindowsUpdate.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnConfigureWindowsUpdate.setMaximumSize(new Dimension(240, 34));
        btnConfigureWindowsUpdate.setFocusPainted(false);
        btnConfigureWindowsUpdate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnConfigureWindowsUpdate.addActionListener(e -> promptWindowsUpdateAction());

        // Switch Modo Oscuro / Claro
        JPanel switchPanel = new JPanel(new BorderLayout(5, 5));
        switchPanel.setMaximumSize(new Dimension(240, 50));
        switchPanel.setBorder(BorderFactory.createTitledBorder("Tema del Sistema"));

        switchDarkMode = new JToggleButton("Modo Oscuro: [ OFF ]");
        switchDarkMode.setFont(new Font("Segoe UI", Font.BOLD, 12));
        switchDarkMode.setFocusPainted(false);
        switchDarkMode.addActionListener(e -> {
            boolean isDark = switchDarkMode.isSelected();
            switchDarkMode.setText(isDark ? "Modo Oscuro: [ ON ]" : "Modo Oscuro: [ OFF ]");
            runScriptAsync(new File("scripts/configure-theme-dark.ps1"), "-Mode", isDark ? "Dark" : "Light");
        });
        switchPanel.add(switchDarkMode, BorderLayout.CENTER);

        configPanel.add(btnConfigureExplorer);
        configPanel.add(Box.createVerticalStrut(8));
        configPanel.add(btnConfigurePower);
        configPanel.add(Box.createVerticalStrut(8));
        configPanel.add(btnConfigureWindowsUpdate);
        configPanel.add(Box.createVerticalStrut(14));
        configPanel.add(switchPanel);
        configPanel.add(Box.createVerticalGlue());

        tabbedPane.addTab("Configuracion", configPanel);

        leftPanel.add(tabbedPane, BorderLayout.CENTER);

        // Boton para limpiar consola al pie del panel lateral
        btnClear = new JButton("Limpiar Consola");
        btnClear.setPreferredSize(new Dimension(240, 32));
        btnClear.addActionListener(e -> consoleArea.setText(""));
        leftPanel.add(btnClear, BorderLayout.SOUTH);

        mainPanel.add(leftPanel, BorderLayout.WEST);

        // 3. Consola central integrada (Salida de PowerShell en tiempo real)
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Registro de Ejecucion"));

        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        consoleArea.setBackground(new Color(30, 30, 30));
        consoleArea.setForeground(new Color(220, 220, 220));
        consoleArea.setCaretColor(Color.WHITE);
        consoleArea.setText("Bienvenido a WinDebSc.\nSelecciona una opcion o cambia de pestana para comenzar.\n\n");

        JScrollPane scrollPane = new JScrollPane(consoleArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JButton createActionButton(String text, String scriptPath) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(240, 34));
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        button.addActionListener(e -> runScriptAsync(new File(scriptPath)));
        return button;
    }

    /**
     * Dialogo interactivo para seleccionar que directivas de Windows Update configurar.
     */
    private void promptWindowsUpdateAction() {
        JCheckBox chkDrivers = new JCheckBox("Bloquear instalacion automatica de drivers (GPU, chipset)", true);
        JCheckBox chkNotify = new JCheckBox("Modo notificar antes de descargar/instalar (evitar reinicios)", true);
        JCheckBox chkReset = new JCheckBox("Restaurar configuracion por defecto de Windows", false);

        chkReset.addActionListener(e -> {
            boolean reset = chkReset.isSelected();
            chkDrivers.setEnabled(!reset);
            chkNotify.setEnabled(!reset);
        });

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(new JLabel("Selecciona que directivas deseas aplicar a Windows Update:"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(chkDrivers);
        panel.add(chkNotify);
        panel.add(chkReset);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Configuracion de Windows Update",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            File script = new File("scripts/configure-windows-update.ps1");
            if (chkReset.isSelected()) {
                runScriptAsync(script, "-ResetDefaults");
            } else {
                List<String> args = new ArrayList<>();
                if (chkDrivers.isSelected()) args.add("-ExcludeDrivers");
                if (chkNotify.isSelected()) args.add("-NotifyOnly");

                if (!args.isEmpty()) {
                    runScriptAsync(script, args.toArray(new String[0]));
                }
            }
        }
    }

    /**
     * Ventana emergente interactiva para seleccionar la accion sobre Microsoft Edge.
     */
    private void promptEdgeAction() {
        String[] options = {"Debloat / Optimizar", "Desinstalar", "Reinstalar", "Cancelar"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "¿Qué quieres hacer con Microsoft Edge?\n\n" +
                "• Debloat: Desactiva aceleración de inicio, procesos de fondo y telemetría.\n" +
                "• Desinstalar: Elimina Edge del sistema y bloquea su reinstalación por Windows Update.\n" +
                "• Reinstalar: Descarga e instala la versión oficial limpia de Microsoft Edge.\n",
                "Gestión de Microsoft Edge",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        File scriptFile = new File("scripts/manage-edge.ps1");

        switch (choice) {
            case 0:
                runScriptAsync(scriptFile, "-Action", "Debloat");
                break;
            case 1:
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "¿Estás seguro de que deseas desinstalar Microsoft Edge?\n" +
                        "Asegúrate de contar con otro navegador web instalado previamente.",
                        "Confirmar desinstalación de Edge",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    runScriptAsync(scriptFile, "-Action", "Uninstall");
                }
                break;
            case 2:
                runScriptAsync(scriptFile, "-Action", "Reinstall");
                break;
            default:
                // Cancelar
                break;
        }
    }

    /**
     * Ejecuta el script en un hilo en segundo plano para evitar bloquear la interfaz grafica.
     */
    private void runScriptAsync(File scriptFile, String... args) {
        if (!scriptFile.exists()) {
            appendConsole("Error: No se encontro el archivo de script: " + scriptFile.getAbsolutePath() + "\n");
            return;
        }

        setButtonsEnabled(false);
        appendConsole("==================================================\n");
        String argsDisplay = (args != null && args.length > 0) ? " " + String.join(" ", args) : "";
        appendConsole("Ejecutando: " + scriptFile.getName() + argsDisplay + "...\n");
        appendConsole("--------------------------------------------------\n");

        // Hilo de trabajo en segundo plano
        new Thread(() -> {
            try {
                int exitCode = runner.runScript(scriptFile, args, line -> {
                    // Actualizacion segura en el hilo de interfaz (EDT)
                    SwingUtilities.invokeLater(() -> appendConsole(line + "\n"));
                });

                SwingUtilities.invokeLater(() -> {
                    appendConsole("--------------------------------------------------\n");
                    appendConsole("Proceso finalizado con codigo de salida: " + exitCode + "\n\n");
                    setButtonsEnabled(true);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    appendConsole("Error critico al ejecutar el script: " + ex.getMessage() + "\n\n");
                    setButtonsEnabled(true);
                });
            }
        }).start();
    }

    private void appendConsole(String text) {
        consoleArea.append(text);
        consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
    }

    private void setButtonsEnabled(boolean enabled) {
        btnRestorePoint.setEnabled(enabled);
        btnDisableTelemetry.setEnabled(enabled);
        btnRemoveBloatware.setEnabled(enabled);
        btnOptimizeServices.setEnabled(enabled);
        btnDisableBingCopilot.setEnabled(enabled);
        btnCleanTempCache.setEnabled(enabled);
        btnOptimizeNetwork.setEnabled(enabled);
        btnOptimizeVisuals.setEnabled(enabled);
        btnManageEdge.setEnabled(enabled);

        btnConfigureExplorer.setEnabled(enabled);
        btnConfigurePower.setEnabled(enabled);
        btnConfigureWindowsUpdate.setEnabled(enabled);
        switchDarkMode.setEnabled(enabled);

        btnClear.setEnabled(enabled);
    }
}
