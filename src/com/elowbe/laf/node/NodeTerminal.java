package com.elowbe.laf.node;

import java.awt.Color;
import java.util.Objects;

public final class NodeTerminal {
    public enum Side {
        INPUT,
        OUTPUT
    }

    private final String id;
    private String label;
    private final Side side;
    private Color color;
    private Object value;
    private NodeComponent.NodeWidget widget;

    public NodeTerminal(String id, String label, Side side, Color color) {
        this.id = Objects.requireNonNull(id, "id");
        this.label = Objects.requireNonNull(label, "label");
        this.side = Objects.requireNonNull(side, "side");
        this.color = Objects.requireNonNull(color, "color");
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = Objects.requireNonNull(label, "label");
    }

    public Side getSide() {
        return side;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = Objects.requireNonNull(color, "color");
    }

    public Object getValue() {
        if (widget != null) {
            return widget.value();
        }
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public NodeComponent.NodeWidget getWidget() {
        return widget;
    }

    void setWidget(NodeComponent.NodeWidget widget) {
        this.widget = widget;
    }

    public boolean hasWidget() {
        return widget != null;
    }

    public boolean isInput() {
        return side == Side.INPUT;
    }

    public boolean isOutput() {
        return side == Side.OUTPUT;
    }
}
