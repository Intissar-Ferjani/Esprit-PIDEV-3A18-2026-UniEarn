package uniearn.controller.auth.freelancer;

import javafx.animation.RotateTransition;
import javafx.util.Duration;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import uniearn.controller.auth.user.SignupController;
import uniearn.model.entities.users.User;
import uniearn.model.entities.users.freelancer.Freelancer;
import uniearn.model.enums.Status;
import uniearn.model.enums.VerifStatus;
import uniearn.services.users.UserService;
import uniearn.services.users.freelancer.FreelancerService;
import uniearn.services.users.freelancer.SkillsApiService;
import uniearn.services.users.freelancer.cvAI.CvAIService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Step 2
public class FreelancerSignupController {

    @FXML private TextField        hourlyRateField;
    @FXML private FlowPane         skillsChipsPane;
    @FXML private TextField        skillSearchField;
    @FXML private ListView<String> skillSuggestionsView;
    @FXML private TextArea         bioField;
    @FXML private ComboBox<String> experienceComboBox;
    @FXML private Button           backButton;
    @FXML private Button           nextButton;
    @FXML private Label            hourlyRateError;
    @FXML private Label            skillsError;
    @FXML private Label            bioError;
    @FXML private Label            experienceError;
    @FXML private Button           uploadCvButton;
    @FXML private Label            cvFileLabel;
    @FXML private Label            cvError;           // managed=true always in FXML
    @FXML private Button           generateBioButton;
    @FXML private Label            aiStatusLabel;
    @FXML private Node             bioSpinnerLabel;   // Label (braille) or FontIcon — both work

    private final FreelancerService freelancerService = new FreelancerService();
    private final SkillsApiService  skillsApiService  = new SkillsApiService();
    private final UserService       userService       = new UserService();
    private final CvAIService       cvAIService       = new CvAIService();
    private final List<String>      selectedSkills    = new ArrayList<>();

    private User   basicUserData;
    private File   selectedCvFile;
    private String savedCvPath;
    private File   stagedPhotoFile = null;

    // Guard against double-submit (double-click / rapid re-click)
    private volatile boolean submitting = false;

    // Braille spinner frames — no external dependency needed for the animation itself
    private static final String[] SPINNER_FRAMES =
            {"⠋","⠙","⠹","⠸","⠼","⠴","⠦","⠧","⠇","⠏"};
    private Thread           spinnerThread;
    private volatile boolean spinning = false;

    // ─── Init ─────────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        setupExperienceComboBox();
        setupSkillsSelector();

        if (generateBioButton != null) generateBioButton.setDisable(true);
        if (aiStatusLabel     != null) { aiStatusLabel.setVisible(false); aiStatusLabel.setManaged(false); }

        // cvError must always occupy its layout slot so errors don't cause jarring jumps
        if (cvError != null) { cvError.setVisible(false); cvError.setManaged(true); cvError.setText(""); }

        hideBioSpinner();

