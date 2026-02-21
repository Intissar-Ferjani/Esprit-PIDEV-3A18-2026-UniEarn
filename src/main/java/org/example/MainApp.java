package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // ✅ Load the homepage as entry point
        Parent root = FXMLLoader.load(getClass().getResource("/home/HomePage.fxml"));

        Scene scene = new Scene(root, 1100, 700);

        primaryStage.setTitle("UniEarn — Plateforme de freelancing étudiant");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}