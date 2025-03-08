package com.dietiestates25ui.service;

import com.dietiestates25ui.exception.GenericServiceException;
import com.dietiestates25ui.model.AgenziaImmobiliare;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AgenziaServiceTest {

    protected static final int MAX_FILE_SIZE = 2 * 1024 * 1024;

    @InjectMocks
    @Spy
    private AgenziaService agenziaService;

    // -- Agenzia --

    // -- Nome --
    // Validi
    private static final String NOME_AGENZIA = "test agenzia 123";
    private static final String NOME_AGENZIA_MINIMAL = "a";
    // Non validi
    private static final String NOME_AGENZIA_EMPTY = "";
    private static final String NOME_AGENZIA_NULL = null;
    private static final String NOME_AGENZIA_WITH_SPECIAL_CHARACTER = "test agenzia 123!";

    // -- Partita IVA --
    // Validi
    private static final String PARTITA_IVA_AGENZIA = "12345678901";
    // Non validi
    private static final String PARTITA_IVA_AGENZIA_EMPTY = "";
    private static final String PARTITA_IVA_AGENZIA_NULL = null;
    private static final String PARTITA_IVA_AGENZIA_WITH_LETTERS = "1234567890a";
    private static final String PARTITA_IVA_AGENZIA_WITH_10_DIGITS = "1234567890";

    // -- Indirizzo --
    // Validi
    private static final String INDIRIZZO_AGENZIA = "Via Roma 1";
    private static final String INDIRIZZO_AGENZIA_MINIMAL = "V";
    // Non validi
    private static final String INDIRIZZO_AGENZIA_EMPTY = "";
    private static final String INDIRIZZO_AGENZIA_NULL = null;

    // -- Email --
    // Validi
    private static final String EMAIL_AGENZIA = "prova@prova.com";
    // Non validi
    private static final String EMAIL_AGENZIA_EMPTY = "";
    private static final String EMAIL_AGENZIA_NULL = null;
    private static final String EMAIL_AGENZIA_WITHOUT_AT = "prova.com";
    private static final String EMAIL_AGENZIA_WITHOUT_DOT = "prova@prova";

    // -- Telefono --
    // Validi
    private static final String TELEFONO_AGENZIA = "1234567890";
    // Non validi
    private static final String TELEFONO_AGENZIA_EMPTY = "";
    private static final String TELEFONO_AGENZIA_NULL = null;
    private static final String TELEFONO_AGENZIA_WITH_LETTERS = "123456789a";
    private static final String TELEFONO_AGENZIA_WITH_9_DIGITS = "123456789";
    private static final String TELEFONO_AGENZIA_WITH_SPECIAL_CHARACTER = "123456789@";

    // -- Logo --
    // Validi
    private static final String LOGO_AGENZIA = "logo.png";
    // Non validi
    private static final String LOGO_AGENZIA_EMPTY = "";
    private static final String LOGO_AGENZIA_NULL = null;

    // ------------------------------------------------------------------

    // -- Password --
    // Validi
    private static final String PASSWORD = "Password1";
    // Non validi
    private static final String PASSWORD_EMPTY = "";
    private static final String PASSWORD_NULL = null;
    private static final String PASSWORD_WITHOUT_UPPERCASE = "password1";
    private static final String PASSWORD_WITHOUT_NUMBER = "Password";
    private static final String PASSWORD_WITH_7_DIGITS = "Passwo7";

    // -- Logo file --
    // Validi
    private static final File LOGO_FILE_WITH_MAX_SIZE = createImageWithSpecificSize("image1.png", 512, 512);
    private static final File LOGO_FILE_WITH_NOMINAL_SIZE = createImageWithSpecificSize("image2.png", 250, 250);
    private static final File LOGO_FILE_WITH_MINIMAL_SIZE = createImageWithSpecificSize("image3.png", 1, 1);
    private static final File LOGO_FILE_WITH_MAX_FILE_SIZE = createImageWithSpecificFileSize("image4.png", MAX_FILE_SIZE);
    private static final File LOGO_FILE_WITH_NOMINAL_FILE_SIZE = createImageWithSpecificFileSize("image5.png", MAX_FILE_SIZE / 2);
    private static final File LOGO_FILE_WITH_MINIMAL_FILE_SIZE = createImageWithSpecificFileSize("image6.png", 1);
    // Non validi
    private static final File LOGO_FILE_NULL = null;
    private static final File LOGO_FILE_NOT_IMAGE = new File("file.txt");
    private static final File LOGO_FILE_NOT_SQUARE = createImageWithSpecificSize("image7.png", 250, 300);
    private static final File LOGO_FILE_TOO_LARGE_FILE_SIZE = createImageWithSpecificFileSize("image8.png", MAX_FILE_SIZE + 1);
    private static final File LOGO_FILE_TOO_LARGE_IMAGE_SIZE = createImageWithSpecificSize("image9.png", 513, 513);





    public static File createImageWithSpecificFileSize(String fileName, long fileSizeInBytes) {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        File imageFile = new File(fileName);
        try {
            ImageIO.write(image, "png", imageFile);
            long currentSize = imageFile.length();
            if (currentSize < fileSizeInBytes) {
                long paddingSize = fileSizeInBytes - currentSize;
                try (FileOutputStream fos = new FileOutputStream(imageFile, true)) {
                    byte[] padding = new byte[1024];
                    while (paddingSize > 0) {
                        int bytesToWrite = (int) Math.min(padding.length, paddingSize);
                        fos.write(padding, 0, bytesToWrite);
                        paddingSize -= bytesToWrite;
                    }
                }
            }
        } catch (IOException e) {
            return null;
        }
        return imageFile;
    }


    public static File createImageWithSpecificSize(String fileName, int width, int height) {
        File imageFile = new File(fileName);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        try {
            ImageIO.write(image, "png", imageFile);
        } catch (IOException e) {
            return null;
        }

        return imageFile;
    }

    @Nested
    @DisplayName("Casi di registrazione agenzia non validi")
    class RegisterAgencyFailureTests {

        static Stream<Arguments> failureCases() {
            return Stream.of(
                    Arguments.of(NOME_AGENZIA_EMPTY, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA_NULL, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA_WITH_SPECIAL_CHARACTER, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA_EMPTY, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA_NULL, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA_WITH_LETTERS, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA_WITH_10_DIGITS, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA_EMPTY, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA_NULL, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA_EMPTY, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA_NULL, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA_WITHOUT_AT, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA_WITHOUT_DOT, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA_EMPTY, LOGO_AGENZIA, PASSWORD, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA_NULL, LOGO_AGENZIA, PASSWORD, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA_WITH_LETTERS, LOGO_AGENZIA, PASSWORD, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA_WITH_9_DIGITS, LOGO_AGENZIA, PASSWORD, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA_WITH_SPECIAL_CHARACTER, LOGO_AGENZIA, PASSWORD, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA_EMPTY, PASSWORD, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA_NULL, PASSWORD, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD_EMPTY, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD_NULL, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD_WITHOUT_UPPERCASE, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD_WITHOUT_NUMBER, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD_WITH_7_DIGITS, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD, LOGO_FILE_NULL),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD, LOGO_FILE_NOT_IMAGE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD, LOGO_FILE_NOT_SQUARE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD, LOGO_FILE_TOO_LARGE_FILE_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD, LOGO_FILE_TOO_LARGE_IMAGE_SIZE)

            );
        }

        @ParameterizedTest
        @MethodSource("failureCases")
        void testRegisterAgency_failure(String nome, String partitaIva, String indirizzo, String email, String telefono, String logo, String password, File logoFile) throws GenericServiceException {
            AgenziaImmobiliare agenzia = new AgenziaImmobiliare(nome, partitaIva, indirizzo, email, telefono, logo);

            assertThrows(GenericServiceException.class, () ->
                    agenziaService.registerAgenzia(agenzia, logoFile, password));

            verify(agenziaService, never()).executeAndHandleMultipart(anyString(), anyString(), any(), anyString(), anyString(), any());
        }
    }

    @Nested
    @DisplayName("Casi di aggiornamento validi")
    class RegisterAgencySuccessTests {

        static Stream<Arguments> successCases() {
            return Stream.of(
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD, LOGO_FILE_WITH_NOMINAL_SIZE),
                    Arguments.of(NOME_AGENZIA_MINIMAL, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA_MINIMAL, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD, LOGO_FILE_WITH_MINIMAL_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD, LOGO_FILE_WITH_MAX_SIZE),
                    Arguments.of(NOME_AGENZIA, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD, LOGO_FILE_WITH_MAX_FILE_SIZE),
                    Arguments.of(NOME_AGENZIA_MINIMAL, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA_MINIMAL, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD, LOGO_FILE_WITH_MINIMAL_FILE_SIZE),
                    Arguments.of(NOME_AGENZIA_MINIMAL, PARTITA_IVA_AGENZIA, INDIRIZZO_AGENZIA_MINIMAL, EMAIL_AGENZIA, TELEFONO_AGENZIA, LOGO_AGENZIA, PASSWORD, LOGO_FILE_WITH_NOMINAL_FILE_SIZE)
            );
        }

        @ParameterizedTest
        @MethodSource("successCases")
        void testRegisterAgency_success(String nome, String partitaIva, String indirizzo, String email, String telefono, String logo, String password, File logoFile) throws GenericServiceException, IOException {
            mockExecuteAndHandleMultipart();

            AgenziaImmobiliare agenzia = new AgenziaImmobiliare(nome, partitaIva, indirizzo, email, telefono, logo);

            agenziaService.registerAgenzia(agenzia, logoFile, password);

            verifySuccessCallOfExecuteAndHandleMultipart(agenzia, logoFile, password);
        }

        private void mockExecuteAndHandleMultipart() throws GenericServiceException {
            doReturn(null).when(agenziaService).executeAndHandleMultipart(
                    eq("/register"),
                    eq("POST"),
                    any(),
                    anyString(),
                    eq(null),
                    eq(null)
            );
        }

        private void verifySuccessCallOfExecuteAndHandleMultipart(AgenziaImmobiliare agenzia, File logoFile, String password) throws GenericServiceException {
            ArgumentCaptor<byte[]> bodyCaptor = ArgumentCaptor.forClass(byte[].class);
            ArgumentCaptor<String> contentTypeCaptor = ArgumentCaptor.forClass(String.class);

            verify(agenziaService, times(1)).executeAndHandleMultipart(
                    eq("/register"),
                    eq("POST"),
                    bodyCaptor.capture(),
                    contentTypeCaptor.capture(),
                    eq(null),
                    eq(null)
            );

//            String body = new String(bodyCaptor.getValue());
//
//            assertAll(
//                    () -> assertTrue(body.contains("nome=" + agenzia.getNome())),
//                    () -> assertTrue(body.contains("partitaIva=" + agenzia.getPartitaIva())),
//                    () -> assertTrue(body.contains("indirizzo=" + agenzia.getIndirizzo())),
//                    () -> assertTrue(body.contains("email=" + agenzia.getEmail())),
//                    () -> assertTrue(body.contains("telefono=" + agenzia.getTelefono())),
//                    () -> assertTrue(body.contains("logo=" + agenzia.getLogo())),
//                    () -> assertTrue(body.contains("password=" + password)),
//                    () -> assertTrue(body.contains("Content-Disposition: form-data; name=\"logo\"; filename=\"" + logoFile.getName() + "\"")),
//                    () -> assertTrue(body.contains("Content-Type: image/png")),
//                    () -> assertTrue(body.contains("Content-Length: " + logoFile.length())),
//                    () -> assertTrue(contentTypeCaptor.getValue().startsWith("multipart/form-data"))
//            );
        }
    }
}
