package com.dietiestates25ui.controller;

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
import java.util.ResourceBundle;
import java.text.DecimalFormat;

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

    private String token;

    private Stage currentStage;

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

    public void setToken(String token) {
        this.token = token;
    }

    private void setupSlider() {
        DecimalFormat df = new DecimalFormat("#");
        radiusSlider.setMin(100);
        radiusSlider.setMax(1000);
        radiusSlider.setValue(100);

        radiusSlider.valueProperty().addListener((ov, old_val, new_val) -> {
            raggioRicercaLabel.setText("Raggio di ricerca: " + df.format(new_val) + "mt");
        });
    }

    private void loadMap() {
        WebEngine webEngine = mapWebView.getEngine();
        webEngine.load(getClass().getResource("/com/dietiestates25ui/view/map2.html").toExternalForm());

        webEngine.setOnAlert(event -> {
            String data = event.getData();
            System.out.println("Data from WebView: " + data);
            // Process Data (Coordinates, address) from WebView
        });
    }

    private void openHomePage() {
        loadScene("/com/dietiestates25ui/view/home-page-view.fxml", (fxmlLoader, stage) -> {
            HomePageController homeController = fxmlLoader.getController();
            homeController.setToken(token);
            homeController.setStage(currentStage);
            if (venditaSelezionato) {
                homeController.venditaButton.setSelected(true);
                homeController.affittoButton.setSelected(false);
            } else {
                homeController.venditaButton.setSelected(false);
                homeController.affittoButton.setSelected(true);
            }
            homeController.tipologiaMenuButton.setText(getTipologiaSelezionata());
        }, tornaIndietroButton, "/com/dietiestates25ui/styles/home-page-style.css");
    }

    private void handleCercaButtonAction() {
        System.out.println("Slider Value: " + radiusSlider.getValue());
        System.out.println("Selections: Vendita: " + venditaSelezionato + ", Affitto: " + affittoSelezionato + ", Tipologia: " + tipologiaSelezionata);
        logger.info("Performing map search with radius: {} meters", radiusSlider.getValue());
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