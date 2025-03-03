package com.dietiestates25ui.controller;

import com.dietiestates25.dto.AnnuncioDTO;
import com.dietiestates25.dto.ImmobileDTO;
import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.service.AgenteService;
import com.dietiestates25ui.service.AnnuncioService;
import com.dietiestates25ui.service.ImmobileService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class StatisticheInserzioniController extends AbstractController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(StatisticheInserzioniController.class);

    @FXML
    private ScrollPane annunciScrollPane;

    @FXML
    private Button esportaStatisticheButton;

    @FXML
    private VBox listaAnnunciVBox;

    @FXML
    private Button tornaIndietroButton;

    private ImmobileService immobileService = new ImmobileService();

    private AnnuncioService annuncioService = new AnnuncioService();

    private Long idAgente;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tornaIndietroButton.setOnAction(event -> openAgenteDashboardPage(tornaIndietroButton));

        idAgente = findAgenteId();

        Platform.runLater(this::updateAnnunci);
    }

    private Long findAgenteId() {
        AgenteService agenteService = new AgenteService();
        try {
            return agenteService.getAgenteDetails(token).getId();
        } catch (GenericServiceException e) {
            logger.error("Errore durante il recupero dei dettagli dell'agente: {}", e.getMessage(), e);
        }
        return null;
    }

    private void updateAnnunci() {
        try {
            List<AnnuncioDTO> annunci = annuncioService.getAnnunciAgente(token, idAgente);
            visualizzaAnnunciNellaLista(annunci);
        } catch (GenericServiceException e) {
            logger.error("Errore durante il recupero degli annunci: {}", e.getMessage(), e);
        }
    }

    private void visualizzaAnnunciNellaLista(List<AnnuncioDTO> annunci) {
        listaAnnunciVBox.getChildren().clear();
        for (AnnuncioDTO annuncio : annunci) {
            ImmobileDTO immobile = null;
            try {
                immobile = immobileService.getImmobileDetails(annuncio.getIdImmobile(), token);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/dietiestates25ui/view/annuncio-item-view.fxml"));
                HBox annuncioItem = loader.load();

                AnnuncioItemController controller = loader.getController();
                controller.setAnnuncio(annuncio, immobile);

                controller.getVisitaButton().setVisible(false);
                controller.getOffertaButton().setVisible(false);

                FlowPane detailFlowPane = controller.getDetailFlowPane();
                detailFlowPane.getChildren().clear();

                addStatsToFlowPane(annuncio, new HBox(), detailFlowPane, "Visualizzazioni: ", "/com/dietiestates25ui/images/eye_open.png");
                addStatsToFlowPane(annuncio, new HBox(), detailFlowPane, "Offerte ricevute: ", "/com/dietiestates25ui/images/euroIconBlack.png");
                addStatsToFlowPane(annuncio, new HBox(), detailFlowPane, "Visite prenotate: ", "/com/dietiestates25ui/images/calendarIconBlack.png");

                listaAnnunciVBox.getChildren().add(annuncioItem);
            } catch (IOException e) {
                logger.error("Errore durante il caricamento del layout dell'annuncio:", e);
            } catch (GenericServiceException e) {
                logger.error("Errore durante il recupero dei dettagli dell'immobile: {}", e.getMessage(), e);
            }
        }
    }

    private void addStatsToFlowPane(AnnuncioDTO annuncio, HBox visualizzazioniHBox, FlowPane detailFlowPane, String detailTipe, String imagePath) {
        visualizzazioniHBox.getStyleClass().add("detailWithIconHBox");

        ImageView visualizzazioniImageView = new ImageView();
        visualizzazioniImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
        visualizzazioniImageView.getStyleClass().add("iconDetailAnnuncio");

        Text visualizzazioniText = new Text();
        visualizzazioniText.setText(detailTipe + annuncio.getNumeroVisualizzazioni());
        visualizzazioniText.getStyleClass().add("textDetailAnnuncio");

        visualizzazioniHBox.getChildren().addAll(visualizzazioniImageView, visualizzazioniText);
        detailFlowPane.getChildren().add(visualizzazioniHBox);
    }
}