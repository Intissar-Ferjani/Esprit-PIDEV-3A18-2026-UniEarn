package uniearn.services.contracts;

import uniearn.database.MyConnection;
import uniearn.model.entities.contracts.ContractType;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service pour gérer les types de contrats
 */
public class ContractTypeService {
    private Connection connection;

    public ContractTypeService() {
        try {
            this.connection = MyConnection.getInstance().getCnx();
        } catch (Exception e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
        }
    }

    /**
     * Récupérer tous les types de contrats
     */
    public List<ContractType> getAllContractTypes() {
        List<ContractType> types = new ArrayList<>();
        String sql = "SELECT * FROM contract_type ORDER BY metier, typeName";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ContractType type = mapResultSetToContractType(rs);
                types.add(type);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des types: " + e.getMessage());
        }
        return types;
    }

    /**
     * Récupérer les types de contrats par métier
     */
    public List<ContractType> getContractTypesByMetier(String metier) {
        List<ContractType> types = new ArrayList<>();
        String sql = "SELECT * FROM contract_type WHERE metier = ? ORDER BY typeName";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, metier);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ContractType type = mapResultSetToContractType(rs);
                    types.add(type);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des types: " + e.getMessage());
        }
        return types;
    }

    /**
     * Récupérer tous les métiers disponibles
     */
    public Set<String> getAllMetiers() {
        Set<String> metiers = new HashSet<>();
        String sql = "SELECT DISTINCT metier FROM contract_type ORDER BY metier";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                metiers.add(rs.getString("metier"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des métiers: " + e.getMessage());
        }
        return metiers;
    }

    /**
     * Récupérer un type de contrat par ID
     */
    public ContractType getContractTypeById(int id) {
        String sql = "SELECT * FROM contract_type WHERE idContractType = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToContractType(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du type: " + e.getMessage());
        }
        return null;
    }

    /**
     * Créer un nouveau type de contrat
     */
    public boolean createContractType(ContractType type) {
        String sql = "INSERT INTO contract_type (typeName, description, metier) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, type.getTypeName());
            stmt.setString(2, type.getDescription());
            stmt.setString(3, type.getMetier());
            int result = stmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création du type: " + e.getMessage());
        }
        return false;
    }

    /**
     * Mettre à jour un type de contrat
     */
    public boolean updateContractType(ContractType type) {
        String sql = "UPDATE contract_type SET typeName = ?, description = ?, metier = ? WHERE idContractType = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, type.getTypeName());
            stmt.setString(2, type.getDescription());
            stmt.setString(3, type.getMetier());
            stmt.setInt(4, type.getIdContractType());
            int result = stmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du type: " + e.getMessage());
        }
        return false;
    }

    /**
     * Supprimer un type de contrat
     */
    public boolean deleteContractType(int id) {
        String sql = "DELETE FROM contract_type WHERE idContractType = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int result = stmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du type: " + e.getMessage());
        }
        return false;
    }

    /**
     * Mapper un ResultSet vers un objet ContractType
     */
    private ContractType mapResultSetToContractType(ResultSet rs) throws SQLException {
        ContractType type = new ContractType();
        type.setIdContractType(rs.getInt("idContractType"));
        type.setTypeName(rs.getString("typeName"));
        type.setDescription(rs.getString("description"));
        type.setMetier(rs.getString("metier"));
        type.setCreatedAt(rs.getTimestamp("createdAt"));
        type.setUpdatedAt(rs.getTimestamp("updatedAt"));
        return type;
    }
}

