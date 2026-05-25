package com.elowbe.demo;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import com.elowbe.laf.Elowbe;
import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbeTheme;

public final class DialogDemoApp {
    private DialogDemoApp() {
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
        JFrame frame = new JFrame("Elowbe Dialog Demo");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(920, 680));
        frame.setContentPane(content(frame));
        frame.pack();
        return frame;
    }

    private static JPanel content(JFrame frame) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIManager.getColor("Panel.background"));
        root.add(header(frame), BorderLayout.NORTH);

        JPanel gallery = new JPanel(new GridBagLayout());
        gallery.setOpaque(false);
        gallery.setBorder(new EmptyBorder(16, 24, 24, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;

        addCard(gallery, messageDialogs(frame), gbc, 0, 0);
        addCard(gallery, confirmDialogs(frame), gbc, 1, 0);
        addCard(gallery, inputDialogs(frame), gbc, 0, 1);
        addCard(gallery, optionDialogs(frame), gbc, 1, 1);
        addCard(gallery, chooserDialogs(frame), gbc, 0, 2);

        JScrollPane scrollPane = new JScrollPane(gallery);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scrollPane, BorderLayout.CENTER);
        return root;
    }

    private static JPanel header(JFrame frame) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(16, 24, 8, 24));
        header.setOpaque(false);

        JPanel copy = new JPanel(new BorderLayout(0, 3));
        copy.setOpaque(false);
        JLabel title = new JLabel("Dialog Demo");
        title.setFont(UIManager.getFont("Elowbe.font.title").deriveFont(18f));
        JLabel subtitle = DemoStyles.muted("Exercise Swing's built-in dialog helpers with the active Elowbe theme.");
        copy.add(title, BorderLayout.NORTH);
        copy.add(subtitle, BorderLayout.SOUTH);

        JButton toggle = DemoStyles.button(buttonText(), "outline");
        toggle.putClientProperty(ElowbeDefaults.ROUNDED_KEY, Boolean.TRUE);
        toggle.addActionListener(event -> {
            Dimension size = frame.getSize();
            Elowbe.toggleTheme();
            frame.setContentPane(content(frame));
            frame.setSize(size);
            DemoStyles.applyFontRoles(frame);
            frame.revalidate();
            frame.repaint();
        });

        header.add(copy, BorderLayout.WEST);
        header.add(toggle, BorderLayout.EAST);
        return header;
    }

    private static void addCard(JPanel gallery, JPanel card, GridBagConstraints gbc, int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = x == 0 && y == 2 ? 2 : 1;
        gallery.add(card, gbc);
    }

    private static JPanel messageDialogs(Component parent) {
        JPanel card = section("Message Dialogs",
                "Covers every JOptionPane message type: plain, information, warning, error, and question.");
        card.add(action("Plain Message", () -> JOptionPane.showMessageDialog(parent,
                "A plain message without a semantic icon.", "Plain Message", JOptionPane.PLAIN_MESSAGE)));
        card.add(action("Information Message", () -> JOptionPane.showMessageDialog(parent,
                "Your preferences have been saved.", "Information Message", JOptionPane.INFORMATION_MESSAGE)));
        card.add(action("Warning Message", () -> JOptionPane.showMessageDialog(parent,
                "This action may take a few minutes.", "Warning Message", JOptionPane.WARNING_MESSAGE)));
        card.add(action("Error Message", () -> JOptionPane.showMessageDialog(parent,
                "The request could not be completed.", "Error Message", JOptionPane.ERROR_MESSAGE)));
        card.add(action("Question Message", () -> JOptionPane.showMessageDialog(parent,
                "Would you like to review the generated settings?", "Question Message", JOptionPane.QUESTION_MESSAGE)));
        return card;
    }

    private static JPanel confirmDialogs(Component parent) {
        JLabel result = DemoStyles.muted("Result: none yet");
        JPanel card = section("Confirm Dialogs",
                "Shows the built-in confirm option sets and records the returned selection.");
        card.add(action("Default Confirm", () -> update(result, okCancelName(JOptionPane.showConfirmDialog(parent,
                "Use the default confirm button set?", "Default Confirm", JOptionPane.DEFAULT_OPTION)))));
        card.add(action("Yes / No", () -> update(result, yesNoName(JOptionPane.showConfirmDialog(parent,
                "Enable desktop notifications?", "Yes / No", JOptionPane.YES_NO_OPTION)))));
        card.add(action("Yes / No / Cancel", () -> update(result, yesNoCancelName(JOptionPane.showConfirmDialog(parent,
                "Save changes before closing?", "Yes / No / Cancel", JOptionPane.YES_NO_CANCEL_OPTION)))));
        card.add(action("OK / Cancel", () -> update(result, okCancelName(JOptionPane.showConfirmDialog(parent,
                "Apply this dialog theme?", "OK / Cancel", JOptionPane.OK_CANCEL_OPTION)))));
        DemoStyles.addGap(card, 20);
        card.add(result);
        return card;
    }

    private static JPanel inputDialogs(Component parent) {
        JLabel result = DemoStyles.muted("Result: none yet");
        JPanel card = section("Input Dialogs",
                "Tests free-form input and selection input from JOptionPane.");
        card.add(action("Text Input", () -> update(result, JOptionPane.showInputDialog(parent,
                "Workspace name:", "Text Input", JOptionPane.QUESTION_MESSAGE))));
        card.add(action("Selection Input", () -> update(result, JOptionPane.showInputDialog(parent,
                "Choose an environment:", "Selection Input", JOptionPane.QUESTION_MESSAGE, null,
                new String[] { "Development", "Staging", "Production" }, "Development"))));
        DemoStyles.addGap(card, 20);
        card.add(result);
        return card;
    }

    private static JPanel optionDialogs(Component parent) {
        JLabel result = DemoStyles.muted("Result: none yet");
        JPanel card = section("Option Dialogs",
                "Exercises custom button labels and a custom component payload.");
        card.add(action("Custom Options", () -> update(result, showCustomOptions(parent))));
        card.add(action("Custom Content", () -> update(result, showCustomContent(parent))));
        DemoStyles.addGap(card, 20);
        card.add(result);
        return card;
    }

    private static JPanel chooserDialogs(Component parent) {
        JLabel result = DemoStyles.muted("Result: none yet");
        JPanel card = section("Chooser Dialogs",
                "Exercises Swing's built-in file and color chooser dialogs.");
        card.add(action("Open File Chooser", () -> update(result, showOpenChooser(parent))));
        card.add(action("Save File Chooser", () -> update(result, showSaveChooser(parent))));
        card.add(action("Color Chooser", () -> update(result, JColorChooser.showDialog(parent,
                "Choose Accent Color", UIManager.getColor("Button.background")))));
        DemoStyles.addGap(card, 20);
        card.add(result);
        return card;
    }

    private static JPanel section(String title, String description) {
        JPanel card = DemoStyles.card();
        card.add(DemoStyles.title(title));
        card.add(DemoStyles.muted(description));
        DemoStyles.addGap(card, 20);
        return card;
    }

    private static JButton action(String text, Runnable runnable) {
        JButton button = DemoStyles.button(text, "outline");
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
        button.addActionListener(event -> runnable.run());
        return button;
    }

    private static Object showCustomOptions(Component parent) {
        String[] options = { "Approve", "Request Changes", "Defer" };
        int result = JOptionPane.showOptionDialog(parent, "How should Elowbe handle this review?", "Custom Options",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        return result >= 0 && result < options.length ? options[result] : "CLOSED_OPTION";
    }

    private static String showCustomContent(Component parent) {
        JPanel panel = DemoStyles.vStack(8);
        JTextField title = DemoStyles.textField("Dialog title");
        JTextArea notes = DemoStyles.textArea("Notes for this dialog test", 4);
        panel.add(DemoStyles.label("Title"));
        panel.add(title);
        panel.add(Box.createVerticalStrut(8));
        panel.add(DemoStyles.label("Notes"));
        panel.add(notes);
        int result = JOptionPane.showConfirmDialog(parent, panel, "Custom Content", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        return okCancelName(result);
    }

    private static Object showOpenChooser(Component parent) {
        JFileChooser chooser = new JFileChooser(new File("."));
        int result = chooser.showOpenDialog(parent);
        return result == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile() : result;
    }

    private static Object showSaveChooser(Component parent) {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setSelectedFile(new File("elowbe-dialog-demo.txt"));
        int result = chooser.showSaveDialog(parent);
        return result == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile() : result;
    }

    private static void update(JLabel label, Object result) {
        label.setText("Result: " + String.valueOf(result));
    }

    private static String yesNoCancelName(int result) {
        if (result == JOptionPane.YES_OPTION) {
            return "YES_OPTION";
        }
        if (result == JOptionPane.NO_OPTION) {
            return "NO_OPTION";
        }
        if (result == JOptionPane.CANCEL_OPTION) {
            return "CANCEL_OPTION";
        }
        if (result == JOptionPane.OK_OPTION) {
            return "OK_OPTION";
        }
        if (result == JOptionPane.CLOSED_OPTION) {
            return "CLOSED_OPTION";
        }
        return Integer.toString(result);
    }

    private static String yesNoName(int result) {
        if (result == JOptionPane.YES_OPTION) {
            return "YES_OPTION";
        }
        if (result == JOptionPane.NO_OPTION) {
            return "NO_OPTION";
        }
        if (result == JOptionPane.CLOSED_OPTION) {
            return "CLOSED_OPTION";
        }
        return Integer.toString(result);
    }

    private static String okCancelName(int result) {
        if (result == JOptionPane.OK_OPTION) {
            return "OK_OPTION";
        }
        if (result == JOptionPane.CANCEL_OPTION) {
            return "CANCEL_OPTION";
        }
        if (result == JOptionPane.CLOSED_OPTION) {
            return "CLOSED_OPTION";
        }
        return Integer.toString(result);
    }

    private static String buttonText() {
        return Elowbe.theme().isDark() ? "Switch to Light" : "Switch to Dark";
    }
}
