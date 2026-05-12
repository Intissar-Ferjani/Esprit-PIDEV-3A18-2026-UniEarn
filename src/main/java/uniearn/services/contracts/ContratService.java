package uniearn.services.contracts;

import uniearn.database.MyConnection;
import uniearn.interfaces.contracts.IContrat;
import uniearn.model.entities.contracts.Contrat;

import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class ContratService implements IContrat {
    private Connection connection;

    public ContratService() {
        try {
            this.connection = MyConnection.getInstance().getCnx();
            debugPrintColumns();
        } catch (Exception e) {
            System.err.println("ERREUR de connexion dans ContratService: " + e.getMessage());
        }
    }

    private void debugPrintColumns() {
        System.out.println("🔍 DATABASE INSPECTOR: Checking 'contract' table schema...");
        String sql = "SELECT * FROM contract WHERE 1=0"; // 1=0 ensures we only get metadata, NO DATA (safe from PacketTooBigException)
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            int count = meta.getColumnCount();
            System.out.print("📦 COLUMNS FOUND: ");
            for (int i = 1; i <= count; i++) {
                System.out.print(meta.getColumnName(i) + (i < count ? ", " : ""));
            }
            System.out.println("\n--------------------------------------------------");
        } catch (SQLException e) {
            System.err.println("❌ ERROR: Could not inspect 'contract' table: " + e.getMessage());
        }
    }

    /**
     * Status conversion helpers (Java int ↔ DB varchar)
    // DB uses: "pending", "signed", "completed", "funded", "cancelled"
    // Java uses: 0=Draft, 1=SignedClient, 2=SignedFreelancer, 3=Active, 4=Cancelled
    // ═══════════════════════════════════════════════════════

    /**
     * Convertit un ID de statut entier en chaîne pour la base de données (compatible Symfony)
     */
    private String statusIntToString(int status) {
        return switch (status) {
            case 0 -> "pending";
            case 1 -> "signed_client";
            case 2 -> "signed_freelancer";
            case 3 -> "signed";
            case 4 -> "funded";
            case 5 -> "released";
            case 6 -> "completed";
            case 7 -> "cancelled";
            default -> "pending";
        };
    }

    /**
     * Convertit une chaîne de statut de la base de données en ID entier
     */
    private int statusStringToInt(String status) {
        if (status == null) return 0;
        String s = status.toLowerCase().trim();
        int result = switch (s) {
            case "pending", "brouillon" -> 0;
            case "signed_client" -> 1;
            case "signed_freelancer" -> 2;
            case "signed", "active" -> 3;
            case "funded" -> 4;
            case "released" -> 5;
            case "completed" -> 6;
            case "cancelled" -> 7;
            default -> 0;
        };
        // System.out.println("🔍 DB READ: Status '" + status + "' mapped to " + result);
        return result;
    }

    // ═══════════════════════════════════════════════════════
    // Signature image conversion helpers (Java byte[] ↔ DB longtext/base64)
    // ═══════════════════════════════════════════════════════

    private String bytesToBase64(byte[] bytes) {
        if (bytes == null) return null;
        // Symfony et les navigateurs attendent le préfixe data:image/png;base64,
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
    }

    private byte[] base64ToBytes(String base64) {
        if (base64 == null || base64.isEmpty()) return null;
        
        // Nettoyer la chaîne (enlever le préfixe data:image/png;base64, et les espaces)
        String cleanBase64 = base64.trim();
        if (cleanBase64.contains(",")) {
            cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
        }
        
        try {
            return Base64.getDecoder().decode(cleanBase64);
        } catch (IllegalArgumentException e) {
            System.err.println("WARN: Invalid base64 signature image data: " + e.getMessage());
            // Log a snippet for debugging
            if (cleanBase64.length() > 20) {
                System.err.println("Snippet: " + cleanBase64.substring(0, 20) + "...");
            }
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════
    // CRUD Operations
    // ═══════════════════════════════════════════════════════

    /**
     * Créer un nouveau contrat
     * DB columns: idContractTemplate, idClient, idFreelancer, title, content,
     *             amount, startDate, endDate, status, createdAt, updatedAt
     */
    public boolean createContrat(Contrat contrat) {
        String sql = "INSERT INTO contract (idContractTemplate, idClient, idFreelancer, title, content, " +
                     "amount, startDate, endDate, status, createdAt, updatedAt) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, contrat.getTemplateID() > 0 ? contrat.getTemplateID() : 1);
            stmt.setInt(2, contrat.getClientID());

            // idFreelancer is already resolved to the correct FK value by the controller
            if (contrat.getFreelancerID() > 0) {
                stmt.setInt(3, contrat.getFreelancerID());
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }

            // Title and content
            String title = contrat.getTitle();
            if (title == null || title.isEmpty()) {
                title = contrat.getType() != null ? contrat.getType() : "Nouveau Contrat";
            }
            stmt.setString(4, title);

            String content = contrat.getContent();
            if (content == null || content.isEmpty()) {
                content = "Contrat créé via UniEarn Java";
            }
            stmt.setString(5, content);

            stmt.setDouble(6, contrat.getAmount());
            stmt.setTimestamp(7, contrat.getStartDate());
            stmt.setTimestamp(8, contrat.getEndDate());
            stmt.setString(9, statusIntToString(contrat.getStatus()));

            // Debug: verify FK values exist in DB before insert
            verifyFreelancerExists(contrat.getFreelancerID());
            verifyClientExists(contrat.getClientID());

            System.out.println("DEBUG createContrat:");
            System.out.println("  idContractTemplate: " + contrat.getTemplateID());
            System.out.println("  title: " + title);
            System.out.println("  startDate: " + contrat.getStartDate());
            System.out.println("  endDate: " + contrat.getEndDate());
            System.out.println("  status: " + statusIntToString(contrat.getStatus()));
            System.out.println("  amount: " + contrat.getAmount());
            System.out.println("  idClient: " + contrat.getClientID());
            System.out.println("  idFreelancer: " + contrat.getFreelancerID());

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
     * Colonnes à récupérer pour la liste (SANS les images lourdes pour éviter PacketTooBigException)
     */
    private static final String COLUMNS_LIST = "idContract, idContractTemplate, idClient, idFreelancer, title, content, amount, startDate, endDate, status, clientSignature, freelancerSignature";

    public List<Contrat> getAllContrats() {
        List<Contrat> contrats = new ArrayList<>();
        String sql = "SELECT " + COLUMNS_LIST + " FROM contract";
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
        String sql = "SELECT " + COLUMNS_LIST + " FROM contract WHERE idClient = ?";
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
     * Récupérer les contrats par projet (projectID n'existe plus en DB, retourne liste vide)
     */
    public List<Contrat> getContratsByProject(int projectID) {
        // projectID column does not exist in the shared Symfony DB
        System.out.println("WARN: getContratsByProject() - projectID column not in DB, returning empty list");
        return new ArrayList<>();
    }

    /**
     * Récupérer les contrats par statut
     */
    public List<Contrat> getContratsByStatus(int status) {
        List<Contrat> contrats = new ArrayList<>();
        int statusInt = status;
        String statusStr = statusIntToString(status);
        String sql = "SELECT " + COLUMNS_LIST + " FROM contract WHERE status = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, statusStr);
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
        String sql = "UPDATE contract SET idContractTemplate = ?, startDate = ?, endDate = ?, " +
                     "status = ?, amount = ?, idClient = ?, idFreelancer = ?, title = ?, content = ?, " +
                     "updatedAt = NOW() WHERE idContract = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, contrat.getTemplateID());
            stmt.setTimestamp(2, contrat.getStartDate());
            stmt.setTimestamp(3, contrat.getEndDate());
            stmt.setString(4, statusIntToString(contrat.getStatus()));
            stmt.setDouble(5, contrat.getAmount());
            stmt.setInt(6, contrat.getClientID());
            stmt.setInt(7, contrat.getFreelancerID());
            String title = contrat.getTitle() != null ? contrat.getTitle() : (contrat.getType() != null ? contrat.getType() : "Contrat");
            stmt.setString(8, title);
            String content = contrat.getContent() != null ? contrat.getContent() : "";
            stmt.setString(9, content);
            stmt.setInt(10, contrat.getIdContract());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ═══════════════════════════════════════════════════════
    // Signature Operations
    // ═══════════════════════════════════════════════════════

    /**
     * Signer le contrat par le client
     */
    public boolean signByClient(int contractID) {
        String sql = "UPDATE contract SET clientSignature = CURRENT_TIMESTAMP, updatedAt = NOW() WHERE idContract = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, contractID);
            boolean updated = stmt.executeUpdate() > 0;
            if (updated) {
                refreshSignatureStatus(contractID);
            }
            return updated;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la signature du client: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Signer le contrat par le client avec stockage de l'image de signature (base64 en DB)
     */
    public boolean signByClientWithImage(int contractID, byte[] clientSignatureImage) {
        String sql = "UPDATE contract SET clientSignature = CURRENT_TIMESTAMP, clientSignatureImage = ?, updatedAt = NOW() WHERE idContract = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, bytesToBase64(clientSignatureImage));
            stmt.setInt(2, contractID);
            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                System.out.println("DEBUG: Contrat signé par le client avec image - Taille: " +
                    (clientSignatureImage != null ? clientSignatureImage.length : 0) + " bytes");
                refreshSignatureStatus(contractID);
            }
            return result;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la signature du client avec image: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Signer le contrat par le freelancer
     */
    public boolean signByFreelancer(int contractID) {
        String sql = "UPDATE contract SET freelancerSignature = CURRENT_TIMESTAMP, updatedAt = NOW() WHERE idContract = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, contractID);
            boolean updated = stmt.executeUpdate() > 0;
            if (updated) {
                refreshSignatureStatus(contractID);
            }
            return updated;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la signature du freelancer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Signer le contrat par le freelancer avec stockage de l'image de signature (base64 en DB)
     */
    public boolean signByFreelancerWithImage(int contractID, byte[] freelancerSignatureImage) {
        String sql = "UPDATE contract SET freelancerSignature = CURRENT_TIMESTAMP, freelancerSignatureImage = ?, updatedAt = NOW() WHERE idContract = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, bytesToBase64(freelancerSignatureImage));
            stmt.setInt(2, contractID);
            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                System.out.println("DEBUG: Contrat signé par le freelancer avec image - Taille: " +
                    (freelancerSignatureImage != null ? freelancerSignatureImage.length : 0) + " bytes");
                refreshSignatureStatus(contractID);
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

    // ═══════════════════════════════════════════════════════
    // ResultSet Mapper — uses actual Symfony DB column names
    // ═══════════════════════════════════════════════════════

    /**
     * Mapper un ResultSet à un objet Contrat
     * DB columns: idContract, idContractTemplate, idClient, idFreelancer,
     *             title, content, amount, startDate, endDate, status (varchar),
     *             clientSignature, clientSignatureImage, freelancerSignature, freelancerSignatureImage,
     *             createdAt, updatedAt
     */
    private Contrat mapResultSetToContrat(ResultSet rs) throws SQLException {
        Contrat contrat = new Contrat();
        contrat.setIdContract(rs.getInt("idContract"));
        contrat.setTemplateID(rs.getInt("idContractTemplate"));
        contrat.setClientID(rs.getInt("idClient"));
        contrat.setFreelancerID(rs.getInt("idFreelancer"));

        // title and content from DB
        contrat.setTitle(rs.getString("title"));
        contrat.setContent(rs.getString("content"));

        // Use title as type for backward compatibility
        contrat.setType(rs.getString("title"));

        contrat.setAmount(rs.getDouble("amount"));

        // startDate / endDate — DB stores as date, Java model uses Timestamp
        try {
            java.sql.Date startDate = rs.getDate("startDate");
            if (startDate != null) {
                contrat.setStartDate(new Timestamp(startDate.getTime()));
            }
        } catch (SQLException e) {
            contrat.setStartDate(rs.getTimestamp("startDate"));
        }

        try {
            java.sql.Date endDate = rs.getDate("endDate");
            if (endDate != null) {
                contrat.setEndDate(new Timestamp(endDate.getTime()));
            }
        } catch (SQLException e) {
            contrat.setEndDate(rs.getTimestamp("endDate"));
        }

        // Status: DB varchar → Java int
        String statusStr = rs.getString("status");
        contrat.setStatus(statusStringToInt(statusStr));

        // Signature dates: DB uses clientSignature / freelancerSignature
        try {
            contrat.setClientSignatureDate(rs.getTimestamp("clientSignature"));
        } catch (SQLException e) {
            // Column may not exist
        }
        try {
            contrat.setFreelancerSignatureDate(rs.getTimestamp("freelancerSignature"));
        } catch (SQLException e) {
            // Column may not exist
        }

        // Signature images: DB stores as longtext (base64), Java uses byte[]
        try {
            String clientSigBase64 = rs.getString("clientSignatureImage");
            if (clientSigBase64 != null && !clientSigBase64.isEmpty()) {
                contrat.setClientSignatureImage(base64ToBytes(clientSigBase64));
            }
        } catch (SQLException e) {
            // Column may not exist
        }

        try {
            String freelancerSigBase64 = rs.getString("freelancerSignatureImage");
            if (freelancerSigBase64 != null && !freelancerSigBase64.isEmpty()) {
                contrat.setFreelancerSignatureImage(base64ToBytes(freelancerSigBase64));
            }
        } catch (SQLException e) {
            // Column may not exist
        }

        // projectID and paymentID don't exist in DB — leave as 0
        contrat.setProjectID(0);
        contrat.setPaymentID(0);

        return contrat;
    }

    /**
     * Recalcule le statut global du contrat à partir des signatures déjà enregistrées.
     * Harmonise la logique Java avec Symfony:
     * - 1/2 = signature partielle
     * - 3 = contrat entièrement signé
     */
    private void refreshSignatureStatus(int contractID) {
        Contrat current = getContratById(contractID);
        if (current == null) {
            return;
        }

        int newStatus;
        if (current.getClientSignatureDate() != null && current.getFreelancerSignatureDate() != null) {
            newStatus = 3;
        } else if (current.getClientSignatureDate() != null) {
            newStatus = 1;
        } else if (current.getFreelancerSignatureDate() != null) {
            newStatus = 2;
        } else {
            newStatus = 0;
        }

        updateContractStatus(contractID, newStatus);
    }

    // ═══════════════════════════════════════════════════════
    // Statistics & Utility Methods
    // ═══════════════════════════════════════════════════════

    /**
     * Obtenir les statistiques des contrats
     */
    public int[] getStats() {
        int[] stats = new int[5]; // pending, signed, -, completed/funded, cancelled
        String sql = "SELECT status, COUNT(*) as count FROM contract GROUP BY status";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String statusStr = rs.getString("status");
                int idx = statusStringToInt(statusStr);
                if (idx >= 0 && idx < stats.length) {
                    stats[idx] += rs.getInt("count");
                }
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
        String sql = "SELECT COUNT(*) as count FROM contract WHERE idClient = ?";
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
        String sql = "SELECT " + COLUMNS_LIST + " FROM contract WHERE idFreelancer = ?";
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
     * Récupérer les contrats par type (uses title column since type doesn't exist)
     */
    public List<Contrat> getContratsByType(String type) {
        List<Contrat> contrats = new ArrayList<>();
        String sql = "SELECT " + COLUMNS_LIST + " FROM contract WHERE title = ?";
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
        String sql = "SELECT COUNT(*) as count FROM contract WHERE idFreelancer = ?";
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
                    String status = rs.getString("status");
                    return "signed".equals(status) || "completed".equals(status) || "funded".equals(status);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mettre à jour le statut d'un contrat
     */
    public boolean updateContractStatus(int contractID, int status) {
        String statusStr = statusIntToString(status);
        System.out.println("🚀 DB UPDATE: Changing Contract #" + contractID + " status to " + status + " ('" + statusStr + "')");
        
        String sql = "UPDATE contract SET status = ?, updatedAt = NOW() WHERE idContract = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, statusStr);
            stmt.setInt(2, contractID);
            boolean success = stmt.executeUpdate() > 0;
            System.out.println("   -> Result: " + (success ? "SUCCESS" : "FAILED"));
            return success;
        } catch (SQLException e) {
            System.err.println("   -> ERROR: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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
                    String status = rs.getString("status");
                    return "completed".equals(status) || "funded".equals(status);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════
    // Helper methods for resolving user/freelancer IDs
    // ═══════════════════════════════════════════════════════

    private Integer getUserIdByClientId(int clientId) {
        String sql = "SELECT userID FROM client WHERE idClient = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("userID");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du userID pour client " + clientId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void verifyFreelancerExists(int freelancerID) {
        String sql = "SELECT idFreelancer, idUser FROM freelancer WHERE idFreelancer = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, freelancerID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("DEBUG FK CHECK: freelancer idFreelancer=" + freelancerID + " EXISTS (idUser=" + rs.getInt("idUser") + ")");
                } else {
                    System.err.println("DEBUG FK CHECK: freelancer idFreelancer=" + freelancerID + " DOES NOT EXIST in DB!");
                    // Print all existing freelancer IDs for diagnosis
                    try (Statement s = connection.createStatement();
                         ResultSet all = s.executeQuery("SELECT idFreelancer, idUser FROM freelancer LIMIT 20")) {
                        System.err.println("  Available freelancers:");
                        while (all.next()) {
                            System.err.println("    idFreelancer=" + all.getInt("idFreelancer") + ", idUser=" + all.getInt("idUser"));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("DEBUG FK CHECK error: " + e.getMessage());
        }
    }

    private void verifyClientExists(int clientID) {
        String sql = "SELECT idClient FROM client WHERE idClient = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clientID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("DEBUG FK CHECK: client idClient=" + clientID + " EXISTS");
                } else {
                    System.err.println("DEBUG FK CHECK: client idClient=" + clientID + " DOES NOT EXIST!");
                }
            }
        } catch (SQLException e) {
            System.err.println("DEBUG FK CHECK error: " + e.getMessage());
        }
    }
}
