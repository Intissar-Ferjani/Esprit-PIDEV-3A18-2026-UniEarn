package uniearn.services.users.freelancer.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import uniearn.model.entities.projet.Project;
import uniearn.model.entities.users.freelancer.Freelancer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeminiRecommendationService {

    public static class Recommendation {
        public Freelancer freelancer;
        public String projectTitle;
        public String role;
        public String reason;
    }

    private static final String DEFAULT_MODEL = "gemini-flash-latest";
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta";

    public List<Recommendation> getRecommendations(List<Project> activeProjects, List<Freelancer> availableFreelancers) throws Exception {
        if (activeProjects == null || activeProjects.isEmpty() || availableFreelancers == null || availableFreelancers.isEmpty()) {
            return new ArrayList<>();
        }

        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.out.println("⚠ No GEMINI_API_KEY found, using simulated fallback.");
            return getSimulatedFallback(activeProjects, availableFreelancers);
        }

        // Format data for AI
        List<Map<String, Object>> projectsData = new ArrayList<>();
        for (Project p : activeProjects) {
            Map<String, Object> map = new HashMap<>();
            map.put("title", p.getTitle());
            map.put("description", p.getDescription());
            projectsData.add(map);
        }

        List<Map<String, Object>> freelancersData = new ArrayList<>();
        for (Freelancer f : availableFreelancers) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", f.getIdUser());
            map.put("skills", f.getSkills());
            map.put("bio", f.getBio());
            freelancersData.add(map);
        }

        Gson gson = new Gson();
        String projectsJson = gson.toJson(projectsData);
        String freelancersJson = gson.toJson(freelancersData);

        String prompt = "You are an expert AI recruiter matching freelancers to client projects.\n\n" +
                "Client Projects:\n" + projectsJson + "\n\n" +
                "Available Freelancers:\n" + freelancersJson + "\n\n" +
                "Analyze the skills and bios of the freelancers and see which ones best match the client's projects. " +
                "Return a JSON array containing EXACTLY the top 5 recommended freelancer IDs, the exact title of the specific project they are best suited for, the suggested developer role (e.g., 'Frontend Developer', 'Backend Developer', 'Full Stack', 'Data Scientist', etc.), and a short (1-2 sentence) reason for each explaining why they are a good fit. " +
                "Format MUST be strict JSON: [{\"id\": 123, \"projectTitle\": \"Title of the Project\", \"role\": \"Backend Developer\", \"reason\": \"Because they have PHP skills...\"}] . Do not include markdown code blocks or any other text.";

        String urlString = BASE_URL + "/models/" + DEFAULT_MODEL + ":generateContent?key=" + apiKey;

        JsonObject requestBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject part = new JsonObject();
        JsonObject textObj = new JsonObject();
        textObj.addProperty("text", prompt);
        JsonArray partsArray = new JsonArray();
        partsArray.add(textObj);
        part.add("parts", partsArray);
        part.addProperty("role", "user");
        contents.add(part);
        requestBody.add("contents", contents);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println("Gemini API Error: " + response.body());
            if (response.statusCode() == 429) {
                throw new Exception("The AI service is currently busy (Rate Limit Exceeded). Please wait a few seconds and try again.");
            }
            throw new Exception("AI Service Error (Code " + response.statusCode() + ")");
        }

        // Parse response
        JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
        String aiText = jsonResponse.getAsJsonArray("candidates")
                .get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString();

        // Clean markdown blocks
        aiText = aiText.replace("```json", "").replace("```", "").trim();

        JsonArray aiMatches = gson.fromJson(aiText, JsonArray.class);
        List<Recommendation> recommendations = new ArrayList<>();

        for (int i = 0; i < aiMatches.size(); i++) {
            JsonObject match = aiMatches.get(i).getAsJsonObject();
            if (match.has("id") && match.has("reason")) {
                int id = match.get("id").getAsInt();
                Freelancer f = availableFreelancers.stream().filter(fr -> fr.getIdUser() == id).findFirst().orElse(null);
                if (f != null) {
                    Recommendation rec = new Recommendation();
                    rec.freelancer = f;
                    rec.projectTitle = match.has("projectTitle") ? match.get("projectTitle").getAsString() : "A project";
                    rec.role = match.has("role") ? match.get("role").getAsString() : "Developer";
                    rec.reason = match.get("reason").getAsString();
                    recommendations.add(rec);
                }
            }
        }

        return recommendations;
    }

    private List<Recommendation> getSimulatedFallback(List<Project> activeProjects, List<Freelancer> availableFreelancers) {
        try {
            Thread.sleep(1000); // Simulate network delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<Recommendation> simulated = new ArrayList<>();
        int count = Math.min(5, availableFreelancers.size());
        for (int i = 0; i < count; i++) {
            Recommendation rec = new Recommendation();
            rec.freelancer = availableFreelancers.get(i);
            rec.projectTitle = activeProjects.get(0).getTitle();
            rec.role = "Full Stack Developer";
            rec.reason = "Simulated match based on strong general rating. (Add GEMINI_API_KEY to system environment for real AI matching)";
            simulated.add(rec);
        }
        return simulated;
    }
}
