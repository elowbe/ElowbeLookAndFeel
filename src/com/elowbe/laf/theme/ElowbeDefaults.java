package com.elowbe.laf.theme;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.UIDefaults;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.DefaultEditorKit;

import com.elowbe.laf.util.ElowbeBorder;
import com.elowbe.laf.util.ElowbeIcons;

public final class ElowbeDefaults {
    public static final String THEME_KEY = "Elowbe.theme";
    public static final String PALETTE_KEY = "Elowbe.palette";
    public static final String STYLE_KEY = "Elowbe.style";
    public static final String BUTTON_VARIANT_KEY = "Elowbe.buttonVariant";
    public static final String PLACEHOLDER_KEY = "Elowbe.placeholderText";
    public static final String CARD_KEY = "Elowbe.card";
    public static final String ROUNDED_KEY = "Elowbe.rounded";

    public static final int RADIUS_SM = 16;
    public static final int RADIUS_MD = 22;
    public static final int RADIUS_LG = 32;
    public static final int RADIUS_PILL = 999;

    private static final String SELECTION_PAGE_UP_ACTION = "selection-page-up";
    private static final String SELECTION_PAGE_DOWN_ACTION = "selection-page-down";

    private ElowbeDefaults() {
    }

    public static void install(UIDefaults defaults, ElowbeTheme theme) {
        ElowbePalette palette = theme.palette();
        Font geistMono = geistMono();
        FontUIResource baseFont = new FontUIResource(geistMono.deriveFont(Font.PLAIN, 13f));
        FontUIResource smallFont = new FontUIResource(geistMono.deriveFont(Font.PLAIN, 12f));
        FontUIResource titleFont = new FontUIResource(geistMono.deriveFont(Font.PLAIN, 15f));
        FontUIResource monoFont = new FontUIResource(geistMono.deriveFont(Font.PLAIN, 12f));

        defaults.put(THEME_KEY, theme);
        defaults.put(PALETTE_KEY, palette);
        defaults.put("control", palette.background);
        defaults.put("info", palette.popover);
        defaults.put("nimbusBase", palette.primary);
        defaults.put("text", palette.foreground);
        defaults.put("textText", palette.foreground);
        defaults.put("window", palette.background);
        defaults.put("desktop", palette.background);
        defaults.put("activeCaption", palette.card);
        defaults.put("inactiveCaption", palette.muted);
        defaults.put("activeCaptionText", palette.foreground);
        defaults.put("inactiveCaptionText", palette.mutedForeground);

        putFont(defaults, baseFont, "Button", "CheckBox", "ColorChooser", "ComboBox", "FileChooser", "Label", "List",
                "Menu", "MenuItem", "OptionPane", "Panel", "PasswordField", "PopupMenu", "RadioButton", "ScrollPane",
                "Spinner", "TabbedPane", "Table", "TableHeader", "TextArea", "TextField", "TextPane", "ToolTip",
                "Tree");
        defaults.put("Elowbe.font.small", smallFont);
        defaults.put("Elowbe.font.title", titleFont);
        defaults.put("Elowbe.font.mono", monoFont);
        defaults.put("TableHeader.font", new FontUIResource(smallFont.deriveFont(Font.BOLD)));

        installColors(defaults, palette);
        installBorders(defaults, palette);
        installMetrics(defaults, palette);
    }

    public static ElowbePalette palette() {
        Object palette = UIManager.get(PALETTE_KEY);
        return palette instanceof ElowbePalette ? (ElowbePalette) palette : ElowbeTheme.LIGHT.palette();
    }

    public static ElowbeTheme theme() {
        Object theme = UIManager.get(THEME_KEY);
        return theme instanceof ElowbeTheme ? (ElowbeTheme) theme : ElowbeTheme.LIGHT;
    }

