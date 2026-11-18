package com.windebsc.ui;

import com.windebsc.core.ScriptRunner;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Ventana principal moderna tipo Dashboard para WinDebSc.
 * Combina barra lateral oscura, tarjetas con degradado, navegacion por vistas
 * y consola integrada con ejecucion asincrona segura en EDT.
 */
public class MainWindow extends JFrame {

    private static final Color SIDEBAR_BG = new Color(19, 23, 34);
    private static final Color SIDEBAR_ACTIVE = new Color(45, 49, 78);
    private static final Color CANVAS_BG = new Color(245, 247, 251);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color CARD_BORDER = new Color(226, 232, 240);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color ACCENT_INDIGO = new Color(99, 102, 241);

    private static final String VIEW_DASHBOARD = "DASHBOARD";
    private static final String VIEW_DEBLOAT = "DEBLOAT";
    private static final String VIEW_CONFIG = "CONFIG";
    private static final String VIEW_CONSOLE = "CONSOLE";
    private static final String VIEW_SEARCH = "SEARCH";

    private final ScriptRunner runner = new ScriptRunner();

    // Layout y navegacion
    private CardLayout cardLayout;
    private JPanel centerCardsPanel;
    private JButton navDashboard;
    private JButton navDebloat;
    private JButton navConfig;
    private JButton navEssentials;
    private JButton navConsole;
    private JTextField searchField;

    // Controles interactivos y estado
    private final List<AbstractButton> actionControls = new ArrayList<>();
    private final List<GradientCard> heroCards = new ArrayList<>();
    private JButton btnDoItForMeSidebar;
    private JToggleButton switchDarkMode;
    private JButton btnClear;
    private JLabel statusBadgeLabel;
    private JLabel quickStatusValueLabel;
    private JProgressBar progressBar;

    // Consola compartida y actividad reciente
    private JTextArea consoleArea;
    private JScrollPane consoleScrollPane;
    private JPanel dashboardConsoleSlot;
    private JPanel fullConsoleSlot;
    private JPanel recentActivityListPanel;
    private final List<String> recentActivities = new ArrayList<>();

    // Catalogo de modulos para busqueda dinamica
    private final List<ModuleEntry> allModules = new ArrayList<>();
    private JPanel searchResultsGrid;

    private static class ModuleEntry {
        final String title;
        final String description;
        final String category;
        final Runnable action;

        ModuleEntry(String title, String description, String category, Runnable action) {
            this.title = title;
            this.description = description;
            this.category = category;
            this.action = action;
        }
    }

    public MainWindow() {
        setTitle("WinDebSc - Suite de Optimizacion y Configuracion Windows");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        registerModules();
        initComponents();
        switchView(VIEW_DASHBOARD, navDashboard);
    }

    private void registerModules() {
        // Debloat y Optimizacion
        allModules.add(new ModuleEntry("Crear Punto de Restauracion", "Genera un punto de seguridad previo en Windows.", "Debloat",
                () -> runScriptAsync(new File("scripts/restore-point.ps1"))));
        allModules.add(new ModuleEntry("Desactivar Telemetria", "Bloquea DiagTrack, CEIP y envio de datos de diagnostico.", "Debloat",
                () -> runScriptAsync(new File("scripts/disable-telemetry.ps1"))));
        allModules.add(new ModuleEntry("Eliminar Bloatware UWP", "Purga apps preinstaladas protegiendo el Shell con whitelist.", "Debloat",
                () -> runScriptAsync(new File("scripts/remove-bloatware.ps1"))));
        allModules.add(new ModuleEntry("Optimizar Servicios", "Deshabilita servicios secundarios obsoletos (Fax, RetailDemo...).", "Debloat",
                () -> runScriptAsync(new File("scripts/optimize-services.ps1"))));
        allModules.add(new ModuleEntry("Desactivar Bing y Copilot", "Elimina busquedas web en Inicio, Copilot y Widgets WebView2.", "Debloat",
                () -> runScriptAsync(new File("scripts/disable-bing-copilot.ps1"))));
        allModules.add(new ModuleEntry("Limpiar Temporales y Cache", "Purga %TEMP%, cache de Windows Update, WER y Papelera.", "Debloat",
                () -> runScriptAsync(new File("scripts/clean-temp-cache.ps1"))));
        allModules.add(new ModuleEntry("Optimizar Red y QoS", "Desactiva P2P Delivery Optimization y reserva QoS del 20%.", "Debloat",
                () -> runScriptAsync(new File("scripts/optimize-network.ps1"))));
        allModules.add(new ModuleEntry("Rendimiento Visual y Gaming", "Apaga Game DVR, aceleracion del raton y retardo de menus.", "Debloat",
                () -> runScriptAsync(new File("scripts/optimize-performance-visuals.ps1"))));
        allModules.add(new ModuleEntry("Gestionar Microsoft Edge", "Debloat, desinstalacion forzada o reinstalacion oficial.", "Debloat",
                this::promptEdgeAction));
        allModules.add(new ModuleEntry("Gestionar OneDrive", "Desactivar inicio, desinstalar del Explorador o reinstalar.", "Debloat",
                this::promptOneDriveAction));

        // Configuracion y Utilidades
        allModules.add(new ModuleEntry("Explorador de Archivos", "Muestra extensiones, ocultos y restaura menu clasico Win 11.", "Configuracion",
                () -> runScriptAsync(new File("scripts/configure-explorer.ps1"))));
        allModules.add(new ModuleEntry("Energia y Rendimiento", "Activa Maximo Rendimiento, quita hibernacion y suspension USB.", "Configuracion",
                () -> runScriptAsync(new File("scripts/configure-power-plan.ps1"))));
        allModules.add(new ModuleEntry("Configurar Windows Update", "Excluye drivers y activa modo notificacion sin reinicios.", "Configuracion",
                this::promptWindowsUpdateAction));
        allModules.add(new ModuleEntry("Privacidad y Permisos", "Bloqueo universal de camara, microfono, ubicacion y diagnosticos.", "Configuracion",
                this::promptPrivacyPermissions));
        allModules.add(new ModuleEntry("Instalar Apps Esenciales", "Despliegue limpio por categorias mediante winget.", "Configuracion",
                this::promptInstallEssentials));
    }

