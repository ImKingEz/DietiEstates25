package com.dietiestates25ui.controller;

import com.dietiestates25.dto.AmministratoreDTO;
import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.handler.FormValidator;
import com.dietiestates25ui.model.Amministratore;
import com.dietiestates25ui.service.AmministratoreService;
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

public class LoginAmministratoreController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(LoginAmministratoreController.class);

    @FXML
    private TextField emailTextField;

    @FXML
    private Button loginButton;

    @FXML
    private Button togglePasswordButton;

    @FXML
    private Button registratiButton;

    @FXML
    private Button tornaIndietroButton;

    private boolean passwordVisible = false;

    private AmministratoreService amministratoreService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            logo.requestFocus();
            currentStage = (Stage) primaryAnchorPane.getScene().getWindow();
        });

        updateLoginButton();

        amministratoreService = new AmministratoreService();
        loginButton.setOnAction(event -> loginAmministratore());

        registratiButton.setOnAction(event -> openRegisterAgenziaPage());

        tornaIndietroButton.setOnAction(event -> openSelectRolePage());

        passwordTextFieldInitializer("loginField");
        togglePasswordButton.setOnAction(event -> passwordVisible = togglePasswordVisibility(passwordVisible));
    }

    private void openRegisterAgenziaPage() {
        loadScene("/com/dietiestates25ui/view/register-agenzia-view.fxml",
                (fxmlLoader, stage) -> {}, registratiButton, "/com/dietiestates25ui/styles/register-style.css");
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

    private void loginAmministratore() {
        String email = emailTextField.getText().trim();
        String password = passwordPasswordField.getText().trim();
        Amministratore admin = new Amministratore(email, password);
        try {
            String token = amministratoreService.loginAmministratore(admin);
            if (token != null) {
                showPopup("Login effettuato con successo", "Reindirizzamento alla dashboard...", SUCCESS_ICON);
                logger.info("Login effettuato con successo. Token JWT: {}", token);

                PauseTransition delay = new PauseTransition(Duration.millis(POPUP_PAUSE));
                delay.setOnFinished(event -> {
                    AmministratoreDTO adminDTO;
                    try {
                        adminDTO = amministratoreService.getAmministratoreDetails(token);
                        admin.setIdAgenzia(adminDTO.getIdAgenzia());
                        openAreaAmministrativaPage(token, admin);
                    } catch (GenericServiceException e) {
                        logger.error("Errore durante il recupero dei dati dell'amministratore: {}", e.getMessage());
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

    private void openAreaAmministrativaPage(String token, Amministratore admin) {
        loadScene("/com/dietiestates25ui/view/area-amministrativa-view.fxml",
                (fxmlLoader, stage) -> {
                    AreaAmministrativaController controller = fxmlLoader.getController();
                    controller.setStage(stage);
                    controller.setToken(token);
                    controller.setAmministratore(admin);
                }, loginButton, "/com/dietiestates25ui/styles/area-amministrativa-style.css");
    }
}