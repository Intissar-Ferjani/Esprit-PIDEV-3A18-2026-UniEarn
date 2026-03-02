package uniearn.services;

import uniearn.database.MyConnection;
import uniearn.model.entities.projet.Project;
import uniearn.model.entities.users.freelancer.Freelancer;
import uniearn.model.enums.UserRole;
import uniearn.model.entities.Payment;
import uniearn.model.entities.contracts.ContractType;
import uniearn.services.projet.ProjectService;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public List<Freelancer> getAllFreelancers() {
        List<Freelancer> freelancers = new ArrayList<>();
        String sql = "SELECT u.idUser, u.name, u.email, u.profilePicturePath, u.role, u.activated, " +
                "f.idFreelancer FROM freelancer f " +
                "JOIN user u ON f.idUser = u.idUser " +
                "ORDER BY u.name";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Freelancer freelancer = new Freelancer();
                freelancer.setIdUser(rs.getInt("idUser"));
                freelancer.setName(rs.getString("name"));
                freelancer.setEmail(rs.getString("email"));
                freelancer.setProfilePicturePath(rs.getString("profilePicturePath"));
                freelancer.setRole(UserRole.valueOf(rs.getString("role")));
                freelancer.setActivated(rs.getBoolean("activated"));
                freelancer.setIdFreelancer(rs.getInt("idFreelancer"));
                freelancers.add(freelancer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return freelancers;
    }

    /**
     * Charger les projets d'un client
     */
    public List<Project> getProjectsByClient(int clientID) {
        try {
            System.out.println("DEBUG: getProjectsByClient() - clientID = " + clientID);
            ProjectService projectService = new ProjectService();
            List<Project> projects = projectService.getProjectsByClientId(clientID);
            System.out.println("DEBUG: Récupéré " + projects.size() + " projets pour clientID=" + clientID);
            for (Project p : projects) {
                System.out.println("  - ID:" + p.getIdproject() + ", Titre:" + p.getTitle());
            }
            return projects;
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des projets pour clientID=" + clientID + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Charger les paiements disponibles (non utilisés)
     */
    public List<Payment> getAvailablePayments() {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT idPayment, amount FROM payment WHERE taskID IS NULL LIMIT 10";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Payment payment = new Payment();
                payment.setIdPayment(rs.getInt("idPayment"));
                payment.setAmount(rs.getDouble("amount"));
                payment.setPaymentStatus(0); // Défaut : En attente
                payments.add(payment);
            }
            System.out.println("DEBUG: Chargé " + payments.size() + " paiements disponibles");
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des paiements: " + e.getMessage());
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

    /**
     * Charger les types de contrats disponibles
     */
    public List<ContractType> getAllContractTypes() {
        List<ContractType> contractTypes = new ArrayList<>();
        String sql = "SELECT * FROM contract_type ORDER BY metier, typeName";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ContractType contractType = new ContractType();
                contractType.setIdContractType(rs.getInt("idContractType"));
                contractType.setTypeName(rs.getString("typeName"));
                contractType.setDescription(rs.getString("description"));
                contractType.setMetier(rs.getString("metier"));
                contractTypes.add(contractType);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contractTypes;
    }

    /**
     * Obtenir les métiers disponibles
     */
    public Set<String> getAllMetiers() {
        Set<String> metiers = new HashSet<>();
        String sql = "SELECT DISTINCT metier FROM contract_type ORDER BY metier";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                metiers.add(rs.getString("metier"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return metiers;
    }

    /**
     * Charger les types de contrats par métier
     */
    public List<ContractType> getContractTypesByMetier(String metier) {
        List<ContractType> contractTypes = new ArrayList<>();
        String sql = "SELECT * FROM contract_type WHERE metier = ? ORDER BY typeName";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, metier);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ContractType contractType = new ContractType();
                    contractType.setIdContractType(rs.getInt("idContractType"));
                    contractType.setTypeName(rs.getString("typeName"));
                    contractType.setDescription(rs.getString("description"));
                    contractType.setMetier(rs.getString("metier"));
                    contractTypes.add(contractType);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contractTypes;
    }

    /**
     * Récupère l'idFreelancer à partir de l'idUser
     */
    public Integer getFreelancerIdByUserId(int idUser) {
        String sql = "SELECT idFreelancer FROM freelancer WHERE idUser = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idUser);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idFreelancer");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération du freelancer: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}