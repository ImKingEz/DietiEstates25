package com.dietiestates25backend.business.service;

import com.dietiestates25.dto.ImmobileDTO;
import com.dietiestates25backend.api.dto.RegisterImmobileDTO;
import com.dietiestates25backend.business.entity.FotoImmobile;
import com.dietiestates25backend.business.entity.Immobile;
import com.dietiestates25backend.data.repository.FotoImmobileRepository;
import com.dietiestates25backend.data.repository.ImmobileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class ImmobileService {

    private static final Logger logger = LoggerFactory.getLogger(ImmobileService.class);

    @Autowired
    private ImmobileRepository immobileRepository;

    @Autowired
    private FotoImmobileRepository fotoImmobileRepository;

    @Transactional
    public ImmobileDTO saveImmobile(RegisterImmobileDTO registerImmobileDTO) {
        logger.debug("saveImmobile() called with registerImmobileDTO: {}", registerImmobileDTO);

        try {
            logger.debug("Creating Immobile entity...");
            Immobile immobile = new Immobile(
                    registerImmobileDTO.getTitolo(),
                    registerImmobileDTO.getTipologia(),
                    registerImmobileDTO.getIndirizzo(),
                    registerImmobileDTO.getPrezzo(),
                    registerImmobileDTO.getDescrizione(),
                    registerImmobileDTO.getDimensione(),
                    registerImmobileDTO.getNumero_camere(),
                    registerImmobileDTO.getNumero_bagni(),
                    registerImmobileDTO.getClasseEnergetica(),
                    registerImmobileDTO.getPiano(),
                    registerImmobileDTO.isAscensore(),
                    registerImmobileDTO.isPortineria(),
                    registerImmobileDTO.isClimatizzazione(),
                    registerImmobileDTO.getLatitudine(),
                    registerImmobileDTO.getLongitudine(),
                    registerImmobileDTO.isVicinoScuole(),
                    registerImmobileDTO.isVicinoParchi(),
                    registerImmobileDTO.isVicinoTrasportoPubblico(),
                    1L
            );
            logger.debug("Immobile entity created: {}", immobile);

            logger.debug("Saving Immobile entity...");
            Immobile savedImmobile = immobileRepository.save(immobile);
            logger.debug("Immobile entity saved: {}", savedImmobile);

            if (registerImmobileDTO.getImmaginiUrls() != null) {
                logger.debug("Processing images...");
                int numero_immagine = 0;
                for (MultipartFile file : registerImmobileDTO.getImmaginiUrls()) {
                    logger.debug("Processing image: {}", file.getOriginalFilename());
                    String logoPath = saveLogo(file, savedImmobile.getId(), numero_immagine);
                    logger.debug("Logo path: {}", logoPath);

                    FotoImmobile fotoImmobile = new FotoImmobile();
                    fotoImmobile.setUrl(logoPath);
                    fotoImmobile.setIdImmobile(savedImmobile.getId());
                    fotoImmobileRepository.save(fotoImmobile);
                    logger.debug("FotoImmobile saved: {}", fotoImmobile);
                    numero_immagine++;
                }
            } else {
                logger.warn("No images received for immobile ID: {}", savedImmobile.getId());
            }

            ImmobileDTO immobileDTO = convertToDTO(savedImmobile);
            logger.debug("ImmobileDTO converted: {}", immobileDTO);
            return immobileDTO;

        } catch (Exception e) {
            logger.error("Errore durante il salvataggio dell'immobile: ", e);
            throw new RuntimeException("Errore durante il salvataggio dell'immobile: " + e.getMessage(), e);
        } finally {
            logger.debug("saveImmobile() completed");
        }
    }

    private String saveLogo(MultipartFile logo, Long immobileId, int numero_immagine) throws IOException {
        if (logo == null || logo.isEmpty()) {
            logger.warn("Logo is null or empty, returning empty string");
            return "";
        }

        String fileExtension = "";
        String originalFileName = StringUtils.cleanPath(logo.getOriginalFilename());
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
            fileExtension = originalFileName.substring(dotIndex);
        }
        String fileName = "image_" + numero_immagine + "_" + immobileId + fileExtension;
        Path uploadDir = Paths.get("uploads/foto_immobili");

        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                logger.info("Created directory: {}", uploadDir.toAbsolutePath());
            }
            Path targetLocation = uploadDir.resolve(fileName);
            Files.copy(logo.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Saved logo to: {}", targetLocation.toAbsolutePath());
            return "/uploads/foto_immobili/" + fileName;
        } catch (IOException e) {
            logger.error("Could not save logo: ", e);
            throw new IOException("Could not save logo: " + e.getMessage(), e); // Lancia l'eccezione
        }
    }

    private ImmobileDTO convertToDTO(Immobile savedImmobile) {
        ImmobileDTO immobileDTO = new ImmobileDTO(
                savedImmobile.getTitolo(),
                savedImmobile.getTipologia(),
                savedImmobile.getIndirizzo(),
                savedImmobile.getPrezzo(),
                savedImmobile.getDescrizione(),
                savedImmobile.getDimensione(),
                savedImmobile.getNumero_camere(),
                savedImmobile.getNumero_bagni(),
                savedImmobile.getClasseEnergetica(),
                savedImmobile.getPiano(),
                savedImmobile.isAscensore(),
                savedImmobile.isPortineria(),
                savedImmobile.isClimatizzazione(),
                savedImmobile.getLatitudine(),
                savedImmobile.getLongitudine(),
                savedImmobile.isVicinoScuole(),
                savedImmobile.isVicinoParchi(),
                savedImmobile.isVicinoTrasportoPubblico()
        );
        return immobileDTO;
    }
}