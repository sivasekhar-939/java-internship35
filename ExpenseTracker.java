import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Transaction entity representing an individual financial expense.
 */
class Transaction implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int id;
    private String description;
    private String category;
    private BigDecimal amount;
    private LocalDate date;

    public Transaction(int id, String description, String category, BigDecimal amount, LocalDate date) {
        this.id = id;
        this.description = description;
        this.category = category;
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.date = date;
    }

    public int getId() { return id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount.setScale(2, RoundingMode.HALF_UP); }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    @Override
    public String toString() {
        return String.format("| %-4d | %-12s | %-16s | %-30s | $%-10.2f |",
                id, date, category, description, amount);
    }
}

/**
 * Container class holding persistent application data for serialization.
 */
class TrackerData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<Transaction> transactions = new ArrayList<>();
    // Key: "YYYY-MM", Value: Overall budget amount
    private Map<String, BigDecimal> monthlyOverallBudgets = new HashMap<>();
    // Key: "YYYY-MM", Value: (Category -> Budget amount)
    private Map<String, Map<String, BigDecimal>> monthlyCategoryBudgets = new HashMap<>();
    private int nextId = 1;

    public List<Transaction> getTransactions() { return transactions; }
    public Map<String, BigDecimal> getMonthlyOverallBudgets() { return monthlyOverallBudgets; }
    public Map<String, Map<String, BigDecimal>> getMonthlyCategoryBudgets() { return monthlyCategoryBudgets; }
    public int getNextIdAndIncrement() { return nextId++; }
}

/**
 * Main Daily Expense Tracker Console Application.
 */
public class ExpenseTracker {

    private static final String DATA_FILE = "expense_tracker.dat";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Scanner sc = new Scanner(System.in);
    private static TrackerData data = new TrackerData();

