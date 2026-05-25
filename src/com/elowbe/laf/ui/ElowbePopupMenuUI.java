package com.elowbe.laf.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPopupMenuUI;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbePalette;
import com.elowbe.laf.util.PaintUtils;

public class ElowbePopupMenuUI extends BasicPopupMenuUI {
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    private final PopupMenuListener transparencyListener = new PopupTransparencyListener();

    public static ComponentUI createUI(JComponent component) {
        return new ElowbePopupMenuUI();
    }

    @Override
    public void installDefaults() {
        super.installDefaults();
        popupMenu.setOpaque(false);
        popupMenu.setBackground(TRANSPARENT);
        popupMenu.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        popupMenu.addPopupMenuListener(transparencyListener);
    }

    @Override
    protected void uninstallListeners() {
        popupMenu.removePopupMenuListener(transparencyListener);
        super.uninstallListeners();
    }

    @Override
    public void paint(Graphics graphics, JComponent component) {
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            ElowbePalette palette = PaintUtils.palette();
            PaintUtils.fillRound(g2, 0, 0, component.getWidth(), component.getHeight(),
                    ElowbeDefaults.RADIUS_MD, palette.popover);
            PaintUtils.drawRound(g2, 0, 0, component.getWidth(), component.getHeight(),
                    ElowbeDefaults.RADIUS_MD, palette.border);
        } finally {
            g2.dispose();
        }
        super.paint(graphics, component);
    }

    private void makePopupWindowTransparent() {
        if (popupMenu.getParent() instanceof JComponent) {
            ((JComponent) popupMenu.getParent()).setOpaque(false);
            popupMenu.getParent().setBackground(TRANSPARENT);
        }

        Window window = SwingUtilities.getWindowAncestor(popupMenu);
        if (window == null) {
            return;
        }

        //window.setBackground(TRANSPARENT);
        if (window instanceof RootPaneContainer) {
            Container contentPane = ((RootPaneContainer) window).getContentPane();
            if (contentPane instanceof JComponent) {
                ((JComponent) contentPane).setOpaque(false);
            }
            contentPane.setBackground(TRANSPARENT);
        }
    }

    private final class PopupTransparencyListener implements PopupMenuListener {
        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent event) {
            SwingUtilities.invokeLater(ElowbePopupMenuUI.this::makePopupWindowTransparent);
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent event) {
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent event) {
        }
    }
}
