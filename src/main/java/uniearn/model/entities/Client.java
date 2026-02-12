package uniearn.model.entities;

import uniearn.interfaces.IClient;
import uniearn.model.enums.UserRole;

import java.util.List;

public class Client extends User {
    private int idClient;
    private double amount;
    private double rating;

    public Client() {}

    public Client(String name, String email, String password, UserRole role, double amount, double rating) {
        super(name, email, password, role);
        this.amount = amount;
        this.rating = rating;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getIdClient() {
        return idClient;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    @Override
    public String toString() {
        return "Client{" +
                "idClient=" + idClient +
                ", amount=" + amount +
                ", rating=" + rating +
                '}';
    }
}
