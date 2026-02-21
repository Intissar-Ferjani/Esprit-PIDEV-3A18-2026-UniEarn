package uniearn.controller.auth.user;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import uniearn.controller.profile.client.ClientProfileController;
import uniearn.controller.profile.freelancer.FreelancerProfileController;
import uniearn.model.entities.users.User;
import uniearn.model.entities.users.client.Client;
import uniearn.model.entities.users.freelancer.Freelancer;
import uniearn.services.users.UserService;
import uniearn.services.users.client.ClientService;
import uniearn.services.users.freelancer.FreelancerService;
import uniearn.database.SessionManager;
import uniearn.utils.PasswordUtil;

import java.io.IOException;
import java.util.regex.Pattern;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Label emailError;
    @FXML private Label passwordError;

    private final UserService userService = new UserService();
    private final ClientService clientService = new ClientService();
    private final FreelancerService freelancerService = new FreelancerService();

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    @FXML
    public void initialize() {
        emailField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validateEmail();
        });

        passwordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validatePassword();
        });
    }

    //    ----------------------------------------------------------------------
//    --- Login ---
    private boolean validateEmail() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showError(emailError, "Email is required");
            return false;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError(emailError, "Please enter a valid email address");
            return false;
        }

        hideError(emailError);
        return true;
    }

    private boolean validatePassword() {
        String password = passwordField.getText();

        if (password.isEmpty()) {
            showError(passwordError, "Password is required");
            return false;
        }

        hideError(passwordError);
        return true;
    }

    @FXML
    private void handleLogin() {
        clearAllErrors();

        if (validateEmail() && validatePassword()) {
            authenticateUser();
        }
    }

    private void authenticateUser() {
        try {
            loginButton.setDisable(true);

            String email = emailField.getText().trim();
            String password = passwordField.getText();

            User foundUser = userService.getAllUsersIncludingAdmins().stream()
                    .filter(user -> user.getEmail().equalsIgnoreCase(email))
                    .findFirst()
                    .orElse(null);

//            User not found
            if (foundUser == null) {
                showError(emailError, "No account found with this email");
                loginButton.setDisable(false);
                return;
            }

//            Account not activated
            if (!foundUser.isActivated()) {
                showErrorAlert("Account Deactivated",
                        "Your account has been deactivated.\n\n" +
                                "Please contact support to reactivate your account.");
                loginButton.setDisable(false);
                return;
            }

//          verify password using BCrypt
            if (!PasswordUtil.verifyPassword(password, foundUser.getPassword())) {
                showError(passwordError, "Incorrect password");
                loginButton.setDisable(false);
                return;
            }

//            Store user in session if user found
            SessionManager.getInstance().setCurrentUser(foundUser);

            System.out.println("✓ Login successful: " + foundUser.getName() + " (" + foundUser.getRole() + ")");
            redirectToProfile(foundUser);

        } catch (Exception e) {
            showErrorAlert("Login Error", "An error occurred during login: " + e.getMessage());
            e.printStackTrace();
            loginButton.setDisable(false);
        }
    }

    private void redirectToProfile(User user) {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();

            switch (user.getRole()) {
                case CLIENT:
                    redirectToClientProfile(user, stage);
                    break;

                case FREELANCER:
                    redirectToFreelancerProfile(user, stage);
                    break;

                case ADMIN:
                    redirectToAdminDashboard(user, stage);
                    break;

                default:
                    showErrorAlert("Unknown Role", "Unable to determine user dashboard.");
                    loginButton.setDisable(false);
            }

        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Unable to load dashboard: " + e.getMessage());
            e.printStackTrace();
            loginButton.setDisable(false);
        }
    }

    private void redirectToClientProfile(User user, Stage stage) throws IOException {
        System.out.println("Attempting to load client profile for user ID: " + user.getIdUser());

        Client client = clientService.getClientById(user.getIdUser());

//        Client not found
        if (client == null) {
            System.err.println("Client data is null for user ID: " + user.getIdUser());
            showErrorAlert("Error", "Unable to load client data from database.");
            loginButton.setDisable(false);
            return;
        }

        System.out.println("✓ Client data loaded: " + client.getName());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/client/client-profile.fxml"));
        Parent root = loader.load();

        ClientProfileController controller = loader.getController();
        controller.setClientData(client);

        stage.setScene(new Scene(root, 1200, 800));
        stage.setTitle("Client Profile - UniEarn");
        stage.centerOnScreen();

        System.out.println("✓ Redirected to Client Profile successfully");
    }

    private void redirectToFreelancerProfile(User user, Stage stage) throws IOException {
        System.out.println("Attempting to load freelancer profile for user ID: " + user.getIdUser());

        Freelancer freelancer = freelancerService.getFreelancerById(user.getIdUser());

//        Freelancer not found
        if (freelancer == null) {
            System.err.println("Freelancer data is null for user ID: " + user.getIdUser());
            showErrorAlert("Error", "Unable to load freelancer profile.");
            loginButton.setDisable(false);
            return;
        }

        System.out.println("✓ Freelancer data loaded: " + freelancer.getName());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/freelancer/freelancer-profile.fxml"));
        Parent root = loader.load();

        FreelancerProfileController controller = loader.getController();
        controller.setFreelancerData(freelancer);

        stage.setScene(new Scene(root, 1200, 800));
        stage.setTitle("Freelancer Profile - UniEarn");
        stage.centerOnScreen();

        System.out.println("✓ Redirected to Freelancer Profile successfully");
    }

    private void redirectToAdminDashboard(User user, Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/admin/admin-dashboard.fxml"));
        Parent root = loader.load();

        stage.setScene(new Scene(root, 1200, 700));
        stage.setTitle("Admin Dashboard - UniEarn");
        stage.centerOnScreen();

        System.out.println("✓ Redirected to Admin Dashboard");
    }


    //    ----------------------------------------------------------------------
//    --- Signup ---
    @FXML
    private void handleSignupRedirect() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/signup/signup.fxml"));
            Parent signupRoot = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(signupRoot, 750, 800));
            stage.setTitle("Sign Up - UniEarn");
            stage.centerOnScreen();

        } catch (IOException e) {
            System.err.println("Error loading signup page: " + e.getMessage());
            showErrorAlert("Navigation Error", "Unable to load signup page.");
        }
    }

    //    ----------------------------------------------------------------------
//    --- Forget pass ---
    @FXML
    private void handleForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/login/forgot-password.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 750, 550));
            stage.setTitle("Forgot Password - UniEarn");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
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
        hideError(emailError);
        hideError(passwordError);
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}