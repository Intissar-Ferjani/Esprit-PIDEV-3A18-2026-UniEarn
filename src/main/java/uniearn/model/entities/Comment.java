package uniearn.model.entities;
import java.time.LocalDateTime;

public class Comment {
    private int id;
    private int postId;
    private String content;
    private String authorName;
    private int authorId;
    private LocalDateTime createdAt;

    public Comment() {}

    public Comment(int id, int postId, String content, String authorName, int authorId) {
        this.id = id;
        this.postId = postId;
        this.content = content;
        this.authorName = authorName;
        this.authorId = authorId;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
