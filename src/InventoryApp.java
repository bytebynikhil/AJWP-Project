import java.sql.*;
import java.util.Scanner;

public class InventoryApp {
    private static final String URL = "jdbc:mysql://localhost:3306/inventory_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Aditya@123456";

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in);
             Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {

            System.out.println("Connected to the database.");

            boolean running = true;
            while (running) {
                showMenu();
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        viewItems(statement);
                        break;
                    case "2":
                        addItem(scanner, statement);
                        break;
                    case "3":
                        updateItem(scanner, statement);
                        break;
                    case "4":
                        deleteItem(scanner, statement);
                        break;
                    case "5":
                        searchItems(scanner, statement);
                        break;
                    case "6":
                        running = false;
                        System.out.println("Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option, please try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private static void showMenu() {
        System.out.println("\nInventory Menu:");
        System.out.println("1. View Items");
        System.out.println("2. Add Item");
        System.out.println("3. Update Item");
        System.out.println("4. Delete Item");
        System.out.println("5. Search Items by Name");
        System.out.println("6. Exit");
        System.out.print("Choose an option: ");
    }

    private static void viewItems(Statement statement) throws SQLException {
        String query = "SELECT * FROM items";
        try (ResultSet rs = statement.executeQuery(query)) {
            System.out.println("ID\tName\t\tQuantity\tPrice");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int qty = rs.getInt("quantity");
                double price = rs.getDouble("price");
                System.out.printf("%d\t%s\t\t%d\t\t%.2f\n", id, name, qty, price);
            }
        }
    }

    private static void addItem(Scanner scanner, Statement statement) throws SQLException {
        System.out.print("Enter name: ");
        String name = scanner.nextLine();
        int qty = readInt("Enter quantity: ", scanner);
        double price = readDouble("Enter price: ", scanner);

        String sql = "INSERT INTO items (name, quantity, price) VALUES (?, ?, ?)";
        try (PreparedStatement ps = statement.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setInt(2, qty);
            ps.setDouble(3, price);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int itemId = rs.getInt(1);
                    System.out.println("Item added with ID: " + itemId);
                }
            }
        }
    }

    private static void updateItem(Scanner scanner, Statement statement) throws SQLException {
        int id = readInt("Enter item ID to update: ", scanner);
        int newQty = readInt("Enter new quantity: ", scanner);
        double newPrice = readDouble("Enter new price: ", scanner);

        String sql = "UPDATE items SET quantity = ?, price = ? WHERE id = ?";
        try (PreparedStatement ps = statement.getConnection().prepareStatement(sql)) {
            ps.setInt(1, newQty);
            ps.setDouble(2, newPrice);
            ps.setInt(3, id);
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Item updated.");
            } else {
                System.out.println("Item not found.");
            }
        }
    }

    private static void deleteItem(Scanner scanner, Statement statement) throws SQLException {
        int id = readInt("Enter item ID to delete: ", scanner);

        String sql = "DELETE FROM items WHERE id = ?";
        try (PreparedStatement ps = statement.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            int rowsDeleted = ps.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Item deleted.");
            } else {
                System.out.println("Item not found.");
            }
        }
    }

    // --- Search Functionality ---
    private static void searchItems(Scanner scanner, Statement statement) throws SQLException {
        System.out.print("Enter name to search for: ");
        String searchTerm = scanner.nextLine().toLowerCase();

        String query = "SELECT * FROM items WHERE LOWER(name) LIKE ?";
        try (PreparedStatement ps = statement.getConnection().prepareStatement(query)) {
            ps.setString(1, "%" + searchTerm + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    System.out.println("No items found.");
                } else {
                    System.out.println("ID\tName\t\tQuantity\tPrice");
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String name = rs.getString("name");
                        int qty = rs.getInt("quantity");
                        double price = rs.getDouble("price");
                        System.out.printf("%d\t%s\t\t%d\t\t%.2f\n", id, name, qty, price);
                    }
                }
            }
        }
    }
    // --- End Search Functionality ---

    private static int readInt(String prompt, Scanner scanner) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
            }
        }
    }

    private static double readDouble(String prompt, Scanner scanner) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }
}
