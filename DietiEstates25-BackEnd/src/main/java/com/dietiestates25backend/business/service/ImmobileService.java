package com.dietiestates25backend.business.service;

import com.dietiestates25.dto.ImmobileDTO;
import com.dietiestates25backend.business.entity.Immobile;
import com.dietiestates25backend.data.repository.ImmobileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ImmobileService {

    private static final Logger logger = LoggerFactory.getLogger(ImmobileService.class);


    private final ImmobileRepository immobileRepository;

    @Autowired
    public ImmobileService(ImmobileRepository immobileRepository) {
        this.immobileRepository = immobileRepository;
    }

    @Transactional
    public ImmobileDTO saveImmobile(ImmobileDTO immobileDTO) throws IOException {
        try {
            Immobile immobile = setImmobile(immobileDTO);

            Immobile savedImmobile = immobileRepository.save(immobile);

            logger.debug("Immobile salvato con id: {}", savedImmobile.getId());

            return convertToDTO(savedImmobile);

        } catch (Exception e) {
            throw new IOException("Errore durante il salvataggio dell'immobile: " + e.getMessage(), e);
        } finally {
            logger.debug("saveImmobile() completed");
        }
    }

    private static Immobile setImmobile(ImmobileDTO immobileDTO) {
        return new Immobile(
                immobileDTO.getTipologia(),
                immobileDTO.getIndirizzo(),
                immobileDTO.getDimensione(),
                immobileDTO.getNumeroLocali(),
                immobileDTO.getNumeroBagni(),
                immobileDTO.getClasseEnergetica(),
                immobileDTO.getPiano(),
                immobileDTO.isAscensore(),
                immobileDTO.isPortineria(),
                immobileDTO.isClimatizzazione(),
                immobileDTO.getLatitudine(),
                immobileDTO.getLongitudine(),
                immobileDTO.isVicinoScuole(),
                immobileDTO.isVicinoParchi(),
                immobileDTO.isVicinoTrasportoPubblico(),
                immobileDTO.getCitta()
        );
    }

    public ImmobileDTO convertToDTO(Immobile savedImmobile) {
        return new ImmobileDTO(
                savedImmobile.getId(),
                savedImmobile.getTipologia(),
                savedImmobile.getIndirizzo(),
                savedImmobile.getDimensione(),
                savedImmobile.getNumeroLocali(),
                savedImmobile.getNumeroBagni(),
                savedImmobile.getClasseEnergetica(),
                savedImmobile.getPiano(),
                savedImmobile.isAscensore(),
                savedImmobile.isPortineria(),
                savedImmobile.isClimatizzazione(),
                savedImmobile.getLatitudine(),
                savedImmobile.getLongitudine(),
                savedImmobile.isVicinoScuole(),
                savedImmobile.isVicinoParchi(),
                savedImmobile.isVicinoTrasportoPubblico(),
                savedImmobile.getCitta()
        );
    }

    public List<Immobile> findImmobiliByCitta(String citta) {
        logger.debug("Ricerca immobili per città  : {}", citta);
        return immobileRepository.findByCittaIgnoreCase(citta);
    }

    public ImmobileDTO getImmobileDetails(Long id) {
        Optional<Immobile> immobileOptional = immobileRepository.findById(id);

        if (immobileOptional.isEmpty()) {
            logger.warn("Immobile non trovato con ID: {}", id);
            throw new IllegalArgumentException("Immobile non trovato con ID: " + id);
        }

        Immobile immobile = immobileOptional.get();

        return convertToDTO(immobile);
    }
}