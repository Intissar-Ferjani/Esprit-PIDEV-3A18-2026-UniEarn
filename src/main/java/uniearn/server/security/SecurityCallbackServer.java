package uniearn.server.security;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;

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
    private static Runnable         onConfirm;
    private static Consumer<String> onLock;

    // ── Lifecycle ─────────────────────────────────────────────────────────

    public static synchronized void start(Runnable confirmCallback,
                                          Consumer<String> lockCallback) {
        onConfirm = confirmCallback;
        onLock    = lockCallback;

        if (server != null) {
            System.out.println("✓ SecurityCallbackServer already running on port " + PORT);
            return;
        }

        // Register a JVM shutdown hook so the port is ALWAYS freed,
        // even when IntelliJ kills the process with the red stop button.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stop();
            System.out.println("✓ Shutdown hook: SecurityCallbackServer stopped, port " + PORT + " freed.");
        }, "callback-server-shutdown"));

        tryBind();
    }

    private static void tryBind() {
        // Try ports 7373, 7374, 7375 — use whichever is free
        for (int port = PORT; port <= PORT + 2; port++) {
            try {
                server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
                server.setExecutor(Executors.newSingleThreadExecutor());
                registerEndpoints(port);
                server.start();

                if (port != PORT) {
                    System.out.println("⚠ Port " + PORT + " busy — using fallback port " + port + " instead.");
                    System.out.println("  NOTE: add MainApp.stop() → SecurityCallbackServer.stop() to fix permanently.");
                } else {
                    System.out.println("✓ SecurityCallbackServer started on port " + port);
                }

                // Store the actual port used so EmailService generates correct URLs
                actualPort = port;
                return;

            } catch (IOException e) {
                System.out.println("  Port " + port + " busy, trying next...");
            }
        }
        System.err.println("✗ Could not bind on ports " + PORT + "-" + (PORT + 2) + ". Email buttons will not work.");
    }

    // The port actually bound (may differ from PORT if PORT was busy)
    private static int actualPort = PORT;

    public static int getActualPort() { return actualPort; }

    private static void registerEndpoints(int port) {
        // ── /confirm → "Yes it's me" ──────────────────────────────────
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

        // ── /lockme → "No, lock it" ───────────────────────────────────
        server.createContext("/lockme", ex -> {
            String token = param(ex, "token");
            String email = token != null ? lockTokens.remove(token) : null;

            if (email != null) {
                uniearn.services.users.security.LoginAttemptService.lockTemporarily(email);
                send(ex, page("🔒 Compte verrouillé",
                        "Votre compte est verrouillé 15 minutes. " +
                                "Réinitialisez votre mot de passe dès que possible.", "#2563eb"));
                Platform.runLater(() -> { if (onLock != null) onLock.accept(email); });
            } else {
                send(ex, page("⚠ Lien expiré",
                        "Ce lien a déjà été utilisé ou a expiré.", "#dc2626"));
            }
        });
    }

    public static synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    // ── Token management ──────────────────────────────────────────────────

    public static String[] generateTokens(String email) {
        String ct = UUID.randomUUID().toString();
        String lt = UUID.randomUUID().toString();
        confirmTokens.put(ct, email);
        lockTokens.put(lt, email);
        System.out.println("✓ Tokens generated for: " + email);
        System.out.println("  Server running on port: " + actualPort);
        System.out.println("  confirm tokens in memory: " + confirmTokens.size());
        System.out.println("  lock    tokens in memory: " + lockTokens.size());
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
