package com.dietiestates25ui.controller;

import com.dietiestates25.dto.AgenteDTO;
import com.dietiestates25.dto.ImmobileDTO;
import com.dietiestates25ui.model.AgenteImmobiliare;
import com.dietiestates25ui.model.Annuncio;
import com.dietiestates25ui.model.Immobile;
import com.dietiestates25ui.service.AgenteService;
import com.dietiestates25ui.service.AnnuncioService;
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
import org.jetbrains.annotations.NotNull;
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
    private Label serviziLabel;
    @FXML
    private Label tipoLabel;
    @FXML
    private Label tipologiaLabel;
    @FXML
    private Label numeroCamereLabel;
    @FXML
    private Label numeroBagniLabel;
    @FXML
    private Label classeEnergeticaLabel;
    @FXML
    private Label pianoLabel;

    private String token;

    private List<File> selectedImageList;

    private Immobile immobile;
    private ImmobileService immobileService = new ImmobileService();

    private Annuncio annuncio;
    private AnnuncioService annuncioService = new AnnuncioService();

    private AgenteImmobiliare agente;
    private AgenteService agenteService = new AgenteService();

    public void setAgente(AgenteImmobiliare agente) {
        this.agente = agente;
    }

    public void setImmobile(Immobile immobile) {
        this.immobile = immobile;
    }

    public void setAnnuncio(Annuncio annuncio) {
        this.annuncio = annuncio;
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
        titoloLabel.setText(annuncio.getTitolo());
        indirizzoLabel.setText(immobile.getIndirizzo());
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY);
        prezzoLabel.setText(currencyFormatter.format(annuncio.getPrezzo()));
        superficieLabel.setText(String.format("%.2f mq", immobile.getDimensione()));
        descrizioneLabel.setText(annuncio.getDescrizione());
        vicinanzeLabel.setText(getVicinanzeText());
        String serviziText = getServiziText();
        serviziLabel.setText(serviziText);

        tipoLabel.setText(annuncio.getTipo());
        tipologiaLabel.setText(immobile.getTipologia());

        numeroCamereLabel.setText(String.valueOf(immobile.getNumeroLocali()));
        numeroBagniLabel.setText(String.valueOf(immobile.getNumeroBagni()));
        classeEnergeticaLabel.setText(immobile.getClasseEnergetica());
        String pianoText = getPianoText();
        pianoLabel.setText(pianoText);
        if (annuncio.getImmaginiUrls() != null && !annuncio.getImmaginiUrls().isEmpty()) {
            String firstImageUrl = annuncio.getImmaginiUrls().get(0);
            try {
                Image image = new Image(firstImageUrl);
                immobileImageView.setImage(image);
            } catch (Exception e) {
                logger.error("Errore durante il caricamento dell'immagine: {}", e.getMessage());
                immobileImageView.setImage(null);
            }
        } else {
            immobileImageView.setImage(null);
        }
    }

    @NotNull
    private String getPianoText() {
        if(immobile.getPiano() == 0) {
            return "Piano terra";
        } else if(immobile.getPiano() == 1) {
            return "Piano intermedio";
        } else {
            return "Ultimo piano";
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

        if (!serviziText.isEmpty()) {
            serviziText.delete(serviziText.length() - 2, serviziText.length());
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

        if (!vicinanzeText.isEmpty()) {
            vicinanzeText.delete(vicinanzeText.length() - 2, vicinanzeText.length());
        }

        return vicinanzeText.toString();
    }

    private void openInserimentoDatiInserzionePage() {
        loadScene("/com/dietiestates25ui/view/inserimento-inserzione-view.fxml",
                (fxmlLoader, stage) -> {
                    InserimentoInserzioneController controller = fxmlLoader.getController();
                    controller.setTitoloTextField(annuncio.getTitolo());
                    controller.setTipologiaMenuButton(immobile.getTipologia());
                    controller.setTipoMenuButton(annuncio.getTipo());
                    controller.setIndirizzoTextField(immobile.getIndirizzo());
                    controller.setPrezzoTextField(String.valueOf(annuncio.getPrezzo()));
                    controller.setDescrizioneTextArea(annuncio.getDescrizione());
                    controller.setSuperficieTextField(String.valueOf(immobile.getDimensione()));
                    controller.setCamereSpinner(immobile.getNumeroLocali());
                    controller.setBagniSpinner(immobile.getNumeroBagni());
                    controller.setClasseEnergeticaMenuButton(immobile.getClasseEnergetica());
                    controller.setPianoMenubutton(getPianoText());
                    controller.setAscensoreCheckBox(immobile.isAscensore());
                    controller.setPortineriaCheckBox(immobile.isPortineria());
                    controller.setClimatizzazioneCheckBox(immobile.isClimatizzazione());
                    controller.setSelectedImageList(annuncio.getImmaginiUrls());
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
            ImmobileDTO immobileDTO = immobileService.salvaImmobile(immobile, token);
            annuncio.setIdImmobile(immobileDTO.getId());
            AgenteDTO agenteDTO = agenteService.getAgenteDetails(token);
            annuncio.setIdAgente(agenteDTO.getId());
            annuncioService.salvaAnnuncio(annuncio, token, selectedImageList);
            showPopup("Immobile e annuncio salvati correttamente!", "Reindirizzamento alla gestione immobili", SUCCESS_ICON);
            PauseTransition delay = new PauseTransition(Duration.millis(POPUP_PAUSE));
            delay.setOnFinished(event -> openGestioneImmobiliPage());
            delay.play();

        } catch (Exception e) {
            showPopup("Errore", "Errore durante il salvataggio dell'immobile", ERROR_ICON);
            logger.error("Errore durante il salvataggio dell'immobile: {}", e.getMessage());
        }
    }

    private void openGestioneImmobiliPage() {
        loadScene("/com/dietiestates25ui/view/agente-dashboard-view.fxml",
                (fxmlLoader, stage) -> {
                    AgenteDashboardController controller = fxmlLoader.getController();
                    controller.setToken(token);
                    controller.setAgente(agente);
                }, indietroButton, "/com/dietiestates25ui/styles/agente-dashboard-style.css");
    }
}