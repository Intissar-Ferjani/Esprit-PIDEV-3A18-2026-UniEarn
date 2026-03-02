package uniearn.model.entities;

import java.sql.Timestamp;

/**
 * Entité représentant un paiement en escrow dans le système UniEarn.
 * Correspond à la table payment_escrow en base de données.
 *
 * Statuts possibles :
 *   PENDING   - Paiement bloqué (en attente de livraison)
 *   COMPLETED - Livré (travail livré, en attente de libération)
 *   RELEASED  - Libéré (fonds transférés au freelancer)
 *   REFUNDED  - Remboursé (fonds retournés au client)
 */
public class EscrowPayment {
    private int id;
    private int contractId;
    private int clientId;
    private int freelancerId;
    private double amount;
    private String status; // PENDING, COMPLETED, RELEASED, REFUNDED
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public EscrowPayment() {}

    public EscrowPayment(int contractId, int clientId, int freelancerId, double amount, String status) {
        this.contractId = contractId;
        this.clientId = clientId;
        this.freelancerId = freelancerId;
        this.amount = amount;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getContractId() {
        return contractId;
    }

    public void setContractId(int contractId) {
        this.contractId = contractId;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getFreelancerId() {
        return freelancerId;
    }

    public void setFreelancerId(int freelancerId) {
        this.freelancerId = freelancerId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Retourne le libellé d'affichage du statut.
     */
    public String getStatusLabel() {
        if (status == null) return "Inconnu";
        return switch (status) {
            case "PENDING"   -> "Bloqué";
            case "COMPLETED" -> "Livré";
            case "RELEASED"  -> "Libéré";
            case "REFUNDED"  -> "Remboursé";
            default          -> status;
        };
    }

    @Override
    public String toString() {
        return "EscrowPayment{" +
                "id=" + id +
                ", contractId=" + contractId +
                ", clientId=" + clientId +
                ", freelancerId=" + freelancerId +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                '}';
    }
}
