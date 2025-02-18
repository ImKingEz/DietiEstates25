package com.dietiestates25ui.controller;

import com.dietiestates25ui.model.Immobile;
import com.dietiestates25ui.service.ImmobileService;
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

import java.io.File;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

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
    @FXML
    private Label serviziLabel; // NUOVA ETICHETTA
    @FXML
    private Label tipologiaLabel;  // NUOVA ETICHETTA
    @FXML
    private Label numeroCamereLabel;  // NUOVA ETICHETTA
    @FXML
    private Label numeroBagniLabel;  // NUOVA ETICHETTA
    @FXML
    private Label classeEnergeticaLabel;  // NUOVA ETICHETTA
    @FXML
    private Label pianoLabel;  // NUOVA ETICHETTA

    private Immobile immobile;

    private String token;

    private List<File> selectedImageList;

    private ImmobileService immobileService = new ImmobileService(); // Crea un'istanza di ImmobileService

    public void setImmobile(Immobile immobile) {
        this.immobile = immobile;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setSelectedImageList(List<File> selectedImageList) {
        this.selectedImageList = selectedImageList;
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
        if (immobile != null) {
            titoloLabel.setText(immobile.getTitolo());
            indirizzoLabel.setText(immobile.getIndirizzo());
            // Formatta il prezzo con il simbolo dell'euro
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY);
            prezzoLabel.setText(currencyFormatter.format(immobile.getPrezzo()));

            // Formatta la superficie con "mq"
            superficieLabel.setText(String.format("%.2f mq", immobile.getDimensione()));
            descrizioneLabel.setText(immobile.getDescrizione());
            vicinanzeLabel.setText(getVicinanzeText());

            // Gestione dei servizi aggiuntivi
            String serviziText = getServiziText();
            serviziLabel.setText(serviziText);

            tipologiaLabel.setText(immobile.getTipologia());
            numeroCamereLabel.setText(String.valueOf(immobile.getNumeroCamere()));
            numeroBagniLabel.setText(String.valueOf(immobile.getNumeroBagni()));
            classeEnergeticaLabel.setText(immobile.getClasseEnergetica());

            // Gestione del piano: se il piano è -1 (interrato) visualizzare "Interrato", altrimenti il numero del piano
            String pianoText = (immobile.getPiano() == -1) ? "Interrato" : String.valueOf(immobile.getPiano());
            pianoLabel.setText(pianoText);

            // Gestione dell'immagine: mostra solo la prima immagine
            if (immobile.getImmaginiUrls() != null && !immobile.getImmaginiUrls().isEmpty()) {
                String firstImageUrl = immobile.getImmaginiUrls().get(0);
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
        } else {
            // ... codice per i valori di default ...
        }
    }

    private String getServiziText() {
        StringBuilder serviziText = new StringBuilder();

        if (immobile.isAscensore()) {
            serviziText.append("Ascensore, ");
        }
        if (immobile.isPortineria()) {
            serviziText.append("Portineria, ");
        }
        if (immobile.isClimatizzazione()) {
            serviziText.append("Climatizzazione, ");
        }

        if (serviziText.length() > 0) {
            serviziText.delete(serviziText.length() - 2, serviziText.length()); // Remove last comma and space
        }

        return serviziText.toString();
    }

    private String getVicinanzeText() {
        StringBuilder vicinanzeText = new StringBuilder();
        if (immobile.isVicinoScuole()) {
            vicinanzeText.append("Scuole, ");
        }
        if (immobile.isVicinoParchi()) {
            vicinanzeText.append("Parchi, ");
        }
        if (immobile.isVicinoTrasportoPubblico()) {
            vicinanzeText.append("Trasporto pubblico, ");
        }

        if (vicinanzeText.length() > 0) {
            vicinanzeText.delete(vicinanzeText.length() - 2, vicinanzeText.length());
        }

        return vicinanzeText.toString();
    }

    private void openInserimentoDatiInserzionePage() {
        loadScene("/com/dietiestates25ui/view/inserimento-inserzione-view.fxml",
                (fxmlLoader, stage) -> {
                    InserimentoInserzioneController controller = fxmlLoader.getController();
                    controller.setTitoloTextField(immobile.getTitolo());
                    controller.setTipologiaChoiceBox(immobile.getTipologia());
                    controller.setIndirizzoTextField(immobile.getIndirizzo());
                    controller.setPrezzoTextField(String.valueOf(immobile.getPrezzo()));
                    controller.setDescrizioneTextArea(immobile.getDescrizione());
                    controller.setSuperficieTextField(String.valueOf(immobile.getDimensione()));
                    controller.setCamereSpinner(immobile.getNumeroCamere());
                    controller.setBagniSpinner(immobile.getNumeroBagni());
                    controller.setClasseEnergeticaTextField(immobile.getClasseEnergetica());
                    controller.setPianoSpinner(immobile.getPiano());
                    controller.setAscensoreCheckBox(immobile.isAscensore());
                    controller.setPortineriaCheckBox(immobile.isPortineria());
                    controller.setClimatizzazioneCheckBox(immobile.isClimatizzazione());
                    controller.setSelectedImageList(immobile.getImmaginiUrls());
                    controller.setVicinoScuoleCheckBox(immobile.isVicinoScuole());
                    controller.setVicinoParchiCheckBox(immobile.isVicinoParchi());
                    controller.setVicinoTrasportoPubblicoCheckBox(immobile.isVicinoTrasportoPubblico());

                    controller.setToken(token);
                    controller.setStage(stage);

                    controller.checkFormValidity();

                    Platform.runLater(() -> controller.logo.requestFocus());

                }, indietroButton, "/com/dietiestates25ui/styles/inserimento-inserzione-style.css");
    }

    private void salvaImmobile() {
        try {
            immobileService.salvaImmobile(immobile, token, selectedImageList);
            showPopup("Successo", "immobile salvato correttamente!", SUCCESS_ICON);
            logger.info("immobile salvato correttamente!");
            PauseTransition delay = new PauseTransition(Duration.millis(POPUP_PAUSE));
            delay.setOnFinished(event -> openGestioneImmobiliPage());
            delay.play();

        } catch (Exception e) {
            showPopup("Errore", "Errore durante il salvataggio dell'immobile", ERROR_ICON);
            logger.error("Errore durante il salvataggio dell'immobile: {}", e.getMessage());
        }
    }

    private void openGestioneImmobiliPage() {
        loadScene("/com/dietiestates25ui/view/gestione-immobili-view.fxml",
                (fxmlLoader, stage) -> {}, indietroButton, "/com/dietiestates25ui/styles/gestione-immobili-style.css");
    }
}