package com.dietiestates25backend.api.controller;

import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25.dto.AnnuncioDTO;
import com.dietiestates25.dto.FiltroAnnunciDTO;
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
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/annunci")
public class AnnuncioController extends BaseController {

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
            List<Annuncio> annunci = annuncioService.findAnnunciByCittaAndTipoAnnuncioAndTipologiaImmobile(
                    citta,
                    filtro.getTipo(),
                    filtro.getTipologia()
            );
            List<AnnuncioDTO> annuncioDTOs = annunci.stream()
                    .map(annuncioService::convertToDTO)
                    .collect(Collectors.toList());
            return successResponse(annuncioDTOs);
        } catch (Exception e) {
            logger.error("Errore durante la ricerca degli annunci: {}", e.getMessage(), e);
            return handleGenericException(e, "Errore durante la ricerca degli annunci", "Annuncio");
        }
    }
}