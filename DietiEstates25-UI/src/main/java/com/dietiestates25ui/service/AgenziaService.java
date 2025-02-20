package com.dietiestates25ui.service;

import com.dietiestates25.dto.AgenziaDTO;
import com.dietiestates25ui.exception.*;
import com.dietiestates25ui.model.AgenziaImmobiliare;
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
            MultipartBodyPublisher publisher = new MultipartBodyPublisher();
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

            fetchCsrfToken();

            byte[] requestBody = publisher.build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(getBaseUrl() + "/register"))
                    .header("Content-Type", publisher.getContentType())
                    .header(csrfTokenHeaderName, csrfTokenValue)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                    .build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            if (statusCode != 201) {
                handleErrorResponse(statusCode, response);
                throw new GenericServiceException("Registrazione fallita con status code: " + statusCode);
            }

        } catch (Exception e) {
            throw handleGenericException(e.getMessage(), e);
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