    private static void installColors(UIDefaults defaults, ElowbePalette palette) {
        putColors(defaults, palette.background, "Panel.background", "Viewport.background", "ScrollPane.background",
                "RootPane.background", "SplitPane.background", "TabbedPane.background", "ToolBar.background",
                "MenuBar.background", "PopupMenu.background", "TableHeader.background");
        putColors(defaults, palette.foreground, "Panel.foreground", "Label.foreground", "Button.foreground",
                "CheckBox.foreground", "RadioButton.foreground", "TabbedPane.foreground", "Menu.foreground",
                "MenuItem.foreground", "Table.foreground", "TableHeader.foreground", "Tree.foreground",
                "List.foreground");
        putColors(defaults, palette.card, "Button.background", "TextField.background", "FormattedTextField.background",
                "PasswordField.background", "TextArea.background", "TextPane.background", "ComboBox.background",
                "List.background", "Spinner.background", "Table.background", "Tree.background", "MenuItem.background");
        putColors(defaults, palette.foreground, "TextField.foreground", "FormattedTextField.foreground",
                "PasswordField.foreground", "TextArea.foreground", "TextPane.foreground", "ComboBox.foreground",
                "Spinner.foreground", "EditorPane.foreground");
        putColors(defaults, palette.mutedForeground, "Label.disabledForeground", "Button.disabledText",
                "CheckBox.disabledText", "RadioButton.disabledText", "TextField.inactiveForeground",
                "TextArea.inactiveForeground", "ComboBox.disabledForeground");
        putColors(defaults, palette.selection, "TextField.selectionBackground", "TextArea.selectionBackground",
                "TextPane.selectionBackground", "List.selectionBackground", "Table.selectionBackground",
                "Tree.selectionBackground", "ComboBox.selectionBackground");
        putColors(defaults, palette.selectionForeground, "TextField.selectionForeground", "TextArea.selectionForeground",
                "TextPane.selectionForeground", "List.selectionForeground", "Table.selectionForeground",
                "Tree.selectionForeground", "ComboBox.selectionForeground");
        putColors(defaults, palette.border, "Separator.foreground", "Separator.background", "Table.gridColor",
                "Tree.hash");
        defaults.put("ProgressBar.foreground", palette.primary);
        defaults.put("ProgressBar.background", palette.secondary);
        defaults.put("ProgressBar.selectionForeground", palette.primaryForeground);
        defaults.put("ProgressBar.selectionBackground", palette.foreground);
        defaults.put("ProgressBar.border", new EmptyBorder(0, 0, 0, 0));
        defaults.put("ToolTip.background", palette.popover);
        defaults.put("ToolTip.foreground", palette.popoverForeground);
        defaults.put("OptionPane.background", palette.background);
        defaults.put("OptionPane.foreground", palette.foreground);
        defaults.put("OptionPane.messageForeground", palette.foreground);
        defaults.put("OptionPane.questionDialog.titlePane.background", palette.background);
        defaults.put("OptionPane.errorDialog.titlePane.background", palette.background);
        defaults.put("OptionPane.warningDialog.titlePane.background", palette.background);
        defaults.put("FileChooser.background", palette.background);
        defaults.put("FileChooser.foreground", palette.foreground);
        defaults.put("FileChooser.listViewBackground", palette.background);
        defaults.put("FileChooser.listViewForeground", palette.foreground);
        defaults.put("FileChooser.listViewBorder", new EmptyBorder(6, 6, 6, 6));
        defaults.put("FileChooser.listViewWindowsStyle", Boolean.FALSE);
        defaults.put("FileChooser.readOnly", Boolean.FALSE);
        defaults.put("FileChooser.usesSingleFilePane", Boolean.TRUE);
        defaults.put("FileChooser.newFolderButtonText", "New Folder");
        defaults.put("FileChooser.newFolderButtonToolTipText", "Create a new folder");
        defaults.put("FileChooser.upFolderToolTipText", "Up one level");
        defaults.put("FileChooser.homeFolderToolTipText", "Home");
        defaults.put("FileChooser.listViewButtonToolTipText", "List");
        defaults.put("FileChooser.detailsViewButtonToolTipText", "Details");
        defaults.put("FileChooser.fileNameHeaderText", "Name");
        defaults.put("FileChooser.fileSizeHeaderText", "Size");
        defaults.put("FileChooser.fileTypeHeaderText", "Type");
        defaults.put("FileChooser.fileDateHeaderText", "Modified");
        defaults.put("FileChooser.fileAttrHeaderText", "Attributes");
        defaults.put("FileChooser.acceptAllFileFilterText", "All Files");
        defaults.put("FileView.directoryIcon", ElowbeIcons.folder(16));
        defaults.put("FileView.fileIcon", ElowbeIcons.file(16));
        defaults.put("FileView.computerIcon", ElowbeIcons.drive(16));
        defaults.put("FileView.hardDriveIcon", ElowbeIcons.drive(16));
        defaults.put("FileView.floppyDriveIcon", ElowbeIcons.drive(16));
        defaults.put("FileChooser.newFolderIcon", ElowbeIcons.plus(14));
        defaults.put("FileChooser.upFolderIcon", ElowbeIcons.arrowLeft(14));
        defaults.put("FileChooser.homeFolderIcon", ElowbeIcons.home(14));
        defaults.put("FileChooser.listViewIcon", ElowbeIcons.list(14));
        defaults.put("FileChooser.detailsViewIcon", ElowbeIcons.details(14));
        defaults.put("ColorChooser.background", palette.background);
        defaults.put("ColorChooser.foreground", palette.foreground);
    }

