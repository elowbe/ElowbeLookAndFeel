package com.elowbe.laf.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPanelUI;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbePalette;
import com.elowbe.laf.util.PaintUtils;

public class ElowbePanelUI extends BasicPanelUI {
    public static ComponentUI createUI(JComponent component) {
        return new ElowbePanelUI();
    }

    @Override
    public void installUI(JComponent component) {
        super.installUI(component);
        component.setOpaque(false);
    }

    @Override
    public void paint(Graphics graphics, JComponent component) {
        ElowbePalette palette = PaintUtils.palette();
        Object style = component.getClientProperty(ElowbeDefaults.STYLE_KEY);
        boolean card = Boolean.TRUE.equals(component.getClientProperty(ElowbeDefaults.CARD_KEY)) || "card".equals(style);
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            if (card) {
                int radius = PaintUtils.radius(component, ElowbeDefaults.RADIUS_LG);
                PaintUtils.fillRound(g2, 0, 0, component.getWidth(), component.getHeight(), radius, palette.card);
                PaintUtils.drawRound(g2, 0, 0, component.getWidth(), component.getHeight(), radius, palette.border);
            } else {
                g2.setColor(component.getBackground() == null ? palette.background : component.getBackground());
                g2.fillRect(0, 0, component.getWidth(), component.getHeight());
            }
        } finally {
            g2.dispose();
        }
        super.paint(graphics, component);
    }
}
