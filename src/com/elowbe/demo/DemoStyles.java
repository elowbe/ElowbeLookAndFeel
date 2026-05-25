package com.elowbe.demo;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.util.ElowbeBorder;
import com.elowbe.laf.util.ElowbeIcons;
import com.elowbe.laf.util.PaintUtils;

final class DemoStyles {
    private static final String FONT_ROLE = "Elowbe.demoFontRole";

    private DemoStyles() {
    }

    static JPanel card() {
        JPanel panel = new JPanel();
        panel.putClientProperty(ElowbeDefaults.CARD_KEY, Boolean.TRUE);
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    static JPanel vStack(int gap) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.putClientProperty("gap", gap);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    static JPanel flow(int align, int gap) {
        JPanel panel = new JPanel(new FlowLayout(align, gap, gap));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    static void addGap(JComponent component, int height) {
        component.add(Box.createVerticalStrut(height));
    }

    static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIManager.getFont("Elowbe.font.title"));
        label.putClientProperty(FONT_ROLE, "title");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(UIManager.getColor("Label.disabledForeground"));
        label.setFont(UIManager.getFont("Elowbe.font.small"));
        label.putClientProperty(FONT_ROLE, "muted");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    static JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIManager.getFont("Elowbe.font.small").deriveFont(Font.BOLD));
        label.putClientProperty(FONT_ROLE, "label");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    static JButton button(String text, String variant) {
        JButton button = new JButton(text);
        button.putClientProperty(ElowbeDefaults.BUTTON_VARIANT_KEY, variant);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        return button;
    }

    static JButton iconButton(String variant, javax.swing.Icon icon) {
        JButton button = button("", variant);
        button.setIcon(icon);
        button.setMargin(new Insets(7, 8, 7, 8));
        return button;
    }

    static JTextField textField(String placeholder) {
        JTextField field = new JTextField();
        field.putClientProperty(ElowbeDefaults.PLACEHOLDER_KEY, placeholder);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    static JPasswordField passwordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.putClientProperty(ElowbeDefaults.PLACEHOLDER_KEY, placeholder);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    static JTextArea textArea(String placeholder, int rows) {
        JTextArea area = new JTextArea(rows, 20);
        area.putClientProperty(ElowbeDefaults.PLACEHOLDER_KEY, placeholder);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setAlignmentX(Component.LEFT_ALIGNMENT);
        return area;
    }

    static JComboBox<String> combo(String... values) {
        JComboBox<String> combo = new JComboBox<>(values);
        combo.setPreferredSize(new Dimension(120, 36));
        combo.setMinimumSize(new Dimension(96, 36));
        combo.setMaximumSize(new Dimension(120, 36));
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
        return combo;
    }

    static JLabel badge(String text) {
        JLabel label = new RoundedBadge(text);
        label.setOpaque(false);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createCompoundBorder(
                new ElowbeBorder(PaintUtils.palette().border, ElowbeDefaults.RADIUS_SM, new Insets(0, 0, 0, 0)),
                new EmptyBorder(3, 8, 3, 8)));
        label.setFont(UIManager.getFont("Elowbe.font.small"));
        return label;
    }

    static void applyFontRoles(Component component) {
        if (component instanceof JComponent) {
            JComponent jComponent = (JComponent) component;
            Object role = jComponent.getClientProperty(FONT_ROLE);
            if ("title".equals(role)) {
                jComponent.setFont(UIManager.getFont("Elowbe.font.title"));
            } else if ("muted".equals(role)) {
                jComponent.setFont(UIManager.getFont("Elowbe.font.small"));
            } else if ("label".equals(role)) {
                jComponent.setFont(UIManager.getFont("Elowbe.font.small").deriveFont(Font.BOLD));
            }
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                applyFontRoles(child);
            }
        }
    }

    static JSlider slider(int value) {
        JSlider slider = new JSlider(0, 100, value);
        slider.setOpaque(false);
        slider.setAlignmentX(Component.LEFT_ALIGNMENT);
        slider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        return slider;
    }

    static ButtonGroup group(javax.swing.AbstractButton... buttons) {
        ButtonGroup group = new ButtonGroup();
        for (javax.swing.AbstractButton button : buttons) {
            group.add(button);
        }
        return group;
    }

    static void fixedWidth(JComponent component, int width) {
        int height = Math.max(1, component.getPreferredSize().height);
        Dimension size = new Dimension(width, height);
        component.setMinimumSize(size);
        component.setPreferredSize(size);
        component.setMaximumSize(size);
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    static JLabel searchLabel() {
        JLabel label = muted("Search...");
        label.setIcon(ElowbeIcons.search(14));
        return label;
    }

    private static final class RoundedBadge extends JLabel {
        private static final long serialVersionUID = 1L;

        private RoundedBadge(String text) {
            super(text, SwingConstants.CENTER);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = PaintUtils.prepare(graphics);
            try {
                PaintUtils.fillRound(g2, 0, 0, getWidth(), getHeight(),
                        ElowbeDefaults.RADIUS_SM, PaintUtils.palette().background);
            } finally {
                g2.dispose();
            }
            super.paintComponent(graphics);
        }
    }
}
