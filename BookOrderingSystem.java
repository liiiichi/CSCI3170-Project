import java.util.Scanner;
import java.sql.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.sql.Date; // For java.sql.Date
import java.util.regex.Pattern; // For match YYYY-MM
// import java.sql.Connection;
// import java.sql.DriverManager;
// import java.sql.PreparedStatement;
// import java.sql.ResultSet;
// import java.sql.SQLException;
// import java.sql.Statement;




public class BookOrderingSystem {

    private static final Scanner scanner = new Scanner(System.in);
    public static String dbAddress = "jdbc:oracle:thin:@db18.cse.cuhk.edu.hk:1521/oradb.cse.cuhk.edu.hk";
    public static String dbUsername = "h043"; // replace with your actual username
    public static String dbPassword = "icedJalj"; // replace with your actual password
    public static void main(String[] args) {
        Connection conn;
        conn = connectToOracle();
        int choice;
        do {
            LocalDate latestOrderDate = getLatestOrderDate(conn);
            System.out.println("The System Date is now: " + (latestOrderDate != null ? latestOrderDate : "0000-00-00"));
            System.out.println("<This is the Book Ordering System.>");
            System.out.println("--------------------------------------------------");
            System.out.println("1. System interface.");
            System.out.println("2. Customer interface.");
            System.out.println("3. Bookstore interface.");
            System.out.println("4. Show System Date.");
            System.out.println("5. Quit the system.");
            System.out.println("--------------------------------------------------");
            System.out.print("Please enter your choice?..");

            choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    showSystemInterface(conn, scanner);
                    break;
                case 2:
                    showCustomerInterface(conn, scanner);
                    break;
                case 3:
                    showBookstoreInterface(conn, scanner);
                    break;
                case 4:
                    System.out.println((latestOrderDate != null ? latestOrderDate : "0000-00-00"));
                    break;
                case 5:
                    System.out.println("Exiting the system...");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 5);

        scanner.close();
    }

    public static Connection connectToOracle(){
        Connection con = null;
        try {
            // This line ensures the driver is registered
            Class.forName("oracle.jdbc.OracleDriver");

            // Establish the connection to the Oracle database
            con = DriverManager.getConnection(dbAddress, dbUsername, dbPassword);
        } catch (ClassNotFoundException e) {
            System.out.println("[Error]: Java Oracle DB Driver not found!!");
            System.exit(0);
        } catch (SQLException e) {
            System.out.println(e);
        }
        return con;
    }

