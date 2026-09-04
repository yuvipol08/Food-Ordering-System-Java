package com.foodordering.util;

/**
 * Small helper methods used by the forms to check what the user typed
 * before the value is sent to the database.
 */
public class Validator {

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

    /** Price must be a number greater than zero. */
    public static boolean isValidPrice(String price) {
        try {
            return Double.parseDouble(price.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Quantity must be a whole number greater than zero. */
    public static boolean isValidQuantity(String quantity) {
        try {
            return Integer.parseInt(quantity.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
