package uniearn.model.entities.users.freelancer;

import uniearn.model.entities.users.User;
import uniearn.model.enums.Status;
import uniearn.model.enums.UserRole;
import uniearn.model.enums.VerifStatus;

import java.util.Arrays;

public class Freelancer extends User {

    private int idFreelancer;
    private double pricePerHour;
    private double amount;
    private double rating;
    private String[] skills;
    private VerifStatus verificationStatus;
    private Status status;
    private Integer idTask;
    private String bio;
    private String studentCardPath;

    public Freelancer() {}

    // without idTask
    public Freelancer(String name, String email, String password, UserRole role, String profilePicturePath, double pricePerHour, double amount, double rating, String[] skills, VerifStatus verificationStatus, Status status, String bio, String studentCardPath) {
        super(name, email, password, role, profilePicturePath);
        this.pricePerHour = pricePerHour;
        this.amount = amount;
        this.rating = rating;
        this.skills = skills;
        this.verificationStatus = verificationStatus;
        this.status = status;
        this.bio = bio;
        this.studentCardPath = studentCardPath;
    }

    public Freelancer(String name, String email, String password, UserRole role, String profilePicturePath, double pricePerHour, double amount, double rating, String[] skills, VerifStatus verificationStatus, Status status, Integer idTask, String bio, String studentCardPath) {
        super(name, email, password, role, profilePicturePath);
        this.pricePerHour = pricePerHour;
        this.amount = amount;
        this.rating = rating;
        this.skills = skills;
        this.verificationStatus = verificationStatus;
        this.status = status;
        this.idTask = idTask;
        this.bio = bio;
        this.studentCardPath = studentCardPath;
    }

    public int getIdFreelancer() {
        return idFreelancer;
    }

    public void setIdFreelancer(int idFreelancer) {
        this.idFreelancer = idFreelancer;
    }

    public double getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(double pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
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

    public Integer getIdTask() {
        return idTask;
    }

    public void setIdTask(Integer idTask) {
        this.idTask = idTask;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getStudentCardPath() {
        return studentCardPath;
    }

    public void setStudentCardPath(String studentCardPath) {
        this.studentCardPath = studentCardPath;
    }

    @Override
    public String toString() {
        return "Freelancer{" +
                "idFreelancer=" + idFreelancer +
                ", pricePerHour=" + pricePerHour +
                ", amount=" + amount +
                ", rating=" + rating +
                ", skills=" + Arrays.toString(skills) +
                ", verificationStatus=" + verificationStatus +
                ", status=" + status +
                ", idTask=" + idTask +
                ", bio='" + bio + '\'' +
                ", studentCardPath='" + studentCardPath + '\'' +
                '}';
    }

}
