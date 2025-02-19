package com.dietiestates25backend.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterAnnuncioDTO {
    private long idImmobile;
    private String titolo;
    private String tipo;
    private double prezzo;
    private String descrizione;
    private List<MultipartFile> immaginiUrls;
}