package uniearn.services;

import uniearn.database.MyConnection;
import uniearn.model.entities.BankAccount;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour la gestion des comptes bancaires (table bank_account).
 */
public class BankAccountService {

    private Connection connection;

    public BankAccountService() {
        try {
            this.connection = MyConnection.getInstance().getCnx();
        } catch (Exception e) {
            System.err.println("Erreur de connexion BankAccountService: " + e.getMessage());
        }
    }

    /**
     * Ajouter un compte bancaire.
     */
    public boolean addBankAccount(BankAccount account) {
        // Si marqué par défaut, réinitialiser les autres comptes de l'utilisateur
        if (account.isDefault()) {
            clearDefault(account.getUserId());
        }
        String sql = "INSERT INTO bank_account (user_id, account_holder_name, iban, bic, bank_name, is_default) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, account.getUserId());
            stmt.setString(2, account.getAccountHolderName());
            stmt.setString(3, account.getIban());
            stmt.setString(4, account.getBic());
            stmt.setString(5, account.getBankName());
            stmt.setBoolean(6, account.isDefault());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du compte bancaire: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupérer tous les comptes bancaires d'un utilisateur.
     */
    public List<BankAccount> getBankAccountsByUser(int userId) {
        List<BankAccount> accounts = new ArrayList<>();
        String sql = "SELECT * FROM bank_account WHERE user_id = ? ORDER BY is_default DESC, date_added DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des comptes: " + e.getMessage());
            e.printStackTrace();
        }
        return accounts;
    }

    /**
     * Récupérer un compte bancaire par son identifiant.
     */
    public BankAccount getBankAccountById(int id) {
        String sql = "SELECT * FROM bank_account WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du compte: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Mettre à jour un compte bancaire.
     */
    public boolean updateBankAccount(BankAccount account) {
        if (account.isDefault()) {
            clearDefault(account.getUserId());
        }
        String sql = "UPDATE bank_account SET account_holder_name = ?, iban = ?, bic = ?, bank_name = ?, " +
                     "is_default = ?, date_modified = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, account.getAccountHolderName());
            stmt.setString(2, account.getIban());
            stmt.setString(3, account.getBic());
            stmt.setString(4, account.getBankName());
            stmt.setBoolean(5, account.isDefault());
            stmt.setInt(6, account.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du compte: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Supprimer un compte bancaire.
     */
    public boolean deleteBankAccount(int id) {
        String sql = "DELETE FROM bank_account WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du compte: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Définir un compte comme compte par défaut pour un utilisateur.
     */
    public boolean setDefaultAccount(int accountId, int userId) {
        clearDefault(userId);
        String sql = "UPDATE bank_account SET is_default = 1, date_modified = CURRENT_TIMESTAMP WHERE id = ? AND user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la définition du compte par défaut: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupérer tous les comptes bancaires (pour les administrateurs).
     */
    public List<BankAccount> getAllBankAccounts() {
        List<BankAccount> accounts = new ArrayList<>();
        String sql = "SELECT * FROM bank_account ORDER BY user_id, is_default DESC, date_added DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                accounts.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de tous les comptes: " + e.getMessage());
            e.printStackTrace();
        }
        return accounts;
    }

    /**
     * Réinitialiser le flag is_default pour tous les comptes d'un utilisateur.
     * @return true si réussi, false si une erreur SQL est survenue
     */
    private boolean clearDefault(int userId) {
        String sql = "UPDATE bank_account SET is_default = 0 WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la réinitialisation du compte par défaut: " + e.getMessage());
            return false;
        }
    }

    private BankAccount mapResultSet(ResultSet rs) throws SQLException {
        BankAccount account = new BankAccount();
        account.setId(rs.getInt("id"));
        account.setUserId(rs.getInt("user_id"));
        account.setAccountHolderName(rs.getString("account_holder_name"));
        account.setIban(rs.getString("iban"));
        account.setBic(rs.getString("bic"));
        account.setBankName(rs.getString("bank_name"));
        account.setDefault(rs.getBoolean("is_default"));
        account.setDateAdded(rs.getTimestamp("date_added"));
        account.setDateModified(rs.getTimestamp("date_modified"));
        return account;
    }
}