    private void initComponents() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(SIDEBAR_BG);

        // =====================================================================
        // 1. BARRA LATERAL OSCURA (IZQUIERDA)
        // =====================================================================
        JPanel sidebar = createSidebar();
        rootPanel.add(sidebar, BorderLayout.WEST);

        // =====================================================================
        // 2. LIENZO PRINCIPAL CLARO CON ESQUINAS REDONDEADAS (DERECHA)
        // =====================================================================
        RoundedPanel mainCanvas = new RoundedPanel(24, CANVAS_BG, null);
        mainCanvas.setLayout(new BorderLayout(0, 14));
        mainCanvas.setBorder(new EmptyBorder(18, 24, 18, 24));

        // Wrapper oscuro para dar efecto visual de marco redondeado en la union con el sidebar
        JPanel canvasWrapper = new JPanel(new BorderLayout());
        canvasWrapper.setBackground(SIDEBAR_BG);
        canvasWrapper.setBorder(new EmptyBorder(10, 0, 10, 10));
        canvasWrapper.add(mainCanvas, BorderLayout.CENTER);

        // Barra superior (Buscador + Estado)
        JPanel topBar = createTopBar();
        mainCanvas.add(topBar, BorderLayout.NORTH);

        // Inicializar consola compartida
        initSharedConsole();

        // Contenedor de vistas con CardLayout
        cardLayout = new CardLayout();
        centerCardsPanel = new JPanel(cardLayout);
        centerCardsPanel.setOpaque(false);

        centerCardsPanel.add(createDashboardView(), VIEW_DASHBOARD);
        centerCardsPanel.add(createDebloatView(), VIEW_DEBLOAT);
        centerCardsPanel.add(createConfigView(), VIEW_CONFIG);
        centerCardsPanel.add(createFullConsoleView(), VIEW_CONSOLE);
        centerCardsPanel.add(createSearchView(), VIEW_SEARCH);

        mainCanvas.add(centerCardsPanel, BorderLayout.CENTER);
        rootPanel.add(canvasWrapper, BorderLayout.CENTER);

