package uniearn.model.entities.contracts;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Modèle pour les types de contrats
 */
public class ContractType implements Serializable {
    private int idContractType;
    private String typeName;
    private String description;
    private String metier;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Constructeurs
    public ContractType() {}

    public ContractType(String typeName, String description, String metier) {
        this.typeName = typeName;
        this.description = description;
        this.metier = metier;
    }

    public ContractType(int idContractType, String typeName, String description,
                       String metier, Timestamp createdAt, Timestamp updatedAt) {
        this.idContractType = idContractType;
        this.typeName = typeName;
        this.description = description;
        this.metier = metier;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public int getIdContractType() {
        return idContractType;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getDescription() {
        return description;
    }

    public String getMetier() {
        return metier;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setIdContractType(int idContractType) {
        this.idContractType = idContractType;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMetier(String metier) {
        this.metier = metier;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "ContractType{" +
                "idContractType=" + idContractType +
                ", typeName='" + typeName + '\'' +
                ", metier='" + metier + '\'' +
                '}';
    }
}

