package com.dietiestates25backend.business.service;

import com.dietiestates25.dto.AgenziaDTO;
import com.dietiestates25backend.api.dto.RegisterAgenziaDTO;
import com.dietiestates25backend.business.entity.AgenziaImmobiliare;
import com.dietiestates25backend.data.repository.AgenziaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgenziaService {

    private static final Logger logger = LoggerFactory.getLogger(AgenziaService.class);

    private final AgenziaRepository agenziaRepository;

    @Autowired
    public AgenziaService(AgenziaRepository agenziaRepository) {
        this.agenziaRepository = agenziaRepository;
    }

    @Transactional
    public AgenziaDTO registraAgenzia(RegisterAgenziaDTO registerAgenziaDTO) {
        logger.debug("Starting registraAgenzia with email: {}", registerAgenziaDTO.getEmail());

        if (agenziaRepository.existsByEmail(registerAgenziaDTO.getEmail())) {
            logger.error("Email già registrata: {}", registerAgenziaDTO.getEmail());
            throw new DataIntegrityViolationException("Email già in uso");
        }

        AgenziaImmobiliare agenzia = new AgenziaImmobiliare(
                registerAgenziaDTO.getNome(),
                registerAgenziaDTO.getPartitaIva(),
                registerAgenziaDTO.getIndirizzo(),
                registerAgenziaDTO.getEmail(),
                registerAgenziaDTO.getTelefono(),
                registerAgenziaDTO.getLogo()
        );

        AgenziaImmobiliare savedAgenzia = agenziaRepository.save(agenzia);
        logger.debug("Agenzia salvata con ID: {}", savedAgenzia.getId());

        AgenziaDTO agenziaDTO = new AgenziaDTO(
                savedAgenzia.getNome(),
                savedAgenzia.getPartitaIva(),
                savedAgenzia.getIndirizzo(),
                savedAgenzia.getEmail(),
                savedAgenzia.getTelefono(),
                savedAgenzia.getLogo()
        );

        logger.debug("Ending registraAgenzia with agenzia: {}", agenziaDTO.getEmail());
        return agenziaDTO;
    }
}