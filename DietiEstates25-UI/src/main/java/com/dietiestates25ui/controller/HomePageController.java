package com.dietiestates25ui.controller;

import com.dietiestates25.dto.AnnuncioDTO;
import com.dietiestates25.dto.UtenteDTO;
import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.model.Utente;
import com.dietiestates25ui.service.AnnuncioService;
import com.dietiestates25ui.service.UtenteService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
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
    private HBox profileHBox;
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
    private String token;

    private String selectedTipologiaText = "Appartamento"; // Imposta "Appartamento" come predefinito

    private Utente utente;
    private UtenteService utenteService = new UtenteService();

    private AnnuncioService annuncioService = new AnnuncioService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            logo.requestFocus();
            currentStage = (Stage) primaryAnchorPane.getScene().getWindow();
        });

        Platform.runLater(this::updateProfileHBox);

        tornaIndietroButton.setOnAction(event -> openLoginPage());
        selezionaMappaButton.setOnAction(event -> handleSelezionaMappaButtonAction());
        cercaButton.setOnAction(event -> handleCercaButtonAction());

        setupTextFieldListeners();

        setupTipologie();
    }

    private void setupTipologie() {
        venditaButton.setSelected(true);  // Seleziona "Vendita" come predefinito
        tipologiaMenuButton.setText(selectedTipologiaText); // Imposta il testo del MenuButton
        ricercaTextField.setPromptText("Effettua una ricerca inserendo una città");
        updateButtonStates();
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
        Text ciaoNome = new Text();
        ciaoNome.getStyleClass().add("profileName");
        ciaoNome.setText("Ciao " + utente.getNome());
        profileHBox.getChildren().addFirst(ciaoNome);
    }

    public void setToken(String token) {
        this.token = token;
        setUtente(token);
    }

    private void setupTextFieldListeners() {
        ricercaTextField.focusedProperty().addListener((obs, oldVal, newVal) -> handleTextFieldFocusChange(newVal));
        ricercaTextField.textProperty().addListener((observable, oldValue, newValue) -> updateButtonStates());
    }

    private void handleTextFieldFocusChange(boolean newVal) {
        if (newVal) {
            ricercaTextField.setPromptText("");
        }
        // updateButtonStates(); // Chiama updateButtonStates anche qui
    }

    private void updateButtonStates() {
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
            ricercaConMappaController.setToken(token);
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

        if (citta != null && !citta.isEmpty()) {
            // Effettua la chiamata asincrona al backend
            CompletableFuture<List<AnnuncioDTO>> futureAnnunci = CompletableFuture.supplyAsync(() -> {
                try {
                    return annuncioService.searchAnnunciByCitta(citta, token);
                } catch (GenericServiceException e) {
                    logger.error("Errore durante la ricerca degli annunci: {}", e.getMessage(), e);
                    Platform.runLater(() -> showPopup("Errore", "Errore durante la ricerca: " + e.getMessage(), ERROR_ICON));
                    return null;
                }
            });

            // Gestisci il risultato quando è disponibile
            futureAnnunci.thenAccept(annunci -> {
                if (annunci != null) {
                    // Aggiorna l'interfaccia utente con i risultati
                    Platform.runLater(() -> {
                        if (annunci.isEmpty()) {
                            showPopup("Nessun risultato", "Nessun annuncio trovato per la città di " + citta, null);
                        } else {
                            // Mostra i risultati (per ora nella console, poi nell'UI)
                            annunci.forEach(annuncio -> {
                                System.out.println("Titolo: " + annuncio.getTitolo());
                                System.out.println("idImmobile: " + annuncio.getIdImmobile());
                                //Ora devo usare l'idImmobile per mostrare l'indirizzo dell'immobile
                            });
                        }
                    });
                }
            });

            futureAnnunci.exceptionally(ex -> {
                logger.error("Errore durante la chiamata al servizio: {}", ex.getMessage(), ex);
                Platform.runLater(() -> showPopup("Errore", "Errore imprevisto: " + ex.getMessage(), ERROR_ICON));
                return null;
            });
        } else {
            showPopup("Attenzione", "Inserisci una città per effettuare la ricerca.", ERROR_ICON);
        }
    }

    @FXML
    void handleTipologiaMenuItemAction(ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        selectedTipologiaText = source.getText();
        tipologiaMenuButton.setText(selectedTipologiaText);
    }
}