package uniearn.services;

import uniearn.model.entities.contracts.Contrat;
import uniearn.services.contracts.ContratService;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Classe de test pour la gestion des contrats
 */
public class ContratServiceTest {

    public static void main(String[] args) {
        ContratService service = new ContratService();

        System.out.println("=== TEST DU SERVICE CONTRAT ===\n");

        // Vérification des IDs existants
        System.out.println("Vérification des données requises...");
        int clientID = 1; // À adapter selon ta BD
        int projectID = 1; // À adapter selon ta BD
        int paymentID = 1; // À adapter selon ta BD

        System.out.println("⚠️  IMPORTANT: Assurez-vous que clientID=" + clientID + ", projectID=" + projectID
                + ", paymentID=" + paymentID + " existent en BD!");
        System.out.println("    Sinon, modifiez ces valeurs avec les IDs réels de votre base de données.\n");

        // Test 1: Créer un contrat
        System.out.println("Test 1: Créer un contrat");
        Contrat contrat = new Contrat();
        contrat.setStartDate(Timestamp.valueOf(LocalDateTime.now()));
        contrat.setEndDate(Timestamp.valueOf(LocalDateTime.now().plusMonths(1)));
        contrat.setStatus(0); // Brouillon
        contrat.setAmount(50000.0);
        contrat.setProjectID(projectID);
        contrat.setClientID(clientID);
        contrat.setPaymentID(paymentID);

        if (service.createContrat(contrat)) {
            System.out.println("✓ Contrat créé avec succès\n");
        } else {
            System.out.println("✗ Échec de création du contrat\n");
        }

        // Test 2: Récupérer tous les contrats
        System.out.println("Test 2: Récupérer tous les contrats");
        var contrats = service.getAllContrats();
        System.out.println("✓ Nombre de contrats: " + contrats.size());
        contrats.forEach(c -> System.out.println("  - " + c.toString()));
        System.out.println();

        // Test 3: Récupérer un contrat par ID
        if (!contrats.isEmpty()) {
            System.out.println("Test 3: Récupérer un contrat par ID");
            Contrat retrieved = service.getContratById(contrats.get(0).getIdContract());
            if (retrieved != null) {
                System.out.println("✓ Contrat récupéré: " + retrieved.toString() + "\n");
            } else {
                System.out.println("✗ Contrat non trouvé\n");
            }

            // Test 4: Mettre à jour un contrat
            System.out.println("Test 4: Mettre à jour un contrat");
            retrieved.setAmount(75000.0);
            if (service.updateContrat(retrieved)) {
                System.out.println("✓ Contrat mis à jour avec succès\n");
            } else {
                System.out.println("✗ Échec de mise à jour\n");
            }

            // Test 5: Signer par le client
            System.out.println("Test 5: Signer par le client");
            if (service.signByClient(retrieved.getIdContract())) {
                System.out.println("✓ Contrat signé par le client (statut: 1)\n");
            } else {
                System.out.println("✗ Échec de signature\n");
            }

            // Test 6: Signer par le freelancer
            System.out.println("Test 6: Signer par le freelancer");
            if (service.signByFreelancer(retrieved.getIdContract())) {
                System.out.println("✓ Contrat signé par le freelancer (statut: 3)\n");
            } else {
                System.out.println("✗ Échec de signature\n");
            }

            // Test 7: Récupérer par client
            System.out.println("Test 7: Récupérer les contrats par client (ID=1)");
            var contratsByClient = service.getContratsByClient(1);
            System.out.println("✓ Nombre de contrats: " + contratsByClient.size() + "\n");

            // Test 8: Récupérer par projet
            System.out.println("Test 8: Récupérer les contrats par projet (ID=1)");
            var contratsByProject = service.getContratsByProject(1);
            System.out.println("✓ Nombre de contrats: " + contratsByProject.size() + "\n");

            // Test 9: Récupérer par statut
            System.out.println("Test 9: Récupérer les contrats par statut (3=Complété)");
            var contratsByStatus = service.getContratsByStatus(3);
            System.out.println("✓ Nombre de contrats: " + contratsByStatus.size() + "\n");

            // Test 10: Supprimer un contrat (optionnel)
            // System.out.println("Test 10: Supprimer un contrat");
            // if (service.deleteContrat(retrieved.getIdContract())) {
            // System.out.println("✓ Contrat supprimé avec succès\n");
            // } else {
            // System.out.println("✗ Échec de suppression\n");
            // }
        }

        System.out.println("=== FIN DES TESTS ===");
    }
}
