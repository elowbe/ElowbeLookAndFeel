package com.elowbe.laf.node;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbePalette;
import com.elowbe.laf.util.PaintUtils;

public class NodeCanvas extends JComponent {
    private static final long serialVersionUID = 1L;

    private static final int GRID_SIZE = 24;
    private static final double MIN_ZOOM = 0.45;
    private static final double MAX_ZOOM = 1.80;
    private static final int HISTORY_LIMIT = 100;

    private final List<NodeConnection> connections = new ArrayList<>();
    private final Map<NodeComponent, Point2D.Double> nodeLocations = new HashMap<>();
    private final Set<NodeComponent> selectedNodes = new LinkedHashSet<>();
    private final List<NodeContextMenuContributor> contextMenuContributors = new ArrayList<>();
    private final Deque<GraphState> undoStack = new ArrayDeque<>();
    private final Deque<GraphState> redoStack = new ArrayDeque<>();
    private ConnectionDrag connectionDrag;
    private TerminalHit highlightedConnectionTarget;
    private NodeDrag nodeDrag;
    private GraphClipboard clipboard;
    private Point lastMousePoint;
    private Point selectionStart;
    private Rectangle selectionRect;
    private Point panStart;
    private double panStartX;
    private double panStartY;
    private double panX;
    private double panY;
    private double zoom = 1.0;
    private boolean restoringHistory;

    public NodeCanvas() {
        setLayout(null);
        setOpaque(false);
        setFocusable(true);
        setPreferredSize(new Dimension(980, 560));
        installKeyBindings();
        installCanvasMouseBehavior();
    }

    public void addNode(NodeComponent node, int x, int y) {
        Objects.requireNonNull(node, "node");
        nodeLocations.put(node, new Point2D.Double(x, y));
        add(node);
        refreshNodeBounds();
        repaint();
    }

    public NodeConnection connect(
            NodeComponent sourceNode,
            NodeTerminal sourceTerminal,
            NodeComponent targetNode,
            NodeTerminal targetTerminal) {
        Color color = PaintUtils.mix(sourceTerminal.getColor(), targetTerminal.getColor(), 0.50f);
        return connect(sourceNode, sourceTerminal, targetNode, targetTerminal, color);
    }

    public NodeConnection connect(
            NodeComponent sourceNode,
            NodeTerminal sourceTerminal,
            NodeComponent targetNode,
            NodeTerminal targetTerminal,
            Color color) {
        return connectInternal(sourceNode, sourceTerminal, targetNode, targetTerminal, color, true);
    }

    private NodeConnection connectInternal(
            NodeComponent sourceNode,
            NodeTerminal sourceTerminal,
            NodeComponent targetNode,
            NodeTerminal targetTerminal,
            Color color,
            boolean recordHistory) {
        if (!canConnect(sourceNode, sourceTerminal, targetNode, targetTerminal)) {
            return null;
        }
        GraphState before = recordHistory ? captureGraphState() : null;
        NodeConnection connection = new NodeConnection(sourceNode, sourceTerminal, targetNode, targetTerminal, color);
        connections.add(connection);
        if (recordHistory) {
            pushUndo(before);
        }
        repaint();
        return connection;
    }

    public List<NodeConnection> getConnections() {
        return Collections.unmodifiableList(connections);
    }

    public String toJson() {
        return NodeGraphJson.toJson(this);
    }

    public void loadJson(String json) {
        Objects.requireNonNull(json, "json");
        GraphState before = captureGraphState();
        NodeGraphJson.load(this, json);
        pushUndo(before);
    }

    public List<NodeComponent> getSelectedNodes() {
        return Collections.unmodifiableList(new ArrayList<>(selectedNodes));
    }

    public List<NodeComponent> getNodes() {
        return Collections.unmodifiableList(graphNodes());
    }

    public void setNodeSelected(NodeComponent node, boolean selected) {
        Objects.requireNonNull(node, "node");
        if (selected) {
            selectedNodes.add(node);
        } else {
            selectedNodes.remove(node);
        }
        node.setSelected(selected);
        repaint();
    }

    public void clearSelection() {
        for (NodeComponent node : new ArrayList<>(selectedNodes)) {
            node.setSelected(false);
        }
        selectedNodes.clear();
        repaint();
    }

    public void addNodeContextMenuContributor(NodeContextMenuContributor contributor) {
        contextMenuContributors.add(Objects.requireNonNull(contributor, "contributor"));
    }

    public void removeNodeContextMenuContributor(NodeContextMenuContributor contributor) {
        contextMenuContributors.remove(contributor);
    }

