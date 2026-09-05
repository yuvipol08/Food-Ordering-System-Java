package com.foodordering.ui;

import com.foodordering.dao.CategoryDAO;
import com.foodordering.dao.FoodItemDAO;
import com.foodordering.model.Category;
import com.foodordering.model.FoodItem;
import com.foodordering.util.UITheme;
import com.foodordering.util.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Admin screen to manage the menu.
 * Food items can be added, updated and deleted here, and an item can be
 * marked as not available when it is temporarily out of stock.
 */
public class FoodItemPanel extends JPanel {

    private final JTextField nameField = new JTextField(16);
    private final JTextField descriptionField = new JTextField(16);
    private final JTextField priceField = new JTextField(16);
    private final JComboBox<Category> categoryBox = new JComboBox<>();
    private final JComboBox<String> availableBox = new JComboBox<>(new String[]{"Yes", "No"});
    private final JLabel idLabel = new JLabel("-");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Food ID", "Food Name", "Description", "Category", "Price (Rs.)", "Available"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private final FoodItemDAO foodItemDAO = new FoodItemDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    private int selectedFoodId = 0;

    public FoodItemPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildFormPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(UITheme.createGroupBorder("Food Item Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        nameField.setFont(UITheme.FIELD_FONT);
        descriptionField.setFont(UITheme.FIELD_FONT);
        priceField.setFont(UITheme.FIELD_FONT);
        categoryBox.setFont(UITheme.FIELD_FONT);
        availableBox.setFont(UITheme.FIELD_FONT);
        idLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(UITheme.createFormLabel("Food ID :"), gbc);
        gbc.gridx = 1; form.add(idLabel, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        form.add(UITheme.createFormLabel("Category :"), gbc);
        gbc.gridx = 3; form.add(categoryBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(UITheme.createFormLabel("Food Name :"), gbc);
        gbc.gridx = 1; form.add(nameField, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        form.add(UITheme.createFormLabel("Price (Rs.) :"), gbc);
        gbc.gridx = 3; form.add(priceField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        form.add(UITheme.createFormLabel("Description :"), gbc);
        gbc.gridx = 1; form.add(descriptionField, gbc);

        gbc.gridx = 2; gbc.gridy = 2;
        form.add(UITheme.createFormLabel("Available :"), gbc);
        gbc.gridx = 3; form.add(availableBox, gbc);

        JButton addButton = UITheme.createButton("Add", UITheme.SUCCESS);
        JButton updateButton = UITheme.createButton("Update", UITheme.BUTTON);
        JButton deleteButton = UITheme.createButton("Delete", UITheme.DANGER);
        JButton clearButton = UITheme.createButton("Clear", new Color(127, 140, 141));

        addButton.addActionListener(e -> addFoodItem());
        updateButton.addActionListener(e -> updateFoodItem());
        deleteButton.addActionListener(e -> deleteFoodItem());
        clearButton.addActionListener(e -> clearForm());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttons.setBackground(Color.WHITE);
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        form.add(buttons, gbc);

        return form;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(UITheme.createGroupBorder("Food Item List"));

        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    /** Reloads both the category combo box and the food item table. */
    public void refresh() {
        loadCategories();
        loadFoodItems();
    }

    private void loadCategories() {
        try {
            categoryBox.removeAllItems();
            for (Category category : categoryDAO.getAllCategories()) {
                categoryBox.addItem(category);
            }
        } catch (SQLException ex) {
            showError("Could not load categories : " + ex.getMessage());
        }
    }

    public void loadFoodItems() {
        try {
            tableModel.setRowCount(0);
            List<FoodItem> items = foodItemDAO.getAllFoodItems();

            for (FoodItem food : items) {
                tableModel.addRow(new Object[]{
                        food.getFoodId(),
                        food.getFoodName(),
                        food.getDescription(),
                        food.getCategoryName(),
                        String.format("%.2f", food.getPrice()),
                        food.getAvailable()
                });
            }
        } catch (SQLException ex) {
            showError("Could not load food items : " + ex.getMessage());
        }
    }

    private void fillFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        selectedFoodId = (int) tableModel.getValueAt(row, 0);
        idLabel.setText(String.valueOf(selectedFoodId));
        nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));

        Object description = tableModel.getValueAt(row, 2);
        descriptionField.setText(description == null ? "" : String.valueOf(description));
        priceField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        availableBox.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 5)));

        String categoryName = String.valueOf(tableModel.getValueAt(row, 3));
        for (int i = 0; i < categoryBox.getItemCount(); i++) {
            if (categoryBox.getItemAt(i).getCategoryName().equals(categoryName)) {
                categoryBox.setSelectedIndex(i);
                break;
            }
        }
    }

    /** Reads the form boxes into a FoodItem object after checking them. */
    private FoodItem readForm() {
        String name = nameField.getText().trim();
        String price = priceField.getText().trim();
        Category category = (Category) categoryBox.getSelectedItem();

        if (Validator.isEmpty(name)) {
            showError("Please enter the food name.");
            return null;
        }
        if (category == null) {
            showError("Please add at least one category before adding food items.");
            return null;
        }
        if (!Validator.isValidPrice(price)) {
            showError("Please enter a price between 0.01 and "
                    + Validator.MAX_PRICE + ", for example 120.50");
            return null;
        }

        FoodItem food = new FoodItem();
        food.setFoodName(name);
        food.setDescription(descriptionField.getText().trim());
        food.setCategoryId(category.getCategoryId());
        food.setPrice(Double.parseDouble(price));
        food.setAvailable(String.valueOf(availableBox.getSelectedItem()));
        return food;
    }

    private void addFoodItem() {
        FoodItem food = readForm();
        if (food == null) {
            return;
        }
        try {
            if (foodItemDAO.addFoodItem(food)) {
                showInfo("Food item added successfully.");
                clearForm();
                loadFoodItems();
            }
        } catch (SQLException ex) {
            showError("Could not add food item : " + ex.getMessage());
        }
    }

    private void updateFoodItem() {
        if (selectedFoodId == 0) {
            showError("Please select a food item from the table first.");
            return;
        }
        FoodItem food = readForm();
        if (food == null) {
            return;
        }
        food.setFoodId(selectedFoodId);

        try {
            if (foodItemDAO.updateFoodItem(food)) {
                showInfo("Food item updated successfully.");
                clearForm();
                loadFoodItems();
            }
        } catch (SQLException ex) {
            showError("Could not update food item : " + ex.getMessage());
        }
    }

    private void deleteFoodItem() {
        if (selectedFoodId == 0) {
            showError("Please select a food item from the table first.");
            return;
        }

        try {
            int used = foodItemDAO.countOrderItems(selectedFoodId);
            if (used > 0) {
                showError("This food item cannot be deleted because it is part of "
                        + used + " old order(s).\n"
                        + "Please set Available to 'No' instead, so that customers "
                        + "cannot order it any more.");
                return;
            }

            int choice = JOptionPane.showConfirmDialog(this,
                    "Delete the food item '" + nameField.getText().trim() + "' ?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION && foodItemDAO.deleteFoodItem(selectedFoodId)) {
                showInfo("Food item deleted successfully.");
                clearForm();
                loadFoodItems();
            }
        } catch (SQLException ex) {
            showError("Could not delete food item : " + ex.getMessage());
        }
    }

    private void clearForm() {
        selectedFoodId = 0;
        idLabel.setText("-");
        nameField.setText("");
        descriptionField.setText("");
        priceField.setText("");
        availableBox.setSelectedIndex(0);
        table.clearSelection();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
