package com.elowbe.laf.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

import com.elowbe.laf.theme.ElowbePalette;
import com.elowbe.laf.util.PaintUtils;

public class ElowbeScrollBarUI extends BasicScrollBarUI {
    public static ComponentUI createUI(JComponent component) {
        return new ElowbeScrollBarUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        scrollbar.setOpaque(false);
        scrollbar.setUnitIncrement(12);
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return zeroButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return zeroButton();
    }

    @Override
    protected void paintTrack(Graphics graphics, JComponent component, Rectangle trackBounds) {
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            g2.setColor(PaintUtils.palette().background);
            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        } finally {
            g2.dispose();
        }
    }

    @Override
    protected void paintThumb(Graphics graphics, JComponent component, Rectangle thumbBounds) {
        if (!component.isEnabled() || thumbBounds.isEmpty()) {
            return;
        }
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            ElowbePalette palette = PaintUtils.palette();
            boolean vertical = scrollbar.getOrientation() == JScrollBar.VERTICAL;
            int inset = 2;
            int x = thumbBounds.x + (vertical ? inset : 0);
            int y = thumbBounds.y + (vertical ? 0 : inset);
            int w = thumbBounds.width - (vertical ? inset * 2 : 0);
            int h = thumbBounds.height - (vertical ? 0 : inset * 2);
            PaintUtils.fillRound(g2, x, y, w, h, 999, palette.border);
        } finally {
            g2.dispose();
        }
    }

    private JButton zeroButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        return button;
    }
}
