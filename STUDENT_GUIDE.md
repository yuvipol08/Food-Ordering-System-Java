# Food Ordering System — Student Guide

This guide is written for **you**, not for the teacher. It explains the project in
plain language so that you can understand it, explain it, and answer questions
about it confidently.

Read section 1, then 9, 10, 11 and 12. Those five sections alone will let you
explain the whole project. Everything else is extra detail and viva practice.

---

## 1. What is this project?

The Food Ordering System is a **desktop application for a restaurant**. It is
written in **Java**, the screens are made with **Java Swing**, and all the data is
kept in a **MySQL** database. Java talks to MySQL through **JDBC**.

There are two kinds of users:

- The **customer** registers, logs in, looks at the food menu, puts dishes in a
  cart, places an order and gets a bill.
- The **admin** (the restaurant owner) manages the food categories and the menu,
  sees all the orders and changes the status of an order.

### 30-second explanation (memorise this one)

> "My project is a Food Ordering System. It is a desktop application made in Java
> using Swing for the screens and MySQL for the database, connected through JDBC.
> A customer can register, log in, see the menu, add food to a cart and place an
> order, and the system prepares the bill. The admin can add and edit food
> categories and food items, see all the orders and change the order status from
> Pending to Delivered."

### 1-minute explanation

> "My project is a Food Ordering System built in Java. The user interface is made
> with Java Swing and the data is stored in a MySQL database. Java connects to
> MySQL using JDBC.
>
> There are two types of users, and the login screen decides which one you are
> from the `role` column in the `users` table. If the role is ADMIN the admin
> dashboard opens, and if it is CUSTOMER the customer dashboard opens.
>
> A customer registers once, then logs in, browses the menu, can filter it by
> category or search a dish by name, adds dishes to a cart with a quantity, and
> places the order. The program calculates the total automatically and shows a
> bill with the order number, the items, the quantities and the final amount.
>
> The admin manages the categories and the food items, can mark a dish as not
> available, sees the orders of all customers, and moves an order through
> Pending, Confirmed, Preparing, Ready and Delivered."

### 2-minute explanation

Say everything above, then add:

> "The database has five tables. `users` holds the admin and the customers,
> `categories` holds the food groups like Starters and Main Course, `food_items`
> holds the menu, `orders` holds one row for each order, and `order_items` holds
> the individual dishes of each order.
>
> The most important part of the project is placing an order, because it writes
> to two tables at the same time. The `OrderDAO.placeOrder()` method switches off
> auto commit, inserts one row into `orders`, reads back the generated
> `order_id`, then inserts one row into `order_items` for every line of the cart,
> and finally commits. If anything fails in between it calls rollback, so a half
> saved order is never left in the database.
>
> The code is divided into packages. The `ui` package has the Swing screens, the
> `dao` package has the classes that contain the SQL, the `model` package has
> simple classes that carry the data, and the `db` package has one class that
> creates the JDBC connection. The screens never contain SQL — they always call a
> DAO method. I tested the application with 47 test cases covering login,
> registration, category and food management, cart, order placement, billing,
> order history and admin order management."

---

## 2. What happens when the application starts?

```
You run MainApp
        |
        v
MainApp checks the database connection
   (DBConnection.testConnection())
        |
        +-- fails --> a message box explains what to check, program stops
        |
        v
LoginFrame opens  (the login window)
        |
        v
You type email and password, click Login
        |
        v
LoginFrame checks the boxes are not empty and the email looks correct
   (Validator.isEmpty, Validator.isValidEmail)
        |
        v
LoginFrame calls UserDAO.login(email, password)
        |
        v
UserDAO asks DBConnection for a connection
        |
        v
SELECT * FROM users WHERE email = ? AND password = ?
        |
        v
      MySQL
        |
        +-- no row found --> UserDAO returns null
        |                    --> "Invalid email or password" message
        |
        v
   a row is found --> UserDAO returns a User object
        |
        v
LoginFrame looks at user.getRole()
        |
        +-- "ADMIN"    --> AdminDashboard opens
        +-- "CUSTOMER" --> CustomerDashboard opens
```

**One sentence to remember:** *MainApp checks the database, opens LoginFrame,
and the role in the users table decides which dashboard opens.*

---

## 3. What technologies did we use?

### Java

- **What is it?** A programming language. Programs written in Java run on the
  Java Virtual Machine, so the same program works on Windows and on Linux.
- **Why did we use it?** It is taught in our course, it already has a library for
  making windows (Swing) and a standard way of talking to a database (JDBC), so
  no extra framework was needed.
- **Where is it in our project?** Everything except the database. All 27 classes
  are Java classes.

### Java Swing

- **What is it?** The standard Java library for making desktop windows, buttons,
  text boxes and tables. It is part of the JDK, nothing extra to install.
- **Why did we use it?** The project is a desktop application, and Swing is the
  simplest way to build desktop screens in Java.
- **Where is it in our project?** The whole `ui` package — `LoginFrame`,
  `AdminDashboard`, `CustomerDashboard`, `MenuPanel`, `CartPanel`, `BillDialog`
  and the rest.

### JDBC

- **What is it?** JDBC stands for Java Database Connectivity. It is the standard
  set of Java classes used to send SQL to a database and read the answer.
- **Why did we use it?** It is the normal way for a Java program to use MySQL,
  and it is simple — just `Connection`, `PreparedStatement` and `ResultSet`.
- **Where is it in our project?** `DBConnection` creates the connection, and all
  four DAO classes (`UserDAO`, `CategoryDAO`, `FoodItemDAO`, `OrderDAO`) use it.

### MySQL

- **What is it?** A database that stores data in tables made of rows and columns,
  and understands the SQL language.
- **Why did we use it?** It is free, it is already installed in our college lab,
  and it supports primary keys and foreign keys which our design needs.
- **Where is it in our project?** The database `food_ordering_db` with its five
  tables. The script `database/food_ordering_db.sql` creates it.

### Maven

- **What is it?** A build tool. It downloads the libraries a project needs and
  compiles the project into a single runnable `.jar` file.
- **Why did we use it?** So that the MySQL JDBC driver is downloaded
  automatically instead of being copied by hand, and so the project builds with
  one command.
- **Where is it in our project?** The `pom.xml` file. It lists one dependency,
  `mysql-connector-j`.

### Git and GitHub

- **What is it?** Git records the history of the project on your computer.
  GitHub keeps a copy online.
- **Why did we use it?** To keep a backup, to see what changed and when, and to
  be able to submit the project.
- **Where is it in our project?** The whole repository. The final code is on the
  `main` branch.

---

## 4. Project architecture

Our project uses a simple **three layer** design. Each layer talks only to the
one below it.

```
      PRESENTATION LAYER   (what you see)
      package com.foodordering.ui
                |
                |  calls DAO methods
                v
      BUSINESS LOGIC LAYER  (the rules and the data)
      packages com.foodordering.dao  and  com.foodordering.model
                |
                |  asks for a connection
                v
      DATA ACCESS LAYER
      package com.foodordering.db  +  MySQL JDBC driver
                |
                |  SQL
                v
            MySQL DATABASE
```

**The golden rule of our project: the screens never contain SQL.** A screen
always calls a DAO method, and only the DAO classes contain SQL statements.

### Package `com.foodordering` (1 class)

- **What is it?** The starting point.
- **`MainApp`** — has the `main` method. It checks the database connection and
  opens `LoginFrame`.

### Package `com.foodordering.db` (1 class)

- **What is it?** The one place that knows how to reach MySQL.
- **Why needed?** So the database address, user name and password are written in
  one file only. If the lab computer has a different MySQL password, you change
  this one file.
- **`DBConnection`** — `getConnection()` returns a JDBC `Connection`.
  `testConnection()` is used at startup to check MySQL is running.

### Package `com.foodordering.model` (7 classes)

- **What is it?** Simple classes that just carry data between the layers. They
  have private fields and public getters and setters, and almost no logic.
- **Why needed?** So that a whole row of a table can be passed around as one
  object instead of ten separate variables.

| Class | What it holds |
|---|---|
| `User` | one row of `users` — admin or customer, told apart by `role` |
| `Category` | one row of `categories` |
| `FoodItem` | one row of `food_items`, plus the category name from a join |
| `Cart` | the shopping cart — a list of `CartItem`, kept in memory only |
| `CartItem` | one line of the cart: a `FoodItem` plus a quantity |
| `Order` | one row of `orders`, plus its list of `OrderItem` |
| `OrderItem` | one row of `order_items` |

### Package `com.foodordering.dao` (4 classes)

- **What is it?** DAO means **Data Access Object**. These classes contain all the
  SQL of the project.
- **Why needed?** So all the SQL is in four files. If a query has to change, you
  know exactly where to look.

| Class | What it does |
|---|---|
| `UserDAO` | login, register, check duplicate email, update profile |
| `CategoryDAO` | add / view / update / delete categories, count dishes in a category |
| `FoodItemDAO` | add / view / update / delete dishes, filter by category, search by name |
| `OrderDAO` | place an order (the transaction), read orders, read order items, update status |

### Package `com.foodordering.ui` (12 classes)

- **What is it?** All the Swing screens.

| Class | What it is |
|---|---|
| `LoginFrame` | the login window |
| `RegisterFrame` | the customer registration window |
| `AdminDashboard` | the admin main window with the left menu |
| `CustomerDashboard` | the customer main window with the left menu |
| `CategoryPanel` | admin screen to manage categories |
| `FoodItemPanel` | admin screen to manage the menu |
| `AdminOrderPanel` | admin screen listing all orders, with the status update |
| `MenuPanel` | customer screen showing the menu, with search and filter |
| `CartPanel` | customer screen showing the cart and the Place Order button |
| `OrderHistoryPanel` | customer screen listing previous orders |
| `ProfilePanel` | customer screen to change name, mobile, address, password |
| `BillDialog` | the bill window |

### Package `com.foodordering.util` (2 classes)

| Class | What it does |
|---|---|
| `Validator` | small checks — is it empty, is the email valid, is the quantity valid |
| `UITheme` | the common colours, fonts and buttons so every screen looks the same, plus `money()` for amounts and `setSizeWithinScreen()` for window sizes |

**Total: 27 Java classes in 6 packages.**

---

## 5. Important Java concepts used in our project

Only the concepts we actually used are listed here.

### class

