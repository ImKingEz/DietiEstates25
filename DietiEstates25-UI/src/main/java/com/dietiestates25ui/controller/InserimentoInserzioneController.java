package com.dietiestates25ui.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import java.util.List;
import java.util.ResourceBundle;
import java.io.File;
import javafx.beans.binding.Bindings;

public class InserimentoInserzioneController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(InserimentoInserzioneController.class);

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
    
    private final ObservableList<File> selectedImageList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            logo.requestFocus();
            currentStage = (Stage) primaryAnchorPane.getScene().getWindow(); //CONTROLLARE
        });

        setupChoiceBox();

        setupSpinners();

        indietroButton.setOnAction(event -> openGestioneImmobiliPage());

        selezionaImmaginiButton.setOnAction(event ->  Platform.runLater(() -> handleImageSelection()));

        updateAvantiButtonState();
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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona Immagini");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Immagini", "*.png", "*.jpg", "*.jpeg"));

        List<File> newFiles = fileChooser.showOpenMultipleDialog(currentStage);

        if (newFiles != null && !newFiles.isEmpty()) {
            selectedImageList.addAll(newFiles);

            if (selectedImageList.size() > 5) {
                Platform.runLater(() -> showPopup("Attenzione", "Puoi selezionare al massimo 5 immagini.", ERROR_ICON));

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
        }
    }

    private void openGestioneImmobiliPage() {
        loadScene("/com/dietiestates25ui/view/gestione-immobili-view.fxml",
                (fxmlLoader, stage) -> {}, indietroButton, "/com/dietiestates25ui/styles/gestione-immobili-style.css");
    }

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

        selezionaImmaginiButton.disableProperty().bind(Bindings.size(selectedImageList).greaterThanOrEqualTo(5));
    }

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

        boolean requiredFieldsFilled = !titolo.isEmpty() && !cittaIndirizzo.isEmpty() && !descrizione.isEmpty() && isPrezzoValid && isSuperficieValid && !classeEnergetica.isEmpty() && (selectedImageList.size() > 0) && tipologia != null;
        boolean isMaxImagesSelected = selectedImageList.size() <= 5;

        avantiButton.setDisable(!requiredFieldsFilled || !isMaxImagesSelected);

        logger.info("Form valid: {}", !avantiButton.isDisable());
    }
}