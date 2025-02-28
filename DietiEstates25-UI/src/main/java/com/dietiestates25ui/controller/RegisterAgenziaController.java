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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static com.dietiestates25ui.handler.FormValidator.setupTextFormatter;

public class RegisterAgenziaController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(RegisterAgenziaController.class);

    @FXML
    private TextField emailTextField;

    @FXML
    private Button indietroButton;

    @FXML
    private TextField indirizzoTextField;

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

    private static final int MAX_FILE_SIZE = 2 * 1024 * 1024;
    private static final int MAX_IMAGE_SIZE = 512;

    private AgenziaImmobiliare agenzia = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> logo.requestFocus());

        updateProseguiButton();

        proseguiButton.setOnAction(event -> openAgenziaCredentialsPage());

        indietroButton.setOnAction(event -> openLoginAmmnistratorePage());

        selezionaImmaginiButton.setOnAction(event -> Platform.runLater(this::handleImageSelection));

        setupTextFormatter(partitaIVATextField);
        setupTextFormatter(telefonoTextField);
    }

    private void handleImageSelection() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona Immagini");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Immagini", "*.png", "*.jpg", "*.jpeg"));

        File newFile = fileChooser.showOpenDialog(currentStage);

        if (newFile != null) {
            if (!validateImage(newFile)) {
                return;
            }

            selectedImageList.clear();
            selectedImageList.add(newFile);
            selectedLogoFile = newFile;

            updateImageThumbnails();
            checkFieldsForContinue();
        }
    }

    private boolean validateImage(File file) {
        if (file.length() > MAX_FILE_SIZE) {
            Platform.runLater(() -> showPopup(POPUP_ERROR_TITLE, "La dimensione del file è troppo grande (max 2MB).", ERROR_ICON));
            return false;
        }

        try {
            Image image = new Image(file.toURI().toString());
            double width = image.getWidth();
            double height = image.getHeight();

            if (width > MAX_IMAGE_SIZE || height > MAX_IMAGE_SIZE) {
                Platform.runLater(() -> showPopup(POPUP_ERROR_TITLE, "Le dimensioni dell'immagine sono troppo grandi (max " + MAX_IMAGE_SIZE + "x" + MAX_IMAGE_SIZE + ").", ERROR_ICON));
                return false;
            }

            if (width != height) {
                Platform.runLater(() -> showPopup(POPUP_ERROR_TITLE, "L'immagine deve essere quadrata.", ERROR_ICON));
                return false;
            }

        } catch (Exception e) {
            logger.error("Errore durante la validazione dell'immagine: {}", e.getMessage());
            Platform.runLater(() -> showPopup(POPUP_ERROR_TITLE, "Errore durante la validazione dell'immagine.", ERROR_ICON));
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

            Image closeIcon = new Image(getClass().getResourceAsStream("/com/dietiestates25ui/images/erroricon.png"));
            ImageView closeIconView = new ImageView(closeIcon);
            closeIconView.setFitWidth(20);
            closeIconView.setFitHeight(20);

            Button deleteButton = new Button();
            deleteButton.setGraphic(closeIconView);

            deleteButton.setStyle("-fx-padding: 0px; -fx-background-color: transparent;");
            deleteButton.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

            deleteButton.setOnAction(e -> {
                logo.requestFocus();
                selectedImageList.remove(file);
                selectedLogoFile = null;
                immaginiFlowPane.getChildren().remove(imageView.getParent());
                checkFieldsForContinue();
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

        agenzia = new AgenziaImmobiliare(nome, partitaIVA, indirizzo, email, telefono, "logo");

        loadScene("/com/dietiestates25ui/view/agenzia-credentials-view.fxml",
                (fxmlLoader, stage) -> {
                    try {
                        AgenziaCredentialsController controller = fxmlLoader.getController();
                        if (controller != null) {
                            controller.setAgenzia(agenzia);
                            controller.setStage(stage);
                            controller.setLogoFile(selectedLogoFile);
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

        checkFieldsForContinue();
    }

    private void checkFieldsForContinue() {
        String nome = nomeTextField.getText().trim();
        String partitaIVA = partitaIVATextField.getText().trim();
        String indirizzo = indirizzoTextField.getText().trim();
        String email = emailTextField.getText().trim();
        String telefono = telefonoTextField.getText().trim();

        boolean isImageSelected = selectedLogoFile != null;

        proseguiButton.setDisable(nome.isBlank() || !FormValidator.isValidPartitaIVA(partitaIVA) || indirizzo.isBlank() || !FormValidator.isValidEmail(email) || !FormValidator.isValidTelefono(telefono) || !isImageSelected);
    }

    public void setAgenzia(AgenziaImmobiliare agenzia) {
        this.agenzia = agenzia;
    }

    public void setLogoFile(File logoFile) {
        this.selectedLogoFile = logoFile;
    }

    public void initializeData() {
        if (agenzia != null) {
            nomeTextField.setText(agenzia.getNome());
            partitaIVATextField.setText(agenzia.getPartitaIva());
            indirizzoTextField.setText(agenzia.getIndirizzo());
            emailTextField.setText(agenzia.getEmail());
            telefonoTextField.setText(agenzia.getTelefono());
        }
        if (selectedLogoFile != null) {
            selectedImageList.add(selectedLogoFile);
            updateImageThumbnails();
        }
    }
}