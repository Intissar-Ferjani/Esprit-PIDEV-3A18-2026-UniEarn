package uniearn.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Calls Giphy API to search GIFs. Uses public beta key for demo.
 * Get your free key at https://developers.giphy.com/
 */
public class GiphyService {

    private static final String API_KEY = System.getProperty("giphy.api.key", "dc6zaTOxFJmzC");
    private static final String SEARCH_URL = "https://api.giphy.com/v1/gifs/search?api_key=%s&q=%s&limit=12";

    public List<String> searchGifs(String query) {
        List<String> urls = new ArrayList<>();
        try {
            String encoded = java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(SEARCH_URL, API_KEY, encoded)))
                    .GET()
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
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
                            if (url == null && fixedHeight.has("webp")) url = fixedHeight.get("webp").asText();
                            if (url != null) urls.add(url);
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
}
