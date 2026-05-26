package com.elowbe.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.elowbe.laf.Elowbe;
import com.elowbe.laf.node.NodeCanvas;
import com.elowbe.laf.node.NodeComponent;
import com.elowbe.laf.node.NodeConnection;
import com.elowbe.laf.node.NodeTerminal;
import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbeTheme;

public final class NodeGraphDemoApp {
    private final NodeCanvas canvas = new NodeCanvas();
    private final List<NodeTerminal> colorableTerminals = new ArrayList<>();
    private final Random random = new Random();
    private NodeComponent sourceNode;
    private NodeComponent filterNode;
    private NodeComponent mixNode;
    private NodeComponent previewNode;
    private NodeTerminal sourceImage;
    private NodeTerminal sourceMask;
    private NodeTerminal filterImageIn;
    private NodeTerminal filterImageOut;
    private NodeTerminal mixImageIn;
    private NodeTerminal mixMaskIn;
    private NodeTerminal mixBlendIn;
    private NodeTerminal mixImageOut;
    private NodeTerminal previewImageIn;

    private NodeGraphDemoApp() {
        seedGraph();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Elowbe.install(ElowbeTheme.LIGHT);
            JFrame frame = createFrame();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    static void showWindow(Component owner) {
        JFrame frame = createFrame();
        frame.setLocationRelativeTo(owner);
        frame.setVisible(true);
    }

    private static JFrame createFrame() {
        NodeGraphDemoApp app = new NodeGraphDemoApp();
        JFrame frame = new JFrame("Elowbe Node Graph Demo");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1120, 720));
        frame.setContentPane(app.content(frame));
        frame.pack();
        return frame;
    }

    private JPanel content(JFrame frame) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIManager.getColor("Panel.background"));
        root.add(header(frame), BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(canvas);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(8, 24, 24, 24));
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scrollPane, BorderLayout.CENTER);

        return root;
    }

    private JPanel header(JFrame frame) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(16, 24, 8, 24));
        header.setOpaque(false);

        JPanel copy = new JPanel(new BorderLayout(0, 3));
        copy.setOpaque(false);
        JLabel title = new JLabel("Node Graph");
        title.setFont(UIManager.getFont("Elowbe.font.title").deriveFont(18f));
        JLabel subtitle = DemoStyles.muted(
                "Left-drag terminals to connect, middle-click a terminal to disconnect, middle-drag the surface to pan, and scroll to zoom.");
        copy.add(title, BorderLayout.NORTH);
        copy.add(subtitle, BorderLayout.SOUTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton reset = DemoStyles.button("Reset Graph", "outline");
        reset.putClientProperty(ElowbeDefaults.ROUNDED_KEY, Boolean.TRUE);
        reset.addActionListener(event -> resetGraph());

        JButton randomize = DemoStyles.button("Randomize Colors", "primary");
        randomize.putClientProperty(ElowbeDefaults.ROUNDED_KEY, Boolean.TRUE);
        randomize.addActionListener(event -> randomizeColors());

        JButton save = DemoStyles.button("Save JSON", "outline");
        save.putClientProperty(ElowbeDefaults.ROUNDED_KEY, Boolean.TRUE);
        save.addActionListener(event -> saveJson(frame));

        JButton load = DemoStyles.button("Load JSON", "outline");
        load.putClientProperty(ElowbeDefaults.ROUNDED_KEY, Boolean.TRUE);
        load.addActionListener(event -> loadJson(frame));

        JButton toggle = DemoStyles.button(buttonText(), "outline");
        toggle.putClientProperty(ElowbeDefaults.ROUNDED_KEY, Boolean.TRUE);
        toggle.addActionListener(event -> {
            Elowbe.toggleTheme();
            toggle.setText(buttonText());
            DemoStyles.applyFontRoles(frame);
            frame.revalidate();
            frame.repaint();
        });

        actions.add(reset);
        actions.add(randomize);
        actions.add(save);
        actions.add(load);
        actions.add(toggle);

        header.add(copy, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private void seedGraph() {
        sourceNode = new NodeComponent("Image Input", "Source asset");
        sourceImage = addColorable(sourceNode.addOutput("image", "image", new Color(59, 130, 246)));
        sourceMask = addColorable(sourceNode.addOutput("mask", "alpha mask", new Color(168, 85, 247)));
        sourceImage.setValue("source-image.png");
        sourceMask.setValue("source-alpha-mask.png");
        JTextField sourcePath = new JTextField("assets/portrait.png");
        sourcePath.putClientProperty(ElowbeDefaults.PLACEHOLDER_KEY, "Image path");
        sourceNode.addWidget("Path", sourcePath);

        filterNode = new NodeComponent("Color Grade", "Adjust tone");
        filterImageIn = addColorable(filterNode.addInput("image", "image", new Color(59, 130, 246)));
        filterImageOut = addColorable(filterNode.addOutput("graded", "graded", new Color(16, 185, 129)));
        filterImageOut.setValue("graded-image.png");
        filterNode.addWidget("Exposure", new JSpinner(new SpinnerNumberModel(0.35, -2.0, 2.0, 0.05)));
        filterNode.addWidget("Preset", new JComboBox<>(new String[] { "Neutral", "Warm", "Cool", "High Contrast" }));

        mixNode = new NodeComponent("Composite", "Merge layers");
        mixImageIn = addColorable(mixNode.addInput("base", "base image", new Color(16, 185, 129)));
        mixMaskIn = addColorable(mixNode.addInput("mask", "mask", new Color(168, 85, 247)));
        mixBlendIn = addColorable(mixNode.addInput("blend", "blend", new Color(99, 102, 241),
                new JComboBox<>(new String[] { "Normal", "Multiply", "Screen", "Overlay" })));
        mixImageOut = addColorable(mixNode.addOutput("result", "result", new Color(245, 158, 11)));
        mixImageOut.setValue("composited-result.png");
        mixNode.addWidget("Opacity", new JSpinner(new SpinnerNumberModel(85, 0, 100, 1)));
        mixNode.addWidget("Invert Mask", new JCheckBox("Enabled", false));

        previewNode = new NodeComponent("Preview", "Render output");
        previewImageIn = addColorable(previewNode.addInput("image", "image", new Color(245, 158, 11)));
        previewNode.addWidget("Show Grid", new JCheckBox("Enabled", true));

        canvas.addNode(sourceNode, 52, 72);
        canvas.addNode(filterNode, 340, 46);
        canvas.addNode(mixNode, 620, 148);
        canvas.addNode(previewNode, 330, 338);
        connectSeedLinks();
        canvas.clearHistory();
    }

    private NodeTerminal addColorable(NodeTerminal terminal) {
        colorableTerminals.add(terminal);
        return terminal;
    }

    private void resetGraph() {
        canvas.clearGraph();
        colorableTerminals.clear();
        sourceNode = null;
        filterNode = null;
        mixNode = null;
        previewNode = null;
        sourceImage = null;
        sourceMask = null;
        filterImageIn = null;
        filterImageOut = null;
        mixImageIn = null;
        mixMaskIn = null;
        mixBlendIn = null;
        mixImageOut = null;
        previewImageIn = null;
        seedGraph();
    }

    private void connectSeedLinks() {
        canvas.clearConnections();
        canvas.connect(sourceNode, sourceImage, filterNode, filterImageIn, sourceImage.getColor());
        canvas.connect(filterNode, filterImageOut, mixNode, mixImageIn, filterImageOut.getColor());
        canvas.connect(sourceNode, sourceImage, mixNode, mixImageIn, sourceImage.getColor());
        canvas.connect(sourceNode, sourceMask, mixNode, mixMaskIn, sourceMask.getColor());
        canvas.connect(mixNode, mixImageOut, previewNode, previewImageIn, mixImageOut.getColor());
    }

    private void randomizeColors() {
        Color[] swatches = {
                new Color(59, 130, 246),
                new Color(16, 185, 129),
                new Color(168, 85, 247),
                new Color(245, 158, 11),
                new Color(244, 63, 94),
                new Color(20, 184, 166)
        };
        for (NodeTerminal terminal : colorableTerminals) {
            terminal.setColor(swatches[random.nextInt(swatches.length)]);
        }
        for (NodeConnection connection : canvas.getConnections()) {
            connection.setColor(connection.getSourceTerminal().getColor());
        }
        canvas.repaint();
    }

    private void saveJson(Component owner) {
        JFileChooser chooser = jsonChooser();
        chooser.setSelectedFile(new java.io.File("elowbe-node-graph.json"));
        if (chooser.showSaveDialog(owner) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Files.writeString(chooser.getSelectedFile().toPath(), canvas.toJson(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(owner, exception.getMessage(), "Save Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadJson(Component owner) {
        JFileChooser chooser = jsonChooser();
        if (chooser.showOpenDialog(owner) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            canvas.loadJson(Files.readString(chooser.getSelectedFile().toPath(), StandardCharsets.UTF_8));
            rebuildDemoTerminalList();
        } catch (IOException | IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(owner, exception.getMessage(), "Load Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JFileChooser jsonChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("JSON files", "json"));
        return chooser;
    }

    private void rebuildDemoTerminalList() {
        colorableTerminals.clear();
        for (NodeComponent node : canvas.getNodes()) {
            colorableTerminals.addAll(node.getInputs());
            colorableTerminals.addAll(node.getOutputs());
        }
    }

    private static String buttonText() {
        return Elowbe.theme().isDark() ? "Switch to Light" : "Switch to Dark";
    }
}
