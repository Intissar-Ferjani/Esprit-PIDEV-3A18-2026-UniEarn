package uniearn.controller.profile.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import uniearn.controller.profile.freelancer.ListFreelancersController;
import uniearn.model.entities.users.client.Client;
import uniearn.services.users.client.ClientService;
import uniearn.services.users.UserService;
import uniearn.database.SessionManager;
import uniearn.utils.user.PasswordUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

public class ClientProfileController {

    @FXML private ImageView profileImageView;
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label companyLabel;
    @FXML private Label industryLabel;
    @FXML private Label ratingLabel;
    @FXML private Label totalSpentLabel;
    @FXML private Label activeProjectsLabel;
    @FXML private Label completedProjectsLabel;
    @FXML private VBox postedProjectsContainer;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private TextField searchField;
    @FXML private Button editProfileButton;
    @FXML private Button postProjectButton;
    @FXML private Button profileBtn;

    private final ClientService clientService = new ClientService();
    private final UserService userService = new UserService();
    private Client currentClient;

    @FXML
    public void initialize() {
        System.out.println("Enhanced ClientProfileController initialized");

        if (statusFilterCombo != null) {
            statusFilterCombo.getItems().addAll(
                    "All Status",
                    "Active",
                    "Completed",
                    "In Progress",
                    "Cancelled"
            );
            statusFilterCombo.setValue("All Status");
        }
    }

    public void setClientData(Client client) {
        this.currentClient = client;

        if (client != null) {
            populateProfileData();
            loadStatistics();
            loadPostedProjects();
        } else {
            showErrorAlert("Error", "Unable to load client profile data.");
        }
    }

    private void populateProfileData() {
        if (nameLabel != null) {
            nameLabel.setText(currentClient.getName());
        }

        if (emailLabel != null) {
            emailLabel.setText(currentClient.getEmail());
        }

        if (companyLabel != null) {
            companyLabel.setText(currentClient.getCompany() != null && !currentClient.getCompany().isEmpty()
                    ? currentClient.getCompany() : "No Company");
        }

        if (industryLabel != null) {
            industryLabel.setText(currentClient.getIndustry() != null && !currentClient.getIndustry().isEmpty()
                    ? currentClient.getIndustry() : "Not Specified");
        }

        if (ratingLabel != null) {
            ratingLabel.setText(String.format("⭐ %.1f", currentClient.getRating()));
        }

        loadProfilePicture();
        System.out.println("✓ Profile data loaded for: " + currentClient.getName());
    }

