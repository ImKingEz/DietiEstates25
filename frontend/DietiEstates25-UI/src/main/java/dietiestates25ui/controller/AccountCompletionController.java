package dietiestates25ui.controller;

import dietiestates25ui.MainApplication;
import dietiestates25ui.dto.UtenteDTO;
import dietiestates25ui.model.Utente;
import dietiestates25ui.service.UtenteService;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AccountCompletionController implements Initializable {
    public static final int ERROR_POPUP_WIDTH = 200;
    public static final int SUCCESS_POPUP_WIDTH = 260;
    @FXML
    private TextField cittaTextField;

    @FXML
    private TextField cognomeTextField;

    @FXML
    private ImageView logo;

    @FXML
    private TextField nomeTextField;

    @FXML
    private Button salvaButton;

    private double popupWidth;

    @FXML
    private AnchorPane primaryAnchorPane;

    private String token;

    private UtenteService utenteService;

    private Stage currentStage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> logo.requestFocus());

        utenteService = new UtenteService();
        salvaButton.setOnAction(event -> saveAccountDetails());

        updateSalvaButton();
    }

    private void updateSalvaButton() {
        nomeTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForSave());
        cognomeTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForSave());
        salvaButton.setDisable(true);
    }

    private void checkFieldsForSave() {
        String nome = nomeTextField.getText().trim();
        String cognome = cognomeTextField.getText().trim();
        salvaButton.setDisable(nome.isBlank() || cognome.isBlank());
    }

    public void setTokenAndLoadDetails(String token) {
        this.token = token;
        System.out.println("(accountcompletion) token: " + token);
        loadUserDetails();
    }

    private void saveAccountDetails() {
        Platform.runLater(() -> logo.requestFocus());

        String nome = nomeTextField.getText().trim();
        String cognome = cognomeTextField.getText().trim();
        String citta = cittaTextField.getText().trim();
        if(nome.isBlank() || cognome.isBlank()){
            showPopup("Errore!", "Nome e Cognome non possono essere vuoti.", "/dietiestates25ui/images/errorIcon.png", 3);
            return;
        }

        Utente utente;
        if (citta.isBlank()) {
            utente = new Utente(nome, cognome, null, null, null);
        } else {
            utente = new Utente(nome, cognome, citta, null, null);
        }
        try {
            utenteService.updateUtente(utente, token);
            try {
                UtenteDTO utenteDTO = utenteService.getUtenteDetails(token);
                if(utenteDTO != null){
                    showPopup("Modifica completata!", "Reindirizzamento alla dashboard...", "/dietiestates25ui/images/successIcon.png", 3);
                    salvaButton.setDisable(true);
                    PauseTransition pause = new PauseTransition(Duration.seconds(3));
                    pause.setOnFinished(e -> {
                        try {
                            openDashboard(token);
                        }catch(Exception ex){
                            System.out.println("Errore nel reindirizzamento alla dashboard");
                        }
                    });
                    pause.play();
                }else{
                    System.out.println("Dati dell'utente non trovati");
                    showPopup("Errore!", "Dati dell'utente non trovati", "/dietiestates25ui/images/errorIcon.png", 3);
                }
            } catch (Exception e) {
                System.out.println("Errore durante il recupero dei dettagli dell'utente: " + e.getMessage());
                showPopup("Errore!", "Errore durante il recupero dei dettagli dell'utente.", "/dietiestates25ui/images/errorIcon.png", 3);
            }
        } catch (Exception e) {
            System.out.println("Errore durante l'update dell'utente: " + e.getMessage());
            showPopup("Errore!", "Errore durante l'update dell'utente (" + e.getMessage() + ").", "/dietiestates25ui/images/errorIcon.png", 3);
        }
    }

    private void loadUserDetails(){
        try {
            UtenteDTO utente = utenteService.getUtenteDetails(token);
            if(utente != null){
                Platform.runLater(() ->{
                    nomeTextField.setText(utente.getNome());
                    cognomeTextField.setText(utente.getCognome());
                    if(utente.getCitta() != null)
                        cittaTextField.setText(utente.getCitta());

                });
            }
        }catch (Exception e){
            System.out.println("Errore durante il caricamento delle informazioni dell'utente" + e.getMessage());
            showPopup("Errore!", "Errore durante il caricamento delle informazioni dell'utente", "/dietiestates25ui/images/errorIcon.png", 3);
        }
    }

    private void openDashboard(String token) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/dietiestates25ui/view/dashboard-view.fxml"));
        Scene sceneDashboard = new Scene(fxmlLoader.load());
        Stage stageDashboard = (Stage) salvaButton.getScene().getWindow();
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
}