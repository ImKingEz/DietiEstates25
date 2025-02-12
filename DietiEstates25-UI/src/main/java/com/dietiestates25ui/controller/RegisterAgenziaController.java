package com.dietiestates25ui.controller;

import com.dietiestates25ui.handler.FormValidator;
import com.dietiestates25ui.model.AgenziaImmobiliare;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class RegisterAgenziaController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(RegisterAgenziaController.class);

    @FXML
    private TextField emailTextField;

    @FXML
    private Button indietroButton;

    @FXML
    private TextField indirizzoTextField;

    @FXML
    private ImageView logo;

    @FXML
    private TextField nomeTextField;

    @FXML
    private TextField partitaIVATextField;

    @FXML
    private Button proseguiButton;

    @FXML
    private TextField telefonoTextField;

    @FXML
    private Button selezionaImmaginiButton;

    @FXML
    private FlowPane immaginiFlowPane;

    private final ObservableList<File> selectedImageList = FXCollections.observableArrayList();

    private File selectedLogoFile = null;

    private final int MAX_FILE_SIZE = 2 * 1024 * 1024;
    private final int MAX_IMAGE_SIZE = 512;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> logo.requestFocus());

        updateProseguiButton();

        proseguiButton.setOnAction(event -> openAgenziaCredentialsPage());

        indietroButton.setOnAction(event -> openLoginAmmnistratorePage());

        selezionaImmaginiButton.setOnAction(event -> Platform.runLater(this::handleImageSelection));
    }

    private void handleImageSelection() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona Immagini");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Immagini", "*.png", "*.jpg", "*.jpeg"));

        File newFile = fileChooser.showOpenDialog(currentStage);

        if (newFile != null) {
            if (!validateImage(newFile)) {
                return; // Non procedere se la validazione fallisce
            }

            selectedImageList.clear();
            selectedImageList.add(newFile);
            selectedLogoFile = newFile;

            updateImageThumbnails();
        }
    }

    private boolean validateImage(File file) {
        if (file.length() > MAX_FILE_SIZE) {
            Platform.runLater(() -> showPopup("Errore", "La dimensione del file è troppo grande (max 2MB).", ERROR_ICON));
            return false;
        }

        try {
            Image image = new Image(file.toURI().toString());
            double width = image.getWidth();
            double height = image.getHeight();

            if (width > MAX_IMAGE_SIZE || height > MAX_IMAGE_SIZE) {
                Platform.runLater(() -> showPopup("Errore", "Le dimensioni dell'immagine sono troppo grandi (max 500x500).", ERROR_ICON));
                return false;
            }

            if (width != height) {
                Platform.runLater(() -> showPopup("Errore", "L'immagine deve essere quadrata.", ERROR_ICON));
                return false;
            }

        } catch (Exception e) {
            logger.error("Errore durante la validazione dell'immagine: {}", e.getMessage());
            Platform.runLater(() -> showPopup("Errore", "Errore durante la validazione dell'immagine.", ERROR_ICON));
            return false;
        }

        return true;
    }

    private void updateImageThumbnails() {
        immaginiFlowPane.getChildren().clear();

        double fixedImageSize = 50;

        for (File file : selectedImageList) {
            Image image = new Image(file.toURI().toString());
            ImageView imageView = new ImageView(image);

            imageView.setFitWidth(fixedImageSize);
            imageView.setFitHeight(fixedImageSize);
            imageView.setPreserveRatio(true);

            // Load the close icon
            Image closeIcon = new Image(getClass().getResourceAsStream("/com/dietiestates25ui/images/close.png"));
            ImageView closeIconView = new ImageView(closeIcon);
            closeIconView.setFitWidth(12);
            closeIconView.setFitHeight(12);

            Button deleteButton = new Button();
            deleteButton.setGraphic(closeIconView);

            deleteButton.setStyle("-fx-background-color: red; -fx-padding: 0px; -fx-font-size: 8px;");
            deleteButton.setPrefSize(20, 20);

            deleteButton.setOnAction(e -> {
                logo.requestFocus();
                selectedImageList.remove(file);
                selectedLogoFile = null;
                immaginiFlowPane.getChildren().remove(imageView.getParent());
            });

            VBox imageContainer = new VBox(imageView, deleteButton);
            imageContainer.setAlignment(Pos.CENTER);

            immaginiFlowPane.getChildren().add(imageContainer);
        }
    }


    private void openLoginAmmnistratorePage() {
        loadScene("/com/dietiestates25ui/view/login-amministratore-view.fxml",
                (fxmlLoader, stage) -> {
                }, indietroButton, "/com/dietiestates25ui/styles/login-amministratore-style.css");
    }

    private void openAgenziaCredentialsPage() {
        String nome = nomeTextField.getText().trim();
        String partitaIVA = partitaIVATextField.getText().trim();
        String indirizzo = indirizzoTextField.getText().trim();
        String email = emailTextField.getText().trim();
        String telefono = telefonoTextField.getText().trim();

        logger.info("Nome: {}, Partita IVA: {}, Indirizzo: {}, Email: {}, Telefono: {}", nome, partitaIVA, indirizzo, email, telefono);

        AgenziaImmobiliare agenzia = new AgenziaImmobiliare(nome, partitaIVA, indirizzo, email, telefono, "logo");

        loadScene("/com/dietiestates25ui/view/agenzia-credentials-view.fxml",
                (fxmlLoader, stage) -> {
                    try {
                        AgenziaCredentialsController controller = fxmlLoader.getController();
                        if (controller != null) {
                            controller.setAgenzia(agenzia);
                            controller.setStage(stage);
                            controller.setLogoFile(selectedLogoFile); // Pass the selected logo file
                            controller.initializeData();
                        } else {
                            logger.error("Controller is null after FXMLLoader.getController()");
                        }
                    } catch (Exception e) {
                        logger.error("Exception during controller setup: ", e);
                    }
                }, proseguiButton, "/com/dietiestates25ui/styles/agenzia-credentials-style.css");
    }

    private void updateProseguiButton() {
        nomeTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForContinue());
        partitaIVATextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForContinue());
        indirizzoTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForContinue());
        emailTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForContinue());
        telefonoTextField.textProperty().addListener((observable, oldValue, newValue) -> checkFieldsForContinue());

        proseguiButton.setDisable(true);
    }

    private void checkFieldsForContinue() {
        String nome = nomeTextField.getText().trim();
        String partitaIVA = partitaIVATextField.getText().trim();
        String indirizzo = indirizzoTextField.getText().trim();
        String email = emailTextField.getText().trim();
        String telefono = telefonoTextField.getText().trim();

        proseguiButton.setDisable(nome.isBlank() || partitaIVA.isBlank() || indirizzo.isBlank() || !FormValidator.isValidEmail(email) || telefono.isBlank());
    }
}