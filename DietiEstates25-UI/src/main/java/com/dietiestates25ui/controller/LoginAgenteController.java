package com.dietiestates25ui.controller;

import com.dietiestates25.dto.AgenteDTO;
import com.dietiestates25.dto.AmministratoreDTO;
import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.handler.FormValidator;
import com.dietiestates25ui.model.AgenteImmobiliare;
import com.dietiestates25ui.model.AgenziaImmobiliare;
import com.dietiestates25ui.model.Amministratore;
import com.dietiestates25ui.service.AgenteService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class LoginAgenteController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(LoginAgenteController.class);

    @FXML
    private TextField emailTextField;

    @FXML
    private Button loginButton;

    @FXML
    private PasswordField passwordPasswordField;

    @FXML
    private Button togglePasswordButton;

    @FXML
    private ImageView eyeImageView;

    @FXML
    private HBox passwordHBox;

    @FXML
    private Button tornaIndietroButton;

    private TextField passwordTextField;

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

        passwordTextFieldInitializer();
        togglePasswordButton.setOnAction(event -> togglePasswordVisibility());
    }

    private void openSelectRolePage() {
        loadScene("/com/dietiestates25ui/view/select-role-view.fxml",
                (fxmlLoader, stage) -> {}, tornaIndietroButton, "/com/dietiestates25ui/styles/select-role-style.css");
    }

    private void passwordTextFieldInitializer() {
        passwordTextField = new TextField();
        passwordTextField.setPromptText(passwordPasswordField.getPromptText());
        passwordTextField.managedProperty().bind(passwordTextField.visibleProperty());
        passwordTextField.setVisible(false);
        passwordTextField.textProperty().bindBidirectional(passwordPasswordField.textProperty());
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            passwordHBox.getChildren().remove(passwordPasswordField);
            passwordHBox.getChildren().addFirst(passwordTextField);
            passwordTextField.setPrefWidth(passwordPasswordField.getWidth());

            passwordTextField.setVisible(true);

            eyeImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/dietiestates25ui/images/eye_open.png"))));
        } else {
            passwordHBox.getChildren().remove(passwordTextField);
            passwordHBox.getChildren().addFirst(passwordPasswordField);

            eyeImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/dietiestates25ui/images/eye_closed.png"))));
            passwordTextField.setVisible(false);
        }

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
        String email = emailTextField.getText().trim();
        String password = passwordPasswordField.getText().trim();
        AgenteImmobiliare agente = new AgenteImmobiliare(email, password);
        try {
            String token = agenteService.loginAgente(agente);
            if (token != null) {
                showPopup("Login effettuato con successo", "Reindirizzamento alla dashboard...", SUCCESS_ICON);
                logger.info("Login effettuato con successo. Token JWT: {}", token);

                PauseTransition delay = new PauseTransition(Duration.millis(POPUP_PAUSE));
                delay.setOnFinished(event -> {
                    AgenteDTO agenteDTO = null;
                    try {
                        agenteDTO = agenteService.getAgenteDetails(token);
                        agente.setIdAgenzia(agenteDTO.getIdAgenzia());
                        agente.setNome(agenteDTO.getNome());
                        agente.setCognome(agenteDTO.getCognome());
                        agente.setDataDiNascita(agenteDTO.getDataDiNascita());
                        agente.setSesso(agenteDTO.getSesso());
                        //openAgenteDashboard(token, agente);
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