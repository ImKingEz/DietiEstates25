package com.dietiestates25backend.business.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "agenzia_immobiliare")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgenziaImmobiliare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "partita_iva", nullable = false)
    private String partitaIva;

    @Column(name = "indirizzo_sede_legale", nullable = false)
    private String indirizzo;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "telefono", nullable = false)
    private String telefono;

    @Column(name = "url_logo", nullable = false)
    private String logo;

    public AgenziaImmobiliare(String nome, String partitaIva, String indirizzo, String email, String telefono, String logo) {
        this.nome = nome;
        this.partitaIva = partitaIva;
        this.indirizzo = indirizzo;
        this.email = email;
        this.telefono = telefono;
        this.logo = logo;
    }
}