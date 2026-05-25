package com.elowbe.laf.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicMenuUI;

import com.elowbe.laf.util.PaintUtils;

public class ElowbeMenuUI extends BasicMenuUI {
    private final MouseListener cursorListener = ElowbeCursorSupport.handCursorOnHover();

    public static ComponentUI createUI(JComponent component) {
        return new ElowbeMenuUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        menuItem.setOpaque(false);
        menuItem.setRolloverEnabled(true);
        defaultTextIconGap = 8;
        selectionBackground = PaintUtils.palette().accent;
        selectionForeground = PaintUtils.palette().foreground;
        acceleratorForeground = PaintUtils.palette().mutedForeground;
        acceleratorSelectionForeground = PaintUtils.palette().foreground;
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        menuItem.addMouseListener(cursorListener);
    }

    @Override
    protected void uninstallListeners() {
        menuItem.removeMouseListener(cursorListener);
        ElowbeCursorSupport.restoreCursor(menuItem);
        super.uninstallListeners();
    }

    @Override
    protected void paintBackground(Graphics graphics, JMenuItem menuItem, java.awt.Color bgColor) {
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            ElowbeMenuPainter.paintMenuSelection(g2, menuItem);
        } finally {
            g2.dispose();
        }
    }

    @Override
    protected void paintText(Graphics graphics, JMenuItem menuItem, java.awt.Rectangle textRect, String text) {
        graphics.setColor(menuItem.isEnabled() ? PaintUtils.palette().foreground : PaintUtils.palette().disabledForeground);
        super.paintText(graphics, menuItem, textRect, text);
    }
}
