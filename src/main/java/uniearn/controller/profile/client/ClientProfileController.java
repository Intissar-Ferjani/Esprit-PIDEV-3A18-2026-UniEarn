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
import javafx.scene.shape.Circle;
import javafx.scene.input.MouseEvent;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import uniearn.controller.profile.freelancer.ListFreelancersController;
import uniearn.model.entities.users.client.Client;
import uniearn.services.users.client.ClientService;
import uniearn.services.users.UserService;
import uniearn.database.SessionManager;
import uniearn.utils.user.PasswordUtil;
import uniearn.model.entities.projet.Project;
import uniearn.services.projet.ProjectService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

public class ClientProfileController {

    @FXML
    private ImageView profileImageView;
    @FXML
    private Label nameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label companyLabel;
    @FXML
    private Label industryLabel;
    @FXML
    private Label activeProjectsLabel;
    @FXML
    private Label completedProjectsLabel;
    @FXML
    private VBox postedProjectsContainer;
    @FXML
    private ComboBox<String> statusFilterCombo;
    @FXML
    private Button editProfileButton;
    @FXML
    private Button postProjectButton;
    @FXML
    private Button profileBtn;
    @FXML
    private StackPane contentArea;
    @FXML
    private ScrollPane dashboardView;
    @FXML
    private HBox topBar;
    @FXML
    private Button btnMaximize;

    private double xOffset = 0;
    private double yOffset = 0;

    private Parent embeddedDashboard;

