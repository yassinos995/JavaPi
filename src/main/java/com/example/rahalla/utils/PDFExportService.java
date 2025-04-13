package com.example.rahalla.utils;

import com.example.rahalla.models.Commentaire;
import com.example.rahalla.models.Post;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;

public class PDFExportService {
    private static final float MARGIN = 50;
    private static final float FONT_SIZE_TITLE = 18;
    private static final float FONT_SIZE_HEADER = 14;
    private static final float FONT_SIZE_NORMAL = 12;
    private static final float LINE_SPACING = 1.5f;
    private static final int MAX_LINE_LENGTH = 80;

    public static void exportPostToPDF(Post post, List<Commentaire> comments, String outputPath) throws IOException {
        validateInputs(post, outputPath);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = page.getMediaBox().getHeight() - MARGIN;

                // Write title
                yPosition = writeTitle(contentStream, post.getTitle(), yPosition);
                yPosition -= FONT_SIZE_TITLE; // Add space after title

                // Write metadata
                yPosition = writeMetadata(contentStream, post, yPosition);
                yPosition -= FONT_SIZE_NORMAL;

                // Write content
                yPosition = writeContent(contentStream, document, post.getContent(), yPosition);
                yPosition -= FONT_SIZE_NORMAL * 2;

                // Add image if present
                if (post.getImage() != null && !post.getImage().isEmpty()) {
                    yPosition = addImage(document, contentStream, post.getImage(), yPosition);
                    yPosition -= FONT_SIZE_NORMAL * 2;
                }

                // Write comments
                if (comments != null && !comments.isEmpty()) {
                    yPosition = writeComments(contentStream, document, comments, yPosition);
                }
            }

            // Save the document
            saveDocument(document, outputPath);
        } catch (IOException e) {
            System.out.println("Error creating PDF" + e.getMessage());
            System.out.println("Failed to create PDF: " + e.getMessage());
        }
    }

    private static void validateInputs(Post post, String outputPath) throws IOException {
        if (post == null) {
            throw new IllegalArgumentException("Post cannot be null");
        }
        if (outputPath == null || outputPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Output path cannot be null or empty");
        }

        // Validate output directory exists
        Path path = Paths.get(outputPath);
        File parentDir = path.getParent().toFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            System.out.println("Failed to create output directory: " + parentDir);
        }
    }

    private static float writeTitle(PDPageContentStream contentStream, String title, float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_TITLE);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(truncateText(title, MAX_LINE_LENGTH));
        contentStream.endText();
        return yPosition - (FONT_SIZE_TITLE * LINE_SPACING);
    }

    private static float writeMetadata(PDPageContentStream contentStream, Post post, float yPosition) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy 'at' hh:mm a");
        String metadata = String.format("Location: %s | Posted on: %s | By: %s",
                post.getLieu(),
                dateFormat.format(post.getCreatedAt()),
                post.getUser().getNom() + " " + post.getUser().getPrenom()
        );

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_NORMAL);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(truncateText(metadata, MAX_LINE_LENGTH));
        contentStream.endText();
        return yPosition - (FONT_SIZE_NORMAL * LINE_SPACING);
    }

    private static float writeContent(PDPageContentStream contentStream, PDDocument document, String content, float yPosition) throws IOException {
        String[] paragraphs = content.split("\n");
        float currentY = yPosition;

        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_NORMAL);
        for (String paragraph : paragraphs) {
            if (currentY < MARGIN) {
                // Close current content stream
                contentStream.close();

                // Create new page and content stream
                PDPage newPage = new PDPage(PDRectangle.A4);
                document.addPage(newPage);
                contentStream = new PDPageContentStream(document, newPage);
                currentY = newPage.getMediaBox().getHeight() - MARGIN;
                contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_NORMAL);
            }

            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, currentY);
            contentStream.showText(truncateText(paragraph.trim(), MAX_LINE_LENGTH));
            contentStream.endText();
            currentY -= FONT_SIZE_NORMAL * LINE_SPACING;
        }
        return currentY;
    }

    private static float addImage(PDDocument document, PDPageContentStream contentStream,
                                  String imagePath, float yPosition) throws IOException {
        try {
            PDImageXObject image = PDImageXObject.createFromFile(imagePath, document);
            float scale = 0.5f; // Scale down image to fit page
            float imageWidth = image.getWidth() * scale;
            float imageHeight = image.getHeight() * scale;

            // Center image horizontally
            float xPosition = (PDRectangle.A4.getWidth() - imageWidth) / 2;

            contentStream.drawImage(image, xPosition, yPosition - imageHeight, imageWidth, imageHeight);
            return yPosition - imageHeight - (FONT_SIZE_NORMAL * LINE_SPACING);
        } catch (IOException e) {
            System.out.println("Failed to add image to PDF: " + e.getMessage());
            return yPosition; // Continue without image
        }
    }

    private static float writeComments(PDPageContentStream contentStream, PDDocument document, List<Commentaire> comments,
                                       float yPosition) throws IOException {
        float currentY = yPosition;

        // Write comments header
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_HEADER);
        contentStream.newLineAtOffset(MARGIN, currentY);
        contentStream.showText("Comments (" + comments.size() + "):");
        contentStream.endText();
        currentY -= FONT_SIZE_HEADER * LINE_SPACING;

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_NORMAL);

        for (Commentaire comment : comments) {
            if (currentY < MARGIN + FONT_SIZE_NORMAL * 3) {
                // Close current content stream
                contentStream.close();

                // Create new page and content stream
                PDPage newPage = new PDPage(PDRectangle.A4);
                document.addPage(newPage);
                contentStream = new PDPageContentStream(document, newPage);
                currentY = newPage.getMediaBox().getHeight() - MARGIN;
                contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_NORMAL);
            }

            // Write comment metadata
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_NORMAL);
            contentStream.newLineAtOffset(MARGIN, currentY);
            String commentMetadata = String.format("%s - %s",
                    comment.getUser().getNom() + " " + comment.getUser().getPrenom(),
                    dateFormat.format(comment.getCreated_at()) // Changed from getDate() to getCreatedAt()
            );
            contentStream.showText(truncateText(commentMetadata, MAX_LINE_LENGTH));
            contentStream.endText();
            currentY -= FONT_SIZE_NORMAL * LINE_SPACING;

            // Write comment content
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_NORMAL);
            contentStream.newLineAtOffset(MARGIN + 20, currentY); // Indent comment content
            contentStream.showText(truncateText(comment.getContent(), MAX_LINE_LENGTH - 5));
            contentStream.endText();
            currentY -= (FONT_SIZE_NORMAL * LINE_SPACING * 1.5f); // Add extra space between comments
        }

        return currentY;
    }

    private static String truncateText(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength - 3) + "..." : text;
    }

    private static void saveDocument(PDDocument document, String outputPath) throws IOException {
        try {
            document.save(outputPath);
            System.out.println("PDF exported successfully to: " + outputPath);
        } catch (IOException e) {
            System.out.println("Failed to save PDF document" + e.getMessage());
            System.out.println("Failed to save PDF document: " + e.getMessage());
        }
    }
}