A class is a blueprint. `Category` is a class.

```java
public class Category {
    private int categoryId;
    private String categoryName;
}
```

### object

An object is one actual thing made from a class.

```java
Category c = new Category(1, "Starters", "Soups and snacks");
```
Here `c` is an object of the class `Category`.

### constructor

A special method with the same name as the class, used to create an object and
set its starting values.

```java
public Category(int categoryId, String categoryName, String description) {
    this.categoryId = categoryId;
    this.categoryName = categoryName;
    this.description = description;
}
```
`this.categoryId` means "the field of this object", and `categoryId` alone means
the value that was passed in.

### method

A block of code with a name that does one job.

```java
public double getTotalAmount() { ... }   // from Cart
```

### variable and data types

| Type | Meaning | Example in our project |
|---|---|---|
| `int` | a whole number | `private int quantity;` in `CartItem` |
| `double` | a number with decimals | `private double price;` in `FoodItem` |
| `String` | text | `private String foodName;` in `FoodItem` |
| `boolean` | true or false | `isAdmin()` returns a boolean |

### if / else

```java
if (user == null) {
    showError("Invalid email or password. Please try again.");
} else if (user.isAdmin()) {
    new AdminDashboard(user).setVisible(true);
} else {
    new CustomerDashboard(user).setVisible(true);
}
```

### loops

**for-each loop** — used in `Cart.getTotalAmount()`:

```java
double total = 0;
for (CartItem item : items) {
    total = total + item.getSubtotal();
}
return total;
```

**while loop** — used in the DAO classes to read every row of a result:

```java
while (rs.next()) {
    list.add(...);     // one object per row
}
```

### ArrayList

A list that can grow. We use it in `Cart`:

```java
private final List<CartItem> items = new ArrayList<>();
```
`List` is the type, `ArrayList` is the actual kind of list we create.

### exception handling (try / catch)

An exception is an error that happens while the program is running. If we do not
catch it, the program stops. We catch it and show a message instead.

```java
try {
    User user = userDAO.login(email, password);
    ...
} catch (SQLException ex) {
    showError("Database error : " + ex.getMessage());
}
```

### try-with-resources

A special `try` that closes things automatically. Anything opened inside the
round brackets is closed when the block ends, even if there is an error.

```java
try (Connection con = DBConnection.getConnection();
     PreparedStatement ps = con.prepareStatement(sql)) {
    ...
}   // con and ps are closed here automatically
```
This is why we never write `con.close()` in most of our DAO methods.

### inheritance

`extends` means "is a kind of". Our screens inherit from Swing classes.

```java
public class LoginFrame extends JFrame { ... }
public class CategoryPanel extends JPanel { ... }
public class BillDialog extends JDialog { ... }
```
`LoginFrame` gets everything a `JFrame` can do (a title bar, a close button, a
size) and we only add our own boxes and buttons.

### encapsulation

Fields are `private` and are read or changed only through public getter and
setter methods. Every model class does this.

```java
private double price;
public double getPrice() { return price; }
public void setPrice(double price) { this.price = price; }
```
This means no other class can put a wrong value straight into the field.

### static

`static` means the method or value belongs to the class, not to an object, so you
can use it without writing `new`.

```java
Validator.isValidEmail(email);          // no object needed
DBConnection.getConnection();
public static final String[] ORDER_STATUS = { "Pending", ... };   // in OrderDAO
```

### method overriding (@Override)

Changing what an inherited method does. We use it to make the tables read only:

```java
@Override
public boolean isCellEditable(int row, int column) {
    return false;
}
```
and in `Category` so a combo box shows the name instead of the object address:

```java
@Override
public String toString() {
    return categoryName;
}
```

### interfaces

We do not write `implements` anywhere in our code, but we do use interfaces:

- `List` and `Connection`, `PreparedStatement`, `ResultSet` are all interfaces.
- Button clicks use the `ActionListener` interface, written in the short lambda
  form:

```java
loginButton.addActionListener(e -> doLogin());
```
This means "when this button is clicked, run `doLogin()`".

### packages and import

A package is a folder that groups related classes.

```java
package com.foodordering.dao;          // this file belongs to the dao package
import com.foodordering.model.User;    // I want to use the User class
```

### generics (the angle brackets)

`List<CartItem>` means "a list that holds only `CartItem` objects". It stops you
putting the wrong kind of object in by mistake.

---

## 6. Java Swing explained simply

Swing gives us ready-made pieces. We just arrange them.

| Component | What it is | Where in our project |
|---|---|---|
| `JFrame` | a full window with a title bar | `LoginFrame`, `AdminDashboard`, `CustomerDashboard` |
| `JDialog` | a smaller window that opens on top of another | `RegisterFrame`, `BillDialog` |
| `JPanel` | an invisible box that holds other components | every screen inside the dashboards |
| `JLabel` | a piece of text on the screen | "Email :", "Total Amount : Rs. 880.00" |
| `JTextField` | a one line box where you type | the email box, the price box |
| `JPasswordField` | same but shows dots instead of letters | the password box |
| `JTextArea` | a box for several lines of text | the address box, the bill text |
| `JButton` | a clickable button | Login, Add, Update, Delete, Place Order |
| `JTable` | a grid of rows and columns | the menu list, the cart, the order list |
| `JComboBox` | a drop down list | the category list, the order status list |
| `JScrollPane` | adds scroll bars around a table or text area | around every `JTable` |
| `JOptionPane` | a small pop-up message box | all our messages and confirmations |

### DefaultTableModel

A `JTable` does not hold the data itself. The data sits in a **table model**. We
use `DefaultTableModel`:

```java
private final DefaultTableModel tableModel =
        new DefaultTableModel(new String[]{"Food ID", "Food Name", ...}, 0) {
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;      // the user cannot type into the table
    }
};
private final JTable table = new JTable(tableModel);
```
To fill it we call `tableModel.setRowCount(0)` to clear it and then
`tableModel.addRow(...)` once per record.

### Layouts (how components are arranged)

| Layout | What it does | Where we use it |
|---|---|---|
| `BorderLayout` | five areas: north, south, east, west, centre | the main windows — title on top, menu on the left, work area in the centre |
| `GridBagLayout` | a grid, keeps labels and boxes in straight columns | all the forms |
| `FlowLayout` | puts components in a row one after another | the button rows |
| `BoxLayout` | stacks components vertically | the left menu strip |
| `CardLayout` | keeps several panels on top of each other and shows one | the work area of both dashboards |

**CardLayout is worth remembering.** All the panels are added once, and clicking
a menu button just shows a different one:

```java
cardLayout.show(contentPanel, "CATEGORY");
```

### Events (ActionListener)

An event is something the user does. A button click is the event we use.

```java
JButton addButton = UITheme.createButton("Add", UITheme.SUCCESS);
addButton.addActionListener(e -> addCategory());
```
Read it as: "when Add is clicked, run the `addCategory()` method".

### JOptionPane

```java
// just information
JOptionPane.showMessageDialog(this, "Category added successfully.",
        "Success", JOptionPane.INFORMATION_MESSAGE);

// a yes / no question
int choice = JOptionPane.showConfirmDialog(this,
        "Delete this category ?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
if (choice == JOptionPane.YES_OPTION) { ... }
```

---

## 7. Database explained simply

### What is a database?

A database is an organised place to keep information so that it stays there after
the program closes. A **relational** database keeps the information in **tables**.
A table is like a spreadsheet: it has **columns** (the fields) and **rows** (the
records).

Our database is called **`food_ordering_db`** and it has **five tables**.

### Primary Key

A column whose value is different for every row, so it identifies the row
uniquely. In `users` the primary key is `user_id`. Ours are all
`AUTO_INCREMENT`, which means MySQL gives the next number automatically
(1, 2, 3, ...), so we never have to think about it.

### Foreign Key

A column that points at the primary key of another table. It is what joins the
tables together. In `orders`, `user_id` is a foreign key pointing at
`users.user_id`. It means: *this order belongs to that customer.*

A foreign key also **protects** the data. MySQL will refuse to save an order for a
customer who does not exist, and will refuse to delete a category that still has
dishes in it.

### CRUD

CRUD is just the four things you can do with data:

| Letter | Meaning | SQL | Example in our project |
|---|---|---|---|
| **C** | Create | `INSERT` | admin adds a food item |
| **R** | Read | `SELECT` | the menu is loaded |
| **U** | Update | `UPDATE` | admin changes the order status |
| **D** | Delete | `DELETE` | admin deletes a category |

### The five tables

#### 1. `users`

- **Why it exists:** to keep the login details of everybody.
- **Primary key:** `user_id`
- **Columns:** `user_id`, `full_name`, `email`, `password`, `phone`, `address`, `role`
- **Important:** `email` is **UNIQUE** so two people cannot register with the same
  email. `role` holds either `ADMIN` or `CUSTOMER` and decides which dashboard
  opens.
- **Relationship:** one user can have many rows in `orders`.

> Why one table for both? Because an admin and a customer need exactly the same
> columns. A second table would have been a copy with a different name. The
> `role` column is enough to tell them apart.

#### 2. `categories`

- **Why it exists:** to group the dishes — Starters, Main Course, Beverages, Desserts.
- **Primary key:** `category_id`
- **Columns:** `category_id`, `category_name`, `description`
- **Important:** `category_name` is UNIQUE.
- **Relationship:** one category has many rows in `food_items`.

#### 3. `food_items`

- **Why it exists:** this is the menu.
- **Primary key:** `food_id`
- **Columns:** `food_id`, `food_name`, `description`, `category_id`, `price`, `available`
- **Foreign key:** `category_id` → `categories.category_id`
- **Important:** `available` is `Yes` or `No`. Only the `Yes` dishes are shown to
  the customer. This lets the admin hide a dish that is finished **without
  deleting it**, so the old orders that contain it stay correct.

#### 4. `orders`

- **Why it exists:** one row for each order that is placed.
- **Primary key:** `order_id`
- **Columns:** `order_id`, `order_number`, `user_id`, `order_date`,
  `total_amount`, `payment_mode`, `status`
- **Foreign key:** `user_id` → `users.user_id`
- **Important:** `order_number` is UNIQUE — this is the number shown to the
  customer, like `ORD20260905023811`. `status` is one of Pending, Confirmed,
  Preparing, Ready, Delivered.

#### 5. `order_items`

