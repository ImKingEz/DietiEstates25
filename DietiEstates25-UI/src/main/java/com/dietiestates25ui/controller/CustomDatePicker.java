package com.dietiestates25ui.controller;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.stream.IntStream;

public class CustomDatePicker extends DatePicker {

    private final PopupControl popup;
    private final ComboBox<Integer> giornoCombo;
    private final ComboBox<String> meseCombo;
    private final ComboBox<Integer> annoCombo;
    private final ObjectProperty<LocalDate> selectedDate = new SimpleObjectProperty<>();

    public CustomDatePicker() {
        super();

        this.setShowWeekNumbers(false);
        this.showingProperty().addListener((obs, wasShowing, isNowShowing) -> {
            if (Boolean.TRUE.equals(isNowShowing)) this.hide();
        });

        giornoCombo = new ComboBox<>();
        meseCombo = new ComboBox<>();
        annoCombo = new ComboBox<>();

        Label giornoLabel = new Label("Giorno");
        Label meseLabel = new Label("Mese");
        Label annoLabel = new Label("Anno");

        HBox giornoBox = new HBox(5, giornoLabel, giornoCombo);
        giornoBox.setAlignment(Pos.CENTER_LEFT);
        HBox meseBox = new HBox(5, meseLabel, meseCombo);
        meseBox.setAlignment(Pos.CENTER_LEFT);
        HBox annoBox = new HBox(5, annoLabel, annoCombo);
        annoBox.setAlignment(Pos.CENTER_LEFT);

        popup = new PopupControl();
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);

        initComboBoxValues();

        VBox popupContent = new VBox(5, annoBox, meseBox, giornoBox);
        popupContent.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-color: gray; -fx-border-radius: 5;");
        popup.getScene().setRoot(popupContent);

        this.setOnMouseClicked(event -> {
            if (!popup.isShowing()) {
                aggiornaGiorni();
                popup.show(this, this.localToScreen(0, this.getHeight()).getX(), this.localToScreen(0, this.getHeight()).getY());
            } else {
                popup.hide();
            }
        });

        giornoCombo.setOnAction(e -> aggiornaData());
        meseCombo.setOnAction(e -> aggiornaGiorni());
        annoCombo.setOnAction(e -> aggiornaGiorni());

        selectedDate.addListener((obs, oldDate, newDate) -> setValue(newDate));
    }

    private void initComboBoxValues() {
        annoCombo.getItems().addAll(IntStream.rangeClosed(LocalDate.now().getYear()-100, LocalDate.now().getYear()-18).boxed().toList());

        String[] mesi = {"Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno",
                "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"};
        meseCombo.getItems().addAll(mesi);
    }

    private void aggiornaGiorni() {
        if (annoCombo.getValue() != null && meseCombo.getSelectionModel().getSelectedIndex() != -1) {
            int anno = annoCombo.getValue();
            int mese = meseCombo.getSelectionModel().getSelectedIndex() + 1;
            int giorni = YearMonth.of(anno, mese).lengthOfMonth();

            giornoCombo.getItems().clear();
            giornoCombo.getItems().addAll(IntStream.rangeClosed(1, giorni).boxed().toList());
        }
    }

    private void aggiornaData() {
        if (giornoCombo.getValue() != null && meseCombo.getValue() != null && annoCombo.getValue() != null) {
            int giorno = giornoCombo.getValue();
            int mese = meseCombo.getSelectionModel().getSelectedIndex() + 1;
            int anno = annoCombo.getValue();

            LocalDate selected = LocalDate.of(anno, mese, giorno);
            LocalDate oggi = LocalDate.now();
            LocalDate maggiorenne = oggi.minusYears(18);

            if (selected.isBefore(maggiorenne) || selected.isEqual(maggiorenne)) {
                selectedDate.set(selected);
                popup.hide();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errore");
                alert.setHeaderText(null);
                alert.setContentText("La persona deve essere maggiorenne per selezionare questa data.");
                alert.showAndWait();
                giornoCombo.getSelectionModel().clearSelection();
                meseCombo.getSelectionModel().clearSelection();
                annoCombo.getSelectionModel().clearSelection();
            }
        }
    }
}
