package com.foodordering.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds one row of the orders table together with its order items.
 * customerName is filled by a join in OrderDAO so that the admin
 * order list can show who placed the order.
 */
public class Order {

    private int orderId;
    private String orderNumber;
    private int userId;
    private String customerName;
    private String orderDate;
    private double totalAmount;
    private String paymentMode;
    private String status;

    private List<OrderItem> orderItems = new ArrayList<>();

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
}