    public void deleteNode(NodeComponent node) {
        Objects.requireNonNull(node, "node");
        if (!nodeLocations.containsKey(node)) {
            return;
        }
        GraphState before = captureGraphState();
        deleteNodeInternal(node);
        pushUndo(before);
    }

    private void deleteNodeInternal(NodeComponent node) {
        connections.removeIf(connection ->
                connection.getSourceNode() == node || connection.getTargetNode() == node);
        selectedNodes.remove(node);
        node.setSelected(false);
        nodeLocations.remove(node);
        remove(node);
        revalidate();
        repaint();
    }

    public void deleteSelectedNodes() {
        if (selectedNodes.isEmpty()) {
            return;
        }
        GraphState before = captureGraphState();
        for (NodeComponent node : new ArrayList<>(selectedNodes)) {
            deleteNodeInternal(node);
        }
        selectedNodes.clear();
        pushUndo(before);
        repaint();
    }

    public void copySelection() {
        if (selectedNodes.isEmpty()) {
            return;
        }
        clipboard = GraphClipboard.capture(new ArrayList<>(selectedNodes), connections, nodeLocations);
    }

    public void cutSelection() {
        if (selectedNodes.isEmpty()) {
            return;
        }
        GraphState before = captureGraphState();
        copySelection();
        for (NodeComponent node : new ArrayList<>(selectedNodes)) {
            deleteNodeInternal(node);
        }
        selectedNodes.clear();
        pushUndo(before);
        repaint();
    }

