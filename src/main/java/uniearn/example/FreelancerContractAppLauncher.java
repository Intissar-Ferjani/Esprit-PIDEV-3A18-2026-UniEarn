package uniearn.example;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Launcher pour lancer FreelancerContractApp avec les bonnes options JavaFX
 * Cette classe contourne le problème des options VM manquantes
 */
public class FreelancerContractAppLauncher {

    public static void main(String[] args) {
        // Vérifier que JavaFX est correctement configuré
        try {
            // Forcer le chargement des modules JavaFX
            System.setProperty("javafx.preloader", "");

            // Lancer l'application
            FreelancerContractApp.launch(FreelancerContractApp.class, args);
        } catch (Exception e) {
            System.err.println("Erreur de lancement JavaFX:");
            e.printStackTrace();
        }
    }
}

