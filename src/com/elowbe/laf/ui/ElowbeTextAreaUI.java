package com.elowbe.laf.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextAreaUI;
import javax.swing.text.JTextComponent;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbePalette;
import com.elowbe.laf.util.PaintUtils;

public class ElowbeTextAreaUI extends BasicTextAreaUI {
    public static ComponentUI createUI(JComponent component) {
        return new ElowbeTextAreaUI();
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
            ElowbePalette palette = PaintUtils.palette();
            int radius = PaintUtils.radius(text, ElowbeDefaults.RADIUS_MD);
            PaintUtils.fillRound(g2, 0, 0, text.getWidth(), text.getHeight(), radius,
                    text.isEnabled() ? palette.card : palette.disabled);
            PaintUtils.drawRound(g2, 0, 0, text.getWidth(), text.getHeight(), radius, palette.input);
        } finally {
            g2.dispose();
        }
        super.paintSafely(graphics);
        new ElowbeTextFieldUI().paintPlaceholder(graphics, text);
    }
}