    private final ClientService clientService = new ClientService();
    private final UserService userService = new UserService();
    private final ProjectService projectService = new ProjectService();
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
                    "Cancelled");
            statusFilterCombo.setValue("All Status");
        }
    }

    public void setClientData(Client client) {
        this.currentClient = client;

        if (client != null) {
            populateProfileData();
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
                    ? currentClient.getCompany()
                    : "No Company");
        }

        if (industryLabel != null) {
            industryLabel.setText(currentClient.getIndustry() != null && !currentClient.getIndustry().isEmpty()
                    ? currentClient.getIndustry()
                    : "Not Specified");
        }

        loadProfilePicture();
        System.out.println("✓ Profile data loaded for: " + currentClient.getName());
    }

    private void applyCircleClip() {
        double radius = profileImageView.getFitWidth() / 2;
        Circle clip = new Circle(radius, radius, radius);
        profileImageView.setClip(clip);
    }

    private void loadProfilePicture() {
        try {
            var user = userService.getUserById(currentClient.getIdUser());

            if (user != null && user.getProfilePicturePath() != null && !user.getProfilePicturePath().isEmpty()) {
                File imageFile = new File(user.getProfilePicturePath());

                if (imageFile.exists()) {
                    profileImageView.setImage(new Image(imageFile.toURI().toString()));
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
        } finally {
            applyCircleClip();
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
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

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

                profileImageView.setImage(new Image(destination.toUri().toString()));
                applyCircleClip();

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

        java.util.List<Project> projects = projectService.getProjectsByClientId(currentClient.getIdClient());

        if (projects.isEmpty()) {
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
        } else {
            for (Project project : projects) {
                postedProjectsContainer.getChildren().add(createProjectCard(project));
            }
            if (activeProjectsLabel != null) {
                activeProjectsLabel.setText(String.valueOf(projects.size()));
            }
        }
    }

    private VBox createProjectCard(Project project) {
        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color: white; -fx-padding: 15; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(project.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1a202c;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        int s = project.getStatus();
        String statusText;
        String statusColor;
        try {
            statusText = uniearn.model.enums.taskstatusenum.values()[s].name();
            if (s == 0)
                statusColor = "#3182ce"; // TODO
            else if (s == 1)
                statusColor = "#d69e2e"; // DOING
            else
                statusColor = "#38a169"; // DONE
        } catch (Exception e) {
            statusText = "UNKNOWN";
            statusColor = "#718096";
        }

        Label statusBadge = new Label(statusText);
        statusBadge.setStyle("-fx-background-color: " + statusColor + "20; -fx-text-fill: " + statusColor
                + "; -fx-padding: 5 10; -fx-background-radius: 12; -fx-font-size: 12px; -fx-font-weight: bold;");

        header.getChildren().addAll(title, spacer, statusBadge);

        Label desc = new Label(project.getDescription());
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 14px;");

        HBox footer = new HBox(20);
        footer.setAlignment(Pos.CENTER_LEFT);

        Label budget = new Label(String.format("💰 %.2f TND", project.getBudget()));
        budget.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748;");

        footer.getChildren().add(budget);

        card.getChildren().addAll(header, desc, footer);
        return card;
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

        TextField nameField = new TextField(currentClient.getName());
        nameField.setStyle("-fx-pref-width: 300px; -fx-font-size: 13px;");

        TextField emailField = new TextField(currentClient.getEmail());
        emailField.setStyle("-fx-pref-width: 300px; -fx-font-size: 13px;");

        TextField companyField = new TextField(currentClient.getCompany());
        companyField.setStyle("-fx-pref-width: 300px; -fx-font-size: 13px;");

        TextField industryField = new TextField(currentClient.getIndustry());
        industryField.setStyle("-fx-pref-width: 300px; -fx-font-size: 13px;");

        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Enter current password");
        currentPasswordField.setStyle("-fx-pref-width: 300px; -fx-font-size: 13px;");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Enter new password (min 8 characters)");
        newPasswordField.setStyle("-fx-pref-width: 300px; -fx-font-size: 13px;");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm new password");
        confirmPasswordField.setStyle("-fx-pref-width: 300px; -fx-font-size: 13px;");

        Label passwordStrengthLabel = new Label();
        passwordStrengthLabel.setStyle("-fx-font-size: 11px;");
        passwordStrengthLabel.setVisible(false);

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

        int row = 0;
        grid.add(createLabel("Full Name:"), 0, row);
        grid.add(nameField, 1, row++);

        grid.add(createLabel("Email Address:"), 0, row);
        grid.add(emailField, 1, row++);

        grid.add(createLabel("Company:"), 0, row);
        grid.add(companyField, 1, row++);

        grid.add(createLabel("Industry:"), 0, row);
        grid.add(industryField, 1, row++);

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

        Separator separator = new Separator();
        GridPane.setColumnSpan(separator, 2);
        grid.add(separator, 0, row++);

        VBox dangerZone = new VBox(10);
        dangerZone.setStyle(
                "-fx-background-color: #fff5f5; -fx-border-color: #fc8181; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 15px;");
        GridPane.setColumnSpan(dangerZone, 2);

        Label dangerLabel = new Label("⚠ Danger Zone");
        dangerLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #c53030; -fx-font-size: 14px;");

        Label dangerDesc = new Label("Once you deactivate your account, there is no going back.");
        dangerDesc.setStyle("-fx-text-fill: #742a2a; -fx-font-size: 12px;");

        Button deactivateBtn = new Button("Deactivate Account");
        deactivateBtn.setStyle(
                "-fx-background-color: #c53030; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
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

                    if (passwordChangeRequested) {
                        if (!validatePasswordChange(currentPassword, newPassword, confirmPassword)) {
                            return;
                        }
                        userService.updatePassword(currentClient.getIdUser(), newPassword);
                        System.out.println("✅ Password updated via userService");
                    }

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
        });
    }

    private boolean validatePasswordChange(String currentPassword, String newPassword, String confirmPassword) {
        if (currentPassword.isEmpty() && (newPassword.isEmpty() || confirmPassword.isEmpty())) {
            showErrorAlert("Validation Error", "Please enter your current password to change it.");
            return false;
        }

        if (currentPassword.isEmpty()) {
            showErrorAlert("Validation Error", "Please enter your current password.");
            return false;
        }

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

        if (!newPassword.equals(confirmPassword)) {
            showErrorAlert("Validation Error",
                    "❌ New passwords do not match.\n\nPlease make sure both passwords are identical.");
            return false;
        }

        if (currentPassword.equals(newPassword)) {
            showErrorAlert("Validation Error", "New password must be different from your current password.");
            return false;
        }

        return true;
    }

    private int calculatePasswordStrength(String password) {
        int strength = 0;

        if (password.length() >= 8)
            strength++;
        if (password.matches(".*[A-Z].*") && password.matches(".*[a-z].*"))
            strength++;
        if (password.matches(".*\\d.*"))
            strength++;
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*"))
            strength++;

        if (strength <= 1)
            return 0;
        if (strength <= 3)
            return 1;
        return 2;
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
                    showSuccessAlert("Account Deactivated",
                            "Your account has been deactivated. Contact support to reactivate your account.");
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
        alert.setContentText(
                "Settings page coming soon!\n\nFeatures:\n• Notification preferences\n• Privacy settings\n• Language selection");
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
            Parent embeddedView = loader.load();

            ListFreelancersController controller = loader.getController();
            controller.setClientData(currentClient);

            dashboardView.setVisible(false);
            dashboardView.setManaged(false);
            if (embeddedDashboard != null) {
                embeddedDashboard.setVisible(false);
                embeddedDashboard.setManaged(false);
            }

            cleanupContentArea();
            contentArea.getChildren().add(embeddedView);
            embeddedView.setVisible(true);
            embeddedView.setManaged(true);

            System.out.println("✓ Embedded Browse Freelancers loaded into contentArea");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load browse freelancers page: " + e.getMessage());
        }
    }

    @FXML
    private void handleMesProjets() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/client/Projet.fxml"));
            Parent embeddedView = loader.load();

            uniearn.controller.projet.ProjectController controller = loader.getController();
            controller.setClientData(currentClient);

            cleanupContentArea();
            contentArea.getChildren().add(embeddedView);
            embeddedView.setVisible(true);
            embeddedView.setManaged(true);

            System.out.println("✓ Embedded Mes Projets loaded into contentArea");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load projects page: " + e.getMessage());
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
                stage.setScene(new Scene(root));
                stage.setTitle("Login - UniEarn");
                stage.setMaximized(true);
                stage.centerOnScreen();
            } else {
                showErrorAlert("Error", "Unable to navigate to login page.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load login page: " + e.getMessage());
        }
    }

    @FXML
    private void handleApplications() {
        try {
            if (embeddedDashboard == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/ClientDashboardView.fxml"));
                embeddedDashboard = loader.load();
            }

            cleanupContentArea();
            if (!contentArea.getChildren().contains(embeddedDashboard)) {
                contentArea.getChildren().add(embeddedDashboard);
            }
            embeddedDashboard.setVisible(true);
            embeddedDashboard.setManaged(true);

            System.out.println("✓ Embedded Applications Dashboard loaded into contentArea");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load applications page: " + e.getMessage());
        }
    }

    @FXML
    private void handleMesContrats() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/client/client-contracts.fxml"));
            Parent embeddedView = loader.load();

            ClientContractsController controller = loader.getController();
            controller.setClientData(currentClient);

            dashboardView.setVisible(false);
            dashboardView.setManaged(false);
            if (embeddedDashboard != null) {
                embeddedDashboard.setVisible(false);
                embeddedDashboard.setManaged(false);
            }

            cleanupContentArea();
            contentArea.getChildren().add(embeddedView);
            embeddedView.setVisible(true);
            embeddedView.setManaged(true);

            System.out.println("✓ Embedded Contracts page loaded into contentArea");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load contracts page: " + e.getMessage());
        }
    }

    @FXML
    private void handleShowDashboard() {
        cleanupContentArea();
        dashboardView.setVisible(true);
        dashboardView.setManaged(true);
        System.out.println("✓ Switched back to main profile dashboard");
    }

    private void cleanupContentArea() {
        // Hide and unmanage the main dashboard and the applications dashboard
        dashboardView.setVisible(false);
        dashboardView.setManaged(false);

        if (embeddedDashboard != null) {
            embeddedDashboard.setVisible(false);
            embeddedDashboard.setManaged(false);
        }

        // Remove any other dynamic views added to the contentArea
        contentArea.getChildren().removeIf(node -> node != dashboardView && node != embeddedDashboard);
    }

    @FXML
    private void handlePaymentMethods() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/client/client-payment-methods.fxml"));
            Parent embeddedView = loader.load();

            dashboardView.setVisible(false);
            dashboardView.setManaged(false);
            if (embeddedDashboard != null) {
                embeddedDashboard.setVisible(false);
                embeddedDashboard.setManaged(false);
            }

            cleanupContentArea();
            contentArea.getChildren().add(embeddedView);
            embeddedView.setVisible(true);
            embeddedView.setManaged(true);

            System.out.println("✅ Payment Methods section loaded for client");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load payment methods page: " + e.getMessage());
        }
    }

    @FXML
    private void handlePayments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/client/client-payments.fxml"));
            Parent embeddedView = loader.load();

            dashboardView.setVisible(false);
            dashboardView.setManaged(false);
            if (embeddedDashboard != null) {
                embeddedDashboard.setVisible(false);
                embeddedDashboard.setManaged(false);
            }

            cleanupContentArea();
            contentArea.getChildren().add(embeddedView);
            embeddedView.setVisible(true);
            embeddedView.setManaged(true);

            System.out.println("✅ Payments section loaded for client");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load payments page: " + e.getMessage());
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

    // ═══════════════════════════════════════════════════════ WINDOW CONTROLS

    @FXML
    private void handleMinimize() {
        Stage stage = (Stage) topBar.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void handleMaximize() {
        Stage stage = (Stage) topBar.getScene().getWindow();
        if (stage.isMaximized()) {
            stage.setMaximized(false);
            // Change to maximize icon
            if (btnMaximize.getGraphic() instanceof FontIcon) {
                ((FontIcon) btnMaximize.getGraphic()).setIconLiteral("fas-expand-arrows-alt");
            }
        } else {
            stage.setMaximized(true);
            // Change to restore icon
            if (btnMaximize.getGraphic() instanceof FontIcon) {
                ((FontIcon) btnMaximize.getGraphic()).setIconLiteral("fas-compress-arrows-alt");
            }
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) topBar.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleMousePressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    private void handleMouseDragged(MouseEvent event) {
        Stage stage = (Stage) topBar.getScene().getWindow();
        if (!stage.isMaximized()) {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        }
    }
}
