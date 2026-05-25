package com.elowbe.laf.ui;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.BasicStroke;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicCheckBoxUI;

import com.elowbe.laf.theme.ElowbePalette;
import com.elowbe.laf.util.ElowbeIcons;
import com.elowbe.laf.util.PaintUtils;

public class ElowbeCheckBoxUI extends BasicCheckBoxUI {
    private static final int SIZE = 15;

    public static ComponentUI createUI(JComponent component) {
        return new ElowbeCheckBoxUI();
    }

    @Override
    protected void installDefaults(AbstractButton button) {
        super.installDefaults(button);
        button.setOpaque(false);
        button.setIconTextGap(7);
        button.setRolloverEnabled(true);
    }

    @Override
    public void paint(Graphics graphics, JComponent component) {
        AbstractButton button = (AbstractButton) component;
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            Insets insets = button.getInsets();
            Rectangle iconRect = new Rectangle(insets.left, (button.getHeight() - SIZE) / 2, SIZE, SIZE);
            paintBox(g2, button, iconRect);
            paintPlainText(g2, button, iconRect.x + SIZE + button.getIconTextGap());
        } finally {
            g2.dispose();
        }
    }

    @Override
    public Dimension getPreferredSize(JComponent component) {
        Dimension size = super.getPreferredSize(component);
        if (size != null) {
            size.width += SIZE + ((AbstractButton) component).getIconTextGap();
            size.height = Math.max(size.height, SIZE + component.getInsets().top + component.getInsets().bottom);
        }
        return size;
    }

    private void paintBox(Graphics2D g2, AbstractButton button, Rectangle rect) {
        ElowbePalette palette = PaintUtils.palette();
        boolean selected = button.getModel().isSelected();
        g2.setColor(selected ? palette.primary : palette.card);
        g2.fillRoundRect(rect.x, rect.y, SIZE, SIZE, 8, 8);
        g2.setColor(selected ? palette.primary : palette.border);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(rect.x, rect.y, SIZE - 1, SIZE - 1, 8, 8);
        if (selected) {
            g2.setColor(palette.primaryForeground);
            ElowbeIcons.check(13).paintIcon(button, g2, rect.x + 1, rect.y + 1);
        }
    }

    private void paintPlainText(Graphics2D g2, AbstractButton button, int x) {
        String text = button.getText();
        if (text == null || text.isBlank()) {
            return;
        }
        FontMetrics metrics = g2.getFontMetrics(button.getFont());
        int y = (button.getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
        g2.setFont(button.getFont());
        g2.setColor(button.isEnabled() ? PaintUtils.palette().foreground : PaintUtils.palette().disabledForeground);
        g2.drawString(text, x, y);
    }
}
