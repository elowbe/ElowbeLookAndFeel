package com.elowbe.laf.ui;

import java.awt.FontMetrics;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.font.GlyphVector;

import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicProgressBarUI;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbePalette;
import com.elowbe.laf.util.PaintUtils;

public class ElowbeProgressBarUI extends BasicProgressBarUI {
    public static ComponentUI createUI(JComponent component) {
        return new ElowbeProgressBarUI();
    }

    @Override
    public void installUI(JComponent component) {
        super.installUI(component);
        component.setOpaque(false);
        component.setFocusable(false);
        component.setBorder(new EmptyBorder(0, 0, 0, 0));
    }

    @Override
    protected void paintDeterminate(Graphics graphics, JComponent component) {
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            ElowbePalette palette = PaintUtils.palette();
            Insets insets = progressBar.getInsets();
            int x = insets.left;
            int y = insets.top;
            int width = progressBar.getWidth() - insets.left - insets.right;
            int height = progressBar.getHeight() - insets.top - insets.bottom;
            int amount = getAmountFull(insets, width, height);
            PaintUtils.fillRound(g2, x, y, width, height, ElowbeDefaults.RADIUS_PILL, palette.secondary);
            PaintUtils.fillRound(g2, x, y, amount, height, ElowbeDefaults.RADIUS_PILL, palette.primary);
            paintLabel(g2, component);
        } finally {
            g2.dispose();
        }
    }

    @Override
    protected void paintIndeterminate(Graphics graphics, JComponent component) {
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            ElowbePalette palette = PaintUtils.palette();
            boxRect = getBox(boxRect);
            PaintUtils.fillRound(g2, 0, 0, progressBar.getWidth(), progressBar.getHeight(),
                    ElowbeDefaults.RADIUS_PILL, palette.secondary);
            if (boxRect != null) {
                PaintUtils.fillRound(g2, boxRect.x, boxRect.y, boxRect.width, boxRect.height,
                        ElowbeDefaults.RADIUS_PILL, palette.primary);
            }
            paintLabel(g2, component);
        } finally {
            g2.dispose();
        }
    }

    private void paintLabel(Graphics2D g2, JComponent component) {
        if (!progressBar.isStringPainted()) {
            return;
        }
        String text = progressBar.getString().toUpperCase();
        FontMetrics metrics = g2.getFontMetrics(progressBar.getFont());
        int x = (component.getWidth() - metrics.stringWidth(text)) / 2;
        int y = (component.getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
        g2.setFont(progressBar.getFont());
        GlyphVector glyphs = progressBar.getFont().createGlyphVector(g2.getFontRenderContext(), text);
        Shape shape = glyphs.getOutline(x, y);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(PaintUtils.palette().primary);
        g2.draw(shape);
        g2.setColor(PaintUtils.palette().primaryForeground);
        g2.fill(shape);
    }
}