    public static void main(String[] args) {
        loadData();

        while (true) {
            printMenu();
            int choice = readIntChoice("Enter your choice: ");

            switch (choice) {
                case 1 -> addExpense();
                case 2 -> viewAllExpenses();
                case 3 -> editExpense();
                case 4 -> deleteExpense();
                case 5 -> searchExpenses();
                case 6 -> setOrUpdateBudget();
                case 7 -> monthlyFinancialSummary();
                case 8 -> categoryWiseSummary();
                case 9 -> viewCurrentMonthBudgetStatus();
                case 0 -> {
                    saveData();
                    System.out.println("\nThank you for using Daily Expense Tracker. Records saved successfully!");
                    sc.close();
                    return;
                }
                default -> System.out.println("Invalid selection. Please choose an option from 0 to 9.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n==========================================");
        System.out.println("       DAILY EXPENSE TRACKER");
        System.out.println("==========================================");
        System.out.println("1. Add expense");
        System.out.println("2. View all expenses");
        System.out.println("3. Edit expense");
        System.out.println("4. Delete expense");
        System.out.println("5. Search expenses");
        System.out.println("6. Set / update monthly budget");
        System.out.println("7. Monthly financial summary");
        System.out.println("8. Category-wise summary");
        System.out.println("9. View current-month budget status");
        System.out.println("0. Save and exit");
        System.out.println("==========================================");
    }

    // ==========================================================
    // 1. Add Expense
    // ==========================================================
    private static void addExpense() {
        System.out.println("\n--- [Add New Expense] ---");
        System.out.print("Enter description: ");
        String description = sc.nextLine().trim();
        while (description.isEmpty()) {
            System.out.print("Description cannot be empty. Try again: ");
            description = sc.nextLine().trim();
        }

        String category = selectOrEnterCategory();
        BigDecimal amount = readBigDecimal("Enter amount ($): ");
        LocalDate date = readDate("Enter date (yyyy-MM-dd, press Enter for today): ");

        int id = data.getNextIdAndIncrement();
        Transaction t = new Transaction(id, description, category, amount, date);
        data.getTransactions().add(t);

        System.out.println("Transaction recorded successfully with ID: " + id);
    }

    // ==========================================================
    // 2. View All Expenses
    // ==========================================================
    private static void viewAllExpenses() {
        List<Transaction> list = data.getTransactions();
        if (list.isEmpty()) {
            System.out.println("\nNo expenses recorded yet!");
            return;
        }

        System.out.println("\n--- [All Recorded Expenses (" + list.size() + ")] ---");
        printTableHeader();
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction t : list) {
            System.out.println(t);
            total = total.add(t.getAmount());
        }
        printTableFooter();
        System.out.printf("Total Cumulative Expenses: $%,.2f across %d transactions.%n", total, list.size());
    }

    // ==========================================================
    // 3. Edit Expense
    // ==========================================================
    private static void editExpense() {
        if (data.getTransactions().isEmpty()) {
            System.out.println("\nNo expenses available to edit.");
            return;
        }

        int id = readIntChoice("\nEnter Expense ID to edit: ");
        Optional<Transaction> match = data.getTransactions().stream()
                .filter(t -> t.getId() == id)
                .findFirst();

        if (match.isEmpty()) {
            System.out.println("Expense with ID " + id + " not found!");
            return;
        }

        Transaction t = match.get();
        System.out.println("Editing: " + t);

        System.out.print("Enter new description [" + t.getDescription() + "]: ");
        String desc = sc.nextLine().trim();
        if (!desc.isEmpty()) t.setDescription(desc);

        System.out.print("Do you want to change the category (" + t.getCategory() + ")? (y/n): ");
        if (sc.nextLine().trim().equalsIgnoreCase("y")) {
            t.setCategory(selectOrEnterCategory());
        }

        System.out.print("Do you want to change amount ($" + t.getAmount() + ")? (y/n): ");
        if (sc.nextLine().trim().equalsIgnoreCase("y")) {
            t.setAmount(readBigDecimal("Enter new amount ($): "));
        }

        System.out.print("Do you want to change date (" + t.getDate() + ")? (y/n): ");
        if (sc.nextLine().trim().equalsIgnoreCase("y")) {
            t.setDate(readDate("Enter new date (yyyy-MM-dd, press Enter for today): "));
        }

        System.out.println("Expense updated successfully!");
    }

    // ==========================================================
    // 4. Delete Expense
    // ==========================================================
    private static void deleteExpense() {
        if (data.getTransactions().isEmpty()) {
            System.out.println("\nNo expenses available to delete.");
            return;
        }

        int id = readIntChoice("\nEnter Expense ID to delete: ");
        boolean removed = data.getTransactions().removeIf(t -> t.getId() == id);

        if (removed) {
            System.out.println("Expense ID " + id + " deleted successfully!");
        } else {
            System.out.println("Expense with ID " + id + " not found!");
        }
    }

    // ==========================================================
    // 5. Search Expenses
    // ==========================================================
    private static void searchExpenses() {
        if (data.getTransactions().isEmpty()) {
            System.out.println("\nNo expenses available to search.");
            return;
        }

        System.out.println("\n--- [Search Expenses] ---");
        System.out.println("1. By Description keyword");
        System.out.println("2. By Category");
        System.out.println("3. By Specific Date");
        System.out.println("4. By Minimum & Maximum Amount");
        System.out.println("0. Cancel");
        int subChoice = readIntChoice("Choose search criteria: ");

        List<Transaction> results = switch (subChoice) {
            case 1 -> {
                System.out.print("Enter keyword: ");
                String kw = sc.nextLine().trim().toLowerCase();
                yield data.getTransactions().stream()
                        .filter(t -> t.getDescription().toLowerCase().contains(kw))
                        .collect(Collectors.toList());
            }
            case 2 -> {
                System.out.print("Enter category: ");
                String cat = sc.nextLine().trim();
                yield data.getTransactions().stream()
                        .filter(t -> t.getCategory().equalsIgnoreCase(cat))
                        .collect(Collectors.toList());
            }
            case 3 -> {
                LocalDate searchDate = readDate("Enter search date (yyyy-MM-dd): ");
                yield data.getTransactions().stream()
                        .filter(t -> t.getDate().equals(searchDate))
                        .collect(Collectors.toList());
            }
            case 4 -> {
                BigDecimal min = readBigDecimal("Enter minimum amount ($): ");
                BigDecimal max = readBigDecimal("Enter maximum amount ($): ");
                yield data.getTransactions().stream()
                        .filter(t -> t.getAmount().compareTo(min) >= 0 && t.getAmount().compareTo(max) <= 0)
                        .collect(Collectors.toList());
            }
            default -> Collections.emptyList();
        };

        if (subChoice == 0) return;

        if (results.isEmpty()) {
            System.out.println("No matching transactions found.");
        } else {
            System.out.println("\nFound " + results.size() + " matching expense(s):");
            printTableHeader();
            results.forEach(System.out::println);
            printTableFooter();
        }
    }

    // ==========================================================
    // 6. Set / Update Monthly Budget
    // ==========================================================
    private static void setOrUpdateBudget() {
        System.out.println("\n--- [Set / Update Monthly Budget] ---");
        String monthKey = readMonthKey();

        System.out.println("1. Set Overall Monthly Budget");
        System.out.println("2. Set Category-Specific Budget");
        int bChoice = readIntChoice("Choice: ");

        if (bChoice == 1) {
            BigDecimal budget = readBigDecimal("Enter overall budget for " + monthKey + " ($): ");
            data.getMonthlyOverallBudgets().put(monthKey, budget);
            System.out.printf("Overall budget for %s set to $%,.2f%n", monthKey, budget);
        } else if (bChoice == 2) {
            String cat = selectOrEnterCategory();
            BigDecimal budget = readBigDecimal("Enter budget for " + cat + " in " + monthKey + " ($): ");
            data.getMonthlyCategoryBudgets()
                    .computeIfAbsent(monthKey, k -> new HashMap<>())
                    .put(cat, budget);
            System.out.printf("Category budget for [%s] in %s set to $%,.2f%n", cat, monthKey, budget);
        } else {
            System.out.println("Invalid option.");
        }
    }

    // ==========================================================
    // 7. Monthly Financial Summary
    // ==========================================================
    private static void monthlyFinancialSummary() {
        String monthKey = readMonthKey();
        YearMonth ym = YearMonth.parse(monthKey);

        List<Transaction> monthList = data.getTransactions().stream()
                .filter(t -> YearMonth.from(t.getDate()).equals(ym))
                .toList();

        System.out.println("\n=======================================================");
        System.out.printf("     MONTHLY FINANCIAL SUMMARY: %s%n", ym.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        System.out.println("=======================================================");

        if (monthList.isEmpty()) {
            System.out.println("No transactions recorded for this month.");
            System.out.println("=======================================================");
            return;
        }

        BigDecimal total = monthList.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long distinctDays = monthList.stream().map(Transaction::getDate).distinct().count();
        BigDecimal avgDaily = distinctDays > 0
                ? total.divide(BigDecimal.valueOf(distinctDays), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Transaction highest = monthList.stream().max(Comparator.comparing(Transaction::getAmount)).orElse(null);
        Transaction lowest = monthList.stream().min(Comparator.comparing(Transaction::getAmount)).orElse(null);

        System.out.printf("  Total Spent          : $%,.2f%n", total);
        System.out.printf("  Total Transactions   : %d%n", monthList.size());
        System.out.printf("  Active Days Count    : %d%n", distinctDays);
        System.out.printf("  Average Daily Spend  : $%,.2f%n", avgDaily);
        if (highest != null) {
            System.out.printf("  Highest Expense      : $%,.2f (%s - %s)%n",
                    highest.getAmount(), highest.getCategory(), highest.getDescription());
        }
        if (lowest != null) {
            System.out.printf("  Lowest Expense       : $%,.2f (%s - %s)%n",
                    lowest.getAmount(), lowest.getCategory(), lowest.getDescription());
        }
        System.out.println("=======================================================");
    }

    // ==========================================================
    // 8. Category-wise Summary
    // ==========================================================
    private static void categoryWiseSummary() {
        String monthKey = readMonthKey();
        YearMonth ym = YearMonth.parse(monthKey);

        List<Transaction> monthList = data.getTransactions().stream()
                .filter(t -> YearMonth.from(t.getDate()).equals(ym))
                .toList();

        System.out.println("\n=================================================================");
        System.out.printf("      CATEGORY-WISE SPENDING: %s%n", ym.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        System.out.println("=================================================================");

        if (monthList.isEmpty()) {
            System.out.println("No expenses recorded for this month.");
            System.out.println("=================================================================");
            return;
        }

        BigDecimal totalMonth = monthList.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> byCategory = monthList.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        System.out.println("| Category         | Amount Spent | Share (%) | Visual Share       |");
        System.out.println("|------------------|--------------|-----------|--------------------|");

        byCategory.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .forEach(entry -> {
                    BigDecimal catAmount = entry.getValue();
                    double percentage = totalMonth.compareTo(BigDecimal.ZERO) > 0
                            ? catAmount.multiply(BigDecimal.valueOf(100)).divide(totalMonth, 1, RoundingMode.HALF_UP).doubleValue()
                            : 0.0;
                    int bars = (int) (percentage / 5);
                    String barVisual = "#".repeat(bars);
                    System.out.printf("| %-16s | $%-11.2f | %-8.1f%% | %-18s |%n",
                            entry.getKey(), catAmount, percentage, barVisual);
                });

        System.out.println("=================================================================");
        System.out.printf("Total Monthly Expenditure: $%,.2f%n", totalMonth);
    }

    // ==========================================================
    // 9. View Current-Month Budget Status
    // ==========================================================
    private static void viewCurrentMonthBudgetStatus() {
        YearMonth ym = YearMonth.now();
        String monthKey = ym.toString();

        BigDecimal overallBudget = data.getMonthlyOverallBudgets().get(monthKey);
        Map<String, BigDecimal> catBudgets = data.getMonthlyCategoryBudgets().getOrDefault(monthKey, Collections.emptyMap());

        List<Transaction> currentMonthList = data.getTransactions().stream()
                .filter(t -> YearMonth.from(t.getDate()).equals(ym))
                .toList();

        BigDecimal totalSpent = currentMonthList.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.println("\n=======================================================");
        System.out.printf("    BUDGET HEALTH & OVERSPENDING REPORT (%s)%n", ym.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        System.out.println("=======================================================");

        if (overallBudget == null) {
            System.out.printf("  Overall Budget       : NOT SET (Go to option 6)%n");
            System.out.printf("  Total Spent So Far   : $%,.2f%n", totalSpent);
        } else {
            BigDecimal diff = overallBudget.subtract(totalSpent);
            double usedPct = overallBudget.compareTo(BigDecimal.ZERO) > 0
                    ? totalSpent.multiply(BigDecimal.valueOf(100)).divide(overallBudget, 1, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;

            System.out.printf("  Monthly Budget Limit : $%,.2f%n", overallBudget);
            System.out.printf("  Total Spent To-Date  : $%,.2f (%5.1f%% utilized)%n", totalSpent, usedPct);

            if (diff.compareTo(BigDecimal.ZERO) >= 0) {
                System.out.printf("  Remaining Budget     : $%,.2f  [HEALTHY - Within Budget]%n", diff);
            } else {
                System.out.printf("  BUDGET DEFICIT       : $%,.2f  [*** OVERSPENT WARNING ***]%n", diff.abs());
            }
        }

        // Category-specific budget breakdown
        if (!catBudgets.isEmpty()) {
            System.out.println("\n--- Category-Specific Budget Health ---");
            Map<String, BigDecimal> spentByCat = currentMonthList.stream()
                    .collect(Collectors.groupingBy(
                            Transaction::getCategory,
                            Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                    ));

            System.out.println("| Category         | Budget     | Spent      | Remaining  | Status     |");
            System.out.println("|------------------|------------|------------|------------|------------|");

            for (Map.Entry<String, BigDecimal> entry : catBudgets.entrySet()) {
                String cat = entry.getKey();
                BigDecimal b = entry.getValue();
                BigDecimal s = spentByCat.getOrDefault(cat, BigDecimal.ZERO);
                BigDecimal rem = b.subtract(s);
                String status = rem.compareTo(BigDecimal.ZERO) >= 0 ? "OK" : "OVERSPENT";

                System.out.printf("| %-16s | $%-9.2f | $%-9.2f | $%-9.2f | %-10s |%n",
                        cat, b, s, rem, status);
            }
        }
        System.out.println("=======================================================");
    }

    // ==========================================================
    // Persistence Helpers (Serialization)
    // ==========================================================
    private static void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(data);
        } catch (IOException e) {
            System.err.println("Error saving transaction data: " + e.getMessage());
        }
    }

    private static void loadData() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                data = (TrackerData) ois.readObject();
            } catch (Exception e) {
                System.out.println("Initializing fresh expense repository (could not load existing file).");
                data = new TrackerData();
                seedDemoData();
            }
        } else {
            seedDemoData();
        }
    }

    private static void seedDemoData() {
        YearMonth currentYM = YearMonth.now();
        String currentMonth = currentYM.toString();
        LocalDate today = LocalDate.now();

        // Seed current month overall & category budgets
        data.getMonthlyOverallBudgets().put(currentMonth, new BigDecimal("2500.00"));
        Map<String, BigDecimal> catBudgets = new HashMap<>();
        catBudgets.put("Food & Dining", new BigDecimal("600.00"));
        catBudgets.put("Transportation", new BigDecimal("300.00"));
        catBudgets.put("Utilities", new BigDecimal("400.00"));
        catBudgets.put("Shopping", new BigDecimal("350.00"));
        data.getMonthlyCategoryBudgets().put(currentMonth, catBudgets);

        // Seed realistic sample expenses
        data.getTransactions().add(new Transaction(data.getNextIdAndIncrement(), "Supermarket Groceries", "Food & Dining", new BigDecimal("86.50"), today.minusDays(5)));
        data.getTransactions().add(new Transaction(data.getNextIdAndIncrement(), "Monthly Train Metro Pass", "Transportation", new BigDecimal("75.00"), today.minusDays(4)));
        data.getTransactions().add(new Transaction(data.getNextIdAndIncrement(), "High-Speed Fiber Internet", "Utilities", new BigDecimal("65.00"), today.minusDays(3)));
        data.getTransactions().add(new Transaction(data.getNextIdAndIncrement(), "Team Dinner & Beverages", "Food & Dining", new BigDecimal("124.00"), today.minusDays(2)));
        data.getTransactions().add(new Transaction(data.getNextIdAndIncrement(), "Software Engineering Book", "Education", new BigDecimal("49.99"), today.minusDays(1)));
        data.getTransactions().add(new Transaction(data.getNextIdAndIncrement(), "Pharmacy Medicine Refill", "Healthcare", new BigDecimal("32.40"), today));
    }

    // ==========================================================
    // UI Helpers
    // ==========================================================
    private static void printTableHeader() {
        System.out.println("+------+--------------+------------------+--------------------------------+-------------+");
        System.out.println("| ID   | Date         | Category         | Description                    | Amount      |");
        System.out.println("+------+--------------+------------------+--------------------------------+-------------+");
    }

    private static void printTableFooter() {
        System.out.println("+------+--------------+------------------+--------------------------------+-------------+");
    }

    private static String selectOrEnterCategory() {
        String[] defaultCategories = {
                "Food & Dining", "Transportation", "Utilities",
                "Shopping", "Healthcare", "Education", "Entertainment", "Other"
        };
        System.out.println("Select Category:");
        for (int i = 0; i < defaultCategories.length; i++) {
            System.out.printf("  %d. %s%n", (i + 1), defaultCategories[i]);
        }
        System.out.println("  9. Custom category");

        int catChoice = readIntChoice("Category choice (1-9): ");
        if (catChoice >= 1 && catChoice <= defaultCategories.length) {
            return defaultCategories[catChoice - 1];
        } else if (catChoice == 9) {
            System.out.print("Enter custom category name: ");
            String custom = sc.nextLine().trim();
            return custom.isEmpty() ? "Other" : custom;
        }
        return "Other";
    }

    private static int readIntChoice(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid numeric value.");
            }
        }
    }

    private static BigDecimal readBigDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                BigDecimal val = new BigDecimal(input);
                if (val.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("Amount must be greater than zero.");
                    continue;
                }
                return val.setScale(2, RoundingMode.HALF_UP);
            } catch (NumberFormatException e) {
                System.out.println("Invalid currency value. Enter decimal number (e.g., 45.50).");
            }
        }
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (input.isEmpty()) {
                return LocalDate.now();
            }
            try {
                return LocalDate.parse(input, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Use yyyy-MM-dd (e.g., 2026-11-09).");
            }
        }
    }

    private static String readMonthKey() {
        System.out.print("Enter Month (yyyy-MM, press Enter for current month): ");
        String input = sc.nextLine().trim();
        if (input.isEmpty()) {
            return YearMonth.now().toString();
        }
        while (true) {
            try {
                return YearMonth.parse(input).toString();
            } catch (DateTimeParseException e) {
                System.out.print("Invalid format. Please enter yyyy-MM (e.g., 2026-11): ");
                input = sc.nextLine().trim();
            }
        }
    }
}

