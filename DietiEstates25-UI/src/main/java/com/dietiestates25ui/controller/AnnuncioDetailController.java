package com.dietiestates25ui.controller;

import com.dietiestates25.dto.AgenteDTO;
import com.dietiestates25.dto.AnnuncioDTO;
import com.dietiestates25.dto.ImmobileDTO;
import com.dietiestates25.dto.MapSearchDTO;
import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.model.FiltroAnnunci;
import com.dietiestates25ui.service.AgenteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

public class AnnuncioDetailController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(AnnuncioDetailController.class);

    private AnnuncioDTO annuncio;
    private ImmobileDTO immobile;

    @FXML
    private Button tornaIndietroButton;

    @FXML
    private Text titoloText;
    @FXML
    private Label descrizioneText;
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
    private Label emailText;

    @FXML
    private AnchorPane detailsAnchorPane;

    @FXML
    private FlowPane detailAnnuncioFlowPane;

    @FXML
    private WebView map;

    @FXML
    private ScrollPane detailsScrollPane;
    @FXML
    private VBox detailsVBox;
    @FXML
    private VBox fotoEMappaVBox;
    @FXML
    private ImageView carouselImageView;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;

    private List<Image> images;
    private int currentImageIndex = 0;

    private FiltroAnnunci filtroAnnunci;
    private String cittaDiRicerca;
    private MapSearchDTO mapSearchDTO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tornaIndietroButton.setOnAction(event -> openRisultatiRicercaPage(cittaDiRicerca, filtroAnnunci, tornaIndietroButton, mapSearchDTO));

        Platform.runLater(this::searchUserNameAndUpdateProfileHBox);

        prevButton.setOnAction(event -> prevImage());
        nextButton.setOnAction(event -> nextImage());

        updateMap();
        updateScrollPanePrefWidth();
    }

    private void prevImage() {
        if (images != null && !images.isEmpty()) {
            currentImageIndex = (currentImageIndex - 1 + images.size()) % images.size();
            carouselImageView.setImage(images.get(currentImageIndex));
        }
    }

    private void nextImage() {
        if (images != null && !images.isEmpty()) {
            currentImageIndex = (currentImageIndex + 1) % images.size();
            carouselImageView.setImage(images.get(currentImageIndex));
        }
    }

    private void updateCarousel() {
        if (annuncio != null && annuncio.getImmaginiUrls() != null && !annuncio.getImmaginiUrls().isEmpty()) {
            images = new ArrayList<>();
            for (String imageUrl : annuncio.getImmaginiUrls()) {
                String fullImageUrl = "http://35.180.252.202:8080" + imageUrl;
                logger.debug("Loading image: {}", fullImageUrl);
                Image image = new Image(fullImageUrl);
                images.add(image);
            }

            if (!images.isEmpty()) {
                carouselImageView.setImage(images.getFirst());
            } else {
                logger.warn("No images loaded for annuncio: {}", annuncio.getTitolo());
                carouselImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/dietiestates25ui/images/noAnnuncioImage.png"))));
            }
        } else {
            logger.warn("Annuncio or immaginiUrls is null or empty");
            carouselImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/dietiestates25ui/images/noAnnuncioImage.png"))));
        }
    }


    private void updateScrollPanePrefWidth() {
        detailsAnchorPane.widthProperty().addListener((observable, oldValue, newValue) ->
                setAnnunciScrollPanePrefWidth(newValue.doubleValue()));

        setAnnunciScrollPanePrefWidth(detailsAnchorPane.getWidth());
    }

    private void setAnnunciScrollPanePrefWidth(double newValue) {
        double scrollPaneWidth = newValue * 0.55;
        detailsScrollPane.setPrefWidth(scrollPaneWidth);
    }

    public void setAnnuncio(AnnuncioDTO annuncio, ImmobileDTO immobile) {
        this.annuncio = annuncio;
        this.immobile = immobile;
        setAnnuncioDetails();
        updateCarousel();
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

        AgenteDTO agente;
        try {
            AgenteService agenteService = new AgenteService();
            agente = agenteService.getAgenteDetails(annuncio.getIdAgente(), token);
            emailText.setText("Per prenotare una visita, fare un offerta o altre informazioni contattaci all'indirizzo email: " + agente.getEmail());
        } catch (GenericServiceException e) {
            logger.error("Errore durante il recupero dei dettagli dell'agente", e);
        }

    }

    private String getPiano() {
        return switch (immobile.getPiano()) {
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

    private void updateMap() {
        detailsAnchorPane.widthProperty().addListener((observable, oldValue, newValue) ->
                setMappaImmobiliVBox(newValue.doubleValue()));

        setMappaImmobiliVBox(fotoEMappaVBox.getWidth());

        Platform.runLater(this::loadMap);
    }

    private void setMappaImmobiliVBox(double newValue) {
        double vboxWidth = newValue * 0.4;
        fotoEMappaVBox.setPrefWidth(vboxWidth);
    }

    private void loadMap() {
        WebEngine webEngine = map.getEngine();
        webEngine.load(Objects.requireNonNull(getClass().getResource("/com/dietiestates25ui/view/mapRisultatiAnnunci.html")).toExternalForm());

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                Platform.runLater(this::visualizzaAnnunciSullaMappa);
            }
        });

        webEngine.setOnAlert(event -> {
            String data = event.getData();
            logger.info("Data from WebView: {}", data);
        });
    }

    private void visualizzaAnnunciSullaMappa() {
        double minLon = Double.MAX_VALUE;
        double minLat = Double.MAX_VALUE;
        double maxLon = Double.MIN_VALUE;
        double maxLat = Double.MIN_VALUE;

        List<Map<String, Object>> annunciPerLaMappa = new ArrayList<>();
        double latitudine = immobile.getLatitudine();
        double longitudine = immobile.getLongitudine();

        minLon = Math.min(minLon, longitudine);
        minLat = Math.min(minLat, latitudine);
        maxLon = Math.max(maxLon, longitudine);
        maxLat = Math.max(maxLat, latitudine);

        Map<String, Object> annuncioMap = new HashMap<>();
        annuncioMap.put("latitudine", latitudine);
        annuncioMap.put("longitudine", longitudine);
        annuncioMap.put("titolo", annuncio.getTitolo());
        annuncioMap.put("prezzo", annuncio.getPrezzo());
        annuncioMap.put("descrizione", annuncio.getDescrizione());
        annuncioMap.put("idImmobile", annuncio.getIdImmobile());
        annunciPerLaMappa.add(annuncioMap);
        double[] extent = {minLon, minLat, maxLon, maxLat};

        String extentString = Arrays.toString(extent);

        WebEngine webEngine = map.getEngine();
        webEngine.executeScript("fitViewToExtent(" + extentString + ");");

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String annunciJson = objectMapper.writeValueAsString(annunciPerLaMappa);
            webEngine.executeScript("addMarkersToMap(" + annunciJson + ");");
        } catch (Exception e) {
            logger.error("Errore serializzazione JSON annuncioDTO:", e);
        }
    }

    public void setFiltroAnnunci(FiltroAnnunci filtroAnnunci, String cittaDiRicerca) {
        this.filtroAnnunci = filtroAnnunci;
        this.cittaDiRicerca = cittaDiRicerca;
    }

    public void setMapSearchDTO(MapSearchDTO mapSearchDTO) {
        this.mapSearchDTO = mapSearchDTO;
    }
}