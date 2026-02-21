package uniearn.services;

import uniearn.database.MyConnection;
import uniearn.model.entities.Post;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostService {

    private final Connection cn = MyConnection.getInstance().getCnx();

    /**
     * Insert a new post. Returns the generated post_id.
     * Schema: freelancer_forum_post (post_id, freelancer_id, title, content, created_at, updated_at)
     */
    public int addPost(Post post) throws SQLException {
        String sql = "INSERT INTO freelancer_forum_post (title, content, updated_at) VALUES (?, ?, NOW())";
        PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, post.getTitle());
        ps.setString(2, post.getContent());
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
        String sql = "SELECT p.post_id, p.title, p.content, p.created_at, p.updated_at FROM freelancer_forum_post p ORDER BY p.post_id DESC";
        try {
            Statement st = cn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Post post = new Post();
                post.setId(rs.getInt("post_id"));
                post.setAuthorId(0);
                post.setTitle(rs.getString("title"));
                post.setContent(rs.getString("content"));
                post.setAuthorName("Forum User");
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
     * Delete a post by post_id. Comments and reactions are cascade-deleted by the DB.
     */
    public void deletePost(int postId) {
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