        setContentPane(rootPanel);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 16));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(235, 0));
        sidebar.setBorder(new EmptyBorder(22, 16, 18, 16));

        // Marca y Logo superior
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JPanel logoBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(168, 85, 247), getWidth(), getHeight(), new Color(236, 72, 153));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                String s = "W";
                g2.drawString(s, (getWidth() - fm.stringWidth(s)) / 2, ((getHeight() - fm.getHeight()) / 2) + fm.getAscent());
                g2.dispose();
            }
        };
        logoBadge.setPreferredSize(new Dimension(34, 34));
        logoBadge.setOpaque(false);

        JLabel brandLabel = new JLabel("WinDebSc");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandLabel.setForeground(Color.WHITE);

        brandPanel.add(logoBadge);
        brandPanel.add(brandLabel);

        // Menu de navegacion vertical
        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        navDashboard = createNavButton("  Inicio");
        navDebloat = createNavButton("  Debloat y Optimizacion");
        navConfig = createNavButton("  Configuracion");
        navEssentials = createNavButton("  Apps Esenciales");
        navConsole = createNavButton("  Consola en Vivo");

        navDashboard.addActionListener(e -> switchView(VIEW_DASHBOARD, navDashboard));
        navDebloat.addActionListener(e -> switchView(VIEW_DEBLOAT, navDebloat));
        navConfig.addActionListener(e -> switchView(VIEW_CONFIG, navConfig));
        navEssentials.addActionListener(e -> promptInstallEssentials());
        navConsole.addActionListener(e -> switchView(VIEW_CONSOLE, navConsole));

        navPanel.add(navDashboard);
        navPanel.add(Box.createVerticalStrut(6));
        navPanel.add(navDebloat);
        navPanel.add(Box.createVerticalStrut(6));
        navPanel.add(navConfig);
        navPanel.add(Box.createVerticalStrut(6));
        navPanel.add(navEssentials);
        navPanel.add(Box.createVerticalStrut(6));
        navPanel.add(navConsole);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.add(brandPanel, BorderLayout.NORTH);
        topWrapper.add(navPanel, BorderLayout.CENTER);

        // Tarjeta destacada inferior izquierda ("Hazlo por mi")
        RoundedPanel promoCard = new RoundedPanel(18, new Color(36, 33, 72), new Color(79, 70, 229, 120));
        promoCard.setLayout(new BorderLayout(0, 8));
        promoCard.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel promoTitle = new JLabel("Modo Automatico");
        promoTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        promoTitle.setForeground(Color.WHITE);

        JLabel promoDesc = new JLabel("<html><body style='width:165px'>Aplica todo el bloque de debloteo seguro en un solo clic.</body></html>");
        promoDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        promoDesc.setForeground(new Color(199, 210, 254));

        btnDoItForMeSidebar = new JButton("Hazlo por mi");
        btnDoItForMeSidebar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDoItForMeSidebar.setForeground(Color.WHITE);
        btnDoItForMeSidebar.setBackground(new Color(124, 58, 237));
        btnDoItForMeSidebar.setFocusPainted(false);
        btnDoItForMeSidebar.setBorderPainted(false);
        btnDoItForMeSidebar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDoItForMeSidebar.setPreferredSize(new Dimension(0, 34));
        btnDoItForMeSidebar.addActionListener(e -> promptDoItForMe());
        actionControls.add(btnDoItForMeSidebar);

        promoCard.add(promoTitle, BorderLayout.NORTH);
        promoCard.add(promoDesc, BorderLayout.CENTER);
        promoCard.add(btnDoItForMeSidebar, BorderLayout.SOUTH);

        sidebar.add(topWrapper, BorderLayout.NORTH);
        sidebar.add(promoCard, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setPreferredSize(new Dimension(200, 38));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(156, 163, 175));
        btn.setBackground(SIDEBAR_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void switchView(String viewName, JButton activeNav) {
        JButton[] navs = {navDashboard, navDebloat, navConfig, navEssentials, navConsole};
        for (JButton n : navs) {
            boolean isActive = (n == activeNav);
            n.setBackground(isActive ? SIDEBAR_ACTIVE : SIDEBAR_BG);
            n.setForeground(isActive ? Color.WHITE : new Color(156, 163, 175));
            n.setFont(new Font("Segoe UI", isActive ? Font.BOLD : Font.PLAIN, 13));
        }

        if (VIEW_DASHBOARD.equals(viewName) && dashboardConsoleSlot != null) {
            dashboardConsoleSlot.removeAll();
            dashboardConsoleSlot.add(consoleScrollPane, BorderLayout.CENTER);
            dashboardConsoleSlot.revalidate();
            dashboardConsoleSlot.repaint();
        } else if (VIEW_CONSOLE.equals(viewName) && fullConsoleSlot != null) {
            fullConsoleSlot.removeAll();
            fullConsoleSlot.add(consoleScrollPane, BorderLayout.CENTER);
            fullConsoleSlot.revalidate();
            fullConsoleSlot.repaint();
        }

        cardLayout.show(centerCardsPanel, viewName);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(16, 0));
        topBar.setOpaque(false);

        // Buscador redondeado
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(320, 36));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.putClientProperty("JTextField.placeholderText", "Buscar modulo, script o ajuste...");
        searchField.putClientProperty("JTextField.showClearButton", true);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onSearchChanged(); }
            @Override public void removeUpdate(DocumentEvent e) { onSearchChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { onSearchChanged(); }
        });

        // Panel derecho con indicador de estado y barra de progreso
        JPanel rightStatus = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightStatus.setOpaque(false);

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(110, 10));
        progressBar.setVisible(false);

        statusBadgeLabel = new JLabel("  Listo • Windows 10/11  ");
        statusBadgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusBadgeLabel.setForeground(new Color(55, 65, 81));
        statusBadgeLabel.setOpaque(true);
        statusBadgeLabel.setBackground(new Color(226, 232, 240));
        statusBadgeLabel.setBorder(new EmptyBorder(6, 10, 6, 10));

        rightStatus.add(progressBar);
        rightStatus.add(statusBadgeLabel);

        topBar.add(searchField, BorderLayout.WEST);
        topBar.add(rightStatus, BorderLayout.EAST);

        return topBar;
    }

    private void initSharedConsole() {
        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        consoleArea.setBackground(new Color(19, 23, 34));
        consoleArea.setForeground(new Color(226, 232, 240));
        consoleArea.setCaretColor(Color.WHITE);
        consoleArea.setBorder(new EmptyBorder(10, 12, 10, 12));
        consoleArea.setText("WinDebSc listo.\nSelecciona una tarjeta o navega por el menu lateral para ejecutar un modulo.\n\n");

        consoleScrollPane = new JScrollPane(consoleArea);
        consoleScrollPane.setBorder(null);
        consoleScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    private JPanel createDashboardView() {
        JPanel dashboard = new JPanel(new BorderLayout(0, 16));
        dashboard.setOpaque(false);

        // Saludo y 4 Tarjetas Hero con degradados
        JPanel topSection = new JPanel(new BorderLayout(0, 14));
        topSection.setOpaque(false);

        JPanel greetingPanel = new JPanel(new BorderLayout(0, 2));
        greetingPanel.setOpaque(false);
        JLabel helloLabel = new JLabel("Bienvenido a WinDebSc");
        helloLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        helloLabel.setForeground(TEXT_PRIMARY);

        JLabel subHelloLabel = new JLabel("Automatiza el debloteo, refuerza tu privacidad y configura Windows desde un panel central.");
        subHelloLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subHelloLabel.setForeground(TEXT_SECONDARY);

        greetingPanel.add(helloLabel, BorderLayout.NORTH);
        greetingPanel.add(subHelloLabel, BorderLayout.SOUTH);

        // Fila de 4 tarjetas degradadas
        JPanel cardsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        cardsRow.setOpaque(false);
        cardsRow.setPreferredSize(new Dimension(0, 165));

        GradientCard cardAuto = new GradientCard(
                "⚡", "Hazlo por mi", "Aplica todo el debloteo seguro en cadena.",
                new Color(124, 58, 237), new Color(99, 102, 241),
                this::promptDoItForMe
        );
        GradientCard cardPrivacy = new GradientCard(
                "🛡", "Privacidad Total", "Bloquea camara, microfono, ubicacion y telemetria.",
                new Color(16, 185, 129), new Color(13, 148, 136),
                this::promptPrivacyPermissions
        );
        GradientCard cardClean = new GradientCard(
                "🧹", "Limpieza Profunda", "Elimina bloatware UWP, temporales y cachés.",
                new Color(244, 63, 94), new Color(251, 113, 133),
                () -> switchView(VIEW_DEBLOAT, navDebloat)
        );
        GradientCard cardApps = new GradientCard(
                "📦", "Apps Esenciales", "Instala utilidades limpias via winget.",
                new Color(37, 99, 235), new Color(56, 189, 248),
                this::promptInstallEssentials
        );

        heroCards.add(cardAuto);
        heroCards.add(cardPrivacy);
        heroCards.add(cardClean);
        heroCards.add(cardApps);

        cardsRow.add(cardAuto);
        cardsRow.add(cardPrivacy);
        cardsRow.add(cardClean);
        cardsRow.add(cardApps);

        topSection.add(greetingPanel, BorderLayout.NORTH);
        topSection.add(cardsRow, BorderLayout.CENTER);

        // Seccion inferior: Terminal oscura (izq/centro) + Actividad y Stats (der)
        JPanel bottomSection = new JPanel(new BorderLayout(14, 0));
        bottomSection.setOpaque(false);

        // Tarjeta de consola oscura
        RoundedPanel terminalCard = new RoundedPanel(18, new Color(19, 23, 34), null);
        terminalCard.setLayout(new BorderLayout(0, 6));
        terminalCard.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel terminalHeader = new JPanel(new BorderLayout());
        terminalHeader.setOpaque(false);
        JLabel termTitle = new JLabel("Consola de Ejecucion en Vivo");
        termTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        termTitle.setForeground(new Color(203, 213, 225));

        btnClear = new JButton("Limpiar");
        btnClear.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnClear.setFocusPainted(false);
        btnClear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClear.addActionListener(e -> consoleArea.setText(""));
        actionControls.add(btnClear);

        terminalHeader.add(termTitle, BorderLayout.WEST);
        terminalHeader.add(btnClear, BorderLayout.EAST);

        dashboardConsoleSlot = new JPanel(new BorderLayout());
        dashboardConsoleSlot.setOpaque(false);
        dashboardConsoleSlot.add(consoleScrollPane, BorderLayout.CENTER);

        terminalCard.add(terminalHeader, BorderLayout.NORTH);
        terminalCard.add(dashboardConsoleSlot, BorderLayout.CENTER);

        // Panel lateral derecho: Actividad Reciente + Estadisticas Rapidas
        JPanel rightWidgets = new JPanel(new GridLayout(2, 1, 0, 12));
        rightWidgets.setOpaque(false);
        rightWidgets.setPreferredSize(new Dimension(245, 0));

        // Widget 1: Actividad Reciente
        RoundedPanel activityCard = new RoundedPanel(18, CARD_BG, CARD_BORDER);
        activityCard.setLayout(new BorderLayout(0, 8));
        activityCard.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel actTitle = new JLabel("Actividad Reciente");
        actTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        actTitle.setForeground(TEXT_PRIMARY);

        recentActivityListPanel = new JPanel();
        recentActivityListPanel.setOpaque(false);
        recentActivityListPanel.setLayout(new BoxLayout(recentActivityListPanel, BoxLayout.Y_AXIS));
        refreshRecentActivityUI();

        activityCard.add(actTitle, BorderLayout.NORTH);
        activityCard.add(recentActivityListPanel, BorderLayout.CENTER);

        // Widget 2: Estadisticas Rapidas
        RoundedPanel statsCard = new RoundedPanel(18, CARD_BG, CARD_BORDER);
        statsCard.setLayout(new BorderLayout(0, 8));
        statsCard.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel statsTitle = new JLabel("Resumen del Sistema");
        statsTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statsTitle.setForeground(TEXT_PRIMARY);

        JPanel statsGrid = new JPanel(new GridLayout(4, 2, 4, 6));
        statsGrid.setOpaque(false);
        addStatRow(statsGrid, "Modulos totales", "16");
        addStatRow(statsGrid, "Debloat / Opt.", "10");
        addStatRow(statsGrid, "Utilidades", "6");

        JLabel stKey = new JLabel("Estado actual");
        stKey.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        stKey.setForeground(TEXT_SECONDARY);
        quickStatusValueLabel = new JLabel("Listo");
        quickStatusValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        quickStatusValueLabel.setForeground(new Color(16, 185, 129));
        quickStatusValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        statsGrid.add(stKey);
        statsGrid.add(quickStatusValueLabel);

        statsCard.add(statsTitle, BorderLayout.NORTH);
        statsCard.add(statsGrid, BorderLayout.CENTER);

        rightWidgets.add(activityCard);
        rightWidgets.add(statsCard);

        bottomSection.add(terminalCard, BorderLayout.CENTER);
        bottomSection.add(rightWidgets, BorderLayout.EAST);

        dashboard.add(topSection, BorderLayout.NORTH);
        dashboard.add(bottomSection, BorderLayout.CENTER);

        return dashboard;
    }

    private void addStatRow(JPanel grid, String label, String value) {
        JLabel k = new JLabel(label);
        k.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        k.setForeground(TEXT_SECONDARY);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 12));
        v.setForeground(TEXT_PRIMARY);
        v.setHorizontalAlignment(SwingConstants.RIGHT);

        grid.add(k);
        grid.add(v);
    }

    private JPanel createDebloatView() {
        JPanel view = new JPanel(new BorderLayout(0, 12));
        view.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Modulos de Debloat y Optimizacion");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);

        JButton btnRunAll = new JButton("Hazlo por mi (Todo)");
        btnRunAll.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRunAll.setBackground(ACCENT_INDIGO);
        btnRunAll.setForeground(Color.WHITE);
        btnRunAll.setFocusPainted(false);
        btnRunAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRunAll.addActionListener(e -> promptDoItForMe());
        actionControls.add(btnRunAll);

        header.add(title, BorderLayout.WEST);
        header.add(btnRunAll, BorderLayout.EAST);

        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 12));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(4, 2, 12, 6));

        for (ModuleEntry entry : allModules) {
            if ("Debloat".equals(entry.category)) {
                grid.add(createModuleCard(entry, "Ejecutar"));
            }
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        view.add(header, BorderLayout.NORTH);
        view.add(scroll, BorderLayout.CENTER);
        return view;
    }

    private JPanel createConfigView() {
        JPanel view = new JPanel(new BorderLayout(0, 12));
        view.setOpaque(false);

        JLabel title = new JLabel("Configuracion y Utilidades del Sistema");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);

        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 12));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(4, 2, 12, 6));

        for (ModuleEntry entry : allModules) {
            if ("Configuracion".equals(entry.category)) {
                grid.add(createModuleCard(entry, "Abrir"));
            }
        }

        // Tarjeta especial para el Switch de Tema Oscuro (ON = Dark / OFF = Light)
        RoundedPanel themeCard = new RoundedPanel(16, CARD_BG, CARD_BORDER);
        themeCard.setLayout(new BorderLayout(8, 8));
        themeCard.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel tTitle = new JLabel("Tema del Sistema (Dark / Light)");
        tTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tTitle.setForeground(TEXT_PRIMARY);

        JLabel tDesc = new JLabel("<html><body style='width:220px'>Alterna globalmente el Modo Oscuro o Claro en aplicaciones y barra de Windows.</body></html>");
        tDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tDesc.setForeground(TEXT_SECONDARY);

        switchDarkMode = new JToggleButton("Modo Oscuro: [ OFF ]");
        switchDarkMode.setFont(new Font("Segoe UI", Font.BOLD, 12));
        switchDarkMode.setFocusPainted(false);
        switchDarkMode.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        switchDarkMode.addActionListener(e -> {
            boolean isDark = switchDarkMode.isSelected();
            switchDarkMode.setText(isDark ? "Modo Oscuro: [ ON ]" : "Modo Oscuro: [ OFF ]");
            runScriptAsync(new File("scripts/configure-theme-dark.ps1"), "-Mode", isDark ? "Dark" : "Light");
        });
        actionControls.add(switchDarkMode);

        JPanel textBlock = new JPanel(new BorderLayout(0, 4));
        textBlock.setOpaque(false);
        textBlock.add(tTitle, BorderLayout.NORTH);
        textBlock.add(tDesc, BorderLayout.CENTER);

        themeCard.add(textBlock, BorderLayout.CENTER);
        themeCard.add(switchDarkMode, BorderLayout.SOUTH);
        grid.add(themeCard);

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        view.add(title, BorderLayout.NORTH);
        view.add(scroll, BorderLayout.CENTER);
        return view;
    }

    private JPanel createFullConsoleView() {
        JPanel view = new JPanel(new BorderLayout(0, 12));
        view.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Consola de Ejecucion Completa");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);

        JButton btnClearFull = new JButton("Limpiar Registro");
        btnClearFull.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnClearFull.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClearFull.addActionListener(e -> consoleArea.setText(""));
        actionControls.add(btnClearFull);

        header.add(title, BorderLayout.WEST);
        header.add(btnClearFull, BorderLayout.EAST);

        RoundedPanel terminalCard = new RoundedPanel(18, new Color(19, 23, 34), null);
        terminalCard.setLayout(new BorderLayout());
        terminalCard.setBorder(new EmptyBorder(12, 12, 12, 12));

        fullConsoleSlot = new JPanel(new BorderLayout());
        fullConsoleSlot.setOpaque(false);
        terminalCard.add(fullConsoleSlot, BorderLayout.CENTER);

        view.add(header, BorderLayout.NORTH);
        view.add(terminalCard, BorderLayout.CENTER);
        return view;
    }

    private JPanel createSearchView() {
        JPanel view = new JPanel(new BorderLayout(0, 12));
        view.setOpaque(false);

        JLabel title = new JLabel("Resultados de Busqueda");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);

        searchResultsGrid = new JPanel(new GridLayout(0, 2, 12, 12));
        searchResultsGrid.setOpaque(false);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(searchResultsGrid, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        view.add(title, BorderLayout.NORTH);
        view.add(scroll, BorderLayout.CENTER);
        return view;
    }

    private void onSearchChanged() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            switchView(VIEW_DASHBOARD, navDashboard);
            return;
        }

        searchResultsGrid.removeAll();
        int matches = 0;
        for (ModuleEntry entry : allModules) {
            if (entry.title.toLowerCase().contains(query) || entry.description.toLowerCase().contains(query)) {
                searchResultsGrid.add(createModuleCard(entry, "Ejecutar"));
                matches++;
            }
        }

        if (matches == 0) {
            JLabel empty = new JLabel("No se encontraron modulos que coincidan con \"" + searchField.getText() + "\".");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            empty.setForeground(TEXT_SECONDARY);
            searchResultsGrid.add(empty);
        }

        searchResultsGrid.revalidate();
        searchResultsGrid.repaint();
        cardLayout.show(centerCardsPanel, VIEW_SEARCH);
    }

    private RoundedPanel createModuleCard(ModuleEntry entry, String buttonText) {
        RoundedPanel card = new RoundedPanel(16, CARD_BG, CARD_BORDER);
        card.setLayout(new BorderLayout(8, 8));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel title = new JLabel(entry.title);
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(TEXT_PRIMARY);

        JLabel desc = new JLabel("<html><body style='width:220px'>" + entry.description + "</body></html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        desc.setForeground(TEXT_SECONDARY);

        JButton btn = new JButton(buttonText);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(new Color(238, 242, 255));
        btn.setForeground(new Color(67, 56, 202));
        btn.addActionListener(e -> entry.action.run());
        actionControls.add(btn);

        JPanel textBlock = new JPanel(new BorderLayout(0, 4));
        textBlock.setOpaque(false);
        textBlock.add(title, BorderLayout.NORTH);
        textBlock.add(desc, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.add(btn);

        card.add(textBlock, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private void addRecentActivity(String name) {
        String time = new SimpleDateFormat("HH:mm").format(new Date());
        recentActivities.add(0, time + " • " + name);
        if (recentActivities.size() > 4) {
            recentActivities.remove(recentActivities.size() - 1);
        }
        refreshRecentActivityUI();
    }

    private void refreshRecentActivityUI() {
        if (recentActivityListPanel == null) return;
        recentActivityListPanel.removeAll();
        if (recentActivities.isEmpty()) {
            JLabel empty = new JLabel("Sin ejecuciones en esta sesion.");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            empty.setForeground(TEXT_SECONDARY);
            recentActivityListPanel.add(empty);
        } else {
            for (String item : recentActivities) {
                JLabel lbl = new JLabel(item);
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                lbl.setForeground(TEXT_PRIMARY);
                lbl.setBorder(new EmptyBorder(2, 0, 2, 0));
                recentActivityListPanel.add(lbl);
            }
        }
        recentActivityListPanel.revalidate();
        recentActivityListPanel.repaint();
    }

    // =========================================================================
    // DIALOGOS MODALES INTERACTIVOS
    // =========================================================================

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
                this, panel, "Configuracion de Windows Update",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
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
                this, container, "Instalador de Aplicaciones Esenciales",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            List<String> selected = new ArrayList<>();
            for (int i = 0; i < allCheckboxes.size(); i++) {
                if (allCheckboxes.get(i).isSelected()) {
                    selected.add(allIds.get(i));
                }
            }
            if (selected.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No has seleccionado ninguna aplicacion.", "Sin seleccion", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            runScriptAsync(new File("scripts/install-essentials.ps1"), "-Packages", String.join(",", selected));
        }
    }

    private void promptEdgeAction() {
        String[] options = {"Debloat / Optimizar", "Desinstalar", "Reinstalar", "Cancelar"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "¿Qué quieres hacer con Microsoft Edge?\n\n" +
                "• Debloat: Desactiva aceleración de inicio, procesos de fondo y telemetría.\n" +
                "• Desinstalar: Elimina Edge del sistema y bloquea su reinstalación por Windows Update.\n" +
                "• Reinstalar: Descarga e instala la versión oficial limpia de Microsoft Edge.\n",
                "Gestión de Microsoft Edge",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]
        );

        File scriptFile = new File("scripts/manage-edge.ps1");
        switch (choice) {
            case 0:
                runScriptAsync(scriptFile, "-Action", "Debloat");
                break;
            case 1:
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "¿Estás seguro de que deseas desinstalar Microsoft Edge?\nAsegúrate de contar con otro navegador instalado.",
                        "Confirmar desinstalación de Edge", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    runScriptAsync(scriptFile, "-Action", "Uninstall");
                }
                break;
            case 2:
                runScriptAsync(scriptFile, "-Action", "Reinstall");
                break;
            default:
                break;
        }
    }

    private void promptOneDriveAction() {
        String[] options = {"Debloat / Desactivar", "Desinstalar", "Reinstalar", "Cancelar"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "¿Qué quieres hacer con Microsoft OneDrive?\n\n" +
                "• Debloat: Desactiva el inicio automático y la sincronización en segundo plano.\n" +
                "• Desinstalar: Ejecuta el desinstalador oficial y oculta su icono del Explorador.\n" +
                "• Reinstalar: Restaura directivas y reinstala OneDrive limpiamente.\n",
                "Gestión de Microsoft OneDrive",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]
        );

        File scriptFile = new File("scripts/manage-onedrive.ps1");
        switch (choice) {
            case 0:
                runScriptAsync(scriptFile, "-Action", "Debloat");
                break;
            case 1:
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "¿Estás seguro de que deseas desinstalar Microsoft OneDrive?",
                        "Confirmar desinstalación de OneDrive", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    runScriptAsync(scriptFile, "-Action", "Uninstall");
                }
                break;
            case 2:
                runScriptAsync(scriptFile, "-Action", "Reinstall");
                break;
            default:
                break;
        }
    }

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
                this, panel, "Proteccion Universal de Privacidad",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
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
                "Nota: Las utilidades opcionales de 'Configuracion' (Explorador, Energia,\n" +
                "Windows Update, Tema Oscuro, Privacidad de hardware y Apps) NO se tocaran.\n\n" +
                "¿Deseas continuar y ejecutar toda la secuencia?";

        int confirm = JOptionPane.showConfirmDialog(
                this, warningMessage, "Hazlo por mi - Advertencia de Ejecucion Completa",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE
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

    // =========================================================================
    // EJECUCION ASINCRONA SEGURA (EDT)
    // =========================================================================

    private void runScriptAsync(File scriptFile, String... args) {
        if (!scriptFile.exists()) {
            appendConsole("Error: No se encontro el archivo de script: " + scriptFile.getAbsolutePath() + "\n");
            return;
        }

        addRecentActivity(scriptFile.getName());
        setControlsEnabled(false);
        appendConsole("==================================================\n");
        String argsDisplay = (args != null && args.length > 0) ? " " + String.join(" ", args) : "";
        appendConsole("Ejecutando: " + scriptFile.getName() + argsDisplay + "...\n");
        appendConsole("--------------------------------------------------\n");

        new Thread(() -> {
            try {
                int exitCode = runner.runScript(scriptFile, args, line ->
                        SwingUtilities.invokeLater(() -> appendConsole(line + "\n")));

                SwingUtilities.invokeLater(() -> {
                    appendConsole("--------------------------------------------------\n");
                    appendConsole("Proceso finalizado con codigo de salida: " + exitCode + "\n\n");
                    setControlsEnabled(true);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    appendConsole("Error critico al ejecutar el script: " + ex.getMessage() + "\n\n");
                    setControlsEnabled(true);
                });
            }
        }).start();
    }

    private void runScriptSequenceAsync(List<String[]> steps) {
        addRecentActivity("Hazlo por mi (" + steps.size() + " modulos)");
        setControlsEnabled(false);
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
                setControlsEnabled(true);
            });
        }).start();
    }

    private void appendConsole(String text) {
        consoleArea.append(text);
        consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
    }

    private void setControlsEnabled(boolean enabled) {
        for (AbstractButton btn : actionControls) {
            btn.setEnabled(enabled);
        }
        for (GradientCard card : heroCards) {
            card.setCardEnabled(enabled);
        }

        if (enabled) {
            statusBadgeLabel.setText("  Listo • Windows 10/11  ");
            statusBadgeLabel.setBackground(new Color(226, 232, 240));
            statusBadgeLabel.setForeground(new Color(55, 65, 81));
            quickStatusValueLabel.setText("Listo");
            quickStatusValueLabel.setForeground(new Color(16, 185, 129));
            progressBar.setIndeterminate(false);
            progressBar.setVisible(false);
        } else {
            statusBadgeLabel.setText("  Ejecutando...  ");
            statusBadgeLabel.setBackground(new Color(254, 243, 199));
            statusBadgeLabel.setForeground(new Color(146, 64, 14));
            quickStatusValueLabel.setText("Ejecutando");
            quickStatusValueLabel.setForeground(new Color(245, 158, 11));
            progressBar.setIndeterminate(true);
            progressBar.setVisible(true);
        }
    }
}
