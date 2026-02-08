package uniearn.model.entities;

import uniearn.database.MyConnection;
import uniearn.interfaces.IProject;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class Project {
    private int idproject;
    private String title;
    private String description;
    private double budget;
    private int status;
    private int client_id;

    public Project() {};

    public Project( String title, String description, double budget, int status, int client_id) {
        this.title = title;
        this.description = description;
        this.budget = budget;
        this.status = status;
        this.client_id = client_id;
    }



    public int getIdproject() {
        return idproject;
    }

    public void setIdproject(int idproject) {
        this.idproject = idproject;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getClient_id() {
        return client_id;
    }

    public void setClient_id(int client_id) {
        this.client_id = client_id;
    }
}

