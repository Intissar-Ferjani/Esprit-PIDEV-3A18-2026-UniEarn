package uniearn.controller.profile.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import uniearn.model.entities.users.User;
import uniearn.model.entities.users.admin.Admin;
import uniearn.model.entities.users.client.Client;
import uniearn.model.entities.users.freelancer.Freelancer;
import uniearn.model.enums.UserRole;
import uniearn.services.users.UserService;
import uniearn.services.users.client.ClientService;
import uniearn.services.users.freelancer.FreelancerService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ListUsersController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilterCombo;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private ComboBox<String> sortByCombo;
    @FXML private VBox usersRowsContainer;
    @FXML private Label totalUsersLabel;

    private final UserService userService = new UserService();
    private final ClientService clientService = new ClientService();
    private final FreelancerService freelancerService = new FreelancerService();

    private Admin currentAdmin;
    private List<User> allUsers;
    private List<User> filteredUsers;

    @FXML
    public void initialize() {
        System.out.println("ManageUsersController initialized");

        if (roleFilterCombo != null)   roleFilterCombo.setValue("All");
        if (statusFilterCombo != null) statusFilterCombo.setValue("All");
        if (sortByCombo != null)       sortByCombo.setValue("Name (A-Z)");

        setupFilterListeners();
        loadUsers();
    }

    public void setAdminData(Admin admin) {
        this.currentAdmin = admin;
        loadUsers();
    }

    public void initializeWithoutAdmin() {
        System.out.println("⚠ Admin data not available, loading users anyway");
        loadUsers();
    }

    private void setupFilterListeners() {
        if (searchField != null)
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        if (roleFilterCombo != null)   roleFilterCombo.setOnAction(e -> applyFilters());
        if (statusFilterCombo != null) statusFilterCombo.setOnAction(e -> applyFilters());
        if (sortByCombo != null)       sortByCombo.setOnAction(e -> applyFilters());
    }

    private void loadUsers() {
        try {
            allUsers = userService.getAllUsers();
            System.out.println("✓ Loaded " + allUsers.size() + " users");
            applyFilters();
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load users: " + e.getMessage());
        }
    }

    private void applyFilters() {
        if (allUsers == null) return;

        filteredUsers = new ArrayList<>(allUsers);

        // Search
        String searchTerm = searchField.getText().toLowerCase().trim();
        if (!searchTerm.isEmpty()) {
            filteredUsers = filteredUsers.stream()
                    .filter(u -> matchesSearchTerm(u, searchTerm))
                    .collect(Collectors.toList());
        }

        // Role
        String roleFilter = roleFilterCombo.getValue();
        if (!"All".equals(roleFilter)) {
            UserRole targetRole = UserRole.valueOf(roleFilter.toUpperCase());
            filteredUsers = filteredUsers.stream()
                    .filter(u -> u.getRole() == targetRole)
                    .collect(Collectors.toList());
        }

        // Status
        String statusFilter = statusFilterCombo.getValue();
        if (!"All".equals(statusFilter)) {
            boolean isActive = "Active".equals(statusFilter);
            filteredUsers = filteredUsers.stream()
                    .filter(u -> u.isActivated() == isActive)
                    .collect(Collectors.toList());
        }

        // Sort
        String sortBy = sortByCombo.getValue();
        switch (sortBy) {
            case "Name (A-Z)":  filteredUsers.sort(Comparator.comparing(User::getName)); break;
            case "Name (Z-A)":  filteredUsers.sort(Comparator.comparing(User::getName).reversed()); break;
            case "Email (A-Z)": filteredUsers.sort(Comparator.comparing(User::getEmail)); break;
            case "Role":        filteredUsers.sort(Comparator.comparing(u -> u.getRole().name())); break;
            case "Status":      filteredUsers.sort(Comparator.comparing(User::isActivated).reversed()); break;
        }

        displayUsers();
    }

    private boolean matchesSearchTerm(User user, String searchTerm) {
        return user.getName().toLowerCase().contains(searchTerm)
                || user.getEmail().toLowerCase().contains(searchTerm);
    }

    private void displayUsers() {
        usersRowsContainer.getChildren().clear();

        totalUsersLabel.setText(filteredUsers.size() + " user" +
                (filteredUsers.size() != 1 ? "s" : ""));

        if (filteredUsers.isEmpty()) {
            showEmptyState();
            return;
        }

        // ✅ Pass index for alternating row colors
        for (int i = 0; i < filteredUsers.size(); i++) {
            usersRowsContainer.getChildren().add(createUserRow(filteredUsers.get(i), i % 2 == 0));
        }
    }

    private void showEmptyState() {
        VBox emptyState = new VBox(15);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setStyle("-fx-padding: 60px;");

        Label icon = new Label("🔍");
        icon.setStyle("-fx-font-size: 64px;");

        Label message = new Label("No users found");
        message.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #657786;");

        Label hint = new Label("Try adjusting your search or filters");
        hint.setStyle("-fx-font-size: 14px; -fx-text-fill: #95a5a6;");

        emptyState.getChildren().addAll(icon, message, hint);
        usersRowsContainer.getChildren().add(emptyState);
    }

    // ✅ FIXED: badges wrapped in HBox containers so columns align with header
    // Column widths match FXML header: Name=200 | Email=240 | Role=150 | Status=150 | grow | Actions=220
    private HBox createUserRow(User user, boolean isEven) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setSpacing(0);
        row.setPadding(new Insets(12, 16, 12, 16));

        String baseStyle = (isEven ? "-fx-background-color: #ffffff;" : "-fx-background-color: #fafbfc;")
                + "-fx-border-color: transparent transparent #e1e8ed transparent;"
                + "-fx-border-width: 0 0 1 0;";
        row.setStyle(baseStyle);

        // ✅ Hover effect
        row.setOnMouseEntered(e -> row.setStyle(
                "-fx-background-color: #eff6ff;"
                        + "-fx-border-color: transparent transparent #dbeafe transparent;"
                        + "-fx-border-width: 0 0 1 0; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle(baseStyle));

        // ── Name (200px) ──
        Label nameLabel = new Label(user.getName());
        nameLabel.setPrefWidth(200);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #14171a; -fx-font-weight: bold;");

        // ── Email (240px) ──
        Label emailLabel = new Label(user.getEmail());
        emailLabel.setPrefWidth(240);
        emailLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #657786;");

        // ── Role badge wrapped in 150px HBox so it occupies the full column ──
        Label roleLabel = new Label(user.getRole().name());
        roleLabel.setStyle(
                "-fx-background-color: " + getRoleColor(user.getRole()) + ";"
                        + "-fx-text-fill: white; -fx-padding: 4 12;"
                        + "-fx-background-radius: 12;"
                        + "-fx-font-size: 11px; -fx-font-weight: bold;");
        HBox roleBox = new HBox(roleLabel);
        roleBox.setPrefWidth(150);
        roleBox.setAlignment(Pos.CENTER_LEFT);

        // ── Status badge wrapped in 150px HBox so it occupies the full column ──
        boolean active = user.isActivated();
        Label statusLabel = new Label(active ? "Active" : "Deactivated");
        statusLabel.setStyle(
                "-fx-background-color: " + (active ? "#27ae60" : "#e74c3c") + ";"
                        + "-fx-text-fill: white; -fx-padding: 4 12;"
                        + "-fx-background-radius: 12;"
                        + "-fx-font-size: 11px; -fx-font-weight: bold;");
        HBox statusBox = new HBox(statusLabel);
        statusBox.setPrefWidth(150);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        // ── Spacer ──
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // ── Actions (220px) ──
        Button viewBtn = new Button("👁 View");
        viewBtn.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white;"
                        + "-fx-font-size: 12px; -fx-padding: 6 16;"
                        + "-fx-background-radius: 6; -fx-cursor: hand;");
        viewBtn.setOnAction(e -> handleViewUser(user));

        Button toggleStatusBtn = new Button(active ? "🚫 Deactivate" : "✓ Activate");
        toggleStatusBtn.setStyle(
                "-fx-background-color: " + (active ? "#e74c3c" : "#27ae60") + ";"
                        + "-fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 6 16;"
                        + "-fx-background-radius: 6; -fx-cursor: hand;");
        toggleStatusBtn.setOnAction(e -> handleToggleUserStatus(user, toggleStatusBtn));

        HBox actionsBox = new HBox(8, viewBtn, toggleStatusBtn);
        actionsBox.setPrefWidth(220);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(nameLabel, emailLabel, roleBox, statusBox, spacer, actionsBox);
        return row;
    }

    private String getRoleColor(UserRole role) {
        switch (role) {
            case ADMIN:      return "#e74c3c";
            case CLIENT:     return "#3498db";
            case FREELANCER: return "#9b59b6";
            default:         return "#95a5a6";
        }
    }

    private void handleViewUser(User user) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("View User Details");
        dialog.initModality(Modality.APPLICATION_MODAL);

        DialogPane dialogPane = new DialogPane();
        dialogPane.setStyle("-fx-background-color: white;");
        dialogPane.setPrefWidth(600);

        VBox header = new VBox(10);
        header.setStyle("-fx-background-color: " + getRoleColor(user.getRole()) + "; -fx-padding: 20px;");
        header.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(user.getName());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        Label emailLabel = new Label(user.getEmail());
        emailLabel.setStyle("-fx-text-fill: #ecf0f1; -fx-font-size: 14px;");
        header.getChildren().addAll(nameLabel, emailLabel);

        VBox content = new VBox(15);
        content.setPadding(new Insets(25));

        content.getChildren().addAll(
                createInfoRow("User ID:", String.valueOf(user.getIdUser())),
                createInfoRow("Role:",    user.getRole().name()),
                createInfoRow("Status:",  user.isActivated() ? "Active" : "Deactivated"),
                createInfoRow("Email:",   user.getEmail())
        );

        if (user.getRole() == UserRole.CLIENT) {
            Client client = clientService.getClientById(user.getIdUser());
            if (client != null) {
                content.getChildren().add(new Separator());
                Label clientHeader = new Label("Client Information");
                clientHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
                content.getChildren().addAll(
                        clientHeader,
                        createInfoRow("Company:",     client.getCompany() != null ? client.getCompany() : "N/A"),
                        createInfoRow("Industry:",    client.getIndustry() != null ? client.getIndustry() : "N/A"),
                        createInfoRow("Total Spent:", String.format("%.2f TND", client.getAmount())),
                        createInfoRow("Rating:",      String.format("%.1f", client.getRating()))
                );
            }
        } else if (user.getRole() == UserRole.FREELANCER) {
            Freelancer freelancer = freelancerService.getFreelancerById(user.getIdUser());
            if (freelancer != null) {
                content.getChildren().add(new Separator());
                Label freelancerHeader = new Label("Freelancer Information");
                freelancerHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
                content.getChildren().addAll(
                        freelancerHeader,
                        createInfoRow("Price/Hour:",   String.format("%.2f TND", freelancer.getPricePerHour())),
                        createInfoRow("Total Earned:", String.format("%.2f TND", freelancer.getAmount())),
                        createInfoRow("Rating:",       String.format("%.1f", freelancer.getRating())),
                        createInfoRow("Verification:", freelancer.getVerificationStatus().name()),
                        createInfoRow("Skills:",       String.join(", ", freelancer.getSkills()))
                );
            }
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");

        VBox dialogContent = new VBox();
        dialogContent.getChildren().addAll(header, scrollPane);
        dialogPane.setContent(dialogContent);
        dialogPane.getButtonTypes().add(new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE));

        dialog.setDialogPane(dialogPane);
        dialog.showAndWait();
    }

    private HBox createInfoRow(String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-min-width: 120;");

        Label valueText = new Label(value);
        valueText.setStyle("-fx-text-fill: #34495e;");
        valueText.setWrapText(true);

        row.getChildren().addAll(labelText, valueText);
        return row;
    }

    private void handleToggleUserStatus(User user, Button button) {
        boolean willActivate = !user.isActivated();
        String action = willActivate ? "activate" : "deactivate";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Action");
        confirm.setHeaderText("Are you sure you want to " + action + " this user?");
        confirm.setContentText(
                "User: " + user.getName() + "\n"
                        + "Email: " + user.getEmail() + "\n\n"
                        + (willActivate
                        ? "This will allow the user to login again."
                        : "This will prevent the user from logging in.")
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (willActivate) {
                        userService.reactivateUser(user.getIdUser());
                        showSuccessAlert("Success", "User account activated successfully!");
                    } else {
                        userService.deactivateUser(user.getIdUser());
                        showSuccessAlert("Success", "User account deactivated successfully!");
                    }
                    loadUsers();
                } catch (Exception e) {
                    showErrorAlert("Error", "Failed to " + action + " user: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        roleFilterCombo.setValue("All");
        statusFilterCombo.setValue("All");
        sortByCombo.setValue("Name (A-Z)");
        applyFilters();
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/admin/admin-dashboard.fxml"));
            Parent root = loader.load();

            AdminDashboardController controller = loader.getController();
            if (currentAdmin != null) {
                controller.setAdminData(currentAdmin);
            } else {
                controller.initializeWithoutAdmin();
            }

            Stage stage = (Stage) usersRowsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard - UniEarn");
            stage.setMaximized(true);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to load dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText("Are you sure you want to logout?");
        confirm.setContentText("You will need to login again to access your account.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    redirectToLogin();
                } catch (Exception e) {
                    e.printStackTrace();
                    showErrorAlert("Error", "Failed to logout: " + e.getMessage());
                }
            }
        });
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/login/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usersRowsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login - UniEarn");
            stage.setMaximized(true);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load login page: " + e.getMessage());
        }
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
