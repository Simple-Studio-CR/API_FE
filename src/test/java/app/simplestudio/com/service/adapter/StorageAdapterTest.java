package app.simplestudio.com.service.adapter;

import app.simplestudio.com.service.IStorageService;
import app.simplestudio.com.util.FileManagerUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageAdapterTest {

    @Mock
    private IStorageService storageService;

    @Mock
    private FileManagerUtil fileManagerUtil;

    @InjectMocks
    private StorageAdapter storageAdapter;

    private static final String TEST_EMISOR_ID = "123456789";
    private static final String TEST_FILE_PATH = "/home/XmlClientes/123456789/test-factura-sign.xml";
    private static final String TEST_CONTENT = "<?xml version=\"1.0\"?><Factura>Test</Factura>";
    private static final String EXPECTED_S3_KEY = "XmlClientes/123456789/xml/signed/test-factura-sign.xml";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(storageAdapter, "pathUploadFilesApi", "/home/XmlClientes/");
        ReflectionTestUtils.setField(storageAdapter, "useS3", true);
        ReflectionTestUtils.setField(storageAdapter, "fallbackToDisk", true);
    }

    // ==================== TESTS DE CONVERSIÓN DE RUTAS ====================

    @Test
    void testConvertFilePathToS3Key_SignedXml() {
        // Given - archivo XML firmado
        String filePath = "/home/XmlClientes/123456789/test-factura-sign.xml";

        when(storageService.buildKey(eq(TEST_EMISOR_ID), eq("xml"), eq("signed"), eq("test-factura-sign.xml")))
            .thenReturn(EXPECTED_S3_KEY);

        // When - convertir a S3 key usando saveToFile (que llama internamente a convertFilePathToS3Key)
        assertDoesNotThrow(() -> storageAdapter.saveToFile(filePath, TEST_CONTENT));

        // Then - verificar que se llamó con la key correcta
        verify(storageService).uploadFile(eq(EXPECTED_S3_KEY), eq(TEST_CONTENT), eq("application/xml"));
    }

    @Test
    void testConvertFilePathToS3Key_ResponseXml() {
        // Given - archivo de respuesta MH
        String filePath = "/home/XmlClientes/123456789/test-respuesta-mh.xml";
        String expectedKey = "XmlClientes/123456789/xml/responses/test-respuesta-mh.xml";

        when(storageService.buildKey(eq(TEST_EMISOR_ID), eq("xml"), eq("responses"), eq("test-respuesta-mh.xml")))
            .thenReturn(expectedKey);

        // When & Then
        assertDoesNotThrow(() -> storageAdapter.saveToFile(filePath, TEST_CONTENT));
        verify(storageService).uploadFile(eq(expectedKey), eq(TEST_CONTENT), eq("application/xml"));
    }

    @Test
    void testConvertFilePathToS3Key_OriginalXml() {
        // Given - archivo XML original
        String filePath = "/home/XmlClientes/123456789/test-factura.xml";
        String expectedKey = "XmlClientes/123456789/xml/original/test-factura.xml";

        when(storageService.buildKey(eq(TEST_EMISOR_ID), eq("xml"), eq("original"), eq("test-factura.xml")))
            .thenReturn(expectedKey);

        // When & Then
        assertDoesNotThrow(() -> storageAdapter.saveToFile(filePath, TEST_CONTENT));
        verify(storageService).uploadFile(eq(expectedKey), eq(TEST_CONTENT), eq("application/xml"));
    }

    // ==================== TESTS DE OPERACIONES PRINCIPALES ====================

    @Test
    void testSaveToFile_S3Success() throws Exception {
        // Given
        when(storageService.buildKey(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(EXPECTED_S3_KEY);

        // When
        storageAdapter.saveToFile(TEST_FILE_PATH, TEST_CONTENT);

        // Then
        verify(storageService).uploadFile(eq(EXPECTED_S3_KEY), eq(TEST_CONTENT), eq("application/xml"));
        verify(fileManagerUtil, never()).saveToFile(anyString(), anyString());
    }

    @Test
    void testSaveToFile_S3FailsWithFallback() throws Exception {
        // Given
        when(storageService.buildKey(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(EXPECTED_S3_KEY);
        when(storageService.uploadFile(anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("S3 connection failed"));

        // When
        storageAdapter.saveToFile(TEST_FILE_PATH, TEST_CONTENT);

        // Then
        verify(storageService).uploadFile(eq(EXPECTED_S3_KEY), eq(TEST_CONTENT), eq("application/xml"));
        verify(fileManagerUtil).saveToFile(eq(TEST_FILE_PATH), eq(TEST_CONTENT));
    }

    @Test
    void testReadFromFile_S3Success() throws Exception {
        // Given
        when(storageService.buildKey(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(EXPECTED_S3_KEY);
        when(storageService.downloadFileAsString(eq(EXPECTED_S3_KEY)))
            .thenReturn(TEST_CONTENT);

        // When
        String result = storageAdapter.readFromFile(TEST_FILE_PATH);

        // Then
        assertEquals(TEST_CONTENT, result);
        verify(storageService).downloadFileAsString(eq(EXPECTED_S3_KEY));
        verify(fileManagerUtil, never()).readFromFile(anyString());
    }

    @Test
    void testReadFromFile_S3NotFoundWithFallback() throws Exception {
        // Given
        when(storageService.buildKey(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(EXPECTED_S3_KEY);
        when(storageService.downloadFileAsString(eq(EXPECTED_S3_KEY)))
            .thenReturn(null);
        when(fileManagerUtil.fileExists(eq(TEST_FILE_PATH)))
            .thenReturn(true);
        when(fileManagerUtil.readFromFile(eq(TEST_FILE_PATH)))
            .thenReturn(TEST_CONTENT);

        // When
        String result = storageAdapter.readFromFile(TEST_FILE_PATH);

        // Then
        assertEquals(TEST_CONTENT, result);
        verify(storageService).downloadFileAsString(eq(EXPECTED_S3_KEY));
        verify(fileManagerUtil).fileExists(eq(TEST_FILE_PATH));
        verify(fileManagerUtil).readFromFile(eq(TEST_FILE_PATH));
    }

    @Test
    void testFileExists_S3Success() {
        // Given
        when(storageService.buildKey(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(EXPECTED_S3_KEY);
        when(storageService.fileExists(eq(EXPECTED_S3_KEY)))
            .thenReturn(true);

        // When
        boolean result = storageAdapter.fileExists(TEST_FILE_PATH);

        // Then
        assertTrue(result);
        verify(storageService).fileExists(eq(EXPECTED_S3_KEY));
        verify(fileManagerUtil, never()).fileExists(anyString());
    }

    @Test
    void testFileExists_S3NotFoundWithFallback() {
        // Given
        when(storageService.buildKey(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(EXPECTED_S3_KEY);
        when(storageService.fileExists(eq(EXPECTED_S3_KEY)))
            .thenReturn(false);
        when(fileManagerUtil.fileExists(eq(TEST_FILE_PATH)))
            .thenReturn(true);

        // When
        boolean result = storageAdapter.fileExists(TEST_FILE_PATH);

        // Then
        assertTrue(result);
        verify(storageService).fileExists(eq(EXPECTED_S3_KEY));
        verify(fileManagerUtil).fileExists(eq(TEST_FILE_PATH));
    }

    // ==================== TESTS DE MÉTODOS ESPECÍFICOS XML ====================

    @Test
    void testSaveXmlFile() throws Exception {
        // Given
        String expectedKey = "XmlClientes/123456789/xml/original/test-factura.xml";
        when(storageService.buildKey(eq(TEST_EMISOR_ID), eq("xml"), eq("original"), eq("test-factura.xml")))
            .thenReturn(expectedKey);

        // When
        storageAdapter.saveXmlFile(TEST_EMISOR_ID, "test-factura", TEST_CONTENT);

        // Then
        verify(storageService).uploadFile(eq(expectedKey), eq(TEST_CONTENT), eq("application/xml"));
    }

    @Test
    void testSaveSignedXmlFile() throws Exception {
        // Given
        String expectedKey = "XmlClientes/123456789/xml/signed/test-factura-sign.xml";
        when(storageService.buildKey(eq(TEST_EMISOR_ID), eq("xml"), eq("signed"), eq("test-factura-sign.xml")))
            .thenReturn(expectedKey);

        // When
        storageAdapter.saveSignedXmlFile(TEST_EMISOR_ID, "test-factura-sign", TEST_CONTENT);

        // Then
        verify(storageService).uploadFile(eq(expectedKey), eq(TEST_CONTENT), eq("application/xml"));
    }

    @Test
    void testSaveResponseXmlFile() throws Exception {
        // Given
        String expectedKey = "XmlClientes/123456789/xml/responses/test-respuesta-mh.xml";
        when(storageService.buildKey(eq(TEST_EMISOR_ID), eq("xml"), eq("responses"), eq("test-respuesta-mh.xml")))
            .thenReturn(expectedKey);

        // When
        storageAdapter.saveResponseXmlFile(TEST_EMISOR_ID, "test-respuesta-mh", TEST_CONTENT);

        // Then
        verify(storageService).uploadFile(eq(expectedKey), eq(TEST_CONTENT), eq("application/xml"));
    }

    @Test
    void testReadSignedXmlFile() throws Exception {
        // Given
        String expectedKey = "XmlClientes/123456789/xml/signed/test-factura-sign.xml";
        when(storageService.buildKey(eq(TEST_EMISOR_ID), eq("xml"), eq("signed"), eq("test-factura-sign.xml")))
            .thenReturn(expectedKey);
        when(storageService.downloadFileAsString(eq(expectedKey)))
            .thenReturn(TEST_CONTENT);

        // When
        String result = storageAdapter.readSignedXmlFile(TEST_EMISOR_ID, "test-factura-sign");

        // Then
        assertEquals(TEST_CONTENT, result);
        verify(storageService).downloadFileAsString(eq(expectedKey));
    }

    @Test
    void testSignedXmlExists() {
        // Given
        String expectedKey = "XmlClientes/123456789/xml/signed/test-factura-sign.xml";
        when(storageService.buildKey(eq(TEST_EMISOR_ID), eq("xml"), eq("signed"), eq("test-factura-sign.xml")))
            .thenReturn(expectedKey);
        when(storageService.fileExists(eq(expectedKey)))
            .thenReturn(true);

        // When
        boolean result = storageAdapter.signedXmlExists(TEST_EMISOR_ID, "test-factura-sign");

        // Then
        assertTrue(result);
        verify(storageService).fileExists(eq(expectedKey));
    }

    // ==================== TESTS DE DETERMINACIÓN DE CONTENT TYPE ====================

    @Test
    void testDetermineContentType() throws Exception {
        // Test XML - usar path válido
        String xmlPath = "/home/XmlClientes/123456789/test.xml";
        when(storageService.buildKey(anyString(), anyString(), anyString(), anyString()))
            .thenReturn("test-key");

        storageAdapter.saveToFile(xmlPath, "content");
        verify(storageService).uploadFile(anyString(), anyString(), eq("application/xml"));

        reset(storageService);
        when(storageService.buildKey(anyString(), anyString(), anyString(), anyString()))
            .thenReturn("test-key");

        // Test P12 certificate
        String certPath = "/home/XmlClientes/123456789/cert.p12";
        storageAdapter.saveToFile(certPath, "content");
        verify(storageService).uploadFile(anyString(), anyString(), eq("application/x-pkcs12"));

        reset(storageService);
        when(storageService.buildKey(anyString(), anyString(), anyString(), anyString()))
            .thenReturn("test-key");

        // Test PNG image
        String logoPath = "/home/XmlClientes/123456789/logo.png";
        storageAdapter.saveToFile(logoPath, "content");
        verify(storageService).uploadFile(anyString(), anyString(), eq("image/png"));
    }

    // ==================== TESTS DE CASOS EDGE ====================

    @Test
    void testConvertFilePathToS3Key_InvalidPath() {
        // Given - useS3 = true, fallbackToDisk = false para forzar excepción
        ReflectionTestUtils.setField(storageAdapter, "fallbackToDisk", false);

        // Test path null
        assertThrows(Exception.class, () -> {
            storageAdapter.saveToFile(null, TEST_CONTENT);
        });

        // Test path empty
        assertThrows(Exception.class, () -> {
            storageAdapter.saveToFile("", TEST_CONTENT);
        });

        // Test path sin suficientes partes
        assertThrows(Exception.class, () -> {
            storageAdapter.saveToFile("/invalid", TEST_CONTENT);
        });
    }

    @Test
    void testConvertFilePathToS3Key_InvalidPathWithFallback() throws Exception {
        // Given - useS3 = true, fallbackToDisk = true
        ReflectionTestUtils.setField(storageAdapter, "fallbackToDisk", true);

        // When - path inválido pero con fallback habilitado
        assertDoesNotThrow(() -> {
            storageAdapter.saveToFile("/invalid", TEST_CONTENT);
        });

        // Then - debería usar fallback
        verify(fileManagerUtil).saveToFile(eq("/invalid"), eq(TEST_CONTENT));
    }

    @Test
    void testCreateDirectoryIfNotExists() {
        // Given - S3 habilitado
        ReflectionTestUtils.setField(storageAdapter, "useS3", true);

        // When
        storageAdapter.createDirectoryIfNotExists("/some/directory");

        // Then - no debe llamar al fileManagerUtil cuando S3 está habilitado
        verify(fileManagerUtil, never()).createDirectoryIfNotExists(anyString());

        // Given - S3 deshabilitado
        ReflectionTestUtils.setField(storageAdapter, "useS3", false);

        // When
        storageAdapter.createDirectoryIfNotExists("/some/directory");

        // Then - debe llamar al fileManagerUtil cuando S3 está deshabilitado
        verify(fileManagerUtil).createDirectoryIfNotExists(eq("/some/directory"));
    }
}