package javaapplication5;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class Transaction {
    private String type;
    private String category;
    private double amount;
    private String date;

    public Transaction(String type, String category, double amount, String date) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    public String getType() { return type; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
}

class FinanceManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/finance_tracker";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Rayanch2265.";

    public void addTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions (type, category, amount, date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, transaction.getType());
            stmt.setString(2, transaction.getCategory());
            stmt.setDouble(3, transaction.getAmount());
            stmt.setString(4, transaction.getDate());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Transaction> getTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT type, category, amount, date FROM transactions";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                transactions.add(new Transaction(
                    rs.getString("type"),
                    rs.getString("category"),
                    rs.getDouble("amount"),
                    rs.getString("date")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }
}

public class CombinedFinanceTracker {
    private FinanceManager financeManager = new FinanceManager();
    private JFrame primaryFrame;
    private DefaultTableModel tableModel;
    private List<Transaction> allTransactions = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CombinedFinanceTracker::new);
    }

    public CombinedFinanceTracker() {
        primaryFrame = new JFrame("Personal Finance Tracker");
        primaryFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        primaryFrame.setSize(800, 600);
        primaryFrame.setLocationRelativeTo(null);
        showHomePage();
        primaryFrame.setVisible(true);
    }

    private void showHomePage() {
        JPanel homePanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome to Your Personal Finance Tracker!", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        homePanel.add(welcomeLabel, BorderLayout.NORTH);

        JButton addTransactionButton = createStyledButton("Add Transaction");
        JButton viewReportsButton = createStyledButton("View Reports");

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        buttonPanel.add(addTransactionButton);
        buttonPanel.add(viewReportsButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(40, 100, 40, 100));

        homePanel.add(buttonPanel, BorderLayout.CENTER);
        primaryFrame.setContentPane(homePanel);

        addTransactionButton.addActionListener(e -> showFinanceTracker());
        viewReportsButton.addActionListener(e -> showMessage("View Reports feature not implemented yet."));
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 16));
        button.setBackground(new Color(51, 153, 255));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void showFinanceTracker() {
        JPanel trackerPanel = new JPanel(new BorderLayout());
        String[] columns = {"Type", "Category", "Amount", "Date"};
        tableModel = new DefaultTableModel(columns, 0);
        JTable transactionTable = new JTable(tableModel);
        transactionTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        trackerPanel.add(scrollPane, BorderLayout.CENTER);

        loadTransactions();

        JButton filterButton = createStyledButton("Filter Transactions");
        JButton addButton = createStyledButton("Add Transaction");
        JButton backButton = createStyledButton("Back");
        JButton netBalanceButton = createStyledButton("Show Net Balance");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(backButton);
        buttonPanel.add(filterButton);
        buttonPanel.add(addButton);
        buttonPanel.add(netBalanceButton);

        trackerPanel.add(buttonPanel, BorderLayout.SOUTH);

        primaryFrame.setContentPane(trackerPanel);
        primaryFrame.revalidate();

        addButton.addActionListener(e -> showAddTransactionDialog());
        backButton.addActionListener(e -> showHomePage());
        filterButton.addActionListener(e -> showFilterDialog());
        netBalanceButton.addActionListener(e -> showNetBalance());
    }

    private void showAddTransactionDialog() {
        JDialog dialog = new JDialog(primaryFrame, "Add Transaction", true);
        dialog.setSize(700, 700);
        dialog.setLayout(new GridBagLayout());
        dialog.setLocationRelativeTo(primaryFrame);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        dialog.add(new JLabel("Type:"), gbc);
        gbc.gridy++;
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"Income", "Expense"});
        dialog.add(typeComboBox, gbc);

        gbc.gridy++;
        dialog.add(new JLabel("Category:"), gbc);
        gbc.gridy++;
        JTextField categoryField = new JTextField();
        dialog.add(categoryField, gbc);

        gbc.gridy++;
        dialog.add(new JLabel("Amount:"), gbc);
        gbc.gridy++;
        JTextField amountField = new JTextField();
        dialog.add(amountField, gbc);

        gbc.gridy++;
        dialog.add(new JLabel("Date (DD-MM-YYYY):"), gbc);
        gbc.gridy++;
        JTextField dateField = new JTextField();
        dialog.add(dateField, gbc);

        gbc.gridy++;
        JLabel errorLabel = new JLabel();
        errorLabel.setForeground(Color.RED);
        dialog.add(errorLabel, gbc);

        gbc.gridy++;
        JButton addButton = createStyledButton("Add");
        gbc.anchor = GridBagConstraints.CENTER;
        dialog.add(addButton, gbc);

        addButton.addActionListener(e -> {
            String type = (String) typeComboBox.getSelectedItem();
            String category = categoryField.getText().trim();
            String amountText = amountField.getText().trim();
            String date = dateField.getText().trim();

            if (category.isEmpty() || amountText.isEmpty() || date.isEmpty()) {
                errorLabel.setText("All fields are required.");
                return;
            }

            try {
                double amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    throw new NumberFormatException();
                }

                if (!date.matches("\\d{2}-\\d{2}-\\d{4}")) {
                    errorLabel.setText("Date must be in DD-MM-YYYY format.");
                    return;
                }

                Transaction transaction = new Transaction(type, category, amount, date);
                financeManager.addTransaction(transaction);
                allTransactions.add(transaction);
                tableModel.addRow(new Object[]{type, category, amount, date});
                dialog.dispose();
            } catch (NumberFormatException ex) {
                errorLabel.setText("Amount must be a valid positive number.");
            }
        });

        dialog.pack();
        dialog.setVisible(true);
    }

    private void loadTransactions() {
        allTransactions = financeManager.getTransactions();
        for (Transaction transaction : allTransactions) {
            tableModel.addRow(new Object[]{transaction.getType(), transaction.getCategory(), transaction.getAmount(), transaction.getDate()});
        }
    }

    private void showNetBalance() {
        double totalIncome = allTransactions.stream()
            .filter(t -> t.getType().equals("Income"))
            .mapToDouble(Transaction::getAmount)
            .sum();

        double totalExpense = allTransactions.stream()
            .filter(t -> t.getType().equals("Expense"))
            .mapToDouble(Transaction::getAmount)
            .sum();

        double netBalance = totalIncome - totalExpense;
        JOptionPane.showMessageDialog(primaryFrame, "Net Balance: " + netBalance);
    }

    private void showFilterDialog() {
        JDialog dialog = new JDialog(primaryFrame, "Filter Transactions", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new GridBagLayout());
        dialog.setLocationRelativeTo(primaryFrame);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        dialog.add(new JLabel("Type:"), gbc);
        gbc.gridy++;
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"All", "Income", "Expense"});
        dialog.add(typeComboBox, gbc);

        gbc.gridy++;
        dialog.add(new JLabel("Category (Optional):"), gbc);
        gbc.gridy++;
        JTextField categoryField = new JTextField();
        dialog.add(categoryField, gbc);

        gbc.gridy++;
        dialog.add(new JLabel("Date (Optional, DD-MM-YYYY):"), gbc);
        gbc.gridy++;
        JTextField dateField = new JTextField();
        dialog.add(dateField, gbc);

        gbc.gridy++;
        JButton filterButton = createStyledButton("Filter");
        dialog.add(filterButton, gbc);

        filterButton.addActionListener(e -> {
            String selectedType = (String) typeComboBox.getSelectedItem();
            String category = categoryField.getText().trim();
            String date = dateField.getText().trim();

            List<Transaction> filteredTransactions = allTransactions.stream()
                .filter(t -> ("All".equals(selectedType) || t.getType().equals(selectedType)))
                .filter(t -> (category.isEmpty() || t.getCategory().equalsIgnoreCase(category)))
                .filter(t -> (date.isEmpty() || t.getDate().equals(date)))
                .collect(Collectors.toList());

            tableModel.setRowCount(0); // Clear the table
            for (Transaction transaction : filteredTransactions) {
                tableModel.addRow(new Object[]{transaction.getType(), transaction.getCategory(), transaction.getAmount(), transaction.getDate()});
            }

            dialog.dispose();
        });

        dialog.pack();
        dialog.setVisible(true);
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(primaryFrame, message);
    }
}
