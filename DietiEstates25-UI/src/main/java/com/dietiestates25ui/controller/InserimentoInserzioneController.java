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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.dietiestates25ui.handler.FormValidator.setupTextFormatter;

public class InserimentoInserzioneController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(InserimentoInserzioneController.class);

    private static final int GEOAPIFY_RADIUS = 500;
    public static final String FEATURES_GEOAPIFY = "features";
    public static final String CATEGORIES_GEOAPIFY = "categories";
    public static final String PIANO_TERRA_ITEM = "Piano terra";
    public static final String PIANO_INTERMEDIO_ITEM = "Piano intermedio";
    public static final String ULTIMO_PIANO_ITEM = "Ultimo piano";
    public static final String FX_TEXT_FILL_MENU_BUTTON = "-fx-text-fill: black;";

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
    private MenuButton classeEnergeticaMenuButton;
    @FXML
    private MenuItem a4MenuItem;
    @FXML
    private MenuItem a3MenuItem;
    @FXML
    private MenuItem a2MenuItem;
    @FXML
    private MenuItem a1MenuItem;
    @FXML
    private MenuItem bMenuItem;
    @FXML
    private MenuItem cMenuItem;
    @FXML
    private MenuItem dMenuItem;
    @FXML
    private MenuItem eMenuItem;
    @FXML
    private MenuItem fMenuItem;
    @FXML
    private MenuItem gMenuItem;
    @FXML
    private MenuButton pianoMenuButton;
    @FXML
    private MenuItem pianoTerraMenuItem;
    @FXML
    private MenuItem pianoIntermedioMenuItem;
    @FXML
    private MenuItem ultimoPianoMenuItem;
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
    private String citta;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logo.requestFocus();

        apriMappaButton.setOnAction(this::handleApriMappaButtonAction);

        indietroButton.setOnAction(event -> openGestioneImmobiliPage());

        mapBackButton.setOnAction(event -> hideMapView());

        setupTipoMenuButton();

        setupTipologiaMenuButton();

        setupClasseEnergeticaMenuButton();

        setupPianoMenuButton();

        setupSpinners();

        selezionaImmaginiButton.setOnAction(event -> Platform.runLater(this::handleImageSelection));

        updateAvantiButtonState();

        indirizzoTextField.textProperty().addListener((observable, oldValue, newValue) -> {});

        createAndPlaceBackButton();
    }

    private void setupPianoMenuButton() {
        pianoTerraMenuItem.setOnAction(event -> impostaPiano(PIANO_TERRA_ITEM));
        pianoIntermedioMenuItem.setOnAction(event -> impostaPiano(PIANO_INTERMEDIO_ITEM));
        ultimoPianoMenuItem.setOnAction(event -> impostaPiano(ULTIMO_PIANO_ITEM));
    }

    private void impostaPiano(String piano) {
        pianoMenuButton.setText(piano);
        pianoMenuButton.setStyle(FX_TEXT_FILL_MENU_BUTTON);
    }

    private void setupTipoMenuButton() {
        venditaMenuItem.setOnAction(event -> impostaTipo("Vendita"));
        affittoMenuItem.setOnAction(event -> impostaTipo("Affitto"));
    }

    private void impostaTipo(String tipo) {
        tipoMenuButton.setText(tipo);
        tipoMenuButton.setStyle(FX_TEXT_FILL_MENU_BUTTON);
    }

    private void setupTipologiaMenuButton() {
        villaMenuItem.setOnAction(event -> impostaTipologia("Villa"));
        appartamentoMenuItem.setOnAction(event -> impostaTipologia("Appartamento"));
        casaIndipendenteMenuItem.setOnAction(event -> impostaTipologia("Casa indipendente"));
        terrenoMenuItem.setOnAction(event -> impostaTipologia("Terreno"));
    }

    private void impostaTipologia(String tipo) {
        tipologiaMenuButton.setText(tipo);
        tipologiaMenuButton.setStyle(FX_TEXT_FILL_MENU_BUTTON);
    }

    private void setupClasseEnergeticaMenuButton() {
        a4MenuItem.setOnAction(event -> impostaClasseEnergetica("A4"));
        a3MenuItem.setOnAction(event -> impostaClasseEnergetica("A3"));
        a2MenuItem.setOnAction(event -> impostaClasseEnergetica("A2"));
        a1MenuItem.setOnAction(event -> impostaClasseEnergetica("A1"));
        bMenuItem.setOnAction(event -> impostaClasseEnergetica("B"));
        cMenuItem.setOnAction(event -> impostaClasseEnergetica("C"));
        dMenuItem.setOnAction(event -> impostaClasseEnergetica("D"));
        eMenuItem.setOnAction(event -> impostaClasseEnergetica("E"));
        fMenuItem.setOnAction(event -> impostaClasseEnergetica("F"));
        gMenuItem.setOnAction(event -> impostaClasseEnergetica("G"));
    }

    private void impostaClasseEnergetica(String classeEnergetica) {
        classeEnergeticaMenuButton.setText(classeEnergetica);
        classeEnergeticaMenuButton.setStyle(FX_TEXT_FILL_MENU_BUTTON);
    }

    public void setCitta(String citta) {
        this.citta = citta;
    }

    public void setLatitudine(double latitudine) {
        this.latitudine = latitudine;
    }

    public void setLongitudine(double longitudine) {
        this.longitudine = longitudine;
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

    public void setPianoMenubutton(String piano) {
        this.pianoMenuButton.setText(piano);
    }

    public void setClasseEnergeticaMenuButton(String classeEnergetica) {
        this.classeEnergeticaMenuButton.setText(classeEnergetica);
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
                URI uri = URI.create(imageUrl);
                Path path = Paths.get(uri);
                File file = path.toFile();
                this.selectedImageList.add(file);
            } catch (IllegalArgumentException e) {
                logger.error("URL non valido: {}", imageUrl, e);
            } catch (Exception e) {
                logger.error("Errore durante la conversione dell'URL in File: {}", e.getMessage(), e);
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

    @FXML
    private void handleApriMappaButtonAction(ActionEvent event) {
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
            return;
        }

        WebEngine webEngine = mapView.getEngine();

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
            showLoadingIndicator();

            indirizzoTextField.setText(address);
            hideMapView();
            this.latitudine = lat;
            this.longitudine = lng;

            CompletableFuture<Void> cityFuture = CompletableFuture.runAsync(() -> getCityFromAddress(address, lat, lng));
            CompletableFuture<Void> featuresFuture = CompletableFuture.runAsync(() -> findNearbyFeatures(lat, lng));
            CompletableFuture.allOf(cityFuture, featuresFuture).thenRun(() -> Platform.runLater(this::hideLoadingIndicator));

            logo.requestFocus();
        }
    }


    private void setupSpinners() {
        camereSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 1));
        bagniSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));
    }

    private void handleImageSelection() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona Immagini");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Immagini", "*.png", "*.jpg", "*.jpeg"));

        List<File> originalFiles = fileChooser.showOpenMultipleDialog(currentStage);

        if (originalFiles != null && !originalFiles.isEmpty()) {
            List<File> newFiles = new ArrayList<>(originalFiles);

            for (File file : originalFiles) {
                if (!validateImage(file)) {
                    newFiles.remove(file);
                }
            }
            selectedImageList.addAll(newFiles);

            if (selectedImageList.size() > 5) {
                Platform.runLater(() -> showPopup(POPUP_ERROR_TITLE, "Puoi selezionare al massimo 5 immagini.", ERROR_ICON));

                while (selectedImageList.size() > 5) {
                    selectedImageList.removeLast();
                }
            }

            updateImageThumbnails();
        }
        checkFormValidity();
    }

    private boolean validateImage(File file) {
        if (file.length() > MAX_FILE_SIZE) {
            Platform.runLater(() -> showPopup(POPUP_ERROR_TITLE, "La dimensione del file è troppo grande (max 2MB).", ERROR_ICON));
            return false;
        }
        return true;
    }

    private void updateImageThumbnails() {
        immaginiFlowPane.getChildren().clear();

        double fixedImageSize = 50;

        for (File file : selectedImageList) {
            Image image = new Image(file.toURI().toString());
            ImageView imageView = new ImageView(image);

            imageView.setFitWidth(fixedImageSize);
            imageView.setFitHeight(fixedImageSize);
            imageView.setPreserveRatio(true);

            Image closeIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/dietiestates25ui/images/erroricon.png")));
            ImageView closeIconView = new ImageView(closeIcon);
            closeIconView.setFitWidth(20);
            closeIconView.setFitHeight(20);

            Button deleteButton = new Button();
            deleteButton.setGraphic(closeIconView);

            deleteButton.setStyle("-fx-padding: 0px; -fx-background-color: transparent;");
            deleteButton.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

            deleteButton.setOnAction(e -> {
                logo.requestFocus();
                selectedImageList.remove(file);
                immaginiFlowPane.getChildren().remove(imageView.getParent());
                checkFormValidity();
            });

            VBox imageContainer = new VBox(imageView, deleteButton);
            imageContainer.setAlignment(Pos.CENTER);

            immaginiFlowPane.getChildren().add(imageContainer);
        }
    }

    private void updateAvantiButtonState() {
        titoloTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        indirizzoTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        setupTextFormatter(prezzoTextField);
        prezzoTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        setupTextFormatter(superficieTextField);
        superficieTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        classeEnergeticaMenuButton.textProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        descrizioneTextArea.textProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());

        tipoMenuButton.textProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        tipologiaMenuButton.textProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        camereSpinner.valueProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        bagniSpinner.valueProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        pianoMenuButton.textProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());

        selezionaImmaginiButton.disableProperty().bind(Bindings.size(selectedImageList).greaterThanOrEqualTo(5));

        avantiButton.setDisable(true);
    }

    public void checkFormValidity() {
        String titolo = titoloTextField.getText().trim();
        String indirizzo = indirizzoTextField.getText().trim();
        String prezzo = prezzoTextField.getText().trim();
        String superficie = superficieTextField.getText().trim();
        String classeEnergetica = classeEnergeticaMenuButton.getText();
        String descrizione = descrizioneTextArea.getText().trim();
        String tipo = tipoMenuButton.getText();
        String tipologia = tipologiaMenuButton.getText();
        String piano = pianoMenuButton.getText();

        boolean isPrezzoValid = false;
        double prezzoValue = 0;
        try {
            prezzoValue = Double.parseDouble(prezzo);
            isPrezzoValid = prezzoValue > 0;
        } catch (NumberFormatException e) {
            logger.error("Prezzo non valido: {}", e.getMessage());
        }

        boolean isSuperficieValid = false;
        double superficieValue = 0;
        try {
            superficieValue = Double.parseDouble(superficie);
            isSuperficieValid = superficieValue > 0;
        } catch (NumberFormatException e) {
            logger.error("Superficie non valida: {}", e.getMessage());
        }

        boolean isPianoValid = piano.equals(PIANO_TERRA_ITEM) || piano.equals(PIANO_INTERMEDIO_ITEM) || piano.equals(ULTIMO_PIANO_ITEM);

        boolean isClasseEnergeticaValid = classeEnergetica.equals("A4") || classeEnergetica.equals("A3") || classeEnergetica.equals("A2") ||
                classeEnergetica.equals("A1") || classeEnergetica.equals("B") || classeEnergetica.equals("C") || classeEnergetica.equals("D") ||
                classeEnergetica.equals("E") || classeEnergetica.equals("F") || classeEnergetica.equals("G");

        boolean requiredFieldsFilled = !titolo.isEmpty() && !indirizzo.isEmpty() && !descrizione.isEmpty() && isPrezzoValid && isSuperficieValid &&
                isClasseEnergeticaValid && (!selectedImageList.isEmpty()) && (tipo.equals("Vendita") || tipo.equals("Affitto")) &&
                (tipologia.equals("Villa") || tipologia.equals("Appartamento") || tipologia.equals("Terreno") || tipologia.equals("Casa indipendente")) && isPianoValid;

        boolean isMaxImagesSelected = selectedImageList.size() <= 5;

        avantiButton.setDisable(!requiredFieldsFilled || !isMaxImagesSelected);
    }

    private void getCityFromAddress(String address, double latitude, double longitude) {
        logger.info("getCityFromAddress chiamato con Indirizzo: {}, Latitudine: {}, Longitudine: {}", address, latitude, longitude);

        if (!isValidCoordinates(latitude, longitude)) {
            logger.warn("Latitudine o Longitudine non valide. Impossibile chiamare l'API Geoapify.");
            return;
        }

        String apiKey = "7c2573a1f65d4a23b59a0382d7f623ac";
        String url = buildGeoapifyUrl(latitude, longitude, apiKey);

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

    private String buildGeoapifyUrl(double latitude, double longitude, String apiKey) {
        String url = String.format(Locale.US, "https://api.geoapify.com/v1/geocode/reverse?lat=%f&lon=%f&apiKey=%s&lang=it",
                latitude, longitude, apiKey);
        logger.info("URL formattato: {}", url);
        return url;
    }

    private void processGeoapifyResponse(String responseBody) {
        logger.info("Risposta completa dall'API Geoapify: {}", responseBody);

        String city = null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);

            if (root.has(FEATURES_GEOAPIFY) && root.get(FEATURES_GEOAPIFY).isArray()) {
                for (JsonNode feature : root.get(FEATURES_GEOAPIFY)) {
                    JsonNode properties = feature.get("properties");

                    if (properties != null && properties.has("city")) {
                        city = properties.get("city").asText();
                        break;
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

        final String finalCity = city;

        Platform.runLater(() -> {
            if (finalCity != null && !finalCity.isEmpty()) {
                this.citta = finalCity;
                logger.info("Città trovata: {}", finalCity);
            } else {
                this.citta = "Città non trovata";
                logger.warn("Città non trovata per l'indirizzo specificato.");
            }
        });

        logger.info("Città estratta dall'API Geoapify: {}", city);
    }

    private Void handleGeoapifyError(Throwable e) {
        Platform.runLater(() -> {
            showPopup(POPUP_ERROR_TITLE, "Errore durante la chiamata all'API di Geoapify: " + e.getMessage(), ERROR_ICON);
            logger.error("Errore durante la chiamata all'API di Geoapify: {}", e.getMessage());
        });
        return null;
    }

    private void findNearbyFeatures(double latitude, double longitude) {
        logger.info("findNearbyFeatures chiamato con Latitudine: {}, Longitudine: {}", latitude, longitude);

        if (!isValidCoordinates(latitude, longitude)) {
            logger.warn("Latitudine o Longitudine non valide. Impossibile chiamare l'API Geoapify Places.");
            return;
        }

        String apiKey = "7c2573a1f65d4a23b59a0382d7f623ac";
        String baseUrl = "https://api.geoapify.com/v2/places?";
        int radius = GEOAPIFY_RADIUS;

        String[] categories = {"education.school", "leisure.park", "public_transport"};

        for (String category : categories) {
            String url = baseUrl +
                    "categories=" + category +
                    "&filter=circle:" + longitude + "," + latitude + "," + radius +
                    "&apiKey=" + apiKey;

            logger.info("URL chiamata API Geoapify Places ({}): {}", category, url);

            final String currentCategory = category;

            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .build();

                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenApply(HttpResponse::body)
                        .thenAccept(responseBody -> processPlacesApiResponse(responseBody, currentCategory))
                        .exceptionally(e -> {
                            handleGeoapifyPlacesError(e, currentCategory);
                            return null;
                        });

            } catch (Exception e) {
                handleGeoapifyPlacesError(e, currentCategory);
            }
        }
    }

    private void processPlacesApiResponse(String responseBody, String category) {
        logger.info("Risposta API Geoapify Places ({}): {}", category, responseBody);
        boolean found = false;

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);

            if (root.has(FEATURES_GEOAPIFY) && root.get(FEATURES_GEOAPIFY).isArray() && !root.get(FEATURES_GEOAPIFY).isEmpty()) {
                found = true;
            }

        } catch (IOException e) {
            Platform.runLater(() -> {
                showPopup(POPUP_ERROR_TITLE, "Errore nell'elaborazione della risposta API Geoapify Places (" + category + "): " + e.getMessage(), ERROR_ICON);
                logger.error("Errore nell'elaborazione della risposta API Geoapify Places ({}): {}", category, e.getMessage());
            });
            return;
        }

        final boolean isFound = found;

        Platform.runLater(() -> {
            logger.info("Categoria: {}, Feature Trovata: {}", category, isFound);
            switch (category) {
                case "education.school":
                    vicinoScuoleCheckBox.setSelected(isFound);
                    logger.info("Impostando vicinoScuoleCheckBox a: {}", isFound);
                    break;
                case "leisure.park":
                    vicinoParchiCheckBox.setSelected(isFound);
                    logger.info("Impostando vicinoParchiCheckBox a: {}", isFound);
                    break;
                case "public_transport":
                    vicinoTrasportoPubblicoCheckBox.setSelected(isFound);
                    logger.info("Impostando vicinoTrasportoPubblicoCheckBox a: {}", isFound);
                    break;
                default:
                    logger.info("Categoria non riconosciuta");
                    break;
            }
        });
    }

    private Void handleGeoapifyPlacesError(Throwable e, String category) {
        Platform.runLater(() -> {
            showPopup(POPUP_ERROR_TITLE, "Errore nella chiamata API Geoapify Places (" + category + "): " + e.getMessage(), ERROR_ICON);
            logger.error("Errore nella chiamata API Geoapify Places ({}): {}", category, e.getMessage());
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

        immobile.setCitta(this.citta);
        immobile.setDimensione(Double.parseDouble(superficieTextField.getText()));
        immobile.setNumeroLocali(camereSpinner.getValue());
        immobile.setNumeroBagni(bagniSpinner.getValue());
        immobile.setClasseEnergetica(classeEnergeticaMenuButton.getText());
        immobile.setPiano(getPianoNumber());
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
                    controller.setAgente(agente);
                    controller.setSelectedImageList(selectedImageList);
                    controller.setStage(stage);
                }, avantiButton, "/com/dietiestates25ui/styles/conferma-inserzione-style.css");
    }

    private int getPianoNumber() {
        if (pianoMenuButton.getText().equals(PIANO_TERRA_ITEM)) {
            return 0;
        } else if (pianoMenuButton.getText().equals(PIANO_INTERMEDIO_ITEM)) {
            return 1;
        } else {
            return 2;
        }
    }

    private void openGestioneImmobiliPage() {
        loadScene("/com/dietiestates25ui/view/agente-dashboard-view.fxml",
                (fxmlLoader, stage) -> {
                    AgenteDashboardController controller = fxmlLoader.getController();
                    controller.setAgente(agente);
                }, indietroButton, "/com/dietiestates25ui/styles/agente-dashboard-style.css");
    }
}