package com.elowbe.laf.ui;

import java.awt.Graphics2D;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbePalette;
import com.elowbe.laf.util.PaintUtils;

final class ElowbeMenuPainter {
    private ElowbeMenuPainter() {
    }

    static void paintMenuBarBackground(Graphics2D g2, JComponent component) {
        g2.setColor(PaintUtils.palette().background);
        g2.fillRect(0, 0, component.getWidth(), component.getHeight());
    }

    static void paintMenuSelection(Graphics2D g2, AbstractButton item) {
        if (!item.isEnabled() || !isHighlighted(item)) {
            return;
        }

        ElowbePalette palette = PaintUtils.palette();
        if (item.getParent() instanceof JMenuBar) {
            PaintUtils.fillRound(g2, 3, 3, item.getWidth() - 6, item.getHeight() - 6,
                    ElowbeDefaults.RADIUS_SM, selectionFill(palette));
        } else {
            PaintUtils.fillRound(g2, 4, 2, item.getWidth() - 8, item.getHeight() - 4,
                    ElowbeDefaults.RADIUS_SM, selectionFill(palette));
        }
    }

    static boolean isHighlighted(AbstractButton item) {
        return item.getModel().isArmed() || item.getModel().isRollover()
                || item instanceof JMenu && item.getModel().isSelected();
    }

    static java.awt.Color selectionFill(ElowbePalette palette) {
        return ElowbeDefaults.theme().isDark() ? palette.pressed : palette.accent;
    }
}
