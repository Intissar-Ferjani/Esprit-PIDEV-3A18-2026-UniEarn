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
import org.kordamp.ikonli.javafx.FontIcon;
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

        if (portfolioInfoTitle != null && portfolio != null) {
            portfolioInfoTitle.setText(portfolio.getTitle());
        }

        if (portfolioDescriptionLabel != null && portfolio != null) {
            String desc = portfolio.getDescription();
            portfolioDescriptionLabel.setText(desc != null && !desc.isEmpty()
                    ? desc : "Showcase your best work to attract clients");
        }

        if (portfolioCreatedDate != null && portfolio != null && portfolio.getCreated_At() != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy");
            portfolioCreatedDate.setText(sdf.format(portfolio.getCreated_At()));
        }

        loadPortfolio();
    }

    private void captureStageReference() {
        if (currentStage != null) return;
        if (backButton != null && backButton.getScene() != null)
            currentStage = (Stage) backButton.getScene().getWindow();
        else if (portfolioTitleLabel != null && portfolioTitleLabel.getScene() != null)
            currentStage = (Stage) portfolioTitleLabel.getScene().getWindow();
        else if (addPortfolioButton != null && addPortfolioButton.getScene() != null)
            currentStage = (Stage) addPortfolioButton.getScene().getWindow();
    }

    private Stage getStage() {
        if (currentStage != null) return currentStage;
        if (backButton != null && backButton.getScene() != null)
            return currentStage = (Stage) backButton.getScene().getWindow();
        if (portfolioTitleLabel != null && portfolioTitleLabel.getScene() != null)
            return currentStage = (Stage) portfolioTitleLabel.getScene().getWindow();
        if (addPortfolioButton != null && addPortfolioButton.getScene() != null)
            return currentStage = (Stage) addPortfolioButton.getScene().getWindow();
        if (portfolioItemsContainer != null && portfolioItemsContainer.getScene() != null)
            return currentStage = (Stage) portfolioItemsContainer.getScene().getWindow();
        return null;
    }

    private void loadPortfolio() {
        if (portfolioItemsContainer == null || currentPortfolio == null) return;
        portfolioItemsContainer.getChildren().clear();

        List<PortfolioItem> items = portfolioItemService
                .getPortfolioItemsByPortfolioId(currentPortfolio.getIdPortfolio());

        if (portfolioItemCount != null)
            portfolioItemCount.setText(String.valueOf(items.size()));

        if (items.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            for (PortfolioItem item : items)
                portfolioItemsContainer.getChildren().add(createPortfolioItemCard(item));
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

    // ─────────────────────────────────────────────────────────────────────────
    //  Portfolio Item Card
    // ─────────────────────────────────────────────────────────────────────────
    private VBox createPortfolioItemCard(PortfolioItem item) {

        // ── Outer card ───────────────────────────────────────────────────────
        VBox card = new VBox(0);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-radius: 14;" +
                        "-fx-border-color: #e8ecf0;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 10, 0, 0, 3);"
        );

        // ── Blue gradient accent strip ────────────────────────────────────────
        Pane accentBar = new Pane();
        accentBar.setPrefHeight(4);
        accentBar.setStyle(
                "-fx-background-color: linear-gradient(to right, #1565c0, #42a5f5);" +
                        "-fx-background-radius: 14 14 0 0;"
        );

        // ── Card body ────────────────────────────────────────────────────────
        VBox body = new VBox(16);
        body.setPadding(new Insets(22, 26, 24, 26));

        // ── Row 1: icon badge + title + action buttons ────────────────────────
        HBox headerRow = new HBox(14);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        // Blue rounded icon badge — use setIconColor() NOT setStyle() for color
        StackPane iconBadge = new StackPane();
        iconBadge.setMinSize(40, 40);
        iconBadge.setMaxSize(40, 40);
        iconBadge.setStyle("-fx-background-color: #e8f0fe; -fx-background-radius: 10;");
        FontIcon projIcon = new FontIcon("fas-code");
        projIcon.setIconSize(16);
        projIcon.setIconColor(javafx.scene.paint.Color.web("#1a73e8"));
        iconBadge.getChildren().add(projIcon);

        Label titleLbl = new Label(item.getTitle());
        titleLbl.setStyle(
                "-fx-font-size: 17px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #1a202c;"
        );
        HBox.setHgrow(titleLbl, Priority.ALWAYS);

        // Edit button — icon color via setIconColor()
        Button editBtn = new Button();
        FontIcon editIcon = new FontIcon("fas-pencil-alt");
        editIcon.setIconSize(12);
        editIcon.setIconColor(javafx.scene.paint.Color.WHITE);
        Label editLbl = new Label();
        editLbl.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-background-color: transparent; -fx-padding: 0;");
        HBox editBox = new HBox(6, editIcon, editLbl);
        editBox.setAlignment(Pos.CENTER);
        editBtn.setGraphic(editBox);
        editBtn.setStyle(
                "-fx-background-color: #1a73e8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 7 16;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-color: transparent;"
        );
        editBtn.setOnAction(e -> handleEditPortfolioItem(item));

        // Delete button
        Button deleteBtn = new Button();
        FontIcon deleteIcon = new FontIcon("fas-trash-alt");
        deleteIcon.setIconSize(13);
        deleteIcon.setIconColor(javafx.scene.paint.Color.web("#c53030"));
        deleteBtn.setGraphic(deleteIcon);
        deleteBtn.setStyle(
                "-fx-background-color: #fff5f5;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #fc8181;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-border-radius: 8;" +
                        "-fx-padding: 7 10;" +
                        "-fx-cursor: hand;"
        );
        deleteBtn.setOnAction(e -> handleDeletePortfolioItem(item));

        headerRow.getChildren().addAll(iconBadge, titleLbl, editBtn, deleteBtn);

        // ── Row 2: description ───────────────────────────────────────────────
        Label descLbl = new Label(item.getDescription());
        descLbl.setWrapText(true);
        descLbl.setStyle(
                "-fx-text-fill: #4a5568;" +
                        "-fx-font-size: 13.5px;" +
                        "-fx-line-spacing: 3px;"
        );

        body.getChildren().addAll(headerRow, descLbl);

        // ── Row 3: technologies ──────────────────────────────────────────────
        if (item.getTechnologies() != null && item.getTechnologies().length > 0) {
            VBox techBlock = new VBox(8);

            HBox techHeader = new HBox(6);
            techHeader.setAlignment(Pos.CENTER_LEFT);
            FontIcon techIcon = new FontIcon("fas-microchip");
            techIcon.setIconSize(11);
            techIcon.setIconColor(javafx.scene.paint.Color.web("#718096"));
            Label techHeading = new Label("Technologies");
            techHeading.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #718096;");
            techHeader.getChildren().addAll(techIcon, techHeading);

            FlowPane techFlow = new FlowPane();
            techFlow.setHgap(7);
            techFlow.setVgap(7);

            for (String tech : item.getTechnologies()) {
                if (tech == null || tech.trim().isEmpty()) continue;
                Label badge = new Label(tech.trim());
                badge.setStyle(
                        "-fx-background-color: #e8f0fe;" +
                                "-fx-text-fill: #1a73e8;" +
                                "-fx-padding: 4 12;" +
                                "-fx-background-radius: 20;" +
                                "-fx-font-size: 12px;" +
                                "-fx-font-weight: bold;"
                );
                techFlow.getChildren().add(badge);
            }

            techBlock.getChildren().addAll(techHeader, techFlow);
            body.getChildren().add(techBlock);
        }

        // ── Row 4: link chips ────────────────────────────────────────────────
        boolean hasProject = item.getProjectUrl() != null && !item.getProjectUrl().isEmpty();
        boolean hasGithub  = item.getGithubUrl()  != null && !item.getGithubUrl().isEmpty();

        if (hasProject || hasGithub) {
            Separator sep = new Separator();
            sep.setStyle("-fx-opacity: 0.45;");
            body.getChildren().add(sep);

            HBox linksRow = new HBox(10);
            linksRow.setAlignment(Pos.CENTER_LEFT);

            if (hasProject) {
                HBox chip = makeLinkChip("fas-external-link-alt", "View Project",
                        javafx.scene.paint.Color.web("#1a73e8"), "#e8f0fe");
                chip.setOnMouseClicked(e -> openUrl(item.getProjectUrl()));
                linksRow.getChildren().add(chip);
            }
            if (hasGithub) {
                HBox chip = makeLinkChip("fas-code-branch", "GitHub",
                        javafx.scene.paint.Color.web("#24292e"), "#f0f0f0");
                chip.setOnMouseClicked(e -> openUrl(item.getGithubUrl()));
                linksRow.getChildren().add(chip);
            }

            body.getChildren().add(linksRow);
        }

        card.getChildren().addAll(accentBar, body);
        return card;
    }

    private HBox makeLinkChip(String iconLiteral, String labelText,
                              javafx.scene.paint.Color iconColor, String bgColor) {
        HBox chip = new HBox(7);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 6 14;" +
                        "-fx-cursor: hand;"
        );

        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(13);
        icon.setIconColor(iconColor);   // ← direct Paint, not CSS string

        // Convert Paint to hex for label text-fill
        String hex = String.format("#%02x%02x%02x",
                (int)(iconColor.getRed() * 255),
                (int)(iconColor.getGreen() * 255),
                (int)(iconColor.getBlue() * 255));

        Label lbl = new Label(labelText);
        lbl.setStyle(
                "-fx-text-fill: " + hex + ";" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-color: transparent;" +
                        "-fx-padding: 0;"
        );

        chip.getChildren().addAll(icon, lbl);
        return chip;
    }

    private void openUrl(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception ex) {
            showErrorAlert("Error", "Could not open URL: " + ex.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Dialogs
    // ─────────────────────────────────────────────────────────────────────────
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

        VBox header = new VBox(5);
        header.setStyle("-fx-background-color: #1976d2; -fx-padding: 20px;");
        Label headerLabel = new Label(isEdit ? "✏ Edit Portfolio Item" : "+ Add Portfolio Item");
        headerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        header.getChildren().add(headerLabel);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 25, 25, 25));
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        TextField titleField = new TextField(isEdit ? existingItem.getTitle() : "");
        titleField.setPromptText("e.g., E-commerce Website");
        titleField.setStyle("-fx-pref-width: 400px; -fx-font-size: 13px; -fx-padding: 10;");

        TextArea descArea = new TextArea(isEdit ? existingItem.getDescription() : "");
        descArea.setPromptText("Describe your project, your role, and key achievements...");
        descArea.setWrapText(true);
        descArea.setPrefRowCount(5);
        descArea.setStyle("-fx-pref-width: 400px; -fx-font-size: 13px;");

        TextField techField = new TextField(
                isEdit && existingItem.getTechnologies() != null
                        ? String.join(", ", existingItem.getTechnologies()) : "");
        techField.setPromptText("e.g., Java, Spring Boot, React, PostgreSQL");
        techField.setStyle("-fx-pref-width: 400px; -fx-font-size: 13px; -fx-padding: 10;");

        TextField projectUrlField = new TextField(
                isEdit && existingItem.getProjectUrl() != null ? existingItem.getProjectUrl() : "");
        projectUrlField.setPromptText("https://example.com");
        projectUrlField.setStyle("-fx-pref-width: 400px; -fx-font-size: 13px; -fx-padding: 10;");

        TextField githubUrlField = new TextField(
                isEdit && existingItem.getGithubUrl() != null ? existingItem.getGithubUrl() : "");
        githubUrlField.setPromptText("https://github.com/username/project");
        githubUrlField.setStyle("-fx-pref-width: 400px; -fx-font-size: 13px; -fx-padding: 10;");

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

        VBox content = new VBox(0);
        content.getChildren().addAll(header, grid);
        dialogPane.setContent(content);

        ButtonType saveButton = new ButtonType(isEdit ? "Update" : "Add", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(saveButton, cancelButton);

        dialog.setDialogPane(dialogPane);
        Button saveBtn = (Button) dialogPane.lookupButton(saveButton);
        saveBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveButton) {
                String title       = titleField.getText().trim();
                String description = descArea.getText().trim();
                String techString  = techField.getText().trim();
                String projectUrl  = projectUrlField.getText().trim();
                String githubUrl   = githubUrlField.getText().trim();

                if (title.isEmpty() || description.isEmpty()) {
                    showErrorAlert("Validation Error", "Title and description are required.");
                    return;
                }

                String[] technologies = techString.isEmpty() ? new String[0] : techString.split(",");
                for (int i = 0; i < technologies.length; i++)
                    technologies[i] = technologies[i].trim();

                if (isEdit) {
                    existingItem.setTitle(title);
                    existingItem.setDescription(description);
                    existingItem.setTechnologies(technologies);
                    existingItem.setProjectUrl(projectUrl.isEmpty() ? null : projectUrl);
                    existingItem.setGithubUrl(githubUrl.isEmpty() ? null : githubUrl);
                    portfolioItemService.updatePortfolioItem(currentPortfolio, existingItem.getIdItem(), existingItem);
                    showSuccessAlert("Success", "Portfolio item updated successfully!");
                } else {
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

    @FXML
    private void handleEditPortfolioInfo() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Portfolio Information");
        dialog.initModality(Modality.APPLICATION_MODAL);

        DialogPane dialogPane = new DialogPane();
        dialogPane.setStyle("-fx-background-color: #f5f5f5;");

        VBox header = new VBox(5);
        header.setStyle("-fx-background-color: #1976d2; -fx-padding: 20px;");
        Label headerLabel = new Label("✏ Edit Portfolio Information");
        headerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        header.getChildren().add(headerLabel);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 25, 25, 25));
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        TextField titleField = new TextField(currentPortfolio.getTitle());
        titleField.setPromptText("Portfolio Title");
        titleField.setStyle("-fx-pref-width: 400px; -fx-font-size: 13px; -fx-padding: 10;");

        TextArea descArea = new TextArea(currentPortfolio.getDescription());
        descArea.setPromptText("Portfolio Description");
        descArea.setWrapText(true);
        descArea.setPrefRowCount(4);
        descArea.setStyle("-fx-pref-width: 400px; -fx-font-size: 13px;");

        int row = 0;
        grid.add(createFormLabel("Portfolio Title *"), 0, row);
        grid.add(titleField, 1, row++);
        grid.add(createFormLabel("Description"), 0, row);
        GridPane.setValignment(createFormLabel("Description"), VPos.TOP);
        grid.add(descArea, 1, row++);

        VBox content = new VBox(0);
        content.getChildren().addAll(header, grid);
        dialogPane.setContent(content);

        ButtonType saveButton = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(saveButton, cancelButton);

        dialog.setDialogPane(dialogPane);
        Button saveBtn = (Button) dialogPane.lookupButton(saveButton);
        saveBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveButton) {
                String title       = titleField.getText().trim();
                String description = descArea.getText().trim();

                if (title.isEmpty()) {
                    showErrorAlert("Validation Error", "Portfolio title is required.");
                    return;
                }

                currentPortfolio.setTitle(title);
                currentPortfolio.setDescription(description);
                portfolioService.updatePortfolio(currentPortfolio.getIdPortfolio(), currentPortfolio);

                if (portfolioTitleLabel != null)
                    portfolioTitleLabel.setText(currentFreelancer.getName() + "'s Portfolio");
                if (portfolioInfoTitle != null)
                    portfolioInfoTitle.setText(title);
                if (portfolioDescriptionLabel != null)
                    portfolioDescriptionLabel.setText(description != null && !description.isEmpty()
                            ? description : "Showcase your best work to attract clients");

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
                    portfolioService.deletePortfolio(currentPortfolio.getIdPortfolio());
                    showSuccessAlert("Success", "Portfolio deleted successfully!");
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

    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: 600; -fx-font-size: 13px; -fx-text-fill: #333;");
        return label;
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