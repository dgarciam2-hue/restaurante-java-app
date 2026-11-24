package com.devtalles.proyecto.utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class RestaurantAlert {

    private RestaurantAlert() {
    }

    public static void showSuccess(Window owner, String title, String message, String buttonText) {
        show(owner, true, title, message, buttonText, null);
    }

    public static void showError(Window owner, String title, String message, String buttonText) {
        show(owner, false, title, message, buttonText, null);
    }

    private static void show(Window owner, boolean isValid, String title, String message, String buttonText, Runnable onAction) {
        Stage stage = new Stage();
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);

        Label symbolLabel = new Label(isValid ? "✓" : "✕");
        symbolLabel.getStyleClass().add("sazon-modal-icon-symbol");

        StackPane iconCircle = new StackPane(symbolLabel);
        iconCircle.getStyleClass().add("sazon-modal-icon-circle");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("sazon-modal-title");

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("sazon-modal-message");
        messageLabel.setMaxWidth(340);

        String resolvedButtonText = (buttonText == null || buttonText.isBlank()) ? "Aceptar" : buttonText;
        Button actionButton = new Button(resolvedButtonText);
        actionButton.getStyleClass().add("sazon-modal-btn");
        actionButton.setOnAction(e -> {
            stage.close();
        });

        VBox card = new VBox(14, iconCircle, titleLabel, messageLabel, actionButton);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(390);
        card.getStyleClass().addAll("sazon-modal-card", isValid ? "sazon-alert-valid" : "sazon-alert-invalid");

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color: transparent;");
        root.setPadding(new Insets(16));

        var cssUrl = RestaurantAlert.class.getResource("/styles/modal-alert.css");
        if (cssUrl != null) {
            root.getStylesheets().add(cssUrl.toExternalForm());
        }

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.showAndWait();
    }
}
