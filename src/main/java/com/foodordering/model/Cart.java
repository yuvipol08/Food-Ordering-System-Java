package com.foodordering.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The shopping cart of the logged in customer.
 * It keeps a list of CartItem objects and gives the total amount.
 */
public class Cart {

    private final List<CartItem> items = new ArrayList<>();

    /**
     * Adds a food item to the cart. If the same item is already present
     * the quantity is increased instead of adding a duplicate row.
     */
    public void addItem(FoodItem food, int quantity) {
        for (CartItem item : items) {
            if (item.getFoodItem().getFoodId() == food.getFoodId()) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        items.add(new CartItem(food, quantity));
    }

    public void updateQuantity(int index, int quantity) {
        items.get(index).setQuantity(quantity);
    }

    public void removeItem(int index) {
        items.remove(index);
    }

    public void clear() {
        items.clear();
    }

    public List<CartItem> getItems() {
        return items;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    /** Adds the subtotal of every line to get the bill amount. */
    public double getTotalAmount() {
        double total = 0;
        for (CartItem item : items) {
            total = total + item.getSubtotal();
        }
        return total;
    }
}
