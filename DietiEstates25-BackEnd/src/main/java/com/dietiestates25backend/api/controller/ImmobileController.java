package com.dietiestates25backend.api.controller;

import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25.dto.ImmobileDTO;
import com.dietiestates25backend.business.entity.Immobile;
import com.dietiestates25backend.business.service.ImmobileService;
import com.dietiestates25backend.business.service.AuthService;
import com.dietiestates25backend.business.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/immobili")
public class ImmobileController {

    private static final Logger logger = LoggerFactory.getLogger(ImmobileController.class);
    @Autowired
    private ImmobileService immobileService;

    @Autowired
    private AuthService authService;
    @Autowired
    private JwtService jwtService;


    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Immobile>> createImmobile(
            @RequestBody ImmobileDTO immobileDTO,
            @RequestHeader("Authorization") String authorizationHeader) {

        logger.info("Ricevuta richiesta di creazione immobile: {}", immobileDTO);
        String token = authorizationHeader.substring(7);
        try {
            UserDetails userDetails = authService.loadUserByUsername(jwtService.extractUsername(token));
            if (jwtService.isTokenValid(token, userDetails)) {
                ResponseEntity<ApiResponse<Immobile>> response = immobileService.saveImmobile(immobileDTO,userDetails.getUsername());
                if(response.getStatusCode() == HttpStatus.CREATED){
                    Immobile savedImmobile = response.getBody().getData();
                    logger.info("Immobile creato con successo con ID: {}", savedImmobile.getId());
                    return response;
                } else {
                    logger.error("Errore durante la creazione dell'immobile: {}", response.getStatusCode());
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }


            } else {
                logger.error("createImmobile() failed, token not valid");
                ApiResponse<Immobile> response = new ApiResponse<>(false, null, "Token non valido");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }


        } catch (Exception e) {
            logger.error("Errore durante la creazione dell'immobile: {}", e.getMessage(), e);
            ApiResponse<Immobile> response = new ApiResponse<>(false, null, "Errore durante la creazione dell'immobile: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}