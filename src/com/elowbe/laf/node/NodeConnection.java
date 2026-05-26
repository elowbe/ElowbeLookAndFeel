package com.elowbe.laf.node;

import java.awt.Color;
import java.util.Objects;

public final class NodeConnection {
    private final NodeComponent sourceNode;
    private final NodeTerminal sourceTerminal;
    private final NodeComponent targetNode;
    private final NodeTerminal targetTerminal;
    private Color color;

    public NodeConnection(
            NodeComponent sourceNode,
            NodeTerminal sourceTerminal,
            NodeComponent targetNode,
            NodeTerminal targetTerminal,
            Color color) {
        this.sourceNode = Objects.requireNonNull(sourceNode, "sourceNode");
        this.sourceTerminal = requireOutput(sourceTerminal);
        this.targetNode = Objects.requireNonNull(targetNode, "targetNode");
        this.targetTerminal = requireInput(targetTerminal);
        this.color = Objects.requireNonNull(color, "color");
    }

    public NodeComponent getSourceNode() {
        return sourceNode;
    }

    public NodeTerminal getSourceTerminal() {
        return sourceTerminal;
    }

    public NodeComponent getTargetNode() {
        return targetNode;
    }

    public NodeTerminal getTargetTerminal() {
        return targetTerminal;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = Objects.requireNonNull(color, "color");
    }

    private static NodeTerminal requireOutput(NodeTerminal terminal) {
        Objects.requireNonNull(terminal, "sourceTerminal");
        if (!terminal.isOutput()) {
            throw new IllegalArgumentException("Source terminal must be an output.");
        }
        return terminal;
    }

    private static NodeTerminal requireInput(NodeTerminal terminal) {
        Objects.requireNonNull(terminal, "targetTerminal");
        if (!terminal.isInput()) {
            throw new IllegalArgumentException("Target terminal must be an input.");
        }
        return terminal;
    }
}
