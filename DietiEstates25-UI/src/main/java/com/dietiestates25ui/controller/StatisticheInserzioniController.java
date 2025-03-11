package com.dietiestates25ui.controller;

import com.dietiestates25.dto.AnnuncioDTO;
import com.dietiestates25.dto.ImmobileDTO;
import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.service.AgenteService;
import com.dietiestates25ui.service.AnnuncioService;
import com.dietiestates25ui.service.ImmobileService;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
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
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;

import java.io.File;
import java.io.FileWriter;
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
    private Button esportaStatistichePDFButton;

    @FXML
    private FlowPane listaAnnunciFlowPane;

    @FXML
    private Button tornaIndietroButton;

    private ImmobileService immobileService = new ImmobileService();

    private AnnuncioService annuncioService = new AnnuncioService();

    private Long idAgente;

    List<AnnuncioDTO> annunci = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(this::searchUserNameAndUpdateProfileHBox);

        tornaIndietroButton.setOnAction(event -> openAgenteDashboardPage(tornaIndietroButton));

        idAgente = findAgenteId();
        Platform.runLater(this::updateAnnunci);

        esportaStatisticheButton.setOnAction(event -> esportaStatisticheCSV());
        esportaStatistichePDFButton.setOnAction(event -> esportaStatistichePDF());
    }

    private void esportaStatistichePDF() {
        if (annunci.isEmpty()) {
            showPopup(POPUP_ERROR_TITLE, "Nessun immobile caricato.", ERROR_ICON);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva statistiche annunci");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf"));
        File file = fileChooser.showSaveDialog(esportaStatisticheButton.getScene().getWindow());

        if (file != null) {
            try (PdfWriter writer = new PdfWriter(file);
                 PdfDocument pdfDocument = new PdfDocument(writer);
                 Document document = new Document(pdfDocument)) {

                PdfFont font = PdfFontFactory.createFont();

                Paragraph title = new Paragraph("Statistiche Annunci Immobiliari")
                        .setFont(font)
                        .setFontSize(16)
                        .setTextAlignment(TextAlignment.CENTER);
                document.add(title);

                Table table = new Table(UnitValue.createPercentArray(new float[]{40, 20, 20, 20}));
                table.setWidth(UnitValue.createPercentValue(100));

                table.addHeaderCell(createHeaderCell("Titolo", font));
                table.addHeaderCell(createHeaderCell("Visualizzazioni", font));
                table.addHeaderCell(createHeaderCell("Offerte", font));
                table.addHeaderCell(createHeaderCell("Visite Prenotate", font));

                for (AnnuncioDTO annuncio : annunci) {
                    table.addCell(createCell(annuncio.getTitolo(), font));
                    table.addCell(createCell(String.valueOf(annuncio.getNumeroVisualizzazioni()), font));
                    table.addCell(createCell(String.valueOf(annuncio.getNumeroOfferte()), font));
                    table.addCell(createCell(String.valueOf(annuncio.getNumeroVisitePrenotate()), font));
                }

                document.add(table);

                logger.info("Statistiche esportate in: {}", file.getAbsolutePath());
                showPopup("Operazione completata.", "Statistiche esportate con successo.", SUCCESS_ICON);

            } catch (IOException e) {
                logger.error("Errore di I/O durante l'esportazione: {}", e.getMessage(), e);
                showPopup(POPUP_ERROR_TITLE, "Si è verificato un errore durante l'esportazione: " + e.getMessage(), ERROR_ICON);
            }

        } else {
            logger.info("Esportazione annullata dall'utente.");
        }
    }

    private Cell createHeaderCell(String content, PdfFont font) {
        return new Cell()
                .add(new Paragraph(content).setFont(font))
                .setTextAlignment(TextAlignment.CENTER);
    }

    private Cell createCell(String content, PdfFont font) {
        return new Cell()
                .add(new Paragraph(content).setFont(font))
                .setTextAlignment(TextAlignment.CENTER);
    }

    @FXML
    private void esportaStatisticheCSV() {
        if (annunci.isEmpty()) {
            showPopup(POPUP_ERROR_TITLE, "Nessun immobile caricato.", ERROR_ICON);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva statistiche annunci");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        File file = fileChooser.showSaveDialog(esportaStatisticheButton.getScene().getWindow());

        if (file != null) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader("Titolo", "Visualizzazioni", "Offerte ricevute", "Visite prenotate")
                    .build();

            try (FileWriter fileWriter = new FileWriter(file);
                 CSVPrinter csvPrinter = new CSVPrinter(fileWriter, csvFormat)) {

                for (AnnuncioDTO annuncio : annunci) {
                    csvPrinter.printRecord(
                            annuncio.getTitolo(),
                            annuncio.getNumeroVisualizzazioni(),
                            annuncio.getNumeroOfferte(),
                            annuncio.getNumeroVisitePrenotate()
                    );
                }

                csvPrinter.flush();
                logger.info("Statistiche esportate in: {}", file.getAbsolutePath());
                showPopup("Operazione completata.", "Statistiche esportate con successo.", SUCCESS_ICON);

            } catch (IOException e) {
                logger.error("Errore di I/O durante l'esportazione: {}", e.getMessage(), e);
                showPopup(POPUP_ERROR_TITLE, "Si è verificato un errore durante l'esportazione: " + e.getMessage(), ERROR_ICON);
            }

        } else {
            logger.info("Esportazione annullata dall'utente.");
        }
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
        showLoadingIndicator();
        try {
            annunci = annuncioService.getAnnunciAgente(token, idAgente);
            visualizzaAnnunciNellaLista(annunci);
        } catch (GenericServiceException e) {
            logger.error("Errore durante il recupero degli annunci: {}", e.getMessage(), e);
        } finally {
            hideLoadingIndicator();
        }
    }

    private void visualizzaAnnunciNellaLista(List<AnnuncioDTO> annunci) {
        listaAnnunciFlowPane.getChildren().clear();
        for (AnnuncioDTO annuncio : annunci) {
            ImmobileDTO immobile = null;
            try {
                immobile = immobileService.getImmobileDetails(annuncio.getIdImmobile(), token);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/dietiestates25ui/view/annuncio-item-view.fxml"));
                HBox annuncioItem = loader.load();
                annuncioItem.setStyle("");

                AnnuncioItemController controller = loader.getController();
                controller.setAnnuncio(annuncio, immobile);

                VBox detailVBox = controller.getDetailVBox();
                detailVBox.getChildren().remove(controller.getButtonHBox());

                FlowPane detailFlowPane = controller.getDetailFlowPane();
                detailFlowPane.getChildren().clear();

                addStatsToFlowPane(new HBox(), detailFlowPane, "Visualizzazioni: ", "/com/dietiestates25ui/images/eye_open_black.png", annuncio.getNumeroVisualizzazioni());
                addStatsToFlowPane(new HBox(), detailFlowPane, "Offerte ricevute: ", "/com/dietiestates25ui/images/euroIconBlack.png", annuncio.getNumeroOfferte());
                addStatsToFlowPane(new HBox(), detailFlowPane, "Visite prenotate: ", "/com/dietiestates25ui/images/calendarIconBlack.png", annuncio.getNumeroVisitePrenotate());

                listaAnnunciFlowPane.getChildren().add(annuncioItem);
            } catch (IOException e) {
                logger.error("Errore durante il caricamento del layout dell'annuncio:", e);
            } catch (GenericServiceException e) {
                logger.error("Errore durante il recupero dei dettagli dell'immobile: {}", e.getMessage(), e);
            }
        }
    }

    private void addStatsToFlowPane(HBox visualizzazioniHBox, FlowPane detailFlowPane, String detailTipe, String imagePath, int numberDetailType) {
        visualizzazioniHBox.getStyleClass().add("detailWithIconHBox");

        ImageView visualizzazioniImageView = new ImageView();
        visualizzazioniImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
        visualizzazioniImageView.getStyleClass().add("iconDetailAnnuncio");

        Text visualizzazioniText = new Text();
        visualizzazioniText.setText(detailTipe + numberDetailType);
        visualizzazioniText.getStyleClass().add("textDetailAnnuncio");

        visualizzazioniHBox.getChildren().addAll(visualizzazioniImageView, visualizzazioniText);
        detailFlowPane.getChildren().add(visualizzazioniHBox);
    }
}