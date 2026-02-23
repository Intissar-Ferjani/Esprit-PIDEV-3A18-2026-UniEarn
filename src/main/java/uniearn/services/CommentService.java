package uniearn.services;

import uniearn.database.MyConnection;
import uniearn.model.entities.Comment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentService {

    private final Connection cn = MyConnection.getInstance().getCnx();

    /** Default freelancer used for forum comments/reactions until login is integrated. Run forum_default_freelancer.sql once. */
    private static final int DEFAULT_FREELANCER_ID = 1;

    /**
     * Add a comment. Schema: freelancer_forum_comment (comment_id, post_id, freelancer_id, comment_text, created_at).
     * Uses DEFAULT_FREELANCER_ID (run forum_default_freelancer.sql once to create it).
     */
    public int addComment(Comment comment) throws SQLException {
        if (cn == null) throw new SQLException("Database not connected.");
        String sql = "INSERT INTO freelancer_forum_comment (post_id, freelancer_id, comment_text) VALUES (?, ?, ?)";
        PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, comment.getPostId());
        ps.setInt(2, DEFAULT_FREELANCER_ID);
        ps.setString(3, comment.getContent());
        ps.executeUpdate();

        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) {
            return keys.getInt(1);
        }
        return -1;
    }

    /**
     * Get all comments for a post. Schema uses comment_id, comment_text, created_at.
     */
    public List<Comment> getCommentsByPostId(int postId) {
        List<Comment> comments = new ArrayList<>();
        if (cn == null) return comments;
        String sql = "SELECT c.comment_id, c.post_id, c.comment_text, c.created_at FROM freelancer_forum_comment c WHERE c.post_id = ? ORDER BY c.comment_id ASC";
        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, postId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getInt("comment_id"));
                comment.setAuthorId(0);
                comment.setPostId(rs.getInt("post_id"));
                comment.setContent(rs.getString("comment_text"));
                comment.setAuthorName("Forum User");
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) {
                    comment.setCreatedAt(ts.toLocalDateTime());
                }
                comments.add(comment);
            }
        } catch (SQLException e) {
            System.out.println("Error loading comments: " + e.getMessage());
        }
        return comments;
    }

    /**
     * Update a comment's text. Schema column is comment_text.
     */
    public void updateComment(int commentId, String newContent) {
        if (cn == null) return;
        String sql = "UPDATE freelancer_forum_comment SET comment_text = ? WHERE comment_id = ?";
        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, newContent);
            ps.setInt(2, commentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating comment: " + e.getMessage());
        }
    }

    /**
     * Delete a comment by comment_id.
     */
    public void deleteComment(int commentId) {
        if (cn == null) return;
        String sql = "DELETE FROM freelancer_forum_comment WHERE comment_id = ?";
        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, commentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting comment: " + e.getMessage());
        }
    }
}
