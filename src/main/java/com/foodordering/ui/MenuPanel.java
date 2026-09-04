package com.foodordering.ui;

import com.foodordering.dao.CategoryDAO;
import com.foodordering.dao.FoodItemDAO;
import com.foodordering.model.Cart;
import com.foodordering.model.Category;
import com.foodordering.model.FoodItem;
import com.foodordering.util.UITheme;
import com.foodordering.util.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Customer screen that shows the menu.
 * Only the food items marked available are displayed. The list can be
 * filtered by category or searched by name, and the selected item is
 * added to the cart with the entered quantity.
 */
public class MenuPanel extends JPanel {

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Food ID", "Food Name", "Description", "Category", "Price (Rs.)"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private final JComboBox<String> categoryBox = new JComboBox<>();
    private final JTextField searchField = new JTextField(14);
    private final JTextField quantityField = new JTextField("1", 4);

    private final FoodItemDAO foodItemDAO = new FoodItemDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    /** Food items currently shown in the table, in the same order as the rows. */
    private List<FoodItem> displayedItems = new ArrayList<>();

    private final Cart cart;
    private final CustomerDashboard dashboard;

    public MenuPanel(Cart cart, CustomerDashboard dashboard) {
        this.cart = cart;
        this.dashboard = dashboard;

        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildActionPanel(), BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildToolbar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(UITheme.createGroupBorder("Search Food"));

        categoryBox.setFont(UITheme.FIELD_FONT);
        searchField.setFont(UITheme.FIELD_FONT);

        JButton filterButton = UITheme.createButton("Show Category", UITheme.BUTTON);
        JButton searchButton = UITheme.createButton("Search by Name", UITheme.BUTTON);
        JButton allButton = UITheme.createButton("Show All", new Color(127, 140, 141));

        filterButton.addActionListener(e -> filterByCategory());
        searchButton.addActionListener(e -> searchByName());
        allButton.addActionListener(e -> {
            categoryBox.setSelectedIndex(0);
            searchField.setText("");
            loadAllItems();
        });

        panel.add(UITheme.createFormLabel("Category :"));
        panel.add(categoryBox);
        panel.add(filterButton);
        panel.add(Box.createHorizontalStrut(15));
        panel.add(UITheme.createFormLabel("Food Name :"));
        panel.add(searchField);
        panel.add(searchButton);
        panel.add(allButton);
        return panel;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(UITheme.createGroupBorder("Available Food Items"));

        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(UITheme.createGroupBorder("Add to Cart"));

        quantityField.setFont(UITheme.FIELD_FONT);

        JButton addButton = UITheme.createButton("Add to Cart", UITheme.SUCCESS);
        JButton goToCart = UITheme.createButton("Go to Cart", UITheme.BUTTON);

        addButton.addActionListener(e -> addToCart());
        goToCart.addActionListener(e -> dashboard.showCard("CART"));

        panel.add(UITheme.createFormLabel("Quantity :"));
        panel.add(quantityField);
        panel.add(addButton);
        panel.add(goToCart);
        return panel;
    }

    /** Reloads the category list and the menu. */
    public void refresh() {
        loadCategories();
        loadAllItems();
    }

    private void loadCategories() {
        try {
            String selected = (String) categoryBox.getSelectedItem();
            categoryBox.removeAllItems();
            categoryBox.addItem("All Categories");

            for (Category category : categoryDAO.getAllCategories()) {
                categoryBox.addItem(category.getCategoryName());
            }
            if (selected != null) {
                categoryBox.setSelectedItem(selected);
            }
        } catch (SQLException ex) {
            showError("Could not load categories : " + ex.getMessage());
        }
    }

    private void loadAllItems() {
        try {
            showItems(foodItemDAO.getAvailableFoodItems());
        } catch (SQLException ex) {
            showError("Could not load the menu : " + ex.getMessage());
        }
    }

    private void filterByCategory() {
        String selected = (String) categoryBox.getSelectedItem();

        if (selected == null || "All Categories".equals(selected)) {
            loadAllItems();
            return;
        }

        try {
            // find the id of the chosen category name
            for (Category category : categoryDAO.getAllCategories()) {
                if (category.getCategoryName().equals(selected)) {
                    showItems(foodItemDAO.getAvailableByCategory(category.getCategoryId()));
                    return;
                }
            }
        } catch (SQLException ex) {
            showError("Could not filter the menu : " + ex.getMessage());
        }
    }

    private void searchByName() {
        String keyword = searchField.getText().trim();

        if (Validator.isEmpty(keyword)) {
            showError("Please type a food name to search.");
            return;
        }

        try {
            List<FoodItem> results = foodItemDAO.searchAvailableByName(keyword);
            showItems(results);

            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No food item found for '" + keyword + "'.",
                        "Search Result", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            showError("Could not search the menu : " + ex.getMessage());
        }
    }

    /** Puts the given food items into the table. */
    private void showItems(List<FoodItem> items) {
        displayedItems = items;
        tableModel.setRowCount(0);

        for (FoodItem food : items) {
            tableModel.addRow(new Object[]{
                    food.getFoodId(),
                    food.getFoodName(),
                    food.getDescription(),
                    food.getCategoryName(),
                    String.format("%.2f", food.getPrice())
            });
        }
    }

    private void addToCart() {
        int row = table.getSelectedRow();

        if (row < 0) {
            showError("Please select a food item from the list first.");
            return;
        }
        if (!Validator.isValidQuantity(quantityField.getText())) {
            showError("Please enter a valid quantity, for example 1 or 2.");
            return;
        }

        int quantity = Integer.parseInt(quantityField.getText().trim());
        FoodItem food = displayedItems.get(row);

        cart.addItem(food, quantity);
        quantityField.setText("1");

        JOptionPane.showMessageDialog(this,
                quantity + " x " + food.getFoodName() + " added to the cart.",
                "Added to Cart", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
