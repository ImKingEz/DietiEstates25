package com.dietiestates25ui.controller;

import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25.dto.ImmobileDTO;
import com.dietiestates25ui.model.Immobile;
import com.dietiestates25ui.service.ImmobileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ConfermaInserzioneController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(ConfermaInserzioneController.class);

    @FXML
    private Button indietroButton;
    @FXML
    private Button confermaButton;
    @FXML
    private Label titoloLabel;
    @FXML
    private Label indirizzoLabel;
    @FXML
    private Label prezzoLabel;
    @FXML
    private Label superficieLabel;
    @FXML
    private Label descrizioneLabel;
    @FXML
    private ImageView immobileImageView;
    @FXML
    private Label vicinanzeLabel;

    private Immobile Immobile;

    private String token;

    private ImmobileService immobileService = new ImmobileService(); // Crea un'istanza di ImmobileService

    public void setImmobile(Immobile Immobile) {
        this.Immobile = Immobile;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            logo.requestFocus();
            currentStage = (Stage) primaryAnchorPane.getScene().getWindow();
            mostraDettagliImmobile();
        });

        indietroButton.setOnAction(event -> openInserimentoDatiInserzionePage());
        confermaButton.setOnAction(event -> salvaImmobile());
    }

    private void mostraDettagliImmobile() {
        if (Immobile != null) {
            titoloLabel.setText("Titolo: " + Immobile.getTitolo());
            indirizzoLabel.setText("Indirizzo: " + Immobile.getIndirizzo());
            prezzoLabel.setText("Prezzo: €" + Immobile.getPrezzo());
            superficieLabel.setText("Superficie: " + Immobile.getDimensione() + " mq");
            descrizioneLabel.setText("Descrizione: " + Immobile.getDescrizione());

            // Gestione dell'immagine
            if (Immobile.getImmaginiUrls() != null && !Immobile.getImmaginiUrls().isEmpty()) {
                String firstImageUrl = Immobile.getImmaginiUrls().get(0);
                try {
                    Image image = new Image(firstImageUrl);
                    immobileImageView.setImage(image);
                } catch (Exception e) {
                    logger.error("Errore durante il caricamento dell'immagine: {}", e.getMessage());
                    // In caso di errore, cancella l'immagine
                    immobileImageView.setImage(null);
                }
            } else {
                // Se non ci sono immagini, cancella l'immagine
                immobileImageView.setImage(null);
            }

            // Gestione delle vicinanze
            StringBuilder vicinanzeText = new StringBuilder();
            if (Immobile.isVicinoScuole()) {
                vicinanzeText.append("Vicino a scuole, ");
            }
            if (Immobile.isVicinoParchi()) {
                vicinanzeText.append("Vicino a parchi, ");
            }
            if (Immobile.isVicinoTrasportoPubblico()) {
                vicinanzeText.append("Vicino a trasporto pubblico, ");
            }

            if (vicinanzeText.length() > 0) {
                vicinanzeText.delete(vicinanzeText.length() - 2, vicinanzeText.length());
            }

            vicinanzeLabel.setText("Vicinanze: " + vicinanzeText.toString());
        } else {
            logger.warn("Immobile is null. Check if data is being passed correctly.");
            // Imposta valori di default o placeholder per evitare errori
            titoloLabel.setText("Titolo: Non disponibile");
            indirizzoLabel.setText("Indirizzo: Non disponibile");
            prezzoLabel.setText("Prezzo: Non disponibile");
            superficieLabel.setText("Superficie: Non disponibile");
            descrizioneLabel.setText("Descrizione: Non disponibile");
            vicinanzeLabel.setText("Vicinanze: Non disponibile");

            immobileImageView.setImage(null);
        }
    }

    private void openInserimentoDatiInserzionePage() {
        loadScene("/com/dietiestates25ui/view/inserimento-inserzione-view.fxml",
                (fxmlLoader, stage) -> {}, indietroButton, "/com/dietiestates25ui/styles/inserimento-inserzione-style.css");
    }

    private void salvaImmobile() {
        try {
            ImmobileService.fetchCsrfToken();
            ImmobileDTO immobileDTO = convertToDTO(Immobile);
            immobileService.salvaImmobile(immobileDTO, token);
            showPopup("Successo", "Immobile salvato correttamente!", SUCCESS_ICON);
            logger.info("Immobile salvato correttamente!");
            PauseTransition delay = new PauseTransition(Duration.millis(POPUP_PAUSE));
            delay.setOnFinished(event -> openGestioneImmobiliPage());
            delay.play();


        } catch (Exception e) {
            Platform.runLater(() -> {
                showPopup("Errore", "Errore durante il salvataggio dell'immobile: " + e.getMessage(), ERROR_ICON);
                logger.error("Errore durante il salvataggio dell'immobile: {}", e.getMessage());
            });
        }
    }

    private ImmobileDTO convertToDTO(Immobile immobile) {
        ImmobileDTO immobileDTO = new ImmobileDTO();
        immobileDTO.setTitolo(immobile.getTitolo());
        immobileDTO.setTipologia(immobile.getTipologia());
        immobileDTO.setIndirizzo(immobile.getIndirizzo());
        immobileDTO.setPrezzo(immobile.getPrezzo());
        immobileDTO.setDescrizione(immobile.getDescrizione());
        immobileDTO.setDimensione(immobile.getDimensione());
        immobileDTO.setNumero_camere(immobile.getNumero_camere());
        immobileDTO.setNumero_bagni(immobile.getNumero_bagni());
        immobileDTO.setClasseEnergetica(immobile.getClasseEnergetica());
        immobileDTO.setPiano(immobile.getPiano());
        immobileDTO.setAscensore(immobile.isAscensore());
        immobileDTO.setPortineria(immobile.isPortineria());
        immobileDTO.setClimatizzazione(immobile.isClimatizzazione());
        immobileDTO.setLatitudine(immobile.getLatitudine());
        immobileDTO.setLongitudine(immobile.getLongitudine());
        immobileDTO.setVicinoScuole(immobile.isVicinoScuole());
        immobileDTO.setVicinoParchi(immobile.isVicinoParchi());
        immobileDTO.setVicinoTrasportoPubblico(immobile.isVicinoTrasportoPubblico());
        immobileDTO.setImmaginiUrls(immobile.getImmaginiUrls());
        return immobileDTO;
    }

    private void openGestioneImmobiliPage() {
        loadScene("/com/dietiestates25ui/view/gestione-immobili-view.fxml",
                (fxmlLoader, stage) -> {}, indietroButton, "/com/dietiestates25ui/styles/gestione-immobili-style.css");
    }
}