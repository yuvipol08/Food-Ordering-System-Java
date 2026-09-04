package com.foodordering.ui;

import com.foodordering.dao.UserDAO;
import com.foodordering.model.User;
import com.foodordering.util.UITheme;
import com.foodordering.util.Validator;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Registration screen for a new customer.
 * After the details are checked a new row with role CUSTOMER is
 * inserted into the users table.
 */
public class RegisterFrame extends JDialog {

    private final JTextField nameField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JPasswordField confirmField = new JPasswordField(20);
    private final JTextField phoneField = new JTextField(20);
    private final JTextArea addressArea = new JTextArea(3, 20);

    private final UserDAO userDAO = new UserDAO();

    public RegisterFrame(JFrame parent) {
        super(parent, "Customer Registration", true);
        setSize(560, 520);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        add(UITheme.createTitleLabel("CUSTOMER REGISTRATION"), BorderLayout.NORTH);
        add(buildFormPanel(), BorderLayout.CENTER);
    }

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(UITheme.BACKGROUND);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(UITheme.createGroupBorder("Enter Your Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        for (JTextField f : new JTextField[]{nameField, emailField, phoneField}) {
            f.setFont(UITheme.FIELD_FONT);
        }
        passwordField.setFont(UITheme.FIELD_FONT);
        confirmField.setFont(UITheme.FIELD_FONT);
        addressArea.setFont(UITheme.FIELD_FONT);
        addressArea.setLineWrap(true);
        addressArea.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));

        String[] labels = {"Full Name :", "Email :", "Password :", "Confirm Password :",
                           "Mobile Number :", "Address :"};
        Component[] fields = {nameField, emailField, passwordField, confirmField,
                              phoneField, new JScrollPane(addressArea)};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            form.add(UITheme.createFormLabel(labels[i]), gbc);
            gbc.gridx = 1;
            form.add(fields[i], gbc);
        }

        JButton registerButton = UITheme.createButton("Register", UITheme.SUCCESS);
        JButton cancelButton = UITheme.createButton("Cancel", UITheme.DANGER);

        registerButton.addActionListener(e -> doRegister());
        cancelButton.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        buttons.setBackground(Color.WHITE);
        buttons.add(registerButton);
        buttons.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = labels.length; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        form.add(buttons, gbc);

        outer.add(form, BorderLayout.CENTER);
        return outer;
    }

    /** Checks every box and saves the customer if everything is correct. */
    private void doRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());
        String phone = phoneField.getText().trim();
        String address = addressArea.getText().trim();

        if (Validator.isEmpty(name) || Validator.isEmpty(email)
                || Validator.isEmpty(password) || Validator.isEmpty(phone)) {
            showError("Name, email, password and mobile number are required.");
            return;
        }
        if (!Validator.isValidEmail(email)) {
            showError("Please enter a valid email address, for example name@example.com");
            return;
        }
        if (password.length() < 5) {
            showError("Password must be at least 5 characters long.");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Password and Confirm Password do not match.");
            return;
        }
        if (!Validator.isValidPhone(phone)) {
            showError("Mobile number must contain exactly 10 digits.");
            return;
        }

        try {
            if (userDAO.emailExists(email)) {
                showError("This email is already registered. Please use another email.");
                return;
            }

            User user = new User(name, email, password, phone, address, "CUSTOMER");

            if (userDAO.register(user)) {
                JOptionPane.showMessageDialog(this,
                        "Registration successful.\nYou can now login with your email and password.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                showError("Registration failed. Please try again.");
            }

        } catch (SQLException ex) {
            showError("Database error : " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }
}
