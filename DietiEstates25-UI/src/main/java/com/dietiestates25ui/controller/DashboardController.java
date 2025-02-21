package com.dietiestates25ui.controller;

import com.dietiestates25.dto.UtenteDTO;
import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.model.FiltroAnnunci;
import com.dietiestates25ui.model.Utente;
import com.dietiestates25ui.service.UtenteService;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.TextFormatter;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.UnaryOperator;

public class DashboardController extends AbstractController implements Initializable {

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
    private ImageView annuncioImageView;

    @FXML
    private ImageView annuncioImageView1;

    @FXML
    private ImageView annuncioImageView2;

    @FXML
    private HBox profileHBox;

    // Price and Surface
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

    // MenuButtons
    @FXML
    private MenuButton tipoMenuButton; // Tipo (Vendita, Affitto)
    @FXML
    private MenuButton tipologiaMenuButton; // Tipologia (Casa indipendente, Appartamento, Villa, Terreno)
    @FXML
    private MenuButton localiMenuButton; // Locali (1, 2, 3, 4, 5 o più)
    @FXML
    private MenuButton bagniMenuButton; // Bagni (1, 2, 3, 4 o più)
    @FXML
    private MenuButton pianoMenuButton; // Piano (Piano terra, Piani intermedi, Ultimo piano)
    @FXML
    private MenuButton classeEnergeticaMenuButton; // Classe energetica (Alta, Media, Bassa)

    // CheckBoxes
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

    private static final double SCROLL_AMOUNT = 600.0;
    private final Duration scrollDuration = Duration.millis(500);

    private FiltroAnnunci filtroAnnunci = new FiltroAnnunci();

    private Utente utente;

