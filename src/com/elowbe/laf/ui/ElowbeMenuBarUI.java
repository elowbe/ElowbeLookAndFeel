package com.elowbe.laf.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicMenuBarUI;

import com.elowbe.laf.util.PaintUtils;

public class ElowbeMenuBarUI extends BasicMenuBarUI {
    public static ComponentUI createUI(JComponent component) {
        return new ElowbeMenuBarUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        menuBar.setOpaque(false);
    }

    @Override
    public void paint(Graphics graphics, JComponent component) {
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            ElowbeMenuPainter.paintMenuBarBackground(g2, component);
        } finally {
            g2.dispose();
        }
        super.paint(graphics, component);
    }
}
