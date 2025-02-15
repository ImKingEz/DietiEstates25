package com.dietiestates25backend.business.service;

import com.dietiestates25.dto.AgenziaDTO;
import com.dietiestates25backend.api.dto.RegisterAgenziaDTO;
import com.dietiestates25backend.business.entity.AgenziaImmobiliare;
import com.dietiestates25backend.business.entity.Amministratore;
import com.dietiestates25backend.data.repository.AgenziaRepository;
import com.dietiestates25backend.data.repository.AmministratoreRepository;
import com.dietiestates25backend.data.repository.UtenteRepository; //Import
import com.dietiestates25backend.data.repository.AgenteRepository; //Import
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
import java.util.Optional;

@Service
public class AgenziaService {

    private static final Logger logger = LoggerFactory.getLogger(AgenziaService.class);

    private final AgenziaRepository agenziaRepository;
    private final AmministratoreRepository amministratoreRepository;
    private final PasswordEncoder passwordEncoder;
    private final UtenteRepository utenteRepository; //Inject
    private final AgenteRepository agenteRepository; //Inject

    @Autowired
    public AgenziaService(AgenziaRepository agenziaRepository, AmministratoreRepository amministratoreRepository, PasswordEncoder passwordEncoder, UtenteRepository utenteRepository, AgenteRepository agenteRepository) {
        this.agenziaRepository = agenziaRepository;
        this.amministratoreRepository = amministratoreRepository;
        this.passwordEncoder = passwordEncoder;
        this.utenteRepository = utenteRepository;
        this.agenteRepository = agenteRepository;
    }

    @Transactional
    public AgenziaDTO registraAgenzia(RegisterAgenziaDTO registerAgenziaDTO) {
        logger.debug("Starting registraAgenzia with email: {}", registerAgenziaDTO.getEmail());

        if (agenziaRepository.existsByEmail(registerAgenziaDTO.getEmail())) {
            logger.error("Email già registrata nella tabella agenzie: {}", registerAgenziaDTO.getEmail());
            throw new DataIntegrityViolationException("Email già in uso nella tabella delle agenzie");
        }
        if (utenteRepository.existsByEmail(registerAgenziaDTO.getEmail())) {
            logger.error("Email già registrata nella tabella utenti: {}", registerAgenziaDTO.getEmail());
            throw new DataIntegrityViolationException("Email già in uso nella tabella degli utenti");
        }
        if (agenteRepository.existsByEmail(registerAgenziaDTO.getEmail())) {
            logger.error("Email già registrata nella tabella agenti: {}", registerAgenziaDTO.getEmail());
            throw new DataIntegrityViolationException("Email già in uso nella tabella degli agenti");
        }
        if (amministratoreRepository.existsByEmail(registerAgenziaDTO.getEmail())) {
            logger.error("Email già registrata nella tabella amministratori: {}", registerAgenziaDTO.getEmail());
            throw new DataIntegrityViolationException("Email già in uso nella tabella degli amministratori");
        }

        String tempLogoPath = "temporary_logo";

        AgenziaImmobiliare agenzia = new AgenziaImmobiliare(
                registerAgenziaDTO.getNome(),
                registerAgenziaDTO.getPartitaIva(),
                registerAgenziaDTO.getIndirizzo(),
                registerAgenziaDTO.getEmail(),
                registerAgenziaDTO.getTelefono(),
                tempLogoPath
        );

        AgenziaImmobiliare savedAgenzia = agenziaRepository.save(agenzia);
        logger.debug("Agenzia salvata con ID: {}", savedAgenzia.getId());

        String logoPath = saveLogo(registerAgenziaDTO.getLogo(), savedAgenzia.getId());

        savedAgenzia.setLogo(logoPath);
        agenziaRepository.save(savedAgenzia);

        Amministratore amministratore = new Amministratore();
        amministratore.setEmail(registerAgenziaDTO.getEmail());
        amministratore.setPassword(passwordEncoder.encode(registerAgenziaDTO.getPassword()));
        amministratore.setIdAgenzia(savedAgenzia.getId());
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

    private String saveLogo(MultipartFile logo, Long agenziaId) {
        if (logo == null || logo.isEmpty()) {
            return "";
        }

        String fileExtension = "";
        String originalFileName = StringUtils.cleanPath(logo.getOriginalFilename());
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
            fileExtension = originalFileName.substring(dotIndex);
        }
        String fileName = "logo_" + agenziaId + fileExtension;
        Path uploadDir = Paths.get("uploads/logos");

        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            Path targetLocation = uploadDir.resolve(fileName);
            Files.copy(logo.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/logos/" + fileName;
        } catch (IOException e) {
            logger.error("Could not save logo: ", e);
            return "";
        }
    }

    public AgenziaDTO getAgenziaDetails(Long agenziaId) {
        Optional<AgenziaImmobiliare> agenziaOptional = agenziaRepository.findById(agenziaId);

        if (agenziaOptional.isEmpty()) {
            logger.warn("Agenzia non trovata con ID: {}", agenziaId);
            throw new IllegalArgumentException("Agenzia non trovata con ID: " + agenziaId); // Oppure una custom exception
        }

        AgenziaImmobiliare agenzia = agenziaOptional.get();

        return new AgenziaDTO(
                agenzia.getNome(),
                agenzia.getPartitaIva(),
                agenzia.getIndirizzo(),
                agenzia.getEmail(),
                agenzia.getTelefono(),
                agenzia.getLogo()
        );
    }
}