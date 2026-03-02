package uniearn.utils.candidature;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Manages external API integrations for Sprint Java.
 */
public class ApiManager {

    private static final String CURRENCY_API_URL = "https://open.er-api.com/v6/latest/TND";
    private static final String PURGOMALUM_API_URL = "https://www.purgomalum.com/service/json?text=";
    private static final String RESTCOUNTRIES_API_URL = "https://restcountries.com/v3.1/name/";
    private static final String DICEBEAR_AVATAR_URL = "https://api.dicebear.com/7.x/avataaars/svg?seed=";
    private static final String SENTIMENT_API_URL = "http://text-processing.com/api/sentiment/";

    private final HttpClient httpClient;

    public ApiManager() {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    /**
     * Generates a DiceBear Avatar URL based on a seed (e.g., username or ID).
     */
    public String getDiceBearAvatarUrl(String seed) {
        return DICEBEAR_AVATAR_URL + seed.replaceAll("\\s+", "_");
    }

    /**
     * Moderates text using PurgoMalum API to remove profanity.
     */
    public CompletableFuture<String> moderateContent(String text) {
        if (text == null || text.isBlank())
            return CompletableFuture.completedFuture("");

        String encodedText = java.net.URLEncoder.encode(text, java.nio.charset.StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PURGOMALUM_API_URL + encodedText))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                        return json.get("result").getAsString();
                    }
                    return text; // Fallback to original
                })
                .exceptionally(ex -> text);
    }

    /**
     * Fetches country flag URL from RestCountries API.
     */
    public CompletableFuture<String> getCountryFlag(String countryName) {
        if (countryName == null || countryName.isBlank())
            return CompletableFuture.completedFuture(null);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(RESTCOUNTRIES_API_URL + countryName.replaceAll("\\s+", "%20")))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        com.google.gson.JsonArray array = JsonParser.parseString(response.body()).getAsJsonArray();
                        if (array.size() > 0) {
                            return array.get(0).getAsJsonObject()
                                    .getAsJsonObject("flags")
                                    .get("png").getAsString();
                        }
                    }
                    return null;
                })
                .exceptionally(ex -> null);
    }

    /**
     * Fetches current exchange rate from TND to target currency.
     */
    public CompletableFuture<Double> getExchangeRate(String targetCurrency) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CURRENCY_API_URL))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                        return json.getAsJsonObject("rates").get(targetCurrency).getAsDouble();
                    }
                    throw new RuntimeException("Failed to fetch exchange rate");
                })
                .exceptionally(ex -> {
                    System.err.println("API Error: " + ex.getMessage());
                    return 0.31; // Fallback
                });
    }

    /**
     * Analyzes sentiment of a string using an external API.
     */
    public CompletableFuture<String> analyzeSentiment(String text) {
        String formData = "text=" + text;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SENTIMENT_API_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                        return json.get("label").getAsString(); // "pos", "neg", "neutral"
                    }
                    return "neutral";
                })
                .exceptionally(ex -> "neutral");
    }
}
