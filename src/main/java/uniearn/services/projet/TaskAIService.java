package uniearn.services.projet;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TaskAIService {

    private static final String OPENROUTER_API_KEY = System.getenv("OPENROUTER_API_KEY");
    private static final String CHAT_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODELS_URL = "https://openrouter.ai/api/v1/models";
    private static final String[] EMERGENCY_FALLBACKS = {
            "openrouter/free",
            "nousresearch/hermes-3-llama-3.1-405b:free",
            "qwen/qwen2.5-72b-instruct:free"
    };
    private static final int MAX_MODELS_TO_TRY = 5;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public List<String> suggestTasks(String projectTitle, String projectDescription) {
        String prompt = "Act as an expert project manager. Given the following project details, suggest 4 to 6 concise, actionable tasks that are essential for its completion.\n\n"
                + "Project Title: " + projectTitle + "\n"
                + "Project Description: " + projectDescription + "\n\n"
                + "Requirements:\n"
                + "- Each task must be a single line (max 60 characters).\n"
                + "- Output ONLY the task titles, one per line.\n"
                + "- Do NOT include numbers, bullet points, or introductory text.\n"
                + "- Provide the tasks in the same language as the project description.";

        System.out.println("AI Suggestion Request for Project: " + projectTitle);
        System.out
                .println("Description Length: " + (projectDescription != null ? projectDescription.length() : "null"));

        String response = callWithFallback(prompt);
        if (response == null) {
            System.err.println("❌ TaskAI: callWithFallback returned null.");
            return new ArrayList<>();
        }

        List<String> tasks = new ArrayList<>();
        for (String line : response.split("\n")) {
            String clean = line.trim().replaceAll("^[-*\\d.]+\\s*", "");
            if (!clean.isEmpty() && clean.length() > 3) {
                tasks.add(clean);
            }
        }
        System.out.println("✅ TaskAI: Extracted " + tasks.size() + " tasks from response.");
        return tasks;
    }

    private String callWithFallback(String prompt) {
        List<String> models = fetchFreeModelIds();
        for (String model : models) {
            System.out.println("Trying Task AI Model: " + model);
            try {
                HttpResponse<String> resp = sendRequest(model, prompt);
                System.out.println("OpenRouter Status Code: " + resp.statusCode());
                if (resp.statusCode() == 200) {
                    String result = parseResponse(resp.body());
                    if (result != null) {
                        System.out.println("✅ AI Response parsed successfully.");
                        return result;
                    }
                } else {
                    System.err.println("Response Error Body: " + resp.body());
                }
            } catch (Exception e) {
                System.err.println("TaskAI request failed for " + model + ": " + e.getMessage());
            }
        }
        return null;
    }

    private List<String> fetchFreeModelIds() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(MODELS_URL))
                    .header("Authorization", "Bearer " + OPENROUTER_API_KEY)
                    .GET().timeout(Duration.ofSeconds(10)).build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                System.err.println("Models API returned " + resp.statusCode() + " — using fallbacks");
                return List.of(EMERGENCY_FALLBACKS);
            }

            List<String> freeIds = new ArrayList<>();
            String body = resp.body();
            String[] entries = body.split("\"id\"\\s*:\\s*\"");

            for (int i = 1; i < entries.length && freeIds.size() < MAX_MODELS_TO_TRY; i++) {
                String chunk = entries[i];
                int endQuote = chunk.indexOf("\"");
                if (endQuote == -1)
                    continue;
                String modelId = chunk.substring(0, endQuote);
                if (modelId.endsWith(":free")) {
                    freeIds.add(modelId);
                }
            }
            return freeIds.isEmpty() ? List.of(EMERGENCY_FALLBACKS) : freeIds;
        } catch (Exception e) {
            System.err.println("Could not fetch models list: " + e.getMessage() + " — using fallbacks");
            return List.of(EMERGENCY_FALLBACKS);
        }
    }

    private HttpResponse<String> sendRequest(String model, String prompt) throws Exception {
        String jsonBody = "{"
                + "\"model\": " + jsonString(model) + ","
                + "\"messages\": [{\"role\": \"user\", \"content\": " + jsonString(prompt) + "}],"
                + "\"max_tokens\": 500,"
                + "\"temperature\": 0.7"
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CHAT_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + OPENROUTER_API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(30))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String parseResponse(String json) {
        try {
            String contentKey = "\"content\":";
            int idx = json.indexOf(contentKey);
            if (idx == -1) {
                System.err.println("No 'content' field in response");
                return null;
            }
            int valueStart = idx + contentKey.length();
            while (valueStart < json.length() && json.charAt(valueStart) == ' ')
                valueStart++;
            if (json.startsWith("null", valueStart))
                return null;

            int start = json.indexOf("\"", valueStart) + 1;
            int end = start;
            while (end < json.length()) {
                if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\')
                    break;
                end++;
            }
            String content = json.substring(start, end)
                    .replace("\\n", "\n").replace("\\\"", "\"")
                    .replace("\\/", "/").replace("\\\\", "\\").trim();

            if (content.isEmpty())
                return null;
            return content;
        } catch (Exception e) {
            System.err.println("Failed to parse response: " + e.getMessage());
            return null;
        }
    }

    private String jsonString(String value) {
        return "\"" + value
                .replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")
                + "\"";
    }
}
