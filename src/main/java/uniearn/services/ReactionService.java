package uniearn.services;

import uniearn.database.MyConnection;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class ReactionService {

    private final Connection cn = MyConnection.getInstance().getCnx();

    /** Default freelancer used for forum reactions until login is integrated. Run forum_default_freelancer.sql once. */
    private static final int DEFAULT_FREELANCER_ID = 1;

    /**
     * Toggle a reaction: if the user already reacted, remove it; otherwise add it.
     * Uses DEFAULT_FREELANCER_ID when userId <= 0 (run forum_default_freelancer.sql once to create it).
     */
    public boolean toggleReaction(int userId, int postId) {
        if (cn == null) return false;
        int freelancerId = userId <= 0 ? DEFAULT_FREELANCER_ID : userId;
        try {
            String checkSql = "SELECT reaction_id FROM freelancer_forum_reaction WHERE freelancer_id = ? AND post_id = ?";
            PreparedStatement checkPs = cn.prepareStatement(checkSql);
            checkPs.setInt(1, freelancerId);
            checkPs.setInt(2, postId);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                int reactionId = rs.getInt("reaction_id");
                String deleteSql = "DELETE FROM freelancer_forum_reaction WHERE reaction_id = ?";
                PreparedStatement deletePs = cn.prepareStatement(deleteSql);
                deletePs.setInt(1, reactionId);
                deletePs.executeUpdate();
                return false;
            } else {
                String insertSql = "INSERT INTO freelancer_forum_reaction (post_id, freelancer_id, reaction_type) VALUES (?, ?, 'LIKE')";
                PreparedStatement insertPs = cn.prepareStatement(insertSql);
                insertPs.setInt(1, postId);
                insertPs.setInt(2, freelancerId);
                insertPs.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error toggling reaction: " + e.getMessage());
            return false;
        }
    }

    /**
     * Count total reactions for a post.
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
     * Check if the current user has reacted to a post. Uses DEFAULT_FREELANCER_ID when userId <= 0.
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
}
