# Viva Quick Reference — Food Ordering System

Read this in 20–30 minutes before the viva.
For the full explanations see `STUDENT_GUIDE.md`.

---

## 1. Thirty-second introduction

> "My project is a Food Ordering System. It is a desktop application made in Java
> using Swing for the screens and MySQL for the database, connected through JDBC.
> A customer can register, log in, see the menu, add food to a cart and place an
> order, and the system prepares the bill. The admin can add and edit food
> categories and food items, see all the orders and change the order status from
> Pending to Delivered."

## 2. One-minute introduction

> "My project is a Food Ordering System built in Java. The user interface is made
> with Java Swing and the data is stored in a MySQL database, and Java connects to
> MySQL using JDBC.
>
> The problem it solves is that in a small restaurant the order is written on a
> paper slip and the bill is added up by hand, so slips get lost, the total can be
> wrong and there is no record of old orders.
>
> There are two users. The login screen is the same for both, and the `role`
> column in the `users` table decides which dashboard opens. The customer browses
> the menu, filters by category or searches by name, adds dishes to a cart and
> places the order — the program calculates the total and shows a bill with a
> unique order number. The admin manages the categories and the menu, can mark a
> dish as not available, sees all the orders and moves each one through Pending,
> Confirmed, Preparing, Ready and Delivered.
>
> The database has five tables joined by foreign keys, and the code has 27 classes
> in three layers. Placing an order writes to two tables in one transaction, so
> either the whole order is saved or nothing is. I tested it with 47 test cases
> and all of them passed."

---

## 3. Technologies

| Technology | Why we used it | Where it is |
|---|---|---|
| **Java (JDK 21)** | In our syllabus; has Swing and JDBC built in | All 27 classes |
| **Java Swing** | Part of the JDK, simplest way to build desktop screens | The whole `ui` package |
| **JDBC** | The standard way for Java to use a database | `DBConnection` + all 4 DAO classes |
| **MySQL 8.0** | Free, in our lab, supports primary and foreign keys | `food_ordering_db`, 5 tables |
| **Maven** | Downloads the MySQL driver, builds the runnable jar | `pom.xml` |
| **Git / GitHub** | History and online backup | The whole repository, `main` branch |

---

## 4. Ten important classes

| # | Class | What it does | Remember this |
|---|---|---|---|
| 1 | `MainApp` | Starting point — has `main()` | Checks the database, then opens LoginFrame |
| 2 | `DBConnection` | Creates the JDBC connection | The only class that knows the URL and password |
| 3 | `Validator` | Checks what the user typed | All methods are `static` |
| 4 | `User` | One row of `users` | `isAdmin()` decides the dashboard |
| 5 | `UserDAO` | SQL of the `users` table | `login()` returns a User or `null` |
| 6 | `Cart` / `CartItem` | The shopping cart | In memory only, not a database table |
| 7 | `FoodItemDAO` | SQL of `food_items` | Its SELECTs join `categories` for the name |
| 8 | `OrderDAO` | SQL of `orders` + `order_items` | `placeOrder()` is the transaction |
| 9 | `LoginFrame` | The login window | Validates, then calls `UserDAO.login()` |
| 10 | `BillDialog` | The bill window | Used after an order, from history and by the admin |

Also worth naming: `AdminDashboard`, `CustomerDashboard`, `MenuPanel`,
`CartPanel`, `CategoryPanel`, `FoodItemPanel`, `AdminOrderPanel`.

**27 classes in 6 packages:** (main) · `db` · `model` · `dao` · `ui` · `util`

---

## 5. The five database tables

| Table | Purpose | Primary key | Foreign keys | Unique |
|---|---|---|---|---|
| `users` | admin + customers | `user_id` | — | `email` |
| `categories` | food groups | `category_id` | — | `category_name` |
| `food_items` | the menu | `food_id` | `category_id` | — |
| `orders` | one row per order | `order_id` | `user_id` | `order_number` |
| `order_items` | dishes of an order | `order_item_id` | `order_id`, `food_id` | — |

**Relationships:**
users 1→N orders · orders 1→N order_items · categories 1→N food_items ·
food_items 1→N order_items

