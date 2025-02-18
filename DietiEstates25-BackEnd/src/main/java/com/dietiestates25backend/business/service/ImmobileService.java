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

@Service
public class ImmobileService {

    private static final Logger logger = LoggerFactory.getLogger(ImmobileService.class);


    private final ImmobileRepository immobileRepository;


    private final FotoImmobileRepository fotoImmobileRepository;

    @Autowired
    public ImmobileService(ImmobileRepository immobileRepository, FotoImmobileRepository fotoImmobileRepository) {
        this.immobileRepository = immobileRepository;
        this.fotoImmobileRepository = fotoImmobileRepository;
    }

    @Transactional
    public ImmobileDTO saveImmobile(RegisterImmobileDTO registerImmobileDTO) throws IOException {
        logger.debug("saveImmobile() called with registerImmobileDTO: {}", registerImmobileDTO);

        try {
            Immobile immobile = setImmobile(registerImmobileDTO);

            Immobile savedImmobile = immobileRepository.save(immobile);

            if (registerImmobileDTO.getImmaginiUrls() != null) {
                setAndSaveFotoImmobile(registerImmobileDTO, savedImmobile);
            } else {
                logger.warn("No images received for immobile ID: {}", savedImmobile.getId());
            }

            return convertToDTO(savedImmobile);

        } catch (Exception e) {
            throw new IOException("Errore durante il salvataggio dell'immobile: " + e.getMessage(), e);
        } finally {
            logger.debug("saveImmobile() completed");
        }
    }

    private void setAndSaveFotoImmobile(RegisterImmobileDTO registerImmobileDTO, Immobile savedImmobile) throws IOException {
        int numeroImmagine = 0;
        for (MultipartFile file : registerImmobileDTO.getImmaginiUrls()) {
            logger.debug("Processing image: {}", file.getOriginalFilename());
            String imagePath = saveImage(file, savedImmobile.getId(), numeroImmagine);
            logger.debug("image path: {}", imagePath);

            FotoImmobile fotoImmobile = new FotoImmobile();
            fotoImmobile.setUrl(imagePath);
            fotoImmobile.setIdImmobile(savedImmobile.getId());
            fotoImmobileRepository.save(fotoImmobile);
            logger.debug("FotoImmobile saved: {}", fotoImmobile);
            numeroImmagine++;
        }
    }

    private static Immobile setImmobile(RegisterImmobileDTO registerImmobileDTO) {
        return new Immobile(
                registerImmobileDTO.getTitolo(),
                registerImmobileDTO.getTipologia(),
                registerImmobileDTO.getIndirizzo(),
                registerImmobileDTO.getPrezzo(),
                registerImmobileDTO.getDescrizione(),
                registerImmobileDTO.getDimensione(),
                registerImmobileDTO.getNumeroCamere(),
                registerImmobileDTO.getNumeroBagni(),
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
    }

    private String saveImage(MultipartFile image, Long immobileId, int numeroImmagine) throws IOException {
        if (image == null || image.isEmpty()) {
            logger.warn("image is null or empty, returning empty string");
            return "";
        }

        String fileExtension = "";
        String originalFileName = StringUtils.cleanPath(image.getOriginalFilename());
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
            fileExtension = originalFileName.substring(dotIndex);
        }
        String fileName = "image_" + numeroImmagine + "_" + immobileId + fileExtension;
        Path uploadDir = Paths.get("uploads/foto_immobili");

        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                logger.info("Created directory: {}", uploadDir.toAbsolutePath());
            }
            Path targetLocation = uploadDir.resolve(fileName);
            Files.copy(image.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Saved image to: {}", targetLocation.toAbsolutePath());
            return "/uploads/foto_immobili/" + fileName;
        } catch (IOException e) {
            throw new IOException("Could not save image: " + e.getMessage(), e); // Lancia l'eccezione
        }
    }

    private ImmobileDTO convertToDTO(Immobile savedImmobile) {
        return new ImmobileDTO(
                savedImmobile.getTitolo(),
                savedImmobile.getTipologia(),
                savedImmobile.getIndirizzo(),
                savedImmobile.getPrezzo(),
                savedImmobile.getDescrizione(),
                savedImmobile.getDimensione(),
                savedImmobile.getNumeroCamere(),
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
                savedImmobile.isVicinoTrasportoPubblico()
        );
    }
}