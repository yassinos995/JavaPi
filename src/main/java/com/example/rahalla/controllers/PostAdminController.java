package com.example.rahalla.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.example.rahalla.models.Commentaire;
import com.example.rahalla.models.Post;
import com.example.rahalla.services.CommentaireService;
import com.example.rahalla.services.PostService;
import com.example.rahalla.services.UserService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

public class PostAdminController implements Initializable {
    private final int userId = 1;  // Made final to address the warning
    private final PostService postService = new PostService();
    private final UserService userService = new UserService();
    private String imagePath;
    @FXML
    private ListView<String> postListView;
    @FXML
    private Button returnToPostInterfaceBtn;
    private boolean showingComments = false;
    private boolean isEditing = false;
    private Stage commentStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadPosts();
        postListView.setCellFactory(lv -> new ListCell<String>() {
            private final VBox vbox = new VBox(10); // Espacement vertical de 10
            private final HBox hbox = new HBox(10); // Espacement horizontal de 10
            private final VBox vContainer = new VBox(15); // Espacement vertical de 15

            {
                vContainer.setPadding(new Insets(10)); // Ajoute un padding autour du conteneur
                vContainer.getChildren().addAll(vbox, hbox);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                // Nettoyage des conteneurs
                vbox.getChildren().clear();
                hbox.getChildren().clear();

                Post post = Post.fromString(item);

                // Configuration de l'image
                ImageView imageView = new ImageView();
                imageView.setFitHeight(100); // Taille plus grande
                imageView.setFitWidth(100);
                imageView.setPreserveRatio(true);

                if (!isEditing) {
                    // Mode affichage
                    Label titleLabel = new Label(post.getTitle());
                    Label contentLabel = new Label(post.getContent());
                    Label locationLabel = new Label("Location: " + post.getLieu());
                    Label dateLabel = new Label("Date: " + post.getCreatedAt().toString());

                    // Style des labels
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    contentLabel.setWrapText(true);
                    contentLabel.setMaxWidth(300);

                    if (post.getImage() != null && !post.getImage().isEmpty() && !post.getImage().equals("null")) {
                        try {
                            File file = new File(post.getImage());
                            if (file.exists()) {  // Vérifie si le fichier existe
                                String imageUri = file.toURI().toString();
                                imageView.setImage(new Image(imageUri));
                            } else {
                                System.out.println("Image non trouvée: " + post.getImage());
                                // Optionnel : afficher une image par défaut
                                // imageView.setImage(new Image("/com/example/rahalla/images/default.png"));
                            }
                        } catch (Exception e) {
                            System.out.println("Erreur de chargement de l'image: " + e.getMessage());
                        }
                    } else {
                        // Retirer l'ImageView si pas d'image
                        imageView.setImage(null);
                    }

                    vbox.getChildren().addAll(imageView, titleLabel, contentLabel, locationLabel, dateLabel);

                    // Boutons en mode affichage
                    Button editBtn = new Button("Edit");
                    Button deleteBtn = new Button("Delete");
                    Button commentBtn = new Button("Show Comments");

                    // Style des boutons
                    editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                    deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                    commentBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

                    // Actions des boutons
                    editBtn.setOnAction(e -> {
                        isEditing = true;
                        loadPosts();
                    });

                    deleteBtn.setOnAction(e -> {
                        postService.remove(post.getId());
                        loadPosts();
                    });

                    commentBtn.setOnAction(e -> {
                        if (commentStage != null && commentStage.isShowing()) {
                            // Si la fenêtre est déjà ouverte, la fermer
                            commentStage.close();
                            commentStage = null;
                            return;
                        }

                        CommentaireService commentaireService = new CommentaireService();
                        List<Commentaire> comments = commentaireService.getCommentsByPostId(post.getId());

                        VBox messageBox = new VBox(10);
                        messageBox.setPadding(new Insets(15));

                        if (comments.isEmpty()) {
                            Label noCommentsLabel = new Label("Aucun commentaire pour le moment");
                            noCommentsLabel.setStyle("-fx-font-size: 14px;");
                            messageBox.getChildren().add(noCommentsLabel);
                        } else {
                            comments.forEach(comment -> {
                                HBox commentBox = new HBox(10);
                                Label commentLabel = new Label(comment.getContent());

                                commentLabel.setWrapText(true);
                                commentLabel.setStyle("-fx-font-size: 13px;");
                                dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");

                                commentBox.getChildren().addAll(commentLabel, dateLabel);
                                messageBox.getChildren().add(commentBox);
                            });
                        }

                        commentStage = new Stage();
                        commentStage.setTitle("Commentaires");
                        commentStage.setScene(new Scene(messageBox));
                        commentStage.initOwner(postListView.getScene().getWindow());

                        // Gérer la fermeture de la fenêtre
                        commentStage.setOnCloseRequest(event -> {
                            commentStage = null;
                        });

                        commentStage.show();
                    });

                    hbox.getChildren().addAll(editBtn, deleteBtn, commentBtn);

                } else {
                    // Mode édition
                    TextField titleField = new TextField(post.getTitle());
                    TextField contentField = new TextField(post.getContent());
                    TextField locationField = new TextField(post.getLieu());

                    Button chooseImageBtn = new Button("Choisir une image");
                    chooseImageBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                    // Style des champs
                    titleField.setMaxWidth(300);
                    contentField.setMaxWidth(300);
                    locationField.setMaxWidth(300);

                    if (post.getImage() != null && !post.getImage().isEmpty() && !post.getImage().equals("null")) {
                        try {
                            File file = new File(post.getImage());
                            if (file.exists()) {
                                imageView.setImage(new Image(file.toURI().toString()));
                            }
                        } catch (Exception e) {
                            System.out.println("Erreur de chargement de l'image: " + e.getMessage());
                        }
                    }
                    chooseImageBtn.setOnAction(e -> {
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Sélectionner une image");
                        fileChooser.getExtensionFilters().add(
                                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
                        );

                        File selectedFile = fileChooser.showOpenDialog(vbox.getScene().getWindow());
                        if (selectedFile != null) {
                            imageView.setImage(new Image(selectedFile.toURI().toString()));
                            post.setImage(selectedFile.getAbsolutePath());
                        }
                    });

                    vbox.getChildren().addAll(
                            imageView,
                            new Label("Title:"), titleField,
                            new Label("Content:"), contentField,
                            new Label("Location:"), locationField
                    );

                    // Boutons en mode édition
                    Button saveBtn = new Button("Save");
                    Button cancelBtn = new Button("Cancel");

                    saveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                    cancelBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                    saveBtn.setOnAction(e -> {
                        post.setTitle(titleField.getText());
                        post.setContent(contentField.getText());
                        post.setLieu(locationField.getText());
                        postService.update(post);
                        isEditing = false;
                        loadPosts();
                    });

                    cancelBtn.setOnAction(e -> {
                        isEditing = false;
                        loadPosts();
                    });

                    hbox.getChildren().addAll(saveBtn, cancelBtn);
                }

                hbox.setAlignment(Pos.CENTER_RIGHT);
                hbox.setPadding(new Insets(10, 0, 0, 0)); // Ajoute un espace en haut
                setGraphic(vContainer);
            }
        });
    }

    void loadPosts() {
        postListView.getItems().clear();
        for (Post post : postService.getAll()) {
            postListView.getItems().add(post.toString());
        }
    }

    @FXML
    void returnToPostInterface(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/rahalla/postInterface.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) returnToPostInterfaceBtn.getScene().getWindow();
            stage.setTitle("User Posts");
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Navigation Error");
            alert.setContentText("Failed to return to post interface: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
