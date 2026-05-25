package com.elowbe.laf;

import javax.swing.UIDefaults;
import javax.swing.plaf.basic.BasicLookAndFeel;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbeTheme;

public class ElowbeLookAndFeel extends BasicLookAndFeel {
    private static final long serialVersionUID = 1L;

    private final ElowbeTheme theme;

    public ElowbeLookAndFeel() {
        this(ElowbeTheme.LIGHT);
    }

    public ElowbeLookAndFeel(ElowbeTheme theme) {
        this.theme = theme == null ? ElowbeTheme.LIGHT : theme;
    }

    @Override
    public String getName() {
        return theme.displayName();
    }

    @Override
    public String getID() {
        return "Elowbe";
    }

    @Override
    public String getDescription() {
        return "A modern web-inspired Swing LookAndFeel with light and dark themes.";
    }

    @Override
    public boolean isNativeLookAndFeel() {
        return false;
    }

    @Override
    public boolean isSupportedLookAndFeel() {
        return true;
    }

    public ElowbeTheme getTheme() {
        return theme;
    }

    @Override
    protected void initClassDefaults(UIDefaults table) {
        super.initClassDefaults(table);
        final String packageName = "com.elowbe.laf.ui.";
        Object[] uiDefaults = {
                "ButtonUI", packageName + "ElowbeButtonUI",
                "ToggleButtonUI", packageName + "ElowbeButtonUI",
                "TextFieldUI", packageName + "ElowbeTextFieldUI",
                "PasswordFieldUI", packageName + "ElowbeTextFieldUI",
                "FormattedTextFieldUI", packageName + "ElowbeTextFieldUI",
                "TextAreaUI", packageName + "ElowbeTextAreaUI",
                "ComboBoxUI", packageName + "ElowbeComboBoxUI",
                "CheckBoxUI", packageName + "ElowbeCheckBoxUI",
                "RadioButtonUI", packageName + "ElowbeRadioButtonUI",
                "SliderUI", packageName + "ElowbeSliderUI",
                "ScrollBarUI", packageName + "ElowbeScrollBarUI",
                "SpinnerUI", packageName + "ElowbeSpinnerUI",
                "TabbedPaneUI", packageName + "ElowbeTabbedPaneUI",
                "TableHeaderUI", packageName + "ElowbeTableHeaderUI",
                "ProgressBarUI", packageName + "ElowbeProgressBarUI",
                "PanelUI", packageName + "ElowbePanelUI",
                "FileChooserUI", packageName + "ElowbeFileChooserUI",
                "ColorChooserUI", packageName + "ElowbeColorChooserUI"
        };
        table.putDefaults(uiDefaults);
    }

    @Override
    protected void initComponentDefaults(UIDefaults table) {
        super.initComponentDefaults(table);
        ElowbeDefaults.install(table, theme);
    }
}
