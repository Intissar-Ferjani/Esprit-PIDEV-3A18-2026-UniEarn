package uniearn.services.projet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import uniearn.database.MyConnection;
import uniearn.interfaces.Projet.IProject;
import uniearn.model.entities.projet.Project;

public class ProjectService implements IProject<Project> {

    private final Connection cn = MyConnection.getInstance().getCnx();

    public ProjectService() {
        ensureFreelancerColumn();
    }

    private void ensureFreelancerColumn() {
        if (cn == null)
            return;
        try {
            // 1. Check for freelancerID (the correct one)
            ResultSet rs = cn.getMetaData().getColumns(null, null, "project", "freelancerID");
            boolean hasNew = rs.next();

            // 2. Check for freelancerIDD (the problematic legacy one)
            ResultSet rsOld = cn.getMetaData().getColumns(null, null, "project", "freelancerIDD");
            boolean hasOld = rsOld.next();

            // Safety: if we can't find it with exact case, try case-insensitive or common variants
            if (!hasOld) {
                ResultSet rsOldAlt = cn.getMetaData().getColumns(null, null, "project", "FREELANCERIDD");
                hasOld = rsOldAlt.next();
            }

            if (hasOld && !hasNew) {
                // If only freelancerIDD exists, rename it to freelancerID
                try {
                    cn.createStatement().executeUpdate("ALTER TABLE project DROP FOREIGN KEY project_ibfk_2");
                } catch (Exception e) {
                }
                cn.createStatement().executeUpdate(
                        "ALTER TABLE project CHANGE COLUMN freelancerIDD freelancerID INT DEFAULT NULL");
                System.out.println("Renamed 'freelancerIDD' to 'freelancerID' in project table");
            } else if (hasOld && hasNew) {
                // If both exist, migrate any data and drop the old one to avoid errors
                // CRITICAL: First make it nullable to avoid "no default value" errors if drop fails
                try {
                    cn.createStatement().executeUpdate("ALTER TABLE project MODIFY COLUMN freelancerIDD INT DEFAULT NULL");
                } catch (Exception e) {
                    System.out.println("Failed to modify freelancerIDD to NULL: " + e.getMessage());
                }
                try {
                    cn.createStatement().executeUpdate(
                            "UPDATE project SET freelancerID = freelancerIDD WHERE freelancerID IS NULL AND freelancerIDD IS NOT NULL");
                } catch (Exception e) {
                }
                try {
                    cn.createStatement().executeUpdate("ALTER TABLE project DROP COLUMN freelancerIDD");
                    System.out.println("Migrated data and dropped redundant 'freelancerIDD' from project table");
                } catch (Exception e) {
                    System.out.println("Could not drop freelancerIDD, but made it nullable. " + e.getMessage());
                }
            } else if (!hasNew) {
                // If neither exists, just add freelancerID
                cn.createStatement().executeUpdate(
                        "ALTER TABLE project ADD COLUMN freelancerID INT DEFAULT NULL");
                System.out.println("Added 'freelancerID' column to project table");
            } else {
                // freelancerID exists, just ensure it's NULLABLE
                cn.createStatement().executeUpdate(
                        "ALTER TABLE project MODIFY COLUMN freelancerID INT DEFAULT NULL");
            }
        } catch (SQLException e) {
            System.out.println("freelancerID column sync error: " + e.getMessage());
        }
    }

    @Override
    public void addProject(Project Project) throws SQLException {
        String request = "INSERT INTO project (title, description, budget, status, ClientID, freelancerID) VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = cn.prepareStatement(request);

        pst.setString(1, Project.getTitle());
        pst.setString(2, Project.getDescription());
        pst.setDouble(3, Project.getBudget());
        pst.setInt(4, Project.getStatus());
        pst.setInt(5, Project.getClient_id());
        pst.setInt(6, Project.getFreelancerid());

        pst.executeUpdate();
    }

