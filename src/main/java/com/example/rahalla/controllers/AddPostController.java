package com.example.rahalla.controllers;

import com.example.rahalla.models.Post;
import com.example.rahalla.models.User;
import com.example.rahalla.services.PostService;
import com.example.rahalla.services.UserService;
import com.example.rahalla.utils.ValidationHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ResourceBundle;

public class AddPostController implements Initializable {
    private static final int TITLE_MIN_LENGTH = 3;
    private static final int TITLE_MAX_LENGTH = 100;
    private static final int CONTENT_MIN_LENGTH = 10;
    private final PostService postService;
    private final UserService userService;
    private final int userId = 1; // TODO: Replace with actual user session
    @FXML
    private TextField titleField;
    @FXML
    private TextArea contentArea;
    @FXML
    private TextField locationField;
    @FXML
    private ImageView imageView;
    @FXML
    private Button loadImageBtn;
    @FXML
    private Button submitBtn;
    @FXML
    private Label titleError;
    @FXML
    private Label contentError;
    @FXML
    private Label locationError;
    private String imagePath;

    public AddPostController() {
        this.postService = new PostService();
        this.userService = new UserService();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupValidation();
        setupUI();
        setupInputListeners();
    }
    private void setupInputListeners() {
        // Title field validation
        titleField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                titleError.setText("Title should not be empty");
                titleError.setVisible(true);
            } else {
                titleError.setVisible(false);
            }
        });

        // Content area validation
        contentArea.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                contentError.setText("Content should not be empty");
                contentError.setVisible(true);
            } else {
                contentError.setVisible(false);
            }
        });

        // Location field validation
        locationField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                locationError.setText("Location should not be empty");
                locationError.setVisible(true);
            } else {
                locationError.setVisible(false);
            }
        });
    }


    private void setupValidation() {
        // Title validation
        ValidationHelper.setupLiveValidation(titleField, value -> {
            if (value.isEmpty()) {
                return new ValidationHelper.ValidationResult(false, "Title is required");
            }
            if (value.length() < TITLE_MIN_LENGTH) {
                return new ValidationHelper.ValidationResult(false,
                        "Title must be at least " + TITLE_MIN_LENGTH + " characters");
            }
            if (value.length() > TITLE_MAX_LENGTH) {
                return new ValidationHelper.ValidationResult(false,
                        "Title cannot exceed " + TITLE_MAX_LENGTH + " characters");
            }
            return new ValidationHelper.ValidationResult(true, null);
        });

        // Content validation
        contentArea.textProperty().addListener((obs, old, newValue) -> {
            ValidationHelper.ValidationResult result;
            if (newValue.isEmpty()) {
                result = new ValidationHelper.ValidationResult(false, "Content is required");
            } else if (newValue.length() < CONTENT_MIN_LENGTH) {
                result = new ValidationHelper.ValidationResult(false,
                        "Content must be at least " + CONTENT_MIN_LENGTH + " characters");
            } else {
                result = new ValidationHelper.ValidationResult(true, null);
            }
            Platform.runLater(() -> {
                contentError.setText(result.getMessage());
                contentError.setVisible(!result.isValid());
            });
        });

        // Location validation with regex
        ValidationHelper.setupLiveValidation(locationField, value -> {
            if (value.isEmpty()) {
                return new ValidationHelper.ValidationResult(false, "Location is required");
            }
            if (!value.matches("^[\\p{L}\\s,.'-]+$")) {
                return new ValidationHelper.ValidationResult(false,
                        "Location can only contain letters, spaces, and basic punctuation");
            }
            return new ValidationHelper.ValidationResult(true, null);
        });
    }

    private void setupUI() {
        titleField.getStyleClass().add("form-field");
        locationField.getStyleClass().add("form-field");
        contentArea.getStyleClass().add("form-textarea");

        submitBtn.getStyleClass().add("button-raised");
        loadImageBtn.getStyleClass().add("button-raised");

        titleError.getStyleClass().addAll("helper-text", "error");
        contentError.getStyleClass().addAll("helper-text", "error");
        locationError.getStyleClass().addAll("helper-text", "error");

        titleError.setVisible(false);
        contentError.setVisible(false);
        locationError.setVisible(false);
    }

    @FXML
    private void handleLoadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File selectedFile = fileChooser.showOpenDialog(loadImageBtn.getScene().getWindow());
        if (selectedFile != null) {
            imagePath = selectedFile.getAbsolutePath();
            imageView.setImage(new Image(  imagePath));
            imageView.setFitWidth(300);
            imageView.setPreserveRatio(true);
            imageView.setVisible(true);
        }
    }
    @FXML
    private void handleSubmit() {
        if (!validateForm()) {
            if (titleField.getText().trim().isEmpty()) {
                titleField.getStyleClass().add("error-field");
            }
            if (contentArea.getText().trim().isEmpty()) {
                contentArea.getStyleClass().add("error-field");
            }
            if (locationField.getText().trim().isEmpty()) {
                locationField.getStyleClass().add("error-field");
            }
            return;
        }
        titleField.getStyleClass().remove("error-field");
        contentArea.getStyleClass().remove("error-field");
        locationField.getStyleClass().remove("error-field");

        Post post = new Post();
        post.setTitle(titleField.getText().trim());
        post.setContent(contentArea.getText().trim());
        post.setLieu(locationField.getText().trim());
        post.setImage(imagePath); // This could be null if no image was selected
        post.setLikes(0);
        post.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        // Get user - handle potential null user safely
        User user = userService.getById(userId);
        if (user == null) {
            ValidationHelper.showValidationError(
                    "User Error",
                    "Unable to find user with ID " + userId + ". Please check your login.");
            return;
        }
        post.setUser(user);

        try {
            if (postService.add(post)) {
                showSuccessAndClose();
            } else {
                ValidationHelper.showValidationError(
                        "Error",
                        "Failed to create post. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Error creating post: " + e.getMessage());
            e.printStackTrace(); // This will help with debugging
            ValidationHelper.showValidationError(
                    "Error",
                    "An unexpected error occurred: " + e.getMessage());
        }
    }

    private void showSuccessAndClose() {
        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Success");
        success.setHeaderText(null);
        success.setContentText("Post created successfully!");

        DialogPane dialogPane = success.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/com/example/rahalla/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        success.showAndWait();

        // Get the current stage and close it
        Stage currentStage = (Stage) submitBtn.getScene().getWindow();
        currentStage.close();

        // Attempt to refresh the post list if we're in a dialog opened by
        // PostUserController
        try {
            if (currentStage.getOwner() != null) {
                // Send a notification to the parent window to refresh posts
                Stage ownerStage = (Stage) currentStage.getOwner();

                // If this was called from a PostUserController, we need to reload posts
                if (ownerStage.getScene().getRoot().getUserData() instanceof PostUserController) {
                    PostUserController controller = (PostUserController) ownerStage.getScene().getRoot().getUserData();
                    controller.reloadPosts();
                }
            }
        } catch (Exception e) {
            System.out.println("Note: Couldn't notify parent window to refresh: " + e.getMessage());
            // Not critical, so we don't need to show an error
        }
    }

    private boolean validateForm() {
        final StringBuilder titleErrors = new StringBuilder();
        final StringBuilder contentErrors = new StringBuilder();
        final StringBuilder locationErrors = new StringBuilder();
        final boolean[] isValidRef = {true}; // Utiliser un tableau pour avoir une référence modifiable

        // Validation du titre
        if (titleField.getText().trim().isEmpty()) {
            titleErrors.append("Le titre est obligatoire");
            isValidRef[0] = false;
        } else if (titleField.getText().length() < TITLE_MIN_LENGTH) {
            titleErrors.append("Le titre doit contenir au moins ").append(TITLE_MIN_LENGTH).append(" caractères");
            isValidRef[0] = false;
        } else if (titleField.getText().length() > TITLE_MAX_LENGTH) {
            titleErrors.append("Le titre ne doit pas dépasser ").append(TITLE_MAX_LENGTH).append(" caractères");
            isValidRef[0] = false;
        }

        // Validation du contenu
        if (contentArea.getText().trim().isEmpty()) {
            contentErrors.append("Le contenu est obligatoire");
            isValidRef[0] = false;
        } else if (contentArea.getText().length() < CONTENT_MIN_LENGTH) {
            contentErrors.append("Le contenu doit contenir au moins ").append(CONTENT_MIN_LENGTH).append(" caractères");
            isValidRef[0] = false;
        }

        // Validation du lieu
        if (locationField.getText().trim().isEmpty()) {
            locationErrors.append("Le lieu est obligatoire");
            isValidRef[0] = false;
        }

        // Appliquer les erreurs et styles
        Platform.runLater(() -> {
            titleError.setText(titleErrors.toString());
            contentError.setText(contentErrors.toString());
            locationError.setText(locationErrors.toString());

            titleError.setVisible(titleErrors.length() > 0);
            contentError.setVisible(contentErrors.length() > 0);
            locationError.setVisible(locationErrors.length() > 0);

            if (!isValidRef[0]) {
                if (titleErrors.length() > 0) titleField.getStyleClass().add("error-field");
                if (contentErrors.length() > 0) contentArea.getStyleClass().add("error-field");
                if (locationErrors.length() > 0) locationField.getStyleClass().add("error-field");
            }
        });

        return isValidRef[0];
    }
}