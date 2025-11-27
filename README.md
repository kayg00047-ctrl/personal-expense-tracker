# Personal Expense Tracker

![Java](https://img.shields.io/badge/Java-17+-blue.svg)
![SQLite](https://img.shields.io/badge/SQLite-3.x-green.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)
![Status](https://img.shields.io/badge/Status-Active-success.svg)

A comprehensive personal expense tracking application built with Java and SQLite, featuring full CRUD operations, category management, monthly reporting, and CSV export capabilities.


##  Features
 </src>
<details>
<summary> Transaction Management </summary>
 </src>
 
 - Add, edit, and delete expense transactions
 - Automatic date defaulting with custom date support
 - Detailed transaction descriptions
 - Transaction history with pagination (50 most recent)
 
</details>
 </src>

<details>
<summary> Category System </summary>
 </src>
 
  -  Pre-loaded common expense categories
  -  Add custom categories
  -  Category-based organization
  -  Delete unused categories
  
  </details>
 </src>  
  
  <details>
<summary> Reporting & Analytics </summary>
 </src>
 
  -  Monthly expense summaries
  -  Category-wise spending breakdown
  -  Transaction count and total calculations
  -  Customizable date range queries
    </details>
    
 </src>    
 
 <details>
 <summary>Data Management</summary>
 </src>
 
  -  SQLite database for persistent storage
  -  CSV export for spreadsheet integration
  -  Data validation and error handling
  -  Automatic timestamp tracking (created_at, updated_at)
 </details>
 
 </src>
 
## Technologies Used

- **Language**: Java 17+
- **Database**: SQLite 3.x
- **JDBC**: Direct database connectivity
- **Design Patterns**: Monolithic architecture (with plans for MVC refactoring)
- **Data Format**: CSV export support

## Prerequisites

- Java Development Kit (JDK) 17 or higher
- SQLite JDBC driver (included in `lib/` or via Maven/Gradle)

## Starting

<details>

<summary> Installation (SQLite) </summary>

1. **Clone the repository**
```bash
   git clone https://github.com/yourusername/personal-expense-tracker.git
   cd personal-expense-tracker
```

2. **Compile the application**
```bash
   javac -d bin src/main/java/com/yourname/expensetracker/*.java
```

3. **Run the application**
```bash
   java -cp bin com.yourname.expensetracker.ExpenseTrackerApp
```

</details>


<details>
<summary> Using Maven (Alternative) </summary>

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.yourname.expensetracker.ExpenseTrackerApp"
```


</details>


##  Database Schema

### Tables


| Syntax | Description |
| ------------ | ----------|
| Header | Title |
| Paragraph | Text |


| Command | Description |
| --- | --- |
| git status | List all new or modified files |
| git diff | Show file differences that haven't been staged |



**categories**

| Column | Type | Constraints |
|--------|------|-------------|
| id | INTEGER | PRIMARY KEY, AUTOINCREMENT |
| name | VARCHAR(100) | UNIQUE, NOT NULL |
| description | TEXT | |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |


**transactions**


| Column | Type | Constraints |
|--------|------|-------------|
| id | INTEGER | PRIMARY KEY, AUTOINCREMENT |
| amount | DECIMAL(10,2) | NOT NULL |
| description | TEXT | |
| transaction_date | DATE | NOT NULL |
| category_id | INTEGER | FOREIGN KEY → categories(id) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |

See [Database Schema Diagram](docs/diagrams/database-schema.md) for visual representation.

##  Usage Examples

### Adding a Transaction