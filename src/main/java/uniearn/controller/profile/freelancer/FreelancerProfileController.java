package uniearn.controller.profile.freelancer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import uniearn.controller.profile.freelancer.FreelancerPortfolioController;
import uniearn.controller.projet.TaskBoardController;
import uniearn.database.SessionManager;
import uniearn.model.entities.users.User;
import uniearn.model.entities.users.freelancer.Freelancer;
import uniearn.model.entities.users.freelancer.Portfolio;
import uniearn.services.users.UserService;
import uniearn.services.users.freelancer.FreelancerService;
import uniearn.services.users.freelancer.PortfolioService;
import uniearn.utils.user.PasswordUtil;

import uniearn.controller.shared.ChatbotWidgetController;
import org.kordamp.ikonli.javafx.FontIcon;

public class FreelancerProfileController {

    @FXML
    private ImageView profileImageView;
    @FXML
    private Label nameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label priceLabel;
    @FXML
    private Label ratingLabel;
    @FXML
    private Label verificationLabel;
    @FXML
    private FlowPane skillsContainer;
    @FXML
    private TextArea bioLabel;
    @FXML
    private Label totalEarnedLabel;
    @FXML
    private Label activeContractsLabel;
    @FXML
    private Label completedProjectsLabel;
    @FXML
    private Button editProfileButton;
    @FXML
    private VBox portfolioSection;
    @FXML
    private Button viewPortfolioButton;
    @FXML
    private Button viewPortfolioCardButton;
    @FXML
    private StackPane contentArea;
    @FXML
    private ScrollPane dashboardView;
    @FXML
    private HBox topBar;
    @FXML
    private Button btnMaximize;

    @FXML
    private VBox chatbotWidget;

    private double xOffset = 0;
    private double yOffset = 0;

    private Parent embeddedDashboard;

