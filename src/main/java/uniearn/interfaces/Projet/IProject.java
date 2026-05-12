package uniearn.interfaces.Projet;

import java.sql.SQLException;
import java.util.List;

public interface IProject<P> {
    void addProject(P Project) throws SQLException;

    void updateProject(int id, P Project);

    void deleteProject(int id);

    P getProjectById(int id);

    List<P> getAllProjects();
}