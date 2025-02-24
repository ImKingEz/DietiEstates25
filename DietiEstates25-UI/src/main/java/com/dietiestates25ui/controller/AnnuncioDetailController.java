package com.dietiestates25ui.controller;

import com.dietiestates25.dto.AnnuncioDTO;
import com.dietiestates25.dto.ImmobileDTO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class AnnuncioDetailController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(AnnuncioDetailController.class);

    private AnnuncioDTO annuncio;
    private ImmobileDTO immobile;

    private String token;

    @FXML
    private Button tornaIndietroButton;

    @FXML
    private Text titoloText;
    @FXML
    private Text descrizioneText;
    @FXML
    private Text prezzoText;
    @FXML
    private Text superficieText;
    @FXML
    private Text localiText;
    @FXML
    private Text bagniText;
    @FXML
    private Text pianoText;
    @FXML
    private Text classeEnergeticaText;

    @FXML
    private FlowPane detailAnnuncioFlowPane;

    @FXML
    private WebView map;

    @FXML
    private VBox mappaImmobiliVBox;


    public void setAnnuncio(AnnuncioDTO annuncio, ImmobileDTO immobile) {
        this.annuncio = annuncio;
        this.immobile = immobile;
        setAnnuncioDetails();
    }

    public void setToken(String token) {
        this.token = token;
    }

    private void setAnnuncioDetails() {
        titoloText.setText(annuncio.getTitolo());
        descrizioneText.setText(annuncio.getDescrizione());
        prezzoText.setText(String.valueOf(annuncio.getPrezzo() + "€"));
        superficieText.setText(String.valueOf("Superficie: " + immobile.getDimensione() + "mq"));
        localiText.setText(String.valueOf("Locali: " + immobile.getNumeroLocali()));
        bagniText.setText(String.valueOf("Bagni: " + immobile.getNumeroBagni()));
        pianoText.setText(getPiano());
        classeEnergeticaText.setText("Classe energetica: " + immobile.getClasseEnergetica());
        if (immobile.isAscensore()) {
            addNewDetail("Ascensore", "/com/dietiestates25ui/images/elevatorIcon.png");
        }
        if (immobile.isPortineria()) {
            addNewDetail("Portineria", "/com/dietiestates25ui/images/portineriaIcon.png");
        }
        if (immobile.isClimatizzazione()) {
            addNewDetail("Climatizzazione", "/com/dietiestates25ui/images/climatizzazioneIcon.png");
        }
        if (immobile.isVicinoScuole()) {
            addNewDetail("Vicino una scuola", "/com/dietiestates25ui/images/schoolIcon.png");
        }
        if (immobile.isVicinoParchi()) {
            addNewDetail("Vicino un parco", "/com/dietiestates25ui/images/parkIcon.png");
        }
        if (immobile.isVicinoTrasportoPubblico()) {
            addNewDetail("Vicino ai trasporti", "/com/dietiestates25ui/images/busIcon.png");
        }
    }

    private String getPiano() {
        return switch(immobile.getPiano()) {
            case 0 -> "Piano terra";
            case 1 -> "Piano intermedio";
            case 2 -> "Ultimo piano";
            default -> "Piano terra";
        };
    }

    private void addNewDetail(String detailText, String imagePath) {
        HBox ascensoreHBox = new HBox();
        ascensoreHBox.getStyleClass().add("detailWithIconHBox");

        ImageView ascensoreImageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
        ascensoreImageView.getStyleClass().add("iconDetailAnnuncio");
        Text ascensoreText = new Text(detailText);
        ascensoreText.getStyleClass().add("textDetailAnnuncio");
        ascensoreHBox.getChildren().addAll(ascensoreImageView, ascensoreText);

        detailAnnuncioFlowPane.getChildren().add(ascensoreHBox);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tornaIndietroButton.setOnAction(event -> openDashboard(token, tornaIndietroButton));
    }
}