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
    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static void pressAnyKeyToContinue() {
        System.out.println("Press Enter key to continue...");
        try {
            System.in.read();
            // scanner.nextLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        clearScreen();
    }

    private static final Scanner scanner = new Scanner(System.in);
    public static String dbAddress = "jdbc:oracle:thin:@db18.cse.cuhk.edu.hk:1521/oradb.cse.cuhk.edu.hk";
    public static String dbUsername = "h043"; // replace with your actual username
    public static String dbPassword = "icedJalj"; // replace with your actual password
    public static LocalDate latestOrderDate = null;

    private static void initializeDate(Connection conn) {
        Path datePath = Paths.get("system_date.txt");
        if (Files.exists(datePath)) {
            try {
                String dateString = new String(Files.readAllBytes(datePath));
                latestOrderDate = LocalDate.parse(dateString);
                System.out.println("Loaded system date from file: " + latestOrderDate);
            } catch (IOException | DateTimeParseException e) {
                System.out.println("Failed to load date from file, loading from database instead.");
                latestOrderDate = getLatestOrderDate(conn);
            }
        } else {
            latestOrderDate = getLatestOrderDate(conn);
        }
    }

    public static void main(String[] args) {
        clearScreen();
        Connection conn;
        conn = connectToOracle();
        initializeDate(conn);
        int choice;
        do {
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

    public static Connection connectToOracle() {
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
        clearScreen();
        // Show system interface menu
        while (true) {

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
                    clearScreen();
                    return; // Go back to the main menu
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
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
                String tableName = sql.split("\\s+")[2];
                System.out.println("Created table " + tableName);
            } catch (SQLException e) {
                String tableName = sql.split("\\s+")[2];
                System.out.println("An error occurred while creating table " + tableName);
                System.out.println(e.getMessage());
                // Break out of the loop if a statement fails to prevent subsequent statements
                // from executing
                // break;
            }
        }
        pressAnyKeyToContinue();
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
                String tableName = sql.split("\\s+")[2];
                System.out.println("Dropped table " + tableName);
            } catch (SQLException e) {
                String tableName = sql.split("\\s+")[2];
                System.out.println("An error occurred while dropping table " + tableName);
                System.out.println(e.getMessage());
                // Break out of the loop if a statement fails to prevent subsequent statements
                // from executing
                // break;
            }
        }
        pressAnyKeyToContinue();
    }

    public static void insertData(Connection conn, Scanner scanner) {
        System.out.print("Please enter the folder path: ");
        String folderPath = scanner.next();

        // Define the expected data files mapped to their corresponding SQL INSERT
        // statements.
        Map<String, String> dataFilesToTable = new LinkedHashMap<>();
        dataFilesToTable.put("book.txt",
                "INSERT INTO book (ISBN, title, unit_price, no_of_copies) VALUES (?, ?, ?, ?)");
        dataFilesToTable.put("book_author.txt", "INSERT INTO book_author (ISBN, author_name) VALUES (?, ?)");
        dataFilesToTable.put("customer.txt",
                "INSERT INTO customer (customer_id, name, shipping_address, credit_card_no) VALUES (?, ?, ?, ?)");
        dataFilesToTable.put("orders.txt",
                "INSERT INTO orders (order_id, order_date, shipping_status, charge, customer_id) VALUES (?, TO_DATE(?, 'YYYY-MM-DD'), ?, ?, ?)");
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
        // System.out.println("All data files have been processed.");
        pressAnyKeyToContinue();
    }

    public static void setSystemDate(Connection conn, Scanner scanner) {
        System.out.print("Please input the new system date (YYYY-MM-DD): ");
        String userInput = scanner.next();
        LocalDate newDate;

        try {
            newDate = LocalDate.parse(userInput);
            if (latestOrderDate != null && newDate.isBefore(latestOrderDate)) {
                System.out.println("The new date cannot be before the latest order date.");
            } else {
                latestOrderDate = newDate;  // Update the global variable
                updateDateFile(newDate);   // Update the date file
                System.out.println("System date set to " + newDate);
            }
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Please use the format YYYY-MM-DD.");
        }
        pressAnyKeyToContinue();
    }

    private static void updateDateFile(LocalDate date) {
        Path datePath = Paths.get("system_date.txt");
        try {
            Files.write(datePath, date.toString().getBytes());
            System.out.println("System date saved to file.");
        } catch (IOException e) {
            System.out.println("Failed to save system date to file: " + e.getMessage());
        }
    }

    // (Not sure)
    // public static void updateSystemDateInDatabase(Connection conn, LocalDate
    // newDate) {
    // String sql = "UPDATE system_config SET system_date = ?";
    // try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
    // pstmt.setDate(1, Date.valueOf(newDate)); // Convert LocalDate to
    // java.sql.Date
    // int rowsAffected = pstmt.executeUpdate();
    // if (rowsAffected > 0) {
    // System.out.println("System date updated in database to " + newDate);
    // } else {
    // System.out.println("System date was not updated, no matching record found.");
    // }
    // } catch (SQLException e) {
    // System.out.println("Error occurred while updating the system date in
    // database:");
    // e.printStackTrace();
    // }
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
        clearScreen();
        while (true) {

            System.out.println("<This is the customer interface.>");
            // TODO: print -------
            System.out.println("1. Book Search.");
            System.out.println("2. Order Creation.");
            System.out.println("3. Order Altering.");
            System.out.println("4. Order Query.");
            System.out.println("5. Back to main menu.");
            System.out.println("");
            System.out.print("What is your choice?..");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    bookSearch(conn, scanner);
                    break;
                case 2:
                    orderCreation(conn, scanner);
                    break;
                case 3:
                    orderAltering(conn, scanner);
                    break;
                case 4:
                    orderQuery(conn, scanner);
                    break;
                case 5:
                    System.out.println("Returning to main menu...");
                    clearScreen();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }

    }

    private static void bookSearch(Connection conn, Scanner scanner) {
        clearScreen();
        while (true) {
            System.out.println("What do u want to search?");
            System.out.println("1 ISBN");
            System.out.println("2 Book Title");
            System.out.println("3 Author Name");
            System.out.println("4 Exit");
            System.out.print("Your choice?... ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    searchByISBN(conn, scanner);
                    break;
                case 2:
                    searchByBookTitle(conn, scanner);
                    break;
                case 3:
                    searchByAuthorName(conn, scanner);
                    break;
                case 4:
                    clearScreen();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }

    }

    private static void executeBookQuery(Connection conn, String query, String searchParameter) {
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, searchParameter);

            ResultSet rs = stmt.executeQuery();

            int numOfRecords = 0;
            System.out.println();
            while (rs.next()) {
                numOfRecords++;
                System.out.println("Record " + numOfRecords);
                System.out.println("ISBN: " + rs.getString("ISBN"));
                System.out.println("Book Title: " + rs.getString("title"));
                System.out.println("Unit Price: " + rs.getString("unit_price"));
                System.out.println("No Of Available: " + rs.getString("no_of_copies"));
                System.out.println("Authors:");
                String[] authors = rs.getString("authors").split(",");
                for (int i = 0; i < authors.length; i++) {
                    System.out.println((i + 1) + " :" + authors[i].trim());
                }
                System.out.println();
            }

            if (numOfRecords == 0) {
                System.out.println("No results found.");
            }

            pressAnyKeyToContinue();
        } catch (SQLException e) {
            System.out.println("An error occurred while searching: " + e.getMessage());
            pressAnyKeyToContinue();
        }
    }

    private static void searchByISBN(Connection conn, Scanner scanner) {
        // clearScreen();
        System.out.print("Input the ISBN: ");
        String isbn = scanner.next();

        String query = "SELECT b.ISBN, b.title, b.unit_price, b.no_of_copies, " +
                "LISTAGG(a.author_name, ', ') WITHIN GROUP (ORDER BY a.author_name) as authors " +
                "FROM book b LEFT JOIN book_author a ON b.ISBN = a.ISBN " +
                "WHERE b.ISBN = ? " +
                "GROUP BY b.ISBN, b.title, b.unit_price, b.no_of_copies " +
                "ORDER BY b.title ASC, b.ISBN ASC";
        executeBookQuery(conn, query, isbn);
    }

    private static void searchByBookTitle(Connection conn, Scanner scanner) {
        // clearScreen();
        System.out.print("Input the Book Title (use '%' for wildcards): ");
        scanner.nextLine();
        String bookTitle = scanner.nextLine();
        String query = "SELECT b.title, b.ISBN, b.unit_price, b.no_of_copies, LISTAGG(ba.author_name, ', ') WITHIN GROUP (ORDER BY ba.author_name) AS authors "
                +
                "FROM book b " +
                "JOIN book_author ba ON b.ISBN = ba.ISBN " +
                "WHERE LOWER(b.title) LIKE LOWER(?) " +
                "GROUP BY b.title, b.ISBN, b.unit_price, b.no_of_copies " +
                "ORDER BY b.title, b.ISBN";
        // System.out.println(bookTitle);
        executeBookQuery(conn, query, bookTitle);

    }

    private static void searchByAuthorName(Connection conn, Scanner scanner) {
        System.out.print("Input the Author Name (use '%' for wildcards): ");
        scanner.nextLine();
        String author = scanner.nextLine();
        String query = "SELECT b.title, b.ISBN, b.unit_price, b.no_of_copies, LISTAGG(ba.author_name, ', ') WITHIN GROUP (ORDER BY ba.author_name) AS authors "
                +
                "FROM book b " +
                "JOIN book_author ba ON b.ISBN = ba.ISBN " +
                "WHERE LOWER(ba.author_name) LIKE LOWER(?) " +
                "GROUP BY b.title, b.ISBN, b.unit_price, b.no_of_copies " +
                "ORDER BY b.title, b.ISBN";
        executeBookQuery(conn, query, author);
    }

    // Stub for orderCreation
    private static void orderCreation(Connection conn, Scanner scanner) {
        try {
            // Disable auto-commit to start the transaction block
            conn.setAutoCommit(false);
    
            System.out.print("Please enter your customer ID: ");
            String customerId = scanner.next();
    
            // Check if customer ID exists in the database
            if (!customerExists(conn, customerId)) {
                System.out.println("Customer ID does not exist.");
                pressAnyKeyToContinue();
                return;
            }
    
            // If customer exists, proceed to book ordering
            Map<String, Integer> orderedBooks = new HashMap<>();
            String isbn;
            System.out.println(">> What books do you want to order?");
            System.out.println(">> Input ISBN and then the quantity.");
            System.out.println(">> You can press 'L' to see ordered list, or 'F' to finish ordering.");
            do {
                System.out.print("Please enter the book's ISBN: ");
                isbn = scanner.next();
    
                if ("L".equalsIgnoreCase(isbn)) {
                    listOrderedBooks(orderedBooks);
                    continue;
                }
    
                if ("F".equalsIgnoreCase(isbn)) {
                    break;
                }
    
                System.out.print("Please enter the quantity of the order: ");
                int quantity = scanner.nextInt();
    
                // Check book availability and add to the order if available
                if (checkBookAvailability(conn, isbn, quantity)) {
                    orderedBooks.put(isbn, orderedBooks.getOrDefault(isbn, 0) + quantity);
                } else {
                    System.out.println("Requested quantity not available for ISBN: " + isbn);
                }
    
            } while (true);
    
            if (!orderedBooks.isEmpty()) {
                createAndInsertOrder(conn, customerId, orderedBooks);
            } else {
                System.out.println("No books were added to the order.");
            }
    
            // Commit the transaction
            conn.commit();
            System.out.println("Order created successfully.");
    
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException se) {
                System.out.println("Error during transaction rollback: " + se.getMessage());
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
        pressAnyKeyToContinue();

    }

    private static boolean customerExists(Connection conn, String customerId) throws SQLException {
        String query = "SELECT COUNT(*) FROM CUSTOMER WHERE CUSTOMER_ID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    private static void listOrderedBooks(Map<String, Integer> orderedBooks) {
        if (orderedBooks.isEmpty()) {
            System.out.println("ISBN\tNumber:");
            return;
        }
        int maxIsbnLength = orderedBooks.keySet().stream().map(String::length).max(Integer::compare).orElse(0);

        // Header
        System.out.printf("%-" + maxIsbnLength + "s Number:\n", "ISBN");
    
        // Rows
        for (Map.Entry<String, Integer> entry : orderedBooks.entrySet()) {
            String isbn = entry.getKey();
            Integer quantity = entry.getValue();
            System.out.printf("%-" + maxIsbnLength + "s %d\n", isbn, quantity);
        }
    }
    
    // Checks if the requested number of book copies is available
    private static boolean checkBookAvailability(Connection conn, String isbn, int requestedQuantity) throws SQLException {
        String checkAvailabilityQuery = "SELECT NO_OF_COPIES FROM BOOK WHERE ISBN = ?";
        try (PreparedStatement stmt = conn.prepareStatement(checkAvailabilityQuery)) {
            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int availableCopies = rs.getInt("NO_OF_COPIES");
                return availableCopies >= requestedQuantity;
            }
        }
        return false;
    }
    
    private static void createAndInsertOrder(Connection conn, String customerId, Map<String, Integer> orderedBooks) throws SQLException {

        String newOrderId = generateNewOrderId(conn);
        double totalCharge = calculateTotalCharge(conn, orderedBooks);
    
        // insert new order
        String insertOrderQuery = "INSERT INTO ORDERS (ORDER_ID, ORDER_DATE, SHIPPING_STATUS, CHARGE, CUSTOMER_ID) VALUES (?, CURRENT_DATE, 'N', ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertOrderQuery)) {
            stmt.setString(1, newOrderId);
            stmt.setDouble(2, totalCharge);
            stmt.setString(3, customerId);
            stmt.executeUpdate();
        }
    
        // insert ordered books and update book copies
        for (Map.Entry<String, Integer> entry : orderedBooks.entrySet()) {
            String isbn = entry.getKey();
            int quantity = entry.getValue();
    
            String insertOrderingQuery = "INSERT INTO ORDERING (ORDER_ID, ISBN, QUANTITY) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertOrderingQuery)) {
                stmt.setString(1, newOrderId);
                stmt.setString(2, isbn);
                stmt.setInt(3, quantity);
                stmt.executeUpdate();
            }
    
            String updateBookCopiesQuery = "UPDATE BOOK SET NO_OF_COPIES = NO_OF_COPIES - ? WHERE ISBN = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateBookCopiesQuery)) {
                stmt.setInt(1, quantity);
                stmt.setString(2, isbn);
                stmt.executeUpdate();
            }
        }
    }
    
    private static String generateNewOrderId(Connection conn) throws SQLException {
        String getMaxOrderIdQuery = "SELECT MAX(ORDER_ID) FROM ORDERS";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(getMaxOrderIdQuery)) {
            if (rs.next()) {
                String maxOrderId = rs.getString(1);
                return String.format("%08d", Integer.parseInt(maxOrderId.trim()) + 1);
            }
        }
        return "00000000"; // default 
    }
    
    // Calculate the total charge for the order
    private static double calculateTotalCharge(Connection conn, Map<String, Integer> orderedBooks) throws SQLException {
        double totalCharge = 0.0;
        for (Map.Entry<String, Integer> entry : orderedBooks.entrySet()) {
            String isbn = entry.getKey();
            int quantity = entry.getValue();
            String getPriceQuery = "SELECT UNIT_PRICE FROM BOOK WHERE ISBN = ?";
            try (PreparedStatement stmt = conn.prepareStatement(getPriceQuery)) {
                stmt.setString(1, isbn);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    double unitPrice = rs.getDouble("UNIT_PRICE");
                    totalCharge += unitPrice * quantity;
                }
            }
        }
        return totalCharge;
    }
    
    // Stub for orderAltering
    private static void orderAltering(Connection conn, Scanner scanner) {
        try{
            conn.setAutoCommit(false);

            System.out.print("Please enter the OrderID that you want to change: ");
            String orderId = scanner.next();
            if (!displayOrderDetails(conn, orderId)) {
                System.out.println("Order ID not found or already shipped.");
                pressAnyKeyToContinue();
                return;
            }
    
            List<String> orderedBookIsbns = displayOrderedBooks(conn, orderId);
            System.out.print("Which book you want to alter (input book no.): ");
            int bookNo = scanner.nextInt();
            if (bookNo < 1 || bookNo > orderedBookIsbns.size()) {
                System.out.println("Invalid book number.");
                pressAnyKeyToContinue();
                return;
            }
            String selectedIsbn = orderedBookIsbns.get(bookNo - 1);

            System.out.print("input 'add' or 'remove': ");
            String action = scanner.next();
            System.out.print("Input the number: ");
            int quantity = scanner.nextInt();

            if ("add".equalsIgnoreCase(action)) {
                addCopiesToOrder(conn, orderId, selectedIsbn, quantity);
                System.out.println("Current Order Details:");
                displayOrderDetails(conn, orderId);
                displayOrderedBooks(conn, orderId);
            } else if ("remove".equalsIgnoreCase(action)) {
                removeCopiesFromOrder(conn, orderId, selectedIsbn, quantity);
                System.out.println("Current Order Details:");
                displayOrderDetails(conn, orderId);
                displayOrderedBooks(conn, orderId);
            } else {
                System.out.println("Invalid action.");
            }

            conn.commit();
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException se) {
                System.out.println("Error during transaction rollback: " + se.getMessage());
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
        pressAnyKeyToContinue();
    
    }

    private static boolean displayOrderDetails(Connection conn, String orderId) throws SQLException {
        String orderDetailsQuery = "SELECT ORDER_ID, SHIPPING_STATUS, CHARGE, CUSTOMER_ID FROM ORDERS WHERE ORDER_ID = ? AND SHIPPING_STATUS = \'N\'";
        try (PreparedStatement stmt = conn.prepareStatement(orderDetailsQuery)) {
            stmt.setString(1, orderId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && "N".equals(rs.getString("SHIPPING_STATUS").trim())) {
                System.out.printf("order_id:%s shipping:%s charge=%.2f customerId=%s\n",
                    rs.getString("ORDER_ID").trim(),
                    rs.getString("SHIPPING_STATUS").trim(),
                    rs.getDouble("CHARGE"),
                    rs.getString("CUSTOMER_ID").trim());
                return true;
            }
        }
        return false;
    }

    private static List<String> displayOrderedBooks(Connection conn, String orderId) throws SQLException {
        String orderedBooksQuery = "SELECT ISBN, QUANTITY FROM ORDERING WHERE ORDER_ID = ?";
        List<String> isbns = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(orderedBooksQuery)) {
            stmt.setString(1, orderId);
            ResultSet rs = stmt.executeQuery();
            int bookNo = 1;
            while (rs.next()) {
                String isbn = rs.getString("ISBN").trim();
                int quantity = rs.getInt("QUANTITY");
                System.out.printf("book no: %d ISBN = %s quantity = %d\n", bookNo++, isbn, quantity);
                isbns.add(isbn);
            }
        }
        return isbns;
    }
    
    private static void addCopiesToOrder(Connection conn, String orderId, String isbn, int additionalQuantity) throws SQLException {
        if (!checkBookAvailability(conn, isbn, additionalQuantity)) {
            System.out.println("Insufficient copies available to add.");
            return;
        }
    
        // Update the ordering table
        String updateOrdering = "UPDATE ordering SET quantity = quantity + ? WHERE order_id = ? AND ISBN = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateOrdering)) {
            pstmt.setInt(1, additionalQuantity);
            pstmt.setString(2, orderId);
            pstmt.setString(3, isbn);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("Failed to add copies to the order.");
                return;
            }
        }
    
        // Update the book table
        String updateBook = "UPDATE book SET no_of_copies = no_of_copies - ? WHERE ISBN = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateBook)) {
            pstmt.setInt(1, additionalQuantity);
            pstmt.setString(2, isbn);
            pstmt.executeUpdate();
        }
    
        // Update the order charge and date
        updateOrderChargeAndDate(conn, orderId, isbn, additionalQuantity, true);
    }

    private static void updateOrderChargeAndDate(Connection conn, String orderId, String isbn, int quantityChange, boolean isAdding) throws SQLException {
        // Retrieve current unit price
        double unitPrice = getBookUnitPrice(conn, isbn);
    
        // Calculate charge difference
        double chargeDiff = unitPrice * quantityChange;
        if (!isAdding) {
            chargeDiff = -chargeDiff;
        }
    
        // Update the orders table
        String updateOrders = "UPDATE orders SET charge = charge + ?, order_date = CURRENT_DATE WHERE order_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateOrders)) {
            pstmt.setDouble(1, chargeDiff);
            pstmt.setString(2, orderId);
            pstmt.executeUpdate();
        }
        System.out.println("Updated!!!");
    }

    private static double getBookUnitPrice(Connection conn, String isbn) throws SQLException {
        String query = "SELECT unit_price FROM book WHERE ISBN = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, isbn);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("unit_price");
            }
        }
        return 0.0;
    }
    
    private static void removeCopiesFromOrder(Connection conn, String orderId, String isbn, int removedQuantity) throws SQLException {
        // update the ordering table
        String updateOrdering = "UPDATE ordering SET quantity = quantity - ? WHERE order_id = ? AND ISBN = ? AND quantity >= ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateOrdering)) {
            pstmt.setInt(1, removedQuantity);
            pstmt.setString(2, orderId);
            pstmt.setString(3, isbn);
            pstmt.setInt(4, removedQuantity);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("Failed to remove copies from the order. Check if sufficient quantity is present.");
                return;
            }
        }
    
        // update the book table
        String updateBook = "UPDATE book SET no_of_copies = no_of_copies + ? WHERE ISBN = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateBook)) {
            pstmt.setInt(1, removedQuantity);
            pstmt.setString(2, isbn);
            pstmt.executeUpdate();
        }
    
        // Update the order charge and date
        updateOrderChargeAndDate(conn, orderId, isbn, removedQuantity, false);
    }

    private static void orderQuery(Connection conn, Scanner scanner) {
        System.out.print("Please Input Customer ID: ");
        String customerId = scanner.next();
        System.out.print("Please Input the Year: ");
        int year = scanner.nextInt();

        String query = 
        "SELECT o.order_id, o.order_date, LISTAGG(b.title || ' (' || ob.quantity || ' copies ordered)', ', ') WITHIN GROUP (ORDER BY b.title) AS books_ordered, " +
        "o.charge, o.shipping_status " +
        "FROM orders o " +
        "JOIN ordering ob ON o.order_id = ob.order_id " +
        "JOIN book b ON ob.ISBN = b.ISBN " +
        "WHERE o.customer_id = ? AND EXTRACT(YEAR FROM o.order_date) = ? " +
        "GROUP BY o.order_id, o.order_date, o.charge, o.shipping_status " +
        "ORDER BY o.order_id";

        try(PreparedStatement pstmt = conn.prepareStatement(query)){
            pstmt.setString(1,customerId);
            pstmt.setInt(2, year);

            ResultSet rs = pstmt.executeQuery();
            int count = 0;
            while(rs.next()){
                ++count;
                System.out.println("Record : " + count);
                System.out.println("OrderID : " + rs.getString("order_id"));
                System.out.println("OrderDate : " + rs.getDate("order_date").toString());
                System.out.println("Books Ordered : " + rs.getString("books_ordered"));
                System.out.println("Charge : " + rs.getDouble("charge"));
                System.out.println("Shipping Status : " + rs.getString("shipping_status").charAt(0));
                System.out.println();
            }
            if(count == 0)
                System.out.println("No Records Found!");
        } catch (SQLException e) {
            System.out.println("An error occurred while querying the orders: " + e.getMessage());
        }
        pressAnyKeyToContinue();
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
        int bookNumber;

        try {
            bookNumber = scanner.nextInt();
            scanner.nextLine(); // clear the buffer
            if (bookNumber <= 0) {
                System.out.println("Please enter a positive number.");
                System.out.println();
                
                return;
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter an integer.");
            System.out.println();

            scanner.nextLine(); // clear the buffer to handle the wrong input
            return;
        }

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
                System.out.println(String.format("%-15s %-20s %-5s", "ISBN", "Title", "Copies"));
                do {
                    String title = rs.getString("title");
                    String isbn = rs.getString("ISBN");
                    int totalOrdered = rs.getInt("total_ordered");
                    System.out.println(String.format("%-15s %-5s %-5d", isbn, title, totalOrdered));
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
