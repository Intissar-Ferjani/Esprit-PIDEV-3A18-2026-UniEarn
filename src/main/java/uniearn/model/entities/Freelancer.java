package uniearn.model.entities;

import uniearn.interfaces.IFreelancer;
import uniearn.model.enums.Status;
import uniearn.model.enums.UserRole;
import uniearn.model.enums.VerifStatus;

import java.util.Arrays;
import java.util.List;

public class Freelancer extends User {
    private double hourly;
    private double balance;
    private double rating;
    private String[] skills;
    private VerifStatus verificationStatus;
    private Status status;

    public Freelancer() {}

    public Freelancer(String name, String email, String password, UserRole role, double hourly, double balance, double rating, String[] skills, VerifStatus verificationStatus, Status status) {
        super(name, email, password, role);
        this.hourly = hourly;
        this.balance = balance;
        this.rating = rating;
        this.skills = skills;
        this.verificationStatus = verificationStatus;
        this.status = status;
    }

    public double getHourly() {
        return hourly;
    }

    public void setHourly(double hourly) {
        this.hourly = hourly;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String[] getSkills() {
        return skills;
    }

    public void setSkills(String[] skills) {
        this.skills = skills;
    }

    public VerifStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerifStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Freelancer{" +
                "hourly=" + hourly +
                ", balance=" + balance +
                ", rating=" + rating +
                ", skills=" + Arrays.toString(skills) +
                ", verificationStatus=" + verificationStatus +
                ", status=" + status +
                '}';
    }

}
