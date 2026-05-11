package uniearn.controller.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import uniearn.services.ai.GeminiChatService;

public class ChatbotWidgetController {

    @FXML
    private VBox chatbotRoot;
    @FXML
    private VBox chatMessages;
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    private TextField messageInput;
    @FXML
    private Label statusLabel;
    @FXML
    private Button sendButton;

    private final GeminiChatService geminiChatService = new GeminiChatService();
    private final List<GeminiChatService.Turn> conversationHistory = new ArrayList<>();

    @FXML
    public void initialize() {
        appendAssistantMessage("Bonjour, je suis UniBuddy. Je peux vous aider sur UniEarn : tableaux de bord, profils, forum, messagerie, contrats et paiements.");
        updateStatus("Prêt");
    }

    @FXML
    private void handleSendMessage() {
        String userMessage = messageInput.getText() == null ? "" : messageInput.getText().trim();
        if (userMessage.isBlank()) {
            updateStatus("Message vide");
            return;
        }

        messageInput.clear();
        appendUserMessage(userMessage);
        updateStatus("UniBuddy réfléchit...");
        setInputDisabled(true);

        List<GeminiChatService.Turn> requestHistory = new ArrayList<>(conversationHistory);
        CompletableFuture.supplyAsync(() -> {
            try {
                return geminiChatService.generateResponse(userMessage, requestHistory);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).whenComplete((response, throwable) -> Platform.runLater(() -> {
            setInputDisabled(false);
            if (throwable != null) {
                updateStatus("Erreur Gemini");
                appendAssistantMessage("Je n'ai pas pu joindre Gemini pour le moment. Vérifiez la clé API et la connexion réseau.");
                return;
            }

            String assistantMessage = response == null || response.isBlank()
                    ? "Je n'ai pas pu générer de réponse."
                    : response.trim();
            appendAssistantMessage(assistantMessage);
            updateStatus("Prêt");
        }));
    }

    @FXML
    private void handleCloseWidget() {
        if (chatbotRoot != null) {
            chatbotRoot.setVisible(false);
            chatbotRoot.setManaged(false);
        }
    }

    public void focusInput() {
        if (messageInput != null) {
            Platform.runLater(messageInput::requestFocus);
        }
    }

    private void appendUserMessage(String message) {
        conversationHistory.add(new GeminiChatService.Turn("user", message));
        if (chatMessages != null) {
            chatMessages.getChildren().add(createBubble(message, true));
            scrollToBottom();
        }
    }

    private void appendAssistantMessage(String message) {
        conversationHistory.add(new GeminiChatService.Turn("model", message));
        if (chatMessages != null) {
            chatMessages.getChildren().add(createBubble(message, false));
            scrollToBottom();
        }
    }

    private HBox createBubble(String message, boolean isUser) {
        Label textLabel = new Label(message);
        textLabel.setWrapText(true);
        textLabel.setMaxWidth(250);
        textLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + (isUser ? "white" : "#0f172a") + ";");

        VBox bubble = new VBox(textLabel);
        bubble.setPadding(new Insets(12, 14, 12, 14));
        bubble.setMaxWidth(280);
        bubble.setStyle("-fx-background-radius: 16; -fx-background-color: "
                + (isUser ? "linear-gradient(to right, #0d6efd, #2563eb)" : "#edf2ff")
                + "; -fx-border-radius: 16;");

        HBox wrapper = new HBox();
        wrapper.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        wrapper.setFillHeight(false);
        wrapper.setMaxWidth(Double.MAX_VALUE);

        if (isUser) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            wrapper.getChildren().addAll(spacer, bubble);
        } else {
            wrapper.getChildren().add(bubble);
        }

        return wrapper;
    }

    private void setInputDisabled(boolean disabled) {
        if (messageInput != null) {
            messageInput.setDisable(disabled);
        }
        if (sendButton != null) {
            sendButton.setDisable(disabled);
        }
    }

    private void updateStatus(String text) {
        if (statusLabel != null) {
            statusLabel.setText(text);
        }
    }

    private void scrollToBottom() {
        if (chatScrollPane != null) {
            Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
        }
    }
}