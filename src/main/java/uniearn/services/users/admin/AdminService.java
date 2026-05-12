package uniearn.services.users.admin;

import uniearn.database.MyConnection;
import uniearn.model.entities.users.admin.Admin;
import uniearn.model.enums.UserRole;
import uniearn.services.users.UserService;

import java.sql.*;

public class AdminService {

    private final Connection cn = MyConnection.getInstance().getCnx();
    private final UserService userService = new UserService();

    public int addAdmin(Admin admin) throws SQLException {
        admin.setRole(UserRole.ADMIN);
        admin.setActivated(true);
        int userId = userService.addUser(admin);

        if (userId <= 0) {
            System.out.println("✗ Failed to insert into user table.");
            return -1;
        }

        String sql = "INSERT INTO admin (idUser) VALUES (?)";
        PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, userId);
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            int adminId = rs.getInt(1);
            admin.setIdUser(userId);
            admin.setIdAdmin(adminId);
            System.out.println("✓ Admin created. idUser=" + userId + ", idAdmin=" + adminId);
            return adminId;
        }

        return -1;
    }

    public Admin getAdminById(int userId) {
        String sql = "SELECT u.*, a.idAdmin FROM user u " +
                     "LEFT JOIN admin a ON u.idUser = a.idUser " +
                     "WHERE u.idUser = ?";

        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Admin admin = new Admin();
                admin.setIdUser(rs.getInt("idUser"));
                admin.setName(rs.getString("name"));
                admin.setEmail(rs.getString("email"));
                admin.setPassword(rs.getString("password"));
                admin.setRole(UserRole.valueOf(rs.getString("role")));
                admin.setActivated(rs.getBoolean("activated"));

                int idAdmin = rs.getInt("idAdmin");
                if (idAdmin > 0) {
                    admin.setIdAdmin(idAdmin);
                }

                return admin;
            }

        } catch (SQLException e) {
            System.err.println("✗ Error loading admin by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}