package uniearn.controller.auth.user;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpServer;

public class GoogleOAuthService {

    private static final String CLIENT_ID;
    private static final String CLIENT_SECRET;

    static {
        Map<String, String> env = loadEnvFile();
        CLIENT_ID     = env.getOrDefault("GOOGLE_CLIENT_ID",     System.getenv("GOOGLE_CLIENT_ID"));
        CLIENT_SECRET = env.getOrDefault("GOOGLE_CLIENT_SECRET", System.getenv("GOOGLE_CLIENT_SECRET"));
    }

    private static Map<String, String> loadEnvFile() {
        Map<String, String> map = new HashMap<>();
        java.io.File envFile = new java.io.File(".env");
        if (!envFile.exists()) envFile = new java.io.File("../.env");
        if (envFile.exists()) {
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(envFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    int eq = line.indexOf('=');
                    if (eq > 0) {
                        String key   = line.substring(0, eq).trim();
                        String value = line.substring(eq + 1).trim().replaceAll("^\"|\"$", "");
                        map.put(key, value);
                    }
                }
                System.out.println("✓ .env file loaded successfully.");
            } catch (Exception e) {
                System.err.println("⚠ Could not read .env file: " + e.getMessage());
            }
        } else {
            System.err.println("⚠ .env file not found.");
        }
        return map;
    }

    private static final String SCOPE = "openid email profile";
    private static volatile HttpServer runningServer = null;

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

    // ── Find a free port ──────────────────────────────────────────────────

    private static int findFreePort() {
        int[] preferred = {8585, 8586, 8587, 8588, 8080, 9000};
        for (int port : preferred) {
            try (ServerSocket s = new ServerSocket(port)) {
                s.setReuseAddress(true);
                return port;
            } catch (IOException ignored) {}
        }
        // Let OS assign any free port
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        } catch (IOException e) {
            return -1;
        }
    }

    // ── Main entry point ──────────────────────────────────────────────────

    public static void startOAuthFlow(Consumer<GoogleUser> onSuccess, Consumer<String> onError) {

        if (CLIENT_ID == null || CLIENT_SECRET == null) {
            onError.accept("Google OAuth credentials not configured. Set GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET in your .env file.");
            return;
        }

        new Thread(() -> {
            try {
                // ── Stop any previous server ───────────────────────────────
                synchronized (GoogleOAuthService.class) {
                    if (runningServer != null) {
                        try { runningServer.stop(0); } catch (Exception ignored) {}
                        runningServer = null;
                        Thread.sleep(400);
                    }
                }

                // ── Find a free port dynamically ───────────────────────────
                int port = findFreePort();
                if (port == -1) {
                    onError.accept("No free port available for OAuth callback. Please restart the application.");
                    return;
                }

                String redirectUri = "http://localhost:" + port + "/oauth/callback";
                System.out.println("✓ Using port " + port + " for OAuth callback.");

                CountDownLatch latch       = new CountDownLatch(1);
                String[]       codeHolder  = new String[1];
                String[]       errorHolder = new String[1];

                HttpServer callbackServer = HttpServer.create(
                        new InetSocketAddress("localhost", port), 0);

                synchronized (GoogleOAuthService.class) {
                    runningServer = callbackServer;
                }

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
                System.out.println("✓ OAuth callback server started on port " + port);

                String authUrl = buildAuthUrl(redirectUri);
                System.out.println("Opening browser: " + authUrl);
                Desktop.getDesktop().browse(new URI(authUrl));

                boolean received = latch.await(3, TimeUnit.MINUTES);

                synchronized (GoogleOAuthService.class) {
                    callbackServer.stop(0);
                    runningServer = null;
                }

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

                String accessToken = exchangeCodeForToken(codeHolder[0], redirectUri);
                if (accessToken == null) {
                    onError.accept("Failed to obtain access token from Google.");
                    return;
                }

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
                synchronized (GoogleOAuthService.class) {
                    if (runningServer != null) {
                        try { runningServer.stop(0); } catch (Exception ignored) {}
                        runningServer = null;
                    }
                }
            }
        }, "google-oauth-thread").start();
    }

    // ── OAuth steps ───────────────────────────────────────────────────────

    private static String buildAuthUrl(String redirectUri) throws Exception {
        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id="    + URLEncoder.encode(CLIENT_ID,   StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope="        + URLEncoder.encode(SCOPE,       StandardCharsets.UTF_8)
                + "&access_type=offline"
                + "&prompt=select_account";
    }

    private static String exchangeCodeForToken(String code, String redirectUri) throws IOException {
        URL url = new URL("https://oauth2.googleapis.com/token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String body = "code="           + URLEncoder.encode(code,          StandardCharsets.UTF_8)
                + "&client_id="     + URLEncoder.encode(CLIENT_ID,     StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, StandardCharsets.UTF_8)
                + "&redirect_uri="  + URLEncoder.encode(redirectUri,   StandardCharsets.UTF_8)
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
