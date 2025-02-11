package com.dietiestates25ui.controller;

import com.dietiestates25ui.MainApplication;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> logo.requestFocus());


        updateProseguiButton();

        proseguiButton.setOnAction(event -> openAgenziaCredentialsPage());

        indietroButton.setOnAction(event -> openLoginAmmnistratorePage());

        selezionaImmaginiButton.setOnAction(event ->  Platform.runLater(this::handleImageSelection));
    }

    private void handleImageSelection() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona Immagini");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Immagini", "*.png", "*.jpg", "*.jpeg"));

        List<File> newFiles = fileChooser.showOpenMultipleDialog(currentStage);

        if (newFiles != null && !newFiles.isEmpty()) {
            selectedImageList.addAll(newFiles);

            if (selectedImageList.size() > 1) {
                Platform.runLater(() -> showPopup("Attenzione", "Puoi selezionare al massimo 1 immagine.", ERROR_ICON));

                while (selectedImageList.size() > 1) {
                    selectedImageList.removeLast();
                }
            }

            updateImageThumbnails();
        }
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
            Image closeIcon = new Image(getClass().getResourceAsStream("/com/dietiestates25ui/images/close.png")); // Adjust path if needed
            ImageView closeIconView = new ImageView(closeIcon);
            closeIconView.setFitWidth(12); // Adjust size as needed
            closeIconView.setFitHeight(12);

            Button deleteButton = new Button();
            deleteButton.setGraphic(closeIconView);  // Set the icon

            deleteButton.setStyle("-fx-background-color: red; -fx-padding: 0px; -fx-font-size: 8px;"); // Remove text, keep style if needed
            deleteButton.setPrefSize(20, 20);


            deleteButton.setOnAction(e -> {
                logo.requestFocus();
                selectedImageList.remove(file);
                immaginiFlowPane.getChildren().remove(imageView.getParent());
            });

            VBox imageContainer = new VBox(imageView, deleteButton);
            imageContainer.setAlignment(Pos.CENTER);

            immaginiFlowPane.getChildren().add(imageContainer);
        }
    }

    private void openLoginAmmnistratorePage() {
        loadScene("/com/dietiestates25ui/view/login-amministratore-view.fxml",
                (fxmlLoader, stage) -> {}, indietroButton, "/com/dietiestates25ui/styles/login-amministratore-style.css");
    }

    private void openAgenziaCredentialsPage() {
        String nome = nomeTextField.getText().trim();
        String partitaIVA = partitaIVATextField.getText().trim();
        String indirizzo = indirizzoTextField.getText().trim();
        String email = emailTextField.getText().trim();
        String telefono = telefonoTextField.getText().trim();
        //TODO Aggiungere logo

        logger.info("Nome: {}, Partita IVA: {}, Indirizzo: {}, Email: {}, Telefono: {}", nome, partitaIVA, indirizzo, email, telefono);

        AgenziaImmobiliare agenzia = new AgenziaImmobiliare(nome, partitaIVA, indirizzo, email, telefono, "logo"); //TODO Aggiungere logo

        loadScene("/com/dietiestates25ui/view/agenzia-credentials-view.fxml",
                (fxmlLoader, stage) -> {
                    try {
                        AgenziaCredentialsController controller = fxmlLoader.getController();
                        if (controller != null) {
                            controller.setAgenzia(agenzia);
                            controller.setStage(stage);
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
        //TODO Aggiungere listener per il logo

        proseguiButton.setDisable(true);
    }

    private void checkFieldsForContinue() {
        String nome = nomeTextField.getText().trim();
        String partitaIVA = partitaIVATextField.getText().trim();
        String indirizzo = indirizzoTextField.getText().trim();
        String email = emailTextField.getText().trim();
        String telefono = telefonoTextField.getText().trim();
        //TODO Implementare il controllo per il logo, partita iva, indirizzo e telefono.

        proseguiButton.setDisable(nome.isBlank() || partitaIVA.isBlank() || indirizzo.isBlank() || !FormValidator.isValidEmail(email) || telefono.isBlank());
        //TODO Aggiungere il controllo per il logo
    }
}