package com.dietiestates25ui.controller;

import com.dietiestates25.dto.AnnuncioDTO;
import com.dietiestates25.dto.MapSearchDTO;
import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.model.FiltroAnnunci;
import com.dietiestates25ui.service.AnnuncioService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class RicercaConMappaController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(RicercaConMappaController.class);

    @FXML
    private WebView mapWebView;
    @FXML
    private Slider radiusSlider;
    @FXML
    private Button cercaButton;
    @FXML
    private Button tornaIndietroButton;
    @FXML
    private Label raggioRicercaLabel;
    @FXML
    private Label titoloLabel;

    private boolean venditaSelezionato;

    private boolean affittoSelezionato;

    private String tipologiaSelezionata;

    private double selectedLatitude;
    private double selectedLongitude;

    private AnnuncioService annuncioService = new AnnuncioService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            titoloLabel.requestFocus();
            currentStage = (Stage) primaryAnchorPane.getScene().getWindow();
        });

        setupSlider();

        tornaIndietroButton.setOnAction(event -> openHomePage());
        cercaButton.setOnAction(event -> handleCercaButtonAction());

        loadMap();
    }

    private void setupSlider() {
        DecimalFormat df = new DecimalFormat("#");
        radiusSlider.setMin(100);
        radiusSlider.setMax(5000);
        radiusSlider.setValue(500);

        radiusSlider.valueProperty().addListener((ov, oldVal, newVal) -> {
            raggioRicercaLabel.setText("Raggio di ricerca: " + df.format(newVal) + "mt");

            Platform.runLater(() -> {
                WebEngine webEngine = mapWebView.getEngine();
                String script = "updateRadius(" + newVal.doubleValue() + ");";
                webEngine.executeScript(script);
            });
        });
    }

    private void loadMap() {
        WebEngine webEngine = mapWebView.getEngine();

        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == javafx.concurrent.Worker.State.SUCCEEDED) {
                Platform.runLater(() -> {
                    double initialRadius = radiusSlider.getValue();
                    String script = "setInitialRadius(" + initialRadius + ");";
                    webEngine.executeScript(script);
                });
            }
        });

        webEngine.load(getClass().getResource("/com/dietiestates25ui/view/map-for-radius.html").toExternalForm());

        webEngine.setOnAlert(event -> {
            String data = event.getData();
            String[] parts = data.split("\\|");
            if (parts.length == 3) {
                try {
                    selectedLatitude = Double.parseDouble(parts[1]);
                    selectedLongitude = Double.parseDouble(parts[2]);
                } catch (NumberFormatException e) {
                    logger.error("Errore durante la conversione delle coordinate: ", e);
                }
            } else {
                logger.warn("Formato dati inatteso ricevuto dalla WebView: {}", data);
            }
        });

    }

    private void openHomePage() {
        loadScene("/com/dietiestates25ui/view/homepage-view.fxml", (fxmlLoader, stage) -> {
            HomePageController homeController = fxmlLoader.getController();
            homeController.setStage(currentStage);
            if (venditaSelezionato) {
                homeController.venditaButton.setSelected(true);
                homeController.affittoButton.setSelected(false);
            } else {
                homeController.venditaButton.setSelected(false);
                homeController.affittoButton.setSelected(true);
            }
            homeController.tipologiaMenuButton.setText(getTipologiaSelezionata());
        }, tornaIndietroButton, "/com/dietiestates25ui/styles/homepage-style.css");
    }

    private void handleCercaButtonAction() {
        double radius = radiusSlider.getValue();
        logger.info("Performing map search with radius: {} meters", radius);

        FiltroAnnunci filtro = new FiltroAnnunci();
        filtro.setTipo(venditaSelezionato ? "Vendita" : "Affitto");
        filtro.setTipologia(tipologiaSelezionata);

        MapSearchDTO mapSearchDTO = new MapSearchDTO(
                selectedLatitude,
                selectedLongitude,
                radius
        );

        CompletableFuture.supplyAsync(() -> {
                    try {
                        return annuncioService.searchAnnunciByMap(mapSearchDTO, filtro, token);
                    } catch (GenericServiceException e) {
                        logger.error("Errore durante la ricerca degli annunci con mappa: {}", e.getMessage(), e);
                        Platform.runLater(() -> showPopup("Errore", "Errore durante la ricerca: " + e.getMessage(), ERROR_ICON));
                        return null;
                    }
                })
                .thenAccept(annunciDTO ->
                    Platform.runLater(() -> {
                        if (annunciDTO == null) {
                            return;
                        }
                        if (annunciDTO.isEmpty()) {
                            showPopup(POPUP_ERROR_TITLE, "Nessun immobile trovato nella zona.", ERROR_ICON);
                            logger.info("Nessun immobile trovato nella zona.");
                        } else {
                            for (AnnuncioDTO annuncioDTO : annunciDTO) {
                                logger.info("Immobile trovato: {}", annuncioDTO);
                            }
                            openRisultatiRicercaPage(token, filtro, cercaButton, mapSearchDTO);
                        }
                    }))
                .exceptionally(ex -> {
                    logger.error("Errore durante la chiamata al servizio: {}", ex.getMessage(), ex);
                    Platform.runLater(() -> showPopup("Errore", "Errore imprevisto: " + ex.getMessage(), ERROR_ICON));
                    return null;
                });
    }

    public void setVenditaSelezionato(boolean venditaSelezionato) {
        this.venditaSelezionato = venditaSelezionato;
    }

    public void setAffittoSelezionato(boolean affittoSelezionato) {
        this.affittoSelezionato = affittoSelezionato;
    }

    public void setTipologiaSelezionata(String tipologiaSelezionata) {
        this.tipologiaSelezionata = tipologiaSelezionata;
    }

    public String getTipologiaSelezionata() {
        return tipologiaSelezionata;
    }
}