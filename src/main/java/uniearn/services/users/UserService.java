package uniearn.services.users;

import uniearn.database.MyConnection;
import uniearn.interfaces.users.IUser;
import uniearn.model.entities.users.User;
import uniearn.model.enums.UserRole;
import uniearn.utils.user.PasswordUtil;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService implements IUser<User> {
    private final Connection cn = MyConnection.getInstance().getCnx();
    private static final Map<String, String[]> resetTokens = new HashMap<>();


    @Override
    public int addUser(User user) throws SQLException {
        String sql = "INSERT INTO user (name, email, password, role, profilePicturePath, activated) VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, user.getName());
        ps.setString(2, user.getEmail());

        // hash pass before storing (only if not already hashed)
        String password = user.getPassword();
        if (!PasswordUtil.isHashed(password)) {
            password = PasswordUtil.hashPassword(password);
        } else {
            System.out.println("✓ Password already hashed, no re-hashing needed");
        }
        ps.setString(3, password);

        ps.setString(4, user.getRole().name());
        ps.setString(5, user.getProfilePicturePath());
        ps.setBoolean(6, user.isActivated());

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            int userId = rs.getInt(1);
            System.out.println("✓ User created with hashed password. User ID: " + userId);
            return userId;
        }

        return -1;
    }

    @Override
    public void updateUser(int id, User user) {
        String sql = "UPDATE user SET name=?, email=?, password=?, role=?, profilePicturePath=?, activated=? WHERE idUser=?";

        try {
            PreparedStatement ps = cn.prepareStatement(sql);

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());

            // if not yet hashed -> hash it
            String password = user.getPassword();
            if (!PasswordUtil.isHashed(password)) {
                password = PasswordUtil.hashPassword(password);
                System.out.println("✓ Password hashed during update");
            } else {
                System.out.println("✓ Password already hashed, no re-hashing needed");
            }
            ps.setString(3, password);

            ps.setString(4, user.getRole().name());
            ps.setString(5, user.getProfilePicturePath());
            ps.setBoolean(6, user.isActivated());
            ps.setInt(7, id);

            ps.executeUpdate();
            System.out.println("User updated successfully!");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    // Update only password (when reset/change)
    public void updatePassword(int userId, String newPassword) throws SQLException {
        String sql = "UPDATE user SET password=? WHERE idUser=?";

        PreparedStatement ps = cn.prepareStatement(sql);

        // Hash the new password
        String hashedPassword = PasswordUtil.hashPassword(newPassword);
        ps.setString(1, hashedPassword);
        ps.setInt(2, userId);

        int rowsAffected = ps.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("✓ Password updated successfully for user: " + userId);
        } else {
            System.out.println("User not found: " + userId);
        }
    }

    public void updateProfilePicture(int userId, String profilePicturePath) throws SQLException {
        String sql = "UPDATE user SET profilePicturePath=? WHERE idUser=?";

        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setString(1, profilePicturePath);
        ps.setInt(2, userId);

        ps.executeUpdate();
        System.out.println("✓ Profile picture updated for user: " + userId);
    }

    @Override
    public void deleteUser(int id) {
        String sql = "DELETE FROM user WHERE idUser=?";

        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, id);

            ps.executeUpdate();
            System.out.println("User deleted successfully!");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public User getUserById(int id) {
        String sql = "SELECT * FROM user WHERE idUser=?";

        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User u = new User();
                u.setIdUser(rs.getInt("idUser"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setRole(UserRole.valueOf(rs.getString("role")));
                u.setProfilePicturePath(rs.getString("profilePicturePath"));
                u.setActivated(rs.getBoolean("activated"));
                return u;
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }


    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM user WHERE email = ?";

        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, email.toLowerCase().trim());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                boolean exists = count > 0;

                if (exists) {
                    System.out.println("⚠ Email already exists: " + email);
                } else {
                    System.out.println("✓ Email available: " + email);
                }

                return exists;
            }

        } catch (SQLException e) {
            System.out.println("Error checking email existence: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }


    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM user WHERE email = ?";

        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, email.toLowerCase().trim());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User u = new User();
                u.setIdUser(rs.getInt("idUser"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password")); // This will be hashed
                u.setRole(UserRole.valueOf(rs.getString("role")));
                u.setProfilePicturePath(rs.getString("profilePicturePath"));
                u.setActivated(rs.getBoolean("activated"));
                return u;
            }

        } catch (SQLException e) {
            System.out.println("Error getting user by email: " + e.getMessage());
        }

        return null;
    }

    //without admin
    @Override
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE role != 'ADMIN'";

        try {
            Statement st = cn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                User u = new User();
                u.setIdUser(rs.getInt("idUser"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setRole(UserRole.valueOf(rs.getString("role")));
                u.setProfilePicturePath(rs.getString("profilePicturePath"));
                u.setActivated(rs.getBoolean("activated"));

                users.add(u);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return users;
    }

    // with admin
    public List<User> getAllUsersIncludingAdmins() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user";

        try {
            Statement st = cn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                User u = new User();
                u.setIdUser(rs.getInt("idUser"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setRole(UserRole.valueOf(rs.getString("role")));
                u.setProfilePicturePath(rs.getString("profilePicturePath"));
                u.setActivated(rs.getBoolean("activated"));

                users.add(u);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return users;
    }

    // all active + without admin
    public List<User> getAllActiveUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE activated=true AND role != 'ADMIN'";

        try {
            Statement st = cn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                User u = new User();
                u.setIdUser(rs.getInt("idUser"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password")); // This will be hashed
                u.setRole(UserRole.valueOf(rs.getString("role")));
                u.setProfilePicturePath(rs.getString("profilePicturePath"));
                u.setActivated(rs.getBoolean("activated"));

                users.add(u);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return users;
    }


    @Override
    public User authenticateUser(String email, String password) {
        String sql = "SELECT * FROM user WHERE email = ? AND activated=true";

        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");

                // using bycrypt to verify password
                if (PasswordUtil.verifyPassword(password, storedHashedPassword)) {
                    User u = new User();
                    u.setIdUser(rs.getInt("idUser"));
                    u.setName(rs.getString("name"));
                    u.setEmail(rs.getString("email"));
                    u.setPassword(rs.getString("password"));
                    u.setRole(UserRole.valueOf(rs.getString("role")));
                    u.setProfilePicturePath(rs.getString("profilePicturePath"));
                    u.setActivated(rs.getBoolean("activated"));

                    System.out.println("✓ User authenticated successfully: " + email);
                    return u;
                } else {
                    System.out.println("⚠ Authentication failed: Incorrect password for " + email);
                }
            } else {
                System.out.println("⚠ Authentication failed: User not found or account not activated");
            }

        } catch (SQLException e) {
            System.out.println("Authentication error: " + e.getMessage());
        }

        return null;
    }

    public void deactivateUser(int userId) throws SQLException {
        String sql = "UPDATE user SET activated=false WHERE idUser=?";

        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setInt(1, userId);

        int rowsAffected = ps.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("✓ User account deactivated: " + userId);
        } else {
            System.out.println("User not found: " + userId);
        }
    }

    public void reactivateUser(int userId) throws SQLException {
        String sql = "UPDATE user SET activated=true WHERE idUser=?";

        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setInt(1, userId);

        int rowsAffected = ps.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("✓ User account reactivated: " + userId);
        } else {
            System.out.println("User not found: " + userId);
        }
    }


    //email reset pass
    public String generateResetToken(String email) {

        //check user exists
        User user = getUserByEmail(email);
        if (user == null) return null;

        // Generate 6-digit code
        String token = String.format("%06d", new java.util.Random().nextInt(999999));
        String expiry = LocalDateTime.now().plusMinutes(15).toString();

        resetTokens.put(email.toLowerCase(), new String[]{token, expiry});
        System.out.println("✓ Reset token generated for: " + email);
        return token;
    }

    public boolean validateResetToken(String email, String token) {
        String[] data = resetTokens.get(email.toLowerCase());
        if (data == null) return false;

        String storedToken = data[0];
        LocalDateTime expiry = LocalDateTime.parse(data[1]);

        if (LocalDateTime.now().isAfter(expiry)) {
            resetTokens.remove(email.toLowerCase());
            System.out.println("⚠ Reset token expired for: " + email);
            return false;
        }

        return storedToken.equals(token);
    }

    public void clearResetToken(String email) {

        resetTokens.remove(email.toLowerCase());
    }
}