package uniearn.services.users;

import uniearn.database.MyConnection;
import uniearn.model.entities.users.admin.Admin;
import uniearn.model.enums.UserRole;

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
}