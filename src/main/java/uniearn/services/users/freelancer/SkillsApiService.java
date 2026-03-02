package uniearn.services.users.freelancer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class SkillsApiService {

    private static final String API_URL =
            "https://api.github.com/search/topics?q=programming&per_page=50";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // Fallback list in case API is unavailable
    private static final List<String> FALLBACK_SKILLS = List.of(
            "Java", "Python", "JavaScript", "TypeScript", "C++", "C#",
            "React", "Angular", "Vue.js", "Node.js", "Spring Boot", "Django",
            "Flutter", "Android", "iOS", "MySQL", "PostgreSQL", "MongoDB",
            "Docker", "Kubernetes", "AWS", "Machine Learning", "Git", "Linux"
    );

    public List<String> fetchSkills(String query) {
        try {
            String url = "https://api.github.com/search/topics?q="
                    + query + "&per_page=30";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/vnd.github.mercy-preview+json")
                    .header("User-Agent", "UniEarn-App")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                return parseSkills(response.body());
            } else {
                System.out.println("⚠ API returned: " + response.statusCode()
                        + ", using fallback");
                return filterFallback(query);
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("⚠ API unavailable, using fallback: " + e.getMessage());
            return filterFallback(query);
        }
    }

    private List<String> parseSkills(String json) throws IOException {
        List<String> skills = new ArrayList<>();
        JsonNode root = mapper.readTree(json);
        JsonNode items = root.get("items");

        if (items != null && items.isArray()) {
            for (JsonNode item : items) {
                String name = null;

                // Try display_name first, fall back to name, skip if both null
                if (item.get("display_name") != null && !item.get("display_name").isNull()) {
                    name = item.get("display_name").asText();
                } else if (item.get("name") != null && !item.get("name").isNull()) {
                    name = item.get("name").asText();
                }

                if (name != null && !name.isBlank()) {
                    skills.add(capitalize(name));
                }
            }
        }
        return skills;
    }

    private List<String> filterFallback(String query) {
        return FALLBACK_SKILLS.stream()
                .filter(s -> s.toLowerCase().contains(query.toLowerCase()))
                .collect(java.util.stream.Collectors.toList());
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
