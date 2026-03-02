package uniearn.services.payment;

import uniearn.database.MyConnection;
import uniearn.model.entities.BankAccount;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion des comptes bancaires
 */
public class BankAccountService {
    private Connection connection;

    public BankAccountService() {
        try {
            this.connection = MyConnection.getInstance().getCnx();
        } catch (Exception e) {
            System.err.println("❌ Erreur de connexion à la base de données: " + e.getMessage());
        }
    }

    /**
     * Ajoute un nouveau compte bancaire
     */
    public BankAccount addBankAccount(BankAccount account) {
        String sql = "INSERT INTO bank_account (user_id, account_holder_name, iban, bic, bank_name, is_default, date_added) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW())";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, account.getUserId());
            stmt.setString(2, account.getAccountHolderName());
            stmt.setString(3, account.getIban());
            stmt.setString(4, account.getBic());
            stmt.setString(5, account.getBankName());
            stmt.setBoolean(6, account.isDefault());

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    account.setId(rs.getInt(1));
                    account.setDateAdded(LocalDateTime.now());
                    System.out.println("✅ Compte bancaire ajouté avec succès: ID=" + account.getId());
                    return account;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout du compte bancaire: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Met à jour un compte bancaire
     */
    public boolean updateBankAccount(BankAccount account) {
        String sql = "UPDATE bank_account SET account_holder_name = ?, iban = ?, bic = ?, bank_name = ?, is_default = ?, date_modified = NOW() " +
                "WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, account.getAccountHolderName());
            stmt.setString(2, account.getIban());
            stmt.setString(3, account.getBic());
            stmt.setString(4, account.getBankName());
            stmt.setBoolean(5, account.isDefault());
            stmt.setInt(6, account.getId());

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Compte bancaire " + account.getId() + " mis à jour");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour du compte bancaire: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Supprime un compte bancaire
     */
    public boolean deleteBankAccount(int accountId) {
        String sql = "DELETE FROM bank_account WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, accountId);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("✅ Compte bancaire " + accountId + " supprimé");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression du compte bancaire: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Récupère tous les comptes bancaires d'un utilisateur
     */
    public List<BankAccount> getBankAccountsByUserId(int userId) {
        List<BankAccount> accounts = new ArrayList<>();
        String sql = "SELECT * FROM bank_account WHERE user_id = ? ORDER BY is_default DESC, date_added DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                accounts.add(mapResultSetToBankAccount(rs));
            }
            System.out.println("✅ Récupéré " + accounts.size() + " comptes bancaires pour l'utilisateur " + userId);
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des comptes bancaires: " + e.getMessage());
            e.printStackTrace();
        }
        return accounts;
    }

    /**
     * Récupère le compte bancaire par défaut d'un utilisateur
     */
    public BankAccount getDefaultBankAccount(int userId) {
        String sql = "SELECT * FROM bank_account WHERE user_id = ? AND is_default = true LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToBankAccount(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération du compte bancaire par défaut: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupère un compte bancaire par ID
     */
    public BankAccount getBankAccountById(int accountId) {
        String sql = "SELECT * FROM bank_account WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToBankAccount(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération du compte bancaire: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Valide un IBAN
     */
    public static boolean isValidIBAN(String iban) {
        if (iban == null || iban.length() < 15 || iban.length() > 34) {
            return false;
        }
        // IBAN doit commencer par 2 lettres de pays et 2 chiffres
        return iban.matches("^[A-Z]{2}[0-9]{2}[A-Z0-9]*$");
    }

    /**
     * Mappe un ResultSet à un objet BankAccount
     */
    private BankAccount mapResultSetToBankAccount(ResultSet rs) throws SQLException {
        BankAccount account = new BankAccount();
        account.setId(rs.getInt("id"));
        account.setUserId(rs.getInt("user_id"));
        account.setAccountHolderName(rs.getString("account_holder_name"));
        account.setIban(rs.getString("iban"));
        account.setBic(rs.getString("bic"));
        account.setBankName(rs.getString("bank_name"));
        account.setDefault(rs.getBoolean("is_default"));

        Timestamp dateAdded = rs.getTimestamp("date_added");
        if (dateAdded != null) {
            account.setDateAdded(dateAdded.toLocalDateTime());
        }

        Timestamp dateModified = rs.getTimestamp("date_modified");
        if (dateModified != null) {
            account.setDateModified(dateModified.toLocalDateTime());
        }

        return account;
    }
}