**Two design questions you will be asked:**
- *Why one `users` table for both?* Because they need the same columns; `role`
  tells them apart.
- *Why `orders` and `order_items` separately?* Because the number, date, customer
  and total appear once, while the dish, quantity and rate repeat.

---

## 6. Twenty most important viva questions

1. **What is your project?** A Food Ordering System — a Java desktop application
   where a customer orders food and the admin manages the menu and the orders.
2. **Which technologies?** Java 21, Java Swing, JDBC, MySQL 8.0, Maven, Git.
3. **How many tables?** Five: users, categories, food_items, orders, order_items.
4. **How many classes?** 27, in 6 packages.
5. **Explain the architecture.** Three layers — Swing screens, then the DAO and
   model classes, then DBConnection and the MySQL driver.
6. **What is a DAO?** Data Access Object — a class that holds all the SQL for one
   part of the database. I have four.
7. **Explain the login.** LoginFrame validates the boxes, calls
   `UserDAO.login()`, which runs a SELECT on `users`. If a row comes back it
   becomes a `User` object; the `role` decides which dashboard opens.
8. **What if the password is wrong?** `login()` returns null and the message
   "Invalid email or password" is shown.
9. **How is the cart total calculated?** Subtotal = rate × quantity for each line,
   total = sum of the subtotals. Example: 220×2 + 180×1 + 25×4 + 80×2 = Rs. 880.
10. **What happens when an order is placed?** Check the cart is not empty and
    every dish is available, start a transaction, insert into `orders`, read back
    the `order_id`, insert into `order_items` for every line, commit, show the
    bill, clear the cart.
11. **Why a transaction?** Because one order writes to two tables. If the second
    insert fails, rollback undoes the first, so there is never an order without
    its items.
12. **How is the order number made?** `ORD` + the date and time to the second,
    e.g. `ORD20260905023811`. It is UNIQUE in the database.
13. **What are the order statuses?** Pending, Confirmed, Preparing, Ready,
    Delivered. Only the admin changes them.
14. **What is a primary key?** A column that identifies a row uniquely, e.g.
    `order_id`, filled automatically by AUTO_INCREMENT.
15. **What is a foreign key?** A column pointing at another table's primary key,
    e.g. `orders.user_id` → `users.user_id`. It keeps the data correct.
16. **What is CRUD?** Create, Read, Update, Delete — INSERT, SELECT, UPDATE,
    DELETE.
17. **What is a PreparedStatement and why use it?** SQL with `?` for the values.
    It prevents SQL injection and is faster. Every query in my project uses one.
18. **Why is there an `available` column?** So the admin can hide a finished dish
    without deleting it, which would break the old orders containing it.
19. **How did you test?** 47 test cases covering every module, plus going through
    the application by hand and checking the tables in MySQL. All passed.
20. **What are the limitations?** No online payment, desktop only, plain-text
    passwords, no forgot-password, the cart is not saved, no reports.

---

## 7. Ten Java questions

1. **Class vs object?** A class is a blueprint, an object is one thing made from
   it. `Category` is a class; `new Category(...)` makes an object.
2. **What is a constructor?** A method with the class's name that runs when the
   object is created, to set the starting values.
3. **What is encapsulation?** Private fields with public getters and setters. All
   seven model classes do this.
4. **Did you use inheritance?** Yes — `LoginFrame extends JFrame`,
   `CategoryPanel extends JPanel`, `BillDialog extends JDialog`.
5. **What is method overriding?** Changing what an inherited method does. I
   override `isCellEditable()` to make tables read only and `toString()` in
   `Category`.
6. **What does `static` mean?** Belongs to the class, not an object, so no `new`
   is needed — e.g. `Validator.isValidEmail(...)`.
7. **Which collection did you use?** `ArrayList` through the `List` interface —
   `List<CartItem>` in `Cart` and the lists returned by the DAO methods.
8. **What is exception handling?** Catching a runtime error so the program shows
   a message instead of stopping. I catch `SQLException` and
   `NumberFormatException`.
9. **What is try-with-resources?** A `try` whose resources close automatically. I
   use it for `Connection`, `PreparedStatement` and `ResultSet`.
