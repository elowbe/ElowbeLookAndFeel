package com.elowbe.demo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import com.elowbe.laf.Elowbe;
import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbeTheme;

public final class ElowbeDemoApp {
    private ElowbeDemoApp() {
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Elowbe.install(ElowbeTheme.LIGHT);
            JFrame frame = new JFrame("Elowbe LookAndFeel");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setMinimumSize(new Dimension(1180, 760));
            frame.setContentPane(content(frame));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static JPanel content(JFrame frame) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIManager.getColor("Panel.background"));
        root.add(header(frame), BorderLayout.NORTH);

        ComponentGalleryPanel gallery = new ComponentGalleryPanel();
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
        JLabel title = new JLabel("Elowbe LookAndFeel");
        title.setFont(UIManager.getFont("Elowbe.font.title").deriveFont(18f));
        JLabel subtitle = DemoStyles.muted("Modern Swing components inspired by the supplied light and dark web UI references.");
        copy.add(title, BorderLayout.NORTH);
        copy.add(subtitle, BorderLayout.SOUTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton dialogs = DemoStyles.button("Dialogs", "outline");
        dialogs.putClientProperty(ElowbeDefaults.ROUNDED_KEY, Boolean.TRUE);
        dialogs.addActionListener(event -> DialogDemoApp.showWindow(frame));

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

        actions.add(dialogs);
        actions.add(toggle);

        header.add(copy, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private static String buttonText() {
        return Elowbe.theme().isDark() ? "Switch to Light" : "Switch to Dark";
    }
}
