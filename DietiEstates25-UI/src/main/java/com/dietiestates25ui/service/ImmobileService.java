package com.dietiestates25ui.service;

import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25.dto.ImmobileDTO;
import com.dietiestates25ui.exception.*;
import com.dietiestates25ui.model.Immobile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.List;

public class ImmobileService extends ApiService {

    private static final Logger logger = LoggerFactory.getLogger(ImmobileService.class);

    public void salvaImmobile(Immobile immobile, String token, List<File> selectedImageList) throws GenericServiceException {
        try {
            MultipartBodyPublisher publisher = new MultipartBodyPublisher();
            addFormDataPartAndFilePartForPublisher(immobile, selectedImageList, publisher);

            fetchCsrfToken();

            byte[] requestBody = publisher.build();

            HttpRequest request;
            request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/immobili/create"))
                    .header("Content-Type", publisher.getContentType())
                    .header("Authorization", "Bearer " + token)
                    .header(csrfTokenHeaderName, csrfTokenValue)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                    .build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            if (statusCode == 201) {
                ApiResponse<ImmobileDTO> apiResponse = handleResponse(response, ImmobileDTO.class);
                if (apiResponse != null && apiResponse.isSuccess()) {
                    logger.info("Immobile salvato correttamente!");
                } else {
                    String errorMessage = (apiResponse != null && apiResponse.getMessage() != null) ? apiResponse.getMessage() : "Errore durante il salvataggio dell'immobile.";
                    throw new GenericServiceException(errorMessage);
                }
            } else {
                    handleErrorResponse(statusCode, response);
                    throw new GenericServiceException("Salvataggio immobile fallito con status code: " + statusCode + ". Errore non specificato dal server.");
            }
        } catch (Exception e) {
            throw handleGenericException("Errore durante il salvataggio dell'immobile: " + e.getMessage(), e);
        }
    }

    private static void addFormDataPartAndFilePartForPublisher(Immobile immobile, List<File> selectedImageList, MultipartBodyPublisher publisher) throws IOException {
        publisher.addFormDataPart("titolo", immobile.getTitolo());
        publisher.addFormDataPart("tipologia", immobile.getTipologia());
        publisher.addFormDataPart("indirizzo", immobile.getIndirizzo());
        publisher.addFormDataPart("prezzo", String.valueOf(immobile.getPrezzo()));
        publisher.addFormDataPart("descrizione", immobile.getDescrizione());
        publisher.addFormDataPart("dimensione", String.valueOf(immobile.getDimensione()));
        publisher.addFormDataPart("numeroCamere", String.valueOf(immobile.getNumeroCamere()));
        publisher.addFormDataPart("numeroBagni", String.valueOf(immobile.getNumeroBagni()));
        publisher.addFormDataPart("classeEnergetica", immobile.getClasseEnergetica());
        publisher.addFormDataPart("piano", String.valueOf(immobile.getPiano()));
        publisher.addFormDataPart("ascensore", String.valueOf(immobile.isAscensore()));
        publisher.addFormDataPart("portineria", String.valueOf(immobile.isPortineria()));
        publisher.addFormDataPart("climatizzazione", String.valueOf(immobile.isClimatizzazione()));
        publisher.addFormDataPart("latitudine", String.valueOf(immobile.getLatitudine()));
        publisher.addFormDataPart("longitudine", String.valueOf(immobile.getLongitudine()));
        publisher.addFormDataPart("vicinoScuole", String.valueOf(immobile.isVicinoScuole()));
        publisher.addFormDataPart("vicinoParchi", String.valueOf(immobile.isVicinoParchi()));
        publisher.addFormDataPart("vicinoTrasportoPubblico", String.valueOf(immobile.isVicinoTrasportoPubblico()));

        for (File file : selectedImageList) {
            if (file != null) {
                Path logoPath = Paths.get(file.getAbsolutePath());
                String mimeType = Files.probeContentType(file.toPath());
                publisher.addFilePart("immaginiUrls", file.getName(), mimeType, logoPath);
            }
        }
    }

    @Override
    protected String getBaseUrl() {
        return "http://localhost:8080/api/immobili";
    }

    @Override
    protected void handleErrorResponse(int statusCode, HttpResponse<String> response) throws ApiClientException, ServiceUnavailableException {
        logger.error("Ricevuta risposta con status code: {}", statusCode);

        if (statusCode >= 400 && statusCode < 500) {
            logClientError(statusCode, response.body());
            throw new ApiClientException("Errore del client: " + statusCode);
        } else if (statusCode >= 500) {
            logServerError(statusCode, response.body());
            throw new ServiceUnavailableException("Errore del server.");
        } else {
            logGenericException(statusCode, response.body());
            throw new ApiClientException("Errore generico: " + statusCode);
        }
    }

    static class MultipartBodyPublisher {
        private final String boundary;
        private final java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        private static final String LINE_FEED = "\r\n";

        public MultipartBodyPublisher() {
            this.boundary = generateBoundary();
        }

        private String generateBoundary() {
            SecureRandom random = new SecureRandom();
            String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < 20; i++) {
                buffer.append(characters.charAt(random.nextInt(characters.length())));
            }
            return buffer.toString();
        }

        public void addFormDataPart(String name, String value) {
            try {
                outputStream.write(("--" + boundary + LINE_FEED).getBytes());
                outputStream.write(("Content-Disposition: form-data; name=\"" + name + "\"" + LINE_FEED).getBytes());
                outputStream.write(("Content-Type: text/plain; charset=UTF-8" + LINE_FEED).getBytes());
                outputStream.write(LINE_FEED.getBytes());
                outputStream.write(value.getBytes());
                outputStream.write(LINE_FEED.getBytes());
            } catch (IOException e) {
                logger.error("Error while adding form data part: {}", e.getMessage());
            }
        }

        public void addFilePart(String fieldName, String fileName, String mimeType, Path filePath) {
            try {
                outputStream.write(("--" + boundary + LINE_FEED).getBytes());
                outputStream.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"" + LINE_FEED).getBytes());
                outputStream.write(("Content-Type: " + mimeType + LINE_FEED).getBytes());
                outputStream.write(("Content-Transfer-Encoding: binary" + LINE_FEED).getBytes());
                outputStream.write(LINE_FEED.getBytes());
                Files.copy(filePath, outputStream);
                outputStream.write(LINE_FEED.getBytes());
            } catch (IOException e) {
                logger.error("Error while adding file part: {}", e.getMessage());
            }
        }

        public byte[] build() {
            try {
                outputStream.write(("--" + boundary + "--" + LINE_FEED).getBytes());
            } catch (IOException e) {
                logger.error("Error while closing multipart body: {}", e.getMessage());
            }
            return outputStream.toByteArray();
        }

        public String getContentType() {
            return "multipart/form-data; boundary=" + boundary;
        }
    }
}