package uniearn.controller;

import uniearn.database.MyConnection;
import uniearn.interfaces.IProject;
import uniearn.model.entities.Project;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class ProjectController implements IProject <Project> {

    @Override
    public void addProject(Project Project) throws SQLException {
        String request = "INSERT INTO project (title, description, budget, status, client_id) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pst= new MyConnection().getCnx().prepareStatement(request);
        pst.setString(1, Project.getTitle());
        pst.setString(2, Project.getDescription());
        pst.setDouble(3, Project.getBudget());
        pst.setInt(4, Project.getStatus());
        pst.setInt(5, Project.getClient_id());

        pst.executeUpdate();
        System.out.println("Project added successfully!");
    }

    @Override
    public void updateProject(int id, Project Project) {
        String request = "UPDATE project SET title=?, description=?, budget=?, status=?, client_id=? WHERE idproject=?";
            try {
                PreparedStatement pst = new MyConnection().getCnx().prepareStatement(request);
                pst.setString(1, Project.getTitle());
                pst.setString(2, Project.getDescription());
                pst.setDouble(3, Project.getBudget());
                pst.setInt(4, Project.getStatus());
                pst.setInt(5, Project.getClient_id());
                pst.setInt(6, id);

                int rowsAffected = pst.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Project updated successfully!");
                } else {
                    System.out.println("No project found with the given ID.");
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

    }

    @Override
    public void deleteProject(int id) {
        String request = "DELETE FROM project WHERE idproject=?";
        try {
            PreparedStatement pst = new MyConnection().getCnx().prepareStatement(request);
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
                PreparedStatement pst = new MyConnection().getCnx().prepareStatement(request);
                pst.setInt(1, id);

                var rs = pst.executeQuery();
                if (rs.next()) {
                    Project project = new Project();
                    project.setIdproject(rs.getInt("idproject"));
                    project.setTitle(rs.getString("title"));
                    project.setDescription(rs.getString("description"));
                    project.setBudget(rs.getDouble("budget"));
                    project.setStatus(rs.getInt("status"));
                    project.setClient_id(rs.getInt("client_id"));
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
    public List getAllProjects() {

        return List.of();
    }
}
