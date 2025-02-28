package com.dietiestates25ui.controller;

import com.dietiestates25ui.controller.CustomDatePicker;
import com.dietiestates25ui.handler.FormValidator;
import com.dietiestates25ui.model.AgenteImmobiliare;
import com.dietiestates25ui.model.Amministratore;
import com.dietiestates25ui.service.AgenteService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class RegisterAgenteController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(RegisterAgenteController.class);

    @FXML
    private TextField cognomeTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private Button indietroButton;
    @FXML
    private TextField nomeTextField;
    @FXML
    private Button registraButton;
    @FXML
    private Button togglePasswordButton;

    // Utilizziamo il CustomDatePicker al posto del DatePicker standard
    @FXML
    private CustomDatePicker dataDatePicker;

    @FXML
    private MenuButton sessoMenuButton;
    @FXML
    private MenuItem maschioMenuItem;
    @FXML
    private MenuItem femminaMenuItem;
    @FXML
    private MenuItem nonBinarioMenuItem;

    private boolean passwordVisible = false;
    private final AgenteService agenteService = new AgenteService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> logo.requestFocus());
        Platform.runLater(() -> currentStage = (Stage) primaryAnchorPane.getScene().getWindow());

        updateRegistraButton();

        indietroButton.setOnAction(event -> openAreaAmministrativaPage());
        registraButton.setOnAction(event -> registraAgente());
        passwordTextFieldInitializer("registerField");
        togglePasswordButton.setOnAction(event -> passwordVisible = togglePasswordVisibility(passwordVisible));

        maschioMenuItem.setOnAction(event -> impostaSesso("Maschio"));
        femminaMenuItem.setOnAction(event -> impostaSesso("Femmina"));
        nonBinarioMenuItem.setOnAction(event -> impostaSesso("Non binario"));

        // Non serve più chiamare updateDatePicker() perché il controllo custom gestisce il dialogo e il formato
        // updateDatePicker();
    }

    private void impostaSesso(String sesso) {
        sessoMenuButton.setText(sesso);
        sessoMenuButton.setStyle("-fx-text-fill: black;");
    }

    private void openAreaAmministrativaPage() {
        loadScene("/com/dietiestates25ui/view/area-amministrativa-view.fxml",
                (fxmlLoader, stage) -> {
                    AreaAmministrativaController controller = fxmlLoader.getController();
                    controller.setStage(stage);
                    controller.setAmministratore(amministratore);
                }, registraButton, "/com/dietiestates25ui/styles/area-amministrativa-style.css");
    }

    @FXML
    private void registraAgente() {
        String nome = nomeTextField.getText().trim();
        String cognome = cognomeTextField.getText().trim();
        LocalDate dataNascita = dataDatePicker.getValue(); // Viene restituito in formato standard (es. yyyy-MM-dd)
        String sesso = sessoMenuButton.getText();
        String email = emailTextField.getText().trim();
        String password = passwordPasswordField.getText().trim();

        Platform.runLater(() -> nomeTextField.getParent().requestFocus());

        AgenteImmobiliare agente = new AgenteImmobiliare(nome, cognome, dataNascita, sesso, email, password, amministratore.getIdAgenzia());
        logger.info("Registrazione di {} {} {} {} {} {} {}", nome, cognome, dataNascita, sesso, email, password, amministratore.getIdAgenzia());

        try {
            agenteService.registraAgente(agente, token);
            registraButton.setDisable(true);
            indietroButton.setDisable(true);
            showPopup("Registrazione completata!", "Reindirizzamento all'area amministrativa...", SUCCESS_ICON);
            PauseTransition delay = new PauseTransition(Duration.millis(POPUP_PAUSE));
            delay.setOnFinished(event -> openAreaAmministrativaPage());
            delay.play();
        } catch (Exception e) {
            logger.error("Errore durante la registrazione dell'agente", e);
            showPopup(POPUP_ERROR_TITLE, e.getMessage(), ERROR_ICON);
        }
    }

    private void updateRegistraButton() {
        nomeTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForRegister());
        cognomeTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForRegister());
        dataDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> checkFieldsForRegister());
        sessoMenuButton.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForRegister());
        emailTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForRegister());
        passwordPasswordField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForRegister());

        registraButton.setDisable(true);
    }

    private void checkFieldsForRegister() {
        String nome = nomeTextField.getText().trim();
        String cognome = cognomeTextField.getText().trim();
        LocalDate dataNascita = dataDatePicker.getValue();
        String sesso = sessoMenuButton.getText();
        String email = emailTextField.getText().trim();
        String password = passwordPasswordField.getText().trim();

        registraButton.setDisable(nome.isBlank() || cognome.isBlank() || dataNascita == null ||
                sesso.equals("Sesso") || !FormValidator.isValidEmail(email) || !FormValidator.isValidPassword(password));
    }
}
