package com.dietiestates25backend.api.controller;

import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25.dto.AmministratoreDTO;
import com.dietiestates25.dto.LoginResponse;
import com.dietiestates25backend.api.dto.RegisterAmministratoreDTO;
import com.dietiestates25backend.api.dto.LoginDTO;
import com.dietiestates25backend.business.service.AmministratoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AmministratoreController extends BaseController {

    public static final String ENTITY_TYPE = "Amministratore";
    private final AmministratoreService amministratoreService;

    @Autowired
    public AmministratoreController(AmministratoreService amministratoreService) {
        this.amministratoreService = amministratoreService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AmministratoreDTO>> registerAdmin(@RequestBody @Valid RegisterAmministratoreDTO registerDTO) {
        String email = registerDTO.getEmail();
        logger.debug("registerAdmin() called with registerDTO: {}", email);
        try {
            AmministratoreDTO amministratoreDTO = amministratoreService.registraAmministratore(registerDTO);
            logger.debug("registerAdmin() successful with admin: {}", amministratoreDTO.getEmail());
            return successResponse(amministratoreDTO, HttpStatus.CREATED);
        } catch (DataIntegrityViolationException ex) {
            logger.error("registerAdmin() failed, email already registered: {}", email);
            return handleDataIntegrityViolation(ex, ENTITY_TYPE);
        } catch (IllegalArgumentException ex) {
            logger.error("registerAdmin() failed, agency not found: {}", registerDTO.getIdAgenzia());
            ApiResponse<AmministratoreDTO> response = new ApiResponse<>(false, null, ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginAdmin(
            @RequestBody @Valid LoginDTO loginDTO,
            @RequestHeader(value = "X-XSRF-TOKEN", required = false) String csrfTokenHeader
    ) {
        String email = loginDTO.getEmail();
        logger.debug("loginAdmin() called with email: {}", email);
        logger.debug("X-CSRF-TOKEN header: {}", csrfTokenHeader);

        try {
            String token = amministratoreService.loginAmministratore(email, loginDTO.getPassword(), csrfTokenHeader);
            LoginResponse loginResponse = new LoginResponse(token);
            logger.debug("loginAdmin() successful for admin: {}", email);
            return successResponse(loginResponse);
        } catch (AuthenticationException ex) {
            logger.error("loginAdmin() failed, authentication error for admin: {}", email);
            return new ResponseEntity<>(new ApiResponse<>(false, null, "Credenziali non valide"), HttpStatus.UNAUTHORIZED);
        }  catch (Exception ex){
            logger.error("loginAdmin() failed with error: {}", ex.getMessage());
            return handleGenericException(ex, "Login fallito", ENTITY_TYPE);
        }
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AmministratoreDTO>> getAdminDetails() {
        logger.debug("getAdminDetails() called");
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            AmministratoreDTO amministratoreDTO = amministratoreService.getAmministratoreDetails(userEmail);
            logger.debug("getAdminDetails() successful for admin: {}", userEmail);
            return successResponse(amministratoreDTO);

        } catch (Exception ex) {
            logger.error("getAdminDetails() failed with error: {}", ex.getMessage());
            return handleGenericException(ex, "Errore nel recupero dei dettagli dell'amministratore", ENTITY_TYPE);
        }
    }
}