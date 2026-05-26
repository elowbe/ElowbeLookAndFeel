package com.elowbe.laf.node;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

final class NodeGraphJson {
    private NodeGraphJson() {
    }

    static String toJson(NodeCanvas canvas) {
        StringBuilder builder = new StringBuilder(4096);
        Map<NodeComponent, String> ids = new HashMap<>();
        List<NodeComponent> nodes = canvas.graphNodes();
        builder.append("{\n  \"version\": 1,\n");
        builder.append("  \"viewport\": { \"panX\": ").append(canvas.graphPanX())
                .append(", \"panY\": ").append(canvas.graphPanY())
                .append(", \"zoom\": ").append(canvas.graphZoom()).append(" },\n");
        builder.append("  \"nodes\": [\n");
        for (int i = 0; i < nodes.size(); i++) {
            NodeComponent node = nodes.get(i);
            String id = "node-" + (i + 1);
            ids.put(node, id);
            Point2D.Double location = canvas.graphLocation(node);
            if (i > 0) {
                builder.append(",\n");
            }
            writeNode(builder, node, id, location);
        }
        builder.append("\n  ],\n  \"connections\": [\n");
        int written = 0;
        for (NodeConnection connection : canvas.getConnections()) {
            String sourceId = ids.get(connection.getSourceNode());
            String targetId = ids.get(connection.getTargetNode());
            if (sourceId == null || targetId == null) {
                continue;
            }
            if (written++ > 0) {
                builder.append(",\n");
            }
            builder.append("    { \"source\": ").append(quote(sourceId))
                    .append(", \"sourceTerminal\": ").append(quote(connection.getSourceTerminal().getId()))
                    .append(", \"target\": ").append(quote(targetId))
                    .append(", \"targetTerminal\": ").append(quote(connection.getTargetTerminal().getId()))
                    .append(", \"color\": ").append(quote(colorToHex(connection.getColor()))).append(" }");
        }
        builder.append("\n  ]\n}\n");
        return builder.toString();
    }

    static void load(NodeCanvas canvas, String json) {
        Object parsed = new Parser(json).parse();
        Map<?, ?> root = asObject(parsed, "root");
        Map<?, ?> viewport = asObject(root.get("viewport"), "viewport");
        List<?> nodeValues = asArray(root.get("nodes"), "nodes");
        List<?> connectionValues = asArray(root.get("connections"), "connections");

        List<NodeComponent> nodes = new ArrayList<>();
        Map<NodeComponent, Point2D.Double> locations = new HashMap<>();
        Map<String, NodeComponent> byId = new HashMap<>();
        for (Object value : nodeValues) {
            Map<?, ?> nodeMap = asObject(value, "node");
            String id = string(nodeMap, "id", "");
            NodeComponent node = new NodeComponent(string(nodeMap, "title", "Node"), string(nodeMap, "subtitle", ""));
            readTerminals(node, asArray(nodeMap.get("inputs"), "inputs"), true);
            readTerminals(node, asArray(nodeMap.get("outputs"), "outputs"), false);
            readWidgets(node, asArray(nodeMap.get("widgets"), "widgets"));
            nodes.add(node);
            byId.put(id, node);
            locations.put(node, new Point2D.Double(number(nodeMap, "x", 0), number(nodeMap, "y", 0)));
        }

        List<NodeConnection> connections = new ArrayList<>();
        for (Object value : connectionValues) {
            Map<?, ?> connectionMap = asObject(value, "connection");
            NodeComponent source = byId.get(string(connectionMap, "source", ""));
            NodeComponent target = byId.get(string(connectionMap, "target", ""));
            if (source == null || target == null) {
                continue;
            }
            NodeTerminal sourceTerminal = source.terminalById(NodeTerminal.Side.OUTPUT,
                    string(connectionMap, "sourceTerminal", ""));
            NodeTerminal targetTerminal = target.terminalById(NodeTerminal.Side.INPUT,
                    string(connectionMap, "targetTerminal", ""));
            if (sourceTerminal != null && targetTerminal != null) {
                connections.add(new NodeConnection(source, sourceTerminal, target, targetTerminal,
                        color(string(connectionMap, "color", "#18181b"))));
            }
        }

        canvas.replaceGraph(nodes, locations, connections, number(viewport, "panX", 0),
                number(viewport, "panY", 0), number(viewport, "zoom", 1));
    }

    private static void writeNode(StringBuilder builder, NodeComponent node, String id, Point2D.Double location) {
        builder.append("    {\n");
        builder.append("      \"id\": ").append(quote(id)).append(",\n");
        builder.append("      \"title\": ").append(quote(node.getTitle())).append(",\n");
        builder.append("      \"subtitle\": ").append(quote(node.getSubtitle())).append(",\n");
        builder.append("      \"x\": ").append(location == null ? 0 : location.x).append(",\n");
        builder.append("      \"y\": ").append(location == null ? 0 : location.y).append(",\n");
        writeTerminals(builder, "inputs", node.getInputs());
        builder.append(",\n");
        writeTerminals(builder, "outputs", node.getOutputs());
        builder.append(",\n");
        writeWidgets(builder, node.getWidgets());
        builder.append("\n    }");
    }

