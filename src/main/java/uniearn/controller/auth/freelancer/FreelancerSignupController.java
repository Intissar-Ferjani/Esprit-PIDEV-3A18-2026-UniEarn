package uniearn.controller.auth.freelancer;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import uniearn.controller.auth.user.SignupController;
import uniearn.model.entities.users.User;
import uniearn.model.entities.users.freelancer.Freelancer;
import uniearn.model.enums.Status;
import uniearn.model.enums.VerifStatus;
import uniearn.services.users.freelancer.FreelancerService;
import uniearn.services.users.freelancer.SkillsApiService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Step 2
public class FreelancerSignupController {

    @FXML private TextField hourlyRateField;
    @FXML private FlowPane skillsChipsPane;
    @FXML private TextField skillSearchField;
    @FXML private ListView<String> skillSuggestionsView;
    @FXML private TextArea bioField;
    @FXML private ComboBox<String> experienceComboBox;
    @FXML private Button backButton;
    @FXML private Button nextButton;
    @FXML private Label hourlyRateError;
    @FXML private Label skillsError;
    @FXML private Label bioError;
    @FXML private Label experienceError;

    private final FreelancerService freelancerService = new FreelancerService();
    private final SkillsApiService skillsApiService = new SkillsApiService();
    private final List<String> selectedSkills = new ArrayList<>();

    // Data received from previous step
    private User basicUserData;

