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
}
