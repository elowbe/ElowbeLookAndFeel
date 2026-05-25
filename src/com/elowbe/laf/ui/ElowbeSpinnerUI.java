package com.elowbe.laf.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicFormattedTextFieldUI;
import javax.swing.plaf.basic.BasicSpinnerUI;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbePalette;
import com.elowbe.laf.util.PaintUtils;

public class ElowbeSpinnerUI extends BasicSpinnerUI {
    public static final String NUMBER_EDITOR_BACKGROUND_RENDERER_KEY = "Elowbe.spinnerNumberEditorBackgroundRenderer";
    public static final String NUMBER_TEXT_RENDERER_KEY = "Elowbe.spinnerNumberTextRenderer";

    @FunctionalInterface
    public interface NumberEditorBackgroundRenderer {
        void paintNumberEditorBackground(Graphics2D g2, JSpinner spinner, JFormattedTextField textField,
                Rectangle bounds);
    }

    @FunctionalInterface
    public interface NumberTextRenderer {
        void paintNumberText(Graphics2D g2, JSpinner spinner, JFormattedTextField textField, Rectangle bounds,
                String text);
    }

    public static ComponentUI createUI(JComponent component) {
        return new ElowbeSpinnerUI();
    }

    @Override
    public void installUI(JComponent component) {
        super.installUI(component);
        component.setOpaque(false);
        component.setBorder(new EmptyBorder(0, 0, 0, 0));
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        spinner.setOpaque(false);
    }

    @Override
    protected JComponent createEditor() {
        JSpinner.DefaultEditor editor = new SpinnerNumberEditor(spinner);
        editor.setOpaque(false);
        editor.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        JFormattedTextField textField = editor.getTextField();
        textField.setOpaque(false);
        textField.setHorizontalAlignment(JFormattedTextField.LEFT);
        textField.setBorder(new EmptyBorder(0, 10, 0, 8));
        textField.setUI(new SpinnerNumberTextUI(spinner));
        textField.setForeground(PaintUtils.palette().foreground);
        textField.setCaretColor(PaintUtils.palette().foreground);
        textField.setMargin(new Insets(0, 0, 0, 0));
        return editor;
    }

    private static final class SpinnerNumberEditor extends JSpinner.NumberEditor {
        private static final long serialVersionUID = 1L;

        private SpinnerNumberEditor(JSpinner spinner) {
            super(spinner, "#");
            setOpaque(false);
            setBorder(new EmptyBorder(0, 0, 0, 0));
        }

        @Override
        public void paint(Graphics graphics) {
            Graphics2D g2 = PaintUtils.prepare(graphics);
            Shape oldClip = g2.getClip();
            try {
                int radius = PaintUtils.radius((JComponent) getSpinner(), ElowbeDefaults.RADIUS_MD);
                g2.setClip(PaintUtils.roundedRect(0, 0, getWidth(), getHeight(), radius));
                super.paint(g2);
            } finally {
                g2.setClip(oldClip);
                g2.dispose();
            }
        }
    }

    private static NumberTextRenderer numberTextRenderer(JSpinner spinner) {
        Object renderer = spinner.getClientProperty(NUMBER_TEXT_RENDERER_KEY);
        if (renderer instanceof NumberTextRenderer) {
            return (NumberTextRenderer) renderer;
        }
        return ElowbeSpinnerUI::paintDefaultNumberText;
    }

    private static NumberEditorBackgroundRenderer numberEditorBackgroundRenderer(JSpinner spinner) {
        Object renderer = spinner.getClientProperty(NUMBER_EDITOR_BACKGROUND_RENDERER_KEY);
        if (renderer instanceof NumberEditorBackgroundRenderer) {
            return (NumberEditorBackgroundRenderer) renderer;
        }
        return ElowbeSpinnerUI::paintDefaultNumberEditorBackground;
    }

    private static void paintDefaultNumberEditorBackground(Graphics2D g2, JSpinner spinner,
            JFormattedTextField textField, Rectangle bounds) {
        // This paints inside the editor's rectangular clip. The spinner shell owns rounded clipping/borders.
//    	   g2.setColor(new Color(255, 0, 0, 40));
//           g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 12, 12);
    }

