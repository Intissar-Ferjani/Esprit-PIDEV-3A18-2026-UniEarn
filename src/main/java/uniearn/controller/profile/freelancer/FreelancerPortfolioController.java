package uniearn.controller.profile.freelancer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import uniearn.model.entities.users.freelancer.Freelancer;
import uniearn.model.entities.users.freelancer.Portfolio;
import uniearn.model.entities.users.freelancer.PortfolioItem;
import uniearn.services.users.freelancer.PortfolioItemService;
import uniearn.services.users.freelancer.PortfolioService;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class FreelancerPortfolioController {

    @FXML private Label portfolioTitleLabel;
    @FXML private Label portfolioDescriptionLabel;
    @FXML private Label portfolioInfoTitle;
    @FXML private Label portfolioCreatedDate;
    @FXML private Label portfolioItemCount;
    @FXML private VBox portfolioItemsContainer;
    @FXML private Button addPortfolioButton;
    @FXML private Button backButton;
    @FXML private VBox emptyStateContainer;

    private final PortfolioService portfolioService = new PortfolioService();
    private final PortfolioItemService portfolioItemService = new PortfolioItemService();
    private Freelancer currentFreelancer;
    private Portfolio currentPortfolio;

    private Stage currentStage;

    @FXML
    public void initialize() {
        System.out.println("FreelancerPortfolioController initialized");
    }

    public void setFreelancerData(Freelancer freelancer, Portfolio portfolio) {
        this.currentFreelancer = freelancer;
        this.currentPortfolio = portfolio;

        captureStageReference();

        if (portfolioTitleLabel != null) {
            portfolioTitleLabel.setText(freelancer.getName() + "'s Portfolio");
        }

        // Populate portfolio info
        if (portfolioInfoTitle != null && portfolio != null) {
            portfolioInfoTitle.setText(portfolio.getTitle());
        }

        if (portfolioDescriptionLabel != null && portfolio != null) {
            String desc = portfolio.getDescription();
            portfolioDescriptionLabel.setText(desc != null && !desc.isEmpty() ? desc : "Showcase your best work to attract clients");
        }

        if (portfolioCreatedDate != null && portfolio != null && portfolio.getCreated_At() != null) {
            // Format date
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy");
            portfolioCreatedDate.setText(sdf.format(portfolio.getCreated_At()));
        }

        loadPortfolio();
    }

    private void captureStageReference() {
        if (currentStage == null) {
            if (backButton != null && backButton.getScene() != null) {
                currentStage = (Stage) backButton.getScene().getWindow();
            } else if (portfolioTitleLabel != null && portfolioTitleLabel.getScene() != null) {
                currentStage = (Stage) portfolioTitleLabel.getScene().getWindow();
            } else if (addPortfolioButton != null && addPortfolioButton.getScene() != null) {
                currentStage = (Stage) addPortfolioButton.getScene().getWindow();
            }
        }
    }

    private Stage getStage() {
        if (currentStage != null) {
            return currentStage;
        }

        // Try to get from various UI components
        if (backButton != null && backButton.getScene() != null) {
            currentStage = (Stage) backButton.getScene().getWindow();
            return currentStage;
        }
        if (portfolioTitleLabel != null && portfolioTitleLabel.getScene() != null) {
            currentStage = (Stage) portfolioTitleLabel.getScene().getWindow();
            return currentStage;
        }
        if (addPortfolioButton != null && addPortfolioButton.getScene() != null) {
            currentStage = (Stage) addPortfolioButton.getScene().getWindow();
            return currentStage;
        }
        if (portfolioItemsContainer != null && portfolioItemsContainer.getScene() != null) {
            currentStage = (Stage) portfolioItemsContainer.getScene().getWindow();
            return currentStage;
        }

        return null;
    }

    private void loadPortfolio() {
        if (portfolioItemsContainer == null || currentPortfolio == null) return;

        portfolioItemsContainer.getChildren().clear();

        List<PortfolioItem> items = portfolioItemService.getPortfolioItemsByPortfolioId(
                currentPortfolio.getIdPortfolio()
        );

        // Update item count
        if (portfolioItemCount != null) {
            portfolioItemCount.setText(String.valueOf(items.size()));
        }

        if (items.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            for (PortfolioItem item : items) {
                portfolioItemsContainer.getChildren().add(createPortfolioItemCard(item));
            }
        }
    }

    private void showEmptyState() {
        if (emptyStateContainer != null) {
            emptyStateContainer.setVisible(true);
            emptyStateContainer.setManaged(true);
        }
    }

    private void hideEmptyState() {
        if (emptyStateContainer != null) {
            emptyStateContainer.setVisible(false);
            emptyStateContainer.setManaged(false);
        }
    }

    private VBox createPortfolioItemCard(PortfolioItem item) {
        VBox card = new VBox(15);
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #e0e0e0; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-padding: 25; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);"
        );

        // Header with title and actions
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleSection = new VBox(5);
        HBox.setHgrow(titleSection, Priority.ALWAYS);

        Label title = new Label(item.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #1a1a1a;");

        titleSection.getChildren().add(title);

        // Action buttons
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = new Button("✏ Edit");
        editBtn.setStyle(
                "-fx-background-color: #1976d2; " +
                        "-fx-text-fill: white; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 8 16; " +
                        "-fx-background-radius: 6; " +
                        "-fx-font-size: 13px;"
        );
        editBtn.setOnAction(e -> handleEditPortfolioItem(item));

        Button deleteBtn = new Button("🗑️");
        deleteBtn.setStyle(
                "-fx-background-color: #dc3545; " +
                        "-fx-text-fill: white; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 8 12; " +
                        "-fx-background-radius: 6; " +
                        "-fx-font-size: 13px;"
        );
        deleteBtn.setOnAction(e -> handleDeletePortfolioItem(item));

        actions.getChildren().addAll(editBtn, deleteBtn);
        header.getChildren().addAll(titleSection, actions);

        // Description
        Label description = new Label(item.getDescription());
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: #555; -fx-font-size: 14px; -fx-line-spacing: 4px;");

        // Technologies
        VBox techSection = new VBox(10);
        if (item.getTechnologies() != null && item.getTechnologies().length > 0) {
            Label techLabel = new Label("Technologies:");
            techLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 13px; -fx-text-fill: #333;");

            FlowPane techFlow = new FlowPane();
            techFlow.setHgap(8);
            techFlow.setVgap(8);

            for (String tech : item.getTechnologies()) {
                Label techBadge = new Label(tech);
                techBadge.setStyle(
                        "-fx-background-color: #e3f2fd; " +
                                "-fx-text-fill: #1976d2; " +
                                "-fx-padding: 6 14; " +
                                "-fx-background-radius: 16; " +
                                "-fx-font-size: 12px; " +
                                "-fx-font-weight: 500;"
                );
                techFlow.getChildren().add(techBadge);
            }

            techSection.getChildren().addAll(techLabel, techFlow);
        }

        // Links
        HBox linksBox = new HBox(15);
        linksBox.setAlignment(Pos.CENTER_LEFT);

        if (item.getProjectUrl() != null && !item.getProjectUrl().isEmpty()) {
            Hyperlink projectLink = new Hyperlink("🔗 View Project");
            projectLink.setStyle(
                    "-fx-text-fill: #1976d2; " +
                            "-fx-font-size: 13px; " +
                            "-fx-border-color: transparent; " +
                            "-fx-underline: false; " +
                            "-fx-font-weight: 500;"
            );
            projectLink.setOnAction(e -> {
                // Open URL in browser
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(item.getProjectUrl()));
                } catch (Exception ex) {
                    showErrorAlert("Error", "Could not open URL: " + ex.getMessage());
                }
            });
            linksBox.getChildren().add(projectLink);
        }

        if (item.getGithubUrl() != null && !item.getGithubUrl().isEmpty()) {
            Hyperlink githubLink = new Hyperlink("💻 GitHub");
            githubLink.setStyle(
                    "-fx-text-fill: #1976d2; " +
                            "-fx-font-size: 13px; " +
                            "-fx-border-color: transparent; " +
                            "-fx-underline: false; " +
                            "-fx-font-weight: 500;"
            );
            githubLink.setOnAction(e -> {
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(item.getGithubUrl()));
                } catch (Exception ex) {
                    showErrorAlert("Error", "Could not open URL: " + ex.getMessage());
                }
            });
            linksBox.getChildren().add(githubLink);
        }

        card.getChildren().addAll(header, description);
        if (techSection.getChildren().size() > 0) {
            card.getChildren().add(techSection);
        }
        if (linksBox.getChildren().size() > 0) {
            card.getChildren().add(linksBox);
        }

        return card;
    }

    @FXML
    private void handleAddPortfolio() {
        showPortfolioItemDialog(null);
    }

    private void handleEditPortfolioItem(PortfolioItem item) {
        showPortfolioItemDialog(item);
    }

    private void showPortfolioItemDialog(PortfolioItem existingItem) {
        boolean isEdit = (existingItem != null);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Portfolio Item" : "Add Portfolio Item");
        dialog.initModality(Modality.APPLICATION_MODAL);

        DialogPane dialogPane = new DialogPane();
        dialogPane.setStyle("-fx-background-color: #f5f5f5;");

        // Header
        VBox header = new VBox(5);
        header.setStyle("-fx-background-color: #1976d2; -fx-padding: 20px;");
        Label headerLabel = new Label(isEdit ? "✏ Edit Portfolio Item" : "+ Add Portfolio Item");
        headerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        header.getChildren().add(headerLabel);

        // Form
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 25, 25, 25));
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        // Fields
        TextField titleField = new TextField(isEdit ? existingItem.getTitle() : "");
        titleField.setPromptText("e.g., E-commerce Website");
        titleField.setStyle("-fx-pref-width: 400px; -fx-font-size: 13px; -fx-padding: 10;");

        TextArea descArea = new TextArea(isEdit ? existingItem.getDescription() : "");
        descArea.setPromptText("Describe your project, your role, and key achievements...");
        descArea.setWrapText(true);
        descArea.setPrefRowCount(5);
        descArea.setStyle("-fx-pref-width: 400px; -fx-font-size: 13px;");

        TextField techField = new TextField(
                isEdit && existingItem.getTechnologies() != null ?
                        String.join(", ", existingItem.getTechnologies()) : ""
        );
        techField.setPromptText("e.g., Java, Spring Boot, React, PostgreSQL");
        techField.setStyle("-fx-pref-width: 400px; -fx-font-size: 13px; -fx-padding: 10;");

        TextField projectUrlField = new TextField(isEdit && existingItem.getProjectUrl() != null ? existingItem.getProjectUrl() : "");
        projectUrlField.setPromptText("https://example.com");
        projectUrlField.setStyle("-fx-pref-width: 400px; -fx-font-size: 13px; -fx-padding: 10;");

        TextField githubUrlField = new TextField(isEdit && existingItem.getGithubUrl() != null ? existingItem.getGithubUrl() : "");
        githubUrlField.setPromptText("https://github.com/username/project");
        githubUrlField.setStyle("-fx-pref-width: 400px; -fx-font-size: 13px; -fx-padding: 10;");

        // Add to grid
        int row = 0;
        grid.add(createFormLabel("Project Title *"), 0, row);
        grid.add(titleField, 1, row++);

        grid.add(createFormLabel("Description *"), 0, row);
        GridPane.setValignment(createFormLabel("Description *"), VPos.TOP);
        grid.add(descArea, 1, row++);

        grid.add(createFormLabel("Technologies"), 0, row);
        grid.add(techField, 1, row++);

        grid.add(createFormLabel("Project URL"), 0, row);
        grid.add(projectUrlField, 1, row++);

        grid.add(createFormLabel("GitHub URL"), 0, row);
        grid.add(githubUrlField, 1, row++);

        // Assemble dialog
        VBox content = new VBox(0);
        content.getChildren().addAll(header, grid);
        dialogPane.setContent(content);

        // Buttons
        ButtonType saveButton = new ButtonType(isEdit ? "Update" : "Add", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(saveButton, cancelButton);

        // Style buttons
        dialog.setDialogPane(dialogPane);
        Button saveBtn = (Button) dialogPane.lookupButton(saveButton);
        saveBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");

        // Handle result
        dialog.showAndWait().ifPresent(response -> {
            if (response == saveButton) {
                String title = titleField.getText().trim();
                String description = descArea.getText().trim();
                String techString = techField.getText().trim();
                String projectUrl = projectUrlField.getText().trim();
                String githubUrl = githubUrlField.getText().trim();

                // Validation
                if (title.isEmpty() || description.isEmpty()) {
                    showErrorAlert("Validation Error", "Title and description are required.");
                    return;
                }

                // Parse technologies
                String[] technologies = techString.isEmpty() ? new String[0] : techString.split(",");
                for (int i = 0; i < technologies.length; i++) {
                    technologies[i] = technologies[i].trim();
                }

                if (isEdit) {
                    // Update existing item
                    existingItem.setTitle(title);
                    existingItem.setDescription(description);
                    existingItem.setTechnologies(technologies);
                    existingItem.setProjectUrl(projectUrl.isEmpty() ? null : projectUrl);
                    existingItem.setGithubUrl(githubUrl.isEmpty() ? null : githubUrl);

                    portfolioItemService.updatePortfolioItem(currentPortfolio, existingItem.getIdItem(), existingItem);
                    showSuccessAlert("Success", "Portfolio item updated successfully!");
                } else {
                    // Create new item
                    PortfolioItem newItem = new PortfolioItem();
                    newItem.setTitle(title);
                    newItem.setDescription(description);
                    newItem.setTechnologies(technologies);
                    newItem.setProjectUrl(projectUrl.isEmpty() ? null : projectUrl);
                    newItem.setGithubUrl(githubUrl.isEmpty() ? null : githubUrl);
                    newItem.setImagesUrl(new String[0]);
                    newItem.setCreated_At(new Timestamp(System.currentTimeMillis()));
                    newItem.setIdPortfolio(currentPortfolio.getIdPortfolio());

                    portfolioItemService.addPortfolioItem(currentPortfolio, newItem);
                    showSuccessAlert("Success", "Portfolio item added successfully!");
                }

                loadPortfolio();
            }
        });
    }

    private void handleDeletePortfolioItem(PortfolioItem item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Portfolio Item");
        confirm.setHeaderText("Delete \"" + item.getTitle() + "\"?");
        confirm.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                portfolioItemService.deletePortfolioItem(item.getIdItem());
                showSuccessAlert("Success", "Portfolio item deleted successfully!");
                loadPortfolio();
            } catch (Exception e) {
                showErrorAlert("Error", "Failed to delete portfolio item: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: 600; -fx-font-size: 13px; -fx-text-fill: #333;");
        return label;
    }

    @FXML
    private void handleEditPortfolioInfo() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Portfolio Information");
        dialog.initModality(Modality.APPLICATION_MODAL);

        DialogPane dialogPane = new DialogPane();
        dialogPane.setStyle("-fx-background-color: #f5f5f5;");

        // Header
        VBox header = new VBox(5);
        header.setStyle("-fx-background-color: #1976d2; -fx-padding: 20px;");
        Label headerLabel = new Label("✏ Edit Portfolio Information");
        headerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        header.getChildren().add(headerLabel);

        // Form
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 25, 25, 25));
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        // Fields
        TextField titleField = new TextField(currentPortfolio.getTitle());
        titleField.setPromptText("Portfolio Title");
        titleField.setStyle("-fx-pref-width: 400px; -fx-font-size: 13px; -fx-padding: 10;");

        TextArea descArea = new TextArea(currentPortfolio.getDescription());
        descArea.setPromptText("Portfolio Description");
        descArea.setWrapText(true);
        descArea.setPrefRowCount(4);
        descArea.setStyle("-fx-pref-width: 400px; -fx-font-size: 13px;");

        // Add to grid
        int row = 0;
        grid.add(createFormLabel("Portfolio Title *"), 0, row);
        grid.add(titleField, 1, row++);

        grid.add(createFormLabel("Description"), 0, row);
        GridPane.setValignment(createFormLabel("Description"), VPos.TOP);
        grid.add(descArea, 1, row++);

        // Assemble dialog
        VBox content = new VBox(0);
        content.getChildren().addAll(header, grid);
        dialogPane.setContent(content);

        // Buttons
        ButtonType saveButton = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(saveButton, cancelButton);

        dialog.setDialogPane(dialogPane);
        Button saveBtn = (Button) dialogPane.lookupButton(saveButton);
        saveBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");

        // Handle result
        dialog.showAndWait().ifPresent(response -> {
            if (response == saveButton) {
                String title = titleField.getText().trim();
                String description = descArea.getText().trim();

                if (title.isEmpty()) {
                    showErrorAlert("Validation Error", "Portfolio title is required.");
                    return;
                }

                currentPortfolio.setTitle(title);
                currentPortfolio.setDescription(description);

                portfolioService.updatePortfolio(currentPortfolio.getIdPortfolio(), currentPortfolio);

                // Update UI
                if (portfolioTitleLabel != null) {
                    portfolioTitleLabel.setText(currentFreelancer.getName() + "'s Portfolio");
                }
                if (portfolioInfoTitle != null) {
                    portfolioInfoTitle.setText(title);
                }
                if (portfolioDescriptionLabel != null) {
                    portfolioDescriptionLabel.setText(description != null && !description.isEmpty() ? description : "Showcase your best work to attract clients");
                }

                showSuccessAlert("Success", "Portfolio information updated successfully!");
            }
        });
    }

    @FXML
    private void handleDeletePortfolio() {
        Stage stage = getStage();
        if (stage == null) {
            showErrorAlert("Error", "Unable to determine window. Please try again.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.WARNING);
        confirm.setTitle("Delete Portfolio");
        confirm.setHeaderText("⚠ Delete Entire Portfolio?");
        confirm.setContentText("This will permanently delete your portfolio and ALL portfolio items.\n\nThis action CANNOT be undone!\n\nType 'DELETE' to confirm:");

        TextField confirmField = new TextField();
        confirmField.setPromptText("Type DELETE");

        VBox content = new VBox(10, new Label(confirm.getContentText()), confirmField);
        content.setPadding(new Insets(10));
        confirm.getDialogPane().setContent(content);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && "DELETE".equals(confirmField.getText())) {
                try {
                    // Delete portfolio (cascade will handle portfolio items automatically)
                    portfolioService.deletePortfolio(currentPortfolio.getIdPortfolio());

                    showSuccessAlert("Success", "Portfolio deleted successfully!");

                    // Set current portfolio to null
                    currentPortfolio = null;

                    navigateBackToProfile(stage);

                } catch (Exception e) {
                    showErrorAlert("Error", "Failed to delete portfolio: " + e.getMessage());
                    e.printStackTrace();
                }
            } else if (response == ButtonType.OK) {
                showErrorAlert("Confirmation Failed", "You must type 'DELETE' to confirm.");
            }
        });
    }

    @FXML
    private void handleBack() {
        Stage stage = getStage();
        if (stage == null) {
            showErrorAlert("Navigation Error", "Unable to navigate back. Please close and reopen the application.");
            return;
        }
        navigateBackToProfile(stage);
    }

    private void navigateBackToProfile(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/freelancer/freelancer-profile.fxml"));
            Parent root = loader.load();

            FreelancerProfileController controller = loader.getController();
            controller.setFreelancerData(currentFreelancer);

            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("My Profile - UniEarn");

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to go back: " + e.getMessage());
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