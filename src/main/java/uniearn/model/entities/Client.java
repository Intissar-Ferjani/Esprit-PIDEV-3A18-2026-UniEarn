package uniearn.model.entities;

import uniearn.interfaces.IClient;
import uniearn.model.enums.UserRole;

import java.util.List;

public class Client extends User {
    private double balance;
    private double rating;

    public Client() {}

    public Client(String name, String email, String password, UserRole role, double balance, double rating) {
        super(name, email, password, role);
        this.balance = balance;
        this.rating = rating;
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

    @Override
    public String toString() {
        return "Client{" +
                "balance=" + balance +
                ", rating=" + rating +
                '}';
    }

}
