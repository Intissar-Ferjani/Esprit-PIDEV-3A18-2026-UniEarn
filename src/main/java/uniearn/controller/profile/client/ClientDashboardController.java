package uniearn.controller.profile.client;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import uniearn.database.SessionManager;
import uniearn.model.entities.candidature.application.Application;
import uniearn.model.entities.candidature.evaluation.Evaluation;
import uniearn.model.enums.ApplicationStatus;
import uniearn.model.enums.EvaluationType;
import uniearn.services.candidature.ApplicationService;
import uniearn.services.candidature.EvaluationService;

import java.util.List;
import java.util.stream.Collectors;

public class ClientDashboardController {

    @FXML
    private Button btnToggleProposals;
    @FXML
    private Button btnToggleReviews;

    @FXML
    private TextField txtSearch;
    @FXML
    private TextField txtSearchReview;
    @FXML
    private ComboBox<String> cmbFilter;

    @FXML
    private FlowPane sidebarListContainer;
    @FXML
    private FlowPane reviewsContainer;
    @FXML
    private StackPane mainContent;

    @FXML
    private VBox viewProposalsRoot;
    @FXML
    private VBox viewReviewsRoot;

    @FXML
    private ScrollPane scrollPropDetails;
    @FXML
    private VBox viewPropDetails;

    @FXML
    private ScrollPane scrollEvalDetails;
    @FXML
    private VBox viewEvalDetails;

    @FXML
    private ScrollPane scrollEvalForm;
    @FXML
    private VBox viewEvalForm;

    @FXML
    private VBox messageContainer;
    @FXML
    private Label lblMessage;

    // --- Stats Labels ---
    @FXML
    private Label lblTotalProposals;
    @FXML
    private Label lblAcceptedProposals;
    @FXML
    private Label lblPendingProposals;
    @FXML
    private Label lblRejectedProposals;
    @FXML
    private Label lblTotalReviews;
    @FXML
    private Label lblAvgRating;

    private final ApplicationService applicationService = new ApplicationService();
    private final EvaluationService evaluationService = new EvaluationService();

    private final ObservableList<Application> proposalsList = FXCollections.observableArrayList();
    private final ObservableList<Evaluation> evaluationsList = FXCollections.observableArrayList();

    private Application currentProposal;
    private Evaluation currentEvaluation;

    private int currentUserId;
    private String currentMode = "PROPOSALS";

    @FXML
    public void initialize() {
        if (SessionManager.getInstance().isLoggedIn()) {
            currentUserId = SessionManager.getInstance().getCurrentUserId();
        } else {
            currentUserId = -1;
        }

        setupFilters();
        loadData();
        showView(viewProposalsRoot);
        updateNavStyle();

        if (txtEvalProjId != null && chkEvalIsProject != null) {
            txtEvalProjId.disableProperty().bind(chkEvalIsProject.selectedProperty().not());
        }

        if (sliderEvalRating != null && lblEvalRatingValue != null) {
            sliderEvalRating.valueProperty()
                    .addListener((obs, old, val) -> lblEvalRatingValue.setText(val.intValue() + " Stars"));
        }

        if (cmbFormType != null) {
            cmbFormType.setItems(FXCollections.observableArrayList(EvaluationType.values()));
            if (cmbFormType.getValue() == null) {
                cmbFormType.setValue(EvaluationType.CLIENT_TO_FREELANCER);
            }
        }
    }

    private void updateNavStyle() {
        if (currentMode.equals("PROPOSALS")) {
            btnToggleProposals.setStyle("-fx-background-color: #00457c; -fx-text-fill: white;");
            btnToggleReviews
                    .setStyle("-fx-background-color: white; -fx-text-fill: #00457c; -fx-border-color: #00457c;");
        } else {
            btnToggleReviews.setStyle("-fx-background-color: #00457c; -fx-text-fill: white;");
            btnToggleProposals
                    .setStyle("-fx-background-color: white; -fx-text-fill: #00457c; -fx-border-color: #00457c;");
        }
    }

