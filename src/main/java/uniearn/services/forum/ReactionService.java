package uniearn.services.forum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import uniearn.database.MyConnection;

public class ReactionService {

    private final Connection cn = MyConnection.getInstance().getCnx();

    /** Default freelancer used for forum reactions until login is integrated. Run forum_default_freelancer.sql once. */
    private static final int DEFAULT_FREELANCER_ID = 1;

    public ReactionService() {
        ensureDislikeSupported();
    }

    /**
     * Automatically alter the reaction_type column to support DISLIKE
     * if it doesn't already. Safe to call multiple times.
     */
    private void ensureDislikeSupported() {
        if (cn == null) return;
        try {
            cn.createStatement().executeUpdate(
                "ALTER TABLE freelancer_forum_reaction MODIFY reaction_type VARCHAR(20) NOT NULL DEFAULT 'LIKE'"
            );
            System.out.println("reaction_type column updated to support DISLIKE.");
        } catch (SQLException e) {
            // Column may already be VARCHAR or the alter is a no-op — that's fine
            System.out.println("reaction_type column check: " + e.getMessage());
        }
    }

    /**
     * Toggle a LIKE reaction (backward-compatible).
     */
    public boolean toggleReaction(int userId, int postId) {
        return toggleReaction(userId, postId, "LIKE");
    }

    /**
     * Toggle a reaction of a given type: if the user already has the same type, remove it;
     * if the user has a different type, switch it; otherwise add it.
     * Returns true if the reaction is now active, false if removed.
     */
    public boolean toggleReaction(int userId, int postId, String reactionType) {
        if (cn == null) return false;
        int freelancerId = userId <= 0 ? DEFAULT_FREELANCER_ID : userId;
        try {
            // Check if user has ANY reaction on this post
            String checkSql = "SELECT reaction_id, reaction_type FROM freelancer_forum_reaction WHERE freelancer_id = ? AND post_id = ?";
            PreparedStatement checkPs = cn.prepareStatement(checkSql);
            checkPs.setInt(1, freelancerId);
            checkPs.setInt(2, postId);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                int reactionId = rs.getInt("reaction_id");
                String existingType = rs.getString("reaction_type");

                if (existingType.equals(reactionType)) {
                    // Same type → remove (toggle off)
                    String deleteSql = "DELETE FROM freelancer_forum_reaction WHERE reaction_id = ?";
                    PreparedStatement deletePs = cn.prepareStatement(deleteSql);
                    deletePs.setInt(1, reactionId);
                    deletePs.executeUpdate();
                    return false;
                } else {
                    // Different type → switch (e.g. LIKE→DISLIKE)
                    String updateSql = "UPDATE freelancer_forum_reaction SET reaction_type = ? WHERE reaction_id = ?";
                    PreparedStatement updatePs = cn.prepareStatement(updateSql);
                    updatePs.setString(1, reactionType);
                    updatePs.setInt(2, reactionId);
                    updatePs.executeUpdate();
                    return true;
                }
            } else {
                // No reaction yet → insert
                String insertSql = "INSERT INTO freelancer_forum_reaction (post_id, freelancer_id, reaction_type) VALUES (?, ?, ?)";
                PreparedStatement insertPs = cn.prepareStatement(insertSql);
                insertPs.setInt(1, postId);
                insertPs.setInt(2, freelancerId);
                insertPs.setString(3, reactionType);
                insertPs.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error toggling reaction: " + e.getMessage());
            return false;
        }
    }

    /**
     * Count reactions of a specific type for a post.
     */
    public int getReactionCount(int postId, String reactionType) {
        if (cn == null) return 0;
        String sql = "SELECT COUNT(*) AS cnt FROM freelancer_forum_reaction WHERE post_id = ? AND reaction_type = ?";
        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, postId);
            ps.setString(2, reactionType);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("cnt");
            }
        } catch (SQLException e) {
            System.out.println("Error counting reactions: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Count total reactions for a post (all types).
     */
    public int getReactionCount(int postId) {
        if (cn == null) return 0;
        String sql = "SELECT COUNT(*) AS cnt FROM freelancer_forum_reaction WHERE post_id = ?";
        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, postId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("cnt");
            }
        } catch (SQLException e) {
            System.out.println("Error counting reactions: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Get the set of freelancer IDs who reacted to a post.
     */
    public Set<Integer> getReactedUserIds(int postId) {
        Set<Integer> ids = new HashSet<>();
        if (cn == null) return ids;
        String sql = "SELECT freelancer_id FROM freelancer_forum_reaction WHERE post_id = ?";
        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, postId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("freelancer_id"));
            }
        } catch (SQLException e) {
            System.out.println("Error loading reactions: " + e.getMessage());
        }
        return ids;
    }

    /**
     * Check if the current user has reacted to a post with any type.
     */
    public boolean hasUserReacted(int userId, int postId) {
        if (cn == null) return false;
        int freelancerId = userId <= 0 ? DEFAULT_FREELANCER_ID : userId;
        String sql = "SELECT reaction_id FROM freelancer_forum_reaction WHERE freelancer_id = ? AND post_id = ?";
        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, freelancerId);
            ps.setInt(2, postId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Error checking reaction: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if the current user has reacted to a post with a specific type.
     */
    public boolean hasUserReacted(int userId, int postId, String reactionType) {
        if (cn == null) return false;
        int freelancerId = userId <= 0 ? DEFAULT_FREELANCER_ID : userId;
        String sql = "SELECT reaction_id FROM freelancer_forum_reaction WHERE freelancer_id = ? AND post_id = ? AND reaction_type = ?";
        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, freelancerId);
            ps.setInt(2, postId);
            ps.setString(3, reactionType);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Error checking reaction: " + e.getMessage());
            return false;
        }
    }
}
