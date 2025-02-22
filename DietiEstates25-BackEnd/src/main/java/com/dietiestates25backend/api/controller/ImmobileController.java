package com.dietiestates25backend.api.controller;

import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25.dto.ImmobileDTO;
import com.dietiestates25backend.business.entity.Immobile;
import com.dietiestates25backend.business.service.ImmobileService;
import com.dietiestates25backend.business.service.AuthService;
import com.dietiestates25backend.business.service.JwtService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/immobili")
public class ImmobileController extends BaseController {

    public static final String ENTITY_TYPE = "Immobile";

    private final ImmobileService immobileService;

    @Autowired
    public ImmobileController(ImmobileService immobileService, AuthService authService, JwtService jwtService) {
        this.immobileService = immobileService;
    }

    @PostMapping(value = "/create")
    @PreAuthorize("hasRole('ROLE_AGENTE')")
    public ResponseEntity<ApiResponse<ImmobileDTO>> registerImmobile(@RequestBody @Valid ImmobileDTO immobileDTO) {
        try {
            return successResponse(immobileService.saveImmobile(immobileDTO), HttpStatus.CREATED);
        } catch (Exception ex) {
            logger.error("salvaImmobile() failed with error: {}", ex.getMessage());
            return handleGenericException(ex, "Errore durante la registrazione", ENTITY_TYPE);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_UTENTE') or hasRole('ROLE_AGENTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ImmobileDTO>> getImmobileDetails(@PathVariable Long id) {
        try {
            ImmobileDTO immobileDTO = immobileService.getImmobileDetails(id);
            ApiResponse<ImmobileDTO> response = new ApiResponse<>(true, immobileDTO, null);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            logger.warn("Immobile non trovato con ID: {}", id);
            ApiResponse<ImmobileDTO> response = new ApiResponse<>(false, null, "Immobile non trovato");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception ex) {
            logger.error("Errore durante il recupero dei dettagli dell'immobile con ID: {}", id, ex);
            ApiResponse<ImmobileDTO> response = new ApiResponse<>(false, null, "Errore durante il recupero dei dettagli dell'immobile: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}