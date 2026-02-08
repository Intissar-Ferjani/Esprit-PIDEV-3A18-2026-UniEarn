package uniearn.model.entities;

import uniearn.interfaces.IPortfolioItems;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PortfolioItem {
    private int id;
    private String title;
    private String description;
    private String[] technologies;
    private String[] imagesUrl;
    private String projectUrl;
    private String githubUrl;
    private Date createdAt;
    private Portfolio portfolio;

    public PortfolioItem() {}

    public PortfolioItem(int id, String title, String description, String[] technologies, String[] imagesUrl, String projectUrl, String githubUrl, Date createdAt, Portfolio portfolio) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.technologies = technologies;
        this.imagesUrl = imagesUrl;
        this.projectUrl = projectUrl;
        this.githubUrl = githubUrl;
        this.createdAt = createdAt;
        this.portfolio = portfolio;
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

    public String[] getTechnologies() {
        return technologies;
    }

    public void setTechnologies(String[] technologies) {
        this.technologies = technologies;
    }

    public String[] getImagesUrl() {
        return imagesUrl;
    }

    public void setImagesUrl(String[] imagesUrl) {
        this.imagesUrl = imagesUrl;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public void setProjectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    @Override
    public String toString() {
        return "PortfolioItem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", technologies=" + Arrays.toString(technologies) +
                ", imagesUrl=" + Arrays.toString(imagesUrl) +
                ", projectUrl='" + projectUrl + '\'' +
                ", githubUrl='" + githubUrl + '\'' +
                ", createdAt=" + createdAt +
                ", portfolio=" + portfolio +
                '}';
    }

}
