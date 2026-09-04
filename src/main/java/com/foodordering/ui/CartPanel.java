package com.foodordering.ui;

import com.foodordering.dao.OrderDAO;
import com.foodordering.model.Cart;
import com.foodordering.model.CartItem;
import com.foodordering.model.Order;
import com.foodordering.model.User;
import com.foodordering.util.UITheme;
import com.foodordering.util.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

/**
 * Customer screen that shows the items in the cart.
 * The quantity of a line can be changed, a line can be removed and the
 * order can be placed. Placing the order saves it in the database and
 * opens the bill.
 */
public class CartPanel extends JPanel {

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Sr No", "Food Name", "Rate (Rs.)", "Quantity", "Subtotal (Rs.)"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private final JLabel totalLabel = new JLabel("Total Amount : Rs. 0.00");
    private final JTextField quantityField = new JTextField("1", 4);
    private final JComboBox<String> paymentBox =
            new JComboBox<>(new String[]{"Cash on Delivery", "Pay at Counter"});

    private final Cart cart;
    private final User customer;
    private final CustomerDashboard dashboard;
    private final OrderDAO orderDAO = new OrderDAO();

    public CartPanel(Cart cart, User customer, CustomerDashboard dashboard) {
        this.cart = cart;
        this.customer = customer;
        this.dashboard = dashboard;

        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildActionPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(UITheme.createGroupBorder("Items in Your Cart"));

        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        totalLabel.setForeground(UITheme.HEADER);
        totalLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 6, 10));
        totalLabel.setHorizontalAlignment(JLabel.RIGHT);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(totalLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildActionPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 6));
        panel.setBackground(UITheme.BACKGROUND);

        JPanel editRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        editRow.setBackground(Color.WHITE);
        editRow.setBorder(UITheme.createGroupBorder("Change Cart"));

        quantityField.setFont(UITheme.FIELD_FONT);

        JButton changeButton = UITheme.createButton("Change Quantity", UITheme.BUTTON);
        JButton removeButton = UITheme.createButton("Remove Item", UITheme.DANGER);
        JButton addMoreButton = UITheme.createButton("Add More Items", new Color(127, 140, 141));

        changeButton.addActionListener(e -> changeQuantity());
        removeButton.addActionListener(e -> removeItem());
        addMoreButton.addActionListener(e -> dashboard.showCard("MENU"));

        editRow.add(UITheme.createFormLabel("New Quantity :"));
        editRow.add(quantityField);
        editRow.add(changeButton);
        editRow.add(removeButton);
        editRow.add(addMoreButton);

        JPanel orderRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        orderRow.setBackground(Color.WHITE);
        orderRow.setBorder(UITheme.createGroupBorder("Place Order"));

        paymentBox.setFont(UITheme.FIELD_FONT);
        JButton placeButton = UITheme.createButton("Place Order", UITheme.SUCCESS);
        placeButton.addActionListener(e -> placeOrder());

        orderRow.add(UITheme.createFormLabel("Payment Mode :"));
        orderRow.add(paymentBox);
        orderRow.add(placeButton);

        panel.add(editRow);
        panel.add(orderRow);
        return panel;
    }

    /** Fills the table from the cart and recalculates the total. */
    public void refreshCart() {
        tableModel.setRowCount(0);
        int srNo = 1;

        for (CartItem item : cart.getItems()) {
            tableModel.addRow(new Object[]{
                    srNo++,
                    item.getFoodItem().getFoodName(),
                    String.format("%.2f", item.getFoodItem().getPrice()),
                    item.getQuantity(),
                    String.format("%.2f", item.getSubtotal())
            });
        }
        totalLabel.setText("Total Amount : Rs. " + String.format("%.2f", cart.getTotalAmount()));
    }

    private void changeQuantity() {
        int row = table.getSelectedRow();

        if (row < 0) {
            showError("Please select an item from the cart first.");
            return;
        }
        if (!Validator.isValidQuantity(quantityField.getText())) {
            showError("Please enter a valid quantity greater than 0.");
            return;
        }

        cart.updateQuantity(row, Integer.parseInt(quantityField.getText().trim()));
        refreshCart();
    }

    private void removeItem() {
        int row = table.getSelectedRow();

        if (row < 0) {
            showError("Please select an item from the cart first.");
            return;
        }

        cart.removeItem(row);
        refreshCart();
    }

    /** Saves the cart as an order and shows the bill. */
    private void placeOrder() {
        if (cart.isEmpty()) {
            showError("Your cart is empty. Please add some food items first.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Place the order for Rs. " + String.format("%.2f", cart.getTotalAmount()) + " ?",
                "Confirm Order", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            Order order = orderDAO.placeOrder(customer.getUserId(), cart,
                                              String.valueOf(paymentBox.getSelectedItem()));

            if (order == null) {
                showError("The order could not be saved. Please try again.");
                return;
            }

            order.setCustomerName(customer.getFullName());
            cart.clear();
            refreshCart();

            JOptionPane.showMessageDialog(this,
                    "Your order has been placed successfully.\n"
                    + "Order Number : " + order.getOrderNumber(),
                    "Order Placed", JOptionPane.INFORMATION_MESSAGE);

            new BillDialog(SwingUtilities.getWindowAncestor(this), order).setVisible(true);

        } catch (SQLException ex) {
            showError("Could not place the order : " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
