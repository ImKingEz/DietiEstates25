package com.dietiestates25ui.controller;

import com.dietiestates25ui.model.Annuncio;
import com.dietiestates25ui.model.Immobile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
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
import java.util.Locale;
import java.util.ResourceBundle;

public class InserimentoInserzioneController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(InserimentoInserzioneController.class);

    private static final int GEOAPIFY_RADIUS = 500;
    public static final String FEATURES_GEOAPIFY = "features";
    public static final String CATEGORIES_GEOAPIFY = "categories";

    @FXML
    private TextField titoloTextField;
    @FXML
    private MenuButton tipoMenuButton;
    @FXML
    private MenuItem venditaMenuItem;
    @FXML
    private MenuItem affittoMenuItem;
    @FXML
    private MenuButton tipologiaMenuButton;
    @FXML
    private MenuItem villaMenuItem;
    @FXML
    private MenuItem appartamentoMenuItem;
    @FXML
    private MenuItem terrenoMenuItem;
    @FXML
    private MenuItem casaIndipendenteMenuItem;
    @FXML
    private TextField indirizzoTextField;
    @FXML
    private Button apriMappaButton;
    @FXML
    private WebView mapView;
    @FXML
    private Button mapBackButton;
    @FXML
    private TextField prezzoTextField;
    @FXML
    private TextArea descrizioneTextArea;
    @FXML
    private Button selezionaImmaginiButton;
    @FXML
    private FlowPane immaginiFlowPane;
    @FXML
    private TextField superficieTextField;
    @FXML
    private Spinner<Integer> camereSpinner;
    @FXML
    private Spinner<Integer> bagniSpinner;
    @FXML
    private TextField classeEnergeticaTextField;
    @FXML
    private Spinner<Integer> pianoSpinner;
    @FXML
    private CheckBox ascensoreCheckBox;
    @FXML
    private CheckBox portineriaCheckBox;
    @FXML
    private CheckBox climatizzazioneCheckBox;
    @FXML
    private Button avantiButton;
    @FXML
    private Button indietroButton;
    @FXML
    private CheckBox vicinoScuoleCheckBox;
    @FXML
    private CheckBox vicinoParchiCheckBox;
    @FXML
    private CheckBox vicinoTrasportoPubblicoCheckBox;

    private final ObservableList<File> selectedImageList = FXCollections.observableArrayList();

    private double latitudine;
    private double longitudine;

    private String token;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //focus sul logo

        apriMappaButton.setOnAction(this::handleApriMappaButtonAction);

        indietroButton.setOnAction(event -> openGestioneImmobiliPage());

        mapBackButton.setOnAction(event -> hideMapView());

        setupTipoMenuButton();
        setupTipologiaMenuButton();
        setupSpinners();

        selezionaImmaginiButton.setOnAction(event -> Platform.runLater(this::handleImageSelection));

        updateAvantiButtonState();

        indirizzoTextField.textProperty().addListener((observable, oldValue, newValue) -> {});

        createAndPlaceBackButton();
    }

    private void setupTipoMenuButton() {
        venditaMenuItem.setOnAction(event -> impostaTipo("Vendita"));
        affittoMenuItem.setOnAction(event -> impostaTipo("Affitto"));
    }

    private void impostaTipo(String tipo) {
        tipoMenuButton.setText(tipo);
        tipoMenuButton.setStyle("-fx-text-fill: black;");
    }

    private void setupTipologiaMenuButton() {
        villaMenuItem.setOnAction(event -> impostaTipologia("Villa"));
        appartamentoMenuItem.setOnAction(event -> impostaTipologia("Appartamento"));
        casaIndipendenteMenuItem.setOnAction(event -> impostaTipologia("Casa indipendente"));
        terrenoMenuItem.setOnAction(event -> impostaTipologia("Terreno"));
    }

    private void impostaTipologia(String tipo) {
        tipologiaMenuButton.setText(tipo);
        tipologiaMenuButton.setStyle("-fx-text-fill: black;");
    }

    public void setTitoloTextField(String titolo) {
        this.titoloTextField.setText(titolo);
    }

    public void setIndirizzoTextField(String indirizzo) {
        this.indirizzoTextField.setText(indirizzo);
    }

    public void setPrezzoTextField(String prezzo) {
        this.prezzoTextField.setText(prezzo);
    }

    public void setDescrizioneTextArea(String descrizione) {
        this.descrizioneTextArea.setText(descrizione);
    }

    public void setSuperficieTextField(String superficie) {
        this.superficieTextField.setText(superficie);
    }

    public void setCamereSpinner(int camere) {
        this.camereSpinner.getValueFactory().setValue(camere);
    }

    public void setBagniSpinner(int bagni) {
        this.bagniSpinner.getValueFactory().setValue(bagni);
    }

    public void setClasseEnergeticaTextField(String classeEnergetica) {
        this.classeEnergeticaTextField.setText(classeEnergetica);
    }

    public void setPianoSpinner(int piano) {
        this.pianoSpinner.getValueFactory().setValue(piano);
    }

    public void setTipoMenuButton(String tipologia) {
        this.tipoMenuButton.setText(tipologia);
    }

    public void setTipologiaMenuButton(String tipologia) {
        this.tipologiaMenuButton.setText(tipologia);
    }

    public void setAscensoreCheckBox(boolean ascensore) {
        this.ascensoreCheckBox.setSelected(ascensore);
    }

    public void setPortineriaCheckBox(boolean portineria) {
        this.portineriaCheckBox.setSelected(portineria);
    }

    public void setClimatizzazioneCheckBox(boolean climatizzazione) {
        this.climatizzazioneCheckBox.setSelected(climatizzazione);
    }

    public void setSelectedImageList(List<String> imageUrls) {
        this.selectedImageList.clear();
        for (String imageUrl : imageUrls) {
            try {
                File file = new File(new URL(imageUrl).toURI());
                this.selectedImageList.add(file);
            } catch (Exception e) {
                logger.error("Errore durante la conversione dell'URL in File: {}", e.getMessage());
            }
        }
        updateImageThumbnails();
    }

    public void setVicinoScuoleCheckBox(boolean vicinoScuole) {
        this.vicinoScuoleCheckBox.setSelected(vicinoScuole);
    }

    public void setVicinoParchiCheckBox(boolean vicinoParchi) {
        this.vicinoParchiCheckBox.setSelected(vicinoParchi);
    }

    public void setVicinoTrasportoPubblicoCheckBox(boolean vicinoTrasportoPubblico) {
        this.vicinoTrasportoPubblicoCheckBox.setSelected(vicinoTrasportoPubblico);
    }

    public void setToken(String token) {
        this.token = token;
    }

    @FXML
    private void handleApriMappaButtonAction(javafx.event.ActionEvent event) {
        try {
            mapView.setPrefWidth(primaryAnchorPane.getWidth());
            mapView.setPrefHeight(primaryAnchorPane.getHeight());

            mapView.setVisible(true);
            mapBackButton.setVisible(true);

            primaryAnchorPane.applyCss();
            primaryAnchorPane.layout();

            logger.info("prima di initializeMap");
            initializeMap();
        } catch (Exception e) {
            logger.error("Errore nel metodo handleApriMappaButtonAction: {}", e.getMessage(), e);
            showPopup(POPUP_ERROR_TITLE, "Errore durante l'apertura della mappa: " + e.getMessage(), ERROR_ICON);
        }
    }

    @FXML
    private void hideMapView() {
        mapView.setVisible(false);
        mapBackButton.setVisible(false);
    }

    private void initializeMap() {
        logger.info("Inizio inizializzazione mappa.");

        if (mapView == null) {
            logger.error("WebView mapView è null. Controllare il file FXML.");
            return; // Esci dal metodo se mapView è null
        }

        WebEngine webEngine = mapView.getEngine();

        // Listener per lo stato di caricamento della pagina web
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> handleWebEngineStateChange(newValue, webEngine));

        webEngine.setJavaScriptEnabled(true);
        webEngine.load(getClass().getResource("/com/dietiestates25ui/view/map.html").toExternalForm());
        logger.info("Caricamento pagina mappa: /com/dietiestates25ui/view/map.html");
    }

    private void handleWebEngineStateChange(Worker.State newValue, WebEngine webEngine) {
        if (newValue == Worker.State.SUCCEEDED) {
            webEngine.setOnAlert(event -> handleMapAlert(event.getData()));
        } else if (newValue == Worker.State.FAILED) {
            Platform.runLater(() -> showPopup(POPUP_ERROR_TITLE, "Errore durante il caricamento della mappa: " + webEngine.getLoadWorker().getMessage(), ERROR_ICON));
        }
    }

    private void handleMapAlert(String data) {
        try {
            String[] parts = data.split("\\|");

            if (parts.length != 3) {
                logger.warn("Formato dati ricevuto dalla mappa non valido: {}", data);
                Platform.runLater(() -> showPopup(POPUP_ERROR_TITLE, "Formato dati ricevuto dalla mappa non valido.", ERROR_ICON));
                return;
            }

            String address = parts[0];
            double lat = Double.parseDouble(parts[1]);
            double lng = Double.parseDouble(parts[2]);

            if (address.split("\\s+").length <= 1) {
                logger.warn("L'indirizzo restituito non è valido: {}", address);
                Platform.runLater(() -> {
                    showPopup(POPUP_ERROR_TITLE, "L'indirizzo restituito non è valido", ERROR_ICON);
                    hideMapView();
                });
                return;
            }

            Platform.runLater(() -> processMapData(address, lat, lng));

        } catch (NumberFormatException e) {
            logger.error("Errore durante la conversione dei dati numerici ricevuti dalla mappa: {}", e.getMessage());
            Platform.runLater(() -> showPopup(POPUP_ERROR_TITLE, "Errore durante l'elaborazione dei dati ricevuti dalla mappa: Formato numerico non valido.", ERROR_ICON));

        } catch (Exception e) {
            logger.error("Errore durante l'elaborazione dei dati ricevuti dalla mappa: {}", e.getMessage(), e);
            Platform.runLater(() -> showPopup(POPUP_ERROR_TITLE, "Errore durante l'elaborazione dei dati ricevuti dalla mappa: " + e.getMessage(), ERROR_ICON));
        }
    }

    private void processMapData(String address, double lat, double lng) {
        if (address.equals("Indirizzo non identificato per questo punto") || address.equals("Errore di connessione")) {
            logger.warn("Errore ricevuto dalla mappa: {}", address);
            showPopup(POPUP_ERROR_TITLE, address, ERROR_ICON);
            hideMapView();
        } else {
            indirizzoTextField.setText(address);
            hideMapView();
            this.latitudine = lat;
            this.longitudine = lng;

            // Chiama getNearbyPlaces *qui*, dopo aver impostato l'indirizzo e le coordinate
            getNearbyPlaces(lat,lng);

            logo.requestFocus();
        }
    }

    private void setupSpinners() {
        camereSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 1));
        bagniSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));
        pianoSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-6, 50, 0));
    }

    private void handleImageSelection() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona Immagini");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Immagini", "*.png", "*.jpg", "*.jpeg"));

        List<File> newFiles = fileChooser.showOpenMultipleDialog(currentStage);

        if (newFiles != null && !newFiles.isEmpty()) {
            selectedImageList.addAll(newFiles);

            if (selectedImageList.size() > 5) {
                Platform.runLater(() -> showPopup(POPUP_ERROR_TITLE, "Puoi selezionare al massimo 5 immagini.", ERROR_ICON));

                while (selectedImageList.size() > 5) {
                    selectedImageList.remove(selectedImageList.size() - 1);
                }
            }

            updateImageThumbnails();
        }
        checkFormValidity();
    }

    private void updateImageThumbnails() {
        immaginiFlowPane.getChildren().clear();

        double fixedImageSize = 50;

        for (File file : selectedImageList) {
            try {
                Image image = new Image(file.toURI().toString());
                ImageView imageView = new ImageView(image);

                imageView.setFitWidth(fixedImageSize);
                imageView.setFitHeight(fixedImageSize);
                imageView.setPreserveRatio(true);

                Button deleteButton = new Button("X");
                deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-padding: 0px; -fx-font-size: 8px;");

                deleteButton.setOnAction(e -> {
                    logo.requestFocus();
                    selectedImageList.remove(file);
                    immaginiFlowPane.getChildren().remove(imageView.getParent());
                    checkFormValidity();
                });

                VBox imageContainer = new VBox(imageView, deleteButton);
                imageContainer.setAlignment(Pos.CENTER);

                immaginiFlowPane.getChildren().add(imageContainer);
            } catch (Exception e) {
                logger.error("Error loading image", e);
            }
        }
    }

    private void openGestioneImmobiliPage() {
        loadScene("/com/dietiestates25ui/view/gestione-immobili-view.fxml",
                (fxmlLoader, stage) -> {}, indietroButton, "/com/dietiestates25ui/styles/gestione-immobili-style.css");
    }

    private void updateAvantiButtonState() {
        titoloTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        indirizzoTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        prezzoTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        superficieTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        classeEnergeticaTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        descrizioneTextArea.textProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());

        tipoMenuButton.textProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        tipologiaMenuButton.textProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        camereSpinner.valueProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        bagniSpinner.valueProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        pianoSpinner.valueProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());

        selezionaImmaginiButton.disableProperty().bind(Bindings.size(selectedImageList).greaterThanOrEqualTo(5));

        avantiButton.setDisable(true);
    }

    public void checkFormValidity() {
        String titolo = titoloTextField.getText().trim();
        String indirizzo = indirizzoTextField.getText().trim();
        String prezzo = prezzoTextField.getText().trim();
        String superficie = superficieTextField.getText().trim();
        String classeEnergetica = classeEnergeticaTextField.getText().trim();
        String descrizione = descrizioneTextArea.getText().trim();
        String tipo = tipoMenuButton.getText();
        String tipologia = tipologiaMenuButton.getText();

        boolean isPrezzoValid = false;
        try {
            Double.parseDouble(prezzo);
            isPrezzoValid = true;
        } catch (NumberFormatException e) {
            // Prezzo non valido
        }

        boolean isSuperficieValid = false;
        try {
            Double.parseDouble(superficie);
            isSuperficieValid = true;
        } catch (NumberFormatException e) {
            // Superficie non valida
        }

        boolean requiredFieldsFilled = !titolo.isEmpty() && !indirizzo.isEmpty() && !descrizione.isEmpty() && isPrezzoValid && isSuperficieValid &&
                !classeEnergetica.isEmpty() && (!selectedImageList.isEmpty()) && (tipo.equals("Vendita") || tipo.equals("Affitto")) &&
                (tipologia.equals("Villa") || tipologia.equals("Appartamento") || tipologia.equals("Terreno") || tipologia.equals("Casa indipendente"));
        boolean isMaxImagesSelected = selectedImageList.size() <= 5;

        avantiButton.setDisable(!requiredFieldsFilled || !isMaxImagesSelected);
    }

    private void getNearbyPlaces(double latitude, double longitude) {
        logger.info("getNearbyPlaces chiamato con Latitudine: {}, Longitudine: {}", latitude, longitude);

        if (!isValidCoordinates(latitude, longitude)) {
            logger.warn("Latitudine o Longitudine non valide. Impossibile chiamare l'API Geoapify.");
            return;
        }

        String apiKey = "7c2573a1f65d4a23b59a0382d7f623ac";
        String url = buildGeoapifyUrl(longitude, latitude, apiKey);

        logger.info("URL chiamata API Geoapify: {}", url);

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(this::processGeoapifyResponse)
                    .exceptionally(this::handleGeoapifyError);
        }
    }

    private boolean isValidCoordinates(double latitude, double longitude) {
        return latitude != 0 && longitude != 0;
    }

    private String buildGeoapifyUrl(double longitude, double latitude, String apiKey) {
        String url = String.format(Locale.US, "https://api.geoapify.com/v2/places?categories=education.school,leisure.park,public_transport&filter=circle:%f,%f,%d&limit=3&apiKey=%s",
                longitude, latitude, GEOAPIFY_RADIUS, apiKey);
        logger.info("URL formattato: {}", url); // Aggiungi questo log
        return url;
    }

    private void processGeoapifyResponse(String responseBody) {
        logger.info("Risposta completa dall'API Geoapify: {}", responseBody);

        boolean hasSchool = false;
        boolean hasPark = false;
        boolean hasPublicTransport = false;

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);

            if (root.has(FEATURES_GEOAPIFY) && root.get(FEATURES_GEOAPIFY).isArray()) {
                for (JsonNode feature : root.get(FEATURES_GEOAPIFY)) {
                    JsonNode properties = feature.get("properties");
                    if (properties != null && properties.has(CATEGORIES_GEOAPIFY)) {
                        hasSchool |= containsCategory(properties.get(CATEGORIES_GEOAPIFY), "education.school");
                        hasPark |= containsCategory(properties.get(CATEGORIES_GEOAPIFY), "leisure.park");
                        hasPublicTransport |= containsCategory(properties.get(CATEGORIES_GEOAPIFY), "public_transport");
                    }
                }
            } else {
                logger.warn("Nessun 'features' trovato nella risposta API o 'features' non è un array.");
            }
        } catch (IOException e) {
            Platform.runLater(() -> {
                showPopup(POPUP_ERROR_TITLE, "Errore durante l'elaborazione della risposta dell'API di Geoapify: " + e.getMessage(), ERROR_ICON);
                logger.error("Errore durante l'elaborazione della risposta dell'API di Geoapify: {}", e.getMessage());
            });
            return;
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
    }

    private boolean containsCategory(JsonNode categories, String categoryToFind) {
        if (categories == null || !categories.isArray()) {
            return false;
        }
        for (JsonNode categoryNode : categories) {
            if (categoryToFind.equals(categoryNode.asText())) {
                return true;
            }
        }
        return false;
    }

    private Void handleGeoapifyError(Throwable e) {
        Platform.runLater(() -> {
            showPopup(POPUP_ERROR_TITLE, "Errore durante la chiamata all'API di Geoapify: " + e.getMessage(), ERROR_ICON);
            logger.error("Errore durante la chiamata all'API di Geoapify: {}", e.getMessage());
        });
        return null;
    }

    @FXML
    private void handleAvantiButtonAction(javafx.event.ActionEvent event) {
        openConfermaInserzionePage();
    }

    private void openConfermaInserzionePage() {
        Immobile immobile = new Immobile();
        immobile.setTipologia(tipologiaMenuButton.getText());
        immobile.setIndirizzo(indirizzoTextField.getText());
        immobile.setDimensione(Double.parseDouble(superficieTextField.getText()));
        immobile.setNumeroLocali(camereSpinner.getValue());
        immobile.setNumeroBagni(bagniSpinner.getValue());
        immobile.setClasseEnergetica(classeEnergeticaTextField.getText());
        immobile.setPiano(pianoSpinner.getValue());
        immobile.setAscensore(ascensoreCheckBox.isSelected());
        immobile.setPortineria(portineriaCheckBox.isSelected());
        immobile.setClimatizzazione(climatizzazioneCheckBox.isSelected());
        immobile.setLatitudine(latitudine);
        immobile.setLongitudine(longitudine);
        immobile.setVicinoScuole(vicinoScuoleCheckBox.isSelected());
        immobile.setVicinoParchi(vicinoParchiCheckBox.isSelected());
        immobile.setVicinoTrasportoPubblico(vicinoTrasportoPubblicoCheckBox.isSelected());

        Annuncio annuncio = new Annuncio();
        annuncio.setTitolo(titoloTextField.getText());
        annuncio.setPrezzo(Double.parseDouble(prezzoTextField.getText()));
        annuncio.setDescrizione(descrizioneTextArea.getText());
        annuncio.setTipo(tipoMenuButton.getText());
        List<String> imageUrls = selectedImageList.stream().map(file -> file.toURI().toString()).toList();
        annuncio.setImmaginiUrls(imageUrls);

        loadScene("/com/dietiestates25ui/view/conferma-inserzione-view.fxml",
                (fxmlLoader, stage) -> {
                    ConfermaInserzioneController controller = fxmlLoader.getController();
                    controller.setImmobile(immobile);
                    controller.setAnnuncio(annuncio);
                    controller.setSelectedImageList(selectedImageList);
                    controller.setToken(token);
                    controller.setStage(stage);
                }, avantiButton, "/com/dietiestates25ui/styles/conferma-inserzione-style.css");
    }
}