package uniearn.services;

import uniearn.database.MyConnection;
import uniearn.interfaces.IClient;
import uniearn.model.entities.Client;
import uniearn.model.entities.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientService extends UserService implements IClient<Client, User> {

    private final Connection cn = MyConnection.getInstance().getCnx();

    public ClientService() {
        System.out.println("ClientService initialized");
    }

    @Override
    public void addClient(Client client) {
        try {
            int generatedUserId = super.addUser(client);

            String sql = "INSERT INTO client (amount, rating, userID) VALUES (?, ?, ?)";

            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setDouble(1, client.getAmount());
            ps.setDouble(2, client.getRating());
            ps.setInt(3, generatedUserId);

            ps.executeUpdate();

            System.out.println("Client added successfully!");

        } catch (SQLException e) {
            System.out.println("Error adding client: " + e.getMessage());
        }
    }

    @Override
    public void updateClient(int idUser, Client client) {

        super.updateUser(idUser, client);

        String sql = "UPDATE client SET amount=?, rating=? WHERE userID=?";

        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setDouble(1, client.getAmount());
            ps.setDouble(2, client.getRating());
            ps.setInt(3, idUser);

            ps.executeUpdate();

            System.out.println("Client updated successfully!");

        } catch (SQLException e) {
            System.out.println("Error updating client: " + e.getMessage());
        }
    }

    @Override
    public void deleteClient(int idUser) {

        String sql = "DELETE FROM client WHERE userID=?";

        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, idUser);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error deleting client: " + e.getMessage());
        }

        super.deleteUser(idUser);
    }

    @Override
    public Client getClientById(int idUser) {

        String sql = "SELECT * FROM client WHERE userID=?";

        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, idUser);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                User user = super.getUserById(idUser);

                Client client = new Client();
                client.setIdClient(rs.getInt("idClient"));
                client.setAmount(rs.getDouble("amount"));
                client.setRating(rs.getDouble("rating"));

                client.setIdUser(user.getIdUser());
                client.setName(user.getName());
                client.setEmail(user.getEmail());
                client.setPassword(user.getPassword());
                client.setRole(user.getRole());

                return client;
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving client: " + e.getMessage());
        }

        return null;
    }

    @Override
    public List<Client> getAllClients() {

        List<Client> clients = new ArrayList<>();

        String sql = "SELECT * FROM client";

        try {
            Statement st = cn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {

                int userID = rs.getInt("userID");

                User user = super.getUserById(userID);

                Client client = new Client();
                client.setIdClient(rs.getInt("idClient"));
                client.setAmount(rs.getDouble("amount"));
                client.setRating(rs.getDouble("rating"));

                client.setIdUser(user.getIdUser());
                client.setName(user.getName());
                client.setEmail(user.getEmail());
                client.setPassword(user.getPassword());
                client.setRole(user.getRole());

                clients.add(client);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving clients: " + e.getMessage());
        }

        return clients;
    }
}
