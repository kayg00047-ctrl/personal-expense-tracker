%% ============================================
%% SEQUENCE DIAGRAM: ADD TRANSACTION FLOW
%% ============================================
sequenceDiagram
    actor User
    participant UI as User Interface
    participant Controller as TransactionController
    participant Service as TransactionService
    participant DAO as TransactionDAO
    participant DB as Database
    
    User->>UI: Enter transaction details
    UI->>UI: Validate input format
    
    alt Input Invalid
        UI-->>User: Show validation error
    else Input Valid
        UI->>Controller: addTransaction(amount, desc, date, categoryId)
        Controller->>Service: addTransaction(data)
        
        Service->>Service: Validate business rules
        
        alt Validation Fails
            Service-->>Controller: ValidationException
            Controller-->>UI: Display error message
            UI-->>User: Show error
        else Validation Passes
            Service->>DAO: create(transaction)
            DAO->>DB: INSERT INTO transactions...
            DB-->>DAO: Return generated ID
            DAO-->>Service: Transaction object with ID
            Service-->>Controller: Success response
            Controller-->>UI: Transaction created
            UI-->>User: Display success message
        end
    end

%% ============================================
%% SEQUENCE DIAGRAM: VIEW MONTHLY SUMMARY
%% ============================================
sequenceDiagram
    actor User
    participant UI as User Interface
    participant Controller as ReportController
    participant Service as ReportService
    participant DAO as TransactionDAO
    participant DB as Database
    
    User->>UI: Request monthly summary
    UI->>UI: Prompt for month (or use current)
    UI->>Controller: getMonthlySummary(month)
    Controller->>Service: generateMonthlySummary(month)
    
    Service->>DAO: findByDateRange(startDate, endDate)
    DAO->>DB: SELECT with JOIN and GROUP BY
    DB-->>DAO: ResultSet with aggregated data
    DAO->>DAO: Map ResultSet to objects
    DAO-->>Service: List of CategorySummary objects
    
    Service->>Service: Calculate totals and percentages
    Service->>Service: Format report data
    Service-->>Controller: MonthlySummaryReport
    
    Controller->>UI: displayMonthlySummary(report)
    UI->>UI: Format for display
    UI-->>User: Show formatted summary
    
    Note over User,DB: Entire flow uses read-only operations<br/>No database modifications

%% ============================================
%% SEQUENCE DIAGRAM: EDIT TRANSACTION
%% ============================================
sequenceDiagram
    actor User
    participant UI as User Interface
    participant Controller as TransactionController
    participant Service as TransactionService
    participant DAO as TransactionDAO
    participant DB as Database
    
    User->>UI: Request to edit transaction
    UI->>UI: Display current transactions
    User->>UI: Select transaction ID
    UI->>Controller: getTransaction(id)
    Controller->>Service: findById(id)
    Service->>DAO: findById(id)
    DAO->>DB: SELECT * FROM transactions WHERE id = ?
    DB-->>DAO: Transaction data
    DAO-->>Service: Transaction object
    Service-->>Controller: Transaction
    Controller-->>UI: Transaction details
    UI-->>User: Show current values
    
    User->>UI: Enter new values
    UI->>Controller: editTransaction(id, newData)
    Controller->>Service: updateTransaction(id, newData)
    
    Service->>DAO: findById(id)
    DAO->>DB: SELECT to verify exists
    DB-->>DAO: Current transaction
    DAO-->>Service: Transaction
    
    Service->>Service: Merge new data with existing
    Service->>Service: Validate updated transaction
    
    Service->>DAO: update(transaction)
    DAO->>DB: UPDATE transactions SET ... WHERE id = ?
    DB-->>DAO: Rows affected
    DAO-->>Service: Success boolean
    Service-->>Controller: Updated transaction
    Controller-->>UI: Update confirmation
    UI-->>User: Display success message

