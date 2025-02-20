package com.dietiestates25ui.controller;

import com.dietiestates25.dto.UtenteDTO;
import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.model.Utente;
import com.dietiestates25ui.service.UtenteService;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

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

    @FXML
    private HBox profileHBox;

    @FXML
    private MenuButton prezzoMenuButton;

    @FXML
    private TextField minPriceTextField;

    @FXML
    private TextField maxPriceTextField;

    private static final double SCROLL_AMOUNT = 600.0;
    private final Duration scrollDuration = Duration.millis(500);

    Utente utente;

    UtenteService utenteService = new UtenteService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> logo.requestFocus());
        Platform.runLater(() -> currentStage = (Stage) primaryAnchorPane.getScene().getWindow());

        Platform.runLater(this::updateProfileHBox);

        Platform.runLater(() -> setAnnuncioImageView(annuncioImageView));
        Platform.runLater(() -> setAnnuncioImageView(annuncioImageView1));
        Platform.runLater(() -> setAnnuncioImageView(annuncioImageView2));

        scrollLeftButton.setOnAction(this::scrollLeft);
        scrollRightButton.setOnAction(this::scrollRight);

        Platform.runLater(this::initializePriceTextFieldListeners);
    }

    private void initializePriceTextFieldListeners() {
        minPriceTextField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (Boolean.FALSE.equals(newVal)) {
                try {
                    double minValue = Double.parseDouble(minPriceTextField.getText());
                    System.out.println("Min price: " + minValue);
                    // Qui puoi fare qualcosa con il valore minimo
                } catch (NumberFormatException e) {
                    System.out.println("Invalid min price.");
                    minPriceTextField.setText("");
                }
            }
        });

        maxPriceTextField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                try {
                    double maxValue = Double.parseDouble(maxPriceTextField.getText());
                    System.out.println("Max price: " + maxValue);
                    // Qui puoi fare qualcosa con il valore massimo
                } catch (NumberFormatException e) {
                    System.out.println("Invalid max price.");
                    maxPriceTextField.setText("");
                }
            }
        });
    }

    private void setUtente(String token) {
        try {
            logger.info("Recupero dati utente con token: {}", token);
            UtenteDTO utenteDTO = utenteService.getUtenteDetails(token);
            utente = new Utente();
            utente.setNome(utenteDTO.getNome());
            utente.setCognome(utenteDTO.getCognome());
            utente.setEmail(utenteDTO.getEmail());
            utente.setCitta(utenteDTO.getCitta());
        } catch (GenericServiceException e) {
            logger.error("Errore durante il recupero dei dati dell'utente: {}", e.getMessage());
        }
    }

    private void updateProfileHBox() {
        Text ciaoNome = new Text();
        ciaoNome.getStyleClass().add("profileName");
        ciaoNome.setText("Ciao " + utente.getNome());
        profileHBox.getChildren().addFirst(ciaoNome);
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
        scroll(SCROLL_AMOUNT);
    }

    @FXML
    private void scrollRight(ActionEvent event) {
        scroll(-SCROLL_AMOUNT);
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
        setUtente(token);
    }
}