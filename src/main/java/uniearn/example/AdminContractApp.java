package uniearn.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.stage.Stage;

public class AdminContractApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Charger le FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/contracts/admin_contracts.fxml"));
            BorderPane root = loader.load();

            Scene scene = new Scene(root, 1400, 800);
            primaryStage.setTitle("UniEarn - Gestion des Contrats (Admin)");
            primaryStage.setScene(scene);
            primaryStage.show();

            System.out.println(" AdminContractApp démarré avec succès!");
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement:");
            e.printStackTrace();
            showErrorDialog(e);
        }
    }

    private void showErrorDialog(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de Chargement");
        alert.setHeaderText("Impossible de charger l'interface");
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

