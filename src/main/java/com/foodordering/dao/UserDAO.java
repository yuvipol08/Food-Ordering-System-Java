package com.foodordering.dao;

import com.foodordering.db.DBConnection;
import com.foodordering.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * All database work of the users table: login, registration and
 * updating the customer profile.
 */
public class UserDAO {

    /**
     * Checks the email and password against the users table.
     * Returns the User object on success and null when the
     * credentials do not match any row.
     */
    public User login(String email, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildUser(rs);
                }
            }
        }
        return null;
    }

    /** Used during registration to stop two accounts with the same email. */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT user_id FROM users WHERE email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Inserts a new customer. The role is always CUSTOMER here. */
    public boolean register(User user) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, password, phone, address, role) "
                   + "VALUES (?, ?, ?, ?, ?, 'CUSTOMER')";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getAddress());

            return ps.executeUpdate() > 0;
        }
    }

    /** Saves the changes made by the customer on the My Profile screen. */
    public boolean updateProfile(User user) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, phone = ?, address = ?, password = ? "
                   + "WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getPhone());
            ps.setString(3, user.getAddress());
            ps.setString(4, user.getPassword());
            ps.setInt(5, user.getUserId());

            return ps.executeUpdate() > 0;
        }
    }

    /** Copies one row of the result set into a User object. */
    private User buildUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setPhone(rs.getString("phone"));
        user.setAddress(rs.getString("address"));
        user.setRole(rs.getString("role"));
        return user;
    }
}
