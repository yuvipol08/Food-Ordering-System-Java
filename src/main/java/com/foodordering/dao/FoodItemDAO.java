package com.foodordering.dao;

import com.foodordering.db.DBConnection;
import com.foodordering.model.FoodItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * All database work of the food_items table.
 * The select queries join the categories table so that the screens
 * can display the category name along with every food item.
 */
public class FoodItemDAO {

    private static final String SELECT_BASE =
            "SELECT f.*, c.category_name FROM food_items f "
          + "JOIN categories c ON f.category_id = c.category_id ";

    /** Complete menu, used by the admin Manage Food Items screen. */
    public List<FoodItem> getAllFoodItems() throws SQLException {
        return runQuery(SELECT_BASE + "ORDER BY f.food_id", null, null);
    }

    /** Only the items marked available, used by the customer menu screen. */
    public List<FoodItem> getAvailableFoodItems() throws SQLException {
        return runQuery(SELECT_BASE + "WHERE f.available = 'Yes' ORDER BY f.food_id", null, null);
    }

    /** Available items of one category, used when the customer filters the menu. */
    public List<FoodItem> getAvailableByCategory(int categoryId) throws SQLException {
        return runQuery(SELECT_BASE + "WHERE f.available = 'Yes' AND f.category_id = ? "
                        + "ORDER BY f.food_id", categoryId, null);
    }

    /** Search by a part of the food name, used by the search box on the menu. */
    public List<FoodItem> searchAvailableByName(String keyword) throws SQLException {
        return runQuery(SELECT_BASE + "WHERE f.available = 'Yes' AND f.food_name LIKE ? "
                        + "ORDER BY f.food_id", null, "%" + keyword + "%");
    }

    public boolean addFoodItem(FoodItem food) throws SQLException {
        String sql = "INSERT INTO food_items (food_name, description, category_id, price, available) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, food.getFoodName());
            ps.setString(2, food.getDescription());
            ps.setInt(3, food.getCategoryId());
            ps.setDouble(4, food.getPrice());
            ps.setString(5, food.getAvailable());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateFoodItem(FoodItem food) throws SQLException {
        String sql = "UPDATE food_items SET food_name = ?, description = ?, category_id = ?, "
                   + "price = ?, available = ? WHERE food_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, food.getFoodName());
            ps.setString(2, food.getDescription());
            ps.setInt(3, food.getCategoryId());
            ps.setDouble(4, food.getPrice());
            ps.setString(5, food.getAvailable());
            ps.setInt(6, food.getFoodId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteFoodItem(int foodId) throws SQLException {
        String sql = "DELETE FROM food_items WHERE food_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, foodId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * A food item that already appears in an old order must not be deleted,
     * because order_items has a foreign key pointing to it. The admin screen
     * checks this first and suggests marking the item as not available.
     */
    public int countOrderItems(int foodId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM order_items WHERE food_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, foodId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Runs one of the select queries above. Only one of intParam or
     * stringParam is used, the other one is passed as null.
     */
    private List<FoodItem> runQuery(String sql, Integer intParam, String stringParam)
            throws SQLException {

        List<FoodItem> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (intParam != null) {
                ps.setInt(1, intParam);
            } else if (stringParam != null) {
                ps.setString(1, stringParam);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FoodItem food = new FoodItem();
                    food.setFoodId(rs.getInt("food_id"));
                    food.setFoodName(rs.getString("food_name"));
                    food.setDescription(rs.getString("description"));
                    food.setCategoryId(rs.getInt("category_id"));
                    food.setCategoryName(rs.getString("category_name"));
                    food.setPrice(rs.getDouble("price"));
                    food.setAvailable(rs.getString("available"));
                    list.add(food);
                }
            }
        }
        return list;
    }
}
