package com.elowbe.laf.node;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.SpinnerNumberModel;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbePalette;
import com.elowbe.laf.util.PaintUtils;

public class NodeComponent extends JComponent {
    private static final long serialVersionUID = 1L;

    private static final int WIDTH = 240;
    private static final int HEADER_HEIGHT = 58;
    private static final int ROW_HEIGHT = 28;
    private static final int PADDING = 14;
    private static final int TERMINAL_SIZE = 12;
    private static final int WIDGET_LABEL_HEIGHT = 15;
    private static final int WIDGET_CONTROL_HEIGHT = 34;
    private static final int WIDGET_GAP = 12;
    private static final double INTERACTIVE_WIDGET_MIN_SCALE = 0.80;

    private final List<NodeTerminal> inputs = new ArrayList<>();
    private final List<NodeTerminal> outputs = new ArrayList<>();
    private final List<NodeWidget> widgets = new ArrayList<>();
    private String title;
    private String subtitle;
    private Interaction interaction = Interaction.NONE;
    private NodeTerminal hoveredTerminal;
    private NodeTerminal highlightedTerminal;
    private double viewScale = 1.0;
    private boolean selected;
    private boolean connectionTargetHighlighted;

    public NodeComponent(String title, String subtitle) {
        this.title = Objects.requireNonNull(title, "title");
        this.subtitle = subtitle == null ? "" : subtitle;
        setLayout(null);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        setSize(getPreferredSize());
        installMouseBehavior();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = Objects.requireNonNull(title, "title");
        repaint();
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle == null ? "" : subtitle;
        repaint();
    }

    public List<NodeTerminal> getInputs() {
        return Collections.unmodifiableList(inputs);
    }

    public List<NodeTerminal> getOutputs() {
        return Collections.unmodifiableList(outputs);
    }

    public NodeTerminal addInput(String id, String label, Color color) {
        NodeTerminal terminal = new NodeTerminal(id, label, NodeTerminal.Side.INPUT, color);
        inputs.add(terminal);
        resizeToPreferred();
        return terminal;
    }

    public NodeTerminal addInput(String id, String label, Color color, JComponent widget) {
        return addInput(id, label, color, widget, () -> cloneWidgetComponent(widget));
    }

    public NodeTerminal addInput(
            String id,
            String label,
            Color color,
            JComponent widget,
            Supplier<JComponent> duplicateFactory) {
        NodeTerminal terminal = addInput(id, label, color);
        NodeWidget nodeWidget = new NodeWidget(label, widget, duplicateFactory);
        terminal.setWidget(nodeWidget);
        add(widget);
        resizeToPreferred();
        return terminal;
    }

    public NodeTerminal addOutput(String id, String label, Color color) {
        NodeTerminal terminal = new NodeTerminal(id, label, NodeTerminal.Side.OUTPUT, color);
        outputs.add(terminal);
        resizeToPreferred();
        return terminal;
    }

    public NodeWidget addWidget(String label, JComponent component) {
        return addWidget(label, component, () -> cloneWidgetComponent(component));
    }

    public NodeWidget addWidget(String label, JComponent component, Supplier<JComponent> duplicateFactory) {
        NodeWidget widget = new NodeWidget(label, component, duplicateFactory);
        widgets.add(widget);
        add(component);
        resizeToPreferred();
        return widget;
    }

    public List<NodeWidget> getWidgets() {
        return Collections.unmodifiableList(widgets);
    }

    public NodeComponent duplicate() {
        NodeComponent copy = new NodeComponent(title, subtitle);
        for (NodeTerminal terminal : inputs) {
            NodeTerminal input;
            if (terminal.hasWidget()) {
                input = copy.addInput(terminal.getId(), terminal.getLabel(), terminal.getColor(),
                        terminal.getWidget().duplicateComponent());
            } else {
                input = copy.addInput(terminal.getId(), terminal.getLabel(), terminal.getColor());
            }
            input.setValue(terminal.getValue());
        }
        for (NodeTerminal terminal : outputs) {
            NodeTerminal output = copy.addOutput(terminal.getId(), terminal.getLabel(), terminal.getColor());
            output.setValue(terminal.getValue());
        }
        for (NodeWidget widget : widgets) {
            copy.addWidget(widget.getLabel(), widget.duplicateComponent());
        }
        return copy;
    }