10. **What is a package?** A folder grouping related classes. Mine are `db`,
    `model`, `dao`, `ui`, `util` and the main package.

---

## 8. Ten database questions

1. **Which database and why?** MySQL 8.0 — free, in our lab, supports foreign
   keys.
2. **Database name?** `food_ordering_db`.
3. **How is it created?** By running `database/food_ordering_db.sql`.
4. **What is AUTO_INCREMENT?** MySQL gives the next number automatically for a new
   row.
5. **What is a unique key?** A column that cannot repeat but is not the primary
   key — I have `users.email`, `categories.category_name`, `orders.order_number`.
6. **What is a JOIN?** Reading related rows from two tables together. My food
   queries join `food_items` with `categories`; the admin order list joins
   `orders` with `users`.
7. **Why does `order_items` store the price?** It is the rate at the time of
   ordering, so an old bill stays correct after a price change.
8. **Can a category with dishes be deleted?** No. The screen counts the dishes
   first and shows a message, and the foreign key would refuse it anyway.
9. **What sample data is there?** 1 admin, 1 demo customer, 4 categories,
   12 food items.
10. **What happens if MySQL is not running?** `MainApp` tests the connection at
    startup and shows a message explaining what to check.

---

## 9. Ten project questions

1. **Who are the users?** Admin and customer.
2. **How many modules?** Ten — login, registration/profile, category, food menu,
   browsing, cart, order, billing, order history, admin order management.
3. **Where is the cart stored?** In memory only, in a `Cart` object created by
   `CustomerDashboard` and shared with `MenuPanel` and `CartPanel`.
4. **Can a customer see another customer's orders?** No —
   `getOrdersByUser()` filters by the logged-in `user_id`.
5. **How does the admin see the customer's name on an order?** A JOIN between
   `orders` and `users`.
6. **What does logout do?** Confirms, closes the dashboard, clears the cart and
   reopens the login window.
7. **Is there online payment?** No — Cash on Delivery or Pay at Counter only.
8. **Can the bill be printed?** It can be saved as a text file; direct printing is
   a future enhancement.
9. **Why no SQL in the screens?** So every query lives in one place, in a DAO
   class, and a change is made once.
10. **What would you improve first?** Encrypt the passwords.

---

## 10. Common mistakes to avoid during the demonstration

**Before you start**

1. **Start the MySQL service first.** This is the most common demo failure. If
   MySQL is off, the application shows a message and closes.
2. **Run the SQL script beforehand** so the tables and sample data exist.
3. **Check the password in `DBConnection.java`** matches the MySQL password on
   that computer.
4. **Place one test order before the examiner arrives**, so the order history and
   the admin order list are not empty.
5. **Keep the login details written down**: `admin@food.com` / `admin123` and
   `customer@food.com` / `cust123`.

**During the demonstration**

6. **Do not rush.** Click, wait for the screen, then speak.
7. **Do not click the same button twice** because nothing seemed to happen — wait
   for the message box.
8. **Close every message box** before clicking the next thing.
9. **Select a row in the table before clicking Update or Delete.** Forgetting this
   is the most common mistake — the app will tell you, but it looks untidy.
10. **Do not delete the demo data** in front of the examiner. If you must show
    Delete, add something first and delete that.
11. **Do not say "I think" or "maybe".** If you are unsure, say what you do know
    about your project.
12. **Do not read from a paper.** Look at the screen and describe what is
    happening.

**Things that impress an examiner**

13. **Show a wrong login on purpose** and explain that the program handles it.
14. **Show a validation message** — type a negative price or a zero quantity.
15. **Open MySQL and show the rows** that were created by the order you just
    placed. `SELECT * FROM orders;` and `SELECT * FROM order_items;`
16. **Explain the transaction** without being asked.
17. **Mention the availability column** and why a dish is hidden rather than
    deleted.
18. **Be honest about the limitations.** Saying "passwords are plain text and I
    would encrypt them" sounds far better than pretending it is secure.

---

*You built it, you tested it, you understand it. Speak slowly and let the screens
do the explaining.*
