package uniearn.services;

import uniearn.database.MyConnection;
import uniearn.model.entities.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour charger les données dynamiques (Freelancers, Projects, Payments)
 */
public class DataLoaderService {
    private Connection connection;

    public DataLoaderService() {
        try {
            this.connection = MyConnection.getInstance().getCnx();
        } catch (Exception e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
        }
    }

    /**
     * Charger tous les freelancers
     */
    public List<User> getAllFreelancers() {
        List<User> freelancers = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE role = 'FREELANCER' ORDER BY name";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = new User();
                user.setIdUser(rs.getInt("idUser"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                freelancers.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return freelancers;
    }

    /**
     * Charger les projets d'un client
     */
    public List<Integer> getProjectsByClient(int clientID) {
        List<Integer> projects = new ArrayList<>();
        String sql = "SELECT idProject FROM project WHERE clientID = ? ORDER BY idProject";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clientID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    projects.add(rs.getInt("idProject"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projects;
    }

    /**
     * Charger les paiements disponibles (non utilisés)
     */
    public List<Integer> getAvailablePayments() {
        List<Integer> payments = new ArrayList<>();
        String sql = "SELECT idPayment FROM payment ORDER BY idPayment DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                payments.add(rs.getInt("idPayment"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    /**
     * Obtenir le nom d'un freelancer par ID
     */
    public String getFreelancerName(int freelancerID) {
        String sql = "SELECT name FROM user WHERE idUser = ? AND role = 'FREELANCER'";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, freelancerID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Inconnu";
    }

    /**
     * Obtenir le nom d'un projet par ID
     */
    public String getProjectName(int projectID) {
        String sql = "SELECT projectName FROM project WHERE idProject = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, projectID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("projectName");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Inconnu";
    }

    /**
     * Obtenir le nom d'un client par ID
     */
    public String getClientName(int clientID) {
        String sql = "SELECT u.name FROM user u INNER JOIN client c ON u.idUser = c.userID WHERE c.idClient = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clientID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Inconnu";
    }
}
