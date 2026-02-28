package uniearn.model.entities;

/**
 * Classe représentant un paiement dans le système UniEarn
 */
public class Payment {
    private int idPayment;
    private double amount;
    private int paymentStatus; // 0: En attente, 1: Complété, 2: Échoué, etc.
    private int taskID;

    /**
     * Constructeur par défaut
     */
    public Payment() {
    }

    /**
     * Constructeur complet
     */
    public Payment(int idPayment, double amount, int paymentStatus, int taskID) {
        this.idPayment = idPayment;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.taskID = taskID;
    }

    // Getters et Setters
    public int getIdPayment() {
        return idPayment;
    }

    public void setIdPayment(int idPayment) {
        this.idPayment = idPayment;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(int paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public int getTaskID() {
        return taskID;
    }

    public void setTaskID(int taskID) {
        this.taskID = taskID;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "idPayment=" + idPayment +
                ", amount=" + amount +
                ", paymentStatus=" + paymentStatus +
                ", taskID=" + taskID +
                '}';
    }
}

