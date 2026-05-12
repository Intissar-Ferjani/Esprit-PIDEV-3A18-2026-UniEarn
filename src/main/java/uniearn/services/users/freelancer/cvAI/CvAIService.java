package uniearn.services.users.freelancer.cvAI;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CvAIService {

    private static final String OPENROUTER_API_KEY = "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";
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

    public enum FailReason {
        NAME_MISMATCH, EXTRACTION_FAILED, API_FAILED
    }

    public static class CvResult {
        private final String bio;
        private final FailReason failReason;
        private final String failMessage;

        private CvResult(String bio, FailReason failReason, String failMessage) {
            this.bio = bio;
            this.failReason = failReason;
            this.failMessage = failMessage;
        }

        public static CvResult success(String bio) {
            return new CvResult(bio, null, null);
        }

        public static CvResult failure(FailReason r, String msg) {
            return new CvResult(null, r, msg);
        }

        public boolean isSuccess() {
            return bio != null;
        }

        public String getBio() {
            return bio;
        }

        public FailReason getFailReason() {
            return failReason;
        }

        public String getFailMessage() {
            return failMessage;
        }
    }

    public CvResult generateBioFromCv(File cvFile, String registeredName) {
        String cvText = extractText(cvFile);

        if (cvText == null || cvText.isBlank()) {
            System.out.println("Could not extract any text from CV: " + cvFile.getName());
            return CvResult.failure(FailReason.EXTRACTION_FAILED,
                    "Could not read your CV. Please upload a clearer PDF.");
        }

        if (registeredName != null && !registeredName.isBlank()) {
            boolean nameFound = doesNameAppearInCv(cvText, registeredName);
            if (!nameFound) {
                System.out.println("✗ Name mismatch — registered: " + registeredName);
                return CvResult.failure(FailReason.NAME_MISMATCH,
                        "The name on your CV doesn't match your registered name ("
                                + registeredName + "). Please upload your own CV.");
            }
            System.out.println("✓ Name verified in CV");
        }

        if (cvText.length() > 3000)
            cvText = cvText.substring(0, 3000) + "...";

        System.out.println("CV text extracted (" + cvText.length() + " chars), sending to OpenRouter...");
        String bio = callWithFallback(cvText);

        if (bio == null) {
            return CvResult.failure(FailReason.API_FAILED,
                    "Could not generate bio. Please write it manually.");
        }

        return CvResult.success(bio);
    }

    private boolean doesNameAppearInCv(String cvText, String registeredName) {
        String normalizedCv = normalize(cvText);
        String normalizedName = normalize(registeredName).trim();

        if (normalizedCv.contains(normalizedName)) {
            System.out.println("✓ Full name matched in CV: " + normalizedName);
            return true;
        }

        String[] nameParts = normalizedName.split("\\s+");
        int matchCount = 0;
        for (String part : nameParts) {
            if (part.length() > 2 && normalizedCv.contains(part)) {
                System.out.println("  ✓ Name part matched: " + part);
                matchCount++;
            }
        }

        int required = (int) Math.ceil(nameParts.length / 2.0);
        boolean partialMatch = matchCount >= required;

        if (partialMatch) {
            System.out.println("✓ Partial name match: " + matchCount + "/" + nameParts.length
                    + " parts (required: " + required + ")");
        } else {
            System.out.println("✗ Name not found: looking for '" + normalizedName
                    + "' — matched " + matchCount + "/" + nameParts.length
                    + " parts (required: " + required + ")");
        }
        return partialMatch;
    }

    private String normalize(String text) {
        if (text == null)
            return "";
        String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD);
        String stripped = decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}", "");
        return stripped.toUpperCase().replaceAll("\\s+", " ").trim();
    }

    private String extractText(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".pdf"))
            return extractFromPdfWithTesseract(file);
        if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg"))
            return extractFromImageWithTesseract(file);
        if (name.endsWith(".docx") || name.endsWith(".doc")) {
            System.out.println("DOCX/DOC not supported without Apache POI. Please upload a PDF.");
            return null;
        }
        System.out.println("Unsupported file type: " + name);
        return null;
    }

    private String extractFromPdfWithTesseract(File pdfFile) {
        try {
            String result = buildTesseract().doOCR(pdfFile);
            if (result == null || result.isBlank()) {
                System.out.println("✗ Tesseract returned no text from PDF");
                return null;
            }
            System.out.println("✓ PDF extracted via Tesseract (" + result.strip().length() + " chars)");
            return result;
        } catch (TesseractException e) {
            System.err.println("Tesseract PDF error: " + e.getMessage());
            return null;
        }
    }

    private String extractFromImageWithTesseract(File imageFile) {
        try {
            BufferedImage img = ImageIO.read(imageFile);
            if (img == null) {
                System.err.println("ImageIO could not read: " + imageFile.getName());
                return null;
            }
            String result = buildTesseract().doOCR(img);
            System.out.println(
                    "✓ Image extracted via Tesseract (" + (result != null ? result.strip().length() : 0) + " chars)");
            return result;
        } catch (TesseractException | IOException e) {
            System.err.println("Tesseract image error: " + e.getMessage());
            return null;
        }
    }

    private Tesseract buildTesseract() {
        Tesseract t = new Tesseract();
        t.setDatapath(new File("tessdata").getAbsolutePath());
        t.setLanguage("fra+eng+ara");
        t.setPageSegMode(3);
        t.setOcrEngineMode(1);
        t.setTessVariable("user_defined_dpi", "300");
        return t;
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
            Pattern promptPattern = Pattern.compile("\"prompt\"\\s*:\\s*\"([^\"]+)\"");
            String[] entries = body.split("\"id\"\\s*:\\s*\"");

            for (int i = 1; i < entries.length && freeIds.size() < MAX_MODELS_TO_TRY; i++) {
                String chunk = entries[i];
                int endQuote = chunk.indexOf("\"");
                if (endQuote == -1)
                    continue;
                String modelId = chunk.substring(0, endQuote);
                if (!modelId.endsWith(":free"))
                    continue;
                if (modelId.contains("thinking") || modelId.contains("reasoner")
                        || modelId.contains(":nitro") || modelId.contains(":floor"))
                    continue;
                int pricingIdx = chunk.indexOf("\"pricing\"");
                if (pricingIdx != -1) {
                    String pricingChunk = chunk.substring(pricingIdx, Math.min(pricingIdx + 200, chunk.length()));
                    Matcher pm = promptPattern.matcher(pricingChunk);
                    if (pm.find() && !"0".equals(pm.group(1)))
                        continue;
                }
                freeIds.add(modelId);
                System.out.println("  Found free model: " + modelId);
            }

            if (freeIds.isEmpty()) {
                System.out.println("No free models found — using fallbacks");
                return List.of(EMERGENCY_FALLBACKS);
            }
            System.out.println("✓ Fetched " + freeIds.size() + " free model(s) from OpenRouter");
            return freeIds;

        } catch (Exception e) {
            System.err.println("Could not fetch models list: " + e.getMessage() + " — using fallbacks");
            return List.of(EMERGENCY_FALLBACKS);
        }
    }

    private String callWithFallback(String cvText) {
        List<String> models = fetchFreeModelIds();
        for (int i = 0; i < models.size(); i++) {
            String model = models.get(i);
            System.out.println("Trying model: " + model + " (" + (i + 1) + "/" + models.size() + ")");
            try {
                HttpResponse<String> response = sendRequest(model, cvText);
                int status = response.statusCode();
                System.out.println("OpenRouter status: " + status + " (model: " + model + ")");
                if (status == 200) {
                    String bio = parseResponse(response.body());
                    if (bio != null && !bio.isBlank())
                        return bio;
                    System.out.println("⚠ Empty/null content from " + model + ", trying next...");
                    continue;
                }
                if (status == 429) {
                    System.out.println("⚠ 429 on " + model + ", trying next...");
                    continue;
                }
                System.err.println("✗ Error " + status + " on " + model + " — trying next...");
            } catch (Exception e) {
                System.err.println("✗ Request failed for " + model + ": " + e.getMessage());
            }
        }
        System.err.println("✗ All models failed.");
        return null;
    }

    private HttpResponse<String> sendRequest(String model, String cvText)
            throws IOException, InterruptedException {

        String prompt = "Based on the following CV content, write a professional freelancer bio in 2-3 sentences "
                + "(between 80 and 300 characters). "
                + "Requirements:\n"
                + "- CRITICAL: Detect the language of the CV and write the bio in that EXACT same language. "
                + "If the CV is in French, respond in French. If in English, respond in English. "
                + "If in Arabic, respond in Arabic. Match the language precisely.\n"
                + "- Written in first person\n"
                + "- Highlights the most relevant skills and experience\n"
                + "- Sounds professional but approachable\n"
                + "- Ready to paste directly into a profile, no intro like 'Here is your bio:'\n\n"
                + "CV Content:\n" + cvText;

        String jsonBody = "{"
                + "\"model\": " + jsonString(model) + ","
                + "\"messages\": [{\"role\": \"user\", \"content\": " + jsonString(prompt) + "}],"
                + "\"max_tokens\": 300,"
                + "\"temperature\": 0.7"
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CHAT_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + OPENROUTER_API_KEY)
                .header("HTTP-Referer", "https://uniearn.app")
                .header("X-Title", "UniEarn")
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
            if (json.startsWith("null", valueStart)) {
                System.out.println("⚠ content is null (reasoning model — skipping)");
                return null;
            }
            if (json.charAt(valueStart) != '"') {
                System.err.println("Unexpected content format");
                return null;
            }
            int start = valueStart + 1;
            int end = start;
            while (end < json.length()) {
                if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\')
                    break;
                end++;
            }
            String content = json.substring(start, end)
                    .replace("\\n", "\n").replace("\\\"", "\"")
                    .replace("\\/", "/").replace("\\\\", "\\").trim();
            if (content.isBlank()) {
                System.out.println("⚠ content is empty");
                return null;
            }
            System.out.println("✅ Bio generated successfully");
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