    NodeTerminal terminalById(NodeTerminal.Side side, String id) {
        List<NodeTerminal> terminals = side == NodeTerminal.Side.INPUT ? inputs : outputs;
        for (NodeTerminal terminal : terminals) {
            if (terminal.getId().equals(id)) {
                return terminal;
            }
        }
        return null;
    }

    public NodeTerminal terminalAt(Point point) {
        for (NodeTerminal terminal : inputs) {
            if (terminalBounds(terminal).contains(point)) {
                return terminal;
            }
        }
        for (NodeTerminal terminal : outputs) {
            if (terminalBounds(terminal).contains(point)) {
                return terminal;
            }
        }
        return null;
    }

    public Point terminalCenter(NodeTerminal terminal) {
        Rectangle bounds = terminalBounds(terminal);
        return new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
    }

    public Object getInputValue(NodeTerminal inputTerminal) {
        Objects.requireNonNull(inputTerminal, "inputTerminal");
        if (!inputTerminal.isInput()) {
            throw new IllegalArgumentException("Only input terminal values can be queried.");
        }
        NodeCanvas canvas = canvas();
        return canvas == null ? inputTerminal.getValue() : canvas.resolveInputValue(this, inputTerminal);
    }

    public List<Object> getInputValues(NodeTerminal inputTerminal) {
        Objects.requireNonNull(inputTerminal, "inputTerminal");
        if (!inputTerminal.isInput()) {
            throw new IllegalArgumentException("Only input terminal values can be queried.");
        }
        NodeCanvas canvas = canvas();
        if (canvas == null) {
            return Collections.singletonList(inputTerminal.getValue());
        }
        return canvas.resolveInputValues(this, inputTerminal);
    }

    double getViewScale() {
        return viewScale;
    }

    void setViewScale(double viewScale) {
        this.viewScale = viewScale;
        repaint();
    }

    void setSelected(boolean selected) {
        if (this.selected == selected) {
            return;
        }
        this.selected = selected;
        repaint();
    }

    void setConnectionTargetHighlighted(boolean highlighted, NodeTerminal terminal) {
        if (connectionTargetHighlighted == highlighted && highlightedTerminal == terminal) {
            return;
        }
        connectionTargetHighlighted = highlighted;
        highlightedTerminal = terminal;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        int height = widgetStartY();
        if (!widgets.isEmpty()) {
            height += widgets.size() * widgetRowHeight() - WIDGET_GAP;
        }
        height += PADDING;
        return new Dimension(WIDTH, height);
    }

    @Override
    public void doLayout() {
        boolean interactive = widgetsInteractive();
        layoutInputTerminalWidgets(interactive);
        int y = widgetStartY();
        for (NodeWidget widget : widgets) {
            JComponent component = widget.getComponent();
            component.setVisible(interactive);
            applyComponentFont(component, widget.scaledFont(viewScale));
            component.setBounds(
                    scaled(PADDING),
                    scaled(y + WIDGET_LABEL_HEIGHT),
                    scaled(contentWidth() - PADDING * 2),
                    scaled(WIDGET_CONTROL_HEIGHT));
            y += widgetRowHeight();
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            g2.setClip(0, 0, getWidth(), getHeight());
            g2.scale(viewScale, viewScale);
            paintCard(g2);
            paintHeader(g2);
            paintTerminalRows(g2, inputs);
            paintTerminalRows(g2, outputs);
            paintInputTerminalWidgetPreviews(g2);
            paintWidgetLabels(g2);
            paintWidgetPreviews(g2);
        } finally {
            g2.dispose();
        }
    }