    private void loadProfilePicture() {
        try {
            var user = userService.getUserById(currentClient.getIdUser());

            if (user != null && user.getProfilePicturePath() != null && !user.getProfilePicturePath().isEmpty()) {
                File imageFile = new File(user.getProfilePicturePath());

                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    profileImageView.setImage(image);
                    System.out.println("✓ Loaded profile picture: " + user.getProfilePicturePath());
                } else {
                    System.out.println("⚠ Profile picture file not found: " + user.getProfilePicturePath());
                    setDefaultProfilePicture();
                }
            } else {
                System.out.println("Using default avatar - no profile picture set");
                setDefaultProfilePicture();
            }
        } catch (Exception e) {
            System.out.println("Error loading profile picture: " + e.getMessage());
            setDefaultProfilePicture();
        }
    }

    private void setDefaultProfilePicture() {
        try {
            var defaultImageUrl = getClass().getResource("/images/default-avatar.png");
            if (defaultImageUrl != null) {
                profileImageView.setImage(new Image(defaultImageUrl.toString()));
            }
        } catch (Exception e) {
            System.out.println("No default avatar available");
        }
    }

    @FXML
    private void handleChangePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());

        if (selectedFile != null) {
            try {
                File profileDir = new File("uploads/profiles");
                if (!profileDir.exists()) {
                    profileDir.mkdirs();
                    System.out.println("✓ Created uploads/profiles directory");
                }

                String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                String filename = "client_" + currentClient.getIdUser() + extension;
                Path destination = Paths.get(profileDir.getPath(), filename);

                Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("✓ Copied file to: " + destination);

                Image image = new Image(destination.toUri().toString());
                profileImageView.setImage(image);

                String relativePath = "uploads/profiles/" + filename;

                try {
                    userService.updateProfilePicture(currentClient.getIdUser(), relativePath);
                    currentClient.setProfilePicturePath(relativePath);
                    showSuccessAlert("Success", "Profile picture updated successfully!");
                    System.out.println("✓ Profile picture updated in database: " + relativePath);
                } catch (SQLException e) {
                    e.printStackTrace();
                    showErrorAlert("Database Error", "Failed to update profile picture in database: " + e.getMessage());
                }

            } catch (IOException e) {
                e.printStackTrace();
                showErrorAlert("Error", "Failed to save profile picture: " + e.getMessage());
            }
        }
    }

    private void loadPostedProjects() {
        postedProjectsContainer.getChildren().clear();

        VBox placeholder = new VBox(10);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setStyle("-fx-padding: 40px;");

        Label icon = new Label("📋");
        icon.setStyle("-fx-font-size: 48px;");

        Label text = new Label("No projects posted yet");
        text.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 16px;");

        Label subtext = new Label("Post your first project to get started!");
        subtext.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 13px;");

        placeholder.getChildren().addAll(icon, text, subtext);
        postedProjectsContainer.getChildren().add(placeholder);
    }

    private void loadStatistics() {
        totalSpentLabel.setText(String.format("%.2f TND", currentClient.getAmount()));
        activeProjectsLabel.setText("3");
        completedProjectsLabel.setText("10");
        System.out.println("✓ Statistics loaded");
    }

    @FXML
    private void handleEditProfile() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile");
        dialog.initModality(Modality.APPLICATION_MODAL);

        DialogPane dialogPane = new DialogPane();
        dialogPane.setStyle("-fx-background-color: white;");

        VBox header = new VBox(5);
        header.setStyle("-fx-background-color: #1976d2; -fx-padding: 20px;");
        Label headerLabel = new Label("✏️ Edit Profile");
        headerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        header.getChildren().add(headerLabel);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 25, 25, 25));
        grid.setStyle("-fx-background-color: white;");

        // Profile fields
        TextField nameField = new TextField(currentClient.getName());
        nameField.setStyle("-fx-pref-width: 300px; -fx-font-size: 13px;");

        TextField emailField = new TextField(currentClient.getEmail());
        emailField.setStyle("-fx-pref-width: 300px; -fx-font-size: 13px;");

        TextField companyField = new TextField(currentClient.getCompany());
        companyField.setStyle("-fx-pref-width: 300px; -fx-font-size: 13px;");

        TextField industryField = new TextField(currentClient.getIndustry());
        industryField.setStyle("-fx-pref-width: 300px; -fx-font-size: 13px;");

        // Password change fields
        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Enter current password");
        currentPasswordField.setStyle("-fx-pref-width: 300px; -fx-font-size: 13px;");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Enter new password (min 8 characters)");
        newPasswordField.setStyle("-fx-pref-width: 300px; -fx-font-size: 13px;");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm new password");
        confirmPasswordField.setStyle("-fx-pref-width: 300px; -fx-font-size: 13px;");

        // Password strength indicator
        Label passwordStrengthLabel = new Label();
        passwordStrengthLabel.setStyle("-fx-font-size: 11px;");
        passwordStrengthLabel.setVisible(false);

        // Real-time password strength validation
        newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                passwordStrengthLabel.setVisible(false);
            } else {
                passwordStrengthLabel.setVisible(true);
                int strength = calculatePasswordStrength(newVal);
                switch (strength) {
                    case 0:
                        passwordStrengthLabel.setText("⚠ Weak password");
                        passwordStrengthLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
                        break;
                    case 1:
                        passwordStrengthLabel.setText("⚡ Medium password");
                        passwordStrengthLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 11px;");
                        break;
                    case 2:
                        passwordStrengthLabel.setText("✅ Strong password");
                        passwordStrengthLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 11px;");
                        break;
                }
            }
        });

        // Add fields to grid
        int row = 0;
        grid.add(createLabel("Full Name:"), 0, row);
        grid.add(nameField, 1, row++);

        grid.add(createLabel("Email Address:"), 0, row);
        grid.add(emailField, 1, row++);

        grid.add(createLabel("Company:"), 0, row);
        grid.add(companyField, 1, row++);

        grid.add(createLabel("Industry:"), 0, row);
        grid.add(industryField, 1, row++);

        // Password change section
        Separator passwordSeparator = new Separator();
        GridPane.setColumnSpan(passwordSeparator, 2);
        grid.add(passwordSeparator, 0, row++);

        Label passwordSectionLabel = new Label("🔒 Change Password");
        passwordSectionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        GridPane.setColumnSpan(passwordSectionLabel, 2);
        grid.add(passwordSectionLabel, 0, row++);

        Label passwordNote = new Label("Leave blank to keep your current password");
        passwordNote.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");
        GridPane.setColumnSpan(passwordNote, 2);
        grid.add(passwordNote, 0, row++);

        grid.add(createLabel("Current Password:"), 0, row);
        grid.add(currentPasswordField, 1, row++);

        grid.add(createLabel("New Password:"), 0, row);
        VBox newPasswordBox = new VBox(5, newPasswordField, passwordStrengthLabel);
        grid.add(newPasswordBox, 1, row++);

        grid.add(createLabel("Confirm Password:"), 0, row);
        grid.add(confirmPasswordField, 1, row++);

        // Deactivate Account Section
        Separator separator = new Separator();
        GridPane.setColumnSpan(separator, 2);
        grid.add(separator, 0, row++);

        VBox dangerZone = new VBox(10);
        dangerZone.setStyle("-fx-background-color: #fff5f5; -fx-border-color: #fc8181; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 15px;");
        GridPane.setColumnSpan(dangerZone, 2);

        Label dangerLabel = new Label("⚠ Danger Zone");
        dangerLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #c53030; -fx-font-size: 14px;");

        Label dangerDesc = new Label("Once you deactivate your account, there is no going back.");
        dangerDesc.setStyle("-fx-text-fill: #742a2a; -fx-font-size: 12px;");

        Button deactivateBtn = new Button("Deactivate Account");
        deactivateBtn.setStyle("-fx-background-color: #c53030; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        deactivateBtn.setOnAction(e -> handleDeactivateAccount(dialog));

        dangerZone.getChildren().addAll(dangerLabel, dangerDesc, deactivateBtn);
        grid.add(dangerZone, 0, row);

        VBox content = new VBox();
        content.getChildren().addAll(header, grid);
        dialogPane.setContent(content);

        ButtonType saveButton = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(saveButton, cancelButton);

        dialog.setDialogPane(dialogPane);

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveButton) {
                try {
                    String currentPassword = currentPasswordField.getText();
                    String newPassword = newPasswordField.getText();
                    String confirmPassword = confirmPasswordField.getText();

                    boolean passwordChangeRequested = !currentPassword.isEmpty() ||
                            !newPassword.isEmpty() ||
                            !confirmPassword.isEmpty();

                    // Step 1: Validate and update password FIRST (before profile update)
                    if (passwordChangeRequested) {
                        if (!validatePasswordChange(currentPassword, newPassword, confirmPassword)) {
                            return;
                        }
                        // Use userService.updatePassword() directly — hashes and saves
                        userService.updatePassword(currentClient.getIdUser(), newPassword);
                        System.out.println("✅ Password updated via userService");
                    }

                    // Step 2: Update profile info using clientService.updateClient()
                    // This is safe — updateClient() does NOT touch the password column
                    currentClient.setName(nameField.getText().trim());
                    currentClient.setEmail(emailField.getText().trim());
                    currentClient.setCompany(companyField.getText().trim());
                    currentClient.setIndustry(industryField.getText().trim());

                    clientService.updateClient(currentClient.getIdUser(), currentClient);
                    populateProfileData();

                    if (passwordChangeRequested) {
                        showSuccessAlert("Success", "✅ Profile and password updated successfully!");
                    } else {
                        showSuccessAlert("Success", "Profile updated successfully!");
                    }

                } catch (Exception ex) {
                    showErrorAlert("Update Failed", "Failed to update profile: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });    }

    // Validate password change with BCrypt verification
    private boolean validatePasswordChange(String currentPassword, String newPassword, String confirmPassword) {
        // Check if all password fields are filled
        if (currentPassword.isEmpty() && (newPassword.isEmpty() || confirmPassword.isEmpty())) {
            showErrorAlert("Validation Error", "Please enter your current password to change it.");
            return false;
        }

        if (currentPassword.isEmpty()) {
            showErrorAlert("Validation Error", "Please enter your current password.");
            return false;
        }

        // Verify current password using BCrypt
        try {
            var user = userService.getUserById(currentClient.getIdUser());
            if (user == null) {
                showErrorAlert("Error", "User not found.");
                return false;
            }

            if (!PasswordUtil.verifyPassword(currentPassword, user.getPassword())) {
                showErrorAlert("Validation Error", "❌ Current password is incorrect.\n\nPlease try again.");
                return false;
            }
        } catch (Exception e) {
            showErrorAlert("Error", "Failed to verify current password: " + e.getMessage());
            return false;
        }

        // Validate new password
        if (newPassword.isEmpty()) {
            showErrorAlert("Validation Error", "Please enter a new password.");
            return false;
        }

        if (newPassword.length() < 8) {
            showErrorAlert("Validation Error", "New password must be at least 8 characters long.");
            return false;
        }

        if (!newPassword.matches(".*[A-Z].*")) {
            showErrorAlert("Validation Error", "New password must contain at least one uppercase letter.");
            return false;
        }

        if (!newPassword.matches(".*[a-z].*")) {
            showErrorAlert("Validation Error", "New password must contain at least one lowercase letter.");
            return false;
        }

        if (!newPassword.matches(".*\\d.*")) {
            showErrorAlert("Validation Error", "New password must contain at least one number.");
            return false;
        }

        // Check if passwords match
        if (!newPassword.equals(confirmPassword)) {
            showErrorAlert("Validation Error", "❌ New passwords do not match.\n\nPlease make sure both passwords are identical.");
            return false;
        }

        // Check if new password is same as current password
        if (currentPassword.equals(newPassword)) {
            showErrorAlert("Validation Error", "New password must be different from your current password.");
            return false;
        }

        return true;
    }

    // Calculate password strength
    private int calculatePasswordStrength(String password) {
        int strength = 0;

        if (password.length() >= 8) strength++;
        if (password.matches(".*[A-Z].*") && password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*\\d.*")) strength++;
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) strength++;

        if (strength <= 1) return 0; // Weak
        if (strength <= 3) return 1; // Medium
        return 2; // Strong
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        return label;
    }

    private void handleDeactivateAccount(Dialog<?> parentDialog) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Deactivate Account");
        confirm.setHeaderText("Are you absolutely sure?");
        confirm.setContentText("Type 'DEACTIVATE' to confirm:");

        TextField confirmField = new TextField();
        confirmField.setPromptText("Type DEACTIVATE");

        VBox content = new VBox(10, new Label(confirm.getContentText()), confirmField);
        content.setPadding(new Insets(10));
        confirm.getDialogPane().setContent(content);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && "DEACTIVATE".equalsIgnoreCase(confirmField.getText())) {
                try {
                    userService.deactivateUser(currentClient.getIdUser());
                    SessionManager.getInstance().logout();
                    showSuccessAlert("Account Deactivated", "Your account has been deactivated. Contact support to reactivate your account.");
                    parentDialog.close();
                    redirectToLogin();
                } catch (Exception e) {
                    showErrorAlert("Error", "Failed to deactivate account: " + e.getMessage());
                    e.printStackTrace();
                }
            } else if (response == ButtonType.OK) {
                showErrorAlert("Confirmation Failed", "You must type 'DEACTIVATE' to confirm.");
            }
        });
    }

    @FXML
    private void handleSettings() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Settings");
        alert.setHeaderText("Account Settings");
        alert.setContentText("Settings page coming soon!\n\nFeatures:\n• Notification preferences\n• Privacy settings\n• Language selection");
        alert.showAndWait();
    }

    @FXML
    private void handlePostProject() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Post Project");
        alert.setHeaderText("Create a New Project");
        alert.setContentText("Post project functionality coming soon!");
        alert.showAndWait();
    }

    @FXML
    private void handleBrowseFreelancers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/freelancer/list-freelancers.fxml"));
            Parent root = loader.load();

            ListFreelancersController controller = loader.getController();
            controller.setClientData(currentClient);

            Stage stage = (Stage) nameLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Browse Freelancers - UniEarn");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load browse freelancers page: " + e.getMessage());
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

            Stage stage = null;
            if (nameLabel != null && nameLabel.getScene() != null) {
                stage = (Stage) nameLabel.getScene().getWindow();
            } else if (profileImageView != null && profileImageView.getScene() != null) {
                stage = (Stage) profileImageView.getScene().getWindow();
            } else if (editProfileButton != null && editProfileButton.getScene() != null) {
                stage = (Stage) editProfileButton.getScene().getWindow();
            }

            if (stage != null) {
                stage.setScene(new Scene(root, 750, 600));
                stage.setTitle("Login - UniEarn");
                stage.centerOnScreen();
            } else {
                showErrorAlert("Error", "Unable to navigate to login page.");
            }
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