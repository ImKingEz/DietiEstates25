package com.dietiestates25backend.api.controller;

import com.dietiestates25.dto.UtenteDTO;
import com.dietiestates25backend.api.dto.*;
import com.dietiestates25backend.business.entity.Utente;
import com.dietiestates25backend.business.service.AuthService;
import com.dietiestates25backend.business.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final JwtService jwtService;

    @Autowired
    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UtenteDTO>> registerUser(@RequestBody @Valid RegisterDTO registerDTO) {
        logger.debug("registerUser() called with registerDTO: {}", registerDTO.getEmail());
        Utente utente = new Utente(
                registerDTO.getNome(),
                registerDTO.getCognome(),
                registerDTO.getCitta(),
                registerDTO.getEmail(),
                registerDTO.getPassword()
        );

        try {
            UtenteDTO utenteDTO = authService.registraUtente(utente);
            logger.debug("registerUser() successful with user: {}", utenteDTO.getEmail());
            ApiResponse<UtenteDTO> response = new ApiResponse<>(true, utenteDTO, null);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (DataIntegrityViolationException ex) {
            logger.error("registerUser() failed, email already registered: {}", registerDTO.getEmail());
            ApiResponse<UtenteDTO> response = new ApiResponse<>(false, null, "Email già in uso");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (Exception ex) {
            logger.error("registerUser() failed with error: {}", ex.getMessage());
            ApiResponse<UtenteDTO> response = new ApiResponse<>(false, null, "Errore durante la registrazione : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginUser(
            @RequestBody @Valid LoginDTO loginDTO,
            @RequestHeader(value = "X-XSRF-TOKEN", required = false) String csrfTokenHeader
    ) {
        logger.debug("loginUser() called with email: {}", loginDTO.getEmail());
        logger.debug("X-CSRF-TOKEN header: {}", csrfTokenHeader); // Aggiungi questo log

        try {
            String token = authService.loginUtente(loginDTO.getEmail(), loginDTO.getPassword(), csrfTokenHeader); // Passa il token al service
            LoginResponse loginResponse = new LoginResponse(token);
            logger.debug("loginUser() successful for user: {}", loginDTO.getEmail());
            ApiResponse<LoginResponse> response = new ApiResponse<>(true, loginResponse, null);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException ex) {
            logger.error("loginUser() failed, authentication error for user: {}", loginDTO.getEmail());
            ApiResponse<LoginResponse> response = new ApiResponse<>(false, null, "Login fallito");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception ex){
            logger.error("loginUser() failed with error: {}", ex.getMessage());
            ApiResponse<LoginResponse> response = new ApiResponse<>(false, null, "Login fallito : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<UtenteDTO>> updateUtente(
            @RequestBody UpdateUtenteDTO updateUtenteDTO,
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestHeader(value = "X-XSRF-TOKEN", required = false) String csrfTokenHeader, // CSRF
            HttpServletRequest request) {
        logger.debug("updateUtente() called with user: {}", updateUtenteDTO);

        // Verifica CSRF
        if (csrfTokenHeader == null ) {
            logger.error("updateUtente() failed, CSRF token missing");
            return new ResponseEntity<>(new ApiResponse<>(false, null, "CSRF token mancante"), HttpStatus.FORBIDDEN);
        }
        //Estrazione dell'header
        String token = authorizationHeader.substring(7);
        try {
            UserDetails userDetails = authService.loadUserByUsername(jwtService.extractUsername(token));
            if (jwtService.isTokenValid(token, userDetails)) {
                UtenteDTO utenteDTO = authService.updateUtente(updateUtenteDTO, userDetails.getUsername());
                logger.debug("updateUtente() successful for user: {}", userDetails.getUsername());
                ApiResponse<UtenteDTO> response = new ApiResponse<>(true, utenteDTO, null);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                logger.error("updateUtente() failed, token not valid");
                ApiResponse<UtenteDTO> response = new ApiResponse<>(false, null, "Token non valido");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            logger.error("updateUtente() failed with error: {}", ex.getMessage());
            ApiResponse<UtenteDTO> response = new ApiResponse<>(false, null, "Errore durante l'aggiornamento dell'utente: " + ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UtenteDTO>> getUserDetails(@RequestHeader("Authorization") String authorizationHeader) {
        logger.debug("getUserDetails() called");
        String token = authorizationHeader.substring(7);
        try {
            UserDetails userDetails = authService.loadUserByUsername(jwtService.extractUsername(token));
            if (jwtService.isTokenValid(token, userDetails)) {
                UtenteDTO utenteDTO = authService.getUtenteDetails(userDetails.getUsername());
                logger.debug("getUserDetails() successful for user: {}", userDetails.getUsername());
                ApiResponse<UtenteDTO> response = new ApiResponse<>(true, utenteDTO, null);
                return ResponseEntity.ok(response);
            } else {
                logger.error("getUserDetails() failed, token not valid");
                ApiResponse<UtenteDTO> response = new ApiResponse<>(false, null, "Token non valido");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

        } catch (Exception ex) {
            logger.error("getUserDetails() failed with error: {}", ex.getMessage());
            ApiResponse<UtenteDTO> response = new ApiResponse<>(false, null, "Errore nel recupero dei dettagli dell'utente: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}