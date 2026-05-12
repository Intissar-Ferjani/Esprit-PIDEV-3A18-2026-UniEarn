package uniearn.model.entities.contracts;

import java.io.Serializable;
import java.sql.Timestamp;

public class Contrat implements Serializable {
    private int idContract;
    private String type;
    private int templateID;
    private String title;
    private String content;
    private Timestamp startDate;
    private Timestamp endDate;
    private int status; // 0: Draft/pending, 1: Client Signed/signed, 2: Freelancer Signed, 3: Active/completed/funded, 4: Cancelled
    private double amount;
    private int projectID;
    private int clientID;
    private int freelancerID;
    private int paymentID;
    private Timestamp clientSignatureDate;
    private Timestamp freelancerSignatureDate;
    private byte[] clientSignatureImage; // Image PNG de la signature du client
    private byte[] freelancerSignatureImage; // Image PNG de la signature du freelancer

    // Constructors
    public Contrat() {
    }

    public Contrat(int idContract, String type, int templateID, Timestamp startDate, Timestamp endDate,
                   int status, double amount, int projectID, int clientID, int freelancerID,
                   int paymentID, Timestamp clientSignatureDate, Timestamp freelancerSignatureDate) {
        this.idContract = idContract;
        this.type = type;
        this.templateID = templateID;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.amount = amount;
        this.projectID = projectID;
        this.clientID = clientID;
        this.freelancerID = freelancerID;
        this.paymentID = paymentID;
        this.clientSignatureDate = clientSignatureDate;
        this.freelancerSignatureDate = freelancerSignatureDate;
    }

    // Getters
    public int getIdContract() {
        return idContract;
    }

    public String getType() {
        return type;
    }

    public int getTemplateID() {
        return templateID;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public int getStatus() {
        return status;
    }

    public double getAmount() {
        return amount;
    }

    public int getProjectID() {
        return projectID;
    }

    public int getClientID() {
        return clientID;
    }

    public int getFreelancerID() {
        return freelancerID;
    }

    public int getPaymentID() {
        return paymentID;
    }

    public Timestamp getClientSignatureDate() {
        return clientSignatureDate;
    }

    public Timestamp getFreelancerSignatureDate() {
        return freelancerSignatureDate;
    }

    // Setters
    public void setIdContract(int idContract) {
        this.idContract = idContract;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTemplateID(int templateID) {
        this.templateID = templateID;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public void setFreelancerID(int freelancerID) {
        this.freelancerID = freelancerID;
    }

    public void setPaymentID(int paymentID) {
        this.paymentID = paymentID;
    }

    public void setClientSignatureDate(Timestamp clientSignatureDate) {
        this.clientSignatureDate = clientSignatureDate;
    }

    public void setFreelancerSignatureDate(Timestamp freelancerSignatureDate) {
        this.freelancerSignatureDate = freelancerSignatureDate;
    }

    public byte[] getClientSignatureImage() {
        return clientSignatureImage;
    }

    public void setClientSignatureImage(byte[] clientSignatureImage) {
        this.clientSignatureImage = clientSignatureImage;
    }

    public byte[] getFreelancerSignatureImage() {
        return freelancerSignatureImage;
    }

    public void setFreelancerSignatureImage(byte[] freelancerSignatureImage) {
        this.freelancerSignatureImage = freelancerSignatureImage;
    }

    /**
     * Obtenir le statut en texte
     */
    public String getStatusString() {
        return switch (status) {
            case 0 -> "Brouillon";
            case 1 -> "Signé Client";
            case 2 -> "Signé Freelancer";
            case 3 -> "Actif";
            case 4 -> "Annulé";
            default -> "Inconnu";
        };
    }

    @Override
    public String toString() {
        return "Contrat{" +
                "idContract=" + idContract +
                ", type='" + type + '\'' +
                ", templateID=" + templateID +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status=" + status +
                ", amount=" + amount +
                ", projectID=" + projectID +
                ", clientID=" + clientID +
                ", freelancerID=" + freelancerID +
                ", paymentID=" + paymentID +
                ", clientSignatureDate=" + clientSignatureDate +
                ", freelancerSignatureDate=" + freelancerSignatureDate +
                '}';
    }
}