- **Why it exists:** an order usually has several dishes, so each dish of an order
  is one row here.
- **Primary key:** `order_item_id`
- **Columns:** `order_item_id`, `order_id`, `food_id`, `quantity`, `price`
- **Foreign keys:** `order_id` → `orders.order_id`, `food_id` → `food_items.food_id`
- **Important:** the `price` column stores the rate **at the time of ordering**.
  If the admin changes the menu price next week, the old bill still shows the
  amount that was actually charged.

### The relationships in one picture

```
users ──(1 to many)──> orders ──(1 to many)──> order_items
                                                    ^
                                                    | (many to 1)
categories ──(1 to many)──> food_items ─────────────+
```

---

## 8. JDBC explained simply

```
   Java Application  (our DAO classes)
            |
            v
          JDBC        (Connection, PreparedStatement, ResultSet)
            |
            v
   MySQL JDBC Driver  (mysql-connector-j)
            |
            v
      MySQL Database
```

JDBC is a set of Java interfaces. The **driver** is the piece that actually knows
how to talk to MySQL. Maven downloads the driver for us.

### The four things you must know

**1. `Connection` — the open line to the database.**

```java
Connection con = DBConnection.getConnection();
```

**2. `PreparedStatement` — a SQL statement with `?` in place of the values.**

```java
String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
PreparedStatement ps = con.prepareStatement(sql);
ps.setString(1, email);       // the first ?
ps.setString(2, password);    // the second ?
```

**3. `ResultSet` — the rows that come back from a SELECT.**

```java
ResultSet rs = ps.executeQuery();
while (rs.next()) {            // move to the next row
    String name = rs.getString("full_name");
    double price = rs.getDouble("price");
}
```
`rs.next()` returns `true` while there is another row, so the `while` loop reads
every row.

**4. The two ways of running a statement**

| Method | Used for | Returns |
|---|---|---|
| `executeQuery()` | `SELECT` | a `ResultSet` (the rows) |
| `executeUpdate()` | `INSERT`, `UPDATE`, `DELETE` | an int — how many rows changed |

That is why our DAO methods often end with:

```java
return ps.executeUpdate() > 0;    // true if at least one row changed
```

### Why `PreparedStatement` and not a joined-up string?

Two reasons, and you should be able to say both:

1. **Safety.** The values go to MySQL as separate parameters, not as part of the
   SQL text. So if somebody types `' OR '1'='1` as a password it is treated as
   ordinary text and cannot change the meaning of the query. This is what
   prevents **SQL injection**.
2. **Speed.** MySQL prepares the statement once and can run it again with
   different values.

### Transactions (used when placing an order)

Normally every statement is saved immediately — this is called **auto commit**.
When we place an order we must write to **two** tables and we want both writes to
succeed together or neither of them to happen. So we switch auto commit off:

```java
con.setAutoCommit(false);   // nothing is saved yet
   ... insert into orders ...
   ... insert into order_items ...
con.commit();               // now save everything together
```
and if something goes wrong:

```java
con.rollback();             // undo everything since setAutoCommit(false)
```

**Say this in the viva:** *"An order is saved in a transaction because it writes
to two tables. If the second insert fails, rollback undoes the first one, so the
database never has an order without its items."*

---

## 9. Login flow

```
LoginFrame  (you type email and password, click Login)
        |
        v
doLogin()  reads the two boxes
        |
        v
Validator.isEmpty()      -- are the boxes filled?
Validator.isValidEmail() -- does the email look like name@example.com?
        |
        +-- something wrong --> JOptionPane message, stop here
        |
        v
userDAO.login(email, password)
        |
        v
DBConnection.getConnection()
        |
        v
SELECT * FROM users WHERE email = ? AND password = ?
        |
        v
      MySQL
        |
        v
ResultSet
        |
        +-- rs.next() is false --> return null
        |                          --> "Invalid email or password"
        v
   buildUser(rs)  copies the row into a User object
        |
        v
LoginFrame checks user.isAdmin()
        |
        +-- true  --> new AdminDashboard(user).setVisible(true)
        +-- false --> new CustomerDashboard(user).setVisible(true)
```

**Classes involved:** `LoginFrame` → `Validator` → `UserDAO` → `DBConnection` →
MySQL → `User` → `AdminDashboard` or `CustomerDashboard`.

---

## 10. Customer flow

```
RegisterFrame        --> UserDAO.register()      --> users table
        |
        v
LoginFrame           --> UserDAO.login()         --> users table
        |
        v
CustomerDashboard    (opens with MenuPanel showing)
        |
        v
MenuPanel            --> FoodItemDAO.getAvailableFoodItems()  --> food_items
        |                FoodItemDAO.getAvailableByCategory()
        |                FoodItemDAO.searchAvailableByName()
        v
Add to Cart          --> Cart.addItem(foodItem, quantity)   (memory only)
        |
        v
CartPanel            --> Cart.updateQuantity(), Cart.removeItem(),
        |                Cart.getTotalAmount()
        v
Place Order          --> OrderDAO.placeOrder(userId, cart, paymentMode)
        |                     --> orders  +  order_items    (one transaction)
        v
BillDialog           --> shows the bill built from the Order object
        |
        v
OrderHistoryPanel    --> OrderDAO.getOrdersByUser()  --> orders table
                         OrderDAO.getOrderItems()    --> order_items table
```

**Note the important detail:** the cart lives only in memory, inside one `Cart`
object created by `CustomerDashboard` and shared with `MenuPanel` and
`CartPanel`. That is why a dish added on the menu screen appears on the cart
screen. It is written to the database only when the order is placed.

---

## 11. Admin flow

```
LoginFrame  (role = ADMIN)
        |
        v
AdminDashboard   (left menu + CardLayout work area)
        |
        +--> CategoryPanel    --> CategoryDAO   --> categories table
        |       Add / View / Update / Delete
        |
        +--> FoodItemPanel    --> FoodItemDAO   --> food_items table
        |       Add / View / Update / Delete, and set Available = Yes or No
        |       (the category drop down is filled from CategoryDAO)
        |
        +--> AdminOrderPanel  --> OrderDAO      --> orders + users tables
                Show all orders / filter by status
                View Order Details --> BillDialog
                Update Status      --> OrderDAO.updateOrderStatus()
                                       Pending -> Confirmed -> Preparing
                                       -> Ready -> Delivered
```

---

## 12. Cart calculation

There are only two calculations in the whole project.

**1. One line of the cart** — in `CartItem`:

```java
public double getSubtotal() {
    return foodItem.getPrice() * quantity;
}
```

**2. The whole cart** — in `Cart`:

```java
public double getTotalAmount() {
    double total = 0;
    for (CartItem item : items) {
        total = total + item.getSubtotal();
    }
    return total;
}
```

### Worked example

| Dish | Rate | Quantity | Subtotal |
|---|---|---|---|
| Paneer Butter Masala | 220.00 | 2 | 220 × 2 = **440.00** |
| Veg Biryani | 180.00 | 1 | 180 × 1 = **180.00** |
| Butter Roti | 25.00 | 4 | 25 × 4 = **100.00** |
| Cold Coffee | 80.00 | 2 | 80 × 2 = **160.00** |
| | | **Total** | **880.00** |

440 + 180 + 100 + 160 = **Rs. 880.00**

This is exactly what the cart screen shows, what is saved in
`orders.total_amount`, and what the bill prints.

**One more thing worth saying:** if you add the same dish again, the cart does
**not** create a second line. `Cart.addItem()` looks through the existing lines
first and only increases the quantity.

---

## 13. Order placement — the most important part

### Why two tables?

An order has some details that appear **once** (the order number, the date, the
customer, the total) and some details that repeat **for every dish** (which dish,
how many, at what rate).

If we used only one table we would have to repeat the order number, the date and
the customer on every dish row — the same information stored again and again.
That is wasteful and easy to get wrong.

So:

- `orders` holds the **one-time** information — one row per order.
- `order_items` holds the **repeating** information — one row per dish.
- They are joined by `order_items.order_id`, a foreign key.

```
orders
+----------+-------------------+---------+--------------+
| order_id | order_number      | user_id | total_amount |
+----------+-------------------+---------+--------------+
|    3     | ORD20260905023811 |    2    |    880.00    |
+----------+-------------------+---------+--------------+
                    |
                    | one order  ->  many items
                    v
order_items
+---------------+----------+---------+----------+--------+
| order_item_id | order_id | food_id | quantity | price  |
+---------------+----------+---------+----------+--------+
|       6       |    3     |    4    |    2     | 220.00 |
|       7       |    3     |    5    |    1     | 180.00 |
|       8       |    3     |    7    |    4     |  25.00 |
|       9       |    3     |    8    |    2     |  80.00 |
+---------------+----------+---------+----------+--------+
```

### What `OrderDAO.placeOrder()` does, step by step

1. If the cart is empty, refuse straight away.
2. Get a connection, and check that **every dish in the cart is still available**
   (the admin may have marked one as not available while it was in the cart).
3. `con.setAutoCommit(false)` — start the transaction.
4. Make the order number from the date and time, for example `ORD20260905023811`.
5. `INSERT INTO orders ...` and read back the `order_id` that MySQL generated,
   using `Statement.RETURN_GENERATED_KEYS` and `ps.getGeneratedKeys()`.
6. `INSERT INTO order_items ...` once for every cart line, using `addBatch()` and
   `executeBatch()` so they are all sent together.
7. `con.commit()` — now everything is saved.
8. Build an `Order` object with the order number and the items, and return it so
   the bill can be shown.
9. If anything fails, the `catch` block calls `con.rollback()` and **nothing** is
   saved.
10. The `finally` block puts auto commit back on and closes the connection.

### The order number

```java
private String generateOrderNumber(int attempt) {
    String number = "ORD" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    return attempt == 0 ? number : number + "-" + attempt;
}
```

The number is the date and time down to the **second**. Two orders placed inside
the same second would get the same number, so `order_number` has a **UNIQUE** key
in the database. If MySQL refuses the number, the loop tries again and the next
order becomes `ORD20260905023811-1`. The loop allows up to 100 tries, which is far
more than a person can click in one second.

**If the teacher asks "what if two orders happen at the same time?"** — this is
your answer, and you can say you actually tested it by placing six orders in a
loop.

---

## 14. Important classes

### `MainApp`

