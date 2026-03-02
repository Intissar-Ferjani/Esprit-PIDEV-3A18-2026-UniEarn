package uniearn.services;

import uniearn.database.MyConnection;
import uniearn.model.entities.BankAccount;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les comptes bancaires des freelancers et admins
 */
public class BankAccountService {
    private Connection connection;

    public BankAccountService() {
        try {
            this.connection = MyConnection.getInstance().getCnx();
        } catch (Exception e) {
            System.err.println("Erreur de connexion à la base de données: " + e.getMessage());
        }
    }

    /**
     * Ajouter un nouveau compte bancaire
     */
    public boolean addBankAccount(BankAccount account) {
        String sql = "INSERT INTO bank_account (user_id, account_holder_name, bank_name, iban, bic, is_default) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, account.getUserId());
            stmt.setString(2, account.getAccountHolderName());
            stmt.setString(3, account.getBankName());
            stmt.setString(4, account.getIban());
            stmt.setString(5, ""); // BIC - optional, set to empty string
            stmt.setBoolean(6, account.isDefault());

            System.out.println("DEBUG: Tentative d'ajout du compte bancaire avec userID=" + account.getUserId() +
                             ", accountHolder=" + account.getAccountHolderName() +
                             ", bank=" + account.getBankName());

            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                System.out.println("DEBUG: Compte bancaire créé avec succès pour userID=" + account.getUserId());
            }
            return result;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du compte bancaire: " + e.getMessage());
            System.err.println("DEBUG: userID passé=" + account.getUserId());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupérer tous les comptes bancaires d'un utilisateur
     */
    public List<BankAccount> getBankAccountsByUser(int userID) {
        List<BankAccount> accounts = new ArrayList<>();
        String sql = "SELECT * FROM bank_account WHERE user_id = ? ORDER BY is_default DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BankAccount account = new BankAccount();
                    account.setId(rs.getInt("id"));
                    account.setUserId(rs.getInt("user_id"));
                    account.setAccountHolderName(rs.getString("account_holder_name"));
                    account.setBankName(rs.getString("bank_name"));
                    account.setIban(rs.getString("iban"));
                    account.setDefault(rs.getBoolean("is_default"));
                    account.setDateAdded(rs.getTimestamp("date_added").toLocalDateTime());
                    accounts.add(account);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des comptes bancaires: " + e.getMessage());
            e.printStackTrace();
        }
        return accounts;
    }

    /**
     * Récupérer un compte bancaire par ID
     */
    public BankAccount getBankAccountById(int idBankAccount) {
        String sql = "SELECT * FROM bank_account WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idBankAccount);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BankAccount account = new BankAccount();
                    account.setId(rs.getInt("id"));
                    account.setUserId(rs.getInt("user_id"));
                    account.setAccountHolderName(rs.getString("account_holder_name"));
                    account.setBankName(rs.getString("bank_name"));
                    account.setIban(rs.getString("iban"));
                    account.setDefault(rs.getBoolean("is_default"));
                    account.setDateAdded(rs.getTimestamp("date_added").toLocalDateTime());
                    return account;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du compte bancaire: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Mettre à jour un compte bancaire
     */
    public boolean updateBankAccount(BankAccount account) {
        String sql = "UPDATE bank_account SET account_holder_name=?, bank_name=?, iban=?, is_default=? " +
                     "WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, account.getAccountHolderName());
            stmt.setString(2, account.getBankName());
            stmt.setString(3, account.getIban());
            stmt.setBoolean(4, account.isDefault());
            stmt.setInt(5, account.getId());

            boolean result = stmt.executeUpdate() > 0;
            if (result && account.isDefault()) {
                // Désactiver tous les autres comptes par défaut pour cet utilisateur
                setDefaultAccount(account.getUserId(), account.getId());
            }
            return result;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du compte bancaire: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Supprimer un compte bancaire
     */
    public boolean deleteBankAccount(int idBankAccount) {
        String sql = "DELETE FROM bank_account WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idBankAccount);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du compte bancaire: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Définir le compte par défaut pour un utilisateur
     */
    private void setDefaultAccount(int userID, int idBankAccount) {
        // D'abord, désactiver tous les comptes par défaut
        String sql1 = "UPDATE bank_account SET is_default = FALSE WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql1)) {
            stmt.setInt(1, userID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la désactivation des comptes par défaut: " + e.getMessage());
        }

        // Ensuite, activer le compte sélectionné
        String sql2 = "UPDATE bank_account SET is_default = TRUE WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql2)) {
            stmt.setInt(1, idBankAccount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'activation du compte par défaut: " + e.getMessage());
        }
    }

    /**
     * Récupérer le compte par défaut d'un utilisateur
     */
    public BankAccount getDefaultBankAccount(int userID) {
        String sql = "SELECT * FROM bank_account WHERE user_id = ? AND is_default = TRUE LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BankAccount account = new BankAccount();
                    account.setId(rs.getInt("id"));
                    account.setUserId(rs.getInt("user_id"));
                    account.setAccountHolderName(rs.getString("account_holder_name"));
                    account.setBankName(rs.getString("bank_name"));
                    account.setIban(rs.getString("iban"));
                    account.setDefault(rs.getBoolean("is_default"));
                    account.setDateAdded(rs.getTimestamp("date_added").toLocalDateTime());
                    return account;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du compte par défaut: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}

