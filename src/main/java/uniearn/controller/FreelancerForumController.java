package uniearn.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.application.Platform;

import uniearn.model.entities.Post;
import uniearn.model.entities.Comment;
import uniearn.services.PostService;
import uniearn.services.CommentService;
import uniearn.services.ReactionService;
import uniearn.services.DefaultFreelancerEnsurer;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import uniearn.services.WebSocketService;
import uniearn.services.GiphyService;
import uniearn.services.ModerationService;
import uniearn.model.dto.NotificationMsg;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class FreelancerForumController {

    @FXML
    private TextField postTitleField;
    @FXML
    private TextArea postContentArea;
    @FXML
    private VBox postsContainer;
    @FXML
    private Label notificationBadge;
    @FXML
    private Button messageButton;
    @FXML
    private Button notificationButton;

    private final PostService postService = new PostService();
    private final CommentService commentService = new CommentService();
    private final ReactionService reactionService = new ReactionService();

    private List<Post> posts = new ArrayList<>();

    // Default user until login is integrated (authorId 0 = "Forum User" from DB)
    private String currentUserName = "Forum User";
    private int currentUserId = 0;

    private int unreadNotificationCount = 0;

    @FXML
    public void initialize() {
        DefaultFreelancerEnsurer.ensureDefaultFreelancerExists();
        reloadPostsFromDb();
        displayPosts();

        // Initialize WebSocket connection for real-time notifications
        new Thread(() -> {
            WebSocketService.getInstance().connect(currentUserName);
            WebSocketService.getInstance().subscribe("/topic/notifications", NotificationMsg.class,
                    notification -> Platform.runLater(() -> {
                        updateNotificationBadge();
                        showAlert(notification.getTitle() != null ? notification.getTitle() : "Notification",
                                (notification.getFromUser() != null ? notification.getFromUser() + ": " : "")
                                        + (notification.getMessage() != null ? notification.getMessage() : ""));
                    }));
        }).start();
    }

    /**
     * Load posts from freelancer_forum_post and their comments from
     * freelancer_forum_comment.
     */
    private void reloadPostsFromDb() {
        posts = postService.getAllPosts();
        for (Post post : posts) {
            List<Comment> comments = commentService.getCommentsByPostId(post.getId());
            post.getComments().clear();
            post.getComments().addAll(comments);
        }
    }

    // ── Create / Clear Post ──────────────────────────────────────────

    @FXML
    private void createPost() {
        String title = postTitleField.getText().trim();
        String content = postContentArea.getText().trim();

        if (title.isEmpty() || content.isEmpty()) {
            showAlert("Error", "Please fill in both title and content!");
            return;
        }

        // Validation: Title must contain only letters and spaces
        if (!title.matches("^[a-zA-Z\\s]+$")) {
            showAlert("Error", "Title must contain only letters and spaces (no numbers or symbols)!");
            return;
        }

        // Validation: Content must be at least 10 characters
        if (content.length() < 10) {
            showAlert("Error", "Post content must be at least 10 characters long!");
            return;
        }

        // Détection de contenus toxiques
        ModerationService moderation = new ModerationService();
        if (!moderation.isContentClean(title) || !moderation.isContentClean(content)) {
            String badInTitle = moderation.getFirstBadWord(title);
            String badInContent = moderation.getFirstBadWord(content);
            String bad = badInTitle != null ? badInTitle : badInContent;
            showAlert("Contenu non autorisé",
                    "Votre publication contient un mot inapproprié (\"" + bad + "\"). " +
                            "Veuillez modifier le titre ou le contenu avant de publier.");
            return;
        }

        Post newPost = new Post();
        newPost.setTitle(title);
        newPost.setContent(content);
        newPost.setAuthorName(currentUserName);
        newPost.setAuthorId(currentUserId);
        try {
            postService.addPost(newPost);
            reloadPostsFromDb();
            displayPosts();
            clearPost();
            showAlert("Success", "Post created successfully!");
        } catch (SQLException e) {
            showAlert("Error", "Could not save post: " + e.getMessage());
        }
    }

    @FXML
    private void clearPost() {
        postTitleField.clear();
        postContentArea.clear();
    }

    // ── Display Posts ────────────────────────────────────────────────

    private void displayPosts() {
        postsContainer.getChildren().clear();
        for (Post post : posts) {
            postsContainer.getChildren().add(createPostCard(post));
        }
    }

    private VBox createPostCard(Post post) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; " +
                "-fx-border-color: #c8e6c9; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        // ── Header: Author + Time + optional Delete ──
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label authorLabel = new Label(post.getAuthorName());
        authorLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        authorLabel.setStyle("-fx-text-fill: #1a7a4c;");

        Label timeLabel = new Label("• " + (post.getCreatedAt() != null ? formatDateTime(post.getCreatedAt()) : ""));
        timeLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(authorLabel, timeLabel, spacer);

        // Edit / Delete buttons — only for the post author
        if (post.getAuthorId() == currentUserId) {
            Button editPostBtn = new Button("✏ Edit");
            editPostBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #3498db; " +
                    "-fx-cursor: hand; -fx-font-size: 12px;");
            editPostBtn.setOnAction(e -> startEditPost(card, post));

            Button deletePostBtn = new Button("🗑 Delete");
            deletePostBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; " +
                    "-fx-cursor: hand; -fx-font-size: 12px;");
            deletePostBtn.setOnAction(e -> {
                postService.deletePost(post.getId());
                reloadPostsFromDb();
                displayPosts();
            });
            header.getChildren().addAll(editPostBtn, deletePostBtn);
        }

        // ── Title ──
        Label titleLabel = new Label(post.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");
        titleLabel.setWrapText(true);

        // ── Content ──
        Label contentLabel = new Label(post.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");

        // ── Action Buttons: Like (from freelancer_forum_reaction), Comment ──
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_LEFT);

        int reactionCount = reactionService.getReactionCount(post.getId());
        boolean userReacted = reactionService.hasUserReacted(currentUserId, post.getId());

        Button likeBtn = new Button("👍 " + reactionCount);
        likeBtn.setStyle(userReacted
                ? "-fx-background-color: #e8f5e9; -fx-text-fill: #1a7a4c; -fx-cursor: hand; -fx-font-size: 13px; -fx-background-radius: 15; -fx-border-color: #1a7a4c; -fx-border-radius: 15;"
                : "-fx-background-color: transparent; -fx-text-fill: #3498db; -fx-cursor: hand; -fx-font-size: 13px;");
        likeBtn.setOnAction(e -> {
            reactionService.toggleReaction(currentUserId, post.getId());
            displayPosts();

            // Send real-time notification if liking (not unliking)
            if (!userReacted && WebSocketService.getInstance().isConnected()) {
                NotificationMsg msg = new NotificationMsg();
                msg.setFromUser(currentUserName);
                msg.setTitle("New Reaction");
                msg.setMessage("reacted to your post: " + post.getTitle());
                msg.setRecipientId(post.getAuthorName());
                msg.setType("REACTION");
                WebSocketService.getInstance().send("/app/notification", msg);
            }
        });

        Button commentBtn = new Button("💬 " + post.getComments().size() + " Comments");
        commentBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #3498db; " +
                "-fx-cursor: hand; -fx-font-size: 13px;");
        commentBtn.setOnAction(e -> toggleComments(card, post));

        actions.getChildren().addAll(likeBtn, commentBtn);

        card.getChildren().addAll(header, titleLabel, contentLabel, new Separator(), actions);
        return card;
    }

    private void startEditPost(VBox card, Post post) {
        // Replace title and content (indices 1 and 2) with editable fields
        TextField titleField = new TextField(post.getTitle());
        titleField.setStyle(
                "-fx-font-size: 14px; -fx-padding: 6; -fx-border-color: #a5d6a7; -fx-border-radius: 5; -fx-background-radius: 5;");
        titleField.setPromptText("Post title");

        TextArea contentArea = new TextArea(post.getContent());
        contentArea.setWrapText(true);
        contentArea.setPrefRowCount(4);
        contentArea.setStyle(
                "-fx-font-size: 14px; -fx-border-color: #a5d6a7; -fx-border-radius: 5; -fx-background-radius: 5;");

        HBox btnRow = new HBox(10);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        Button saveBtn = new Button("Save");
        saveBtn.setStyle(
                "-fx-background-color: #1a7a4c; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5; -fx-font-size: 12px;");
        saveBtn.setOnAction(e -> {
            String newTitle = titleField.getText().trim();
            String newContent = contentArea.getText().trim();
            if (!newTitle.isEmpty() && !newContent.isEmpty()) {
                ModerationService modEdit = new ModerationService();
                if (!modEdit.isContentClean(newTitle) || !modEdit.isContentClean(newContent)) {
                    String bad = modEdit.getFirstBadWord(newTitle);
                    if (bad == null) bad = modEdit.getFirstBadWord(newContent);
                    showAlert("Contenu non autorisé",
                            "Votre publication contient un mot inapproprié (\"" + bad + "\"). Veuillez modifier.");
                    return;
                }
                postService.updatePost(post.getId(), newTitle, newContent);
                reloadPostsFromDb();
                displayPosts();
            } else {
                showAlert("Error", "Title and content cannot be empty.");
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(
                "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5; -fx-font-size: 12px;");
        cancelBtn.setOnAction(e -> displayPosts());

        btnRow.getChildren().addAll(saveBtn, cancelBtn);

        VBox editForm = new VBox(10);
        editForm.getChildren().addAll(titleField, contentArea, btnRow);

        card.getChildren().remove(2); // content label
        card.getChildren().remove(1); // title label
        card.getChildren().add(1, editForm);
    }

    // ── Comments Section ─────────────────────────────────────────────

    private void toggleComments(VBox postCard, Post post) {
        boolean commentsVisible = postCard.getChildren().stream()
                .anyMatch(node -> node instanceof VBox && "commentsSection".equals(node.getId()));

        if (commentsVisible) {
            postCard.getChildren().removeIf(node -> node instanceof VBox && "commentsSection".equals(node.getId()));
        } else {
            VBox commentsSection = createCommentsSection(post);
            commentsSection.setId("commentsSection");
            postCard.getChildren().add(commentsSection);
        }
    }

    private VBox createCommentsSection(Post post) {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10, 0, 0, 20));
        section.setStyle("-fx-background-color: #f0f7f4; -fx-padding: 15; -fx-background-radius: 5;");

        for (Comment comment : post.getComments()) {
            section.getChildren().add(createCommentBox(post, comment));
            section.getChildren().add(new Separator());
        }

        // New comment input with GIF support
        HBox newCommentBox = new HBox(10);
        newCommentBox.setAlignment(Pos.CENTER_LEFT);

        TextField commentField = new TextField();
        commentField.setPromptText("Write a comment or add a GIF...");
        commentField.setPrefWidth(350);
        commentField.setStyle("-fx-border-color: #a5d6a7; -fx-border-radius: 5; -fx-background-radius: 5;");
        HBox.setHgrow(commentField, Priority.ALWAYS);

        Button gifBtn = new Button("GIF");
        gifBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");
        gifBtn.setTooltip(new Tooltip("Search and add a GIF"));
        gifBtn.setOnAction(e -> openGiphyPicker(commentField));

        Button sendBtn = new Button("Send");
        sendBtn.setStyle("-fx-background-color: #1a7a4c; -fx-text-fill: white; " +
                "-fx-cursor: hand; -fx-background-radius: 5;");

        sendBtn.setOnAction(e -> {
            String text = commentField.getText().trim();
            if (!text.isEmpty()) {
                // Validation: min 2 chars for text, or allow GIF URLs (which are long)
                if (text.length() < 2 && !GiphyService.isGiphyUrl(text)) {
                    showAlert("Error", "Comment must be at least 2 characters!");
                    return;
                }
                // Détection de contenus toxiques dans le commentaire
                ModerationService moderation = new ModerationService();
                if (!moderation.isContentClean(text)) {
                    String bad = moderation.getFirstBadWord(text);
                    showAlert("Contenu non autorisé",
                            "Votre commentaire contient un mot inapproprié (\"" + bad + "\"). " +
                                    "Veuillez modifier votre message.");
                    return;
                }
                Comment comment = new Comment();
                comment.setPostId(post.getId());
                comment.setContent(text);
                comment.setAuthorName(currentUserName);
                comment.setAuthorId(currentUserId);
                try {
                    commentService.addComment(comment);
                    reloadPostsFromDb();
                    displayPosts();
                    commentField.clear();

                    // Send real-time notification to post author
                    if (WebSocketService.getInstance().isConnected()) {
                        NotificationMsg msg = new NotificationMsg();
                        msg.setFromUser(currentUserName);
                        msg.setTitle("New Comment");
                        msg.setMessage("commented on your post: " + post.getTitle());
                        msg.setRecipientId(post.getAuthorName());
                        msg.setType("COMMENT");
                        WebSocketService.getInstance().send("/app/notification", msg);
                    }

                } catch (SQLException ex) {
                    showAlert("Error", "Could not save comment: " + ex.getMessage());
                }
            }
        });

        newCommentBox.getChildren().addAll(commentField, gifBtn, sendBtn);
        section.getChildren().add(newCommentBox);

        return section;
    }

    private VBox createCommentBox(Post post, Comment comment) {
        VBox box = new VBox(5);

        // Author + time row
        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label authorLabel = new Label(comment.getAuthorName());
        authorLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        authorLabel.setStyle("-fx-text-fill: #1a7a4c;");

        Label timeLabel = new Label(comment.getCreatedAt() != null ? formatDateTime(comment.getCreatedAt()) : "");
        timeLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 11px;");

        topRow.getChildren().addAll(authorLabel, timeLabel);

        // Comment content (text + GIF if present)
        VBox contentBox = new VBox(4);
        String content = comment.getContent();
        if (GiphyService.isGiphyUrl(content)) {
            ImageView gifView = new ImageView(new Image(content, true));
            gifView.setFitWidth(200);
            gifView.setPreserveRatio(true);
            contentBox.getChildren().add(gifView);
        } else {
            String giphyUrl = extractGiphyUrl(content);
            if (giphyUrl != null) {
                String textPart = content.replace(giphyUrl, "").trim();
                if (!textPart.isEmpty()) {
                    Label textLabel = new Label(textPart);
                    textLabel.setWrapText(true);
                    textLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: black;");
                    contentBox.getChildren().add(textLabel);
                }
                ImageView gifView = new ImageView(new Image(giphyUrl, true));
                gifView.setFitWidth(200);
                gifView.setPreserveRatio(true);
                contentBox.getChildren().add(gifView);
            } else {
                Label contentLabel = new Label(content);
                contentLabel.setWrapText(true);
                contentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: black;");
                contentBox.getChildren().add(contentLabel);
            }
        }
        box.getChildren().addAll(topRow, contentBox);

        // Edit / Delete buttons — only for the comment author
        if (comment.getAuthorId() == currentUserId) {
            HBox actionRow = new HBox(10);
            actionRow.setAlignment(Pos.CENTER_LEFT);

            Button editBtn = new Button("✏ Edit");
            editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #3498db; " +
                    "-fx-cursor: hand; -fx-font-size: 11px;");
            editBtn.setOnAction(e -> startEditComment(box, post, comment));

            Button deleteBtn = new Button("🗑 Delete");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; " +
                    "-fx-cursor: hand; -fx-font-size: 11px;");
            deleteBtn.setOnAction(e -> {
                commentService.deleteComment(comment.getId());
                reloadPostsFromDb();
                displayPosts();
            });

            actionRow.getChildren().addAll(editBtn, deleteBtn);
            box.getChildren().add(actionRow);
        }

        return box;
    }

    private void startEditComment(VBox commentBox, Post post, Comment comment) {
        // Replace comment content with an editable text field
        commentBox.getChildren().clear();

        TextField editField = new TextField(comment.getContent());
        editField.setStyle("-fx-border-color: #a5d6a7; -fx-border-radius: 5; -fx-background-radius: 5;");

        HBox btnRow = new HBox(8);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-background-color: #1a7a4c; -fx-text-fill: white; " +
                "-fx-cursor: hand; -fx-background-radius: 5; -fx-font-size: 11px;");
        saveBtn.setOnAction(e -> {
            String newText = editField.getText().trim();
            if (!newText.isEmpty()) {
                ModerationService modEdit = new ModerationService();
                if (!modEdit.isContentClean(newText)) {
                    String bad = modEdit.getFirstBadWord(newText);
                    showAlert("Contenu non autorisé",
                            "Votre commentaire contient un mot inapproprié (\"" + bad + "\"). Veuillez modifier.");
                    return;
                }
                commentService.updateComment(comment.getId(), newText);
                reloadPostsFromDb();
                displayPosts();
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                "-fx-cursor: hand; -fx-background-radius: 5; -fx-font-size: 11px;");
        cancelBtn.setOnAction(e -> displayPosts());

        btnRow.getChildren().addAll(saveBtn, cancelBtn);
        commentBox.getChildren().addAll(editField, btnRow);
    }

    // ── Navigation ───────────────────────────────────────────────────

    @FXML
    private void openMessages() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/Freelancer/Messages.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) messageButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Messages");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load Messages page!");
        }
    }

    @FXML
    private void openNotifications() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/Freelancer/Notifications.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) notificationButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Notifications");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load Notifications page!");
        }
    }

    private void openGiphyPicker(TextField commentField) {
        Stage pickerStage = new Stage();
        pickerStage.setTitle("Search GIFs");
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: white;");

        HBox searchBox = new HBox(8);
        TextField searchField = new TextField();
        searchField.setPromptText("Search GIFs...");
        searchField.setPrefWidth(250);
        Button searchBtn = new Button("Search");
        searchBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white;");
        searchBox.getChildren().addAll(searchField, searchBtn);

        FlowPane resultsPane = new FlowPane(8, 8);
        resultsPane.setPrefWidth(450);
        resultsPane.setPrefHeight(300);

        Runnable doSearch = () -> {
            resultsPane.getChildren().clear();
            String q = searchField.getText().trim();
            if (q.isEmpty()) q = "fun";
            List<String> urls = new GiphyService().searchGifs(q);
            for (String url : urls) {
                ImageView iv = new ImageView(new Image(url, true));
                iv.setFitWidth(100);
                iv.setPreserveRatio(true);
                iv.setStyle("-fx-cursor: hand;");
                iv.setOnMouseClicked(event -> {
                    String current = commentField.getText();
                    commentField.setText(current + (current.isEmpty() ? "" : " ") + url);
                    pickerStage.close();
                });
                resultsPane.getChildren().add(iv);
            }
        };
        searchBtn.setOnAction(e -> doSearch.run());
        searchField.setOnAction(e -> doSearch.run());

        root.getChildren().addAll(searchBox, new ScrollPane(resultsPane));
        pickerStage.setScene(new Scene(root, 470, 380));
        pickerStage.show();
        doSearch.run();
    }

    private String extractGiphyUrl(String content) {
        if (content == null) return null;
        int idx = content.indexOf("https://");
        if (idx < 0) idx = content.indexOf("http://");
        if (idx < 0) return null;
        int end = idx;
        while (end < content.length() && !Character.isWhitespace(content.charAt(end))) end++;
        String url = content.substring(idx, end);
        return GiphyService.isGiphyUrl(url) ? url : null;
    }

    private void updateNotificationBadge() {
        unreadNotificationCount++;
        if (notificationBadge != null) {
            notificationBadge.setText(String.valueOf(unreadNotificationCount));
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private String formatDateTime(java.time.LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
        return dateTime.format(formatter);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.equals("Error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}