    private void installMouseBehavior() {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                NodeCanvas canvas = canvas();
                if (canvas == null) {
                    return;
                }
                Point canvasPoint = SwingUtilities.convertPoint(NodeComponent.this, event.getPoint(), canvas);
                canvas.updateMousePosition(canvasPoint);
                if (event.isPopupTrigger() || SwingUtilities.isRightMouseButton(event)) {
                    canvas.showNodeContextMenu(NodeComponent.this, event.getX(), event.getY());
                    interaction = Interaction.NONE;
                    return;
                }
                canvas.requestFocusInWindow();
                Point basePoint = toBasePoint(event.getPoint());
                NodeTerminal terminal = terminalAt(basePoint);
                if (terminal != null && SwingUtilities.isMiddleMouseButton(event)) {
                    canvas.disconnectTerminal(NodeComponent.this, terminal);
                    interaction = Interaction.NONE;
                    return;
                }
                if (terminal != null && SwingUtilities.isLeftMouseButton(event)) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    canvas.beginConnectionDrag(NodeComponent.this, terminal, canvasPoint);
                    interaction = Interaction.CONNECTING;
                    return;
                }
                if (SwingUtilities.isMiddleMouseButton(event)) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    canvas.beginPan(canvasPoint);
                    interaction = Interaction.PANNING;
                    return;
                }
                if (!SwingUtilities.isLeftMouseButton(event)) {
                    return;
                }
                moveToFront();
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                canvas.beginNodeDrag(NodeComponent.this, canvasPoint, basePoint);
                interaction = Interaction.DRAGGING_NODE;
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                NodeCanvas canvas = canvas();
                if (canvas == null) {
                    return;
                }
                Point canvasPoint = SwingUtilities.convertPoint(NodeComponent.this, event.getPoint(), canvas);
                canvas.updateMousePosition(canvasPoint);
                if (interaction == Interaction.CONNECTING) {
                    canvas.updateConnectionDrag(canvasPoint);
                } else if (interaction == Interaction.PANNING) {
                    canvas.dragPan(canvasPoint);
                } else if (interaction == Interaction.DRAGGING_NODE) {
                    canvas.dragNode(NodeComponent.this, canvasPoint);
                }
            }

            @Override
            public void mouseMoved(MouseEvent event) {
                NodeCanvas canvas = canvas();
                if (canvas != null) {
                    canvas.updateMousePosition(SwingUtilities.convertPoint(NodeComponent.this, event.getPoint(), canvas));
                }
                updateHoveredTerminal(event.getPoint());
                updateCursor(event.getPoint());
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                NodeCanvas canvas = canvas();
                if (canvas != null && (event.isPopupTrigger() || SwingUtilities.isRightMouseButton(event))) {
                    canvas.updateMousePosition(SwingUtilities.convertPoint(NodeComponent.this, event.getPoint(), canvas));
                    canvas.showNodeContextMenu(NodeComponent.this, event.getX(), event.getY());
                    interaction = Interaction.NONE;
                    return;
                }
                if (canvas != null) {
                    Point canvasPoint = SwingUtilities.convertPoint(NodeComponent.this, event.getPoint(), canvas);
                    canvas.updateMousePosition(canvasPoint);
                    if (interaction == Interaction.CONNECTING) {
                        canvas.finishConnectionDrag(canvasPoint);
                    } else if (interaction == Interaction.PANNING) {
                        canvas.endPan();
                    } else if (interaction == Interaction.DRAGGING_NODE) {
                        canvas.endNodeDrag();
                    }
                }
                interaction = Interaction.NONE;
                updateCursor(event.getPoint());
            }

            @Override
            public void mouseExited(MouseEvent event) {
                updateHoveredTerminal(null);
                if (interaction == Interaction.NONE) {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        addMouseWheelListener(event -> {
            NodeCanvas canvas = canvas();
            if (canvas != null) {
                Point canvasPoint = SwingUtilities.convertPoint(NodeComponent.this, event.getPoint(), canvas);
                canvas.updateMousePosition(canvasPoint);
                updateHoveredTerminal(event.getPoint());
                canvas.zoomAt(canvasPoint, event.getWheelRotation());
                event.consume();
            }
        });
    }

    private void paintCard(Graphics2D g2) {
        ElowbePalette palette = PaintUtils.palette();
        PaintUtils.fillRound(g2, 0, 0, contentWidth(), contentHeight(), ElowbeDefaults.RADIUS_LG, palette.card);
        PaintUtils.drawRound(g2, 0, 0, contentWidth(), contentHeight(), ElowbeDefaults.RADIUS_LG, palette.border);
        if (connectionTargetHighlighted) {
            PaintUtils.drawRound(g2, 0, 0, contentWidth(), contentHeight(), ElowbeDefaults.RADIUS_LG,
                    PaintUtils.withAlpha(palette.ring, 210));
            PaintUtils.drawRound(g2, 2, 2, contentWidth() - 4, contentHeight() - 4, ElowbeDefaults.RADIUS_LG,
                    PaintUtils.withAlpha(palette.ring, 130));
        } else if (selected) {
            PaintUtils.drawRound(g2, 0, 0, contentWidth(), contentHeight(), ElowbeDefaults.RADIUS_LG, palette.ring);
        }
        g2.setColor(palette.border);
        g2.drawLine(PADDING, HEADER_HEIGHT, contentWidth() - PADDING, HEADER_HEIGHT);
    }

    private void paintHeader(Graphics2D g2) {
        ElowbePalette palette = PaintUtils.palette();
        Font titleFont = UIManager.getFont("Elowbe.font.title").deriveFont(Font.BOLD);
        Font mutedFont = UIManager.getFont("Elowbe.font.small");
        g2.setFont(titleFont);
        g2.setColor(palette.cardForeground);
        g2.drawString(title, PADDING, 23);
        g2.setFont(mutedFont);
        g2.setColor(palette.mutedForeground);
        g2.drawString(subtitle, PADDING, 42);
    }

    private void paintTerminalRows(Graphics2D g2, List<NodeTerminal> terminals) {
        Font font = UIManager.getFont("Elowbe.font.small");
        FontMetrics metrics = g2.getFontMetrics(font);
        ElowbePalette palette = PaintUtils.palette();
        g2.setFont(font);
        for (NodeTerminal terminal : terminals) {
            Rectangle bounds = terminalBounds(terminal);
            int centerY = bounds.y + bounds.height / 2;
            boolean hovered = terminal == hoveredTerminal || terminal == highlightedTerminal
                    || (interaction == Interaction.CONNECTING && terminal == hoveredTerminal);
            Color terminalColor = hovered ? brighten(terminal.getColor()) : terminal.getColor();
            int haloAlpha = hovered ? 92 : 34;
            int haloSize = hovered ? 10 : 6;
            g2.setColor(PaintUtils.withAlpha(terminalColor, haloAlpha));
            g2.fillOval(bounds.x - haloSize / 2, bounds.y - haloSize / 2, bounds.width + haloSize,
                    bounds.height + haloSize);
            g2.setColor(terminalColor);
            g2.fillOval(bounds.x, bounds.y, bounds.width, bounds.height);
            g2.setStroke(new BasicStroke(1.4f));
            g2.setColor(PaintUtils.mix(terminalColor, palette.background, hovered ? 0.12f : 0.30f));
            g2.drawOval(bounds.x, bounds.y, bounds.width, bounds.height);

            int textY = centerY + metrics.getAscent() / 2 - 2;
            g2.setColor(palette.foreground);
            if (terminal.isInput()) {
                if (!terminal.hasWidget()) {
                    g2.drawString(terminal.getLabel(), PADDING + 12, textY);
                }
            } else {
                int textWidth = metrics.stringWidth(terminal.getLabel());
                g2.drawString(terminal.getLabel(), contentWidth() - PADDING - 12 - textWidth, textY);
            }
        }
    }

    private void paintInputTerminalWidgetPreviews(Graphics2D g2) {
        if (widgetsInteractive()) {
            return;
        }
        ElowbePalette palette = PaintUtils.palette();
        g2.setFont(UIManager.getFont("Elowbe.font.small"));
        for (NodeTerminal terminal : inputs) {
            if (!terminal.hasWidget()) {
                continue;
            }
            Rectangle row = terminalWidgetBounds(terminal);
            NodeWidget widget = terminal.getWidget();
            if (widget.getComponent() instanceof JCheckBox) {
                int boxSize = 13;
                int boxY = row.y + (row.height - boxSize) / 2;
                PaintUtils.fillRound(g2, row.x, boxY, boxSize, boxSize, ElowbeDefaults.RADIUS_SM, palette.card);
                PaintUtils.drawRound(g2, row.x, boxY, boxSize, boxSize, ElowbeDefaults.RADIUS_SM, palette.input);
                if (((JCheckBox) widget.getComponent()).isSelected()) {
                    g2.setColor(palette.primary);
                    g2.fillOval(row.x + 4, boxY + 4, 5, 5);
                }
                g2.setColor(palette.foreground);
                g2.drawString(widget.displayText(), row.x + boxSize + 6, row.y + 18);
            } else {
                PaintUtils.fillRound(g2, row.x, row.y, row.width, row.height, ElowbeDefaults.RADIUS_SM, palette.card);
                PaintUtils.drawRound(g2, row.x, row.y, row.width, row.height, ElowbeDefaults.RADIUS_SM, palette.input);
                g2.setColor(palette.foreground);
                g2.drawString(widget.displayText(), row.x + 8, row.y + 18);
            }
        }
    }

    private void paintWidgetLabels(Graphics2D g2) {
        Font font = UIManager.getFont("Elowbe.font.small").deriveFont(Font.BOLD);
        ElowbePalette palette = PaintUtils.palette();
        g2.setFont(font);
        g2.setColor(palette.foreground);
        int y = widgetStartY();
        for (NodeWidget widget : widgets) {
            g2.drawString(widget.getLabel(), PADDING, y + 9);
            y += widgetRowHeight();
        }
    }

    private void paintWidgetPreviews(Graphics2D g2) {
        if (widgetsInteractive()) {
            return;
        }
        ElowbePalette palette = PaintUtils.palette();
        Font font = UIManager.getFont("Elowbe.font.small");
        g2.setFont(font);
        int y = widgetStartY();
        for (NodeWidget widget : widgets) {
            int controlY = y + WIDGET_LABEL_HEIGHT;
            if (widget.getComponent() instanceof JCheckBox) {
                paintCheckboxPreview(g2, widget, controlY, palette);
            } else {
                PaintUtils.fillRound(g2, PADDING, controlY, contentWidth() - PADDING * 2, WIDGET_CONTROL_HEIGHT,
                        ElowbeDefaults.RADIUS_MD, palette.card);
                PaintUtils.drawRound(g2, PADDING, controlY, contentWidth() - PADDING * 2, WIDGET_CONTROL_HEIGHT,
                        ElowbeDefaults.RADIUS_MD, palette.input);
                g2.setColor(palette.foreground);
                g2.drawString(widget.displayText(), PADDING + 10, controlY + 22);
            }
            y += widgetRowHeight();
        }
    }

    private void paintCheckboxPreview(Graphics2D g2, NodeWidget widget, int controlY, ElowbePalette palette) {
        JCheckBox checkbox = (JCheckBox) widget.getComponent();
        int boxSize = 16;
        int boxY = controlY + (WIDGET_CONTROL_HEIGHT - boxSize) / 2;
        PaintUtils.fillRound(g2, PADDING, boxY, boxSize, boxSize, ElowbeDefaults.RADIUS_SM, palette.card);
        PaintUtils.drawRound(g2, PADDING, boxY, boxSize, boxSize, ElowbeDefaults.RADIUS_SM, palette.input);
        if (checkbox.isSelected()) {
            g2.setColor(palette.primary);
            g2.fillOval(PADDING + 4, boxY + 4, 8, 8);
        }
        g2.setColor(palette.foreground);
        g2.drawString(widget.displayText(), PADDING + boxSize + 8, controlY + 22);
    }

    private Rectangle terminalBounds(NodeTerminal terminal) {
        int index = terminal.isInput() ? inputs.indexOf(terminal) : outputs.indexOf(terminal);
        if (index < 0) {
            throw new IllegalArgumentException("Terminal does not belong to this node.");
        }
        int rowHeight = terminalRowHeight();
        int y = HEADER_HEIGHT + PADDING + index * rowHeight + (rowHeight - TERMINAL_SIZE) / 2;
        int x = terminal.isInput() ? 0 : contentWidth() - TERMINAL_SIZE;
        return new Rectangle(x, y, TERMINAL_SIZE, TERMINAL_SIZE);
    }

    private int terminalAreaHeight() {
        int rows = Math.max(inputs.size(), outputs.size());
        return Math.max(2, rows) * terminalRowHeight();
    }

    private int terminalRowHeight() {
        for (NodeTerminal terminal : inputs) {
            if (terminal.hasWidget()) {
                return Math.max(ROW_HEIGHT, WIDGET_CONTROL_HEIGHT + WIDGET_GAP);
            }
        }
        return ROW_HEIGHT;
    }

    private void layoutInputTerminalWidgets(boolean interactive) {
        for (NodeTerminal terminal : inputs) {
            if (!terminal.hasWidget()) {
                continue;
            }
            NodeWidget widget = terminal.getWidget();
            JComponent component = widget.getComponent();
            component.setVisible(interactive);
            applyComponentFont(component, widget.scaledFont(viewScale));
            Rectangle bounds = terminalWidgetBounds(terminal);
            component.setBounds(scaled(bounds.x), scaled(bounds.y), scaled(bounds.width), scaled(bounds.height));
        }
    }

    private Rectangle terminalWidgetBounds(NodeTerminal terminal) {
        int index = inputs.indexOf(terminal);
        if (index < 0) {
            throw new IllegalArgumentException("Terminal does not belong to this node.");
        }
        int rowHeight = terminalRowHeight();
        int y = HEADER_HEIGHT + PADDING + index * rowHeight + (rowHeight - WIDGET_CONTROL_HEIGHT) / 2;
        int x = PADDING + 12;
        int maxRight = outputAt(index) == null ? contentWidth() - PADDING : contentWidth() - PADDING - 76;
        int width = Math.max(72, maxRight - x);
        return new Rectangle(x, y, width, WIDGET_CONTROL_HEIGHT);
    }

    private NodeTerminal outputAt(int index) {
        return index >= 0 && index < outputs.size() ? outputs.get(index) : null;
    }

    private int widgetStartY() {
        return HEADER_HEIGHT + PADDING + terminalAreaHeight() + PADDING;
    }

    private int widgetRowHeight() {
        return WIDGET_LABEL_HEIGHT + WIDGET_CONTROL_HEIGHT + WIDGET_GAP;
    }

    private boolean widgetsInteractive() {
        return viewScale >= INTERACTIVE_WIDGET_MIN_SCALE;
    }

    private Point toBasePoint(Point point) {
        return new Point((int) Math.round(point.x / viewScale), (int) Math.round(point.y / viewScale));
    }

    private int scaled(int value) {
        return Math.max(1, (int) Math.round(value * viewScale));
    }

    private static void applyComponentFont(Component component, Font font) {
        if (font == null) {
            return;
        }
        component.setFont(font);
        if (component instanceof java.awt.Container) {
            for (Component child : ((java.awt.Container) component).getComponents()) {
                applyComponentFont(child, font);
            }
        }
    }

    private void updateCursor(Point point) {
        if (interaction == Interaction.CONNECTING) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return;
        }
        if (interaction == Interaction.DRAGGING_NODE || interaction == Interaction.PANNING) {
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            return;
        }
        if (terminalAt(toBasePoint(point)) != null) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        }
    }

    private void updateHoveredTerminal(Point point) {
        NodeTerminal terminal = point == null ? null : terminalAt(toBasePoint(point));
        if (hoveredTerminal == terminal) {
            return;
        }
        hoveredTerminal = terminal;
        repaint();
    }

    private Color brighten(Color color) {
        return PaintUtils.mix(color, Color.WHITE, ElowbeDefaults.theme().isDark() ? 0.36f : 0.24f);
    }

    private int contentWidth() {
        return getPreferredSize().width;
    }

    private int contentHeight() {
        return getPreferredSize().height;
    }

    private NodeCanvas canvas() {
        return getParent() instanceof NodeCanvas ? (NodeCanvas) getParent() : null;
    }

    private void resizeToPreferred() {
        NodeCanvas canvas = canvas();
        if (canvas != null) {
            canvas.refreshNodeBounds();
        } else {
            setSize(getPreferredSize());
        }
        doLayout();
        revalidate();
        repaint();
    }

    private void moveToFront() {
        if (getParent() != null) {
            getParent().setComponentZOrder(this, 0);
            getParent().repaint();
        }
    }

    private enum Interaction {
        NONE,
        CONNECTING,
        DRAGGING_NODE,
        PANNING
    }

    public static final class NodeWidget {
        private final String label;
        private final JComponent component;
        private final Supplier<JComponent> duplicateFactory;
        private final Font baseFont;

        NodeWidget(String label, JComponent component, Supplier<JComponent> duplicateFactory) {
            this.label = Objects.requireNonNull(label, "label");
            this.component = Objects.requireNonNull(component, "component");
            this.duplicateFactory = Objects.requireNonNull(duplicateFactory, "duplicateFactory");
            this.baseFont = component.getFont();
            component.setOpaque(false);
        }

        public String getLabel() {
            return label;
        }

        public JComponent getComponent() {
            return component;
        }

        String type() {
            if (component instanceof JTextField) {
                return "text";
            }
            if (component instanceof JSpinner) {
                return "number";
            }
            if (component instanceof JCheckBox) {
                return "checkbox";
            }
            if (component instanceof JComboBox<?>) {
                return "combo";
            }
            if (component instanceof JLabel) {
                return "label";
            }
            return "component";
        }

        Object value() {
            if (component instanceof JTextField) {
                return ((JTextField) component).getText();
            }
            if (component instanceof JSpinner) {
                return ((JSpinner) component).getValue();
            }
            if (component instanceof JCheckBox) {
                return Boolean.valueOf(((JCheckBox) component).isSelected());
            }
            if (component instanceof JComboBox<?>) {
                Object item = ((JComboBox<?>) component).getSelectedItem();
                return item == null ? "" : item.toString();
            }
            if (component instanceof JLabel) {
                return ((JLabel) component).getText();
            }
            return displayText();
        }

        private JComponent duplicateComponent() {
            return duplicateFactory.get();
        }

        private Font scaledFont(double scale) {
            Font font = baseFont;
            if (font == null) {
                font = UIManager.getFont("Label.font");
            }
            if (font == null) {
                return null;
            }
            return font.deriveFont(Math.max(9f, (float) (font.getSize2D() * scale)));
        }

        private String displayText() {
            if (component instanceof JTextField) {
                return ((JTextField) component).getText();
            }
            if (component instanceof JSpinner) {
                return String.valueOf(((JSpinner) component).getValue());
            }
            if (component instanceof JCheckBox) {
                return ((JCheckBox) component).getText();
            }
            if (component instanceof JComboBox<?>) {
                Object item = ((JComboBox<?>) component).getSelectedItem();
                return item == null ? "" : item.toString();
            }
            if (component instanceof JLabel) {
                return ((JLabel) component).getText();
            }
            return component.getClass().getSimpleName();
        }
    }

    private static JComponent cloneWidgetComponent(JComponent component) {
        if (component instanceof JTextField) {
            JTextField field = new JTextField(((JTextField) component).getText());
            field.putClientProperty(ElowbeDefaults.PLACEHOLDER_KEY,
                    component.getClientProperty(ElowbeDefaults.PLACEHOLDER_KEY));
            return field;
        }
        if (component instanceof JSpinner) {
            JSpinner spinner = (JSpinner) component;
            if (spinner.getModel() instanceof SpinnerNumberModel) {
                SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
                return new JSpinner(new SpinnerNumberModel(
                        (Number) model.getValue(),
                        (Comparable<?>) model.getMinimum(),
                        (Comparable<?>) model.getMaximum(),
                        (Number) model.getStepSize()));
            }
            return new JSpinner();
        }
        if (component instanceof JCheckBox) {
            JCheckBox original = (JCheckBox) component;
            return new JCheckBox(original.getText(), original.isSelected());
        }
        if (component instanceof JComboBox<?>) {
            JComboBox<?> original = (JComboBox<?>) component;
            JComboBox<Object> combo = new JComboBox<>();
            for (int i = 0; i < original.getItemCount(); i++) {
                combo.addItem(original.getItemAt(i));
            }
            combo.setSelectedItem(original.getSelectedItem());
            return combo;
        }
        if (component instanceof JLabel) {
            return new JLabel(((JLabel) component).getText());
        }
        JComponent clone = new JLabel(component.getClass().getSimpleName());
        clone.setAlignmentX(Component.LEFT_ALIGNMENT);
        return clone;
    }
}
