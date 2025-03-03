package com.dietiestates25ui.service;

import com.dietiestates25.dto.*;
import com.dietiestates25ui.exception.*;
import com.dietiestates25ui.model.Annuncio;
import com.dietiestates25ui.model.FiltroAnnunci;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class AnnuncioService extends ApiService {

    private static final Logger logger = LoggerFactory.getLogger(AnnuncioService.class);

    public AnnuncioDTO salvaAnnuncio(Annuncio annuncio, String token, List<File> selectedImageList) throws GenericServiceException {
        try {
            logger.info("Salvataggio dell'annuncio: {}", annuncio.getTitolo());
            MultipartBodyPublisher publisher = new MultipartBodyPublisher();
            addFormDataPartAndFilePartForPublisher(annuncio, selectedImageList, publisher);

            byte[] requestBody = publisher.build();
            String contentType = publisher.getContentType();

            return executeAndHandleMultipart("/create", "POST", requestBody, contentType, token, AnnuncioDTO.class);
        } catch (Exception e) {
            throw handleGenericException("Errore durante il salvataggio dell'annuncio: " + e.getMessage(), e);
        }
    }

    private void addFormDataPartAndFilePartForPublisher(Annuncio annuncio, List<File> selectedImageList, MultipartBodyPublisher publisher) throws IOException {
        publisher.addFormDataPart("titolo", annuncio.getTitolo());
        publisher.addFormDataPart("tipo", annuncio.getTipo());
        publisher.addFormDataPart("prezzo", String.valueOf(annuncio.getPrezzo()));
        publisher.addFormDataPart("descrizione", annuncio.getDescrizione());
        publisher.addFormDataPart("idImmobile", String.valueOf(annuncio.getIdImmobile()));
        publisher.addFormDataPart("idAgente", String.valueOf(annuncio.getIdAgente()));

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
        return "http://localhost:8080/api/annunci";
    }

    public List<AnnuncioDTO> searchAnnunciByCittaAndFiltro(String citta, FiltroAnnunci filtro, String token) throws GenericServiceException {
        try {
            String encodedCitta = URLEncoder.encode(citta, StandardCharsets.UTF_8);
            String path = "/search?citta=" + encodedCitta;
            return executeAndHandleSearch(path, "POST", filtro, token, new TypeReference<List<AnnuncioDTO>>() {});
        } catch (Exception e) {
            throw handleGenericException("Errore durante la ricerca degli annunci: " + e.getMessage(), e);
        }
    }

    private <T> T executeAndHandleSearch(String path, String method, Object body, String token, TypeReference<T> typeReference) throws GenericServiceException {
        try {
            fetchCsrfToken();
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(getBaseUrl() + path))
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .header(csrfTokenHeaderName, csrfTokenValue);

            if (token != null && !token.isEmpty()) {
                requestBuilder.header(AUTHORIZATION, BEARER + token);
            }

            HttpRequest request;
            String jsonBody;
            switch (method.toUpperCase()) {
                case "POST":
                    jsonBody = objectMapper.writeValueAsString(body);
                    requestBuilder.POST(HttpRequest.BodyPublishers.ofString(jsonBody));
                    break;
                case "PUT":
                    jsonBody = objectMapper.writeValueAsString(body);
                    requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(jsonBody));
                    break;
                case "GET":
                    requestBuilder.GET();
                    break;
                default:
                    throw new IllegalArgumentException("Metodo HTTP non supportato: " + method);
            }

            request = requestBuilder.build();

            HttpResponse<String> response = executeRequest(request);
            int statusCode = response.statusCode();

            if (statusCode >= 200 && statusCode < 300) {
                return handleResponseSearch(response, typeReference);
            } else {
                handleErrorResponse(statusCode, response);
                throw new GenericServiceException("Operazione fallita con status code: " + statusCode);
            }

        } catch (Exception e) {
            throw handleGenericException(e.getMessage(), e);
        }
    }

    private <T> T handleResponseSearch(HttpResponse<String> response, TypeReference<T> typeReference) throws GenericServiceException, ApiClientException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ApiResponse<?> apiResponse = objectMapper.readValue(response.body(), ApiResponse.class);
            if (apiResponse != null && apiResponse.isSuccess()) {
                Object data = apiResponse.getData();
                String dataJson = objectMapper.writeValueAsString(data);
                return objectMapper.readValue(dataJson, typeReference);
            } else {
                String errorMessage = (apiResponse != null && apiResponse.getMessage() != null) ? apiResponse.getMessage() : "Errore sconosciuto.";
                throw new GenericServiceException(errorMessage);
            }
        } catch (IOException e) {
            logger.error("Errore durante la lettura della risposta JSON: ", e);
            throw new ApiClientException("Risposta del server non valida. Riprova più tardi.");
        }
    }

    public AnnuncioDTO getAnnuncioByIdImmobile(long idImmobile, String token) throws GenericServiceException {
        return executeAndHandle("/immobile/" + idImmobile,
                "GET",
                null,
                token,
                AnnuncioDTO.class);
    }

    public List<AnnuncioDTO> searchAnnunciInRadius(MapSearchDTO map, FiltroAnnunci filtro, String token) throws GenericServiceException {
        try {
            AnnunciRadiusSearchDTO searchDTO = getAnnunciRadiusSearchDTO(map, filtro);

            return executeAndHandleSearch(
                    "/search/radius",
                    "POST",
                    searchDTO,
                    token,
                    new TypeReference<List<AnnuncioDTO>>() {}
            );
        } catch (Exception e) {
            throw handleGenericException("Errore durante la ricerca degli annunci in radius: " + e.getMessage(), e);
        }
    }

    @NotNull
    private static AnnunciRadiusSearchDTO getAnnunciRadiusSearchDTO(MapSearchDTO map, FiltroAnnunci filtro) {
        FiltroAnnunciDTO filtroDTO = new FiltroAnnunciDTO(filtro.getTipo(), filtro.getTipologia(), filtro.getPrezzoMin(),
                filtro.getPrezzoMax(), filtro.getSuperficieMin(), filtro.getSuperficieMax(), filtro.getLocali(), filtro.getBagni(),
                filtro.getPiano(), filtro.getClasseEnergetica(), filtro.getAscensore(), filtro.getPortineria(), filtro.getClimatizzazione(),
                filtro.getVicinoScuola(), filtro.getVicinoParco(), filtro.getVicinoTrasportoPubblico());
        AnnunciRadiusSearchDTO searchDTO = new AnnunciRadiusSearchDTO();
        searchDTO.setMap(map);
        searchDTO.setFiltro(filtroDTO);
        return searchDTO;
    }

    public AnnuncioDTO updateAnnuncioStats(Long idImmobile, String tipoAggiornamento, String token) throws GenericServiceException {
        UpdateAnnuncioDTO updateAnnuncioDTO = new UpdateAnnuncioDTO(idImmobile, tipoAggiornamento);
        return executeAndHandle("/updateStats", "PUT", updateAnnuncioDTO, token, AnnuncioDTO.class);
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


}