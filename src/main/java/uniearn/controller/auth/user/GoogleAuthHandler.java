package uniearn.controller.auth.user;

import javafx.application.Platform;
import uniearn.model.entities.users.User;
import uniearn.services.users.UserService;
import uniearn.database.SessionManager;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;


public class GoogleAuthHandler {

    private final UserService userService = new UserService();

    public void handleGoogleLogin(Consumer<User>   onExistingUser,
                                  Consumer<User>   onNewUser,
                                  Consumer<String> onError) {

        GoogleOAuthService.startOAuthFlow(
                googleUser -> {
                    try {
                        if (googleUser.email == null) {
                            Platform.runLater(() -> onError.accept("Google account has no email address."));
                            return;
                        }

                        User existing = userService.getUserByEmail(googleUser.email);

                        if (existing != null) {
                            // ── Existing user → just log in ───────────────────
                            if (!existing.isActivated()) {
                                Platform.runLater(() -> onError.accept(
                                        "Votre compte a été désactivé. Contactez le support."));
                                return;
                            }
                            SessionManager.getInstance().setCurrentUser(existing);
                            System.out.println("✓ Google login — existing user: " + existing.getName());
                            Platform.runLater(() -> onExistingUser.accept(existing));

                        } else {
                            // ── New user → download avatar + build partial User ─
                            String avatarPath = downloadGoogleAvatar(
                                    googleUser.pictureUrl, googleUser.email);

                            User partial = new User();
                            partial.setName(googleUser.name);
                            partial.setEmail(googleUser.email);

                            // Temporary placeholder — will be replaced when they set a real password
                            partial.setPassword("GOOGLE_OAUTH_" + googleUser.googleId);
                            partial.setProfilePicturePath(avatarPath);
                            partial.setActivated(true);

                            System.out.println("✓ Google OAuth — new user: " + partial.getEmail());
                            Platform.runLater(() -> onNewUser.accept(partial));
                        }

                    } catch (Exception e) {
                        System.err.println("GoogleAuthHandler error: " + e.getMessage());
                        Platform.runLater(() -> onError.accept(
                                "Erreur lors de la connexion Google: " + e.getMessage()));
                    }
                },
                errorMsg -> Platform.runLater(() -> onError.accept(errorMsg))
        );
    }

    // ── Avatar download ───────────────────────────────────────────────────

    //Downloads the Google profile picture to uploads/profiles/google_<safeEmail>.jpg.
    private String downloadGoogleAvatar(String pictureUrl, String email) {
        if (pictureUrl == null || pictureUrl.isBlank()) return null;
        try {
            //request a larger size from Google (default is 96px, s256 = 256px)
            String sizedUrl = pictureUrl.contains("=s")
                    ? pictureUrl.replaceAll("=s\\d+", "=s256")
                    : pictureUrl + "=s256";

            URL url = new URL(sizedUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() != 200) return null;

            Path dir = Paths.get("uploads/profiles");
            Files.createDirectories(dir);

            String safeName = "google_" + email.replaceAll("[^a-zA-Z0-9]", "_") + ".jpg";
            Path dest = dir.resolve(safeName);

            try (InputStream in = conn.getInputStream()) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.println("✓ Google avatar downloaded: " + dest);
            return dest.toString();

        } catch (Exception e) {
            System.err.println("⚠ Could not download Google avatar: " + e.getMessage());
            return null;
        }
    }
}