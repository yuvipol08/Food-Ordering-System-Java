package com.foodordering.ui;

import com.foodordering.dao.CategoryDAO;
import com.foodordering.model.Category;
import com.foodordering.util.UITheme;
import com.foodordering.util.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Admin screen to add, view, update and delete food categories.
 * Clicking a row of the table copies its values into the form so that
 * the category can be updated or deleted.
 */
public class CategoryPanel extends JPanel {

    private final JTextField nameField = new JTextField(18);
    private final JTextField descriptionField = new JTextField(18);
    private final JLabel idLabel = new JLabel("-");

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new String[]{"Category ID", "Category Name", "Description"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;   // the table is only for viewing
                }
            };
    private final JTable table = new JTable(tableModel);
    private final CategoryDAO categoryDAO = new CategoryDAO();

    private int selectedCategoryId = 0;

    public CategoryPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildFormPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);

        loadCategories();
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(UITheme.createGroupBorder("Category Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        nameField.setFont(UITheme.FIELD_FONT);
        descriptionField.setFont(UITheme.FIELD_FONT);
        idLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(UITheme.createFormLabel("Category ID :"), gbc);
        gbc.gridx = 1; form.add(idLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(UITheme.createFormLabel("Category Name :"), gbc);
        gbc.gridx = 1; form.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        form.add(UITheme.createFormLabel("Description :"), gbc);
        gbc.gridx = 1; form.add(descriptionField, gbc);

        JButton addButton = UITheme.createButton("Add", UITheme.SUCCESS);
        JButton updateButton = UITheme.createButton("Update", UITheme.BUTTON);
        JButton deleteButton = UITheme.createButton("Delete", UITheme.DANGER);
        JButton clearButton = UITheme.createButton("Clear", new Color(127, 140, 141));

        addButton.addActionListener(e -> addCategory());
        updateButton.addActionListener(e -> updateCategory());
        deleteButton.addActionListener(e -> deleteCategory());
        clearButton.addActionListener(e -> clearForm());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttons.setBackground(Color.WHITE);
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        form.add(buttons, gbc);

        return form;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(UITheme.createGroupBorder("Category List"));

        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    /** Reads all categories from the database and shows them in the table. */
    public void loadCategories() {
        try {
            tableModel.setRowCount(0);
            List<Category> categories = categoryDAO.getAllCategories();

            for (Category category : categories) {
                tableModel.addRow(new Object[]{
                        category.getCategoryId(),
                        category.getCategoryName(),
                        category.getDescription()
                });
            }
        } catch (SQLException ex) {
            showError("Could not load categories : " + ex.getMessage());
        }
    }

    /** Copies the selected row into the form boxes. */
    private void fillFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        selectedCategoryId = (int) tableModel.getValueAt(row, 0);
        idLabel.setText(String.valueOf(selectedCategoryId));
        nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        Object description = tableModel.getValueAt(row, 2);
        descriptionField.setText(description == null ? "" : String.valueOf(description));
    }

    private void addCategory() {
        String name = nameField.getText().trim();

        if (Validator.isEmpty(name)) {
            showError("Please enter the category name.");
            return;
        }

        try {
            Category category = new Category(0, name, descriptionField.getText().trim());

            if (categoryDAO.addCategory(category)) {
                showInfo("Category added successfully.");
                clearForm();
                loadCategories();
            }
        } catch (SQLException ex) {
            // A duplicate name breaks the UNIQUE key on category_name
            if (ex.getMessage().toLowerCase().contains("duplicate")) {
                showError("This category name already exists. Please use a different name.");
            } else {
                showError("Could not add category : " + ex.getMessage());
            }
        }
    }

    private void updateCategory() {
        if (selectedCategoryId == 0) {
            showError("Please select a category from the table first.");
            return;
        }
        String name = nameField.getText().trim();
        if (Validator.isEmpty(name)) {
            showError("Please enter the category name.");
            return;
        }

        try {
            Category category = new Category(selectedCategoryId, name,
                                             descriptionField.getText().trim());

            if (categoryDAO.updateCategory(category)) {
                showInfo("Category updated successfully.");
                clearForm();
                loadCategories();
            }
        } catch (SQLException ex) {
            if (ex.getMessage().toLowerCase().contains("duplicate")) {
                showError("This category name already exists. Please use a different name.");
            } else {
                showError("Could not update category : " + ex.getMessage());
            }
        }
    }

    private void deleteCategory() {
        if (selectedCategoryId == 0) {
            showError("Please select a category from the table first.");
            return;
        }

        try {
            int used = categoryDAO.countFoodItems(selectedCategoryId);
            if (used > 0) {
                showError("This category cannot be deleted because " + used
                        + " food item(s) belong to it.\n"
                        + "Please delete or move those food items first.");
                return;
            }

            int choice = JOptionPane.showConfirmDialog(this,
                    "Delete the category '" + nameField.getText().trim() + "' ?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION && categoryDAO.deleteCategory(selectedCategoryId)) {
                showInfo("Category deleted successfully.");
                clearForm();
                loadCategories();
            }
        } catch (SQLException ex) {
            showError("Could not delete category : " + ex.getMessage());
        }
    }

    private void clearForm() {
        selectedCategoryId = 0;
        idLabel.setText("-");
        nameField.setText("");
        descriptionField.setText("");
        table.clearSelection();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
