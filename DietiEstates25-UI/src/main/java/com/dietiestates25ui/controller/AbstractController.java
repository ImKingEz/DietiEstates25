package com.dietiestates25ui.controller;

import com.dietiestates25.dto.AgenziaDTO;
import com.dietiestates25.dto.MapSearchDTO;
import com.dietiestates25ui.MainApplication;
import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.model.AgenteImmobiliare;
import com.dietiestates25ui.model.Amministratore;
import com.dietiestates25ui.model.FiltroAnnunci;
import com.dietiestates25ui.model.Utente;
import com.dietiestates25ui.service.AgenziaService;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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
    protected static final int MAX_FILE_SIZE = 2 * 1024 * 1024;

    @FXML
    protected AnchorPane primaryAnchorPane;

    protected Stage currentStage;

    private AnchorPane loadingOverlay;

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

    @FXML
    protected HBox profileHBox;
    
    protected Utente utente;
    
    protected Amministratore amministratore;
    
    protected AgenteImmobiliare agente;

    protected String token = TokenManager.getInstance().getToken();

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
            popupY = currentStage.getY() + 40;

            popup.hide();
            popup.show(currentStage, popupX, popupY);

            fadeIn.play();
        });
    }

    protected void openRisultatiRicercaPage(String citta, FiltroAnnunci filtro, Button button, MapSearchDTO mapSearchDTO) {
        loadScene("/com/dietiestates25ui/view/risultati-ricerca-view.fxml",
                (fxmlLoader, stage) -> {
                    RisultatiRicercaController controller = fxmlLoader.getController();
                    controller.setFiltroAnnunci(filtro, citta);
                    controller.setStage(currentStage);
                    controller.setMapSearchDTO(mapSearchDTO);
                }, button, "/com/dietiestates25ui/styles/risultati-ricerca-style.css");
    }

    public void openHomepage(Button button) {
        loadScene("/com/dietiestates25ui/view/homepage-view.fxml",
                (fxmlLoader, stage) -> {
                    HomePageController homePageController = fxmlLoader.getController();
                    homePageController.setStage(stage);
                }, button, "/com/dietiestates25ui/styles/homepage-style.css");
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

    protected void searchUserNameAndUpdateProfileHBox() {
        if (TokenManager.getInstance().getLoggedInUser() instanceof Utente u) {
            updateProfileHBox(u.getNome());
        } else if (TokenManager.getInstance().getLoggedInUser() instanceof Amministratore adm) {
            AgenziaService agenziaService = new AgenziaService();
            try {
                AgenziaDTO agenzia = agenziaService.getAgenziaDetails(adm.getIdAgenzia(), token);
                updateProfileHBox(agenzia.getNome());
            } catch (GenericServiceException e) {
                logger.error("Errore durante il recupero dei dati dell'agenzia: {}", e.getMessage());
            }
        } else if (TokenManager.getInstance().getLoggedInUser() instanceof AgenteImmobiliare age) {
            updateProfileHBox(age.getNome());
        } else {
            showPopup(POPUP_ERROR_TITLE, "Utente non valido.", ERROR_ICON);
            throw new IllegalStateException("Utente non valido.");
        }
    }

    protected void updateProfileHBox(String nome) {
        Text ciaoNome = new Text();
        ciaoNome.getStyleClass().add("profileName");
        ciaoNome.setText("Ciao " + nome);
        profileHBox.getChildren().addFirst(ciaoNome);
    }

    protected void setAmministratore(Amministratore user) {
        this.amministratore = user;
        this.agente = null;
        this.utente = null;
    }

    protected void setAgente(AgenteImmobiliare user) {
        this.agente = user;
        this.amministratore = null;
        this.utente = null;
    }

    protected void setUtente(Utente user) {
        this.utente = user;
        this.amministratore = null;
        this.agente = null;
    }
    protected void showLoadingIndicator() {
        if (loadingOverlay == null) {
            loadingOverlay = new AnchorPane();
            loadingOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
            loadingOverlay.prefWidthProperty().bind(primaryAnchorPane.widthProperty());
            loadingOverlay.prefHeightProperty().bind(primaryAnchorPane.heightProperty());

            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setMaxSize(100, 100);

            StackPane stack = new StackPane();
            stack.prefWidthProperty().bind(loadingOverlay.widthProperty());
            stack.prefHeightProperty().bind(loadingOverlay.heightProperty());
            stack.getChildren().add(progressIndicator);

            AnchorPane.setTopAnchor(stack, 0.0);
            AnchorPane.setBottomAnchor(stack, 0.0);
            AnchorPane.setLeftAnchor(stack, 0.0);
            AnchorPane.setRightAnchor(stack, 0.0);

            loadingOverlay.getChildren().add(stack);
        }
        if (!primaryAnchorPane.getChildren().contains(loadingOverlay)) {
            primaryAnchorPane.getChildren().add(loadingOverlay);
        }
        loadingOverlay.setVisible(true);
    }

    protected void hideLoadingIndicator() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(false);
            primaryAnchorPane.getChildren().remove(loadingOverlay);
        }
    }
}