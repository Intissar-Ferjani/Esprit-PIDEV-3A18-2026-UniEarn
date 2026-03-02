package uniearn.model.entities;

/**
 * Classe représentant un paiement dans le système UniEarn
 */
public class Payment {
    private int idPayment;
    private double amount;
    private String paymentStatus; // PENDING, COMPLETED, FAILED, etc.
    private Integer taskID;
    private Integer userID;

    /**
     * Constructeur par défaut
     */
    public Payment() {
    }

    /**
     * Constructeur complet
     */
    public Payment(int idPayment, double amount, String paymentStatus, Integer taskID) {
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

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Integer getTaskID() {
        return taskID;
    }

    public void setTaskID(Integer taskID) {
        this.taskID = taskID;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
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

