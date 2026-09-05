package com.foodordering.util;

/**
 * Small helper methods used by the forms to check what the user typed
 * before the value is sent to the database.
 */
public class Validator {

    /** Largest quantity of one dish that can be ordered at a time. */
    public static final int MAX_QUANTITY = 100;

    /** Largest price that can be given to a dish, in rupees. */
    public static final double MAX_PRICE = 99999.99;

    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    /** Accepts a normal email such as name@example.com */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) {
            return false;
        }
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /** Mobile number must be exactly 10 digits. */
    public static boolean isValidPhone(String phone) {
        return !isEmpty(phone) && phone.matches("\\d{10}");
    }

    /**
     * Price must be a number greater than zero and not larger than MAX_PRICE.
     * The upper limit stops a very large value from breaking the DECIMAL(8,2)
     * column in the food_items table.
     */
    public static boolean isValidPrice(String price) {
        try {
            double value = Double.parseDouble(price.trim());
            return value > 0 && value <= MAX_PRICE;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Quantity must be a whole number from 1 to MAX_QUANTITY. The upper limit
     * keeps the order total sensible and stops a huge number from breaking the
     * total_amount column.
     */
    public static boolean isValidQuantity(String quantity) {
        try {
            int value = Integer.parseInt(quantity.trim());
            return value > 0 && value <= MAX_QUANTITY;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
