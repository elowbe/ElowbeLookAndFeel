package com.elowbe.laf.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalFileChooserUI;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbePalette;
import com.elowbe.laf.util.ElowbeBorder;
import com.elowbe.laf.util.PaintUtils;

public class ElowbeFileChooserUI extends MetalFileChooserUI {
    private static final Dimension DEFAULT_SIZE = new Dimension(880, 620);
    private static final int NAME_COLUMN_WIDTH = 560;
    private static final int SIZE_COLUMN_WIDTH = 82;
    private static final int TYPE_COLUMN_WIDTH = 110;
    private static final int MODIFIED_COLUMN_WIDTH = 132;
    private static final int ATTRIBUTES_COLUMN_WIDTH = 96;

    public static ComponentUI createUI(JComponent component) {
        return new ElowbeFileChooserUI((JFileChooser) component);
    }

    public ElowbeFileChooserUI(JFileChooser fileChooser) {
        super(fileChooser);
    }

    @Override
    public void installComponents(JFileChooser fileChooser) {
        super.installComponents(fileChooser);
        styleFileChooser(fileChooser);
    }

    @Override
    protected void installDefaults(JFileChooser fileChooser) {
        super.installDefaults(fileChooser);
        ElowbePalette palette = PaintUtils.palette();
        fileChooser.setBackground(palette.background);
        fileChooser.setForeground(palette.foreground);
        fileChooser.setBorder(new EmptyBorder(18, 18, 18, 18));
        fileChooser.setPreferredSize(DEFAULT_SIZE);
        fileChooser.setMinimumSize(new Dimension(720, 480));
    }

    private void styleFileChooser(JFileChooser fileChooser) {
        style(fileChooser);
        SwingUtilities.invokeLater(() -> {
            showDetailsView(fileChooser);
            style(fileChooser);
        });
    }

    private void showDetailsView(JFileChooser fileChooser) {
        String detailsToolTip = UIManager.getString("FileChooser.detailsViewButtonToolTipText");
        AbstractButton detailsButton = findButtonByToolTip(fileChooser, detailsToolTip);
        if (detailsButton != null && !detailsButton.isSelected()) {
            detailsButton.doClick(0);
        }
    }

