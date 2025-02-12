package com.dietiestates25ui.controller;

import com.dietiestates25ui.MainApplication;
import com.dietiestates25ui.model.Immobile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.ResourceBundle;

public class InserimentoInserzioneController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(InserimentoInserzioneController.class);

    @FXML private TextField titoloTextField;
    @FXML private ChoiceBox<String> tipologiaChoiceBox;
    @FXML private TextField indirizzoTextField;
    @FXML private Button apriMappaButton;
    @FXML private WebView mapView;
    @FXML private TextField prezzoTextField;
    @FXML private TextArea descrizioneTextArea;
    @FXML private Button selezionaImmaginiButton;
    @FXML private FlowPane immaginiFlowPane;
    @FXML private TextField superficieTextField;
    @FXML private Spinner<Integer> camereSpinner;
    @FXML private Spinner<Integer> bagniSpinner;
    @FXML private TextField classeEnergeticaTextField;
    @FXML private Spinner<Integer> pianoSpinner;
    @FXML private CheckBox ascensoreCheckBox;
    @FXML private CheckBox portineriaCheckBox;
    @FXML private CheckBox climatizzazioneCheckBox;
    @FXML private Button avantiButton;
    @FXML private Button indietroButton;
    @FXML private CheckBox vicinoScuoleCheckBox;
    @FXML private CheckBox vicinoParchiCheckBox;
    @FXML private CheckBox vicinoTrasportoPubblicoCheckBox;

    private final ObservableList<File> selectedImageList = FXCollections.observableArrayList();
    private double latitudine;
    private double longitudine;

    private boolean mapInitialized = false;  // Flag per verificare se la mappa è stata inizializzata

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inizializza gli elementi FXML
        System.out.println("dentro initialize");
        apriMappaButton.setOnAction(this::handleApriMappaButtonAction);
        indietroButton.setOnAction(event -> openGestioneImmobiliPage());

        setupChoiceBox();
        setupSpinners();
        selezionaImmaginiButton.setOnAction(event -> Platform.runLater(this::handleImageSelection));
        updateAvantiButtonState();

        // Listener sull'indirizzoTextField (chiamato solo se la mappa è inizializzata)
        indirizzoTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Se la mappa non è visible, non fare nulla.
        });
    }

    public void setStage(Stage stage) {
        this.currentStage = stage;
    }

    @FXML
    private void handleApriMappaButtonAction(javafx.event.ActionEvent event) {
        System.out.println("dentro handleApriMappaButtonAction");
        try {
            mapView.setPrefWidth(primaryAnchorPane.getWidth());
            mapView.setPrefHeight(primaryAnchorPane.getHeight());
            AnchorPane.setTopAnchor(mapView, 0.0);
            AnchorPane.setBottomAnchor(mapView, 60.0); // Spazio per i bottoni
            AnchorPane.setLeftAnchor(mapView, 0.0);
            AnchorPane.setRightAnchor(mapView, 0.0);
            mapView.setVisible(true);

            // Forza un aggiornamento del layout
            primaryAnchorPane.applyCss();
            primaryAnchorPane.layout();

            System.out.println("prima di initializeMap");
            initializeMap();
        } catch (Exception e) {
            logger.error("Errore nel metodo handleApriMappaButtonAction: {}", e.getMessage(), e);
            showPopup("Errore", "Errore durante l'apertura della mappa: " + e.getMessage(), ERROR_ICON);
        }
    }

    private void initializeMap() {
        System.out.println("dentro initializeMap");
        if (mapView != null) {
            WebEngine webEngine = mapView.getEngine();

            // Listener per quando la pagina web è completamente caricata
            webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == Worker.State.SUCCEEDED) {
                    System.out.println("Pagina web caricata correttamente!");
                    mapInitialized = true;  // Set the flag

                    //Inizializza il comportamento per l'alert di javascript SOLO quando la mappa è caricata
                    webEngine.setOnAlert(event -> {
                        String data = event.getData();
                        String[] parts = data.split("\\|");

                        if (parts.length == 3) {
                            String address = parts[0];
                            double lat = Double.parseDouble(parts[1]);
                            double lng = Double.parseDouble(parts[2]);
                            Platform.runLater(() -> {
                                indirizzoTextField.setText(address);
                                mapView.setVisible(false);
                                this.latitudine = lat;
                                this.longitudine = lng;
                                logger.info("Latitudine: {}, Longitudine: {}", this.latitudine, this.longitudine); //Verifica

                                //Chiama getNearbyPlaces solo se hai latitudine e longitudine
                                if (latitudine != 0 && longitudine != 0) {
                                    getNearbyPlaces(address);
                                } else {
                                    logger.warn("Latitudine e Longitudine non valide, impossibile chiamare getNearbyPlaces");
                                }
                            });
                        } else {
                            Platform.runLater(() -> showPopup("Errore", "Formato dati ricevuto dalla mappa non valido.", ERROR_ICON));
                        }
                    });

                    // Esegui Javascript per forzare il resize dopo il caricamento
                    //String script = "if (typeof map !== 'undefined') { map.invalidateSize(); map.fitBounds([[40.8529 - 0.1, 14.2681 - 0.1], [40.8529 + 0.1, 14.2681 + 0.1]]); console.log('invalidateSize() executed'); } else { console.error('map is undefined'); }";
                    //webEngine.executeScript(script);

                } else if (newValue == Worker.State.FAILED) {
                    System.err.println("Errore durante il caricamento della pagina web: " + webEngine.getLoadWorker().getMessage());
                }
            });

            // Abilita la comunicazione tra Java e JavaScript
            webEngine.setJavaScriptEnabled(true);

            // Carica la pagina HTML che contiene la mappa
            webEngine.load(getClass().getResource("/com/dietiestates25ui/view/map.html").toExternalForm());


        } else {
            logger.error("WebView mapView is null. Check FXML file.");
        }
    }

    private void populateMap(String address) {
        if (mapView != null && mapView.isVisible() && mapInitialized && address != null && !address.isEmpty()) {
            WebEngine webEngine = mapView.getEngine();  //Ottieni l'istanza locale

            if(webEngine != null) { //Verifica che webEngine non sia NULL prima di chiamare executeScript
                String script = "geocodeAddress('" + address + "');";  // Richiama la funzione Javascript
                Platform.runLater(() -> webEngine.executeScript(script));
            }
        }
    }

    private void setupChoiceBox() {
        tipologiaChoiceBox.setItems(FXCollections.observableArrayList("Affitto", "Vendita"));
        tipologiaChoiceBox.setValue("Affitto");
    }

    private void setupSpinners() {
        camereSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 1));
        bagniSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));
        pianoSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-6, 50, 0));
    }

    private void handleImageSelection() {
    }

    private void updateImageThumbnails() {
    }

    private void openGestioneImmobiliPage() {
    }

    private void updateAvantiButtonState() {
    }

    private void checkFormValidity() {
    }

    private void getCoordinatesFromAddress(String address) {
    }

    private void parseCoordinates(String responseBody) {
    }
    private void getNearbyPlaces(String address) {
        logger.info("Chiamata a getNearbyPlaces per l'indirizzo: {}", address);
        if (address == null || address.isEmpty()) {
            logger.warn("Indirizzo vuoto o nullo. Impossibile chiamare l'API Geoapify.");
            return;
        }

        if(latitudine == 0 || longitudine == 0){
            logger.warn("Latitudine o Longitudine non settate a valori diversi da 0. Impossibile chiamare l'API Geoapify.");
            return;
        }
        String apiKey = "7c2573a1f65d4a23b59a0382d7f623ac"; // Replace with your actual API key
        String encodedAddress = address.replace(" ", "%20");
        String url = "https://api.geoapify.com/v2/places?categories=education.school,leisure.park,public_transport&filter=circle:" +
                longitudine + "," + latitudine + ",1000&limit=3&apiKey=" + apiKey;

        logger.info("URL chiamata API Geoapify: {}", url); // Stampa l'URL completo

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseBody -> {
                    logger.info("Risposta completa dall'API Geoapify: {}", responseBody); // Stampa la risposta completa

                    boolean hasSchool = false;
                    boolean hasPark = false;
                    boolean hasPublicTransport = false;

                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(responseBody);

                        if (root.has("features") && root.get("features").isArray()) {
                            for (JsonNode feature : root.get("features")) {
                                JsonNode properties = feature.get("properties");
                                if (properties != null && properties.has("categories")) {
                                    JsonNode categories = properties.get("categories");
                                    for (JsonNode categoryNode : categories) {
                                        String category = categoryNode.asText();
                                        switch (category) {
                                            case "education.school":
                                                hasSchool = true;
                                                break;
                                            case "leisure.park":
                                                hasPark = true;
                                                break;
                                            case "public_transport":
                                                hasPublicTransport = true;
                                                break;
                                        }
                                    }
                                }
                            }
                        } else {
                            logger.warn("Nessun 'features' trovato nella risposta API o 'features' non è un array.");
                        }
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            showPopup("Errore", "Errore durante l'elaborazione della risposta dell'API di Geoapify: " + e.getMessage(), ERROR_ICON);
                            logger.error("Errore durante l'elaborazione della risposta dell'API di Geoapify: {}", e.getMessage());
                        });
                        return; // Esci dalla lambda in caso di errore
                    }

                    final boolean finalHasSchool = hasSchool;
                    final boolean finalHasPark = hasPark;
                    final boolean finalHasPublicTransport = hasPublicTransport;

                    Platform.runLater(() -> {
                        vicinoScuoleCheckBox.setSelected(finalHasSchool);
                        vicinoParchiCheckBox.setSelected(finalHasPark);
                        vicinoTrasportoPubblicoCheckBox.setSelected(finalHasPublicTransport);
                    });

                    logger.info("Vicino a scuole: {}, Vicino a parchi: {}, Vicino a trasporto pubblico: {}", hasSchool, hasPark, hasPublicTransport);
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        showPopup("Errore", "Errore durante la chiamata all'API di Geoapify: " + e.getMessage(), ERROR_ICON);
                        logger.error("Errore durante la chiamata all'API di Geoapify: {}", e.getMessage());
                    });
                    return null;
                });
    }

    @FXML
    private void handleAvantiButtonAction(javafx.event.ActionEvent event) {
        openConfermaInserzionePage();
    }

    private void openConfermaInserzionePage() {
        Immobile Immobile = new Immobile();
        Immobile.setTitolo(titoloTextField.getText());
        Immobile.setTipologia(tipologiaChoiceBox.getValue());
        Immobile.setIndirizzo(indirizzoTextField.getText());
        Immobile.setPrezzo(Double.parseDouble(prezzoTextField.getText()));
        Immobile.setDescrizione(descrizioneTextArea.getText());
        Immobile.setDimensione(Double.parseDouble(superficieTextField.getText()));
        Immobile.setNumero_camere(camereSpinner.getValue());
        Immobile.setNumero_bagni(bagniSpinner.getValue());
        Immobile.setClasseEnergetica(classeEnergeticaTextField.getText());
        Immobile.setPiano(pianoSpinner.getValue());
        Immobile.setAscensore(ascensoreCheckBox.isSelected());
        Immobile.setPortineria(portineriaCheckBox.isSelected());
        Immobile.setClimatizzazione(climatizzazioneCheckBox.isSelected());
        List<String> imageUrls = selectedImageList.stream().map(file -> file.toURI().toString()).toList();
        Immobile.setImmaginiUrls(imageUrls);
        Immobile.setLatitudine(latitudine);
        Immobile.setLongitudine(longitudine);
        Immobile.setVicinoScuole(vicinoScuoleCheckBox.isSelected());
        Immobile.setVicinoParchi(vicinoParchiCheckBox.isSelected());
        Immobile.setVicinoTrasportoPubblico(vicinoTrasportoPubblicoCheckBox.isSelected());

        loadScene("/com/dietiestates25ui/view/conferma-inserzione-view.fxml",
                (fxmlLoader, stage) -> {
                    ConfermaInserzioneController controller = fxmlLoader.getController();
                    controller.setImmobile(Immobile);
                }, avantiButton, "/com/dietiestates25ui/styles/conferma-inserzione-style.css");
    }
}