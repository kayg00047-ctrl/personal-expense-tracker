
    # Installation Guide - Personal Expense Tracker

## Setup Instructions

Follow these instructions to set up and run the Personal Expense Tracker on your local machine.

---

## Prerequisites

Before you begin, ensure you have the following installed:

### 1. **Java Development Kit (JDK)**
- **Required Version**: JDK 17 or higher
- **Check if installed**:
  ```bash
  java -version
  ```
- **Download**: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://adoptium.net/)

### 2. **Git** (for cloning the repository)
- **Check if installed**:
  ```bash
  git --version
  ```
- **Download**: [Git Downloads](https://git-scm.com/downloads)

### 3. **SQLite JDBC Driver** (included in repository or via Maven)
- No separate installation needed if using Maven
- Manual download: [SQLite JDBC](https://github.com/xerial/sqlite-jdbc/releases)

---

## Installation Methods

### Method 1: Manual Compilation (No Build Tool)

#### Step 1: Clone the Repository
```bash
git clone https://github.com/yourusername/personal-expense-tracker.git
cd personal-expense-tracker
```

#### Step 2: Create Directory Structure
```bash
mkdir -p bin
```

#### Step 3: Download SQLite JDBC Driver (if not included)
```bash
# Download the JAR file to the lib/ directory
mkdir -p lib
# Visit https://github.com/xerial/sqlite-jdbc/releases
# Download sqlite-jdbc-3.44.1.0.jar to lib/
```

#### Step 4: Compile the Application
```bash
javac -d bin -cp "lib/*" src/main/java/com/yourname/expensetracker/ExpenseTrackerApp.java
```

#### Step 5: Run the Application
```bash
java -cp "bin:lib/*" com.yourname.expensetracker.ExpenseTrackerApp
```

**Windows users**: Replace `:` with `;` in classpath:
```bash
java -cp "bin;lib/*" com.yourname.expensetracker.ExpenseTrackerApp
```

---

### Method 2: Using Maven

#### Step 1: Clone the Repository
```bash
git clone https://github.com/yourusername/personal-expense-tracker.git
cd personal-expense-tracker
```

#### Step 2: Verify Maven Installation
```bash
mvn -version
```
If not installed, download from [Apache Maven](https://maven.apache.org/download.cgi)

#### Step 3: Build the Project
```bash
mvn clean compile
```

#### Step 4: Run the Application
```bash
mvn exec:java -Dexec.mainClass="com.yourname.expensetracker.ExpenseTrackerApp"
```

#### Step 5: Package as JAR (Optional)
```bash
mvn clean package
java -jar target/expense-tracker-1.0.0.jar
```

---

### Method 3: Using Gradle

#### Step 1: Clone the Repository
```bash
git clone https://github.com/yourusername/personal-expense-tracker.git
cd personal-expense-tracker
```

#### Step 2: Build and Run
```bash
./gradlew build
./gradlew run
```

**Windows users**: Use `gradlew.bat` instead of `./gradlew`

---

## IDE Setup

### IntelliJ IDEA

1. **Open Project**
   - File → Open → Select project directory
   - IntelliJ will detect the Maven/Gradle configuration

2. **Run Configuration**
   - Right-click `ExpenseTrackerApp.java`
   - Select "Run 'ExpenseTrackerApp.main()'"

3. **Set JDK**
   - File → Project Structure → Project
   - Set SDK to JDK 17 or higher

### Eclipse

1. **Import Project**
   - File → Import → Maven → Existing Maven Projects
   - Browse to project directory

2. **Run Application**
   - Right-click `ExpenseTrackerApp.java`
   - Run As → Java Application

3. **Set JDK**
   - Right-click project → Properties → Java Build Path
   - Set JRE to 17 or higher

### VS Code

1. **Open Project**
   - File → Open Folder → Select project directory

2. **Install Extensions**
   - Extension Pack for Java
   - Maven for Java (if using Maven)

3. **Run Application**
   - Press F5 or use Run menu
   - Select "Java" as configuration type

---

## Verification

After installation, verify everything works:

1. **Run the Application**
   - You should see the main menu with options 1-8

2. **Test Database Connection**
   - Select option 1 (Add Transaction)
   - Add a test transaction
   - If successful, database is working

3. **Check Database File**
   - A file named `expenses.db` should appear in project root
   - This confirms SQLite database was created

4. **Test Export**
   - Select option 7 (Export to CSV)
   - A CSV file should be created in project root

---

## Troubleshooting

### "Class not found" Error
**Problem**: Java can't find the main class
**Solution**: 
```bash
# Ensure you're in the correct directory
pwd
# Verify bin/ directory has compiled classes
ls -la bin/
# Use full classpath in java command
```

### "SQLite JDBC driver not found"
**Problem**: JDBC driver not in classpath
**Solution**:
```bash
# Verify lib/ directory has JAR file
ls -la lib/
# Include lib/* in classpath when running
java -cp "bin:lib/*" ...
```

### "Permission denied" on Linux/Mac
**Problem**: Gradle wrapper not executable
**Solution**:
```bash
chmod +x gradlew
./gradlew build
```

### Database Lock Error
**Problem**: Database file is locked by another process
**Solution**:
```bash
# Close any other applications using the database
# Delete expenses.db and restart application
rm expenses.db
```

### Java Version Error
**Problem**: Wrong Java version
**Solution**:
```bash
# Check Java version
java -version
# Update JAVA_HOME environment variable
export JAVA_HOME=/path/to/jdk-17
```

---

## Next Steps

1. **Run the Application** and explore all features
2. **Add some test transactions** to see how it works
3. **Generate a monthly summary** to test reporting
4. **Export to CSV** and open in Excel/Google Sheets
5. **Read the [User Guide](USER_GUIDE.md)** for detailed usage

---

## Uninstallation

To remove the application:

```bash
# Delete project directory
cd ..
rm -rf personal-expense-tracker

# Database file is in project directory, so this removes it too
```

---

## Getting Help

If you encounter issues:

1. **Check this guide** for common problems
2. **Review error messages** carefully
3. **Check GitHub Issues**: [Project Issues](https://github.com/yourusername/personal-expense-tracker/issues)
4. **Open a new issue** with:
   - Your OS and Java version
   - Complete error message
   - Steps to reproduce

---

## Additional Resources

- [Java Documentation](https://docs.oracle.com/en/java/)
- [Maven Getting Started](https://maven.apache.org/guides/getting-started/)
- [SQLite Documentation](https://www.sqlite.org/docs.html)
- [JDBC Tutorial](https://docs.oracle.com/javase/tutorial/jdbc/)