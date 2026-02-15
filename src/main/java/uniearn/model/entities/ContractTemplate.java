package uniearn.model.entities;

import java.sql.Timestamp;

/**
 * Modèle pour les templates de contrats
 */
public class ContractTemplate {
    private int idTemplate;
    private String templateName;
    private String description;
    private String templateContent;
    private Timestamp createdDate;
    private Timestamp updatedDate;

    // Constructeurs
    public ContractTemplate() {}

    public ContractTemplate(String templateName, String description, String templateContent) {
        this.templateName = templateName;
        this.description = description;
        this.templateContent = templateContent;
    }

    public ContractTemplate(int idTemplate, String templateName, String description,
                           String templateContent, Timestamp createdDate, Timestamp updatedDate) {
        this.idTemplate = idTemplate;
        this.templateName = templateName;
        this.description = description;
        this.templateContent = templateContent;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    // Getters et Setters
    public int getIdTemplate() {
        return idTemplate;
    }

    public void setIdTemplate(int idTemplate) {
        this.idTemplate = idTemplate;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTemplateContent() {
        return templateContent;
    }

    public void setTemplateContent(String templateContent) {
        this.templateContent = templateContent;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public Timestamp getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Timestamp updatedDate) {
        this.updatedDate = updatedDate;
    }

    @Override
    public String toString() {
        return "ContractTemplate{" +
                "idTemplate=" + idTemplate +
                ", templateName='" + templateName + '\'' +
                ", description='" + description + '\'' +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                '}';
    }
}

