package uniearn.controller.profile.client;

import uniearn.model.entities.candidature.evaluation.Evaluation;
import uniearn.model.enums.EvaluationType;
import uniearn.services.candidature.EvaluationService;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;
import java.util.stream.Collectors;

public class ClientEvaluationController {

    // --- FXML Fields: Sidebar ---
    @FXML
    private VBox sidebarListContainer;
    @FXML
    private TextField txtSearchTerm;
    @FXML
    private ComboBox<Integer> cmbRatingFilter;

    // --- FXML Fields: Main Content Area ---
    @FXML
    private StackPane contentArea;
    @FXML
    private VBox viewEmpty;
    @FXML
    private VBox viewDetails;
    @FXML
    private VBox viewForm;

    // --- FXML Fields: Details View ---
    @FXML
    private Label lblDetailRating;
    @FXML
    private Label lblDetailDate;
    @FXML
    private Label lblDetailEvaluatedId;
    @FXML
    private Label lblDetailProjectId;
    @FXML
    private Text txtDetailComment;

    // --- FXML Fields: Form View ---
    @FXML
    private TextField txtFormEvaluatedId;
    @FXML
    private TextField txtFormProjectId;
    @FXML
    private Slider sliderRating;
    @FXML
    private Label lblFormRatingValue;
    @FXML
    private TextArea txtFormComment;
    @FXML
    private ComboBox<EvaluationType> cmbFormType;

    // --- FXML Fields: Message Toast ---
    @FXML
    private VBox messageContainer;
    @FXML
    private Label lblMessage;

    private EvaluationService evaluationService;
    private ObservableList<Evaluation> evaluationsList;
    private Evaluation currentEvaluation;

    // In a real app, this would be the logged-in client ID
    private int currentUserId = 2; // Assuming 2 is a client for demo