    private static void installBorders(UIDefaults defaults, ElowbePalette palette) {
        defaults.put("Button.border", new ElowbeBorder(palette.border, RADIUS_MD, new Insets(7, 13, 7, 13)));
        defaults.put("TextField.border", new ElowbeBorder(palette.input, RADIUS_MD, new Insets(7, 10, 7, 10)));
        defaults.put("PasswordField.border", new ElowbeBorder(palette.input, RADIUS_MD, new Insets(7, 10, 7, 10)));
        defaults.put("FormattedTextField.border", new ElowbeBorder(palette.input, RADIUS_MD, new Insets(7, 10, 7, 10)));
        defaults.put("TextArea.border", new ElowbeBorder(palette.input, RADIUS_MD, new Insets(8, 10, 8, 10)));
        defaults.put("ComboBox.border", new ElowbeBorder(palette.input, RADIUS_MD, new Insets(6, 10, 6, 34)));
        defaults.put("Spinner.border", new EmptyBorder(0, 0, 0, 0));
        defaults.put("ScrollPane.border", new ElowbeBorder(palette.border, RADIUS_MD, new Insets(0, 0, 0, 0)));
        defaults.put("ToolTip.border", new ElowbeBorder(palette.border, RADIUS_MD, new Insets(6, 8, 6, 8)));
        defaults.put("PopupMenu.border", new ElowbeBorder(palette.border, RADIUS_MD, new Insets(4, 4, 4, 4)));
        defaults.put("OptionPane.border", new EmptyBorder(18, 18, 18, 18));
        defaults.put("OptionPane.messageAreaBorder", new EmptyBorder(0, 0, 8, 0));
        defaults.put("OptionPane.buttonAreaBorder", new EmptyBorder(12, 0, 8, 0));
        defaults.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        defaults.put("TabbedPane.tabInsets", new Insets(6, 10, 6, 10));
        defaults.put("Menu.border", new EmptyBorder(6, 8, 6, 8));
        defaults.put("MenuItem.border", new EmptyBorder(6, 8, 6, 8));
    }

    private static void installMetrics(UIDefaults defaults, ElowbePalette palette) {
        defaults.put("Button.margin", new Insets(7, 13, 7, 13));
        defaults.put("ToggleButton.margin", new Insets(7, 13, 7, 13));
        defaults.put("ComboBox.padding", new Insets(6, 10, 6, 10));
        defaults.put("ScrollBar.width", 10);
        defaults.put("ProgressBar.cellSpacing", 0);
        defaults.put("ProgressBar.cellLength", 1);
        defaults.put("TextField.caretForeground", palette.foreground);
        defaults.put("TextArea.caretForeground", palette.foreground);
        installTextInputMaps(defaults);
    }

