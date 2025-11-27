-- Database Schema for Personal Expense Tracker
-- SQLite Version 3.x

CREATE TABLE IF NOT EXISTS categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    amount DECIMAL(10, 2) NOT NULL,
    description TEXT,
    transaction_date DATE NOT NULL,
    category_id INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_transaction_date ON transactions(transaction_date);
CREATE INDEX IF NOT EXISTS idx_category_id ON transactions(category_id);

-- Sample data insertion
INSERT INTO categories (name) VALUES 
    ('Food & Dining'),
    ('Transportation'),
    ('Shopping'),
    ('Entertainment'),
    ('Bills & Utilities'),
    ('Healthcare'),
    ('Education'),
    ('Other');