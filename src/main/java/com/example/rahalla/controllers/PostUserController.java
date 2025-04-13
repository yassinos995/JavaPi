package com.example.rahalla.controllers;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javafx.scene.control.ScrollPane;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import com.example.rahalla.models.Commentaire;
import com.example.rahalla.models.Post;
import com.example.rahalla.services.CommentaireService;
import com.example.rahalla.services.PostService;
import com.example.rahalla.utils.EmailService;
import com.example.rahalla.utils.PDFExportService;
import com.example.rahalla.utils.QRCodeService;
import com.google.zxing.WriterException;

import javafx.animation.PauseTransition;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PostUserController implements Initializable {
    private static final String BASE_URL = "http://rahalla.com";
    private final Set<Integer> likedPosts = new HashSet<>();
    private final PostService postService;
    private final CommentaireService commentaireService;
    public Button returnToPostInterfaceBtn;
    private boolean postsLoading = false;
    @FXML
    private VBox postsContainer;
    @FXML
    private TextField searchField;
    @FXML
    private Button themeToggleBtn;

    public PostUserController() {
        this.postService = new PostService();
        this.commentaireService = new CommentaireService();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupSearchField();
        loadPosts();
    }

    private void loadPosts() {
        postsContainer.getChildren().clear();
        List<Post> posts = postService.getAll();

        if (posts == null || posts.isEmpty()) {
            Label noPostsLabel = new Label("No posts available");
            postsContainer.getChildren().add(noPostsLabel);
            return;
        }

        for (Post post : posts) {
            createPostBox(post);
        }
    }

    private void createPostBox(Post post) {
        VBox postBox = new VBox(10);
        postBox.setId("post-" + post.getId());
        postBox.getStyleClass().add("post-box");
        postBox.setPadding(new Insets(15));

        // Post title
        Label titleLabel = new Label(post.getTitle());
        titleLabel.getStyleClass().add("post-title");
        postBox.getChildren().add(titleLabel);

        // Post image with validation
        String imagePath = post.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    if (!image.isError()) {
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(300);
                        imageView.setPreserveRatio(true);
                        imageView.getStyleClass().add("post-image");
                        postBox.getChildren().add(imageView);
                    }
                }
            } catch (Exception e) {
                System.out.println("Failed to load image: " + e.getMessage());
            }
        }

        // Post content
        TextArea contentArea = new TextArea();
        contentArea.setWrapText(true);
        contentArea.setEditable(false);
        contentArea.setPrefRowCount(3);
        contentArea.setText(post.getContent());
        contentArea.getStyleClass().add("form-textarea");
        postBox.getChildren().add(contentArea);

        // Add remaining components
        HBox metaBox = new HBox(15);
        metaBox.getStyleClass().add("post-meta");
        metaBox.getChildren().addAll(
                new Label("📍 " + post.getLieu()),
                new Label("📅 " + formatDate(post.getCreatedAt())));
        postBox.getChildren().add(metaBox);

        // Action buttons
        HBox buttonBox = createActionButtons(post);
        postBox.getChildren().add(buttonBox);

        VBox commentsSection = new VBox(5);
        commentsSection.setId("comments-section-" + post.getId());
        commentsSection.getStyleClass().add("comments-section");
        commentsSection.setPadding(new Insets(10, 0, 0, 20));

        List<Commentaire> comments = commentaireService.getCommentsByPostId(post.getId());
        if (!comments.isEmpty()) {int commentsToShow = Math.min(3, comments.size());
            for (int i = 0; i < commentsToShow; i++) {
                Commentaire comment = comments.get(i);
                HBox commentBox = new HBox(10);
                Label commentLabel = new Label("💬 " + comment.getContent());
                Label dateLabel = new Label();
                dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
                Button editCommentBtn = new Button("✏️");
                editCommentBtn.getStyleClass().add("comment-action-button");
                editCommentBtn.setOnAction(e -> handleEditComment(comment));

                Button deleteCommentBtn = new Button("🗑️");
                deleteCommentBtn.getStyleClass().add("comment-action-button");
                deleteCommentBtn.setOnAction(e -> handleDeleteComment(comment, post));

                commentBox.getChildren().addAll(commentLabel, dateLabel,editCommentBtn,deleteCommentBtn);
                commentsSection.getChildren().add(commentBox);
            }
            if (comments.size() > 3) {
                Button showMoreBtn = new Button("Voir plus de commentaires (" + (comments.size() - 3) + ")");
                showMoreBtn.getStyleClass().add("show-more-button");
                showMoreBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2196F3; -fx-cursor: hand;");

                showMoreBtn.setOnAction(e -> {
                    Stage commentStage = new Stage();
                    commentStage.initModality(Modality.APPLICATION_MODAL);
                    commentStage.setTitle("Tous les commentaires");

                    VBox allCommentsBox = new VBox(10);
                    allCommentsBox.setPadding(new Insets(15));

                    ScrollPane scrollPane = new ScrollPane();
                    scrollPane.setContent(allCommentsBox);
                    scrollPane.setFitToWidth(true);
                    scrollPane.setPrefViewportWidth(400);
                    scrollPane.setPrefViewportHeight(300);

                    comments.forEach(comment -> {
                        HBox commentBox = new HBox(10);
                        Label commentLabel = new Label("💬 " + comment.getContent());
                        Label dateLabel = new Label();
                        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");

                        Button editCommentBtn = new Button("✏️");
                        editCommentBtn.getStyleClass().add("comment-action-button");
                        editCommentBtn.setOnAction(e1 -> handleEditComment(comment));

                        Button deleteCommentBtn = new Button("🗑️");
                        deleteCommentBtn.getStyleClass().add("comment-action-button");
                        deleteCommentBtn.setOnAction(e1 -> handleDeleteComment(comment, post));

                        commentBox.getChildren().addAll(commentLabel, dateLabel);
                        allCommentsBox.getChildren().add(commentBox);
                    }); 

                    Scene scene = new Scene(scrollPane);
                    scene.getStylesheets().add(getClass().getResource("/com/example/rahalla/styles.css").toExternalForm());
                    commentStage.setScene(scene);
                    commentStage.show();
                });

                commentsSection.getChildren().add(showMoreBtn);
            }

            postBox.getChildren().add(commentsSection);
        }

        // Share buttons
        Button exportButton = new Button("Export PDF");
        exportButton.getStyleClass().add("button-raised");
        exportButton.setOnAction(e -> handleExportPDF(post));

        Button qrButton = new Button("Share QR");
        qrButton.getStyleClass().add("button-raised");
        qrButton.setOnAction(e -> handleGenerateQR(post));

        HBox shareButtons = new HBox(10, exportButton, qrButton);
        shareButtons.setAlignment(Pos.CENTER_RIGHT);
        postBox.getChildren().add(shareButtons);

        postsContainer.getChildren().add(postBox);




    }

    private void handleEditComment(Commentaire comment) {
        TextInputDialog dialog = new TextInputDialog(comment.getContent());
        dialog.setTitle("Modifier le commentaire");
        dialog.setHeaderText("Éditer votre commentaire");
        dialog.setContentText("Nouveau contenu:");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/example/rahalla/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        dialog.showAndWait().ifPresent(newContent -> {
            if (newContent.trim().isEmpty()) {
                showAlert("Erreur", null, "Le commentaire ne peut pas être vide", Alert.AlertType.ERROR);
                return;
            }

            comment.setContent(newContent);
            if (commentaireService.update(comment)) {
                showAlert("Succès", null, "Commentaire modifié avec succès!", Alert.AlertType.INFORMATION);
                loadPosts(); // Refresh the posts to show updated comment
            } else {
                showAlert("Erreur", null, "Échec de la modification", Alert.AlertType.ERROR);
            }
        });
    }

    private void handleDeleteComment(Commentaire comment, Post post) {
        showConfirmationDialog(
                "Confirmation",
                "Supprimer ce commentaire?",
                "Cette action est irréversible.",
                () -> {
                    if (commentaireService.remove(comment.getId())) {
                        showAlert("Succès", null, "Commentaire supprimé avec succès", Alert.AlertType.INFORMATION);
                        loadPosts(); // Refresh the posts
                    } else {
                        showAlert("Erreur", null, "Échec de la suppression", Alert.AlertType.ERROR);
                    }
                });
    }

    private PauseTransition searchDebouncer = new PauseTransition(Duration.millis(100));

    private void setupSearchField() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchDebouncer.setOnFinished(e -> {
                if (!newValue.trim().isEmpty()) {
                    displaySearchResults(newValue.trim());
                } else {
                    loadPosts();
                }
            });
            searchDebouncer.playFromStart();
        });
    }


    private void displaySearchResults(String searchKeyword) {
        postsContainer.getChildren().clear();
        final String finalKeyword = searchKeyword.toLowerCase();

        // Optimisation de la recherche
        List<Post> filteredPosts = postService.getAll().stream()
                .filter(post ->
                        post.getTitle().toLowerCase().contains(finalKeyword) ||
                                post.getContent().toLowerCase().contains(finalKeyword) ||
                                post.getLieu().toLowerCase().contains(finalKeyword))
                .limit(20)
                .collect(Collectors.toList());

        if (filteredPosts.isEmpty()) {
            Label noMatchLabel = new Label("Aucun post ne correspond à \"" + searchKeyword + "\"");
            postsContainer.getChildren().add(noMatchLabel);
            return;
        }

        filteredPosts.forEach(this::createPostBox);
    }

    private HBox createActionButtons(Post post) {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button likeButton = createLikeButton(post);
        Button commentButton = createCommentButton(post);
        Button shareButton = createShareButton(post);
        Button editButton = createEditButton(post);
        Button deleteButton = createDeleteButton(post);

        buttonBox.getChildren().addAll(likeButton, commentButton, shareButton, editButton, deleteButton);
        return buttonBox;
    }

    private Button createLikeButton(Post post) {
        Button likeButton = new Button("❤️ " + post.getLikes());
        likeButton.setId("like-" + post.getId());
        likeButton.getStyleClass().add("like-button");
        updateLikeButtonStyle(likeButton, post);

        likeButton.setOnMouseEntered(e -> {
            if (!likedPosts.contains(post.getId())) {
                likeButton.getStyleClass().add("like-button-hover");
            }
        });

        likeButton.setOnMouseExited(e -> {
            if (!likedPosts.contains(post.getId())) {
                likeButton.getStyleClass().remove("like-button-hover");
            }
        });

        likeButton.setOnAction(e -> handleLike(post, likeButton));
        return likeButton;
    }

    private Button createCommentButton(Post post) {
        Button commentButton = new Button("💬 Comment");
        commentButton.getStyleClass().add("action-button");
        commentButton.setOnAction(e -> handleComment(post));
        return commentButton;
    }

    private Button createShareButton(Post post) {
        Button shareButton = new Button("↗️ Share");
        shareButton.getStyleClass().add("action-button");
        shareButton.setOnAction(e -> handleShare(post));
        return shareButton;
    }

    private Button createEditButton(Post post) {
        Button editButton = new Button("Modifier");
        editButton.getStyleClass().add("edit-button");
        editButton.setOnAction(e -> handleEditPost(e, post));
        return editButton;
    }

    private Button createDeleteButton(Post post) {
        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(e -> handleDeletePost(post));
        return deleteButton;
    }

    private void handleLike(Post post, Button likeButton) {
        if (likedPosts.contains(post.getId())) {
            return;
        }

        likedPosts.add(post.getId());
        likeButton.getStyleClass().add("liked");
        likeButton.setText("❤️ " + (post.getLikes() + 1));

        postService.incrementLikes(post.getId());

        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
        pause.setOnFinished(e -> {
            int updatedLikes = postService.getLikeCount(post.getId());
            post.setLikes(updatedLikes);
            updateLikeButtonStyle(likeButton, post);
        });
        pause.play();
    }

    private void handleEditPost(ActionEvent event, Post post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/rahalla/editpost.fxml"));
            Parent root = loader.load();

            EditPostController controller = loader.getController();
            controller.setPostData(postService.getById(post.getId()));

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier le Post");

            stage.setOnHidden(e -> loadPosts());
            stage.show();
        } catch (IOException e) {
            System.out.println("Error opening editor" + e.getMessage());
            showAlert("Erreur", null, "Impossible d'ouvrir l'éditeur", Alert.AlertType.ERROR);
        }
    }

    private void handleDeletePost(Post post) {
        showConfirmationDialog(
                "Confirmation",
                "Supprimer ce post?",
                "Cette action est irréversible.",
                () -> {
                    if (postService.remove(post.getId())) {
                        showAlert("Succès", null, "Post supprimé avec succès", Alert.AlertType.INFORMATION);
                        loadPosts();
                    } else {
                        showAlert("Erreur", null, "Échec de la suppression", Alert.AlertType.ERROR);
                    }
                });
    }

    private void updateLikeButtonStyle(Button button, Post post) {
        if (likedPosts.contains(post.getId())) {
            button.getStyleClass().add("liked");
        } else {
            button.getStyleClass().remove("liked");
        }
        button.setText("❤️ " + post.getLikes());
    }

    private void handleComment(Post post) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Ajouter un commentaire");
        dialog.setHeaderText("Commenter: " + post.getTitle());
        dialog.setContentText("Votre commentaire:");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/example/rahalla/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        dialog.showAndWait().ifPresent(comment -> {
            if (comment.trim().isEmpty()) {
                showAlert("Erreur", null, "Le commentaire ne peut pas être vide", Alert.AlertType.ERROR);
                return;
            }
            try {
                // Create and save the comment
                Commentaire commentaire = new Commentaire();
                commentaire.setContent(comment);
                commentaire.setPost(post);
                commentaire.setCreated_at(new java.sql.Timestamp(System.currentTimeMillis()));
                // Using a placeholder user ID - in a real app this would be the logged in user
                commentaire.setUser(post.getUser()); // Using post owner as commenter for demo

                boolean success = commentaireService.add(commentaire);
                if (success) {
                    VBox postBox = (VBox) postsContainer.lookup("#post-" + post.getId());
                    if (postBox != null) {
                        VBox commentsSection = (VBox) postBox.lookup("#comments-section-" + post.getId());
                        if (commentsSection == null) {
                            commentsSection = new VBox(5);
                            commentsSection.setId("comments-section-" + post.getId());
                            commentsSection.getStyleClass().add("comments-section");
                            commentsSection.setPadding(new Insets(10, 0, 0, 20));
                            postBox.getChildren().add(commentsSection);
                        }
                        HBox commentBox = new HBox(10);
                        Label commentLabel = new Label("💬 " + comment);
                        Label dateLabel = new Label();
                        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
                        commentBox.getChildren().addAll(commentLabel, dateLabel);
                        commentsSection.getChildren().add(commentBox);
                        showAlert("Succès", null, "Commentaire ajouté avec succès!", Alert.AlertType.INFORMATION);
                    }

                } else {
                    showAlert("Erreur", null, "Impossible d'ajouter le commentaire", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                System.out.println("Error adding comment: " + e.getMessage());
                showAlert("Erreur", null, "Impossible d'ajouter le commentaire: " + e.getMessage(),
                        Alert.AlertType.ERROR);
            }
        });
    }

    private void handleShare(Post post) {
        String shareContent = String.format("Découvrez ce post: %s\n\n%s\n\nPartagé depuis Camping Blog",
                post.getTitle(), post.getContent());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Partager le post");
        alert.setHeaderText("Post copié dans le presse-papiers");
        alert.setContentText(shareContent);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/example/rahalla/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(shareContent);
        clipboard.setContent(content);

        alert.showAndWait();
    }

    private void handleExportPDF(Post post) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select Export Location");
        File selectedDir = dirChooser.showDialog(postsContainer.getScene().getWindow());

        if (selectedDir != null) {
            try {
                String fileName = selectedDir.getAbsolutePath() + File.separator +
                        "post_" + post.getId() + ".pdf";

                PDFExportService.exportPostToPDF(
                        post,
                        new CommentaireService().getCommentsByPostId(post.getId()),
                        fileName);

                showAlert(
                        "Export Successful",
                        "Post exported as PDF",
                        "The post has been exported to: " + fileName,
                        Alert.AlertType.INFORMATION);

                // Notify post author with error handling
                try {
                    EmailService.sendEmail(
                            post.getUser().getEmail(),
                            "Your post has been exported",
                            "Your post '" + post.getTitle() + "' has been exported as PDF.");
                } catch (Exception emailError) {
                    System.out.println("Failed to send email notification" + emailError.getMessage());
                    // Show warning but don't block the export success
                    showAlert(
                            "Email Notification Failed",
                            "Export successful but failed to notify author",
                            "The post was exported successfully but we couldn't send an email notification: "
                                    + emailError.getMessage(),
                            Alert.AlertType.WARNING);
                }
            } catch (IOException | IllegalArgumentException e) {
                showAlert(
                        "Export Failed",
                        "Failed to export post",
                        "Error: " + e.getMessage(),
                        Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleExportAll() {
        try {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Select Export Directory");
            File selectedDir = dirChooser.showDialog(postsContainer.getScene().getWindow());

            if (selectedDir != null) {
                List<Post> posts = postService.getAll();
                CommentaireService commentaireService = new CommentaireService();
                int successCount = 0;

                for (Post post : posts) {
                    try {
                        String fileName = selectedDir.getAbsolutePath() + File.separator +
                                "post_" + post.getId() + ".pdf";
                        PDFExportService.exportPostToPDF(
                                post,
                                commentaireService.getCommentsByPostId(post.getId()),
                                fileName);
                        successCount++;
                    } catch (IOException e) {
                        System.out.println("Failed to export post " + post.getId() + e.getMessage());
                    }
                }

                showAlert(
                        "Export Complete",
                        "Export Status",
                        "Successfully exported " + successCount + " out of " + posts.size() + " posts",
                        successCount == posts.size() ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
            }
        } catch (Exception e) {
            System.out.println("Database error during export" + e.getMessage());
            showAlert(
                    "Export Failed",
                    "Database Error",
                    "Failed to export posts: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleGenerateReport() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Monthly Report");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
            fileChooser.setInitialFileName("monthly_report_" +
                    java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy_MM")) + ".pdf");

            File file = fileChooser.showSaveDialog(postsContainer.getScene().getWindow());
            if (file != null) {
                generateMonthlyReport(file.getAbsolutePath());
            }
        } catch (IOException e) {
            System.out.println("Failed to generate report" + e.getMessage());
            showAlert(
                    "Report Generation Failed",
                    "Failed to generate monthly report",
                    "Error: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void generateMonthlyReport(String outputPath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float y = page.getMediaBox().getHeight() - 50;
                float margin = 50;

                // Title
                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 18);
                content.newLineAtOffset(margin, y);
                content.showText("Monthly Activity Report");
                content.endText();
                y -= 30;

                // Date
                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 12);
                content.newLineAtOffset(margin, y);
                content.showText("Generated: " + java.time.LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy HH:mm:ss")));
                content.endText();
                y -= 30;

                // Statistics
                List<Post> allPosts = postService.getAll();
                CommentaireService commentaireService = new CommentaireService();

                int totalPosts = allPosts.size();
                int totalComments = commentaireService.getAll().size();
                int totalLikes = allPosts.stream().mapToInt(Post::getLikes).sum();

                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 14);
                content.newLineAtOffset(margin, y);
                content.showText("Statistics");
                content.endText();

                y -= 20;
                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 12);
                content.newLineAtOffset(margin, y);
                content.showText(String.format("Total Posts: %d", totalPosts));
                content.endText();

                y -= 15;
                content.beginText();
                content.newLineAtOffset(margin, y);
                content.showText(String.format("Total Comments: %d", totalComments));
                content.endText();

                y -= 15;
                content.beginText();
                content.newLineAtOffset(margin, y);
                content.showText(String.format("Total Likes: %d", totalLikes));
                content.endText();

                y -= 30;
                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 14);
                content.newLineAtOffset(margin, y);
                content.showText("Most Active Users");
                content.endText();
            }

            document.save(outputPath);

            showAlert(
                    "Report Generated",
                    "Monthly report has been generated successfully",
                    "The report has been saved to: " + outputPath,
                    Alert.AlertType.INFORMATION);
        }
    }

    private void handleGenerateQR(Post post) {
        try {
            String shareUrl = BASE_URL + "/posts/" + post.getId();
            BufferedImage qrBufferedImage = QRCodeService.generateQRCode(shareUrl);

            // Convert BufferedImage to JavaFX Image
            WritableImage qrImage = new WritableImage(qrBufferedImage.getWidth(), qrBufferedImage.getHeight());
            for (int x = 0; x < qrBufferedImage.getWidth(); x++) {
                for (int y = 0; y < qrBufferedImage.getHeight(); y++) {
                    qrImage.getPixelWriter().setArgb(x, y, qrBufferedImage.getRGB(x, y));
                }
            }

            showQRCodeDialog(qrImage, post.getTitle());
        } catch (IOException | WriterException e) {
            System.out.println("Failed to generate QR code" + e.getMessage());
            showAlert(
                    "QR Code Generation Failed",
                    "Error",
                    "Failed to generate QR code: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void showQRCodeDialog(Image qrImage, String postTitle) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("QR Code for: " + postTitle);

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        ImageView qrView = new ImageView(qrImage);
        qrView.setFitWidth(200);
        qrView.setFitHeight(200);

        Button saveButton = new Button("Save QR Code");
        saveButton.getStyleClass().add("button-raised");
        saveButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save QR Code");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PNG files", "*.png"));
            fileChooser.setInitialFileName("qr_post_" + postTitle.replaceAll("\\W+", "_") + ".png");

            File selectedFile = fileChooser.showSaveDialog(dialog);
            if (selectedFile != null) {
                try {
                    // Convert WritableImage to BufferedImage
                    int width = (int) qrImage.getWidth();
                    int height = (int) qrImage.getHeight();
                    BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bufferedImage.setRGB(x, y, qrImage.getPixelReader().getArgb(x, y));
                        }
                    }

                    ImageIO.write(bufferedImage, "png", selectedFile);

                    showAlert(
                            "Success",
                            "QR Code Saved",
                            "QR code has been saved to: " + selectedFile.getAbsolutePath(),
                            Alert.AlertType.INFORMATION);
                } catch (IOException ex) {
                    System.out.println("Failed to save QR code" + ex.getMessage());
                    showAlert(
                            "Save Failed",
                            "Error",
                            "Failed to save QR code: " + ex.getMessage(),
                            Alert.AlertType.ERROR);
                }
            }
        });

        content.getChildren().addAll(
                new Label("Scan this QR code to share the post:"),
                qrView,
                saveButton);

        Scene scene = new Scene(content);
        scene.getStylesheets().add(getClass().getResource("/com/example/rahalla/styles.css").toExternalForm());

        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private String formatDate(java.sql.Timestamp timestamp) {
        if (timestamp == null)
            return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
        return timestamp.toLocalDateTime().format(formatter);
    }

    @FXML
    private void navigateToAddPost() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/rahalla/addPost.fxml"));
            Parent root = loader.load();

            Stage addPostStage = new Stage();
            addPostStage.setTitle("Nouveau Post");
            addPostStage.setScene(new Scene(root));
            addPostStage.initModality(Modality.APPLICATION_MODAL);

            // Store reference to this controller in the scene's userData
            postsContainer.getScene().getRoot().setUserData(this);

            // Make the current window the owner of the dialog
            addPostStage.initOwner(postsContainer.getScene().getWindow());

            addPostStage.showAndWait();
            // We don't need to call loadPosts here since the AddPostController will call
            // reloadPosts
        } catch (IOException e) {
            System.out.println("Error opening add post window" + e.getMessage());
            showAlert("Erreur", null, "Impossible d'ouvrir l'éditeur de post", Alert.AlertType.ERROR);
        }
    }

    /**
     * Public method to reload posts from outside this controller
     * Used by AddPostController to refresh posts after adding a new one
     */
    public void reloadPosts() {
        loadPosts();
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/example/rahalla/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        alert.showAndWait();
    }

    private void showConfirmationDialog(String title, String header, String content, Runnable onConfirm) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle(title);
        confirmation.setHeaderText(header);
        confirmation.setContentText(content);

        DialogPane dialogPane = confirmation.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/example/rahalla/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            onConfirm.run();
        }
    }

    @FXML
    private void returnToPostInterface() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/rahalla/postInterface.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) returnToPostInterfaceBtn.getScene().getWindow();
            stage.setTitle("User Posts");
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            System.out.println("Failed to return to post interface" + e.getMessage());
            showAlert(
                    "Navigation Error",
                    "Failed to Navigate",
                    "Could not return to post interface: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }
}
