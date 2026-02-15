package uniearn.services;

import uniearn.database.MyConnection;
import uniearn.interfaces.IProject;
import uniearn.model.Project;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PorjectService implements IProject <Project> {

    private final Connection cn = MyConnection.getInstance().getCnx();


    @Override
    public void addProject(Project Project) throws SQLException {
        String request = "INSERT INTO project (title, description, budget, status, ClientID) VALUES (?, ?, ?, ?, ?)";

        PreparedStatement pst= cn.prepareStatement(request);

        pst.setString(1, Project.getTitle());
        pst.setString(2, Project.getDescription());
        pst.setDouble(3, Project.getBudget());
        pst.setInt(4, Project.getStatus());
        pst.setInt(5, Project.getClient_id());


        pst.executeUpdate();
    }

    @Override
    public void updateProject(int id, Project Project) {
        String request = "UPDATE project SET title=?, description=?, budget=?, status=?, ClientID=? WHERE idproject=?";
        try {

            PreparedStatement pst = cn.prepareStatement(request);

            pst.setString(1, Project.getTitle());
            pst.setString(2, Project.getDescription());
            pst.setDouble(3, Project.getBudget());
            pst.setInt(4, Project.getStatus());
            pst.setInt(5, Project.getClient_id());
            pst.setInt(6, id);

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
        String request = "SELECT * FROM project";
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
                projects.add(project);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return projects;
    }
}
