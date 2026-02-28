package uniearn.services;

import uniearn.database.MyConnection;
import uniearn.interfaces.IUser;
import uniearn.model.entities.users.User;
import uniearn.model.enums.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les utilisateurs
 */
public class UserService implements IUser<User> {
    private final Connection cn = MyConnection.getInstance().getCnx();

    @Override
    public void addUser(User user) throws SQLException {

        String sql = "INSERT INTO user (name, email, password, role) VALUES (?, ?, ?, ?)";

        PreparedStatement ps = cn.prepareStatement(sql);

        ps.setString(1, user.getName());
        ps.setString(2, user.getEmail());
        ps.setString(3, user.getPassword());
        ps.setString(4, user.getRole().name());

        ps.executeUpdate();
        System.out.println("User added to DB successfully!");
    }


    @Override
    public void updateUser(int id, User user) {
        String sql = "UPDATE user SET name=?, email=?, password=?, role=? WHERE idUser=?";

        try {
            PreparedStatement ps = cn.prepareStatement(sql);

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole().name());
            ps.setInt(5, id);

            ps.executeUpdate();
            System.out.println("User updated successfully!");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
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
                return u;
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    @Override
    public List<User> getAllUsers() {
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

                users.add(u);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return users;
    }
}
