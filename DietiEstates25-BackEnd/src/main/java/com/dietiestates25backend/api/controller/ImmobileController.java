package com.dietiestates25backend.api.controller;

import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25.dto.ImmobileDTO;
import com.dietiestates25backend.api.dto.RegisterImmobileDTO;
import com.dietiestates25backend.business.service.ImmobileService;
import com.dietiestates25backend.business.service.AuthService;
import com.dietiestates25backend.business.service.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImmobileDTO>> createImmobile(
            @ModelAttribute RegisterImmobileDTO registerImmobileDTO,
            @RequestHeader("Authorization") String authorizationHeader) {

        logger.debug("createImmobile() called with @ModelAttribute");
        logger.debug("Authorization header: {}", authorizationHeader);
        logger.debug("RegisterImmobileDTO: {}", registerImmobileDTO);

        if (registerImmobileDTO.getImmaginiUrls() != null) {
            logger.debug("immaginiUrls size: {}", registerImmobileDTO.getImmaginiUrls().size());
            for (MultipartFile file : registerImmobileDTO.getImmaginiUrls()) {
                logger.debug("Image file name: {}, size: {}", file.getOriginalFilename(), file.getSize());
            }
        } else {
            logger.debug("immaginiUrls is null");
        }

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