- **Purpose:** the starting point of the program.
- **Important method:** `main(String[] args)`
- **Used by:** nobody — the JVM calls it.
- **Remember:** it checks the database first, then opens `LoginFrame`. If MySQL
  is not running it shows a clear message instead of crashing.

### `DBConnection`

- **Purpose:** the only class that knows the database URL, user name and password.
- **Important methods:** `getConnection()`, `testConnection()`
- **Used by:** all four DAO classes and `MainApp`.
- **Remember:** *"If the MySQL password is different on another computer, I only
  change this one file."*

### `Validator`

- **Purpose:** the small checks used by every form.
- **Important methods:** `isEmpty()`, `isValidEmail()`, `isValidPhone()`,
  `isValidPrice()`, `isValidQuantity()`
- **Used by:** `LoginFrame`, `RegisterFrame`, `MenuPanel`, `CartPanel`,
  `FoodItemPanel`, `CategoryPanel`, `ProfilePanel`.
- **Remember:** all the methods are `static`, so we call them as
  `Validator.isValidEmail(...)` without creating an object.

### `User`

- **Purpose:** holds one row of the `users` table.
- **Important method:** `isAdmin()` — returns true when `role` is `ADMIN`.
- **Used by:** `LoginFrame`, both dashboards, `UserDAO`, `ProfilePanel`.
- **Remember:** the same class is used for the admin and for customers; the
  `role` field tells them apart.

### `UserDAO`

- **Purpose:** all the SQL of the `users` table.
- **Important methods:** `login()`, `register()`, `emailExists()`, `updateProfile()`
- **Used by:** `LoginFrame`, `RegisterFrame`, `ProfilePanel`.
- **Remember:** `login()` returns a `User` object or `null`. `null` means the
  credentials were wrong.

### `Cart` and `CartItem`

- **Purpose:** the shopping cart, kept in memory only.
- **Important methods:** `addItem()`, `updateQuantity()`, `removeItem()`,
  `getTotalAmount()`, `isEmpty()`; `CartItem.getSubtotal()`
- **Used by:** `CustomerDashboard` creates it, `MenuPanel` and `CartPanel` share it.
- **Remember:** the cart is **not** a database table. It is written to the
  database only when the order is placed.

### `FoodItemDAO`

- **Purpose:** all the SQL of the `food_items` table.
- **Important methods:** `getAllFoodItems()` (admin), `getAvailableFoodItems()`
  (customer menu), `getAvailableByCategory()`, `searchAvailableByName()`,
  `addFoodItem()`, `updateFoodItem()`, `deleteFoodItem()`, `countOrderItems()`
- **Used by:** `FoodItemPanel`, `MenuPanel`.
- **Remember:** the SELECT queries **join** the `categories` table so the screen
  can show the category name instead of the category number.

### `OrderDAO`

- **Purpose:** all the SQL of `orders` and `order_items`. The most important DAO.
- **Important methods:** `placeOrder()` (the transaction), `getOrdersByUser()`,
  `getAllOrders()`, `getOrdersByStatus()`, `getOrderItems()`, `updateOrderStatus()`
- **Used by:** `CartPanel`, `OrderHistoryPanel`, `AdminOrderPanel`.
- **Remember:** `placeOrder()` is the transaction. This is the method the teacher
  is most likely to ask about.

### `LoginFrame`

- **Purpose:** the first screen.
- **Important method:** `doLogin()`
- **Remember:** it validates, calls `UserDAO.login()`, and the role decides which
  dashboard opens.

### `AdminDashboard` and `CustomerDashboard`

- **Purpose:** the main windows, each with a left menu and a work area.
- **Important method:** `showCard(...)` / the menu button listeners
- **Remember:** both use a **CardLayout**. All the panels are added once and the
  menu button just shows a different one.

### `BillDialog`

- **Purpose:** shows the bill.
- **Important method:** `buildBillText()`
- **Used by:** `CartPanel` (after an order), `OrderHistoryPanel` (View Bill),
  `AdminOrderPanel` (View Order Details).
- **Remember:** the same window is used in three places, so the bill always looks
  the same.

---

## 15. Code reading guide

These are the pieces of code you should be able to point at and explain.

### 15.1 `DBConnection.getConnection()`

```java
private static final String URL =
        "jdbc:mysql://localhost:3306/food_ordering_db?useSSL=false&serverTimezone=UTC";
private static final String USERNAME = "root";
private static final String PASSWORD = "root";

public static Connection getConnection() throws SQLException {
    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
        throw new SQLException("MySQL JDBC driver not found. ...");
    }
    return DriverManager.getConnection(URL, USERNAME, PASSWORD);
}
```

**Line by line:** `URL` says which database on which computer — `localhost` means
this computer and `3306` is the MySQL port. `Class.forName(...)` loads the MySQL
driver. `DriverManager.getConnection(...)` opens the connection and returns it.

**If it was removed:** no class could reach the database and every screen would
fail.

**Say to the teacher:** *"This is the only class that knows the database address
and password, so if the password changes I edit one file."*

### 15.2 `UserDAO.login()`

```java
public User login(String email, String password) throws SQLException {
    String sql = "SELECT * FROM users WHERE email = ? AND password = ?";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, email);
        ps.setString(2, password);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return buildUser(rs);
            }
        }
    }
    return null;
}
```

**Line by line:** the SQL has two `?` so the values are sent as parameters.
`setString(1, email)` fills the first `?`. `executeQuery()` runs the SELECT.
`rs.next()` is true only if a matching row was found — then `buildUser(rs)` copies
the row into a `User` object. If no row matched we fall through and `return null`.

**If it was removed:** nobody could log in.

**Say to the teacher:** *"It returns a User object when the email and password
match a row, and null when they do not."*

### 15.3 The check that decides the dashboard (`LoginFrame.doLogin()`)

```java
User user = userDAO.login(email, password);

if (user == null) {
    showError("Invalid email or password. Please try again.");
    passwordField.setText("");
    return;
}

dispose();
if (user.isAdmin()) {
    new AdminDashboard(user).setVisible(true);
} else {
    new CustomerDashboard(user).setVisible(true);
}
```

**Line by line:** `null` means wrong credentials, so we show the message, clear
the password box and stop. Otherwise `dispose()` closes the login window and the
role decides which dashboard is created.

**Say to the teacher:** *"Role based access is done here — the role column in the
users table decides which screens the user can reach."*

### 15.4 `Validator.isValidQuantity()`

```java
public static final int MAX_QUANTITY = 100;

public static boolean isValidQuantity(String quantity) {
    try {
        int value = Integer.parseInt(quantity.trim());
        return value > 0 && value <= MAX_QUANTITY;
    } catch (NumberFormatException e) {
        return false;
    }
}
```

**Line by line:** `Integer.parseInt` turns the typed text into a number. If the
user typed letters it throws `NumberFormatException`, we catch it and return
`false`. If it is a number we also check it is between 1 and 100.

**If it was removed:** a user could type `abc` or `-5` or a huge number and the
program would show an ugly error or save wrong data.

**Say to the teacher:** *"Every value typed by the user is checked here before it
goes anywhere near the database."*

### 15.5 `Cart.addItem()`

```java
public void addItem(FoodItem food, int quantity) {
    for (CartItem item : items) {
        if (item.getFoodItem().getFoodId() == food.getFoodId()) {
            item.setQuantity(item.getQuantity() + quantity);
            return;
        }
    }
    items.add(new CartItem(food, quantity));
}
```

**Line by line:** the loop looks through the lines already in the cart. If the
same `food_id` is found we just add to its quantity and `return` immediately. If
the loop finishes without finding it, we add a new line.

**If it was removed (the loop):** the same dish would appear twice in the cart.

**Say to the teacher:** *"If the customer adds the same dish again, the quantity
increases instead of a duplicate line appearing."*

### 15.6 `Cart.getTotalAmount()`

```java
public double getTotalAmount() {
    double total = 0;
    for (CartItem item : items) {
        total = total + item.getSubtotal();
    }
    return total;
}
```

**Line by line:** start at zero, add the subtotal of every line, return the sum.
`getSubtotal()` is `price * quantity`.

**Say to the teacher:** *"The total is calculated by the program, so it can never
be added up wrongly."*

### 15.7 The transaction in `OrderDAO.placeOrder()`

```java
con = DBConnection.getConnection();
checkAllItemsAvailable(con, cart);
con.setAutoCommit(false);
...
// insert the order, read back the generated order_id
...
// insert every cart line into order_items
con.commit();
```
and

```java
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
```

**Line by line:** `setAutoCommit(false)` means nothing is saved until we say so.
Both inserts happen. `commit()` saves them together. If anything throws, the
catch block calls `rollback()` which undoes everything, and the exception is
passed on so the screen can show a message. The `finally` block always runs, so
the connection is always closed.

**If it was removed:** an order could be saved without its items, or items
without their order.

**Say to the teacher:** *"This is the transaction. Either the whole order is
saved or none of it is."*

### 15.8 Reading back the generated `order_id`

```java
try (PreparedStatement ps =
             con.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
    ...
    ps.executeUpdate();

    try (ResultSet keys = ps.getGeneratedKeys()) {
        if (!keys.next()) {
            con.rollback();
            return null;
        }
        orderId = keys.getInt(1);
    }
    break;
}
```

**Line by line:** `order_id` is `AUTO_INCREMENT`, so MySQL decides the number, not
us. `RETURN_GENERATED_KEYS` asks MySQL to send it back, and `getGeneratedKeys()`
reads it. We need it because every `order_items` row must carry this `order_id`.

**Say to the teacher:** *"I need the order_id that MySQL generated, because the
order items point back to it with a foreign key."*

### 15.9 Inserting the items as a batch

```java
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
```

**Line by line:** the loop fills the four `?` for each cart line and calls
`addBatch()` which stores it. `executeBatch()` sends them all to MySQL together,
which is faster than sending them one at a time.

**Note `ps.setDouble(4, ...)`** — the rate is copied into `order_items.price`. This
is why an old bill stays correct after a price change.

### 15.10 `checkAllItemsAvailable()`

```java
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
```

**Why it exists:** a dish can sit in the cart for a while. If the admin marks it
as not available in the meantime, it must not be ordered. If no row comes back
the dish is gone or unavailable, so we throw an exception with a readable message
and the order is refused.

**Say to the teacher:** *"I found this while testing — the dish was hidden from
the menu but a cart that already had it could still order it, so I added this
check."*