    private static void writeTerminals(StringBuilder builder, String key, List<NodeTerminal> terminals) {
        builder.append("      \"").append(key).append("\": [");
        for (int i = 0; i < terminals.size(); i++) {
            NodeTerminal terminal = terminals.get(i);
            if (i > 0) {
                builder.append(", ");
            }
            builder.append("{ \"id\": ").append(quote(terminal.getId()))
                    .append(", \"label\": ").append(quote(terminal.getLabel()))
                    .append(", \"color\": ").append(quote(colorToHex(terminal.getColor())))
                    .append(", \"value\": ");
            writeValue(builder, terminal.getValue());
            if (terminal.hasWidget()) {
                builder.append(", \"widget\": ");
                writeWidgetObject(builder, terminal.getWidget());
            }
            builder.append(" }");
        }
        builder.append("]");
    }

    private static void writeWidgets(StringBuilder builder, List<NodeComponent.NodeWidget> widgets) {
        builder.append("      \"widgets\": [");
        for (int i = 0; i < widgets.size(); i++) {
            NodeComponent.NodeWidget widget = widgets.get(i);
            if (i > 0) {
                builder.append(", ");
            }
            writeWidgetObject(builder, widget);
        }
        builder.append("]");
    }

    private static void writeWidgetObject(StringBuilder builder, NodeComponent.NodeWidget widget) {
        builder.append("{ \"label\": ").append(quote(widget.getLabel()))
                .append(", \"type\": ").append(quote(widget.type()))
                .append(", \"value\": ");
        writeValue(builder, widget.value());
        if (widget.getComponent() instanceof JComboBox<?>) {
            JComboBox<?> combo = (JComboBox<?>) widget.getComponent();
            builder.append(", \"items\": [");
            for (int item = 0; item < combo.getItemCount(); item++) {
                if (item > 0) {
                    builder.append(", ");
                }
                writeValue(builder, combo.getItemAt(item));
            }
            builder.append("]");
        } else if (widget.getComponent() instanceof JSpinner
                && ((JSpinner) widget.getComponent()).getModel() instanceof SpinnerNumberModel) {
            SpinnerNumberModel model = (SpinnerNumberModel) ((JSpinner) widget.getComponent()).getModel();
            builder.append(", \"min\": ");
            writeValue(builder, model.getMinimum());
            builder.append(", \"max\": ");
            writeValue(builder, model.getMaximum());
            builder.append(", \"step\": ");
            writeValue(builder, model.getStepSize());
        }
        builder.append(" }");
    }

    private static void readTerminals(NodeComponent node, List<?> terminals, boolean input) {
        for (Object value : terminals) {
            Map<?, ?> terminalMap = asObject(value, "terminal");
            NodeTerminal terminal;
            if (input && terminalMap.get("widget") instanceof Map<?, ?>) {
                Map<?, ?> widgetMap = asObject(terminalMap.get("widget"), "terminal widget");
                terminal = node.addInput(string(terminalMap, "id", ""), string(terminalMap, "label", ""),
                        color(string(terminalMap, "color", "#3b82f6")),
                        widgetComponent(string(widgetMap, "type", "text"), widgetMap.get("value"), widgetMap));
            } else if (input) {
                terminal = node.addInput(string(terminalMap, "id", ""), string(terminalMap, "label", ""),
                        color(string(terminalMap, "color", "#3b82f6")));
            } else {
                terminal = node.addOutput(string(terminalMap, "id", ""), string(terminalMap, "label", ""),
                        color(string(terminalMap, "color", "#3b82f6")));
            }
            terminal.setValue(terminalMap.get("value"));
        }
    }

    private static void readWidgets(NodeComponent node, List<?> widgets) {
        for (Object value : widgets) {
            Map<?, ?> widgetMap = asObject(value, "widget");
            String label = string(widgetMap, "label", "Widget");
            String type = string(widgetMap, "type", "text");
            Object widgetValue = widgetMap.get("value");
            node.addWidget(label, widgetComponent(type, widgetValue, widgetMap));
        }
    }

    private static JComponent widgetComponent(String type, Object value, Map<?, ?> widgetMap) {
        if ("number".equals(type)) {
            Number current = value instanceof Number ? (Number) value : Double.valueOf(0);
            Number min = widgetMap.get("min") instanceof Number ? (Number) widgetMap.get("min") : Double.valueOf(-9999);
            Number max = widgetMap.get("max") instanceof Number ? (Number) widgetMap.get("max") : Double.valueOf(9999);
            Number step = widgetMap.get("step") instanceof Number ? (Number) widgetMap.get("step") : Double.valueOf(1);
            return new JSpinner(new SpinnerNumberModel(current.doubleValue(), min.doubleValue(), max.doubleValue(),
                    step.doubleValue()));
        }
        if ("checkbox".equals(type)) {
            return new JCheckBox("Enabled", Boolean.TRUE.equals(value));
        }
        if ("combo".equals(type)) {
            JComboBox<String> combo = new JComboBox<>();
            Object items = widgetMap.get("items");
            if (items instanceof List<?>) {
                for (Object item : (List<?>) items) {
                    combo.addItem(item == null ? "" : item.toString());
                }
            }
            combo.setSelectedItem(value == null ? "" : value.toString());
            return combo;
        }
        if ("label".equals(type)) {
            return new JLabel(value == null ? "" : value.toString());
        }
        return new JTextField(value == null ? "" : value.toString());
    }

