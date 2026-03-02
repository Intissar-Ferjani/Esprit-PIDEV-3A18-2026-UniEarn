package uniearn.controller.profile.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import uniearn.model.entities.users.User;
import uniearn.model.entities.users.admin.Admin;
import uniearn.model.enums.UserRole;
import uniearn.services.users.UserService;
import uniearn.database.SessionManager;

import java.io.IOException;
import java.util.List;

public class AdminDashboardController {

    @FXML private Label adminNameLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label clientsLabel;
    @FXML private Label freelancersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label activePercentLabel;

    private final UserService userService = new UserService();
    private Admin currentAdmin;

    @FXML
    public void initialize() {
        System.out.println("AdminDashboardController initialized");
        loadStatistics();
    }

    public void setAdminData(Admin admin) {
        this.currentAdmin = admin;
        if (adminNameLabel != null && admin != null) {
            adminNameLabel.setText("👤 " + admin.getName());
        }
        loadStatistics();
    }

    public void initializeWithoutAdmin() {
        System.out.println("⚠ Admin data not available, loading statistics anyway");
        if (adminNameLabel != null) {
            adminNameLabel.setText("👤 Admin");
        }
        loadStatistics();
    }

    private void loadStatistics() {
        try {
            List<User> allUsers = userService.getAllUsers();

            long clientCount = allUsers.stream()
                    .filter(u -> u.getRole() == UserRole.CLIENT)
                    .count();

            long freelancerCount = allUsers.stream()
                    .filter(u -> u.getRole() == UserRole.FREELANCER)
                    .count();

            long activeCount = allUsers.stream()
                    .filter(User::isActivated)
                    .count();

            int totalCount = allUsers.size();
            double activePercent = totalCount > 0 ? (activeCount * 100.0 / totalCount) : 0;

            if (totalUsersLabel != null) {
                totalUsersLabel.setText(String.valueOf(totalCount));
            }

            if (clientsLabel != null) {
                clientsLabel.setText(clientCount + " Clients");
            }

            if (freelancersLabel != null) {
                freelancersLabel.setText(freelancerCount + " Freelancers");
            }

            if (activeUsersLabel != null) {
                activeUsersLabel.setText(String.valueOf(activeCount));
            }

            if (activePercentLabel != null) {
                activePercentLabel.setText(String.format("%.1f%% of total", activePercent));
            }

            System.out.println("✓ Statistics loaded: " + totalCount + " total users, " + activeCount + " active");

        } catch (Exception e) {
            System.err.println("Error loading statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleContractTemplates() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/admin/admin-contract-templates.fxml"));
            Parent embeddedView = loader.load();

            AdminContractTemplatesController controller = loader.getController();
            controller.setAdminData(currentAdmin);

            // Trouver le StackPane ou VBox principal pour remplacer la vue
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            stage.setScene(new Scene(embeddedView, 1200, 700));
            stage.setTitle("Gestion des Templates - UniEarn");

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load contract templates page: " + e.getMessage());
        }
    }

    @FXML
    private void handleManageUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/admin/list-users.fxml"));
            Parent root = loader.load();

            ListUsersController controller = loader.getController();
            if (currentAdmin != null) {
                controller.setAdminData(currentAdmin);
            } else {
                controller.initializeWithoutAdmin();
            }

            Stage stage = (Stage) totalUsersLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Manage Users - UniEarn");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to load user management page: " + e.getMessage());
        }
    }

    @FXML
    private void handleManageContracts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/contracts/admin_contracts.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1400, 800));
            stage.setTitle("Manage Contracts - UniEarn");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to load contracts management page: " + e.getMessage());
        }
    }

    @FXML
    private void handleManagePayments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/admin/admin-payments.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Manage Payments - UniEarn");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to load payments management page: " + e.getMessage());
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
                    // Clear session on logout
                    SessionManager.getInstance().logout();

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

            Stage stage = (Stage) totalUsersLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 750, 600));
            stage.setTitle("Login - UniEarn");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load login page: " + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}