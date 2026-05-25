package com.elowbe.laf.theme;

public enum ElowbeTheme {
    LIGHT("Elowbe Light", ElowbePalette.light(), false),
    DARK("Elowbe Dark", ElowbePalette.dark(), true);

    private final String displayName;
    private final ElowbePalette palette;
    private final boolean dark;

    ElowbeTheme(String displayName, ElowbePalette palette, boolean dark) {
        this.displayName = displayName;
        this.palette = palette;
        this.dark = dark;
    }

    public String displayName() {
        return displayName;
    }

    public ElowbePalette palette() {
        return palette;
    }

    public boolean isDark() {
        return dark;
    }

    public ElowbeTheme opposite() {
        return dark ? LIGHT : DARK;
    }
}