    private UtenteService utenteService = new UtenteService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> logo.requestFocus());
        Platform.runLater(() -> currentStage = (Stage) primaryAnchorPane.getScene().getWindow());

        updateProfileHBox();

        Platform.runLater(() -> setAnnuncioImageView(annuncioImageView)); //test
        Platform.runLater(() -> setAnnuncioImageView(annuncioImageView1)); //test
        Platform.runLater(() -> setAnnuncioImageView(annuncioImageView2)); //test

        scrollLeftButton.setOnAction(this::scrollLeft);
        scrollRightButton.setOnAction(this::scrollRight);

        handleFilter();

        updateAnnunciScrollPanePrefWidth();

    }

    private void handleFilter() {
        handlePriceFilter();
        handleSuperficeFilter();
        handleMenuItemFilter();
        handleCheckBoxFilter();
    }

    private void handleCheckBoxFilter() {
        ascensoreCheckBox.setOnAction(event -> {
            filtroAnnunci.setAscensore(ascensoreCheckBox.isSelected());
            updateAnnunci();
        });
        portineriaCheckBox.setOnAction(event -> {
            filtroAnnunci.setPortineria(portineriaCheckBox.isSelected());
            updateAnnunci();
        });
        climatizzazioneCheckBox.setOnAction(event -> {
            filtroAnnunci.setClimatizzazione(climatizzazioneCheckBox.isSelected());
            updateAnnunci();
        });
        scuolaCheckBox.setOnAction(event -> {
            filtroAnnunci.setVicinoScuola(scuolaCheckBox.isSelected());
            updateAnnunci();
        });
        parcoCheckBox.setOnAction(event -> {
            filtroAnnunci.setVicinoParco(parcoCheckBox.isSelected());
            updateAnnunci();
        });
        trasportoPubblicoCheckBox.setOnAction(event -> {
            filtroAnnunci.setVicinoTrasportoPubblico(trasportoPubblicoCheckBox.isSelected());
            updateAnnunci();
        });
    }

    private void handleMenuItemFilter() {
        initializeMenuItems(tipoMenuButton, filtroAnnunci::setTipo);
        initializeMenuItems(tipologiaMenuButton, filtroAnnunci::setTipologia);
        initializeMenuItemsLocali(localiMenuButton, filtroAnnunci::setLocali);
        initializeMenuItemsBagni(bagniMenuButton, filtroAnnunci::setBagni);
        initializeMenuItemsPiano(pianoMenuButton, filtroAnnunci::setPiano);
        initializeMenuItemsClasseEnergetica(classeEnergeticaMenuButton, filtroAnnunci::setClasseEnergetica);
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

        Platform.runLater(() -> setAnnunciScrollPanePrefWidth(listaAnnunciAnchorPane.getWidth()));
    }

    private void setAnnunciScrollPanePrefWidth(double newValue) {
        double scrollPaneWidth = newValue * 0.5; // 60% della larghezza
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

    // Utility method to handle MenuButton selections
    private void initializeMenuItems(MenuButton menuButton, Consumer<String> filterSetter) {
        for (MenuItem item : menuButton.getItems()) {
            item.setOnAction(event -> {
                String selectedText = item.getText();
                filterSetter.accept(selectedText);
                menuButton.setText(selectedText); // Update MenuButton text
                updateAnnunci();
            });
        }
    }

    // Utility method to handle MenuButton selections
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
                menuButton.setText("Locali: " + selectedText); // Update MenuButton text
                updateAnnunci();
            });
        }
    }

    // Utility method to handle MenuButton selections
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
                menuButton.setText("Bagni: " + selectedText); // Update MenuButton text
                updateAnnunci();
            });
        }
    }

    // Utility method to handle MenuButton selections
    private void initializeMenuItemsPiano(MenuButton menuButton, IntConsumer filterSetter) {
        for (MenuItem item : menuButton.getItems()) {
            item.setOnAction(event -> {
                String selectedText = item.getText();
                int value = switch (selectedText) {
                    case "Piano terra" -> 0;
                    case "Piani intermedi" -> 1;
                    case "Ultimo piano" -> 2;
                    default -> 0;
                };
                filterSetter.accept(value);
                menuButton.setText(selectedText); // Update MenuButton text
                updateAnnunci();
            });
        }
    }

    private void initializeMenuItemsClasseEnergetica(MenuButton classeEnergeticaMenuButton, Consumer<String> filterSetter) {
        for (MenuItem item : classeEnergeticaMenuButton.getItems()) {
            item.setOnAction(event -> {
                String selectedText = item.getText();
                String value = switch (selectedText) {
                    case "Alta (A, A+, A1-A4)" -> "Alta";
                    case "Media (B, C, D e superiore)" -> "Media";
                    case "Bassa (E, F, G e superiore)" -> "Bassa";
                    default -> "Bassa";
                };
                filterSetter.accept(value);
                classeEnergeticaMenuButton.setText("Classe energetica: " + selectedText); // Update MenuButton text
                updateAnnunci();
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

    private void scroll(double deltaX) {
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
        scroll(SCROLL_AMOUNT);
    }

    @FXML
    private void scrollRight(ActionEvent event) {
        scroll(-SCROLL_AMOUNT);
    }

    private void setAnnuncioImageView(ImageView imageViewAnnuncio) {
        if (imageViewAnnuncio != null) {
            Image image = new Image("file:C:\\Users\\WIN10\\Pictures\\Screenshots\\casa.png");
            imageViewAnnuncio.setImage(image);

            // Calcola le proporzioni
            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();
            double aspectRatio = imageWidth / imageHeight;

            // Determina se l'immagine è più larga che alta o più stretta
            if (aspectRatio > 1) { // Immagine più larga che alta
                // Calcola la larghezza necessaria per riempire l'altezza
                // Fondamentalmente è sempre imageHeight visto che fitHeight è 200

                // Calcola l'offset per centrare la porzione visibile
                double offsetX = (imageWidth - imageHeight) / 2;

                // Imposta il viewport
                imageViewAnnuncio.setViewport(new Rectangle2D(offsetX, 0, imageHeight, imageHeight));
            } else { // Immagine più alta che larga o quadrata
                //Non serve fare nulla, l'immagine si adatterà all'altezza senza ritagliare
            }

        } else {
            logger.error("ImageView annuncioImageView non iniettato! Controlla l'FXML.");
        }
    }


    public void setToken(String token) {
        this.token = token;
        setUtente(token);
    }

    //TODO: implementa l'update degli annunci
    private void updateAnnunci() {
        logger.info("Aggiornamento degli annunci con i seguenti filtri: {}", filtroAnnunci);
        // Qui andrebbe la logic per filtrare e visualizzare gli annunci
    }
}