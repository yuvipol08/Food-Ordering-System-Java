# Food Ordering System — Project Flow

A one-page picture of how the whole project works.
Print this page and keep it with you during the demonstration.

---

## 1. The main flow

```
                         USER
                          |
                          v
                        LOGIN
              (LoginFrame + UserDAO)
                          |
                          v
                     ROLE CHECK
            (the role column of the users table)
                          |
             +------------+------------+
             |                         |
             v                         v
          ADMIN                    CUSTOMER
    (AdminDashboard)         (CustomerDashboard)
```

---

## 2. The customer side

```
        CUSTOMER
            |
            v
     REGISTER  (only the first time)
     RegisterFrame -> UserDAO.register() -> users table
            |
            v
          LOGIN
            |
            v
           MENU
     MenuPanel -> FoodItemDAO -> food_items table
     (shows only the dishes with available = 'Yes')
     (can filter by category or search by name)
            |
            v
           CART
     Cart + CartItem  (kept in memory, not in the database)
     add item / change quantity / remove item
     subtotal = rate x quantity
     total    = sum of all subtotals
            |
            v
          ORDER
     CartPanel -> OrderDAO.placeOrder()
            |
            +---> orders table        (1 row : number, date, customer, total, status)
            +---> order_items table   (1 row for every dish)
            both inside ONE transaction
            |
            v
           BILL
     BillDialog -> shows order number, items, quantities, rates, total
            |
            v
      ORDER HISTORY
     OrderHistoryPanel -> OrderDAO.getOrdersByUser() -> orders table
     (the customer sees only their own orders and the current status)
```

---

## 3. The admin side

```
          ADMIN
            |
            v
        DASHBOARD
      (AdminDashboard)
            |
            v
        CATEGORY
     CategoryPanel -> CategoryDAO -> categories table
     Add / View / Update / Delete
     (a category with dishes in it cannot be deleted)
            |
            v
          FOOD
     FoodItemPanel -> FoodItemDAO -> food_items table
     Add / View / Update / Delete
     set Available = Yes or No
            |
            v
         ORDERS
     AdminOrderPanel -> OrderDAO -> orders + users tables
     see every order with the customer name
     filter by status
     View Order Details -> BillDialog
            |
            v
     UPDATE STATUS
     OrderDAO.updateOrderStatus()
     Pending -> Confirmed -> Preparing -> Ready -> Delivered
            |
            v
     the customer sees the new status in My Orders
```

---

## 4. Where each technology fits

```
+---------------------------------------------------------------+
|  WHAT YOU SEE ON THE SCREEN                                    |
|  JAVA SWING                                                    |
|  windows, buttons, text boxes, tables, drop downs, messages    |
|  LoginFrame, AdminDashboard, CustomerDashboard, MenuPanel,     |
|  CartPanel, BillDialog, ...                                    |
+---------------------------------------------------------------+
                          |
                          |  the button click calls a DAO method
                          v
+---------------------------------------------------------------+
|  THE RULES AND THE DATA                                        |
|  JAVA                                                          |
|  DAO classes hold the SQL : UserDAO, CategoryDAO,              |
|                             FoodItemDAO, OrderDAO              |
|  Model classes carry the data : User, Category, FoodItem,      |
|                             Cart, CartItem, Order, OrderItem   |
|  Validator checks what the user typed                          |
+---------------------------------------------------------------+
                          |
                          |  DBConnection.getConnection()
                          v
+---------------------------------------------------------------+
|  THE BRIDGE                                                    |
|  JDBC                                                          |
|  Connection  ->  PreparedStatement  ->  ResultSet              |
|  executeQuery()  for SELECT                                    |
|  executeUpdate() for INSERT / UPDATE / DELETE                  |
|  DBConnection is the only class that knows the URL,            |
|  the user name and the password                                |
+---------------------------------------------------------------+
                          |
                          |  SQL over the MySQL driver
                          v
+---------------------------------------------------------------+
|  WHERE THE DATA LIVES                                          |
|  MySQL   database : food_ordering_db                           |
|                                                                |
|  users  |  categories  |  food_items  |  orders  |  order_items|
+---------------------------------------------------------------+
```

**One sentence:** *Swing shows the screens, Java holds the logic, JDBC is the
bridge, and MySQL stores the data.*

---

## 5. The five tables and how they join

```
   users                             categories
   +-----------+                     +-------------+
   | user_id   |<--+                 | category_id |<--+
   | full_name |   |                 | category_.. |   |
   | email     |   |                 | description |   |
   | password  |   |                 +-------------+   |
   | phone     |   |                                   |
   | address   |   |                 food_items        |
   | role      |   |                 +-------------+   |
   +-----------+   |                 | food_id     |<--|--+
                   |                 | food_name   |   |  |
   orders          |                 | description |   |  |
   +--------------+|                 | category_id |---+  |
   | order_id     ||                 | price       |      |
   | order_number ||                 | available   |      |
   | user_id      |+                 +-------------+      |
   | order_date   |                                       |
   | total_amount |        order_items                    |
   | payment_mode |        +---------------+              |
   | status       |<-------| order_item_id |              |
   +--------------+        | order_id      |              |
          ^                | food_id       |--------------+
          |                | quantity      |
          +----------------| price         |
                           +---------------+
```

| Relationship | Meaning |
|---|---|
| `users` 1 → N `orders` | one customer places many orders |
| `orders` 1 → N `order_items` | one order contains many dishes |
| `categories` 1 → N `food_items` | one category has many dishes |
| `food_items` 1 → N `order_items` | one dish appears in many orders |

---

## 6. The order journey in one line

```
Menu  ->  Cart (memory)  ->  Place Order  ->  orders + order_items  ->  Bill  ->  History
```

And on the admin side:

```
Pending  ->  Confirmed  ->  Preparing  ->  Ready  ->  Delivered
```
