package com.dietiestates25ui.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class HomePageController extends AbstractController implements Initializable {

    @FXML
    private AnchorPane primaryAnchorPane;
    @FXML
    private GridPane navbarGridPane;
    @FXML
    private HBox profileHBox;
    @FXML
    private ImageView logo;
    @FXML
    private Button venditaButton;
    @FXML
    private Button affittoButton;
    @FXML
    private MenuButton tipologiaMenuButton;
    @FXML
    private TextField ricercaTextField;
    @FXML
    private Button cercaButton;
    @FXML
    private Button selezionaMappaButton;

    private Stage currentStage;

    private String token;

    public void setToken(String token) {
        this.token = token;
    }

    public void setStage(Stage stage) {
        this.currentStage = stage;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            logo.requestFocus();
            currentStage = (Stage) primaryAnchorPane.getScene().getWindow(); // Imposta currentStage
        });

        // Imposta il testo della navbar
        // Puoi farlo anche impostando direttamente il testo in FXML
        //profileHBox.getChildren().
        // In questo caso, ho omesso il codice, perché se vuoi, scrivi qua il codice da implementare

        // Gestione del prompt text del TextField
        ricercaTextField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                ricercaTextField.setPromptText(""); // Cancella il prompt text quando il campo è in focus
            } else if (ricercaTextField.getText().isEmpty()) {
                ricercaTextField.setPromptText("Effettua una ricerca inserendo un comune"); // Ripristina il prompt text se il campo è vuoto
            }
        });

        // Azioni dei bottoni (da implementare)
        selezionaMappaButton.setOnAction(event -> {
            // Apri la schermata della mappa
            System.out.println("Seleziona sulla mappa cliccato!");
            //loadScene(...);  // Sostituisci con il metodo per caricare la scena della mappa
        });

        cercaButton.setOnAction(event -> {
            // Esegui la ricerca
            System.out.println("Cerca cliccato!");
            //loadScene(...) // Sostituisci con il metodo per caricare la scena dei risultati
        });
    }
}