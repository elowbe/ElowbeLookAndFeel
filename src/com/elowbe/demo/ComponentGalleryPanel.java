package com.elowbe.demo;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.util.ElowbeIcons;

public class ComponentGalleryPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    public ComponentGalleryPanel() {
        super(new GridBagLayout());
        setBorder(new EmptyBorder(24, 24, 24, 24));
        build();
    }

    private void build() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 0;

        addCard(paymentCard(), gbc, 0, 0, 1, 2);
        addCard(collaborationCard(), gbc, 1, 0, 1, 1);
        addCard(browserSettingsCard(), gbc, 2, 0, 1, 1);
        addCard(assistantCard(), gbc, 3, 0, 1, 1);
        addCard(chatAndSearchCard(), gbc, 1, 1, 1, 1);
        addCard(environmentCard(), gbc, 2, 1, 1, 2);
        addCard(surveyCard(), gbc, 3, 1, 1, 1);
        addCard(loadingCard(), gbc, 3, 2, 1, 1);
        addCard(progressCard(), gbc, 1, 2, 1, 1);
    }

    private void addCard(JPanel panel, GridBagConstraints gbc, int x, int y, int width, int height) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        add(panel, gbc);
    }

    private JPanel paymentCard() {
        JPanel card = DemoStyles.card();
        card.add(DemoStyles.title("Payment Method"));
        card.add(DemoStyles.muted("All transactions are secure and encrypted"));
        DemoStyles.addGap(card, 14);
        card.add(DemoStyles.label("Name on Card"));
        card.add(DemoStyles.textField("John Doe"));
        DemoStyles.addGap(card, 10);
        JPanel row = DemoStyles.flow(java.awt.FlowLayout.LEFT, 8);
        JTextField cardNumber = DemoStyles.textField("1234 5678 9012 3456");
        JTextField cvv = DemoStyles.textField("123");
        DemoStyles.fixedWidth(cardNumber, 160);
        DemoStyles.fixedWidth(cvv, 68);
        row.add(labeled("Card Number", cardNumber));
        row.add(labeled("CVV", cvv));
        card.add(row);
        card.add(DemoStyles.muted("Enter your 16-digit number."));
        DemoStyles.addGap(card, 12);
        JPanel date = DemoStyles.flow(java.awt.FlowLayout.LEFT, 8);
        JComboBox<String> month = DemoStyles.combo("MM", "01", "02", "03", "04", "05", "06");
        JComboBox<String> year = DemoStyles.combo("YYYY", "2026", "2027", "2028", "2029");
        DemoStyles.fixedWidth(month, 150);
        DemoStyles.fixedWidth(year, 160);
        date.add(labeled("Month", month));
        date.add(labeled("Year", year));
        card.add(date);
        DemoStyles.addGap(card, 16);
        card.add(separator());
        DemoStyles.addGap(card, 14);
        card.add(DemoStyles.title("Billing Address"));
        card.add(DemoStyles.muted("The billing address associated with your payment method"));
        JCheckBox same = new JCheckBox("Same as shipping address", true);
        same.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(same);
        DemoStyles.addGap(card, 14);
        card.add(separator());
        DemoStyles.addGap(card, 14);
        card.add(DemoStyles.label("Comments"));
        card.add(DemoStyles.textArea("Add any additional comments", 3));
        DemoStyles.addGap(card, 14);
        JPanel actions = DemoStyles.flow(java.awt.FlowLayout.LEFT, 8);
        actions.add(DemoStyles.button("Submit", "primary"));
        actions.add(DemoStyles.button("Cancel", "outline"));
        card.add(actions);
        return card;
    }

    private JPanel collaborationCard() {
        JPanel card = DemoStyles.card();
        card.setPreferredSize(new Dimension(260, 170));
        JLabel avatars = new JLabel("   ", SwingConstants.CENTER);
        avatars.setIcon(ElowbeIcons.star(18));
        avatars.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(Box.createVerticalGlue());
        card.add(avatars);
        JLabel title = DemoStyles.title("No Team Members");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        JLabel muted = DemoStyles.muted("Invite your team to collaborate on this project.");
        muted.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(muted);
        DemoStyles.addGap(card, 12);
        javax.swing.JButton invite = DemoStyles.button("+ Invite Members", "primary");
        invite.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(invite);
        card.add(Box.createVerticalGlue());
        return card;
    }

    private JPanel browserSettingsCard() {
        JPanel card = DemoStyles.card();
        card.add(urlBar());
        DemoStyles.addGap(card, 12);
        card.add(twoFactorRow());
        DemoStyles.addGap(card, 10);
        javax.swing.JButton verified = DemoStyles.button("Your profile has been verified.    >", "outline");
        verified.setMaximumSize(new Dimension(Integer.MAX_VALUE, verified.getPreferredSize().height));
        card.add(verified);
        return card;
    }

    private JPanel assistantCard() {
        JPanel card = DemoStyles.card();
        JTextField context = DemoStyles.textField("Add context");
        context.setText("@  Add context");
        card.add(context);
        DemoStyles.addGap(card, 10);
        JTextArea ask = DemoStyles.textArea("Ask, search, or make anything...", 3);
        card.add(ask);
        DemoStyles.addGap(card, 12);
        JPanel row = DemoStyles.flow(java.awt.FlowLayout.LEFT, 6);
        row.add(DemoStyles.iconButton("ghost", ElowbeIcons.arrowLeft(15)));
        row.add(DemoStyles.button("Archive", "outline"));
        row.add(DemoStyles.button("Report", "outline"));
        row.add(DemoStyles.button("Snooze", "outline"));
        row.add(DemoStyles.button("...", "ghost"));
        card.add(row);
        JCheckBox terms = new JCheckBox("I agree to the terms and conditions", true);
        terms.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(terms);
        return card;
    }

    private JPanel chatAndSearchCard() {
        JPanel card = DemoStyles.card();
        JPanel badges = DemoStyles.flow(java.awt.FlowLayout.LEFT, 6);
        badges.add(DemoStyles.badge("Syncing"));
        badges.add(DemoStyles.badge("Updating"));
        badges.add(DemoStyles.badge("Loading"));
        card.add(badges);
        JPanel message = new JPanel(new BorderLayout(8, 0));
        message.setOpaque(false);
        message.setAlignmentX(Component.LEFT_ALIGNMENT);
        message.add(DemoStyles.iconButton("ghost", ElowbeIcons.plus(14)), BorderLayout.WEST);
        message.add(DemoStyles.textField("Send a message..."), BorderLayout.CENTER);
        message.add(DemoStyles.iconButton("ghost", ElowbeIcons.spinner(14)), BorderLayout.EAST);
        card.add(message);
        DemoStyles.addGap(card, 16);
        card.add(DemoStyles.label("Price Range"));
        card.add(DemoStyles.muted("Set your budget range ($200 - 800)."));
        JSlider slider = DemoStyles.slider(78);
        card.add(slider);
        DemoStyles.addGap(card, 12);
        JTextField search = DemoStyles.textField("Search...");
        search.setText("Search...                                      12 results");
        card.add(search);
        DemoStyles.addGap(card, 10);
        JTextField url = DemoStyles.textField("https://example.com");
        card.add(url);
        DemoStyles.addGap(card, 10);
        card.add(DemoStyles.textArea("Ask, Search or Chat...", 3));
        DemoStyles.addGap(card, 8);
        card.add(DemoStyles.muted("@elowbe"));
        return card;
    }

    private JPanel environmentCard() {
        JPanel card = DemoStyles.card();
        card.add(DemoStyles.title("Compute Environment"));
        card.add(DemoStyles.muted("Select the compute environment for your cluster."));
        DemoStyles.addGap(card, 12);
        JRadioButton k8s = option("Kubernetes", true);
        JRadioButton vm = option("Virtual Machine", false);
        ButtonGroup group = new ButtonGroup();
        group.add(k8s);
        group.add(vm);
        card.add(k8s);
        card.add(indentedDetail("Run GPU workloads on a K8s configured cluster. This is the default."));
        DemoStyles.addGap(card, 8);
        card.add(vm);
        card.add(indentedDetail("Access a VM configured cluster to run workloads. Coming soon."));
        DemoStyles.addGap(card, 20);
        card.add(DemoStyles.label("Number of GPUs"));
        card.add(DemoStyles.muted("You can add more later."));
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(8, 1, 64, 1));
        spinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        spinner.setPreferredSize(new Dimension(118, 34));
        spinner.setMinimumSize(new Dimension(118, 34));
        spinner.setMaximumSize(new Dimension(128, 34));
        card.add(spinner);
        DemoStyles.addGap(card, 18);
        JCheckBox tint = new JCheckBox("Wallpaper Tinting", true);
        tint.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(tint);
        card.add(DemoStyles.muted("Allow the wallpaper to be tinted."));
        return card;
    }

    private JPanel surveyCard() {
        JPanel card = DemoStyles.card();
        JPanel top = DemoStyles.flow(java.awt.FlowLayout.LEFT, 6);
        top.add(DemoStyles.button("1", "primary"));
        top.add(DemoStyles.button("2", "outline"));
        top.add(DemoStyles.button("3", "outline"));
        top.add(DemoStyles.iconButton("outline", ElowbeIcons.arrowLeft(14)));
        top.add(DemoStyles.iconButton("outline", ElowbeIcons.arrowRight(14)));
        top.add(DemoStyles.button("Copilot", "outline"));
        card.add(top);
        DemoStyles.addGap(card, 12);
        card.add(DemoStyles.title("How did you hear about us?"));
        card.add(DemoStyles.muted("Select the option that best describes how you found us."));
        DemoStyles.addGap(card, 12);
        JRadioButton social = new JRadioButton("Social Media", true);
        JRadioButton search = new JRadioButton("Search Engine");
        JRadioButton referral = new JRadioButton("Referral");
        JRadioButton other = new JRadioButton("Other");
        DemoStyles.group(social, search, referral, other);
        JPanel chips = DemoStyles.flow(java.awt.FlowLayout.LEFT, 6);
        chips.add(social);
        chips.add(search);
        chips.add(referral);
        chips.add(other);
        card.add(chips);
        return card;
    }

    private JPanel loadingCard() {
        JPanel card = DemoStyles.card();
        card.setPreferredSize(new Dimension(260, 180));
        card.add(Box.createVerticalGlue());
        JLabel spinner = new JLabel(ElowbeIcons.spinner(22), SwingConstants.CENTER);
        spinner.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(spinner);
        DemoStyles.addGap(card, 14);
        JLabel title = DemoStyles.title("Processing your request");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        JLabel body = DemoStyles.muted("Please wait while we process your request. Do not refresh the page.");
        body.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(body);
        DemoStyles.addGap(card, 12);
        javax.swing.JButton cancel = DemoStyles.button("Cancel", "outline");
        cancel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(cancel);
        card.add(Box.createVerticalGlue());
        return card;
    }

    private JPanel progressCard() {
        JPanel card = DemoStyles.card();
        card.add(DemoStyles.title("Progress and Inputs"));
        card.add(DemoStyles.muted("Compact controls with subtle borders and radius."));
        DemoStyles.addGap(card, 14);
        JProgressBar progress = new JProgressBar(0, 100);
        progress.setValue(52);
        progress.setString("");
        progress.setStringPainted(true);
        progress.setAlignmentX(Component.LEFT_ALIGNMENT);
        progress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        card.add(progress);
        DemoStyles.addGap(card, 14);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Overview", new JLabel("Cards, badges, forms"));
        tabs.addTab("Settings", new JLabel("Theme-ready defaults"));
        tabs.setAlignmentX(Component.LEFT_ALIGNMENT);
        tabs.setMaximumSize(new Dimension(Integer.MAX_VALUE, tabs.getPreferredSize().height));
        card.add(tabs);
        DemoStyles.addGap(card, 12);
        JScrollPane scroll = new JScrollPane(new JLabel("A thin modern scrollbar appears when content grows."));
        scroll.setPreferredSize(new Dimension(220, 50));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(scroll);
        return card;
    }

    private JPanel labeled(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(DemoStyles.label(label), BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private JLabel centeredMuted(String text) {
        JLabel label = DemoStyles.muted(text);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JPanel urlBar() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setPreferredSize(new Dimension(1, 44));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        JTextField field = DemoStyles.textField("https://");
        field.setText("https://");
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        panel.add(field, BorderLayout.CENTER);
        panel.add(DemoStyles.iconButton("ghost", ElowbeIcons.star(14)), BorderLayout.EAST);
        return panel;
    }

    private JPanel twoFactorRow() {
        JPanel panel = DemoStyles.card();
        panel.setBorder(new EmptyBorder(10, 12, 10, 12));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(DemoStyles.label("Two-factor authentication"));
        panel.add(DemoStyles.muted("Verify via email or phone number."));
        DemoStyles.addGap(panel, 10);
        panel.add(DemoStyles.button("Enable", "primary"));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        return panel;
    }

    private JRadioButton option(String title, boolean selected) {
        JRadioButton button = new JRadioButton(title, selected);
        button.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        return button;
    }

    private JLabel indentedDetail(String detail) {
        JLabel label = DemoStyles.muted(detail);
        label.setBorder(new EmptyBorder(0, 30, 0, 8));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel separator() {
        JLabel line = new JLabel();
        line.setOpaque(true);
        line.setBackground(UIManager.getColor("Separator.foreground"));
        line.setPreferredSize(new Dimension(1, 1));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        return line;
    }
}
