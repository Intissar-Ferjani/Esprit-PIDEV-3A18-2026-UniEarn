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
import uniearn.controller.projet.ProjectController;
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

        // Initialize with default values
        if (roleFilterCombo != null) {
            roleFilterCombo.setValue("All");
        }
        if (statusFilterCombo != null) {
            statusFilterCombo.setValue("All");
        }
        if (sortByCombo != null) {
            sortByCombo.setValue("Name (A-Z)");
        }

        // Setup filter listeners
        setupFilterListeners();

        // Auto-load users on initialization
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
        // Search field listener
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }

        // Filter combo listeners
        if (roleFilterCombo != null) {
            roleFilterCombo.setOnAction(e -> applyFilters());
        }
        if (statusFilterCombo != null) {
            statusFilterCombo.setOnAction(e -> applyFilters());
        }
        if (sortByCombo != null) {
            sortByCombo.setOnAction(e -> applyFilters());
        }
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

        // search filter
        String searchTerm = searchField.getText().toLowerCase().trim();
        if (!searchTerm.isEmpty()) {
            filteredUsers = filteredUsers.stream()
                    .filter(u -> matchesSearchTerm(u, searchTerm))
                    .collect(Collectors.toList());
        }

        // role filter
        String roleFilter = roleFilterCombo.getValue();
        if (!"All".equals(roleFilter)) {
            UserRole targetRole = UserRole.valueOf(roleFilter.toUpperCase());
            filteredUsers = filteredUsers.stream()
                    .filter(u -> u.getRole() == targetRole)
                    .collect(Collectors.toList());
        }

        // status filter
        String statusFilter = statusFilterCombo.getValue();
        if (!"All".equals(statusFilter)) {
            boolean isActive = "Active".equals(statusFilter);
            filteredUsers = filteredUsers.stream()
                    .filter(u -> u.isActivated() == isActive)
                    .collect(Collectors.toList());
        }

        // sorting
        String sortBy = sortByCombo.getValue();
        switch (sortBy) {
            case "Name (A-Z)":
                filteredUsers.sort(Comparator.comparing(User::getName));
                break;
            case "Name (Z-A)":
                filteredUsers.sort(Comparator.comparing(User::getName).reversed());
                break;
            case "Email (A-Z)":
                filteredUsers.sort(Comparator.comparing(User::getEmail));
                break;
            case "Role":
                filteredUsers.sort(Comparator.comparing(u -> u.getRole().name()));
                break;
            case "Status":
                filteredUsers.sort(Comparator.comparing(User::isActivated).reversed());
                break;
        }

        displayUsers();
    }

    private boolean matchesSearchTerm(User user, String searchTerm) {
        // Search in name
        if (user.getName().toLowerCase().contains(searchTerm)) {
            return true;
        }

        // Search in email
        if (user.getEmail().toLowerCase().contains(searchTerm)) {
            return true;
        }

        return false;
    }

    private void displayUsers() {
        usersRowsContainer.getChildren().clear();

        // Update total count
        totalUsersLabel.setText(filteredUsers.size() + " user" +
                (filteredUsers.size() != 1 ? "s" : ""));

        if (filteredUsers.isEmpty()) {
            showEmptyState();
            return;
        }

        // Display each user as a table row
        for (User user : filteredUsers) {
            usersRowsContainer.getChildren().add(createUserRow(user));
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

    private HBox createUserRow(User user) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #e1e8ed; " +
                        "-fx-border-width: 0 0 1 0; " +
                        "-fx-padding: 15;"
        );

        // Name
        Label nameLabel = new Label(user.getName());
        nameLabel.setPrefWidth(200);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #14171a; -fx-font-weight: bold;");

        // Email
        Label emailLabel = new Label(user.getEmail());
        emailLabel.setPrefWidth(220);
        emailLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #657786;");

        // Role Badge
        Label roleLabel = new Label(user.getRole().name());
        roleLabel.setPrefWidth(100);
        String roleColor = getRoleColor(user.getRole());
        roleLabel.setStyle(
                "-fx-background-color: " + roleColor + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 4 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: bold;"
        );
        roleLabel.setMaxWidth(85);

        // Status Badge
        Label statusLabel = new Label(user.isActivated() ? "Active" : "Deactivated");
        statusLabel.setPrefWidth(100);
        String statusColor = user.isActivated() ? "#27ae60" : "#e74c3c";
        statusLabel.setStyle(
                "-fx-background-color: " + statusColor + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 4 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: bold;"
        );
        statusLabel.setMaxWidth(90);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Action Buttons
        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        actionsBox.setPrefWidth(250);

        // View Button
        Button viewBtn = new Button("👁 View");
        viewBtn.setStyle(
                "-fx-background-color: #3498db; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 12px; " +
                        "-fx-padding: 6 16; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"
        );
        viewBtn.setOnAction(e -> handleViewUser(user));

        // Activate/Deactivate Button
        Button toggleStatusBtn = new Button(user.isActivated() ? "🚫 Deactivate" : "✓ Activate");
        String btnColor = user.isActivated() ? "#e74c3c" : "#27ae60";
        toggleStatusBtn.setStyle(
                "-fx-background-color: " + btnColor + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 12px; " +
                        "-fx-padding: 6 16; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"
        );
        toggleStatusBtn.setOnAction(e -> handleToggleUserStatus(user, toggleStatusBtn));

        actionsBox.getChildren().addAll(viewBtn, toggleStatusBtn);

        row.getChildren().addAll(nameLabel, emailLabel, roleLabel, statusLabel, spacer, actionsBox);

        return row;
    }

    private String getRoleColor(UserRole role) {
        switch (role) {
            case ADMIN:
                return "#e74c3c";
            case CLIENT:
                return "#3498db";
            case FREELANCER:
                return "#9b59b6";
            default:
                return "#95a5a6";
        }
    }

    private void handleViewUser(User user) {
        // User view dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("View User Details");
        dialog.initModality(Modality.APPLICATION_MODAL);

        DialogPane dialogPane = new DialogPane();
        dialogPane.setStyle("-fx-background-color: white;");
        dialogPane.setPrefWidth(600);

        // Header
        VBox header = new VBox(10);
        header.setStyle("-fx-background-color: " + getRoleColor(user.getRole()) + "; -fx-padding: 20px;");
        header.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(user.getName());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        Label emailLabel = new Label(user.getEmail());
        emailLabel.setStyle("-fx-text-fill: #ecf0f1; -fx-font-size: 14px;");

        header.getChildren().addAll(nameLabel, emailLabel);

        // Content
        VBox content = new VBox(15);
        content.setPadding(new Insets(25));

        // Basic Info
        content.getChildren().addAll(
                createInfoRow("User ID:", String.valueOf(user.getIdUser())),
                createInfoRow("Role:", user.getRole().name()),
                createInfoRow("Status:", user.isActivated() ? "Active" : "Deactivated"),
                createInfoRow("Email:", user.getEmail())
        );

        // Role-specific info
        if (user.getRole() == UserRole.CLIENT) {
            Client client = clientService.getClientById(user.getIdUser());
            if (client != null) {
                content.getChildren().add(new Separator());
                Label clientHeader = new Label("Client Information");
                clientHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
                content.getChildren().addAll(
                        clientHeader,
                        createInfoRow("Company:", client.getCompany() != null ? client.getCompany() : "N/A"),
                        createInfoRow("Industry:", client.getIndustry() != null ? client.getIndustry() : "N/A"),
                        createInfoRow("Total Spent:", String.format("%.2f TND", client.getAmount())),
                        createInfoRow("Rating:", String.format("%.1f", client.getRating()))
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
                        createInfoRow("Price/Hour:", String.format("%.2f TND", freelancer.getPricePerHour())),
                        createInfoRow("Total Earned:", String.format("%.2f TND", freelancer.getAmount())),
                        createInfoRow("Rating:", String.format("%.1f", freelancer.getRating())),
                        createInfoRow("Verification:", freelancer.getVerificationStatus().name()),
                        createInfoRow("Skills:", String.join(", ", freelancer.getSkills()))
                );
            }
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");

        VBox dialogContent = new VBox();
        dialogContent.getChildren().addAll(header, scrollPane);
        dialogPane.setContent(dialogContent);

        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().add(closeButton);

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
                "User: " + user.getName() + "\n" +
                        "Email: " + user.getEmail() + "\n\n" +
                        (willActivate ? "This will allow the user to login again." :
                                "This will prevent the user from logging in.")
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

                    // Reload users to reflect changes
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
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Admin Dashboard - UniEarn");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to load dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void handleproject() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/client/Projet.fxml"));
            Parent root = loader.load();

            ProjectController controller = loader.getController();

            Stage stage = (Stage) root.getScene().getWindow();
            stage.setScene(new Scene(root, 1600, 900));
            stage.setTitle("Manage Projects - UniEarn");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to load project management page: " + e.getMessage());
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
            stage.setScene(new Scene(root, 750, 600));
            stage.setTitle("Login - UniEarn");
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