package uniearn.services.forum;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Auto-classifies post text into categories based on keyword matching.
 * Categories: Technology, Design, Business, Career, General (default).
 */
public class CategoryService {

    private static final Map<String, List<String>> CATEGORY_KEYWORDS = new LinkedHashMap<>();
    private static final Pattern WORD_SPLIT = Pattern.compile("[\\s\\p{Punct}]+");

    static {
        CATEGORY_KEYWORDS.put("Technology", Arrays.asList(
                "java", "python", "javascript", "code", "coding", "programming", "software",
                "developer", "api", "database", "framework", "react", "angular", "spring",
                "node", "backend", "frontend", "fullstack", "devops", "cloud", "docker",
                "kubernetes", "git", "algorithm", "web", "mobile", "app", "html", "css",
                "sql", "server", "linux", "debug", "testing", "computer", "tech", "ai",
                "machine learning", "data science", "cybersecurity", "blockchain"
        ));
        CATEGORY_KEYWORDS.put("Design", Arrays.asList(
                "design", "ui", "ux", "figma", "photoshop", "illustrator", "logo",
                "graphic", "branding", "prototype", "wireframe", "mockup", "creative",
                "color", "typography", "layout", "animation", "visual", "sketch", "adobe",
                "canva", "interface", "illustration", "banner", "poster"
        ));
        CATEGORY_KEYWORDS.put("Business", Arrays.asList(
                "business", "marketing", "sales", "revenue", "startup", "entrepreneur",
                "client", "project management", "budget", "finance", "invoice", "proposal",
                "contract", "strategy", "growth", "profit", "investment", "market", "brand",
                "customer", "ecommerce", "pricing", "negotiation", "accounting"
        ));
        CATEGORY_KEYWORDS.put("Career", Arrays.asList(
                "career", "job", "hiring", "resume", "portfolio", "interview", "freelance",
                "freelancing", "remote", "work", "salary", "experience", "skill", "mentor",
                "intern", "promotion", "opportunity", "linkedin", "networking", "professional",
                "tips", "advice", "guide", "beginner", "certification"
        ));
    }

    /**
     * Analyse the post title and content and return the best-matching category.
     * Falls back to "General" when no keyword matches.
     */
    public String categorize(String title, String content) {
        String combined = ((title != null ? title : "") + " " + (content != null ? content : ""))
                .toLowerCase(Locale.ROOT);
        String[] words = WORD_SPLIT.split(combined);
        Set<String> wordSet = new HashSet<>(Arrays.asList(words));

        Map<String, Integer> scores = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            int score = 0;
            for (String keyword : entry.getValue()) {
                if (keyword.contains(" ")) {
                    // Multi-word keywords — check substring
                    if (combined.contains(keyword)) score += 2;
                } else {
                    if (wordSet.contains(keyword)) score++;
                }
            }
            scores.put(entry.getKey(), score);
        }

        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .filter(e -> e.getValue() > 0)
                .map(Map.Entry::getKey)
                .orElse("General");
    }
}