    public void pasteClipboard() {
        if (clipboard == null || clipboard.nodes.isEmpty()) {
            return;
        }
        GraphState before = captureGraphState();
        clearSelection();
        Map<Integer, NodeComponent> pastedNodes = new HashMap<>();
        Point2D.Double anchor = pasteAnchor();
        double minX = clipboard.minX();
        double minY = clipboard.minY();
        for (int index = 0; index < clipboard.nodes.size(); index++) {
            NodeSnapshot snapshot = clipboard.nodes.get(index);
            NodeComponent node = snapshot.node.duplicate();
            pastedNodes.put(index, node);
            addNode(node, (int) Math.round(anchor.x + snapshot.location.x - minX),
                    (int) Math.round(anchor.y + snapshot.location.y - minY));
            setNodeSelected(node, true);
        }
        for (ConnectionSnapshot snapshot : clipboard.connections) {
            NodeComponent sourceNode = pastedNodes.get(snapshot.sourceIndex);
            NodeComponent targetNode = pastedNodes.get(snapshot.targetIndex);
            if (sourceNode == null || targetNode == null) {
                continue;
            }
            NodeTerminal sourceTerminal = sourceNode.terminalById(NodeTerminal.Side.OUTPUT, snapshot.sourceTerminalId);
            NodeTerminal targetTerminal = targetNode.terminalById(NodeTerminal.Side.INPUT, snapshot.targetTerminalId);
            if (sourceTerminal != null && targetTerminal != null) {
                connectInternal(sourceNode, sourceTerminal, targetNode, targetTerminal, snapshot.color, false);
            }
        }
        pushUndo(before);
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void clearHistory() {
        undoStack.clear();
        redoStack.clear();
    }

    public void undo() {
        if (!canUndo()) {
            return;
        }
        GraphState current = captureGraphState();
        GraphState previous = undoStack.pop();
        restoreGraphState(previous);
        redoStack.push(current);
    }

    public void redo() {
        if (!canRedo()) {
            return;
        }
        GraphState current = captureGraphState();
        GraphState next = redoStack.pop();
        restoreGraphState(next);
        undoStack.push(current);
    }

    void updateMousePosition(Point point) {
        lastMousePoint = point == null ? null : new Point(point);
    }

    public Object resolveInputValue(NodeComponent node, NodeTerminal inputTerminal) {
        List<Object> values = resolveInputValues(node, inputTerminal);
        if (values.isEmpty()) {
            return inputTerminal.getValue();
        }
        if (values.size() == 1) {
            return values.get(0);
        }
        return values;
    }

    public List<Object> resolveInputValues(NodeComponent node, NodeTerminal inputTerminal) {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(inputTerminal, "inputTerminal");
        if (!inputTerminal.isInput()) {
            throw new IllegalArgumentException("Only input terminal values can be resolved.");
        }
        List<Object> values = new ArrayList<>();
        for (NodeConnection connection : connections) {
            if (connection.getTargetNode() == node && connection.getTargetTerminal() == inputTerminal) {
                values.add(connection.getSourceTerminal().getValue());
            }
        }
        if (values.isEmpty() && inputTerminal.getValue() != null) {
            values.add(inputTerminal.getValue());
        }
        return Collections.unmodifiableList(values);
    }

    public void clearConnections() {
        if (connections.isEmpty()) {
            return;
        }
        GraphState before = captureGraphState();
        connections.clear();
        connectionDrag = null;
        pushUndo(before);
        repaint();
    }

    public void clearGraph() {
        if (nodeLocations.isEmpty() && connections.isEmpty()) {
            return;
        }
        GraphState before = captureGraphState();
        clearGraphInternal();
        pushUndo(before);
        revalidate();
        repaint();
    }

    private void clearGraphInternal() {
        connections.clear();
        selectedNodes.clear();
        nodeLocations.clear();
        connectionDrag = null;
        setConnectionTargetHighlight(null);
        nodeDrag = null;
        clipboard = null;
        selectionStart = null;
        selectionRect = null;
        removeAll();
    }

    public void disconnectTerminal(NodeComponent node, NodeTerminal terminal) {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(terminal, "terminal");
        GraphState before = captureGraphState();
        boolean removed = connections.removeIf(connection ->
                (connection.getSourceNode() == node && connection.getSourceTerminal() == terminal)
                        || (connection.getTargetNode() == node && connection.getTargetTerminal() == terminal));
        if (removed) {
            pushUndo(before);
        }
        repaint();
    }

    void showNodeContextMenu(NodeComponent node, int x, int y) {
        requestFocusInWindow();
        if (!selectedNodes.contains(node)) {
            clearSelection();
            setNodeSelected(node, true);
        }
        JPopupMenu menu = new JPopupMenu();
        JMenuItem delete = new JMenuItem(selectedNodes.size() > 1 ? "Delete Selected" : "Delete");
        delete.addActionListener(event -> deleteSelectedNodes());
        menu.add(delete);
        if (!contextMenuContributors.isEmpty()) {
            menu.addSeparator();
            for (NodeContextMenuContributor contributor : contextMenuContributors) {
                contributor.contribute(this, node, menu);
            }
        }
        menu.show(node, x, y);
    }

    public void beginConnectionDrag(NodeComponent node, NodeTerminal terminal, Point point) {
        connectionDrag = new ConnectionDrag(node, terminal, point);
        repaint();
    }

    public void updateConnectionDrag(Point point) {
        if (connectionDrag != null) {
            connectionDrag.current = point;
            updateConnectionTargetHighlight(point);
            repaint();
        }
    }

    public void finishConnectionDrag(Point point) {
        if (connectionDrag == null) {
            return;
        }
        TerminalHit hit = terminalAt(point);
        ConnectionDrag drag = connectionDrag;
        connectionDrag = null;
        setConnectionTargetHighlight(null);
        if (hit != null && (hit.node != drag.node || hit.terminal != drag.terminal)) {
            connectCompatible(drag.node, drag.terminal, hit.node, hit.terminal);
        }
        repaint();
    }

    public void beginNodeDrag(NodeComponent node, Point point, Point localOffset) {
        if (!selectedNodes.contains(node)) {
            clearSelection();
            setNodeSelected(node, true);
        }
        nodeDrag = new NodeDrag(node, localOffset, selectedNodeLocations(), captureGraphState());
        dragNode(node, point);
    }

    public void dragNode(NodeComponent node, Point point) {
        if (nodeDrag == null || nodeDrag.node != node) {
            return;
        }
        Point2D.Double world = toWorld(point);
        Point2D.Double original = nodeDrag.originalLocations.get(node);
        if (original == null) {
            return;
        }
        double targetX = world.x - nodeDrag.localOffset.x;
        double targetY = world.y - nodeDrag.localOffset.y;
        double deltaX = targetX - original.x;
        double deltaY = targetY - original.y;
        for (Map.Entry<NodeComponent, Point2D.Double> entry : nodeDrag.originalLocations.entrySet()) {
            setNodeWorldLocation(entry.getKey(), entry.getValue().x + deltaX, entry.getValue().y + deltaY);
        }
    }

    public void endNodeDrag() {
        if (nodeDrag != null && nodeDrag.moved(nodeLocations)) {
            pushUndo(nodeDrag.beforeState);
        }
        nodeDrag = null;
    }

    public void beginPan(Point point) {
        panStart = point;
        panStartX = panX;
        panStartY = panY;
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    public void dragPan(Point point) {
        if (panStart == null) {
            return;
        }
        panX = panStartX + point.x - panStart.x;
        panY = panStartY + point.y - panStart.y;
        refreshNodeBounds();
        repaint();
    }

    public void endPan() {
        panStart = null;
        setCursor(Cursor.getDefaultCursor());
    }

    public void refreshNodeBounds() {
        for (Map.Entry<NodeComponent, Point2D.Double> entry : nodeLocations.entrySet()) {
            NodeComponent node = entry.getKey();
            Point2D.Double location = entry.getValue();
            Dimension size = node.getPreferredSize();
            node.setViewScale(zoom);
            node.setBounds(
                    (int) Math.round(panX + location.x * zoom),
                    (int) Math.round(panY + location.y * zoom),
                    Math.max(1, (int) Math.round(size.width * zoom)),
                    Math.max(1, (int) Math.round(size.height * zoom)));
            node.doLayout();
        }
        revalidate();
        repaint();
    }

    private void setNodeWorldLocation(NodeComponent node, double x, double y) {
        nodeLocations.put(node, new Point2D.Double(x, y));
        refreshNodeBounds();
        repaint();
    }

    List<NodeComponent> graphNodes() {
        return new ArrayList<>(nodeLocations.keySet());
    }

    public Point2D.Double graphLocation(NodeComponent node) {
        Point2D.Double location = nodeLocations.get(node);
        return location == null ? null : new Point2D.Double(location.x, location.y);
    }

    double graphPanX() {
        return panX;
    }

    double graphPanY() {
        return panY;
    }

    double graphZoom() {
        return zoom;
    }

    void replaceGraph(List<NodeComponent> nodes, Map<NodeComponent, Point2D.Double> locations,
            List<NodeConnection> loadedConnections, double panX, double panY, double zoom) {
        clearGraphInternal();
        this.panX = panX;
        this.panY = panY;
        this.zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));
        for (NodeComponent node : nodes) {
            Point2D.Double location = locations.get(node);
            if (location == null) {
                continue;
            }
            nodeLocations.put(node, new Point2D.Double(location.x, location.y));
            add(node);
        }
        connections.addAll(loadedConnections);
        clearSelection();
        refreshNodeBounds();
        clearHistory();
    }

