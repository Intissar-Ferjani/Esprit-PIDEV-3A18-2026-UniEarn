package uniearn.controller.auth.user;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpServer;

public class GoogleOAuthService {

    private static final String CLIENT_ID = null;
    private static final String CLIENT_SECRET = null;
    // ─────────────────────────────────────────────────────────────────────
    private static final int    CALLBACK_PORT  = 8585;
    private static final String REDIRECT_URI   = "http://localhost:" + CALLBACK_PORT + "/oauth/callback";
    private static final String SCOPE          = "openid email profile";

    // ── Public data class ─────────────────────────────────────────────────

    public static class GoogleUser {
        public final String googleId;
        public final String name;
        public final String email;
        public final String pictureUrl;

        public GoogleUser(String googleId, String name, String email, String pictureUrl) {
            this.googleId   = googleId;
            this.name       = name;
            this.email      = email;
            this.pictureUrl = pictureUrl;
        }

        @Override public String toString() {
            return "GoogleUser{name='" + name + "', email='" + email + "'}";
        }
    }

    // ── Main entry point ──────────────────────────────────────────────────

    //Opens the browser for Google login + return result
    public static void startOAuthFlow(Consumer<GoogleUser> onSuccess, Consumer<String> onError) {
        new Thread(() -> {
            try {
                // Step 1 — start local callback server before opening browser
                CountDownLatch latch    = new CountDownLatch(1);
                String[]       codeHolder = new String[1];
                String[]       errorHolder = new String[1];

                HttpServer callbackServer = HttpServer.create(
                        new InetSocketAddress("localhost", CALLBACK_PORT), 0);

                callbackServer.createContext("/oauth/callback", exchange -> {
                    try {
                        String query = exchange.getRequestURI().getQuery();
                        String code  = null;
                        String err   = null;

                        if (query != null) {
                            for (String pair : query.split("&")) {
                                String[] kv = pair.split("=", 2);
                                if (kv.length == 2) {
                                    if ("code".equals(kv[0]))  code = kv[1];
                                    if ("error".equals(kv[0])) err  = kv[1];
                                }
                            }
                        }

                        codeHolder[0]  = code;
                        errorHolder[0] = err;

                        //closing page
                        String html = code != null
                                ? closingPage("✅ Connexion réussie",
                                "Vous pouvez retourner dans l'application UniEarn.", "#16a34a")
                                : closingPage("❌ Connexion annulée",
                                "Vous pouvez fermer cet onglet.", "#dc2626");

                        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
                        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
                        exchange.sendResponseHeaders(200, bytes.length);
                        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }

                    } catch (Exception e) {
                        errorHolder[0] = e.getMessage();
                    } finally {
                        latch.countDown();
                    }
                });

                callbackServer.setExecutor(null);
                callbackServer.start();
                System.out.println("✓ OAuth callback server started on port " + CALLBACK_PORT);

                // Step 2 — open browser to Google consent screen
                String authUrl = buildAuthUrl();
                System.out.println("Opening browser: " + authUrl);
                Desktop.getDesktop().browse(new URI(authUrl));

                // Step 3 — wait up to 3 minutes for the user to log in
                boolean received = latch.await(3, TimeUnit.MINUTES);
                callbackServer.stop(0);

                if (!received) {
                    onError.accept("Timeout: Google login took too long. Please try again.");
                    return;
                }

                if (errorHolder[0] != null) {
                    onError.accept("Google login cancelled: " + errorHolder[0]);
                    return;
                }

                if (codeHolder[0] == null) {
                    onError.accept("No authorization code received from Google.");
                    return;
                }

                // Step 4 — exchange code for access token
                String accessToken = exchangeCodeForToken(codeHolder[0]);
                if (accessToken == null) {
                    onError.accept("Failed to obtain access token from Google.");
                    return;
                }

                // Step 5 — fetch user profile
                GoogleUser googleUser = fetchUserInfo(accessToken);
                if (googleUser == null) {
                    onError.accept("Failed to fetch user profile from Google.");
                    return;
                }

                System.out.println("✓ Google OAuth success: " + googleUser);
                onSuccess.accept(googleUser);

            } catch (Exception e) {
                System.err.println("OAuth error: " + e.getMessage());
                onError.accept("OAuth error: " + e.getMessage());
            }
        }, "google-oauth-thread").start();
    }

    // ── OAuth steps ───────────────────────────────────────────────────────

    private static String buildAuthUrl() throws Exception {
        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id="     + URLEncoder.encode(CLIENT_ID,    StandardCharsets.UTF_8)
                + "&redirect_uri="  + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope="         + URLEncoder.encode(SCOPE,        StandardCharsets.UTF_8)
                + "&access_type=offline"
                + "&prompt=select_account";
    }

    private static String exchangeCodeForToken(String code) throws IOException {
        URL url = new URL("https://oauth2.googleapis.com/token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String body = "code="          + URLEncoder.encode(code,          StandardCharsets.UTF_8)
                + "&client_id="    + URLEncoder.encode(CLIENT_ID,     StandardCharsets.UTF_8)
                + "&client_secret="+ URLEncoder.encode(CLIENT_SECRET, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI,  StandardCharsets.UTF_8)
                + "&grant_type=authorization_code";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        if (conn.getResponseCode() != 200) {
            String error = new String(conn.getErrorStream().readAllBytes());
            System.err.println("Token exchange failed: " + error);
            return null;
        }

        String response = new String(conn.getInputStream().readAllBytes());
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        return json.has("access_token") ? json.get("access_token").getAsString() : null;
    }

    private static GoogleUser fetchUserInfo(String accessToken) throws IOException {
        URL url = new URL("https://www.googleapis.com/oauth2/v3/userinfo");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);

        if (conn.getResponseCode() != 200) return null;

        String response = new String(conn.getInputStream().readAllBytes());
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();

        return new GoogleUser(
                json.has("sub")     ? json.get("sub").getAsString()     : null,
                json.has("name")    ? json.get("name").getAsString()    : "Unknown",
                json.has("email")   ? json.get("email").getAsString()   : null,
                json.has("picture") ? json.get("picture").getAsString() : null
        );
    }

    // ── Browser page ──────────────────────────────────────────────────────

    private static String closingPage(String title, String message, String color) {
        return "<!DOCTYPE html><html><head><meta charset='utf-8'><title>" + title + "</title>"
                + "<style>*{box-sizing:border-box}body{font-family:'Segoe UI',Arial,sans-serif;"
                + "display:flex;align-items:center;justify-content:center;min-height:100vh;"
                + "margin:0;background:#f8fafc}.c{background:#fff;border-radius:16px;"
                + "box-shadow:0 4px 24px rgba(0,0,0,.08);padding:48px 40px;max-width:400px;"
                + "text-align:center}h1{color:" + color + ";margin:0 0 16px;font-size:22px}"
                + "p{color:#475569;font-size:15px}small{color:#94a3b8;font-size:12px}</style></head>"
                + "<body><div class='c'>"
                + "<div style='font-size:48px;margin-bottom:16px'>🎓</div>"
                + "<h1>" + title + "</h1><p>" + message + "</p>"
                + "<br/><small>Vous pouvez fermer cet onglet.</small>"
                + "</div></body></html>";
    }
}