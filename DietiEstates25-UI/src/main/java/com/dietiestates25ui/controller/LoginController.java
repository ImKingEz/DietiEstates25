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

public class LoginController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML
    private TextField emailTextField;

    @FXML
    private Button facebookButton;

    @FXML
    private Button githubButton;

    @FXML
    private Button googleButton;

    @FXML
    private Button loginButton;

    @FXML
    private PasswordField passwordPasswordField;

    @FXML
    private Button registratiButton;

    @FXML
    private Button togglePasswordButton;

    @FXML
    private ImageView eyeImageView;

    @FXML
    private HBox passwordHBox;

    @FXML
    private Button agenziaImmobiliareButton;

    private TextField passwordTextField;

    private UtenteService utenteService;

    private OAuth2Handler oAuth2Handler;

    private boolean passwordVisible = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            logo.requestFocus();
            currentStage = (Stage) primaryAnchorPane.getScene().getWindow();
        });

        updateLoginButton();
        loginButton.setOnAction(event -> loginUtente());
        registratiButton.setOnAction(event -> openRegisterPage());

        utenteService = new UtenteService();

        oAuth2Handler = new OAuth2Handler(this, loginButton);
        actionButtonProvider();

        createAndPlaceBackButton();

        updateWebView();

        passwordTextFieldInitializer();
        togglePasswordButton.setOnAction(event -> togglePasswordVisibility());

        agenziaImmobiliareButton.setOnAction(event -> openSelectRolePage());
    }

    private void openSelectRolePage() {
        loadScene("/com/dietiestates25ui/view/select-role-view.fxml",
                (fxmlLoader, stage) -> {}, agenziaImmobiliareButton, "/com/dietiestates25ui/styles/select-role-style.css");
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

    private void updateWebView() {
        webView.setVisible(false);
        webEngine = webView.getEngine();

        webEngine.locationProperty().addListener((observable, oldValue, newValue) -> oAuth2Handler.handleOAuthRedirect(newValue));
        webEngine.loadContent(
                """
                <script>
                    window.onload = function() {
                      if(window.location.href.includes('oauth2/success?token=') || window.location.href.includes('oauth2/error?error=') || window.location.href.includes('oauth2/firstlogin?token=')){
                          window.location.href = window.location.href;
                      }
                    }
                </script>
                """
        );
    }


    private void actionButtonProvider() {
        googleButton.setOnAction(event -> loginWithProvider("google"));
        facebookButton.setOnAction(event -> loginWithProvider("facebook"));
        githubButton.setOnAction(event -> loginWithProvider("github"));
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

    private void loginUtente() {
        String email = emailTextField.getText().trim();
        String password = passwordPasswordField.getText().trim();
        Utente user = new Utente(email, password);
        try {
            String token = utenteService.loginUtente(user);
            if (token != null) {
                showPopup("Login effettuato con successo", "Reindirizzamento alla dashboard...", SUCCESS_ICON);
                logger.info("Login effettuato con successo. Token JWT: {}", token);

                PauseTransition delay = new PauseTransition(Duration.millis(POPUP_PAUSE));
                delay.setOnFinished(event -> openDashboard(token, loginButton));
                delay.play();
            }
        } catch (Exception e) {
            logger.error("Errore durante il login: {}", e.getMessage());
            showPopup("Errore durante il login", e.getMessage(), ERROR_ICON);
        }
    }



    private void loginWithProvider(String provider) {
        try {
            webView.setVisible(true);
            providerBackButton.setVisible(true);
            webEngine.load("http://localhost:8080/oauth2/authorization/" + provider);
        } catch (Exception e) {
            logger.error("Errore durante il login con provider: {}", e.getMessage());
            showPopup("Errore durante il login con provider", e.getMessage(), ERROR_ICON);
        }
    }


    private void openRegisterPage() {
        loadScene("/com/dietiestates25ui/view/register-view.fxml",
                (fxmlLoader, stage) -> {}, registratiButton, "/com/dietiestates25ui/styles/register-style.css");
    }
}