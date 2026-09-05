package com.foodordering.dao;

import com.foodordering.db.DBConnection;
import com.foodordering.model.Cart;
import com.foodordering.model.CartItem;
import com.foodordering.model.Order;
import com.foodordering.model.OrderItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * All database work of the orders and order_items tables.
 * Placing an order writes to both tables, so it is done inside
 * one transaction.
 */
public class OrderDAO {

    /** The five stages an order can pass through. */
    public static final String[] ORDER_STATUS = {
            "Pending", "Confirmed", "Preparing", "Ready", "Delivered"
    };

    /**
     * How many order numbers are tried before giving up. The suffix stays
     * two digits, so the order number never grows longer than the 20
     * characters allowed by the order_number column.
     */
    private static final int MAX_NUMBER_ATTEMPTS = 100;

    /** MySQL error number for a duplicate value in a UNIQUE column. */
    private static final int DUPLICATE_ENTRY = 1062;

    /**
     * Saves the cart as a new order.
     *
     * One row is inserted into orders and one row into order_items for
     * every line of the cart. Auto commit is switched off so that if any
     * insert fails the whole order is rolled back and no half saved order
     * is left in the database.
     *
     * Returns the saved Order object (with its order number) or null on failure.
     */
    public Order placeOrder(int userId, Cart cart, String paymentMode) throws SQLException {

        if (cart == null || cart.isEmpty()) {
            throw new SQLException("The cart is empty, so no order can be placed.");
        }

        String orderSql = "INSERT INTO orders (order_number, user_id, order_date, total_amount, "
                        + "payment_mode, status) VALUES (?, ?, ?, ?, ?, 'Pending')";
        String itemSql  = "INSERT INTO order_items (order_id, food_id, quantity, price) "
                        + "VALUES (?, ?, ?, ?)";

        Connection con = null;

        try {
            con = DBConnection.getConnection();
            checkAllItemsAvailable(con, cart);
            con.setAutoCommit(false);

            Date now = new Date();
            double total = cart.getTotalAmount();

            String orderNumber = null;
            int orderId = 0;

            // Step 1 : insert the order and read back the generated order_id.
            // The order number is made from the date and time up to the second, so two
            // orders placed in the same second would get the same number. The UNIQUE key
            // on order_number stops the second one, and the loop below simply tries again
            // with a numbered order such as ORD20260904180849-1.
            for (int attempt = 0; attempt < MAX_NUMBER_ATTEMPTS; attempt++) {

                orderNumber = generateOrderNumber(attempt);

                try (PreparedStatement ps =
                             con.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, orderNumber);
                    ps.setInt(2, userId);
                    ps.setTimestamp(3, new Timestamp(now.getTime()));
                    ps.setDouble(4, total);
                    ps.setString(5, paymentMode);
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            con.rollback();
                            return null;
                        }
                        orderId = keys.getInt(1);
                    }
                    break;

                } catch (SQLIntegrityConstraintViolationException duplicate) {
                    // Only a duplicate order number is worth trying again. Any other
                    // integrity error, such as a user_id that does not exist, is a real
                    // problem and is thrown out immediately.
                    if (duplicate.getErrorCode() != DUPLICATE_ENTRY
                            || attempt == MAX_NUMBER_ATTEMPTS - 1) {
                        throw duplicate;
                    }
                }
            }

            // Step 2 : insert every cart line into order_items
            try (PreparedStatement ps = con.prepareStatement(itemSql)) {
                for (CartItem item : cart.getItems()) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, item.getFoodItem().getFoodId());
                    ps.setInt(3, item.getQuantity());
                    ps.setDouble(4, item.getFoodItem().getPrice());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            con.commit();

            // Build the object that the bill screen will display
            Order order = new Order();
            order.setOrderId(orderId);
            order.setOrderNumber(orderNumber);
            order.setUserId(userId);
            order.setOrderDate(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(now));
            order.setTotalAmount(total);
            order.setPaymentMode(paymentMode);
            order.setStatus("Pending");

            for (CartItem item : cart.getItems()) {
                order.getOrderItems().add(new OrderItem(
                        item.getFoodItem().getFoodId(),
                        item.getFoodItem().getFoodName(),
                        item.getQuantity(),
                        item.getFoodItem().getPrice()));
            }
            return order;

        } catch (SQLException e) {
            if (con != null) {
                con.rollback();
            }
            throw e;
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }

    /**
     * Checks that every dish in the cart is still marked available. The admin
     * may have marked a dish as not available while it was lying in the cart,
     * and such a dish must not be ordered.
     */
    private void checkAllItemsAvailable(Connection con, Cart cart) throws SQLException {
        String sql = "SELECT food_id FROM food_items WHERE food_id = ? AND available = 'Yes'";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (CartItem item : cart.getItems()) {
                ps.setInt(1, item.getFoodItem().getFoodId());

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("'" + item.getFoodItem().getFoodName()
                                + "' is not available any more.\n"
                                + "Please remove it from the cart and try again.");
                    }
                }
            }
        }
    }

    /**
     * Makes the order number, for example ORD20260904153012.
     * The date and the time up to the second are used. If that number is already
     * taken because another order was placed in the same second, placeOrder calls
     * this method again with the next attempt number and gets ORD20260904153012-1.
     */
    private String generateOrderNumber(int attempt) {
        String number = "ORD" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        return attempt == 0 ? number : number + "-" + attempt;
    }

    /** Orders of one customer, newest first. Used by the Order History screen. */
    public List<Order> getOrdersByUser(int userId) throws SQLException {
        String sql = "SELECT o.*, u.full_name FROM orders o "
                   + "JOIN users u ON o.user_id = u.user_id "
                   + "WHERE o.user_id = ? ORDER BY o.order_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return buildOrderList(rs);
            }
        }
    }

    /** All orders of all customers, used by the admin Manage Orders screen. */
    public List<Order> getAllOrders() throws SQLException {
        String sql = "SELECT o.*, u.full_name FROM orders o "
                   + "JOIN users u ON o.user_id = u.user_id ORDER BY o.order_id DESC";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            return buildOrderList(rs);
        }
    }

    /** Orders having one particular status, used by the admin status filter. */
    public List<Order> getOrdersByStatus(String status) throws SQLException {
        String sql = "SELECT o.*, u.full_name FROM orders o "
                   + "JOIN users u ON o.user_id = u.user_id "
                   + "WHERE o.status = ? ORDER BY o.order_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                return buildOrderList(rs);
            }
        }
    }

    /**
     * Reads the food items of one order. Used when a bill is reprinted
     * from the Order History screen or viewed by the admin.
     */
    public List<OrderItem> getOrderItems(int orderId) throws SQLException {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT oi.*, f.food_name FROM order_items oi "
                   + "JOIN food_items f ON oi.food_id = f.food_id "
                   + "WHERE oi.order_id = ? ORDER BY oi.order_item_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setOrderItemId(rs.getInt("order_item_id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setFoodId(rs.getInt("food_id"));
                    item.setFoodName(rs.getString("food_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPrice(rs.getDouble("price"));
                    list.add(item);
                }
            }
        }
        return list;
    }

    /** Changes the status of an order, for example Pending to Confirmed. */
    public boolean updateOrderStatus(int orderId, String status) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Copies the rows of the result set into a list of Order objects. */
    private List<Order> buildOrderList(ResultSet rs) throws SQLException {
        List<Order> list = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        while (rs.next()) {
            Order order = new Order();
            order.setOrderId(rs.getInt("order_id"));
            order.setOrderNumber(rs.getString("order_number"));
            order.setUserId(rs.getInt("user_id"));
            order.setCustomerName(rs.getString("full_name"));
            order.setOrderDate(format.format(rs.getTimestamp("order_date")));
            order.setTotalAmount(rs.getDouble("total_amount"));
            order.setPaymentMode(rs.getString("payment_mode"));
            order.setStatus(rs.getString("status"));
            list.add(order);
        }
        return list;
    }
}
