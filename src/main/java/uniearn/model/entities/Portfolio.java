package uniearn.model.entities;

import uniearn.interfaces.IPortfolio;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Portfolio {
    private int id;
    private String title;
    private String description;
    private Date createdAt;
    private Freelancer freelancer;
    private PortfolioItem[] items;

    public Portfolio() {}

    public Portfolio(int id, String title, String description, Date createdAt, Freelancer freelancer, PortfolioItem[] items) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.freelancer = freelancer;
        this.items = items;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Freelancer getFreelancer() {
        return freelancer;
    }

    public void setFreelancer(Freelancer freelancer) {
        this.freelancer = freelancer;
    }

    public PortfolioItem[] getItems() {
        return items;
    }

    public void setItems(PortfolioItem[] items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Portfolio{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", freelancer=" + freelancer +
                ", items=" + Arrays.toString(items) +
                '}';
    }

}
