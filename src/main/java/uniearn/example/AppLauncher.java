package uniearn.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Application Launcher - Permet de choisir quelle interface lancer
 */
public class AppLauncher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Interface de sélection
            VBox root = createLauncherUI(primaryStage);

            Scene scene = new Scene(root, 600, 450);
            primaryStage.setTitle("UniEarn - Sélecteur d'Interfaces");
            primaryStage.setScene(scene);
            primaryStage.show();

            System.out.println("✅ Launcher démarré avec succès!");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du démarrage:");
            e.printStackTrace();
        }
    }

    private VBox createLauncherUI(Stage primaryStage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14;");

        // Titre
        Label titleLabel = new Label("🎯 UniEarn - Sélecteur d'Interfaces");
        titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #1E56DB;");

        Label subtitleLabel = new Label("Choisissez l'interface à tester:");
        subtitleLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #666;");

        // Boutons pour chaque interface
        VBox buttonsBox = new VBox(15);
        buttonsBox.setPadding(new Insets(20));
        buttonsBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-color: #f9f9f9;");

        // Bouton Admin
        Button adminButton = createInterfaceButton(
            "👨‍💼 Interface Admin",
            "Gestion des contrats, templates et approvals",
            () -> launchApp("/contracts/admin_contracts.fxml", "UniEarn - Gestion des Contrats (Admin)", primaryStage)
        );

        // Bouton Client
        Button clientButton = createInterfaceButton(
            "👤 Interface Client",
            "Créer et signer des contrats",
            () -> launchApp("/contracts/client_contracts.fxml", "UniEarn - Gestion des Contrats (Client)", primaryStage)
        );

        // Bouton Freelancer
        Button freelancerButton = createInterfaceButton(
            "🚀 Interface Freelancer",
            "Consulter et signer les contrats",
            () -> launchApp("/contracts/freelancer_contracts.fxml", "UniEarn - Gestion des Contrats (Freelancer)", primaryStage)
        );

        // Bouton Signature
        Button signatureButton = createInterfaceButton(
            "✍️ Interface Signature",
            "Signer les contrats électroniquement",
            () -> launchApp("/contracts/contract_signature.fxml", "UniEarn - Signature Électronique", primaryStage)
        );

        buttonsBox.getChildren().addAll(
            adminButton,
            clientButton,
            freelancerButton,
            signatureButton
        );

        // Bouton Quitter
        Button quitButton = new Button("❌ Quitter");
        quitButton.setStyle("-fx-font-size: 12; -fx-padding: 10; -fx-background-color: #f44336; -fx-text-fill: white;");
        quitButton.setPrefWidth(Double.MAX_VALUE);
        quitButton.setOnAction(e -> primaryStage.close());

        root.getChildren().addAll(
            titleLabel,
            subtitleLabel,
            buttonsBox,
            new Separator(),
            quitButton
        );

        return root;
    }

    private Button createInterfaceButton(String title, String description, Runnable action) {
        VBox buttonContent = new VBox(5);
        buttonContent.setPadding(new Insets(10));

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #1E56DB;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #666; -fx-wrap-text: true;");

        buttonContent.getChildren().addAll(titleLabel, descLabel);

        Button button = new Button();
        button.setGraphic(buttonContent);
        button.setStyle(
            "-fx-padding: 15;" +
            "-fx-background-color: white;" +
            "-fx-border-color: #1E56DB;" +
            "-fx-border-width: 2;" +
            "-fx-cursor: hand;" +
            "-fx-text-alignment: left;"
        );
        button.setPrefHeight(80);
        button.setPrefWidth(Double.MAX_VALUE);

        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-padding: 15;" +
            "-fx-background-color: #f0f4ff;" +
            "-fx-border-color: #1E56DB;" +
            "-fx-border-width: 2;" +
            "-fx-cursor: hand;" +
            "-fx-text-alignment: left;"
        ));

        button.setOnMouseExited(e -> button.setStyle(
            "-fx-padding: 15;" +
            "-fx-background-color: white;" +
            "-fx-border-color: #1E56DB;" +
            "-fx-border-width: 2;" +
            "-fx-cursor: hand;" +
            "-fx-text-alignment: left;"
        ));

        button.setOnAction(e -> action.run());

        return button;
    }

    private void launchApp(String fxmlPath, String title, Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();

            Stage newStage = new Stage();
            Scene scene = new Scene(root);
            newStage.setTitle(title);
            newStage.setScene(scene);
            newStage.setWidth(1400);
            newStage.setHeight(800);
            newStage.show();

            System.out.println("✅ " + title + " démarré avec succès!");
        } catch (Exception e) {
            showErrorAlert("Erreur de lancement", "Impossible de charger l'interface:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Une erreur s'est produite");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

