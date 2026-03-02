package uniearn.server.security;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;
import uniearn.services.users.security.LoginAttemptService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.function.Consumer;


public class SecurityCallbackServer {

    public static final int PORT = 7373;

    private static final Map<String, String> confirmTokens = new HashMap<>();
    private static final Map<String, String> lockTokens    = new HashMap<>();

    private static HttpServer       server;
    private static Runnable         onConfirm; // → open ForgotPassword
    private static Consumer<String> onLock;    // → lock + show message (param = email)

    // ── Lifecycle ─────────────────────────────────────────────────────────

    // Start the server
    public static synchronized void start(Runnable confirmCallback, Consumer<String> lockCallback) {
        onConfirm = confirmCallback;
        onLock    = lockCallback;
        if (server != null) return;

        try {
            server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
            server.setExecutor(Executors.newSingleThreadExecutor());

            server.createContext("/confirm", ex -> {
                String token = param(ex, "token");
                String email = token != null ? confirmTokens.remove(token) : null;
                if (email != null) {
                    send(ex, page("✅ Identité confirmée",
                            "Retournez dans l'application — la page de réinitialisation s'est ouverte.",
                            "#16a34a"));
                    Platform.runLater(() -> { if (onConfirm != null) onConfirm.run(); });
                } else {
                    send(ex, page("⚠ Lien expiré",
                            "Ce lien a déjà été utilisé ou a expiré.", "#dc2626"));
                }
            });

            server.createContext("/lockme", ex -> {
                String token = param(ex, "token");
                String email = token != null ? lockTokens.remove(token) : null;
                if (email != null) {
                    LoginAttemptService.lockTemporarily(email);
                    send(ex, page("🔒 Compte verrouillé",
                            "Votre compte est verrouillé 15 minutes. Personne ne peut s'y connecter. "
                                    + "Réinitialisez votre mot de passe dès que possible.", "#2563eb"));
                    Platform.runLater(() -> { if (onLock != null) onLock.accept(email); });
                } else {
                    send(ex, page("⚠ Lien expiré",
                            "Ce lien a déjà été utilisé ou a expiré.", "#dc2626"));
                }
            });

            server.start();
            System.out.println("✓ SecurityCallbackServer started on port " + PORT);
        } catch (IOException e) {
            System.err.println("⚠ SecurityCallbackServer failed: " + e.getMessage());
        }
    }

    public static synchronized void stop() {
        if (server != null) { server.stop(0); server = null; }
    }

    // ── Token management ──────────────────────────────────────────────────

    //Generates a one-time {confirmToken, lockToken} for given email
    public static String[] generateTokens(String email) {
        String ct = UUID.randomUUID().toString();
        String lt = UUID.randomUUID().toString();
        confirmTokens.put(ct, email);
        lockTokens.put(lt, email);
        return new String[]{ ct, lt };
    }

    // ── HTTP helpers ──────────────────────────────────────────────────────

    private static String param(HttpExchange ex, String key) {
        String q = ex.getRequestURI().getQuery();
        if (q == null) return null;
        for (String pair : q.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) return kv[1];
        }
        return null;
    }

    private static void send(HttpExchange ex, String html) throws IOException {
        byte[] b = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        ex.sendResponseHeaders(200, b.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(b); }
    }

    private static String page(String title, String body, String color) {
        return "<!DOCTYPE html><html><head><meta charset='utf-8'><title>" + title + "</title>"
                + "<style>*{box-sizing:border-box}body{font-family:'Segoe UI',Arial,sans-serif;"
                + "display:flex;align-items:center;justify-content:center;min-height:100vh;"
                + "margin:0;background:#f8fafc}.c{background:#fff;border-radius:16px;"
                + "box-shadow:0 4px 24px rgba(0,0,0,.08);padding:48px 40px;max-width:440px;"
                + "text-align:center}h1{color:" + color + ";margin:0 0 16px;font-size:22px}"
                + "p{color:#475569;font-size:15px;line-height:1.6}"
                + "small{color:#94a3b8;font-size:12px}</style></head>"
                + "<body><div class='c'><h1>" + title + "</h1><p>" + body + "</p>"
                + "<br/><small>Vous pouvez fermer cet onglet.</small></div></body></html>";
    }
}