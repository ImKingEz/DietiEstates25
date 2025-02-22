package com.dietiestates25ui.controller;

import com.dietiestates25.dto.AnnuncioDTO;
import com.dietiestates25.dto.ImmobileDTO;
import com.dietiestates25ui.model.Annuncio;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class AnnuncioItemController {

    private static final Logger logger = LoggerFactory.getLogger(AnnuncioItemController.class);

    @FXML
    private ImageView annuncioImageView;

    @FXML
    private Text annuncioTitle;

    @FXML
    private Text prezzoAnnuncio;

    @FXML
    private Text textSuperficieDetailAnnuncio;

    @FXML
    private Text textLocaliDetailAnnuncio;

    @FXML
    private Text textBagniDetailAnnuncio;

    @FXML
    private Text textVicinanzaDetailAnnuncio;

    @FXML
    private ImageView imageVicinanzaDetailAnnuncio;

    public void setAnnuncio(AnnuncioDTO annuncio, ImmobileDTO immobile) {
        if (annuncio.getImmaginiUrls() != null && !annuncio.getImmaginiUrls().isEmpty()) {
            setAnnuncioImageView(annuncio.getImmaginiUrls().getFirst());
        } else {
            // Imposta un'immagine di placeholder o nascondi l'ImageView
            logger.warn("Nessuna immagine disponibile per l'annuncio: {}", annuncio.getTitolo());
            annuncioImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/dietiestates25ui/images/placeholder.png")))); // Assicurati di avere un'immagine placeholder
        }
        annuncioTitle.setText(annuncio.getTitolo());
        prezzoAnnuncio.setText(String.valueOf(annuncio.getPrezzo()));
        textLocaliDetailAnnuncio.setText(immobile.getNumeroLocali() + " locali");
        textBagniDetailAnnuncio.setText(immobile.getNumeroBagni() + " bagni");
        textSuperficieDetailAnnuncio.setText(immobile.getDimensione() + " mq");
        if (immobile.isVicinoScuole()) {
            textVicinanzaDetailAnnuncio.setText("Vicino una scuola");
            imageVicinanzaDetailAnnuncio.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/dietiestates25ui/images/schoolIcon.png"))));
        } else if (immobile.isVicinoParchi()) {
            textVicinanzaDetailAnnuncio.setText("Vicino un parco");
            imageVicinanzaDetailAnnuncio.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/dietiestates25ui/images/parkIcon.png"))));
        } else if (immobile.isVicinoTrasportoPubblico()) {
            textVicinanzaDetailAnnuncio.setText("Vicino ai trasporti");
            imageVicinanzaDetailAnnuncio.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/dietiestates25ui/images/busIcon.png"))));
        } else if (immobile.isAscensore()) {
            textVicinanzaDetailAnnuncio.setText("Con ascensore");
            imageVicinanzaDetailAnnuncio.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/dietiestates25ui/images/elevatorIcon.png"))));
        } else if (immobile.isPortineria()) {
            textVicinanzaDetailAnnuncio.setText("Con portineria");
            imageVicinanzaDetailAnnuncio.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/dietiestates25ui/images/portineriaIcon.png"))));
        } else if (immobile.isClimatizzazione()) {
            textVicinanzaDetailAnnuncio.setText("Con climatizzazione");
            imageVicinanzaDetailAnnuncio.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/dietiestates25ui/images/airConditionerIcon.png"))));
        } else {
            textVicinanzaDetailAnnuncio.setVisible(false);
            imageVicinanzaDetailAnnuncio.setVisible(false);
        }
    }

    private void setAnnuncioImageView(String imageUrl) {
        logger.debug("Setting image: http://localhost:8080{}", imageUrl);
        if (annuncioImageView != null) {
            // Modifica qui: carica l'immagine direttamente dall'URL restituito dal backend
            Image image = new Image("http://localhost:8080" + imageUrl); // Assicurati che l'URL sia corretto
            annuncioImageView.setImage(image);

            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();
            double aspectRatio = imageWidth / imageHeight;
            if (aspectRatio > 1) {
                double offsetX = (imageWidth - imageHeight) / 2;
                annuncioImageView.setViewport(new Rectangle2D(offsetX, 0, imageHeight, imageHeight));
            }
        } else {
            logger.error("ImageView annuncioImageView non iniettato! Controlla l'FXML.");
        }
    }
}