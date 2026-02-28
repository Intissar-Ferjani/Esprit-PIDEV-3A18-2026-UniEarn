package uniearn.example;

import uniearn.crud.ContratCRUD;
import uniearn.model.entities.contracts.Contrat;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

/**
 * Application interactive pour tester le CRUD des contrats
 * Simple et sans Spring Boot
 */
public class ContratTestApp {

    private ContratCRUD crud;
    private Scanner scanner;

    public ContratTestApp() {
        this.crud = new ContratCRUD();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║   GESTION DES CONTRATS - APPLICATION TEST   ║");
        System.out.println("╚════════════════════════════════════════════╝\n");

        boolean running = true;
        while (running) {
            afficherMenu();
            try {
                String choix = scanner.nextLine().trim();
                running = traiterChoix(choix);
            } catch (Exception e) {
                System.out.println("❌ Erreur: " + e.getMessage());
            }
        }

        System.out.println("\n✅ Application fermée. Au revoir !");
        scanner.close();
    }

    private void afficherMenu() {
        System.out.println("\n┌─ MENU PRINCIPAL ─────────────────────┐");
        System.out.println("│ 1. Créer un contrat                  │");
        System.out.println("│ 2. Afficher tous les contrats        │");
        System.out.println("│ 3. Rechercher un contrat par ID      │");
        System.out.println("│ 4. Rechercher par client             │");
        System.out.println("│ 5. Rechercher par projet             │");
        System.out.println("│ 6. Mettre à jour un contrat          │");
        System.out.println("│ 7. Signer par le client              │");
        System.out.println("│ 8. Signer par le freelancer          │");
        System.out.println("│ 9. Supprimer un contrat              │");
        System.out.println("│ 10. Statistiques                     │");
        System.out.println("│ 0. Quitter                           │");
        System.out.println("└──────────────────────────────────────┘");
        System.out.print("Votre choix: ");
    }

    private boolean traiterChoix(String choix) {
        return switch (choix) {
            case "1" -> creerContrat();
            case "2" -> afficherTousContrats();
            case "3" -> rechercherParID();
            case "4" -> rechercherParClient();
            case "5" -> rechercherParProjet();
            case "6" -> mettreAJourContrat();
            case "7" -> signerClient();
            case "8" -> signerFreelancer();
            case "9" -> supprimerContrat();
            case "10" -> afficherStatistiques();
            case "0" -> false;
            default -> {
                System.out.println("❌ Choix invalide");
                yield true;
            }
        };
    }

