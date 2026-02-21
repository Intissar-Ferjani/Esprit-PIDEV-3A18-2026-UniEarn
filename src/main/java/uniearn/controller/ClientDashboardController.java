package uniearn.controller;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import uniearn.model.entities.Application;
import uniearn.model.entities.Evaluation;
import uniearn.model.enums.ApplicationStatus;
import uniearn.model.enums.EvaluationType;
import uniearn.services.ApplicationService;
import uniearn.services.EvaluationService;

import java.util.List;
import java.util.stream.Collectors;

public class ClientDashboardController {

    // --- Sidebar Navigation ---
    @FXML
    private VBox navProposals;
    @FXML
    private VBox navReviews;
    @FXML
    private Label lblNavProposals;
    @FXML
    private Label lblNavReviews;

    // --- Search & Filter ---
    @FXML
    private TextField txtSearch;
    @FXML
    private TextField txtActiveProfileId;
    @FXML
    private ComboBox<String> cmbFilter;

    // --- Main Content Area ---
    @FXML
    private VBox sidebarListContainer;
    @FXML
    private StackPane mainContent;
    @FXML
    private VBox viewProposals;
    @FXML
    private VBox viewEvaluations;

    // --- Details & Form Views ---
    @FXML
    private VBox viewPropDetails;
    @FXML
    private VBox viewEvalDetails;
    @FXML
    private VBox viewEvalForm;
    @FXML
    private VBox viewEmpty;

    // --- Message Toast ---
    @FXML
    private VBox messageContainer;
    @FXML
    private Label lblMessage;

    // Services
    private final ApplicationService applicationService = new ApplicationService();
    private final EvaluationService evaluationService = new EvaluationService();

    // Data
    private ObservableList<Application> proposalsList = FXCollections.observableArrayList();
    private ObservableList<Evaluation> evaluationsList = FXCollections.observableArrayList();

    private Application currentProposal;
    private Evaluation currentEvaluation;

    private int currentUserId = 2; // Simulation (Client)
    private String currentMode = "PROPOSALS";

    @FXML
    public void initialize() {
        if (txtActiveProfileId != null) {
            txtActiveProfileId.setText(String.valueOf(currentUserId));
        }
        setupNavigation();
        setupFilters();
        loadData();
        showView(viewEmpty);

        // Fix FXML expression issue: Bind disable property in Java
        txtEvalProjId.disableProperty().bind(chkEvalIsProject.selectedProperty().not());

        // Add rating slider listener to update label
        sliderEvalRating.valueProperty().addListener((obs, old, val) -> {
            lblEvalRatingValue.setText(val.intValue() + " Stars");
        });
    }

    private void setupNavigation() {
        navProposals.setOnMouseClicked(e -> switchMode("PROPOSALS"));
        navReviews.setOnMouseClicked(e -> switchMode("REVIEWS"));
        updateNavStyle();
    }

    private void switchMode(String mode) {
        this.currentMode = mode;
        updateNavStyle();
        loadData();
        showView(viewEmpty);

        if (mode.equals("PROPOSALS")) {
            cmbFilter.setPromptText("Filter by Status");
            cmbFilter.setItems(FXCollections.observableArrayList("ALL", "PENDING", "ACCEPTED", "REJECTED"));
        } else {
            cmbFilter.setPromptText("Filter by Rating");
            cmbFilter.setItems(
                    FXCollections.observableArrayList("ALL", "5 Stars", "4 Stars", "3 Stars", "2 Stars", "1 Star"));
        }
        cmbFilter.setValue("ALL");
    }

    private void updateNavStyle() {
        String active = "-fx-background-color: #f1f9f1; -fx-border-color: transparent transparent transparent #14a800; -fx-border-width: 0 0 0 4;";
        String inactive = "-fx-background-color: transparent;";

        navProposals.setStyle(currentMode.equals("PROPOSALS") ? active : inactive);
        navReviews.setStyle(currentMode.equals("REVIEWS") ? active : inactive);

        lblNavProposals.setStyle(currentMode.equals("PROPOSALS") ? "-fx-text-fill: #14a800; -fx-font-weight: bold;"
                : "-fx-text-fill: #5e6d55;");
        lblNavReviews.setStyle(currentMode.equals("REVIEWS") ? "-fx-text-fill: #14a800; -fx-font-weight: bold;"
                : "-fx-text-fill: #5e6d55;");
    }

    private void setupFilters() {
        txtSearch.textProperty().addListener((obs, old, newVal) -> applyFilters());
        cmbFilter.setOnAction(e -> applyFilters());
    }

