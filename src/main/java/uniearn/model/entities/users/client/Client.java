package uniearn.model.entities.users.client;

import uniearn.model.entities.users.User;
import uniearn.model.enums.UserRole;

public class Client extends User {
    private int idClient;
    private double amount;
    private double rating;
    private String company;
    private String industry;

    public Client() {}

    public Client(String name, String email, String password, UserRole role, String profilePicturePath, double amount, double rating, String company, String industry) {
        super(name, email, password, role, profilePicturePath);
        this.amount = amount;
        this.rating = rating;
        this.company = company;
        this.industry = industry;
    }

    public int getIdClient() {
        return idClient;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
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

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    @Override
    public String toString() {
        return "Client{" +
                "idClient=" + idClient +
                ", amount=" + amount +
                ", rating=" + rating +
                ", company='" + company + '\'' +
                ", industry='" + industry + '\'' +
                '}';
    }
}
