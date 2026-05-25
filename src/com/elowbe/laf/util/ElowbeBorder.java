package com.elowbe.laf.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;

public class ElowbeBorder extends AbstractBorder {
    private static final long serialVersionUID = 1L;

    private final Color color;
    private final int radius;
    private final Insets insets;

    public ElowbeBorder(Color color, int radius, Insets insets) {
        this.color = color;
        this.radius = radius;
        this.insets = insets;
    }

    @Override
    public void paintBorder(Component component, Graphics graphics, int x, int y, int width, int height) {
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            PaintUtils.drawRound(g2, x, y, width, height, radius, color);
        } finally {
            g2.dispose();
        }
    }

    @Override
    public Insets getBorderInsets(Component component) {
        return (Insets) insets.clone();
    }

    @Override
    public Insets getBorderInsets(Component component, Insets targetInsets) {
        targetInsets.top = insets.top;
        targetInsets.left = insets.left;
        targetInsets.bottom = insets.bottom;
        targetInsets.right = insets.right;
        return targetInsets;
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}