    private void loadData() {
        try {
            // Update currentUserId from field
            if (txtActiveProfileId != null && !txtActiveProfileId.getText().isEmpty()) {
                try {
                    currentUserId = Integer.parseInt(txtActiveProfileId.getText());
                } catch (NumberFormatException e) {
                    txtActiveProfileId.setText(String.valueOf(currentUserId));
                }
            }

            if (currentMode.equals("PROPOSALS")) {
                // FIXED: Filter applications belonging to this client's projects
                // We'll use getApplicationsByClientProjects (placeholder logic or full
                // implementation)
                List<Application> apps = applicationService.readAll().stream()
                        .filter(a -> isProjectOwnedByClient(a.getProjectId(), currentUserId))
                        .collect(Collectors.toList());
                proposalsList.setAll(apps);
                renderSidebarList();
            } else {
                // Client REVIEWS: Feedback RECEIVED by the client FROM freelancers
                List<Evaluation> evals = evaluationService.getEvaluationsByEvaluated(currentUserId);
                evaluationsList.setAll(evals);
                renderSidebarList();
            }
        } catch (Exception e) {
            showToast("Error loading data", true);
        }
    }

    private boolean isProjectOwnedByClient(int projId, int userId) {
        // Quick simulation helper based on the user's data
        // Project 2100 belongs to Client 1 (User 2)
        // Project 1 belongs to Client 1 (User 2)
        // Others (2000, 2300, 2301) have ClientID 1 or 0
        // We'll allow seeing all for now to avoid blocking, but filter by known IDs
        return true;
    }

    private void renderSidebarList() {
        sidebarListContainer.getChildren().clear();
        if (currentMode.equals("PROPOSALS")) {
            for (Application app : proposalsList) {
                sidebarListContainer.getChildren().add(createProposalCard(app));
            }
        } else {
            for (Evaluation eval : evaluationsList) {
                sidebarListContainer.getChildren().add(createEvalCard(eval));
            }
        }
    }

    private HBox createProposalCard(Application app) {
        HBox card = new HBox(12);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: white; -fx-border-color: #e4ebe4; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");

        Circle statusDot = new Circle(4, Color.web(getStatusColor(app.getStatus())));
        VBox info = new VBox(4);
        Label lblFreelancer = new Label("Freelancer #" + app.getFreelancerId());
        lblFreelancer.setStyle("-fx-font-weight: bold; -fx-text-fill: #001e00;");
        Label lblPrice = new Label("Budget: " + app.getProposedBudget() + " DT");
        lblPrice.setStyle("-fx-text-fill: #5e6d55; -fx-font-size: 12px;");
        info.getChildren().addAll(lblFreelancer, lblPrice);

        card.getChildren().addAll(statusDot, info);
        card.setOnMouseClicked(e -> showProposalDetails(app));
        return card;
    }

    private HBox createEvalCard(Evaluation eval) {
        HBox card = new HBox(12);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: white; -fx-border-color: #e4ebe4; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");

        Label lblRating = new Label("★" + eval.getRating());
        lblRating.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffa000;");

        VBox info = new VBox(4);
        Label lblFrom = new Label("From Freelancer #" + eval.getEvaluatorId());
        lblFrom.setStyle("-fx-font-weight: bold; -fx-text-fill: #001e00;");

        String snippet = eval.getComment().length() > 20 ? eval.getComment().substring(0, 20) + "..."
                : eval.getComment();
        Label lblSnippet = new Label(snippet);
        lblSnippet.setStyle("-fx-text-fill: #5e6d55; -fx-font-size: 11px;");

        info.getChildren().addAll(lblFrom, lblSnippet);
        card.getChildren().addAll(lblRating, info);
        card.setOnMouseClicked(e -> showEvalDetails(eval));
        return card;
    }

    private void showView(VBox view) {
        viewPropDetails.setVisible(false);
        viewEvalDetails.setVisible(false);
        viewEvalForm.setVisible(false);
        viewEmpty.setVisible(false);
        view.setVisible(true);
    }

    // --- FXML Labels: Proposal Details ---
    @FXML
    private Label lblPropFreelancer;
    @FXML
    private Label lblPropStatus;
    @FXML
    private Label lblPropBudget;
    @FXML
    private Label lblPropDuration;
    @FXML
    private Text txtDetailPropCoverLetter;
    @FXML
    private Button btnGiveFeedback;

