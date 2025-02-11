package com.dietiestates25ui.controller;

import com.dietiestates25ui.MainApplication;
import com.dietiestates25ui.handler.FormValidator;
import com.dietiestates25ui.model.AgenziaImmobiliare;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class RegisterAgenziaController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(RegisterAgenziaController.class);

    @FXML
    private TextField emailTextField;

    @FXML
    private Button indietroButton;

    @FXML
    private TextField indirizzoTextField;

    @FXML
    private ImageView logo;

    @FXML
    private TextField logoTextField;

    @FXML
    private TextField nomeTextField;

    @FXML
    private TextField partitaIVATextField;

    @FXML
    private Button proseguiButton;

    @FXML
    private TextField telefonoTextField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> logo.requestFocus());

        updateProseguiButton();

        proseguiButton.setOnAction(event -> openAgenziaCredentialsPage());

        indietroButton.setOnAction(event -> openLoginAmmnistratorePage());
    }

    private void openLoginAmmnistratorePage() {
        loadScene("/com/dietiestates25ui/view/login-amministratore-view.fxml",
                (fxmlLoader, stage) -> {}, indietroButton, "/com/dietiestates25ui/styles/login-amministratore-style.css");
    }

    private void openAgenziaCredentialsPage() {
        String nome = nomeTextField.getText().trim();
        String partitaIVA = partitaIVATextField.getText().trim();
        String indirizzo = indirizzoTextField.getText().trim();
        String email = emailTextField.getText().trim();
        String telefono = telefonoTextField.getText().trim();
        String logo = logoTextField.getText().trim(); // Anche il logo!

        logger.info("Nome: {}, Partita IVA: {}, Indirizzo: {}, Email: {}, Telefono: {}", nome, partitaIVA, indirizzo, email, telefono);

        AgenziaImmobiliare agenzia = new AgenziaImmobiliare(nome, partitaIVA, indirizzo, email, telefono, logo);

        loadScene("/com/dietiestates25ui/view/agenzia-credentials-view.fxml",
                (fxmlLoader, stage) -> {
                    try {
                        AgenziaCredentialsController controller = fxmlLoader.getController();
                        if (controller != null) {
                            controller.setAgenzia(agenzia);
                            controller.setStage(stage);
                            controller.initializeData();
                        } else {
                            logger.error("Controller is null after FXMLLoader.getController()");
                        }
                    } catch (Exception e) {
                        logger.error("Exception during controller setup: ", e);
                    }
                }, proseguiButton, "/com/dietiestates25ui/styles/agenzia-credentials-style.css");
    }

    private void updateProseguiButton() {
        nomeTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForContinue());
        partitaIVATextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForContinue());
        indirizzoTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForContinue());
        emailTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForContinue());
        telefonoTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForContinue());
        logoTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForContinue());

        proseguiButton.setDisable(true);
    }

    private void checkFieldsForContinue() {
        String nome = nomeTextField.getText().trim();
        String partitaIVA = partitaIVATextField.getText().trim();
        String indirizzo = indirizzoTextField.getText().trim();
        String email = emailTextField.getText().trim();
        String telefono = telefonoTextField.getText().trim();
        String logo = logoTextField.getText().trim();
        //TODO Implementare il controllo per il logo, partita iva, indirizzo e telefono.

        proseguiButton.setDisable(nome.isBlank() || partitaIVA.isBlank() || indirizzo.isBlank() || !FormValidator.isValidEmail(email) || telefono.isBlank() || logo.isBlank());
    }
}