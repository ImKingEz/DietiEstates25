package com.dietiestates25ui.controller;

import com.dietiestates25ui.model.AgenziaImmobiliare;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class AgenziaCredentialsController extends AbstractController implements Initializable {

    AgenziaImmobiliare agenzia;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setAgenzia(AgenziaImmobiliare agenzia) {
        this.agenzia = agenzia;
    }
}
