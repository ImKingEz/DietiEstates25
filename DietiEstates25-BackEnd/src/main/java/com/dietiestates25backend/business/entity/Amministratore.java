package com.dietiestates25backend.business.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "amministratore")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Amministratore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    public Amministratore(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
