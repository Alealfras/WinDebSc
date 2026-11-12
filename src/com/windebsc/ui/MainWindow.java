package com.windebsc.ui;

import com.windebsc.core.ScriptRunner;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

/**
 * Ventana principal de la aplicacion WinDebSc.
 * Utiliza ejecucion asincrona para mantener la interfaz receptiva durante la ejecucion de scripts.
 */
public class MainWindow extends JFrame {

    private final ScriptRunner runner = new ScriptRunner();
    private JTextArea consoleArea;
    private JButton btnRestorePoint;
    private JButton btnDisableTelemetry;
    private JButton btnRemoveBloatware;
    private JButton btnOptimizeServices;
    private JButton btnDisableBingCopilot;
    private JButton btnCleanTempCache;
    private JButton btnOptimizeNetwork;
    private JButton btnManageEdge;
    private JButton btnClear;

    public MainWindow() {
        setTitle("WinDebSc - Suite de Optimizacion Windows");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(860, 680);
        setMinimumSize(new Dimension(650, 520));
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

        JLabel subtitleLabel = new JLabel("Herramienta de automatizacion, privacidad y optimizacion de Windows");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.DARK_GRAY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. Panel lateral izquierdo de acciones (Botones)
        JPanel actionsPanel = new JPanel();
        actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.Y_AXIS));
        actionsPanel.setPreferredSize(new Dimension(240, 0));
        actionsPanel.setBorder(BorderFactory.createTitledBorder("Modulos de Optimizacion"));

        btnRestorePoint = createActionButton("Crear Punto Restauracion", "scripts/restore-point.ps1");
        btnDisableTelemetry = createActionButton("Desactivar Telemetria", "scripts/disable-telemetry.ps1");
        btnRemoveBloatware = createActionButton("Eliminar Bloatware UWP", "scripts/remove-bloatware.ps1");
        btnOptimizeServices = createActionButton("Optimizar Servicios", "scripts/optimize-services.ps1");
        btnDisableBingCopilot = createActionButton("Desactivar Bing / Copilot", "scripts/disable-bing-copilot.ps1");
        btnCleanTempCache = createActionButton("Limpiar Temporales y Cache", "scripts/clean-temp-cache.ps1");
        btnOptimizeNetwork = createActionButton("Optimizar Red / QoS", "scripts/optimize-network.ps1");

        btnManageEdge = new JButton("Gestionar Microsoft Edge");
        btnManageEdge.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnManageEdge.setMaximumSize(new Dimension(220, 36));
        btnManageEdge.setFocusPainted(false);
        btnManageEdge.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnManageEdge.addActionListener(e -> promptEdgeAction());

        actionsPanel.add(Box.createVerticalStrut(10));
        actionsPanel.add(btnRestorePoint);
        actionsPanel.add(Box.createVerticalStrut(8));
        actionsPanel.add(btnDisableTelemetry);
        actionsPanel.add(Box.createVerticalStrut(8));
        actionsPanel.add(btnRemoveBloatware);
        actionsPanel.add(Box.createVerticalStrut(8));
        actionsPanel.add(btnOptimizeServices);
        actionsPanel.add(Box.createVerticalStrut(8));
        actionsPanel.add(btnDisableBingCopilot);
        actionsPanel.add(Box.createVerticalStrut(8));
        actionsPanel.add(btnCleanTempCache);
        actionsPanel.add(Box.createVerticalStrut(8));
        actionsPanel.add(btnOptimizeNetwork);
        actionsPanel.add(Box.createVerticalStrut(8));
        actionsPanel.add(btnManageEdge);
        actionsPanel.add(Box.createVerticalGlue());

        // Boton para limpiar consola
        btnClear = new JButton("Limpiar Consola");
        btnClear.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnClear.setMaximumSize(new Dimension(220, 32));
        btnClear.addActionListener(e -> consoleArea.setText(""));
        actionsPanel.add(btnClear);
        actionsPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(actionsPanel, BorderLayout.WEST);

        // 3. Consola central integrada (Salida de PowerShell en tiempo real)
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Registro de Ejecucion"));

        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        consoleArea.setBackground(new Color(30, 30, 30));
        consoleArea.setForeground(new Color(220, 220, 220));
        consoleArea.setCaretColor(Color.WHITE);
        consoleArea.setText("Bienvenido a WinDebSc.\nSelecciona una opcion para comenzar.\n\n");

        JScrollPane scrollPane = new JScrollPane(consoleArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JButton createActionButton(String text, String scriptPath) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(220, 36));
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        button.addActionListener(e -> runScriptAsync(new File(scriptPath)));
        return button;
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
        btnManageEdge.setEnabled(enabled);
        btnClear.setEnabled(enabled);
    }
}
