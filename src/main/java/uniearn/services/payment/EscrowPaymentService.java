package uniearn.services.payment;

import uniearn.database.MyConnection;
import uniearn.model.entities.PaymentEscrow;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion des paiements en escrow
 * Gère le blocage et la libération des montants
 *
 * @deprecated Ce service est remplacé par {@link StripePaymentService}
 * Conservé uniquement pour compatibilité avec les anciennes données
 */
@Deprecated
public class EscrowPaymentService {
    private Connection connection;

    public EscrowPaymentService() {
        try {
            this.connection = MyConnection.getInstance().getCnx();
            createTableIfNotExists();
        } catch (Exception e) {
            System.err.println("❌ Erreur de connexion à la base de données: " + e.getMessage());
        }
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS payment_escrow (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "contract_id INT NOT NULL, " +
                     "client_id INT NOT NULL, " +
                     "freelancer_id INT NOT NULL, " +
                     "amount DECIMAL(10, 2) NOT NULL, " +
                     "status VARCHAR(20) NOT NULL DEFAULT 'PENDING', " +
                     "date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                     "date_completion TIMESTAMP NULL, " +
                     "date_liberation TIMESTAMP NULL, " +
                     "notes TEXT NULL" +
                     ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Database: 'payment_escrow' table is ready.");
        } catch (Exception e) {
            System.err.println("⚠ Notice: Could not create payment_escrow table (it might already exist or permission denied): " + e.getMessage());
        }
    }

    /**
     * Crée un nouvel escrow pour un contrat
     */
    public PaymentEscrow createEscrow(int contractId, int clientId, int freelancerId, BigDecimal amount) {
        String sql = "INSERT INTO payment_escrow (contract_id, client_id, freelancer_id, amount, status, date_creation) " +
                "VALUES (?, ?, ?, ?, 'PENDING', NOW())";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, contractId);
            stmt.setInt(2, clientId);
            stmt.setInt(3, freelancerId);
            stmt.setBigDecimal(4, amount);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    PaymentEscrow escrow = new PaymentEscrow(contractId, clientId, freelancerId, amount);
                    escrow.setId(rs.getInt(1));
                    escrow.setDateCreation(LocalDateTime.now());
                    System.out.println("✅ Escrow créé avec succès: ID=" + escrow.getId());
                    return escrow;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la création de l'escrow: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Marque un escrow comme complété (projet livré)
     */
    public boolean markAsCompleted(int escrowId) {
        String sql = "UPDATE payment_escrow SET status = 'COMPLETED', date_completion = NOW() WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, escrowId);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                // Synchroniser avec le contrat
                PaymentEscrow escrow = getEscrowById(escrowId);
                if (escrow != null) {
                    new uniearn.services.contracts.ContratService().updateContractStatus(escrow.getContractId(), 6); // 6 = completed
                }
                System.out.println("✅ Escrow " + escrowId + " marqué comme complété");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour de l'escrow: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Libère le montant au freelancer (admin valide la livraison)
     */
    public boolean releasePayment(int escrowId, String adminNotes) {
        String sql = "UPDATE payment_escrow SET status = 'RELEASED', date_liberation = NOW(), notes = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, adminNotes);
            stmt.setInt(2, escrowId);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                // Synchroniser avec le contrat
                PaymentEscrow escrow = getEscrowById(escrowId);
                if (escrow != null) {
                    new uniearn.services.contracts.ContratService().updateContractStatus(escrow.getContractId(), 5); // 5 = released
                }
                System.out.println("✅ Montant escrow " + escrowId + " libéré au freelancer");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la libération du paiement: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Rembourse le client (paiement échoué ou annulation)
     */
    public boolean refundPayment(int escrowId, String adminNotes) {
        String sql = "UPDATE payment_escrow SET status = 'REFUNDED', date_liberation = NOW(), notes = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, adminNotes);
            stmt.setInt(2, escrowId);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                // Synchroniser avec le contrat
                PaymentEscrow escrow = getEscrowById(escrowId);
                if (escrow != null) {
                    new uniearn.services.contracts.ContratService().updateContractStatus(escrow.getContractId(), 7); // 7 = cancelled/refunded
                }
                System.out.println("✅ Paiement escrow " + escrowId + " remboursé au client");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du remboursement: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Récupère un escrow par ID
     */
    public PaymentEscrow getEscrowById(int escrowId) {
        String sql = "SELECT * FROM payment_escrow WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, escrowId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToPaymentEscrow(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de l'escrow: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupère tous les escrows d'un client
     */
    public List<PaymentEscrow> getEscrowsByClient(int clientId) {
        List<PaymentEscrow> escrows = new ArrayList<>();
        String sql = "SELECT * FROM payment_escrow WHERE client_id = ? ORDER BY date_creation DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                escrows.add(mapResultSetToPaymentEscrow(rs));
            }
            System.out.println("✅ Récupéré " + escrows.size() + " escrows pour le client " + clientId);
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des escrows client: " + e.getMessage());
            e.printStackTrace();
        }
        return escrows;
    }

    /**
     * Récupère tous les escrows d'un freelancer
     */
    public List<PaymentEscrow> getEscrowsByFreelancer(int freelancerId) {
        List<PaymentEscrow> escrows = new ArrayList<>();
        String sql = "SELECT * FROM payment_escrow WHERE freelancer_id = ? ORDER BY date_creation DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, freelancerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                escrows.add(mapResultSetToPaymentEscrow(rs));
            }
            System.out.println("✅ Récupéré " + escrows.size() + " escrows pour le freelancer " + freelancerId);
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des escrows freelancer: " + e.getMessage());
            e.printStackTrace();
        }
        return escrows;
    }