    private static Map<?, ?> asObject(Object value, String name) {
        if (value instanceof Map<?, ?>) {
            return (Map<?, ?>) value;
        }
        throw new IllegalArgumentException("Expected JSON object for " + name + ".");
    }

    private static List<?> asArray(Object value, String name) {
        if (value instanceof List<?>) {
            return (List<?>) value;
        }
        return Collections.emptyList();
    }

    private static String string(Map<?, ?> map, String key, String fallback) {
        Object value = map.get(key);
        return value == null ? fallback : value.toString();
    }

    private static double number(Map<?, ?> map, String key, double fallback) {
        Object value = map.get(key);
        return value instanceof Number ? ((Number) value).doubleValue() : fallback;
    }

    private static Color color(String hex) {
        return Color.decode(hex);
    }

    private static String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private static void writeValue(StringBuilder builder, Object value) {
        if (value == null) {
            builder.append("null");
        } else if (value instanceof Number || value instanceof Boolean) {
            builder.append(value);
        } else {
            builder.append(quote(value.toString()));
        }
    }

    private static String quote(String value) {
        StringBuilder builder = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"' || c == '\\') {
                builder.append('\\').append(c);
            } else if (c == '\n') {
                builder.append("\\n");
            } else if (c == '\r') {
                builder.append("\\r");
            } else if (c == '\t') {
                builder.append("\\t");
            } else {
                builder.append(c);
            }
        }
        return builder.append('"').toString();
    }

    private static final class Parser {
        private final String text;
        private int index;

        private Parser(String text) {
            this.text = text;
        }

        private Object parse() {
            Object value = parseValue();
            skipWhitespace();
            if (index != text.length()) {
                throw error("Unexpected trailing content");
            }
            return value;
        }

        private Object parseValue() {
            skipWhitespace();
            if (peek('{')) {
                return parseObject();
            }
            if (peek('[')) {
                return parseArray();
            }
            if (peek('"')) {
                return parseString();
            }
            if (match("true")) {
                return Boolean.TRUE;
            }
            if (match("false")) {
                return Boolean.FALSE;
            }
            if (match("null")) {
                return null;
            }
            return parseNumber();
        }

        private Map<String, Object> parseObject() {
            expect('{');
            Map<String, Object> map = new LinkedHashMap<>();
            skipWhitespace();
            if (peek('}')) {
                index++;
                return map;
            }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                map.put(key, parseValue());
                skipWhitespace();
                if (peek('}')) {
                    index++;
                    return map;
                }
                expect(',');
            }
        }

        private List<Object> parseArray() {
            expect('[');
            List<Object> list = new ArrayList<>();
            skipWhitespace();
            if (peek(']')) {
                index++;
                return list;
            }
            while (true) {
                list.add(parseValue());
                skipWhitespace();
                if (peek(']')) {
                    index++;
                    return list;
                }
                expect(',');
            }
        }

        private String parseString() {
            expect('"');
            StringBuilder builder = new StringBuilder();
            while (index < text.length()) {
                char c = text.charAt(index++);
                if (c == '"') {
                    return builder.toString();
                }
                if (c == '\\') {
                    if (index >= text.length()) {
                        throw error("Unterminated escape");
                    }
                    char escaped = text.charAt(index++);
                    if (escaped == 'n') {
                        builder.append('\n');
                    } else if (escaped == 'r') {
                        builder.append('\r');
                    } else if (escaped == 't') {
                        builder.append('\t');
                    } else {
                        builder.append(escaped);
                    }
                } else {
                    builder.append(c);
                }
            }
            throw error("Unterminated string");
        }

        private Number parseNumber() {
            int start = index;
            if (peek('-')) {
                index++;
            }
            while (index < text.length() && Character.isDigit(text.charAt(index))) {
                index++;
            }
            if (peek('.')) {
                index++;
                while (index < text.length() && Character.isDigit(text.charAt(index))) {
                    index++;
                }
            }
            String number = text.substring(start, index);
            if (number.isEmpty() || "-".equals(number)) {
                throw error("Expected value");
            }
            return Double.valueOf(number);
        }

        private void skipWhitespace() {
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
                index++;
            }
        }

        private boolean match(String token) {
            if (text.startsWith(token, index)) {
                index += token.length();
                return true;
            }
            return false;
        }

        private boolean peek(char c) {
            return index < text.length() && text.charAt(index) == c;
        }

        private void expect(char c) {
            skipWhitespace();
            if (!peek(c)) {
                throw error("Expected '" + c + "'");
            }
            index++;
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(message + " at character " + index + ".");
        }
    }
}
