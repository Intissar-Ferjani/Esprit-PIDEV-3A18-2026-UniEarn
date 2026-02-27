package uniearn.controller.auth.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import uniearn.controller.auth.user.SignupController;
import uniearn.model.entities.users.client.Client;
import uniearn.model.entities.users.User;
import uniearn.model.enums.UserRole;
import uniearn.services.users.client.ClientService;

import java.io.IOException;

public class ClientSignupController {

    @FXML private TextField amountField;
    @FXML private TextField companyField;
    @FXML private ComboBox<String> industryComboBox;
    @FXML private Button backButton;
    @FXML private Button completeButton;
    @FXML private Label amountError;
    @FXML private Label companyError;
    @FXML private Label industryError;
    @FXML private Label termsError;

    private final ClientService clientService = new ClientService();
    SignupController signupController = new SignupController();

    private User basicUserData;

    @FXML
    public void initialize() {
        setupIndustryComboBox();

        if (companyField != null) {
            companyField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) validateCompany();
            });
        }

        if (amountField != null) {
            amountField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) validateAmount();
            });

            amountField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("\\d*(\\.\\d{0,2})?")) {
                    amountField.setText(oldVal);
                }
            });
        }

        if (industryComboBox != null) {
            industryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) hideError(industryError);
            });
        }
    }

    private void setupIndustryComboBox() {
        if (industryComboBox != null) {
            industryComboBox.getItems().addAll(
                    "Technology & IT",
                    "Marketing & Advertising",
                    "Design & Creative",
                    "Writing & Content",
                    "Business & Consulting",
                    "Education & Training",
                    "Healthcare",
                    "Finance & Accounting",
                    "Legal Services",
                    "Real Estate",
                    "E-commerce & Retail",
                    "Manufacturing",
                    "Hospitality & Tourism",
                    "Construction",
                    "Other"
            );
        }
    }

    public void setUserData(User user) {
        this.basicUserData = user;
    }

    public void restoreClientData(String amount, String company, String industry) {
        if (amount != null && amountField != null) amountField.setText(amount);
        if (company != null && companyField != null) companyField.setText(company);
        if (industry != null && industryComboBox != null) industryComboBox.setValue(industry);
    }

    @FXML
    private void handleComplete() {
        clearAllErrors();

        boolean isCompanyValid = validateCompany();
        boolean isAmountValid = validateAmount();
        boolean isIndustryValid = validateIndustry();

        if (isCompanyValid && isAmountValid && isIndustryValid) {
            createClient();
        }
    }

    private boolean validateCompany() {
        if (companyField == null) return true;

        String company = companyField.getText().trim();

        // Company is REQUIRED
        if (company.isEmpty()) {
            showError(companyError, "Company name is required");
            return false;
        }

        // Check minimum length
        if (company.length() < 2) {
            showError(companyError, "Company name must be at least 2 characters");
            return false;
        }

        // Check maximum length
        if (company.length() > 100) {
            showError(companyError, "Company name must not exceed 100 characters");
            return false;
        }

        // ✅ FIX: Proper error handling for company uniqueness check
        try {
            boolean exists = clientService.companyExists(company);
            if (exists) {
                showError(companyError, "Company name already in use. Please use a different name.");
                return false;
            }
        } catch (Exception e) {
            // Log the error but don't show alert - just show inline error
            System.err.println("Error checking company uniqueness: " + e.getMessage());
            e.printStackTrace();

            // Show user-friendly error in the label, not an alert
            showError(companyError, "Unable to verify company name. Please try again.");
            return false;
        }

        hideError(companyError);
        return true;
    }

    private boolean validateAmount() {
        if (amountField == null) return true;

        String amountText = amountField.getText().trim();

        if (!amountText.isEmpty()) {
            try {
                double amount = Double.parseDouble(amountText);
                if (amount < 0) {
                    showError(amountError, "Amount cannot be negative");
                    return false;
                }
                if (amount > 1000000) {
                    showError(amountError, "Amount seems unusually high. Please verify.");
                    return false;
                }
            } catch (NumberFormatException e) {
                showError(amountError, "Please enter a valid number");
                return false;
            }
        }

        hideError(amountError);
        return true;
    }

    private boolean validateIndustry() {
        if (industryComboBox == null) return true;

        if (industryComboBox.getValue() == null) {
            showError(industryError, "Please select an industry");
            return false;
        }

        hideError(industryError);
        return true;
    }

    private void createClient() {
        try {
            if (completeButton != null) {
                completeButton.setDisable(true);
            }

            Client client = new Client();

            client.setName(basicUserData.getName());
            client.setEmail(basicUserData.getEmail());
            client.setPassword(basicUserData.getPassword());
            client.setRole(UserRole.CLIENT);

            String amountText = amountField != null ? amountField.getText().trim() : "";
            double amount = amountText.isEmpty() ? 0.0 : Double.parseDouble(amountText);
            client.setAmount(amount);

            String company = companyField != null ? companyField.getText().trim() : "";
            client.setCompany(company);

            String industry = industryComboBox != null ? industryComboBox.getValue() : "Other";
            client.setIndustry(industry);

            client.setRating(0.0);

            clientService.addClient(client);

            showSuccessAlert("Welcome to UniEarn!",
                    "Your client account has been created successfully!\n\n" +
                            "Company: " + company + "\n" +
                            "Industry: " + industry + "\n" +
                            (amountText.isEmpty() ? "" : "Initial Budget: " + amountText + " TND\n") +
                            "\nYou can now start posting projects and hiring freelancers.");

            redirectToLogin();

        } catch (Exception e) {
            showErrorAlert("Unexpected Error",
                    "An error occurred: " + e.getMessage());
            e.printStackTrace();
            if (completeButton != null) {
                completeButton.setDisable(false);
            }
        }
    }

    @FXML
    private void handleBack() {
        String currentAmount = amountField != null ? amountField.getText() : "";
        String currentCompany = companyField != null ? companyField.getText() : "";
        String currentIndustry = industryComboBox != null ? industryComboBox.getValue() : null;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/signup/signup.fxml"));
            Parent signupRoot = loader.load();

            signupController.restoreUserData(basicUserData);

            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene signupScene = new Scene(signupRoot);
            stage.setScene(signupScene);
            stage.setTitle("Sign Up - UniEarn");

        } catch (IOException e) {
            System.err.println("Error loading signup page: " + e.getMessage());
            showErrorAlert("Navigation Error", "Unable to go back to signup page.");
        }
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/login/login.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = (Stage) completeButton.getScene().getWindow();
            Scene loginScene = new Scene(loginRoot);
            stage.setScene(loginScene);
            stage.setTitle("Login - UniEarn");

        } catch (IOException | NullPointerException e) {
            System.out.println("Login page not found. Closing window.");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Complete");
            alert.setHeaderText("Client Account Created!");
            alert.setContentText("Your account has been created successfully. Please restart the application to login.");
            alert.showAndWait();

            if (completeButton != null) {
                Stage stage = (Stage) completeButton.getScene().getWindow();
                stage.close();
            }
        }
    }

    private void showError(Label errorLabel, String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }

    private void hideError(Label errorLabel) {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setText("");
        }
    }

    private void clearAllErrors() {
        hideError(amountError);
        hideError(companyError);
        hideError(industryError);
        hideError(termsError);
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