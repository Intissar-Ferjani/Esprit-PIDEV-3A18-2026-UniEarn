package uniearn.model.entities.projet;

public class Project {
    private int idproject;
    private String title;
    private String description;
    private double budget;
    private String status;
    private int clientid;
    private int freelancerid;
    private String freelancerName;

    public Project() {};

    public Project(String title, String description, double budget, String status, int clientid ,int freelancerid) {
        this.title = title;
        this.description = description;
        this.budget = budget;
        this.status = status;
        this.clientid = clientid;
        this.freelancerid = freelancerid;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getClient_id() {
        return clientid;
    }

    public void setClient_id(int clientid) {this.clientid = clientid;}

    public int getFreelancerid() {return freelancerid;}

    public void setFreelancerid(int freelancerid) {
        this.freelancerid = freelancerid;
    }

    public String getFreelancerName() { return freelancerName; }
    public void setFreelancerName(String freelancerName) { this.freelancerName = freelancerName; }
}

