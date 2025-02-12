package com.dietiestates25ui.controller;

import com.dietiestates25ui.handler.FormValidator;
import com.dietiestates25ui.model.AgenziaImmobiliare;
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

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Random;
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

    @FXML
    private Button generaButton;

    private boolean passwordVisible = false;

    private File logoFile;

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

        disableGeneraButtonOnPasswordInsertion();

        generaButton.setOnAction(event -> generateAndSetPassword());
    }

    private void disableGeneraButtonOnPasswordInsertion() {
        passwordPasswordField.textProperty().addListener((observable, oldValue, newValue) ->
                generaButton.setDisable(!newValue.isEmpty()));
    }

    private void generateAndSetPassword() {
        String generatedPassword = generateRandomPassword(10);
        passwordPasswordField.setText(generatedPassword);
        copyToClipboard(generatedPassword);
        Platform.runLater(() -> showPopup("Password Generata", "Password casuale copiata negli appunti!", SUCCESS_ICON));
        generaButton.setDisable(true);
    }

    private String generateRandomPassword(int len) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        // Assicurati che ci sia almeno una lettera maiuscola
        sb.append((char) (random.nextInt(26) + 'A')); // A-Z

        // Assicurati che ci sia almeno un numero
        sb.append((char) (random.nextInt(10) + '0')); // 0-9

        // Aggiungi i restanti caratteri casuali
        for (int i = 2; i < len; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        // Mescola la password per una maggiore casualità
        List<Character> charList = new java.util.ArrayList<>();
        for (char c : sb.toString().toCharArray()) {
            charList.add(c);
        }
        java.util.Collections.shuffle(charList);

        StringBuilder shuffledPassword = new StringBuilder();
        for (char c : charList) {
            shuffledPassword.append(c);
        }

        return shuffledPassword.toString();
    }

    private void copyToClipboard(String text) {
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    public void initializeData() {
        logger.info("initializeData() called in AgenziaCredentialsController");
        if (agenzia != null) {
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
                (fxmlLoader, stage) -> {
                }, indietroButton, "/com/dietiestates25ui/styles/register-style.css");
    }

    private void registraAgenzia() {
        Platform.runLater(() -> logo.getParent().requestFocus());

        String email = emailTextField.getText().trim();
        String password = passwordPasswordField.getText().trim();

        try {
            logger.info("Tentativo di registrazione agenzia e amministratore...");

            boolean registrationSuccessful = uploadAgenziaData(agenzia, logoFile, email, password);

            if (registrationSuccessful) {
                confermaButton.setDisable(true);
                indietroButton.setDisable(true);
                generaButton.setDisable(true);
                showPopup("Agenzia registrata con successo", "Registrazione completata", SUCCESS_ICON);
                PauseTransition delay = new PauseTransition(Duration.millis(POPUP_PAUSE));
                delay.setOnFinished(event -> openLoginAmministratorePage());
                delay.play();
            } else {
                showPopup(POPUP_ERROR_TITLE, "Errore durante la registrazione dell'agenzia o dell'amministratore.", ERROR_ICON);
            }

        } catch (Exception e) {
            logger.error("Errore durante la registrazione: {}", e.getMessage());
            showPopup(POPUP_ERROR_TITLE, e.getMessage(), ERROR_ICON);
        }
    }

    private void openLoginAmministratorePage() {
        loadScene("/com/dietiestates25ui/view/login-amministratore-view.fxml",
                (fxmlLoader, stage) -> {
                }, indietroButton, "/com/dietiestates25ui/styles/login-amministratore-style.css");
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

    public void setLogoFile(File logoFile) {
        this.logoFile = logoFile;
    }

    private boolean uploadAgenziaData(AgenziaImmobiliare agenzia, File logoFile, String email, String password) {
        try {
            MultipartBodyPublisher publisher = new MultipartBodyPublisher();
            publisher.addFormDataPart("nome", agenzia.getNome());
            publisher.addFormDataPart("partitaIva", agenzia.getPartitaIva());
            publisher.addFormDataPart("indirizzo", agenzia.getIndirizzo());
            publisher.addFormDataPart("email", agenzia.getEmail());
            publisher.addFormDataPart("telefono", agenzia.getTelefono());
            publisher.addFormDataPart("password", password);

            if (logoFile != null) {
                Path logoPath = Paths.get(logoFile.getAbsolutePath());
                String mimeType = Files.probeContentType(logoPath);
                publisher.addFilePart("logo", logoFile.getName(), mimeType, logoPath);
            }

            String csrfToken = null;
            String csrfCookieName = "XSRF-TOKEN";
            CookieManager cookieManager = new CookieManager();
            HttpClient client = HttpClient.newBuilder()
                    .cookieHandler(cookieManager)
                    .build();

            HttpRequest csrfRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/csrf"))
                    .GET()
                    .build();

            HttpResponse<String> csrfResponse = client.send(csrfRequest, HttpResponse.BodyHandlers.ofString());

            if (csrfResponse.headers().map().containsKey("Set-Cookie")) {
                List<String> setCookieHeaders = csrfResponse.headers().allValues("Set-Cookie");
                for (String setCookieHeader : setCookieHeaders) {
                    HttpCookie cookie = HttpCookie.parse(setCookieHeader).get(0);
                    cookieManager.getCookieStore().add(null, cookie);
                }
            }

            for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
                if (csrfCookieName.equals(cookie.getName())) {
                    csrfToken = cookie.getValue();
                    break;
                }
            }

            if (csrfToken == null) {
                logger.error("CSRF token not found in cookies.");
                return false;
            }

            byte[] requestBody = publisher.build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/agenzie/register"))
                    .header("Content-Type", publisher.getContentType())
                    .header("X-XSRF-TOKEN", csrfToken)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();

            if (statusCode == 201) {
                logger.info("Agenzia e amministratore registrati con successo.");
                return true;
            } else {
                logger.error("Errore durante la registrazione dell'agenzia o dell'amministratore: {}", response.body());
                return false;
            }

        } catch (IOException | InterruptedException e) {
            logger.error("Errore durante l'upload dei dati dell'agenzia: {}", e.getMessage());
            return false;
        }
    }

    static class MultipartBodyPublisher {
        private final String boundary;
        private final java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        private final String LINE_FEED = "\r\n";

        public MultipartBodyPublisher() {
            this.boundary = generateBoundary();
        }

        private String generateBoundary() {
            String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            StringBuilder buffer = new StringBuilder();
            Random random = new Random();
            for (int i = 0; i < 20; i++) { // A shorter boundary
                buffer.append(characters.charAt(random.nextInt(characters.length())));
            }
            return buffer.toString();
        }

        public void addFormDataPart(String name, String value) {
            try {
                outputStream.write(("--" + boundary + LINE_FEED).getBytes());
                outputStream.write(("Content-Disposition: form-data; name=\"" + name + "\"" + LINE_FEED).getBytes());
                outputStream.write(("Content-Type: text/plain; charset=UTF-8" + LINE_FEED).getBytes());
                outputStream.write(LINE_FEED.getBytes());
                outputStream.write(value.getBytes());
                outputStream.write(LINE_FEED.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void addFilePart(String fieldName, String fileName, String mimeType, Path filePath) {
            try {
                outputStream.write(("--" + boundary + LINE_FEED).getBytes());
                outputStream.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"" + LINE_FEED).getBytes());
                outputStream.write(("Content-Type: " + mimeType + LINE_FEED).getBytes());
                outputStream.write(("Content-Transfer-Encoding: binary" + LINE_FEED).getBytes());
                outputStream.write(LINE_FEED.getBytes());
                Files.copy(filePath, outputStream);
                outputStream.write(LINE_FEED.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public byte[] build() {
            try {
                outputStream.write(("--" + boundary + "--" + LINE_FEED).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return outputStream.toByteArray();
        }

        public String getContentType() {
            return "multipart/form-data; boundary=" + boundary;
        }
    }
}