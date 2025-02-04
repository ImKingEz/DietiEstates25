package dietiestates25ui.controller;

import dietiestates25ui.MainApplication;
import dietiestates25ui.model.Utente;
import dietiestates25ui.service.UtenteService;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    public static final int DOMAIN_MIN_LENGTH = 2;

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
    private AnchorPane primaryAnchorPane;

    @FXML
    private ImageView logo;

    @FXML
    private WebView webView;

    private UtenteService utenteService;

    private Stage currentStage;

    private WebEngine webEngine;

    private Button providerBackButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            logo.requestFocus();
            currentStage = (Stage) primaryAnchorPane.getScene().getWindow();
        });

        updateLoginButton();

        utenteService = new UtenteService();
        loginButton.setOnAction(event -> loginUtente());
        registratiButton.setOnAction(event -> openRegisterPage());

        actionButtonProvider();

        createAndPlaceBackButton();

        updateWebView();
    }

    private void showPopup(String text1, String text2, String imagePath, int popupDuration) {
        Popup popup = new Popup();
        HBox popupContent = new HBox();
        popupContent.setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-border-color: black; -fx-border-width: 1px; -fx-alignment: center; -fx-spacing: 10px; -fx-border-radius: 10px; -fx-background-radius: 10px;");

        Text titleText = new Text(text1);
        titleText.setStyle("-fx-font-weight: bold;");

        Text messageText = new Text(text2);

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

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), popupContent);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition pause = new PauseTransition(Duration.seconds(popupDuration));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), popupContent);
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

            Bounds logoBounds = logo.localToScene(logo.getBoundsInLocal());
            double logoBottomY = logoBounds.getMinY();
            double popupY = logoBottomY - 10;

            popup.hide();
            popup.show(currentStage, popupX, popupY);

            fadeIn.play();
        });
    }


    private void updateWebView() {
        webView.setVisible(false);
        webEngine = webView.getEngine();

        webEngine.locationProperty().addListener((observable, oldValue, newValue) -> redirectWebView(newValue));
        webEngine.loadContent("<script>\n" +
                "        window.onload = function() {\n" +
                "          if(window.location.href.includes('oauth2/success?token=') || window.location.href.includes('oauth2/error?error=') || window.location.href.includes('oauth2/firstlogin?token=')){\n" +
                "              window.location.href = window.location.href;\n" +
                "          }\n" +
                "        }\n" +
                "    </script>");
    }

    private void actionButtonProvider() {
        googleButton.setOnAction(event -> loginWithProvider("google"));
        facebookButton.setOnAction(event -> loginWithProvider("facebook"));
        githubButton.setOnAction(event -> loginWithProvider("github"));
    }

    private void redirectWebView(String newValue) {
        if (newValue != null && !newValue.isEmpty()) {
            if (newValue.contains("oauth2/success?token=")) {
                String token = parseTokenFromURL(newValue);
                if (token != null && !token.isBlank()) {
                    try {
                        openDashboard(token);
                    } catch (IOException e) {
                        System.out.println("Errore nel reindirizzamento alla dashboard");
                    }
                }
            } else if (newValue.contains("oauth2/firstlogin?token=")) {
                String token = parseTokenFromURL(newValue);
                if (token != null && !token.isBlank()) {
                    try {
                        openAccountCompletion(token);
                    } catch (IOException e) {
                        System.out.println("Errore nel reindirizzamento alla pagina di completamento account");
                    }
                }
            } else if (newValue.contains("oauth2/error?error=")) {
                String error = parseErrorFromURL(newValue);
                if (error != null && !error.isBlank()) {
                    System.out.println("Errore durante il login OAuth2: " + error);
                    showPopup("Errore durante il login", error, "/dietiestates25ui/images/errorIcon.png",3);

                }
            }
        }
    }

    private void createAndPlaceBackButton() {
        providerBackButton = new Button("Torna indietro");
        providerBackButton.setVisible(false);
        providerBackButton.setOnAction(e -> hideWebView());
        AnchorPane.setBottomAnchor(providerBackButton, 10.0);
        AnchorPane.setLeftAnchor(providerBackButton, 10.0);
        primaryAnchorPane.getChildren().add(providerBackButton);
    }

    private void hideWebView() {
        webView.setVisible(false);
        providerBackButton.setVisible(false);
        webEngine.load(null);
    }

    private void openAccountCompletion(String token) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/dietiestates25ui/view/account-completion-view.fxml"));
        Scene sceneAccountCompletion = new Scene(fxmlLoader.load());
        Stage stageAccountCompletion = (Stage) loginButton.getScene().getWindow();
        Stage currentStage = (Stage) primaryAnchorPane.getScene().getWindow();

        stageAccountCompletion.setWidth(currentStage.getWidth());
        stageAccountCompletion.setHeight(currentStage.getHeight());

        stageAccountCompletion.setX(currentStage.getX());
        stageAccountCompletion.setY(currentStage.getY());

        sceneAccountCompletion.getStylesheets().add(getClass().getResource("/dietiestates25ui/styles/account-completion-style.css").toExternalForm());
        AccountCompletionController accountCompletionController = fxmlLoader.getController();
        accountCompletionController.setTokenAndLoadDetails(token);
        stageAccountCompletion.setTitle("DietiEstates25");
        stageAccountCompletion.setScene(sceneAccountCompletion);
        stageAccountCompletion.show();
    }

    private String parseTokenFromURL(String url) {
        try {
            if (url != null && (url.contains("oauth2/success?token=") || url.contains("oauth2/firstlogin?token="))) {
                String token = url.substring(url.indexOf("oauth2/"));
                token = token.substring(token.indexOf("token=") + 6);
                if (token.contains(" ")) {
                    return token.substring(0, token.indexOf(" "));
                }
                return token;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private String parseErrorFromURL(String url) {
        try {
            if (url != null && url.contains("oauth2/error?error=")) {
                String error = url.substring(url.indexOf("oauth2/error?error="));
                error = error.substring("oauth2/error?error=".length());
                if (error.contains(" ")) {
                    return error.substring(0, error.indexOf(" "));
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private void openDashboard(String token) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/dietiestates25ui/view/dashboard-view.fxml"));
        Scene sceneDashboard = new Scene(fxmlLoader.load());
        Stage stageDashboard = (Stage) loginButton.getScene().getWindow();
        Stage currentStage = (Stage) primaryAnchorPane.getScene().getWindow();

        stageDashboard.setWidth(currentStage.getWidth());
        stageDashboard.setHeight(currentStage.getHeight());

        stageDashboard.setX(currentStage.getX());
        stageDashboard.setY(currentStage.getY());
        sceneDashboard.getStylesheets().add(getClass().getResource("/dietiestates25ui/styles/dashboard-style.css").toExternalForm());
        DashboardController dashboardController = fxmlLoader.getController();
        dashboardController.setToken(token);
        stageDashboard.setTitle("DietiEstates25");
        stageDashboard.setScene(sceneDashboard);
        stageDashboard.show();
    }

    private void updateLoginButton() {
        emailTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForLogin());
        passwordPasswordField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForLogin());
        loginButton.setDisable(true);
    }

    private void checkFieldsForLogin() {
        String email = emailTextField.getText().trim();
        String password = passwordPasswordField.getText().trim();
        loginButton.setDisable(!isValidEmail(email) || password.isBlank());
    }

    private void loginUtente() {
        String email = emailTextField.getText().trim();
        String password = passwordPasswordField.getText().trim();
        Utente user = new Utente(email, password);
        try {
            String token = utenteService.loginUtente(user);
            if (token != null) {
                showPopup("Login effettuato con successo", "Reindirizzamento alla dashboard...", "/dietiestates25ui/images/successIcon.png", 3);
                System.out.println("Login effettuato con successo.");
                System.out.println("Token JWT: " + token);
                PauseTransition delay = new PauseTransition(Duration.millis(3000));
                delay.setOnFinished(event -> {
                    try {
                        openDashboard(token);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                delay.play();
            }
        } catch (Exception e) {
            System.out.println("Errore durante il login: " + e.getMessage());
            showPopup("Errore durante il login", e.getMessage(), "/dietiestates25ui/images/errorIcon.png",3);
        }
    }


    private void loginWithProvider(String provider) {
        try {
            webView.setVisible(true);
            providerBackButton.setVisible(true);
            webEngine.load("http://localhost:8080/oauth2/authorization/" + provider);
        } catch (Exception e) {
            e.printStackTrace();
            showPopup("Errore durante il login con provider", e.getMessage(), "/dietiestates25ui/images/errorIcon.png",3);
        }
    }


    private void openRegisterPage() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/dietiestates25ui/view/register-view.fxml"));
            Scene sceneRegister = new Scene(fxmlLoader.load());
            Stage stageRegister = (Stage) registratiButton.getScene().getWindow();
            Stage currentStage = (Stage) primaryAnchorPane.getScene().getWindow();

            stageRegister.setWidth(currentStage.getWidth());
            stageRegister.setHeight(currentStage.getHeight());

            stageRegister.setX(currentStage.getX());
            stageRegister.setY(currentStage.getY());

            sceneRegister.getStylesheets().add(getClass().getResource("/dietiestates25ui/styles/register-style.css").toExternalForm());

            stageRegister.setTitle("DietiEstates25");
            stageRegister.setScene(sceneRegister);
            stageRegister.show();
        } catch (IOException e) {
            e.printStackTrace();
            showPopup("Errore durante l'apertura della pagina di registrazione", e.getMessage(), "/dietiestates25ui/images/errorIcon.png",3);

        }

    }

    private boolean isValidEmail(String email) {
        if (email.isBlank()) {
            return false;
        }
        boolean hasAt = false;
        for (int i = 0; i < email.length(); i++) {
            if (hasAt) {
                return isEmailValidAfterAt(email, i);
            } else if (email.charAt(i) == '@' && i > 0) {
                hasAt = true;
            } else {
                if (!isEmailValidBeforeAt(email, i)) {
                    return false;
                }
            }
        }
        return false;
    }

    private static boolean isEmailValidBeforeAt(String email, int i) {
        return Character.isLetter(email.charAt(i)) || Character.isDigit(email.charAt(i)) || email.charAt(i) == '.' || email.charAt(i) == '-' || email.charAt(i) == '_';
    }

    private static boolean isEmailValidAfterAt(String email, int i) {
        boolean hasDot = false;
        for (int j = i; j < email.length(); j++) {
            if (j == i && !Character.isLetter(email.charAt(j))) {
                return false;
            } else if (!Character.isLetter(email.charAt(j))) {
                if (email.charAt(j) == '.' && !hasDot && j < email.length() - DOMAIN_MIN_LENGTH) {
                    hasDot = true;
                } else {
                    return false;
                }
            }
        }
        return hasDot;
    }
}