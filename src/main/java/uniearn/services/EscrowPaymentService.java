package uniearn.services;

import uniearn.database.MyConnection;
import uniearn.model.entities.EscrowPayment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour la gestion des paiements en escrow (table payment_escrow).
 */
public class EscrowPaymentService {

    private Connection connection;

    public EscrowPaymentService() {
        try {
            this.connection = MyConnection.getInstance().getCnx();
        } catch (Exception e) {
            System.err.println("Erreur de connexion EscrowPaymentService: " + e.getMessage());
        }
    }

    /**
     * Créer un nouveau paiement en escrow.
     */
    public boolean createEscrowPayment(EscrowPayment payment) {
        String sql = "INSERT INTO payment_escrow (contract_id, client_id, freelancer_id, amount, status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, payment.getContractId());
            stmt.setInt(2, payment.getClientId());
            stmt.setInt(3, payment.getFreelancerId());
            stmt.setDouble(4, payment.getAmount());
            stmt.setString(5, payment.getStatus() != null ? payment.getStatus() : "PENDING");
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création du paiement escrow: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupérer tous les paiements escrow d'un client.
     */
    public List<EscrowPayment> getPaymentsByClient(int clientId) {
        List<EscrowPayment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payment_escrow WHERE client_id = ? ORDER BY created_at DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des paiements client: " + e.getMessage());
            e.printStackTrace();
        }
        return payments;
    }

    /**
     * Récupérer tous les paiements escrow d'un freelancer.
     */
    public List<EscrowPayment> getPaymentsByFreelancer(int freelancerId) {
        List<EscrowPayment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payment_escrow WHERE freelancer_id = ? ORDER BY created_at DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, freelancerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des paiements freelancer: " + e.getMessage());
            e.printStackTrace();
        }
        return payments;
    }

    /**
     * Récupérer tous les paiements escrow (pour les administrateurs).
     */
    public List<EscrowPayment> getAllPayments() {
        List<EscrowPayment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payment_escrow ORDER BY created_at DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                payments.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de tous les paiements: " + e.getMessage());
            e.printStackTrace();
        }
        return payments;
    }

    /**
     * Récupérer un paiement escrow par son identifiant.
     */
    public EscrowPayment getPaymentById(int id) {
        String sql = "SELECT * FROM payment_escrow WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du paiement: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Mettre à jour le statut d'un paiement escrow.
     */
    public boolean updateStatus(int paymentId, String newStatus) {
        String sql = "UPDATE payment_escrow SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, paymentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du statut: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Supprimer un paiement escrow.
     */
    public boolean deletePayment(int id) {
        String sql = "DELETE FROM payment_escrow WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du paiement: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private EscrowPayment mapResultSet(ResultSet rs) throws SQLException {
        EscrowPayment payment = new EscrowPayment();
        payment.setId(rs.getInt("id"));
        payment.setContractId(rs.getInt("contract_id"));
        payment.setClientId(rs.getInt("client_id"));
        payment.setFreelancerId(rs.getInt("freelancer_id"));
        payment.setAmount(rs.getDouble("amount"));
        payment.setStatus(rs.getString("status"));
        payment.setCreatedAt(rs.getTimestamp("created_at"));
        payment.setUpdatedAt(rs.getTimestamp("updated_at"));
        return payment;
    }
}
