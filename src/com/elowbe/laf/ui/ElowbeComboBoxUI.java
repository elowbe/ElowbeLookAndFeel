package com.elowbe.laf.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.IllegalComponentStateException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.filechooser.FileFilter;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbePalette;
import com.elowbe.laf.util.ElowbeIcons;
import com.elowbe.laf.util.PaintUtils;

public class ElowbeComboBoxUI extends BasicComboBoxUI {
    private final MouseListener cursorListener = ElowbeCursorSupport.handCursorOnHover();

    public static ComponentUI createUI(JComponent component) {
        return new ElowbeComboBoxUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        comboBox.setOpaque(false);
        comboBox.setFocusable(true);
        comboBox.setLightWeightPopupEnabled(true);
        comboBox.setBorder(new EmptyBorder(0, 0, 0, 0));
        comboBox.setRenderer(new SleekComboRenderer());
        comboBox.setMaximumRowCount(8);
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        comboBox.addMouseListener(cursorListener);
    }

    @Override
    protected void uninstallListeners() {
        comboBox.removeMouseListener(cursorListener);
        ElowbeCursorSupport.restoreCursor(comboBox);
        super.uninstallListeners();
    }

    @Override
    protected JButton createArrowButton() {
        return new ChevronButton();
    }

    @Override
    protected ComboPopup createPopup() {
        return new SleekComboPopup(comboBox);
    }

