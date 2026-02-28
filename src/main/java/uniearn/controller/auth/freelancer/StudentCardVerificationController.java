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
import uniearn.services.users.freelancer.ocr.StudentCardOCRService;
import uniearn.services.users.freelancer.FreelancerService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

public class StudentCardVerificationController {

    @FXML private Button uploadCardButton;
    @FXML private Button backButton;
    @FXML private Button nextButton;
    @FXML private Label cardFileLabel;
    @FXML private Label verificationStatusLabel;

    private final FreelancerService freelancerService = new FreelancerService();
    private final StudentCardOCRService ocrService = new StudentCardOCRService();

    private Freelancer freelancerData;
    private File selectedFile;
    private String savedFilePath;
    private boolean cardVerifiedByOCR = false;

    private String hourlyRate;
    private String skills;
    private String bio;
    private String experience;
    private String cvPath;


    @FXML
    public void initialize() {
        nextButton.setDisable(true);
    }

    public void setFreelancerData(Freelancer freelancer, String hourlyRate, String skills, String bio, String experience, String cvPath) {
        this.freelancerData = freelancer;
        this.hourlyRate = hourlyRate;
        this.skills = skills;
        this.bio = bio;
        this.experience = experience;
        this.cvPath = cvPath;
        System.out.println("✓ Verification step initialized for: " + freelancer.getName());
    }

    public void restoreCardPath(String cardPath) {
        if (cardPath != null && !cardPath.isEmpty()) {
            this.savedFilePath = cardPath;
            this.cardVerifiedByOCR = true;

            String fileName = Paths.get(cardPath).getFileName().toString();
            cardFileLabel.setText("✓ " + fileName);
            cardFileLabel.setStyle("-fx-text-fill: #28a745;");

            showVerificationStatus("✅ Previously verified card restored", true);
            nextButton.setDisable(false);
        }
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
            if (!validateFile(selectedFile)) return;

            try {
                savedFilePath = saveStudentCard(selectedFile);

                cardFileLabel.setText("📎 " + selectedFile.getName());
                cardFileLabel.setStyle("-fx-text-fill: #555555;");

                nextButton.setDisable(true);
                uploadCardButton.setDisable(true);
                showVerificationStatus("🔍 Analyzing your student card, please wait...", true);

                Thread ocrThread = new Thread(() -> {
                    StudentCardOCRService.OCRResult result = ocrService.verifyStudentCard(
                            selectedFile,
                            freelancerData.getName()
                    );
                    cardVerifiedByOCR = result.isVerified();

                    javafx.application.Platform.runLater(() -> {
                        uploadCardButton.setDisable(false);

                        if (result.isVerified()) {
                            cardFileLabel.setText("✓ " + selectedFile.getName());
                            cardFileLabel.setStyle("-fx-text-fill: #28a745;");
                            showVerificationStatus("✅ " + result.getMessage(), true);
                            nextButton.setDisable(false);

                            // ── Success popup ──────────────────────────────
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Card Verified");
                            alert.setHeaderText("✅ Student Card Verified Successfully!");
                            alert.showAndWait();

                        } else {
                            showVerificationStatus("❌ " + result.getMessage(), false);
                            nextButton.setDisable(true);
                        }
                    });
                });
                ocrThread.setDaemon(true);
                ocrThread.start();

            } catch (IOException e) {
                showErrorAlert("Upload Failed", "Could not save the file: " + e.getMessage());
                uploadCardButton.setDisable(false);
            }
        }
    }

    private boolean validateFile(File file) {
        long fileSizeInMB = file.length() / (1024 * 1024);
        if (fileSizeInMB > 5) {
            showErrorAlert("File Too Large", "Please select a file smaller than 5MB");
            return false;
        }
        String fileName = file.getName().toLowerCase();
        if (!fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg")
                && !fileName.endsWith(".png") && !fileName.endsWith(".pdf")) {
            showErrorAlert("Invalid File Type", "Please select a JPG, PNG, or PDF file");
            return false;
        }
        return true;
    }

    private String saveStudentCard(File file) throws IOException {
        Path uploadsDir = Paths.get("uploads/student_cards");
        Files.createDirectories(uploadsDir);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName = freelancerData.getIdUser() + "_" + timestamp + "_" + file.getName();
        Path targetPath = uploadsDir.resolve(fileName);
        Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("✓ Student card saved to: " + targetPath);
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

            VerifStatus status = cardVerifiedByOCR ? VerifStatus.verified : VerifStatus.unverified;

            freelancerService.updateVerificationData(
                    freelancerData.getIdUser(),
                    savedFilePath,
                    status
            );

            System.out.println("✓ Verification status set to: " + status.name());
            redirectToPortfolioStep();

        } catch (SQLException e) {
            showErrorAlert("Upload Failed", "Failed to save verification data: " + e.getMessage());
            e.printStackTrace();
            nextButton.setDisable(false);
        }
    }

    private void redirectToPortfolioStep() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/auth/signup/freelancer/portfolio-information.fxml"));
            Parent root = loader.load();

            FreelancerPortfolioController controller = loader.getController();
            controller.setFreelancerData(
                    freelancerData,
                    hourlyRate,
                    skills,
                    bio,
                    experience,
                    savedFilePath,
                    cvPath
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/auth/signup/freelancer/freelancer-information.fxml"));
            Parent root = loader.load();

            FreelancerSignupController controller = loader.getController();
            controller.setUserData(convertFreelancerToUser(freelancerData));
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
            ((Stage) nextButton.getScene().getWindow()).close();
        }
    }

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
        verificationStatusLabel.setStyle(success
                ? "-fx-text-fill: #28a745; -fx-font-weight: bold;"
                : "-fx-text-fill: #e67e22; -fx-font-weight: bold;");
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
}