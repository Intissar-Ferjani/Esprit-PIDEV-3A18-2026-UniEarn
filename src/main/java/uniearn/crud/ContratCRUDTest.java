package uniearn.crud;

import uniearn.model.entities.Contrat;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Test simple pour le CRUD des contrats
 */
public class ContratCRUDTest {

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║    TEST CRUD - GESTION DES CONTRATS             ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");

        ContratCRUD crud = new ContratCRUD();

        try {
            // TEST 1: CREATE
            System.out.println("1️⃣  TEST CREATE - Créer un nouveau contrat");
            Contrat contrat1 = new Contrat();
            contrat1.setStartDate(Timestamp.valueOf(LocalDateTime.now()));
            contrat1.setEndDate(Timestamp.valueOf(LocalDateTime.now().plusMonths(1)));
            contrat1.setStatus(0);
            contrat1.setAmount(50000.0);
            contrat1.setProjectID(1);
            contrat1.setClientID(1);
            contrat1.setPaymentID(1);

            if (crud.create(contrat1)) {
                System.out.println("✅ Contrat créé avec succès\n");
            } else {
                System.out.println("❌ Erreur lors de la création\n");
            }

            // TEST 2: READ ALL
            System.out.println("2️⃣  TEST READ ALL - Lister tous les contrats");
            List<Contrat> tous = crud.readAll();
            System.out.println("✅ " + tous.size() + " contrat(s) trouvé(s)\n");
            tous.forEach(crud::displayContrat);

            // TEST 3: READ BY ID
            if (!tous.isEmpty()) {
                System.out.println("3️⃣  TEST READ BY ID");
                Contrat found = crud.readById(tous.get(0).getIdContract());
                if (found != null) {
                    System.out.println("✅ Contrat trouvé:");
                    crud.displayContrat(found);
                } else {
                    System.out.println("❌ Contrat non trouvé\n");
                }

                // TEST 4: UPDATE
                System.out.println("4️⃣  TEST UPDATE - Modifier un contrat");
                found.setAmount(75000.0);
                if (crud.update(found)) {
                    System.out.println("✅ Contrat mis à jour (montant: 75000 DA)\n");
                } else {
                    System.out.println("❌ Erreur lors de la mise à jour\n");
                }

                // TEST 5: SIGN BY CLIENT
                System.out.println("5️⃣  TEST SIGN BY CLIENT");
                if (crud.signByClient(found.getIdContract())) {
                    System.out.println("✅ Contrat signé par le client (statut: Signé Client)\n");
                } else {
                    System.out.println("❌ Erreur lors de la signature\n");
                }

                // TEST 6: SIGN BY FREELANCER
                System.out.println("6️⃣  TEST SIGN BY FREELANCER");
                if (crud.signByFreelancer(found.getIdContract())) {
                    System.out.println("✅ Contrat signé par le freelancer (statut: Complété)\n");
                } else {
                    System.out.println("❌ Erreur lors de la signature\n");
                }

                // TEST 7: READ BY CLIENT
                System.out.println("7️⃣  TEST READ BY CLIENT");
                List<Contrat> byClient = crud.readByClient(1);
                System.out.println("✅ " + byClient.size() + " contrat(s) pour le client 1\n");

                // TEST 8: READ BY PROJECT
                System.out.println("8️⃣  TEST READ BY PROJECT");
                List<Contrat> byProject = crud.readByProject(1);
                System.out.println("✅ " + byProject.size() + " contrat(s) pour le projet 1\n");

                // TEST 9: EXISTS
                System.out.println("9️⃣  TEST EXISTS");
                if (crud.exists(found.getIdContract())) {
                    System.out.println("✅ Le contrat " + found.getIdContract() + " existe\n");
                } else {
                    System.out.println("❌ Le contrat n'existe pas\n");
                }

                // TEST 10: COUNT
                System.out.println("🔟 TEST COUNT");
                System.out.println("✅ Nombre total de contrats: " + crud.count() + "\n");

                // TEST 11: STATS
                System.out.println("1️⃣1️⃣  TEST STATISTIQUES");
                int[] stats = crud.getStatsByStatus();
                System.out.println("✅ Statistiques:");
                System.out.println("   - Brouillons: " + stats[0]);
                System.out.println("   - Signés Client: " + stats[1]);
                System.out.println("   - Signés Freelancer: " + stats[2]);
                System.out.println("   - Complétés: " + stats[3] + "\n");

                // TEST 12: DELETE (Optionnel - à décommenter)
                // System.out.println("1️⃣2️⃣  TEST DELETE");
                // if (crud.delete(found.getIdContract())) {
                //     System.out.println("✅ Contrat supprimé avec succès\n");
                // } else {
                //     System.out.println("❌ Erreur lors de la suppression\n");
                // }
            }

            System.out.println("╔════════════════════════════════════════════════╗");
            System.out.println("║         ✅ TOUS LES TESTS TERMINES              ║");
            System.out.println("╚════════════════════════════════════════════════╝\n");

        } catch (Exception e) {
            System.err.println("❌ Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

