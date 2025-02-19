package com.dietiestates25ui.controller;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.geometry.Rectangle2D;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController extends AbstractController implements Initializable {

    private String token;

    @FXML
    private ScrollPane filterScrollPane;

    @FXML
    private HBox filterHBox;

    @FXML
    private Button scrollLeftButton;

    @FXML
    private Button scrollRightButton;

    @FXML
    private ImageView annuncioImageView;

    @FXML
    private ImageView annuncioImageView1;

    @FXML
    private ImageView annuncioImageView2;

    private final double scrollAmount = 600.0;
    private final Duration scrollDuration = Duration.millis(500);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> logo.requestFocus());
        Platform.runLater(() -> currentStage = (Stage) primaryAnchorPane.getScene().getWindow());

        Platform.runLater(() -> setAnnuncioImageView(annuncioImageView));
        Platform.runLater(() -> setAnnuncioImageView(annuncioImageView1));
        Platform.runLater(() -> setAnnuncioImageView(annuncioImageView2));

        scrollLeftButton.setOnAction(this::scrollLeft);
        scrollRightButton.setOnAction(this::scrollRight);
    }

    private void scroll(double deltaX) {
        double currentX = filterHBox.getTranslateX();
        double targetX = currentX + deltaX;

        double minX = filterScrollPane.getWidth() - filterHBox.getWidth();
        targetX = Math.max(minX, Math.min(0, targetX));

        TranslateTransition tt = new TranslateTransition(scrollDuration, filterHBox);
        tt.setToX(targetX);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();
    }

    @FXML
    private void scrollLeft(ActionEvent event) {
        scroll(scrollAmount);
    }

    @FXML
    private void scrollRight(ActionEvent event) {
        scroll(-scrollAmount);
    }

    private void setAnnuncioImageView(ImageView imageViewAnnuncio) {
        if (imageViewAnnuncio != null) {
            Image image = new Image("file:C:\\Users\\WIN10\\Pictures\\Screenshots\\casa.png");
            imageViewAnnuncio.setImage(image);

            // Calcola le proporzioni
            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();
            double aspectRatio = imageWidth / imageHeight;

            // Determina se l'immagine è più larga o più stretta dell'ImageView
            if (aspectRatio > 1) { // Immagine più larga che alta
                // Calcola la larghezza necessaria per riempire l'altezza
                double viewportWidth = imageHeight * (200.0 / 200.0); // Fondamentalmente è sempre imageHeight visto che fitHeight è 200

                // Calcola l'offset per centrare la porzione visibile
                double offsetX = (imageWidth - viewportWidth) / 2;

                // Imposta il viewport
                imageViewAnnuncio.setViewport(new Rectangle2D(offsetX, 0, viewportWidth, imageHeight));
            } else { // Immagine più alta che larga o quadrata
                //Non serve fare nulla, l'immagine si adatterà all'altezza senza ritagliare
            }

        } else {
            System.err.println("ImageView annuncioImageView non iniettato! Controlla l'FXML.");
        }
    }


    public void setToken(String token) {
        this.token = token;
    }

}