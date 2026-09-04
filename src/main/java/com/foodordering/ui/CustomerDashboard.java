package com.foodordering.ui;

import com.foodordering.model.Cart;
import com.foodordering.model.User;
import com.foodordering.util.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * Main window of a customer.
 * It keeps one Cart object and shares it with the menu screen and the
 * cart screen, so an item added on the menu screen is seen on the cart screen.
 */
public class CustomerDashboard extends JFrame {

    private final User customer;
    private final Cart cart = new Cart();

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private final MenuPanel menuPanel;
    private final CartPanel cartPanel;
    private final OrderHistoryPanel historyPanel;
    private final ProfilePanel profilePanel;

    public CustomerDashboard(User customer) {
        this.customer = customer;

        menuPanel = new MenuPanel(cart, this);
        cartPanel = new CartPanel(cart, customer, this);
        historyPanel = new OrderHistoryPanel(customer);
        profilePanel = new ProfilePanel(customer);

        setTitle("Food Ordering System - Customer");
        setSize(1000, 620);
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

        JLabel title = new JLabel("  FOOD ORDERING SYSTEM");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));

        JLabel welcome = new JLabel("Welcome, " + customer.getFullName() + "   ");
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

        sidebar.add(createMenuButton("Browse Menu", "MENU"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createMenuButton("My Cart", "CART"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createMenuButton("My Orders", "HISTORY"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createMenuButton("My Profile", "PROFILE"));
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

        button.addActionListener(e -> showCard(cardName));
        return button;
    }

    /** Opens one of the screens and reloads its data. */
    public void showCard(String cardName) {
        cardLayout.show(contentPanel, cardName);

        switch (cardName) {
            case "MENU" -> menuPanel.refresh();
            case "CART" -> cartPanel.refreshCart();
            case "HISTORY" -> historyPanel.loadOrders();
            default -> { }
        }
    }

    private JPanel buildContent() {
        contentPanel.add(menuPanel, "MENU");
        contentPanel.add(cartPanel, "CART");
        contentPanel.add(historyPanel, "HISTORY");
        contentPanel.add(profilePanel, "PROFILE");
        cardLayout.show(contentPanel, "MENU");
        return contentPanel;
    }

    private void doLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Do you really want to logout ?", "Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            cart.clear();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
