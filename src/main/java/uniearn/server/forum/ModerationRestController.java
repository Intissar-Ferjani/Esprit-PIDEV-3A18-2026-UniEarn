package uniearn.server.forum;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * REST endpoint that proxies text to the Google Perspective API for toxicity analysis.
 * The JavaFX client calls POST /api/moderation/check with {"text":"..."}.
 */
@RestController
@RequestMapping("/api/moderation")
public class ModerationRestController {

    @Value("${perspective.api.key:}")
    private String perspectiveApiKey;

    private static final String PERSPECTIVE_URL =
            "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze";
    private static final double TOXICITY_THRESHOLD = 0.7;
    private static final ObjectMapper JSON = new ObjectMapper();

    @PostMapping("/check")
    public Map<String, Object> checkContent(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        String text = body.get("text");

        if (text == null || text.isBlank()) {
            result.put("toxic", false);
            result.put("score", 0.0);
            result.put("usingPerspective", false);
            return result;
        }

        if (perspectiveApiKey == null || perspectiveApiKey.isBlank()) {
            result.put("toxic", false);
            result.put("score", 0.0);
            result.put("usingPerspective", false);
            return result;
        }

        try {
            String requestBody = JSON.writeValueAsString(Map.of(
                    "comment", Map.of("text", text),
                    "languages", new String[]{"en", "fr"},
                    "requestedAttributes", Map.of("TOXICITY", Map.of())
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PERSPECTIVE_URL + "?key=" + perspectiveApiKey))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(5))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() == 200) {
                JsonNode root = JSON.readTree(response.body());
                double score = root.path("attributeScores")
                        .path("TOXICITY")
                        .path("summaryScore")
                        .path("value")
                        .asDouble(0.0);
                boolean toxic = score >= TOXICITY_THRESHOLD;

                result.put("toxic", toxic);
                result.put("score", score);
                result.put("usingPerspective", true);
            } else {
                result.put("toxic", false);
                result.put("score", 0.0);
                result.put("usingPerspective", false);
                result.put("error", "Perspective API returned status " + response.statusCode());
            }
        } catch (Exception e) {
            result.put("toxic", false);
            result.put("score", 0.0);
            result.put("usingPerspective", false);
            result.put("error", e.getMessage());
        }

        return result;
    }
}
