package com.dietiestates25ui.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class HomePageController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(HomePageController.class);

    @FXML
    private AnchorPane primaryAnchorPane;
    @FXML
    private GridPane navbarGridPane;
    @FXML
    private HBox profileHBox;
    @FXML
    private ImageView logo;
    @FXML
    private ToggleButton venditaButton;
    @FXML
    private ToggleButton affittoButton;
    @FXML
    private MenuButton tipologiaMenuButton;
    @FXML
    private TextField ricercaTextField;
    @FXML
    private Button cercaButton;
    @FXML
    private Button selezionaMappaButton;
    @FXML
    private ToggleGroup venditaAffittoToggleGroup;
    @FXML
    private MenuItem villaMenuItem;
    @FXML
    private MenuItem appartamentoMenuItem;
    @FXML
    private MenuItem terrenoMenuItem;
    @FXML
    private MenuItem casaIndipendenteMenuItem;
    @FXML
    private Button tornaIndietroButton;

    private Stage currentStage;
    private String token;

    private boolean tipologiaSelezionata = false;

    private boolean venditaSelezionato = false;
    private boolean affittoSelezionato = false;

    private String selectedTipologiaText = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(this::requestFocusOnLogo);

        setupTextFieldListeners();

        selezionaMappaButton.setDisable(true);
        setupButtonActions();
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setStage(Stage stage) {
        this.currentStage = stage;
    }

    public void setPreviousSelection(boolean vendita, boolean affitto, String tipologia) {
        this.venditaSelezionato = vendita;
        this.affittoSelezionato = affitto;
        this.selectedTipologiaText = tipologia;

        if (vendita && venditaButton != null) {
            venditaButton.setSelected(true);
        } else if (affitto && affittoButton != null) {
            affittoButton.setSelected(true);
        }

        if (selectedTipologiaText != null && tipologiaMenuButton != null) {
            tipologiaMenuButton.setText(selectedTipologiaText);
            tipologiaSelezionata = true;
        }

        updateButtonStates();
    }

    private void requestFocusOnLogo() {
        logo.requestFocus();
        currentStage = (Stage) primaryAnchorPane.getScene().getWindow();
    }

    private void setupTextFieldListeners() {
        ricercaTextField.focusedProperty().addListener((obs, oldVal, newVal) -> handleTextFieldFocusChange(newVal));
        ricercaTextField.textProperty().addListener((observable, oldValue, newValue) -> updateButtonStates());
    }

    private void setupButtonActions() {
        tornaIndietroButton.setOnAction(event -> openLoginPage());
        selezionaMappaButton.setOnAction(event -> handleSelezionaMappaButtonAction());
        cercaButton.setOnAction(event -> handleCercaButtonAction());
    }

    private void handleTextFieldFocusChange(boolean newVal) {
        if (newVal) {
            ricercaTextField.setPromptText("");
        } else if (ricercaTextField.getText().isEmpty()) {
            ricercaTextField.setPromptText("Effettua una ricerca inserendo una città");
        }
    }

    private void updateButtonStates() {
        boolean isRicercaTextFieldVuoto = ricercaTextField.getText().isEmpty();
        boolean isVenditaAffittoSelezionato = venditaAffittoToggleGroup.getSelectedToggle() != null;
        boolean isTipologiaPresente = (selectedTipologiaText != null && !selectedTipologiaText.isEmpty());

        cercaButton.setDisable(isRicercaTextFieldVuoto || !isTipologiaPresente || !isVenditaAffittoSelezionato);
        selezionaMappaButton.setDisable(!isRicercaTextFieldVuoto || !isTipologiaPresente || !isVenditaAffittoSelezionato);
    }

    private void openLoginPage() {
        loadScene("/com/dietiestates25ui/view/login-view.fxml", (fxmlLoader, stage) -> {
        }, tornaIndietroButton, "/com/dietiestates25ui/styles/login-style.css");
    }

    private void handleSelezionaMappaButtonAction() {
        loadScene("/com/dietiestates25ui/view/ricerca-con-mappa-view.fxml", (fxmlLoader, stage) -> {
            RicercaConMappaController ricercaConMappaController = fxmlLoader.getController();
            ricercaConMappaController.setToken(token);
            ricercaConMappaController.setStage(currentStage);
            ricercaConMappaController.setVenditaSelezionato(getVenditaSelezionato());
            ricercaConMappaController.setAffittoSelezionato(getAffittoSelezionato());
            ricercaConMappaController.setTipologiaSelezionata(tipologiaMenuButton.getText());
        }, selezionaMappaButton, "/com/dietiestates25ui/styles/ricerca-con-mappa-style.css");
    }

    private void handleCercaButtonAction() {
        System.out.println("Cerca cliccato!");
        // Implementa la logica di ricerca
    }

    private boolean getVenditaSelezionato() {
        return venditaButton.isSelected();
    }

    private boolean getAffittoSelezionato() {
        return affittoButton.isSelected();
    }

    @FXML
    void handleTipologiaMenuItemAction(ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        selectedTipologiaText = source.getText();
        tipologiaMenuButton.setText(selectedTipologiaText);
        tipologiaSelezionata = true;
        updateButtonStates();
    }

    public String getSelectedTipologiaText() {
        return selectedTipologiaText;
    }

    public boolean getAffittoSelezionatoHomePage() {
        return affittoSelezionato;
    }

    public boolean getVenditaSelezionatoHomePage() {
        return venditaSelezionato;
    }
}