    @FXML
    public void initialize() {
        setupExperienceComboBox();
        setupSkillsSelector();

        // Validation listeners
        hourlyRateField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validateHourlyRate();
        });
        bioField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validateBio();
        });
        experienceComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) hideError(experienceError);
        });

        // Format hourly rate to accept only numbers and decimals
        hourlyRateField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d{0,2})?")) {
                hourlyRateField.setText(oldVal);
            }
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
        // Show dropdown on focus/click
        skillSearchField.setOnMouseClicked(e -> {
            if (!skillSuggestionsView.isVisible()) {
                loadSuggestions(skillSearchField.getText().trim());
            }
        });

        // Filter as user types
        skillSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            loadSuggestions(newVal.trim());
        });

        // ✅ Use mouse PRESSED instead of clicked — fires before focus lost
        skillSuggestionsView.setOnMousePressed(e -> {
            String selected = skillSuggestionsView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                toggleSkill(selected);
                // ✅ Keep dropdown open for multi-select
                skillSearchField.requestFocus();
                // Refresh list to show updated checkmarks
                Platform.runLater(() -> loadSuggestions(skillSearchField.getText().trim()));
            }
        });

        // ✅ Only hide when clicking completely outside
        skillSearchField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                // Delay to allow skillSuggestionsView.onMousePressed to fire first
                Platform.runLater(() -> {
                    if (!skillSuggestionsView.isFocused()) {
                        hideSuggestions();
                    }
                });
            }
        });

        skillSuggestionsView.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                Platform.runLater(() -> {
                    if (!skillSearchField.isFocused()) {
                        hideSuggestions();
                    }
                });
            }
        });

        // ✅ Checkbox-style cells showing selected state
        skillSuggestionsView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    boolean isSelected = selectedSkills.contains(item);
                    HBox row = new HBox(10);
                    row.setAlignment(Pos.CENTER_LEFT);

                    // Checkbox indicator
                    Label checkbox = new Label(isSelected ? "☑" : "☐");
                    checkbox.setStyle("-fx-font-size: 16px; -fx-text-fill: "
                            + (isSelected ? "#1976d2" : "#666") + ";");

                    Label skillLabel = new Label(item);
                    skillLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: "
                            + (isSelected ? "#1976d2" : "#2d3748") + ";"
                            + (isSelected ? "-fx-font-weight: bold;" : ""));

                    row.getChildren().addAll(checkbox, skillLabel);
                    setGraphic(row);
                    setText(null);
                    setStyle("-fx-padding: 6 10; -fx-cursor: hand; -fx-background-color: "
                            + (isSelected ? "#e3f2fd" : "white") + ";");
                }
            }
        });
    }

    private void loadSuggestions(String query) {
        Task<List<String>> fetchTask = new Task<>() {
            @Override
            protected List<String> call() {
                if (query.length() < 1) {
                    // Show popular skills when empty
                    return skillsApiService.fetchSkills("programming");
                }
                return skillsApiService.fetchSkills(query);
            }
        };

        fetchTask.setOnSucceeded(e -> {
            List<String> results = fetchTask.getValue().stream()
                    .limit(10)
                    .collect(Collectors.toList());

            if (!results.isEmpty()) {
                skillSuggestionsView.setItems(FXCollections.observableArrayList(results));
                showSuggestions();
            } else {
                hideSuggestions();
            }
        });

        fetchTask.setOnFailed(e -> hideSuggestions());

        Thread thread = new Thread(fetchTask);
        thread.setDaemon(true);
        thread.start();
    }

    // ✅ Toggle: add if not selected, remove if already selected
    private void toggleSkill(String skill) {
        if (selectedSkills.contains(skill)) {
            // Remove chip
            selectedSkills.remove(skill);
            skillsChipsPane.getChildren().removeIf(node -> {
                if (node instanceof HBox) {
                    HBox chip = (HBox) node;
                    return chip.getUserData() != null && chip.getUserData().equals(skill);
                }
                return false;
            });
        } else {
            // Add chip
            addSkillChip(skill);
        }
        hideError(skillsError);
    }

    private void addSkillChip(String skill) {
        if (selectedSkills.size() >= 20) {
            showError(skillsError, "Maximum 20 skills allowed");
            return;
        }
        if (selectedSkills.contains(skill)) return;

        selectedSkills.add(skill);

        HBox chip = new HBox(6);
        chip.setUserData(skill); // ✅ Tag chip with skill name for easy removal
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setStyle(
                "-fx-background-color: #1976d2;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-padding: 4 10 4 12;" +
                        "-fx-cursor: hand;"
        );

        Label skillLabel = new Label(skill);
        skillLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        Label removeBtn = new Label("✕");
        removeBtn.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.8);" +
                        "-fx-font-size: 11px;" +
                        "-fx-cursor: hand;"
        );
        removeBtn.setOnMouseClicked(e -> {
            selectedSkills.remove(skill);
            skillsChipsPane.getChildren().remove(chip);
            // Refresh dropdown checkmarks if open
            if (skillSuggestionsView.isVisible()) {
                skillSuggestionsView.refresh();
            }
        });

        chip.getChildren().addAll(skillLabel, removeBtn);
        skillsChipsPane.getChildren().add(chip);
    }

    private void showSuggestions() {
        skillSuggestionsView.setVisible(true);
        skillSuggestionsView.setManaged(true);
    }

    private void hideSuggestions() {
        skillSuggestionsView.setVisible(false);
        skillSuggestionsView.setManaged(false);
    }

    // Receive user data from Step 1
    public void setUserData(User user) {
        this.basicUserData = user;
        System.out.println("✓ Freelancer signup step 2 initialized for: " + user.getName());
    }

    // Restore step 2 fields when coming back
    public void restoreFreelancerData(String hourlyRate, String skills, String bio, String experience) {
        if (hourlyRate != null && !hourlyRate.isEmpty()) {
            hourlyRateField.setText(hourlyRate);
        }
        if (skills != null && !skills.isEmpty()) {
            // Re-add each skill as a chip
            for (String skill : skills.split(",")) {
                String trimmed = skill.trim();
                if (!trimmed.isEmpty()) addSkillChip(trimmed);
            }
        }
        if (bio != null && !bio.isEmpty()) {
            bioField.setText(bio);
        }
        if (experience != null && !experience.isEmpty()) {
            experienceComboBox.setValue(experience);
        }
    }

    private boolean validateHourlyRate() {
        String rateText = hourlyRateField.getText().trim();

        if (rateText.isEmpty()) {
            showError(hourlyRateError, "Hourly rate is required");
            return false;
        }

        try {
            double rate = Double.parseDouble(rateText);
            if (rate <= 0) {
                showError(hourlyRateError, "Rate must be greater than 0");
                return false;
            }
            if (rate > 10000) {
                showError(hourlyRateError, "Rate seems unusually high");
                return false;
            }
        } catch (NumberFormatException e) {
            showError(hourlyRateError, "Please enter a valid number");
            return false;
        }

        hideError(hourlyRateError);
        return true;
    }

    private boolean validateSkills() {
        if (selectedSkills.isEmpty()) {
            showError(skillsError, "Please add at least one skill");
            return false;
        }
        hideError(skillsError);
        return true;
    }

    private boolean validateBio() {
        String bio = bioField.getText().trim();

        if (bio.isEmpty()) {
            showError(bioError, "Please write a brief bio");
            return false;
        }
        if (bio.length() < 50) {
            showError(bioError, "Bio should be at least 50 characters");
            return false;
        }
        if (bio.length() > 500) {
            showError(bioError, "Bio should not exceed 500 characters");
            return false;
        }

        hideError(bioError);
        return true;
    }

    private boolean validateExperience() {
        if (experienceComboBox.getValue() == null) {
            showError(experienceError, "Please select your experience level");
            return false;
        }
        hideError(experienceError);
        return true;
    }

    @FXML
    private void handleNext() {
        clearAllErrors();

        // Validate all fields before proceeding
        boolean valid = validateHourlyRate();
        valid = validateSkills() && valid;
        valid = validateBio() && valid;
        valid = validateExperience() && valid;

        if (!valid) return;

        try {
            nextButton.setDisable(true);

            Freelancer freelancer = new Freelancer();

            // Step 1 data
            freelancer.setName(basicUserData.getName());
            freelancer.setEmail(basicUserData.getEmail());
            freelancer.setPassword(basicUserData.getPassword());
            freelancer.setRole(basicUserData.getRole());

            // Step 2 data
            freelancer.setPricePerHour(Double.parseDouble(hourlyRateField.getText().trim()));
            freelancer.setSkills(selectedSkills.toArray(new String[0]));
            freelancer.setBio(bioField.getText().trim());

            // Default values
            freelancer.setAmount(0.0);
            freelancer.setRating(0.0);
            freelancer.setVerificationStatus(VerifStatus.unverified);
            freelancer.setStatus(Status.AVAILABLE);
            freelancer.setIdTask(null);

            freelancerService.addFreelancer(freelancer);

            System.out.println("✓ Freelancer profile created, proceeding to verification step");

            redirectToIDVerificationStep(freelancer);

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                showErrorAlert("Registration Failed", "This email is already registered");
            } else {
                showErrorAlert("Database Error", e.getMessage());
            }
            nextButton.setDisable(false);
        } catch (Exception e) {
            showErrorAlert("Unexpected Error", e.getMessage());
            e.printStackTrace();
            nextButton.setDisable(false);
        }
    }

    private void redirectToIDVerificationStep(Freelancer freelancer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/auth/signup/freelancer/student-id-verification.fxml"));
            Parent root = loader.load();

            StudentCardVerificationController controller = loader.getController();
            controller.setFreelancerData(
                    freelancer,
                    hourlyRateField.getText().trim(),
                    String.join(", ", selectedSkills),  // ✅ join chip list
                    bioField.getText().trim(),
                    experienceComboBox.getValue()
            );

            Stage stage = (Stage) nextButton.getScene().getWindow();
            stage.setScene(new Scene(root, 850, 800));
            stage.setTitle("Student Verification - UniEarn");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Unable to load verification page.");
            nextButton.setDisable(false);
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/auth/signup/freelancer/freelancer-information.fxml"));
            Parent signupRoot = loader.load();

            SignupController signupController = loader.getController();
            signupController.restoreUserData(basicUserData);

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(signupRoot, 750, 600));
            stage.setTitle("Sign Up - UniEarn");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Unable to go back to signup page.");
        }
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void hideError(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setText("");
    }

    private void clearAllErrors() {
        hideError(hourlyRateError);
        hideError(skillsError);
        hideError(bioError);
        hideError(experienceError);
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}