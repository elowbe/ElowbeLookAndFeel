package com.elowbe.laf.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.JTextField;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.JTextComponent;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbePalette;
import com.elowbe.laf.util.PaintUtils;

public class ElowbeTextFieldUI extends BasicTextFieldUI {
    public static ComponentUI createUI(JComponent component) {
        return new ElowbeTextFieldUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        getComponent().setOpaque(false);
    }

    @Override
    protected void paintSafely(Graphics graphics) {
        JTextComponent text = getComponent();
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            paintChrome(g2, text);
        } finally {
            g2.dispose();
        }
        super.paintSafely(graphics);
        paintPlaceholder(graphics, text);
    }

    protected void paintChrome(Graphics2D g2, JTextComponent text) {
        if (SwingUtilities.getAncestorOfClass(JSpinner.class, text) != null) {
            return;
        }
        ElowbePalette palette = PaintUtils.palette();
        int radius = PaintUtils.radius(text, ElowbeDefaults.RADIUS_MD);
        Color fill = text.isEnabled() ? palette.card : palette.disabled;
        Color border = palette.input;
        PaintUtils.fillRound(g2, 0, 0, text.getWidth(), text.getHeight(), radius, fill);
        PaintUtils.drawRound(g2, 0, 0, text.getWidth(), text.getHeight(), radius, border);
    }

    protected void paintPlaceholder(Graphics graphics, JTextComponent text) {
        Object placeholder = text.getClientProperty(ElowbeDefaults.PLACEHOLDER_KEY);
        if (placeholder == null || text.getDocument().getLength() > 0) {
            return;
        }
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            g2.setColor(PaintUtils.palette().mutedForeground);
            g2.setFont(text.getFont());
            Insets insets = text.getInsets();
            Rectangle visible = text.getVisibleRect();
            int baseline = visible.y + insets.top + text.getFontMetrics(text.getFont()).getAscent();
            if (text instanceof JTextField) {
                baseline = (text.getHeight() - text.getFontMetrics(text.getFont()).getHeight()) / 2
                        + text.getFontMetrics(text.getFont()).getAscent();
            }
            g2.drawString(placeholder.toString(), insets.left, baseline);
        } finally {
            g2.dispose();
        }
    }
}
