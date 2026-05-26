package com.elowbe.laf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicLookAndFeel;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbePalette;
import com.elowbe.laf.theme.ElowbeTheme;
import com.elowbe.laf.util.ElowbeBorder;
import com.elowbe.laf.util.PaintUtils;

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
                "MenuBarUI", packageName + "ElowbeMenuBarUI",
                "MenuUI", packageName + "ElowbeMenuUI",
                "MenuItemUI", packageName + "ElowbeMenuItemUI",
                "CheckBoxMenuItemUI", packageName + "ElowbeCheckBoxMenuItemUI",
                "RadioButtonMenuItemUI", packageName + "ElowbeRadioButtonMenuItemUI",
                "PopupMenuUI", packageName + "ElowbePopupMenuUI",
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

    public static final class CommandPallete<T> extends JPanel {
        private static final long serialVersionUID = 1L;

        private final JTextField filter = new JTextField();
        private final DefaultListModel<T> visibleItems = new DefaultListModel<>();
        private final JList<T> list = new JList<>(visibleItems);
        private final JScrollPane scrollPane;
        private final List<T> allItems = new ArrayList<>();
        private Consumer<T> selectionHandler = item -> {
        };
        private JPopupMenu popup;

        public CommandPallete() {
            super(new BorderLayout(0, 8));
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setPreferredSize(new Dimension(360, 380));

            filter.putClientProperty(ElowbeDefaults.PLACEHOLDER_KEY, "Search commands");
            filter.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent event) {
                    refreshFilter();
                }

                @Override
                public void removeUpdate(DocumentEvent event) {
                    refreshFilter();
                }

                @Override
                public void changedUpdate(DocumentEvent event) {
                    refreshFilter();
                }
            });
            filter.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent event) {
                    if (event.getKeyCode() == KeyEvent.VK_DOWN) {
                        moveSelection(1);
                        event.consume();
                    } else if (event.getKeyCode() == KeyEvent.VK_UP) {
                        moveSelection(-1);
                        event.consume();
                    } else if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                        chooseSelection();
                        event.consume();
                    } else if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        hidePalette();
                        event.consume();
                    }
                }
            });

            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setVisibleRowCount(12);
            list.setOpaque(true);
            list.setCellRenderer(createCellRenderer());
            list.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent event) {
                    if (event.getClickCount() == 2) {
                        chooseSelection();
                    }
                }
            });

            scrollPane = new JScrollPane(list);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.setOpaque(true);
            scrollPane.getViewport().setOpaque(true);

            add(filter, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);
            applyThemeColors();
        }

        public void setItems(Collection<T> items) {
            allItems.clear();
            if (items != null) {
                allItems.addAll(items);
            }
            refreshFilter();
        }

        public void setSelectionHandler(Consumer<T> selectionHandler) {
            this.selectionHandler = Objects.requireNonNull(selectionHandler, "selectionHandler");
        }

        public void show(Component invoker, int x, int y) {
            preparePopup();
            popup.show(invoker, x, y);
            focusFilter();
        }

        public void showCentered(Component invoker) {
            Objects.requireNonNull(invoker, "invoker");
            preparePopup();
            Component anchor = invoker;
            Window window = SwingUtilities.getWindowAncestor(invoker);
            if (window instanceof RootPaneContainer container) {
                anchor = container.getRootPane();
            } else if (window != null) {
                anchor = window;
            }
            Dimension size = getPreferredSize();
            int anchorWidth = Math.max(anchor.getWidth(), window == null ? 0 : window.getWidth());
            int anchorHeight = Math.max(anchor.getHeight(), window == null ? 0 : window.getHeight());
            int x = Math.max(0, (anchorWidth - size.width) / 2);
            int y = Math.max(0, (anchorHeight - size.height) / 2);
            popup.show(anchor, x, y);
            focusFilter();
        }

        private void preparePopup() {
            if (popup == null) {
                popup = new JPopupMenu();
                popup.setBorder(BorderFactory.createEmptyBorder());
                popup.add(this);
            }
            applyThemeColors();
            filter.setText("");
            refreshFilter();
        }

        private void applyThemeColors() {
            ElowbePalette palette = PaintUtils.palette();
            Color listBackground = uiColor("List.background", palette.card);
            Color listForeground = uiColor("List.foreground", palette.foreground);
            Color selectionBackground = uiColor("List.selectionBackground", palette.accent);
            Color selectionForeground = uiColor("List.selectionForeground", palette.accentForeground);

            setBackground(palette.popover);
            filter.setBackground(uiColor("TextField.background", palette.card));
            filter.setForeground(uiColor("TextField.foreground", palette.foreground));
            filter.setCaretColor(uiColor("TextField.caretForeground", palette.foreground));
            filter.setBorder(new ElowbeBorder(palette.input, ElowbeDefaults.RADIUS_MD, new Insets(7, 10, 7, 10)));

            list.setBackground(listBackground);
            list.setForeground(listForeground);
            list.setSelectionBackground(selectionBackground);
            list.setSelectionForeground(selectionForeground);
            if (UIManager.getFont("List.font") != null) {
                list.setFont(UIManager.getFont("List.font"));
            }

            scrollPane.setBackground(listBackground);
            scrollPane.getViewport().setBackground(listBackground);

            if (popup != null) {
                popup.setBackground(palette.popover);
                SwingUtilities.updateComponentTreeUI(popup);
            }
            SwingUtilities.updateComponentTreeUI(this);
        }

        private ListCellRenderer<? super T> createCellRenderer() {
            DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
                private static final long serialVersionUID = 1L;

                @Override
                public Component getListCellRendererComponent(
                        JList<?> list,
                        Object value,
                        int index,
                        boolean isSelected,
                        boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(
                            list, value, index, isSelected, cellHasFocus);
                    label.setOpaque(true);
                    label.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                    if (isSelected) {
                        label.setBackground(list.getSelectionBackground());
                        label.setForeground(list.getSelectionForeground());
                    } else {
                        label.setBackground(list.getBackground());
                        label.setForeground(list.getForeground());
                    }
                    return label;
                }
            };
            renderer.setOpaque(true);
            return renderer;
        }

        private static Color uiColor(String key, Color fallback) {
            Color color = UIManager.getColor(key);
            return color == null ? fallback : color;
        }

        private void focusFilter() {
            SwingUtilities.invokeLater(() -> {
                filter.requestFocusInWindow();
                filter.selectAll();
            });
        }

        public void hidePalette() {
            if (popup != null) {
                popup.setVisible(false);
            }
        }

        private void refreshFilter() {
            String needle = filter.getText() == null ? "" : filter.getText().trim().toLowerCase(Locale.ROOT);
            visibleItems.clear();
            for (T item : allItems) {
                if (needle.isEmpty() || item.toString().toLowerCase(Locale.ROOT).contains(needle)) {
                    visibleItems.addElement(item);
                }
            }
            if (!visibleItems.isEmpty()) {
                list.setSelectedIndex(0);
            }
        }

        private void moveSelection(int delta) {
            if (visibleItems.isEmpty()) {
                return;
            }
            int next = list.getSelectedIndex() + delta;
            next = Math.max(0, Math.min(visibleItems.size() - 1, next));
            list.setSelectedIndex(next);
            list.ensureIndexIsVisible(next);
        }

        private void chooseSelection() {
            T item = list.getSelectedValue();
            if (item == null) {
                return;
            }
            hidePalette();
            selectionHandler.accept(item);
        }
    }
}
