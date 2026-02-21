package uniearn.model.entities;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Post {
    private int id;
    private String title;
    private String content;
    private String authorName;
    private int authorId;
    private LocalDateTime createdAt;
    private List<Comment> comments;
    private int likes;
    private int dislikes;
    private Set<Integer> likedBy;   // in-memory UI tracking
    private Set<Integer> dislikedBy;

    public Post() {
        this.comments = new ArrayList<>();
        this.likedBy = new HashSet<>();
        this.dislikedBy = new HashSet<>();
    }

    public Post(int id, String title, String content, String authorName, int authorId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.authorName = authorName;
        this.authorId = authorId;
        this.createdAt = LocalDateTime.now();
        this.comments = new ArrayList<>();
        this.likes = 0;
        this.dislikes = 0;
        this.likedBy = new HashSet<>();
        this.dislikedBy = new HashSet<>();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public int getDislikes() { return dislikes; }
    public void setDislikes(int dislikes) { this.dislikes = dislikes; }

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }

    public void removeComment(int commentId) {
        this.comments.removeIf(c -> c.getId() == commentId);
    }

    // In-memory like/dislike tracking for UI
    public void toggleLike(int userId) {
        if (likedBy.contains(userId)) {
            likedBy.remove(userId);
        } else {
            likedBy.add(userId);
            dislikedBy.remove(userId);
        }
    }

    public void toggleDislike(int userId) {
        if (dislikedBy.contains(userId)) {
            dislikedBy.remove(userId);
        } else {
            dislikedBy.add(userId);
            likedBy.remove(userId);
        }
    }

    public int getLikeCount() { return likes + likedBy.size(); }
    public int getDislikeCount() { return dislikes + dislikedBy.size(); }
    public boolean isLikedBy(int userId) { return likedBy.contains(userId); }
    public boolean isDislikedBy(int userId) { return dislikedBy.contains(userId); }
    public Set<Integer> getLikedBySet() { return likedBy; }
    public Set<Integer> getDislikedBySet() { return dislikedBy; }
}
