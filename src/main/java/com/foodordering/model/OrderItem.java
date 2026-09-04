package com.foodordering.model;

/**
 * Holds one row of the order_items table.
 * The price column stores the rate at the time the order was placed,
 * so an old bill stays correct even if the menu price changes later.
 */
public class OrderItem {

    private int orderItemId;
    private int orderId;
    private int foodId;
    private String foodName;
    private int quantity;
    private double price;

    public OrderItem() {
    }

    public OrderItem(int foodId, String foodName, int quantity, double price) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.quantity = quantity;
        this.price = price;
    }

    public int getOrderItemId() { return orderItemId; }
    public void setOrderItemId(int orderItemId) { this.orderItemId = orderItemId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getFoodId() { return foodId; }
    public void setFoodId(int foodId) { this.foodId = foodId; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getSubtotal() {
        return price * quantity;
    }
}
