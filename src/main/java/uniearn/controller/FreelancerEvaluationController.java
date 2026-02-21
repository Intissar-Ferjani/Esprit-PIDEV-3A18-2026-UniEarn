package uniearn.controller;

import uniearn.model.entities.Evaluation;
import uniearn.model.enums.EvaluationType;
import uniearn.services.EvaluationService;
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

public class FreelancerEvaluationController {

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
    @FXML
    private Label lblDetailType;

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
    private CheckBox chkIsProjectRelated;

    // --- FXML Fields: Message Toast ---
    @FXML
    private VBox messageContainer;
    @FXML
    private Label lblMessage;

    private EvaluationService evaluationService;
    private ObservableList<Evaluation> evaluationsList;
    private Evaluation currentEvaluation;

    // In a real app, this would be the logged-in freelancer ID
    private int currentUserId = 1;

    public FreelancerEvaluationController() {
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
        sliderRating.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblFormRatingValue.setText((int) newVal.doubleValue() + " Stars");
        });

        if (chkIsProjectRelated != null) {
            chkIsProjectRelated.selectedProperty().addListener((obs, old, isSelected) -> {
                txtFormProjectId.setDisable(!isSelected);
                if (!isSelected)
                    txtFormProjectId.clear();
            });
            txtFormProjectId.setDisable(true); // Default
        }

        // Initial Load
        loadFreelancerEvaluations();
        showView(viewEmpty);
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        loadFreelancerEvaluations();
    }

    private void loadFreelancerEvaluations() {
        try {
            // Load evaluations GIVEN by this freelancer to clients
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
        Label lblHeader = new Label("To: Client #" + ev.getEvaluatedId());
        lblHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E293B; -fx-font-size: 13px;");

        String snippet = ev.getComment().length() > 25 ? ev.getComment().substring(0, 25) + "..." : ev.getComment();
        Label lblSub = new Label(snippet);
        lblSub.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");

        content.getChildren().addAll(lblHeader, lblSub);
        card.getChildren().addAll(ratingLbl, content);

        // Interaction
        card.setOnMouseClicked(e -> {
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
            lblDetailProjectId.setText("Project: #" + ev.getProjectId());
            lblDetailProjectId.setVisible(true);
        } else {
            lblDetailDate.setText("Generic Review");
            lblDetailProjectId.setVisible(false);
        }

        lblDetailEvaluatedId.setText("Client ID: " + ev.getEvaluatedId());
        txtDetailComment.setText(ev.getComment());
        lblDetailType.setText(ev.getType() != null ? ev.getType().getDisplayName() : "Unknown");

        showView(viewDetails);
    }

    // ================== ACTIONS ==================

    @FXML
    private void handleShowForm() {
        if (currentEvaluation != null) {
            // Edit Mode
            txtFormEvaluatedId.setText(String.valueOf(currentEvaluation.getEvaluatedId()));

            if (currentEvaluation.getProjectId() != null && currentEvaluation.getProjectId() > 0) {
                txtFormProjectId.setText(String.valueOf(currentEvaluation.getProjectId()));
                if (chkIsProjectRelated != null)
                    chkIsProjectRelated.setSelected(true);
            } else {
                txtFormProjectId.clear();
                if (chkIsProjectRelated != null)
                    chkIsProjectRelated.setSelected(false);
            }

            txtFormComment.setText(currentEvaluation.getComment());
            sliderRating.setValue(currentEvaluation.getRating());

            txtFormEvaluatedId.setDisable(true); // Can't change target when editing
        } else {
            // Create Mode
            clearForm();
            txtFormEvaluatedId.setDisable(false);
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
            int rating = (int) sliderRating.getValue();
            String comment = txtFormComment.getText();

            Integer projectId = null;
            if (chkIsProjectRelated != null && chkIsProjectRelated.isSelected()
                    && !txtFormProjectId.getText().isEmpty()) {
                projectId = Integer.parseInt(txtFormProjectId.getText());
            }

            EvaluationType type = EvaluationType.FREELANCER_TO_CLIENT;

            if (currentEvaluation != null) {
                // Update
                if (evaluationService.updateEvaluation(currentEvaluation.getIdEvaluation(), rating, comment,
                        currentUserId)) {
                    showToast("Updated successfully", false);
                    loadFreelancerEvaluations();
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
                    loadFreelancerEvaluations();
                    showView(viewEmpty);
                } else {
                    showToast("Failed: Already reviewed?", true);
                }
            }

        } catch (NumberFormatException e) {
            showToast("Invalid Numbers", true);
        } catch (Exception e) {
            showToast("Error: " + e.getMessage(), true);
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
                    loadFreelancerEvaluations();
                    showView(viewEmpty);
                } else {
                    showToast("Delete failed", true);
                }
            }
        });
    }

    private boolean validateForm() {
        if (txtFormEvaluatedId.getText().isEmpty() || txtFormComment.getText().isEmpty()) {
            showToast("Client ID and Comment required", true);
            return false;
        }
        if (txtFormComment.getText().length() < 10) {
            showToast("Comment too short (min 10 chars)", true);
            return false;
        }

        try {
            Integer.parseInt(txtFormEvaluatedId.getText());
            if (chkIsProjectRelated != null && chkIsProjectRelated.isSelected()
                    && !txtFormProjectId.getText().isEmpty()) {
                Integer.parseInt(txtFormProjectId.getText());
            }
        } catch (NumberFormatException e) {
            showToast("ID must be a number", true);
            return false;
        }

        return true;
    }

    private void clearForm() {
        currentEvaluation = null;
        txtFormEvaluatedId.clear();
        txtFormProjectId.clear();
        txtFormComment.clear();
        sliderRating.setValue(5);
        if (chkIsProjectRelated != null)
            chkIsProjectRelated.setSelected(false);
    }

    private void applyFilters() {
        String term = txtSearchTerm.getText().toLowerCase().trim();
        Integer rate = cmbRatingFilter.getValue();

        List<Evaluation> filtered = evaluationsList.stream()
                .filter(ev -> rate == null || ev.getRating() == rate)
                .filter(ev -> term.isEmpty() || String.valueOf(ev.getEvaluatedId()).contains(term))
                .collect(Collectors.toList());

        renderSidebarList(filtered);
    }

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
