package com.example.rahalla.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.example.rahalla.models.Post;
import com.example.rahalla.services.PostService;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class EditPostController implements Initializable {
    private final PostService postService;
    @FXML
    private Button cancelBtn;

    @FXML
    private TextArea contentField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loadBtn;

    @FXML
    private TextField locationField;

    @FXML
    private ImageView postImageView;

    @FXML
    private Button saveButton;

    @FXML
    private TextField titleField;
    private Post currentPost;
    private ProgressIndicator loadingSpinner;

    public EditPostController() {
        this.postService = new PostService();
    }

    @FXML
    void loadImageBtn(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        fileChooser.setTitle("Choisir une image");
        java.io.File file = fileChooser.showOpenDialog(postImageView.getScene().getWindow());
        if (file != null) {
            String imagePath = file.getAbsolutePath();
            postImageView.setImage(new javafx.scene.image.Image( imagePath));
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupValidation();
        setupFormStyling();
        setupLoadingIndicator();
    }

    private void setupLoadingIndicator() {
        loadingSpinner = new ProgressIndicator();
        loadingSpinner.setMaxSize(20, 20);
        loadingSpinner.getStyleClass().add("spinner");
        loadingSpinner.setVisible(false);
    }

    private void setLoading(boolean loading) {
        if (saveButton != null) {
            if (loading) {
                saveButton.getStyleClass().add("loading-button");
                saveButton.setGraphic(loadingSpinner);
                loadingSpinner.setVisible(true);
            } else {
                saveButton.getStyleClass().remove("loading-button");
                saveButton.setGraphic(null);
                loadingSpinner.setVisible(false);
            }
            saveButton.setDisable(loading);
            cancelBtn.setDisable(loading);
        }
    }

    private void setupFormStyling() {
        titleField.getStyleClass().add("form-field");
        locationField.getStyleClass().add("form-field");
        contentField.getStyleClass().add("form-textarea");

        if (errorLabel != null) {
            errorLabel.setTextFill(Color.RED);
            errorLabel.setVisible(false);
        }
    }

    private void setupValidation() {
        titleField.textProperty().addListener((obs, old, newValue) -> {
            validateField(titleField, !newValue.trim().isEmpty(), "Le titre est obligatoire");
        });

        contentField.textProperty().addListener((obs, old, newValue) -> {
            validateField(contentField, !newValue.trim().isEmpty(), "Le contenu est obligatoire");
        });

        locationField.textProperty().addListener((obs, old, newValue) -> {
            validateField(locationField, !newValue.trim().isEmpty(), "Le lieu est obligatoire");
        });
    }

    public void setPostData(Post post) {
        this.currentPost = post;
        titleField.setText(post.getTitle());
        contentField.setText(post.getContent());
        locationField.setText(post.getLieu());
        if (post.getImage() != null) {
            postImageView.setImage(new javafx.scene.image.Image(post.getImage()));
        } else {
            postImageView.setImage(null);
        }
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        setLoading(true);
        hideError();

        try {
            updatePostFromForm();

            // Validate using ValidationService

            if (postService.update(currentPost)) {
                showAlert("Success", "Post updated successfully", Alert.AlertType.INFORMATION);
                navigateToHome();
            } else {
                showError("Failed to update post. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Error updating post" + e.getMessage());
            showError("An unexpected error occurred: " + e.getMessage());
        } finally {
            setLoading(false);
        }
    }

    private void validateField(Control field, boolean isValid, String errorMessage) {
        if (!isValid) {

            showError(errorMessage);
        } else {

            hideError();
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            if (!errorLabel.isVisible()) {
                errorLabel.setVisible(true);
                FadeTransition ft = new FadeTransition(Duration.millis(200), errorLabel);
                ft.setFromValue(0.0);
                ft.setToValue(1.0);
                ft.play();
            }
        }
    }

    private void hideError() {
        if (errorLabel != null && errorLabel.isVisible()) {
            FadeTransition ft = new FadeTransition(Duration.millis(200), errorLabel);
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.setOnFinished(e -> errorLabel.setVisible(false));
            ft.play();
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (titleField.getText().trim().isEmpty()) {
            validateField(titleField, false, "Le titre est obligatoire");
            isValid = false;
        }
        if (contentField.getText().trim().isEmpty()) {
            validateField(contentField, false, "Le contenu est obligatoire");
            isValid = false;
        }
        if (locationField.getText().trim().isEmpty()) {
            validateField(locationField, false, "Le lieu est obligatoire");
            isValid = false;
        }

        return isValid;
    }

    private void updatePostFromForm() {
        currentPost.setTitle(titleField.getText().trim());
        currentPost.setContent(contentField.getText().trim());
        currentPost.setLieu(locationField.getText().trim());
        currentPost.setImage(postImageView.getImage() != null ? postImageView.getImage().getUrl() : null);
    }

    @FXML
    private void handleCancel() {
        if (hasChanges()) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation");
            confirmation.setHeaderText("Abandonner les modifications?");
            confirmation.setContentText("Les modifications non sauvegardées seront perdues.");

            DialogPane dialogPane = confirmation.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("/com/example/rahalla/styles.css").toExternalForm());
            dialogPane.getStyleClass().add("dialog-pane");

            if (confirmation.showAndWait().get() == ButtonType.OK) {
                navigateToHome();
            }
        } else {
            navigateToHome();
        }
    }

    private boolean hasChanges() {
        return !titleField.getText().equals(currentPost.getTitle()) ||
                !contentField.getText().equals(currentPost.getContent()) ||
                !locationField.getText().equals(currentPost.getLieu());
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/example/rahalla/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        alert.showAndWait();
    }

    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/rahalla/postsUser.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.out.println("Error navigating to home" + e.getMessage());
            showAlert("Erreur", "Impossible de retourner à la page d'accueil", Alert.AlertType.ERROR);
        }
    }
}