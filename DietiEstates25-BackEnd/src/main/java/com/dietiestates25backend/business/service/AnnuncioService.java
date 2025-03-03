package com.dietiestates25backend.business.service;

import com.dietiestates25.dto.AnnuncioDTO;
import com.dietiestates25.dto.FiltroAnnunciDTO;
import com.dietiestates25.dto.MapSearchDTO;
import com.dietiestates25.dto.UpdateAnnuncioDTO;
import com.dietiestates25backend.api.dto.RegisterAnnuncioDTO;
import com.dietiestates25backend.business.entity.FotoImmobile;
import com.dietiestates25backend.business.entity.Annuncio;
import com.dietiestates25backend.data.repository.FotoImmobileRepository;
import com.dietiestates25backend.data.repository.AnnuncioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Optional;

@Service
public class AnnuncioService {
    private static final Logger logger = LoggerFactory.getLogger(AnnuncioService.class);

    private final AnnuncioRepository annuncioRepository;

    private final FotoImmobileRepository fotoImmobileRepository;

    @Value("${file.upload.directory}")
    private String uploadDirectory;

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
        Path uploadDir = Paths.get(uploadDirectory);

        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                logger.info("Created directory: {}", uploadDir.toAbsolutePath());
            }
            Path targetLocation = uploadDir.resolve(fileName);
            Files.copy(image.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Saved image to: {}", targetLocation.toAbsolutePath());
            return "/" + uploadDirectory + "/" + fileName;
        } catch (IOException e) {
            throw new IOException("Could not save image: " + e.getMessage(), e);
        }
    }

    public AnnuncioDTO convertToDTO(Annuncio savedAnnuncio) {
        List<FotoImmobile> fotoImmobili = fotoImmobileRepository.findByIdAnnuncio(savedAnnuncio.getId());
        List<String> immaginiUrls = fotoImmobili.stream()
                .map(FotoImmobile::getUrl)
                .toList();

        return new AnnuncioDTO(
                savedAnnuncio.getIdImmobile(),
                savedAnnuncio.getIdAgente(),
                savedAnnuncio.getTitolo(),
                savedAnnuncio.getTipo(),
                savedAnnuncio.getPrezzo(),
                savedAnnuncio.getDescrizione(),
                immaginiUrls,
                savedAnnuncio.getNumeroVisualizzazioni(),
                savedAnnuncio.getNumeroOfferte(),
                savedAnnuncio.getNumeroVisitePrenotate()
        );
    }

    public List<Annuncio> findAnnunciByCittaAndFiltri(FiltroAnnunciDTO filtro, String citta) {
        logger.debug("Ricerca annunci per città : {}, filtro: {}", citta, filtro);
        return annuncioRepository.findByFiltro(filtro, citta);
    }

    public AnnuncioDTO getAnnuncioByIdImmobile(Long id) {
        Optional<Annuncio> annuncioOptional = annuncioRepository.findByIdImmobile(id);

        if (annuncioOptional.isEmpty()) {
            logger.warn("Annuncio non trovato con ID immobile: {}", id);
            throw new IllegalArgumentException("Annuncio non trovato con ID immobile: " + id);
        }

        Annuncio annuncio = annuncioOptional.get();

        return convertToDTO(annuncio);
    }

    public List<AnnuncioDTO> findAnnunciInRadius(MapSearchDTO map, FiltroAnnunciDTO filtro) {
        logger.debug("Ricerca annunci in raggio: {}, filtro: {}", map, filtro);
        List<Annuncio> annunci = annuncioRepository.findAnnunciInRadius(map, filtro);
        return annunci.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional
    public Annuncio updateAnnuncioStats(UpdateAnnuncioDTO updateAnnuncioDTO) {
        Optional<Annuncio> annuncioOptional = annuncioRepository.findByIdImmobile(updateAnnuncioDTO.getIdImmobile());

        if (annuncioOptional.isPresent()) {
            Annuncio annuncio = annuncioOptional.get();

            switch (updateAnnuncioDTO.getTipoAggiornamento()) {
                case "visualizzazione":
                    annuncio.setNumeroVisualizzazioni(annuncio.getNumeroVisualizzazioni() + 1);
                    break;
                case "offerta":
                    annuncio.setNumeroOfferte(annuncio.getNumeroOfferte() + 1);
                    break;
                case "visita":
                    annuncio.setNumeroVisitePrenotate(annuncio.getNumeroVisitePrenotate() + 1);
                    break;
                default:
                    throw new IllegalArgumentException("Tipo di aggiornamento non valido: " + updateAnnuncioDTO.getTipoAggiornamento());
            }

            Annuncio savedAnnuncio = annuncioRepository.save(annuncio);
            logger.info("Aggiornate statistiche annuncio IdImmobile: {}, Tipo: {}", annuncio.getId(), updateAnnuncioDTO.getTipoAggiornamento());
            return savedAnnuncio;
        } else {
            logger.warn("Annuncio non trovato con IdImmobile: {}", updateAnnuncioDTO.getIdImmobile());
            throw new IllegalArgumentException("Annuncio non trovato con IdImmobile: " + updateAnnuncioDTO.getIdImmobile());
        }
    }

    public List<AnnuncioDTO> getAnnunciAgente(Long idAgente) {
        logger.debug("Ricerca annunci per agente");
        List<Annuncio> annunci = annuncioRepository.findByIdAgente(idAgente);
        return annunci.stream()
                .map(this::convertToDTO)
                .toList();
    }

}