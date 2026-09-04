package com.foodordering.model;

/**
 * Holds one row of the food_items table.
 * categoryName is not a column, it is filled by a join in FoodItemDAO
 * so that the menu table can show the category name instead of its id.
 */
public class FoodItem {

    private int foodId;
    private String foodName;
    private String description;
    private int categoryId;
    private String categoryName;
    private double price;
    private String available;

    public FoodItem() {
    }

    public int getFoodId() { return foodId; }
    public void setFoodId(int foodId) { this.foodId = foodId; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getAvailable() { return available; }
    public void setAvailable(String available) { this.available = available; }

    public boolean isAvailable() {
        return "Yes".equalsIgnoreCase(available);
    }
}
