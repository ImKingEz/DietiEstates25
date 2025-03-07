package com.dietiestates25ui.service;

import com.dietiestates25.dto.AnnuncioDTO;
import com.dietiestates25.dto.UpdateAnnuncioDTO;
import com.dietiestates25ui.exception.GenericServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnuncioServiceTest {

    private static final String TOKEN = "validToken";

    @InjectMocks
    @Spy
    private AnnuncioService annuncioService;

    // -- Id immobile --
    // Validi
    private static final Long ID_IMMOBILE_NOMINAL = 5L;
    private static final Long ID_IMMOBILE_MINIMAL = 1L;
    // Non validi
    private static final Long ID_IMMOBILE_ZERO = 0L;
    private static final Long ID_IMMOBILE_NEGATIVE = -1L;
    private static final Long ID_IMMOBILE_NULL = null;

    // -- Tipo aggiornamento --
    // Validi
    private static final String TIPO_AGGIORNAMENTO_VISUALIZZAZIONE = "visualizzazione";
    private static final String TIPO_AGGIORNAMENTO_VISITA = "visita";
    private static final String TIPO_AGGIORNAMENTO_OFFERTA = "offerta";
    // Non validi
    private static final String TIPO_AGGIORNAMENTO_NULL = null;
    private static final String TIPO_AGGIORNAMENTO_EMPTY = "";
    private static final String TIPO_AGGIORNAMENTO_RANDOM = "random";


    @Nested
    @DisplayName("Casi di aggiornamento non validi")
    class UpdateStatsFailureTests {

        static Stream<Arguments> failureCases() {
            return Stream.of(
                    Arguments.of(ID_IMMOBILE_ZERO, TIPO_AGGIORNAMENTO_VISUALIZZAZIONE),
                    Arguments.of(ID_IMMOBILE_NEGATIVE, TIPO_AGGIORNAMENTO_VISITA),
                    Arguments.of(ID_IMMOBILE_NULL, TIPO_AGGIORNAMENTO_OFFERTA),
                    Arguments.of(ID_IMMOBILE_NOMINAL, TIPO_AGGIORNAMENTO_NULL),
                    Arguments.of(ID_IMMOBILE_MINIMAL, TIPO_AGGIORNAMENTO_EMPTY),
                    Arguments.of(ID_IMMOBILE_NOMINAL, TIPO_AGGIORNAMENTO_RANDOM)
            );
        }

        @ParameterizedTest
        @MethodSource("failureCases")
        void testUpdateAnnuncioStats_failure(Long idImmobile, String tipoAggiornamento) throws GenericServiceException {
            assertThrows(IllegalArgumentException.class, () ->
                    annuncioService.updateAnnuncioStats(idImmobile, tipoAggiornamento, TOKEN));

            verify(annuncioService, never()).executeAndHandle(anyString(), anyString(), any(), anyString(), any());
        }
    }

    @Nested
    @DisplayName("Casi di aggiornamento validi")
    class UpdateStatsSuccessTests {

        static Stream<Arguments> successCases() {
            return Stream.of(
                    Arguments.of(ID_IMMOBILE_NOMINAL, TIPO_AGGIORNAMENTO_VISUALIZZAZIONE),
                    Arguments.of(ID_IMMOBILE_MINIMAL, TIPO_AGGIORNAMENTO_OFFERTA),
                    Arguments.of(ID_IMMOBILE_NOMINAL, TIPO_AGGIORNAMENTO_VISITA)
            );
        }

        @ParameterizedTest
        @MethodSource("successCases")
        void testUpdateAnnuncioStats_success(Long idImmobile, String tipoAggiornamento) throws GenericServiceException {
            AnnuncioDTO fakeResponse = new AnnuncioDTO();

            mockExecuteAndHandle(fakeResponse);

            AnnuncioDTO result = annuncioService.updateAnnuncioStats(idImmobile, tipoAggiornamento, TOKEN);

            assertNotNull(result);
            assertEquals(fakeResponse, result);

            verifySuccessCallOfExecuteAndHandle(idImmobile, tipoAggiornamento);
        }


        private void mockExecuteAndHandle(AnnuncioDTO fakeResponse) throws GenericServiceException {
            doReturn(fakeResponse).when(annuncioService).executeAndHandle(
                    eq("/updateStats"),
                    eq("PUT"),
                    any(UpdateAnnuncioDTO.class),
                    eq(TOKEN),
                    eq(AnnuncioDTO.class)
            );
        }

        private void verifySuccessCallOfExecuteAndHandle(Long idImmobile, String tipoAggiornamento) throws GenericServiceException {
            verify(annuncioService).executeAndHandle(
                    eq("/updateStats"),
                    eq("PUT"),
                    argThat(updateDto ->
                            updateDto instanceof UpdateAnnuncioDTO &&
                                    ((UpdateAnnuncioDTO) updateDto).getIdImmobile().equals(idImmobile) &&
                                    ((UpdateAnnuncioDTO) updateDto).getTipoAggiornamento().equals(tipoAggiornamento)
                    ),
                    eq(TOKEN),
                    eq(AnnuncioDTO.class)
            );
        }
    }
}