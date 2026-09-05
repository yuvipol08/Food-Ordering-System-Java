package com.foodordering.ui;

import com.foodordering.dao.OrderDAO;
import com.foodordering.model.Order;
import com.foodordering.model.User;
import com.foodordering.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Customer screen that lists the previous orders of the logged in customer
 * with their status and total amount. The bill of any old order can be
 * opened again from here.
 */
public class OrderHistoryPanel extends JPanel {

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Order ID", "Order Number", "Order Date",
                         "Total (Rs.)", "Payment Mode", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private final User customer;
    private final OrderDAO orderDAO = new OrderDAO();

    public OrderHistoryPanel(User customer) {
        this.customer = customer;

        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        loadOrders();
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(UITheme.createGroupBorder("My Previous Orders"));

        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(UITheme.createGroupBorder("Order Details"));

        JButton viewButton = UITheme.createButton("View Bill", UITheme.BUTTON);
        JButton refreshButton = UITheme.createButton("Refresh", new Color(127, 140, 141));

        viewButton.addActionListener(e -> viewBill());
        refreshButton.addActionListener(e -> loadOrders());

        panel.add(viewButton);
        panel.add(refreshButton);
        return panel;
    }

    /** Reads the orders of this customer from the database. */
    public void loadOrders() {
        try {
            tableModel.setRowCount(0);
            List<Order> orders = orderDAO.getOrdersByUser(customer.getUserId());

            for (Order order : orders) {
                tableModel.addRow(new Object[]{
                        order.getOrderId(),
                        order.getOrderNumber(),
                        order.getOrderDate(),
                        UITheme.money(order.getTotalAmount()),
                        order.getPaymentMode(),
                        order.getStatus()
                });
            }
        } catch (SQLException ex) {
            showError("Could not load your orders : " + ex.getMessage());
        }
    }

    /** Opens the bill of the selected old order. */
    private void viewBill() {
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
            order.setCustomerName(customer.getFullName());
            order.setOrderDate(String.valueOf(tableModel.getValueAt(row, 2)));
            order.setTotalAmount(Double.parseDouble(String.valueOf(tableModel.getValueAt(row, 3))));
            order.setPaymentMode(String.valueOf(tableModel.getValueAt(row, 4)));
            order.setStatus(String.valueOf(tableModel.getValueAt(row, 5)));
            order.setOrderItems(orderDAO.getOrderItems(orderId));

            new BillDialog(SwingUtilities.getWindowAncestor(this), order).setVisible(true);

        } catch (SQLException ex) {
            showError("Could not load the bill : " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
