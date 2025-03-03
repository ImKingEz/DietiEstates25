package com.dietiestates25ui.controller;

import com.dietiestates25.dto.AgenziaDTO;
import com.dietiestates25.dto.AnnuncioDTO;
import com.dietiestates25.dto.UtenteDTO;
import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.model.*;
import com.dietiestates25ui.service.AgenziaService;
import com.dietiestates25ui.service.AmministratoreService;
import com.dietiestates25ui.service.AnnuncioService;
import com.dietiestates25ui.service.UtenteService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class HomePageController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(HomePageController.class);

    @FXML
    private GridPane navbarGridPane;
    @FXML
    public ToggleButton venditaButton;
    @FXML
    public ToggleButton affittoButton;
    @FXML
    public MenuButton tipologiaMenuButton;
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

    private String selectedTipologiaText = "Appartamento";
    private UtenteService utenteService = new UtenteService();

    private AnnuncioService annuncioService = new AnnuncioService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            logo.requestFocus();
            currentStage = (Stage) primaryAnchorPane.getScene().getWindow();
        });

        Platform.runLater(this::searchUserNameAndUpdateProfileHBox);

        tornaIndietroButton.setOnAction(event -> openLoginPage());
        selezionaMappaButton.setOnAction(event -> handleSelezionaMappaButtonAction());
        cercaButton.setOnAction(event -> handleCercaButtonAction());

        setupTextFieldListeners();

        setupTipologie();
    }

    private void setupTipologie() {
        venditaButton.setSelected(true);
        tipologiaMenuButton.setText(selectedTipologiaText);
        ricercaTextField.setPromptText("Effettua una ricerca inserendo una città");
        updateselezionaMappaButtonStates();
        updateTipologiaButtonStates();
    }

    private void updateTipologiaButtonStates() {
        venditaButton.setOnAction(event -> updateVenditaButtonState());
        affittoButton.setOnAction(event -> updateAffittaButtonState());
    }

    private void updateAffittaButtonState() {
        venditaButton.setSelected(false);
        affittoButton.setSelected(true);
    }

    private void updateVenditaButtonState() {
        venditaButton.setSelected(true);
        affittoButton.setSelected(false);
    }

    private void setupTextFieldListeners() {
        ricercaTextField.focusedProperty().addListener((obs, oldVal, newVal) -> handleTextFieldFocusChange(newVal));
        ricercaTextField.textProperty().addListener((observable, oldValue, newValue) -> updateselezionaMappaButtonStates());
    }

    private void handleTextFieldFocusChange(boolean newVal) {
        if (newVal) {
            ricercaTextField.setPromptText("");
        }
    }

    private void updateselezionaMappaButtonStates() {
        boolean isRicercaTextFieldVuoto = ricercaTextField.getText().isEmpty();
        selezionaMappaButton.setDisable(!isRicercaTextFieldVuoto);
    }

    private void openLoginPage() {
        loadScene("/com/dietiestates25ui/view/login-view.fxml", (fxmlLoader, stage) -> {
        }, tornaIndietroButton, "/com/dietiestates25ui/styles/login-style.css");
    }

    private void handleSelezionaMappaButtonAction() {
        loadScene("/com/dietiestates25ui/view/ricerca-con-mappa-view.fxml", (fxmlLoader, stage) -> {
            RicercaConMappaController ricercaConMappaController = fxmlLoader.getController();
            ricercaConMappaController.setStage(currentStage);
            ricercaConMappaController.setTipologiaSelezionata(tipologiaMenuButton.getText());
            if (venditaButton.isSelected()) {
                ricercaConMappaController.setVenditaSelezionato(true);
                ricercaConMappaController.setAffittoSelezionato(false);
            } else {
                ricercaConMappaController.setVenditaSelezionato(false);
                ricercaConMappaController.setAffittoSelezionato(true);
            }
        }, selezionaMappaButton, "/com/dietiestates25ui/styles/ricerca-con-mappa-style.css");
    }

    private void handleCercaButtonAction() {
        String citta = ricercaTextField.getText();
        if (citta == null || citta.isEmpty()) {
            showPopup("Attenzione", "Inserisci una città per effettuare la ricerca.", ERROR_ICON);
            return;
        }

        FiltroAnnunci filtro = new FiltroAnnunci();
        filtro.setTipo(venditaButton.isSelected() ? "Vendita" : "Affitto");
        filtro.setTipologia(tipologiaMenuButton.getText());

        CompletableFuture.supplyAsync(() -> {
                    try {
                        return annuncioService.searchAnnunciByCittaAndFiltro(citta, filtro, token);
                    } catch (GenericServiceException e) {
                        logger.error("Errore durante la ricerca degli annunci: {}", e.getMessage(), e);
                        Platform.runLater(() -> showPopup(POPUP_ERROR_TITLE, "Errore durante la ricerca: " + e.getMessage(), ERROR_ICON));
                        return null;
                    }
                })
                .thenAccept(annunciDTO ->
                    Platform.runLater(() ->
                        handleAnnunci(citta, filtro, annunciDTO)))
                .exceptionally(ex -> {
                    logger.error("Errore durante la chiamata al servizio: {}", ex.getMessage(), ex);
                    Platform.runLater(() -> showPopup(POPUP_ERROR_TITLE, "Errore imprevisto: " + ex.getMessage(), ERROR_ICON));
                    return null;
                });
    }

    private void handleAnnunci(String citta, FiltroAnnunci filtro, List<AnnuncioDTO> annunciDTOs) {
        if (annunciDTOs == null) {
            return;
        }
        if (annunciDTOs.isEmpty()) {
            showPopup(POPUP_ERROR_TITLE, "Nessun immobile trovato con queste caratteristiche", ERROR_ICON);
            logger.info("Nessun immobile trovato con queste caratteristiche");
        } else {
            openRisultatiRicercaPage(citta, filtro, cercaButton, null);
        }
    }

    @FXML
    void handleTipologiaMenuItemAction(ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        selectedTipologiaText = source.getText();
        tipologiaMenuButton.setText(selectedTipologiaText);
    }
}