        hourlyRateField.focusedProperty().addListener((obs, o, n) -> { if (!n) validateHourlyRate(); });
        bioField.focusedProperty().addListener((obs, o, n)        -> { if (!n) validateBio(); });
        experienceComboBox.valueProperty().addListener((obs, o, n) -> { if (n != null) hideError(experienceError); });
        hourlyRateField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*(\\.\\d{0,2})?")) hourlyRateField.setText(o);
        });
    }

    private void setupExperienceComboBox() {
        experienceComboBox.getItems().addAll(
                "Beginner (0-1 years)",
                "Intermediate (1-3 years)",
                "Advanced (3-5 years)",
                "Expert (5+ years)"
        );
    }

    private void setupSkillsSelector() {
        skillSearchField.setOnMouseClicked(e -> {
            if (!skillSuggestionsView.isVisible()) loadSuggestions(skillSearchField.getText().trim());
        });
        skillSearchField.textProperty().addListener((obs, o, n) -> loadSuggestions(n.trim()));

        skillSuggestionsView.setOnMousePressed(e -> {
            String sel = skillSuggestionsView.getSelectionModel().getSelectedItem();
            if (sel != null) {
                toggleSkill(sel);
                skillSearchField.requestFocus();
                Platform.runLater(() -> loadSuggestions(skillSearchField.getText().trim()));
            }
        });

        skillSearchField.focusedProperty().addListener((obs, o, n) -> {
            if (!n) Platform.runLater(() -> { if (!skillSuggestionsView.isFocused()) hideSuggestions(); });
        });
        skillSuggestionsView.focusedProperty().addListener((obs, o, n) -> {
            if (!n) Platform.runLater(() -> { if (!skillSearchField.isFocused()) hideSuggestions(); });
        });

        skillSuggestionsView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); return; }
                boolean sel = selectedSkills.contains(item);
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                Label cb  = new Label(sel ? "☑" : "☐");
                cb.setStyle("-fx-font-size:16px;-fx-text-fill:" + (sel ? "#1976d2" : "#666") + ";");
                Label lbl = new Label(item);
                lbl.setStyle("-fx-font-size:13px;-fx-text-fill:" + (sel ? "#1976d2" : "#2d3748") + ";"
                        + (sel ? "-fx-font-weight:bold;" : ""));
                row.getChildren().addAll(cb, lbl);
                setGraphic(row); setText(null);
                setStyle("-fx-padding:6 10;-fx-cursor:hand;-fx-background-color:" + (sel ? "#e3f2fd" : "white") + ";");
            }
        });
    }

    private void loadSuggestions(String query) {
        Task<List<String>> task = new Task<>() {
            @Override protected List<String> call() {
                return skillsApiService.fetchSkills(query.length() < 1 ? "programming" : query);
            }
        };
        task.setOnSucceeded(e -> {
            List<String> res = task.getValue().stream().limit(10).collect(Collectors.toList());
            if (!res.isEmpty()) { skillSuggestionsView.setItems(FXCollections.observableArrayList(res)); showSuggestions(); }
            else hideSuggestions();
        });
        task.setOnFailed(e -> hideSuggestions());
        Thread t = new Thread(task); t.setDaemon(true); t.start();
    }

    private void toggleSkill(String skill) {
        if (selectedSkills.contains(skill)) {
            selectedSkills.remove(skill);
            skillsChipsPane.getChildren().removeIf(n -> n instanceof HBox && skill.equals(((HBox) n).getUserData()));
        } else addSkillChip(skill);
        hideError(skillsError);
    }

    private void addSkillChip(String skill) {
        if (selectedSkills.size() >= 20) { showError(skillsError, "Maximum 20 skills allowed"); return; }
        if (selectedSkills.contains(skill)) return;
        selectedSkills.add(skill);
        HBox chip = new HBox(6);
        chip.setUserData(skill); chip.setAlignment(Pos.CENTER_LEFT);
        chip.setStyle("-fx-background-color:#1976d2;-fx-background-radius:20px;-fx-padding:4 10 4 12;-fx-cursor:hand;");
        Label lbl = new Label(skill);
        lbl.setStyle("-fx-text-fill:white;-fx-font-size:12px;");
        Label x = new Label("✕");
        x.setStyle("-fx-text-fill:rgba(255,255,255,0.8);-fx-font-size:11px;-fx-cursor:hand;");
        x.setOnMouseClicked(e -> {
            selectedSkills.remove(skill); skillsChipsPane.getChildren().remove(chip);
            if (skillSuggestionsView.isVisible()) skillSuggestionsView.refresh();
        });
        chip.getChildren().addAll(lbl, x);
        skillsChipsPane.getChildren().add(chip);
    }

    private void showSuggestions() { skillSuggestionsView.setVisible(true);  skillSuggestionsView.setManaged(true);  }
    private void hideSuggestions()  { skillSuggestionsView.setVisible(false); skillSuggestionsView.setManaged(false); }

    // ─── CV Upload ── PDF + DOC/DOCX ─────────────────────────────────────────

    @FXML
    private void handleUploadCv() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Upload Your CV");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Document Files", "*.pdf", "*.doc", "*.docx"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        selectedCvFile = fc.showOpenDialog(uploadCvButton.getScene().getWindow());
        if (selectedCvFile == null) return;

        if (selectedCvFile.length() > 5L * 1024 * 1024) {
            showCvError("CV file must be smaller than 5 MB."); selectedCvFile = null; return;
        }

        String fname = selectedCvFile.getName().toLowerCase();
        if (!fname.endsWith(".pdf") && !fname.endsWith(".doc") && !fname.endsWith(".docx")) {
            showCvError("Please select a PDF, DOC, or DOCX file."); selectedCvFile = null; return;
        }

        try {
            savedCvPath = saveCvFile(selectedCvFile);
            cvFileLabel.setText("✓ " + selectedCvFile.getName());
            cvFileLabel.setStyle("-fx-text-fill:#16a34a;-fx-font-size:12px;-fx-font-style:normal;-fx-font-weight:bold;");
            hideCvError();
            System.out.println("✓ CV saved to: " + savedCvPath);
            if (generateBioButton != null) generateBioButton.setDisable(false);
            generateBioWithAi(); // auto-trigger
        } catch (IOException e) {
            showCvError("Failed to save CV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─── AI Bio Generation ────────────────────────────────────────────────────

    @FXML
    private void generateBioWithAi() {
        if (selectedCvFile == null) { showCvError("Please upload a CV first."); return; }

        String fname = selectedCvFile.getName().toLowerCase();
        if (fname.endsWith(".doc")) {
            setAiStatus("⚠ .doc format not supported for AI — please use PDF or DOCX", false);
            return;
        }

        if (generateBioButton != null) generateBioButton.setDisable(true);
        setAiStatus("Analyzing CV and generating bio...", true);
        bioField.setPromptText("Generating your professional bio with AI...");
        startBioSpinner();

        final String registeredName = (basicUserData != null) ? basicUserData.getName() : null;

        Thread aiThread = new Thread(() -> {
            CvAIService.CvResult result;
            try {
                result = cvAIService.generateBioFromCv(selectedCvFile, registeredName);
            } catch (Exception ex) {
                // Safety net — never leave the UI frozen
                ex.printStackTrace();
                result = CvAIService.CvResult.failure(
                        CvAIService.FailReason.API_FAILED,
                        "Unexpected error. Please write your bio manually.");
            }

            final CvAIService.CvResult finalResult = result;

            Platform.runLater(() -> {
                stopBioSpinner();
                if (generateBioButton != null) generateBioButton.setDisable(false);

                if (finalResult.isSuccess()) {
                    bioField.setText(finalResult.getBio());
                    setAiStatus("✅ Bio generated! Feel free to edit it.", true);
                    hideCvError();
                    hideError(bioError);

                } else {
                    bioField.setPromptText("Tell clients about yourself, your experience, and what makes you unique...");

                    switch (finalResult.getFailReason()) {
                        case NAME_MISMATCH, EXTRACTION_FAILED ->
                                showCvError("⚠ " + finalResult.getFailMessage());
                        case API_FAILED ->
                                setAiStatus("❌ " + finalResult.getFailMessage(), false);
                    }
                }
            });
        });
        aiThread.setDaemon(true);
        aiThread.start();
    }

    // ─── Spinner ──────────────────────────────────────────────────────────────

    private RotateTransition rotateTransition;

    private void startBioSpinner() {
        if (bioSpinnerLabel == null) return;
        spinning = true;
        bioSpinnerLabel.setVisible(true);
        bioSpinnerLabel.setManaged(true);

        if (bioSpinnerLabel instanceof javafx.scene.control.Label lbl) {
            // Braille text animation — no FontAwesome needed
            spinnerThread = new Thread(() -> {
                int i = 0;
                while (spinning) {
                    final String frame = SPINNER_FRAMES[i++ % SPINNER_FRAMES.length];
                    Platform.runLater(() -> lbl.setText(frame));
                    try { Thread.sleep(100); } catch (InterruptedException e) { break; }
                }
            });
            spinnerThread.setDaemon(true);
            spinnerThread.start();
        } else {
            // FontIcon — use a RotateTransition instead of text frames
            rotateTransition = new RotateTransition(Duration.millis(800), bioSpinnerLabel);
            rotateTransition.setByAngle(360);
            rotateTransition.setCycleCount(javafx.animation.Animation.INDEFINITE);
            rotateTransition.setInterpolator(javafx.animation.Interpolator.LINEAR);
            rotateTransition.play();
        }
    }

    private void stopBioSpinner() {
        spinning = false;
        if (spinnerThread   != null) spinnerThread.interrupt();
        if (rotateTransition != null) { rotateTransition.stop(); rotateTransition = null; }
        hideBioSpinner();
    }

    private void hideBioSpinner() {
        if (bioSpinnerLabel == null) return;
        bioSpinnerLabel.setVisible(false);
        bioSpinnerLabel.setManaged(false);
        // Clear text only if it's a Label (FontIcon has no setText)
        if (bioSpinnerLabel instanceof javafx.scene.control.Label lbl) lbl.setText("");
    }

    // ─── CV error helpers ─────────────────────────────────────────────────────

    private void showCvError(String msg) {
        if (cvError == null) return;
        cvError.setText(msg);
        cvError.setVisible(true);
        cvError.setManaged(true);
    }

    private void hideCvError() {
        if (cvError == null) return;
        cvError.setText("");
        cvError.setVisible(false);
        cvError.setManaged(true); // keep managed so layout stays stable
    }

    private void setAiStatus(String message, boolean positive) {
        if (aiStatusLabel == null) return;
        aiStatusLabel.setText(message);
        aiStatusLabel.setStyle(positive
                ? "-fx-font-size:11px;-fx-text-fill:#16a34a;-fx-font-weight:bold;"
                : "-fx-font-size:11px;-fx-text-fill:#e74c3c;-fx-font-weight:bold;");
        aiStatusLabel.setVisible(true);
        aiStatusLabel.setManaged(true);
    }

    private String saveCvFile(File file) throws IOException {
        Path dir = Paths.get("uploads/cv");
        Files.createDirectories(dir);
        Path dest = dir.resolve(System.currentTimeMillis() + "_" + file.getName());
        Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
        return dest.toString();
    }

    // ─── Data In / Out ────────────────────────────────────────────────────────

    public void setUserData(User user) {
        this.basicUserData = user;
        if (user.getProfilePicturePath() != null) {
            File f = new File(user.getProfilePicturePath());
            if (f.exists()) this.stagedPhotoFile = f;
        }
        System.out.println("✓ Freelancer signup step 2 initialized for: " + user.getName());
    }

    public void setStagedPhotoFile(File file) { this.stagedPhotoFile = file; }

    public void restoreFreelancerData(String hourlyRate, String skills, String bio, String experience) {
        if (hourlyRate != null && !hourlyRate.isEmpty())  hourlyRateField.setText(hourlyRate);
        if (skills     != null && !skills.isEmpty()) {
            for (String s : skills.split(",")) { String t = s.trim(); if (!t.isEmpty()) addSkillChip(t); }
        }
        if (bio        != null && !bio.isEmpty())         bioField.setText(bio);
        if (experience != null && !experience.isEmpty())  experienceComboBox.setValue(experience);
    }

    // ─── Validation ───────────────────────────────────────────────────────────

    private boolean validateHourlyRate() {
        String t = hourlyRateField.getText().trim();
        if (t.isEmpty())  { showError(hourlyRateError, "Hourly rate is required"); return false; }
        try {
            double r = Double.parseDouble(t);
            if (r <= 0)    { showError(hourlyRateError, "Rate must be greater than 0"); return false; }
            if (r > 10000) { showError(hourlyRateError, "Rate seems unusually high");   return false; }
        } catch (NumberFormatException e) { showError(hourlyRateError, "Please enter a valid number"); return false; }
        hideError(hourlyRateError); return true;
    }

    private boolean validateSkills() {
        if (selectedSkills.isEmpty()) { showError(skillsError, "Please add at least one skill"); return false; }
        hideError(skillsError); return true;
    }

    private boolean validateBio() {
        String bio = bioField.getText().trim();
        if (bio.isEmpty())      { showError(bioError, "Please write a brief bio");              return false; }
        if (bio.length() < 50)  { showError(bioError, "Bio should be at least 50 characters"); return false; }
        if (bio.length() > 500) { showError(bioError, "Bio should not exceed 500 characters"); return false; }
        hideError(bioError); return true;
    }

    private boolean validateExperience() {
        if (experienceComboBox.getValue() == null) {
            showError(experienceError, "Please select your experience level"); return false;
        }
        hideError(experienceError); return true;
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    @FXML
    private void handleNext() {
        // Immediately block any second call — double-click protection
        if (submitting) return;
        submitting = true;
        nextButton.setDisable(true);

        clearAllErrors();
        boolean valid = validateHourlyRate();
        valid = validateSkills()     && valid;
        valid = validateBio()        && valid;
        valid = validateExperience() && valid;
        if (!valid) {
            // Validation failed — re-enable so user can fix and retry
            submitting = false;
            nextButton.setDisable(false);
            return;
        }

        try {
            Freelancer f = new Freelancer();
            f.setName(basicUserData.getName());
            f.setEmail(basicUserData.getEmail());
            f.setPassword(basicUserData.getPassword());
            f.setRole(basicUserData.getRole());
            f.setPricePerHour(Double.parseDouble(hourlyRateField.getText().trim()));
            f.setSkills(selectedSkills.toArray(new String[0]));
            f.setBio(bioField.getText().trim());
            f.setAmount(0.0); f.setRating(0.0);
            f.setVerificationStatus(VerifStatus.unverified);
            f.setStatus(Status.AVAILABLE);
            f.setIdTask(null);
            f.setCvPath(savedCvPath);

            freelancerService.addFreelancer(f);
            int userId = f.getIdUser();
            if (userId > 0) saveAndLinkPhoto(userId);

            System.out.println("✓ Freelancer profile created, proceeding to verification step");
            redirectToIDVerificationStep(f);

        } catch (SQLException e) {
            showErrorAlert("Database Error", e.getMessage().contains("Duplicate entry")
                    ? "This email is already registered" : e.getMessage());
            submitting = false;
            nextButton.setDisable(false);
        } catch (Exception e) {
            showErrorAlert("Unexpected Error", e.getMessage());
            e.printStackTrace();
            submitting = false;
            nextButton.setDisable(false);
        }
    }

    private void saveAndLinkPhoto(int userId) {
        if (stagedPhotoFile == null || !stagedPhotoFile.exists()) return;
        try {
            File dir = new File("uploads/profiles");
            if (!dir.exists()) dir.mkdirs();
            String ext  = stagedPhotoFile.getName().substring(stagedPhotoFile.getName().lastIndexOf("."));
            Path   dest = Paths.get(dir.getPath(), "freelancer_" + userId + ext);
            Files.copy(stagedPhotoFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            userService.updateProfilePicture(userId, "uploads/profiles/freelancer_" + userId + ext);
            System.out.println("✓ Profile photo linked for user: " + userId);
        } catch (Exception e) {
            System.out.println("⚠ Failed to save profile photo (non-fatal): " + e.getMessage());
        }
    }

    private void redirectToIDVerificationStep(Freelancer freelancer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/auth/signup/freelancer/student-id-verification.fxml"));
            Parent root = loader.load();
            StudentCardVerificationController ctrl = loader.getController();
            ctrl.setFreelancerData(freelancer, hourlyRateField.getText().trim(),
                    String.join(", ", selectedSkills), bioField.getText().trim(),
                    experienceComboBox.getValue(), savedCvPath);
            Stage stage = (Stage) nextButton.getScene().getWindow();
            stage.setScene(new Scene(root, 850, 800));
            stage.setTitle("Student Verification - UniEarn"); stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace(); showErrorAlert("Navigation Error", "Unable to load verification page.");
            nextButton.setDisable(false);
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/signup/signup.fxml"));
            Parent root = loader.load();
            SignupController ctrl = loader.getController();
            ctrl.restoreUserData(basicUserData);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 850, 700));
            stage.setTitle("Sign Up - UniEarn"); stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace(); showErrorAlert("Navigation Error", "Unable to go back to signup page.");
        }
    }

    // ─── Generic helpers ──────────────────────────────────────────────────────

    private void showError(Label lbl, String msg) { lbl.setText(msg); lbl.setVisible(true); lbl.setManaged(true); }
    private void hideError(Label lbl)             { lbl.setVisible(false); lbl.setText(""); }
    private void clearAllErrors() {
        hideError(hourlyRateError); hideError(skillsError);
        hideError(bioError); hideError(experienceError); hideCvError();
    }
    private void showErrorAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}