### 15.11 `CategoryDAO.countFoodItems()` and why delete is protected

```java
public int countFoodItems(int categoryId) throws SQLException {
    String sql = "SELECT COUNT(*) FROM food_items WHERE category_id = ?";
    ...
}
```
and in `CategoryPanel`:

```java
int used = categoryDAO.countFoodItems(selectedCategoryId);
if (used > 0) {
    showError("This category cannot be deleted because " + used
            + " food item(s) belong to it. ...");
    return;
}
```

**Why:** `food_items.category_id` is a foreign key. MySQL would refuse the delete
with a technical error, so we count first and show a message a normal person can
understand.

### 15.12 Filling a `JTable` (`MenuPanel.showItems()`)

```java
private void showItems(List<FoodItem> items) {
    displayedItems = items;
    tableModel.setRowCount(0);

    for (FoodItem food : items) {
        tableModel.addRow(new Object[]{
                food.getFoodId(), food.getFoodName(), food.getDescription(),
                food.getCategoryName(), String.format("%.2f", food.getPrice())
        });
    }
}
```

**Line by line:** `setRowCount(0)` clears the table. Then one `addRow` per dish.
`String.format("%.2f", ...)` shows the price with exactly two decimals.
`displayedItems` is kept because the selected table row number is used to find the
matching `FoodItem` object.

### 15.13 The search query

```java
public List<FoodItem> searchAvailableByName(String keyword) throws SQLException {
    return runQuery(SELECT_BASE + "WHERE f.available = 'Yes' AND f.food_name LIKE ? "
                    + "ORDER BY f.food_id", null, "%" + keyword + "%");
}
```

**Line by line:** `LIKE` with `%` on both sides finds the word anywhere in the
name, so searching "paneer" finds "Paneer Tikka" and "Paneer Butter Masala". Note
`available = 'Yes'` — the search never returns a dish the customer cannot order.

### 15.14 The join used by the admin order list

```java
String sql = "SELECT o.*, u.full_name FROM orders o "
           + "JOIN users u ON o.user_id = u.user_id ORDER BY o.order_id DESC";
```

**Line by line:** `orders` only stores `user_id`, a number. The `JOIN` brings the
customer's name from the `users` table so the admin screen can show it.
`ORDER BY o.order_id DESC` puts the newest order at the top.

### 15.15 Building the bill (`BillDialog.buildBillText()`)

```java
bill.append(String.format("%-3s %-20s %5s %4s %10s%n",
        "No", "Food Item", "Rate", "Qty", "Amount"));
...
bill.append(String.format("%-3d %-20s %5.2f %4d %10.2f%n",
        srNo++, trim(item.getFoodName()), item.getPrice(),
        item.getQuantity(), item.getSubtotal()));
```

**Line by line:** `String.format` with fixed widths keeps the columns in a
straight line. `%-20s` means text padded to 20 characters on the left, `%10.2f`
means a number in 10 characters with 2 decimals. The text is shown in a
`JTextArea` with a monospaced font so it looks like a printed receipt.

---

## 16. Viva questions and answers

Answers are written the way you should say them — short and in your own words.

### A. General project questions

**1. What is your project?**
A Food Ordering System — a Java desktop application where a customer can browse a
restaurant menu, add food to a cart, place an order and get a bill, and the admin
can manage the menu and the orders.

**2. Who are the users of your system?**
Two: the admin (the restaurant owner) and the customer.

**3. What problem does it solve?**
In a small restaurant the order is written on a paper slip and the bill is added
up by hand. Slips get lost, the total can be wrong, and there is no record of old
orders. My system stores everything in a database and calculates the total
automatically.

**4. Is it a web application?**
No, it is a desktop application. It runs on one computer with Java and MySQL
installed. No internet connection is needed.

**5. How many modules does your project have?**
Ten: login, customer registration and profile, category management, food menu
management, menu browsing with search and filter, cart, order placement, billing,
order history, and admin order management.

**6. How many Java classes are there?**
27 classes in 6 packages.

**7. How long does it take to run?**
You run the SQL script once to create the database, then run the jar file. The
login window opens immediately.

**8. What is the main file?**
`MainApp.java`. It has the `main` method.

**9. What are the default login details?**
Admin: `admin@food.com` / `admin123`. Demo customer: `customer@food.com` /
`cust123`. Both are created by the SQL script.

**10. Can more than one customer use it?**
Yes, any number of customers can register. Each customer sees only their own
orders.

**11. Did you build this yourself?**
Yes. I designed the five tables, wrote the DAO classes with the SQL, built the
Swing screens and tested the whole application.

### B. Java questions

**12. Why did you choose Java?**
It is the language in our syllabus, it already has Swing for making screens and
JDBC for databases, so I did not need any extra framework.

**13. Which version of Java did you use?**
JDK 21.

**14. What is a class and what is an object?**
A class is a blueprint, an object is one actual thing made from it. `Category` is
a class; `new Category(1, "Starters", "...")` creates an object.

**15. What is a constructor?**
A special method with the same name as the class that runs when an object is
created, to set its starting values.

**16. What is `this` used for?**
Inside a constructor or method, `this.categoryId` means the field of this object,
which separates it from a parameter with the same name.

**17. What is encapsulation and where did you use it?**
Keeping fields `private` and giving public getters and setters. All seven model
classes do this, so no other class can put a wrong value straight into a field.

**18. Did you use inheritance?**
Yes. `LoginFrame extends JFrame`, `CategoryPanel extends JPanel`,
`BillDialog extends JDialog`. My class gets everything the Swing class can do and
I only add my own components.

**19. What is method overriding? Give an example.**
Changing what an inherited method does. I override `isCellEditable()` to return
`false` so the user cannot type into a table, and `toString()` in `Category` so a
combo box shows the category name.

**20. What does `static` mean?**
The method or value belongs to the class, not to an object, so you can use it
without `new`. All the `Validator` methods and `DBConnection.getConnection()` are
static.

**21. What is `final`?**
The value cannot be changed after it is set. I used it for constants like
`MAX_QUANTITY` and for the `Cart`'s list of items.

**22. Which collection did you use?**
`ArrayList`, through the `List` interface — for example
`List<CartItem> items = new ArrayList<>();` in `Cart`, and every DAO method that
returns many rows returns a `List`.

**23. What is exception handling?**
Catching an error that happens while the program runs so the program shows a
message instead of stopping. I catch `SQLException` in every screen and
`NumberFormatException` in `Validator`.

**24. What is try-with-resources?**
A `try` where anything opened in the round brackets is closed automatically at
the end, even if there is an error. I use it for `Connection`,
`PreparedStatement` and `ResultSet` so a connection is never left open.

**25. What is the difference between `==` and `.equals()`?**
`==` compares whether two references point at the same object; `.equals()`
compares the contents. For text I use `.equals()`, for example
`"ADMIN".equalsIgnoreCase(role)`.

**26. What is a package?**
A folder that groups related classes. I have six: the main one, `db`, `model`,
`dao`, `ui` and `util`.

### C. Swing questions

**27. What is Swing?**
The standard Java library for making desktop windows, buttons, text boxes and
tables. It is part of the JDK.

**28. Why Swing and not JavaFX or a web page?**
Swing comes with the JDK so there is nothing extra to install, it is what we
studied, and my project is a single-computer desktop application.

**29. What is the difference between JFrame, JPanel and JDialog?**
`JFrame` is a full window with a title bar. `JPanel` is an invisible container
that holds components inside a window. `JDialog` is a smaller window that opens
on top of another one — I use it for registration and the bill.

**30. Which Swing components did you use?**
JFrame, JDialog, JPanel, JLabel, JTextField, JPasswordField, JTextArea, JButton,
JTable, JComboBox, JScrollPane and JOptionPane.

**31. What is JOptionPane used for?**
Pop-up messages. `showMessageDialog` for information and errors, and
`showConfirmDialog` for a yes/no question before deleting a record or placing an
order.

**32. How do you show data in a JTable?**
A `JTable` displays what is in its table model. I use `DefaultTableModel`, clear
it with `setRowCount(0)` and add one row per record with `addRow(...)`.

**33. How did you stop the user editing the table?**
I overrode `isCellEditable()` in the table model to return `false`.

**34. Which layouts did you use?**
`BorderLayout` for the main windows, `GridBagLayout` for the forms, `FlowLayout`
for button rows, `BoxLayout` for the left menu strip, and `CardLayout` for the
work area.

**35. What is CardLayout and why did you use it?**
It keeps several panels stacked and shows one at a time. Both dashboards use it,
so clicking a left-menu button just shows a different panel in the same window.

**36. How does a button work in Swing?**
You add an `ActionListener` to it. I write it as a lambda:
`addButton.addActionListener(e -> addCategory());` — when the button is clicked
that method runs.

**37. What is an event?**
Something the user does, like clicking a button or typing. The listener is the
code that runs in response.

**38. How do all your screens look the same?**
I wrote a small class `UITheme` that holds the colours and fonts and has methods
`createButton`, `createTitleLabel`, `styleTable`, `money` and
`setSizeWithinScreen`. Every screen uses them, so changing one file changes the
whole look.

**38a. Why does `UITheme.money()` exist?**
Because `String.format("%.2f", ...)` follows the regional settings of the
computer. On a computer set to a region that writes 460,00 instead of 460.00,
the screens that read the amount back out of the table could not open the bill.
`UITheme.money()` always formats with a dot, so the application behaves the same
on every computer.

**38b. Why does `UITheme.setSizeWithinScreen()` exist?**
Because on a laptop with Windows display scaling switched on, a fixed window of
1000 x 620 can be slightly taller than the screen and the Logout button falls
below the visible area. This method never makes a window bigger than the screen
actually has.

### D. JDBC questions

**39. What is JDBC?**
Java Database Connectivity — the standard Java API used to send SQL to a database
and read the result.

**40. What is a driver?**
The piece of software that knows how to talk to a particular database. I use
`mysql-connector-j`, and Maven downloads it automatically.

**41. What are the steps to connect Java with MySQL?**
Load the driver, get a `Connection` from `DriverManager`, create a
`PreparedStatement`, set the values, run `executeQuery()` or `executeUpdate()`,
read the `ResultSet`, and close everything.

**42. Where is your connection code?**
In `DBConnection.java`, in the `getConnection()` method. It is the only class
that knows the URL, user name and password.

