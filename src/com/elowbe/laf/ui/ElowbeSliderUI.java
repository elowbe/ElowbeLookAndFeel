package com.elowbe.laf.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.BasicStroke;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSliderUI;

import com.elowbe.laf.theme.ElowbePalette;
import com.elowbe.laf.util.PaintUtils;

public class ElowbeSliderUI extends BasicSliderUI {
    private final MouseListener cursorListener = ElowbeCursorSupport.handCursorOnHover();

    public ElowbeSliderUI(JSlider slider) {
        super(slider);
    }

    public static ComponentUI createUI(JComponent component) {
        return new ElowbeSliderUI((JSlider) component);
    }

    @Override
    public void installUI(JComponent component) {
        super.installUI(component);
        component.setOpaque(false);
        component.setFocusable(false);
    }

    @Override
    protected void installListeners(JSlider slider) {
        super.installListeners(slider);
        slider.addMouseListener(cursorListener);
    }

    @Override
    protected void uninstallListeners(JSlider slider) {
        slider.removeMouseListener(cursorListener);
        ElowbeCursorSupport.restoreCursor(slider);
        super.uninstallListeners(slider);
    }

    @Override
    protected Dimension getThumbSize() {
        return new Dimension(16, 16);
    }

    @Override
    public void paint(Graphics graphics, JComponent component) {
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            g2.setColor(PaintUtils.palette().card);
            g2.fillRect(0, 0, component.getWidth(), component.getHeight());
        } finally {
            g2.dispose();
        }
        super.paint(graphics, component);
    }

    @Override
    public void paintTrack(Graphics graphics) {
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            ElowbePalette palette = PaintUtils.palette();
            Rectangle track = trackRect;
            int cy = track.y + track.height / 2 - 2;
            int start = track.x;
            int end = track.x + track.width;
            int value = xPositionForValue(slider.getValue());
            PaintUtils.fillRound(g2, start, cy, end - start, 4, 4, palette.secondary);
            PaintUtils.fillRound(g2, start, cy, Math.max(4, value - start), 4, 4, palette.primary);
        } finally {
            g2.dispose();
        }
    }

    @Override
    public void paintThumb(Graphics graphics) {
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            ElowbePalette palette = PaintUtils.palette();
            g2.setColor(palette.card);
            g2.fillOval(thumbRect.x, thumbRect.y, thumbRect.width - 1, thumbRect.height - 1);
            g2.setColor(palette.primary);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(thumbRect.x, thumbRect.y, thumbRect.width - 1, thumbRect.height - 1);
        } finally {
            g2.dispose();
        }
    }

    @Override
    public void paintFocus(Graphics graphics) {
    }

    @Override
    public void setThumbLocation(int x, int y) {
        Rectangle dirty = new Rectangle(thumbRect);
        dirty.grow(4, 4);
        super.setThumbLocation(x, y);
        Rectangle moved = new Rectangle(thumbRect);
        moved.grow(4, 4);
        dirty.add(moved);
        slider.repaint(dirty);
    }
}