    private AbstractButton findButtonByToolTip(Component component, String toolTip) {
        if (toolTip != null && component instanceof AbstractButton) {
            AbstractButton button = (AbstractButton) component;
            if (toolTip.equals(button.getToolTipText())) {
                return button;
            }
        }

        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                AbstractButton button = findButtonByToolTip(child, toolTip);
                if (button != null) {
                    return button;
                }
            }
        }
        return null;
    }

    private void style(Component component) {
        ElowbePalette palette = PaintUtils.palette();
        if (component instanceof JComponent) {
            JComponent jComponent = (JComponent) component;
            jComponent.setFont(UIManager.getFont("FileChooser.font"));
        }

        if (component instanceof JPanel) {
            component.setBackground(palette.background);
        } else if (component instanceof JLabel) {
            component.setForeground(palette.foreground);
            component.setBackground(palette.background);
        } else if (component instanceof JTextField) {
            styleTextField((JTextField) component, palette);
        } else if (component instanceof JComboBox) {
            styleComboBox((JComboBox<?>) component, palette);
        } else if (component instanceof JList) {
            styleList((JList<?>) component, palette);
        } else if (component instanceof JTable) {
            styleTable((JTable) component, palette);
        } else if (component instanceof JTableHeader) {
            styleTableHeader((JTableHeader) component, palette);
        } else if (component instanceof JToolBar) {
            styleToolBar((JToolBar) component, palette);
        } else if (component instanceof AbstractButton) {
            styleButton((AbstractButton) component);
        }

        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                style(child);
            }
        }
    }

    private void styleTextField(JTextField field, ElowbePalette palette) {
        field.setBackground(palette.card);
        field.setForeground(palette.foreground);
        field.setCaretColor(palette.foreground);
        field.setSelectionColor(palette.selection);
        field.setSelectedTextColor(palette.selectionForeground);
        field.setBorder(new ElowbeBorder(palette.input, ElowbeDefaults.RADIUS_MD, new Insets(7, 10, 7, 10)));
    }

    private void styleComboBox(JComboBox<?> comboBox, ElowbePalette palette) {
        comboBox.setBackground(palette.card);
        comboBox.setForeground(palette.foreground);
        comboBox.setBorder(new ElowbeBorder(palette.input, ElowbeDefaults.RADIUS_MD, new Insets(6, 10, 6, 34)));
        Dimension preferred = comboBox.getPreferredSize();
        Dimension size = new Dimension(Math.max(preferred.width, 320), Math.max(preferred.height, 44));
        comboBox.setMinimumSize(size);
        comboBox.setPreferredSize(size);
    }

    private void styleList(JList<?> list, ElowbePalette palette) {
        list.setBackground(palette.background);
        list.setForeground(palette.foreground);
        list.setSelectionBackground(palette.selection);
        list.setSelectionForeground(palette.selectionForeground);
        list.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
    }

    private void styleTable(JTable table, ElowbePalette palette) {
        table.setBackground(palette.background);
        table.setForeground(palette.foreground);
        table.setSelectionBackground(palette.selection);
        table.setSelectionForeground(palette.selectionForeground);
        table.setGridColor(new Color(0, 0, 0, 0));
        table.setShowGrid(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        table.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        configureDetailsColumns(table);
        if (table.getTableHeader() != null) {
            styleTableHeader(table.getTableHeader(), palette);
        }
    }

    private void configureDetailsColumns(JTable table) {
        TableColumnModel columns = table.getColumnModel();
        for (int i = 0; i < columns.getColumnCount(); i++) {
            TableColumn column = columns.getColumn(i);
            String header = String.valueOf(column.getHeaderValue());
            int width = detailColumnWidth(header);
            column.setMinWidth(width == NAME_COLUMN_WIDTH ? 240 : width - 16);
            column.setPreferredWidth(width);
            column.setMaxWidth(width == NAME_COLUMN_WIDTH ? Integer.MAX_VALUE : width + 42);
        }
    }

    private int detailColumnWidth(String header) {
        if (matchesHeader(header, "FileChooser.fileSizeHeaderText")) {
            return SIZE_COLUMN_WIDTH;
        }
        if (matchesHeader(header, "FileChooser.fileTypeHeaderText")) {
            return TYPE_COLUMN_WIDTH;
        }
        if (matchesHeader(header, "FileChooser.fileDateHeaderText")) {
            return MODIFIED_COLUMN_WIDTH;
        }
        if (matchesHeader(header, "FileChooser.fileAttrHeaderText")) {
            return ATTRIBUTES_COLUMN_WIDTH;
        }
        return NAME_COLUMN_WIDTH;
    }

    private boolean matchesHeader(String header, String defaultsKey) {
        String expected = UIManager.getString(defaultsKey);
        return expected != null && expected.equals(header);
    }

    private void styleTableHeader(JTableHeader header, ElowbePalette palette) {
        header.setOpaque(true);
        header.setBackground(palette.card);
        header.setForeground(palette.foreground);
        header.setFont(UIManager.getFont("FileChooser.font"));
        header.setBorder(new ElowbeBorder(palette.border, ElowbeDefaults.RADIUS_SM, new Insets(4, 6, 4, 6)));
    }

    private void styleToolBar(JToolBar toolBar, ElowbePalette palette) {
        toolBar.setOpaque(false);
        toolBar.setFloatable(false);
        toolBar.setBackground(palette.background);
        toolBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
    }

    private void styleButton(AbstractButton button) {
        String text = button.getText();
        boolean textButton = text != null && !text.isBlank();
        button.putClientProperty(ElowbeDefaults.BUTTON_VARIANT_KEY, textButton ? "outline" : "ghost");
        button.setMargin(textButton ? new Insets(7, 13, 7, 13) : new Insets(7, 8, 7, 8));
        button.setFocusPainted(false);
    }
}