**43. What is your JDBC URL?**
`jdbc:mysql://localhost:3306/food_ordering_db` — `localhost` is this computer,
`3306` is the MySQL port and `food_ordering_db` is the database name.

**44. What is a Statement and what is a PreparedStatement?**
A `Statement` sends plain SQL text. A `PreparedStatement` has `?` marks and the
values are supplied separately.

**45. Why did you use PreparedStatement?**
Two reasons: it prevents SQL injection because the values are sent as parameters
and cannot change the meaning of the query, and it is faster because MySQL
prepares the statement once. Every query in my project uses it.

**46. What is SQL injection?**
When a user types SQL into an input box to change what the query does — for
example typing `' OR '1'='1` as a password. A `PreparedStatement` treats it as
ordinary text, so it cannot happen.

**47. What is a ResultSet?**
The rows that come back from a SELECT. `rs.next()` moves to the next row and
returns false when there are no more, so I read it in a `while` loop.

**48. Difference between executeQuery() and executeUpdate()?**
`executeQuery()` is for SELECT and returns a `ResultSet`. `executeUpdate()` is for
INSERT, UPDATE and DELETE and returns how many rows changed.

**49. How do you know an insert worked?**
`executeUpdate()` returns the number of rows changed, so I write
`return ps.executeUpdate() > 0;`.

**50. What happens if MySQL is not running?**
`MainApp` calls `DBConnection.testConnection()` at startup. If it fails, a message
box tells the user to check that the MySQL service is running and that the
database was created, and the program stops cleanly instead of crashing.

### E. MySQL and database questions

**51. Which database did you use and why?**
MySQL 8.0 — it is free, already installed in our lab, and it supports primary
keys and foreign keys.

**52. What is the name of your database?**
`food_ordering_db`.

**53. How many tables and what are they?**
Five: `users`, `categories`, `food_items`, `orders` and `order_items`.

**54. What is a primary key?**
A column whose value is different for every row, so it identifies the row
uniquely. Mine are `user_id`, `category_id`, `food_id`, `order_id` and
`order_item_id`, and all of them are `AUTO_INCREMENT`.

**55. What is AUTO_INCREMENT?**
MySQL gives the next number automatically for each new row, so I never have to
generate the id myself.

**56. What is a foreign key?**
A column that points at the primary key of another table, joining the two. I have
four: `food_items.category_id`, `orders.user_id`, `order_items.order_id` and
`order_items.food_id`.

**57. Why are foreign keys useful?**
They keep the data correct. MySQL refuses to save an order for a customer who
does not exist, and refuses to delete a category that still has dishes.

**58. What is a unique key? Where did you use it?**
A column that cannot repeat, but it is not the primary key. I have three:
`users.email`, `categories.category_name` and `orders.order_number`.

**59. What is CRUD?**
Create, Read, Update, Delete — INSERT, SELECT, UPDATE and DELETE in SQL. My
category and food modules do all four.

**60. Why do you keep the admin and customers in one table?**
Because they need exactly the same columns. A second table would be a copy with a
different name. The `role` column, holding ADMIN or CUSTOMER, is enough to tell
them apart and it is what decides which dashboard opens.

**61. Why is there an `available` column instead of deleting a dish?**
So the admin can hide a dish that is finished for the day without deleting it.
If the dish were deleted the old orders containing it would break, because
`order_items.food_id` points at it.

**62. Why does `order_items` store the price when `food_items` already has it?**
Because it stores the rate **at the time of ordering**. If the admin changes the
menu price next week, the old bill still shows the amount that was actually
charged.

**63. What is a JOIN? Where did you use one?**
A JOIN reads related rows from two tables together. My food queries join
`food_items` with `categories` to get the category name, and the admin order list
joins `orders` with `users` to get the customer name.

**64. How do you create the database?**
By running the script `database/food_ordering_db.sql`. It creates the database,
the five tables, the keys, and inserts the sample data.

**65. What sample data is there?**
One admin, one demo customer, four categories and twelve food items.

### F. Project architecture questions

**66. Explain the architecture of your project.**
Three layers. The presentation layer is the Swing screens in the `ui` package.
The business logic layer is the DAO classes and the model classes. The data
access layer is `DBConnection` and the MySQL driver. Each layer talks only to the
one below it.

**67. Why did you divide the code into layers?**
So that each part has one job and is easy to find. If a query has to change I
know it is in a DAO class; if a screen has to change it is in the `ui` package.

**68. What is a DAO?**
Data Access Object — a class that contains all the SQL for one part of the
database. I have four: `UserDAO`, `CategoryDAO`, `FoodItemDAO` and `OrderDAO`.

**69. Why not write the SQL directly in the screens?**
Then the same query would be copied into several screens and any change would
have to be made in many places. Keeping SQL in the DAO classes means one place
per query.

**70. What is a model class?**
A simple class that carries data — private fields with getters and setters. It
lets a whole row be passed around as one object.

**71. Which class starts the application?**
`MainApp`. It checks the database connection and opens `LoginFrame`.

**72. How is the cart shared between two screens?**
`CustomerDashboard` creates one `Cart` object and passes the same object to
`MenuPanel` and `CartPanel`, so both work on the same cart.

**73. Where would you add a new screen?**
A new class in the `ui` package, added to the `CardLayout` of the right dashboard
with a new button in the left menu, and a new DAO method if it needs data.

### G. Login questions

**74. Explain your login process.**
The user types email and password. `LoginFrame` checks the boxes are not empty
and the email format is correct, then calls `UserDAO.login()`. That runs a SELECT
on the `users` table with the two values as parameters. If a row comes back it is
copied into a `User` object, otherwise `null` is returned. The `role` of the
returned user decides which dashboard opens.

**75. What happens if the password is wrong?**
`UserDAO.login()` returns `null`, so the message "Invalid email or password.
Please try again." is shown, the password box is cleared and the login screen
stays open.

**76. What if the user leaves a box empty?**
`Validator.isEmpty()` catches it and the message "Please enter both email and
password." is shown. Nothing is sent to the database.

**77. How does the system know who is an admin?**
The `role` column of the `users` table. `User.isAdmin()` returns true when it is
`ADMIN`.

**78. Can a customer open the admin screens?**
No. The admin screens exist only inside `AdminDashboard`, and that window is
created only when the role is ADMIN.

**79. How are passwords stored?**
As plain text in the `password` column. I know this is a weakness — in a real
system I would store an encrypted form of the password. I have listed it as a
limitation and as a future enhancement.

**80. What does logout do?**
It asks for confirmation, then closes the dashboard, clears the customer's cart
and opens the login window again.

### H. Cart questions

**81. Where is the cart stored?**
Only in memory, in a `Cart` object. It is not a database table. It is written to
the database only when the order is placed.

**82. Why is the cart not in the database?**
Because it is temporary. It belongs to one session and is finished as soon as the
order is placed. Keeping it in memory is simpler and enough for this project.

**83. What happens if you add the same dish twice?**
`Cart.addItem()` searches the existing lines first. If the same `food_id` is
there it only increases the quantity, so no duplicate line appears.

**84. How is the total calculated?**
Each line is rate multiplied by quantity in `CartItem.getSubtotal()`, and
`Cart.getTotalAmount()` adds all the subtotals in a loop.

**85. Give an example of the calculation.**
220 × 2 = 440, plus 180 × 1 = 180, plus 25 × 4 = 100, plus 80 × 2 = 160, total
Rs. 880.00.

**86. Can the quantity be zero or negative?**
No. `Validator.isValidQuantity()` accepts only a whole number from 1 to 100 and
shows a message otherwise.

**87. What happens to the cart when you log out?**
It is cleared, so the next user starts with an empty cart.

### I. Order questions

**88. What happens when a customer places an order?**
The program checks the cart is not empty and every dish is still available, then
starts a transaction, inserts one row into `orders`, reads back the generated
`order_id`, inserts one row into `order_items` for every cart line, and commits.
Then it shows the bill and clears the cart.

**89. Why are `orders` and `order_items` two separate tables?**
Because an order has some details that appear once — the number, date, customer
and total — and some that repeat for every dish. Using one table would mean
repeating the order number and date on every dish row.

**90. What is a transaction?**
A group of database operations that must all succeed together. If one fails, all
of them are undone.

**91. How did you implement the transaction?**
`con.setAutoCommit(false)` before the inserts, `con.commit()` after both of them,
and `con.rollback()` in the catch block if anything fails.

**92. What happens if the second insert fails?**
`rollback()` undoes the first insert too, so there is no order without items. I
tested this.

**93. How is the order number generated?**
From the date and the time down to the second, like `ORD20260905023811`.

**94. What if two orders happen in the same second?**
They would get the same number, so `order_number` has a UNIQUE key. If MySQL
refuses the number the program tries again and the second order becomes
`ORD20260905023811-1`. I tested it by placing six orders in a loop.

**95. What are the order statuses?**
Pending, Confirmed, Preparing, Ready and Delivered. A new order always starts as
Pending.

**96. Who changes the status?**
Only the admin, from the Manage Orders screen. The customer sees the new status
in My Orders.

**97. Can a customer see another customer's orders?**
No. `getOrdersByUser()` filters by the logged-in customer's `user_id`.

### J. Billing questions

**98. How is the bill generated?**
`BillDialog` receives the `Order` object with its items and builds the receipt
text line by line with `String.format`, then shows it in a `JTextArea` with a
monospaced font.

**99. What does the bill show?**
Order number, order date, customer name, payment mode, order status, then one
line per dish with the rate, quantity and amount, and finally the total.

**100. Is there online payment?**
No. The payment mode is recorded as Cash on Delivery or Pay at Counter. A payment
gateway was outside the scope of a college project.

**101. Can the bill be printed?**
It can be saved as a text file with the Save Bill button. Direct printing is in my
future enhancements.

**102. Can an old bill be seen again?**
Yes — from My Orders the customer clicks View Bill, and the admin can click View
Order Details. Both open the same `BillDialog`.

**103. Why does the bill use `String.format`?**
To keep the columns in a straight line. `%-20s` pads the dish name to 20
characters and `%10.2f` prints the amount with two decimals in a fixed width.

### K. Testing questions

**104. How did you test your project?**
Both ways. I wrote automated tests that call the DAO classes directly and check
the database afterwards, and I also went through the whole application by hand,
screen by screen, and checked the tables in MySQL after every operation.

