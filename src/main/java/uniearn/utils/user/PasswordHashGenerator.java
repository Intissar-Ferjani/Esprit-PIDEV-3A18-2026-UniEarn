package uniearn.utils.user;

import java.util.Scanner;

/**
 * Utilitaire pour générer des mots de passe hachés
 * Utile pour réinitialiser manuellement un mot de passe dans la base de données
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("     GÉNÉRATEUR DE HASH DE MOT DE PASSE (BCrypt)");
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println();

        Scanner scanner = new Scanner(System.in);

        System.out.print("Entrez le mot de passe en clair à hacher: ");
        String plainPassword = scanner.nextLine();

        if (plainPassword.isEmpty()) {
            System.out.println("❌ Erreur: Le mot de passe ne peut pas être vide");
            scanner.close();
            return;
        }

        System.out.println("\n⏳ Génération du hash BCrypt...");

        try {
            // Générer le hash
            String hashedPassword = PasswordUtil.hashPassword(plainPassword);

            System.out.println("\n✅ Hash généré avec succès !");
            System.out.println("═══════════════════════════════════════════════════");
            System.out.println("Mot de passe en clair : " + plainPassword);
            System.out.println("Hash BCrypt           : " + hashedPassword);
            System.out.println("Longueur du hash      : " + hashedPassword.length() + " caractères");
            System.out.println("═══════════════════════════════════════════════════");

            // Vérification
            System.out.println("\n🔍 Vérification du hash...");
            boolean isValid = PasswordUtil.verifyPassword(plainPassword, hashedPassword);

            if (isValid) {
                System.out.println("✅ Hash valide ! La vérification fonctionne correctement.");
            } else {
                System.out.println("❌ Erreur: Le hash n'est pas valide !");
            }

            // Requête SQL
            System.out.println("\n📋 Requête SQL pour mettre à jour la base de données :");
            System.out.println("═══════════════════════════════════════════════════");
            System.out.println("UPDATE user");
            System.out.println("SET password = '" + hashedPassword + "'");
            System.out.println("WHERE email = 'votre-email@example.com';");
            System.out.println("═══════════════════════════════════════════════════");

            System.out.println("\n⚠️  ATTENTION :");
            System.out.println("   - Remplacez 'votre-email@example.com' par l'email réel");
            System.out.println("   - Exécutez cette requête dans votre base de données");
            System.out.println("   - Le hash est unique à chaque génération (c'est normal)");

        } catch (Exception e) {
            System.out.println("\n❌ Erreur lors de la génération du hash:");
            System.out.println("   " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }

        System.out.println("\n═══════════════════════════════════════════════════");
        System.out.println("              Terminé avec succès !");
        System.out.println("═══════════════════════════════════════════════════");
    }
}