    @FXML
    private void handleForum() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/freelancer/Forum.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) profileImageView.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Forum - UniEarn");
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Navigation error to Forum.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void handleMessages() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/freelancer/Messages.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) profileImageView.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Messages - UniEarn");
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load Messages page: " + e.getMessage());
        }
    }

    private final FreelancerService freelancerService = new FreelancerService();
    private final UserService userService = new UserService();
    private final PortfolioService portfolioService = new PortfolioService();

    private Freelancer currentFreelancer;
    private Portfolio currentPortfolio;

    @FXML
    public void initialize() {
        System.out.println("FreelancerProfileController initialized");
    }

    public void setFreelancerData(Freelancer freelancer) {
        this.currentFreelancer = freelancer;

        if (freelancer != null) {
            populateProfileData();
            loadStatistics();
            checkPortfolioStatus();
        } else {
            showErrorAlert("Error", "Unable to load freelancer profile data.");
        }
    }

    private void populateProfileData() {
        if (nameLabel != null) {
            nameLabel.setText(currentFreelancer.getName());
        }

        if (emailLabel != null) {
            emailLabel.setText(currentFreelancer.getEmail());
        }

        if (priceLabel != null) {
            priceLabel.setText(String.format("%.2f TND/hr", currentFreelancer.getPricePerHour()));
        }

        if (ratingLabel != null) {
            ratingLabel.setText(String.format(" %.1f", currentFreelancer.getRating()));
        }

        if (verificationLabel != null) {
            String verificationText = switch (currentFreelancer.getVerificationStatus()) {
                case verified -> "Verified";
                case unverified -> "Not Verified";
            };
            verificationLabel.setText(verificationText);
        }

        loadSkills();

        if (bioLabel != null) {
            String bio = currentFreelancer.getBio();
            bioLabel.setText(bio != null && !bio.isEmpty() ? bio : "No bio available");
        }

        loadProfilePicture();
        System.out.println("✓ Profile data loaded for: " + currentFreelancer.getName());
    }

    private void loadSkills() {
        if (skillsContainer != null && currentFreelancer.getSkills() != null) {
            skillsContainer.getChildren().clear();

            for (String skill : currentFreelancer.getSkills()) {
                if (skill != null && !skill.trim().isEmpty()) {
                    Label skillLabel = new Label(skill.trim());
                    skillLabel.setStyle(
                            "-fx-background-color: #e3f2fd; " +
                                    "-fx-text-fill: #1976d2; " +
                                    "-fx-padding: 4 12; " +
                                    "-fx-background-radius: 12; " +
                                    "-fx-font-size: 12px;");
                    skillsContainer.getChildren().add(skillLabel);
                }
            }
        }
    }

    private void applyCircleClip() {
        double radius = profileImageView.getFitWidth() / 2;
        Circle clip = new Circle(radius, radius, radius);
        profileImageView.setClip(clip);
    }

    private void loadProfilePicture() {
        try {
            User user = userService.getUserById(currentFreelancer.getIdUser());
            String picturePath = (user != null) ? user.getProfilePicturePath() : null;

            if (picturePath != null) {
                currentFreelancer.setProfilePicturePath(picturePath);
            }

            if (picturePath != null && !picturePath.isEmpty()) {
                File imageFile = new File(picturePath);
                if (imageFile.exists()) {
                    profileImageView.setImage(new Image(imageFile.toURI().toString()));
                    System.out.println("✓ Loaded profile picture: " + picturePath);
                } else {
                    System.out.println("⚠ Profile picture file not found: " + picturePath);
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

    private void loadStatistics() {
        if (totalEarnedLabel != null) {
            totalEarnedLabel.setText(String.format("%.2f TND", currentFreelancer.getAmount()));
        }

        if (activeContractsLabel != null) {
            activeContractsLabel.setText("2");
        }

        if (completedProjectsLabel != null) {
            completedProjectsLabel.setText("8");
        }

        System.out.println("✓ Statistics loaded");
    }

    private void checkPortfolioStatus() {
        List<Portfolio> portfolios = portfolioService.getAllPortfolios();
        currentPortfolio = portfolios.stream()
                .filter(p -> p.getFreelancerId() == currentFreelancer.getIdFreelancer())
                .findFirst()
                .orElse(null);

        updatePortfolioUI();
    }

    private void updatePortfolioUI() {
        if (portfolioSection == null) {
            System.out.println("⚠ Portfolio section not found in FXML");
            return;
        }

        portfolioSection.getChildren().clear();

        HBox portfolioHeader = new HBox(15);
        portfolioHeader.setAlignment(javafx.geometry.Pos.CENTER);
        VBox.setMargin(portfolioHeader, new Insets(0));

        if (currentPortfolio == null) {
            System.out.println("⚠ No portfolio found - showing Create button");

            VBox createContent = new VBox(10);
            createContent.setAlignment(javafx.geometry.Pos.CENTER);
            createContent.setSpacing(10);

            Label icon = new Label("💼");
            icon.setStyle("-fx-font-size: 48px;");

            Label title = new Label("Build Your Portfolio");
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #333;");

            Label subtitle = new Label("Create a portfolio to showcase your work and attract more clients");
            subtitle.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");

            Button createButton = new Button("+ Create Portfolio");
            createButton.getStyleClass().add("btn-primary");
            createButton.setStyle("-fx-font-size: 14px; -fx-padding: 12 24;");
            createButton.setOnAction(e -> handleCreatePortfolio());

            createContent.getChildren().addAll(icon, title, subtitle, createButton);
            portfolioHeader.getChildren().add(createContent);

        } else {
            System.out.println("✓ Portfolio loaded - showing View button");

            VBox viewContent = new VBox(10);
            viewContent.setAlignment(javafx.geometry.Pos.CENTER);
            viewContent.setSpacing(10);
            HBox.setHgrow(viewContent, Priority.ALWAYS);

            Label icon = new Label("💼");
            icon.setStyle("-fx-font-size: 48px;");

            Label title = new Label("Showcase Your Work");
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #333;");

            Label subtitle = new Label("Build your portfolio to attract more clients");
            subtitle.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");

            Button viewButton = new Button("View My Portfolio →");
            viewButton.getStyleClass().add("btn-primary");
            viewButton.setStyle("-fx-font-size: 14px; -fx-padding: 12 24;");
            viewButton.setOnAction(e -> handleViewPortfolio());

            viewContent.getChildren().addAll(icon, title, subtitle, viewButton);
            portfolioHeader.getChildren().add(viewContent);
        }

        portfolioSection.getChildren().add(portfolioHeader);
    }

    @FXML
    private void handleCreatePortfolio() {
        Portfolio newPortfolio = new Portfolio();
        newPortfolio.setTitle(currentFreelancer.getName() + "'s Portfolio");
        newPortfolio.setDescription("My professional portfolio");
        newPortfolio.setCreated_At(new Timestamp(System.currentTimeMillis()));
        newPortfolio.setFreelancerId(currentFreelancer.getIdFreelancer());

        try {
            portfolioService.addPortfolio(newPortfolio);
            currentPortfolio = newPortfolio;

            showSuccessAlert("Success", "Portfolio created successfully!");
            updatePortfolioUI();

            System.out.println("✓ Portfolio created for freelancer ID: " + currentFreelancer.getIdFreelancer());

        } catch (Exception e) {
            showErrorAlert("Error", "Failed to create portfolio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewPortfolio() {
        if (currentPortfolio == null) {
            showErrorAlert("Error", "No portfolio found. Please create a portfolio first.");
            checkPortfolioStatus();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/freelancer/freelancer-portfolio.fxml"));
            Parent root = loader.load();

            FreelancerPortfolioController controller = loader.getController();
            controller.setFreelancerData(currentFreelancer, currentPortfolio);

            Stage stage = (Stage) nameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("My Portfolio - UniEarn");
            stage.setMaximized(true);

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load portfolio page: " + e.getMessage());
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
                }

                String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                String filename = currentFreelancer.getIdUser() + extension;
                Path destination = Paths.get(profileDir.getPath(), filename);

                Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                profileImageView.setImage(new Image(destination.toUri().toString()));
                applyCircleClip();

                String relativePath = "uploads/profiles/" + filename;

                try {
                    userService.updateProfilePicture(currentFreelancer.getIdUser(), relativePath);
                    showSuccessAlert("Success", "Profile picture updated successfully!");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showErrorAlert("Database Error", "Failed to update profile picture: " + e.getMessage());
                }

            } catch (IOException e) {
                e.printStackTrace();
                showErrorAlert("Error", "Failed to save profile picture: " + e.getMessage());
            }
        }
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
        Label headerLabel = new Label("✏");
        headerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        header.getChildren().add(headerLabel);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 25, 25, 25));
        grid.setStyle("-fx-background-color: white;");

        TextField nameField = new TextField(currentFreelancer.getName());
        nameField.setStyle("-fx-pref-width: 300px; -fx-font-size: 13px;");

        TextField emailField = new TextField(currentFreelancer.getEmail());
        emailField.setStyle("-fx-pref-width: 300px; -fx-font-size: 13px;");

        TextField priceField = new TextField(String.valueOf(currentFreelancer.getPricePerHour()));
        priceField.setStyle("-fx-pref-width: 300px; -fx-font-size: 13px;");

        TextField skillsField = new TextField(String.join(", ", currentFreelancer.getSkills()));
        skillsField.setStyle("-fx-pref-width: 300px; -fx-font-size: 13px;");

        TextArea bioArea = new TextArea(currentFreelancer.getBio());
        bioArea.setWrapText(true);
        bioArea.setPrefRowCount(4);
        bioArea.setStyle("-fx-pref-width: 300px; -fx-font-size: 13px;");

        int row = 0;
        grid.add(createLabel("Full Name:"), 0, row);
        grid.add(nameField, 1, row++);

        grid.add(createLabel("Email Address:"), 0, row);
        grid.add(emailField, 1, row++);

        grid.add(createLabel("Price Per Hour (TND):"), 0, row);
        grid.add(priceField, 1, row++);

        grid.add(createLabel("Skills (comma-separated):"), 0, row);
        grid.add(skillsField, 1, row++);

        grid.add(createLabel("Bio:"), 0, row);
        GridPane.setValignment(createLabel("Bio:"), VPos.TOP);
        grid.add(bioArea, 1, row++);

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

        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Enter current password");
        currentPasswordField.setStyle("-fx-pref-width: 300px; -fx-font-size: 13px;");
        grid.add(createLabel("Current Password:"), 0, row);
        grid.add(currentPasswordField, 1, row++);

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Enter new password (min 8 characters)");
        newPasswordField.setStyle("-fx-pref-width: 300px; -fx-font-size: 13px;");

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

        grid.add(createLabel("New Password:"), 0, row);
        VBox newPasswordBox = new VBox(5, newPasswordField, passwordStrengthLabel);
        grid.add(newPasswordBox, 1, row++);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm new password");
        confirmPasswordField.setStyle("-fx-pref-width: 300px; -fx-font-size: 13px;");
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

        Label dangerDesc = new Label("Once you deactivate your account, you will not be able to login.");
        dangerDesc.setStyle("-fx-text-fill: #742a2a; -fx-font-size: 12px;");

        Button deactivateBtn = new Button("Deactivate Account");
        deactivateBtn.setStyle(
                "-fx-background-color: #c53030; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        deactivateBtn.setOnAction(e -> handleDeactivateAccount(dialog));

        dangerZone.getChildren().addAll(dangerLabel, dangerDesc, deactivateBtn);
        grid.add(dangerZone, 0, row);

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        scrollPane.setStyle("-fx-background-color: white; -fx-background: white;");

        VBox content = new VBox();
        content.getChildren().addAll(header, scrollPane);
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
                        userService.updatePassword(currentFreelancer.getIdUser(), newPassword);
                        System.out.println("✅ Password updated via userService");

                        User refreshed = userService.getUserById(currentFreelancer.getIdUser());
                        if (refreshed != null) {
                            currentFreelancer.setPassword(refreshed.getPassword());
                        }
                    }

                    currentFreelancer.setName(nameField.getText().trim());
                    currentFreelancer.setEmail(emailField.getText().trim());
                    currentFreelancer.setPricePerHour(Double.parseDouble(priceField.getText().trim()));

                    String[] skills = skillsField.getText().split(",");
                    for (int i = 0; i < skills.length; i++) {
                        skills[i] = skills[i].trim();
                    }
                    currentFreelancer.setSkills(skills);
                    currentFreelancer.setBio(bioArea.getText().trim());

                    freelancerService.updateFreelancer(currentFreelancer.getIdUser(), currentFreelancer);
                    populateProfileData();

                    if (passwordChangeRequested) {
                        showSuccessAlert("Success", "✅ Profile and password updated successfully!");
                    } else {
                        showSuccessAlert("Success", "Profile updated successfully!");
                    }

                } catch (NumberFormatException ex) {
                    showErrorAlert("Invalid Input", "Please enter a valid price per hour.");
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
            var user = userService.getUserById(currentFreelancer.getIdUser());
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
                    userService.deactivateUser(currentFreelancer.getIdUser());
                    SessionManager.getInstance().logout();
                    showSuccessAlert("Account Deactivated",
                            "Your account has been deactivated. Contact support to reactivate.");
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

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        return label;
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
    public void handleApplications() {
        try {
            if (embeddedDashboard == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/FreelancerDashboardView.fxml"));
                embeddedDashboard = loader.load();
            }

            dashboardView.setVisible(false);
            dashboardView.setManaged(false);

            if (!contentArea.getChildren().contains(embeddedDashboard)) {
                contentArea.getChildren().add(embeddedDashboard);
            }
            embeddedDashboard.setVisible(true);
            embeddedDashboard.setManaged(true);

            System.out.println("✓ Embedded Freelancer Dashboard loaded into contentArea");
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur de navigation", "Impossible d'ouvrir la page des candidatures: " + e.getMessage());
        }
    }

    @FXML
    public void handleEvaluations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/FreelancerEvaluationView.fxml"));
            Parent embeddedView = loader.load();

            dashboardView.setVisible(false);
            dashboardView.setManaged(false);
            if (embeddedDashboard != null) {
                embeddedDashboard.setVisible(false);
                embeddedDashboard.setManaged(false);
            }

            contentArea.getChildren().removeIf(node -> node != dashboardView && node != embeddedDashboard);
            contentArea.getChildren().add(embeddedView);
            embeddedView.setVisible(true);
            embeddedView.setManaged(true);

            System.out.println("✓ Embedded Freelancer Evaluations loaded into contentArea");
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur de navigation", "Impossible d'ouvrir la page des évaluations: " + e.getMessage());
        }
    }

    @FXML
    public void handleShowDashboard() {
        if (embeddedDashboard != null) {
            embeddedDashboard.setVisible(false);
            embeddedDashboard.setManaged(false);
        }

        // Hide any other embedded views
        contentArea.getChildren().removeIf(node -> node != dashboardView && node != embeddedDashboard);

        dashboardView.setVisible(true);
        dashboardView.setManaged(true);
        System.out.println("✓ Switched back to main freelancer dashboard");
    }

    @FXML
    public void handleFreelancerProjects() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/freelancer/freelancer-projects.fxml"));
            Parent root = loader.load();

            uniearn.controller.projet.FreelancerProjectsController controller = loader.getController();
            controller.setFreelancerData(currentFreelancer);

            // Extract the center node if it's a BorderPane to avoid redundant sidebars
            Node content = root;
            if (root instanceof BorderPane) {
                content = ((BorderPane) root).getCenter();
            }

            dashboardView.setVisible(false);
            dashboardView.setManaged(false);
            if (embeddedDashboard != null) {
                embeddedDashboard.setVisible(false);
                embeddedDashboard.setManaged(false);
            }

            // Remove any previously added embedded views except the main dashboard parts
            contentArea.getChildren().removeIf(node -> node != dashboardView && node != embeddedDashboard);

            contentArea.getChildren().add(content);
            content.setVisible(true);
            content.setManaged(true);

            System.out.println("✓ Embedded Freelancer Projects (Center only) loaded into contentArea");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load projects page: " + e.getMessage());
        }
    }

    @FXML
    public void handleTaskBoard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/client/TaskBoard.fxml"));
            Parent root = loader.load();

            // *** KEY FIX: pass the current freelancer so the board loads the right
            // tasks/projects ***
            TaskBoardController controller = loader.getController();
            controller.setFreelancerData(currentFreelancer);

            // Extract the center node if it's a BorderPane to avoid redundant sidebars
            Node content = root;
            if (root instanceof BorderPane) {
                content = ((BorderPane) root).getCenter();
            }

            dashboardView.setVisible(false);
            dashboardView.setManaged(false);
            if (embeddedDashboard != null) {
                embeddedDashboard.setVisible(false);
                embeddedDashboard.setManaged(false);
            }

            contentArea.getChildren().removeIf(node -> node != dashboardView && node != embeddedDashboard);
            contentArea.getChildren().add(content);
            content.setVisible(true);
            content.setManaged(true);

            System.out
                    .println("✓ Embedded Task Board loaded for freelancer ID: " + currentFreelancer.getIdFreelancer());
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load Task Board: " + e.getMessage());
        }
    }

    @FXML
    public void handleMesContrats() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/contracts/freelancer_contracts.fxml"));
            Parent embeddedView = loader.load();

            dashboardView.setVisible(false);
            dashboardView.setManaged(false);
            if (embeddedDashboard != null) {
                embeddedDashboard.setVisible(false);
                embeddedDashboard.setManaged(false);
            }

            contentArea.getChildren().removeIf(node -> node != dashboardView && node != embeddedDashboard);
            contentArea.getChildren().add(embeddedView);
            embeddedView.setVisible(true);
            embeddedView.setManaged(true);

            System.out.println("✓ Embedded Contracts loaded into contentArea");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load contracts page: " + e.getMessage());
        }
    }

    @FXML
    public void handleBrowseFreelancers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/freelancer/list-freelancers.fxml"));
            Parent embeddedView = loader.load();

            // ListFreelancersController controller = loader.getController();
            // controller.setClientData(currentClient);

            dashboardView.setVisible(false);
            dashboardView.setManaged(false);
            if (embeddedDashboard != null) {
                embeddedDashboard.setVisible(false);
                embeddedDashboard.setManaged(false);
            }

            contentArea.getChildren().removeIf(node -> node != dashboardView && node != embeddedDashboard);
            contentArea.getChildren().add(embeddedView);
            embeddedView.setVisible(true);
            embeddedView.setManaged(true);

            System.out.println("✓ Embedded Browse Freelancers loaded into contentArea (Freelancer)");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load browse freelancers page: " + e.getMessage());
        }
    }

    @FXML
    private void handlePayments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/freelancer/freelancer-payments.fxml"));
            Parent embeddedView = loader.load();

            dashboardView.setVisible(false);
            dashboardView.setManaged(false);
            if (embeddedDashboard != null) {
                embeddedDashboard.setVisible(false);
                embeddedDashboard.setManaged(false);
            }

            contentArea.getChildren().removeIf(node -> node != dashboardView && node != embeddedDashboard);
            contentArea.getChildren().add(embeddedView);
            embeddedView.setVisible(true);
            embeddedView.setManaged(true);

            System.out.println("✅ Payments/Revenues section loaded for freelancer");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load payments page: " + e.getMessage());
        }
    }

    @FXML
    private void handlePaymentMethods() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/profile/freelancer/freelancer-payment-methods.fxml"));
            Parent embeddedView = loader.load();

            FreelancerPaymentMethodsController controller = loader.getController();
            if (currentFreelancer != null && currentFreelancer.getIdUser() > 0) {
                controller.setUserID(currentFreelancer.getIdUser());
            }

            dashboardView.setVisible(false);
            dashboardView.setManaged(false);
            if (embeddedDashboard != null) {
                embeddedDashboard.setVisible(false);
                embeddedDashboard.setManaged(false);
            }

            contentArea.getChildren().removeIf(node -> node != dashboardView && node != embeddedDashboard);
            contentArea.getChildren().add(embeddedView);
            embeddedView.setVisible(true);
            embeddedView.setManaged(true);

            System.out.println("✅ Payment methods section loaded for freelancer");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load payment methods page: " + e.getMessage());
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

    @FXML
    private void handleMinimize(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void handleMaximize(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        if (stage.isMaximized()) {
            stage.setMaximized(false);
            FontIcon icon = new FontIcon("fas-expand-arrows-alt");
            icon.setIconSize(12);
            btnMaximize.setGraphic(icon);
        } else {
            stage.setMaximized(true);
            FontIcon icon = new FontIcon("fas-compress-arrows-alt");
            icon.setIconSize(12);
            btnMaximize.setGraphic(icon);
        }
    }

    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleMousePressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    private void handleMouseDragged(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        if (!stage.isMaximized()) {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        }
    }

    @FXML
    private void handleToggleChatbot() {
        if (chatbotWidget != null) {
            boolean isVisible = chatbotWidget.isVisible();
            chatbotWidget.setVisible(!isVisible);
            chatbotWidget.setManaged(!isVisible);
        }
    }
}
