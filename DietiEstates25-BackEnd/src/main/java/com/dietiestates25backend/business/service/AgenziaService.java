package com.dietiestates25backend.business.service;

import com.dietiestates25.dto.AgenziaDTO;
import com.dietiestates25backend.api.dto.RegisterAgenziaDTO;
import com.dietiestates25backend.business.entity.AgenziaImmobiliare;
import com.dietiestates25backend.business.entity.Amministratore;
import com.dietiestates25backend.data.repository.AgenziaRepository;
import com.dietiestates25backend.data.repository.AmministratoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class AgenziaService {

    private static final Logger logger = LoggerFactory.getLogger(AgenziaService.class);

    private final AgenziaRepository agenziaRepository;
    private final AmministratoreRepository amministratoreRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AgenziaService(AgenziaRepository agenziaRepository, AmministratoreRepository amministratratoreRepository, PasswordEncoder passwordEncoder) {
        this.agenziaRepository = agenziaRepository;
        this.amministratoreRepository = amministratratoreRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AgenziaDTO registraAgenzia(RegisterAgenziaDTO registerAgenziaDTO) {
        logger.debug("Starting registraAgenzia with email: {}", registerAgenziaDTO.getEmail());

        if (agenziaRepository.existsByEmail(registerAgenziaDTO.getEmail())) {
            logger.error("Email già registrata: {}", registerAgenziaDTO.getEmail());
            throw new DataIntegrityViolationException("Email già in uso");
        }

        String logoPath = saveLogo(registerAgenziaDTO.getLogo());

        AgenziaImmobiliare agenzia = new AgenziaImmobiliare(
                registerAgenziaDTO.getNome(),
                registerAgenziaDTO.getPartitaIva(),
                registerAgenziaDTO.getIndirizzo(),
                registerAgenziaDTO.getEmail(),
                registerAgenziaDTO.getTelefono(),
                logoPath
        );

        AgenziaImmobiliare savedAgenzia = agenziaRepository.save(agenzia);
        logger.debug("Agenzia salvata con ID: {}", savedAgenzia.getId());

        // Crea e salva l'amministratore
        Amministratore amministratore = new Amministratore();
        amministratore.setEmail(registerAgenziaDTO.getEmail());
        amministratore.setPassword(passwordEncoder.encode(registerAgenziaDTO.getPassword())); // Hash della password
        amministratoreRepository.save(amministratore);
        logger.debug("Amministratore salvato con email: {}", amministratore.getEmail());

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

    private String saveLogo(MultipartFile logo) {
        if (logo == null || logo.isEmpty()) {
            return null; // Or a default logo path
        }

        String fileName = StringUtils.cleanPath(logo.getOriginalFilename());
        Path uploadDir = Paths.get("uploads/logos"); // Creare la cartella uploads/logos
        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            Path targetLocation = uploadDir.resolve(fileName);
            Files.copy(logo.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/logos/" + fileName; // Path relativo per accedere al logo
        } catch (IOException e) {
            logger.error("Could not save logo: ", e);
            return null; // Handle the error appropriately
        }
    }
}