package com.foodordering.ui;

import com.foodordering.dao.UserDAO;
import com.foodordering.model.User;
import com.foodordering.util.UITheme;
import com.foodordering.util.Validator;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Customer screen that shows the details entered during registration.
 * The name, mobile number, address and password can be changed here.
 * The email is only displayed because it is used to log in.
 */
public class ProfilePanel extends JPanel {

    private final JTextField nameField = new JTextField(20);
    private final JTextField phoneField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JTextArea addressArea = new JTextArea(3, 20);
    private final JLabel emailLabel = new JLabel();
    private final JLabel idLabel = new JLabel();

    private final User customer;
    private final UserDAO userDAO = new UserDAO();

    public ProfilePanel(User customer) {
        this.customer = customer;

        setLayout(new GridBagLayout());
        setBackground(UITheme.BACKGROUND);

        add(buildForm());
        loadProfile();
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(UITheme.createGroupBorder("My Profile"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 10, 7, 10);
        gbc.anchor = GridBagConstraints.WEST;

        nameField.setFont(UITheme.FIELD_FONT);
        phoneField.setFont(UITheme.FIELD_FONT);
        passwordField.setFont(UITheme.FIELD_FONT);
        addressArea.setFont(UITheme.FIELD_FONT);
        addressArea.setLineWrap(true);
        addressArea.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));
        emailLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        idLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        String[] labels = {"Customer ID :", "Email :", "Full Name :",
                           "Mobile Number :", "Password :", "Address :"};
        Component[] fields = {idLabel, emailLabel, nameField, phoneField,
                              passwordField, new JScrollPane(addressArea)};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            form.add(UITheme.createFormLabel(labels[i]), gbc);
            gbc.gridx = 1;
            form.add(fields[i], gbc);
        }

        JButton saveButton = UITheme.createButton("Save Changes", UITheme.SUCCESS);
        JButton resetButton = UITheme.createButton("Reset", new Color(127, 140, 141));

        saveButton.addActionListener(e -> saveProfile());
        resetButton.addActionListener(e -> loadProfile());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        buttons.setBackground(Color.WHITE);
        buttons.add(saveButton);
        buttons.add(resetButton);

        gbc.gridx = 0; gbc.gridy = labels.length; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        form.add(buttons, gbc);

        return form;
    }

    /** Puts the details of the logged in customer into the boxes. */
    private void loadProfile() {
        idLabel.setText(String.valueOf(customer.getUserId()));
        emailLabel.setText(customer.getEmail());
        nameField.setText(customer.getFullName());
        phoneField.setText(customer.getPhone());
        passwordField.setText(customer.getPassword());
        addressArea.setText(customer.getAddress() == null ? "" : customer.getAddress());
    }

    private void saveProfile() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (Validator.isEmpty(name)) {
            showError("Please enter your full name.");
            return;
        }
        if (!Validator.isValidPhone(phone)) {
            showError("Mobile number must contain exactly 10 digits.");
            return;
        }
        if (password.length() < 5) {
            showError("Password must be at least 5 characters long.");
            return;
        }

        try {
            customer.setFullName(name);
            customer.setPhone(phone);
            customer.setPassword(password);
            customer.setAddress(addressArea.getText().trim());

            if (userDAO.updateProfile(customer)) {
                JOptionPane.showMessageDialog(this,
                        "Your profile has been updated successfully.",
                        "Profile Updated", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            showError("Could not update the profile : " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
