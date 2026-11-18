package com.windebsc.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Tarjeta interactiva con degradado de color, esquinas redondeadas y efecto hover.
 */
public class GradientCard extends JPanel {

    private final Color startColor;
    private final Color endColor;
    private final int arc = 22;
    private boolean hovered = false;
    private boolean cardEnabled = true;
    private Runnable onClick;

    public GradientCard(String iconText, String title, String description, Color startColor, Color endColor, Runnable onClick) {
        this.startColor = startColor;
        this.endColor = endColor;
        this.onClick = onClick;

        setOpaque(false);
        setLayout(new BorderLayout(6, 8));
        setBorder(new EmptyBorder(16, 16, 14, 16));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Icono superior en recuadro translúcido
        JLabel iconLabel = new JLabel(iconText, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setPreferredSize(new Dimension(38, 38));
        iconLabel.setOpaque(true);
        iconLabel.setBackground(new Color(255, 255, 255, 45));

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topRow.setOpaque(false);
        topRow.add(iconLabel);

        // Titulo y descripcion
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);

        JLabel descLabel = new JLabel("<html><body style='width:125px'>" + description + "</body></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLabel.setForeground(new Color(255, 255, 255, 220));

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(descLabel);

        // Flecha inferior derecha
        JLabel arrowLabel = new JLabel("→");
        arrowLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        arrowLabel.setForeground(new Color(255, 255, 255, 230));
        arrowLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        add(topRow, BorderLayout.NORTH);
        add(textPanel, BorderLayout.CENTER);
        add(arrowLabel, BorderLayout.SOUTH);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (cardEnabled) {
                    hovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (cardEnabled && onClick != null) {
                    onClick.run();
                }
            }
        });
    }

    public void setCardEnabled(boolean enabled) {
        this.cardEnabled = enabled;
        setCursor(enabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color c1 = hovered ? startColor.brighter() : startColor;
        Color c2 = hovered ? endColor.brighter() : endColor;

        if (!cardEnabled) {
            c1 = new Color(148, 163, 184);
            c2 = new Color(100, 116, 139);
        }

        GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
        g2.setPaint(gp);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arc, arc));

        if (hovered && cardEnabled) {
            g2.setColor(new Color(255, 255, 255, 90));
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, arc, arc));
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
