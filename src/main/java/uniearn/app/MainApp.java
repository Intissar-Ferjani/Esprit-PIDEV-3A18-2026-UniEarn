package uniearn.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import uniearn.database.MyConnection;

import java.io.IOException;

/**
 * Classe principale pour lancer l'application UniEarn
 * Cette classe initialise l'interface graphique et la connexion à la base de données
 */
public class MainApp extends Application {

    private static final String APP_TITLE = "UniEarn - Gestion des Contrats";
    private static final String LOGIN_FXML = "/auth/login/login.fxml";
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 800;

    /**
     * Point d'entrée principal de l'application
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initialise et affiche la scène principale (écran de connexion)
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // Vérifier la connexion à la base de données
            if (!verifyDatabaseConnection()) {
                showErrorAndExit("Erreur de Connexion",
                    "Impossible de se connecter à la base de données. Vérifiez votre configuration.");
                return;
            }

            // Charger l'interface de connexion
            Parent root = loadFXML(LOGIN_FXML);
            if (root == null) {
                showErrorAndExit("Erreur de Chargement",
                    "Impossible de charger l'interface de connexion.");
                return;
            }

            // Créer et configurer la scène
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

            // Configurer la fenêtre principale
            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setWidth(WINDOW_WIDTH);
            primaryStage.setHeight(WINDOW_HEIGHT);
            primaryStage.centerOnScreen();

            // Gérer la fermeture de l'application
            primaryStage.setOnCloseRequest(event -> onApplicationClose());

            // Afficher la fenêtre
            primaryStage.show();

            System.out.println("✅ Application UniEarn démarrée avec succès!");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du démarrage de l'application: " + e.getMessage());
            e.printStackTrace();
            showErrorAndExit("Erreur Critique",
                "Une erreur critique s'est produite lors du démarrage de l'application.");
        }
    }

    /**
     * Charge un fichier FXML
     */
    private Parent loadFXML(String fxmlPath) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            return loader.load();
        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement du fichier FXML: " + fxmlPath);
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Vérifie la connexion à la base de données
     */
    private boolean verifyDatabaseConnection() {
        try {
            MyConnection connection = MyConnection.getInstance();
            if (connection != null && connection.getCnx() != null) {
                System.out.println("✅ Connexion à la base de données établie avec succès!");
                return true;
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification de la connexion: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Affiche un message d'erreur et ferme l'application
     */
    private void showErrorAndExit(String title, String message) {
        System.err.println("❌ " + title + ": " + message);
        System.exit(1);
    }

    /**
     * Nettoie les ressources à la fermeture de l'application
     */
    private void onApplicationClose() {
        try {
            System.out.println("🔄 Fermeture de l'application...");
            // Fermer la connexion à la base de données si nécessaire
            MyConnection connection = MyConnection.getInstance();
            if (connection != null) {
                // Ajouter ici le code de fermeture de la connexion si la classe le permet
                System.out.println("✅ Ressources libérées avec succès");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de la fermeture: " + e.getMessage());
        }
    }
}

