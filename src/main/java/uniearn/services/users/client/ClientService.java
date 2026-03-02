package uniearn.services.users.client;

import uniearn.database.MyConnection;
import uniearn.interfaces.users.client.IClient;
import uniearn.model.entities.users.client.Client;
import uniearn.model.entities.users.User;
import uniearn.model.enums.UserRole;
import uniearn.services.users.UserService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientService extends UserService implements IClient<Client, User> {

    private final Connection cn = MyConnection.getInstance().getCnx();

    public ClientService() {
        System.out.println("ClientService initialized");
    }

    @Override
    public void addClient(Client client) throws SQLException {
        System.out.println("=== Starting Client Creation ===");
        System.out.println("Client name: " + client.getName());
        System.out.println("Client email: " + client.getEmail());
        System.out.println("Client company: " + client.getCompany());

        // Step 1: Insert into user table (password will be hashed by UserService)
        int generatedUserId = super.addUser(client);
        if (generatedUserId <= 0) {
            throw new SQLException("Failed to create user record - no ID generated");
        }

        client.setIdUser(generatedUserId);
        System.out.println("✓ User created with ID: " + generatedUserId);

        // Step 2: Insert into client table using correct column name 'userID'
        String sql = "INSERT INTO client (amount, rating, company, industry, userID) VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setDouble(1, client.getAmount());
            ps.setDouble(2, client.getRating());
            ps.setString(3, client.getCompany());
            ps.setString(4, client.getIndustry());
            ps.setInt(5, generatedUserId);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Failed to insert client record - no rows affected");
            }

            System.out.println("✓ Client record created. Rows affected: " + rowsAffected);
            System.out.println("=== Client Creation Complete ===");

        } catch (SQLException e) {
            System.err.println("❌ ERROR creating client record: " + e.getMessage());
            e.printStackTrace();

            // Rollback user creation if client creation fails
            try {
                super.deleteUser(generatedUserId);
                System.out.println("⚠ Rolled back user creation due to client creation failure");
            } catch (Exception rollbackError) {
                System.err.println("❌ Failed to rollback user creation: " + rollbackError.getMessage());
            }
            throw e;
        }
    }

    @Override
    public void updateClient(int idUser, Client client) {
        // Update user info WITHOUT touching the password
        String userSql = "UPDATE user SET name=?, email=?, role=?, profilePicturePath=?, activated=? WHERE idUser=?";
        try {
            PreparedStatement ps = cn.prepareStatement(userSql);
            ps.setString(1, client.getName());
            ps.setString(2, client.getEmail());
            ps.setString(3, client.getRole().name());
            ps.setString(4, client.getProfilePicturePath());
            ps.setBoolean(5, client.isActivated());
            ps.setInt(6, idUser);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Update client-specific fields
        String clientSql = "UPDATE client SET amount=?, rating=?, company=?, industry=? WHERE userID=?";
        try {
            PreparedStatement ps = cn.prepareStatement(clientSql);
            ps.setDouble(1, client.getAmount());
            ps.setDouble(2, client.getRating());
            ps.setString(3, client.getCompany());
            ps.setString(4, client.getIndustry());
            ps.setInt(5, idUser);
            ps.executeUpdate();
            System.out.println("✓ Client updated successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteClient(int idUser) {
        String sql = "DELETE FROM client WHERE userID=?";

        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, idUser);
            ps.executeUpdate();
            System.out.println("✓ Client record deleted");

        } catch (SQLException e) {
            System.err.println("❌ Error deleting client: " + e.getMessage());
            e.printStackTrace();
        }

        super.deleteUser(idUser);
    }

    @Override
    public Client getClientById(int idUser) {
        System.out.println("=== Looking up client with userID: " + idUser + " ===");

        String sql = "SELECT * FROM client WHERE userID=?";

        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, idUser);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = super.getUserById(idUser);

                if (user == null) {
                    System.err.println("❌ User not found with idUser: " + idUser);
                    return null;
                }

                Client client = new Client();
                client.setIdClient(rs.getInt("idClient"));
                client.setAmount(rs.getDouble("amount"));
                client.setRating(rs.getDouble("rating"));
                client.setCompany(rs.getString("company"));
                client.setIndustry(rs.getString("industry"));

                client.setIdUser(user.getIdUser());
                client.setName(user.getName());
                client.setEmail(user.getEmail());
                client.setPassword(user.getPassword());
                client.setRole(user.getRole());
                client.setProfilePicturePath(user.getProfilePicturePath());
                client.setActivated(user.isActivated());

                System.out.println("✓ Client data loaded successfully for: " + client.getName());
                return client;

            } else {
                System.err.println("❌ No client record found in client table for userID: " + idUser);

                // Debug - check if user exists
                User user = super.getUserById(idUser);
                if (user != null) {
                    System.err.println("⚠ User exists but client record is missing!");
                    System.err.println("User role: " + user.getRole());
                } else {
                    System.err.println("⚠ User also doesn't exist in user table!");
                }

                return null;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error retrieving client: " + e.getMessage());
            e.printStackTrace();
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

                if (user == null) {
                    System.err.println("⚠ Skipping client - user not found for userID: " + userID);
                    continue;
                }

                Client client = new Client();
                client.setIdClient(rs.getInt("idClient"));
                client.setAmount(rs.getDouble("amount"));
                client.setRating(rs.getDouble("rating"));
                client.setCompany(rs.getString("company"));
                client.setIndustry(rs.getString("industry"));

                client.setIdUser(user.getIdUser());
                client.setName(user.getName());
                client.setEmail(user.getEmail());
                client.setPassword(user.getPassword());
                client.setRole(user.getRole());
                client.setProfilePicturePath(user.getProfilePicturePath());
                client.setActivated(user.isActivated());

                clients.add(client);
            }

            System.out.println("✓ Retrieved " + clients.size() + " clients");

        } catch (SQLException e) {
            System.err.println("❌ Error retrieving clients: " + e.getMessage());
            e.printStackTrace();
        }

        return clients;
    }

    /**
     * ✅ FIXED: Check if a company name is already registered in the system
     * This method is now safe to call and won't throw unexpected exceptions
     *
     * @param companyName The company name to check
     * @return true if company name exists, false otherwise
     */
    public boolean companyExists(String companyName) {
        // Null or empty company names are allowed (not required)
        if (companyName == null || companyName.trim().isEmpty()) {
            System.out.println("✓ Empty company name - skipping check");
            return false;
        }

        String sql = "SELECT COUNT(*) FROM client WHERE LOWER(company) = LOWER(?)";

        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, companyName.trim());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                boolean exists = count > 0;

                if (exists) {
                    System.out.println("⚠ Company name already exists: " + companyName);
                } else {
                    System.out.println("✓ Company name available: " + companyName);
                }

                return exists;
            }

        } catch (SQLException e) {
            // Don't throw exception - just log and return false
            // This allows the validation to continue gracefully
            System.err.println("❌ Error checking company existence: " + e.getMessage());
            System.err.println("   SQL State: " + e.getSQLState());
            System.err.println("   Error Code: " + e.getErrorCode());
            e.printStackTrace();

            // Return false to allow user to proceed (fail-open approach)
            // The database unique constraint will catch duplicates as a fallback
            System.out.println("⚠ Returning false due to database error (fail-open)");
        }

        return false;
    }

    /**
     * Get a client by their company name
     * @param companyName The company name to search for
     * @return Client object if found, null otherwise
     */
    public Client getClientByCompany(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT * FROM client WHERE LOWER(company) = LOWER(?)";

        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, companyName.trim());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int userID = rs.getInt("userID");
                User user = super.getUserById(userID);

                if (user == null) {
                    System.err.println("❌ User not found for company: " + companyName);
                    return null;
                }

                Client client = new Client();
                client.setIdClient(rs.getInt("idClient"));
                client.setAmount(rs.getDouble("amount"));
                client.setRating(rs.getDouble("rating"));
                client.setCompany(rs.getString("company"));
                client.setIndustry(rs.getString("industry"));

                client.setIdUser(user.getIdUser());
                client.setName(user.getName());
                client.setEmail(user.getEmail());
                client.setPassword(user.getPassword());
                client.setRole(user.getRole());
                client.setProfilePicturePath(user.getProfilePicturePath());
                client.setActivated(user.isActivated());

                return client;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting client by company: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}