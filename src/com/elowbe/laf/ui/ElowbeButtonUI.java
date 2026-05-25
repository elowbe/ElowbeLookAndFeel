package com.elowbe.laf.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbePalette;
import com.elowbe.laf.util.PaintUtils;

public class ElowbeButtonUI extends BasicButtonUI {
    public static ComponentUI createUI(JComponent component) {
        return new ElowbeButtonUI();
    }

    @Override
    protected void installDefaults(AbstractButton button) {
        super.installDefaults(button);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(true);
        button.setBorderPainted(false);
    }

    @Override
    public void paint(Graphics graphics, JComponent component) {
        AbstractButton button = (AbstractButton) component;
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            paintBackground(g2, button);
        } finally {
            g2.dispose();
        }
        Color old = button.getForeground();
        button.setForeground(foreground(button));
        try {
            super.paint(graphics, component);
        } finally {
            button.setForeground(old);
        }
    }

    @Override
    protected void paintText(Graphics graphics, AbstractButton button, java.awt.Rectangle textRect, String text) {
        super.paintText(graphics, button, textRect, text);
    }

    private void paintBackground(Graphics2D g2, AbstractButton button) {
        ElowbePalette palette = PaintUtils.palette();
        String variant = variant(button);
        int radius = radius(button, variant);
        Color background = background(button, palette, variant);
        Color border = border(button, palette, variant);

        if (!"ghost".equals(variant) && !"link".equals(variant)) {
            PaintUtils.fillRound(g2, 0, 0, button.getWidth(), button.getHeight(), radius, background);
            PaintUtils.drawRound(g2, 0, 0, button.getWidth(), button.getHeight(), radius, border);
        } else if (button.getModel().isRollover() || button.getModel().isSelected()) {
            PaintUtils.fillRound(g2, 0, 0, button.getWidth(), button.getHeight(), radius, hoverSurface(palette));
        }
    }

    private Color background(AbstractButton button, ElowbePalette palette, String variant) {
        if ("primary".equals(variant)) {
            return PaintUtils.buttonBackground(button, palette.primary,
                    PaintUtils.mix(palette.primary, palette.background, 0.10f),
                    PaintUtils.mix(palette.primary, palette.background, 0.18f));
        }
        if ("destructive".equals(variant)) {
            return PaintUtils.buttonBackground(button, palette.destructive,
                    PaintUtils.mix(palette.destructive, palette.background, 0.10f),
                    PaintUtils.mix(palette.destructive, palette.background, 0.18f));
        }
        if ("outline".equals(variant)) {
            return PaintUtils.buttonBackground(button, palette.background, hoverSurface(palette), pressedSurface(palette));
        }
        return PaintUtils.buttonBackground(button, palette.secondary, hoverSurface(palette), pressedSurface(palette));
    }

    private Color hoverSurface(ElowbePalette palette) {
        return ElowbeDefaults.theme().isDark() ? palette.border : palette.accent;
    }

    private Color pressedSurface(ElowbePalette palette) {
        return ElowbeDefaults.theme().isDark() ? palette.input : palette.pressed;
    }

    private Color foreground(AbstractButton button) {
        ElowbePalette palette = PaintUtils.palette();
        String variant = variant(button);
        Color color;
        if ("primary".equals(variant)) {
            color = palette.primaryForeground;
        } else if ("destructive".equals(variant)) {
            color = palette.destructiveForeground;
        } else {
            color = palette.foreground;
        }
        return PaintUtils.buttonForeground(button, color);
    }

    private Color border(AbstractButton button, ElowbePalette palette, String variant) {
        if ("primary".equals(variant) || "destructive".equals(variant)) {
            return new Color(0, 0, 0, 0);
        }
        if (!button.isEnabled()) {
            return palette.disabled;
        }
        return palette.border;
    }

    private String variant(AbstractButton button) {
        Object variant = button.getClientProperty(ElowbeDefaults.BUTTON_VARIANT_KEY);
        if (variant == null) {
            variant = button.getClientProperty(ElowbeDefaults.STYLE_KEY);
        }
        return variant == null ? "secondary" : variant.toString();
    }

    private int radius(AbstractButton button, String variant) {
        if ("pill".equals(variant) || Boolean.TRUE.equals(button.getClientProperty(ElowbeDefaults.ROUNDED_KEY))) {
            return ElowbeDefaults.RADIUS_PILL;
        }
        return PaintUtils.radius(button, ElowbeDefaults.RADIUS_MD);
    }
}