%% ============================================
%% SEQUENCE DIAGRAM: EXPORT TO CSV
%% ============================================
sequenceDiagram
    actor User
    participant UI as User Interface
    participant Controller as ReportController
    participant Service as ReportService
    participant DAO as TransactionDAO
    participant DB as Database
    participant FileSystem as File System
    
    User->>UI: Request CSV export
    UI->>Controller: exportToCSV()
    Controller->>Service: exportAllTransactions()
    
    Service->>DAO: findAll()
    DAO->>DB: SELECT t.*, c.name FROM transactions t JOIN categories c
    DB-->>DAO: Complete result set
    DAO-->>Service: List of all transactions
    
    Service->>Service: Format data for CSV
    Service->>Service: Escape special characters
    Service->>Service: Generate filename with date
    
    Service->>FileSystem: Write CSV file
    FileSystem-->>Service: File created confirmation
    
    Service-->>Controller: Export complete (filename, row count)
    Controller-->>UI: Export success
    UI-->>User: "Exported X transactions to filename.csv"

%% ============================================
%% ACTIVITY DIAGRAM: USER WORKFLOW
%% ============================================
graph TB
    Start([User Opens App]) --> Init[Initialize Database]
    Init --> MainMenu{Display Main Menu}
    
    MainMenu -->|1| AddTrans[Add Transaction]
    MainMenu -->|2| ViewTrans[View Transactions]
    MainMenu -->|3| EditTrans[Edit Transaction]
    MainMenu -->|4| DelTrans[Delete Transaction]
    MainMenu -->|5| Summary[Monthly Summary]
    MainMenu -->|6| ManageCat[Manage Categories]
    MainMenu -->|7| Export[Export to CSV]
    MainMenu -->|8| Exit([Exit Application])
    
    AddTrans --> InputData[Input Transaction Data]
    InputData --> ValidateAdd{Valid Data?}
    ValidateAdd -->|No| ErrorAdd[Show Error]
    ErrorAdd --> AddTrans
    ValidateAdd -->|Yes| SaveTrans[Save to Database]
    SaveTrans --> SuccessAdd[Show Success]
    SuccessAdd --> MainMenu
    
    ViewTrans --> QueryDB[Query Database]
    QueryDB --> DisplayList[Display Transaction List]
    DisplayList --> MainMenu
    
    EditTrans --> ViewTrans2[Show Transactions]
    ViewTrans2 --> SelectEdit[Select Transaction ID]
    SelectEdit --> InputChanges[Input Changes]
    InputChanges --> ValidateEdit{Valid Changes?}
    ValidateEdit -->|No| ErrorEdit[Show Error]
    ErrorEdit --> InputChanges
    ValidateEdit -->|Yes| UpdateDB[Update Database]
    UpdateDB --> SuccessEdit[Show Success]
    SuccessEdit --> MainMenu
    
    DelTrans --> ViewTrans3[Show Transactions]
    ViewTrans3 --> SelectDel[Select Transaction ID]
    SelectDel --> ConfirmDel{Confirm Delete?}
    ConfirmDel -->|No| MainMenu
    ConfirmDel -->|Yes| DeleteDB[Delete from Database]
    DeleteDB --> SuccessDel[Show Success]
    SuccessDel --> MainMenu
    
    Summary --> InputMonth[Input Month or Use Current]
    InputMonth --> CalcSummary[Calculate Category Totals]
    CalcSummary --> DisplaySummary[Display Summary Report]
    DisplaySummary --> MainMenu
    
    ManageCat --> CatMenu{Category Menu}
    CatMenu -->|View| ViewCat[View Categories]
    CatMenu -->|Add| AddCat[Add Category]
    CatMenu -->|Delete| DelCat[Delete Category]
    ViewCat --> MainMenu
    AddCat --> MainMenu
    DelCat --> MainMenu
    
    Export --> GenerateCSV[Generate CSV File]
    GenerateCSV --> SaveFile[Save to File System]
    SaveFile --> SuccessExp[Show Success]
    SuccessExp --> MainMenu
    
    style Start fill:#90EE90
    style Exit fill:#FFB6C1
    style MainMenu fill:#87CEEB
    style SaveTrans fill:#FFD700
    style UpdateDB fill:#FFD700
    style DeleteDB fill:#FFD700