package com.dietiestates25.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiltroAnnunciDTO {
    private String tipo;
    private String tipologia;
    // Aggiungi altri campi filtro qui in futuro
}