package com.example.rahalla.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;

public class ValidationHelper {
    public static void showValidationError(String title, String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Validation Error");
        alert.setContentText(errorMessage);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                ValidationHelper.class.getResource("/com/example/rahalla/styles.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("dialog-pane");

        alert.showAndWait();
    }

    public static void setValidationStyle(TextInputControl field, boolean isValid, String message) {
        if (!isValid) {
            field.getStyleClass().add("validation-error");
            Tooltip tooltip = new Tooltip(message);
            tooltip.getStyleClass().add("validation-tooltip");
            Tooltip.install(field, tooltip);
        } else {
            field.getStyleClass().removeAll("validation-error");
            Tooltip.uninstall(field, field.getTooltip());
            field.setTooltip(null);
        }
    }

    public static void clearValidation(TextInputControl field) {
        field.getStyleClass().removeAll("validation-error");
        Tooltip.uninstall(field, field.getTooltip());
        field.setTooltip(null);
    }

    public static void setupLiveValidation(TextInputControl field, ValidationRule rule) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            ValidationResult result = rule.validate(newValue);
            setValidationStyle(field, result.isValid(), result.getMessage());
        });
    }

    @FunctionalInterface
    public interface ValidationRule {
        ValidationResult validate(String value);
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}