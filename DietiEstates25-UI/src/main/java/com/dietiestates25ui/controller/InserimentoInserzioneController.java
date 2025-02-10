package com.dietiestates25ui.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class InserimentoInserzioneController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(InserimentoInserzioneController.class);

    @FXML
    private TextField titoloTextField;

    @FXML
    private ChoiceBox<String> tipologiaChoiceBox;

    @FXML
    private TextField cittaIndirizzoTextField;

    @FXML
    private TextField prezzoTextField;

    @FXML
    private TextArea descrizioneTextArea;

    @FXML
    private TextField immaginiTextField;

    @FXML
    private TextField superficieTextField;

    @FXML
    private Spinner<Integer> camereSpinner;

    @FXML
    private Spinner<Integer> bagniSpinner;

    @FXML
    private TextField classeEnergeticaTextField;

    @FXML
    private TextField pianoTextField;

    @FXML
    private CheckBox ascensoreCheckBox;

    @FXML
    private CheckBox portineriaCheckBox;

    @FXML
    private CheckBox climatizzazioneCheckBox;

    @FXML
    private Spinner<Integer> pianoSpinner;

    @FXML
    private ChoiceBox<String> ulterioriServiziChoiceBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            logo.requestFocus();
            currentStage = (Stage) primaryAnchorPane.getScene().getWindow();
        });
        // Inizializza i componenti della schermata
        tipologiaChoiceBox.setItems(FXCollections.observableArrayList("Affitto", "Vendita"));
        tipologiaChoiceBox.setValue("Affitto"); // Imposta il valore predefinito

        // Configura gli spinner per camere e bagni
        SpinnerValueFactory<Integer> camereValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1); // min, max, initial value
        camereSpinner.setValueFactory(camereValueFactory);

        SpinnerValueFactory<Integer> bagniValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 1);
        bagniSpinner.setValueFactory(bagniValueFactory);

        //Configura lo spinner per piano
        SpinnerValueFactory<Integer> pianoValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(-5, 50, 0); //valore iniziale 0
        pianoSpinner.setValueFactory(pianoValueFactory);

        // Configura ChoiceBox per ulteriori servizi
        List<String> servizi = Arrays.asList("Ascensore", "Portineria", "Climatizzazione");
        ulterioriServiziChoiceBox.setItems(FXCollections.observableArrayList(servizi));
    }
}