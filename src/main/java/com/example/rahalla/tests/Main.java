package com.example.rahalla.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/rahalla/postInterface.fxml"));
        primaryStage.setTitle("Blog Posts");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }
}