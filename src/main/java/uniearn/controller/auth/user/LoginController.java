package uniearn.controller.auth.user;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import uniearn.controller.profile.admin.AdminDashboardController;
import uniearn.controller.profile.client.ClientProfileController;
import uniearn.controller.profile.freelancer.FreelancerProfileController;
import uniearn.database.SessionManager;
import uniearn.model.entities.users.User;
import uniearn.model.entities.users.admin.Admin;
import uniearn.model.entities.users.client.Client;
import uniearn.model.entities.users.freelancer.Freelancer;
import uniearn.server.security.SecurityCallbackServer;
import uniearn.services.users.UserService;
import uniearn.services.users.admin.AdminService;
import uniearn.services.users.client.ClientService;
import uniearn.services.users.freelancer.FreelancerService;
import uniearn.services.users.mail.EmailService;
import uniearn.services.users.security.LoginAttemptService;
import uniearn.services.users.security.WebcamCaptureService;
import uniearn.utils.user.PasswordUtil;

public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Button googleLoginButton;
    @FXML
    private Hyperlink forgotPasswordLink;
    @FXML
    private Label emailError;
    @FXML
    private Label passwordError;
    @FXML
    private Label googleStatusLabel;

    private final UserService userService = new UserService();
    private final ClientService clientService = new ClientService();
    private final FreelancerService freelancerService = new FreelancerService();
    private final EmailService emailService = new EmailService();
    private final GoogleAuthHandler googleAuthHandler = new GoogleAuthHandler();
    private final AdminService adminService = new AdminService();

    private static final String MAIN_WINDOW_POLICY_KEY = "uniearn.main_window_policy";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @FXML
    public void initialize() {
        SecurityCallbackServer.start(
                this::openForgotPassword,
                email -> showError(passwordError,
                        "\uD83D\uDD12 Compte verrouillé via email. Réessayez dans "
                                + LoginAttemptService.remainingLockTime(email) + "."));

        emailField.focusedProperty().addListener((o, ov, nv) -> {
            if (!nv)
                validateEmail();
        });
        passwordField.focusedProperty().addListener((o, ov, nv) -> {
            if (!nv)
                validatePassword();
        });
    }

    // ── Standard login ────────────────────────────────────────────────────

    private boolean validateEmail() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showError(emailError, "Email requis");
            return false;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError(emailError, "Email invalide");
            return false;
        }
        hideError(emailError);
        return true;
    }

    private boolean validatePassword() {
        if (passwordField.getText().isEmpty()) {
            showError(passwordError, "Mot de passe requis");
            return false;
        }
        hideError(passwordError);
        return true;
    }

    @FXML
    private void handleLogin() {
        clearAllErrors();
        if (validateEmail() && validatePassword())
            authenticateUser();
    }

    private void authenticateUser() {
        loginButton.setDisable(true);
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        try {
            if (LoginAttemptService.isLocked(email)) {
                showError(passwordError, "⏳ Compte verrouillé. Réessayez dans "
                        + LoginAttemptService.remainingLockTime(email) + ".");
                loginButton.setDisable(false);
                return;
            }

            User user = userService.getAllUsersIncludingAdmins().stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(email))
                    .findFirst().orElse(null);

            if (user == null) {
                showError(emailError, "Aucun compte trouvé avec cet email");
                loginButton.setDisable(false);
                return;
            }
            if (!user.isActivated()) {
                showErrorAlert("Compte désactivé", "Votre compte a été désactivé.\nContactez le support.");
                loginButton.setDisable(false);
                return;
            }
            if (!PasswordUtil.verifyPassword(password, user.getPassword())) {
                handleWrongPassword(email, user);
                loginButton.setDisable(false);
                return;
            }

            LoginAttemptService.reset(email);
            SessionManager.getInstance().setCurrentUser(user);
            System.out.println("✓ Login: " + user.getName());
            redirectToProfile(user);

        } catch (Exception e) {
            showErrorAlert("Erreur", e.getMessage());
            e.printStackTrace();
            loginButton.setDisable(false);
        }
    }

    private void handleWrongPassword(String email, User user) {
        int fails = LoginAttemptService.recordFailure(email);
        int remaining = LoginAttemptService.maxAttempts() - fails;

        if (fails < LoginAttemptService.maxAttempts()) {
            showError(passwordError, "Mot de passe incorrect. " + remaining + " tentative(s) restante(s).");
            return;
        }

        showError(passwordError, "⛔ Compte verrouillé " + LoginAttemptService.lockMinutes()
                + " min. Un email vous a été envoyé.");

        final String ownerName = user.getName();
        final String ip = LoginAttemptService.detectLocalIp();
        Thread t = new Thread(() -> {
            try {
                File photo = WebcamCaptureService.capture(email);
                String[] tokens = SecurityCallbackServer.generateTokens(email);
                emailService.sendIntruderAlert(email, ownerName, ip, photo, tokens[0], tokens[1]);
                System.out.println("✓ Security alert sent to " + email);
            } catch (Exception ex) {
                System.err.println("⚠ Alert failed: " + ex.getMessage());
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // ── Google Login ──────────────────────────────────────────────────────

    @FXML
    private void handleGoogleLogin() {
        setGoogleButtonState(true, "Connexion Google…");

        googleAuthHandler.handleGoogleLogin(
                // Existing user → go straight to their profile
                user -> {
                    setGoogleButtonState(false, null);
                    redirectToProfile(user);
                },
                // New user → send to signup to pick role + fill details
                partialUser -> {
                    setGoogleButtonState(false, null);
                    openSignupWithGoogleData(partialUser);
                },
                // Error
                errorMsg -> {
                    setGoogleButtonState(false, null);
                    showErrorAlert("Connexion Google échouée", errorMsg);
                });
    }

    // Pre-fills the signup form with Google data -> only pick role
    private void openSignupWithGoogleData(User googleUser) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/auth/signup/signup.fxml"));
            Parent root = loader.load();
            SignupController ctrl = loader.getController();
            ctrl.prefillFromGoogle(googleUser);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 750, 800));
            stage.setTitle("Finaliser l'inscription — UniEarn");
            stage.centerOnScreen();
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir la page d'inscription.");
            e.printStackTrace();
        }
    }

    private void setGoogleButtonState(boolean loading, String label) {
        Platform.runLater(() -> {
            if (googleLoginButton != null)
                googleLoginButton.setDisable(loading);
            if (googleStatusLabel != null) {
                if (label != null) {
                    googleStatusLabel.setText(label);
                    googleStatusLabel.setVisible(true);
                } else {
                    googleStatusLabel.setVisible(false);
                }
            }
        });
    }

    // ── Profile routing ───────────────────────────────────────────────────

    private void redirectToProfile(User user) {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            ensureMainWindowPolicy(stage);

            switch (user.getRole()) {
                case CLIENT -> redirectToClientProfile(user, stage);
                case FREELANCER -> redirectToFreelancerProfile(user, stage);
                case ADMIN -> redirectToAdminDashboard(user, stage);
                default -> {
                    showErrorAlert("Rôle inconnu", "Dashboard introuvable.");
                    loginButton.setDisable(false);
                }
            }
        } catch (IOException e) {
            showErrorAlert("Erreur de navigation", e.getMessage());
            loginButton.setDisable(false);
        }
    }

    private void redirectToClientProfile(User user, Stage stage) throws IOException {
        Client c = clientService.getClientById(user.getIdUser());
        if (c == null) {
            showErrorAlert("Erreur", "Profil client introuvable.");
            loginButton.setDisable(false);
            return;
        }
        FXMLLoader l = new FXMLLoader(getClass().getResource("/profile/client/client-profile.fxml"));
        Parent root = l.load();
        ((ClientProfileController) l.getController()).setClientData(c);
        stage.setScene(new Scene(root, 1200, 800));
        stage.setTitle("Client — UniEarn");
        stage.centerOnScreen();
    }

    private void redirectToFreelancerProfile(User user, Stage stage) throws IOException {
        Freelancer f = freelancerService.getFreelancerById(user.getIdUser());
        if (f == null) {
            showErrorAlert("Erreur", "Profil freelancer introuvable.");
            loginButton.setDisable(false);
            return;
        }
        FXMLLoader l = new FXMLLoader(getClass().getResource("/profile/freelancer/freelancer-profile.fxml"));
        Parent root = l.load();
        ((FreelancerProfileController) l.getController()).setFreelancerData(f);
        stage.setScene(new Scene(root, 1200, 800));
        stage.setTitle("Freelancer — UniEarn");
        stage.centerOnScreen();
    }

    private void redirectToAdminDashboard(User user, Stage stage) throws IOException {
        System.out.println("Attempting to load admin dashboard for user ID: " + user.getIdUser());

        Admin admin = adminService.getAdminById(user.getIdUser());

        if (admin == null) {
            System.err.println("Admin data is null for user ID: " + user.getIdUser());
            showErrorAlert("Error", "Unable to load admin data from database.");
            loginButton.setDisable(false);
            return;
        }

        System.out.println("✓ Admin data loaded: " + admin.getName());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/admin/admin-dashboard.fxml"));
        Parent root = loader.load();

        AdminDashboardController controller = loader.getController();
        controller.setAdminData(admin);

        stage.setScene(new Scene(root, 1200, 700));
        stage.setTitle("Admin Dashboard - UniEarn");
        stage.centerOnScreen();

        System.out.println("✓ Redirected to Admin Dashboard successfully");
    }

    // ── Navigation ────────────────────────────────────────────────────────

    private void openForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/login/forgot-password.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 750, 550));
            stage.setTitle("Réinitialisation — UniEarn");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSignupRedirect() {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(
                    new FXMLLoader(getClass().getResource("/auth/signup/signup.fxml")).load(), 750, 800));
            stage.setTitle("Inscription — UniEarn");
            stage.centerOnScreen();
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir l'inscription.");
        }
    }

    @FXML
    private void handleForgotPassword() {
        openForgotPassword();
    }

    // ── UI helpers ────────────────────────────────────────────────────────

    private void showError(Label l, String msg) {
        l.setText(msg);
        l.setVisible(true);
    }

    private void hideError(Label l) {
        l.setText("");
        l.setVisible(false);
    }

    private void clearAllErrors() {
        hideError(emailError);
        hideError(passwordError);
    }

    private void showErrorAlert(String title, String msg) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle(title);
            a.setHeaderText(null);
            a.setContentText(msg);
            a.showAndWait();
        });
    }

    private void ensureMainWindowPolicy(Stage stage) {
        if (stage.getProperties().containsKey(MAIN_WINDOW_POLICY_KEY)) {
            return;
        }
        stage.getProperties().put(MAIN_WINDOW_POLICY_KEY, true);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setResizable(true);
    }
}
