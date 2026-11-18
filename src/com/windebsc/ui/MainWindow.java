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
    private JButton btnDoItForMe;
    private JButton btnRestorePoint;
    private JButton btnDisableTelemetry;
    private JButton btnRemoveBloatware;
    private JButton btnOptimizeServices;
    private JButton btnDisableBingCopilot;
    private JButton btnCleanTempCache;
    private JButton btnOptimizeNetwork;
    private JButton btnOptimizeVisuals;
    private JButton btnManageEdge;
    private JButton btnManageOneDrive;

    // Seccion 2: Configuracion y Utilidades
    private JButton btnConfigureExplorer;
    private JButton btnConfigurePower;
    private JButton btnConfigureWindowsUpdate;
    private JButton btnInstallEssentials;
    private JButton btnPrivacyPermissions;
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

        btnDoItForMe = new JButton("Hazlo por mi");
        btnDoItForMe.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDoItForMe.setMaximumSize(new Dimension(240, 36));
        btnDoItForMe.setFocusPainted(false);
        btnDoItForMe.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDoItForMe.addActionListener(e -> promptDoItForMe());

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

        btnManageOneDrive = new JButton("Gestionar OneDrive");
        btnManageOneDrive.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnManageOneDrive.setMaximumSize(new Dimension(240, 34));
        btnManageOneDrive.setFocusPainted(false);
        btnManageOneDrive.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnManageOneDrive.addActionListener(e -> promptOneDriveAction());

        debloatPanel.add(btnDoItForMe);
        debloatPanel.add(Box.createVerticalStrut(8));
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setMaximumSize(new Dimension(240, 2));
        debloatPanel.add(sep);
        debloatPanel.add(Box.createVerticalStrut(8));
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
        debloatPanel.add(Box.createVerticalStrut(6));
        debloatPanel.add(btnManageOneDrive);
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

        btnInstallEssentials = new JButton("Instalar Apps Esenciales...");
        btnInstallEssentials.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnInstallEssentials.setMaximumSize(new Dimension(240, 34));
        btnInstallEssentials.setFocusPainted(false);
        btnInstallEssentials.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnInstallEssentials.addActionListener(e -> promptInstallEssentials());

        btnPrivacyPermissions = new JButton("Privacidad y Permisos...");
        btnPrivacyPermissions.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPrivacyPermissions.setMaximumSize(new Dimension(240, 34));
        btnPrivacyPermissions.setFocusPainted(false);
        btnPrivacyPermissions.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnPrivacyPermissions.addActionListener(e -> promptPrivacyPermissions());

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
        configPanel.add(Box.createVerticalStrut(8));
        configPanel.add(btnInstallEssentials);
        configPanel.add(Box.createVerticalStrut(8));
        configPanel.add(btnPrivacyPermissions);
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
     * Dialogo interactivo con casillas organizadas por categorias para instalar apps via winget.
     */
    private void promptInstallEssentials() {
        String[] categories = {
                "Navegadores y Comunicacion",
                "Multimedia y Audio",
                "Gaming y Launchers",
                "Hardware y Monitorizacion",
                "Utilidades y Desarrollo"
        };

        String[][][] categorizedApps = {
                {
                        {"Mozilla.Firefox", "Mozilla Firefox"},
                        {"Brave.Brave", "Brave Browser"},
                        {"Discord.Discord", "Discord"}
                },
                {
                        {"VideoLAN.VLC", "VLC Media Player"},
                        {"OBSProject.OBSStudio", "OBS Studio"},
                        {"VB-Audio.Voicemeeter", "Voicemeeter"}
                },
                {
                        {"Valve.Steam", "Steam"},
                        {"EpicGames.EpicGamesLauncher", "Epic Games Launcher"}
                },
                {
                        {"REALiX.HWiNFO", "HWiNFO"},
                        {"Guru3D.Afterburner", "MSI Afterburner"},
                        {"CPUID.CPU-Z", "CPU-Z"},
                        {"TechPowerUp.GPU-Z", "GPU-Z"}
                },
                {
                        {"7zip.7zip", "7-Zip (Compresor)"},
                        {"Notepad++.Notepad++", "Notepad++"},
                        {"voidtools.Everything", "Everything (Buscador)"},
                        {"ShareX.ShareX", "ShareX (Capturas)"},
                        {"Microsoft.PowerToys", "Microsoft PowerToys"},
                        {"Git.Git", "Git"},
                        {"Microsoft.VisualStudioCode", "Visual Studio Code"}
                }
        };

        List<JCheckBox> allCheckboxes = new ArrayList<>();
        List<String> allIds = new ArrayList<>();

        JPanel categoriesGrid = new JPanel(new GridLayout(0, 2, 12, 10));
        categoriesGrid.setBorder(new EmptyBorder(6, 6, 6, 6));

        for (int c = 0; c < categories.length; c++) {
            JPanel catPanel = new JPanel();
            catPanel.setLayout(new BoxLayout(catPanel, BoxLayout.Y_AXIS));
            catPanel.setBorder(BorderFactory.createTitledBorder(categories[c]));

            for (String[] app : categorizedApps[c]) {
                JCheckBox cb = new JCheckBox(app[1]);
                cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                catPanel.add(cb);
                allCheckboxes.add(cb);
                allIds.add(app[0]);
            }
            catPanel.add(Box.createVerticalGlue());
            categoriesGrid.add(catPanel);
        }

        JScrollPane scrollPane = new JScrollPane(categoriesGrid);
        scrollPane.setPreferredSize(new Dimension(600, 390));
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        scrollPane.setBorder(null);

        JPanel container = new JPanel(new BorderLayout(8, 10));
        container.setBorder(new EmptyBorder(6, 6, 6, 6));
        container.add(new JLabel("Selecciona las aplicaciones que deseas instalar desde repositorios oficiales (winget):"), BorderLayout.NORTH);
        container.add(scrollPane, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(
                this,
                container,
                "Instalador de Aplicaciones Esenciales",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            List<String> selected = new ArrayList<>();
            for (int i = 0; i < allCheckboxes.size(); i++) {
                if (allCheckboxes.get(i).isSelected()) {
                    selected.add(allIds.get(i));
                }
            }

            if (selected.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "No has seleccionado ninguna aplicacion para instalar.",
                        "Sin seleccion",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

            runScriptAsync(new File("scripts/install-essentials.ps1"), "-Packages", String.join(",", selected));
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
     * Ventana emergente interactiva para seleccionar la accion sobre Microsoft OneDrive.
     */
    private void promptOneDriveAction() {
        String[] options = {"Debloat / Desactivar", "Desinstalar", "Reinstalar", "Cancelar"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "¿Qué quieres hacer con Microsoft OneDrive?\n\n" +
                "• Debloat: Desactiva el inicio automático y la sincronización en segundo plano.\n" +
                "• Desinstalar: Ejecuta el desinstalador oficial y oculta su icono del Explorador.\n" +
                "• Reinstalar: Restaura directivas y reinstala OneDrive limpiamente.\n",
                "Gestión de Microsoft OneDrive",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        File scriptFile = new File("scripts/manage-onedrive.ps1");

        switch (choice) {
            case 0:
                runScriptAsync(scriptFile, "-Action", "Debloat");
                break;
            case 1:
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "¿Estás seguro de que deseas desinstalar Microsoft OneDrive?\n" +
                        "Se detendrá la sincronización en la nube y se ocultará del Explorador.",
                        "Confirmar desinstalación de OneDrive",
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
     * Dialogo interactivo para configurar bloqueos universales de privacidad de hardware/datos.
     */
    private void promptPrivacyPermissions() {
        JCheckBox chkCamera = new JCheckBox("Bloquear acceso universal a la Camara", true);
        JCheckBox chkMicrophone = new JCheckBox("Bloquear acceso universal al Microfono", true);
        JCheckBox chkLocation = new JCheckBox("Bloquear acceso universal a la Ubicacion", true);
        JCheckBox chkDiagnostics = new JCheckBox("Bloquear acceso a Diagnosticos de Aplicaciones", true);
        JCheckBox chkReset = new JCheckBox("Restaurar permisos por defecto (Permitir todo)", false);

        chkReset.addActionListener(e -> {
            boolean reset = chkReset.isSelected();
            chkCamera.setEnabled(!reset);
            chkMicrophone.setEnabled(!reset);
            chkLocation.setEnabled(!reset);
            chkDiagnostics.setEnabled(!reset);
        });

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(new JLabel("Selecciona los bloqueos universales de privacidad que deseas aplicar:"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(chkCamera);
        panel.add(chkMicrophone);
        panel.add(chkLocation);
        panel.add(chkDiagnostics);
        panel.add(new JSeparator());
        panel.add(chkReset);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Proteccion Universal de Privacidad",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            File script = new File("scripts/configure-privacy-permissions.ps1");
            if (chkReset.isSelected()) {
                runScriptAsync(script, "-ResetDefaults");
            } else {
                List<String> args = new ArrayList<>();
                if (chkCamera.isSelected()) args.add("-BlockCamera");
                if (chkMicrophone.isSelected()) args.add("-BlockMicrophone");
                if (chkLocation.isSelected()) args.add("-BlockLocation");
                if (chkDiagnostics.isSelected()) args.add("-BlockDiagnostics");

                if (!args.isEmpty()) {
                    runScriptAsync(script, args.toArray(new String[0]));
                }
            }
        }
    }

    /**
     * Ventana de advertencia y ejecucion automatizada en cadena de todo el bloque de Debloat.
     */
    private void promptDoItForMe() {
        String warningMessage =
                "¡ATENCION! Se aplicaran automaticamente TODAS las herramientas de Debloat y Optimizacion:\n\n" +
                "  1. Crear Punto de Restauracion (seguridad previa)\n" +
                "  2. Desactivar Telemetria completa\n" +
                "  3. Eliminar Bloatware UWP (con lista blanca de seguridad)\n" +
                "  4. Optimizar Servicios secundarios\n" +
                "  5. Desactivar Bing, Copilot y Widgets\n" +
                "  6. Limpiar Temporales y Cache (Windows Update, WER, Papelera)\n" +
                "  7. Optimizar Red y QoS (desactivar P2P Delivery Optimization)\n" +
                "  8. Optimizar Rendimiento Visual y Gaming (Game DVR, Mouse 1:1)\n" +
                "  9. Debloat de Microsoft Edge y Apps en segundo plano\n" +
                " 10. Debloat de Microsoft OneDrive\n\n" +
                "Nota: Las utilidades opcionales de la pestana 'Configuracion' (Explorador,\n" +
                "Energia, Windows Update, Tema Oscuro, Privacidad de hardware y Apps)\n" +
                "NO se modificaran y quedan bajo tu eleccion.\n\n" +
                "¿Deseas continuar y ejecutar toda la secuencia?";

        int confirm = JOptionPane.showConfirmDialog(
                this,
                warningMessage,
                "Hazlo por mi - Advertencia de Ejecucion Completa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            List<String[]> steps = new ArrayList<>();
            steps.add(new String[]{"scripts/restore-point.ps1"});
            steps.add(new String[]{"scripts/disable-telemetry.ps1"});
            steps.add(new String[]{"scripts/remove-bloatware.ps1"});
            steps.add(new String[]{"scripts/optimize-services.ps1"});
            steps.add(new String[]{"scripts/disable-bing-copilot.ps1"});
            steps.add(new String[]{"scripts/clean-temp-cache.ps1"});
            steps.add(new String[]{"scripts/optimize-network.ps1"});
            steps.add(new String[]{"scripts/optimize-performance-visuals.ps1"});
            steps.add(new String[]{"scripts/manage-edge.ps1", "-Action", "Debloat"});
            steps.add(new String[]{"scripts/manage-onedrive.ps1", "-Action", "Debloat"});

            runScriptSequenceAsync(steps);
        }
    }

    /**
     * Ejecuta una secuencia de scripts de forma ordenada en un unico hilo secundario.
     */
    private void runScriptSequenceAsync(List<String[]> steps) {
        setButtonsEnabled(false);
        appendConsole("==================================================\n");
        appendConsole("INICIANDO MODO 'HAZLO POR MI' (" + steps.size() + " modulos)...\n");
        appendConsole("==================================================\n\n");

        new Thread(() -> {
            int total = steps.size();
            for (int i = 0; i < total; i++) {
                String[] step = steps.get(i);
                File scriptFile = new File(step[0]);
                String[] args = new String[step.length - 1];
                System.arraycopy(step, 1, args, 0, args.length);

                int stepIndex = i + 1;
                String argsDisplay = args.length > 0 ? " " + String.join(" ", args) : "";

                SwingUtilities.invokeLater(() -> {
                    appendConsole(">>> [" + stepIndex + "/" + total + "] Ejecutando: " + scriptFile.getName() + argsDisplay + "\n");
                    appendConsole("--------------------------------------------------\n");
                });

                if (!scriptFile.exists()) {
                    SwingUtilities.invokeLater(() ->
                            appendConsole("Error: No se encontro " + scriptFile.getAbsolutePath() + " (omitiendo).\n\n"));
                    continue;
                }

                try {
                    int exitCode = runner.runScript(scriptFile, args, line ->
                            SwingUtilities.invokeLater(() -> appendConsole("  " + line + "\n")));

                    SwingUtilities.invokeLater(() ->
                            appendConsole("Finalizado paso " + stepIndex + " (codigo " + exitCode + ")\n\n"));
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() ->
                            appendConsole("Error en paso " + stepIndex + ": " + ex.getMessage() + "\n\n"));
                }
            }

            SwingUtilities.invokeLater(() -> {
                appendConsole("==================================================\n");
                appendConsole("MODO 'HAZLO POR MI' COMPLETADO.\n");
                appendConsole("==================================================\n\n");
                setButtonsEnabled(true);
            });
        }).start();
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
        btnDoItForMe.setEnabled(enabled);
        btnRestorePoint.setEnabled(enabled);
        btnDisableTelemetry.setEnabled(enabled);
        btnRemoveBloatware.setEnabled(enabled);
        btnOptimizeServices.setEnabled(enabled);
        btnDisableBingCopilot.setEnabled(enabled);
        btnCleanTempCache.setEnabled(enabled);
        btnOptimizeNetwork.setEnabled(enabled);
        btnOptimizeVisuals.setEnabled(enabled);
        btnManageEdge.setEnabled(enabled);
        btnManageOneDrive.setEnabled(enabled);

        btnConfigureExplorer.setEnabled(enabled);
        btnConfigurePower.setEnabled(enabled);
        btnConfigureWindowsUpdate.setEnabled(enabled);
        btnInstallEssentials.setEnabled(enabled);
        btnPrivacyPermissions.setEnabled(enabled);
        switchDarkMode.setEnabled(enabled);

        btnClear.setEnabled(enabled);
    }
}
