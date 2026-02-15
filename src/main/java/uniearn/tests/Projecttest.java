package uniearn.tests;

import uniearn.model.Project;
import uniearn.services.PorjectService;

import java.security.Provider;
import java.sql.SQLException;

public class Projecttest {

    public static void main(String[] args) {

        try {

            PorjectService service = new PorjectService();

            //test add project
            Project p = new Project("Project1", "Description of project 1", 1000.0, 1, 1);
            service.addProject(p);
            System.out.println("Project added successfully!");

            //test delete project
            service.deleteProject(2);
            System.out.println("Project with ID 2 deleted successfully!");

            //test to show all projects
            System.out.println("All projects:");
            service.getAllProjects().forEach(project -> {
                System.out.println("ID: " + project.getIdproject() + ", Title: " + project.getTitle() + ", Description: " + project.getDescription() + ", Budget: " + project.getBudget() + ", Status: " + project.getStatus() + ", Client ID: " + project.getClient_id());
            });

            //test update project
            Project p1 = service.getProjectById(4);
            p1.setTitle("Updated Project Title");
            p1.setDescription("Updated Project Description");
            p1.setBudget(629);
            p1.setStatus(1);
            p1.setClient_id(1);
            p1.setIdproject(4);
            service.updateProject(4, p1);


             //test to show project by id
            System.out.println("get project by id:");
            Project project = service.getProjectById(4);
            System.out.println("ID: " + project.getIdproject() + ", Title: " + project.getTitle() + ", Description: " + project.getDescription() + ", Budget: " + project.getBudget() + ", Status: " + project.getStatus() + ", Client ID: " + project.getClient_id());


        }catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }



    }
}