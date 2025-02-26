package com.dietiestates25ui.controller;

import com.dietiestates25ui.MainApplication;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.util.Objects;

public abstract class AbstractController {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractController.class);

    public static final String APP_TITLE = "DietiEstates25";
    public static final String ERROR_ICON = "/com/dietiestates25ui/images/errorIcon.png";
    public static final String SUCCESS_ICON = "/com/dietiestates25ui/images/successIcon.png";
    public static final String POPUP_ERROR_TITLE = "Errore!";
    public static final int POPUP_DURATION = 3000;
    public static final int FADEINOUT_DURATION = 200;
    public static final int POPUP_PAUSE = POPUP_DURATION + FADEINOUT_DURATION * 2;
    protected static final String PIANO_TERRA = "Piano terra";
    protected static final String PIANO_INTERMEDIO = "Piano intermedio";
    protected static final String ULTIMO_PIANO = "Ultimo piano";

    @FXML
    protected AnchorPane primaryAnchorPane;

    protected Stage currentStage;

    @FXML
    protected ImageView logo;

    @FXML
    protected WebView webView;

    protected WebEngine webEngine;

    @FXML
    protected Button providerBackButton;

    @FXML
    protected TextField passwordTextField;

    @FXML
    protected HBox passwordHBox;

    @FXML
    protected PasswordField passwordPasswordField;

    @FXML
    protected ImageView eyeImageView;

    public Stage getCurrentStage() {
        return currentStage;
    }

    public void setStage(Stage stage) {
        this.currentStage = stage;
    }

    protected void createAndPlaceBackButton() {
        providerBackButton = new Button("Torna indietro");
        providerBackButton.getStyleClass().add("generalButton");
        providerBackButton.setVisible(false);
        providerBackButton.setOnAction(e -> hideWebView());
        AnchorPane.setBottomAnchor(providerBackButton, 10.0);
        AnchorPane.setLeftAnchor(providerBackButton, 10.0);
        primaryAnchorPane.getChildren().add(providerBackButton);
    }

    protected void hideWebView() {
        webView.setVisible(false);
        providerBackButton.setVisible(false);
        webEngine.load(null);
    }

    public void showPopup(String title, String message, String imagePath) {
        Popup popup = new Popup();
        HBox popupContent = new HBox();
        popupContent.setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-border-color: black; -fx-border-width: 1px; -fx-alignment: center; -fx-spacing: 10px; -fx-border-radius: 10px; -fx-background-radius: 10px;");

        Text titleText = new Text(title);
        titleText.setStyle("-fx-font-weight: bold;");

        Text messageText = new Text(message);

        VBox textPopupVBox = new VBox();
        textPopupVBox.getChildren().addAll(titleText, messageText);

        ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));
        imageView.setFitHeight(30);
        imageView.setFitWidth(30);

        popupContent.getChildren().addAll(textPopupVBox, imageView);

        popup.getContent().add(popupContent);
        popup.setAutoHide(true);

        popupContent.setEffect(new GaussianBlur(0));
        popup.show(currentStage);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(FADEINOUT_DURATION), popupContent);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition pause = new PauseTransition(Duration.millis(POPUP_DURATION));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(FADEINOUT_DURATION), popupContent);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        fadeIn.setOnFinished(e -> pause.play());
        pause.setOnFinished(e -> fadeOut.play());
        fadeOut.setOnFinished(e -> popup.hide());

        Platform.runLater(() -> {
            double popupWidth = popupContent.getWidth();
            if (popupWidth <= 0) {
                popupWidth = 300;
            }

            double centerX = currentStage.getX() + (currentStage.getWidth() / 2);
            double popupX = centerX - (popupWidth / 2);

            double popupY;
            if (logo != null) {  // **Aggiunto controllo null**
                Bounds logoBounds = logo.localToScene(logo.getBoundsInLocal());
                double logoBottomY = logoBounds.getMinY();
                popupY = logoBottomY - 10;
            } else {
                popupY = currentStage.getY() + 10;
            }

            popup.hide();
            popup.show(currentStage, popupX, popupY);

            fadeIn.play();
        });
    }

    public void openDashboard(String token) {
        openDashboard(token, null);
    }

    public void openDashboard(String token, Button button) {
        loadScene("/com/dietiestates25ui/view/dashboard-view.fxml",
                (fxmlLoader, stage) -> {
                    DashboardController dashboardController = fxmlLoader.getController();
                    dashboardController.setStage(stage); // Chiama setStage anziché modificare currentStage
                    dashboardController.setToken(token);
                }, button, "/com/dietiestates25ui/styles/dashboard-style.css");
    }

    protected void loadScene(String fxmlPath, SceneConfigurator sceneConfigurator, Button sourceButton, String stylesheetPath) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource(fxmlPath));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage;

            if (sourceButton != null) {
                stage = (Stage) sourceButton.getScene().getWindow();

                stage.setWidth(stage.getWidth());
                stage.setHeight(stage.getHeight());
                stage.setX(stage.getX());
                stage.setY(stage.getY());
            } else {
                stage = currentStage;
            }

            AbstractController controller = fxmlLoader.getController();
            if(controller != null) {
                controller.setStage(stage);
            }

            if (stylesheetPath != null && !stylesheetPath.isEmpty()) {
                scene.getStylesheets().add(getClass().getResource(stylesheetPath).toExternalForm());
            }

            sceneConfigurator.configure(fxmlLoader, stage);

            stage.setTitle(APP_TITLE);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            logger.error("Errore durante il caricamento della scena: {}", e.getMessage(), e);
            showPopup("Errore durante il caricamento della scena", e.getMessage(), ERROR_ICON);
        }
    }


    @FunctionalInterface
    public interface SceneConfigurator {
        void configure(FXMLLoader fxmlLoader, Stage stage);
    }

    protected void passwordTextFieldInitializer(String classOfTextField) {
        passwordTextField = new TextField();
        passwordTextField.getStyleClass().add(classOfTextField);
        passwordTextField.setPromptText(passwordPasswordField.getPromptText());
        passwordTextField.managedProperty().bind(passwordTextField.visibleProperty());
        passwordTextField.setVisible(false);
        passwordTextField.textProperty().bindBidirectional(passwordPasswordField.textProperty());
    }

    protected boolean togglePasswordVisibility(boolean passwordVisible) {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            if (!passwordHBox.getChildren().contains(passwordTextField)) {
                passwordHBox.getChildren().remove(passwordPasswordField);
                passwordHBox.getChildren().addFirst(passwordTextField);
                passwordTextField.setPrefWidth(passwordPasswordField.getWidth());
            }
            passwordTextField.setVisible(true);
            eyeImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/dietiestates25ui/images/eye_open.png"))));
        } else {
            if (!passwordHBox.getChildren().contains(passwordPasswordField)) {
                passwordHBox.getChildren().remove(passwordTextField);
                passwordHBox.getChildren().addFirst(passwordPasswordField);
            }
            eyeImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/dietiestates25ui/images/eye_closed.png"))));
            passwordTextField.setVisible(false);
        }
        return passwordVisible;
    }
}