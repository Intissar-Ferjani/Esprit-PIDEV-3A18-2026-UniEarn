package uniearn.tests;

import uniearn.model.entities.Project;
import uniearn.services.ProjectService;

public class Projecttest {

    public static void main(String[] args) {



            ProjectService service = new ProjectService();

            //test add project/*
            //Project p = new Project("esprit", "Validation", 1000.0, 1, 1);
            //service.addProject(p);
            //System.out.println("Project added successfully!");

            //test delete project
            service.deleteProject(25);
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






    }
}