package uniearn.example;

import uniearn.crud.ContratCRUD;
import uniearn.model.entities.Contrat;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

/**
 * Exemple d'application interactive pour tester le CRUD des contrats
 */
public class ContratApp {

    private ContratCRUD crud;
    private Scanner scanner;

    public ContratApp() {
        this.crud = new ContratCRUD();
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        ContratApp app = new ContratApp();
        app.menu();
    }

    private void menu() {
        boolean continuer = true;

        while (continuer) {
            System.out.println("\n╔════════════════════════════════════════════════╗");
            System.out.println("║      GESTION DES CONTRATS - MENU PRINCIPAL      ║");
            System.out.println("╠════════════════════════════════════════════════╣");
            System.out.println("║ 1. Créer un contrat                             ║");
            System.out.println("║ 2. Afficher tous les contrats                   ║");
            System.out.println("║ 3. Afficher un contrat (par ID)                 ║");
            System.out.println("║ 4. Modifier un contrat                          ║");
            System.out.println("║ 5. Signer par le client                         ║");
            System.out.println("║ 6. Signer par le freelancer                     ║");
            System.out.println("║ 7. Supprimer un contrat                         ║");
            System.out.println("║ 8. Afficher les contrats par client             ║");
            System.out.println("║ 9. Afficher les contrats par projet             ║");
            System.out.println("║ 10. Statistiques                                ║");
            System.out.println("║ 0. Quitter                                      ║");
            System.out.println("╚════════════════════════════════════════════════╝");
            System.out.print("Choisir une option: ");

            int choix = -1;
            try {
                choix = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("❌ Entrée invalide");
                continue;
            }

            switch (choix) {
                case 1 -> creerContrat();
                case 2 -> afficherTous();
                case 3 -> afficherParId();
                case 4 -> modifierContrat();
                case 5 -> signerClient();
                case 6 -> signerFreelancer();
                case 7 -> supprimerContrat();
                case 8 -> afficherParClient();
                case 9 -> afficherParProjet();
                case 10 -> afficherStatistiques();
                case 0 -> {
                    continuer = false;
                    System.out.println("👋 Au revoir!");
                }
                default -> System.out.println("❌ Option non valide");
            }
        }

        scanner.close();
    }

    private void creerContrat() {
        System.out.println("\n--- Créer un Contrat ---");
        try {
            System.out.print("Montant (DA): ");
            double montant = Double.parseDouble(scanner.nextLine());

            System.out.print("Client ID: ");
            int clientID = Integer.parseInt(scanner.nextLine());

            System.out.print("Projet ID: ");
            int projectID = Integer.parseInt(scanner.nextLine());

            System.out.print("Paiement ID: ");
            int paymentID = Integer.parseInt(scanner.nextLine());

            Contrat contrat = new Contrat();
            contrat.setStartDate(Timestamp.valueOf(LocalDateTime.now()));
            contrat.setEndDate(Timestamp.valueOf(LocalDateTime.now().plusMonths(1)));
            contrat.setStatus(0);
            contrat.setAmount(montant);
            contrat.setClientID(clientID);
            contrat.setProjectID(projectID);
            contrat.setPaymentID(paymentID);

            if (crud.create(contrat)) {
                System.out.println("✅ Contrat créé avec succès");
            } else {
                System.out.println("❌ Erreur lors de la création");
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
        }
    }

    private void afficherTous() {
        System.out.println("\n--- Tous les Contrats ---");
        List<Contrat> contrats = crud.readAll();

        if (contrats.isEmpty()) {
            System.out.println("Aucun contrat trouvé");
        } else {
            System.out.println("┌─────┬──────────┬──────────┬────────────┐");
            System.out.println("│ ID  │  Client  │ Montant  │   Statut   │");
            System.out.println("├─────┼──────────┼──────────┼────────────┤");
            for (Contrat c : contrats) {
                System.out.printf("│ %-3d │ %-8d │ %-8.0f │ %-10s │\n",
                    c.getIdContract(), c.getClientID(), c.getAmount(), c.getStatusString());
            }
            System.out.println("└─────┴──────────┴──────────┴────────────┘");
        }
    }

    private void afficherParId() {
        System.out.print("\nID du contrat: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            Contrat contrat = crud.readById(id);

            if (contrat != null) {
                crud.displayContrat(contrat);
            } else {
                System.out.println("❌ Contrat non trouvé");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ ID invalide");
        }
    }

    private void modifierContrat() {
        System.out.print("\nID du contrat à modifier: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            Contrat contrat = crud.readById(id);

            if (contrat == null) {
                System.out.println("❌ Contrat non trouvé");
                return;
            }

            System.out.print("Nouveau montant (" + contrat.getAmount() + "): ");
            String montantStr = scanner.nextLine();
            if (!montantStr.isEmpty()) {
                contrat.setAmount(Double.parseDouble(montantStr));
            }

            if (crud.update(contrat)) {
                System.out.println("✅ Contrat mis à jour");
            } else {
                System.out.println("❌ Erreur lors de la mise à jour");
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
        }
    }

    private void signerClient() {
        System.out.print("\nID du contrat à signer (client): ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            if (crud.signByClient(id)) {
                System.out.println("✅ Contrat signé par le client");
            } else {
                System.out.println("❌ Erreur");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ ID invalide");
        }
    }

    private void signerFreelancer() {
        System.out.print("\nID du contrat à signer (freelancer): ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            if (crud.signByFreelancer(id)) {
                System.out.println("✅ Contrat signé par le freelancer");
            } else {
                System.out.println("❌ Erreur");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ ID invalide");
        }
    }

    private void supprimerContrat() {
        System.out.print("\nID du contrat à supprimer: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());

            System.out.print("Êtes-vous sûr? (oui/non): ");
            if (scanner.nextLine().equalsIgnoreCase("oui")) {
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
    }

    private void afficherParClient() {
        System.out.print("\nID du client: ");
        try {
            int clientID = Integer.parseInt(scanner.nextLine());
            List<Contrat> contrats = crud.readByClient(clientID);

            System.out.println("\nContrats du client " + clientID + ": " + contrats.size());
            contrats.forEach(crud::displayContrat);
        } catch (NumberFormatException e) {
            System.out.println("❌ ID invalide");
        }
    }

    private void afficherParProjet() {
        System.out.print("\nID du projet: ");
        try {
            int projectID = Integer.parseInt(scanner.nextLine());
            List<Contrat> contrats = crud.readByProject(projectID);

            System.out.println("\nContrats du projet " + projectID + ": " + contrats.size());
            contrats.forEach(crud::displayContrat);
        } catch (NumberFormatException e) {
            System.out.println("❌ ID invalide");
        }
    }

    private void afficherStatistiques() {
        System.out.println("\n--- Statistiques ---");
        int[] stats = crud.getStatsByStatus();
        int total = crud.count();

        System.out.println("Total de contrats: " + total);
        System.out.println("  📝 Brouillons:        " + stats[0]);
        System.out.println("  ✍️  Signés Client:     " + stats[1]);
        System.out.println("  ✍️  Signés Freelancer: " + stats[2]);
        System.out.println("  ✅ Complétés:         " + stats[3]);
    }
}

