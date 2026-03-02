package uniearn.model.entities;

import java.time.LocalDateTime;

/**
 * Classe représentant un compte bancaire associé à un utilisateur
 */
public class BankAccount {
    private int id;
    private int userId;
    private String accountHolderName;
    private String iban;
    private String bic;
    private String bankName;
    private boolean isDefault;
    private LocalDateTime dateAdded;
    private LocalDateTime dateModified;

    // Constructeurs
    public BankAccount() {
    }

    public BankAccount(int userId, String accountHolderName, String iban, String bic, String bankName) {
        this.userId = userId;
        this.accountHolderName = accountHolderName;
        this.iban = iban;
        this.bic = bic;
        this.bankName = bankName;
        this.isDefault = false;
        this.dateAdded = LocalDateTime.now();
    }

    // Getters et Setters
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

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public LocalDateTime getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(LocalDateTime dateAdded) {
        this.dateAdded = dateAdded;
    }

    public LocalDateTime getDateModified() {
        return dateModified;
    }

    public void setDateModified(LocalDateTime dateModified) {
        this.dateModified = dateModified;
    }

    @Override
    public String toString() {
        return "BankAccount{" +
                "id=" + id +
                ", userId=" + userId +
                ", accountHolderName='" + accountHolderName + '\'' +
                ", iban='" + iban + '\'' +
                ", bic='" + bic + '\'' +
                ", bankName='" + bankName + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }
}

