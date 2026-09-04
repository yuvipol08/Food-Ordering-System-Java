-- ============================================================
--  Food Ordering System
--  Database script for MySQL
--
--  How to run:
--     mysql -u root -p < food_ordering_db.sql
--  or open this file in MySQL Workbench and execute it.
-- ============================================================

DROP DATABASE IF EXISTS food_ordering_db;
CREATE DATABASE food_ordering_db;
USE food_ordering_db;

-- ------------------------------------------------------------
-- Table 1 : users
-- Stores login details of the admin and of every registered
-- customer. The role column decides which dashboard opens
-- after a successful login.
-- ------------------------------------------------------------
CREATE TABLE users (
    user_id    INT AUTO_INCREMENT,
    full_name  VARCHAR(100) NOT NULL,
    email      VARCHAR(100) NOT NULL,
    password   VARCHAR(50)  NOT NULL,
    phone      VARCHAR(15),
    address    VARCHAR(255),
    role       VARCHAR(10)  NOT NULL DEFAULT 'CUSTOMER',
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_users_email (email)
);

-- ------------------------------------------------------------
-- Table 2 : categories
-- Stores the food categories such as Starters, Main Course,
-- Beverages and Desserts.
-- ------------------------------------------------------------
CREATE TABLE categories (
    category_id   INT AUTO_INCREMENT,
    category_name VARCHAR(50) NOT NULL,
    description   VARCHAR(255),
    PRIMARY KEY (category_id),
    UNIQUE KEY uk_categories_name (category_name)
);

-- ------------------------------------------------------------
-- Table 3 : food_items
-- Stores the menu. Every food item belongs to one category,
-- so category_id is a foreign key to the categories table.
-- ------------------------------------------------------------
CREATE TABLE food_items (
    food_id     INT AUTO_INCREMENT,
    food_name   VARCHAR(100)  NOT NULL,
    description VARCHAR(255),
    category_id INT           NOT NULL,
    price       DECIMAL(8,2)  NOT NULL,
    available   VARCHAR(3)    NOT NULL DEFAULT 'Yes',
    PRIMARY KEY (food_id),
    CONSTRAINT fk_food_category FOREIGN KEY (category_id)
        REFERENCES categories (category_id)
);

-- ------------------------------------------------------------
-- Table 4 : orders
-- One row for every order placed by a customer. It keeps the
-- generated order number, the date, the total amount and the
-- current status of the order.
-- ------------------------------------------------------------
CREATE TABLE orders (
    order_id     INT AUTO_INCREMENT,
    order_number VARCHAR(20)   NOT NULL,
    user_id      INT           NOT NULL,
    order_date   DATETIME      NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    payment_mode VARCHAR(25)   NOT NULL DEFAULT 'Cash on Delivery',
    status       VARCHAR(15)   NOT NULL DEFAULT 'Pending',
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_orders_number (order_number),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id)
        REFERENCES users (user_id)
);

-- ------------------------------------------------------------
-- Table 5 : order_items
-- Stores the individual food items of an order. One order can
-- contain many rows here, so it has a foreign key to orders
-- and a foreign key to food_items.
-- ------------------------------------------------------------
CREATE TABLE order_items (
    order_item_id INT AUTO_INCREMENT,
    order_id      INT          NOT NULL,
    food_id       INT          NOT NULL,
    quantity      INT          NOT NULL,
    price         DECIMAL(8,2) NOT NULL,
    PRIMARY KEY (order_item_id),
    CONSTRAINT fk_item_order FOREIGN KEY (order_id)
        REFERENCES orders (order_id),
    CONSTRAINT fk_item_food FOREIGN KEY (food_id)
        REFERENCES food_items (food_id)
);

-- ============================================================
--  Sample data
-- ============================================================

-- Default admin account (used to log in as administrator)
INSERT INTO users (full_name, email, password, phone, address, role) VALUES
('System Administrator', 'admin@food.com', 'admin123', '9876543210', 'Pune', 'ADMIN');

-- A demo customer so the menu and order screens can be checked quickly
INSERT INTO users (full_name, email, password, phone, address, role) VALUES
('Demo Customer', 'customer@food.com', 'cust123', '9812345678', 'Nasarapur, Pune', 'CUSTOMER');

INSERT INTO categories (category_name, description) VALUES
('Starters',    'Soups, snacks and small dishes served before the meal'),
('Main Course', 'Rice, roti and curry items'),
('Beverages',   'Cold drinks, tea, coffee and juices'),
('Desserts',    'Sweet dishes served after the meal');

INSERT INTO food_items (food_name, description, category_id, price, available) VALUES
('Veg Manchurian',  'Fried vegetable balls in spicy sauce',   1, 120.00, 'Yes'),
('Paneer Tikka',    'Grilled paneer cubes with masala',       1, 160.00, 'Yes'),
('Tomato Soup',     'Hot tomato soup served with bread',      1,  70.00, 'Yes'),
('Paneer Butter Masala', 'Paneer cooked in butter gravy',     2, 220.00, 'Yes'),
('Veg Biryani',     'Basmati rice cooked with vegetables',    2, 180.00, 'Yes'),
('Dal Tadka',       'Yellow dal with garlic tempering',       2, 140.00, 'Yes'),
('Butter Roti',     'Tandoori roti with butter',              2,  25.00, 'Yes'),
('Cold Coffee',     'Chilled coffee with milk',               3,  80.00, 'Yes'),
('Masala Chai',     'Indian tea with ginger and spices',      3,  25.00, 'Yes'),
('Fresh Lime Soda', 'Soda with lemon, sweet or salted',       3,  60.00, 'No'),
('Gulab Jamun',     'Two pieces of hot gulab jamun',          4,  60.00, 'Yes'),
('Ice Cream',       'Vanilla or butterscotch scoop',          4,  70.00, 'Yes');
