package com.dietiestates25ui.controller;

import com.dietiestates25ui.handler.FormValidator;
import com.dietiestates25ui.model.AgenziaImmobiliare;
import com.dietiestates25ui.model.Amministratore;
import com.dietiestates25ui.service.AgenziaService;
import com.dietiestates25ui.service.AmministratoreService;
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
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class AgenziaCredentialsController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(AgenziaCredentialsController.class);

    private AgenziaImmobiliare agenzia;

    @FXML
    private TextField emailTextField;

    @FXML
    private ImageView eyeImageView;

    @FXML
    private Button indietroButton;

    @FXML
    private ImageView logo;

    @FXML
    private HBox passwordHBox;

    @FXML
    private PasswordField passwordPasswordField;

    @FXML
    private Button confermaButton;

    @FXML
    private Button togglePasswordButton;

    @FXML
    private TextField passwordTextField;

    private boolean passwordVisible = false;

    AgenziaService agenziaService;

    AmministratoreService amministratoreService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> logo.requestFocus());

        updateConfermaButton();

        agenziaService = new AgenziaService();
        amministratoreService = new AmministratoreService();
        confermaButton.setOnAction(event -> registraAgenzia());

        indietroButton.setOnAction(event -> openRegisterAgenziaPage());

        passwordTextFieldInitializer();
        togglePasswordButton.setOnAction(event -> togglePasswordVisibility());
    }

    public void initializeData() {
        logger.info("initializeData() called in AgenziaCredentialsController");
        if (agenzia != null) {
            logger.debug("Agenzia email is: ", agenzia.getEmail());
            Platform.runLater(() -> emailTextField.setText(agenzia.getEmail()));
        } else {
            logger.error("Agenzia is null");
        }
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

    private void openRegisterAgenziaPage() {
        loadScene("/com/dietiestates25ui/view/register-agenzia-view.fxml",
                (fxmlLoader, stage) -> {}, indietroButton, "/com/dietiestates25ui/styles/register-style.css");
    }

    private void registraAgenzia() {
        Platform.runLater(() -> logo.getParent().requestFocus());

        try {
            logger.info("Chiamata a agenziaService.registraAgenzia()");
            agenziaService.registraAgenzia(agenzia);
            logger.info("Agenzia registrata con successo");
            Amministratore amministratore = new Amministratore(agenzia.getEmail(), passwordPasswordField.getText());
            amministratoreService.registraAmministratore(amministratore);
            logger.info("Amministratore registrato con successo");
            confermaButton.setDisable(true);
            indietroButton.setDisable(true);
            showPopup("Agenzia registrata con successo", "Registrazione completata", SUCCESS_ICON);
            PauseTransition delay = new PauseTransition(Duration.millis(POPUP_PAUSE));
            delay.setOnFinished(event -> openLoginAmministratorePage());
            delay.play();
        } catch (Exception e) {
            logger.error("Errore durante la registrazione: {}", e.getMessage());
            showPopup(POPUP_ERROR_TITLE, e.getMessage(), ERROR_ICON);
        }
    }

    private void openLoginAmministratorePage() {
        loadScene("/com/dietiestates25ui/view/login-amministratore-view.fxml",
                (fxmlLoader, stage) -> {}, indietroButton, "/com/dietiestates25ui/styles/login-amministratore-style.css");
    }

    private void updateConfermaButton() {
        emailTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldForConfirm());
        passwordPasswordField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldForConfirm());
        confermaButton.setDisable(true);
    }

    private void checkFieldForConfirm() {
        String email = emailTextField.getText().trim();
        String password = passwordPasswordField.getText().trim();
        confermaButton.setDisable(!FormValidator.isValidEmail(email) || !FormValidator.isValidPassword(password));
    }

    public void setAgenzia(AgenziaImmobiliare agenzia) {
        this.agenzia = agenzia;
    }
}