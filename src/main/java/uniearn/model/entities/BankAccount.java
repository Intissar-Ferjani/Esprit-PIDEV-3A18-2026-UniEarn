package uniearn.model.entities;

import java.sql.Timestamp;

/**
 * Entité représentant un compte bancaire dans le système UniEarn.
 * Correspond à la table bank_account en base de données.
 */
public class BankAccount {
    private int id;
    private int userId;
    private String accountHolderName;
    private String iban;
    private String bic;
    private String bankName;
    private boolean isDefault;
    private Timestamp dateAdded;
    private Timestamp dateModified;

    public BankAccount() {}

    public BankAccount(int userId, String accountHolderName, String iban, String bic, String bankName, boolean isDefault) {
        this.userId = userId;
        this.accountHolderName = accountHolderName;
        this.iban = iban;
        this.bic = bic;
        this.bankName = bankName;
        this.isDefault = isDefault;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Timestamp getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Timestamp dateAdded) {
        this.dateAdded = dateAdded;
    }

    public Timestamp getDateModified() {
        return dateModified;
    }

    public void setDateModified(Timestamp dateModified) {
        this.dateModified = dateModified;
    }

    /**
     * Retourne l'IBAN masqué pour un affichage sécurisé.
     * Affiche seulement les 4 derniers caractères.
     */
    public String getMaskedIban() {
        if (iban == null || iban.length() < 4) {
            return "****";
        }
        return "****" + iban.substring(iban.length() - 4);
    }

    @Override
    public String toString() {
        return "BankAccount{" +
                "id=" + id +
                ", userId=" + userId +
                ", accountHolderName='" + accountHolderName + '\'' +
                ", iban='" + getMaskedIban() + '\'' +
                ", bankName='" + bankName + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }
}