    private static void installTextInputMaps(UIDefaults defaults) {
        Object fieldInputMap = fieldInputMap();
        defaults.put("TextField.focusInputMap", fieldInputMap);
        defaults.put("PasswordField.focusInputMap", fieldInputMap);
        defaults.put("FormattedTextField.focusInputMap", fieldInputMap);

        Object multilineInputMap = multilineInputMap();
        defaults.put("TextArea.focusInputMap", multilineInputMap);
        defaults.put("EditorPane.focusInputMap", multilineInputMap);
        defaults.put("TextPane.focusInputMap", multilineInputMap);
    }

    private static UIDefaults.LazyInputMap fieldInputMap() {
        return new UIDefaults.LazyInputMap(new Object[] {
                shortcut("C"), DefaultEditorKit.copyAction,
                shortcut("V"), DefaultEditorKit.pasteAction,
                shortcut("X"), DefaultEditorKit.cutAction,
                "COPY", DefaultEditorKit.copyAction,
                "PASTE", DefaultEditorKit.pasteAction,
                "CUT", DefaultEditorKit.cutAction,
                "ctrl INSERT", DefaultEditorKit.copyAction,
                "shift INSERT", DefaultEditorKit.pasteAction,
                "shift DELETE", DefaultEditorKit.cutAction,
                "BACK_SPACE", DefaultEditorKit.deletePrevCharAction,
                "shift BACK_SPACE", DefaultEditorKit.deletePrevCharAction,
                "DELETE", DefaultEditorKit.deleteNextCharAction,
                "RIGHT", DefaultEditorKit.forwardAction,
                "KP_RIGHT", DefaultEditorKit.forwardAction,
                "LEFT", DefaultEditorKit.backwardAction,
                "KP_LEFT", DefaultEditorKit.backwardAction,
                "ctrl RIGHT", DefaultEditorKit.nextWordAction,
                "ctrl KP_RIGHT", DefaultEditorKit.nextWordAction,
                "ctrl LEFT", DefaultEditorKit.previousWordAction,
                "ctrl KP_LEFT", DefaultEditorKit.previousWordAction,
                "shift RIGHT", DefaultEditorKit.selectionForwardAction,
                "shift KP_RIGHT", DefaultEditorKit.selectionForwardAction,
                "shift LEFT", DefaultEditorKit.selectionBackwardAction,
                "shift KP_LEFT", DefaultEditorKit.selectionBackwardAction,
                "ctrl shift RIGHT", DefaultEditorKit.selectionNextWordAction,
                "ctrl shift KP_RIGHT", DefaultEditorKit.selectionNextWordAction,
                "ctrl shift LEFT", DefaultEditorKit.selectionPreviousWordAction,
                "ctrl shift KP_LEFT", DefaultEditorKit.selectionPreviousWordAction,
                "HOME", DefaultEditorKit.beginLineAction,
                "END", DefaultEditorKit.endLineAction,
                "shift HOME", DefaultEditorKit.selectionBeginLineAction,
                "shift END", DefaultEditorKit.selectionEndLineAction,
                "ctrl HOME", DefaultEditorKit.beginAction,
                "ctrl END", DefaultEditorKit.endAction,
                "ctrl shift HOME", DefaultEditorKit.selectionBeginAction,
                "ctrl shift END", DefaultEditorKit.selectionEndAction,
                shortcut("A"), DefaultEditorKit.selectAllAction,
                "ENTER", JTextField.notifyAction
        });
    }

