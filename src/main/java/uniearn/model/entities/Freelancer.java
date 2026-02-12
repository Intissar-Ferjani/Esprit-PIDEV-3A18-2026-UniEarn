package uniearn.model.entities;

import uniearn.model.enums.Status;
import uniearn.model.enums.UserRole;
import uniearn.model.enums.VerifStatus;

import java.util.Arrays;

public class Freelancer extends User {

    private double pricePerHour;
    private double amount;
    private double rating;
    private String[] skills;
    private VerifStatus verificationStatus;
    private Status status;
    private Integer idTask; // changed from int -> Integer to allow null

    public Freelancer() {}

    // Constructor without task
    public Freelancer(String name, String email, String password, UserRole role,
                      double pricePerHour, double amount, double rating, String[] skills,
                      VerifStatus verificationStatus, Status status) {
        this(name, email, password, role, pricePerHour, amount, rating, skills, verificationStatus, status, null);
    }

    // Full constructor with task
    public Freelancer(String name, String email, String password, UserRole role,
                      double pricePerHour, double amount, double rating, String[] skills,
                      VerifStatus verificationStatus, Status status, Integer idTask) {
        super(name, email, password, role);
        this.pricePerHour = pricePerHour;
        this.amount = amount;
        this.rating = rating;
        this.skills = skills;
        this.verificationStatus = verificationStatus;
        this.status = status;
        this.idTask = idTask;
    }

    // Getters & Setters
    public double getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(double pricePerHour) { this.pricePerHour = pricePerHour; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String[] getSkills() { return skills; }
    public void setSkills(String[] skills) { this.skills = skills; }

    public VerifStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerifStatus verificationStatus) { this.verificationStatus = verificationStatus; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Integer getIdTask() { return idTask; }
    public void setIdTask(Integer idTask) { this.idTask = idTask; }

    @Override
    public String toString() {
        return "Freelancer{" +
                "idUser=" + getIdUser() +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", pricePerHour=" + pricePerHour +
                ", amount=" + amount +
                ", rating=" + rating +
                ", skills=" + Arrays.toString(skills) +
                ", verificationStatus=" + verificationStatus +
                ", status=" + status +
                ", idTask=" + idTask +
                '}';
    }
}
