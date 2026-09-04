package com.foodordering.model;

/**
 * One line of the shopping cart: a food item plus the quantity
 * the customer selected. The cart only lives in memory, it is
 * written to the database when the order is placed.
 */
public class CartItem {

    private final FoodItem foodItem;
    private int quantity;

    public CartItem(FoodItem foodItem, int quantity) {
        this.foodItem = foodItem;
        this.quantity = quantity;
    }

    public FoodItem getFoodItem() { return foodItem; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    /** Price of this line = rate of the food item x quantity. */
    public double getSubtotal() {
        return foodItem.getPrice() * quantity;
    }
}
