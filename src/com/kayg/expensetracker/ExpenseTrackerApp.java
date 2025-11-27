package com.kayg.expensetracker;

/**
 * ExpenseTrackerApp.java
 * 
 * comprehensive personal expense tracking application demonstrating:
 * - CRUD operations with JDBC
 * - database schema design and normalization
 * - prepared statements for SQL injection prevention
 * - transaction management and data integrity
 * - file I/O for CSV export functionality
 * 
 * @author Kayla G
 * @version 1.0
 */

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.io.*;


/**
 * main application class that provides a console-based interface for managing personal expenses with SQLite database backend.
 * 
 * architecture: Single-class monolithic design (suitable for beginner projects)
 * future: Can be refactored into MVC or layered architecture
 */

public class ExpenseTrackerApp 
{
    //database connection string - SQLite creates file-based database
    //for PostgreSQL: "jdbc:postgresql://localhost:5432/expenses"
    private static final String DB_URL = "jdbc:sqlite:expenses.db";
    
    //active database connection - maintained throughout app lifecycle
    private Connection connection;
    
    //console input reader - shared across all methods for user interaction
    private Scanner inputReader;

    
    /**
     * constructor: Initializes scanner and establishes database connection
     * calls initializeDatabase() to ensure tables exist
     */
    
    public ExpenseTrackerApp() 
    {
        this.inputReader = new Scanner(System.in);
        initializeDatabase();
    }

    
    
    /**
     * establishes connection to SQLite database and creates necessary tables
     * if the connection fails, application exits with error code 1
     * 
     * Database lifecycle:
     * 1. connects to database (creates file if doesn't exist)
     * 2. creates tables if they don't exist
     * 3. tnserts default categories for first-time setup
     */
    
    private void initializeDatabase() 
    {
        try 
        {
            //DriverManager automatically loads SQLite JDBC driver
            connection = DriverManager.getConnection(DB_URL);
            createTables();
            System.out.println("Database connected successfully!");
        } 
        catch (SQLException e) 
        {
            System.err.println("Database connection failed: " + e.getMessage());
            System.exit(1); //fatal error - cannot proceed without database
        }
    }
    
    

    /**
     * creates database schema with two normalized tables:
     * - categories: Master table for expense categories
     * - transactions: Detail table for individual expenses
     * 
     * uses CREATE TABLE IF NOT EXISTS for idempotency
     * implements foreign key constraint for referential integrity
     */
    