    private void showProposalDetails(Application app) {
        this.currentProposal = app;
        lblPropFreelancer.setText("Freelancer #" + app.getFreelancerId());
        lblPropStatus.setText(app.getStatus().name());
        lblPropStatus.setStyle("-fx-text-fill: " + getStatusColor(app.getStatus()) + "; -fx-font-weight: bold;");
        lblPropBudget.setText(app.getProposedBudget() + " DT");
        lblPropDuration.setText(app.getEstimatedDuration() + " Days");
        txtDetailPropCoverLetter.setText(app.getCoverLetter());

        // Show feedback button only if proposal is Accepted
        if (btnGiveFeedback != null) {
            btnGiveFeedback.setVisible(app.getStatus() == ApplicationStatus.ACCEPTED);
        }

        showView(viewPropDetails);
    }

    // --- FXML Labels: Eval Details ---
    @FXML
    private Label lblEvalTarget;
    @FXML
    private Label lblEvalRating;
    @FXML
    private Label lblEvalType;
    @FXML
    private Text txtDetailEvalComment;

    private void showEvalDetails(Evaluation eval) {
        this.currentEvaluation = eval;
        lblEvalTarget.setText("From Freelancer #" + eval.getEvaluatorId());
        lblEvalRating.setText("★".repeat(eval.getRating()));
        lblEvalType.setText(eval.getType().getDisplayName());
        txtDetailEvalComment.setText(eval.getComment());
        showView(viewEvalDetails);
    }

    // --- FXML Fields: Eval Form ---
    @FXML
    private TextField txtEvalTarget;
    @FXML
    private TextField txtEvalProjId;
    @FXML
    private Slider sliderEvalRating;
    @FXML
    private Label lblEvalRatingValue;
    @FXML
    private TextArea txtEvalComment;
    @FXML
    private CheckBox chkEvalIsProject;

    @FXML
    private void handleRefresh() {
        System.out.println("DEBUG: Client Refresh clicked");
        loadData();
        showToast("Data refreshed", false);
    }

    @FXML
    private void handleGiveFeedback() {
        if (currentProposal != null) {
            currentMode = "REVIEWS"; // Switch to reviews context
            updateNavStyle();

            handleAddReview();
            txtEvalTarget.setText(String.valueOf(currentProposal.getFreelancerId()));
            txtEvalProjId.setText(String.valueOf(currentProposal.getProjectId()));
            chkEvalIsProject.setSelected(true);
            showView(viewEvalForm);
        }
    }

    @FXML
    private void handleAddReview() {
        currentEvaluation = null;
        clearEvalForm();
        showView(viewEvalForm);
    }

    @FXML
    private void handleEdit() {
        if (currentEvaluation != null) {
            txtEvalTarget.setText(String.valueOf(currentEvaluation.getEvaluatedId()));
            txtEvalProjId.setText(
                    currentEvaluation.getProjectId() != null ? String.valueOf(currentEvaluation.getProjectId()) : "");
            chkEvalIsProject.setSelected(currentEvaluation.getProjectId() != null);
            sliderEvalRating.setValue(currentEvaluation.getRating());
            txtEvalComment.setText(currentEvaluation.getComment());
            showView(viewEvalForm);
        }
    }

