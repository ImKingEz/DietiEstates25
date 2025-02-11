package com.dietiestates25ui.controller;

import com.dietiestates25ui.handler.FormValidator;
import com.dietiestates25ui.handler.OAuth2Handler;
import com.dietiestates25ui.model.Utente;
import com.dietiestates25ui.service.UtenteService;
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

public class RegisterController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    @FXML
    private TextField cittaTextField;

    @FXML
    private TextField cognomeTextField;

    @FXML
    private TextField emailTextField;

    @FXML
    private TextField nomeTextField;

    @FXML
    private PasswordField passwordPasswordField;

    @FXML
    private Button registratiButton;

    @FXML
    private Button indietroButton;

    @FXML
    private Button googleButton;

    @FXML
    private Button facebookButton;

    @FXML
    private Button githubButton;

    @FXML
    private Button togglePasswordButton;

    @FXML
    private ImageView eyeImageView;

    @FXML
    private HBox passwordHBox;

    private TextField passwordTextField;

    private UtenteService utenteService;

    private OAuth2Handler oAuth2Handler;

    private boolean passwordVisible = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> logo.requestFocus());
        Platform.runLater(() -> currentStage = (Stage) primaryAnchorPane.getScene().getWindow());

        updateRegistratiButton();

        utenteService = new UtenteService();
        registratiButton.setOnAction(event -> registraUtente());

        indietroButton.setOnAction(event -> openLoginPage());

        createAndPlaceBackButton();

        OAuth2Handler oAuth2Handler = new OAuth2Handler(this, registratiButton);
        updateWebView(oAuth2Handler);

        actionButtonProvider();

        passwordTextFieldInitializer();
        togglePasswordButton.setOnAction(event -> togglePasswordVisibility());
    }

    private void passwordTextFieldInitializer() {
        passwordTextField = new TextField();
        passwordTextField.setPromptText(passwordPasswordField.getPromptText());
        passwordTextField.managedProperty().bind(passwordTextField.visibleProperty());
        passwordTextField.setVisible(false);
        passwordTextField.prefWidthProperty().bind(passwordPasswordField.widthProperty());
        passwordTextField.textProperty().bindBidirectional(passwordPasswordField.textProperty());
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            // Mostra la password
            passwordHBox.getChildren().remove(passwordPasswordField);
            passwordHBox.getChildren().add(1, passwordTextField);

            passwordTextField.setVisible(true);

            eyeImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/dietiestates25ui/images/eye_open.png"))));
        } else {
            // Nascondi la password

            passwordHBox.getChildren().remove(passwordTextField);
            passwordHBox.getChildren().add(1, passwordPasswordField);

            eyeImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/dietiestates25ui/images/eye_closed.png"))));
            passwordTextField.setVisible(false);
        }
    }

    private void updateWebView(OAuth2Handler oAuth2Handler) {
        webView.setVisible(false);
        webEngine = webView.getEngine();

        webEngine.locationProperty().addListener((observable, oldValue, newValue) -> oAuth2Handler.handleOAuthRedirect(newValue));
        webEngine.loadContent("""
                <script>
                    window.onload = function() {
                      if(window.location.href.includes('oauth2/success?token=') || window.location.href.includes('oauth2/error?error=') || window.location.href.includes('oauth2/firstlogin?token=')){
                          window.location.href = window.location.href;
                      }
                    }
                </script>
                """);
    }

    private void actionButtonProvider() {
        googleButton.setOnAction(event -> registerWithProvider("google"));
        facebookButton.setOnAction(event -> registerWithProvider("facebook"));
        githubButton.setOnAction(event -> registerWithProvider("github"));
    }

    private void registerWithProvider(String provider) {
        try {
            webView.setVisible(true);
            providerBackButton.setVisible(true);
            webEngine.load("http://localhost:8080/oauth2/authorization/" + provider);
        } catch (Exception e) {
            logger.error("Errore durante la registrazione con provider: {}", e.getMessage());
            showPopup("Errore durante la registrazione con provider", e.getMessage(), ERROR_ICON);
        }
    }

    private void updateRegistratiButton() {
        nomeTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForRegister());
        cognomeTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForRegister());
        emailTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForRegister());
        passwordPasswordField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForRegister());

        registratiButton.setDisable(true);
    }

    private void registraUtente() {
        String nome = nomeTextField.getText().trim();
        String cognome = cognomeTextField.getText().trim();
        String citta = cittaTextField.getText().trim();
        String email = emailTextField.getText().trim();
        String password = passwordPasswordField.getText().trim();

        Platform.runLater(() -> nomeTextField.getParent().requestFocus());

        Utente user;
        if (citta.isBlank()) {
            user = new Utente(nome, cognome, email, password);
        } else {
            user = new Utente(nome, cognome, citta, email, password);
        }
        try {
            logger.info("Chiamata a utenteService.registraUtente()");
            utenteService.registraUtente(user);
            logger.info("Registrazione effettuata con successo.");
            registratiButton.setDisable(true);
            indietroButton.setDisable(true);
            showPopup("Registrazione completata!", "Reindirizzamento al login...", SUCCESS_ICON);
            PauseTransition delay = new PauseTransition(Duration.millis(POPUP_PAUSE));
            delay.setOnFinished(event -> openLoginPage());
            delay.play();
        } catch (Exception e) {
            logger.error("Errore durante la registrazione: {}", e.getMessage());
            showPopup(POPUP_ERROR_TITLE, e.getMessage(), ERROR_ICON);
        }
    }

    private void checkFieldsForRegister() {
        String nome = nomeTextField.getText().trim();
        String cognome = cognomeTextField.getText().trim();
        String email = emailTextField.getText().trim();
        String password = passwordPasswordField.getText().trim();
        registratiButton.setDisable(nome.isBlank() || cognome.isBlank() || !FormValidator.isValidEmail(email) || !FormValidator.isValidPassword(password));
    }

    private void openLoginPage() {
        loadScene("/com/dietiestates25ui/view/login-view.fxml",
                (fxmlLoader, stage) -> {}, indietroButton, "/com/dietiestates25ui/styles/login-style.css");
    }
}