package uniearn.services.forum;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.scene.image.Image;

/**
 * Calls Giphy API to search GIFs. Uses public beta key for demo.
 * Get your free key at https://developers.giphy.com/
 */
public class GiphyService {

    private static final String API_KEY = System.getProperty("giphy.api.key", "TmJf0bOAQsNAe80HbonqlIRZV3FyL1RR");
    private static final String SEARCH_URL = "https://api.giphy.com/v1/gifs/search?api_key=%s&q=%s&limit=12";

    /**
     * Shared HttpClient that follows redirects — used for both API calls and image
     * downloads
     */
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public List<String> searchGifs(String query) {
        List<String> urls = new ArrayList<>();
        try {
            String encoded = java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(SEARCH_URL, API_KEY, encoded)))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> response = HTTP_CLIENT
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("❌ Giphy API Error: Received response code " + response.statusCode());
                if (response.statusCode() == 403) {
                    System.err.println("👉 Hint: Your Giphy API Key might be invalid or rate-limited.");
                }
                return urls;
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            JsonNode data = root.get("data");
            if (data != null && data.isArray()) {
                for (JsonNode item : data) {
                    JsonNode images = item.get("images");
                    if (images != null) {
                        JsonNode fixedHeight = images.get("fixed_height");
                        if (fixedHeight != null) {
                            String url = fixedHeight.has("url") ? fixedHeight.get("url").asText() : null;
                            if (url == null && fixedHeight.has("webp"))
                                url = fixedHeight.get("webp").asText();
                            if (url != null)
                                urls.add(url);
                        } else if (images.has("downsized")) {
                            urls.add(images.get("downsized").get("url").asText());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Giphy search failed: " + e.getMessage());
        }
        return urls;
    }

    /** Check if a string is a Giphy GIF URL */
    public static boolean isGiphyUrl(String text) {
        return text != null && (text.contains("giphy.com") || text.contains("gph.is"));
    }

    /**
     * Download an image from a URL using HttpClient (handles redirects, TLS,
     * User-Agent).
     * Giphy's CDN rejects requests without a browser-like User-Agent and needs
     * proper
     * redirect handling, which JavaFX's built-in Image(url) loader does not
     * provide.
     *
     * @return a JavaFX Image, or null if the download fails
     */
    public static Image loadImage(String url) {
        if (url == null || url.isEmpty())
            return null;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();
            HttpResponse<byte[]> response = HTTP_CLIENT.send(request,
                    HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200 && response.body().length > 0) {
                return new Image(new ByteArrayInputStream(response.body()));
            } else {
                System.err.println("GiphyService.loadImage HTTP " + response.statusCode()
                        + " for: " + url);
            }
        } catch (Exception e) {
            System.err.println("GiphyService.loadImage failed for: " + url + " → " + e.getMessage());
        }
        return null;
    }
}
