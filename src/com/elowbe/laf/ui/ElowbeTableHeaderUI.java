package com.elowbe.laf.ui;

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTableHeaderUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbePalette;
import com.elowbe.laf.util.ElowbeBorder;
import com.elowbe.laf.util.PaintUtils;

public class ElowbeTableHeaderUI extends BasicTableHeaderUI {
    public static ComponentUI createUI(javax.swing.JComponent component) {
        return new ElowbeTableHeaderUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        ElowbePalette palette = PaintUtils.palette();
        header.setOpaque(false);
        header.setBackground(palette.card);
        header.setForeground(palette.foreground);
        header.setFont(UIManager.getFont("TableHeader.font"));
        header.setBorder(new EmptyBorder(0, 0, 6, 0));
        header.setDefaultRenderer(new HeaderRenderer(header.getDefaultRenderer()));
    }

    @Override
    public void paint(Graphics graphics, javax.swing.JComponent component) {
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            g2.setColor(PaintUtils.palette().background);
            g2.fillRect(0, 0, component.getWidth(), component.getHeight());
        } finally {
            g2.dispose();
        }
        super.paint(graphics, component);
    }

    private static final class HeaderRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        private final TableCellRenderer fallback;
        private final Border border = new ElowbeBorder(PaintUtils.palette().border, ElowbeDefaults.RADIUS_SM,
                new Insets(7, 10, 7, 10));

        private HeaderRenderer(TableCellRenderer fallback) {
            this.fallback = fallback;
            setHorizontalAlignment(SwingConstants.LEFT);
            setOpaque(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            Component rendered = fallback == null ? null
                    : fallback.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            JLabel label = rendered instanceof JLabel ? (JLabel) rendered : this;
            ElowbePalette palette = PaintUtils.palette();
            label.setText(value == null ? "" : value.toString());
            label.setOpaque(false);
            label.setForeground(palette.foreground);
            label.setBackground(palette.card);
            Font font = UIManager.getFont("TableHeader.font");
            label.setFont(font == null ? table.getFont().deriveFont(Font.BOLD) : font);
            label.setBorder(border);
            label.setHorizontalAlignment(SwingConstants.LEFT);
            return label;
        }
    }
}
