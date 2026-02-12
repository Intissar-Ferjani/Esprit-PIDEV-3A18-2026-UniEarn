package uniearn.services;

import uniearn.database.MyConnection;
import uniearn.interfaces.IFreelancer;
import uniearn.model.entities.Freelancer;
import uniearn.model.entities.User;
import uniearn.model.enums.Status;
import uniearn.model.enums.UserRole;
import uniearn.model.enums.VerifStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FreelancerService extends UserService implements IFreelancer<Freelancer, User> {

    private final Connection cn = MyConnection.getInstance().getCnx();

    @Override
    public void addFreelancer(Freelancer freelancer) {
        try {
            int generatedId = super.addUser(freelancer);

            String sql = "INSERT INTO freelancer " +
                    "(idUser, pricePerHour, amount, rating, skills, verificationStatus, status, idTask) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = cn.prepareStatement(sql);

            ps.setInt(1, generatedId);
            ps.setDouble(2, freelancer.getPricePerHour());
            ps.setDouble(3, freelancer.getAmount());
            ps.setDouble(4, freelancer.getRating());
            ps.setString(5, String.join(",", freelancer.getSkills()));

            ps.setString(6, freelancer.getVerificationStatus().name());
            ps.setString(7, freelancer.getStatus().name().toLowerCase());

            // Handle nullable idTask
            if (freelancer.getIdTask() != null) {
                ps.setInt(8, freelancer.getIdTask());
            } else {
                ps.setNull(8, java.sql.Types.INTEGER);
            }

            ps.executeUpdate();
            System.out.println("Freelancer added successfully!");
        } catch (SQLException e) {
            System.out.println("Error adding freelancer: " + e.getMessage());
        }
    }

    @Override
    public void updateFreelancer(int id, Freelancer freelancer) {
        super.updateUser(id, freelancer);

        try {
            String sql = "UPDATE freelancer SET pricePerHour=?, amount=?, rating=?, skills=?, verificationStatus=?, status=?, idTask=? WHERE idUser=?";
            PreparedStatement ps = cn.prepareStatement(sql);

            ps.setDouble(1, freelancer.getPricePerHour());
            ps.setDouble(2, freelancer.getAmount());
            ps.setDouble(3, freelancer.getRating());
            ps.setString(4, String.join(",", freelancer.getSkills()));

            ps.setString(5, freelancer.getVerificationStatus().name());
            ps.setString(6, freelancer.getStatus().name().toLowerCase());

            if (freelancer.getIdTask() != null) {
                ps.setInt(7, freelancer.getIdTask());
            } else {
                ps.setNull(7, java.sql.Types.INTEGER);
            }

            ps.setInt(8, id);
            ps.executeUpdate();
            System.out.println("Freelancer updated successfully!");
        } catch (SQLException e) {
            System.out.println("Error updating freelancer: " + e.getMessage());
        }
    }

    @Override
    public void deleteFreelancer(int id) {
        try {
            String sql = "DELETE FROM freelancer WHERE idUser=?";
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();

            super.deleteUser(id);
            System.out.println("Freelancer deleted successfully!");
        } catch (SQLException e) {
            System.out.println("Error deleting freelancer: " + e.getMessage());
        }
    }

    @Override
    public Freelancer getFreelancerById(int id) {
        User baseUser = super.getUserById(id);
        if (baseUser == null) return null;

        String sql = "SELECT * FROM freelancer WHERE idUser=?";
        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Freelancer f = new Freelancer();

                f.setIdUser(baseUser.getIdUser());
                f.setName(baseUser.getName());
                f.setEmail(baseUser.getEmail());
                f.setPassword(baseUser.getPassword());
                f.setRole(baseUser.getRole());

                f.setPricePerHour(rs.getDouble("pricePerHour"));
                f.setAmount(rs.getDouble("amount"));
                f.setRating(rs.getDouble("rating"));
                f.setSkills(rs.getString("skills").split(","));

                f.setVerificationStatus(VerifStatus.valueOf(rs.getString("verificationStatus")));

                f.setStatus(Status.valueOf(rs.getString("status").toUpperCase()));

                int idTask = rs.getInt("idTask");
                if (rs.wasNull()) {
                    f.setIdTask(null);
                } else {
                    f.setIdTask(idTask);
                }

                return f;
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving freelancer: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Freelancer> getAllFreelancers() {
        List<Freelancer> freelancers = new ArrayList<>();
        List<User> users = super.getAllUsers();

        for (User u : users) {
            if (u.getRole() == UserRole.FREELANCER) {
                Freelancer f = getFreelancerById(u.getIdUser());
                if (f != null) freelancers.add(f);
            }
        }
        return freelancers;
    }
}
