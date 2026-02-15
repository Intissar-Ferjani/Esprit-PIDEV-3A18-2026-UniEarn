package uniearn.services;

import uniearn.database.MyConnection;
import uniearn.model.entities.ContractTemplate;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour la gestion des templates de contrats (Admin)
 */
public class ContractTemplateService {
    private Connection connection;

    public ContractTemplateService() {
        try {
            this.connection = MyConnection.getInstance().getCnx();
        } catch (Exception e) {
            System.err.println("Erreur de connexion à la base de données: " + e.getMessage());
        }
    }

    /**
     * Créer un nouveau template
     */
    public boolean createTemplate(ContractTemplate template) {
        String sql = "INSERT INTO contract_template (templateName, description, templateContent) " +
                     "VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, template.getTemplateName());
            stmt.setString(2, template.getDescription());
            stmt.setString(3, template.getTemplateContent());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupérer un template par ID
     */
    public ContractTemplate getTemplateById(int id) {
        String sql = "SELECT * FROM contract_template WHERE idTemplate = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTemplate(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupérer un template par nom
     */
    public ContractTemplate getTemplateByName(String name) {
        String sql = "SELECT * FROM contract_template WHERE templateName = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTemplate(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupérer tous les templates
     */
    public List<ContractTemplate> getAllTemplates() {
        List<ContractTemplate> templates = new ArrayList<>();
        String sql = "SELECT * FROM contract_template ORDER BY createdDate DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                templates.add(mapResultSetToTemplate(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return templates;
    }

    /**
     * Mettre à jour un template
     */
    public boolean updateTemplate(ContractTemplate template) {
        String sql = "UPDATE contract_template SET templateName = ?, description = ?, " +
                     "templateContent = ?, updatedDate = CURRENT_TIMESTAMP WHERE idTemplate = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, template.getTemplateName());
            stmt.setString(2, template.getDescription());
            stmt.setString(3, template.getTemplateContent());
            stmt.setInt(4, template.getIdTemplate());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Supprimer un template
     */
    public boolean deleteTemplate(int id) {
        String sql = "DELETE FROM contract_template WHERE idTemplate = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Vérifier si un template existe
     */
    public boolean templateExists(int id) {
        String sql = "SELECT COUNT(*) FROM contract_template WHERE idTemplate = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mapper un ResultSet vers un objet ContractTemplate
     */
    private ContractTemplate mapResultSetToTemplate(ResultSet rs) throws SQLException {
        ContractTemplate template = new ContractTemplate();
        template.setIdTemplate(rs.getInt("idTemplate"));
        template.setTemplateName(rs.getString("templateName"));
        template.setDescription(rs.getString("description"));
        template.setTemplateContent(rs.getString("templateContent"));
        template.setCreatedDate(rs.getTimestamp("createdDate"));
        template.setUpdatedDate(rs.getTimestamp("updatedDate"));
        return template;
    }
}

