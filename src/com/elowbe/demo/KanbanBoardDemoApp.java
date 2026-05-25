package com.elowbe.demo;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.elowbe.laf.Elowbe;
import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbeTheme;
import com.elowbe.laf.util.ElowbeIcons;
import com.elowbe.laf.util.PaintUtils;

public final class KanbanBoardDemoApp {
    private static final int CARD_WIDTH = 220;
    private static final int COLUMN_WIDTH = CARD_WIDTH + 38;

    private static final DataFlavor TASK_FLAVOR = new DataFlavor(Task.class, "Kanban Task");

    private final List<Column> columns = new ArrayList<>();
    private final JLabel statusLabel = DemoStyles.muted("Drag cards between columns or use the arrow buttons.");
    private KanbanBoardDemoApp() {
        seedBoard();
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
        KanbanBoardDemoApp app = new KanbanBoardDemoApp();
        JFrame frame = new JFrame("Elowbe Kanban Demo");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1140, 720));
        frame.setContentPane(app.content(frame));
        frame.pack();
        frame.setResizable(false);
        return frame;
    }

    private JPanel content(JFrame frame) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIManager.getColor("Panel.background"));
        root.add(header(frame), BorderLayout.NORTH);

        JPanel board = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        board.setOpaque(false);
        board.setBorder(new EmptyBorder(16, 24, 8, 24));
        for (Column column : columns) {
            board.add(column.panel());
        }

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0, 24, 24, 24));
        footer.add(statusLabel, BorderLayout.WEST);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(board, BorderLayout.CENTER);
        south.add(footer, BorderLayout.SOUTH);
        root.add(south, BorderLayout.CENTER);
        return root;
    }

    private JPanel header(JFrame frame) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(16, 24, 8, 24));
        header.setOpaque(false);

        JPanel copy = new JPanel(new BorderLayout(0, 3));
        copy.setOpaque(false);
        JLabel title = new JLabel("Kanban Board");
        title.setFont(UIManager.getFont("Elowbe.font.title").deriveFont(18f));
        JLabel subtitle = DemoStyles.muted("Drag cards across columns, add tasks, and preview list styling in a workflow layout.");
        copy.add(title, BorderLayout.NORTH);
        copy.add(subtitle, BorderLayout.SOUTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton addTask = DemoStyles.button("Add Task", "primary");
        addTask.putClientProperty(ElowbeDefaults.ROUNDED_KEY, Boolean.TRUE);
        addTask.addActionListener(event -> addTask());

        JButton toggle = DemoStyles.button(buttonText(), "outline");
        toggle.putClientProperty(ElowbeDefaults.ROUNDED_KEY, Boolean.TRUE);
        toggle.addActionListener(event -> {
            Elowbe.toggleTheme();
            DemoStyles.applyFontRoles(frame);
            for (Column column : columns) {
                column.list.repaint();
            }
            frame.repaint();
        });

        actions.add(addTask);
        actions.add(toggle);

        header.add(copy, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private void seedBoard() {
        Column backlog = addColumn("Backlog", "Ideas waiting to be picked up.");
        Column progress = addColumn("In Progress", "Work actively underway.");
        Column review = addColumn("Review", "Ready for feedback or QA.");
        Column done = addColumn("Done", "Completed and shipped.");

        backlog.add(new Task("Design board layout", "Sketch columns, card spacing, and drag affordances.",
                Priority.HIGH, "Alex"));
        backlog.add(new Task("Write release notes", "Summarize UI polish and new demo apps.", Priority.MEDIUM,
                "Jamie"));
        progress.add(new Task("Theme token audit", "Verify light and dark palettes across cards and lists.",
                Priority.HIGH, "Riley"));
        progress.add(new Task("Combo box polish", "Align popup padding with text field metrics.", Priority.LOW,
                "Sam"));
        review.add(new Task("Dialog spacing pass", "Check option pane margins on macOS and Windows.",
                Priority.MEDIUM, "Alex"));
        done.add(new Task("Spinner arrows", "Replace default stepper visuals with Elowbe icons.", Priority.LOW,
                "Jamie"));
    }

    private Column addColumn(String title, String subtitle) {
        Column column = new Column(title, subtitle);
        columns.add(column);
        return column;
    }

    private void addTask() {
        String title = JOptionPane.showInputDialog(null, "Task title", "New Task", JOptionPane.PLAIN_MESSAGE);
        if (title == null) {
            return;
        }
        String trimmed = title.trim();
        if (trimmed.isEmpty()) {
            statusLabel.setText("Task title cannot be empty.");
            return;
        }
        columns.get(0).add(new Task(trimmed, "New task added from the board header.", Priority.MEDIUM, "You"));
        statusLabel.setText("Added \"" + trimmed + "\" to Backlog.");
    }

    private void moveTask(Column from, int index, Column to, int targetIndex) {
        if (index < 0 || index >= from.model.size()) {
            return;
        }
        Task task = from.model.getElementAt(index);
        from.model.remove(index);
        int insertAt = Math.max(0, Math.min(targetIndex, to.model.size()));
        to.model.add(insertAt, task);
        to.list.setSelectedIndex(insertAt);
        to.list.ensureIndexIsVisible(insertAt);
        statusLabel.setText("Moved \"" + task.title + "\" to " + to.title + ".");
    }

    private void shiftTask(Column column, int index, int direction) {
        int targetColumn = columns.indexOf(column) + direction;
        if (targetColumn < 0 || targetColumn >= columns.size()) {
            statusLabel.setText("Cannot move task further " + (direction < 0 ? "left" : "right") + ".");
            return;
        }
        moveTask(column, index, columns.get(targetColumn), columns.get(targetColumn).model.size());
    }

    private void deleteTask(Column column, int index) {
        if (index < 0 || index >= column.model.size()) {
            return;
        }
        Task task = column.model.remove(index);
        statusLabel.setText("Removed \"" + task.title + "\".");
    }

    private Column columnForList(JList<Task> list) {
        for (Column column : columns) {
            if (column.list == list) {
                return column;
            }
        }
        return null;
    }

    private static String buttonText() {
        return Elowbe.theme().isDark() ? "Switch to Light" : "Switch to Dark";
    }

    private enum Priority {
        HIGH("High"),
        MEDIUM("Medium"),
        LOW("Low");

        private final String label;

        Priority(String label) {
            this.label = label;
        }
    }

    private static final class Task {
        private final String title;
        private final String description;
        private final Priority priority;
        private final String assignee;

        private Task(String title, String description, Priority priority, String assignee) {
            this.title = title;
            this.description = description;
            this.priority = priority;
            this.assignee = assignee;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private final class Column {
        private final String title;
        private final DefaultListModel<Task> model = new DefaultListModel<>();
        private final JList<Task> list = new JList<>(model);
        private final JLabel countLabel = DemoStyles.muted("");

        private Column(String title, String subtitle) {
            this.title = title;
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setLayoutOrientation(JList.VERTICAL);
            list.setFixedCellHeight(-1);
            list.setCellRenderer(new TaskCardRenderer());
            list.setDragEnabled(true);
            list.setDropMode(javax.swing.DropMode.INSERT);
            list.setTransferHandler(new TaskTransferHandler());
            list.setBackground(PaintUtils.palette().background);
            model.addListDataListener(new ListDataListener() {
                @Override
                public void intervalAdded(ListDataEvent event) {
                    refreshCount();
                }

                @Override
                public void intervalRemoved(ListDataEvent event) {
                    refreshCount();
                }

                @Override
                public void contentsChanged(ListDataEvent event) {
                    refreshCount();
                }
            });
        }

        private void refreshCount() {
            int count = model.size();
            countLabel.setText(count + (count == 1 ? " card" : " cards"));
        }

        private void add(Task task) {
            model.addElement(task);
        }

        private JPanel panel() {
            JPanel column = DemoStyles.card();
            column.setLayout(new BorderLayout(0, 12));
            Dimension columnSize = new Dimension(COLUMN_WIDTH, 560);
            column.setPreferredSize(columnSize);
            column.setMaximumSize(columnSize);

            JPanel heading = new JPanel(new BorderLayout(0, 4));
            heading.setOpaque(false);
            heading.add(DemoStyles.title(title), BorderLayout.NORTH);
            heading.add(countLabel, BorderLayout.SOUTH);
            refreshCount();
            column.add(heading, BorderLayout.NORTH);

            list.setOpaque(false);
            JScrollPane scrollPane = new JScrollPane(list);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.setPreferredSize(new Dimension(COLUMN_WIDTH, 480));
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            column.add(scrollPane, BorderLayout.CENTER);

            JPanel actions = DemoStyles.flow(FlowLayout.LEFT, 8);
            JButton left = DemoStyles.iconButton("outline", ElowbeIcons.arrowLeft(12));
            left.putClientProperty(ElowbeDefaults.ROUNDED_KEY, Boolean.TRUE);
            left.setToolTipText("Move selected card left");
            left.addActionListener(event -> shiftTask(this, list.getSelectedIndex(), -1));

            JButton right = DemoStyles.iconButton("outline", ElowbeIcons.arrowRight(12));
            right.putClientProperty(ElowbeDefaults.ROUNDED_KEY, Boolean.TRUE);
            right.setToolTipText("Move selected card right");
            right.addActionListener(event -> shiftTask(this, list.getSelectedIndex(), 1));

            JButton remove = DemoStyles.iconButton("outline", ElowbeIcons.minus(12));
            remove.putClientProperty(ElowbeDefaults.ROUNDED_KEY, Boolean.TRUE);
            remove.setToolTipText("Remove selected card");
            remove.addActionListener(event -> deleteTask(this, list.getSelectedIndex()));

            actions.add(left);
            actions.add(right);
            actions.add(remove);
            column.add(actions, BorderLayout.SOUTH);
            return column;
        }
    }

    private final class TaskTransferHandler extends TransferHandler {
        private static final long serialVersionUID = 1L;

        private int sourceIndex = -1;
        private Column sourceColumn;

        @Override
        public int getSourceActions(JComponent component) {
            return MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent component) {
            JList<Task> list = (JList<Task>) component;
            sourceIndex = list.getSelectedIndex();
            sourceColumn = columnForList(list);
            Task task = list.getSelectedValue();
            return task == null ? null : new TaskTransferable(task);
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            if (action == MOVE && sourceColumn != null && sourceIndex >= 0) {
                sourceColumn.model.remove(sourceIndex);
                sourceIndex = -1;
                sourceColumn = null;
            }
        }

        @Override
        public boolean canImport(TransferSupport support) {
            if (!support.isDrop() || !support.isDataFlavorSupported(TASK_FLAVOR)) {
                return false;
            }
            JList.DropLocation location = (JList.DropLocation) support.getDropLocation();
            return location.getIndex() >= 0;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            try {
                Task task = (Task) support.getTransferable().getTransferData(TASK_FLAVOR);
                JList<Task> targetList = (JList<Task>) support.getComponent();
                Column targetColumn = columnForList(targetList);
                if (targetColumn == null) {
                    return false;
                }
                int index = ((JList.DropLocation) support.getDropLocation()).getIndex();
                targetColumn.model.add(index, task);
                targetList.setSelectedIndex(index);
                targetList.ensureIndexIsVisible(index);
                statusLabel.setText("Moved \"" + task.title + "\" to " + targetColumn.title + ".");
                return true;
            } catch (UnsupportedFlavorException | IOException exception) {
                return false;
            }
        }
    }

    private static final class TaskTransferable implements Transferable {
        private final Task task;

        private TaskTransferable(Task task) {
            this.task = task;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { TASK_FLAVOR };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return TASK_FLAVOR.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return task;
        }
    }

    private static final class TaskCardRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected,
                boolean focused) {
            JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            cell.setOpaque(false);
            cell.setBorder(new EmptyBorder(0, 0, 8, 0));

            if (!(value instanceof Task)) {
                return cell;
            }
            Task task = (Task) value;
            JLabel label = new JLabel();
            label.setOpaque(true);
            label.setBackground(selected ? PaintUtils.palette().accent : PaintUtils.palette().card);
            label.setForeground(PaintUtils.palette().foreground);
            label.setFont(label.getFont().deriveFont(Font.PLAIN, 13f));
            label.setVerticalAlignment(javax.swing.SwingConstants.TOP);

            String description = task.description.length() > 56
                    ? task.description.substring(0, 53) + "..."
                    : task.description;
            label.setText("<html><body style='width:" + (CARD_WIDTH - 28) + "px'>"
                    + "<b>" + escape(task.title) + "</b><br/>"
                    + "<span style='color:#888888;font-size:11px;'>" + escape(description) + "</span><br/>"
                    + "<span style='color:#888888;font-size:11px;'>" + escape(task.assignee) + " · "
                    + escape(task.priority.label) + "</span></body></html>");

            if (selected) {
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PaintUtils.palette().primary, 1),
                        new EmptyBorder(11, 13, 11, 13)));
            } else {
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PaintUtils.palette().border, 1),
                        new EmptyBorder(11, 13, 11, 13)));
            }

            label.setPreferredSize(new Dimension(CARD_WIDTH, label.getPreferredSize().height));
            cell.add(label);
            return cell;
        }

        private String escape(String text) {
            return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }
}
