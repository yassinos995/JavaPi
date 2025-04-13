package com.example.rahalla.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class PostInterfaceController {

    @FXML
    private Button adminPostBtn;

    @FXML
    private Button userPostBtn;

    @FXML
    void navigateToAdminPosts(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/rahalla/postsAdmin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) adminPostBtn.getScene().getWindow();
            stage.setTitle("Admin Posts");
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to navigate to admin posts: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    void navigateToUserPosts(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/rahalla/postsUser.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userPostBtn.getScene().getWindow();
            stage.setTitle("User Posts"); // Fixed: was incorrectly set to "Admin Posts"
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to navigate to user posts: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
