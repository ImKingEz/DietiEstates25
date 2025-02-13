package com.dietiestates25ui.controller;

import com.dietiestates25ui.handler.FormValidator;
import com.dietiestates25ui.model.Amministratore;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.ResourceBundle;

public class RegisterAgenteController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(RegisterAgenteController.class);

    @FXML
    private TextField cognomeTextField;

    @FXML
    private TextField emailTextField;

    @FXML
    private ImageView eyeImageView;

    @FXML
    private Button indietroButton;

    @FXML
    private ImageView logo;

    @FXML
    private TextField nomeTextField;

    @FXML
    private HBox passwordHBox;

    @FXML
    private PasswordField passwordPasswordField;

    @FXML
    private Button registraButton;

    @FXML
    private Button togglePasswordButton;

    @FXML
    private DatePicker dataDatePicker;

    @FXML
    private SplitMenuButton sessoSplitMenuButton;

    @FXML
    private MenuItem maschioMenuItem;

    @FXML
    private MenuItem femminaMenuItem;

    @FXML
    private MenuItem nonBinarioMenuItem;

    private TextField passwordTextField;

    private boolean passwordVisible = false;

    private String token;

    private Amministratore amministratore;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> logo.requestFocus());
        Platform.runLater(() -> currentStage = (Stage) primaryAnchorPane.getScene().getWindow());

        updateRegistraButton();

        indietroButton.setOnAction(event -> openAreaAmministrativaPage());

        passwordTextFieldInitializer();
        togglePasswordButton.setOnAction(event -> togglePasswordVisibility());

        maschioMenuItem.setOnAction(event -> impostaSesso("Maschio"));
        femminaMenuItem.setOnAction(event -> impostaSesso("Femmina"));
        nonBinarioMenuItem.setOnAction(event -> impostaSesso("Non binario"));

        updateDatePicker();
    }

    private void impostaSesso(String sesso) {
        sessoSplitMenuButton.setText(sesso);
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

    private void updateDatePicker() {
        dataDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                LocalDate minBirthDate = today.minusYears(18);
                setDisable(empty || date.isAfter(minBirthDate));
            }
        });
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
        nomeTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForRegister());
        cognomeTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForRegister());
        dataDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> checkFieldsForRegister());
        sessoSplitMenuButton.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForRegister());
        emailTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForRegister());
        passwordPasswordField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForRegister());

        registraButton.setDisable(true);
    }

    private void registraAgente() {
        String nome = nomeTextField.getText().trim();
        String cognome = cognomeTextField.getText().trim();
        LocalDate dataNascita = dataDatePicker.getValue();
        String sesso = sessoSplitMenuButton.getText();
        String email = emailTextField.getText().trim();
        String password = passwordPasswordField.getText().trim();

        Platform.runLater(() -> nomeTextField.getParent().requestFocus());

        //TODO: Implementare la registrazione dell'agente
    }

    private void checkFieldsForRegister() {
        String nome = nomeTextField.getText().trim();
        String cognome = cognomeTextField.getText().trim();
        LocalDate dataNascita = dataDatePicker.getValue();
        String sesso = sessoSplitMenuButton.getText();
        String email = emailTextField.getText().trim();
        String password = passwordPasswordField.getText().trim();

        registraButton.setDisable(nome.isBlank() || cognome.isBlank() || dataNascita == null || sesso.equals("Sesso") || !FormValidator.isValidEmail(email) || !FormValidator.isValidPassword(password));
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setAmministratore(Amministratore admin) {
        this.amministratore = admin;
    }
}