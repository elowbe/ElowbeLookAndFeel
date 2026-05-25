package com.elowbe.demo;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.elowbe.laf.Elowbe;
import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbeTheme;

public final class NoteTakingDemoApp {
    private final DefaultListModel<Note> notes = new DefaultListModel<>();
    private final JList<Note> noteList = new JList<>(notes);
    private final JTextField searchField = DemoStyles.textField("Search notes...");
    private final JTextField titleField = DemoStyles.textField("Note title");
    private final JTextArea bodyArea = DemoStyles.textArea("Write your note...", 14);
    private final JComboBox<String> categoryCombo = DemoStyles.combo("Personal", "Work", "Ideas", "Archive");
    private final JCheckBox pinnedBox = new JCheckBox("Pinned");
    private final JLabel statusLabel = DemoStyles.muted("Ready");
    private final JLabel wordCountLabel = DemoStyles.muted("0 words");
    private final JLabel previewTitle = DemoStyles.title("Select a note");
    private final JLabel previewMeta = DemoStyles.muted("No note selected");

    private boolean updating;
    private int nextNoteNumber = 4;

    private NoteTakingDemoApp() {
        seedNotes();
        configureBindings();
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
        NoteTakingDemoApp app = new NoteTakingDemoApp();
        JFrame frame = new JFrame("Elowbe Notes Demo");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1190, 700));
        frame.setContentPane(app.content(frame));
        frame.pack();
        app.noteList.setSelectedIndex(0);
        return frame;
    }

    private JPanel content(JFrame frame) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIManager.getColor("Panel.background"));
        root.add(header(frame), BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout(16, 0));
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(16, 24, 24, 24));
        main.add(sidebar(), BorderLayout.WEST);
        main.add(editor(), BorderLayout.CENTER);
        main.add(details(), BorderLayout.EAST);

        root.add(main, BorderLayout.CENTER);
        return root;
    }

    private JPanel header(JFrame frame) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(16, 24, 8, 24));
        header.setOpaque(false);

        JPanel copy = new JPanel(new BorderLayout(0, 3));
        copy.setOpaque(false);
        JLabel title = new JLabel("Notes");
        title.setFont(UIManager.getFont("Elowbe.font.title").deriveFont(18f));
        JLabel subtitle = DemoStyles.muted("A compact note-taking workspace that exercises lists, forms, text areas, and actions.");
        copy.add(title, BorderLayout.NORTH);
        copy.add(subtitle, BorderLayout.SOUTH);

        JButton toggle = DemoStyles.button(buttonText(), "outline");
        toggle.putClientProperty(ElowbeDefaults.ROUNDED_KEY, Boolean.TRUE);
        toggle.addActionListener(event -> {
            Elowbe.toggleTheme();
            DemoStyles.applyFontRoles(frame);
            frame.repaint();
        });

        header.add(copy, BorderLayout.WEST);
        header.add(toggle, BorderLayout.EAST);
        return header;
    }

    private JPanel sidebar() {
        JPanel panel = DemoStyles.card();
        panel.setPreferredSize(new Dimension(350, 1));
        panel.add(DemoStyles.title("Notebook"));
        panel.add(DemoStyles.muted("Filter and switch between draft notes."));
        DemoStyles.addGap(panel, 14);

        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        panel.add(searchField);
        DemoStyles.addGap(panel, 12);

        noteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        noteList.setCellRenderer(new NoteCellRenderer());
        JScrollPane scrollPane = new JScrollPane(noteList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setPreferredSize(new Dimension(350, 360));
        panel.add(scrollPane);

        DemoStyles.addGap(panel, 14);
        JPanel actions = DemoStyles.flow(FlowLayout.LEFT, 8);
        actions.add(actionButton("+ New", this::addNote));
        actions.add(actionButton("Duplicate", this::duplicateNote));
        actions.add(actionButton("Delete", this::deleteNote));
        panel.add(actions);
        return panel;
    }

    private JPanel editor() {
        JPanel panel = DemoStyles.card();
        panel.add(DemoStyles.title("Editor"));
        panel.add(DemoStyles.muted("Edits are kept in memory for the demo session."));
        DemoStyles.addGap(panel, 14);

        panel.add(DemoStyles.label("Title"));
        panel.add(titleField);
        DemoStyles.addGap(panel, 12);

        JPanel row = DemoStyles.flow(FlowLayout.LEFT, 10);
        row.add(labeled("Category", categoryCombo));
        row.add(pinnedBox);
        panel.add(row);
        DemoStyles.addGap(panel, 12);

        panel.add(DemoStyles.label("Body"));
        JScrollPane bodyScroll = new JScrollPane(bodyArea);
        bodyScroll.setBorder(BorderFactory.createEmptyBorder());
        bodyScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        bodyScroll.setPreferredSize(new Dimension(440, 300));
        panel.add(bodyScroll);

        DemoStyles.addGap(panel, 12);
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.add(statusLabel, BorderLayout.WEST);
        footer.add(wordCountLabel, BorderLayout.EAST);
        panel.add(footer);
        return panel;
    }

    private JPanel details() {
        JPanel panel = DemoStyles.card();
        panel.setPreferredSize(new Dimension(250, 1));
        panel.add(DemoStyles.title("Quick View"));
        panel.add(DemoStyles.muted("A lightweight summary of the selected note."));
        DemoStyles.addGap(panel, 18);

        JPanel preview = DemoStyles.card();
        preview.setBorder(new EmptyBorder(14, 14, 14, 14));
        preview.add(previewTitle);
        preview.add(previewMeta);
        DemoStyles.addGap(preview, 14);
        preview.add(DemoStyles.badge("Autosaved locally"));
        preview.setMaximumSize(new Dimension(Integer.MAX_VALUE, preview.getPreferredSize().height + 48));
        panel.add(preview);

        DemoStyles.addGap(panel, 18);
        panel.add(DemoStyles.label("Today"));
        panel.add(DemoStyles.muted("3 draft notes"));
        DemoStyles.addGap(panel, 12);
        panel.add(DemoStyles.label("Tips"));
        panel.add(DemoStyles.muted("Use the search field to narrow the note list."));
        DemoStyles.addGap(panel, 12);
        panel.add(DemoStyles.muted("Toggle the theme to preview both palettes."));
        return panel;
    }

    private void configureBindings() {
        noteList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                loadSelectedNote();
            }
        });
        searchField.getDocument().addDocumentListener(changeListener(this::applyFilter));
        titleField.getDocument().addDocumentListener(changeListener(this::saveEditorState));
        bodyArea.getDocument().addDocumentListener(changeListener(this::saveEditorState));
        categoryCombo.addActionListener(event -> saveEditorState());
        pinnedBox.addActionListener(event -> saveEditorState());
    }

    private void seedNotes() {
        notes.addElement(new Note("Project kickoff", "Work",
                "Draft agenda for the kickoff meeting.\n\n- Goals\n- Timeline\n- Risks", true));
        notes.addElement(new Note("Weekend ideas", "Personal",
                "Try the new coffee place, sketch the app icon, and clean up the desk.", false));
        notes.addElement(new Note("Product polish", "Ideas",
                "Add an empty state, refine the note preview, and keep actions close to the content.", false));
    }

    private void loadSelectedNote() {
        Note note = noteList.getSelectedValue();
        updating = true;
        try {
            titleField.setText(note == null ? "" : note.title);
            bodyArea.setText(note == null ? "" : note.body);
            categoryCombo.setSelectedItem(note == null ? "Personal" : note.category);
            pinnedBox.setSelected(note != null && note.pinned);
        } finally {
            updating = false;
        }
        refreshSummary(note);
    }

    private void saveEditorState() {
        if (updating) {
            return;
        }
        Note note = noteList.getSelectedValue();
        if (note == null) {
            return;
        }
        note.title = titleField.getText().trim().isEmpty() ? "Untitled note" : titleField.getText().trim();
        note.body = bodyArea.getText();
        note.category = String.valueOf(categoryCombo.getSelectedItem());
        note.pinned = pinnedBox.isSelected();
        noteList.repaint();
        refreshSummary(note);
        statusLabel.setText("Saved changes");
    }

    private void refreshSummary(Note note) {
        int words = wordCount(note == null ? "" : note.body);
        wordCountLabel.setText(words + (words == 1 ? " word" : " words"));
        previewTitle.setText(note == null ? "Select a note" : note.title);
        previewMeta.setText(note == null ? "No note selected" : note.category + (note.pinned ? " - pinned" : ""));
    }

    private void applyFilter() {
        String query = searchField.getText().trim().toLowerCase(Locale.ROOT);
        for (int index = 0; index < notes.size(); index++) {
            Note note = notes.getElementAt(index);
            if (query.isEmpty() || note.matches(query)) {
                noteList.setSelectedIndex(index);
                statusLabel.setText(query.isEmpty() ? "Ready" : "Filtered notes");
                return;
            }
        }
        noteList.clearSelection();
        loadSelectedNote();
        statusLabel.setText("No matching notes");
    }

    private void addNote() {
        Note note = new Note("Untitled note " + nextNoteNumber++, "Personal", "", false);
        notes.addElement(note);
        noteList.setSelectedValue(note, true);
        titleField.requestFocusInWindow();
        statusLabel.setText("Created a new note");
    }

    private void duplicateNote() {
        Note selected = noteList.getSelectedValue();
        if (selected == null) {
            return;
        }
        Note copy = new Note(selected.title + " copy", selected.category, selected.body, selected.pinned);
        notes.addElement(copy);
        noteList.setSelectedValue(copy, true);
        statusLabel.setText("Duplicated note");
    }

    private void deleteNote() {
        int index = noteList.getSelectedIndex();
        if (index < 0) {
            return;
        }
        notes.remove(index);
        if (!notes.isEmpty()) {
            noteList.setSelectedIndex(Math.min(index, notes.size() - 1));
        } else {
            loadSelectedNote();
        }
        statusLabel.setText("Deleted note");
    }

    private JButton actionButton(String text, Runnable action) {
        JButton button = DemoStyles.button(text, "outline");
        button.addActionListener(event -> action.run());
        return button;
    }

    private JPanel labeled(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.add(DemoStyles.label(label), BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private static DocumentListener changeListener(Runnable runnable) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                runnable.run();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                runnable.run();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                runnable.run();
            }
        };
    }

    private static int wordCount(String text) {
        String trimmed = text.trim();
        return trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
    }

    private static String buttonText() {
        return Elowbe.theme().isDark() ? "Switch to Light" : "Switch to Dark";
    }

    private static final class Note {
        private String title;
        private String category;
        private String body;
        private boolean pinned;

        private Note(String title, String category, String body, boolean pinned) {
            this.title = title;
            this.category = category;
            this.body = body;
            this.pinned = pinned;
        }

        private boolean matches(String query) {
            return title.toLowerCase(Locale.ROOT).contains(query)
                    || category.toLowerCase(Locale.ROOT).contains(query)
                    || body.toLowerCase(Locale.ROOT).contains(query);
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private static final class NoteCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected,
                boolean focused) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focused);
            if (value instanceof Note) {
                Note note = (Note) value;
                label.setText((note.pinned ? "* " : "") + note.title + "  -  " + note.category);
            }
            label.setBorder(new EmptyBorder(9, 10, 9, 10));
            label.setFont(label.getFont().deriveFont(Font.PLAIN));
            return label;
        }
    }
}