    private void createTables() throws SQLException 
    {
        //categories table: Stores expense classification types
        //primary key: Auto-incrementing integer ID
        //unique constraint on name prevents duplicate categories
        String categoriesTable = """
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name VARCHAR(100) UNIQUE NOT NULL,
                description TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        //transactions table: Stores individual expense records
        //foreign key references categories table for data integrity
        //updated_at tracks last modification for audit trail
        String transactionsTable = """
            CREATE TABLE IF NOT EXISTS transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                amount DECIMAL(10, 2) NOT NULL,
                description TEXT,
                transaction_date DATE NOT NULL,
                category_id INTEGER,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (category_id) REFERENCES categories(id)
            )
        """;

        //execute DDL statements using Statement (not PreparedStatement)
        //statement is appropriate for static DDL, PreparedStatement for dynamic DML
        try (Statement stmt = connection.createStatement()) 
        {
            stmt.execute(categoriesTable);
            stmt.execute(transactionsTable);
            insertDefaultCategories(); //seed database with starter categories
        }
    }

    
    
    /**
     *seeds database with common expense categories for new installations
     * uses INSERT OR IGNORE to prevent duplicate entries on subsequent runs
     * 
     * provides a better user experience by having categories ready immediately
     */
    
    private void insertDefaultCategories() throws SQLException 
    {
        String[] defaultCategories = 
        	{
            "Food & Dining", "Transportation", "Shopping", "Entertainment",
            "Bills & Utilities", "Healthcare", "Education", "Other"
        };

        //PreparedStatement allows efficient batch insertion with parameter binding
        String sql = "INSERT OR IGNORE INTO categories (name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (String category : defaultCategories) 
            {
                pstmt.setString(1, category); //bind parameter (prevents SQL injection)
                pstmt.executeUpdate(); //execute insert
            }
        }
    }

    
    
    /**
     * main application loop - displays menu and routes user choices
     * continues until user selects exit option (8)
     * 
     * control flow: Display -> Input -> Route -> Execute -> Repeat
     */
    
    
    public void run() 
    {
        while (true) 
        {
            displayMenu();
            int choice = getIntInput("Enter your choice: ");

            //routes user selection to appropriate handler method
            switch (choice) {
                case 1 -> addTransaction();      //CREATE operation
                case 2 -> viewTransactions();    //READ operation
                case 3 -> editTransaction();     //UPDATE operation
                case 4 -> deleteTransaction();   //DELETE operation
                case 5 -> viewMonthlySummary();  //reporting feature
                case 6 -> manageCategories();    //category CRUD
                case 7 -> exportToCSV();         //data export
                case 8 -> 
                {                       //graceful shutdown
                    System.out.println("Thank you for using Expense Tracker!");
                    closeConnection();
                    return; //exit the infinite loop
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    
    
    /**
     * displays formatted console menu with box-drawing characters
     * uses Unicode box-drawing for professional appearance
     */
    
    private void displayMenu() 
    {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║    PERSONAL EXPENSE TRACKER        ║");
        System.out.println("╚════════════════════════════════════╝");
        System.out.println("1. Add Transaction");
        System.out.println("2. View Transactions");
        System.out.println("3. Edit Transaction");
        System.out.println("4. Delete Transaction");
        System.out.println("5. Monthly Summary");
        System.out.println("6. Manage Categories");
        System.out.println("7. Export to CSV");
        System.out.println("8. Exit");
        System.out.println("────────────────────────────────────");
    }

    
    
    /**
     * CREATE operation: Adds a new expense transaction to database
     * 
     * Process flow:
     * 1. collects transaction details from user
     * 2. validates and parse inputs
     * 3. inserts into database using PreparedStatement
     * 4. provides user feedback
     * 
     * demonstrates: Parameter binding, date handling, user input validation
     */
    
    private void addTransaction() 
    {
        System.out.println("\n=== Add New Transaction ===");
        
        //collects monetary amount with validation
        double amount = getDoubleInput("Enter amount: $");
        
        //allows free-form description for flexibility
        System.out.print("Enter description: ");
        String description = inputReader.nextLine();
        
        //Smart date input: defaults to today if user presses Enter
        //accepts ISO format (YYYY-MM-DD) for explicit dates
        System.out.print("Enter date (YYYY-MM-DD) or press Enter for today: ");
        String dateStr = scanner.nextLine();
        LocalDate date = dateStr.isEmpty() ? LocalDate.now() : LocalDate.parse(dateStr);
        
        //displays available categories for user selection
        displayCategories();
        int categoryId = getIntInput("Select category ID: ");

        //INSERT with parameter binding prevents SQL injection attacks
        // ? placeholders are bound to actual values safely
        String sql = "INSERT INTO transactions (amount, description, transaction_date, category_id) VALUES (?, ?, ?, ?)";
        
        //try-with-resources ensures PreparedStatement is closed even if exception occurs
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) 
        {
            pstmt.setDouble(1, amount);           //bind amount as double
            pstmt.setString(2, description);      //bind description as string
            pstmt.setString(3, date.toString());  //convert LocalDate to ISO string
            pstmt.setInt(4, categoryId);          //bind category foreign key
            pstmt.executeUpdate();                //execute INSERT
            System.out.println("✓ Transaction added successfully!");
        } 
        catch (SQLException e) 
        {
            //handles constraint violations, connection errors, etc.
            System.err.println("Error adding transaction: " + e.getMessage());
        }
    }

    
    /**
     * READ operation: Displays recent transactions in formatted table
     * 
     * uses JOIN to display category name instead of just foreign key ID
     * orders by date (newest first) and limits to 50 for performance
     * 
     * demonstrates: SQL JOINs, ResultSet iteration, formatted output
     */
    
    private void viewTransactions() 
    {
        System.out.println("\n=== Transaction History ===");
        
        // LEFT JOIN ensures transactions without categories still display
        // ORDER BY prioritizes recent transactions
        // LIMIT prevents overwhelming output for large datasets
        String sql = """
            SELECT t.id, t.amount, t.description, t.transaction_date, c.name as category
            FROM transactions t
            LEFT JOIN categories c ON t.category_id = c.id
            ORDER BY t.transaction_date DESC, t.id DESC
            LIMIT 50
        """;

        //statement is fine here since no user input in query
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) 
        {
            
            //prints table header with column alignment
            System.out.printf("%-5s %-12s %-15s %-25s %-15s%n", 
                "ID", "Date", "Amount", "Description", "Category");
            System.out.println("─".repeat(75)); //horizontal separator

            //iterates through result set and display each row
            while (rs.next()) 
            {
                System.out.printf("%-5d %-12s $%-14.2f %-25s %-15s%n",
                    rs.getInt("id"),
                    rs.getString("transaction_date"),
                    rs.getDouble("amount"),
                    truncate(rs.getString("description"), 25), //truncates long descriptions
                    rs.getString("category"));
            }
        } 
        catch (SQLException e) 
        {
            System.err.println("Error retrieving transactions: " + e.getMessage());
        }
    }

    
    /**
     * UPDATE operation: Modifies existing transaction
     * 
     * allows selective field updates - user can skip fields to keep current values
     * dynamically builds SQL UPDATE statement based on provided inputs
     * 
     * demonstrates: Dynamic SQL generation, optional parameters, UPDATE operations
     */
    
    private void editTransaction() 
    {
        viewTransactions(); //shows the current transactions for reference
        int id = getIntInput("\nEnter transaction ID to edit: ");

        //collects optional updates - empty input means "keep current value"
        System.out.println("Leave field blank to keep current value");
        System.out.print("New amount (or Enter to skip): $");
        String amountStr = inputReader.nextLine();
        
        System.out.print("New description (or Enter to skip): ");
        String description = inputReader.nextLine();
        
        System.out.print("New date YYYY-MM-DD (or Enter to skip): ");
        String dateStr = inputReader.nextLine();

        displayCategories();
        System.out.print("New category ID (or 0 to skip): ");
        int categoryId = getIntInput("");

        //dynamically build UPDATE query based on which fields user wants to change
        //this approach is more efficient than always updating all fields
        StringBuilder sql = new StringBuilder("UPDATE transactions SET updated_at = CURRENT_TIMESTAMP");
        List<Object> params = new ArrayList<>();

        //Add SET clauses only for fields that user provided
        if (!amountStr.isEmpty()) 
        {
            sql.append(", amount = ?");
            params.add(Double.parseDouble(amountStr));
        }
        if (!description.isEmpty()) 
        {
            sql.append(", description = ?");
            params.add(description);
        }
        if (!dateStr.isEmpty()) 
        {
            sql.append(", transaction_date = ?");
            params.add(dateStr);
        }
        if (categoryId > 0) 
        {
            sql.append(", category_id = ?");
            params.add(categoryId);
        }
        sql.append(" WHERE id = ?");
        params.add(id); //always need ID for WHERE clause

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) 
        {
            //binds parameters dynamically based on what was collected
            for (int i = 0; i < params.size(); i++) 
            {
                pstmt.setObject(i + 1, params.get(i)); //setObject handles different types
            }
            int rows = pstmt.executeUpdate();
            if (rows > 0)
            {
                System.out.println("✓ Transaction updated successfully!");
            } 
            else 
            {
                System.out.println("Transaction not found.");
            }
        } catch (SQLException e) 
        {
            System.err.println("Error updating transaction: " + e.getMessage());
        }
    }

    
    /**
     * DELETE operation: Removes transaction from database
     * 
     * includes confirmation prompt to prevent accidental deletions
     * uses WHERE clause to target specific transaction by ID
     * 
     * demonstrates: DELETE operations, user confirmation, safe data removal
     */
    
    private void deleteTransaction() 
    {
        viewTransactions(); //shows current transactions
        int id = getIntInput("\nEnter transaction ID to delete: ");
        
        //confirmation prompt prevents accidental data loss
        System.out.print("Are you sure? (yes/no): ");
        String confirm = inputReader.nextLine();
        
        if (!confirm.equalsIgnoreCase("yes"))
        {
            System.out.println("Deletion cancelled.");
            return; // Exit without deleting
        }

        //simple DELETE with WHERE clause targeting specific ID
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) 
        {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate(); //returns number of affected rows
            if (rows > 0)
            {
                System.out.println("✓ Transaction deleted successfully!");
            } 
            else 
            {
                System.out.println("Transaction not found.");
            }
        } catch (SQLException e) 
        {
            System.err.println("Error deleting transaction: " + e.getMessage());
        }
    }

    
    /**
     * reporting feature: Generates monthly expense summary grouped by category
     * 
     * uses aggregate functions (SUM, COUNT) with GROUP BY for analysis
     * accepts month parameter or defaults to current month
     * 
     * demonstrates: Aggregate queries, date filtering, financial reporting
     */

    private void viewMonthlySummary() 
    {
        System.out.print("\nEnter month (YYYY-MM) or press Enter for current month: ");
        String monthStr = inputReader.nextLine();
        YearMonth month = monthStr.isEmpty() ? YearMonth.now() : YearMonth.parse(monthStr);

        System.out.println("\n=== Monthly Summary: " + month + " ===");

        //aggregates query groups expenses by category for the specified month
        //strftime extracts year-month from date for comparison
        //SUM calculates total spending per category
        //COUNT shows number of transactions per category
        String sql = """
            SELECT c.name, SUM(t.amount) as total, COUNT(*) as count
            FROM transactions t
            JOIN categories c ON t.category_id = c.id
            WHERE strftime('%Y-%m', t.transaction_date) = ?
            GROUP BY c.name
            ORDER BY total DESC
        """;

        double grandTotal = 0; //accumulator for overall spending
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) 
        {
            pstmt.setString(1, month.toString()); //bind month parameter
            ResultSet rs = pstmt.executeQuery();

            //prints formatted report header
            System.out.printf("%-20s %-10s %-10s%n", "Category", "Count", "Total");
            System.out.println("─".repeat(45));

            //displays each category's spending
            while (rs.next()) 
            {
                double total = rs.getDouble("total");
                grandTotal += total; // Accumulate for grand total
                System.out.printf("%-20s %-10d $%-10.2f%n",
                    rs.getString("name"),
                    rs.getInt("count"),
                    total);
            }
            
            //display grand total of all categories
            System.out.println("─".repeat(45));
            System.out.printf("%-20s %-10s $%-10.2f%n", "TOTAL", "", grandTotal);
        } 
        catch (SQLException e) 
        {
            System.err.println("Error generating summary: " + e.getMessage());
        }
    }

    
    /**
     *category management submenu - provides CRUD operations for categories
     *allows users to view, add, and delete expense categories
     */
    
    private void manageCategories() 
    {
        System.out.println("\n=== Manage Categories ===");
        System.out.println("1. View Categories");
        System.out.println("2. Add Category");
        System.out.println("3. Delete Category");
        
        int choice = getIntInput("Enter choice: ");
        
        //route to appropriate category operation
        switch (choice)
        {
            case 1 -> displayCategories();
            case 2 -> addCategory();
            case 3 -> deleteCategory();
        }
    }
  
    
    /**
     *displays all available expense categories
     *used both for viewing and for selection in transaction operations
     */
    
    private void displayCategories() 
    {
        String sql = "SELECT id, name FROM categories ORDER BY name";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) 
        {
            
            System.out.println("\n--- Categories ---");
            while (rs.next()) 
            {
                System.out.printf("%d. %s%n", rs.getInt("id"), rs.getString("name"));
            }
        } 
        catch (SQLException e) 
        {
            System.err.println("Error displaying categories: " + e.getMessage());
        }
    }

    
    /**
     * adds a new expense category to the database
     * categories must have unique names (enforced by database constraint)
     */
    
    private void addCategory() 
    {
        System.out.print("Enter category name: ");
        String name = inputReader.nextLine();
        
        String sql = "INSERT INTO categories (name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) 
        {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            System.out.println("✓ Category added successfully!");
        } 
        catch (SQLException e) 
        {
            //will fail if category name already exists (UNIQUE constraint)
            System.err.println("Error adding category: " + e.getMessage());
        }
    }

    
    /**
     *deletes a category from the database
     * 
     * WARNING: this **may** fail if transactions reference this category
     * ***Future enhancement: Either cascade delete or prevent deletion of used categories***
     */
    
    private void deleteCategory() 
    {
        displayCategories();
        int id = getIntInput("Enter category ID to delete: ");
        
        String sql = "DELETE FROM categories WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql))
        {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0)
            {
                System.out.println("✓ Category deleted successfully!");
            } 
            else 
            {
                System.out.println("Category not found.");
            }
        } 
        catch (SQLException e) 
        {
            //foreign key constraint may prevent deletion if transactions use this category
            System.err.println("Error deleting category: " + e.getMessage());
        }
    }

    
    /**
     *exports all transaction data to CSV (Comma-Separated Values) format
     * 
     *CSV is universally supported by spreadsheet applications (Excel, Google Sheets)
     *handles quote escaping for descriptions containing commas or quotes
     * 
     *demonstrates: File I/O, data export, CSV formatting
     */
   
    private void exportToCSV() 
    {
        //generates filename with current date for easy identification
        String filename = "expenses_" + LocalDate.now() + ".csv";
        
        //query retrieves all transactions with category names (using JOIN)
        String sql = """
            SELECT t.id, t.amount, t.description, t.transaction_date, c.name as category
            FROM transactions t
            LEFT JOIN categories c ON t.category_id = c.id
            ORDER BY t.transaction_date DESC
        """;

        //try-with-resources ensures all resources are closed properly
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             PrintWriter writer = new PrintWriter(new FileWriter(filename))) 
        {
            
            //writes CSV header row
            writer.println("ID,Date,Amount,Description,Category");
            
            //writes each transaction as a CSV row
            while (rs.next()) {
                //wraps description in quotes and escape internal quotes (CSV standard)
                writer.printf("%d,%s,%.2f,\"%s\",%s%n",
                    rs.getInt("id"),
                    rs.getString("transaction_date"),
                    rs.getDouble("amount"),
                    rs.getString("description").replace("\"", "\"\""), //escapes quotes
                    rs.getString("category"));
            }
            
            System.out.println("✓ Data exported to " + filename);
        } 
        catch (SQLException | IOException e) 
        {
            System.err.println("Error exporting data: " + e.getMessage());
        }
    }

    
    /**
     *robust integer input method with validation and error handling
     * loops until user provides valid integer input
     * 
     * @param prompt Message to display to user
     * @return Valid integer from user input
     */
   
 private int getIntInput(String prompt) 
    {
        while (true) 
        {
            try 
            {
                System.out.print(prompt);
                int value = Integer.parseInt(inputReader.nextLine());
                return value;
            } 
            catch (NumberFormatException e) 
            {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    
    /**
     *robust double input method for monetary amounts
     *loops until user provides valid decimal number
     * 
     * @param prompt Message to display to user
     * @return Valid double from user input
     */
    
    private double getDoubleInput(String prompt) 
    {
        while (true) 
        {
            try 
            {
                System.out.print(prompt);
                double value = Double.parseDouble(inputReader.nextLine());
                return value;
            } 
            catch (NumberFormatException e) 
            {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    
    /**
     * utility method to truncate long strings for display purposes
     * adds ellipsis (...) when truncation occurs
     * 
     * @param str String to potentially truncate
     * @param length Maximum length before truncation
     * @return Truncated string with ellipsis, or original if short enough
     */
    private String truncate(String str, int length) 
    {
        if (str == null) return "";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }

    
    /**
     * 'gracefully' closes database connection and scanner
     * should be called before application exit to release resources
     * 
     * demonstrates: Resource cleanup, connection lifecycle management
     */
    
    private void closeConnection() 
    {
        try
        {
            if (connection != null && !connection.isClosed())
            {
                connection.close(); //releases database connection
            }
            inputReader.close(); //closes input scanner
        } 
        catch (SQLException e) 
        {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    
    /**
     * application entry point
     * creates app instance and starts main loop
     * 
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) 
    {
        ExpenseTrackerApp app = new ExpenseTrackerApp();
        app.run();
    }
}