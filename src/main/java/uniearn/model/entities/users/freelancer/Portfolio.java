package uniearn.model.entities.users.freelancer;

import java.sql.Timestamp;
import java.util.Date;

public class Portfolio {
    private int idPortfolio;
    private String title;
    private String description;
    private Timestamp created_At;
    private int freelancerId;

    public Portfolio() {}

    public Portfolio(String title, String description, Date created_At, int freelancerId) {
        this.title = title;
        this.description = description;
        this.created_At = new Timestamp(System.currentTimeMillis());
        this.freelancerId = freelancerId;
    }

    public int getIdPortfolio() {
        return idPortfolio;
    }

    public void setIdPortfolio(int idPortfolio) {
        this.idPortfolio = idPortfolio;
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

    public Timestamp getCreated_At() {
        return created_At;
    }

    public void setCreated_At(Timestamp created_At) {
        this.created_At = created_At;
    }

    public int getFreelancerId() {
        return freelancerId;
    }

    public void setFreelancerId(int freelancerId) {
        this.freelancerId = freelancerId;
    }

    @Override
    public String toString() {
        return "Portfolio{" +
                "idPortfolio=" + idPortfolio +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", created_At=" + created_At +
                ", freelancerId=" + freelancerId +
                '}';
    }
}