    private static void paintDefaultNumberText(Graphics2D g2, JSpinner spinner, JFormattedTextField textField,
            Rectangle bounds, String text) {
        Insets insets = textField.getInsets();
        g2.setFont(textField.getFont());
        g2.setColor(textField.isEnabled() ? PaintUtils.palette().foreground : PaintUtils.palette().disabledForeground);

        int textWidth = g2.getFontMetrics().stringWidth(text);
        int x = bounds.x + insets.left;
        if (textField.getHorizontalAlignment() == JFormattedTextField.CENTER) {
            x = bounds.x + (bounds.width - textWidth) / 2;
        } else if (textField.getHorizontalAlignment() == JFormattedTextField.RIGHT) {
            x = bounds.x + bounds.width - insets.right - textWidth;
        }

        int y = bounds.y + (bounds.height - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
        g2.drawString(text, x, y);
    }

    private static final class SpinnerNumberTextUI extends BasicFormattedTextFieldUI {
        private final JSpinner spinner;

        private SpinnerNumberTextUI(JSpinner spinner) {
            this.spinner = spinner;
        }

        @Override
        protected void paintSafely(Graphics graphics) {
            JFormattedTextField textField = (JFormattedTextField) getComponent();
            Graphics2D g2 = PaintUtils.prepare(graphics);
            try {
                Rectangle bounds = new Rectangle(0, 0, textField.getWidth(), textField.getHeight());
                numberEditorBackgroundRenderer(spinner).paintNumberEditorBackground(g2, spinner, textField, bounds);
                numberTextRenderer(spinner).paintNumberText(g2, spinner, textField, bounds, textField.getText());
            } finally {
                g2.dispose();
            }
        }
    }

    @Override
    protected Component createNextButton() {
        JButton button = SegmentButton.plus();
        installNextButtonListeners(button);
        return button;
    }

    @Override
    protected Component createPreviousButton() {
        JButton button = SegmentButton.minus();
        installPreviousButtonListeners(button);
        return button;
    }

    @Override
    protected LayoutManager createLayout() {
        return new SegmentedSpinnerLayout();
    }

    @Override
    public void paint(Graphics graphics, JComponent component) {
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            ElowbePalette palette = PaintUtils.palette();
            int radius = PaintUtils.radius(component, ElowbeDefaults.RADIUS_MD);
            PaintUtils.fillRound(g2, 1, 1, component.getWidth() - 2, component.getHeight() - 2, radius, palette.card);
        } finally {
            g2.dispose();
        }

       super.paint(graphics, component);

        g2 = PaintUtils.prepare(graphics);
        try {
            ElowbePalette palette = PaintUtils.palette();
            int radius = PaintUtils.radius(component, ElowbeDefaults.RADIUS_MD);
            PaintUtils.drawRound(g2, 1, 1, component.getWidth() - 2, component.getHeight() - 2, radius, palette.input);
            g2.setStroke(new BasicStroke(2f));
            int buttonWidth = SegmentButton.WIDTH;
            int firstDivider = component.getWidth() - buttonWidth * 2;
            int secondDivider = component.getWidth() - buttonWidth;
            g2.setColor(palette.border);
//            g2.drawLine(firstDivider, 1, firstDivider, component.getHeight() - 3);
//            g2.drawLine(secondDivider, 1, secondDivider, component.getHeight() -3);
        } finally {
            g2.dispose();
        }
    }

    private static final class SegmentedSpinnerLayout implements LayoutManager {
        @Override
        public void addLayoutComponent(String name, Component component) {
        }

        @Override
        public void removeLayoutComponent(Component component) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return new Dimension(118, 34);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(92, 30);
        }

        @Override
        public void layoutContainer(Container parent) {
            Insets insets = parent.getInsets();
            int x = insets.left;
            int y = insets.top;
            int width = parent.getWidth() - insets.left - insets.right;
            int height = parent.getHeight() - insets.top - insets.bottom;
            int buttonWidth = SegmentButton.WIDTH;
            Component editor = find(parent, JSpinner.DefaultEditor.class);
            Component next = findByName(parent, "Spinner.nextButton");
            Component previous = findByName(parent, "Spinner.previousButton");

            if (editor != null) {
                editor.setBounds(x + 5, y + 4, Math.max(0, width - buttonWidth * 2 - 9), height - 8);
                editor.invalidate();
                editor.validate();
            }
            if (previous != null) {
                previous.setBounds(x + width - buttonWidth * 2, y + 1, buttonWidth, height - 2);
            }
            if (next != null) {
                next.setBounds(x + width - buttonWidth, y + 1, buttonWidth - 1, height - 2);
            }
        }

        private Component find(Container parent, Class<?> type) {
            for (Component component : parent.getComponents()) {
                if (type.isInstance(component)) {
                    return component;
                }
            }
            return null;
        }

        private Component findByName(Container parent, String name) {
            for (Component component : parent.getComponents()) {
                if (name.equals(component.getName())) {
                    return component;
                }
            }
            return null;
        }
    }

    private static final class SegmentButton extends JButton {
        private static final long serialVersionUID = 1L;
        private static final int WIDTH = 31;
        private final boolean plus;

        private SegmentButton(boolean plus) {
            this.plus = plus;
            setText("");
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusable(false);
            setMargin(new Insets(0, 0, 0, 0));
            setPreferredSize(new Dimension(WIDTH, 34));
        }

        static SegmentButton plus() {
            SegmentButton button = new SegmentButton(true);
            button.setName("Spinner.nextButton");
            return button;
        }

        static SegmentButton minus() {
            SegmentButton button = new SegmentButton(false);
            button.setName("Spinner.previousButton");
            return button;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = PaintUtils.prepare(graphics);
            try {
                if (getModel().isRollover() || getModel().isPressed()) {
                     boolean darkMode =  ElowbeDefaults.theme().isDark();
                    PaintUtils.fillRound(g2, 1, 1, getWidth() - 3, getHeight() - 3,
                            ElowbeDefaults.RADIUS_SM, darkMode  ? new Color(50,50,50) : new Color(230,230,230));
                }
            } finally {
                g2.dispose();
            }
            g2 = PaintUtils.prepare(graphics);
            try {
               g2.setColor(isEnabled() ? PaintUtils.palette().foreground : PaintUtils.palette().disabledForeground);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                int half = 5;
                g2.drawLine(cx - half, cy, cx + half, cy);
                if (plus) {
                    g2.drawLine(cx, cy - half, cx, cy + half);
                }
            } finally {
                g2.dispose();
            }
        }
    }
}
