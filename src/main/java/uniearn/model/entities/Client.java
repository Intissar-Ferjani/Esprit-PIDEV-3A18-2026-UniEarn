package uniearn.model.entities;

import uniearn.model.entities.users.User;
import uniearn.model.enums.UserRole;

import java.io.Serializable;

public class Client extends User implements Serializable {
    private int idClient;
    private double amount;
    private double rating;
    private String company;
    private String industry;
    private int userID;

    // Constructors
    public Client() {}

    public Client(String name, String email, String password, UserRole role, double amount, double rating) {
        super(name, email, password, role);
        this.amount = amount;
        this.rating = rating;
    }

    public Client(int idClient, String name, String email, String password, UserRole role,
                  double amount, double rating, String company, String industry, int userID) {
        super(name, email, password, role);
        this.idClient = idClient;
        this.amount = amount;
        this.rating = rating;
        this.company = company;
        this.industry = industry;
        this.userID = userID;
    }

    // Getters
    public int getIdClient() {
        return idClient;
    }

    public double getAmount() {
        return amount;
    }

    public double getRating() {
        return rating;
    }

    public String getCompany() {
        return company;
    }

    public String getIndustry() {
        return industry;
    }

    public int getUserID() {
        return userID;
    }

    // Setters
    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    @Override
    public String toString() {
        return "Client{" +
                "idClient=" + idClient +
                ", name='" + this.getName() + '\'' +
                ", amount=" + amount +
                ", rating=" + rating +
                ", company='" + company + '\'' +
                ", industry='" + industry + '\'' +
                '}';
    }

}