    private static UIDefaults.LazyInputMap multilineInputMap() {
        return new UIDefaults.LazyInputMap(new Object[] {
                shortcut("C"), DefaultEditorKit.copyAction,
                shortcut("V"), DefaultEditorKit.pasteAction,
                shortcut("X"), DefaultEditorKit.cutAction,
                "COPY", DefaultEditorKit.copyAction,
                "PASTE", DefaultEditorKit.pasteAction,
                "CUT", DefaultEditorKit.cutAction,
                "ctrl INSERT", DefaultEditorKit.copyAction,
                "shift INSERT", DefaultEditorKit.pasteAction,
                "shift DELETE", DefaultEditorKit.cutAction,
                "BACK_SPACE", DefaultEditorKit.deletePrevCharAction,
                "shift BACK_SPACE", DefaultEditorKit.deletePrevCharAction,
                "DELETE", DefaultEditorKit.deleteNextCharAction,
                "RIGHT", DefaultEditorKit.forwardAction,
                "KP_RIGHT", DefaultEditorKit.forwardAction,
                "LEFT", DefaultEditorKit.backwardAction,
                "KP_LEFT", DefaultEditorKit.backwardAction,
                "UP", DefaultEditorKit.upAction,
                "KP_UP", DefaultEditorKit.upAction,
                "DOWN", DefaultEditorKit.downAction,
                "KP_DOWN", DefaultEditorKit.downAction,
                "PAGE_UP", DefaultEditorKit.pageUpAction,
                "PAGE_DOWN", DefaultEditorKit.pageDownAction,
                "ctrl RIGHT", DefaultEditorKit.nextWordAction,
                "ctrl KP_RIGHT", DefaultEditorKit.nextWordAction,
                "ctrl LEFT", DefaultEditorKit.previousWordAction,
                "ctrl KP_LEFT", DefaultEditorKit.previousWordAction,
                "shift RIGHT", DefaultEditorKit.selectionForwardAction,
                "shift KP_RIGHT", DefaultEditorKit.selectionForwardAction,
                "shift LEFT", DefaultEditorKit.selectionBackwardAction,
                "shift KP_LEFT", DefaultEditorKit.selectionBackwardAction,
                "shift UP", DefaultEditorKit.selectionUpAction,
                "shift KP_UP", DefaultEditorKit.selectionUpAction,
                "shift DOWN", DefaultEditorKit.selectionDownAction,
                "shift KP_DOWN", DefaultEditorKit.selectionDownAction,
                "shift PAGE_UP", SELECTION_PAGE_UP_ACTION,
                "shift PAGE_DOWN", SELECTION_PAGE_DOWN_ACTION,
                "ctrl shift RIGHT", DefaultEditorKit.selectionNextWordAction,
                "ctrl shift KP_RIGHT", DefaultEditorKit.selectionNextWordAction,
                "ctrl shift LEFT", DefaultEditorKit.selectionPreviousWordAction,
                "ctrl shift KP_LEFT", DefaultEditorKit.selectionPreviousWordAction,
                "HOME", DefaultEditorKit.beginLineAction,
                "END", DefaultEditorKit.endLineAction,
                "shift HOME", DefaultEditorKit.selectionBeginLineAction,
                "shift END", DefaultEditorKit.selectionEndLineAction,
                "ctrl HOME", DefaultEditorKit.beginAction,
                "ctrl END", DefaultEditorKit.endAction,
                "ctrl shift HOME", DefaultEditorKit.selectionBeginAction,
                "ctrl shift END", DefaultEditorKit.selectionEndAction,
                shortcut("A"), DefaultEditorKit.selectAllAction,
                "ENTER", DefaultEditorKit.insertBreakAction,
                "TAB", DefaultEditorKit.insertTabAction
        });
    }

    private static String shortcut(String key) {
        return (isMac() ? "meta " : "ctrl ") + key;
    }

    private static boolean isMac() {
        return System.getProperty("os.name", "").startsWith("Mac");
    }

    private static Font geistMono() {
        try (InputStream stream = ElowbeDefaults.class.getResourceAsStream("/GeistMono-Medium.ttf")) {
            if (stream != null) {
                Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
                return font;
            }
        } catch (FontFormatException | IOException ex) {
            // Fall through to the system fallback when the bundled font cannot be parsed.
        }

        String os = System.getProperty("os.name", "").toLowerCase();
        String fallback = os.contains("mac") ? "SF Pro Text" : os.contains("win") ? "Segoe UI" : "SansSerif";
        return new Font(fallback, Font.PLAIN, 13);
    }

    private static void putFont(UIDefaults defaults, FontUIResource font, String... prefixes) {
        for (String prefix : prefixes) {
            defaults.put(prefix + ".font", font);
        }
    }

    private static void putColors(UIDefaults defaults, ColorUIResource color, String... keys) {
        for (String key : keys) {
            defaults.put(key, color);
        }
    }
}
