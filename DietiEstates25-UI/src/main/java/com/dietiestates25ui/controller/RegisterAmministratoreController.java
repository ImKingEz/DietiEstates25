package com.dietiestates25ui.controller;

import com.dietiestates25ui.handler.FormValidator;
import com.dietiestates25ui.model.Amministratore;
import com.dietiestates25ui.service.AmministratoreService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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

public class RegisterAmministratoreController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(RegisterAmministratoreController.class);

    @FXML
    private TextField emailTextField;

    @FXML
    private ImageView eyeImageView;

    @FXML
    private Button indietroButton;

    @FXML
    private HBox passwordHBox;

    @FXML
    private PasswordField passwordPasswordField;

    @FXML
    private Button registraButton;

    @FXML
    private Button togglePasswordButton;

    private TextField passwordTextField;

    private boolean passwordVisible = false;

    private String token;

    private Amministratore amministratore;

    private final AmministratoreService amministratoreService = new AmministratoreService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> logo.requestFocus());
        Platform.runLater(() -> currentStage = (Stage) primaryAnchorPane.getScene().getWindow());

        updateRegistraButton();

        indietroButton.setOnAction(event -> openAreaAmministrativaPage());

        registraButton.setOnAction(event -> registraAgente());

        passwordTextFieldInitializer();
        togglePasswordButton.setOnAction(event -> togglePasswordVisibility());
    }

    private void openAreaAmministrativaPage() {
        loadScene("/com/dietiestates25ui/view/area-amministrativa-view.fxml",
                (fxmlLoader, stage) -> {
                    AreaAmministrativaController controller = fxmlLoader.getController();
                    controller.setStage(stage);
                    controller.setToken(token);
                    controller.setAmministratore(amministratore);
                }, registraButton, "/com/dietiestates25ui/styles/area-amministrativa-style.css");
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
            passwordHBox.getChildren().remove(passwordPasswordField);
            passwordHBox.getChildren().add(1, passwordTextField);

            passwordTextField.setVisible(true);

            eyeImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/dietiestates25ui/images/eye_open.png"))));
        } else {
            passwordHBox.getChildren().remove(passwordTextField);
            passwordHBox.getChildren().add(1, passwordPasswordField);

            eyeImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/dietiestates25ui/images/eye_closed.png"))));
            passwordTextField.setVisible(false);
        }
    }

    private void updateRegistraButton() {
        emailTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForRegister());
        passwordPasswordField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForRegister());

        registraButton.setDisable(true);
    }

    @FXML
    private void registraAgente() {
        String email = emailTextField.getText().trim();
        String password = passwordPasswordField.getText().trim();

        Platform.runLater(() -> logo.getParent().requestFocus());

        Amministratore newAmministratore = new Amministratore(email, password, amministratore.getIdAgenzia());
        logger.info("Registrazione dell'amministratore {} {} {}", email, password, amministratore.getIdAgenzia());

        try {
            amministratoreService.registraAmministratore(newAmministratore);
            registraButton.setDisable(true);
            indietroButton.setDisable(true);
            showPopup("Registrazione completata!", "Reindirizzamento all'area amministrativa...", SUCCESS_ICON);
            PauseTransition delay = new PauseTransition(Duration.millis(POPUP_PAUSE));
            delay.setOnFinished(event -> openAreaAmministrativaPage());
            delay.play();
        } catch (Exception e) {
            logger.error("Errore durante la registrazione dell'amministratore", e);
            showPopup(POPUP_ERROR_TITLE, e.getMessage(), ERROR_ICON);
        }
    }

    private void checkFieldsForRegister() {
        String email = emailTextField.getText().trim();
        String password = passwordPasswordField.getText().trim();

        registraButton.setDisable(!FormValidator.isValidEmail(email) || !FormValidator.isValidPassword(password));
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setAmministratore(Amministratore admin) {
        this.amministratore = admin;
    }
}