package com.foodordering.ui;

import com.foodordering.dao.OrderDAO;
import com.foodordering.model.Order;
import com.foodordering.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Admin screen that lists the orders placed by all customers.
 * The admin can filter the list by status, look at the items of one
 * order and move an order to the next status.
 */
public class AdminOrderPanel extends JPanel {

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Order ID", "Order Number", "Customer", "Order Date",
                         "Total (Rs.)", "Payment Mode", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private final JComboBox<String> filterBox = new JComboBox<>();
    private final JComboBox<String> statusBox = new JComboBox<>(OrderDAO.ORDER_STATUS);

    private final OrderDAO orderDAO = new OrderDAO();

    public AdminOrderPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildActionPanel(), BorderLayout.SOUTH);

        loadOrders();
    }

    private JPanel buildToolbar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(UITheme.createGroupBorder("Filter Orders"));

        filterBox.addItem("All Orders");
        for (String status : OrderDAO.ORDER_STATUS) {
            filterBox.addItem(status);
        }
        filterBox.setFont(UITheme.FIELD_FONT);

        JButton showButton = UITheme.createButton("Show", UITheme.BUTTON);
        JButton refreshButton = UITheme.createButton("Refresh", new Color(127, 140, 141));

        showButton.addActionListener(e -> loadOrders());
        refreshButton.addActionListener(e -> {
            filterBox.setSelectedIndex(0);
            loadOrders();
        });

        panel.add(UITheme.createFormLabel("Status :"));
        panel.add(filterBox);
        panel.add(showButton);
        panel.add(refreshButton);
        return panel;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(UITheme.createGroupBorder("Customer Orders"));

        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(UITheme.createGroupBorder("Update Order Status"));

        statusBox.setFont(UITheme.FIELD_FONT);

        JButton updateButton = UITheme.createButton("Update Status", UITheme.SUCCESS);
        JButton viewButton = UITheme.createButton("View Order Details", UITheme.BUTTON);

        updateButton.addActionListener(e -> updateStatus());
        viewButton.addActionListener(e -> viewOrderDetails());

        panel.add(UITheme.createFormLabel("New Status :"));
        panel.add(statusBox);
        panel.add(updateButton);
        panel.add(viewButton);
        return panel;
    }

    /** Loads all orders, or only the orders of the selected status. */
    public void loadOrders() {
        try {
            tableModel.setRowCount(0);
            String filter = String.valueOf(filterBox.getSelectedItem());

            List<Order> orders = "All Orders".equals(filter)
                    ? orderDAO.getAllOrders()
                    : orderDAO.getOrdersByStatus(filter);

            for (Order order : orders) {
                tableModel.addRow(new Object[]{
                        order.getOrderId(),
                        order.getOrderNumber(),
                        order.getCustomerName(),
                        order.getOrderDate(),
                        UITheme.money(order.getTotalAmount()),
                        order.getPaymentMode(),
                        order.getStatus()
                });
            }
        } catch (SQLException ex) {
            showError("Could not load orders : " + ex.getMessage());
        }
    }

    private void updateStatus() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Please select an order from the table first.");
            return;
        }

        int orderId = (int) tableModel.getValueAt(row, 0);
        String orderNumber = String.valueOf(tableModel.getValueAt(row, 1));
        String newStatus = String.valueOf(statusBox.getSelectedItem());

        try {
            if (orderDAO.updateOrderStatus(orderId, newStatus)) {
                JOptionPane.showMessageDialog(this,
                        "Status of order " + orderNumber + " changed to " + newStatus + ".",
                        "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                loadOrders();
            }
        } catch (SQLException ex) {
            showError("Could not update status : " + ex.getMessage());
        }
    }

    /** Opens the bill window so the admin can see what the customer ordered. */
    private void viewOrderDetails() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Please select an order from the table first.");
            return;
        }

        int orderId = (int) tableModel.getValueAt(row, 0);

        try {
            Order order = new Order();
            order.setOrderId(orderId);
            order.setOrderNumber(String.valueOf(tableModel.getValueAt(row, 1)));
            order.setCustomerName(String.valueOf(tableModel.getValueAt(row, 2)));
            order.setOrderDate(String.valueOf(tableModel.getValueAt(row, 3)));
            order.setTotalAmount(Double.parseDouble(String.valueOf(tableModel.getValueAt(row, 4))));
            order.setPaymentMode(String.valueOf(tableModel.getValueAt(row, 5)));
            order.setStatus(String.valueOf(tableModel.getValueAt(row, 6)));
            order.setOrderItems(orderDAO.getOrderItems(orderId));

            new BillDialog(SwingUtilities.getWindowAncestor(this), order).setVisible(true);

        } catch (SQLException ex) {
            showError("Could not load order details : " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
