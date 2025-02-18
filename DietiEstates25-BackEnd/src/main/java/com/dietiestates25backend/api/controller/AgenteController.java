package com.dietiestates25backend.api.controller;

import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25.dto.AgenteDTO;
import com.dietiestates25.dto.LoginResponse;
import com.dietiestates25backend.api.dto.LoginDTO;
import com.dietiestates25backend.api.dto.RegisterAgenteDTO;
import com.dietiestates25backend.business.service.AgenteService;
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
@RequestMapping("/api/agenti")
public class AgenteController extends BaseController {

    public static final String ENTITY_TYPE = "Agente";
    private final AgenteService agenteService;

    @Autowired
    public AgenteController(AgenteService agenteService) {
        this.agenteService = agenteService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginUser(
            @RequestBody @Valid LoginDTO loginDTO,
            @RequestHeader(value = "X-XSRF-TOKEN", required = false) String csrfTokenHeader
    ) {
        String email = loginDTO.getEmail();
        logger.debug("loginAgente() called with email: {}", email);
        logger.debug("X-CSRF-TOKEN header: {}", csrfTokenHeader);

        try {
            String token = agenteService.loginAgente(email, loginDTO.getPassword(), csrfTokenHeader);
            LoginResponse loginResponse = new LoginResponse(token);
            logger.debug("loginUser() successful for agente: {}", email);
            return successResponse(loginResponse);
        } catch (AuthenticationException ex) {
            logger.error("loginUser() failed, authentication error for agente: {}", email);
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Credenziali non valide"), HttpStatus.UNAUTHORIZED);
        } catch (Exception ex){
            logger.error("loginUser() failed with error: {}", ex.getMessage());
            return handleGenericException(ex, "Login fallito", ENTITY_TYPE);
        }
    }

    @PostMapping("/register")
    @PreAuthorize("hasAuthority('WRITE_AGENTE')")
    public ResponseEntity<ApiResponse<AgenteDTO>> registerAgente(@RequestBody @Valid RegisterAgenteDTO registerAgenteDTO) {
        String email = registerAgenteDTO.getEmail();
        logger.debug("registerAgente() called with registerAgenteDTO: {}", email);
        try {
            AgenteDTO agenteDTO = agenteService.registraAgente(registerAgenteDTO);
            logger.debug("registerAgente() successful with agente: {}", agenteDTO.getEmail());
            return successResponse(agenteDTO, HttpStatus.CREATED);
        } catch (DataIntegrityViolationException ex) {
            logger.error("registerAgente() failed, email already registered: {}", email);
            return handleDataIntegrityViolation(ex, ENTITY_TYPE);
        } catch (Exception ex) {
            logger.error("registerAgente() failed with error: {}", ex.getMessage());
            return handleGenericException(ex, "Errore durante la registrazione", ENTITY_TYPE);
        }
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('WRITE_AGENTE')")
    public ResponseEntity<ApiResponse<AgenteDTO>> updateAgente(@RequestBody @Valid RegisterAgenteDTO registerAgenteDTO) {
        String email = registerAgenteDTO.getEmail();
        logger.debug("updateAgente() called with registerAgenteDTO: {}", email);
        try {
            AgenteDTO agenteDTO = agenteService.registraAgente(registerAgenteDTO);
            logger.debug("updateAgente() successful with agente: {}", agenteDTO.getEmail());
            return successResponse(agenteDTO);
        } catch (DataIntegrityViolationException ex) {
            logger.error("updateAgente() failed, email already registered: {}", email);
            return handleDataIntegrityViolation(ex, ENTITY_TYPE);
        } catch (Exception ex) {
            logger.error("updateAgente() failed with error: {}", ex.getMessage());
            return handleGenericException(ex, "Errore durante la registrazione", ENTITY_TYPE);
        }
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('READ_AGENTE')")
    public ResponseEntity<ApiResponse<AgenteDTO>> getUserDetails() {
        logger.debug("getUserDetails() called");
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            AgenteDTO agenteDTO = agenteService.getAgenteDetails(userEmail);
            logger.debug("getUserDetails() successful for agente: {}", userEmail);
            return successResponse(agenteDTO);
        } catch (Exception ex) {
            logger.error("getUserDetails() failed with error: {}", ex.getMessage());
            return handleGenericException(ex, "Errore nel recupero dei dettagli dell'agente", ENTITY_TYPE);
        }
    }
}