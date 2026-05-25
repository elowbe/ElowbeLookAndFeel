# Elowbe LookAndFeel

A modern Look and Feel for Java Swing heavily inspired by [shadcn](https://ui.shadcn.com/).

<!-- Screenshots — add your images here -->

<p align="center">
 <img width="2287" height="1179" alt="SCR-20260525-blli" src="https://github.com/user-attachments/assets/1e3f493c-aada-400f-a5aa-73be2f53cfa9" />
<img width="2287" height="1179" alt="SCR-20260525-blig" src="https://github.com/user-attachments/assets/1378a5d1-8884-480f-ae51-812c2b0af11f" />

</p>


## Installation

You need **Java 17** or later.

### Option 1: Download a release JAR

1. Open the [Releases](https://github.com/elowbe/ElowbeLookAndFeel/releases) page.
2. Download `ElowbeLookAndFeel-<version>.jar`.
3. Add the JAR to your application's classpath (build tool dependency, `-cp`, or IDE library).

### Option 2: Build from source

```bash
git clone https://github.com/elowbe/ElowbeLookAndFeel.git
cd ElowbeLookAndFeel
mvn package
```

The built JAR is at `target/ElowbeLookAndFeel-<version>.jar`.

To install into your local Maven repository:

```bash
mvn install
```

Then add it as a dependency in your project:

```xml
<dependency>
  <groupId>ElowbeLookAndFeel</groupId>
  <artifactId>ElowbeLookAndFeel</artifactId>
  <version><!-- release version --></version>
</dependency>
```

## Running the demos

The demos showcase Elowbe on real Swing UIs — a component gallery, dialogs, a kanban board, and a note-taking app.

### Option 1: Download the demos JAR (releases)

1. From the [Releases](https://github.com/elowbe/ElowbeLookAndFeel/releases) page, download `ElowbeLookAndFeel-demos-<version>.jar`.
2. Run it:

```bash
java -jar ElowbeLookAndFeel-demos-<version>.jar
```

If the demos JAR requires the main library on the classpath, run:

```bash
java -cp "ElowbeLookAndFeel-<version>.jar:ElowbeLookAndFeel-demos-<version>.jar" com.elowbe.demo.ElowbeDemoApp
```

On Windows, use `;` instead of `:` in the classpath.

### Option 2: Run demos from source

From a cloned repository, after building:

```bash
mvn exec:java
```

That launches the **Component Gallery** demo (`ElowbeDemoApp`) by default.

To run a specific demo:

| Demo | Main class | Description |
|------|------------|-------------|
| Component Gallery | `com.elowbe.demo.ElowbeDemoApp` | Buttons, inputs, tables, tabs, and other styled components |
| Dialogs | `com.elowbe.demo.DialogDemoApp` | Option panes, file chooser, and color chooser |
| Kanban Board | `com.elowbe.demo.KanbanBoardDemoApp` | Drag-and-drop task board |
| Note Taking | `com.elowbe.demo.NoteTakingDemoApp` | Searchable notes with categories and preview |

Example — run the kanban demo:

```bash
mvn exec:java -Dexec.mainClass=com.elowbe.demo.KanbanBoardDemoApp
```

Or with `java` directly (after `mvn package`):

```bash
java -cp "target/ElowbeLookAndFeel-<version>.jar:target/classes" com.elowbe.demo.KanbanBoardDemoApp
```

## FAQ

### How do I apply Elowbe to my Swing application?

Install the Look and Feel before creating UI components, ideally on the Event Dispatch Thread:

```java
import com.elowbe.laf.Elowbe;
import com.elowbe.laf.theme.ElowbeTheme;

Elowbe.install(ElowbeTheme.LIGHT); // or ElowbeTheme.DARK
```

Or use the default light theme:

```java
Elowbe.install();
```

### How do I switch between light and dark at runtime?

```java
Elowbe.setTheme(ElowbeTheme.DARK);
// or
Elowbe.toggleTheme();
```

`setTheme` and `toggleTheme` refresh all open windows automatically.

### Which Swing components are styled?

Elowbe provides custom UI delegates for common components including buttons, text fields, text areas, combo boxes, check boxes, radio buttons, sliders, scroll bars, spinners, tabbed panes, table headers, progress bars, panels, file choosers, and color choosers. Components without a custom delegate fall back to the platform defaults.

### What Java version is required?

Java **17** or later. The project is built with Maven using `--release 17`.

### What font is it using?

The font is [Vercel's Geist](https://vercel.com/font)

### Can I use Elowbe alongside other Look and Feels?

Yes. Elowbe is a standard Swing `LookAndFeel`. Install it with `Elowbe.install()` or `UIManager.setLookAndFeel(new ElowbeLookAndFeel())` like any other LAF. Only one Look and Feel is active at a time.

### Does Elowbe work on macOS, Windows, and Linux?

Yes. Elowbe is a cross-platform Swing Look and Feel and runs anywhere Java 17+ and Swing are supported.

### Where do I report bugs or request features?

Open an issue on [GitHub](https://github.com/elowbe/ElowbeLookAndFeel/issues).

## Additional Screenshots
<p align="center">
<img width="1302" height="836" alt="SCR-20260525-bkpl" src="https://github.com/user-attachments/assets/e827414d-963d-4de8-a41e-0dae458cbe70" />

<img width="1302" height="836" alt="SCR-20260525-bkyo" src="https://github.com/user-attachments/assets/5b681cdd-0d29-421d-ad5f-d95ae200565f" />

<img width="530" height="532" alt="SCR-20260525-blra" src="https://github.com/user-attachments/assets/3e03b5d8-ec1d-4a82-8976-16ad3eaac75d" />

</p>

