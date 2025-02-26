package com.dietiestates25backend.business.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "foto_immobile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FotoImmobile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "id_annuncio")
    private Long idAnnuncio;
}