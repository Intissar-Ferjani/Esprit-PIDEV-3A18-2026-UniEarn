package uniearn.controller.profile.freelancer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import uniearn.controller.profile.freelancer.FreelancerPortfolioController;
import uniearn.controller.projet.TaskBoardController;
import uniearn.model.entities.users.User;
import uniearn.model.entities.users.freelancer.Freelancer;
import uniearn.model.entities.users.freelancer.Portfolio;
import uniearn.services.users.freelancer.FreelancerService;
import uniearn.services.users.freelancer.PortfolioService;
import uniearn.services.users.UserService;
import uniearn.database.SessionManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

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
    private HBox skillsContainer;
    @FXML
    private Label bioLabel;
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
            ratingLabel.setText(String.format("⭐ %.1f", currentFreelancer.getRating()));
        }

        if (verificationLabel != null) {
            String verificationText = switch (currentFreelancer.getVerificationStatus()) {
                case verified -> "✓ Verified";
                case unverified -> "✗ Not Verified";
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

    private void loadProfilePicture() {
        try {
            User user = userService.getUserById(currentFreelancer.getIdUser());

            if (user != null && user.getProfilePicturePath() != null && !user.getProfilePicturePath().isEmpty()) {
                File imageFile = new File(user.getProfilePicturePath());

                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    profileImageView.setImage(image);
                    System.out.println("✓ Loaded profile picture: " + user.getProfilePicturePath());
                } else {
                    System.out.println("⚠ Profile picture file not found: " + user.getProfilePicturePath());
                }
            } else {
                System.out.println("Using default avatar - no profile picture set");
            }
        } catch (Exception e) {
            System.out.println("Error loading profile picture: " + e.getMessage());
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

    /**
     * Check if portfolio exists and update UI accordingly
     */
    private void checkPortfolioStatus() {
        List<Portfolio> portfolios = portfolioService.getAllPortfolios();
        currentPortfolio = portfolios.stream()
                .filter(p -> p.getFreelancerId() == currentFreelancer.getIdFreelancer())
                .findFirst()
                .orElse(null);

        updatePortfolioUI();
    }

    // Update portfolio UI based on portfolio existence
    private void updatePortfolioUI() {
        if (portfolioSection == null) {
            System.out.println("⚠ Portfolio section not found in FXML");
            return;
        }

        // Clear existing content
        portfolioSection.getChildren().clear();

        HBox portfolioHeader = new HBox(15);
        portfolioHeader.setAlignment(javafx.geometry.Pos.CENTER);
        VBox.setMargin(portfolioHeader, new Insets(0));

        if (currentPortfolio == null) {
            System.out.println("⚠ No portfolio found - showing Create button");

            // No portfolio - show create option
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

            // Portfolio exists - show view option
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

            // Update UI
            updatePortfolioUI();

            System.out.println("✓ Portfolio created for freelancer ID: " + currentFreelancer.getIdFreelancer());

        } catch (Exception e) {
            showErrorAlert("Error", "Failed to create portfolio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewPortfolio() {
        // Check if portfolio still exists before navigating
        if (currentPortfolio == null) {
            showErrorAlert("Error", "No portfolio found. Please create a portfolio first.");
            checkPortfolioStatus(); // Refresh UI state
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/freelancer/freelancer-portfolio.fxml"));
            Parent root = loader.load();

            FreelancerPortfolioController controller = loader.getController();
            controller.setFreelancerData(currentFreelancer, currentPortfolio);

            Stage stage = (Stage) nameLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("My Portfolio - UniEarn");

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

                Image image = new Image(destination.toUri().toString());
                profileImageView.setImage(image);

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
        Label headerLabel = new Label("✏ Edit Profile");
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
                    showSuccessAlert("Success", "Profile updated successfully!");
                } catch (NumberFormatException ex) {
                    showErrorAlert("Invalid Input", "Please enter a valid price per hour.");
                } catch (Exception ex) {
                    showErrorAlert("Update Failed", "Failed to update profile: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
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

                    // Clear session on deactivation
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
    private void handleFreelancerProjects() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/freelancer/freelancer-projects.fxml"));
            Parent root = loader.load();
            uniearn.controller.projet.FreelancerProjectsController controller = loader.getController();
            controller.setFreelancerData(currentFreelancer);

            Stage stage = (Stage) nameLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Projets Disponibles - UniEarn");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load projects: " + e.getMessage());
        }
    }

    @FXML
    private void handleTaskBoard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/client/TaskBoard.fxml"));
            Parent root = loader.load();
            TaskBoardController controller = loader.getController();
            controller.setFreelancerData(currentFreelancer);

            Stage stage = (Stage) nameLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Mes Tâches - TaskBoard");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load task board: " + e.getMessage());
        }
    }

    @FXML
    private void handleSettings() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Settings");
        alert.setHeaderText("Account Settings");
        alert.setContentText(
                "Settings page coming soon!\n\nFeatures:\n• Change password\n• Notification preferences\n• Privacy settings\n• Language selection");
        alert.showAndWait();
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