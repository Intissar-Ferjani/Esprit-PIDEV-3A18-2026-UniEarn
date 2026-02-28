package uniearn.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import uniearn.database.MyConnection;

import java.io.IOException;

/**
 * Classe principale pour lancer l'application UniEarn
 * Cette classe initialise l'interface graphique et la connexion à la base de données
 */
public class MainApp extends Application {

    private static final String APP_TITLE = "UniEarn - Admin Contrats";
    // Changer ici pour tester différentes interfaces:
    // /contracts/admin_contracts.fxml, /contracts/client_contracts.fxml, /contracts/freelancer_contracts.fxml
    private static final String MAIN_FXML = "/contracts/admin_contracts.fxml";
    private static final int WINDOW_WIDTH = 1400;
    private static final int WINDOW_HEIGHT = 900;

    /**
     * Point d'entrée principal de l'application
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initialise et affiche le menu de sélection d'interface
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

            // Créer le menu de sélection
            VBox menu = createSelectionMenu(primaryStage);
            Scene scene = new Scene(menu, 600, 500);

            // Configurer la fenêtre principale
            primaryStage.setTitle("UniEarn - Sélection Interface");
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();

            System.out.println("✅ Menu de sélection affiché!");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du démarrage de l'application: " + e.getMessage());
            e.printStackTrace();
            showErrorAndExit("Erreur Critique",
                "Une erreur critique s'est produite lors du démarrage de l'application.");
        }
    }

    /**
     * Crée le menu de sélection des interfaces
     */
    private VBox createSelectionMenu(Stage stage) {
        VBox menu = new VBox(20);
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(40));
        menu.setStyle("-fx-background-color: linear-gradient(to bottom right, #667eea 0%, #764ba2 100%);");

        Label title = new Label("🎯 UniEarn - Sélection Interface");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Choisissez l'interface à tester :");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #F5F5F5;");

        // Boutons pour chaque interface
        Button btnAdmin = createInterfaceButton("👨‍💼 Interface ADMIN",
            "Gérer tous les contrats et templates",
            "/contracts/admin_contracts.fxml", stage);

        Button btnClient = createInterfaceButton("👤 Interface CLIENT",
            "Créer et gérer vos contrats",
            "/contracts/client_contracts.fxml", stage);

        Button btnFreelancer = createInterfaceButton("💼 Interface FREELANCER",
            "Consulter et signer vos contrats",
            "/contracts/freelancer_contracts.fxml", stage);

        Button btnTemplates = createInterfaceButton("📋 Gestion TEMPLATES",
            "Créer et gérer les modèles de contrats",
            "/contracts/contract_template_admin.fxml", stage);

        menu.getChildren().addAll(title, subtitle, btnAdmin, btnClient, btnFreelancer, btnTemplates);

        return menu;
    }

    /**
     * Crée un bouton stylisé pour le menu
     */
    private Button createInterfaceButton(String title, String description, String fxmlPath, Stage stage) {
        VBox buttonContent = new VBox(5);
        buttonContent.setAlignment(Pos.CENTER);

        Label btnTitle = new Label(title);
        btnTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label btnDesc = new Label(description);
        btnDesc.setStyle("-fx-font-size: 12px; -fx-opacity: 0.8;");

        buttonContent.getChildren().addAll(btnTitle, btnDesc);

        Button button = new Button();
        button.setGraphic(buttonContent);
        button.setPrefWidth(450);
        button.setPrefHeight(70);
        button.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                       "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2); " +
                       "-fx-cursor: hand;");

        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: #f0f0f0; -fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 3); " +
            "-fx-cursor: hand; -fx-scale-x: 1.02; -fx-scale-y: 1.02;"));

        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: white; -fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2); " +
            "-fx-cursor: hand;"));

        button.setOnAction(e -> loadInterface(fxmlPath, stage));

        return button;
    }

    /**
     * Charge une interface spécifique
     */
    private void loadInterface(String fxmlPath, Stage stage) {
        try {
            System.out.println("🔄 Chargement de l'interface: " + fxmlPath);
            Parent root = loadFXML(fxmlPath);

            if (root != null) {
                Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
                stage.setScene(scene);
                stage.setTitle(APP_TITLE + " - " + getInterfaceName(fxmlPath));
                stage.centerOnScreen();
                System.out.println("✅ Interface chargée avec succès!");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement de l'interface: " + e.getMessage());
            e.printStackTrace();
            showErrorAndExit("Erreur de Chargement",
                "Impossible de charger l'interface: " + fxmlPath);
        }
    }

    /**
     * Retourne le nom de l'interface à partir du chemin FXML
     */
    private String getInterfaceName(String fxmlPath) {
        if (fxmlPath.contains("admin")) return "Admin";
        if (fxmlPath.contains("client")) return "Client";
        if (fxmlPath.contains("freelancer")) return "Freelancer";
        if (fxmlPath.contains("template")) return "Templates";
        return "Interface";
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

