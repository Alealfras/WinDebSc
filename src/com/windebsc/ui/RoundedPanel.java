package com.windebsc.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Panel con esquinas redondeadas y borde sutil para tarjetas de contenido.
 */
public class RoundedPanel extends JPanel {

    private final int arc;
    private Color borderColor;

    public RoundedPanel(int arc, Color backgroundColor, Color borderColor) {
        this.arc = arc;
        this.borderColor = borderColor;
        setOpaque(false);
        setBackground(backgroundColor);
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Shape round = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.setColor(getBackground());
        g2.fill(round);

        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(round);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
