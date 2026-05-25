package com.elowbe.laf;

import java.awt.Window;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbeTheme;

public final class Elowbe {
    private Elowbe() {
    }

    public static void install() {
        install(ElowbeTheme.LIGHT);
    }

    public static void install(ElowbeTheme theme) {
        try {
            UIManager.setLookAndFeel(new ElowbeLookAndFeel(theme));
        } catch (UnsupportedLookAndFeelException ex) {
            throw new IllegalStateException("Unable to install Elowbe LookAndFeel", ex);
        }
    }

    public static ElowbeTheme theme() {
        return ElowbeDefaults.theme();
    }

    public static void setTheme(ElowbeTheme theme) {
        install(theme);
        refreshOpenWindows();
    }

    public static void toggleTheme() {
        setTheme(theme().opposite());
    }

    public static void refresh(JComponent component) {
        SwingUtilities.updateComponentTreeUI(component);
        component.revalidate();
        component.repaint();
    }

    public static void refreshOpenWindows() {
        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
            window.invalidate();
            window.validate();
            window.repaint();
        }
    }
}
