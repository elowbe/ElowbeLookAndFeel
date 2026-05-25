package com.elowbe.laf.theme;

import java.awt.Color;

import javax.swing.plaf.ColorUIResource;

public final class ElowbePalette {
    public final ColorUIResource background;
    public final ColorUIResource foreground;
    public final ColorUIResource card;
    public final ColorUIResource cardForeground;
    public final ColorUIResource popover;
    public final ColorUIResource popoverForeground;
    public final ColorUIResource primary;
    public final ColorUIResource primaryForeground;
    public final ColorUIResource secondary;
    public final ColorUIResource secondaryForeground;
    public final ColorUIResource muted;
    public final ColorUIResource mutedForeground;
    public final ColorUIResource accent;
    public final ColorUIResource accentForeground;
    public final ColorUIResource destructive;
    public final ColorUIResource destructiveForeground;
    public final ColorUIResource border;
    public final ColorUIResource input;
    public final ColorUIResource ring;
    public final ColorUIResource selection;
    public final ColorUIResource selectionForeground;
    public final ColorUIResource hover;
    public final ColorUIResource pressed;
    public final ColorUIResource disabled;
    public final ColorUIResource disabledForeground;

    private ElowbePalette(
            ColorUIResource background,
            ColorUIResource foreground,
            ColorUIResource card,
            ColorUIResource cardForeground,
            ColorUIResource popover,
            ColorUIResource popoverForeground,
            ColorUIResource primary,
            ColorUIResource primaryForeground,
            ColorUIResource secondary,
            ColorUIResource secondaryForeground,
            ColorUIResource muted,
            ColorUIResource mutedForeground,
            ColorUIResource accent,
            ColorUIResource accentForeground,
            ColorUIResource destructive,
            ColorUIResource destructiveForeground,
            ColorUIResource border,
            ColorUIResource input,
            ColorUIResource ring,
            ColorUIResource selection,
            ColorUIResource selectionForeground,
            ColorUIResource hover,
            ColorUIResource pressed,
            ColorUIResource disabled,
            ColorUIResource disabledForeground) {
        this.background = background;
        this.foreground = foreground;
        this.card = card;
        this.cardForeground = cardForeground;
        this.popover = popover;
        this.popoverForeground = popoverForeground;
        this.primary = primary;
        this.primaryForeground = primaryForeground;
        this.secondary = secondary;
        this.secondaryForeground = secondaryForeground;
        this.muted = muted;
        this.mutedForeground = mutedForeground;
        this.accent = accent;
        this.accentForeground = accentForeground;
        this.destructive = destructive;
        this.destructiveForeground = destructiveForeground;
        this.border = border;
        this.input = input;
        this.ring = ring;
        this.selection = selection;
        this.selectionForeground = selectionForeground;
        this.hover = hover;
        this.pressed = pressed;
        this.disabled = disabled;
        this.disabledForeground = disabledForeground;
    }

    public static ElowbePalette light() {
        return new ElowbePalette(
                color("#ffffff"),
                color("#09090b"),
                color("#ffffff"),
                color("#09090b"),
                color("#ffffff"),
                color("#09090b"),
                color("#18181b"),
                color("#fafafa"),
                color("#f4f4f5"),
                color("#18181b"),
                color("#f4f4f5"),
                color("#71717a"),
                color("#f4f4f5"),
                color("#18181b"),
                color("#dc2626"),
                color("#fef2f2"),
                color("#e4e4e7"),
                color("#e4e4e7"),
                color("#18181b"),
                color("#18181b"),
                color("#fafafa"),
                color("#f8fafc"),
                color("#e4e4e7"),
                color("#f4f4f5"),
                color("#a1a1aa"));
    }

    public static ElowbePalette dark() {
        return new ElowbePalette(
                color("#000000"),
                color("#fafafa"),
                color("#000000"),
                color("#fafafa"),
                color("#000000"),
                color("#fafafa"),
                color("#fafafa"),
                color("#000000"),
                color("#000000"),
                color("#fafafa"),
                color("#000000"),
                color("#a1a1aa"),
                color("#000000"),
                color("#fafafa"),
                color("#ef4444"),
                color("#fef2f2"),
                color("#27272a"),
                color("#27272a"),
                color("#fafafa"),
                color("#fafafa"),
                color("#000000"),
                color("#000000"),
                color("#3f3f46"),
                color("#000000"),
                color("#71717a"));
    }

    public static ColorUIResource color(String hex) {
        return new ColorUIResource(Color.decode(hex));
    }
}
