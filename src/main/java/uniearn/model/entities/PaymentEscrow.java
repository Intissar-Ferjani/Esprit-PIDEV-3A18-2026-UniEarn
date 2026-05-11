package uniearn.model.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Classe représentant un paiement en escrow
 * Le montant est bloqué chez l'admin jusqu'à la validation de la livraison du freelancer
 */
public class PaymentEscrow {
    private int id;
    private int contractId;
    private int clientId;
    private int freelancerId;
    private BigDecimal amount;
    private String status; // PENDING, COMPLETED, RELEASED, REFUNDED
    private LocalDateTime dateCreation;
    private LocalDateTime dateCompletion; // Quand le projet est livré
    private LocalDateTime dateLiberation; // Quand l'admin libère l'argent
    private String notes; // Notes admin sur la validation

    // Constructeurs
    public PaymentEscrow() {
    }

    public PaymentEscrow(int contractId, int clientId, int freelancerId, BigDecimal amount) {
        this.contractId = contractId;
        this.clientId = clientId;
        this.freelancerId = freelancerId;
        this.amount = amount;
        this.status = "PENDING";
        this.dateCreation = LocalDateTime.now();
    }

    // Getters et Setters
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateCompletion() {
        return dateCompletion;
    }

    public void setDateCompletion(LocalDateTime dateCompletion) {
        this.dateCompletion = dateCompletion;
    }

    public LocalDateTime getDateLiberation() {
        return dateLiberation;
    }

    public void setDateLiberation(LocalDateTime dateLiberation) {
        this.dateLiberation = dateLiberation;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "PaymentEscrow{" +
                "id=" + id +
                ", contractId=" + contractId +
                ", clientId=" + clientId +
                ", freelancerId=" + freelancerId +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                ", dateCreation=" + dateCreation +
                ", dateCompletion=" + dateCompletion +
                ", dateLiberation=" + dateLiberation +
                '}';
    }
}