    public ClientEvaluationController() {
        this.evaluationService = new EvaluationService();
        this.evaluationsList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        // Setup Filter
        cmbRatingFilter.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        cmbRatingFilter.setOnAction(e -> applyFilters());
        txtSearchTerm.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Form Setup
        cmbFormType.setItems(FXCollections.observableArrayList(EvaluationType.values()));
        cmbFormType.setValue(EvaluationType.CLIENT_TO_FREELANCER);

        sliderRating.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblFormRatingValue.setText((int) newVal.doubleValue() + " Stars");
        });

        // Initial Load
        loadClientEvaluations();
        showView(viewEmpty);
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        loadClientEvaluations();
    }

    private void loadClientEvaluations() {
        try {
            // Load evaluations GIVEN by this user (Client)
            List<Evaluation> evals = evaluationService.getEvaluationsByEvaluator(currentUserId);
            evaluationsList.setAll(evals);
            renderSidebarList(evaluationsList);
        } catch (Exception e) {
            showToast("Error loading evaluations", true);
            e.printStackTrace();
        }
    }

    private void renderSidebarList(List<Evaluation> evals) {
        sidebarListContainer.getChildren().clear();

        if (evals.isEmpty()) {
            Label emptyLbl = new Label("No reviews given yet.");
            emptyLbl.setPadding(new Insets(10));
            emptyLbl.setStyle("-fx-text-fill: #94A3B8; -fx-font-style: italic;");
            sidebarListContainer.getChildren().add(emptyLbl);
            return;
        }

        for (Evaluation ev : evals) {
            HBox card = createSidebarCard(ev);
            sidebarListContainer.getChildren().add(card);
        }
    }

    private HBox createSidebarCard(Evaluation ev) {
        HBox card = new HBox(10);
        card.setPadding(new Insets(12));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0; -fx-cursor: hand; -fx-background-color: white;");

        // Rating Indicator
        Label ratingLbl = new Label("★" + ev.getRating());
        ratingLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #F59E0B; -fx-font-size: 13px; -fx-min-width: 25px;");

        VBox content = new VBox(4);
        Label lblHeader = new Label("To: Freelancer #" + ev.getEvaluatedId());
        lblHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E293B; -fx-font-size: 13px;");

        String snippet = ev.getComment().length() > 25 ? ev.getComment().substring(0, 25) + "..." : ev.getComment();
        Label lblSub = new Label(snippet);
        lblSub.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");

        content.getChildren().addAll(lblHeader, lblSub);

        card.getChildren().addAll(ratingLbl, content);

        // Interaction
        card.setOnMouseClicked(e -> {
            // Highlight
            sidebarListContainer.getChildren().forEach(n -> n.setStyle(
                    "-fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0; -fx-cursor: hand; -fx-background-color: white;"));
            card.setStyle(
                    "-fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0; -fx-cursor: hand; -fx-background-color: #F8FAFC; -fx-border-left-color: #4F46E5; -fx-border-left-width: 3;");

            showDetails(ev);
        });

        return card;
    }

    private void showView(VBox view) {
        viewEmpty.setVisible(false);
        viewDetails.setVisible(false);
        viewForm.setVisible(false);
        view.setVisible(true);
    }

    private void showDetails(Evaluation ev) {
        this.currentEvaluation = ev;

        lblDetailRating.setText("★".repeat(ev.getRating()));

        if (ev.getProjectId() != null && ev.getProjectId() > 0) {
            lblDetailDate.setText("Project #" + ev.getProjectId());
            lblDetailProjectId.setText("Project ID: " + ev.getProjectId());
        } else {
            lblDetailDate.setText("General Review");
            lblDetailProjectId.setText("Type: " + (ev.getType() != null ? ev.getType().getDisplayName() : "General"));
        }

        lblDetailEvaluatedId.setText("Freelancer ID: " + ev.getEvaluatedId());
        txtDetailComment.setText(ev.getComment());

        showView(viewDetails);
    }

    // ================== ACTIONS ==================

    @FXML
    private void handleShowForm() {
        if (currentEvaluation != null) {
            // Edit Mode
            txtFormEvaluatedId.setText(String.valueOf(currentEvaluation.getEvaluatedId()));
            if (currentEvaluation.getProjectId() != null) {
                txtFormProjectId.setText(String.valueOf(currentEvaluation.getProjectId()));
            } else {
                txtFormProjectId.clear();
            }
            txtFormComment.setText(currentEvaluation.getComment());
            sliderRating.setValue(currentEvaluation.getRating());
            cmbFormType.setValue(currentEvaluation.getType());

            txtFormEvaluatedId.setDisable(true); // Can't change target when editing usually
            txtFormProjectId.setDisable(true);
        } else {
            // Create Mode
            clearForm();
            txtFormEvaluatedId.setDisable(false);
            txtFormProjectId.setDisable(false);
        }
        showView(viewForm);
    }

    @FXML
    private void handleCancelForm() {
        if (currentEvaluation != null)
            showView(viewDetails);
        else
            showView(viewEmpty);
    }

    @FXML
    private void handleSubmitForm() {
        if (!validateForm())
            return;

        try {
            int evaluatedId = Integer.parseInt(txtFormEvaluatedId.getText());
            Integer projectId = null;
            if (!txtFormProjectId.getText().isEmpty()) {
                try {
                    projectId = Integer.parseInt(txtFormProjectId.getText());
                } catch (NumberFormatException e) {
                    // Ignore or treat as null/0
                }
            }

            int rating = (int) sliderRating.getValue();
            String comment = txtFormComment.getText();
            EvaluationType type = cmbFormType.getValue();

            if (currentEvaluation != null) {
                // Update
                if (evaluationService.updateEvaluation(currentEvaluation.getIdEvaluation(), rating, comment,
                        currentUserId)) {
                    showToast("Updated successfully", false);
                    loadClientEvaluations();
                    try {
                        currentEvaluation = evaluationService.read(currentEvaluation.getIdEvaluation());
                        if (currentEvaluation != null) {
                            showDetails(currentEvaluation);
                        } else {
                            showView(viewEmpty);
                        }
                    } catch (Exception e) {
                        showView(viewEmpty);
                    }
                } else {
                    showToast("Update failed", true);
                }
            } else {
                // Create
                Evaluation newEval = new Evaluation(currentUserId, evaluatedId, projectId, rating, comment, type);
                if (evaluationService.createEvaluation(newEval)) {
                    showToast("Posted successfully", false);
                    loadClientEvaluations();
                    showView(viewEmpty);
                } else {
                    showToast("Failed: Already reviewed?", true);
                }
            }

        } catch (NumberFormatException e) {
            showToast("Invalid Numbers", true);
        } catch (Exception e) {
            showToast("Database Error: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        if (currentEvaluation == null)
            return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete this review?");
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                if (evaluationService.deleteEvaluation(currentEvaluation.getIdEvaluation(), currentUserId)) {
                    showToast("Deleted", false);
                    currentEvaluation = null;
                    loadClientEvaluations();
                    showView(viewEmpty);
                } else {
                    showToast("Delete failed", true);
                }
            }
        });
    }

    private boolean validateForm() {
        if (txtFormEvaluatedId.getText().isEmpty() || txtFormComment.getText().isEmpty()) {
            showToast("Freelancer ID and Comment required", true);
            return false;
        }
        if (txtFormComment.getText().length() < 10) {
            showToast("Comment too short (min 10 chars)", true);
            return false;
        }

        // Validate project ID if provided
        if (!txtFormProjectId.getText().isEmpty()) {
            try {
                Integer.parseInt(txtFormProjectId.getText());
            } catch (NumberFormatException e) {
                showToast("Invalid Project ID", true);
                return false;
            }
        }

        return true;
    }

    private void clearForm() {
        currentEvaluation = null;
        txtFormEvaluatedId.clear();
        txtFormProjectId.clear();
        txtFormComment.clear();
        sliderRating.setValue(5);
    }

    private void applyFilters() {
        String term = txtSearchTerm.getText().toLowerCase().trim();
        Integer rate = cmbRatingFilter.getValue();

        List<Evaluation> filtered = evaluationsList.stream()
                .filter(ev -> rate == null || ev.getRating() == rate)
                .filter(ev -> term.isEmpty() ||
                        String.valueOf(ev.getEvaluatedId()).contains(term))
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
}
