package com.foodordering.ui;

import com.foodordering.model.User;
import com.foodordering.util.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * Main window of the admin.
 * A menu strip is shown on the left and the selected screen is displayed
 * on the right using a CardLayout, so only one panel is visible at a time.
 */
public class AdminDashboard extends JFrame {

    private final User admin;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private final CategoryPanel categoryPanel = new CategoryPanel();
    private final FoodItemPanel foodItemPanel = new FoodItemPanel();
    private final AdminOrderPanel orderPanel = new AdminOrderPanel();

    public AdminDashboard(User admin) {
        this.admin = admin;

        setTitle("Food Ordering System - Admin Dashboard");
        UITheme.setSizeWithinScreen(this, 1000, 620);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.HEADER);

        JLabel title = new JLabel("  FOOD ORDERING SYSTEM  -  ADMIN PANEL");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));

        JLabel welcome = new JLabel("Logged in as : " + admin.getFullName() + "   ");
        welcome.setFont(UITheme.LABEL_FONT);
        welcome.setForeground(Color.WHITE);

        header.add(title, BorderLayout.WEST);
        header.add(welcome, BorderLayout.EAST);
        return header;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));

        sidebar.add(createMenuButton("Dashboard Home", "HOME"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createMenuButton("Manage Categories", "CATEGORY"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createMenuButton("Manage Food Items", "FOOD"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createMenuButton("Manage Orders", "ORDERS"));
        sidebar.add(Box.createVerticalGlue());

        JButton logout = UITheme.createButton("Logout", UITheme.DANGER);
        logout.setAlignmentX(Component.CENTER_ALIGNMENT);
        logout.setMaximumSize(new Dimension(190, 38));
        logout.addActionListener(e -> doLogout());
        sidebar.add(logout);

        return sidebar;
    }

    private JButton createMenuButton(String text, String cardName) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBackground(new Color(72, 96, 120));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(190, 40));
        button.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        button.addActionListener(e -> {
            cardLayout.show(contentPanel, cardName);
            refreshCard(cardName);
        });
        return button;
    }

    /** Reloads the data of a screen every time it is opened. */
    private void refreshCard(String cardName) {
        switch (cardName) {
            case "CATEGORY" -> categoryPanel.loadCategories();
            case "FOOD" -> foodItemPanel.refresh();
            case "ORDERS" -> orderPanel.loadOrders();
            default -> { }
        }
    }

    private JPanel buildContent() {
        contentPanel.add(buildHomePanel(), "HOME");
        contentPanel.add(categoryPanel, "CATEGORY");
        contentPanel.add(foodItemPanel, "FOOD");
        contentPanel.add(orderPanel, "ORDERS");
        cardLayout.show(contentPanel, "HOME");
        return contentPanel;
    }

    /** Simple welcome screen shown when the admin dashboard opens. */
    private JPanel buildHomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BACKGROUND);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(UITheme.createGroupBorder("Welcome"));

        String[] lines = {
                "Welcome to the Admin Panel of the Food Ordering System.",
                " ",
                "Use the menu on the left side to :",
                "     -  add, update and delete food categories",
                "     -  add, update and delete food items of the menu",
                "     -  view the orders placed by customers and change their status",
                " ",
                "Click Logout when the work is finished."
        };

        for (String line : lines) {
            JLabel label = new JLabel(line);
            label.setFont(UITheme.LABEL_FONT);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(label);
            card.add(Box.createVerticalStrut(4));
        }

        panel.add(card);
        return panel;
    }

    private void doLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Do you really want to logout ?", "Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
