package uniearn.services.users.freelancer;

import uniearn.database.MyConnection;
import uniearn.interfaces.users.freelancer.IFreelancer;
import uniearn.model.entities.users.freelancer.Freelancer;
import uniearn.model.entities.users.User;
import uniearn.model.enums.Status;
import uniearn.model.enums.UserRole;
import uniearn.model.enums.VerifStatus;
import uniearn.services.users.UserService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FreelancerService extends UserService implements IFreelancer<Freelancer, User> {

    private final Connection cn = MyConnection.getInstance().getCnx();

    // ── Step 2 ────────────────────────────────────────────────────────────────
    @Override
    public void addFreelancer(Freelancer freelancer) throws SQLException {

        // + check if same user is already inserted
        int existingUserId = findUserIdByEmail(freelancer.getEmail());

        int generatedUserId;
        if (existingUserId > 0) {
            // User row already exists — reuse it, don't insert again
            generatedUserId = existingUserId;
            System.out.println("⚠ User row already exists for " + freelancer.getEmail()
                    + " (idUser=" + generatedUserId + ") — skipping duplicate insert");
        } else {
            generatedUserId = super.addUser(freelancer);
        }

        freelancer.setIdUser(generatedUserId);

        // ── Avoid Duplc ─────────────────────
        if (freelancerRowExists(generatedUserId)) {
            System.out.println("⚠ Freelancer row already exists for idUser=" + generatedUserId
                    + " — skipping duplicate insert");
            return;
        }

        String sql = "INSERT INTO freelancer " +
                "(idUser, pricePerHour, amount, rating, skills, bio, studentCardPath, cvPath, verificationStatus, status, idTask) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        ps.setInt(1, generatedUserId);
        ps.setDouble(2, freelancer.getPricePerHour());
        ps.setDouble(3, freelancer.getAmount());
        ps.setDouble(4, freelancer.getRating());
        ps.setString(5, String.join(",", freelancer.getSkills()));
        ps.setString(6, freelancer.getBio());

        ps.setNull(7, java.sql.Types.VARCHAR);

        if (freelancer.getCvPath() != null) {
            ps.setString(8, freelancer.getCvPath());
        } else {
            ps.setNull(8, java.sql.Types.VARCHAR);
        }

        ps.setString(9, freelancer.getVerificationStatus().name());
        ps.setString(10, freelancer.getStatus().name().toLowerCase());

        if (freelancer.getIdTask() != null) {
            ps.setInt(11, freelancer.getIdTask());
        } else {
            ps.setNull(11, java.sql.Types.INTEGER);
        }

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            int generatedFreelancerId = rs.getInt(1);
            freelancer.setIdFreelancer(generatedFreelancerId);
            System.out.println("✓ Freelancer profile created with idFreelancer: " + generatedFreelancerId);
        }

        System.out.println("✓ Freelancer profile created successfully (Step 2)");
    }

    // ── Helpers ─────────────────────────
    private int findUserIdByEmail(String email) {
        String sql = "SELECT idUser FROM user WHERE email = ?";
        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, email.toLowerCase().trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt("idUser");
        } catch (SQLException e) {
            System.err.println("Error checking existing user by email: " + e.getMessage());
        }
        return -1;
    }

    private boolean freelancerRowExists(int userId) {
        String sql = "SELECT COUNT(*) FROM freelancer WHERE idUser = ?";
        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error checking existing freelancer row: " + e.getMessage());
        }
        return false;
    }

    public int getUserIdByFreelancerId(int freelancerId) {
        String sql = "SELECT idUser FROM freelancer WHERE idFreelancer = ?";
        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, freelancerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt("idUser");
        } catch (SQLException e) {
            System.err.println("Error resolving userId from freelancerId: " + e.getMessage());
        }
        return -1;
    }

    // ── Step 3: Update freelancer + student card verification ──────────────
    public void updateVerificationData(int freelancerId, String studentCardPath, VerifStatus status)
            throws SQLException {
        String sql = "UPDATE freelancer SET studentCardPath = ?, verificationStatus = ? WHERE idUser = ?";
        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setString(1, studentCardPath);
        ps.setString(2, status.name());
        ps.setInt(3, freelancerId);
        int rowsAffected = ps.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("✓ Verification data updated successfully (Step 3)");
        }
    }

    @Override
    public void updateFreelancer(int id, Freelancer freelancer) {
        super.updateUser(id, freelancer);

        try {
            String sql = "UPDATE freelancer SET pricePerHour=?, amount=?, rating=?, skills=?, bio=?, " +
                    "studentCardPath=?, verificationStatus=?, status=?, idTask=? WHERE idUser=?";
            PreparedStatement ps = cn.prepareStatement(sql);

            ps.setDouble(1, freelancer.getPricePerHour());
            ps.setDouble(2, freelancer.getAmount());
            ps.setDouble(3, freelancer.getRating());
            ps.setString(4, String.join(",", freelancer.getSkills()));
            ps.setString(5, freelancer.getBio());

            if (freelancer.getStudentCardPath() != null) {
                ps.setString(6, freelancer.getStudentCardPath());
            } else {
                ps.setNull(6, java.sql.Types.VARCHAR);
            }

            ps.setString(7, freelancer.getVerificationStatus().name());
            ps.setString(8, freelancer.getStatus().name().toLowerCase());

            if (freelancer.getIdTask() != null) {
                ps.setInt(9, freelancer.getIdTask());
            } else {
                ps.setNull(9, java.sql.Types.INTEGER);
            }

            ps.setInt(10, id);
            ps.executeUpdate();
            System.out.println("Freelancer updated successfully!");
        } catch (SQLException e) {
            System.out.println("Error updating freelancer: " + e.getMessage());
            e.printStackTrace();
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
        if (baseUser == null)
            return null;

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
                f.setProfilePicturePath(baseUser.getProfilePicturePath());
                f.setActivated(baseUser.isActivated());

                f.setIdFreelancer(rs.getInt("idFreelancer"));
                f.setPricePerHour(rs.getDouble("pricePerHour"));
                f.setAmount(rs.getDouble("amount"));
                f.setRating(rs.getDouble("rating"));

                String skills = rs.getString("skills");
                f.setSkills(skills != null ? skills.split(",") : new String[0]);

                f.setBio(rs.getString("bio"));
                f.setStudentCardPath(rs.getString("studentCardPath"));
                f.setCvPath(rs.getString("cvPath"));
                f.setVerificationStatus(VerifStatus.valueOf(rs.getString("verificationStatus")));
                f.setStatus(Status.valueOf(rs.getString("status").toUpperCase()));

                int idTask = rs.getInt("idTask");
                f.setIdTask(rs.wasNull() ? null : idTask);

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
                if (f != null)
                    freelancers.add(f);
            }
        }
        return freelancers;
    }
}