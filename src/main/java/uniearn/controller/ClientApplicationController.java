package uniearn.controller;

import uniearn.model.entities.Application;
import uniearn.model.enums.ApplicationStatus;
import uniearn.services.ApplicationService;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;
import java.util.stream.Collectors;

public class ClientApplicationController {

    // --- FXML Fields: Sidebar ---
    @FXML
    private VBox sidebarListContainer;
    @FXML
    private TextField txtSearchTerm;
    @FXML
    private ComboBox<ApplicationStatus> cmbStatusFilter;

    // --- FXML Fields: Main Content Area ---
    @FXML
    private StackPane contentArea;
    @FXML
    private VBox viewEmpty;
    @FXML
    private VBox viewDetails;

    // --- FXML Fields: Details View ---
    @FXML
    private Label lblDetailId;
    @FXML
    private Label lblDetailDate;
    @FXML
    private Label lblDetailStatus;
    @FXML
    private Label lblDetailFreelancerId;
    @FXML
    private Label lblDetailProjectId;
    @FXML
    private Label lblDetailBudget;
    @FXML
    private Label lblDetailDuration;
    @FXML
    private Text txtDetailCoverLetter;

    // --- FXML Fields: Message Toast ---
    @FXML
    private VBox messageContainer;
    @FXML
    private Label lblMessage;

    private ApplicationService applicationService;
    private ObservableList<Application> applicationsList;
    private Application currentApplication;

    public ClientApplicationController() {
        this.applicationService = new ApplicationService();
        this.applicationsList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        // Setup Filter
        cmbStatusFilter.setItems(FXCollections.observableArrayList(ApplicationStatus.values()));
        cmbStatusFilter.setOnAction(e -> applyFilters());
        txtSearchTerm.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Initial Load
        loadClientApplications();
        showView(viewEmpty);
    }

    private void loadClientApplications() {
        try {
            // In a real app, this would be:
            // service.getApplicationsForClientProjects(clientId)
            // For now, we load ALL applications to simulate the dashboard
            List<Application> apps = applicationService.readAll();
            applicationsList.setAll(apps);
            renderSidebarList(applicationsList);
        } catch (Exception e) {
            showToast("Error loading applications", true);
            e.printStackTrace();
        }
    }

    private void renderSidebarList(List<Application> apps) {
        sidebarListContainer.getChildren().clear();

        if (apps.isEmpty()) {
            Label emptyLbl = new Label("No applications received.");
            emptyLbl.setPadding(new Insets(10));
            emptyLbl.setStyle("-fx-text-fill: #94A3B8; -fx-font-style: italic;");
            sidebarListContainer.getChildren().add(emptyLbl);
            return;
        }

        for (Application app : apps) {
            HBox card = createSidebarCard(app);
            sidebarListContainer.getChildren().add(card);
        }
    }