    @Override
    public void paint(Graphics graphics, JComponent component) {
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            ElowbePalette palette = PaintUtils.palette();
            int radius = PaintUtils.radius(component, ElowbeDefaults.RADIUS_MD);
            PaintUtils.fillRound(g2, 0, 0, component.getWidth(), component.getHeight(), radius,
                    component.isEnabled() ? palette.card : palette.disabled);
            PaintUtils.drawRound(g2, 0, 0, component.getWidth(), component.getHeight(), radius, palette.input);
            paintSelectedValue(g2, component);
        } finally {
            g2.dispose();
        }
    }

    @Override
    public void paintCurrentValue(Graphics graphics, Rectangle bounds, boolean hasFocus) {
        paintSelectedValue((Graphics2D) graphics, comboBox);
    }

    private void paintSelectedValue(Graphics2D graphics, JComponent component) {
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            Object value = comboBox.getSelectedItem();
            if (value == null) {
                return;
            }
            g2.setFont(comboBox.getFont());
            g2.setColor(comboBox.isEnabled() ? PaintUtils.palette().foreground : PaintUtils.palette().disabledForeground);
            int arrowWidth = arrowButton == null ? 34 : arrowButton.getWidth();
            int textX = 14;
            int maxWidth = Math.max(0, component.getWidth() - arrowWidth - textX - 8);
            String text = elide(displayText(value), g2, maxWidth);
            int y = (component.getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
            g2.drawString(text, textX, y);
        } finally {
            g2.dispose();
        }
    }

    private static String displayText(Object value) {
        if (value instanceof FileFilter) {
            return ((FileFilter) value).getDescription();
        }
        return value.toString();
    }

    private String elide(String text, Graphics2D g2, int maxWidth) {
        if (g2.getFontMetrics().stringWidth(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int ellipsisWidth = g2.getFontMetrics().stringWidth(ellipsis);
        for (int length = text.length(); length > 0; length--) {
            String candidate = text.substring(0, length);
            if (g2.getFontMetrics().stringWidth(candidate) + ellipsisWidth <= maxWidth) {
                return candidate + ellipsis;
            }
        }
        return ellipsis;
    }

    private static final class ChevronButton extends JButton {
        private static final long serialVersionUID = 1L;

        private ChevronButton() {
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusable(false);
            setPreferredSize(new Dimension(34, 32));
            setMargin(new Insets(0, 0, 0, 0));
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = PaintUtils.prepare(graphics);
            try {
                if (getModel().isRollover() || getModel().isPressed()) {
                    PaintUtils.fillRound(g2, 5, 5, getWidth() - 10, getHeight() - 10,
                            ElowbeDefaults.RADIUS_SM, PaintUtils.palette().accent);
                }
                g2.setColor(PaintUtils.palette().mutedForeground);
                ElowbeIcons.chevronDown(15).paintIcon(this, g2, (getWidth() - 15) / 2, (getHeight() - 15) / 2);
            } finally {
                g2.dispose();
            }
        }
    }

    private static final class SleekComboRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list,
                    value == null ? "" : displayText(value), index, false, false);
            ElowbePalette palette = PaintUtils.palette();
            label.setOpaque(false);
            label.setBackground(new Color(0, 0, 0, 0));
            label.setBorder(new EmptyBorder(8, 14, 8, 42));
            label.setForeground(palette.foreground);
            label.putClientProperty("Elowbe.comboSelected", Boolean.valueOf(isSelected && index >= 0));
            return label;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            boolean selected = Boolean.TRUE.equals(getClientProperty("Elowbe.comboSelected"));
            Graphics2D g2 = PaintUtils.prepare(graphics);
            try {
                ElowbePalette palette = PaintUtils.palette();
                if (selected) {
                    PaintUtils.fillRound(g2, 2, 2, getWidth() - 4, getHeight() - 4,
                            ElowbeDefaults.RADIUS_SM, ElowbeMenuPainter.selectionFill(palette));
                }
            } finally {
                g2.dispose();
            }
            super.paintComponent(graphics);
            if (selected) {
                Graphics2D check = PaintUtils.prepare(graphics);
                try {
                    check.setColor(PaintUtils.palette().foreground);
                    ElowbeIcons.check(15).paintIcon(this, check, getWidth() - 28, (getHeight() - 15) / 2);
                } finally {
                    check.dispose();
                }
            }
        }
    }

    private static final class SleekComboPopup extends BasicComboPopup {
        private static final long serialVersionUID = 1L;

        @SuppressWarnings("rawtypes")
        private SleekComboPopup(javax.swing.JComboBox combo) {
            super(combo);
            setOpaque(true);
            setBorderPainted(false);
            setBorder(BorderFactory.createEmptyBorder());
            setBackground(PaintUtils.palette().popover);
        }

        @Override
        protected void configureList() {
            super.configureList();
            list.setOpaque(false);
            list.setFixedCellHeight(38);
            list.setBorder(new EmptyBorder(6, 6, 6, 6));
            list.setSelectionBackground(PaintUtils.palette().accent);
            list.setSelectionForeground(PaintUtils.palette().foreground);
            list.setBackground(PaintUtils.palette().popover);
            list.setForeground(PaintUtils.palette().foreground);
            list.addMouseListener(ElowbeCursorSupport.handCursorOnHover());
        }

        @Override
        protected JScrollPane createScroller() {
            JScrollPane scroller = new JScrollPane(list);
            scroller.setOpaque(false);
            scroller.getViewport().setOpaque(false);
            scroller.setBackground(new Color(0, 0, 0, 0));
            scroller.getViewport().setBackground(new Color(0, 0, 0, 0));
            scroller.setBorder(BorderFactory.createEmptyBorder());
            scroller.setViewportBorder(BorderFactory.createEmptyBorder());
            return scroller;
        }

        @Override
        public void paint(Graphics graphics) {
            Graphics2D g2 = PaintUtils.prepare(graphics);
            try {
                ElowbePalette palette = PaintUtils.palette();
                Shape popupShape = PaintUtils.roundedRect(0, 0, getWidth(), getHeight(), ElowbeDefaults.RADIUS_MD);
                g2.setColor(palette.popover);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(palette.popover);
                g2.fill(popupShape);
                g2.setClip(popupShape);
                super.paint(g2);
                g2.setClip(null);
                PaintUtils.drawRound(g2, 0, 0, getWidth(), getHeight(), ElowbeDefaults.RADIUS_MD, palette.border);
            } finally {
                g2.dispose();
            }
        }

        @Override
        protected Rectangle computePopupBounds(int px, int py, int pw, int ph) {
            int rows = Math.max(1, Math.min(comboBox.getMaximumRowCount(), comboBox.getItemCount()));
            int height = Math.max(ph, rows * list.getFixedCellHeight() + 18);
            return super.computePopupBounds(px, popupY(py, height), Math.max(pw, popupWidth()), height);
        }

        private int popupY(int defaultY, int height) {
            try {
                Point screenLocation = comboBox.getLocationOnScreen();
                GraphicsConfiguration configuration = comboBox.getGraphicsConfiguration();
                Rectangle bounds = configuration == null ? null : configuration.getBounds();
                if (bounds != null && screenLocation.y + defaultY + height > bounds.y + bounds.height) {
                    return -height;
                }
            } catch (IllegalComponentStateException ex) {
                return defaultY;
            }
            return defaultY;
        }

        private int popupWidth() {
            int width = 0;
            for (int index = 0; index < comboBox.getItemCount(); index++) {
                Object value = comboBox.getItemAt(index);
                if (value != null) {
                    width = Math.max(width,
                            comboBox.getFontMetrics(comboBox.getFont()).stringWidth(displayText(value)));
                }
            }
            return width + 72;
        }
    }
}
