package com.dietiestates25.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnunciRadiusSearchDTO {
    private MapSearchDTO map;
    private FiltroAnnunciDTO filtro;
}
