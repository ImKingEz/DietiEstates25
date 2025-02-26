package com.dietiestates25ui.controller;

import com.dietiestates25.dto.AnnuncioDTO;
import com.dietiestates25.dto.ImmobileDTO;
import com.dietiestates25.dto.UtenteDTO;
import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.model.FiltroAnnunci;
import com.dietiestates25ui.model.Utente;
import com.dietiestates25ui.service.AnnuncioService;
import com.dietiestates25ui.service.ImmobileService;
import com.dietiestates25ui.service.UtenteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.control.TextFormatter;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.UnaryOperator;

public class DashboardController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    public static final String TEXT_FIELD_ERROR_CSS_CLASS = "text-field-error";
    private String token;

    @FXML
    private ScrollPane filterScrollPane;

    @FXML
    private HBox filterHBox;

    @FXML
    private Button scrollLeftButton;

    @FXML
    private Button scrollRightButton;

    @FXML
    private Button reimpostaFiltriButton;

    @FXML
    private ImageView annuncioImageView;

    @FXML
    private ImageView annuncioImageView1;

    @FXML
    private ImageView annuncioImageView2;

    @FXML
    private HBox profileHBox;

    @FXML
    private TextField minPriceTextField;
    @FXML
    private TextField maxPriceTextField;
    @FXML
    private Button confermaPrezzoButton;
    @FXML
    private TextField minSuperficieTextField;
    @FXML
    private TextField maxSuperficieTextField;
    @FXML
    private Button confermaSuperficieButton;

    @FXML
    private MenuButton tipoMenuButton;
    @FXML
    private MenuButton tipologiaMenuButton;
    @FXML
    private MenuButton localiMenuButton;
    @FXML
    private MenuButton bagniMenuButton;
    @FXML
    private MenuButton pianoMenuButton;
    @FXML
    private MenuButton classeEnergeticaMenuButton;

    @FXML
    private CheckBox ascensoreCheckBox;
    @FXML
    private CheckBox portineriaCheckBox;
    @FXML
    private CheckBox climatizzazioneCheckBox;
    @FXML
    private CheckBox scuolaCheckBox;
    @FXML
    private CheckBox parcoCheckBox;
    @FXML
    private CheckBox trasportoPubblicoCheckBox;

    @FXML
    private MenuButton prezzoMenuButton;
    @FXML
    private MenuButton superficieMenuButton;

    @FXML
    private AnchorPane listaAnnunciAnchorPane;

    @FXML
    private ScrollPane annunciScrollPane;

    @FXML
    private VBox mappaImmobiliVBox;
    @FXML
    private WebView map;

    @FXML
    private VBox listaAnnunciVBox;
    @FXML
    private Text numeroAnnunciText;

    private String cittaDiRicerca = "Roma";

    private static final double SCROLL_AMOUNT = 600.0;
    private final Duration scrollDuration = Duration.millis(500);

    private FiltroAnnunci filtroAnnunci = new FiltroAnnunci();

    private Utente utente;

    private UtenteService utenteService = new UtenteService();

    private AnnuncioService annuncioService = new AnnuncioService();

    private ImmobileService immobileService = new ImmobileService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> logo.requestFocus());
        Platform.runLater(() -> currentStage = (Stage) primaryAnchorPane.getScene().getWindow());

        updateProfileHBox();

        scrollLeftButton.setOnAction(this::scrollLeft);
        scrollRightButton.setOnAction(this::scrollRight);

        handleFilter();

        updateAnnunciScrollPanePrefWidth();
        updateMap();
    }

    private void updateMap() {
        listaAnnunciAnchorPane.widthProperty().addListener((observable, oldValue, newValue) ->
                setMappaImmobiliVBox(newValue.doubleValue()));

        setMappaImmobiliVBox(mappaImmobiliVBox.getWidth());

        Platform.runLater(this::loadMap);
    }

    private void setMappaImmobiliVBox(double newValue) {
        double vboxWidth = newValue * 0.4;
        mappaImmobiliVBox.setPrefWidth(vboxWidth);
    }

    private void loadMap() {
        WebEngine webEngine = map.getEngine();
        webEngine.load(Objects.requireNonNull(getClass().getResource("/com/dietiestates25ui/view/mapRisultatiAnnunci.html")).toExternalForm());

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                webEngine.executeScript("window.mostraDettagliAnnuncioDaMappa = function(idImmobile) { " +
                        "  javafx.scene.web.WebEngine.executeScript('$(\"idImmobile\").DashboardController.mostraDettagliAnnuncioDaMappa(' + idImmobile + ')');" +
                        "}");
                Platform.runLater(this::updateAnnunci);
            }
        });

        webEngine.setOnAlert(event -> {
            String data = event.getData();
            logger.info("Data from WebView: {}", data);
            mostraDettagliAnnuncioDaMappa(Long.parseLong(data));
        });
    }

    private void handleFilter() {
        handlePriceFilter();
        handleSuperficeFilter();
        handleMenuItemFilter();
        handleCheckBoxFilter();
        handleReimpostaFiltriButton();
    }

    private void handleReimpostaFiltriButton() {
        reimpostaFiltriButton.setOnAction(event -> {
            String tipo = filtroAnnunci.getTipo();
            String tipologia = filtroAnnunci.getTipologia();

            filtroAnnunci = new FiltroAnnunci();
            filtroAnnunci.setTipo(tipo);
            filtroAnnunci.setTipologia(tipologia);

            minPriceTextField.clear();
            maxPriceTextField.clear();
            minSuperficieTextField.clear();
            maxSuperficieTextField.clear();

            tipoMenuButton.setText(tipo);
            tipologiaMenuButton.setText(tipologia);
            localiMenuButton.setText("Locali");
            bagniMenuButton.setText("Bagni");
            pianoMenuButton.setText("Piano");
            classeEnergeticaMenuButton.setText("Classe energetica");

            ascensoreCheckBox.setSelected(false);
            portineriaCheckBox.setSelected(false);
            climatizzazioneCheckBox.setSelected(false);
            scuolaCheckBox.setSelected(false);
            parcoCheckBox.setSelected(false);
            trasportoPubblicoCheckBox.setSelected(false);

            updateAnnunci();
        });
    }

    private void handleCheckBoxFilter() {
        ascensoreCheckBoxOnAction();
        portineriaCheckBoxOnAction();
        climatizzazioneCheckBoxOnAction();
        scuolaCheckBoxOnAction();
        parcoCheckBoxOnAction();
        trasportoPubblicoCheckBoxOnAction();
    }

    private void trasportoPubblicoCheckBoxOnAction() {
        trasportoPubblicoCheckBox.setOnAction(event -> {
            if (trasportoPubblicoCheckBox.isSelected()) {
                filtroAnnunci.setVicinoTrasportoPubblico(true);
            } else {
                filtroAnnunci.setVicinoTrasportoPubblico(null);
            }
            updateAnnunci();
        });
    }

    private void parcoCheckBoxOnAction() {
        parcoCheckBox.setOnAction(event -> {
            if (parcoCheckBox.isSelected()) {
                filtroAnnunci.setVicinoParco(true);
            } else {
                filtroAnnunci.setVicinoParco(null);
            }
            updateAnnunci();
        });
    }

    private void scuolaCheckBoxOnAction() {
        scuolaCheckBox.setOnAction(event -> {
            if (scuolaCheckBox.isSelected()) {
                filtroAnnunci.setVicinoScuola(true);
            } else {
                filtroAnnunci.setVicinoScuola(null);
            }
            updateAnnunci();
        });
    }

    private void climatizzazioneCheckBoxOnAction() {
        climatizzazioneCheckBox.setOnAction(event -> {
            if (climatizzazioneCheckBox.isSelected()) {
                filtroAnnunci.setClimatizzazione(true);
            } else {
                filtroAnnunci.setClimatizzazione(null);
            }
            updateAnnunci();
        });
    }

    private void portineriaCheckBoxOnAction() {
        portineriaCheckBox.setOnAction(event -> {
            if (portineriaCheckBox.isSelected()) {
                filtroAnnunci.setPortineria(true);
            } else {
                filtroAnnunci.setPortineria(null);
            }
            updateAnnunci();
        });
    }

    private void ascensoreCheckBoxOnAction() {
        ascensoreCheckBox.setOnAction(event -> {
            if (ascensoreCheckBox.isSelected()) {
                filtroAnnunci.setAscensore(true);
            } else {
                filtroAnnunci.setAscensore(null);
            }
            updateAnnunci();
        });
    }

    private void handleSuperficeFilter() {
        setupTextFormatter(minSuperficieTextField);
        setupTextFormatter(maxSuperficieTextField);

        minSuperficieTextField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (Boolean.FALSE.equals(newVal)) {
                validateSuperficieFields();
            }
        });
        maxSuperficieTextField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (Boolean.FALSE.equals(newVal)) {
                validateSuperficieFields();
            }
        });

        confermaSuperficieButton.setOnAction(event -> superficieMenuButton.hide());
    }

    private void handlePriceFilter() {
        setupTextFormatter(minPriceTextField);
        setupTextFormatter(maxPriceTextField);

        minPriceTextField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (Boolean.FALSE.equals(newVal)) {
                validatePriceFields();
            }
        });
        maxPriceTextField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (Boolean.FALSE.equals(newVal)) {
                validatePriceFields();
            }
        });

        confermaPrezzoButton.setOnAction(event -> prezzoMenuButton.hide());
    }

    private void updateAnnunciScrollPanePrefWidth() {
        listaAnnunciAnchorPane.widthProperty().addListener((observable, oldValue, newValue) ->
                setAnnunciScrollPanePrefWidth(newValue.doubleValue()));

        setAnnunciScrollPanePrefWidth(listaAnnunciAnchorPane.getWidth());
    }

    private void setAnnunciScrollPanePrefWidth(double newValue) {
        double scrollPaneWidth = newValue * 0.55;
        annunciScrollPane.setPrefWidth(scrollPaneWidth);
    }

    private void setupTextFormatter(TextField textField) {
        UnaryOperator<TextFormatter.Change> numberFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*(\\.\\d*)?")) {
                return change;
            }
            return null;
        };
        TextFormatter<Object> textFormatter = new TextFormatter<>(numberFilter);
        textField.setTextFormatter(textFormatter);
    }

    private void validatePriceFields() {
        Double minValue = null;
        Double maxValue = null;
        boolean hasError = false;

        try {
            minValue = minPriceTextField.getText().isEmpty() ? 0d : Double.parseDouble(minPriceTextField.getText());
            if (minValue < 0) {
                throw new NumberFormatException("Il valore minimo non può essere negativo.");
            }
            minPriceTextField.getStyleClass().remove(TEXT_FIELD_ERROR_CSS_CLASS);
        } catch (NumberFormatException e) {
            logger.error("Valore minimo non valido: {}", e.getMessage());
            minPriceTextField.getStyleClass().add(TEXT_FIELD_ERROR_CSS_CLASS);
            hasError = true;
        }

        try {
            maxValue = maxPriceTextField.getText().isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxPriceTextField.getText());
            if (maxValue <= 0) {
                throw new NumberFormatException("Il valore massimo deve essere maggiore di zero.");
            }
            maxPriceTextField.getStyleClass().remove(TEXT_FIELD_ERROR_CSS_CLASS);
            logger.error("{}", maxPriceTextField.getStyleClass());
        } catch (NumberFormatException e) {
            logger.error("Valore massimo non valido: {}", e.getMessage());
            maxPriceTextField.getStyleClass().add(TEXT_FIELD_ERROR_CSS_CLASS);
            hasError = true;
        }

        if (!hasError && minValue > maxValue) {
            logger.error("Il valore minimo è superiore al valore massimo.");
            minPriceTextField.getStyleClass().add(TEXT_FIELD_ERROR_CSS_CLASS);
            maxPriceTextField.getStyleClass().add(TEXT_FIELD_ERROR_CSS_CLASS);
            hasError = true;
        }

        if (!hasError) {
            applyPriceFilter(minValue, maxValue);
        }
    }

    private void validateSuperficieFields() {
        Double minValue = null;
        Double maxValue = null;
        boolean hasError = false;

        try {
            minValue = minSuperficieTextField.getText().isEmpty() ? 0d : Double.parseDouble(minSuperficieTextField.getText());
            if (minValue < 0) {
                throw new NumberFormatException("Il valore minimo non può essere negativo.");
            }
            minSuperficieTextField.getStyleClass().remove(TEXT_FIELD_ERROR_CSS_CLASS);
        } catch (NumberFormatException e) {
            logger.error("Valore minimo non valido: {}", e.getMessage());
            minSuperficieTextField.getStyleClass().add(TEXT_FIELD_ERROR_CSS_CLASS);
            hasError = true;
        }

        try {
            maxValue = maxSuperficieTextField.getText().isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxSuperficieTextField.getText());
            if (maxValue <= 0) {
                throw new NumberFormatException("Il valore massimo deve essere maggiore di zero.");
            }
            maxSuperficieTextField.getStyleClass().remove(TEXT_FIELD_ERROR_CSS_CLASS);
        } catch (NumberFormatException e) {
            logger.error("Valore massimo non valido: {}", e.getMessage());
            maxSuperficieTextField.getStyleClass().add(TEXT_FIELD_ERROR_CSS_CLASS);
            hasError = true;
        }

        if (!hasError && minValue > maxValue) {
            logger.error("Il valore minimo è superiore al valore massimo.");
            minSuperficieTextField.getStyleClass().add(TEXT_FIELD_ERROR_CSS_CLASS);
            maxSuperficieTextField.getStyleClass().add(TEXT_FIELD_ERROR_CSS_CLASS);
            hasError = true;
        }

        if (!hasError) {
            applySuperficieFilter(minValue, maxValue);
        }
    }

    private void applyPriceFilter(Double minValue, Double maxValue) {
        filtroAnnunci.setPrezzoMin(minValue);
        filtroAnnunci.setPrezzoMax(maxValue);
        logger.info("Filtri prezzo impostati: min = {}, max = {}", filtroAnnunci.getPrezzoMin(), filtroAnnunci.getPrezzoMax());
        updateAnnunci();
    }

    private void applySuperficieFilter(Double minValue, Double maxValue) {
        filtroAnnunci.setSuperficieMin(minValue);
        filtroAnnunci.setSuperficieMax(maxValue);
        logger.info("Filtri superficie impostati: min = {}, max = {}", filtroAnnunci.getSuperficieMin(), filtroAnnunci.getSuperficieMax());
        updateAnnunci();
    }

    private void handleMenuItemFilter() {
        initializeMenuItems(tipoMenuButton, selectedText -> {
            filtroAnnunci.setTipo(selectedText);
            tipoMenuButton.setText(selectedText);
            updateAnnunci();
        });
        initializeMenuItems(tipologiaMenuButton, selectedText -> {
            filtroAnnunci.setTipologia(selectedText);
            tipologiaMenuButton.setText(selectedText);
            updateAnnunci();
        });
        initializeMenuItemsLocali(localiMenuButton, value -> {
            filtroAnnunci.setLocali(value);
            localiMenuButton.setText("Locali: " + value);
            updateAnnunci();
        });
        initializeMenuItemsBagni(bagniMenuButton, value -> {
            filtroAnnunci.setBagni(value);
            bagniMenuButton.setText("Bagni: " + value);
            updateAnnunci();
        });
        initializeMenuItemsPiano(pianoMenuButton, value -> {
            filtroAnnunci.setPiano(value);
            pianoMenuButton.setText(getPianoText(value));
            updateAnnunci();
        });
        initializeMenuItemsClasseEnergetica(classeEnergeticaMenuButton, selectedText -> {
            filtroAnnunci.setClasseEnergetica(selectedText);
            classeEnergeticaMenuButton.setText("Classe energetica: " + selectedText);
            updateAnnunci();
        });
    }

    private String getPianoText(int value) {
        return switch (value) {
            case 0 -> PIANO_TERRA;
            case 1 -> PIANO_INTERMEDIO;
            case 2 -> ULTIMO_PIANO;
            default -> PIANO_TERRA;
        };
    }


    private void initializeMenuItems(MenuButton menuButton, Consumer<String> filterSetter) {
        for (MenuItem item : menuButton.getItems()) {
            item.setOnAction(event -> {
                String selectedText = item.getText();
                filterSetter.accept(selectedText);
            });
        }
    }

    private void initializeMenuItemsLocali(MenuButton menuButton, IntConsumer filterSetter) {
        for (MenuItem item : menuButton.getItems()) {
            item.setOnAction(event -> {
                String selectedText = item.getText();
                int value;
                if (selectedText.equals("5 o più")) {
                    value = 5;
                } else {
                    value = Integer.parseInt(selectedText);
                }
                filterSetter.accept(value);
            });
        }
    }

    private void initializeMenuItemsBagni(MenuButton menuButton, IntConsumer filterSetter) {
        for (MenuItem item : menuButton.getItems()) {
            item.setOnAction(event -> {
                String selectedText = item.getText();
                int value;
                if (selectedText.equals("4 o più")) {
                    value = 4;
                } else {
                    value = Integer.parseInt(selectedText);
                }
                filterSetter.accept(value);
            });
        }
    }

    private void initializeMenuItemsPiano(MenuButton menuButton, IntConsumer filterSetter) {
        for (MenuItem item : menuButton.getItems()) {
            item.setOnAction(event -> {
                String selectedText = item.getText();
                int value = switch (selectedText) {
                    case PIANO_TERRA -> 0;
                    case PIANO_INTERMEDIO -> 1;
                    case ULTIMO_PIANO -> 2;
                    default -> 0;
                };
                filterSetter.accept(value);
            });
        }
    }

    private void initializeMenuItemsClasseEnergetica(MenuButton classeEnergeticaMenuButton, Consumer<List<String>> filterSetter) {
        for (MenuItem item : classeEnergeticaMenuButton.getItems()) {
            item.setOnAction(event -> {
                String selectedText = item.getText();
                List<String> classiEnergeticheSelezionate = new ArrayList<>();
                switch (selectedText) {
                    case "Alta (A, A+, A1-A4)": {
                        classiEnergeticheSelezionate.addAll(List.of("A", "A+", "A1", "A2", "A3", "A4"));
                        break;
                    }
                    case "Media (B, C, D e superiore)": {
                        classiEnergeticheSelezionate.addAll(List.of("B", "C", "D"));
                        break;
                    }
                    case "Bassa (E, F, G e superiore)": {
                        classiEnergeticheSelezionate.addAll(List.of("E", "F", "G"));
                        break;
                    }
                    default: {
                        classiEnergeticheSelezionate.addAll(List.of("E", "F", "G"));
                    }
                }
                filterSetter.accept(classiEnergeticheSelezionate);
            });
        }
    }

    private void setUtente(String token) {
        try {
            logger.info("Recupero dati utente con token: {}", token);
            UtenteDTO utenteDTO = utenteService.getUtenteDetails(token);
            utente = new Utente();
            utente.setNome(utenteDTO.getNome());
            utente.setCognome(utenteDTO.getCognome());
            utente.setEmail(utenteDTO.getEmail());
            utente.setCitta(utenteDTO.getCitta());
        } catch (GenericServiceException e) {
            logger.error("Errore durante il recupero dei dati dell'utente: {}", e.getMessage());
        }
    }

    private void updateProfileHBox() {
        Platform.runLater(() -> {
            Text ciaoNome = new Text();
            ciaoNome.getStyleClass().add("profileName");
            ciaoNome.setText("Ciao " + utente.getNome());
            profileHBox.getChildren().addFirst(ciaoNome);
        });
    }

    private void scroll(ActionEvent event) {
        double deltaX = (event.getSource() == scrollLeftButton) ? SCROLL_AMOUNT : -SCROLL_AMOUNT;
        double currentX = filterHBox.getTranslateX();
        double targetX = currentX + deltaX;

        double minX = filterScrollPane.getWidth() - filterHBox.getWidth();
        targetX = Math.clamp(targetX, minX, 0);

        TranslateTransition tt = new TranslateTransition(scrollDuration, filterHBox);
        tt.setToX(targetX);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();
    }

    @FXML
    private void scrollLeft(ActionEvent event) {
        scroll(event);
    }

    @FXML
    private void scrollRight(ActionEvent event) {
        scroll(event);
    }

    public void setToken(String token) {
        this.token = token;
        setUtente(token);
        //TODO: Rimuovere sotto dopo la merge se viene passato dalla schermata precedente una lista di annunci
        filtroAnnunci.setTipo("Vendita");
        filtroAnnunci.setTipologia("Casa indipendente");
    }

    private void updateAnnunci() {
        try {
            List<AnnuncioDTO> annunci;
            logger.info("Aggiornamento degli annunci per la città: {} con i seguenti filtri: {}", cittaDiRicerca, filtroAnnunci);
            annunci = annuncioService.searchAnnunciByCittaAndFiltro(cittaDiRicerca, filtroAnnunci, token);

            Platform.runLater(() -> {
                if (annunci != null && !annunci.isEmpty()) {
                    visualizzaAnnunciSullaMappa(annunci);
                    visualizzaAnnunciNellaLista(annunci);
                } else {
                    showPopup("Nessun risultato trovato.", "Prova a cambiare i filtri.", ERROR_ICON);
                    logger.warn("Nessun annuncio trovato per la città: {}", cittaDiRicerca);
                    visualizzaAnnunciSullaMappa(Collections.emptyList());
                    visualizzaAnnunciNellaLista(Collections.emptyList());
                }
            });
        } catch (GenericServiceException e) {
            logger.error("Errore durante il recupero degli annunci: {}", e.getMessage(), e);
        }
    }

    private void visualizzaAnnunciSullaMappa(List<AnnuncioDTO> annunci) {
        double minLon = Double.MAX_VALUE;
        double minLat = Double.MAX_VALUE;
        double maxLon = Double.MIN_VALUE;
        double maxLat = Double.MIN_VALUE;

        List<Map<String, Object>> annunciPerLaMappa = new ArrayList<>();
        for (AnnuncioDTO annuncio : annunci) {
            ImmobileDTO immobile = null;
            try {
                immobile = immobileService.getImmobileDetails(annuncio.getIdImmobile(), token);

                double latitudine = immobile.getLatitudine();
                double longitudine = immobile.getLongitudine();

                minLon = Math.min(minLon, longitudine);
                minLat = Math.min(minLat, latitudine);
                maxLon = Math.max(maxLon, longitudine);
                maxLat = Math.max(maxLat, latitudine);

                Map<String, Object> annuncioMap = new HashMap<>();
                annuncioMap.put("latitudine", latitudine);
                annuncioMap.put("longitudine", longitudine);
                annuncioMap.put("titolo", annuncio.getTitolo());
                annuncioMap.put("prezzo", annuncio.getPrezzo());
                annuncioMap.put("descrizione", annuncio.getDescrizione());
                annuncioMap.put("idImmobile", annuncio.getIdImmobile());
                annunciPerLaMappa.add(annuncioMap);

            } catch (GenericServiceException e) {
                logger.error("Errore durante il recupero dei dettagli dell'immobile: {}", e.getMessage(), e);
            }

        }

        if (!annunci.isEmpty()) {
            double[] extent = {minLon, minLat, maxLon, maxLat};

            String extentString = Arrays.toString(extent);

            WebEngine webEngine = map.getEngine();
            webEngine.executeScript("fitViewToExtent(" + extentString + ");");
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String annunciJson = objectMapper.writeValueAsString(annunciPerLaMappa);
            WebEngine webEngine = map.getEngine();
            webEngine.executeScript("addMarkersToMap(" + annunciJson + ");");
        } catch (Exception e) {
            logger.error("Errore serializzazione JSON annunci:", e);
        }
    }

    private void visualizzaAnnunciNellaLista(List<AnnuncioDTO> annunci) {
        listaAnnunciVBox.getChildren().clear();
        numeroAnnunciText.setText(annunci.size() + " risultati");
        listaAnnunciVBox.getChildren().add(numeroAnnunciText);
        for (AnnuncioDTO annuncio : annunci) {
            ImmobileDTO immobile = null;
            try {
                immobile = immobileService.getImmobileDetails(annuncio.getIdImmobile(), token);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/dietiestates25ui/view/annuncio-item-view.fxml"));
                HBox annuncioItem = loader.load();

                AnnuncioItemController controller = loader.getController();
                controller.setAnnuncio(annuncio, immobile);

                // Aggiungiamo il listener per il click
                ImmobileDTO finalImmobile = immobile;
                annuncioItem.setOnMouseClicked(event -> mostraDettagliAnnuncio(annuncio, finalImmobile));

                listaAnnunciVBox.getChildren().add(annuncioItem);
            } catch (IOException e) {
                logger.error("Errore durante il caricamento del layout dell'annuncio:", e);
            } catch (GenericServiceException e) {
                logger.error("Errore durante il recupero dei dettagli dell'immobile: {}", e.getMessage(), e);
            }
        }
    }

    private void mostraDettagliAnnuncioDaMappa(Long idImmobile) {
        try {
            AnnuncioDTO annuncio = annuncioService.getAnnuncioByIdImmobile(idImmobile, token);
            ImmobileDTO immobile = immobileService.getImmobileDetails(idImmobile, token);

            if (annuncio != null && immobile != null) {
                Platform.runLater(() -> mostraDettagliAnnuncio(annuncio, immobile));
            } else {
                logger.error("Annuncio o Immobile non trovato per idImmobile: {}", idImmobile);
                showPopup("Errore", "Annuncio o immobile non trovato.", ERROR_ICON);
            }
        } catch (GenericServiceException e) {
            logger.error("Errore durante il recupero dell'annuncio o dell'immobile: {}", e.getMessage(), e);
            showPopup("Errore", "Impossibile recuperare i dettagli dell'annuncio.", ERROR_ICON);
        }
    }

    private void mostraDettagliAnnuncio(AnnuncioDTO annuncio, ImmobileDTO immobile) {
        loadScene("/com/dietiestates25ui/view/annuncio-detail-view.fxml",
                (fxmlLoader, stage) -> {
                    AnnuncioDetailController annuncioDetailController = fxmlLoader.getController();
                    annuncioDetailController.setStage(stage);
                    annuncioDetailController.setToken(token);
                    annuncioDetailController.setAnnuncio(annuncio, immobile);
                }, reimpostaFiltriButton, "/com/dietiestates25ui/styles/annuncio-detail-style.css");
    }
}