package com.elowbe.laf.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicColorChooserUI;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.util.PaintUtils;

public class ElowbeColorChooserUI extends BasicColorChooserUI {
    private static final int THUMB_RADIUS = 7;
    private static final int PLANE_SELECTOR_RADIUS = 8;
    private static final int TRACK_INSET = THUMB_RADIUS + 1;
    private static final int TRACK_HEIGHT = 12;

    public static ComponentUI createUI(JComponent component) {
        return new ElowbeColorChooserUI();
    }

    @Override
    public void installUI(JComponent component) {
        javax.swing.JColorChooser colorChooser = (javax.swing.JColorChooser) component;
        colorChooser.setPreviewPanel(new HiddenPreviewPanel());
        super.installUI(component);
        colorChooser.setOpaque(false);
        colorChooser.setBorder(new EmptyBorder(16, 18, 14, 18));
        for (AbstractColorChooserPanel panel : colorChooser.getChooserPanels()) {
            colorChooser.removeChooserPanel(panel);
        }
        colorChooser.addChooserPanel(new ModernColorChooserPanel());
        removePreviewSlot(colorChooser);
    }

    @Override
    public void paint(Graphics graphics, JComponent component) {
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            g2.setColor(PaintUtils.palette().background);
            g2.fillRect(0, 0, component.getWidth(), component.getHeight());
        } finally {
            g2.dispose();
        }
        super.paint(graphics, component);
    }

    private void removePreviewSlot(Container container) {
        Component[] children = container.getComponents();
        for (Component child : children) {
            if (isPreviewSlot(child)) {
                container.remove(child);
                continue;
            }
            if (child instanceof Container) {
                removePreviewSlot((Container) child);
            }
        }
    }

    private boolean isPreviewSlot(Component component) {
        if (component instanceof HiddenPreviewPanel) {
            return true;
        }
        if (!(component instanceof JComponent)) {
            return false;
        }
        javax.swing.border.Border border = ((JComponent) component).getBorder();
        return border instanceof TitledBorder && "Preview".equals(((TitledBorder) border).getTitle());
    }

    private static final class ModernColorChooserPanel extends AbstractColorChooserPanel {
        private static final long serialVersionUID = 1L;

        private float hue = 0.62f;
        private float saturation = 0.78f;
        private float brightness = 0.55f;
        private int alpha = 255;
        private boolean updating;

        private ColorPlane colorPlane;
        private HueStrip hueStrip;
        private AlphaStrip alphaStrip;
        private ColorSwatch swatch;
        private JTextField hexField;
        private JTextField alphaField;

        @Override
        protected void buildChooser() {
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(12, 18, 10, 18));

            JPanel content = panel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;

            colorPlane = new ColorPlane(this);
            content.add(colorPlane, gbc);

            gbc.gridy++;
            gbc.insets = new Insets(16, 0, 0, 0);
            hueStrip = new HueStrip(this);
            content.add(hueStrip, gbc);

            gbc.gridy++;
            gbc.insets = new Insets(12, 0, 0, 0);
            alphaStrip = new AlphaStrip(this);
            content.add(alphaStrip, gbc);

            gbc.gridy++;
            gbc.insets = new Insets(16, 0, 0, 0);
            content.add(createFields(), gbc);

            add(content, BorderLayout.CENTER);
        }

        @Override
        public void updateChooser() {
            if (updating || getColorSelectionModel() == null) {
                return;
            }
            Color selected = getColorFromModel();
            float[] hsb = Color.RGBtoHSB(selected.getRed(), selected.getGreen(), selected.getBlue(), null);
            if (hsb[1] > 0f || hsb[2] > 0f) {
                hue = hsb[0];
            }
            saturation = hsb[1];
            brightness = hsb[2];
            alpha = selected.getAlpha();
            updateFields();
            repaintPickers();
        }

        @Override
        public String getDisplayName() {
            return "Picker";
        }

        @Override
        public Icon getSmallDisplayIcon() {
            return null;
        }

        @Override
        public Icon getLargeDisplayIcon() {
            return null;
        }

        private JPanel createFields() {
            JPanel row = panel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 0, 0, 8);

            swatch = new ColorSwatch(this);
            row.add(swatch, gbc);

            JComboBox<String> format = new JComboBox<>(new String[] { "HEX" });
            format.setFocusable(false);
            format.setPreferredSize(new Dimension(92, 38));
            row.add(format, gbc);

            hexField = new JTextField();
            hexField.setHorizontalAlignment(SwingConstants.LEFT);
            hexField.setPreferredSize(new Dimension(122, 38));
            hexField.setBorder(new EmptyBorder(0, 12, 0, 12));
            hexField.addActionListener(event -> applyHexField());
            hexField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent event) {
                    applyHexField();
                }
            });
            row.add(hexField, gbc);

            alphaField = new JTextField();
            alphaField.setHorizontalAlignment(SwingConstants.CENTER);
            alphaField.setPreferredSize(new Dimension(70, 38));
            alphaField.setBorder(new EmptyBorder(0, 8, 0, 8));
            alphaField.addActionListener(event -> applyAlphaField());
            alphaField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent event) {
                    applyAlphaField();
                }
            });
            gbc.insets = new Insets(0, 0, 0, 0);
            row.add(alphaField, gbc);

            return row;
        }

        private void setHueFromX(int x, int width) {
            hue = trackValue(x, width);
            updateModel();
        }

        private void setAlphaFromX(int x, int width) {
            alpha = Math.round(trackValue(x, width) * 255f);
            updateModel();
        }

        private void setColorFromPoint(Point point, Dimension size) {
            saturation = clamp(point.x / (float) Math.max(1, size.width - 1));
            brightness = 1f - clamp(point.y / (float) Math.max(1, size.height - 1));
            updateModel();
        }

        private void updateModel() {
            if (getColorSelectionModel() == null) {
                return;
            }
            updating = true;
            try {
                Color rgb = Color.getHSBColor(hue, saturation, brightness);
                getColorSelectionModel().setSelectedColor(new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), alpha));
                updateFields();
                repaintPickers();
            } finally {
                updating = false;
            }
        }

        private void applyHexField() {
            if (updating || hexField == null) {
                return;
            }
            Color parsed = parseHex(hexField.getText());
            if (parsed == null) {
                updateFields();
                return;
            }
            float[] hsb = Color.RGBtoHSB(parsed.getRed(), parsed.getGreen(), parsed.getBlue(), null);
            hue = hsb[1] == 0f && hsb[2] == 0f ? hue : hsb[0];
            saturation = hsb[1];
            brightness = hsb[2];
            alpha = parsed.getAlpha();
            updateModel();
        }

        private void applyAlphaField() {
            if (updating || alphaField == null) {
                return;
            }
            String text = alphaField.getText().trim().replace("%", "");
            try {
                int percent = Math.max(0, Math.min(100, Integer.parseInt(text)));
                alpha = Math.round(percent / 100f * 255f);
                updateModel();
            } catch (NumberFormatException ex) {
                updateFields();
            }
        }

        private Color parseHex(String text) {
            String value = text.trim();
            if (value.startsWith("#")) {
                value = value.substring(1);
            }
            if (value.length() != 6 && value.length() != 8) {
                return null;
            }
            try {
                int rgb = Integer.parseUnsignedInt(value.substring(0, 6), 16);
                int parsedAlpha = value.length() == 8 ? Integer.parseUnsignedInt(value.substring(6, 8), 16) : alpha;
                return new Color((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff, parsedAlpha);
            } catch (NumberFormatException ex) {
                return null;
            }
        }

        private void updateFields() {
            if (hexField == null || alphaField == null) {
                return;
            }
            Color color = selectedColor();
            hexField.setText(String.format(Locale.ROOT, "#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()));
            alphaField.setText(Math.round(alpha / 255f * 100f) + "%");
        }

        private void repaintPickers() {
            if (colorPlane != null) {
                colorPlane.repaint();
            }
            if (hueStrip != null) {
                hueStrip.repaint();
            }
            if (alphaStrip != null) {
                alphaStrip.repaint();
            }
            if (swatch != null) {
                swatch.repaint();
            }
        }

        private Color selectedColor() {
            Color rgb = Color.getHSBColor(hue, saturation, brightness);
            return new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), alpha);
        }
    }

    private static final class ColorPlane extends PickerComponent {
        private static final long serialVersionUID = 1L;

        private ColorPlane(ModernColorChooserPanel chooser) {
            super(chooser, new Dimension(300, 180));
        }

        @Override
        protected void updateFromMouse(MouseEvent event) {
            chooser.setColorFromPoint(event.getPoint(), getSize());
        }

        @Override
        protected void paintPicker(Graphics2D g2) {
            g2.setColor(PaintUtils.palette().card);
            g2.fillRect(0, 0, getWidth(), getHeight());
            Shape clip = PaintUtils.roundedRect(0, 0, getWidth(), getHeight(), ElowbeDefaults.RADIUS_SM);
            g2.setClip(clip);
            BufferedImage gradient = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < getWidth(); x++) {
                float saturation = x / (float) Math.max(1, getWidth() - 1);
                for (int y = 0; y < getHeight(); y++) {
                    float brightness = 1f - y / (float) Math.max(1, getHeight() - 1);
                    gradient.setRGB(x, y, Color.HSBtoRGB(chooser.hue, saturation, brightness));
                }
            }
            g2.drawImage(gradient, 0, 0, null);
            g2.setClip(null);
            PaintUtils.drawRound(g2, 0, 0, getWidth(), getHeight(), ElowbeDefaults.RADIUS_SM, PaintUtils.palette().border);

            Shape oldClip = g2.getClip();
            g2.setClip(0, 0, getWidth(), getHeight());
            int x = Math.max(PLANE_SELECTOR_RADIUS, Math.min(getWidth() - PLANE_SELECTOR_RADIUS - 1,
                    Math.round(chooser.saturation * (getWidth() - 1))));
            int y = Math.max(PLANE_SELECTOR_RADIUS, Math.min(getHeight() - PLANE_SELECTOR_RADIUS - 1,
                    Math.round((1f - chooser.brightness) * (getHeight() - 1))));
            g2.setStroke(new BasicStroke(1.5f));
            g2.setColor(PaintUtils.withAlpha(Color.BLACK, 110));
            g2.drawOval(x - PLANE_SELECTOR_RADIUS, y - PLANE_SELECTOR_RADIUS,
                    PLANE_SELECTOR_RADIUS * 2, PLANE_SELECTOR_RADIUS * 2);
            g2.setColor(PaintUtils.withAlpha(Color.WHITE, 210));
            g2.drawOval(x - PLANE_SELECTOR_RADIUS + 1, y - PLANE_SELECTOR_RADIUS + 1,
                    PLANE_SELECTOR_RADIUS * 2 - 2, PLANE_SELECTOR_RADIUS * 2 - 2);
            g2.setClip(oldClip);
        }
    }

    private static final class HueStrip extends PickerComponent {
        private static final long serialVersionUID = 1L;

        private HueStrip(ModernColorChooserPanel chooser) {
            super(chooser, new Dimension(300, 18));
        }

        @Override
        protected void updateFromMouse(MouseEvent event) {
            chooser.setHueFromX(event.getX(), getWidth());
        }

        @Override
        protected void paintPicker(Graphics2D g2) {
            g2.setColor(PaintUtils.palette().card);
            g2.fillRect(0, 0, getWidth(), getHeight());
            int trackX = TRACK_INSET;
            int trackW = Math.max(1, getWidth() - TRACK_INSET * 2);
            int trackY = (getHeight() - TRACK_HEIGHT) / 2;
            Shape clip = PaintUtils.roundedRect(trackX, trackY, trackW, TRACK_HEIGHT, TRACK_HEIGHT);
            g2.setClip(clip);
            for (int x = 0; x < trackW; x++) {
                g2.setColor(Color.getHSBColor(x / (float) Math.max(1, trackW - 1), 1f, 1f));
                g2.drawLine(trackX + x, trackY, trackX + x, trackY + TRACK_HEIGHT);
            }
            g2.setClip(null);
            paintTrackMarker(g2, trackX + Math.round(chooser.hue * (trackW - 1)),
                    Color.getHSBColor(chooser.hue, 1f, 1f));
        }
    }

    private static final class AlphaStrip extends PickerComponent {
        private static final long serialVersionUID = 1L;

        private AlphaStrip(ModernColorChooserPanel chooser) {
            super(chooser, new Dimension(300, 18));
        }

        @Override
        protected void updateFromMouse(MouseEvent event) {
            chooser.setAlphaFromX(event.getX(), getWidth());
        }

        @Override
        protected void paintPicker(Graphics2D g2) {
            g2.setColor(PaintUtils.palette().card);
            g2.fillRect(0, 0, getWidth(), getHeight());
            Color color = opaque(chooser.selectedColor());
            int trackX = TRACK_INSET;
            int trackW = Math.max(1, getWidth() - TRACK_INSET * 2);
            int trackY = (getHeight() - TRACK_HEIGHT) / 2;
            Shape clip = PaintUtils.roundedRect(trackX, trackY, trackW, TRACK_HEIGHT, TRACK_HEIGHT);
            g2.setClip(clip);
            paintChecker(g2, getWidth(), getHeight(), getHeight());
            g2.setPaint(new java.awt.GradientPaint(trackX, trackY,
                    new Color(color.getRed(), color.getGreen(), color.getBlue(), 0),
                    trackX + trackW, trackY, new Color(color.getRed(), color.getGreen(), color.getBlue(), 255)));
            g2.fillRect(trackX, trackY, trackW, TRACK_HEIGHT);
            g2.setClip(null);
            paintTrackMarker(g2, trackX + Math.round(chooser.alpha / 255f * (trackW - 1)),
                    new Color(color.getRed(), color.getGreen(), color.getBlue(), 255));
        }
    }

    private abstract static class PickerComponent extends JComponent {
        private static final long serialVersionUID = 1L;

        protected final ModernColorChooserPanel chooser;

        private PickerComponent(ModernColorChooserPanel chooser, Dimension preferredSize) {
            this.chooser = chooser;
            setOpaque(false);
            setPreferredSize(preferredSize);
            setMinimumSize(preferredSize);
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.CROSSHAIR_CURSOR));

            MouseAdapter listener = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent event) {
                    updateFromMouse(event);
                }

                @Override
                public void mouseDragged(MouseEvent event) {
                    updateFromMouse(event);
                }
            };
            addMouseListener(listener);
            addMouseMotionListener(listener);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = PaintUtils.prepare(graphics);
            try {
                paintPicker(g2);
            } finally {
                g2.dispose();
            }
        }

        protected abstract void updateFromMouse(MouseEvent event);

        protected abstract void paintPicker(Graphics2D g2);

        protected void paintTrackMarker(Graphics2D g2, int x, Color fill) {
            int centerX = Math.max(TRACK_INSET, Math.min(getWidth() - TRACK_INSET - 1, x));
            g2.setColor(opaque(fill));
            g2.fillRoundRect(centerX - 3, 1, 6, getHeight() - 2, 6, 6);
            g2.setStroke(new BasicStroke(1.5f));
            g2.setColor(PaintUtils.palette().foreground);
            g2.drawRoundRect(centerX - 3, 1, 6, getHeight() - 3, 6, 6);
        }
    }

    private static final class ColorSwatch extends JComponent {
        private static final long serialVersionUID = 1L;

        private final ModernColorChooserPanel chooser;

        private ColorSwatch(ModernColorChooserPanel chooser) {
            this.chooser = chooser;
            setOpaque(false);
            setPreferredSize(new Dimension(38, 38));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = PaintUtils.prepare(graphics);
            try {
                paintChecker(g2, getWidth(), getHeight(), ElowbeDefaults.RADIUS_SM);
                PaintUtils.fillRound(g2, 0, 0, getWidth(), getHeight(), ElowbeDefaults.RADIUS_SM, chooser.selectedColor());
                PaintUtils.drawRound(g2, 0, 0, getWidth(), getHeight(), ElowbeDefaults.RADIUS_SM, PaintUtils.palette().border);
            } finally {
                g2.dispose();
            }
        }
    }

    private static final class HiddenPreviewPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private HiddenPreviewPanel() {
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(0, 0);
        }
    }

    private static JPanel panel(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    private static void paintChecker(Graphics2D g2, int width, int height, int radius) {
        Shape oldClip = g2.getClip();
        if (radius > 0) {
            g2.setClip(PaintUtils.roundedRect(0, 0, width, height, radius));
        }
        int size = 6;
        for (int y = 0; y < height; y += size) {
            for (int x = 0; x < width; x += size) {
                boolean light = ((x / size) + (y / size)) % 2 == 0;
                g2.setColor(light ? new Color(245, 245, 245) : new Color(210, 210, 210));
                g2.fillRect(x, y, size, size);
            }
        }
        g2.setClip(oldClip);
    }

    private static float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    private static float trackValue(int x, int width) {
        int start = TRACK_INSET;
        int end = Math.max(start + 1, width - TRACK_INSET - 1);
        return clamp((x - start) / (float) (end - start));
    }

    private static Color opaque(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue());
    }
}
