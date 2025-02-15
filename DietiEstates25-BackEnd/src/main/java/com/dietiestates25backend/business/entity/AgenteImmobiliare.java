package com.dietiestates25backend.business.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "agente_immobiliare")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgenteImmobiliare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_agenzia", nullable = false)
    private Long idAgenzia;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "cognome", nullable = false)
    private String cognome;

    @Column(name = "data_di_nascita", nullable = false)
    private LocalDate dataDiNascita;

    @Column(name = "sesso", nullable = false)
    private String sesso;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    public AgenteImmobiliare(Long idAgenzia, String nome, String cognome, LocalDate dataDiNascita, String sesso, String email, String password) {
        this.idAgenzia = idAgenzia;
        this.nome = nome;
        this.cognome = cognome;
        this.dataDiNascita = dataDiNascita;
        this.sesso = sesso;
        this.email = email;
        this.password = password;
    }
}
