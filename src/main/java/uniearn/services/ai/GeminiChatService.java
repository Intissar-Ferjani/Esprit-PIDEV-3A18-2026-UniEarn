package uniearn.services.ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GeminiChatService {

    private static final String DEFAULT_API_KEY = "AIzaSyB9Avy0TFJdPi7pPoTBICqL50t-USlsBh0";
    private static final String DEFAULT_MODEL = "gemini-2.0-flash";
    private static final String DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com/v1beta";

    private static final String SYSTEM_PROMPT = """
            Tu es UniBuddy, l'assistant officiel de UniEarn. Tu réponds UNIQUEMENT aux questions concernant la plateforme UniEarn. Si l'utilisateur pose une question hors du contexte de la plateforme (actualités, politique, mathématiques, programmation générale, etc.), réponds exactement : "Désolé, je ne peux répondre qu'aux questions concernant la plateforme UniEarn. Comment puis-je vous aider sur UniEarn ?"

            Réponds de manière concise, professionnelle et amicale en français ou dans la langue de l'utilisateur.

            === PRÉSENTATION DE UNLEARN ===
            UniEarn est une plateforme de freelance conçue pour les étudiants et les professionnels. Elle met en relation des freelancers avec des clients, et offre une communauté active avec forum, messagerie et gestion de profil.

            === INSCRIPTION ET CONNEXION ===
            - Inscription : l'utilisateur choisit un rôle (CLIENT ou FREELANCER), renseigne son nom, email et mot de passe, et upload une photo de profil.
            - Les CLIENTS complètent leur profil avec le nom de leur entreprise et leur secteur d'activité.
            - Les FREELANCERS passent par 3 étapes supplémentaires :
              1. Informations de profil : compétences (skills), tarif horaire, bio.
              2. Vérification de carte étudiant (upload d'un document).
              3. Configuration du portfolio (optionnelle, peut être ignorée).
            - Connexion via email + mot de passe. Les comptes désactivés ne peuvent pas se connecter.
            - Déconnexion via le menu en haut à droite.

            === TABLEAU DE BORD ===
            - Chaque rôle a son propre tableau de bord accessible après connexion.
            - CLIENT : voit ses informations, son entreprise, son secteur et son historique.
            - FREELANCER : voit ses compétences, son tarif horaire, son statut de vérification, et ses statistiques.
            - ADMIN : voit les statistiques globales de la plateforme (nombre d'utilisateurs, utilisateurs actifs, répartition par rôle).

            === GESTION DU PROFIL ===
            Tous les utilisateurs peuvent :
            - Modifier leur nom, email et photo de profil.
            - Changer leur mot de passe (l'ancien mot de passe est requis).
            - Désactiver leur compte (nécessite de taper "DEACTIVATE" pour confirmer).

            Les FREELANCERS peuvent aussi modifier :
            - Leurs compétences (skills).
            - Leur tarif horaire (price per hour).
            - Leur bio.

            === PORTFOLIO (FREELANCERS) ===
            - Chaque freelancer peut créer un portfolio.
            - Le portfolio contient des projets (items) avec : titre, description, technologies utilisées, URL du projet, URL GitHub, image.
            - Actions possibles : créer, modifier ou supprimer le portfolio, et ajouter/modifier/supprimer des projets individuels.
            - La suppression du portfolio nécessite de taper "DELETE" pour confirmer.

            === FORUM COMMUNAUTAIRE ===
            Le forum est accessible aux freelancers. Fonctionnalités :
            - Créer un post avec titre, contenu et catégorie.
            - Catégories disponibles : Technology, Design, Business, Career, General.
            - Modifier ou supprimer ses propres posts.
            - Ajouter, modifier ou supprimer des commentaires sur les posts.
            - Réagir avec Like (👍) ou Dislike (👎) à un post (cliquer à nouveau annule la réaction).
            - Joindre des GIFs (via GIPHY) dans les posts et commentaires.
            - Recherche de posts par mot-clé et filtrage par catégorie.
            - Pagination : 10 posts par page.
            - Modération automatique : filtre de mots grossiers + détection de toxicité par IA (Google Perspective API).
            - Les réactions et commentaires génèrent des notifications pour l'auteur du post.

            === MESSAGERIE ===
            - Messagerie privée 1-à-1 entre freelancers.
            - Recherche d'un freelancer par nom pour démarrer une conversation.
            - Liste des conversations triée par messages non lus puis par date du dernier message.
            - Indicateur de statut en ligne (point vert = actif dans les 5 dernières minutes).
            - Badge de messages non lus.
            - Actualisation automatique toutes les 2 secondes (polling en temps réel).
            - Un message ne peut pas dépasser 255 caractères.
            - Les messages envoyés génèrent une notification pour le destinataire.

            === NOTIFICATIONS ===
            - Cloche 🔔 en haut de la page affichant le nombre de notifications non lues.
            - Types de notifications : MESSAGE (nouveau message reçu), LIKE (quelqu'un a aimé votre post), DISLIKE, COMMENT (nouveau commentaire sur votre post).
            - Possibilité de marquer toutes les notifications comme lues en un clic.
            - Chaque notification contient un lien vers la source (post ou conversation).

            === ADMINISTRATION ===
            L'espace admin (réservé aux administrateurs) permet :
            - Voir les statistiques globales : nombre total d'utilisateurs, utilisateurs actifs, répartition par rôle.
            - Gérer les utilisateurs : voir la liste, activer/désactiver un compte, supprimer un utilisateur.
            - Modérer le forum : voir tous les posts, les supprimer, supprimer des commentaires spécifiques.

            === FONCTIONNALITÉS À VENIR ===
            Ces fonctionnalités sont prévues mais pas encore disponibles :
            - Projets disponibles : parcourir des offres de mission.
            - Mes contrats : gérer les contrats signés.
            - Tableau des tâches (Task Board).
            - Paiements et revenus.

            === INFORMATIONS TECHNIQUES UTILES ===
            - Les fichiers uploadables incluent : photo de profil, CV, carte étudiant, images de portfolio.
            - La plateforme fonctionne en rôles : CLIENT, FREELANCER, ADMIN.
            - Toutes les opérations sensibles (suppression, désactivation) sont protégées par confirmation ou token CSRF.
            """;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();
    private final Gson gson = new Gson();

    public record Turn(String role, String text) {
    }

    public String generateResponse(String userMessage, List<Turn> history) throws IOException, InterruptedException {
        String apiKey = readSetting("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = DEFAULT_API_KEY;
        }

        String model = readSetting("GEMINI_MODEL");
        if (model == null || model.isBlank()) {
            model = DEFAULT_MODEL;
        }

        String baseUrl = readSetting("GEMINI_BASE_URL");
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = DEFAULT_BASE_URL;
        }

        JsonObject payload = new JsonObject();
        JsonObject systemInstruction = new JsonObject();
        JsonArray systemParts = new JsonArray();
        JsonObject systemPart = new JsonObject();
        systemPart.addProperty("text", SYSTEM_PROMPT);
        systemParts.add(systemPart);
        systemInstruction.add("parts", systemParts);
        payload.add("systemInstruction", systemInstruction);

        JsonArray contents = new JsonArray();
        List<Turn> safeHistory = history == null ? List.of() : new ArrayList<>(history);
        int startIndex = Math.max(0, safeHistory.size() - 8);
        for (int i = startIndex; i < safeHistory.size(); i++) {
            Turn turn = safeHistory.get(i);
            if (turn == null || turn.text() == null || turn.text().isBlank()) {
                continue;
            }
            contents.add(toContent(turn.role(), turn.text()));
        }
        contents.add(toContent("user", userMessage));
        payload.add("contents", contents);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.7);
        generationConfig.addProperty("maxOutputTokens", 512);
        payload.add("generationConfig", generationConfig);

        String endpoint = normalizeBaseUrl(baseUrl) + "/models/" + model + ":generateContent?key=" + apiKey;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .timeout(Duration.ofSeconds(45))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Gemini API error " + response.statusCode() + ": " + response.body());
        }

        String text = extractText(response.body());
        if (text == null || text.isBlank()) {
            throw new IOException("Gemini returned an empty response.");
        }
        return text.trim();
    }

    private JsonObject toContent(String role, String text) {
        JsonObject content = new JsonObject();
        content.addProperty("role", normalizeRole(role));
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", text);
        parts.add(part);
        content.add("parts", parts);
        return content;
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "user";
        }
        String lower = role.trim().toLowerCase();
        if (lower.equals("assistant") || lower.equals("model")) {
            return "model";
        }
        return "user";
    }

    private String extractText(String responseBody) {
        JsonObject root = gson.fromJson(responseBody, JsonObject.class);
        if (root == null) {
            return null;
        }

        if (root.has("error")) {
            JsonObject error = root.getAsJsonObject("error");
            String message = error != null && error.has("message") ? error.get("message").getAsString() : responseBody;
            throw new IllegalStateException(message);
        }

        JsonArray candidates = root.has("candidates") ? root.getAsJsonArray("candidates") : null;
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
        if (firstCandidate == null || !firstCandidate.has("content")) {
            return null;
        }

        JsonObject content = firstCandidate.getAsJsonObject("content");
        if (content == null || !content.has("parts")) {
            return null;
        }

        JsonArray parts = content.getAsJsonArray("parts");
        StringBuilder builder = new StringBuilder();
        for (JsonElement element : parts) {
            if (element == null || !element.isJsonObject()) {
                continue;
            }
            JsonObject part = element.getAsJsonObject();
            if (part.has("text")) {
                String text = part.get("text").getAsString();
                if (text != null && !text.isBlank()) {
                    if (builder.length() > 0) {
                        builder.append('\n');
                    }
                    builder.append(text.trim());
                }
            }
        }
        return builder.length() == 0 ? null : builder.toString();
    }

    private String normalizeBaseUrl(String baseUrl) {
        String normalized = baseUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String readSetting(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }
        return value;
    }
}