**105. How many test cases?**
47 test cases are documented in the report, covering login, registration,
category and food management, menu, cart, order placement, billing, order
history, admin order management, validation and transaction rollback.

**106. Did all of them pass?**
Yes, all 47 passed on the finished application.

**107. Did you find any bugs?**
Yes, several. The category delete first showed a raw SQL error, so I added a
count check first. The cart showed the same dish twice when it was added again.
And in the final round of testing I found three more: several orders in the same
second could run out of order numbers, a dish marked as not available could still
be ordered from a cart that already had it, and a very large quantity was
accepted. I fixed all three.

**108. What is black box testing?**
Testing the application through its screens like a normal user, comparing what
appears with what should appear, without looking at the code while testing. That
is what I did.

**109. How did you test the rollback?**
I placed an order where the rate of a dish was too large for the `DECIMAL(8,2)`
price column. The `orders` row was inserted, the `order_items` insert failed, and
after the rollback both tables were unchanged.

**110. How did you check the database?**
By opening the tables in MySQL after each operation and comparing the rows with
what the screen showed.

**111. What is validation testing?**
Typing wrong values on purpose — blank boxes, letters in a number box, a negative
price, a zero quantity — and checking that a clear message appears instead of the
program crashing.

### L. Git and GitHub questions

**112. What is Git?**
A version control tool that records the history of the project, so I can see what
changed and go back if needed.

**113. What is GitHub?**
A website that keeps a copy of the Git repository online, as a backup and to
share the project.

**114. Which branch is your project on?**
`main`. That is the only branch of the project.

**115. What is a commit?**
A saved point in the history with a message describing what changed, like "Add
shopping cart".

**116. What does .gitignore do?**
Lists files that should not be saved in the repository — for me, the `target`
folder that Maven creates, class files and IDE settings.

**117. Why is the compiled jar not in the repository?**
Because it is generated from the source code. Anyone can rebuild it with
`mvn clean package`.

### M. Questions about limitations

**118. What are the limitations of your project?**
No online payment, it is a desktop application only, passwords are stored as
plain text, there is no forgot-password option, the cart is lost if the
application is closed before ordering, there are no dish photographs, no sales
reports, and only one admin account.

**119. Why is there no payment gateway?**
A real gateway needs a merchant account and internet banking integration, which
is outside a college project. The payment is recorded as Cash on Delivery or Pay
at Counter.

**120. Why are the passwords not encrypted?**
I kept the login simple so the whole flow could be explained. I know it is a
weakness and I have listed encrypted passwords as the first future enhancement.

**121. Can two people use it at the same time?**
Two copies could run against the same MySQL server, but I built and tested it as a
single-computer application.

**122. What happens if the internet goes off?**
Nothing — the program and the database are on the same computer, so no internet
is needed.

### N. Future enhancement questions

**123. What would you improve first?**
Store the passwords in an encrypted form instead of plain text.

**124. What else?**
A forgot-password screen, direct printing of the bill, sales reports for the
admin, photographs of the dishes, saving the cart in the database, discounts and
taxes on the bill, and eventually a web or Android version using the same tables.

**125. Could this become a web application?**
Yes. The database design would stay the same; only the screens would be replaced
by web pages, and the DAO classes could be reused.

---

## 17. Difficult teacher questions

These are the questions a teacher asks to find out whether you really built the
project. Practise saying these out loud.

**"Why did you choose Java?"**
> Because it is the language in our syllabus and it already gives me everything I
> need — Swing for the screens and JDBC for the database. I did not have to learn
> an extra framework, so I could concentrate on the design and the logic.

**"Why Swing and not a web application?"**
> The requirement was a desktop application for one computer in the restaurant.
> Swing comes with the JDK, so there is nothing extra to install and no web server
> to configure. It also matches what we studied.

**"Why MySQL?"**
> It is free, it is already in our lab, and my design needs primary keys and
> foreign keys, which MySQL supports properly. My data is clearly relational —
> orders belong to customers and dishes belong to categories.

**"Why JDBC and not something else?"**
> JDBC is the standard way for a Java program to use a database, and it is only
> three main interfaces — Connection, PreparedStatement and ResultSet. Anything
> bigger would have been extra complexity I could not have explained.

**"Why did you design these five tables?"**
> Each one keeps one kind of information. `users` for the people, `categories` for
> the food groups, `food_items` for the menu, `orders` for each order and
> `order_items` for the dishes inside an order. I did not add any table I did not
> need.

**"Why are orders and order_items separate?"**
> Because an order has details that appear once — number, date, customer, total —
> and details that repeat for every dish. If I used one table I would repeat the
> order number and date on every dish row. Two tables joined by a foreign key
> stores each fact once.

**"What is a primary key?"**
> A column whose value is different in every row, so it identifies that row
> uniquely. In `orders` it is `order_id`, and MySQL fills it automatically with
> AUTO_INCREMENT.

**"What is a foreign key?"**
> A column that points at the primary key of another table. `orders.user_id`
> points at `users.user_id`, which means this order belongs to that customer. It
> also stops wrong data — MySQL will not save an order for a customer who does not
> exist.

**"What exactly happens when a customer places an order?"**
> The program checks the cart is not empty and that every dish is still available.
> Then it switches off auto commit, inserts one row into `orders`, reads back the
> `order_id` that MySQL generated, inserts one row into `order_items` for every
> line of the cart, and commits. Then it shows the bill and clears the cart. If
> anything fails in between, rollback undoes everything.

**"How is the total calculated?"**
> Each cart line is rate multiplied by quantity, and the total is the sum of all
> the line subtotals. For example 220 × 2 plus 180 × 1 plus 25 × 4 plus 80 × 2
> gives Rs. 880. The program does the arithmetic, so it is always correct.

**"How does the admin update an order?"**
> On the Manage Orders screen the admin selects the order, chooses the new status
> from the drop down and clicks Update Status. That runs an UPDATE on the `orders`
> table, and the customer sees the new status in My Orders.

**"What happens if the database is not available?"**
> `MainApp` tests the connection when the program starts. If MySQL is not running
> it shows a message explaining what to check and stops cleanly. If the connection
> is lost later, each screen catches the SQLException and shows the message
> instead of crashing.

**"What happens if the user enters wrong credentials?"**
> `UserDAO.login()` returns null, and the login screen shows "Invalid email or
> password", clears the password box and stays open.

**"What is CRUD?"**
> Create, Read, Update, Delete — INSERT, SELECT, UPDATE and DELETE. My category
> and food modules do all four.

**"What is a PreparedStatement and why did you use it?"**
> A SQL statement with question marks where the values go. The values are sent
> separately, so text typed by the user cannot change the meaning of the query —
> that prevents SQL injection. It is also faster because MySQL prepares the
> statement once. Every query in my project uses one.

**"Why is there no payment gateway?"**
> A real gateway needs a merchant account and bank integration, which is not
> possible in a college project. The payment is recorded as Cash on Delivery or
> Pay at Counter, which is how a small restaurant actually works.

**"What are the limitations of your project?"**
> No online payment, desktop only, passwords stored as plain text, no
> forgot-password option, the cart is lost if the program closes before ordering,
> no dish photographs, no sales reports, and one admin account.

**"What would you improve in the future?"**
> First encrypted passwords, then a forgot-password screen, printing the bill,
> sales reports for the admin, photographs of the dishes, and eventually a web or
> Android version using the same database design.

**"Show me where that is in the code."**
> Do not panic. Open the class by name. Login is `ui/LoginFrame.java` and
> `dao/UserDAO.java`. The cart calculation is `model/Cart.java` and
> `model/CartItem.java`. The order is `dao/OrderDAO.java`, method `placeOrder`.
> The bill is `ui/BillDialog.java`, method `buildBillText`. The connection is
> `db/DBConnection.java`.

**"Did you write this code yourself?"**
> Yes. Then explain one part in detail without being asked — the transaction in
> `placeOrder` is the best one, because it shows you understand why two tables are
> used and what rollback does.

---

## 18. Presentation script

Say these lines in your own words. Do not read them out.

**STEP 1 — Open the application**
> "This is my project, a Food Ordering System. It is a Java desktop application
> and the data is stored in MySQL. I will run it now."

Run the jar. The login window opens.

**STEP 2 — Show the login screen**
> "This is the login screen. The same screen is used by the admin and by the
> customers. The role stored in the database decides which dashboard opens."

**STEP 3 — Show a wrong login first** (this impresses examiners)

Type a wrong password and click Login.
> "If the password is wrong the system shows this message and stays on the login
> screen. Nothing is saved and the program does not crash."

**STEP 4 — Login as admin**
> "Now I will log in as the admin."

Type `admin@food.com` / `admin123`.
> "This is the admin dashboard. The menu on the left is for categories, food items
> and orders."

**STEP 5 — Manage Categories**

Click Manage Categories.
> "These are the food categories, loaded from the `categories` table. I will add a
> new one."

Type a name, click Add.
> "It is saved immediately and the table refreshes. If I try to delete a category
> that still has dishes in it, the system stops me, because `food_items` has a
> foreign key pointing at `categories`."

**STEP 6 — Manage Food Items**

Click Manage Food Items.
> "This is the menu. Each dish has a name, description, category, price and an
> availability. I will add one."

Fill the form, click Add.
> "The availability column is useful — if a dish is finished for the day I set it
> to No and it disappears from the customer menu, but the dish is not deleted, so
> the old orders that contain it stay correct."

**STEP 7 — Show validation**

Type a negative price and click Add.
> "Every value is checked before it goes to the database, so the system shows a
> message instead of saving wrong data."

**STEP 8 — Logout**

Click Logout, confirm.
> "Logout takes us back to the login screen."

**STEP 9 — Register a customer**

Click New Customer? Register.
> "A new customer registers here. The email must be unique and the mobile number
> must be exactly ten digits."

Fill the form, click Register.

**STEP 10 — Login as the customer**
> "Now I log in with the account I just created."

**STEP 11 — Browse the menu**
> "This is the customer dashboard. It shows only the dishes that are marked
> available."

**STEP 12 — Filter and search**

Choose a category, click Show Category. Then type a dish name and click Search.
> "The customer can see one category at a time, or search a dish by name."

**STEP 13 — Add to the cart**

