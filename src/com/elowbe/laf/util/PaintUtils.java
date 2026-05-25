package com.elowbe.laf.util;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.RoundRectangle2D;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.UIManager;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbePalette;

public final class PaintUtils {
    private PaintUtils() {
    }

    public static Graphics2D prepare(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    public static ElowbePalette palette() {
        return ElowbeDefaults.palette();
    }

    public static int radius(JComponent component, int fallback) {
        Object rounded = component.getClientProperty(ElowbeDefaults.ROUNDED_KEY);
        if (rounded instanceof Number) {
            return ((Number) rounded).intValue();
        }
        if (rounded instanceof Boolean && ((Boolean) rounded).booleanValue()) {
            return ElowbeDefaults.RADIUS_PILL;
        }
        return fallback;
    }

    public static Shape roundedRect(int x, int y, int width, int height, int radius) {
        int actualRadius = Math.min(radius, Math.min(width, height));
        return new RoundRectangle2D.Float(x, y, width, height, actualRadius, actualRadius);
    }

    public static void fillRound(Graphics2D g2, int x, int y, int width, int height, int radius, Color color) {
        g2.setColor(color);
        g2.fill(roundedRect(x, y, width, height, radius));
    }

    public static void drawRound(Graphics2D g2, int x, int y, int width, int height, int radius, Color color) {
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(color);
        g2.draw(roundedRect(x+1, y+1, Math.max(0, width - 2), Math.max(0, height - 2), radius));
        g2.setStroke(oldStroke);
    }

    public static void paintFocusRing(Graphics2D g2, JComponent component, int radius) {
        if (!component.hasFocus()) {
            return;
        }
        Color ring = withAlpha(palette().ring, 70);
        g2.setColor(ring);
        g2.draw(roundedRect(2, 2, component.getWidth() - 5, component.getHeight() - 5, radius + 2));
        g2.setColor(withAlpha(ring, 45));
        g2.draw(roundedRect(1, 1, component.getWidth() - 3, component.getHeight() - 3, radius + 4));
    }

    public static Color buttonBackground(AbstractButton button, Color normal, Color hover, Color pressed) {
        if (!button.isEnabled()) {
            return palette().disabled;
        }
        if (button.getModel().isPressed() || button.getModel().isSelected()) {
            return pressed;
        }
        if (button.getModel().isRollover()) {
            return hover;
        }
        return normal;
    }

    public static Color buttonForeground(AbstractButton button, Color normal) {
        return button.isEnabled() ? normal : palette().disabledForeground;
    }

    public static Color resolveColor(Component component, String key, Color fallback) {
        Color color = UIManager.getColor(key);
        if (color != null) {
            return color;
        }
        return fallback == null ? component.getForeground() : fallback;
    }

    public static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, alpha)));
    }

    public static Color mix(Color first, Color second, float amount) {
        float clamped = Math.max(0f, Math.min(1f, amount));
        int r = Math.round(first.getRed() + (second.getRed() - first.getRed()) * clamped);
        int g = Math.round(first.getGreen() + (second.getGreen() - first.getGreen()) * clamped);
        int b = Math.round(first.getBlue() + (second.getBlue() - first.getBlue()) * clamped);
        int a = Math.round(first.getAlpha() + (second.getAlpha() - first.getAlpha()) * clamped);
        return new Color(r, g, b, a);
    }

    public static void withAlpha(Graphics2D g2, float alpha, Runnable painter) {
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        painter.run();
        g2.setComposite(old);
    }
}
