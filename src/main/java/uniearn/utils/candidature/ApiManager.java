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
    private static final String SENTIMENT_API_URL = "http://text-processing.com/api/sentiment/";

    private final HttpClient httpClient;

    public ApiManager() {
        this.httpClient = HttpClient.newHttpClient();
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
                    return 0.31; // Fallback rate (simulated)
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
