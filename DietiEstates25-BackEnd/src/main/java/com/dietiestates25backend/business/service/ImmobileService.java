package com.dietiestates25backend.business.service;

import com.dietiestates25.dto.ApiResponse;
import com.dietiestates25.dto.ImmobileDTO;
import com.dietiestates25backend.business.entity.FotoImmobile;
import com.dietiestates25backend.business.entity.Immobile;
import com.dietiestates25backend.business.entity.Utente;
import com.dietiestates25backend.data.repository.FotoImmobileRepository;
import com.dietiestates25backend.data.repository.ImmobileRepository;
import com.dietiestates25backend.data.repository.UtenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ImmobileService {

    private static final Logger logger = LoggerFactory.getLogger(ImmobileService.class);

    @Autowired
    private ImmobileRepository immobileRepository;

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private FotoImmobileRepository fotoImmobileRepository; // Aggiungi l'autowired per il repository FotoImmobile

    @Transactional // Aggiungi l'annotazione @Transactional
    public ResponseEntity<ApiResponse<Immobile>> saveImmobile(ImmobileDTO immobileDTO, String email) {
        Immobile immobile = new Immobile();
        immobile.setTitolo(immobileDTO.getTitolo());
        immobile.setTipo(immobileDTO.getTipologia());
        immobile.setIndirizzo(immobileDTO.getIndirizzo());
        immobile.setPrezzo(immobileDTO.getPrezzo());
        immobile.setDescrizione(immobileDTO.getDescrizione());
        immobile.setDimensione(immobileDTO.getDimensione());
        immobile.setNumero_stanze(immobileDTO.getNumero_camere());
        immobile.setNumero_bagni(immobileDTO.getNumero_bagni());
        immobile.setClasseEnergetica(immobileDTO.getClasseEnergetica());
        immobile.setPiano(immobileDTO.getPiano());
        immobile.setAscensore(immobileDTO.isAscensore());
        immobile.setPortineria(immobileDTO.isPortineria());
        immobile.setClimatizzazione(immobileDTO.isClimatizzazione());
        immobile.setLatitudine(immobileDTO.getLatitudine());
        immobile.setLongitudine(immobileDTO.getLongitudine());
        immobile.setVicinoScuole(immobileDTO.isVicinoScuole());
        immobile.setVicinoParchi(immobileDTO.isVicinoParchi());
        immobile.setVicinoTrasportoPubblico(immobileDTO.isVicinoTrasportoPubblico());
        immobile.setIdAgente(1L);

        logger.debug("Dati dell'immobile prima del salvataggio: {}", immobile); // Aggiungi questo log

        Immobile savedImmobile = immobileRepository.save(immobile);

        // Crea le entità FotoImmobile dalla lista di URL
        List<FotoImmobile> fotoImmobili = immobileDTO.getImmaginiUrls().stream()
                .map(url -> {
                    FotoImmobile fotoImmobile = new FotoImmobile();
                    fotoImmobile.setUrl(url);
                    fotoImmobile.setIdImmobile(savedImmobile.getId()); // Imposta l'ID dell'immobile
                    return fotoImmobile;
                })
                .collect(Collectors.toList());

        // Salva le entità FotoImmobile nel database
        fotoImmobileRepository.saveAll(fotoImmobili); // Salva le foto

        logger.info("Immobile salvato con successo con ID: {}", savedImmobile.getId());

        ApiResponse<Immobile> response = new ApiResponse<>(true, savedImmobile, null);
        return new ResponseEntity<>(response, HttpStatus.CREATED); //aggiungi il reponse entity e l'api response
    }

    // Metodo di utilità per convertire la stringa in Enum
    private String convertTipologiaToEnum(String tipologia) {
        if ("Vendita".equalsIgnoreCase(tipologia)) {
            return "vendita";
        } else if ("Affitto".equalsIgnoreCase(tipologia)) {
            return "affitto";
        } else {
            // Gestisci il caso in cui la tipologia non è valida
            throw new IllegalArgumentException("Tipologia non valida: " + tipologia);
        }
    }
}