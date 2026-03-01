package uniearn.controller.profile.forum;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import uniearn.model.entities.forum.Comment;
import uniearn.model.entities.forum.NotificationMsg;
import uniearn.model.entities.forum.Post;
import uniearn.services.forum.CategoryService;
import uniearn.services.forum.CommentService;
import uniearn.services.forum.DefaultFreelancerEnsurer;
import uniearn.services.forum.GiphyService;
import uniearn.services.forum.ModerationService;
import uniearn.services.forum.NotificationStore;
import uniearn.services.forum.PostService;
import uniearn.services.forum.ReactionService;
import uniearn.services.forum.WebSocketService;

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
    @FXML
    private HBox postGifContainer;
    @FXML
    private ImageView postGifPreview;
    @FXML
    private ComboBox<String> categoryFilter;
    @FXML
    private ComboBox<String> postCategoryCombo;

    private final PostService postService = new PostService();
    private final CommentService commentService = new CommentService();
    private final ReactionService reactionService = new ReactionService();
    private final CategoryService categoryService = new CategoryService();

    private List<Post> posts = new ArrayList<>();

    // Default user until login is integrated (authorId 0 = "Forum User" from DB)
    private String currentUserName = "Forum User";
    private int currentUserId = 0;

    private int unreadNotificationCount = 0;
    private String selectedPostGifUrl = null;

    @FXML
    public void initialize() {
        DefaultFreelancerEnsurer.ensureDefaultFreelancerExists();
        reloadPostsFromDb();

        // Set up category filter
        if (categoryFilter != null) {
            categoryFilter.getItems().addAll("All", "Technology", "Design", "Business", "Career", "General");
            categoryFilter.setValue("All");
            categoryFilter.setOnAction(e -> displayPosts());
        }
        // Set up category selector for new posts
        if (postCategoryCombo != null) {
            postCategoryCombo.getItems().addAll("Technology", "Design", "Business", "Career", "General");
            postCategoryCombo.setValue("General");
        }
        // Hide GIF preview initially
        if (postGifContainer != null) {
            postGifContainer.setVisible(false);
            postGifContainer.setManaged(false);
        }

        displayPosts();

        // Initialize WebSocket connection for real-time notifications (with retry)
        new Thread(() -> {
            // Retry connection up to 5 times (server may still be starting)
            for (int attempt = 1; attempt <= 5; attempt++) {
                if (WebSocketService.getInstance().isConnected()) break;
                System.out.println("WebSocket connect attempt " + attempt + "...");
                WebSocketService.getInstance().connect(currentUserName);
                if (WebSocketService.getInstance().isConnected()) break;
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            }

            if (WebSocketService.getInstance().isConnected()) {
                System.out.println("✅ WebSocket connected! Subscribing to notifications...");
                WebSocketService.getInstance().subscribe("/topic/notifications", NotificationMsg.class,
                        notification -> Platform.runLater(() -> {
                            NotificationStore.getInstance().add(notification);
                            updateNotificationBadge();
                        }));
            } else {
                System.err.println("⚠ Could not connect to WebSocket after retries. Notifications disabled.");
            }
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
        newPost.setGifUrl(selectedPostGifUrl);
        String selectedCat = (postCategoryCombo != null && postCategoryCombo.getValue() != null)
                ? postCategoryCombo.getValue() : categoryService.categorize(title, content);
        newPost.setCategory(selectedCat);
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
        if (postCategoryCombo != null) postCategoryCombo.setValue("General");
        selectedPostGifUrl = null;
        if (postGifPreview != null) postGifPreview.setImage(null);
        if (postGifContainer != null) {
            postGifContainer.setVisible(false);
            postGifContainer.setManaged(false);
        }
    }

    // ── Display Posts ────────────────────────────────────────────────

    private void displayPosts() {
        postsContainer.getChildren().clear();
        String selectedCategory = categoryFilter != null && categoryFilter.getValue() != null
                ? categoryFilter.getValue() : "All";
        for (Post post : posts) {
            if (!"All".equals(selectedCategory) && !selectedCategory.equals(post.getCategory())) {
                continue;
            }
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
        authorLabel.setStyle("-fx-text-fill: #1a56db;");

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

        // ── Action Buttons: Like, Dislike, Comment ──
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_LEFT);

        int likeCount = reactionService.getReactionCount(post.getId(), "LIKE");
        boolean userLiked = reactionService.hasUserReacted(currentUserId, post.getId(), "LIKE");

        int dislikeCount = reactionService.getReactionCount(post.getId(), "DISLIKE");
        boolean userDisliked = reactionService.hasUserReacted(currentUserId, post.getId(), "DISLIKE");

        Button likeBtn = new Button("👍 " + likeCount);
        likeBtn.setStyle(userLiked
                ? "-fx-background-color: #eff6ff; -fx-text-fill: #1a56db; -fx-cursor: hand; -fx-font-size: 13px; -fx-background-radius: 15; -fx-border-color: #1a56db; -fx-border-radius: 15;"
                : "-fx-background-color: transparent; -fx-text-fill: #3498db; -fx-cursor: hand; -fx-font-size: 13px;");
        likeBtn.setOnAction(e -> {
            reactionService.toggleReaction(currentUserId, post.getId(), "LIKE");
            displayPosts();

            // Send real-time notification if liking (not unliking)
            if (!userLiked && WebSocketService.getInstance().isConnected()) {
                NotificationMsg msg = new NotificationMsg();
                msg.setFromUser(currentUserName);
                msg.setTitle("New Reaction");
                msg.setMessage("liked your post: " + post.getTitle());
                msg.setRecipientId(post.getAuthorName());
                msg.setType("REACTION");
                WebSocketService.getInstance().send("/app/notification", msg);
            }
        });

        Button dislikeBtn = new Button("👎 " + dislikeCount);
        dislikeBtn.setStyle(userDisliked
                ? "-fx-background-color: #fef2f2; -fx-text-fill: #dc2626; -fx-cursor: hand; -fx-font-size: 13px; -fx-background-radius: 15; -fx-border-color: #dc2626; -fx-border-radius: 15;"
                : "-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-cursor: hand; -fx-font-size: 13px;");
        dislikeBtn.setOnAction(e -> {
            reactionService.toggleReaction(currentUserId, post.getId(), "DISLIKE");
            displayPosts();

            // Send real-time notification if disliking (not un-disliking)
            if (!userDisliked && WebSocketService.getInstance().isConnected()) {
                NotificationMsg msg = new NotificationMsg();
                msg.setFromUser(currentUserName);
                msg.setTitle("New Reaction");
                msg.setMessage("disliked your post: " + post.getTitle());
                msg.setRecipientId(post.getAuthorName());
                msg.setType("REACTION");
                WebSocketService.getInstance().send("/app/notification", msg);
            }
        });

        Button commentBtn = new Button("💬 " + post.getComments().size() + " Comments");
        commentBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #3498db; " +
                "-fx-cursor: hand; -fx-font-size: 13px;");
        commentBtn.setOnAction(e -> toggleComments(card, post));

        actions.getChildren().addAll(likeBtn, dislikeBtn, commentBtn);

        card.getChildren().add(header);

        // ── Category Badge ──
        if (post.getCategory() != null && !post.getCategory().isEmpty() && !"General".equals(post.getCategory())) {
            Label categoryLabel = new Label("📁 " + post.getCategory());
            categoryLabel.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #1a56db; " +
                    "-fx-padding: 3 10 3 10; -fx-background-radius: 12; -fx-font-size: 11px;");
            card.getChildren().add(categoryLabel);
        }

        card.getChildren().addAll(titleLabel, contentLabel);

        // ── GIF / Image (if present, clickable to open full size) ──
        if (post.getGifUrl() != null && !post.getGifUrl().isEmpty()) {
            ImageView gifView = new ImageView();
            gifView.setFitWidth(150);
            gifView.setPreserveRatio(true);
            gifView.setCursor(javafx.scene.Cursor.HAND);
            gifView.setOnMouseClicked(ev -> openImagePopup(post.getGifUrl()));
            loadImageInto(post.getGifUrl(), gifView);
            card.getChildren().add(gifView);
        }

        card.getChildren().addAll(new Separator(), actions);
        return card;
    }

    private void startEditPost(VBox card, Post post) {
        // Replace title and content (indices 1 and 2) with editable fields
        TextField titleField = new TextField(post.getTitle());
        titleField.setStyle(
                "-fx-font-size: 14px; -fx-padding: 6; -fx-border-color: #bfdbfe; -fx-border-radius: 5; -fx-background-radius: 5;");
        titleField.setPromptText("Post title");

        TextArea contentArea = new TextArea(post.getContent());
        contentArea.setWrapText(true);
        contentArea.setPrefRowCount(4);
        contentArea.setStyle(
                "-fx-font-size: 14px; -fx-border-color: #bfdbfe; -fx-border-radius: 5; -fx-background-radius: 5;");

        HBox btnRow = new HBox(10);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        Button saveBtn = new Button("Save");
        saveBtn.setStyle(
                "-fx-background-color: #1a56db; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5; -fx-font-size: 12px;");
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
        commentField.setStyle("-fx-border-color: #bfdbfe; -fx-border-radius: 5; -fx-background-radius: 5;");
        HBox.setHgrow(commentField, Priority.ALWAYS);

        Button gifBtn = new Button("GIF");
        gifBtn.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");
        gifBtn.setTooltip(new Tooltip("Search and add a GIF"));
        gifBtn.setOnAction(e -> openGiphyPicker(commentField));

        Button sendBtn = new Button("Send");
        sendBtn.setStyle("-fx-background-color: #1a56db; -fx-text-fill: white; " +
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
        authorLabel.setStyle("-fx-text-fill: #1a56db;");

        Label timeLabel = new Label(comment.getCreatedAt() != null ? formatDateTime(comment.getCreatedAt()) : "");
        timeLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 11px;");

        topRow.getChildren().addAll(authorLabel, timeLabel);

        // Comment content (text + GIF if present)
        VBox contentBox = new VBox(4);
        String content = comment.getContent();
        if (GiphyService.isGiphyUrl(content)) {
            ImageView gifView = new ImageView();
            gifView.setFitWidth(120);
            gifView.setPreserveRatio(true);
            gifView.setCursor(javafx.scene.Cursor.HAND);
            final String imgUrl = content;
            gifView.setOnMouseClicked(ev -> openImagePopup(imgUrl));
            loadImageInto(content, gifView);
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
                ImageView gifView = new ImageView();
                gifView.setFitWidth(120);
                gifView.setPreserveRatio(true);
                gifView.setCursor(javafx.scene.Cursor.HAND);
                final String clickUrl = giphyUrl;
                gifView.setOnMouseClicked(ev -> openImagePopup(clickUrl));
                loadImageInto(giphyUrl, gifView);
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
        editField.setStyle("-fx-border-color: #bfdbfe; -fx-border-radius: 5; -fx-background-radius: 5;");

        HBox btnRow = new HBox(8);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-background-color: #1a56db; -fx-text-fill: white; " +
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

    // ── GIF Picker for Posts ─────────────────────────────────────────

    @FXML
    private void openGiphyPickerForPost() {
        Stage pickerStage = new Stage();
        pickerStage.setTitle("Add GIF to Post");
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: white;");

        HBox searchBox = new HBox(8);
        TextField searchField = new TextField();
        searchField.setPromptText("Search GIFs...");
        searchField.setPrefWidth(250);
        Button searchBtn = new Button("Search");
        searchBtn.setStyle("-fx-background-color: #1a56db; -fx-text-fill: white;");
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
                ImageView iv = new ImageView();
                iv.setFitWidth(100);
                iv.setPreserveRatio(true);
                iv.setStyle("-fx-cursor: hand;");
                loadGifInto(url, iv);
                iv.setOnMouseClicked(event -> {
                    selectedPostGifUrl = url;
                    if (postGifPreview != null) {
                        loadGifInto(url, postGifPreview);
                    }
                    if (postGifContainer != null) {
                        postGifContainer.setVisible(true);
                        postGifContainer.setManaged(true);
                    }
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

    @FXML
    private void removePostGif() {
        selectedPostGifUrl = null;
        if (postGifPreview != null) postGifPreview.setImage(null);
        if (postGifContainer != null) {
            postGifContainer.setVisible(false);
            postGifContainer.setManaged(false);
        }
    }

    @FXML
    private void uploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select an Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp")
        );
        Stage stage = (Stage) postsContainer.getScene().getWindow();
        java.io.File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            selectedPostGifUrl = selectedFile.toURI().toString();
            if (postGifPreview != null) {
                postGifPreview.setImage(new Image(selectedPostGifUrl, 120, 0, true, true));
            }
            if (postGifContainer != null) {
                postGifContainer.setVisible(true);
                postGifContainer.setManaged(true);
            }
        }
    }

    // ── Navigation ───────────────────────────────────────────────────

    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/freelancer/Freelancer.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) postsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load Dashboard page!");
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) postsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load Login page!");
        }
    }

    @FXML
    private void openMessages() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/freelancer/Messages.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/freelancer/Notifications.fxml"));
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
        searchBtn.setStyle("-fx-background-color: #1a56db; -fx-text-fill: white;");
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
                ImageView iv = new ImageView();
                iv.setFitWidth(100);
                iv.setPreserveRatio(true);
                iv.setStyle("-fx-cursor: hand;");
                loadGifInto(url, iv);
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
    /**
     * Load a GIF image asynchronously using GiphyService.loadImage (HttpClient)
     * and set it on the given ImageView once downloaded.
     */
    /**
     * Load an image (GIF URL or local file URI) into an ImageView.
     * Handles both http(s) URLs via GiphyService and local file:// URIs.
     */
    private void loadImageInto(String url, ImageView view) {
        if (url == null || url.isEmpty()) return;
        if (url.startsWith("file:")) {
            try {
                Image img = new Image(url, view.getFitWidth(), 0, true, true);
                view.setImage(img);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            loadGifInto(url, view);
        }
    }

    private void loadGifInto(String url, ImageView view) {
        if (url == null || url.isEmpty()) return;
        new Thread(() -> {
            Image img = GiphyService.loadImage(url);
            if (img != null) {
                Platform.runLater(() -> view.setImage(img));
            }
        }).start();
    }
    private String formatDateTime(java.time.LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
        return dateTime.format(formatter);
    }

    /**
     * Open a popup window showing the image at full size.
     */
    private void openImagePopup(String imageUrl) {
        try {
            Stage popup = new Stage();
            popup.setTitle("Image Preview");

            ImageView fullView = new ImageView();
            fullView.setPreserveRatio(true);
            fullView.setFitWidth(600);
            fullView.setFitHeight(500);

            // Load the image
            if (imageUrl.startsWith("file:")) {
                fullView.setImage(new Image(imageUrl, 600, 500, true, true));
            } else {
                new Thread(() -> {
                    Image img = GiphyService.loadImage(imageUrl);
                    if (img != null) Platform.runLater(() -> fullView.setImage(img));
                }).start();
            }

            ScrollPane scrollPane = new ScrollPane(fullView);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setStyle("-fx-background-color: #1a1a2e;");

            Scene scene = new Scene(scrollPane, 650, 550);
            popup.setScene(scene);
            popup.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.equals("Error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}