    private GraphState captureGraphState() {
        return GraphState.capture(nodeLocations, connections, selectedNodes);
    }

    private void restoreGraphState(GraphState state) {
        restoringHistory = true;
        try {
            for (NodeComponent node : nodeLocations.keySet()) {
                node.setSelected(false);
            }
            connections.clear();
            selectedNodes.clear();
            nodeLocations.clear();
            connectionDrag = null;
            setConnectionTargetHighlight(null);
            nodeDrag = null;
            selectionStart = null;
            selectionRect = null;
            removeAll();

            for (NodeState nodeState : state.nodes) {
                nodeLocations.put(nodeState.node, new Point2D.Double(nodeState.location.x, nodeState.location.y));
                add(nodeState.node);
                nodeState.node.setSelected(nodeState.selected);
                if (nodeState.selected) {
                    selectedNodes.add(nodeState.node);
                }
            }
            for (ConnectionState connectionState : state.connections) {
                connections.add(new NodeConnection(
                        connectionState.sourceNode,
                        connectionState.sourceTerminal,
                        connectionState.targetNode,
                        connectionState.targetTerminal,
                        connectionState.color));
            }
            refreshNodeBounds();
        } finally {
            restoringHistory = false;
        }
    }

    private void pushUndo(GraphState before) {
        if (restoringHistory || before == null) {
            return;
        }
        undoStack.push(before);
        while (undoStack.size() > HISTORY_LIMIT) {
            undoStack.removeLast();
        }
        redoStack.clear();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            paintSurface(g2);
            paintGrid(g2);
            if (selectionRect != null) {
                paintSelectionRect(g2);
            }
            for (NodeConnection connection : connections) {
                paintConnection(g2, connection, false);
            }
            if (connectionDrag != null) {
                paintConnectionDrag(g2, connectionDrag);
            }
        } finally {
            g2.dispose();
        }
    }

    private void paintSurface(Graphics2D g2) {
        ElowbePalette palette = PaintUtils.palette();
        PaintUtils.fillRound(g2, 0, 0, getWidth(), getHeight(), ElowbeDefaults.RADIUS_LG, palette.background);
        PaintUtils.drawRound(g2, 0, 0, getWidth(), getHeight(), ElowbeDefaults.RADIUS_LG, palette.border);
    }

    private void paintGrid(Graphics2D g2) {
        ElowbePalette palette = PaintUtils.palette();
        g2.setColor(PaintUtils.withAlpha(palette.border, ElowbeDefaults.theme().isDark() ? 74 : 86));
        g2.setStroke(new BasicStroke(1f));
        double scaledGrid = GRID_SIZE * zoom;
        if (scaledGrid < 8) {
            scaledGrid = 8;
        }
        double startX = panX % scaledGrid;
        double startY = panY % scaledGrid;
        for (double x = startX; x < getWidth(); x += scaledGrid) {
            g2.drawLine((int) Math.round(x), 1, (int) Math.round(x), getHeight() - 2);
        }
        for (double y = startY; y < getHeight(); y += scaledGrid) {
            g2.drawLine(1, (int) Math.round(y), getWidth() - 2, (int) Math.round(y));
        }
    }

    private void paintSelectionRect(Graphics2D g2) {
        ElowbePalette palette = PaintUtils.palette();
        g2.setColor(PaintUtils.withAlpha(palette.ring, 32));
        g2.fill(selectionRect);
        g2.setColor(PaintUtils.withAlpha(palette.ring, 180));
        g2.setStroke(new BasicStroke(1.4f));
        g2.draw(selectionRect);
    }

    private void paintConnection(Graphics2D g2, NodeConnection connection, boolean selected) {
        Point start = terminalPoint(connection.getSourceNode(), connection.getSourceTerminal());
        Point end = terminalPoint(connection.getTargetNode(), connection.getTargetTerminal());
        int distance = Math.max(70, Math.abs(end.x - start.x) / 2);
        CubicCurve2D curve = new CubicCurve2D.Float(
                start.x,
                start.y,
                start.x + distance,
                start.y,
                end.x - distance,
                end.y,
                end.x,
                end.y);
        Color color = selected ? PaintUtils.mix(connection.getColor(), PaintUtils.palette().foreground, 0.18f)
                : connection.getColor();
        g2.setStroke(new BasicStroke(selected ? 3.4f : 2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(PaintUtils.withAlpha(color, 58));
        g2.draw(curve);
        g2.setStroke(new BasicStroke(selected ? 2.3f : 1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(color);
        g2.draw(curve);
    }

    private void paintConnectionDrag(Graphics2D g2, ConnectionDrag drag) {
        Point start = terminalPoint(drag.node, drag.terminal);
        Point end = drag.current;
        if (drag.terminal.isInput()) {
            Point swap = start;
            start = end;
            end = swap;
        }
        int distance = Math.max(70, Math.abs(end.x - start.x) / 2);
        CubicCurve2D curve = new CubicCurve2D.Float(
                start.x,
                start.y,
                start.x + distance,
                start.y,
                end.x - distance,
                end.y,
                end.x,
                end.y);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(PaintUtils.withAlpha(drag.terminal.getColor(), 180));
        g2.draw(curve);
    }

    private Point terminalPoint(NodeComponent node, NodeTerminal terminal) {
        Point point = node.terminalCenter(terminal);
        return new Point(
                node.getX() + (int) Math.round(point.x * zoom),
                node.getY() + (int) Math.round(point.y * zoom));
    }

    private void connectCompatible(
            NodeComponent firstNode,
            NodeTerminal firstTerminal,
            NodeComponent secondNode,
            NodeTerminal secondTerminal) {
        if (firstTerminal.isOutput() && secondTerminal.isInput()) {
            connect(firstNode, firstTerminal, secondNode, secondTerminal, firstTerminal.getColor());
        } else if (firstTerminal.isInput() && secondTerminal.isOutput()) {
            connect(secondNode, secondTerminal, firstNode, firstTerminal, secondTerminal.getColor());
        }
    }

    private void updateConnectionTargetHighlight(Point point) {
        if (connectionDrag == null) {
            setConnectionTargetHighlight(null);
            return;
        }
        TerminalHit hit = terminalAt(point);
        if (hit == null || !canConnect(connectionDrag.node, connectionDrag.terminal, hit.node, hit.terminal)
                && !canConnect(hit.node, hit.terminal, connectionDrag.node, connectionDrag.terminal)) {
            setConnectionTargetHighlight(null);
            return;
        }
        setConnectionTargetHighlight(hit);
    }

    private void setConnectionTargetHighlight(TerminalHit hit) {
        if (sameTerminalHit(highlightedConnectionTarget, hit)) {
            return;
        }
        if (highlightedConnectionTarget != null) {
            highlightedConnectionTarget.node.setConnectionTargetHighlighted(false, null);
        }
        highlightedConnectionTarget = hit;
        if (highlightedConnectionTarget != null) {
            highlightedConnectionTarget.node.setConnectionTargetHighlighted(true, highlightedConnectionTarget.terminal);
        }
    }

    private boolean sameTerminalHit(TerminalHit first, TerminalHit second) {
        if (first == second) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        return first.node == second.node && first.terminal == second.terminal;
    }

    private boolean compatible(NodeTerminal first, NodeTerminal second) {
        return first.isOutput() && second.isInput() || first.isInput() && second.isOutput();
    }

    private boolean canConnect(
            NodeComponent firstNode,
            NodeTerminal firstTerminal,
            NodeComponent secondNode,
            NodeTerminal secondTerminal) {
        if (firstNode == secondNode || firstTerminal == secondTerminal || !compatible(firstTerminal, secondTerminal)) {
            return false;
        }
        NodeComponent sourceNode = firstTerminal.isOutput() ? firstNode : secondNode;
        NodeTerminal sourceTerminal = firstTerminal.isOutput() ? firstTerminal : secondTerminal;
        NodeComponent targetNode = firstTerminal.isInput() ? firstNode : secondNode;
        NodeTerminal targetTerminal = firstTerminal.isInput() ? firstTerminal : secondTerminal;
        return !hasConnection(sourceNode, sourceTerminal, targetNode, targetTerminal)
                && !wouldCreateLoop(sourceNode, targetNode);
    }

    private boolean hasConnection(
            NodeComponent sourceNode,
            NodeTerminal sourceTerminal,
            NodeComponent targetNode,
            NodeTerminal targetTerminal) {
        for (NodeConnection connection : connections) {
            if (connection.getSourceNode() == sourceNode
                    && connection.getSourceTerminal() == sourceTerminal
                    && connection.getTargetNode() == targetNode
                    && connection.getTargetTerminal() == targetTerminal) {
                return true;
            }
        }
        return false;
    }

    private boolean wouldCreateLoop(NodeComponent sourceNode, NodeComponent targetNode) {
        if (sourceNode == targetNode) {
            return true;
        }
        return hasPath(targetNode, sourceNode, new HashSet<>());
    }

    private boolean hasPath(NodeComponent from, NodeComponent to, Set<NodeComponent> visited) {
        if (!visited.add(from)) {
            return false;
        }
        for (NodeConnection connection : connections) {
            if (connection.getSourceNode() != from) {
                continue;
            }
            NodeComponent next = connection.getTargetNode();
            if (next == to || hasPath(next, to, visited)) {
                return true;
            }
        }
        return false;
    }

    private Map<NodeComponent, Point2D.Double> selectedNodeLocations() {
        Map<NodeComponent, Point2D.Double> locations = new HashMap<>();
        for (NodeComponent node : selectedNodes) {
            Point2D.Double location = nodeLocations.get(node);
            if (location != null) {
                locations.put(node, new Point2D.Double(location.x, location.y));
            }
        }
        return locations;
    }

    private void beginSelection(Point point) {
        requestFocusInWindow();
        selectionStart = point;
        selectionRect = new Rectangle(point);
        clearSelection();
    }

    private void updateSelection(Point point) {
        if (selectionStart == null) {
            return;
        }
        selectionRect = normalizedRectangle(selectionStart, point);
        for (Map.Entry<NodeComponent, Point2D.Double> entry : nodeLocations.entrySet()) {
            NodeComponent node = entry.getKey();
            boolean selected = selectionRect.intersects(node.getBounds());
            if (selected) {
                selectedNodes.add(node);
            } else {
                selectedNodes.remove(node);
            }
            node.setSelected(selected);
        }
        repaint();
    }

    private void finishSelection(Point point) {
        updateSelection(point);
        selectionStart = null;
        selectionRect = null;
        repaint();
    }

    private Rectangle normalizedRectangle(Point start, Point end) {
        int x = Math.min(start.x, end.x);
        int y = Math.min(start.y, end.y);
        int width = Math.abs(end.x - start.x);
        int height = Math.abs(end.y - start.y);
        return new Rectangle(x, y, width, height);
    }

    private TerminalHit terminalAt(Point point) {
        for (int i = 0; i < getComponentCount(); i++) {
            if (!(getComponent(i) instanceof NodeComponent)) {
                continue;
            }
            NodeComponent node = (NodeComponent) getComponent(i);
            Rectangle bounds = node.getBounds();
            if (!bounds.contains(point)) {
                continue;
            }
            Point local = new Point(point.x - bounds.x, point.y - bounds.y);
            Point base = new Point(
                    (int) Math.round(local.x / node.getViewScale()),
                    (int) Math.round(local.y / node.getViewScale()));
            NodeTerminal terminal = node.terminalAt(base);
            if (terminal != null) {
                return new TerminalHit(node, terminal);
            }
        }
        return null;
    }

    private Point2D.Double toWorld(Point point) {
        return new Point2D.Double((point.x - panX) / zoom, (point.y - panY) / zoom);
    }

    public Point2D.Double viewToWorld(Point point) {
        Objects.requireNonNull(point, "point");
        return toWorld(point);
    }

    private Point2D.Double pasteAnchor() {
        if (lastMousePoint != null) {
            return toWorld(lastMousePoint);
        }
        return new Point2D.Double(36, 36);
    }

    void zoomAt(Point point, int wheelRotation) {
        double oldZoom = zoom;
        double factor = wheelRotation < 0 ? 1.08 : 0.92;
        zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom * factor));
        if (oldZoom == zoom) {
            return;
        }
        Point2D.Double world = new Point2D.Double((point.x - panX) / oldZoom, (point.y - panY) / oldZoom);
        panX = point.x - world.x * zoom;
        panY = point.y - world.y * zoom;
        refreshNodeBounds();
    }

    private void installKeyBindings() {
        int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke('C', shortcut), "copy");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke('X', shortcut), "cut");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke('V', shortcut), "paste");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("DELETE"), "delete");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("BACK_SPACE"), "delete");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke('A', shortcut), "selectAll");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke('Z', shortcut), "undo");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke('Z', shortcut | InputEvent.SHIFT_DOWN_MASK), "redo");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke('Y', shortcut), "redo");
        getActionMap().put("copy", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                copySelection();
            }
        });
        getActionMap().put("cut", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                cutSelection();
            }
        });
        getActionMap().put("paste", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                pasteClipboard();
            }
        });
        getActionMap().put("delete", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                deleteSelectedNodes();
            }
        });
        getActionMap().put("selectAll", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                for (NodeComponent node : nodeLocations.keySet()) {
                    setNodeSelected(node, true);
                }
            }
        });
        getActionMap().put("undo", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                undo();
            }
        });
        getActionMap().put("redo", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                redo();
            }
        });
    }

    private void installCanvasMouseBehavior() {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                updateMousePosition(event.getPoint());
                requestFocusInWindow();
                if (SwingUtilities.isMiddleMouseButton(event)) {
                    beginPan(event.getPoint());
                } else if (SwingUtilities.isLeftMouseButton(event)) {
                    beginSelection(event.getPoint());
                }
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                updateMousePosition(event.getPoint());
                if (panStart != null) {
                    dragPan(event.getPoint());
                } else if (selectionStart != null) {
                    updateSelection(event.getPoint());
                }
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                updateMousePosition(event.getPoint());
                if (panStart != null) {
                    endPan();
                } else if (selectionStart != null) {
                    finishSelection(event.getPoint());
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent event) {
                updateMousePosition(event.getPoint());
                zoomAt(event.getPoint(), event.getWheelRotation());
                event.consume();
            }

            @Override
            public void mouseMoved(MouseEvent event) {
                updateMousePosition(event.getPoint());
            }
        };
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        addMouseWheelListener(adapter);
    }

    private static final class ConnectionDrag {
        private final NodeComponent node;
        private final NodeTerminal terminal;
        private Point current;

        private ConnectionDrag(NodeComponent node, NodeTerminal terminal, Point current) {
            this.node = node;
            this.terminal = terminal;
            this.current = current;
        }
    }

    private static final class NodeDrag {
        private final NodeComponent node;
        private final Point localOffset;
        private final Map<NodeComponent, Point2D.Double> originalLocations;
        private final GraphState beforeState;

        private NodeDrag(
                NodeComponent node,
                Point localOffset,
                Map<NodeComponent, Point2D.Double> originalLocations,
                GraphState beforeState) {
            this.node = node;
            this.localOffset = localOffset;
            this.originalLocations = originalLocations;
            this.beforeState = beforeState;
        }

        private boolean moved(Map<NodeComponent, Point2D.Double> currentLocations) {
            for (Map.Entry<NodeComponent, Point2D.Double> entry : originalLocations.entrySet()) {
                Point2D.Double current = currentLocations.get(entry.getKey());
                Point2D.Double original = entry.getValue();
                if (current == null || Math.abs(current.x - original.x) > 0.001
                        || Math.abs(current.y - original.y) > 0.001) {
                    return true;
                }
            }
            return false;
        }
    }

    private static final class GraphState {
        private final List<NodeState> nodes;
        private final List<ConnectionState> connections;

        private GraphState(List<NodeState> nodes, List<ConnectionState> connections) {
            this.nodes = nodes;
            this.connections = connections;
        }

        private static GraphState capture(
                Map<NodeComponent, Point2D.Double> nodeLocations,
                List<NodeConnection> connections,
                Set<NodeComponent> selectedNodes) {
            List<NodeState> nodeStates = new ArrayList<>();
            for (Map.Entry<NodeComponent, Point2D.Double> entry : nodeLocations.entrySet()) {
                Point2D.Double location = entry.getValue();
                nodeStates.add(new NodeState(entry.getKey(), new Point2D.Double(location.x, location.y),
                        selectedNodes.contains(entry.getKey())));
            }

            List<ConnectionState> connectionStates = new ArrayList<>();
            for (NodeConnection connection : connections) {
                connectionStates.add(new ConnectionState(
                        connection.getSourceNode(),
                        connection.getSourceTerminal(),
                        connection.getTargetNode(),
                        connection.getTargetTerminal(),
                        connection.getColor()));
            }
            return new GraphState(nodeStates, connectionStates);
        }
    }

    private static final class NodeState {
        private final NodeComponent node;
        private final Point2D.Double location;
        private final boolean selected;

        private NodeState(NodeComponent node, Point2D.Double location, boolean selected) {
            this.node = node;
            this.location = location;
            this.selected = selected;
        }
    }

    private static final class ConnectionState {
        private final NodeComponent sourceNode;
        private final NodeTerminal sourceTerminal;
        private final NodeComponent targetNode;
        private final NodeTerminal targetTerminal;
        private final Color color;

        private ConnectionState(
                NodeComponent sourceNode,
                NodeTerminal sourceTerminal,
                NodeComponent targetNode,
                NodeTerminal targetTerminal,
                Color color) {
            this.sourceNode = sourceNode;
            this.sourceTerminal = sourceTerminal;
            this.targetNode = targetNode;
            this.targetTerminal = targetTerminal;
            this.color = color;
        }
    }

    private static final class GraphClipboard {
        private final List<NodeSnapshot> nodes;
        private final List<ConnectionSnapshot> connections;

        private GraphClipboard(List<NodeSnapshot> nodes, List<ConnectionSnapshot> connections) {
            this.nodes = nodes;
            this.connections = connections;
        }

        private static GraphClipboard capture(
                List<NodeComponent> selectedNodes,
                List<NodeConnection> connections,
                Map<NodeComponent, Point2D.Double> nodeLocations) {
            List<NodeSnapshot> nodes = new ArrayList<>();
            Map<NodeComponent, Integer> indexes = new HashMap<>();
            for (int index = 0; index < selectedNodes.size(); index++) {
                NodeComponent node = selectedNodes.get(index);
                Point2D.Double location = nodeLocations.get(node);
                if (location == null) {
                    continue;
                }
                indexes.put(node, nodes.size());
                nodes.add(new NodeSnapshot(node.duplicate(), new Point2D.Double(location.x, location.y)));
            }

            List<ConnectionSnapshot> copiedConnections = new ArrayList<>();
            for (NodeConnection connection : connections) {
                Integer sourceIndex = indexes.get(connection.getSourceNode());
                Integer targetIndex = indexes.get(connection.getTargetNode());
                if (sourceIndex == null || targetIndex == null) {
                    continue;
                }
                copiedConnections.add(new ConnectionSnapshot(
                        sourceIndex.intValue(),
                        connection.getSourceTerminal().getId(),
                        targetIndex.intValue(),
                        connection.getTargetTerminal().getId(),
                        connection.getColor()));
            }
            return new GraphClipboard(nodes, copiedConnections);
        }

        private double minX() {
            double min = Double.MAX_VALUE;
            for (NodeSnapshot node : nodes) {
                min = Math.min(min, node.location.x);
            }
            return min == Double.MAX_VALUE ? 0 : min;
        }

        private double minY() {
            double min = Double.MAX_VALUE;
            for (NodeSnapshot node : nodes) {
                min = Math.min(min, node.location.y);
            }
            return min == Double.MAX_VALUE ? 0 : min;
        }
    }

    private static final class NodeSnapshot {
        private final NodeComponent node;
        private final Point2D.Double location;

        private NodeSnapshot(NodeComponent node, Point2D.Double location) {
            this.node = node;
            this.location = location;
        }
    }

    private static final class ConnectionSnapshot {
        private final int sourceIndex;
        private final String sourceTerminalId;
        private final int targetIndex;
        private final String targetTerminalId;
        private final Color color;

        private ConnectionSnapshot(
                int sourceIndex,
                String sourceTerminalId,
                int targetIndex,
                String targetTerminalId,
                Color color) {
            this.sourceIndex = sourceIndex;
            this.sourceTerminalId = sourceTerminalId;
            this.targetIndex = targetIndex;
            this.targetTerminalId = targetTerminalId;
            this.color = color;
        }
    }

    private static final class TerminalHit {
        private final NodeComponent node;
        private final NodeTerminal terminal;

        private TerminalHit(NodeComponent node, NodeTerminal terminal) {
            this.node = node;
            this.terminal = terminal;
        }
    }
}
