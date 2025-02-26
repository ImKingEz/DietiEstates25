package com.dietiestates25ui.controller;

import com.dietiestates25.dto.AgenteDTO;
import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.handler.FormValidator;
import com.dietiestates25ui.model.AgenteImmobiliare;
import com.dietiestates25ui.service.AgenteService;
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

public class LoginAgenteController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(LoginAgenteController.class);

    @FXML
    private TextField emailTextField;

    @FXML
    private Button loginButton;

    @FXML
    private Button togglePasswordButton;

    @FXML
    private Button tornaIndietroButton;

    private boolean passwordVisible = false;

    private AgenteService agenteService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            logo.requestFocus();
            currentStage = (Stage) primaryAnchorPane.getScene().getWindow();
        });

        updateLoginButton();

        agenteService = new AgenteService();
        loginButton.setOnAction(event -> loginAgente());

        tornaIndietroButton.setOnAction(event -> openSelectRolePage());

        passwordTextFieldInitializer("loginField");
        togglePasswordButton.setOnAction(event -> passwordVisible = togglePasswordVisibility(passwordVisible));
    }

    private void openSelectRolePage() {
        loadScene("/com/dietiestates25ui/view/select-role-view.fxml",
                (fxmlLoader, stage) -> {}, tornaIndietroButton, "/com/dietiestates25ui/styles/select-role-style.css");
    }

    private void updateLoginButton() {
        emailTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForLogin());
        passwordPasswordField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForLogin());
        loginButton.setDisable(true);
    }

    private void checkFieldsForLogin() {
        String email = emailTextField.getText().trim();
        String password = passwordPasswordField.getText().trim();
        loginButton.setDisable(!FormValidator.isValidEmail(email) || password.isBlank());
    }

    private void loginAgente() {
        Platform.runLater(logo::requestFocus);
        String email = emailTextField.getText().trim();
        String password = passwordPasswordField.getText().trim();
        AgenteImmobiliare agente = new AgenteImmobiliare(email, password);
        try {
            String token = agenteService.loginAgente(agente);
            if (token != null) {
                showPopup("Login effettuato con successo", "Reindirizzamento alla dashboard...", SUCCESS_ICON);
                logger.info("Login effettuato con successo. Token JWT: {}", token);

                tornaIndietroButton.setDisable(true);
                loginButton.setDisable(true);

                PauseTransition delay = new PauseTransition(Duration.millis(POPUP_PAUSE));
                delay.setOnFinished(event -> {
                    AgenteDTO agenteDTO;
                    try {
                        agenteDTO = agenteService.getAgenteDetails(token);
                        agente.setIdAgenzia(agenteDTO.getIdAgenzia());
                        agente.setNome(agenteDTO.getNome());
                        agente.setCognome(agenteDTO.getCognome());
                        agente.setDataDiNascita(agenteDTO.getDataDiNascita());
                        agente.setSesso(agenteDTO.getSesso());
                        openAgenteDashboard(token, agente);
                        logger.info("Dati dell'agente recuperati con successo: {} {} {} {} {}",
                                agente.getIdAgenzia(), agente.getNome(), agente.getCognome(), agente.getDataDiNascita(), agente.getSesso());
                    } catch (GenericServiceException e) {
                        logger.error("Errore durante il recupero dei dati dell'agente: {}", e.getMessage());
                        showPopup("Errore durante il login", e.getMessage(), ERROR_ICON);
                    }
                });
                delay.play();
            }
        } catch (Exception e) {
            logger.error("Errore durante il login: {}", e.getMessage());
            showPopup("Errore durante il login", e.getMessage(), ERROR_ICON);
        }
    }

    private void openAgenteDashboard(String token, AgenteImmobiliare agente) {
        loadScene("/com/dietiestates25ui/view/agente-dashboard-view.fxml",
                (fxmlLoader, stage) -> {
                    AgenteDashboardController controller = fxmlLoader.getController();
                    controller.setAgente(agente);
                    controller.setToken(token);
                }, loginButton, "/com/dietiestates25ui/styles/agente-dashboard-style.css");
    }
}
