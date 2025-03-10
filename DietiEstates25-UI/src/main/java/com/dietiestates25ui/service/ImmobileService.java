package com.dietiestates25ui.service;

import com.dietiestates25.dto.ImmobileDTO;
import com.dietiestates25ui.exception.*;
import com.dietiestates25ui.model.Immobile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.http.HttpResponse;

public class ImmobileService extends ApiService {

    private static final Logger logger = LoggerFactory.getLogger(ImmobileService.class);

    private static final String BASE_URL = "http://" + BACKEND_IP + ":8080/api/immobili";

    @Override
    protected String getBaseUrl() {
        return BASE_URL;
    }

    public ImmobileDTO salvaImmobile(Immobile immobile, String token) throws GenericServiceException {
        return executeAndHandle("/create", "POST", immobile, token, ImmobileDTO.class);
    }

    public ImmobileDTO getImmobileDetails(long idImmobile, String token) throws GenericServiceException {
        return executeAndHandle("/" + idImmobile, "GET", null, token, ImmobileDTO.class);
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