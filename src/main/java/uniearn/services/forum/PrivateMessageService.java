package uniearn.services.forum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import uniearn.database.MyConnection;
import uniearn.model.entities.forum.ChatMessage;

/**
 * Service for persisting and retrieving private messages between users.
 * Table: forum_private_message (message_id, sender_name, recipient_name, content, created_at, is_read)
 */
public class PrivateMessageService {

    private final Connection cn = MyConnection.getInstance().getCnx();

    public PrivateMessageService() {
        ensureTableExists();
    }

    private void ensureTableExists() {
        if (cn == null) return;
        try {
            cn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS forum_private_message (" +
                "  message_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  sender_name VARCHAR(255) NOT NULL," +
                "  recipient_name VARCHAR(255) NOT NULL," +
                "  content TEXT NOT NULL," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  is_read TINYINT(1) DEFAULT 0" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
        } catch (SQLException e) {
            System.out.println("Message table check: " + e.getMessage());
        }
    }

    /**
     * Save a private message to the database.
     */
    public void saveMessage(String sender, String recipient, String content) {
        if (cn == null) return;
        String sql = "INSERT INTO forum_private_message (sender_name, recipient_name, content) VALUES (?, ?, ?)";
        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, sender);
            ps.setString(2, recipient);
            ps.setString(3, content);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving message: " + e.getMessage());
        }
    }

    /**
     * Get all messages in a conversation between two users, ordered by time.
     */
    public List<ChatMessage> getConversation(String user1, String user2) {
        List<ChatMessage> messages = new ArrayList<>();
        if (cn == null) return messages;
        String sql = "SELECT * FROM forum_private_message " +
                     "WHERE (sender_name = ? AND recipient_name = ?) OR (sender_name = ? AND recipient_name = ?) " +
                     "ORDER BY created_at ASC";
        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, user1);
            ps.setString(2, user2);
            ps.setString(3, user2);
            ps.setString(4, user1);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ChatMessage msg = new ChatMessage();
                msg.setSender(rs.getString("sender_name"));
                msg.setRecipient(rs.getString("recipient_name"));
                msg.setContent(rs.getString("content"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) {
                    msg.setTimestamp(ts.toLocalDateTime()
                        .format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")));
                }
                msg.setType(ChatMessage.MessageType.CHAT);
                messages.add(msg);
            }
        } catch (SQLException e) {
            System.out.println("Error loading conversation: " + e.getMessage());
        }
        return messages;
    }

    /**
     * Get list of users this user has conversations with, with the last message preview.
     * Returns a map of username → last message content, ordered by most recent first.
     */
    public Map<String, String> getConversationList(String currentUser) {
        Map<String, String> conversations = new LinkedHashMap<>();
        if (cn == null) return conversations;
        String sql = "SELECT other_user, content FROM (" +
                     "  SELECT CASE WHEN sender_name = ? THEN recipient_name ELSE sender_name END AS other_user, " +
                     "         content, created_at " +
                     "  FROM forum_private_message " +
                     "  WHERE sender_name = ? OR recipient_name = ? " +
                     "  ORDER BY created_at DESC" +
                     ") sub GROUP BY other_user ORDER BY MAX(created_at) DESC";
        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, currentUser);
            ps.setString(2, currentUser);
            ps.setString(3, currentUser);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                conversations.put(rs.getString("other_user"), rs.getString("content"));
            }
        } catch (SQLException e) {
            System.out.println("Error loading conversation list: " + e.getMessage());
        }
        return conversations;
    }

    /**
     * Get contacts linked via shared projects (client ↔ freelancer).
     * For a freelancer: returns client names who have projects assigned to them.
     * For a client: returns freelancer names assigned to their projects.
     * Returns map of username → project title as preview text.
     */
    public Map<String, String> getProjectLinkedContacts(String currentUserName) {
        Map<String, String> contacts = new LinkedHashMap<>();
        if (cn == null) return contacts;

        // Check if current user is a client or freelancer and find linked contacts
        String sql = "SELECT DISTINCT u2.name AS contact_name, p.title AS project_title FROM project p " +
                     "JOIN client c ON p.ClientID = c.idClient " +
                     "JOIN user u1 ON c.userID = u1.idUser " +
                     "JOIN freelancer f ON p.freelancerID = f.idFreelancer " +
                     "JOIN user u2 ON f.idUser = u2.idUser " +
                     "WHERE u1.name = ? " +
                     "UNION " +
                     "SELECT DISTINCT u1.name AS contact_name, p.title AS project_title FROM project p " +
                     "JOIN client c ON p.ClientID = c.idClient " +
                     "JOIN user u1 ON c.userID = u1.idUser " +
                     "JOIN freelancer f ON p.freelancerID = f.idFreelancer " +
                     "JOIN user u2 ON f.idUser = u2.idUser " +
                     "WHERE u2.name = ?";
        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, currentUserName);
            ps.setString(2, currentUserName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String contactName = rs.getString("contact_name");
                String projectTitle = rs.getString("project_title");
                if (!contactName.equals(currentUserName)) {
                    contacts.put(contactName, "\uD83D\uDCC1 Project: " + projectTitle);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading project contacts: " + e.getMessage());
        }
        return contacts;
    }
}
