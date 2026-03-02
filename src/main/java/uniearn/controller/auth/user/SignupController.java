package uniearn.controller.auth.user;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import uniearn.controller.auth.client.ClientSignupController;
import uniearn.controller.auth.freelancer.FreelancerSignupController;
import uniearn.model.entities.users.User;
import uniearn.model.entities.users.admin.Admin;
import uniearn.model.enums.UserRole;
import uniearn.services.users.UserService;
import uniearn.services.users.admin.AdminService;
import uniearn.utils.user.PasswordUtil;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;


// - Step 1 : Basic User Signup

public class SignupController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordFieldVisible;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordFieldVisible;
    @FXML private ComboBox<UserRole> roleComboBox;
    @FXML private Button signupButton;
    @FXML private Button generatePasswordButton;
    @FXML private Button togglePasswordButton;
    @FXML private Button toggleConfirmPasswordButton;
    @FXML private Label nameError;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;
    @FXML private Label roleError;
    @FXML private CheckBox termsCheckbox;
    @FXML private Label termsError;
    @FXML private ImageView profileImageView;
    @FXML private Button    googleSignupButton;


    private final UserService userService = new UserService();
    private final AdminService adminService = new AdminService();
    private final GoogleAuthHandler googleAuthHandler = new GoogleAuthHandler();
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private boolean isGoogleSignup = false;
    private File selectedProfilePhoto = null;


    @FXML
    public void initialize() {
        setupRoleComboBox();
        setupPasswordFieldSync();

        nameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validateName();
        });
        emailField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validateEmail();
        });
        passwordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !isGoogleSignup) validatePassword();
        });
        confirmPasswordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !isGoogleSignup) validateConfirmPassword();
        });
        roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) hideError(roleError);
        });
        termsCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) hideError(termsError);
        });

        passwordFieldVisible.setVisible(false);
        passwordFieldVisible.setManaged(false);
        confirmPasswordFieldVisible.setVisible(false);
        confirmPasswordFieldVisible.setManaged(false);
    }

    private void setupPasswordFieldSync() {
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!isPasswordVisible) {
                passwordFieldVisible.setText(newVal);
            }
        });
        passwordFieldVisible.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isPasswordVisible) {
                passwordField.setText(newVal);
            }
        });

        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!isConfirmPasswordVisible) {
                confirmPasswordFieldVisible.setText(newVal);
            }
        });
        confirmPasswordFieldVisible.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isConfirmPasswordVisible) {
                confirmPasswordField.setText(newVal);
            }
        });
    }

    // ── Terms & Conditions ────────────────────────────────────────────────
    @FXML
    public void handleViewTerms() {
        try {

            java.net.URL termsUrl = getClass().getResource("/auth/signup/terms-and-conditions.fxml");
            if (termsUrl == null) {
                termsUrl = getClass().getResource("/auth/terms-and-conditions.fxml");
            }
            if (termsUrl == null) {
                showErrorAlert("File Not Found",
                        "Could not locate terms-and-conditions.fxml.\n" +
                                "Place it at: resources/auth/signup/terms-and-conditions.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(termsUrl);
            Parent root = loader.load();

            TermsController tc = loader.getController();
            tc.setOpenedFromSignup(true, this);

            tc.setFormSnapshot(
                    nameField.getText(),
                    emailField.getText(),
                    isPasswordVisible ? passwordFieldVisible.getText() : passwordField.getText(),
                    roleComboBox.getValue(),
                    selectedProfilePhoto
            );

            Stage stage = (Stage) signupButton.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 700));
            stage.setTitle("Terms & Conditions - UniEarn");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Unable to open Terms & Conditions.");
        }
    }


    public void restoreAfterTerms(boolean accepted,
                                  String name,
                                  String email,
                                  String password,
                                  UserRole role,
                                  File profilePhoto) {
        if (name != null)     nameField.setText(name);
        if (email != null)    emailField.setText(email);
        if (password != null) {
            passwordField.setText(password);
            confirmPasswordField.setText(password);
            passwordFieldVisible.setText(password);
            confirmPasswordFieldVisible.setText(password);
        }
        if (role != null)     roleComboBox.setValue(role);
        if (profilePhoto != null) {
            selectedProfilePhoto = profilePhoto;
            try {
                profileImageView.setImage(new Image(profilePhoto.toURI().toString()));
            } catch (Exception ignored) {}
        }

        termsCheckbox.setSelected(accepted);
        if (accepted) hideError(termsError);
    }

    // ── Password helpers ──────────────────────────────────────────────────

    @FXML
    private void handleGeneratePassword() {
        try {
            String randomPassword = PasswordUtil.generateRandomPassword(12);

            passwordField.setText(randomPassword);
            passwordFieldVisible.setText(randomPassword);
            confirmPasswordField.setText(randomPassword);
            confirmPasswordFieldVisible.setText(randomPassword);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Password Generated");
            alert.setHeaderText("Secure Password Created");
            alert.setContentText("A strong password has been generated and filled in.\n\n" +
                    "💡 Tip: Click the 👁 button to view your password before signing up.\n\n" +
                    "⚠ Make sure to save this password somewhere safe!");
            alert.showAndWait();

            validatePassword();
            validateConfirmPassword();

            System.out.println("✓ Random password generated and set");

        } catch (Exception e) {
            showErrorAlert("Error", "Failed to generate password: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTogglePassword() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            passwordFieldVisible.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordFieldVisible.setVisible(true);
            passwordFieldVisible.setManaged(true);
            togglePasswordButton.setText("🙈");
        } else {
            passwordField.setText(passwordFieldVisible.getText());
            passwordFieldVisible.setVisible(false);
            passwordFieldVisible.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            togglePasswordButton.setText("👁");
        }
    }

    @FXML
    private void handleToggleConfirmPassword() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;

        if (isConfirmPasswordVisible) {
            confirmPasswordFieldVisible.setText(confirmPasswordField.getText());
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            confirmPasswordFieldVisible.setVisible(true);
            confirmPasswordFieldVisible.setManaged(true);
            toggleConfirmPasswordButton.setText("🙈");
        } else {
            confirmPasswordField.setText(confirmPasswordFieldVisible.getText());
            confirmPasswordFieldVisible.setVisible(false);
            confirmPasswordFieldVisible.setManaged(false);
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            toggleConfirmPasswordButton.setText("👁");
        }
    }

    // ── Validation ────────────────────────────────────────────────────────

    private boolean validateTerms() {
        if (!termsCheckbox.isSelected()) {
            showError(termsError, "You must accept the terms to continue");
            return false;
        }
        hideError(termsError);
        return true;
    }

    public void restoreUserData(User userData) {
        nameField.setText(userData.getName());
        emailField.setText(userData.getEmail());
        passwordField.setText(userData.getPassword());
        confirmPasswordField.setText(userData.getPassword());
        roleComboBox.setValue(userData.getRole());
        signupButton.setText("Continue");
        termsCheckbox.setSelected(true);
    }


    public void prefillFromGoogle(User googleUser) {
        isGoogleSignup = true;

        // Fill and lock name + email — from Google
        nameField.setText(googleUser.getName());
        nameField.setEditable(false);
        nameField.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;");

        emailField.setText(googleUser.getEmail());
        emailField.setEditable(false);
        emailField.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;");

        //hide password
        javafx.application.Platform.runLater(() -> {
            hideNodeById("passwordSection");
            hideNodeById("confirmPasswordSection");
        });

        //hide field nodes directly
        hideNode(passwordField);
        hideNode(passwordFieldVisible);
        hideNode(confirmPasswordField);
        hideNode(confirmPasswordFieldVisible);
        hideNode(generatePasswordButton);
        hideNode(togglePasswordButton);
        hideNode(toggleConfirmPasswordButton);

        // Clear any stale validation errors
        if (passwordError != null)        { passwordError.setVisible(false); passwordError.setText(""); }
        if (confirmPasswordError != null) { confirmPasswordError.setVisible(false); confirmPasswordError.setText(""); }

        // Accept terms automatically
        termsCheckbox.setSelected(true);

        // Show Google avatar if downloaded
        if (googleUser.getProfilePicturePath() != null) {
            try {
                java.io.File avatarFile = new java.io.File(googleUser.getProfilePicturePath());
                if (avatarFile.exists()) {
                    profileImageView.setImage(new javafx.scene.image.Image(avatarFile.toURI().toString()));
                    selectedProfilePhoto = avatarFile;
                }
            } catch (Exception e) {
                System.out.println("⚠ Could not load Google avatar: " + e.getMessage());
            }
        }

        // Store the OAuth token + hashed by userService.
        // Google users log in via OAuth, not this value.
        passwordField.setText(googleUser.getPassword());
        confirmPasswordField.setText(googleUser.getPassword());

        signupButton.setText("Continue with Google →");
        System.out.println("✓ Signup form pre-filled from Google: " + googleUser.getEmail());
    }

    //hide node
    private void hideNodeById(String fxId) {
        if (signupButton == null || signupButton.getScene() == null) return;
        javafx.scene.Node node = signupButton.getScene().lookup("#" + fxId);
        if (node != null) { node.setVisible(false); node.setManaged(false); }
    }
    private void hideNode(javafx.scene.Node node) {
        if (node != null) { node.setVisible(false); node.setManaged(false); }
    }

    private void setupRoleComboBox() {
        roleComboBox.getItems().addAll(UserRole.values());
        roleComboBox.setConverter(new StringConverter<UserRole>() {
            @Override
            public String toString(UserRole role) {
                if (role == null) return "";
                switch (role) {
                    case ADMIN: return "Administrator";
                    case CLIENT: return "Client";
                    case FREELANCER: return "Freelancer";
                    default: return role.name();
                }
            }
            @Override
            public UserRole fromString(String string) { return null; }
        });
    }

    @FXML
    private void handleSignup() {
        clearAllErrors();
        boolean passwordOk = isGoogleSignup || (validatePassword() && validateConfirmPassword());
        if (validateName() && validateEmail() && passwordOk && validateRole()) {
            createUser();
        }
    }

    private boolean validateName() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) { showError(nameError, "Name is required"); return false; }
        if (name.length() < 2) { showError(nameError, "Name must be at least 2 characters"); return false; }
        if (name.length() > 50) { showError(nameError, "Name must not exceed 50 characters"); return false; }
        if (!name.matches("^[a-zA-Z\\s]+$")) { showError(nameError, "Name can only contain letters and spaces"); return false; }
        hideError(nameError);
        return true;
    }

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

        try {
            if (userService.emailExists(email)) {
                showError(emailError, "This email is already registered. Please use a different email or login.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error checking email uniqueness: " + e.getMessage());
            showError(emailError, "Unable to verify email. Please try again.");
            return false;
        }

        hideError(emailError);
        return true;
    }

    private boolean validatePassword() {
        String password = isPasswordVisible ?
                passwordFieldVisible.getText() : passwordField.getText();

        if (password.isEmpty()) { showError(passwordError, "Password is required"); return false; }
        if (password.length() < 8) { showError(passwordError, "Password must be at least 8 characters"); return false; }
        if (!password.matches(".*[A-Z].*")) { showError(passwordError, "Password must contain at least one uppercase letter"); return false; }
        if (!password.matches(".*[a-z].*")) { showError(passwordError, "Password must contain at least one lowercase letter"); return false; }
        if (!password.matches(".*\\d.*")) { showError(passwordError, "Password must contain at least one number"); return false; }
        hideError(passwordError);
        return true;
    }

    private boolean validateConfirmPassword() {
        String password = isPasswordVisible ?
                passwordFieldVisible.getText() : passwordField.getText();
        String confirmPassword = isConfirmPasswordVisible ?
                confirmPasswordFieldVisible.getText() : confirmPasswordField.getText();

        if (confirmPassword.isEmpty()) { showError(confirmPasswordError, "Please confirm your password"); return false; }
        if (!password.equals(confirmPassword)) { showError(confirmPasswordError, "Passwords do not match"); return false; }
        hideError(confirmPasswordError);
        return true;
    }

    private boolean validateRole() {
        if (roleComboBox.getValue() == null) { showError(roleError, "Please select a role"); return false; }
        hideError(roleError);
        return true;
    }

    // ── User creation ─────────────────────────────────────────────────────

    private void createUser() {
        try {
            signupButton.setDisable(true);

            String password = isPasswordVisible ?
                    passwordFieldVisible.getText() : passwordField.getText();

            UserRole selectedRole = roleComboBox.getValue();

            // Redirect based on role
            if (selectedRole == UserRole.CLIENT) {
                User newUser = buildBaseUser(password);
                redirectToClientSignup(newUser);
                return;

            } else if (selectedRole == UserRole.FREELANCER) {
                User newUser = buildBaseUser(password);
                redirectToFreelancerSignup(newUser);
                return;

            } else if (selectedRole == UserRole.ADMIN) {
                Admin newAdmin = new Admin();
                newAdmin.setName(nameField.getText().trim());
                newAdmin.setEmail(emailField.getText().trim());
                newAdmin.setPassword(password);
                newAdmin.setRole(UserRole.ADMIN);
                newAdmin.setProfilePicturePath(null);
                newAdmin.setActivated(true);

                int adminId = adminService.addAdmin(newAdmin);
                if (adminId > 0) {
                    showSuccessAlert("Account Created!", "Welcome " + newAdmin.getName() + "!");
                    redirectToLogin();
                } else {
                    showErrorAlert("Registration Failed", "Unable to create admin account.");
                    signupButton.setDisable(false);
                }
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry") || e.getMessage().contains("email")) {
                showError(emailError, "This email is already registered");
            } else {
                showErrorAlert("Database Error", "Unable to create account. Please try again.");
            }
            signupButton.setDisable(false);
            e.printStackTrace();
        } catch (Exception e) {
            showErrorAlert("Unexpected Error", "An error occurred. Please try again.");
            signupButton.setDisable(false);
            e.printStackTrace();
        }
    }


    private User buildBaseUser(String password) {
        User user = new User();
        user.setName(nameField.getText().trim());
        user.setEmail(emailField.getText().trim());
        user.setPassword(password);
        user.setRole(roleComboBox.getValue());

        if (selectedProfilePhoto != null) {
            user.setProfilePicturePath(selectedProfilePhoto.getAbsolutePath());
        }
        return user;
    }


    @FXML
    private void handleChangePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selected = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());

        if (selected != null) {
            selectedProfilePhoto = selected;

            Image preview = new Image(selected.toURI().toString());
            profileImageView.setImage(preview);
            System.out.println("✓ Profile photo selected (preview only): " + selected.getAbsolutePath());
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────

    private void redirectToClientSignup(User userData) {
        try {
            if (!validateTerms()) {
                signupButton.setDisable(false);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/signup/client/client-signup.fxml"));
            Parent root = loader.load();

            ClientSignupController controller = loader.getController();
            controller.setUserData(userData);

            Stage stage = (Stage) signupButton.getScene().getWindow();
            stage.setScene(new Scene(root, 850, 700));
            stage.setTitle("Complete Your Profile - UniEarn");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Unable to load client signup page.");
            signupButton.setDisable(false);
        }
    }

    private void redirectToFreelancerSignup(User userData) {
        try {
            if (!validateTerms()) {
                signupButton.setDisable(false);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/signup/freelancer/freelancer-information.fxml"));
            Parent root = loader.load();

            FreelancerSignupController controller = loader.getController();
            controller.setUserData(userData);

            Stage stage = (Stage) signupButton.getScene().getWindow();
            stage.setScene(new Scene(root, 850, 800));
            stage.setTitle("Freelancer Profile - UniEarn");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Unable to load freelancer signup page.");
            signupButton.setDisable(false);
        }
    }

    // ── Google Signup ─────────────────────────────────────────────────────

    @FXML
    private void handleGoogleSignup() {
        if (googleSignupButton != null) {
            googleSignupButton.setDisable(true);
            googleSignupButton.setText("🔵  Connecting to Google…");
        }

        googleAuthHandler.handleGoogleLogin(
                // Existing user → just redirect to their profile (they already have an account)
                user -> {
                    if (googleSignupButton != null) {
                        googleSignupButton.setDisable(false);
                        googleSignupButton.setText("🔵  Sign up with Google");
                    }
                    showSuccessAlert("Existing account",
                            "You already have a UniEarn account with this Google address.\n"
                                    + "You will be redirected to your profile.");
                    // Navigate to login
                    redirectToLogin();
                },
                // New user → pre-fill the current signup form
                partialUser -> {
                    if (googleSignupButton != null) {
                        googleSignupButton.setDisable(false);
                        googleSignupButton.setText("🔵  Sign up with Google");
                    }
                    prefillFromGoogle(partialUser);
                },
                errorMsg -> {
                    if (googleSignupButton != null) {
                        googleSignupButton.setDisable(false);
                        googleSignupButton.setText("🔵  Sign up with Google");
                    }
                    showErrorAlert("Google connection failed", errorMsg);
                }
        );
    }

    @FXML
    private void handleLoginRedirect() { redirectToLogin(); }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/login/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) signupButton.getScene().getWindow();
            stage.setScene(new Scene(root, 750, 600));
            stage.setTitle("Login - UniEarn");
            stage.centerOnScreen();
        } catch (IOException | NullPointerException e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Complete");
            alert.setContentText("Account created. Please restart to login.");
            alert.showAndWait();
            ((Stage) signupButton.getScene().getWindow()).close();
        }
    }

    // ── Alert / error helpers ─────────────────────────────────────────────

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void hideError(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setText("");
    }

    private void clearAllErrors() {
        hideError(nameError);
        hideError(emailError);
        hideError(passwordError);
        hideError(confirmPasswordError);
        hideError(roleError);
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