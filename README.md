# 💰 Daily Expense Tracker (Core Java)

[![Java](https://img.shields.io/badge/Java-17%2B%20%2F%2021%20%2F%2023-orange.svg)](https://www.oracle.com/java/)
[![Build](https://img.shields.io/badge/Build-Passing-brightgreen.svg)]()
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Architecture](https://img.shields.io/badge/Architecture-OOP%20%26%20Serialization-blueviolet.svg)]()
[![Persistence](https://img.shields.io/badge/Storage-Object%20Serialization%20(.dat)-success.svg)]()

A robust, console-based personal finance management application developed in **Core Java**. It enables users to record daily expenditures, categorize transactions, set and monitor monthly budgets, detect overspending, and generate structured monthly financial reports with automated persistent disk storage.

---

## 📌 Table of Contents

- [1. Project Overview](#1-project-overview)
- [2. System Architecture](#2-system-architecture)
- [3. Application Features](#3-application-features)
- [4. Technologies Used & Design Decisions](#4-technologies-used--design-decisions)
- [5. How to Compile and Run](#5-how-to-compile-and-run)
- [6. Menu Navigation & Operations](#6-menu-navigation--operations)
- [7. Interactive Console Walkthrough](#7-interactive-console-walkthrough)
- [8. Data Persistence Architecture](#8-data-persistence-architecture)
- [9. Future Roadmap](#9-future-roadmap)
- [10. License](#10-license)

---

## 1. Project Overview

Managing personal finances requires accurate monetary calculation, structured categorical breakdown, and disciplined budget monitoring.

The **Daily Expense Tracker** provides a full suite of personal accounting tools:
- **Zero Framework Footprint**: Built purely with Core Java without requiring third-party libraries, databases, or web servers.
- **Permanent Data Persistence**: Uses native Java Object Serialization to store all transactions and budgets in a binary file (`expense_tracker.dat`) between executions.
- **Accurate Currency Handling**: Employs `BigDecimal` with strict `HALF_UP` rounding to prevent floating-point calculation inaccuracies common with `float` or `double`.
- **Preloaded Seed Data**: Out-of-the-box demo records allow immediate exploration of budget health, summaries, and category charts.

---

## 2. System Architecture

```
                       ┌──────────────────────────────────────────┐
                       │           ExpenseTracker.java            │
                       │   (Console Controller & Menu Dispatcher) │
                       └────────────────────┬─────────────────────┘
                                            │ coordinates
                                            ▼
                       ┌──────────────────────────────────────────┐
                       │               TrackerData                │
                       │  (In-Memory State & Budget Repository)   │
                       └─────────────┬────────────────────────────┘
                                     │ contains
                 ┌───────────────────┴───────────────────┐
                 ▼                                       ▼
    ┌─────────────────────────┐             ┌─────────────────────────┐
    │    List<Transaction>    │             │   Monthly & Category    │
    │  (Id, Date, Amount,     │             │      Budget Maps        │
    │   Category, Desc)       │             │   (HashMap<String, B>)  │
    └────────────┬────────────┘             └────────────┬────────────┘
                 │                                       │
                 └───────────────────┬───────────────────┘
                                     │ serialized via
                                     ▼
                       ┌──────────────────────────┐
                       │   expense_tracker.dat    │
                       │   (Binary Serialization) │
                       └──────────────────────────┘
```

---

## 3. Application Features

| Feature | Description |
| :--- | :--- |
| **Transaction CRUD** | Add, view, edit, and delete expense entries with automatic ID generation. |
| **Multi-Criteria Search** | Filter transactions by keyword in description, category, specific date, or amount range ($Min - Max$). |
| **Categorization** | Tag expenses under Food & Dining, Transportation, Utilities, Shopping, Healthcare, Education, Entertainment, or custom categories. |
| **Dual Budget Control** | Set an **Overall Monthly Budget** and granular **Category-Specific Budgets** for any target month (`YYYY-MM`). |
| **Financial Summaries** | Monthly totals, daily averages, active expenditure days, and highest/lowest transactions. |
| **Visual Category Breakdown** | Percentage share of each category accompanied by ASCII graphical visualization bars (`#`). |
| **Budget Deficit & Overspending Alert** | Real-time budget health check warning when expenses exceed allocated limits. |
| **Automated Data Persistence** | Auto-loads from disk on startup and serializes changes upon selecting `0. Save and exit`. |

---

## 4. Technologies Used & Design Decisions

| Technology | Purpose & Architectural Decision |
| :--- | :--- |
| **Java 17+** | Leverages modern switch expressions, enhanced streams, and records-ready architecture. |
| **`BigDecimal`** | Financial industry standard for currency handling. Avoids IEEE 754 floating-point precision loss. |
| **`LocalDate` & `YearMonth`** | Immutable, thread-safe date manipulation (ISO-8601 standard: `yyyy-MM-dd` and `yyyy-MM`). |
| **Java Serialization (`Serializable`)** | Enables fast, lightweight binary state persistence without setting up SQL servers. |
| **Java Collections (`ArrayList`, `HashMap`)** | Dynamic list storage for transactions and constant-time $O(1)$ lookups for budget thresholds. |
| **Streams API** | Functional reductions (`filter`, `reduce`, `groupingBy`, `summing`) for real-time reporting. |

---

## 5. How to Compile and Run

### Prerequisites
* Java JDK 17 or higher installed.
* Verify your installation:
  ```powershell
  javac -version
  java -version
  ```

### Step 1: Compile the application
From the workspace root directory (`c:\Users\sivas\OneDrive\Desktop\java intership`):
```powershell
javac ExpenseTracker.java
```

### Step 2: Execute the application
```powershell
java ExpenseTracker
```

---

## 6. Menu Navigation & Operations

Upon launching, the interactive menu displays:

```text
==========================================
       DAILY EXPENSE TRACKER
==========================================
1. Add expense
2. View all expenses
3. Edit expense
4. Delete expense
5. Search expenses
6. Set / update monthly budget
7. Monthly financial summary
8. Category-wise summary
9. View current-month budget status
0. Save and exit
==========================================
Enter your choice:
```

---

## 7. Interactive Console Walkthrough

### 1. View All Recorded Expenses (`Option 2`)
```text
--- [All Recorded Expenses (6)] ---
+------+--------------+------------------+--------------------------------+-------------+
| ID   | Date         | Category         | Description                    | Amount      |
+------+--------------+------------------+--------------------------------+-------------+
| 1    | 2026-11-04   | Food & Dining    | Supermarket Groceries          | $86.50      |
| 2    | 2026-11-05   | Transportation   | Monthly Train Metro Pass       | $75.00      |
| 3    | 2026-11-06   | Utilities        | High-Speed Fiber Internet      | $65.00      |
| 4    | 2026-11-07   | Food & Dining    | Team Dinner & Beverages        | $124.00     |
| 5    | 2026-11-08   | Education        | Software Engineering Book      | $49.99      |
| 6    | 2026-11-09   | Healthcare       | Pharmacy Medicine Refill       | $32.40      |
+------+--------------+------------------+--------------------------------+-------------+
Total Cumulative Expenses: $432.89 across 6 transactions.
```

### 2. Monthly Financial Summary (`Option 7`)
```text
=======================================================
     MONTHLY FINANCIAL SUMMARY: November 2026
=======================================================
  Total Spent          : $432.89
  Total Transactions   : 6
  Active Days Count    : 6
  Average Daily Spend  : $72.15
  Highest Expense      : $124.00 (Food & Dining - Team Dinner & Beverages)
  Lowest Expense       : $32.40 (Healthcare - Pharmacy Medicine Refill)
=======================================================
```

### 3. Category-wise Spending Breakdown with Visual Bar (`Option 8`)
```text
=================================================================
      CATEGORY-WISE SPENDING: November 2026
=================================================================
| Category         | Amount Spent | Share (%) | Visual Share       |
|------------------|--------------|-----------|--------------------|
| Food & Dining    | $210.50      | 48.6    % | #########          |
| Transportation   | $75.00       | 17.3    % | ###                |
| Utilities        | $65.00       | 15.0    % | ###                |
| Education        | $49.99       | 11.5    % | ##                 |
| Healthcare       | $32.40       | 7.5     % | #                  |
=================================================================
Total Monthly Expenditure: $432.89
```

### 4. Current-Month Budget Health & Overspending Warning (`Option 9`)
```text
=======================================================
    BUDGET HEALTH & OVERSPENDING REPORT (November 2026)
=======================================================
  Monthly Budget Limit : $2,500.00
  Total Spent To-Date  : $432.89 ( 17.3% utilized)
  Remaining Budget     : $2,067.11  [HEALTHY - Within Budget]

--- Category-Specific Budget Health ---
| Category         | Budget     | Spent      | Remaining  | Status     |
|------------------|------------|------------|------------|------------|
| Utilities        | $400.00    | $65.00     | $335.00    | OK         |
| Transportation   | $300.00    | $75.00     | $225.00    | OK         |
| Shopping         | $350.00    | $0.00      | $350.00    | OK         |
| Food & Dining    | $600.00    | $210.50    | $389.50    | OK         |
=======================================================
```

---

## 8. Data Persistence Architecture

The application handles storage through Java Object Serialization:

```java
// Saving data permanently to disk
try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("expense_tracker.dat"))) {
    oos.writeObject(data);
}

// Restoring state upon startup
try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("expense_tracker.dat"))) {
    data = (TrackerData) ois.readObject();
}
```

* When the application runs for the first time without an existing `expense_tracker.dat` file, it seeds realistic sample data for demonstration.
* Subsequent modifications (adds, edits, deletes, budgets) are saved when you exit via option `0`.

---

## 9. Future Roadmap

- [ ] **GUI Front-End**: Develop a desktop graphical interface using **Java Swing** or **JavaFX**.
- [ ] **Database Persistence**: Replace file serialization with **MySQL + JDBC** relational schemas.
- [ ] **Export Engine**: Export monthly expense sheets and audit logs to formatted **CSV** and **PDF**.
- [ ] **Recurring Transactions**: Support automated recurring payments (rent, subscriptions, utilities).

---

## 10. License

This project is licensed under the **MIT License** — feel free to use it for personal budgeting, academic projects, and portfolio presentations.

