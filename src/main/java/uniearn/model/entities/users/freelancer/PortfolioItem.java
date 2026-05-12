package uniearn.model.entities.users.freelancer;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;

public class PortfolioItem {
    private int idItem;
    private String title;
    private String description;
    private String[] technologies;
    private String[] imagesUrl;
    private String projectUrl;
    private String githubUrl;
    private Timestamp created_At;
    private int idPortfolio;

    public PortfolioItem() {}

    public PortfolioItem(String title, String description, String[] technologies, String[] imagesUrl, String projectUrl, String githubUrl, Date created_At, int idPortfolio) {
        this.title = title;
        this.description = description;
        this.technologies = technologies;
        this.imagesUrl = new String[0];
        this.projectUrl = projectUrl;
        this.githubUrl = githubUrl;
        this.created_At = new Timestamp(System.currentTimeMillis());
        this.idPortfolio = idPortfolio;
    }

    public int getIdItem() {
        return idItem;
    }

    public void setIdItem(int idItem) {
        this.idItem = idItem;
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

    public Timestamp getCreated_At() {
        return created_At;
    }

    public void setCreated_At(Timestamp created_At) {
        this.created_At = created_At;
    }

    public int getIdPortfolio() {
        return idPortfolio;
    }

    public void setIdPortfolio(int idPortfolio) {
        this.idPortfolio = idPortfolio;
    }

    @Override
    public String toString() {
        return "PortfolioItem{" +
                "idItem=" + idItem +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", technologies=" + Arrays.toString(technologies) +
                ", imagesUrl=" + Arrays.toString(imagesUrl) +
                ", projectUrl='" + projectUrl + '\'' +
                ", githubUrl='" + githubUrl + '\'' +
                ", created_At=" + created_At +
                ", idPortfolio=" + idPortfolio +
                '}';
    }
}