    private boolean creerContrat() {
        System.out.println("\n┌─ CRÉER UN CONTRAT ─┐");
        try {
            System.out.print("ID Client: ");
            int clientID = Integer.parseInt(scanner.nextLine());

            System.out.print("ID Projet: ");
            int projectID = Integer.parseInt(scanner.nextLine());

            System.out.print("Montant (DA): ");
            double amount = Double.parseDouble(scanner.nextLine());

            System.out.print("ID Paiement (optionnel, appuyez sur Entrée): ");
            String paymentStr = scanner.nextLine();
            int paymentID = paymentStr.isEmpty() ? 0 : Integer.parseInt(paymentStr);

            System.out.print("Durée en mois: ");
            int mois = Integer.parseInt(scanner.nextLine());

            Contrat contrat = new Contrat();
            contrat.setClientID(clientID);
            contrat.setProjectID(projectID);
            contrat.setAmount(amount);
            contrat.setPaymentID(paymentID);
            contrat.setStartDate(Timestamp.valueOf(LocalDateTime.now()));
            contrat.setEndDate(Timestamp.valueOf(LocalDateTime.now().plusMonths(mois)));
            contrat.setStatus(0); // Brouillon

            if (crud.create(contrat)) {
                System.out.println("✅ Contrat créé avec succès !");
            } else {
                System.out.println("❌ Erreur lors de la création");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Nombre invalide");
        }
        return true;
    }

    private boolean afficherTousContrats() {
        System.out.println("\n┌─ TOUS LES CONTRATS ─┐");
        List<Contrat> contrats = crud.readAll();

        if (contrats.isEmpty()) {
            System.out.println("❌ Aucun contrat trouvé");
        } else {
            System.out.println("✓ " + contrats.size() + " contrat(s) trouvé(s)\n");
            for (Contrat c : contrats) {
                afficherContratBref(c);
            }
        }
        return true;
    }

    private boolean rechercherParID() {
        System.out.println("\n┌─ RECHERCHER PAR ID ─┐");
        try {
            System.out.print("ID du contrat: ");
            int id = Integer.parseInt(scanner.nextLine());

            Contrat contrat = crud.readById(id);
            if (contrat != null) {
                System.out.println();
                crud.displayContrat(contrat);
            } else {
                System.out.println("❌ Contrat non trouvé");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ ID invalide");
        }
        return true;
    }

    private boolean rechercherParClient() {
        System.out.println("\n┌─ RECHERCHER PAR CLIENT ─┐");
        try {
            System.out.print("ID du client: ");
            int clientID = Integer.parseInt(scanner.nextLine());

            List<Contrat> contrats = crud.readByClient(clientID);
            if (contrats.isEmpty()) {
                System.out.println("❌ Aucun contrat pour ce client");
            } else {
                System.out.println("✓ " + contrats.size() + " contrat(s) trouvé(s)\n");
                for (Contrat c : contrats) {
                    afficherContratBref(c);
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ ID invalide");
        }
        return true;
    }

    private boolean rechercherParProjet() {
        System.out.println("\n┌─ RECHERCHER PAR PROJET ─┐");
        try {
            System.out.print("ID du projet: ");
            int projectID = Integer.parseInt(scanner.nextLine());

            List<Contrat> contrats = crud.readByProject(projectID);
            if (contrats.isEmpty()) {
                System.out.println("❌ Aucun contrat pour ce projet");
            } else {
                System.out.println("✓ " + contrats.size() + " contrat(s) trouvé(s)\n");
                for (Contrat c : contrats) {
                    afficherContratBref(c);
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ ID invalide");
        }
        return true;
    }

    private boolean mettreAJourContrat() {
        System.out.println("\n┌─ METTRE À JOUR ─┐");
        try {
            System.out.print("ID du contrat: ");
            int id = Integer.parseInt(scanner.nextLine());

            Contrat contrat = crud.readById(id);
            if (contrat == null) {
                System.out.println("❌ Contrat non trouvé");
                return true;
            }

            System.out.println("\nValeurs actuelles:");
            System.out.println("- Montant: " + contrat.getAmount());
            System.out.println("- Client: " + contrat.getClientID());
            System.out.println("- Projet: " + contrat.getProjectID());

            System.out.print("\nNouveau montant (ou Entrée pour garder): ");
            String montantStr = scanner.nextLine();
            if (!montantStr.isEmpty()) {
                contrat.setAmount(Double.parseDouble(montantStr));
            }

            if (crud.update(contrat)) {
                System.out.println("✅ Contrat mis à jour");
            } else {
                System.out.println("❌ Erreur lors de la mise à jour");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Valeur invalide");
        }
        return true;
    }

    private boolean signerClient() {
        System.out.println("\n┌─ SIGNER PAR LE CLIENT ─┐");
        try {
            System.out.print("ID du contrat: ");
            int id = Integer.parseInt(scanner.nextLine());

            if (crud.signByClient(id)) {
                System.out.println("✅ Contrat signé par le client");
            } else {
                System.out.println("❌ Erreur");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ ID invalide");
        }
        return true;
    }

    private boolean signerFreelancer() {
        System.out.println("\n┌─ SIGNER PAR LE FREELANCER ─┐");
        try {
            System.out.print("ID du contrat: ");
            int id = Integer.parseInt(scanner.nextLine());

            if (crud.signByFreelancer(id)) {
                System.out.println("✅ Contrat signé par le freelancer");
            } else {
                System.out.println("❌ Erreur");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ ID invalide");
        }
        return true;
    }

    private boolean supprimerContrat() {
        System.out.println("\n┌─ SUPPRIMER ─┐");
        try {
            System.out.print("ID du contrat à supprimer: ");
            int id = Integer.parseInt(scanner.nextLine());

            System.out.print("Êtes-vous sûr? (o/n): ");
            if (scanner.nextLine().equalsIgnoreCase("o")) {
                if (crud.delete(id)) {
                    System.out.println("✅ Contrat supprimé");
                } else {
                    System.out.println("❌ Erreur");
                }
            } else {
                System.out.println("Suppression annulée");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ ID invalide");
        }
        return true;
    }

    private boolean afficherStatistiques() {
        System.out.println("\n┌─ STATISTIQUES ─┐");
        int[] stats = crud.getStatsByStatus();
        System.out.println("Total: " + crud.count() + " contrat(s)");
        System.out.println("  • Brouillon:           " + stats[0]);
        System.out.println("  • Signé Client:        " + stats[1]);
        System.out.println("  • Signé Freelancer:    " + stats[2]);
        System.out.println("  • Complété:            " + stats[3]);
        return true;
    }

    private void afficherContratBref(Contrat c) {
        System.out.printf("ID: %d | Client: %d | Montant: %.2f DA | Statut: %-20s%n",
                c.getIdContract(), c.getClientID(), c.getAmount(), c.getStatusString());
    }

    public static void main(String[] args) {
        new ContratTestApp().start();
    }
}

