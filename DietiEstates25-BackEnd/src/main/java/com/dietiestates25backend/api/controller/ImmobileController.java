package com.dietiestates25backend.api.controller;

import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25.dto.ImmobileDTO;
import com.dietiestates25backend.api.dto.RegisterImmobileDTO;
import com.dietiestates25backend.business.service.ImmobileService;
import com.dietiestates25backend.business.service.AuthService;
import com.dietiestates25backend.business.service.JwtService;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/immobili")
public class ImmobileController {

    private static final Logger logger = LoggerFactory.getLogger(ImmobileController.class);

    private final ImmobileService immobileService;

    private final AuthService authService;

    private final JwtService jwtService;

    @Autowired
    public ImmobileController(ImmobileService immobileService, AuthService authService, JwtService jwtService) {
        this.immobileService = immobileService;
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //@PreAuthorize("hasRole('ROLE_AGENTE')")
    @PermitAll //TODO: remove this line
    public ResponseEntity<ApiResponse<ImmobileDTO>> createImmobile(
            @ModelAttribute RegisterImmobileDTO registerImmobileDTO,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = authorizationHeader.substring(7);

        try {
            UserDetails userDetails = authService.loadUserByUsername(jwtService.extractUsername(token));
            if (jwtService.isTokenValid(token, userDetails)) {
                ImmobileDTO immobileDTO = immobileService.saveImmobile(registerImmobileDTO);
                ApiResponse<ImmobileDTO> response = new ApiResponse<>(true, immobileDTO, null);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                logger.error("createImmobile() failed, token not valid");
                ApiResponse<ImmobileDTO> response = new ApiResponse<>(false, null, "Token non valido");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            logger.error("Errore durante la creazione dell'immobile: {}", e.getMessage(), e);
            ApiResponse<ImmobileDTO> response = new ApiResponse<>(false, null, "Errore durante la creazione dell'immobile: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}