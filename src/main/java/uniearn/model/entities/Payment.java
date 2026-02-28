package uniearn.model.entities;

import java.io.Serializable;
import java.sql.Timestamp;

public class Payment implements Serializable {
    private int idPayment;
    private double amount;
    private Timestamp datePayment;
    private int paymentStatus;
    private int userID;
    private int taskID;

    // Constructors
    public Payment() {
    }

    public Payment(double amount, int userID) {
        this.amount = amount;
        this.userID = userID;
        this.paymentStatus = 0; // Par défaut: En Attente
    }

    public Payment(int idPayment, double amount, Timestamp datePayment,
                   int paymentStatus, int userID, int taskID) {
        this.idPayment = idPayment;
        this.amount = amount;
        this.datePayment = datePayment;
        this.paymentStatus = paymentStatus;
        this.userID = userID;
        this.taskID = taskID;
    }

    // Getters
    public int getIdPayment() {
        return idPayment;
    }

    public double getAmount() {
        return amount;
    }

    public Timestamp getDatePayment() {
        return datePayment;
    }

    public int getPaymentStatus() {
        return paymentStatus;
    }

    public int getUserID() {
        return userID;
    }

    public int getTaskID() {
        return taskID;
    }

    // Setters
    public void setIdPayment(int idPayment) {
        this.idPayment = idPayment;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDatePayment(Timestamp datePayment) {
        this.datePayment = datePayment;
    }

    public void setPaymentStatus(int paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setUserID(int userID) {
        this.userID = userID;
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
                ", userID=" + userID +
                ", datePayment=" + datePayment +
                '}';
    }
}
