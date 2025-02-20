package com.dietiestates25ui.controller;

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
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
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
    private Label raggioRicercaLabel; // Aggiungi il campo per la label

    private boolean venditaSelezionato;

    private boolean affittoSelezionato;

    private String tipologiaSelezionata;

    private String token;

    private HomePageController homePageController;

    private Stage currentStage;

    public void setToken(String token) {
        this.token = token;
    }

    public void setStage(Stage stage) {
        this.currentStage = stage;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupUI();
        setupButtonActions();
        loadMap();
    }

    private void setupUI() {
        DecimalFormat df = new DecimalFormat("#");
        radiusSlider.setMin(100);
        radiusSlider.setMax(1000);
        radiusSlider.setValue(100);

        radiusSlider.valueProperty().addListener((ov, old_val, new_val) -> {
            raggioRicercaLabel.setText("Raggio di ricerca: " + df.format(new_val) + "mt");
        });
    }

    private void setupButtonActions() {
        tornaIndietroButton.setOnAction(event -> openHomePage());
        cercaButton.setOnAction(event -> handleCercaButtonAction());
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
            homeController.setPreviousSelection(getVenditaSelezionato(), getAffittoSelezionato(), getTipologiaSelezionata());
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

    public boolean getVenditaSelezionato() {
        return venditaSelezionato;
    }

    public boolean getAffittoSelezionato() {
        return affittoSelezionato;
    }

    public String getTipologiaSelezionata() {
        return tipologiaSelezionata;
    }
}