    private static void showSystemInterface(Connection conn, Scanner scanner) {
        // Show system interface menu
        System.out.println("<This is the system interface.>");
        System.out.println("1. Create Table.");
        System.out.println("2. Delete Table.");
        System.out.println("3. Insert Data.");
        System.out.println("4. Set System Date.");
        System.out.println("5. Back to main menu.");
        System.out.print("Please enter your choice?..");

        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                createTable(conn);
                break;
            case 2:
                deleteTable(conn);
                break;
            case 3:
                insertData(conn, scanner);
                break;
            case 4:
                setSystemDate(conn, scanner);
                break;
            case 5:
                return; // Go back to the main menu
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    public static void createTable(Connection conn) {
        String[] createTableSQLs = new String[] {
            "CREATE TABLE book (" +
            "    ISBN VARCHAR2(13) PRIMARY KEY," +
            "    title VARCHAR2(100) NOT NULL," +
            "    unit_price NUMBER NOT NULL," +
            "    no_of_copies NUMBER NOT NULL" +
            ")",
            "CREATE TABLE book_author (" +
            "    ISBN VARCHAR2(13)," +
            "    author_name VARCHAR2(50)," +
            "    CONSTRAINT pk_book_author PRIMARY KEY (ISBN, author_name)," +
            "    CONSTRAINT fk_book_author_book FOREIGN KEY (ISBN) REFERENCES book(ISBN)" +
            ")",
            "CREATE TABLE customer (" +
            "    customer_id VARCHAR2(10) PRIMARY KEY," +
            "    name VARCHAR2(50) NOT NULL," +
            "    shipping_address VARCHAR2(200) NOT NULL," +
            "    credit_card_no VARCHAR2(19) NOT NULL" +
            ")",
            "CREATE TABLE orders (" +
            "    order_id CHAR(8) PRIMARY KEY," +
            "    order_date DATE NOT NULL," +
            "    shipping_status CHAR(1) NOT NULL," +
            "    charge NUMBER NOT NULL," +
            "    customer_id VARCHAR2(10)," +
            "    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id)" +
            ")",
            "CREATE TABLE ordering (" +
            "    order_id CHAR(8)," +
            "    ISBN VARCHAR2(13)," +
            "    quantity NUMBER NOT NULL," +
            "    CONSTRAINT pk_ordering PRIMARY KEY (order_id, ISBN)," +
            "    CONSTRAINT fk_ordering_order FOREIGN KEY (order_id) REFERENCES orders(order_id)," +
            "    CONSTRAINT fk_ordering_book FOREIGN KEY (ISBN) REFERENCES book(ISBN)" +
            ")"
        };

        for (String sql : createTableSQLs) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
                System.out.println("Created table using SQL: " + sql);
            } catch (SQLException e) {
                System.out.println("An error occurred while creating tables:");
                System.out.println(e.getMessage());
                // Break out of the loop if a statement fails to prevent subsequent statements from executing
                break;
            }
        }
    }

    public static void deleteTable(Connection conn) {
        // List of SQL DROP TABLE statements
        String[] dropTableSQLs = {
            // "DROP TABLE category CASCADE CONSTRAINTS",
            "DROP TABLE book_author CASCADE CONSTRAINTS",
            "DROP TABLE book CASCADE CONSTRAINTS",
            "DROP TABLE customer CASCADE CONSTRAINTS",
            "DROP TABLE orders CASCADE CONSTRAINTS",
            "DROP TABLE ordering CASCADE CONSTRAINTS"
        };

        // Execute each DROP TABLE statement
        for (String sql : dropTableSQLs) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
                System.out.println("Dropped table using SQL: " + sql);
            } catch (SQLException e) {
                System.out.println("An error occurred while dropping table with "+ sql);
                System.out.println(e.getMessage());
                // Break out of the loop if a statement fails to prevent subsequent statements from executing
                break;
            }
        }
    }

    public static void insertData(Connection conn, Scanner scanner) {
        System.out.print("Please enter the folder path: ");
        String folderPath = scanner.next();

        // Define the expected data files mapped to their corresponding SQL INSERT statements.
        Map<String, String> dataFilesToTable = new LinkedHashMap<>();
        dataFilesToTable.put("book.txt", "INSERT INTO book (ISBN, title, unit_price, no_of_copies) VALUES (?, ?, ?, ?)");
        dataFilesToTable.put("book_author.txt", "INSERT INTO book_author (ISBN, author_name) VALUES (?, ?)");
        dataFilesToTable.put("customer.txt", "INSERT INTO customer (customer_id, name, shipping_address, credit_card_no) VALUES (?, ?, ?, ?)");
        dataFilesToTable.put("orders.txt", "INSERT INTO orders (order_id, order_date, shipping_status, charge, customer_id) VALUES (?, TO_DATE(?, 'YYYY-MM-DD'), ?, ?, ?)");
        dataFilesToTable.put("ordering.txt", "INSERT INTO ordering (order_id, ISBN, quantity) VALUES (?, ?, ?)");

        // Process each data file and insert the data into the database.
        for (Map.Entry<String, String> entry : dataFilesToTable.entrySet()) {
            String fileName = entry.getKey();
            String sql = entry.getValue();
            Path filePath = Paths.get(folderPath, fileName);

            try (BufferedReader reader = Files.newBufferedReader(filePath);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] dataValues = line.split("\\|");
                    for (int i = 0; i < dataValues.length; i++) {
                        pstmt.setString(i + 1, dataValues[i].trim());
                    }
                    pstmt.executeUpdate();
                }
                System.out.println("Data loaded from " + fileName);
            } catch (IOException e) {
                System.out.println("An error occurred while reading the file: " + fileName);
                e.printStackTrace();
            } catch (SQLException e) {
                System.out.println("An error occurred while inserting data into the database from file: " + fileName);
                e.printStackTrace();
            }
        }
        System.out.println("All data files have been processed.");
    }

    public static void setSystemDate(Connection conn, Scanner scanner) {
        // Fetch the latest order date from the database
        LocalDate latestOrderDate = getLatestOrderDate(conn);

        // Inform the user of the latest order date or indicate that it's not set
        if (latestOrderDate != null) {
            System.out.println("Latest date in orders: " + latestOrderDate);
        } else {
            System.out.println("No orders found. Any date can be set as the system date.");
        }

        // Prompt the user for a new date
        System.out.print("Please input the new system date (YYYYMMDD): ");
        String userInput = scanner.next();
        LocalDate newDate;

        try {
            // Parse the user input into a LocalDate
            newDate = LocalDate.parse(userInput, DateTimeFormatter.BASIC_ISO_DATE);

            // If latestOrderDate is not null and newDate is before it, reject the change
            if (latestOrderDate != null && newDate.isBefore(latestOrderDate)) {
                System.out.println("The new date cannot be before the latest order date.");
            } else {
                // If latestOrderDate is null or newDate is valid, set the new system date
                System.out.println("System date set to " + newDate);
            }
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Please use the format YYYYMMDD.");
        }
    }
    
    // (Not sure)
    // public static void updateSystemDateInDatabase(Connection conn, LocalDate newDate) {
    //     String sql = "UPDATE system_config SET system_date = ?";
    //     try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
    //         pstmt.setDate(1, Date.valueOf(newDate)); // Convert LocalDate to java.sql.Date
    //         int rowsAffected = pstmt.executeUpdate();
    //         if (rowsAffected > 0) {
    //             System.out.println("System date updated in database to " + newDate);
    //         } else {
    //             System.out.println("System date was not updated, no matching record found.");
    //         }
    //     } catch (SQLException e) {
    //         System.out.println("Error occurred while updating the system date in database:");
    //         e.printStackTrace();
    //     }
    // }

    private static LocalDate getLatestOrderDate(Connection conn) {
        String sql = "SELECT MAX(order_date) AS latest_date FROM orders";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                Date latestDate = rs.getDate("latest_date");
                return latestDate != null ? latestDate.toLocalDate() : null;
            }
        } catch (SQLException e) {
            // System.out.println(e.getMessage());
        }
        return null;
    }

    private static void showCustomerInterface(Connection conn, Scanner scanner) {
        // Implement customer interface related options here
        System.out.println("Customer Interface selected.");
        // Implement the interface logic here.
    }

    private static void showBookstoreInterface(Connection conn, Scanner scanner) {
        // Implement bookstore interface related options here
        
        // Implement the interface logic here.
        int choice = -1;
        do{
            System.out.println("<This is the bookstore interface.>");
            System.out.println("------------------------------");
            System.out.println("1. Order Update.");
            System.out.println("2. Order Query.");
            System.out.println("3. N Most Popular Book Query.");
            System.out.println("4. Back to main menu.");
            System.out.println();

            System.out.print("What is your choice??..");

            choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    orderUpdate(conn, scanner);
                    break;
                case 2:
                    orderQuery(conn, scanner);
                    break;
                case 3:
                    nmostPopBook(conn, scanner);
                    break;
                case 4:
                    return; // Go back to the main menu
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 4);

    }

    private static void orderUpdate(Connection conn, Scanner scanner) {

        // scan input
        scanner.nextLine(); // clear the buffer
        System.out.print("Please input the order ID: ");
        String orderId = scanner.nextLine();

        if (!orderId.matches("^[A-Za-z0-9]{8}$")){
            System.out.println("Invalid order ID! Please enter valid order ID ");
            return;
        }

        // Check current shipping status before deciding to update
        String shippingStatus = "";
        try (PreparedStatement checkStatusStmt = conn.prepareStatement(
            "SELECT shipping_status FROM orders WHERE order_id = ?")) {
            checkStatusStmt.setString(1, orderId);
            ResultSet rs = checkStatusStmt.executeQuery();
            if (rs.next()) {
                shippingStatus = rs.getString("shipping_status");
                if ("Y".equals(shippingStatus)) {
                    System.out.println("Shipping status is already 'Y'. No update allowed.");
                    return;
                }
            } else {
                System.out.println("Order ID not found.");
                return;
            }
        } catch (SQLException e) {
            System.out.println("Error checking current shipping status: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // count number of books
        int quantity_sum = 0;
        try (PreparedStatement checkStatusStmt = conn.prepareStatement(
            "SELECT SUM(quantity) AS quantity_sum FROM ordering WHERE order_id = ?")) {
            checkStatusStmt.setString(1, orderId);
            ResultSet rs = checkStatusStmt.executeQuery();
            if (rs.next()) {
                quantity_sum = rs.getInt("quantity_sum");
            } else {
                System.out.println("Error summing the quantity");
                return;
            }
        } catch (SQLException e) {
            System.out.println("Error checking current quantity: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        System.out.println(
            "the Shipping status of 00000003 is " +
            shippingStatus +
            " and "+
            quantity_sum+
            " books ordered"
        );
        System.out.println("Are you sure to update the shipping status? (Yes=Y)");

        String userResponse = scanner.nextLine();
        System.out.println("This is user res " +userResponse);

        if (!("Yes".equals(userResponse) || "Y".equals(userResponse))) {
            System.out.println("Update **CANCELLED**");
            return;
        }

        // if yes
        String update_sql = "UPDATE orders " +
                "SET shipping_status = 'Y' " +
                "WHERE order_id = ? AND shipping_status = 'N' " +
                "AND EXISTS (" +
                "  SELECT 1 " +
                "  FROM ordering " +
                "  WHERE order_id = ? AND quantity >= 1" +
                ")";
        
        try (PreparedStatement pstmt = conn.prepareStatement(update_sql)) {
            // Set parameters for the prepared statement
            pstmt.setString(1, orderId);
            pstmt.setString(2, orderId);

            // Execute the update
            int rowsAffected = pstmt.executeUpdate();
            System.out.println(rowsAffected + " row(s) updated.");

            System.out.println("Updated shiping status");
            
        } catch (SQLException e) {
            System.out.println("Error updating order: " + e.getMessage());
            e.printStackTrace();
        }

        return;
    }

    private static void orderQuery(Connection conn, Scanner scanner) {

        // scan input
        scanner.nextLine(); // clear the buffer
        System.out.print("Please input the Month for Order Query (e.g.2005-09):");
        String yearMonth = scanner.nextLine().trim();
        System.out.println();
        System.out.println();

        // validation here
        if (!Pattern.matches("\\d{4}-\\d{2}", yearMonth)) {
            System.out.println("Invalid input format.");
            return;
        }

        String listOrdersQuery = "SELECT order_id, customer_id, order_date, charge FROM orders WHERE shipping_status = 'Y' AND TO_CHAR(order_date, 'YYYY-MM') = ? ORDER BY order_id";
        String totalChargeQuery = "SELECT SUM(charge) AS total_charge FROM orders WHERE shipping_status = 'Y' AND TO_CHAR(order_date, 'YYYY-MM') = ?";

        try {
            PreparedStatement listStmt = conn.prepareStatement(listOrdersQuery);
            listStmt.setString(1, yearMonth);
            ResultSet rs = listStmt.executeQuery();

            System.out.println("Orders in " + yearMonth + ":");

            int index = 1;
            while (rs.next()) {
                System.out.println("Record : " + index);
                System.out.println("order_id: " + rs.getInt("order_id"));
                System.out.println("customer_id: " + rs.getString("customer_id"));
                System.out.println("date: " + rs.getDate("order_date"));
                System.out.println("charge: " + rs.getDouble("charge"));
                System.out.println();
                System.out.println();
                index++;
            }
            rs.close();
            listStmt.close();

            // Query for total charge
            PreparedStatement totalStmt = conn.prepareStatement(totalChargeQuery);
            totalStmt.setString(1, yearMonth);
            ResultSet rs_1 = totalStmt.executeQuery();

            if (rs_1.next()) {
                double totalCharge = rs_1.getDouble("total_charge");
                if (rs_1.wasNull()) {
                    System.out.println("No charges this month or no data available.");
                } else {
                    System.out.println("Total charge of the month is " + totalCharge);
                }
                System.out.println();
                System.out.println();
            }

            rs_1.close();
            totalStmt.close();


        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
            e.printStackTrace();
        }
        return;
    }

    private static void nmostPopBook(Connection conn, Scanner scanner) {

        scanner.nextLine(); // clear the buffer
        System.out.print("Please input the N popular books number: ");
        int bookNumber = scanner.nextInt();

        String nMostQuery = """
        SELECT title, ISBN, total_ordered
        FROM (
            SELECT b.title, b.ISBN, SUM(o.quantity) AS total_ordered,
                   DENSE_RANK() OVER (ORDER BY SUM(o.quantity) DESC) AS book_rank
            FROM ordering o
            JOIN book b ON o.ISBN = b.ISBN
            GROUP BY b.title, b.ISBN
        ) RankedBooks
        WHERE book_rank <= ?
        ORDER BY total_ordered DESC, title ASC, ISBN ASC
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(nMostQuery)) {
            pstmt.setInt(1, bookNumber);  // Set the rank limit to the user's input
            
            ResultSet rs = pstmt.executeQuery();
            
            // Check if the result set has data
            if (!rs.next()) {
                System.out.println("No books found.");
            } else {
                System.out.println("ISBN\tTitle\tcopies");
                do {
                    String title = rs.getString("title");
                    String isbn = rs.getString("ISBN");
                    int totalOrdered = rs.getInt("total_ordered");
                    System.out.println(title + "\t" + isbn + "\t" + totalOrdered);
                } while (rs.next());
                System.out.println();
            }
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            e.printStackTrace();
        }

        return;
    }
}
