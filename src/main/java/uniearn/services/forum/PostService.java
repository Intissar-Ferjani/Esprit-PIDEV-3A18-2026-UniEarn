package uniearn.services.forum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import uniearn.database.MyConnection;
import uniearn.model.entities.forum.Post;

public class PostService {

    private final Connection cn = MyConnection.getInstance().getCnx();

    public PostService() {
        ensureViewsColumn();
    }

    /** Add views column if it doesn't exist yet */
    private void ensureViewsColumn() {
        if (cn == null) return;
        try {
            ResultSet rs = cn.getMetaData().getColumns(null, null, "freelancer_forum_post", "views");
            if (!rs.next()) {
                cn.createStatement().executeUpdate(
                    "ALTER TABLE freelancer_forum_post ADD COLUMN views INT NOT NULL DEFAULT 0");
                System.out.println("Added 'views' column to freelancer_forum_post");
            }
        } catch (SQLException e) {
            System.out.println("Views column check: " + e.getMessage());
        }
    }

    /**
     * Insert a new post. Returns the generated post_id.
     * Schema: freelancer_forum_post (post_id, freelancer_id, title, content, created_at, updated_at)
     */
    public int addPost(Post post) throws SQLException {
        if (cn == null) throw new SQLException("Database not connected.");
        String sql = "INSERT INTO freelancer_forum_post (freelancer_id, title, content, gif_url, category, updated_at) VALUES (?, ?, ?, ?, ?, NOW())";
        PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, post.getAuthorId());
        ps.setString(2, post.getTitle());
        ps.setString(3, post.getContent());
        ps.setString(4, post.getGifUrl());
        ps.setString(5, post.getCategory());
        ps.executeUpdate();

        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) {
            return keys.getInt(1);
        }
        return -1;
    }

    /**
     * Get all posts, newest first. Schema uses post_id, created_at, updated_at.
     */
    public List<Post> getAllPosts() {
        List<Post> posts = new ArrayList<>();
        if (cn == null) return posts;
        String sql = "SELECT p.post_id, p.freelancer_id, p.title, p.content, p.gif_url, p.category, p.views, p.created_at, p.updated_at, "
                + "u.name AS author_name "
                + "FROM freelancer_forum_post p "
                + "LEFT JOIN freelancer f ON p.freelancer_id = f.idFreelancer "
                + "LEFT JOIN user u ON f.idUser = u.idUser "
                + "ORDER BY p.post_id DESC";
        try {
            Statement st = cn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Post post = new Post();
                post.setId(rs.getInt("post_id"));
                post.setAuthorId(rs.getInt("freelancer_id"));
                post.setTitle(rs.getString("title"));
                post.setContent(rs.getString("content"));
                post.setGifUrl(rs.getString("gif_url"));
                post.setCategory(rs.getString("category"));
                post.setViews(rs.getInt("views"));
                String authorName = rs.getString("author_name");
                post.setAuthorName(authorName != null ? authorName : "Forum User");
                Timestamp ts = rs.getTimestamp("updated_at");
                if (ts != null) {
                    post.setCreatedAt(ts.toLocalDateTime());
                } else {
                    Timestamp created = rs.getTimestamp("created_at");
                    if (created != null) {
                        post.setCreatedAt(created.toLocalDateTime());
                    }
                }
                posts.add(post);
            }
        } catch (SQLException e) {
            System.out.println("Error loading posts: " + e.getMessage());
        }
        return posts;
    }

    /**
     * Update a post's title and content. Also updates updated_at.
     */
    public void updatePost(int postId, String title, String content) {
        if (cn == null) return;
        String sql = "UPDATE freelancer_forum_post SET title = ?, content = ?, updated_at = NOW() WHERE post_id = ?";
        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, title);
            ps.setString(2, content);
            ps.setInt(3, postId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating post: " + e.getMessage());
        }
    }

    /**
     * Increment the view count for a post.
     */
    public void incrementViews(int postId) {
        if (cn == null) return;
        String sql = "UPDATE freelancer_forum_post SET views = views + 1 WHERE post_id = ?";
        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, postId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error incrementing views: " + e.getMessage());
        }
    }

    /**
     * Delete a post by post_id. Comments and reactions are cascade-deleted by the DB.
     */
    public void deletePost(int postId) {
        if (cn == null) return;
        String sql = "DELETE FROM freelancer_forum_post WHERE post_id = ?";
        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, postId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting post: " + e.getMessage());
        }
    }
}
