package com.dietiestates25backend.api.controller;

import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25.dto.UtenteDTO;
import com.dietiestates25.dto.LoginResponse;
import com.dietiestates25backend.api.dto.*;
import com.dietiestates25backend.business.service.AuthService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class AuthController extends BaseController {

    public static final String ENTITY_TYPE = "Utente";
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UtenteDTO>> registerUser(@RequestBody @Valid RegisterUtenteDTO registerDTO) {
        String email = registerDTO.getEmail();
        logger.debug("registerUser() called with registerDTO: {}", email);
        try {
            UtenteDTO utenteDTO = authService.registraUtente(registerDTO);
            logger.debug("registerUser() successful with user: {}", utenteDTO.getEmail());
            return successResponse(utenteDTO, HttpStatus.CREATED);
        } catch (DataIntegrityViolationException ex) {
            logger.error("registerUser() failed, email already registered: {}", email);
            return handleDataIntegrityViolation(ex, ENTITY_TYPE);
        } catch (Exception ex) {
            logger.error("registerUser() failed with error: {}", ex.getMessage());
            return handleGenericException(ex, "Errore durante la registrazione", ENTITY_TYPE);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginUser(
            @RequestBody @Valid LoginDTO loginDTO,
            @RequestHeader(value = "X-XSRF-TOKEN", required = false) String csrfTokenHeader
    ) {
        String email = loginDTO.getEmail();
        logger.debug("loginUser() called with email: {}", email);
        logger.debug("X-CSRF-TOKEN header: {}", csrfTokenHeader);

        try {
            String token = authService.loginUtente(email, loginDTO.getPassword(), csrfTokenHeader);
            LoginResponse loginResponse = new LoginResponse(token);
            logger.debug("loginUser() successful for user: {}", email);
            return successResponse(loginResponse);
        } catch (AuthenticationException ex) {
            logger.error("loginUser() failed, authentication error for user: {}", email);
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Credenziali non valide"), HttpStatus.UNAUTHORIZED);
        } catch (Exception ex){
            logger.error("loginUser() failed with error: {}", ex.getMessage());
            return handleGenericException(ex, "Login fallito", ENTITY_TYPE);
        }
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('ROLE_UTENTE')")
    public ResponseEntity<ApiResponse<UtenteDTO>> updateUtente(
            @RequestBody UpdateUtenteDTO updateUtenteDTO,
            @RequestHeader(value = "X-XSRF-TOKEN", required = false) String csrfTokenHeader) {
        logger.debug("updateUtente() called with user: {}", updateUtenteDTO);

        if (csrfTokenHeader == null ) {
            logger.error("updateUtente() failed, CSRF token missing");
            return new ResponseEntity<>(new ApiResponse<>(false, null, "CSRF token mancante"), HttpStatus.FORBIDDEN);
        }

        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            UtenteDTO utenteDTO = authService.updateUtente(updateUtenteDTO, userEmail);
            logger.debug("updateUtente() successful for user: {}", userEmail);
            return successResponse(utenteDTO);

        } catch (Exception ex) {
            logger.error("updateUtente() failed with error: {}", ex.getMessage());
            return handleGenericException(ex, "Errore durante l'aggiornamento dell'utente", ENTITY_TYPE);
        }
    }

    @GetMapping("/me")
    @PermitAll
    public ResponseEntity<ApiResponse<UtenteDTO>> getUserDetails() {
        logger.debug("getUserDetails() called");

        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            UtenteDTO utenteDTO = authService.getUtenteDetails(userEmail);
            logger.debug("getUserDetails() successful for user: {}", userEmail);
            return successResponse(utenteDTO);

        } catch (Exception ex) {
            logger.error("getUserDetails() failed with error: {}", ex.getMessage());
            return handleGenericException(ex, "Errore nel recupero dei dettagli dell'utente", ENTITY_TYPE);
        }
    }
}