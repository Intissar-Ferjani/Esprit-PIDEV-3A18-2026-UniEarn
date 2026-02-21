package uniearn.controller.auth.freelancer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import uniearn.model.entities.users.User;
import uniearn.model.entities.users.freelancer.Freelancer;
import uniearn.model.enums.VerifStatus;
import uniearn.services.users.freelancer.FreelancerService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

//Step 3
public class StudentCardVerificationController {

    @FXML private Button uploadCardButton;
    @FXML private Button backButton;
    @FXML private Button nextButton;
    @FXML private Label cardFileLabel;
    @FXML private Label verificationStatusLabel;

    private final FreelancerService freelancerService = new FreelancerService();

    private Freelancer freelancerData;
    private File selectedFile;
    private String savedFilePath;

    // Step 2 field values for restoration on back navigation
    private String hourlyRate;
    private String skills;
    private String bio;
    private String experience;

    @FXML
    public void initialize() {
        // Disable next button until file is uploaded
        nextButton.setDisable(true);
    }

//    Receive freelancer data and step 2 values from previous step
    public void setFreelancerData(Freelancer freelancer, String hourlyRate, String skills, String bio, String experience) {
        this.freelancerData = freelancer;
        this.hourlyRate = hourlyRate;
        this.skills = skills;
        this.bio = bio;
        this.experience = experience;
        System.out.println("✓ Verification step initialized for: " + freelancer.getName());
    }

    @FXML
    private void handleUploadCard() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload Student Card");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        selectedFile = fileChooser.showOpenDialog(uploadCardButton.getScene().getWindow());

        if (selectedFile != null) {
            // Validate file
            if (!validateFile(selectedFile)) {
                return;
            }

            // Save file to uploads directory
            try {
                savedFilePath = saveStudentCard(selectedFile);

                // Update UI
                cardFileLabel.setText("✓ " + selectedFile.getName());
                cardFileLabel.setStyle("-fx-text-fill: #28a745;");

                showVerificationStatus("File uploaded successfully! Ready to proceed.", true);

                // Enable next button
                nextButton.setDisable(false);

            } catch (IOException e) {
                showErrorAlert("Upload Failed", "Could not save the file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean validateFile(File file) {
        // Check file size (max 5MB)
        long fileSizeInMB = file.length() / (1024 * 1024);
        if (fileSizeInMB > 5) {
            showErrorAlert("File Too Large", "Please select a file smaller than 5MB");
            return false;
        }

        // Check file extension
        String fileName = file.getName().toLowerCase();
        if (!fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg") &&
                !fileName.endsWith(".png") && !fileName.endsWith(".pdf")) {
            showErrorAlert("Invalid File Type", "Please select a JPG, PNG, or PDF file");
            return false;
        }

        return true;
    }

    private String saveStudentCard(File file) throws IOException {
        // Create uploads directory if it doesn't exist
        Path uploadsDir = Paths.get("uploads/student_cards");
        Files.createDirectories(uploadsDir);

        // Generate unique filename
        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName = freelancerData.getIdUser() + "_" + timestamp + "_" + file.getName();
        Path targetPath = uploadsDir.resolve(fileName);

        // Copy file to uploads directory
        Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("✓ Student card saved to: " + targetPath.toString());
        return targetPath.toString();
    }

    @FXML
    private void handleNext() {
        if (savedFilePath == null) {
            showErrorAlert("No File Selected", "Please upload your student card before continuing");
            return;
        }

        try {
            nextButton.setDisable(true);

            // Update database with student card path + status
            freelancerService.updateVerificationData(
                    freelancerData.getIdUser(),
                    savedFilePath,
                    VerifStatus.unverified
            );

            System.out.println("✓ Student card uploaded, verification status: PENDING");

            // Navigate to Step 4
            redirectToPortfolioStep();

        } catch (SQLException e) {
            showErrorAlert("Upload Failed", "Failed to save verification data: " + e.getMessage());
            e.printStackTrace();
            nextButton.setDisable(false);
        }
    }

//    Redirect to step 4
    private void redirectToPortfolioStep() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/signup/freelancer/portfolio-information.fxml"));
            Parent root = loader.load();

            FreelancerPortfolioController controller = loader.getController();

            // Pass all data to Step 4
            controller.setFreelancerData(
                    freelancerData,
                    hourlyRate,
                    skills,
                    bio,
                    experience,
                    savedFilePath
            );

            Stage stage = (Stage) nextButton.getScene().getWindow();
            stage.setScene(new Scene(root, 850, 700));
            stage.setTitle("Portfolio - UniEarn");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showSuccessAlert("Registration Complete!",
                    "Your account has been created successfully!\n\n" +
                            "Our team will verify your student status within 24-48 hours.\n" +
                            "You'll receive an email notification once verified.");
            redirectToLogin();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/signup/freelancer/freelancer-information.fxml"));
            Parent root = loader.load();

            FreelancerSignupController controller = loader.getController();

            // Restore Step 1 data
            controller.setUserData(convertFreelancerToUser(freelancerData));

            // Restore Step 2 data
            controller.restoreFreelancerData(hourlyRate, skills, bio, experience);

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 850, 600));
            stage.setTitle("Freelancer Profile - UniEarn");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Unable to go back to profile page.");
        }
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/login/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) nextButton.getScene().getWindow();
            stage.setScene(new Scene(root, 750, 600));
            stage.setTitle("Login - UniEarn");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showInfoAlert("Registration Complete",
                    "Your account has been created successfully!\n" +
                            "Please restart the application and log in with your credentials.");
            ((Stage) nextButton.getScene().getWindow()).close();
        }
    }

//    convert Freelancer back to User for Step 1 restoration
    private User convertFreelancerToUser(Freelancer freelancer) {
        User user = new User();
        user.setIdUser(freelancer.getIdUser());
        user.setName(freelancer.getName());
        user.setEmail(freelancer.getEmail());
        user.setPassword(freelancer.getPassword());
        user.setRole(freelancer.getRole());
        return user;
    }

    private void showVerificationStatus(String message, boolean success) {
        verificationStatusLabel.setText(message);
        verificationStatusLabel.setStyle(success ?
                "-fx-text-fill: #28a745; -fx-font-weight: bold;" :
                "-fx-text-fill: #dc3545; -fx-font-weight: bold;");
        verificationStatusLabel.setVisible(true);
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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