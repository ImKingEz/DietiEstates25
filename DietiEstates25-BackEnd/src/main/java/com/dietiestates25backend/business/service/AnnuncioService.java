package com.dietiestates25backend.business.service;

import com.dietiestates25.dto.AnnuncioDTO;
import com.dietiestates25backend.api.dto.RegisterAnnuncioDTO;
import com.dietiestates25backend.business.entity.FotoImmobile;
import com.dietiestates25backend.business.entity.Annuncio;
import com.dietiestates25backend.data.repository.FotoImmobileRepository;
import com.dietiestates25backend.data.repository.AnnuncioRepository;
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
import java.util.stream.Collectors;

@Service
public class AnnuncioService {
    private static final Logger logger = LoggerFactory.getLogger(AnnuncioService.class);

    private final AnnuncioRepository annuncioRepository;

    private final FotoImmobileRepository fotoImmobileRepository;

    @Autowired
    public AnnuncioService(AnnuncioRepository annuncioRepository, FotoImmobileRepository fotoImmobileRepository) {
        this.annuncioRepository = annuncioRepository;
        this.fotoImmobileRepository = fotoImmobileRepository;
    }

    @Transactional
    public AnnuncioDTO saveAnnuncio(RegisterAnnuncioDTO registerAnnuncioDTO) throws IOException {
        try {
            Annuncio annuncio = setAnnuncio(registerAnnuncioDTO);

            Annuncio savedAnnuncio = annuncioRepository.save(annuncio);

            logger.info("idimmobile in annuncio dopo la save: {}", savedAnnuncio.getIdImmobile());

            if (registerAnnuncioDTO.getImmaginiUrls() != null) {
                setAndSaveFotoImmobile(registerAnnuncioDTO, savedAnnuncio);
            } else {
                logger.warn("No images received for annuncio ID: {}", savedAnnuncio.getId());
            }

            return convertToDTO(savedAnnuncio);

        } catch (Exception e) {
            throw new IOException("Errore durante il salvataggio dell'annuncio: " + e.getMessage(), e);
        } finally {
            logger.debug("saveAnnuncio() completed");
        }
    }

    private void setAndSaveFotoImmobile(RegisterAnnuncioDTO registerAnnuncioDTO, Annuncio savedAnnuncio) throws IOException {
        int numeroImmagine = 0;
        for (MultipartFile file : registerAnnuncioDTO.getImmaginiUrls()) {
            logger.debug("Processing image: {}", file.getOriginalFilename());
            String imagePath = saveImage(file, savedAnnuncio.getId(), numeroImmagine);
            logger.debug("image path: {}", imagePath);

            FotoImmobile fotoImmobile = new FotoImmobile();
            fotoImmobile.setUrl(imagePath);
            fotoImmobile.setIdAnnuncio(savedAnnuncio.getId());
            fotoImmobileRepository.save(fotoImmobile);
            logger.debug("FotoImmobile saved: {}", fotoImmobile);
            numeroImmagine++;
        }
    }

    private static Annuncio setAnnuncio(RegisterAnnuncioDTO registerAnnuncioDTO) {
        return new Annuncio(
                registerAnnuncioDTO.getTitolo(),
                registerAnnuncioDTO.getTipo(),
                registerAnnuncioDTO.getPrezzo(),
                registerAnnuncioDTO.getDescrizione(),
                registerAnnuncioDTO.getIdAgente(),
                registerAnnuncioDTO.getIdImmobile()
        );
    }

    private String saveImage(MultipartFile image, Long annuncioId, int numeroImmagine) throws IOException {
        if (image == null || image.isEmpty()) {
            logger.warn("image is null or empty, returning empty string");
            return "";
        }
        String originalFileName = null;
        String imageFileName = image.getOriginalFilename();
        if (imageFileName != null) {
            originalFileName = StringUtils.cleanPath(imageFileName);
        } else {
            return "";
        }
        String fileExtension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
            fileExtension = originalFileName.substring(dotIndex);
        }
        String fileName = "image_" + numeroImmagine + "_" + annuncioId + fileExtension;
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

    public AnnuncioDTO convertToDTO(Annuncio savedAnnuncio) {
        List<FotoImmobile> fotoImmobili = fotoImmobileRepository.findByIdAnnuncio(savedAnnuncio.getId());
        List<String> immaginiUrls = fotoImmobili.stream()
                .map(FotoImmobile::getUrl)
                .collect(Collectors.toList());

        return new AnnuncioDTO(
                savedAnnuncio.getIdImmobile(),
                savedAnnuncio.getIdAgente(),
                savedAnnuncio.getTitolo(),
                savedAnnuncio.getTipo(),
                savedAnnuncio.getPrezzo(),
                savedAnnuncio.getDescrizione(),
                immaginiUrls
        );
    }

    public List<Annuncio> findAnnunciByCittaAndTipoAnnuncioAndTipologiaImmobile(String citta, String tipoAnnuncio, String tipologiaImmobile) {
        logger.debug("Ricerca annunci per cittÃ : {}, tipo: {}, tipologia: {}", citta, tipoAnnuncio, tipologiaImmobile);
        return annuncioRepository.findByCittaAndTipoAnnuncioAndTipologiaImmobile(citta, tipoAnnuncio, tipologiaImmobile);
    }
}