    private HBox createSidebarCard(Application app) {
        HBox card = new HBox(10);
        card.setPadding(new Insets(12));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0; -fx-cursor: hand; -fx-background-color: white;");

        // Status Indicator
        Circle statusDot = new Circle(4);
        statusDot.setFill(javafx.scene.paint.Color.web(getStatusColor(app.getStatus())));

        VBox content = new VBox(4);
        Label lblHeader = new Label("Project #" + app.getProjectId());
        lblHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E293B; -fx-font-size: 13px;");

        Label lblSub = new Label("Freelancer #" + app.getFreelancerId());
        lblSub.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");

        content.getChildren().addAll(lblHeader, lblSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label date = new Label("$" + (int) app.getProposedBudget());
        date.setStyle("-fx-text-fill: #10B981; -fx-font-size: 11px; -fx-font-weight: bold;");

        card.getChildren().addAll(statusDot, content, spacer, date);

        // Interaction
        card.setOnMouseClicked(e -> {
            // Highlight selected
            sidebarListContainer.getChildren().forEach(n -> n.setStyle(
                    "-fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0; -fx-cursor: hand; -fx-background-color: white;"));
            card.setStyle(
                    "-fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0; -fx-cursor: hand; -fx-background-color: #F8FAFC; -fx-border-left-color: #4F46E5; -fx-border-left-width: 3;");

            showDetails(app);
        });

        return card;
    }

    private void showView(VBox view) {
        viewEmpty.setVisible(false);
        viewDetails.setVisible(false);
        view.setVisible(true);
    }

    private void showDetails(Application app) {
        this.currentApplication = app;

        lblDetailId.setText("Application #" + app.getIdApplication());
        lblDetailDate.setText("Project #" + app.getProjectId());
        lblDetailStatus.setText(app.getStatus().name());
        lblDetailStatus.setStyle(
                "-fx-padding: 5 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-color: "
                        + getStatusBgColor(app.getStatus()) + "; -fx-text-fill: " + getStatusColor(app.getStatus())
                        + ";");

        lblDetailFreelancerId.setText("Freelancer ID: " + app.getFreelancerId());
        lblDetailProjectId.setText("Project ID: " + app.getProjectId());
        lblDetailBudget.setText(String.format("$%.2f", app.getProposedBudget()));
        lblDetailDuration.setText(app.getEstimatedDuration() + " Days");
        txtDetailCoverLetter.setText(app.getCoverLetter());

        showView(viewDetails);
    }

    // ================== ACTIONS ==================

    @FXML
    private void handleAccept() {
        if (currentApplication == null)
            return;

        if (currentApplication.getStatus() == ApplicationStatus.ACCEPTED) {
            showToast("Already Accepted", true);
            return;
        }

        confirmAction("Accept Application", "This will ACCEPT this proposal and REJECT all others for this project.",
                () -> {
                    if (applicationService.acceptApplication(currentApplication.getIdApplication())) {
                        showToast("Proposal Accepted! Others rejected.", false);
                        refreshCurrent();
                    } else {
                        showToast("Failed to accept", true);
                    }
                });
    }

    @FXML
    private void handleReject() {
        if (currentApplication == null)
            return;

        if (currentApplication.getStatus() == ApplicationStatus.REJECTED) {
            showToast("Already Rejected", true);
            return;
        }

        confirmAction("Reject Application", "Are you sure you want to reject this candidate?", () -> {
            if (applicationService.rejectApplication(currentApplication.getIdApplication())) {
                showToast("Candidate Rejected", false);
                refreshCurrent();
            } else {
                showToast("Failed to reject", true);
            }
        });
    }

    private void refreshCurrent() {
        loadClientApplications();
        if (currentApplication != null) {
            applicationsList.stream()
                    .filter(a -> a.getIdApplication() == currentApplication.getIdApplication())
                    .findFirst()
                    .ifPresent(this::showDetails);
        }
    }

    private void applyFilters() {
        String term = txtSearchTerm.getText().toLowerCase().trim();
        // Simple stripping of "project #" for cleaner search
        if (term.startsWith("project #"))
            term = term.replace("project #", "").trim();
        else if (term.startsWith("project"))
            term = term.replace("project", "").trim();

        final String finalTerm = term;
        ApplicationStatus status = cmbStatusFilter.getValue();

        List<Application> filtered = applicationsList.stream()
                .filter(app -> status == null || app.getStatus() == status)
                .filter(app -> finalTerm.isEmpty() ||
                        String.valueOf(app.getProjectId()).contains(finalTerm) ||
                        String.valueOf(app.getFreelancerId()).contains(finalTerm))
                .collect(Collectors.toList());

        renderSidebarList(filtered);
    }

    // ================== HELPERS ==================

    private void showToast(String message, boolean isError) {
        if (lblMessage == null)
            return;
        lblMessage.setText(message);
        String color = isError ? "#EF4444" : "#10B981";
        lblMessage.setStyle("-fx-background-color: #333333; -fx-text-fill: " + color
                + "; -fx-padding: 10 20; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);");
        messageContainer.setVisible(true);
        messageContainer.setManaged(true);

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            messageContainer.setVisible(false);
            messageContainer.setManaged(false);
        });
        delay.play();
    }

    private void confirmAction(String title, String content, Runnable action) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK)
                action.run();
        });
    }

    private String getStatusColor(ApplicationStatus status) {
        if (status == null)
            return "#64748B";
        return switch (status) {
            case ACCEPTED -> "#10B981";
            case REJECTED -> "#EF4444";
            case PENDING -> "#F59E0B";
            case WITHDRAWN -> "#6366F1";
        };
    }

    private String getStatusBgColor(ApplicationStatus status) {
        if (status == null)
            return "#F1F5F9";
        return switch (status) {
            case ACCEPTED -> "#ECFDF5";
            case REJECTED -> "#FEF2F2";
            case PENDING -> "#FFFBEB";
            case WITHDRAWN -> "#EEF2FF";
        };
    }
}
