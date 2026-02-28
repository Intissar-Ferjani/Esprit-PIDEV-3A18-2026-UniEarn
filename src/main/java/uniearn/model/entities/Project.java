package uniearn.model.entities;

import java.io.Serializable;
import java.sql.Timestamp;

public class Project implements Serializable {
    private int idProject;
    private String title;
    private String description;
    private double budget;
    private int status;
    private int clientID;
    private int freelancerID;

    // Constructors
    public Project() {
    }

    public Project(String title, String description, double budget, int clientID) {
        this.title = title;
        this.description = description;
        this.budget = budget;
        this.clientID = clientID;
        this.status = 0;
    }

    public Project(int idProject, String title, String description, double budget,
                   int status, int clientID, int freelancerID) {
        this.idProject = idProject;
        this.title = title;
        this.description = description;
        this.budget = budget;
        this.status = status;
        this.clientID = clientID;
        this.freelancerID = freelancerID;
    }

    // Getters
    public int getIdProject() {
        return idProject;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public double getBudget() {
        return budget;
    }

    public int getStatus() {
        return status;
    }

    public int getClientID() {
        return clientID;
    }

    public int getFreelancerID() {
        return freelancerID;
    }

    // Setters
    public void setIdProject(int idProject) {
        this.idProject = idProject;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public void setFreelancerID(int freelancerID) {
        this.freelancerID = freelancerID;
    }

    @Override
    public String toString() {
        return "Project{" +
                "idProject=" + idProject +
                ", title='" + title + '\'' +
                ", budget=" + budget +
                ", status=" + status +
                ", clientID=" + clientID +
                '}';
    }
}
