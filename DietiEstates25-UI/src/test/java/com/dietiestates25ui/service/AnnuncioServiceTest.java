package com.dietiestates25ui.service;

import com.dietiestates25.dto.AnnuncioDTO;
import com.dietiestates25.dto.UpdateAnnuncioDTO;
import com.dietiestates25ui.exception.GenericServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnuncioServiceTest {

    private static final String UPDATE_STATS_URL = "/updateStats";
    private static final String METHOD = "PUT";
    private static final String TOKEN = "validToken";

    @Mock
    private ApiService apiService;

    @InjectMocks
    private AnnuncioService annuncioService;

    private Long idImmobileNominal = 5L;

    private String tipoAggiornamento;

    @Test
    void updateVisualizzazioneAnnuncioValid() throws GenericServiceException {
        tipoAggiornamento = "visualizzazione";
        AnnuncioDTO expectedAnnuncioDTO = new AnnuncioDTO();
        expectedAnnuncioDTO.setIdImmobile(idImmobileNominal);
        when(apiService.executeAndHandle(eq(UPDATE_STATS_URL), eq(METHOD), any(UpdateAnnuncioDTO.class), eq(TOKEN), eq(AnnuncioDTO.class)))
                .thenReturn(expectedAnnuncioDTO);

        AnnuncioDTO actualAnnuncioDTO = annuncioService.updateAnnuncioStats(idImmobileNominal, tipoAggiornamento, TOKEN);

        assertNotNull(actualAnnuncioDTO);
        assertEquals(expectedAnnuncioDTO, actualAnnuncioDTO);
        verify(apiService, times(1)).executeAndHandle(eq(UPDATE_STATS_URL), eq(METHOD), any(UpdateAnnuncioDTO.class), eq(TOKEN), eq(AnnuncioDTO.class));
    }

}
