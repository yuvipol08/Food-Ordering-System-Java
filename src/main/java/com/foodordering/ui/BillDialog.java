package com.foodordering.ui;

import com.foodordering.model.Order;
import com.foodordering.model.OrderItem;
import com.foodordering.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;

/**
 * Shows the bill of an order.
 * The bill is written as plain text into a JTextArea so that it looks
 * like a printed receipt, and it can also be saved as a text file.
 */
public class BillDialog extends JDialog {

    private final Order order;
    private final JTextArea billArea = new JTextArea();

    public BillDialog(Window parent, Order order) {
        super(parent, "Bill - " + order.getOrderNumber(), ModalityType.APPLICATION_MODAL);
        this.order = order;

        setSize(520, 560);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        add(UITheme.createTitleLabel("ORDER BILL"), BorderLayout.NORTH);
        add(buildBillArea(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        billArea.setText(buildBillText());
        billArea.setCaretPosition(0);
    }

    private JScrollPane buildBillArea() {
        billArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        billArea.setEditable(false);
        billArea.setBackground(Color.WHITE);
        billArea.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        return new JScrollPane(billArea);
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        panel.setBackground(UITheme.BACKGROUND);

        JButton saveButton = UITheme.createButton("Save Bill as Text File", UITheme.BUTTON);
        JButton closeButton = UITheme.createButton("Close", new Color(127, 140, 141));

        saveButton.addActionListener(e -> saveBill());
        closeButton.addActionListener(e -> dispose());

        panel.add(saveButton);
        panel.add(closeButton);
        return panel;
    }

    /**
     * Builds the text of the receipt line by line.
     * String.format is used so that the columns stay in a straight line.
     */
    private String buildBillText() {
        StringBuilder bill = new StringBuilder();
        String line = "--------------------------------------------------\n";

        bill.append("       F O O D   O R D E R I N G   S Y S T E M\n");
        bill.append("                  ORDER  RECEIPT\n");
        bill.append(line);
        bill.append(String.format("Order Number  : %s%n", order.getOrderNumber()));
        bill.append(String.format("Order Date    : %s%n", order.getOrderDate()));
        bill.append(String.format("Customer Name : %s%n", order.getCustomerName()));
        bill.append(String.format("Payment Mode  : %s%n", order.getPaymentMode()));
        bill.append(String.format("Order Status  : %s%n", order.getStatus()));
        bill.append(line);
        bill.append(String.format("%-3s %-20s %5s %4s %10s%n",
                "No", "Food Item", "Rate", "Qty", "Amount"));
        bill.append(line);

        int srNo = 1;
        for (OrderItem item : order.getOrderItems()) {
            bill.append(String.format("%-3d %-20s %5.2f %4d %10.2f%n",
                    srNo++,
                    trim(item.getFoodName()),
                    item.getPrice(),
                    item.getQuantity(),
                    item.getSubtotal()));
        }

        bill.append(line);
        bill.append(String.format("%-34s %13.2f%n", "TOTAL AMOUNT (Rs.)", order.getTotalAmount()));
        bill.append(line);
        bill.append("\n        Thank you for your order. Visit again !\n");

        return bill.toString();
    }

    /** Cuts a long food name so that the bill columns stay aligned. */
    private String trim(String name) {
        return name.length() > 20 ? name.substring(0, 20) : name;
    }

    /** Saves the same text into a .txt file chosen by the user. */
    private void saveBill() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(order.getOrderNumber() + ".txt"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try (PrintWriter writer = new PrintWriter(chooser.getSelectedFile())) {
            writer.print(billArea.getText());
            JOptionPane.showMessageDialog(this,
                    "Bill saved successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not save the bill : " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
