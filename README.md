# Food Ordering System

A simple desktop application for a restaurant, written in **Java (Swing)** with a
**MySQL** database. A customer can register, log in, browse the food menu, add
items to a cart, place an order and get a bill. The administrator manages the
food categories, the menu and the orders received from customers.

This project was developed as a BCA final year project.

---

## Features

**Customer**
- Registration and login with an email and password
- Browse the menu, filter by category or search a dish by name
- Add dishes to a cart, change the quantity, remove an item
- Automatic calculation of the subtotal of each line and the total amount
- Place an order and get a bill with a unique order number
- View previous orders with their amount and current status
- Update the personal profile

**Admin**
- Add, view, update and delete food categories
- Add, view, update and delete food items with price and availability
- View the orders of all customers and filter them by status
- Open the details of an order
- Change the order status : Pending, Confirmed, Preparing, Ready, Delivered

---

## Technologies Used

| Layer | Technology |
|---|---|
| Front end | Java Swing |
| Business logic | Core Java (JDK 21) |
| Database connectivity | JDBC (MySQL Connector/J 8.4.0) |
| Database | MySQL 8.0 |
| Build tool | Apache Maven |

---

## System Requirements

- JDK 21 (or JDK 17)
- MySQL Server 8.0
- Any Java IDE such as Apache NetBeans, IntelliJ IDEA or Eclipse
- Apache Maven 3.9 (only if you want to build the runnable jar)
- 4 GB RAM and about 20 GB free disk space

---

## Database Setup

1. Start the MySQL server.
2. Run the script `database/food_ordering_db.sql`.

   From the command line :

   ```
   mysql -u root -p < database/food_ordering_db.sql
   ```

   Or open the same file in MySQL Workbench and execute it.

3. The script creates the database `food_ordering_db` with five tables
   (`users`, `categories`, `food_items`, `orders`, `order_items`) and inserts
   the sample data.
4. Open `src/main/java/com/foodordering/db/DBConnection.java` and change
   `USERNAME` and `PASSWORD` if your MySQL password is different.

---

## How to Run the Project

**Using Maven**

```
mvn clean package
java -jar target/food-ordering-system.jar
```

**Using an IDE**

1. Open the project folder in NetBeans, IntelliJ IDEA or Eclipse.
2. Make sure the MySQL Connector/J jar is on the classpath
   (Maven downloads it automatically from `pom.xml`).
3. Run the class `com.foodordering.MainApp`.

The login window opens first. If the database cannot be reached, a message is
shown explaining what to check.

---

## Demo Login Details

The SQL script creates these two accounts :

| Role | Email | Password |
|---|---|---|
| Admin | admin@food.com | admin123 |
| Customer | customer@food.com | cust123 |

New customers can also be created from the **Register** button on the login screen.

Input rules used by the forms:

- Email must look like `name@example.com` and cannot already be registered
- Mobile number must be exactly 10 digits
- Password must be at least 5 characters
- Food price must be between 0.01 and 99999.99
- Quantity must be a whole number from 1 to 100

---

## Project Structure

```
Food-Ordering-System-Java
 ├── pom.xml                       Maven build file
 ├── README.md
 ├── STUDENT_GUIDE.md              full explanation and viva preparation
 ├── STUDENT_PROJECT_FLOW.md       one-page flow diagram
 ├── VIVA_QUICK_REFERENCE.md       short revision sheet
 ├── database
 │    └── food_ordering_db.sql     script that creates the database
 ├── docs
 │    ├── diagrams                 UML and ER diagrams
 │    ├── screenshots              screenshots of the application
 │    └── BCA_Project_Report_Food_Ordering_System.docx
 └── src/main/java/com/foodordering
      ├── MainApp.java             starting point of the application
      ├── db
      │    └── DBConnection.java   JDBC connection to MySQL
      ├── model                    User, Category, FoodItem, Cart,
      │                            CartItem, Order, OrderItem
      ├── dao                      UserDAO, CategoryDAO, FoodItemDAO, OrderDAO
      ├── ui                       LoginFrame, RegisterFrame, AdminDashboard,
      │                            CustomerDashboard, CategoryPanel,
      │                            FoodItemPanel, AdminOrderPanel, MenuPanel,
      │                            CartPanel, OrderHistoryPanel, ProfilePanel,
      │                            BillDialog
      └── util                     Validator, UITheme
```

The code follows a simple three layer design. The screens in `ui` call the
methods of the DAO classes, the DAO classes contain all the SQL statements, and
`DBConnection` is the only class that knows the database URL, user name and
password.

---

## Database Tables

| Table | Purpose |
|---|---|
| `users` | admin and customer accounts, with a `role` column |
| `categories` | food categories such as Starters and Main Course |
| `food_items` | the menu, linked to a category |
| `orders` | one row per order with its number, date, total and status |
| `order_items` | the dishes of each order with quantity and rate |

An order and its order items are saved in one transaction, so a half saved
order is never left in the database.

---

## Testing Status

The application was tested against a real MySQL database, not a simulated one.

| What was tested | Result |
|---|---|
| Clean Maven build from source | Passes, 27 classes compile with no errors |
| Database created from `food_ordering_db.sql` | 5 tables, 5 primary keys, 4 foreign keys, 3 unique keys |
| Functional test suite | 43 checks, all passed |
| Extended test suite (database, validation, errors) | 115 checks, all passed |
| Live demonstration flow through the real screens | 40 steps, all passed |
| Clean clone of `main`, rebuilt and re-tested | Passes |

Documented test cases in the project report: **47, all passed.**

Problems found during testing and fixed:

- Several orders placed inside the same second could run out of order numbers
- A dish marked as not available could still be ordered from a cart that already
  contained it
- A very large quantity was accepted by the quantity box

## Documents for the Student

| File | What it is |
|---|---|
| `STUDENT_GUIDE.md` | Full explanation of the project, Java and database concepts used, code walkthrough and 125 viva questions with answers |
| `STUDENT_PROJECT_FLOW.md` | One-page diagram of the whole flow and where each technology fits |
| `VIVA_QUICK_REFERENCE.md` | Short revision sheet to read before the viva |
| `docs/BCA_Project_Report_Food_Ordering_System.docx` | The project report |

## Future Enhancements

- Store the passwords in an encrypted form instead of plain text
- Add a forgot password option
- Print the bill directly instead of saving it as a text file
- Add sales reports for the admin
- Show a photograph of each dish on the menu
- Save the cart in the database so it is not lost when the application is closed
- Add discounts and taxes to the bill calculation