    /**
     * Récupère tous les escrows en attente de validation (pour admin)
     */
    public List<PaymentEscrow> getPendingEscrows() {
        List<PaymentEscrow> escrows = new ArrayList<>();
        String sql = "SELECT * FROM payment_escrow WHERE status IN ('PENDING', 'COMPLETED') ORDER BY date_creation ASC";

        try (Statement stmt = connection.createStatement()) {

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                escrows.add(mapResultSetToPaymentEscrow(rs));
            }
            System.out.println("✅ Récupéré " + escrows.size() + " escrows en attente");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des escrows en attente: " + e.getMessage());
            e.printStackTrace();
        }
        return escrows;
    }

    /**
     * Récupère TOUS les escrows (pour l'historique admin)
     */
    public List<PaymentEscrow> getAllEscrows() {
        List<PaymentEscrow> escrows = new ArrayList<>();
        String sql = "SELECT * FROM payment_escrow ORDER BY date_creation DESC";

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                escrows.add(mapResultSetToPaymentEscrow(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de tous les escrows: " + e.getMessage());
        }
        return escrows;
    }

    /**
     * Récupère l'escrow associé à un contrat
     */
    public PaymentEscrow getEscrowByContractId(int contractId) {
        String sql = "SELECT * FROM payment_escrow WHERE contract_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, contractId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToPaymentEscrow(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de l'escrow par contrat: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Calcule le montant total bloqué chez l'admin
     */
    public BigDecimal getTotalBlockedAmount() {
        String sql = "SELECT SUM(amount) as total FROM payment_escrow WHERE status IN ('PENDING', 'COMPLETED')";

        try (Statement stmt = connection.createStatement()) {

            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du calcul du montant bloqué: " + e.getMessage());
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    /**
     * Mappe un ResultSet à un objet PaymentEscrow
     */
    private PaymentEscrow mapResultSetToPaymentEscrow(ResultSet rs) throws SQLException {
        PaymentEscrow escrow = new PaymentEscrow();
        escrow.setId(rs.getInt("id"));
        escrow.setContractId(rs.getInt("contract_id"));
        escrow.setClientId(rs.getInt("client_id"));
        escrow.setFreelancerId(rs.getInt("freelancer_id"));
        escrow.setAmount(rs.getBigDecimal("amount"));
        escrow.setStatus(rs.getString("status"));

        Timestamp dateCreation = rs.getTimestamp("date_creation");
        if (dateCreation != null) {
            escrow.setDateCreation(dateCreation.toLocalDateTime());
        }

        Timestamp dateCompletion = rs.getTimestamp("date_completion");
        if (dateCompletion != null) {
            escrow.setDateCompletion(dateCompletion.toLocalDateTime());
        }

        Timestamp dateLiberation = rs.getTimestamp("date_liberation");
        if (dateLiberation != null) {
            escrow.setDateLiberation(dateLiberation.toLocalDateTime());
        }

        escrow.setNotes(rs.getString("notes"));

        return escrow;
    }

    /**
     * Supprime un paiement escrow
     */
    public boolean deleteEscrow(int escrowId) {
        String sql = "DELETE FROM payment_escrow WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, escrowId);

            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("✅ Escrow supprimé avec succès: ID=" + escrowId);
                return true;
            } else {
                System.err.println("❌ Aucun escrow trouvé avec l'ID: " + escrowId);
                return false;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression du paiement escrow: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Scanne tous les contrats et crée les entrées escrow manquantes pour les contrats payés
     * @return le nombre d'entrées créées
     */
    public int syncMissingEscrows() {
        int createdCount = 0;
        // On ne sélectionne que les contrats qui sont explicitement dans un état payé ou plus
        String sql = "SELECT * FROM contract WHERE status IN ('funded', 'released', 'completed') " +
                     "AND idContract NOT IN (SELECT contract_id FROM payment_escrow)";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int contractId = rs.getInt("idContract");
                int clientId = rs.getInt("idClient");
                int freelancerId = rs.getInt("idFreelancer");
                BigDecimal amount = rs.getBigDecimal("amount");
                int status = rs.getInt("status");

                // Créer l'entrée escrow
                String insertSql = "INSERT INTO payment_escrow (contract_id, client_id, freelancer_id, amount, status, date_creation) VALUES (?, ?, ?, ?, ?, NOW())";
                try (PreparedStatement pStmt = connection.prepareStatement(insertSql)) {
                    pStmt.setInt(1, contractId);
                    pStmt.setInt(2, clientId);
                    pStmt.setInt(3, freelancerId);
                    pStmt.setBigDecimal(4, amount);
                    
                    // Mapper le statut du contrat au statut escrow
                    String escrowStatus = (status == 4) ? "PENDING" : "COMPLETED";
                    pStmt.setString(5, escrowStatus);
                    
                    pStmt.executeUpdate();
                    createdCount++;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la synchronisation: " + e.getMessage());
        }
        return createdCount;
    }
}

