package uniearn.services;

import uniearn.database.MyConnection;
import uniearn.interfaces.IApplication;
import uniearn.model.entities.Application;
import uniearn.model.enums.ApplicationStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ApplicationService implements IApplication {

    private final Connection connection;

    public ApplicationService() {
        this.connection = MyConnection.getInstance().getCnx();
    }

    // ================== CRUD ==================
    @Override
    public void create(Application application) throws SQLException {
        if (!application.isValid())
            throw new SQLException("Invalid application data");

        if (alreadyApplied(application.getFreelancerId(), application.getProjectId()))
            throw new SQLException("Already applied");

        String query = """
                INSERT INTO application
                (freelancer_id, project_id, status, cover_letter, proposed_budget, estimated_duration, applied_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, application.getFreelancerId());
        stmt.setInt(2, application.getProjectId());
        stmt.setString(3, application.getStatus().name());
        stmt.setString(4, application.getCoverLetter());
        stmt.setDouble(5, application.getProposedBudget());
        stmt.setInt(6, application.getEstimatedDuration());
        stmt.setTimestamp(7, Timestamp.valueOf(application.getAppliedAt()));
        stmt.setTimestamp(8, Timestamp.valueOf(application.getUpdatedAt()));
        stmt.executeUpdate();

        ResultSet keys = stmt.getGeneratedKeys();
        if (keys.next())
            application.setIdApplication(keys.getInt(1));
    }

    @Override
    public Application read(int idApplication) throws SQLException {
        String query = "SELECT * FROM application WHERE idApplication=?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, idApplication);
        ResultSet rs = stmt.executeQuery();
        if (rs.next())
            return extractApplication(rs);
        return null;
    }

    @Override
    public List<Application> readAll() throws SQLException {
        List<Application> list = new ArrayList<>();
        String query = "SELECT * FROM application ORDER BY applied_at DESC";
        PreparedStatement stmt = connection.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();
        while (rs.next())
            list.add(extractApplication(rs));
        return list;
    }

    @Override
    public void update(Application application) throws SQLException {
        String query = """
                UPDATE application SET
                freelancer_id=?, project_id=?, status=?, cover_letter=?, proposed_budget=?, estimated_duration=?, updated_at=?
                WHERE idApplication=?
                """;
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, application.getFreelancerId());
        stmt.setInt(2, application.getProjectId());
        stmt.setString(3, application.getStatus().name());
        stmt.setString(4, application.getCoverLetter());
        stmt.setDouble(5, application.getProposedBudget());
        stmt.setInt(6, application.getEstimatedDuration());
        stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
        stmt.setInt(8, application.getIdApplication());
        stmt.executeUpdate();
    }

    @Override
    public void delete(int idApplication) throws SQLException {
        String query = "DELETE FROM application WHERE idApplication=?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, idApplication);
        stmt.executeUpdate();
    }

    // ================== Specific Queries ==================
    @Override
    public List<Application> getApplicationsByProject(int projectId) throws SQLException {
        List<Application> list = new ArrayList<>();
        String query = "SELECT * FROM application WHERE project_id=? ORDER BY applied_at DESC";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, projectId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next())
            list.add(extractApplication(rs));
        return list;
    }

    @Override
    public List<Application> getApplicationsByFreelancer(int freelancerId) throws SQLException {
        List<Application> list = new ArrayList<>();
        String query = "SELECT * FROM application WHERE freelancer_id=? ORDER BY applied_at DESC";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, freelancerId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next())
            list.add(extractApplication(rs));
        return list;
    }

    @Override
    public List<Application> getApplicationsByStatus(ApplicationStatus status) throws SQLException {
        List<Application> list = new ArrayList<>();
        String query = "SELECT * FROM application WHERE status=? ORDER BY applied_at DESC";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, status.name());
        ResultSet rs = stmt.executeQuery();
        while (rs.next())
            list.add(extractApplication(rs));
        return list;
    }

    // ================== Business Logic ==================
    @Override
    public void updateStatus(int idApplication, ApplicationStatus newStatus) throws SQLException {
        String query = "UPDATE application SET status=?, updated_at=? WHERE idApplication=?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, newStatus.name());
        stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
        stmt.setInt(3, idApplication);
        stmt.executeUpdate();
    }

    @Override
    public boolean alreadyApplied(int freelancerId, int projectId) throws SQLException {
        String query = "SELECT COUNT(*) FROM application WHERE freelancer_id=? AND project_id=?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, freelancerId);
        stmt.setInt(2, projectId);
        ResultSet rs = stmt.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }

    // ================== Advanced Features ==================
    @Override
    public List<Application> searchApplications(String searchTerm) throws SQLException {
        List<Application> list = new ArrayList<>();
        String query = "SELECT * FROM application WHERE cover_letter LIKE ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, "%" + searchTerm + "%");
        ResultSet rs = stmt.executeQuery();
        while (rs.next())
            list.add(extractApplication(rs));
        return list;
    }

    @Override
    public int countApplicationsByProject(int projectId) throws SQLException {
        String query = "SELECT COUNT(*) FROM application WHERE project_id=?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, projectId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next())
            return rs.getInt(1);
        return 0;
    }

    @Override
    public int countApplicationsByStatus(int projectId, ApplicationStatus status) throws SQLException {
        String query = "SELECT COUNT(*) FROM application WHERE project_id=? AND status=?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, projectId);
        stmt.setString(2, status.name());
        ResultSet rs = stmt.executeQuery();
        if (rs.next())
            return rs.getInt(1);
        return 0;
    }

    // ================== SAFE WRAPPERS ==================
    @Override
    public void applyToProject(Application application) throws SQLException {
        create(application);
    }

    @Override
    public boolean acceptApplication(int applicationId) {
        try {
            Application app = read(applicationId);
            if (app == null)
                return false;

            updateStatus(applicationId, ApplicationStatus.ACCEPTED);

            List<Application> otherApps = getApplicationsByProject(app.getProjectId());
            for (Application other : otherApps) {
                if (other.getIdApplication() != applicationId) {
                    updateStatus(other.getIdApplication(), ApplicationStatus.REJECTED);
                }
            }
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean rejectApplication(int applicationId) {
        try {
            updateStatus(applicationId, ApplicationStatus.REJECTED);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean withdrawApplication(int applicationId, int freelancerId) {
        try {
            Application app = read(applicationId);
            if (app == null || app.getFreelancerId() != freelancerId)
                return false;
            delete(applicationId);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public IApplication.ApplicationStatistics getProjectStatistics(int projectId) {
        try {
            int total = countApplicationsByProject(projectId);
            int accepted = countApplicationsByStatus(projectId, ApplicationStatus.ACCEPTED);
            int rejected = countApplicationsByStatus(projectId, ApplicationStatus.REJECTED);
            int pending = countApplicationsByStatus(projectId, ApplicationStatus.PENDING);

            return new IApplication.ApplicationStatistics(total, accepted, rejected, pending);
        } catch (SQLException e) {
            return new IApplication.ApplicationStatistics(0, 0, 0, 0);
        }
    }

    public int getClientIdByProject(int projectId) {
        String query = "SELECT ClientID FROM project WHERE idProject = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, projectId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("ClientID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ================== Helper ==================
    private Application extractApplication(ResultSet rs) throws SQLException {
        return new Application(
                rs.getInt("idApplication"),
                rs.getInt("freelancer_id"),
                rs.getInt("project_id"),
                ApplicationStatus.valueOf(rs.getString("status")),
                rs.getString("cover_letter"),
                rs.getDouble("proposed_budget"),
                rs.getInt("estimated_duration"),
                rs.getTimestamp("applied_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime());
    }
}