    private void switchMode(String mode) {
        currentMode = mode;
        updateNavStyle();
        loadData();

        if (mode.equals("PROPOSALS")) {
            showView(viewProposalsRoot);
            if (cmbFilter != null) {
                cmbFilter.setPromptText("Filter by Status");
                cmbFilter.setItems(FXCollections.observableArrayList("ALL", "PENDING", "ACCEPTED", "REJECTED"));
                cmbFilter.setValue("ALL");
            }
        } else {
            showView(viewReviewsRoot);
            if (cmbFilter != null) {
                cmbFilter.setPromptText("Filter by Rating");
                cmbFilter.setItems(
                        FXCollections.observableArrayList("ALL", "5 Stars", "4 Stars", "3 Stars", "2 Stars", "1 Star"));
                cmbFilter.setValue("ALL");
            }
        }
    }

    @FXML
    private void handleShowProposals() {
        switchMode("PROPOSALS");
    }

    @FXML
    public void handleShowReviews() {
        switchMode("REVIEWS");
    }

    private void setupFilters() {
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, old, val) -> applyFilters());
        }
        if (txtSearchReview != null) {
            txtSearchReview.textProperty().addListener((obs, old, val) -> applyFilters());
        }
        if (cmbFilter != null) {
            cmbFilter.setOnAction(e -> applyFilters());
        }
    }

    private void loadData() {
        try {
            List<Application> apps = applicationService.readAll().stream()
                    .filter(a -> isProjectOwnedByClient(a.getProjectId(), currentUserId))
                    .collect(Collectors.toList());
            proposalsList.setAll(apps);

            if (currentMode.equals("PROPOSALS")) {
                renderSidebarList();
                updateProposalStats();
            } else {
                List<Evaluation> evals = evaluationService.getEvaluationsByEvaluated(currentUserId);
                evaluationsList.setAll(evals);
                renderSidebarList();
                updateReviewStats();
            }
        } catch (Exception e) {
            showToast("Error loading data", true);
        }
    }

    private void updateProposalStats() {
        if (lblTotalProposals == null)
            return;

        long total = proposalsList.size();
        long accepted = proposalsList.stream().filter(a -> a.getStatus() == ApplicationStatus.ACCEPTED).count();
        long pending = proposalsList.stream().filter(a -> a.getStatus() == ApplicationStatus.PENDING).count();
        long rejected = proposalsList.stream().filter(a -> a.getStatus() == ApplicationStatus.REJECTED).count();

        lblTotalProposals.setText(String.valueOf(total));
        lblAcceptedProposals.setText(String.valueOf(accepted));
        lblPendingProposals.setText(String.valueOf(pending));
        lblRejectedProposals.setText(String.valueOf(rejected));
    }

    private void updateReviewStats() {
        if (lblTotalReviews == null)
            return;

        long total = evaluationsList.size();
        double avg = evaluationsList.stream()
                .mapToInt(Evaluation::getRating)
                .average()
                .orElse(0.0);

        lblTotalReviews.setText(String.valueOf(total));
        lblAvgRating.setText(String.format("%.1f", avg));
    }

    private boolean isProjectOwnedByClient(int projId, int userId) {
        return true;
    }

    private void renderSidebarList() {
        FlowPane target = currentMode.equals("PROPOSALS") ? sidebarListContainer : reviewsContainer;
        if (target == null) {
            return;
        }

        target.getChildren().clear();
        if (currentMode.equals("PROPOSALS")) {
            for (Application app : proposalsList) {
                target.getChildren().add(createProposalCard(app));
            }
        } else {
            for (Evaluation eval : evaluationsList) {
                target.getChildren().add(createEvalCard(eval));
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

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox quickActions = new HBox(8);
        quickActions.setAlignment(Pos.CENTER_RIGHT);
        if (app.getStatus() == ApplicationStatus.PENDING) {
            Button btnAcc = createQuickBtn("✓", "#10b981");
            btnAcc.setOnAction(e -> {
                currentProposal = app;
                handleAccept();
                e.consume();
            });
            Button btnRej = createQuickBtn("✕", "#ef4444");
            btnRej.setOnAction(e -> {
                currentProposal = app;
                handleReject();
                e.consume();
            });
            Button btnDel = createQuickBtn("🗑", "#64748b");
            btnDel.setOnAction(e -> {
                currentProposal = app;
                handleDelete();
                e.consume();
            });
            quickActions.getChildren().addAll(btnAcc, btnRej, btnDel);
        }

        card.getChildren().addAll(statusDot, info, spacer, quickActions);
        card.setOnMouseClicked(e -> showProposalDetails(app));
        return card;
    }

    private Button createQuickBtn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + color + "11; -fx-text-fill: " + color
                + "; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand; -fx-border-color: "
                + color + "33; -fx-border-radius: 6;");
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color: " + color
                + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;"));
        b.setOnMouseExited(e -> b.setStyle("-fx-background-color: " + color + "11; -fx-text-fill: " + color
                + "; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand; -fx-border-color: "
                + color + "33; -fx-border-radius: 6;"));
        return b;
    }

    private HBox createEvalCard(Evaluation eval) {
        HBox card = new HBox(12);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: white; -fx-border-color: #e4ebe4; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");

        Label lblRating = new Label("*" + eval.getRating());
        lblRating.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffa000;");

        VBox info = new VBox(4);
        Label lblFrom = new Label("From Freelancer #" + eval.getEvaluatorId());
        lblFrom.setStyle("-fx-font-weight: bold; -fx-text-fill: #001e00;");

        String comment = eval.getComment() == null ? "" : eval.getComment();
        String snippet = comment.length() > 20 ? comment.substring(0, 20) + "..." : comment;
        Label lblSnippet = new Label(snippet);
        lblSnippet.setStyle("-fx-text-fill: #5e6d55; -fx-font-size: 11px;");

        info.getChildren().addAll(lblFrom, lblSnippet);
        card.getChildren().addAll(lblRating, info);
        card.setOnMouseClicked(e -> showEvalDetails(eval));
        return card;
    }

    private void showView(javafx.scene.Node view) {
        if (viewProposalsRoot != null)
            viewProposalsRoot.setVisible(false);
        if (viewReviewsRoot != null)
            viewReviewsRoot.setVisible(false);

        if (scrollPropDetails != null)
            scrollPropDetails.setVisible(false);
        if (scrollEvalDetails != null)
            scrollEvalDetails.setVisible(false);
        if (scrollEvalForm != null)
            scrollEvalForm.setVisible(false);

        if (view != null) {
            view.setVisible(true);
        }
    }

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
        currentProposal = app;
        currentEvaluation = null;

        lblPropFreelancer.setText("Freelancer #" + app.getFreelancerId());
        lblPropStatus.setText(app.getStatus().name());
        lblPropStatus.setStyle("-fx-text-fill: " + getStatusColor(app.getStatus()) + "; -fx-font-weight: bold;");
        lblPropBudget.setText(app.getProposedBudget() + " DT");
        lblPropDuration.setText(app.getEstimatedDuration() + " Days");
        txtDetailPropCoverLetter.setText(app.getCoverLetter());

        if (btnGiveFeedback != null) {
            btnGiveFeedback.setVisible(true);
        }

        showView(scrollPropDetails);
    }

    @FXML
    private Label lblEvalTarget;
    @FXML
    private Label lblEvalRating;
    @FXML
    private Label lblEvalType;
    @FXML
    private Text txtDetailEvalComment;

    private void showEvalDetails(Evaluation eval) {
        currentEvaluation = eval;
        currentProposal = proposalsList.stream()
                .filter(a -> a.getFreelancerId() == eval.getEvaluatorId())
                .findFirst()
                .orElse(null);

        lblEvalTarget.setText("From Freelancer #" + eval.getEvaluatorId());
        lblEvalRating.setText("*".repeat(Math.max(1, eval.getRating())));
        if (lblEvalType != null && eval.getType() != null) {
            lblEvalType.setText(eval.getType().getDisplayName());
        }
        txtDetailEvalComment.setText(eval.getComment());
        showView(scrollEvalDetails);
    }

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
    private ComboBox<EvaluationType> cmbFormType;

    @FXML
    private void handleRefresh() {
        loadData();
        showToast("Data refreshed", false);
    }

    @FXML
    private void handleGiveFeedback() {
        currentMode = "REVIEWS";
        updateNavStyle();

        handleAddReview();

        if (currentProposal != null) {
            txtEvalTarget.setText(String.valueOf(currentProposal.getFreelancerId()));
            txtEvalProjId.setText(String.valueOf(currentProposal.getProjectId()));
            if (chkEvalIsProject != null) {
                chkEvalIsProject.setSelected(true);
            }
        } else if (currentEvaluation != null) {
            txtEvalTarget.setText(String.valueOf(currentEvaluation.getEvaluatorId()));
            txtEvalProjId.setText(
                    currentEvaluation.getProjectId() != null ? String.valueOf(currentEvaluation.getProjectId()) : "");
            if (chkEvalIsProject != null) {
                chkEvalIsProject.setSelected(currentEvaluation.getProjectId() != null);
            }
        }

        showView(scrollEvalForm);
    }

    @FXML
    public void handleAddReview() {
        currentEvaluation = null;
        clearEvalForm();
        showView(scrollEvalForm);
    }

    @FXML
    private void handleEdit() {
        if (currentEvaluation != null) {
            txtEvalTarget.setText(String.valueOf(currentEvaluation.getEvaluatedId()));
            txtEvalProjId.setText(
                    currentEvaluation.getProjectId() != null ? String.valueOf(currentEvaluation.getProjectId()) : "");
            if (chkEvalIsProject != null) {
                chkEvalIsProject.setSelected(currentEvaluation.getProjectId() != null);
            }
            sliderEvalRating.setValue(currentEvaluation.getRating());
            txtEvalComment.setText(currentEvaluation.getComment());
            if (cmbFormType != null && currentEvaluation.getType() != null) {
                cmbFormType.setValue(currentEvaluation.getType());
            }
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
            if (currentMode.equals("PROPOSALS")) {
                showView(viewProposalsRoot);
            } else {
                showView(viewReviewsRoot);
            }
        } catch (Exception e) {
            showToast("Delete failed: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleSubmitEval() {
        if (!validateEvalForm()) {
            return;
        }

        try {
            boolean isNew = currentEvaluation == null;
            Evaluation eval = isNew ? new Evaluation() : currentEvaluation;

            eval.setEvaluatorId(currentUserId);
            eval.setEvaluatedId(Integer.parseInt(txtEvalTarget.getText()));

            Integer projId = null;
            boolean hasProjectSelection = chkEvalIsProject != null ? chkEvalIsProject.isSelected()
                    : !txtEvalProjId.getText().isEmpty();
            if (hasProjectSelection && !txtEvalProjId.getText().isEmpty()) {
                projId = Integer.parseInt(txtEvalProjId.getText());
            }
            eval.setProjectId(projId);
            eval.setRating((int) sliderEvalRating.getValue());
            eval.setComment(txtEvalComment.getText());
            eval.setType(cmbFormType != null && cmbFormType.getValue() != null
                    ? cmbFormType.getValue()
                    : EvaluationType.CLIENT_TO_FREELANCER);

            if (isNew) {
                boolean success = evaluationService.createEvaluation(eval);
                if (success) {
                    showToast("Review submitted!", false);
                } else {
                    showToast("Review failed (already exists for this project)", true);
                }
            } else {
                evaluationService.update(eval);
                showToast("Review updated!", false);
            }

            loadData();
            showView(viewReviewsRoot);
        } catch (Exception e) {
            showToast("Review failed: " + e.getMessage(), true);
        }
    }

    private boolean validateEvalForm() {
        boolean valid = true;

        try {
            int tid = Integer.parseInt(txtEvalTarget.getText());
            setValidationStyle(txtEvalTarget, tid > 0);
            if (tid <= 0) {
                valid = false;
            }
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
        if (chkEvalIsProject != null) {
            chkEvalIsProject.setSelected(false);
        }
        if (cmbFormType != null) {
            cmbFormType.setValue(EvaluationType.CLIENT_TO_FREELANCER);
        }
        setValidationStyle(txtEvalTarget, true);
        setValidationStyle(txtEvalComment, true);
    }

    @FXML
    private void handleAccept() {
        if (currentProposal == null) {
            showToast("No related proposal selected", true);
            return;
        }
        if (currentProposal.getStatus() != ApplicationStatus.PENDING) {
            showToast("Only pending proposals can be accepted", true);
            return;
        }

        if (applicationService.acceptApplication(currentProposal.getIdApplication())) {
            showToast("Proposal Accepted!", false);
            loadData();
            showProposalDetails(currentProposal);
        }
    }

    @FXML
    private void handleReject() {
        if (currentProposal == null) {
            showToast("No related proposal selected", true);
            return;
        }
        if (currentProposal.getStatus() != ApplicationStatus.PENDING) {
            showToast("Only pending proposals can be rejected", true);
            return;
        }

        if (applicationService.rejectApplication(currentProposal.getIdApplication())) {
            showToast("Proposal Rejected!", false);
            loadData();
            showProposalDetails(currentProposal);
        }
    }

    @FXML
    private void handleWithdraw() {
        if (currentProposal == null) {
            showToast("No related proposal selected", true);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Withdraw Proposal");
        alert.setHeaderText("Are you sure you want to withdraw/cancel this proposal?");
        alert.setContentText("This action will remove the proposal permanently.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (applicationService.withdrawApplication(currentProposal.getIdApplication(),
                    currentProposal.getFreelancerId())) {
                showToast("Proposal Withdrawn!", false);
                loadData();
                showView(viewProposalsRoot);
            } else {
                showToast("Withdraw failed", true);
            }
        }
    }

    private void applyFilters() {
        final String rawTerm = currentMode.equals("REVIEWS") && txtSearchReview != null
                ? txtSearchReview.getText()
                : txtSearch.getText();
        final String term = rawTerm == null ? "" : rawTerm.toLowerCase();
        final String filter = cmbFilter != null ? cmbFilter.getValue() : "ALL";

        if (currentMode.equals("PROPOSALS")) {
            List<Application> filtered = proposalsList.stream()
                    .filter(a -> filter == null || filter.equals("ALL") || a.getStatus().name().equals(filter))
                    .filter(a -> term.isEmpty() || String.valueOf(a.getFreelancerId()).contains(term))
                    .collect(Collectors.toList());
            renderCustomSidebar(filtered, null);
        } else {
            List<Evaluation> filtered = evaluationsList.stream()
                    .filter(e -> filter == null || filter.equals("ALL") || (e.getRating() + " Stars").equals(filter))
                    .filter(e -> term.isEmpty() || String.valueOf(e.getEvaluatorId()).contains(term)
                            || (e.getComment() != null && e.getComment().toLowerCase().contains(term)))
                    .collect(Collectors.toList());
            renderCustomSidebar(null, filtered);
        }
    }

    private void renderCustomSidebar(List<Application> apps, List<Evaluation> evals) {
        FlowPane target = apps != null ? sidebarListContainer : reviewsContainer;
        if (target == null) {
            return;
        }

        target.getChildren().clear();
        if (apps != null) {
            for (Application a : apps) {
                target.getChildren().add(createProposalCard(a));
            }
        } else if (evals != null) {
            for (Evaluation e : evals) {
                target.getChildren().add(createEvalCard(e));
            }
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

    private String getStatusColor(ApplicationStatus status) {
        if (status == null)
            return "#5e6d55";
        return switch (status) {
            case ACCEPTED -> "#14a800";
            case REJECTED -> "#d93025";
            case PENDING -> "#ffa000";
            case WITHDRAWN -> "#5e6d55";
        };
    }

    @FXML
    private void handleCancelForm() {
        if (currentMode.equals("PROPOSALS")) {
            showView(viewProposalsRoot);
        } else {
            showView(viewReviewsRoot);
        }
    }

    @FXML
    private void handleMessageFreelancer() {
        int freelancerId = getSelectedFreelancerId();
        if (freelancerId <= 0) {
            showToast("Select a freelancer first", true);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/freelancer/Messages.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("UniEarn - Messages (Freelancer #" + freelancerId + ")");
            stage.setMaximized(true);
            stage.centerOnScreen();
        } catch (Exception e) {
            showToast("Failed to open messages: " + e.getMessage(), true);
        }
    }

    private int getSelectedFreelancerId() {
        if (currentProposal != null) {
            return currentProposal.getFreelancerId();
        }
        if (currentEvaluation != null) {
            return currentEvaluation.getEvaluatorId();
        }
        return -1;
    }

    @FXML
    private void handleBackToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/client/client-profile.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("UniEarn - Client Profile");
            stage.setMaximized(true);
            stage.centerOnScreen();
        } catch (Exception e) {
            showToast("Failed to return to profile: " + e.getMessage(), true);
        }
    }
}
