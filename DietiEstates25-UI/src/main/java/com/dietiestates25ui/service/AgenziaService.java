package com.dietiestates25ui.service;

import com.dietiestates25.dto.AgenziaDTO;
import com.dietiestates25ui.exception.*;
import com.dietiestates25ui.handler.FormValidator;
import com.dietiestates25ui.model.AgenziaImmobiliare;
import javafx.application.Platform;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AgenziaService extends ApiService {

    private static final Logger logger = LoggerFactory.getLogger(AgenziaService.class);
    private static final String BASE_URL = "http://localhost:8080/api/agenzie";

    @Override
    protected String getBaseUrl() {
        return BASE_URL;
    }

    public AgenziaDTO getAgenziaDetails(Long agenziaId, String token) throws GenericServiceException {
        return executeAndHandle("/" + agenziaId, "GET", null, token, AgenziaDTO.class);
    }

    public void registerAgenzia(AgenziaImmobiliare agenzia, File logoFile, String password) throws GenericServiceException {
        try {
            if (agenzia.getNome() == null || agenzia.getEmail() == null || agenzia.getPartitaIva() == null || agenzia.getIndirizzo() == null || agenzia.getTelefono() == null || agenzia.getLogo() == null || password == null || logoFile == null) {
                throw new IllegalArgumentException("Tutti i campi sono obbligatori.");
            }
            if (agenzia.getNome().isBlank() || agenzia.getEmail().isBlank() || agenzia.getPartitaIva().isBlank() || agenzia.getIndirizzo().isBlank() || agenzia.getTelefono().isBlank() || agenzia.getLogo().isBlank() || password.isBlank()) {
                throw new IllegalArgumentException("Tutti i campi sono obbligatori.");
            }
            if (agenzia.getNome().matches("^[a-zA-Z0-9 ]+$")) {
                throw new IllegalArgumentException("Il nome dell'agenzia non può contenere caratteri speciali");
            }
            if (FormValidator.isValidEmail(agenzia.getEmail())) {
                throw new IllegalArgumentException("Email non valida.");
            }
            if (FormValidator.isValidPassword(password)) {
                throw new IllegalArgumentException("La password deve contenere almeno 8 caratteri, una lettera maiuscola e un numero.");
            }
            if (FormValidator.isValidPartitaIVA(agenzia.getPartitaIva())) {
                throw new IllegalArgumentException("Partita IVA non valida.");
            }
            if (FormValidator.isValidTelefono(agenzia.getTelefono())) {
                throw new IllegalArgumentException("Telefono non valido.");
            }
            if (!validateImage(logoFile)) {
                throw new IllegalArgumentException("Il file del logo non rispetta le caratteristiche necessarie.");
            }
            logger.info("Registrazione dell'agenzia: {}", agenzia.getNome());
            MultipartBodyPublisher publisher = new MultipartBodyPublisher();
            addFormDataPartAndFilePartForPublisher(agenzia, logoFile, password, publisher);

            byte[] requestBody = publisher.build();
            String contentType = publisher.getContentType();

            executeAndHandleMultipart("/register", "POST", requestBody, contentType, null, null);
            logger.info("Agenzia registrata con successo.");
        } catch (Exception e) {
            throw handleGenericException(e.getMessage(), e);
        }
    }

    private boolean validateImage(File file) {
        final int MAX_IMAGE_SIZE = 512;
        final int MAX_FILE_SIZE = 2048;

        if (file.length() > MAX_FILE_SIZE) {
            return false;
        }

        try {
            Image image = new Image(file.toURI().toString());
            double width = image.getWidth();
            double height = image.getHeight();

            if (width > MAX_IMAGE_SIZE || height > MAX_IMAGE_SIZE) {
                return false;
            }

            if (width != height) {
                return false;
            }

        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private static void addFormDataPartAndFilePartForPublisher(AgenziaImmobiliare agenzia, File logoFile, String password, MultipartBodyPublisher publisher) throws IOException {
        publisher.addFormDataPart("nome", agenzia.getNome());
        publisher.addFormDataPart("partitaIva", agenzia.getPartitaIva());
        publisher.addFormDataPart("indirizzo", agenzia.getIndirizzo());
        publisher.addFormDataPart("email", agenzia.getEmail());
        publisher.addFormDataPart("telefono", agenzia.getTelefono());
        publisher.addFormDataPart("password", password);

        if (logoFile != null) {
            Path logoPath = Paths.get(logoFile.getAbsolutePath());
            String mimeType = Files.probeContentType(logoPath);
            publisher.addFilePart("logo", logoFile.getName(), mimeType, logoPath);
        }
    }

    public String generateRandomPassword(int len) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        sb.append((char) (random.nextInt(26) + 'A'));

        sb.append((char) (random.nextInt(10) + '0'));

        for (int i = 2; i < len; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        List<Character> charList = new ArrayList<>();
        for (char c : sb.toString().toCharArray()) {
            charList.add(c);
        }
        Collections.shuffle(charList, random);

        StringBuilder shuffledPassword = new StringBuilder();
        for (char c : charList) {
            shuffledPassword.append(c);
        }

        return shuffledPassword.toString();
    }

    @Override
    protected void handleErrorResponse(int statusCode, HttpResponse<String> response) throws AuthenticationException, ApiClientException, ServiceUnavailableException {
        if (statusCode == 409) {
            logEmailAlreadyInUse(response);
            throw new AuthenticationException("Email già in uso. Inserisci un'altra email.");
        } else if (statusCode >= 400 && statusCode < 500) {
            logClientError(statusCode, response.body());
            throw new ApiClientException("Errore del client: " + statusCode);
        } else if (statusCode >= 500) {
            logServerError(statusCode, response.body());
            throw new ServiceUnavailableException("Errore del server.");
        } else {
            logGenericException(statusCode, response.body());
            throw new ApiClientException(response.body());
        }
    }
}