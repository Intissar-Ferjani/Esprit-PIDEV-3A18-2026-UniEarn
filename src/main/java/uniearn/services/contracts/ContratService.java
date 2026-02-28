package uniearn.services.contracts;

import uniearn.database.MyConnection;
import uniearn.interfaces.contracts.IContrat;
import uniearn.model.entities.contracts.Contrat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContratService implements IContrat {
    private Connection connection;

    public ContratService() {
        try {
            this.connection = MyConnection.getInstance().getCnx();
        } catch (Exception e) {
            System.err.println("Erreur de connexion à la base de données: " + e.getMessage());
        }
    }

    /**
     * Créer un nouveau contrat
     */
    public boolean createContrat(Contrat contrat) {
        String sql = "INSERT INTO contract (Type, templateID, startDate, endDate, status, amount, " +
                     "projectID, clientID, freelancerID, paymentID) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, contrat.getType() != null ? contrat.getType() : "Standard");
            stmt.setInt(2, contrat.getTemplateID() > 0 ? contrat.getTemplateID() : 1);
            stmt.setTimestamp(3, contrat.getStartDate());
            stmt.setTimestamp(4, contrat.getEndDate());
            stmt.setInt(5, contrat.getStatus());
            stmt.setDouble(6, contrat.getAmount());
            stmt.setInt(7, contrat.getProjectID());
            stmt.setInt(8, contrat.getClientID());
            // Mettre le freelancerID à NULL car la table freelancer a une structure différente
            if (contrat.getFreelancerID() > 0) {
                stmt.setInt(9, contrat.getFreelancerID());
            } else {
                stmt.setNull(9, java.sql.Types.INTEGER);
            }
            if (contrat.getPaymentID() > 0) {
                stmt.setInt(10, contrat.getPaymentID());
            } else {
                stmt.setNull(10, java.sql.Types.INTEGER);
            }

            System.out.println("DEBUG createContrat:");
            System.out.println("  Type: " + contrat.getType());
            System.out.println("  templateID: " + contrat.getTemplateID());
            System.out.println("  startDate: " + contrat.getStartDate());
            System.out.println("  endDate: " + contrat.getEndDate());
            System.out.println("  status: " + contrat.getStatus());
            System.out.println("  amount: " + contrat.getAmount());
            System.out.println("  projectID: " + contrat.getProjectID());
            System.out.println("  clientID: " + contrat.getClientID());
            System.out.println("  freelancerID: " + (contrat.getFreelancerID() > 0 ? contrat.getFreelancerID() : "NULL"));
            System.out.println("  paymentID: " + (contrat.getPaymentID() > 0 ? contrat.getPaymentID() : "NULL"));

            boolean result = stmt.executeUpdate() > 0;
            System.out.println("DEBUG: Contrat créé avec succès: " + result);
            return result;
        } catch (SQLException e) {
            System.err.println("ERREUR SQL lors de la création du contrat: " + e.getMessage());
            System.err.println("Code d'erreur: " + e.getErrorCode());
            System.err.println("État SQL: " + e.getSQLState());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupérer un contrat par ID
     */
    public Contrat getContratById(int id) {
        String sql = "SELECT * FROM contract WHERE idContract = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToContrat(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupérer tous les contrats
     */
    public List<Contrat> getAllContrats() {
        List<Contrat> contrats = new ArrayList<>();
        String sql = "SELECT * FROM contract";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                contrats.add(mapResultSetToContrat(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contrats;
    }

    /**
     * Récupérer les contrats par client
     */
    public List<Contrat> getContratsByClient(int clientID) {
        List<Contrat> contrats = new ArrayList<>();
        String sql = "SELECT * FROM contract WHERE clientID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clientID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    contrats.add(mapResultSetToContrat(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contrats;
    }

    /**
     * Récupérer les contrats par projet
     */
    public List<Contrat> getContratsByProject(int projectID) {
        List<Contrat> contrats = new ArrayList<>();
        String sql = "SELECT * FROM contract WHERE projectID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, projectID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    contrats.add(mapResultSetToContrat(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contrats;
    }

    /**
     * Récupérer les contrats par statut
     */
    public List<Contrat> getContratsByStatus(int status) {
        List<Contrat> contrats = new ArrayList<>();
        String sql = "SELECT * FROM contract WHERE status = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    contrats.add(mapResultSetToContrat(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contrats;
    }

    /**
     * Mettre à jour un contrat
     */
    public boolean updateContrat(Contrat contrat) {
        String sql = "UPDATE contract SET Type = ?, templateID = ?, startDate = ?, endDate = ?, " +
                     "status = ?, amount = ?, projectID = ?, clientID = ?, freelancerID = ?, paymentID = ? " +
                     "WHERE idContract = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, contrat.getType());
            stmt.setInt(2, contrat.getTemplateID());
            stmt.setTimestamp(3, contrat.getStartDate());
            stmt.setTimestamp(4, contrat.getEndDate());
            stmt.setInt(5, contrat.getStatus());
            stmt.setDouble(6, contrat.getAmount());
            stmt.setInt(7, contrat.getProjectID());
            stmt.setInt(8, contrat.getClientID());
            stmt.setInt(9, contrat.getFreelancerID());
            stmt.setInt(10, contrat.getPaymentID());
            stmt.setInt(11, contrat.getIdContract());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Signer le contrat par le client (avec image de signature)
     */
    public boolean signByClient(int contractID) {
        String sql = "UPDATE contract SET status = 1, clientSignatureDate = CURRENT_TIMESTAMP WHERE idContract = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, contractID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la signature du client: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Signer le contrat par le client avec stockage de l'image de signature
     * @param contractID ID du contrat
     * @param clientSignatureImage Image PNG de la signature du client (bytes)
     * @return true si succès, false sinon
     */
    public boolean signByClientWithImage(int contractID, byte[] clientSignatureImage) {
        String sql = "UPDATE contract SET status = 1, clientSignatureDate = CURRENT_TIMESTAMP, " +
                     "clientSignatureImage = ? WHERE idContract = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBytes(1, clientSignatureImage);
            stmt.setInt(2, contractID);
            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                System.out.println("DEBUG: Contrat signé par le client avec image - Taille: " +
                    (clientSignatureImage != null ? clientSignatureImage.length : 0) + " bytes");
            }
            return result;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la signature du client avec image: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Signer le contrat par le freelancer (avec image de signature)
     */
    public boolean signByFreelancer(int contractID) {
        String sql = "UPDATE contract SET status = CASE WHEN status = 1 THEN 3 ELSE 2 END, " +
                     "freelancerSignatureDate = CURRENT_TIMESTAMP WHERE idContract = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, contractID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la signature du freelancer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Signer le contrat par le freelancer avec stockage de l'image de signature
     * @param contractID ID du contrat
     * @param freelancerSignatureImage Image PNG de la signature du freelancer (bytes)
     * @return true si succès, false sinon
     */
    public boolean signByFreelancerWithImage(int contractID, byte[] freelancerSignatureImage) {
        String sql = "UPDATE contract SET status = CASE WHEN status = 1 THEN 3 ELSE 2 END, " +
                     "freelancerSignatureDate = CURRENT_TIMESTAMP, freelancerSignatureImage = ? WHERE idContract = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBytes(1, freelancerSignatureImage);
            stmt.setInt(2, contractID);
            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                System.out.println("DEBUG: Contrat signé par le freelancer avec image - Taille: " +
                    (freelancerSignatureImage != null ? freelancerSignatureImage.length : 0) + " bytes");
            }
            return result;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la signature du freelancer avec image: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Supprimer un contrat
     */
    public boolean deleteContrat(int id) {
        String sql = "DELETE FROM contract WHERE idContract = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mapper un ResultSet à un objet Contrat
     */
    private Contrat mapResultSetToContrat(ResultSet rs) throws SQLException {
        Contrat contrat = new Contrat();
        contrat.setIdContract(rs.getInt("idContract"));
        contrat.setType(rs.getString("Type"));
        contrat.setTemplateID(rs.getInt("templateID"));
        contrat.setStartDate(rs.getTimestamp("startDate"));
        contrat.setEndDate(rs.getTimestamp("endDate"));
        contrat.setStatus(rs.getInt("status"));
        contrat.setAmount(rs.getDouble("amount"));
        contrat.setProjectID(rs.getInt("projectID"));
        contrat.setClientID(rs.getInt("clientID"));
        contrat.setFreelancerID(rs.getInt("freelancerID"));
        contrat.setPaymentID(rs.getInt("paymentID"));
        contrat.setClientSignatureDate(rs.getTimestamp("clientSignatureDate"));
        contrat.setFreelancerSignatureDate(rs.getTimestamp("freelancerSignatureDate"));

        // ✅ IMPORTANT: Récupérer les images de signature
        try {
            byte[] clientSignatureImage = rs.getBytes("clientSignatureImage");
            if (clientSignatureImage != null) {
                contrat.setClientSignatureImage(clientSignatureImage);
                System.out.println("DEBUG: Image signature client récupérée - Taille: " + clientSignatureImage.length + " bytes");
            }
        } catch (SQLException e) {
            // Colonne peut ne pas exister si migration non appliquée
            System.out.println("AVERTISSEMENT: Colonne clientSignatureImage non trouvée (migration non appliquée?)");
        }

        try {
            byte[] freelancerSignatureImage = rs.getBytes("freelancerSignatureImage");
            if (freelancerSignatureImage != null) {
                contrat.setFreelancerSignatureImage(freelancerSignatureImage);
                System.out.println("DEBUG: Image signature freelancer récupérée - Taille: " + freelancerSignatureImage.length + " bytes");
            }
        } catch (SQLException e) {
            // Colonne peut ne pas exister si migration non appliquée
            System.out.println("AVERTISSEMENT: Colonne freelancerSignatureImage non trouvée (migration non appliquée?)");
        }

        return contrat;
    }

    /**
     * Obtenir les statistiques des contrats
     */
    public int[] getStats() {
        int[] stats = new int[4];
        String sql = "SELECT status, COUNT(*) as count FROM contract GROUP BY status";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int status = rs.getInt("status");
                stats[status] = rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    /**
     * Vérifier si un contrat existe
     */
    public boolean exists(int id) {
        return getContratById(id) != null;
    }

    /**
     * Compter le nombre total de contrats
     */
    public int countAll() {
        String sql = "SELECT COUNT(*) as count FROM contract";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Compter les contrats par client
     */
    public int countByClient(int clientID) {
        String sql = "SELECT COUNT(*) as count FROM contract WHERE clientID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clientID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Récupérer les contrats par freelancer
     */
    public List<Contrat> getContratsByFreelancer(int freelancerID) {
        List<Contrat> contrats = new ArrayList<>();
        String sql = "SELECT * FROM contract WHERE freelancerID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, freelancerID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    contrats.add(mapResultSetToContrat(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contrats;
    }

    /**
     * Récupérer les contrats par type
     */
    public List<Contrat> getContratsByType(String type) {
        List<Contrat> contrats = new ArrayList<>();
        String sql = "SELECT * FROM contract WHERE Type = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, type);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    contrats.add(mapResultSetToContrat(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contrats;
    }

    /**
     * Compter les contrats par freelancer
     */
    public int countByFreelancer(int freelancerID) {
        String sql = "SELECT COUNT(*) as count FROM contract WHERE freelancerID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, freelancerID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Vérifier si un contrat est signé par le client
     */
    public boolean isSignedByClient(int contractID) {
        String sql = "SELECT status FROM contract WHERE idContract = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, contractID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("status") >= 1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Vérifier si un contrat est complètement signé
     */
    public boolean isFullySigned(int contractID) {
        String sql = "SELECT status FROM contract WHERE idContract = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, contractID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("status") == 3;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
