package com.dietiestates25backend.api.controller;

import com.dietiestates25.dto.*;
import com.dietiestates25backend.api.dto.RegisterAnnuncioDTO;
import com.dietiestates25backend.business.entity.Annuncio;
import com.dietiestates25backend.business.service.AnnuncioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/annunci")
public class AnnuncioController extends BaseController {

    public static final String ENTITY_TYPE = "Annuncio";
    private final AnnuncioService annuncioService;

    @Autowired
    public AnnuncioController(AnnuncioService annuncioService) {
        this.annuncioService = annuncioService;
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_AGENTE')")
    public ResponseEntity<ApiResponse<AnnuncioDTO>> createAnnuncio(
            @ModelAttribute @Valid RegisterAnnuncioDTO registerAnnuncioDTO) {

        try {
            AnnuncioDTO annuncioDTO = annuncioService.saveAnnuncio(registerAnnuncioDTO);
            return successResponse(annuncioDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Errore durante la creazione dell'annuncio: {}", e.getMessage(), e);
            ApiResponse<AnnuncioDTO> response = new ApiResponse<>(false, null, "Errore durante la creazione dell'annuncio: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/search")
    @PreAuthorize("hasRole('ROLE_UTENTE') or hasRole('ROLE_AGENTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<AnnuncioDTO>>> searchAnnunci(
            @RequestParam String citta,
            @RequestBody FiltroAnnunciDTO filtro) {
        try {
            List<Annuncio> annunci = annuncioService.findAnnunciByCittaAndFiltri(filtro, citta);
            List<AnnuncioDTO> annuncioDTOs = annunci.stream()
                    .map(annuncioService::convertToDTO)
                    .toList();
            return successResponse(annuncioDTOs);
        } catch (Exception e) {
            logger.error("Errore durante la ricerca degli annunci: {}", e.getMessage(), e);
            return handleGenericException(e, "Errore durante la ricerca degli annunci", ENTITY_TYPE);
        }
    }

    @GetMapping("/immobile/{id}")
    @PreAuthorize("hasRole('ROLE_UTENTE') or hasRole('ROLE_AGENTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AnnuncioDTO>> getAnnuncioByImmobile(@PathVariable Long id) {
        try {
            AnnuncioDTO annuncioDTO = annuncioService.getAnnuncioByIdImmobile(id);
            ApiResponse<AnnuncioDTO> response = new ApiResponse<>(true, annuncioDTO, null);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            logger.warn("annuncio non trovata con ID immobile: {}", id);
            ApiResponse<AnnuncioDTO> response = new ApiResponse<>(false, null, "annuncio non trovata");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception ex) {
            logger.error("Errore durante il recupero dei dettagli dell'annuncio con ID immobile: {}", id, ex);
            ApiResponse<AnnuncioDTO> response = new ApiResponse<>(false, null, "Errore durante il recupero dei dettagli dell'annuncio: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/search/radius")
    @PreAuthorize("hasRole('ROLE_UTENTE') or hasRole('ROLE_AGENTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<AnnuncioDTO>>> searchAnnunciInRadius(
            @RequestBody AnnunciRadiusSearchDTO searchDTO) {
        try {
            List<AnnuncioDTO> annuncioDTOs = annuncioService.findAnnunciInRadius(
                    searchDTO.getMap(), searchDTO.getFiltro());
            return successResponse(annuncioDTOs);
        } catch (Exception e) {
            logger.error("Errore durante la ricerca degli annunci in radius: {}", e.getMessage(), e);
            return handleGenericException(e, "Errore durante la ricerca degli annunci in radius", ENTITY_TYPE);
        }
    }

    @PutMapping("/updateStats")
    @PreAuthorize("hasRole('ROLE_UTENTE') or hasRole('ROLE_AGENTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AnnuncioDTO>> updateAnnuncioStats(@RequestBody UpdateAnnuncioDTO updateAnnuncioDTO) {
        try {
            Annuncio annuncioAggiornato = annuncioService.updateAnnuncioStats(updateAnnuncioDTO);
            AnnuncioDTO annuncioDTO = annuncioService.convertToDTO(annuncioAggiornato);
            return successResponse(annuncioDTO);
        } catch (Exception e) {
            logger.error("Errore durante l'aggiornamento delle statistiche dell'annuncio: {}", e.getMessage(), e);
            return handleGenericException(e, "Errore durante l'aggiornamento delle statistiche dell'annuncio", ENTITY_TYPE);
        }
    }

    @PostMapping("/search/agente/{idAgente}")
    @PreAuthorize("hasRole('ROLE_AGENTE')")
    public ResponseEntity<ApiResponse<List<AnnuncioDTO>>> searchAnnunciAgente(@PathVariable Long idAgente) {
        try {
            List<AnnuncioDTO> annuncioDTOs = annuncioService.getAnnunciAgente(idAgente);
            return successResponse(annuncioDTOs);
        } catch (Exception e) {
            logger.error("Errore durante il recupero degli annunci: {}", e.getMessage(), e);
            return handleGenericException(e, "Errore durante il recupero degli annunci", ENTITY_TYPE);
        }
    }
}