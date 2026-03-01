package uniearn.services.forum;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Détection de contenus toxiques : appelle d'abord le backend (Perspective API),
 * sinon repli sur une liste locale de mots interdits.
 */
public class ModerationService {

    private static final String BACKEND_CHECK_URL = "http://localhost:8081/api/moderation/check";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(4);
    private static final ObjectMapper JSON = new ObjectMapper();

    private static final Set<String> BAD_WORDS = new HashSet<>(Arrays.asList(
            "insulte", "injure", "haine", "raciste", "stupide", "idiot", "débile",
            "merde", "putain", "connard", "salope", "enculé", "nique", "fdp", "pd",
            "hate", "stupid", "idiot", "dumb", "shit", "fuck", "asshole", "bitch",
            "damn", "crap", "wtf", "stfu", "kill", "die"
    ));

    private static final Pattern WORD_SPLIT = Pattern.compile("[\\s\\p{Punct}]+");

    /** Dernier message d'erreur (ex: "contenu toxique détecté par Perspective") */
    private String lastToxicReason;

    /**
     * Vérifie si le texte est acceptable (non toxique).
     * Appelle le backend (Perspective) si disponible, sinon liste locale.
     */
    public boolean isContentClean(String text) {
        lastToxicReason = null;
        if (text == null || text.isEmpty()) return true;

        // 1) Appel backend (Perspective API)
        try {
            Boolean backendResult = checkViaBackend(text);
            if (backendResult != null) {
                if (!backendResult) lastToxicReason = "contenu toxique detecté par Perspective API";
                return backendResult;
            }
        } catch (Exception ignored) {
            // Backend indisponible ou erreur → repli liste locale
        }

        // 2) Repli : liste locale de mots interdits
        if (!isContentCleanLocal(text)) {
            lastToxicReason = getFirstBadWord(text);
            return false;
        }
        return true;
    }

    /**
     * Appelle le backend POST /api/moderation/check.
     * @return true si propre, false si toxique, null si backend indisponible / pas de Perspective
     */
    private Boolean checkViaBackend(String text) throws Exception {
        String body = "{\"text\":\"" + escapeJson(text) + "\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BACKEND_CHECK_URL))
                .header("Content-Type", "application/json")
                .timeout(REQUEST_TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) return null;
        JsonNode root = JSON.readTree(response.body());
        boolean usingPerspective = root.path("usingPerspective").asBoolean(false);
        if (!usingPerspective) return null;
        boolean toxic = root.path("toxic").asBoolean(false);
        return !toxic;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }

    private boolean isContentCleanLocal(String text) {
        String normalized = text.toLowerCase(Locale.ROOT).trim();
        String withoutUrls = normalized.replaceAll("https?://[^\\s]+", " ");
        for (String word : WORD_SPLIT.split(withoutUrls)) {
            if (word.isEmpty()) continue;
            if (BAD_WORDS.contains(word)) return false;
        }
        return true;
    }

    /**
     * Premier mot interdit trouvé (liste locale), ou lastToxicReason si détection via backend.
     */
    public String getFirstBadWord(String text) {
        if (lastToxicReason != null) return lastToxicReason;
        if (text == null || text.isEmpty()) return null;
        String normalized = text.toLowerCase(Locale.ROOT).trim();
        String withoutUrls = normalized.replaceAll("https?://[^\\s]+", " ");
        for (String word : WORD_SPLIT.split(withoutUrls)) {
            if (word.isEmpty()) continue;
            if (BAD_WORDS.contains(word)) return word;
        }
        return null;
    }
}
