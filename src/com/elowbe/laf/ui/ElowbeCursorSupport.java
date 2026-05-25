package com.elowbe.laf.ui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;

final class ElowbeCursorSupport {
    private static final Cursor HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private static final String PREVIOUS_CURSOR_KEY = "Elowbe.previousCursor";

    private ElowbeCursorSupport() {
    }

    static MouseListener handCursorOnHover() {
        return new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                Component component = event.getComponent();
                if (!component.isEnabled()) {
                    return;
                }
                setHandCursor(component);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                restoreCursor(event.getComponent());
            }
        };
    }

    static void setHandCursor(Component component) {
        rememberCursor(component);
        component.setCursor(HAND_CURSOR);
    }

    static void restoreCursor(Component component) {
        if (!(component instanceof JComponent)) {
            return;
        }
        JComponent jComponent = (JComponent) component;
        Object previous = jComponent.getClientProperty(PREVIOUS_CURSOR_KEY);
        if (previous instanceof Cursor) {
            component.setCursor((Cursor) previous);
        }
        jComponent.putClientProperty(PREVIOUS_CURSOR_KEY, null);
    }

    private static void rememberCursor(Component component) {
        if (!(component instanceof JComponent)) {
            return;
        }
        JComponent jComponent = (JComponent) component;
        if (jComponent.getClientProperty(PREVIOUS_CURSOR_KEY) == null) {
            jComponent.putClientProperty(PREVIOUS_CURSOR_KEY, component.getCursor());
        }
    }
}
