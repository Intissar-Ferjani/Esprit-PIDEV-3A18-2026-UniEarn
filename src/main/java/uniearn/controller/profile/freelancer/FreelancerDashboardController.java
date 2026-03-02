package uniearn.controller.profile.freelancer;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.text.Text;
import javafx.util.Duration;
import uniearn.model.entities.candidature.application.Application;
import uniearn.model.entities.candidature.evaluation.Evaluation;
import uniearn.model.enums.ApplicationStatus;
import uniearn.model.enums.EvaluationType;
import uniearn.services.candidature.ApplicationService;
import uniearn.services.candidature.EvaluationService;
import uniearn.utils.candidature.ApiManager;
import uniearn.utils.candidature.PdfExporter;
import uniearn.database.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FreelancerDashboardController {

    // --- Sidebar Navigation ---
    @FXML
    private Button btnTglApps;
    @FXML
    private Button btnTglEvals;
    @FXML
    private Button btnTglStats;
    @FXML
    private HBox topNavPill;
    @FXML
    private Button btnBackToProjects;

    // --- Search & Filter ---
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cmbFilter;

    // --- Main Content Area ---
    @FXML
    private StackPane mainContent;
    @FXML
    private VBox viewAppsRoot;
    @FXML
    private VBox viewEvalsRoot;
    @FXML
    private FlowPane sidebarListContainer; // For Applications in new FXML
    @FXML
    private FlowPane evalsContainer; // For Evaluations in new FXML

    @FXML
    private ScrollPane viewStats;

    @FXML
    private VBox viewAppDetails;
    @FXML
    private VBox viewAppForm;
    @FXML
    private VBox viewEvalDetails;
    @FXML
    private VBox viewEvalForm;

    // Statistics Fields
    @FXML
    private javafx.scene.chart.PieChart chartStatus;
    @FXML
    private javafx.scene.chart.BarChart<String, Integer> chartRatings;
    @FXML
    private Label lblTotalEarnings;
    @FXML
    private Label lblAcceptanceRate;

    // --- Message Toast ---
    @FXML
    private VBox messageContainer;
    @FXML
    private Label lblMessage;

    // Services
    private final ApplicationService applicationService = new ApplicationService();
    private final EvaluationService evaluationService = new EvaluationService();
    private final ApiManager apiManager = new ApiManager();

    @FXML
    private Label lblAppBudgetUSD;

    // Data
    private ObservableList<Application> applicationsList = FXCollections.observableArrayList();
    private ObservableList<Evaluation> evaluationsList = FXCollections.observableArrayList();

    private Application currentApplication;
    private Evaluation currentEvaluation;

    private int currentUserId; // Loaded via SessionManager
    private int currentFreelancerId = -1;
    private String currentMode = "APPLICATIONS"; // APPLICATIONS or EVALUATIONS
    private boolean cameFromProjects = false;
    private Integer selectedProjectId;

    @FXML
    public void initialize() {
        if (SessionManager.getInstance().isLoggedIn()) {
            currentUserId = SessionManager.getInstance().getCurrentUserId();
            // Fetch freelancer profile to get the correct idFreelancer
            uniearn.services.users.freelancer.FreelancerService fs = new uniearn.services.users.freelancer.FreelancerService();
            uniearn.model.entities.users.freelancer.Freelancer f = fs.getFreelancerById(currentUserId);
            if (f != null) {
                currentFreelancerId = f.getIdFreelancer();
            }
        } else {
            currentUserId = -1;
        }
        setupNavigation();
        setupFilters();
        loadData();
        showView(viewAppsRoot);

        // Fix FXML expression issue: Bind disable property in Java
        txtEvalProjId.disableProperty().bind(chkEvalIsProject.selectedProperty().not());

        // Add rating slider listener to update label
        sliderEvalRating.valueProperty().addListener((obs, old, val) -> {
            lblEvalRatingValue.setText(val.intValue() + " Stars");
        });
    }

    /**
     * Public method to allow navigation from the Projects view directly to a
     * specific application form.
     */
    public void switchToApplicationForm(int projectId) {
        javafx.application.Platform.runLater(() -> {
            switchMode("APPLICATIONS");
            currentApplication = null;
            clearAppForm();
            this.selectedProjectId = projectId;
            this.cameFromProjects = true;
            showView(viewAppForm);
        });
    }

    @FXML
    public void handleBackToProjects() {
        this.cameFromProjects = false;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/freelancer/freelancer-profile.fxml"));
            Parent root = loader.load();

            FreelancerProfileController profileController = loader.getController();

            // Re-fetch the full freelancer object
            uniearn.services.users.freelancer.FreelancerService freelancerService = new uniearn.services.users.freelancer.FreelancerService();
            uniearn.model.entities.users.freelancer.Freelancer freelancer = freelancerService
                    .getFreelancerById(currentUserId);

            profileController.setFreelancerData(freelancer);

            // Switch to the projects view inside the profile shell
            profileController.handleFreelancerProjects();

            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Mon Profil - UniEarn");
            stage.centerOnScreen();

            System.out.println("✓ Navigated directly back to main profile (Projects view)");
        } catch (IOException e) {
            e.printStackTrace();
            showToast("Navigation failed: " + e.getMessage(), true);
        }
    }

    private void setupNavigation() {
        // Navigation is handled via onAction in FXML
        updateNavStyle();
    }

    @FXML
    private void handleShowApps() {
        this.cameFromProjects = false;
        switchMode("APPLICATIONS");
    }

    @FXML
    private void handleShowEvals() {
        this.cameFromProjects = false;
        switchMode("EVALUATIONS");
    }

    @FXML
    private void handleShowStats() {
        this.cameFromProjects = false;
        switchMode("STATS");
    }

    private void switchMode(String mode) {
        this.currentMode = mode;
        updateNavStyle();
        loadData();
        // Updated: Show the root view of the current mode instead of viewEmpty
        if (mode.equals("APPLICATIONS"))
            showView(viewAppsRoot);
        else if (mode.equals("EVALUATIONS"))
            showView(viewEvalsRoot);
        else if (mode.equals("STATS"))
            showView(viewStats);

        if (mode.equals("APPLICATIONS")) {
            cmbFilter.setPromptText("Filter by Status");
            cmbFilter.setItems(FXCollections.observableArrayList("ALL", "PENDING", "ACCEPTED", "REJECTED"));
            cmbFilter.setValue("ALL");
        } else if (mode.equals("EVALUATIONS")) {
            cmbFilter.setPromptText("Filter by Rating");
            cmbFilter.setItems(
                    FXCollections.observableArrayList("ALL", "5 Stars", "4 Stars", "3 Stars", "2 Stars", "1 Star"));
            cmbFilter.setValue("ALL");
        } else if (mode.equals("STATS")) {
            loadStats();
        }
    }

    private void updateNavStyle() {
        // Styling for Buttons
        String activeStyle = "-fx-background-color: #00457c; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 4; -fx-cursor: hand; -fx-pref-width: 90;";
        String inactiveStyle = "-fx-background-color: white; -fx-text-fill: #00457c; -fx-border-color: #00457c; -fx-border-radius: 4; -fx-font-size: 11px; -fx-background-radius: 4; -fx-cursor: hand; -fx-pref-width: 80;";

        if (btnTglApps != null)
            btnTglApps.setStyle(currentMode.equals("APPLICATIONS") ? activeStyle : inactiveStyle);
        if (btnTglEvals != null)
            btnTglEvals.setStyle(currentMode.equals("EVALUATIONS") ? activeStyle : inactiveStyle);
        if (btnTglStats != null)
            btnTglStats.setStyle(currentMode.equals("STATS") ? activeStyle : inactiveStyle);
    }

    private void setupFilters() {
        txtSearch.textProperty().addListener((obs, old, newVal) -> applyFilters());
        cmbFilter.setOnAction(e -> applyFilters());
    }

    private void loadData() {
        try {
            if (currentMode.equals("APPLICATIONS")) {
                List<Application> apps = (currentFreelancerId != -1)
                        ? applicationService.getApplicationsByFreelancer(currentFreelancerId)
                        : new ArrayList<>();
                applicationsList.setAll(apps);
                renderSidebarList();
            } else {
                // Evaluations are linked to the user account or freelancer profile?
                // Based on previous logic, they were linked to idUser.
                List<Evaluation> evals = evaluationService.getEvaluationsByEvaluated(currentUserId);
                evaluationsList.setAll(evals);
                renderSidebarList();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error loading data", true);
        }
    }

    private void renderSidebarList() {
        if (currentMode.equals("APPLICATIONS")) {
            sidebarListContainer.getChildren().clear();
            for (Application app : applicationsList) {
                sidebarListContainer.getChildren().add(createAppCard(app));
            }
        } else if (currentMode.equals("EVALUATIONS")) {
            evalsContainer.getChildren().clear();
            for (Evaluation eval : evaluationsList) {
                evalsContainer.getChildren().add(createEvalCard(eval));
            }
        }
    }

    private HBox createAppCard(Application app) {
        HBox card = new HBox(12);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("card");
        card.setStyle(
                "-fx-background-color: white; -fx-border-color: #e4ebe4; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");

        Circle statusDot = new Circle(4, Color.web(getStatusColor(app.getStatus())));
        VBox info = new VBox(4);
        Label lblProj = new Label("Project #" + app.getProjectId());
        lblProj.setStyle("-fx-font-weight: bold; -fx-text-fill: #001e00;");
        Label lblPrice = new Label(app.getProposedBudget() + " DT");
        lblPrice.setStyle("-fx-text-fill: #5e6d55; -fx-font-size: 12px;");
        info.getChildren().addAll(lblProj, lblPrice);

        card.getChildren().addAll(statusDot, info);
        card.setOnMouseClicked(e -> showAppDetails(app));
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
        Label lblFrom = new Label("From Evaluator #" + eval.getEvaluatorId());
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

    // --- Detail Switching ---
    private void showView(Region view) {
        if (viewAppsRoot != null)
            viewAppsRoot.setVisible(false);
        if (viewAppDetails != null)
            viewAppDetails.setVisible(false);
        if (viewAppForm != null)
            viewAppForm.setVisible(false);
        if (viewEvalsRoot != null)
            viewEvalsRoot.setVisible(false);
        if (viewEvalDetails != null)
            viewEvalDetails.setVisible(false);
        if (viewEvalForm != null)
            viewEvalForm.setVisible(false);
        if (viewStats != null)
            viewStats.setVisible(false);

        if (view != null)
            view.setVisible(true);

        // Hide top navigation when in form mode
        if (topNavPill != null) {
            boolean isForm = (view == viewAppForm || view == viewEvalForm);
            topNavPill.setVisible(!isForm);
            topNavPill.setManaged(!isForm);
        }

        // Handle btnBackToProjects visibility:
        // Only show if we came from projects AND we are NOT in the main list views
        if (btnBackToProjects != null) {
            boolean isMainList = (view == viewAppsRoot || view == viewEvalsRoot || view == viewStats);
            boolean shouldShow = cameFromProjects && !isMainList;
            btnBackToProjects.setVisible(shouldShow);
            btnBackToProjects.setManaged(shouldShow);
        }
    }

    // --- FXML Labels: App Details ---
    @FXML
    private Label lblAppProjId;
    @FXML
    private Label lblAppStatus;
    @FXML
    private Label lblAppBudget;
    @FXML
    private Label lblAppDuration;
    @FXML
    private Text txtDetailAppCoverLetter;
    @FXML
    private Button btnReviewClient;

    // --- FXML Fields: App Form ---
    @FXML
    private TextField txtAppBudget;
    @FXML
    private TextField txtAppDuration;
    @FXML
    private TextArea txtAppCoverLetter;

    // --- FXML Labels: Eval Details ---
    @FXML
    private Label lblEvalTarget;
    @FXML
    private Label lblEvalRating;
    @FXML
    private Label lblEvalType;
    @FXML
    private Label lblEvalSentiment;
    @FXML
    private Text txtDetailEvalComment;

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
    public void handleExportPdf() {
        if (currentApplication == null)
            return;

        try {
            String fileName = "Application_" + currentApplication.getIdApplication() + ".pdf";
            String path = System.getProperty("user.home") + "/Downloads/" + fileName;
            PdfExporter.exportApplication(currentApplication, path);
            showToast("PDF saved to Downloads folder", false);
        } catch (Exception e) {
            showToast("PDF Export failed: " + e.getMessage(), true);
        }
    }

    private void showAppDetails(Application app) {
        this.currentApplication = app;
        lblAppProjId.setText("Project #" + app.getProjectId());
        lblAppStatus.setText(app.getStatus().name());
        lblAppStatus.setStyle("-fx-text-fill: " + getStatusColor(app.getStatus()) + "; -fx-font-weight: bold;");
        lblAppBudget.setText(app.getProposedBudget() + " DT");
        lblAppBudgetUSD.setText(""); // Reset conversion
        lblAppDuration.setText(app.getEstimatedDuration() + " Days");

        // Advanced Feature 2: Search Highlighting
        String coverLetter = app.getCoverLetter();
        String searchTerm = txtSearch.getText().trim();
        txtDetailAppCoverLetter.setText(coverLetter);

        if (!searchTerm.isEmpty() && coverLetter.toLowerCase().contains(searchTerm.toLowerCase())) {
            // Simple highlighting by making text uppercase if it matches (simulated for
            // Text node)
            // In a full implementation, we'd use TextFlow with different Text segments
            System.out.println("Highlighter: Match found for '" + searchTerm + "'");
        }

        // Show review client button ONLY if the application is accepted
        if (btnReviewClient != null) {
            btnReviewClient.setVisible(app.getStatus() == uniearn.model.enums.ApplicationStatus.ACCEPTED);
        }

        showView(viewAppDetails);
    }

    private void showEvalDetails(Evaluation eval) {
        this.currentEvaluation = eval;
        lblEvalTarget.setText("From User #" + eval.getEvaluatorId());
        lblEvalRating.setText("★".repeat(Math.max(1, eval.getRating())));
        lblEvalType.setText(eval.getType().getDisplayName());
        txtDetailEvalComment.setText(eval.getComment());
        lblEvalSentiment.setText("Analyze");
        lblEvalSentiment.setStyle(
                "-fx-padding: 2 8; -fx-background-color: #e8f0fe; -fx-text-fill: #1967d2; -fx-background-radius: 12;");
        showView(viewEvalDetails);
    }

    @FXML
    private void handleAnalyzeSentiment() {
        if (currentEvaluation == null)
            return;

        lblEvalSentiment.setText("Analyzing...");
        apiManager.analyzeSentiment(currentEvaluation.getComment()).thenAccept(label -> {
            javafx.application.Platform.runLater(() -> {
                String color = switch (label) {
                    case "pos" -> "#14a800";
                    case "neg" -> "#d93025";
                    default -> "#ffa000";
                };
                String text = switch (label) {
                    case "pos" -> "Positive";
                    case "neg" -> "Negative";
                    default -> "Neutral";
                };
                lblEvalSentiment.setText(text);
                lblEvalSentiment.setStyle("-fx-padding: 2 8; -fx-background-color: " + color + "22; -fx-text-fill: "
                        + color + "; -fx-background-radius: 12; -fx-font-weight: bold;");
            });
        });
    }

    @FXML
    private void handleRefresh() {
        System.out.println("DEBUG: Refresh clicked - Mode: " + currentMode);
        loadData();
        showToast("Data refreshed", false);
    }

    @FXML
    private void handleAdd() {
        if (currentMode.equals("APPLICATIONS")) {
            currentApplication = null;
            clearAppForm();
            selectedProjectId = null; // Manual add from dashboard? User shouldn't really do this now
            showView(viewAppForm);
        } else {
            currentEvaluation = null;
            clearEvalForm();
            showView(viewEvalForm);
        }
    }

    @FXML
    private void handleReviewClient() {
        if (currentApplication != null) {
            currentMode = "EVALUATIONS";
            updateNavStyle();

            currentEvaluation = null;
            clearEvalForm();

            // Fetch the client ID linked to this project
            int clientId = applicationService.getClientIdByProject(currentApplication.getProjectId());
            txtEvalTarget.setText(String.valueOf(clientId));
            txtEvalProjId.setText(String.valueOf(currentApplication.getProjectId()));
            chkEvalIsProject.setSelected(true);

            showView(viewEvalForm);
        }
    }

    @FXML
    private void handleEdit() {
        if (currentMode.equals("APPLICATIONS") && currentApplication != null) {
            selectedProjectId = currentApplication.getProjectId();
            txtAppBudget.setText(String.valueOf(currentApplication.getProposedBudget()));
            txtAppDuration.setText(String.valueOf(currentApplication.getEstimatedDuration()));
            txtAppCoverLetter.setText(currentApplication.getCoverLetter());
            showView(viewAppForm);
        } else if (currentMode.equals("EVALUATIONS") && currentEvaluation != null) {
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
            if (currentMode.equals("APPLICATIONS") && currentApplication != null) {
                applicationService.delete(currentApplication.getIdApplication());
                showToast("Application deleted", false);
            } else if (currentMode.equals("EVALUATIONS") && currentEvaluation != null) {
                evaluationService.delete(currentEvaluation.getIdEvaluation());
                showToast("Review deleted", false);
            }
            loadData();
            if (currentMode.equals("APPLICATIONS"))
                showView(viewAppsRoot);
            else
                showView(viewEvalsRoot);
        } catch (Exception e) {
            showToast("Delete failed: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleSubmitApp() {
        if (!validateAppForm())
            return;

        try {
            if (currentFreelancerId == -1) {
                showToast("Error: No freelancer profile found", true);
                return;
            }

            boolean isNew = (currentApplication == null);
            Application app = isNew ? new Application() : currentApplication;

            app.setFreelancerId(currentFreelancerId);
            app.setProjectId(isNew ? selectedProjectId : currentApplication.getProjectId());
            app.setCoverLetter(txtAppCoverLetter.getText());
            app.setProposedBudget(Double.parseDouble(txtAppBudget.getText()));
            app.setEstimatedDuration(Integer.parseInt(txtAppDuration.getText()));

            System.out.println("Submitting App: Freelancer=" + currentFreelancerId + " Project=" + app.getProjectId());

            if (isNew) {
                applicationService.applyToProject(app);
                showToast("Proposal submitted in DT!", false);
            } else {
                applicationService.update(app);
                showToast("Proposal updated!", false);
            }

            loadData();
            if (cameFromProjects) {
                handleBackToProjects();
            } else {
                showView(viewAppsRoot);
            }
        } catch (Exception e) {
            showToast("Failed: " + e.getMessage(), true);
        }
    }

    private boolean validateAppForm() {
        boolean valid = true;

        if (currentApplication == null && selectedProjectId == null) {
            showToast("Error: No project selected", true);
            return false;
        }

        try {
            double budget = Double.parseDouble(txtAppBudget.getText());
            setValidationStyle(txtAppBudget, budget > 0);
            if (budget <= 0) {
                showToast("Budget must be positive DT!", true);
                valid = false;
            }
        } catch (Exception e) {
            setValidationStyle(txtAppBudget, false);
            valid = false;
        }

        try {
            int dur = Integer.parseInt(txtAppDuration.getText());
            setValidationStyle(txtAppDuration, dur > 0);
            if (dur <= 0) {
                showToast("Duration must be at least 1 day", true);
                valid = false;
            }
        } catch (Exception e) {
            setValidationStyle(txtAppDuration, false);
            valid = false;
        }

        boolean cvValid = txtAppCoverLetter.getText().trim().length() >= 20;
        setValidationStyle(txtAppCoverLetter, cvValid);
        if (!cvValid) {
            showToast("Cover letter must be at least 20 chars", true);
            valid = false;
        }

        return valid;
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
            eval.setType(EvaluationType.FREELANCER_TO_CLIENT);

            if (!eval.isValid()) {
                if (eval.getEvaluatorId() == eval.getEvaluatedId()) {
                    showToast("Error: You cannot review yourself!", true);
                } else if (eval.getComment().length() < 15) {
                    showToast("Error: Comment too short (Min 15 chars)", true);
                } else {
                    showToast("Error: Invalid review data", true);
                }
                return;
            }

            // Advanced Feature 4: Verified Review Logic
            if (eval.getProjectId() != null) {
                // Check if the project was actually 'accepted' (simulating completion check)
                boolean isVerified = applicationService.getApplicationsByFreelancer(eval.getEvaluatorId()).stream()
                        .anyMatch(a -> a.getProjectId() == eval.getProjectId()
                                && a.getStatus() == ApplicationStatus.ACCEPTED);
                if (isVerified) {
                    eval.setComment("[Verified Review] " + eval.getComment());
                }
            }

            if (isNew) {
                evaluationService.createEvaluation(eval);
                showToast("Review submitted!", false);
                // API 4: Simulated Email Notification
                System.out.println(
                        "SIMULATED API: Sending email to user " + eval.getEvaluatedId() + " about new review...");
            } else {
                evaluationService.update(eval);
                showToast("Review updated!", false);
            }

            loadData();
            if (cameFromProjects) {
                handleBackToProjects();
            } else {
                showView(viewEvalsRoot);
            }
        } catch (SQLException e) {
            showToast(e.getMessage(), true);
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

    // --- VALIDATION HELPER ---
    private void setValidationStyle(Control control, boolean isValid) {
        if (isValid) {
            control.setStyle("-fx-border-color: #e4ebe4; -fx-background-radius: 8; -fx-border-radius: 8;");
        } else {
            control.setStyle(
                    "-fx-border-color: #d93025; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-width: 1.5;");
        }
    }

    // --- TO BE CONTINUED WITH FORM LOGIC AND FXML FIELDS ---
    private void clearAppForm() {
        txtAppBudget.clear();
        txtAppDuration.clear();
        txtAppCoverLetter.clear();
        setValidationStyle(txtAppBudget, true);
        setValidationStyle(txtAppDuration, true);
        setValidationStyle(txtAppCoverLetter, true);
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

    private void applyFilters() {
        String term = txtSearch.getText().toLowerCase();
        String filter = cmbFilter.getValue();

        if (currentMode.equals("APPLICATIONS")) {
            List<Application> filtered = applicationsList.stream()
                    .filter(a -> filter == null || filter.equals("ALL") || a.getStatus().name().equals(filter))
                    .filter(a -> term.isEmpty() || String.valueOf(a.getProjectId()).contains(term)
                            || a.getCoverLetter().toLowerCase().contains(term))
                    .collect(Collectors.toList());

            sidebarListContainer.getChildren().clear();
            for (Application a : filtered)
                sidebarListContainer.getChildren().add(createAppCard(a));
        } else if (currentMode.equals("EVALUATIONS")) {
            List<Evaluation> filtered = evaluationsList.stream()
                    .filter(e -> filter == null || filter.equals("ALL") || (e.getRating() + " Stars").equals(filter))
                    .filter(e -> term.isEmpty() || String.valueOf(e.getEvaluatedId()).contains(term)
                            || e.getComment().toLowerCase().contains(term))
                    .collect(Collectors.toList());

            evalsContainer.getChildren().clear();
            for (Evaluation e : filtered)
                evalsContainer.getChildren().add(createEvalCard(e));
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

    @FXML
    private void handleCancelForm() {
        if (cameFromProjects) {
            handleBackToProjects();
        } else {
            if (currentMode.equals("APPLICATIONS"))
                showView(viewAppsRoot);
            else
                showView(viewEvalsRoot);
        }
    }

    @FXML
    private void handleConvertCurrency() {
        if (currentApplication == null)
            return;

        lblAppBudgetUSD.setText("Converting...");
        double budgetDT = currentApplication.getProposedBudget();

        apiManager.getExchangeRate("USD").thenAccept(rate -> {
            javafx.application.Platform.runLater(() -> {
                double budgetUSD = budgetDT * rate;
                lblAppBudgetUSD.setText(String.format("≈ $%.2f USD", budgetUSD));
            });
        });
    }

    private void loadStats() {
        showView(viewStats);
        long accepted = applicationsList.stream().filter(a -> a.getStatus() == ApplicationStatus.ACCEPTED).count();
        long rejected = applicationsList.stream().filter(a -> a.getStatus() == ApplicationStatus.REJECTED).count();
        long pending = applicationsList.stream().filter(a -> a.getStatus() == ApplicationStatus.PENDING).count();

        ObservableList<javafx.scene.chart.PieChart.Data> pieData = FXCollections.observableArrayList(
                new javafx.scene.chart.PieChart.Data("Accepted", accepted),
                new javafx.scene.chart.PieChart.Data("Rejected", rejected),
                new javafx.scene.chart.PieChart.Data("Pending", pending));
        chartStatus.setData(pieData);

        double totalEarned = applicationsList.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.ACCEPTED)
                .mapToDouble(Application::getProposedBudget)
                .sum();
        lblTotalEarnings.setText(String.format("%.2f DT", totalEarned));

        double rate = applicationsList.isEmpty() ? 0 : (double) accepted / applicationsList.size() * 100;
        lblAcceptanceRate.setText(String.format("%.1f%%", rate));

        // Rating Distribution BarChart
        javafx.scene.chart.XYChart.Series<String, Integer> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName("Ratings");
        for (int i = 1; i <= 5; i++) {
            final int r = i;
            int count = (int) evaluationsList.stream().filter(e -> e.getRating() == r).count();
            series.getData().add(new javafx.scene.chart.XYChart.Data<>(i + "★", count));
        }
        chartRatings.getData().clear();
        chartRatings.getData().add(series);
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
    private void handleBackToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/freelancer/freelancer-profile.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("UniEarn - Freelancer Profile");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Failed to return to profile: " + e.getMessage(), true);
        }
    }
}
