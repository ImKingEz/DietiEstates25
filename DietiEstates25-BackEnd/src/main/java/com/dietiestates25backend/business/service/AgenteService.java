package com.dietiestates25backend.business.service;

import com.dietiestates25.dto.AgenteDTO;
import com.dietiestates25backend.api.dto.RegisterAgenteDTO;
import com.dietiestates25backend.business.entity.AgenteImmobiliare;
import com.dietiestates25backend.data.repository.AgenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AgenteService {

    private static final Logger logger = LoggerFactory.getLogger(AgenteService.class);

    private final AgenteRepository agenteRepository;

    @Autowired
    public AgenteService(AgenteRepository agenteRepository) {
        this.agenteRepository = agenteRepository;
    }

    public AgenteDTO registraAgente(RegisterAgenteDTO registerAgenteDTO) {
        logger.debug("Starting registraAgente with email: {}", registerAgenteDTO.getEmail());

        if (agenteRepository.existsByEmail(registerAgenteDTO.getEmail())) {
            logger.error("Email già registrata: {}", registerAgenteDTO.getEmail());
            throw new DataIntegrityViolationException("Email già in uso");
        }

        AgenteImmobiliare agente = new AgenteImmobiliare();
        agente.setIdAgenzia(registerAgenteDTO.getIdAgenzia());
        agente.setNome(registerAgenteDTO.getNome());
        agente.setCognome(registerAgenteDTO.getCognome());
        agente.setDataDiNascita(registerAgenteDTO.getDataDiNascita());
        agente.setSesso(registerAgenteDTO.getSesso()); // Assegna direttamente la stringa
        agente.setEmail(registerAgenteDTO.getEmail());
        agente.setPassword(registerAgenteDTO.getPassword()); //TODO: DA HASHAARE

        AgenteImmobiliare savedAgente = agenteRepository.save(agente);
        logger.debug("Agente salvato con ID: {}", savedAgente.getId());

        AgenteDTO agenteDTO = new AgenteDTO();
        agenteDTO.setIdAgenzia(savedAgente.getIdAgenzia());
        agenteDTO.setNome(savedAgente.getNome());
        agenteDTO.setCognome(savedAgente.getCognome());
        agenteDTO.setDataDiNascita(savedAgente.getDataDiNascita());
        agenteDTO.setSesso(savedAgente.getSesso()); // Assegna direttamente la stringa
        agenteDTO.setEmail(savedAgente.getEmail());

        logger.debug("Ending registraAgente with agente: {}", agenteDTO.getEmail());
        return agenteDTO;
    }

    public AgenteDTO updateAgente(RegisterAgenteDTO registerAgenteDTO) {
        logger.debug("Starting updateAgente with email: {}", registerAgenteDTO.getEmail());

        if (agenteRepository.existsByEmail(registerAgenteDTO.getEmail())) {
            logger.error("Email già registrata: {}", registerAgenteDTO.getEmail());
            throw new DataIntegrityViolationException("Email già in uso");
        }

        AgenteImmobiliare agente = new AgenteImmobiliare();
        agente.setIdAgenzia(registerAgenteDTO.getIdAgenzia());
        agente.setNome(registerAgenteDTO.getNome());
        agente.setCognome(registerAgenteDTO.getCognome());
        agente.setDataDiNascita(registerAgenteDTO.getDataDiNascita());
        agente.setSesso(registerAgenteDTO.getSesso()); // Assegna direttamente la stringa
        agente.setEmail(registerAgenteDTO.getEmail());
        agente.setPassword(registerAgenteDTO.getPassword()); //TODO: DA HASHAARE

        AgenteImmobiliare savedAgente = agenteRepository.save(agente);
        logger.debug("Agente salvato con ID: {}", savedAgente.getId());

        AgenteDTO agenteDTO = new AgenteDTO();
        agenteDTO.setIdAgenzia(savedAgente.getIdAgenzia());
        agenteDTO.setNome(savedAgente.getNome());
        agenteDTO.setCognome(savedAgente.getCognome());
        agenteDTO.setDataDiNascita(savedAgente.getDataDiNascita());
        agenteDTO.setSesso(savedAgente.getSesso()); // Assegna direttamente la stringa
        agenteDTO.setEmail(savedAgente.getEmail());

        logger.debug("Ending registraAgente with agente: {}", agenteDTO.getEmail());
        return agenteDTO;
    }

    public AgenteDTO getAgenteDetails(Long id) {
        Optional<AgenteImmobiliare> agenteOptional = agenteRepository.findById(id);

        if (agenteOptional.isEmpty()) {
            logger.warn("Agente non trovato con ID: {}", id);
            throw new IllegalArgumentException("Agente non trovato con ID: " + id); // Oppure una custom exception
        }

        AgenteImmobiliare agente = agenteOptional.get();

        AgenteDTO agenteDTO = new AgenteDTO();
        agenteDTO.setIdAgenzia(agente.getIdAgenzia());
        agenteDTO.setNome(agente.getNome());
        agenteDTO.setCognome(agente.getCognome());
        agenteDTO.setDataDiNascita(agente.getDataDiNascita());
        agenteDTO.setSesso(agente.getSesso()); // Assegna direttamente la stringa
        agenteDTO.setEmail(agente.getEmail());

        return agenteDTO;
    }
}