package com.elowbe.laf.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbePalette;
import com.elowbe.laf.util.PaintUtils;

public class ElowbeTabbedPaneUI extends BasicTabbedPaneUI {
    public static ComponentUI createUI(JComponent component) {
        return new ElowbeTabbedPaneUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        tabPane.setOpaque(false);
    }

    @Override
    protected void paintTabBackground(Graphics graphics, int tabPlacement, int tabIndex, int x, int y, int w, int h,
            boolean isSelected) {
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            ElowbePalette palette = PaintUtils.palette();
            Color fill = isSelected ? palette.card : palette.secondary;
            PaintUtils.fillRound(g2, x + 1, y + 1, w - 2, h - 2, ElowbeDefaults.RADIUS_MD, fill);
            if (isSelected) {
                PaintUtils.drawRound(g2, x + 1, y + 1, w - 2, h - 2, ElowbeDefaults.RADIUS_MD, palette.border);
            }
        } finally {
            g2.dispose();
        }
    }

    @Override
    protected void paintText(Graphics graphics, int tabPlacement, java.awt.Font font, java.awt.FontMetrics metrics,
            int tabIndex, String title, Rectangle textRect, boolean isSelected) {
        graphics.setColor(isSelected ? PaintUtils.palette().foreground : PaintUtils.palette().mutedForeground);
        graphics.setFont(font);
        graphics.drawString(title, textRect.x, textRect.y + metrics.getAscent());
    }

    @Override
    protected void paintContentBorder(Graphics graphics, int tabPlacement, int selectedIndex) {
    }

    @Override
    protected void paintTabBorder(Graphics graphics, int tabPlacement, int tabIndex, int x, int y, int w, int h,
            boolean isSelected) {
    }

    @Override
    protected void paintFocusIndicator(Graphics graphics, int tabPlacement, Rectangle[] rects, int tabIndex,
            Rectangle iconRect, Rectangle textRect, boolean isSelected) {
    }
}