Select a dish, type quantity 2, click Add to Cart. Add two more.
> "I select a dish, enter the quantity and add it to the cart."

**STEP 14 — Show the cart**

Click Go to Cart.
> "The cart shows the rate, the quantity and the subtotal of every line, and the
> total at the bottom. The subtotal is rate multiplied by quantity and the total
> is the sum of the subtotals — the program calculates it, so it cannot be wrong."

**STEP 15 — Change the cart**

Change a quantity, then remove a line.
> "The quantity can be changed and a line can be removed. The total is
> recalculated every time."

**STEP 16 — Place the order**

Click Place Order, confirm.
> "When I confirm, the order is saved in the `orders` table and each dish in the
> `order_items` table. Both inserts happen in one transaction, so either the whole
> order is saved or nothing is."

Show the order number message.
> "Every order gets a unique order number made from the date and time."

**STEP 17 — Show the bill**
> "This is the bill. It shows the order number, the date, my name, the payment
> mode, each dish with its rate and quantity, and the final amount. The same
> window can be opened again later for any old order."

Close the bill.

**STEP 18 — Order history**

Click My Orders.
> "The customer can see all their previous orders here with the amount and the
> current status. A customer sees only their own orders."

**STEP 19 — Logout and login as admin**
> "Now I will log in as the admin again to show the order from the other side."

**STEP 20 — Admin sees the order**

Click Manage Orders.
> "The admin sees the orders of all customers with the customer name, which comes
> from a join between `orders` and `users`. Here is the order I just placed, with
> the status Pending."

**STEP 21 — Change the status**

Select the order, choose Confirmed, click Update Status.
> "The admin moves the order through Pending, Confirmed, Preparing, Ready and
> Delivered. The change is saved in MySQL immediately."

**STEP 22 — Show the customer sees it**

Logout, login as the customer, click My Orders.
> "And the customer sees the new status here. That completes the full cycle from
> ordering to delivery."

**STEP 23 — Close**
> "That is my project. I also tested it with 47 test cases covering all the
> modules, and all of them passed. May I show you the database or the code?"

---

## 19. Five-minute presentation

| Time | What to do | What to say |
|---|---|---|
| 0:00–0:30 | Stand, open the app | The 30-second introduction from section 1 |
| 0:30–1:00 | Login screen, wrong password once | "The role in the database decides which dashboard opens" |
| 1:00–1:45 | Admin: categories and food items | "The admin manages the menu. Availability hides a dish without deleting it" |
| 1:45–2:15 | Logout, login as customer | "Same login screen, different dashboard" |
| 2:15–3:00 | Menu, filter, search, add to cart | "Only available dishes are shown" |
| 3:00–3:45 | Cart, change quantity, place order | "Rate × quantity, then the sum. One transaction into two tables" |
| 3:45–4:15 | Bill | "Order number, items, quantities, total" |
| 4:15–4:45 | Order history, then admin status update | "The admin changes the status and the customer sees it" |
| 4:45–5:00 | Close | "Five tables, 27 classes, 47 test cases, all passed" |

**If you only have 5 minutes, skip:** registration, the profile screen and the
search box. Do not skip the cart total or the order placement.

---

## 20. Ten-minute presentation

| Time | What to do |
|---|---|
| 0:00–0:45 | Introduction — problem, solution, technologies (the 1-minute version) |
| 0:45–1:30 | Show the architecture on paper: 3 layers and the 5 tables |
| 1:30–2:00 | Start the app, login screen, wrong password |
| 2:00–3:00 | Admin: add a category, add a food item, show a validation message |
| 3:00–3:30 | Show availability = No and explain why we do not delete |
| 3:30–4:00 | Logout, register a new customer, login |
| 4:00–5:00 | Menu, category filter, search by name |
| 5:00–6:00 | Add three dishes, show the cart, change a quantity, remove a line |
| 6:00–7:00 | Place the order. Explain the transaction while it saves |
| 7:00–7:30 | Bill — read out the order number and the total |
| 7:30–8:00 | Order history |
| 8:00–8:45 | Login as admin, show the order, change the status to Confirmed and Preparing |
| 8:45–9:15 | Login as customer, show the updated status |
| 9:15–10:00 | Testing summary, limitations, future enhancements, invite questions |

**Optional extra if there is time:** open MySQL and run
`SELECT * FROM orders;` and `SELECT * FROM order_items;` to show the rows that
were just created. This convinces an examiner very quickly.

---

## 21. If the teacher says "Explain your project"

Use this. It takes about one minute.

> "My project is a Food Ordering System for a small restaurant. It is a desktop
> application written in Java. The screens are made with Java Swing and the data
> is stored in a MySQL database, and Java talks to MySQL through JDBC.
>
> The problem I wanted to solve is that in a small restaurant the order is written
> on a paper slip and the bill is added up by hand. Slips get lost, the total can
> be wrong, and there is no record of old orders.
>
> There are two users. The customer registers, logs in, sees the menu, can filter
> it by category or search a dish by name, adds dishes to a cart with a quantity
> and places the order. The program calculates the total and shows a bill with a
> unique order number. The admin manages the categories and the menu, can mark a
> dish as not available, sees all the orders and moves each one through Pending,
> Confirmed, Preparing, Ready and Delivered.
>
> The database has five tables — users, categories, food_items, orders and
> order_items — joined by foreign keys. The code has 27 classes in three layers:
> the Swing screens, the DAO classes that hold all the SQL, and one class that
> creates the JDBC connection.
>
> The most important part is placing an order, because it writes to two tables in
> a single transaction, so either the whole order is saved or nothing is. I tested
> the application with 47 test cases and all of them passed."

Then stop and let the teacher ask questions.

---

## 22. How to handle a question you don't know

Never invent a technical answer. A teacher can tell immediately, and one honest
answer costs far less than one wrong one.

**Use one of these:**

> "I am not sure about that exact term, but in my project what happens is ..."
> *(then explain the part you do know)*

> "I have not used that in this project. What I used instead is ..."

> "I did not implement that. I listed it in the limitations of my report, and my
> plan for it would be ..."

> "May I show you in the code? I can find it and explain what it does."

> "I know where that is handled — it is in the OrderDAO class. Let me open it."

**Three rules:**

1. **Bring it back to your project.** You know your project better than anyone in
   the room. "In my project, ..." is always a safe start.
2. **Never say just "I don't know" and stop.** Say what you do know about the
   nearest thing.
3. **If you are corrected, accept it.** Say "Thank you, I will note that" and move
   on. Arguing looks worse than not knowing.

**If your mind goes completely blank**, ask for the question again: "Could you
please repeat the question?" It gives you a few seconds and it is completely
normal.

**If the application misbehaves during the demo**, stay calm and say: "Let me
check the database connection" — then check that MySQL is running. Do not
apologise repeatedly; just fix it and carry on.

---

## 23. VIVA CHEAT SHEET

*Read this in the last ten minutes before you go in.*

**Project:** Food Ordering System — a Java desktop application for a restaurant.
A customer orders food, the admin manages the menu and the orders.

**Technologies:** Java (JDK 21) · Java Swing (screens) · JDBC (connection) ·
MySQL 8.0 (database) · Maven (build) · Git and GitHub (version control).

**Modules (10):** login · registration and profile · category management · food
menu management · menu browsing with search and filter · cart · order placement ·
billing · order history · admin order management.

**Tables (5):**

| Table | Primary key | Foreign keys |
|---|---|---|
| `users` | user_id | — |
| `categories` | category_id | — |
| `food_items` | food_id | category_id |
| `orders` | order_id | user_id |
| `order_items` | order_item_id | order_id, food_id |

**Main classes:** `MainApp` (start) · `DBConnection` (JDBC) · `Validator`
(checks) · `User`, `Cart`, `CartItem`, `Order`, `OrderItem` (data) · `UserDAO`,
`CategoryDAO`, `FoodItemDAO`, `OrderDAO` (SQL) · `LoginFrame`, `AdminDashboard`,
`CustomerDashboard`, `MenuPanel`, `CartPanel`, `BillDialog` (screens).
**27 classes in 6 packages.**

**Login flow:** LoginFrame → Validator → UserDAO.login() → SELECT on `users` →
User object or null → role decides AdminDashboard or CustomerDashboard.

**Order flow:** MenuPanel → Cart (memory) → CartPanel → OrderDAO.placeOrder() →
`orders` + `order_items` in ONE transaction → BillDialog → OrderHistoryPanel.

**Cart calculation:** subtotal = rate × quantity; total = sum of subtotals.
Example: 220×2 + 180×1 + 25×4 + 80×2 = **Rs. 880.00**

**JDBC:** Connection · PreparedStatement (`?` for values, stops SQL injection) ·
ResultSet · `executeQuery()` for SELECT · `executeUpdate()` for
INSERT/UPDATE/DELETE.

**Transaction:** `setAutoCommit(false)` → both inserts → `commit()`;
`rollback()` in the catch block. Used because one order writes to two tables.

**CRUD:** Create=INSERT, Read=SELECT, Update=UPDATE, Delete=DELETE.

**Primary key:** identifies a row uniquely, e.g. `order_id`, AUTO_INCREMENT.
**Foreign key:** points at another table's primary key, e.g. `orders.user_id` →
`users.user_id`. It keeps the data correct.

**Order statuses:** Pending → Confirmed → Preparing → Ready → Delivered.

**Order number:** `ORD` + date and time to the second, e.g. `ORD20260905023811`.
UNIQUE in the database; if two land in the same second the next becomes `...-1`.

**Why `available` and not delete:** hides a finished dish from the menu without
breaking the old orders that contain it.

**Why `order_items.price`:** stores the rate at the time of ordering, so an old
bill stays correct after a price change.

**Testing:** 47 documented test cases, all passed. Bugs found and fixed included
the duplicate order number in the same second, a not-available dish still being
orderable from an old cart, and a very large quantity being accepted.

**Limitations:** no online payment · desktop only · plain-text passwords · no
forgot password · cart not saved · no dish photos · no sales reports · one admin.

**Future:** encrypted passwords · forgot password · print the bill · sales
reports · dish photographs · save the cart · discounts and taxes · web or Android
version.

**Demo login:** admin@food.com / admin123 · customer@food.com / cust123

---

*Good luck. You built it, you tested it, and you understand it. Speak slowly and
show the screens — the application does most of the explaining for you.*