    @Override
    public void updateProject(int id, Project Project) {
        String request = "UPDATE project SET title=?, description=?, budget=?, status=?, ClientID=?, freelancerID=? WHERE idproject=?";
        try {

            PreparedStatement pst = cn.prepareStatement(request);

            pst.setString(1, Project.getTitle());
            pst.setString(2, Project.getDescription());
            pst.setDouble(3, Project.getBudget());
            pst.setInt(4, Project.getStatus());
            pst.setInt(5, Project.getClient_id());
            pst.setInt(6, Project.getFreelancerid());
            pst.setInt(7, id);

            int rows = pst.executeUpdate();

            System.out.println(rows + " row(s) updated.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    @Override
    public void deleteProject(int id) {
        String request = "DELETE FROM project WHERE idproject=?";
        try {

            PreparedStatement pst = cn.prepareStatement(request);

            pst.setInt(1, id);

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Project deleted successfully!");
            } else {
                System.out.println("No project found with the given ID.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    @Override
    public Project getProjectById(int id) {
        String request = "SELECT * FROM project WHERE idproject=?";
        try {
            PreparedStatement pst = cn.prepareStatement(request);
            pst.setInt(1, id);

            var rs = pst.executeQuery();

            if (rs.next()) {
                Project project = new Project();
                project.setIdproject(rs.getInt("idproject"));
                project.setTitle(rs.getString("title"));
                project.setDescription(rs.getString("description"));
                project.setBudget(rs.getDouble("budget"));
                project.setStatus(rs.getInt("status"));
                project.setClient_id(rs.getInt("ClientID"));
                project.setFreelancerid(rs.getInt("freelancerID"));
                return project;
            } else {
                System.out.println("No project found with the given ID.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public List<Project> getAllProjects() {
        List<Project> projects = new ArrayList<>();
        String request = "SELECT p.*, u.name AS freelancer_name FROM project p "
                + "LEFT JOIN freelancer f ON p.freelancerID = f.idFreelancer "
                + "LEFT JOIN user u ON f.idUser = u.idUser";
        try (PreparedStatement pst = cn.prepareStatement(request);
                ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Project project = new Project();
                project.setIdproject(rs.getInt("idproject"));
                project.setTitle(rs.getString("title"));
                project.setDescription(rs.getString("description"));
                project.setBudget(rs.getDouble("budget"));
                project.setStatus(rs.getInt("status"));
                project.setClient_id(rs.getInt("ClientID"));
                project.setFreelancerid(rs.getInt("freelancerID"));
                String fname = rs.getString("freelancer_name");
                project.setFreelancerName(fname != null ? fname : "Unknown");
                projects.add(project);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return projects;
    }

    public List<Project> getProjectsByClientId(int clientId) {
        List<Project> projects = new ArrayList<>();
        String request = "SELECT p.*, u.name AS freelancer_name FROM project p "
                + "LEFT JOIN freelancer f ON p.freelancerID = f.idFreelancer "
                + "LEFT JOIN user u ON f.idUser = u.idUser "
                + "WHERE p.ClientID=?";
        try {
            PreparedStatement pst = cn.prepareStatement(request);
            pst.setInt(1, clientId);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Project project = new Project();
                project.setIdproject(rs.getInt("idproject"));
                project.setTitle(rs.getString("title"));
                project.setDescription(rs.getString("description"));
                project.setBudget(rs.getDouble("budget"));
                project.setStatus(rs.getInt("status"));
                project.setClient_id(rs.getInt("ClientID"));
                project.setFreelancerid(rs.getInt("freelancerID"));
                String fname = rs.getString("freelancer_name");
                project.setFreelancerName(fname != null ? fname : "Unknown");
                projects.add(project);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return projects;
    }

    /** Get the name of the freelancer assigned to a project */
    public String getFreelancerNameById(int freelancerId) {
        if (cn == null)
            return "Unknown";
        String sql = "SELECT u.name FROM freelancer f JOIN user u ON f.idUser = u.idUser WHERE f.idFreelancer = ?";
        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, freelancerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getString("name");
        } catch (SQLException e) {
            System.out.println("Error getting freelancer name: " + e.getMessage());
        }
        return "Unknown";
    }
}
