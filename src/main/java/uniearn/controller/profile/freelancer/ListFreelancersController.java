package uniearn.controller.profile.freelancer;

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
import javafx.stage.Modality;
import javafx.stage.Stage;
import uniearn.model.entities.users.client.Client;
import uniearn.model.entities.users.freelancer.Freelancer;
import uniearn.model.enums.VerifStatus;
import uniearn.services.users.freelancer.FreelancerService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ListFreelancersController {

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> verificationFilterCombo;
    @FXML
    private ComboBox<String> ratingFilterCombo;
    @FXML
    private ComboBox<String> sortByCombo;
    @FXML
    private VBox freelancersContainer;
    @FXML
    private Label resultsCountLabel;

    private final FreelancerService freelancerService = new FreelancerService();
    private Client currentClient;
    private List<Freelancer> allFreelancers;
    private List<Freelancer> filteredFreelancers;

    @FXML
    public void initialize() {
        System.out.println("ListFreelancersController initialized");

        // Initialize filter combos with default values
        if (verificationFilterCombo != null) {
            verificationFilterCombo.setValue("All");
        }
        if (ratingFilterCombo != null) {
            ratingFilterCombo.setValue("Any");
        }
        if (sortByCombo != null) {
            sortByCombo.setValue("Rating (High)");
        }

        // Add listeners -> real-time filtering
        setupFilterListeners();
    }

    public void setClientData(Client client) {
        this.currentClient = client;
        loadFreelancers();
    }

    private void setupFilterListeners() {
        // Search field listener
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }

        // Filter combo listeners
        if (verificationFilterCombo != null) {
            verificationFilterCombo.setOnAction(e -> applyFilters());
        }
        if (ratingFilterCombo != null) {
            ratingFilterCombo.setOnAction(e -> applyFilters());
        }
        if (sortByCombo != null) {
            sortByCombo.setOnAction(e -> applyFilters());
        }
    }

    private void loadFreelancers() {
        try {
            allFreelancers = freelancerService.getAllFreelancers();
            System.out.println("✓ Loaded " + allFreelancers.size() + " freelancers");
            applyFilters();
        } catch (Exception e) {
            System.err.println("Error loading freelancers: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load freelancers: " + e.getMessage());
        }
    }

    private void applyFilters() {
        if (allFreelancers == null)
            return;

        filteredFreelancers = new ArrayList<>(allFreelancers);

        // Apply search filter
        String searchTerm = searchField.getText().toLowerCase().trim();
        if (!searchTerm.isEmpty()) {
            filteredFreelancers = filteredFreelancers.stream()
                    .filter(f -> matchesSearchTerm(f, searchTerm))
                    .collect(Collectors.toList());
        }

        // Apply verification filter
        String verificationFilter = verificationFilterCombo.getValue();
        if (!"All".equals(verificationFilter)) {
            VerifStatus targetStatus = "Verified".equals(verificationFilter)
                    ? VerifStatus.verified
                    : VerifStatus.unverified;
            filteredFreelancers = filteredFreelancers.stream()
                    .filter(f -> f.getVerificationStatus() == targetStatus)
                    .collect(Collectors.toList());
        }

        // Apply rating filter
        String ratingFilter = ratingFilterCombo.getValue();
        if (!"Any".equals(ratingFilter)) {
            double minRating = Double.parseDouble(ratingFilter.replace("+", ""));
            filteredFreelancers = filteredFreelancers.stream()
                    .filter(f -> f.getRating() >= minRating)
                    .collect(Collectors.toList());
        }

        // Apply sorting
        String sortBy = sortByCombo.getValue();
        switch (sortBy) {
            case "Rating (High)":
                filteredFreelancers.sort(Comparator.comparingDouble(Freelancer::getRating).reversed());
                break;
            case "Rating (Low)":
                filteredFreelancers.sort(Comparator.comparingDouble(Freelancer::getRating));
                break;
            case "Price (Low)":
                filteredFreelancers.sort(Comparator.comparingDouble(Freelancer::getPricePerHour));
                break;
            case "Price (High)":
                filteredFreelancers.sort(Comparator.comparingDouble(Freelancer::getPricePerHour).reversed());
                break;
            case "Name (A-Z)":
                filteredFreelancers.sort(Comparator.comparing(Freelancer::getName));
                break;
        }

        displayFreelancers();
    }

    private boolean matchesSearchTerm(Freelancer freelancer, String searchTerm) {
        // Search in name
        if (freelancer.getName().toLowerCase().contains(searchTerm)) {
            return true;
        }

        // Search in bio
        if (freelancer.getBio() != null && freelancer.getBio().toLowerCase().contains(searchTerm)) {
            return true;
        }

        // Search in skills
        if (freelancer.getSkills() != null) {
            for (String skill : freelancer.getSkills()) {
                if (skill.toLowerCase().contains(searchTerm)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void displayFreelancers() {
        freelancersContainer.getChildren().clear();

        // Update results count
        resultsCountLabel.setText(filteredFreelancers.size() + " freelancer" +
                (filteredFreelancers.size() != 1 ? "s" : "") + " found");

        if (filteredFreelancers.isEmpty()) {
            showEmptyState();
            return;
        }

        // Display each freelancer
        for (Freelancer freelancer : filteredFreelancers) {
            freelancersContainer.getChildren().add(createFreelancerCard(freelancer));
        }
    }

    private void showEmptyState() {
        VBox emptyState = new VBox(15);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setStyle("-fx-padding: 60px;");

        Label icon = new Label("🔍");
        icon.setStyle("-fx-font-size: 64px;");

        Label message = new Label("No freelancers found");
        message.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #657786;");

        Label hint = new Label("Try adjusting your search or filters");
        hint.setStyle("-fx-font-size: 14px; -fx-text-fill: #95a5a6;");

        emptyState.getChildren().addAll(icon, message, hint);
        freelancersContainer.getChildren().add(emptyState);
    }

    private HBox createFreelancerCard(Freelancer freelancer) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #e1e8ed; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-padding: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");
        card.setMaxWidth(Double.MAX_VALUE);

        // Profile Picture
        VBox avatarBox = new VBox();
        avatarBox.setAlignment(Pos.CENTER);
        ImageView avatar = new ImageView();
        avatar.setFitWidth(80);
        avatar.setFitHeight(80);
        avatar.setPreserveRatio(true);

        try {
            // load default profile picture
            avatar.setImage(new Image(getClass().getResourceAsStream("/images/default-avatar.jpg")));
        } catch (Exception e) {
            // Use placeholder
        }

        avatar.setStyle(
                "-fx-background-radius: 50%; -fx-border-radius: 50%; -fx-border-color: #e1e8ed; -fx-border-width: 2;");
        avatarBox.getChildren().add(avatar);

        // Freelancer Info
        VBox infoBox = new VBox(8);
        infoBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // Name and verification
        HBox nameRow = new HBox(10);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(freelancer.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #14171a;");
        nameRow.getChildren().add(nameLabel);

        if (freelancer.getVerificationStatus() == VerifStatus.verified) {
            Label verifiedBadge = new Label("✓ Verified");
            verifiedBadge.setStyle(
                    "-fx-background-color: #e8f5e9; " +
                            "-fx-text-fill: #2e7d32; " +
                            "-fx-padding: 3 10; " +
                            "-fx-background-radius: 12; " +
                            "-fx-font-size: 11px; " +
                            "-fx-font-weight: bold;");
            nameRow.getChildren().add(verifiedBadge);
        }

        // Email
        Label emailLabel = new Label("📧 " + freelancer.getEmail());
        emailLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #657786;");

        // Rating and Price
        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER_LEFT);

        Label ratingLabel = new Label(String.format("⭐ %.1f", freelancer.getRating()));
        ratingLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f39c12;");

        Label priceLabel = new Label(String.format("💰 %.2f TND/hr", freelancer.getPricePerHour()));
        priceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        statsRow.getChildren().addAll(ratingLabel, priceLabel);

        // Skills (show first 3)
        if (freelancer.getSkills() != null && freelancer.getSkills().length > 0) {
            HBox skillsRow = new HBox(8);
            skillsRow.setAlignment(Pos.CENTER_LEFT);

            int skillCount = Math.min(3, freelancer.getSkills().length);
            for (int i = 0; i < skillCount; i++) {
                Label skillLabel = new Label(freelancer.getSkills()[i]);
                skillLabel.setStyle(
                        "-fx-background-color: #e3f2fd; " +
                                "-fx-text-fill: #1976d2; " +
                                "-fx-padding: 4 12; " +
                                "-fx-background-radius: 12; " +
                                "-fx-font-size: 12px;");
                skillsRow.getChildren().add(skillLabel);
            }

            if (freelancer.getSkills().length > 3) {
                Label moreLabel = new Label("+" + (freelancer.getSkills().length - 3) + " more");
                moreLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6;");
                skillsRow.getChildren().add(moreLabel);
            }

            infoBox.getChildren().addAll(nameRow, emailLabel, statsRow, skillsRow);
        } else {
            infoBox.getChildren().addAll(nameRow, emailLabel, statsRow);
        }

        // Action Button
        VBox actionBox = new VBox(10);
        actionBox.setAlignment(Pos.CENTER);

        Button viewProfileBtn = new Button("View Profile");
        viewProfileBtn.setStyle(
                "-fx-background-color: #1976d2; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 20; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;");
        viewProfileBtn.setOnAction(e -> handleViewProfile(freelancer));

        actionBox.getChildren().add(viewProfileBtn);

        card.getChildren().addAll(avatarBox, infoBox, actionBox);

        return card;
    }

    private void handleViewProfile(Freelancer freelancer) {
        // Create a custom dialog to show freelancer profile (read-only)
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Freelancer Profile");
        dialog.initModality(Modality.APPLICATION_MODAL);

        DialogPane dialogPane = new DialogPane();
        dialogPane.setStyle("-fx-background-color: white;");
        dialogPane.setPrefWidth(700);
        dialogPane.setPrefHeight(600);

        // Header
        VBox header = new VBox(10);
        header.setStyle("-fx-background-color: #1976d2; -fx-padding: 20px;");
        header.setAlignment(Pos.CENTER);

        ImageView avatar = new ImageView();
        avatar.setFitWidth(100);
        avatar.setFitHeight(100);
        avatar.setPreserveRatio(true);
        try {
            avatar.setImage(new Image(getClass().getResourceAsStream("/images/default-avatar.jpg")));
        } catch (Exception e) {
            // Use placeholder
        }

        Label nameLabel = new Label(freelancer.getName());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        Label emailLabel = new Label(freelancer.getEmail());
        emailLabel.setStyle("-fx-text-fill: #e3f2fd; -fx-font-size: 14px;");

        header.getChildren().addAll(avatar, nameLabel, emailLabel);

        // Content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));

        // Stats Section
        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 20; -fx-background-radius: 10;");

        VBox ratingBox = createStatBox("⭐ Rating", String.format("%.1f", freelancer.getRating()));
        VBox priceBox = createStatBox("💰 Price/Hour", String.format("%.2f TND", freelancer.getPricePerHour()));
        VBox statusBox = createStatBox("✓ Status",
                freelancer.getVerificationStatus() == VerifStatus.verified ? "Verified" : "Unverified");

        statsBox.getChildren().addAll(ratingBox, priceBox, statusBox);

        // Bio Section
        if (freelancer.getBio() != null && !freelancer.getBio().isEmpty()) {
            VBox bioSection = new VBox(10);
            Label bioTitle = new Label("About");
            bioTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #14171a;");

            Label bioText = new Label(freelancer.getBio());
            bioText.setWrapText(true);
            bioText.setStyle("-fx-font-size: 14px; -fx-text-fill: #657786; -fx-line-spacing: 5px;");

            bioSection.getChildren().addAll(bioTitle, bioText);
            content.getChildren().add(bioSection);
        }

        // Skills Section
        if (freelancer.getSkills() != null && freelancer.getSkills().length > 0) {
            VBox skillsSection = new VBox(10);
            Label skillsTitle = new Label("Skills");
            skillsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #14171a;");

            FlowPane skillsPane = new FlowPane();
            skillsPane.setHgap(10);
            skillsPane.setVgap(10);

            for (String skill : freelancer.getSkills()) {
                Label skillLabel = new Label(skill);
                skillLabel.setStyle(
                        "-fx-background-color: #e3f2fd; " +
                                "-fx-text-fill: #1976d2; " +
                                "-fx-padding: 8 16; " +
                                "-fx-background-radius: 16; " +
                                "-fx-font-size: 13px;");
                skillsPane.getChildren().add(skillLabel);
            }

            skillsSection.getChildren().addAll(skillsTitle, skillsPane);
            content.getChildren().add(skillsSection);
        }

        content.getChildren().add(0, statsBox);
        scrollPane.setContent(content);

        // Assemble dialog
        VBox dialogContent = new VBox();
        dialogContent.getChildren().addAll(header, scrollPane);
        dialogPane.setContent(dialogContent);

        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().add(closeButton);

        dialog.setDialogPane(dialogPane);
        dialog.showAndWait();
    }

    private VBox createStatBox(String label, String value) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);

        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6;");

        Label valueText = new Label(value);
        valueText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #14171a;");

        box.getChildren().addAll(labelText, valueText);
        return box;
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        verificationFilterCombo.setValue("All");
        ratingFilterCombo.setValue("Any");
        sortByCombo.setValue("Rating (High)");
        applyFilters();
    }

    @FXML
    private void handleproject() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/client/Projet.fxml"));
            Parent root = loader.load();

            uniearn.controller.projet.ProjectController controller = loader.getController();
            controller.setClientData(currentClient);

            Stage stage = (Stage) freelancersContainer.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Mes Projets - UniEarn");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load projects page: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/client/client-profile.fxml"));
            Parent root = loader.load();

            uniearn.controller.profile.client.ClientProfileController controller = loader.getController();
            controller.setClientData(currentClient);

            Stage stage = (Stage) freelancersContainer.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.centerOnScreen();
        } catch (IOException e) {
            // e.printStackTrace();
            showErrorAlert("Error", "Failed to load profile page: " + e.getMessage());
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

            Stage stage = (Stage) freelancersContainer.getScene().getWindow();
            stage.setScene(new Scene(root, 750, 600));
            stage.setTitle("Login - UniEarn");
            stage.centerOnScreen();
        } catch (IOException e) {
            // e.printStackTrace();
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