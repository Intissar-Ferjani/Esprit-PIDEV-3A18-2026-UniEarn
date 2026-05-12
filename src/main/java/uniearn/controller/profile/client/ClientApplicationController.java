package uniearn.controller.profile.client;

import uniearn.model.entities.candidature.application.Application;
import uniearn.model.enums.ApplicationStatus;
import uniearn.services.candidature.ApplicationService;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ClientApplicationController {

    // --- FXML Fields: Main Layout ---
    @FXML
    private VBox viewListRoot;
    @FXML
    private ScrollPane scrollDetails;
    @FXML
    private VBox viewDetails;

    // --- FXML Fields: Sidebar / Search & Filter ---
    @FXML
    private FlowPane sidebarListContainer;
    @FXML
    private TextField txtSearchTerm;
    @FXML
    private ComboBox<ApplicationStatus> cmbStatusFilter;

    // --- FXML Fields: Stats Bar ---
    @FXML
    private Label lblTotalApps;
    @FXML
    private Label lblAcceptedApps;
    @FXML
    private Label lblPendingApps;
    @FXML
    private Label lblRejectedApps;

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
    @FXML
    private Button btnGiveFeedback;

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
        showView(viewListRoot);
    }

    private void loadClientApplications() {
        try {
            // In a real app, we would load applications for the logged-in client's projects
            List<Application> apps = applicationService.readAll();
            applicationsList.setAll(apps);
            renderSidebarList(applicationsList);
            updateStats();
        } catch (SQLException e) {
            showToast("Error loading applications", true);
            e.printStackTrace();
        }
    }

    private void updateStats() {
        long total = applicationsList.size();
        long accepted = applicationsList.stream().filter(a -> a.getStatus() == ApplicationStatus.ACCEPTED).count();
        long pending = applicationsList.stream().filter(a -> a.getStatus() == ApplicationStatus.PENDING).count();
        long rejected = applicationsList.stream().filter(a -> a.getStatus() == ApplicationStatus.REJECTED).count();

        lblTotalApps.setText(String.valueOf(total));
        lblAcceptedApps.setText(String.valueOf(accepted));
        lblPendingApps.setText(String.valueOf(pending));
        lblRejectedApps.setText(String.valueOf(rejected));
    }

    private void renderSidebarList(List<Application> apps) {
        sidebarListContainer.getChildren().clear();

        if (apps.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50));
            Label emptyLbl = new Label("Aucune candidature reçue.");
            emptyLbl.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 16px; -fx-font-style: italic;");
            emptyBox.getChildren().add(emptyLbl);
            sidebarListContainer.getChildren().add(emptyBox);
            return;
        }

        for (Application app : apps) {
            VBox card = createApplicationCard(app);
            sidebarListContainer.getChildren().add(card);
        }
    }

    private VBox createApplicationCard(Application app) {
        VBox card = new VBox(12);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");

        // Status Tag
        Label statusTag = new Label(app.getStatus().name());
        statusTag
                .setStyle("-fx-padding: 3 10; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold; " +
                        "-fx-background-color: " + getStatusBgColor(app.getStatus()) + "; -fx-text-fill: "
                        + getStatusColor(app.getStatus()) + ";");

        // Header
        String projectTitle = applicationService.getProjectTitle(app.getProjectId());
        Label lblProject = new Label(projectTitle);
        lblProject.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 15px;");

        String freelancerName = applicationService.getFreelancerName(app.getFreelancerId());
        Label lblFreelancer = new Label(freelancerName);
        lblFreelancer.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        // Budget info
        HBox budgetBox = new HBox(5);
        budgetBox.setAlignment(Pos.CENTER_LEFT);
        Label budgetVal = new Label(String.format("%.2f DT", app.getProposedBudget()));
        budgetVal.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 14px;");

        card.getChildren().addAll(statusTag, lblProject, lblFreelancer, budgetBox);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        card.getChildren().add(spacer);

        if (app.getStatus() == ApplicationStatus.PENDING) {
            HBox actions = new HBox(10);
            actions.setAlignment(Pos.CENTER);
            Button btnAcc = createQuickBtn("✓", "#10b981");
            btnAcc.setOnAction(e -> {
                currentApplication = app;
                handleAccept();
                e.consume();
            });
            Button btnRej = createQuickBtn("✕", "#ef4444");
            btnRej.setOnAction(e -> {
                currentApplication = app;
                handleReject();
                e.consume();
            });
            Button btnDel = createQuickBtn("🗑", "#64748b");
            btnDel.setOnAction(e -> {
                currentApplication = app;
                handleDelete();
                e.consume();
            });
            actions.getChildren().addAll(btnAcc, btnRej, btnDel);
            card.getChildren().add(actions);
        }

        // Interaction
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);"));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"));
        card.setOnMouseClicked(e -> showDetails(app));

        return card;
    }

    private Button createQuickBtn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + color + "11; -fx-text-fill: " + color
                + "; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 12; -fx-cursor: hand; -fx-border-color: "
                + color + "33; -fx-border-radius: 6;");
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color: " + color
                + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 12; -fx-cursor: hand;"));
        b.setOnMouseExited(e -> b.setStyle("-fx-background-color: " + color + "11; -fx-text-fill: " + color
                + "; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 12; -fx-cursor: hand; -fx-border-color: "
                + color + "33; -fx-border-radius: 6;"));
        return b;
    }

    private void showView(javafx.scene.Node view) {
        viewListRoot.setVisible(false);
        viewListRoot.setManaged(false);
        scrollDetails.setVisible(false);
        scrollDetails.setManaged(false);

        view.setVisible(true);
        view.setManaged(true);
    }

    private void showDetails(Application app) {
        this.currentApplication = app;

        lblDetailId.setText("Candidature #" + app.getIdApplication());
        String detailProjectTitle = applicationService.getProjectTitle(app.getProjectId());
        lblDetailDate.setText(detailProjectTitle);
        lblDetailStatus.setText(app.getStatus().name());
        lblDetailStatus
                .setStyle("-fx-padding: 5 14; -fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 11px; " +
                        "-fx-background-color: " + getStatusBgColor(app.getStatus()) + "; -fx-text-fill: "
                        + getStatusColor(app.getStatus()) + ";");

        String detailFreelancerName = applicationService.getFreelancerName(app.getFreelancerId());
        lblDetailFreelancerId.setText(detailFreelancerName);
        lblDetailProjectId.setText(detailProjectTitle);
        lblDetailBudget.setText(String.format("%.2f DT", app.getProposedBudget()));
        lblDetailDuration.setText(app.getEstimatedDuration() + " Jours");
        txtDetailCoverLetter.setText(app.getCoverLetter());

        // Show feedback button only for accepted applications
        btnGiveFeedback.setVisible(app.getStatus() == ApplicationStatus.ACCEPTED);
        btnGiveFeedback.setManaged(app.getStatus() == ApplicationStatus.ACCEPTED);

        showView(scrollDetails);
    }

    // ================== ACTIONS ==================

    @FXML
    private void handleAccept() {
        if (currentApplication == null)
            return;

        if (currentApplication.getStatus() == ApplicationStatus.ACCEPTED) {
            showToast("Déjà acceptée", true);
            return;
        }

        confirmAction("Accepter la Candidature",
                "Voulez-vous accepter cette proposition et refuser les autres pour ce projet ?", () -> {
                    if (applicationService.acceptApplication(currentApplication.getIdApplication())) {
                        showToast("Candidature acceptée !", false);
                        refreshCurrent();
                    } else {
                        showToast("Erreur lors de l'acceptation", true);
                    }
                });
    }

    @FXML
    private void handleReject() {
        if (currentApplication == null)
            return;

        if (currentApplication.getStatus() == ApplicationStatus.REJECTED) {
            showToast("Déjà refusée", true);
            return;
        }

        confirmAction("Refuser la Candidature", "Êtes-vous sûr de vouloir refuser ce candidat ?", () -> {
            if (applicationService.rejectApplication(currentApplication.getIdApplication())) {
                showToast("Candidature refusée", false);
                refreshCurrent();
            } else {
                showToast("Erreur lors du refus", true);
            }
        });
    }

    @FXML
    private void handleDelete() {
        if (currentApplication == null)
            return;

        confirmAction("Supprimer la Candidature", "Cette action est irréversible. Continuer ?", () -> {
            try {
                applicationService.delete(currentApplication.getIdApplication());
                showToast("Candidature supprimée", false);
                loadClientApplications();
                showView(viewListRoot);
            } catch (SQLException e) {
                showToast("Erreur lors de la suppression", true);
            }
        });
    }

    @FXML
    private void handleGiveFeedback() {
        if (currentApplication == null)
            return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/ClientDashboardView.fxml"));
            Parent root = loader.load();
            ClientDashboardController controller = loader.getController();

            Stage stage = (Stage) viewListRoot.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);

            controller.handleShowReviews();
            controller.handleAddReview();
            // Pre-fill if possible? Need exposure in ClientDashboardController
        } catch (Exception e) {
            showToast("Navigation failed: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleWithdraw() {
        if (currentApplication == null)
            return;

        confirmAction("Retirer la Candidature", "Voulez-vous vraiment retirer/annuler cette candidature?", () -> {
            if (applicationService.withdrawApplication(currentApplication.getIdApplication(),
                    currentApplication.getFreelancerId())) {
                showToast("Candidature retirée", false);
                loadClientApplications();
                showView(viewListRoot);
            } else {
                showToast("Échec du retrait", true);
            }
        });
    }

    @FXML
    private void handleMessageFreelancer() {
        if (currentApplication == null)
            return;
        String freelancerName = applicationService.getFreelancerName(currentApplication.getFreelancerId());
        showToast("Ouverture de la messagerie avec " + freelancerName, false);
    }

    @FXML
    private void handleRefresh() {
        loadClientApplications();
        showToast("Données actualisées", false);
    }

    @FXML
    private void handleCancelForm() {
        showView(viewListRoot);
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
        ApplicationStatus status = cmbStatusFilter.getValue();

        List<Application> filtered = applicationsList.stream()
                .filter(app -> status == null || app.getStatus() == status)
                .filter(app -> term.isEmpty() ||
                        applicationService.getProjectTitle(app.getProjectId()).toLowerCase().contains(term) ||
                        applicationService.getFreelancerName(app.getFreelancerId()).toLowerCase().contains(term) ||
                        app.getCoverLetter().toLowerCase().contains(term))
                .collect(Collectors.toList());

        renderSidebarList(filtered);
    }

    // ================== HELPERS ==================

    private void showToast(String message, boolean isError) {
        if (lblMessage == null)
            return;
        lblMessage.setText(message);
        String color = isError ? "#EF4444" : "#10B981";
        lblMessage.setStyle("-fx-background-color: #1e293b; -fx-text-fill: " + color
                + "; -fx-padding: 12 24; -fx-background-radius: 20;");

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
        alert.setHeaderText(null);
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
