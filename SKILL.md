---
name: elowbe-look-and-feel
description: Build Java Swing apps with ElowbeLookAndFeel — shadcn-inspired LAF with light/dark themes, rounded controls, and node-graph widgets. Trigger on Elowbe, Swing GUI, jiconfont, or Elowbe-styled desktop apps.
---

# Elowbe Look and Feel

Cross-platform Swing `LookAndFeel` (Java 17+) inspired by [shadcn/ui](https://ui.shadcn.com/). See `README.md` for releases and demo commands.

## Design philosophy

- **Flat & minimal** — No gradients or 3D chrome; thin borders and solid fills.
- **Semantic palette** — `background`, `foreground`, `card`, `primary`, `secondary`, `muted`, `accent`, `destructive`, `border`, `input`, `selection` (see `ElowbePalette`).
- **Rounded** — `RADIUS_SM` 16, `RADIUS_MD` 22, `RADIUS_LG` 32, `RADIUS_PILL` 999. Inputs/buttons: MD; cards: LG.
- **Geist Mono** — 13px base; `Elowbe.font.small` / `.title` / `.mono` for hierarchy.
- **Themes** — Dark: true black + white primary CTAs. Light: white + zinc neutrals. List selection = inverted primary fill.
- **Layout** — Card containers (~18px padding), generous gaps, `BoxLayout` / `BorderLayout`.

## Dependencies

Add to `pom.xml`:

```xml
<dependency>
  <groupId>ElowbeLookAndFeel</groupId>
  <artifactId>ElowbeLookAndFeel</artifactId>
  <version><!-- release version --></version>
</dependency>
<dependency>
  <groupId>com.github.jiconfont</groupId>
  <artifactId>jiconfont-swing</artifactId>
  <version>1.0.0</version>
</dependency>
<dependency>
  <groupId>com.github.jiconfont</groupId>
  <artifactId>jiconfont-font_awesome</artifactId>
  <version>4.7.0.1</version>
</dependency>
```

## Setup

```java
import com.elowbe.laf.Elowbe;
import com.elowbe.laf.theme.ElowbeTheme;

public static void main(String[] args) {
    Elowbe.install(ElowbeTheme.DARK); // or LIGHT — before any Swing component
    SwingUtilities.invokeLater(() -> { /* UI */ });
}
```

Theme: `Elowbe.setTheme(...)`, `Elowbe.toggleTheme()`, `Elowbe.refresh(root)`.

## Styled components

Delegates in `com.elowbe.laf.ui.*` — **Button**, **ToggleButton**, **TextField** / **PasswordField** / **FormattedTextField**, **TextArea**, **ComboBox**, **Spinner**, **CheckBox**, **RadioButton**, **Slider**, **ProgressBar**, **Panel**, **ScrollBar**, **TabbedPane**, **TableHeader**, menus/popups, **FileChooser**, **ColorChooser**. Other Swing widgets use platform defaults.

## Client properties

Set via `ElowbeDefaults` keys on `putClientProperty`:

| Key | Component | Values / effect |
|-----|-----------|-----------------|
| `BUTTON_VARIANT_KEY` | `JButton` | `primary`, `secondary`, `outline`, `destructive`, `ghost`, `link`, `pill` |
| `ROUNDED_KEY` | `JButton` | Full pill (toolbars, theme toggle) |
| `CARD_KEY` | `JPanel` | Rounded bordered card (`ElowbePanelUI`) |
| `PLACEHOLDER_KEY` | text inputs | Muted hint when empty |

```java
card.putClientProperty(ElowbeDefaults.CARD_KEY, Boolean.TRUE);
submit.putClientProperty(ElowbeDefaults.BUTTON_VARIANT_KEY, "primary");
field.putClientProperty(ElowbeDefaults.PLACEHOLDER_KEY, "Search...");
```

**Hierarchy:** `primary` = main CTA · `outline` = secondary · `ghost`/`link` = icon/tertiary · `destructive` = irreversible actions.

## Palette, borders, icons

```java
ElowbePalette p = ElowbeDefaults.palette();
panel.setBackground(p.background);
component.setBorder(new ElowbeBorder(p.border, ElowbeDefaults.RADIUS_MD, insets));
```

Never hard-code `#000`/`#fff` — use palette tokens for theme switches. Icons: prefer `ElowbeIcons`; else register `IconFontSwing` + Font Awesome with `p.foreground` tint.

## Custom widgets

| API | Purpose |
|-----|---------|
| `NodeCanvas`, `NodeComponent`, `NodeConnection` | Zoom/pan graph editor, terminals, bezier wires |
| `NodeGraphJson` | Save/load graphs |
| `ElowbeLookAndFeel.CommandPallete` | Filterable command popup |

Patterns: `com.elowbe.demo.DemoStyles` (cards, stacks, badges), `NodeGraphDemoApp`, `KanbanBoardDemoApp`.

## Best practices

1. `Elowbe.install()` first; UI only on EDT.
2. Panels are non-opaque — use `CARD_KEY` or explicit backgrounds.
3. Form controls: fields ~34px tall, combos ~36px; `setMaximumSize` in vertical stacks.
4. Sidebar lists: solid `selection` background for active row (Notes demo).
5. Copy demo layout/spacing; call `Elowbe.refresh` after manual delegate changes.

## Demos

`ElowbeDemoApp` (gallery) · `NoteTakingDemoApp` · `KanbanBoardDemoApp` · `NodeGraphDemoApp` · `DialogDemoApp` — `mvn exec:java -Dexec.mainClass=...`

## Agent rules

- Default `ElowbeTheme.DARK` unless user wants light.
- Buttons/cards/placeholders via client properties — no custom button painting.
- Colors from `ElowbePalette`; jiconfont before custom icons.
- Non-trivial UI → reference demo classes above.