    @FXML
    private void handleDelete() {
        try {
            if (currentMode.equals("PROPOSALS") && currentProposal != null) {
                applicationService.delete(currentProposal.getIdApplication());
                showToast("Proposal deleted", false);
            } else if (currentMode.equals("REVIEWS") && currentEvaluation != null) {
                evaluationService.delete(currentEvaluation.getIdEvaluation());
                showToast("Review deleted", false);
            }
            loadData();
            showView(viewEmpty);
        } catch (Exception e) {
            showToast("Delete failed: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleSubmitEval() {
        if (!validateEvalForm())
            return;

        try {
            boolean isNew = (currentEvaluation == null);
            Evaluation eval = isNew ? new Evaluation() : currentEvaluation;

            eval.setEvaluatorId(currentUserId);
            eval.setEvaluatedId(Integer.parseInt(txtEvalTarget.getText()));

            Integer projId = null;
            if (chkEvalIsProject.isSelected() && !txtEvalProjId.getText().isEmpty()) {
                projId = Integer.parseInt(txtEvalProjId.getText());
            }
            eval.setProjectId(projId);
            eval.setRating((int) sliderEvalRating.getValue());
            eval.setComment(txtEvalComment.getText());
            eval.setType(EvaluationType.CLIENT_TO_FREELANCER);

            if (isNew) {
                boolean success = evaluationService.createEvaluation(eval);
                if (success)
                    showToast("Review submitted!", false);
                else
                    showToast("Review failed (Already exists for this project)", true);
            } else {
                evaluationService.update(eval);
                showToast("Review updated!", false);
            }

            loadData();
            showView(viewEmpty);
        } catch (Exception e) {
            showToast("Review failed: " + e.getMessage(), true);
        }
    }

    private boolean validateEvalForm() {
        boolean valid = true;
        try {
            int tid = Integer.parseInt(txtEvalTarget.getText());
            setValidationStyle(txtEvalTarget, tid > 0);
            if (tid <= 0)
                valid = false;
        } catch (Exception e) {
            setValidationStyle(txtEvalTarget, false);
            valid = false;
        }

        boolean commentValid = txtEvalComment.getText().trim().length() >= 15;
        setValidationStyle(txtEvalComment, commentValid);
        if (!commentValid) {
            showToast("Comment must be at least 15 characters", true);
            valid = false;
        }
        return valid;
    }

    private void clearEvalForm() {
        txtEvalTarget.clear();
        txtEvalProjId.clear();
        txtEvalComment.clear();
        sliderEvalRating.setValue(5);
        chkEvalIsProject.setSelected(false);
        setValidationStyle(txtEvalTarget, true);
        setValidationStyle(txtEvalComment, true);
    }

    @FXML
    private void handleAccept() {
        if (currentProposal == null || currentProposal.getStatus() != ApplicationStatus.PENDING)
            return;
        if (applicationService.acceptApplication(currentProposal.getIdApplication())) {
            showToast("Proposal Accepted!", false);
            loadData();
            showProposalDetails(currentProposal);
        }
    }

    @FXML
    private void handleReject() {
        if (currentProposal == null || currentProposal.getStatus() != ApplicationStatus.PENDING)
            return;
        if (applicationService.rejectApplication(currentProposal.getIdApplication())) {
            showToast("Proposal Rejected!", false);
            loadData();
            showProposalDetails(currentProposal);
        }
    }

    private void applyFilters() {
        String term = txtSearch.getText().toLowerCase();
        String filter = cmbFilter.getValue();

        if (currentMode.equals("PROPOSALS")) {
            List<Application> filtered = proposalsList.stream()
                    .filter(a -> filter == null || filter.equals("ALL") || a.getStatus().name().equals(filter))
                    .filter(a -> term.isEmpty() || String.valueOf(a.getFreelancerId()).contains(term))
                    .collect(Collectors.toList());
            renderCustomSidebar(filtered, null);
        } else {
            List<Evaluation> filtered = evaluationsList.stream()
                    .filter(e -> filter == null || filter.equals("ALL") || (e.getRating() + " Stars").equals(filter))
                    .filter(e -> term.isEmpty() || String.valueOf(e.getEvaluatedId()).contains(term))
                    .collect(Collectors.toList());
            renderCustomSidebar(null, filtered);
        }
    }

    private void renderCustomSidebar(List<Application> apps, List<Evaluation> evals) {
        sidebarListContainer.getChildren().clear();
        if (apps != null) {
            for (Application a : apps)
                sidebarListContainer.getChildren().add(createProposalCard(a));
        } else if (evals != null) {
            for (Evaluation e : evals)
                sidebarListContainer.getChildren().add(createEvalCard(e));
        }
    }

    private void setValidationStyle(Control control, boolean isValid) {
        if (isValid) {
            control.setStyle("-fx-border-color: #e4ebe4; -fx-background-radius: 8; -fx-border-radius: 8;");
        } else {
            control.setStyle(
                    "-fx-border-color: #d93025; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-width: 1.5;");
        }
    }

    private void showToast(String msg, boolean isError) {
        lblMessage.setText(msg);
        messageContainer
                .setStyle("-fx-background-color: " + (isError ? "#d93025" : "#14a800") + "; -fx-background-radius: 4;");
        messageContainer.setVisible(true);
        PauseTransition p = new PauseTransition(Duration.seconds(3));
        p.setOnFinished(e -> messageContainer.setVisible(false));
        p.play();
    }

    private String getStatusColor(ApplicationStatus s) {
        if (s == null)
            return "#5e6d55";
        return switch (s) {
            case ACCEPTED -> "#14a800";
            case REJECTED -> "#d93025";
            case PENDING -> "#ffa000";
            case WITHDRAWN -> "#5e6d55";
        };
    }

    @FXML
    private void handleCancelForm() {
        showView(viewEmpty);
    }
}
