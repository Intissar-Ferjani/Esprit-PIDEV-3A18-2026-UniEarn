package uniearn.model.entities;

import uniearn.interfaces.IClient;
import uniearn.model.enums.UserRole;

import java.util.List;

public class Client extends User implements IClient<Client,User> {
    private double balance;
    private double rating;

    public Client() {}

    public Client(int id, String name, String email, String password, UserRole role, double balance, double rating) {
        super(id, name, email, password, role);
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


    @Override
    public void addClient(Client client) {

    }

    @Override
    public void updateClient(int id, Client client) {

    }

    @Override
    public void deleteClient(int id) {

    }

    @Override
    public Client getClientById(int id) {
        return null;
    }

    @Override
    public List<Client> getAllClients() {
        return List.of();
    }
}
