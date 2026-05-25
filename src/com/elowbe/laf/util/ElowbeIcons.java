package com.elowbe.laf.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

import jiconfont.IconCode;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

public final class ElowbeIcons {
    private static boolean registered;

    private ElowbeIcons() {
    }

    public static Icon search(int size) {
        return icon(FontAwesome.SEARCH, size);
    }

    public static Icon chevronDown(int size) {
        return icon(FontAwesome.CHEVRON_DOWN, size);
    }

    public static Icon check(int size) {
        return icon(FontAwesome.CHECK, size);
    }

    public static Icon arrowLeft(int size) {
        return icon(FontAwesome.CHEVRON_LEFT, size);
    }

    public static Icon arrowRight(int size) {
        return icon(FontAwesome.CHEVRON_RIGHT, size);
    }

    public static Icon plus(int size) {
        return icon(FontAwesome.PLUS, size);
    }

    public static Icon minus(int size) {
        return icon(FontAwesome.MINUS, size);
    }

    public static Icon dot(int size) {
        return icon(FontAwesome.CIRCLE, size);
    }

    public static Icon spinner(int size) {
        return icon(FontAwesome.REFRESH, size);
    }

    public static Icon star(int size) {
        return icon(FontAwesome.STAR, size);
    }

    public static Icon file(int size) {
        return new FileSystemIcon(size, "file");
    }

    public static Icon folder(int size) {
        return new FileSystemIcon(size, "folder");
    }

    public static Icon drive(int size) {
        return new FileSystemIcon(size, "drive");
    }

    public static Icon home(int size) {
        return icon(FontAwesome.HOME, size);
    }

    public static Icon list(int size) {
        return new FileSystemIcon(size, "list");
    }

    public static Icon details(int size) {
        return new FileSystemIcon(size, "details");
    }

    private static Icon icon(IconCode code, int size) {
        register();
        return new FontAwesomeIcon(code, size);
    }

    private static synchronized void register() {
        if (!registered) {
            IconFontSwing.register(FontAwesome.getIconFont());
            registered = true;
        }
    }

    private static final class FontAwesomeIcon implements Icon {
        private final IconCode code;
        private final int size;

        private FontAwesomeIcon(IconCode code, int size) {
            this.code = code;
            this.size = size;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Color color = graphics.getColor();
            if (color == null && component != null) {
                color = component.getForeground();
            }
            Icon icon = IconFontSwing.buildIcon(code, size, color == null ? Color.GRAY : color);
            icon.paintIcon(component, graphics, x, y);
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }

    private static final class FileSystemIcon implements Icon {
        private final int size;
        private final String type;

        private FileSystemIcon(int size, String type) {
            this.size = size;
            this.type = type;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2 = PaintUtils.prepare(graphics);
            try {
                Color foreground = component == null ? PaintUtils.palette().foreground : component.getForeground();
                Color fill = PaintUtils.palette().background;
                Color accent = PaintUtils.palette().mutedForeground;
                g2.translate(x, y);
                g2.setColor(fill);
                if ("folder".equals(type)) {
                    g2.fillRoundRect(1, 5, size - 2, size - 7, 4, 4);
                    g2.fillRoundRect(2, 3, Math.max(6, size / 2), 5, 3, 3);
                    g2.setColor(foreground);
                    g2.drawRoundRect(1, 5, size - 3, size - 8, 4, 4);
                    g2.drawRoundRect(2, 3, Math.max(6, size / 2), 5, 3, 3);
                } else if ("drive".equals(type)) {
                    g2.fillRoundRect(2, 4, size - 4, size - 8, 4, 4);
                    g2.setColor(foreground);
                    g2.drawRoundRect(2, 4, size - 5, size - 9, 4, 4);
                    g2.setColor(accent);
                    g2.fillOval(size - 6, size - 7, 3, 3);
                } else if ("list".equals(type)) {
                    drawList(g2, foreground, false);
                } else if ("details".equals(type)) {
                    drawList(g2, foreground, true);
                } else {
                    g2.fillRoundRect(3, 2, size - 6, size - 4, 3, 3);
                    g2.setColor(foreground);
                    g2.drawRoundRect(3, 2, size - 7, size - 5, 3, 3);
                    g2.setColor(accent);
                    g2.drawLine(size - 7, 5, size - 4, 8);
                    g2.drawLine(size - 4, 8, size - 7, 8);
                }
            } finally {
                g2.dispose();
            }
        }

        private void drawList(Graphics2D g2, Color color, boolean columns) {
            g2.setColor(color);
            for (int row = 0; row < 3; row++) {
                int y = 4 + row * 5;
                g2.fillRoundRect(3, y, 3, 3, 2, 2);
                g2.drawLine(8, y + 1, size - 3, y + 1);
                if (columns) {
                    g2.drawLine(size / 2, y - 1, size / 2, y + 3);
                }
            }
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
}
