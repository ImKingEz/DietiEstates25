package com.dietiestates25ui.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.stage.FileChooser;
import java.io.File;

import javafx.beans.binding.Bindings;

/**
 * Controller per la schermata di inserimento di una nuova inserzione immobiliare.
 * Gestisce l'interazione con l'utente e la validazione dei dati inseriti.
 */
public class InserimentoInserzioneController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(InserimentoInserzioneController.class);

    //region FXML Injections
    @FXML private TextField titoloTextField;
    @FXML private ChoiceBox<String> tipologiaChoiceBox;
    @FXML private TextField cittaIndirizzoTextField;
    @FXML private TextField prezzoTextField;
    @FXML private TextArea descrizioneTextArea;
    @FXML private Button selezionaImmaginiButton;
    @FXML private FlowPane immaginiFlowPane;
    @FXML private TextField superficieTextField;
    @FXML private Spinner<Integer> camereSpinner;
    @FXML private Spinner<Integer> bagniSpinner;
    @FXML private TextField classeEnergeticaTextField;
    @FXML private TextField pianoTextField;
    @FXML private CheckBox ascensoreCheckBox;
    @FXML private CheckBox portineriaCheckBox;
    @FXML private CheckBox climatizzazioneCheckBox;
    @FXML private Spinner<Integer> pianoSpinner;
    @FXML private Button avantiButton;
    @FXML private Button indietroButton;
    //endregion

    // Lista per conservare i file delle immagini selezionate
    private final ObservableList<File> selectedFilesList = FXCollections.observableArrayList();

    /**
     * Metodo chiamato all'inizializzazione del controller.
     * Imposta i valori predefiniti dei componenti della schermata e definisce i comportamenti degli eventi.
     * @param url L'URL del file FXML.
     * @param resourceBundle Il ResourceBundle contenente le risorse localizzate.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Richiede il focus sul logo per una migliore esperienza utente
        Platform.runLater(() -> logo.requestFocus());

        // Inizializza i componenti
        setupChoiceBox();
        setupSpinners();
        setupButtonActions();
        updateAvantiButtonState();
    }

    /**
     * Imposta le opzioni della ChoiceBox per la tipologia di inserzione.
     */
    private void setupChoiceBox() {
        tipologiaChoiceBox.setItems(FXCollections.observableArrayList("Affitto", "Vendita"));
        tipologiaChoiceBox.setValue("Affitto");
    }

    /**
     * Configura i valori minimi, massimi e iniziali degli Spinners per camere, bagni e piano.
     */
    private void setupSpinners() {
        camereSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 1));
        bagniSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));
        pianoSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-6, 50, 0));
    }

    /**
     * Definisce le azioni da eseguire quando vengono premuti i bottoni "Indietro" e "Seleziona Immagini".
     */
    private void setupButtonActions() {
        indietroButton.setOnAction(event -> openGestioneImmobiliPage());

        selezionaImmaginiButton.setOnAction(event -> handleImageSelection());
    }

    /**
     * Gestisce la selezione delle immagini da parte dell'utente.
     * Permette di selezionare fino a 5 immagini e le visualizza in miniatura nel FlowPane.
     */
    private void handleImageSelection() {
        // Configura il FileChooser per la selezione delle immagini
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona Immagini");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Immagini", "*.png", "*.jpg", "*.jpeg"));

        // Mostra la finestra di dialogo per la selezione multipla dei file
        List<File> newFiles = fileChooser.showOpenMultipleDialog(currentStage);

        // Se sono stati selezionati nuovi file
        if (newFiles != null && !newFiles.isEmpty()) {
            // Aggiunge i nuovi file alla lista esistente
            selectedFilesList.addAll(newFiles);

            // Se sono state selezionate più di 5 immagini, mostra un avviso e limita la lista
            if (selectedFilesList.size() > 5) {
                showPopup("Attenzione", "Puoi selezionare al massimo 5 immagini.", ERROR_ICON);
                while (selectedFilesList.size() > 5) {
                    selectedFilesList.remove(selectedFilesList.size() - 1);
                }
            }

            // Aggiorna la visualizzazione delle miniature
            updateImageThumbnails();
        }

        // Aggiorna lo stato di validità del form
        checkFormValidity();
    }

    /**
     * Aggiorna la visualizzazione delle miniature delle immagini nel FlowPane.
     */
    private void updateImageThumbnails() {
        // Pulisce le miniature precedenti
        immaginiFlowPane.getChildren().clear();

        // Dimensione fissa per le miniature
        double fixedImageSize = 50;

        // Per ogni file nella lista delle immagini selezionate
        for (File file : selectedFilesList) {
            // Crea un'immagine e un ImageView
            Image image = new Image(file.toURI().toString());
            ImageView imageView = new ImageView(image);

            // Imposta le dimensioni fisse della miniatura
            imageView.setFitWidth(fixedImageSize);
            imageView.setFitHeight(fixedImageSize);
            imageView.setPreserveRatio(true);

            // Crea un bottone per eliminare l'immagine
            Button deleteButton = new Button("X");
            deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-padding: 0px; -fx-font-size: 8px;");

            // Definisce l'azione da eseguire quando si preme il bottone di eliminazione
            deleteButton.setOnAction(e -> {
                logo.requestFocus();
                selectedFilesList.remove(file); // Rimuove il file dalla lista
                immaginiFlowPane.getChildren().remove(imageView.getParent()); // Rimuove il VBox dal FlowPane
                checkFormValidity(); // Aggiorna lo stato del bottone
            });

            // Crea un VBox per contenere l'immagine e il pulsante
            VBox imageContainer = new VBox(imageView, deleteButton);
            imageContainer.setAlignment(Pos.CENTER);

            // Aggiunge il VBox al FlowPane
            immaginiFlowPane.getChildren().add(imageContainer);
        }
    }

    /**
     * Apre la schermata di gestione immobili.
     */
    private void openGestioneImmobiliPage() {
        loadScene("/com/dietiestates25ui/view/gestione-immobili-view.fxml",
                (fxmlLoader, stage) -> {}, indietroButton, "/com/dietiestates25ui/styles/gestione-immobili-style.css");
    }

    /**
     * Imposta i listener per i campi del form, in modo da aggiornare lo stato del bottone "Avanti"
     * ogni volta che viene modificato un campo.
     */
    private void updateAvantiButtonState() {
        titoloTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        cittaIndirizzoTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        prezzoTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        superficieTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        classeEnergeticaTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        descrizioneTextArea.textProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());

        tipologiaChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        camereSpinner.valueProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        bagniSpinner.valueProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());
        pianoSpinner.valueProperty().addListener((observable, oldValue, newValue) -> checkFormValidity());

        // Usa Bindings.size per disabilitare il bottone selezionaImmaginiButton quando ci sono già 5 immagini
        selezionaImmaginiButton.disableProperty().bind(Bindings.size(selectedFilesList).greaterThanOrEqualTo(5));

        checkFormValidity();
    }

    /**
     * Controlla se tutti i campi obbligatori sono stati compilati correttamente.
     * Abilita o disabilita il bottone "Avanti" in base al risultato della validazione.
     */
    private void checkFormValidity() {
        String titolo = titoloTextField.getText().trim();
        String cittaIndirizzo = cittaIndirizzoTextField.getText().trim();
        String prezzo = prezzoTextField.getText().trim();
        String superficie = superficieTextField.getText().trim();
        String classeEnergetica = classeEnergeticaTextField.getText().trim();
        String descrizione = descrizioneTextArea.getText().trim();
        String tipologia = tipologiaChoiceBox.getValue();

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

        boolean requiredFieldsFilled = !titolo.isEmpty() && !cittaIndirizzo.isEmpty() && !descrizione.isEmpty() && isPrezzoValid && isSuperficieValid && !classeEnergetica.isEmpty() && (selectedFilesList.size() > 0) && tipologia != null;
        boolean isMaxImagesSelected = selectedFilesList.size() <= 5;

        avantiButton.setDisable(!requiredFieldsFilled || !isMaxImagesSelected);

        logger.info("Form valid: {}", !avantiButton.isDisable());
    }
}