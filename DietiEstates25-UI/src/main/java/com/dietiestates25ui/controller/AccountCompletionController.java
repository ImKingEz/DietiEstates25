package com.dietiestates25ui.controller;

import com.dietiestates25.dto.UtenteDTO;
import com.dietiestates25ui.model.Utente;
import com.dietiestates25ui.service.UtenteService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class AccountCompletionController extends AbstractController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(AccountCompletionController.class);

    @FXML
    private TextField cittaTextField;

    @FXML
    private TextField cognomeTextField;

    @FXML
    private TextField nomeTextField;

    @FXML
    private Button salvaButton;

    private UtenteService utenteService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> logo.requestFocus());

        utenteService = new UtenteService();
        salvaButton.setOnAction(event -> saveAccountDetails());

        updateSalvaButton();
        Platform.runLater(() -> currentStage = (Stage) primaryAnchorPane.getScene().getWindow());
    }

    private void updateSalvaButton() {
        nomeTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForSave());
        cognomeTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForSave());
        salvaButton.setDisable(true);
    }

    private void checkFieldsForSave() {
        String nome = nomeTextField.getText().trim();
        String cognome = cognomeTextField.getText().trim();
        salvaButton.setDisable(nome.isBlank() || cognome.isBlank());
    }

    private void saveAccountDetails() {
        Platform.runLater(() -> logo.requestFocus());

        String nome = nomeTextField.getText().trim();
        String cognome = cognomeTextField.getText().trim();
        String citta = cittaTextField.getText().trim();

        if (!isValidAccountDetails(nome, cognome)) {
            showPopup(POPUP_ERROR_TITLE, "Nome e Cognome non possono essere vuoti.", ERROR_ICON);
            return;
        }

        Utente utente = createUtente(nome, cognome, citta);
        TokenManager.getInstance().setLoggedInUser(utente);

        try {
            utenteService.updateUtente(utente, token);
            salvaButton.setDisable(true);
            handleUtenteDetails(token);
        } catch (Exception e) {
            logAndShowUpdateError(e);
        }
    }

    private boolean isValidAccountDetails(String nome, String cognome) {
        return !nome.isBlank() && !cognome.isBlank();
    }

    private Utente createUtente(String nome, String cognome, String citta) {
        return citta.isBlank() ? new Utente(nome, cognome, null, null, null) : new Utente(nome, cognome, citta, null, null);
    }

    private void handleUtenteDetails(String token) {
        try {
            UtenteDTO utenteDTO = utenteService.getUtenteDetails(token);
            if (utenteDTO != null) {
                showSuccessAndRedirect();
            } else {
                logAndShowDetailsNotFoundError();
            }
        } catch (Exception e) {
            logAndShowDetailsError(e);
        }
    }

    private void showSuccessAndRedirect() {
        showPopup("Modifica completata!", "Reindirizzamento alla dashboard...", SUCCESS_ICON);
        salvaButton.setDisable(true);
        PauseTransition pause = new PauseTransition(Duration.millis(POPUP_PAUSE));
        pause.setOnFinished(e -> openHomepage(salvaButton));
        pause.play();
    }

    private void logAndShowDetailsNotFoundError() {
        logger.warn("Dati dell'utente non trovati");
        showPopup(POPUP_ERROR_TITLE, "Dati dell'utente non trovati", ERROR_ICON);
    }

    private void logAndShowDetailsError(Exception e) {
        logger.error("Errore durante il recupero dei dettagli dell'utente: {}", e.getMessage(), e);
        showPopup(POPUP_ERROR_TITLE, "Errore durante il recupero dei dettagli dell'utente.", ERROR_ICON);
    }

    private void logAndShowUpdateError(Exception e) {
        logger.error("Errore durante l'update dell'utente: {}", e.getMessage(), e);
        showPopup(POPUP_ERROR_TITLE, "Errore durante l'update dell'utente (" + e.getMessage() + ").", ERROR_ICON);
    }

    public void loadUserDetails(){
        try {
            UtenteDTO utente = utenteService.getUtenteDetails(token);
            if(utente != null){
                Platform.runLater(() ->{
                    nomeTextField.setText(utente.getNome());
                    cognomeTextField.setText(utente.getCognome());
                    if(utente.getCitta() != null)
                        cittaTextField.setText(utente.getCitta());
                });
            }
        } catch (Exception e){
            logger.error("Errore durante il caricamento delle informazioni dell'utente: {}", e.getMessage(), e);
            showPopup(POPUP_ERROR_TITLE, "Errore durante il caricamento delle informazioni dell'utente", ERROR_ICON);
        }
    }
}