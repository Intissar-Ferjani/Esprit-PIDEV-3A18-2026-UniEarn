package uniearn.controller.auth.freelancer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import uniearn.model.entities.users.User;
import uniearn.model.entities.users.freelancer.Freelancer;
import uniearn.model.entities.users.freelancer.Portfolio;
import uniearn.model.entities.users.freelancer.PortfolioItem;
import uniearn.services.users.freelancer.PortfolioService;
import uniearn.services.users.freelancer.PortfolioItemService;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;


// Step 4 : Portfolio

public class FreelancerPortfolioController {

    @FXML private TextField portfolioTitleField;
    @FXML private TextArea portfolioDescField;
    @FXML private TextField projectTitleField;
    @FXML private TextArea projectDescField;
    @FXML private TextField technologiesField;
    @FXML private TextField projectUrlField;
    @FXML private TextField githubUrlField;
    @FXML private Button backButton;
    @FXML private Button completeButton;
    @FXML private Label portfolioTitleError;
    @FXML private Label portfolioDescError;

    private final PortfolioService portfolioService = new PortfolioService();
    private final PortfolioItemService portfolioItemService = new PortfolioItemService();

    private Freelancer freelancerData;

    // previous steps fields for restoration
    // Step 2
    private String hourlyRate;
    private String skills;
    private String bio;
    private String experience;

    // Step 3
    private String studentCardPath;


//  Receive freelancer data from previous step
    public void setFreelancerData(Freelancer freelancer, String hourlyRate, String skills,
                                  String bio, String experience, String studentCardPath) {
        this.freelancerData = freelancer;
        this.hourlyRate = hourlyRate;
        this.skills = skills;
        this.bio = bio;
        this.experience = experience;
        this.studentCardPath = studentCardPath;
        System.out.println("✓ Portfolio step initialized for: " + freelancer.getName());
    }

    @FXML
    private void handleComplete() {
        // Check if user has portfolio
        boolean hasPortfolioData = hasAnyPortfolioData();

        if (hasPortfolioData) {
            if (!validatePortfolioData()) {
                return;
            }

            try {
                completeButton.setDisable(true);
                savePortfolioData();

                showSuccessAndComplete("Portfolio Created!",
                        "Your portfolio has been saved successfully!");

            } catch (Exception e) {
                showErrorAlert("Error", "Failed to save portfolio: " + e.getMessage());
                e.printStackTrace();
                completeButton.setDisable(false);
            }
        } else {
            // No portfolio data
            showSuccessAndComplete("Registration Complete!",
                    "Your account has been created successfully!\n" +
                            "You can add portfolio items later from your profile.");
        }
    }


    private void savePortfolioData() {

        Portfolio portfolio = new Portfolio();
        portfolio.setTitle(portfolioTitleField.getText().trim());
        portfolio.setDescription(portfolioDescField.getText().trim());
        portfolio.setFreelancerId(freelancerData.getIdFreelancer());
        portfolio.setCreated_At(new Timestamp(new Date().getTime()));

        portfolioService.addPortfolio(portfolio);

        System.out.println("✓ Portfolio created with ID: " + portfolio.getIdPortfolio());

        // If portfolio exists -> create portfolio item
        if (hasPortfolioData()) {
            PortfolioItem item = new PortfolioItem();
            item.setTitle(projectTitleField.getText().trim());
            item.setDescription(projectDescField.getText().trim());

            // technologies (comma-separated to array)
            String techString = technologiesField.getText().trim();
            String[] technologies = techString.isEmpty() ? new String[0] : techString.split(",\\s*");
            item.setTechnologies(technologies);

            // Set URLs
            item.setProjectUrl(projectUrlField.getText().trim());
            item.setGithubUrl(githubUrlField.getText().trim());

            // Set images array
            item.setImagesUrl(new String[0]);

            item.setCreated_At(new Timestamp(new Date().getTime()));
            item.setIdPortfolio(portfolio.getIdPortfolio());

            // Add portfolio item
            portfolioItemService.addPortfolioItem(portfolio, item);

            System.out.println("✓ Portfolio item created");
        }
    }

//    Check if user has portfolio
    private boolean hasPortfolioData() {
        return !projectTitleField.getText().trim().isEmpty() ||
                !projectDescField.getText().trim().isEmpty() ||
                !technologiesField.getText().trim().isEmpty() ||
                !projectUrlField.getText().trim().isEmpty() ||
                !githubUrlField.getText().trim().isEmpty();
    }

    @FXML
    private void handleSkip() {
        showSuccessAndComplete("Registration Complete!",
                "Your account has been created successfully!\n" +
                        "You can add portfolio items later from your profile.");
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/signup/freelancer/student-card-verification.fxml"));
            Parent root = loader.load();

            StudentCardVerificationController controller = loader.getController();

            // Restore all previous data
            controller.setFreelancerData(freelancerData, hourlyRate, skills, bio, experience);

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 850, 600));
            stage.setTitle("Student Verification - UniEarn");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Unable to go back to verification page.");
        }
    }

    private boolean hasAnyPortfolioData() {
        return !portfolioTitleField.getText().trim().isEmpty() ||
                !portfolioDescField.getText().trim().isEmpty() ||
                hasPortfolioData();
    }

    private boolean validatePortfolioData() {
        boolean isValid = true;

        String title = portfolioTitleField.getText().trim();
        String desc = portfolioDescField.getText().trim();

        if (!title.isEmpty()) {
            if (desc.isEmpty()) {
                showError(portfolioDescError, "Portfolio description is required when title is provided");
                isValid = false;
            } else {
                hideError(portfolioDescError);
            }

            if (title.length() < 5) {
                showError(portfolioTitleError, "Portfolio title should be at least 5 characters");
                isValid = false;
            } else {
                hideError(portfolioTitleError);
            }
        } else {
            if (!desc.isEmpty()) {
                showError(portfolioTitleError, "Portfolio title is required when description is provided");
                isValid = false;
            }
        }

        // Validate URLs if provided
        String projectUrl = projectUrlField.getText().trim();
        String githubUrl = githubUrlField.getText().trim();

        if (!projectUrl.isEmpty() && !isValidUrl(projectUrl)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid URL");
            alert.setContentText("Project URL doesn't appear to be valid. Please check the format.");
            alert.showAndWait();
        }

        if (!githubUrl.isEmpty() && !isValidUrl(githubUrl)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid URL");
            alert.setContentText("GitHub URL doesn't appear to be valid. Please check the format.");
            alert.showAndWait();
        }

        return isValid;
    }

    private boolean isValidUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    private void showSuccessAndComplete(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();

        redirectToLogin();
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/login/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) completeButton.getScene().getWindow();
            stage.setScene(new Scene(root, 750, 600));
            stage.setTitle("Login - UniEarn");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showInfoAlert("Registration Complete",
                    "Your account has been created successfully!\n" +
                            "Please restart the application and log in with your credentials.");
            ((Stage) completeButton.getScene().getWindow()).close();
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

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}