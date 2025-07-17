package snn.soluciones.com.util;

import java.nio.file.Files;
import java.nio.file.Path;
import snn.soluciones.com.mh.DirectPasswordProvider;
import snn.soluciones.com.mh.FirstCertificateSelector;
import snn.soluciones.com.service.storage.S3FileService;
import java.security.cert.X509Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xades4j.providers.KeyingDataProvider;
import xades4j.providers.impl.FileSystemKeyStoreKeyingDataProvider;

@Component
public class CertificateManagerUtil {

    private static final Logger log = LoggerFactory.getLogger(CertificateManagerUtil.class);

    @Autowired
    private S3FileService s3FileService;
    
    /**
     * Configuración de certificado para firma
     */
    public static class CertificateConfig {
        private String keyStorePath;
        private String password;
        private String keyStoreType;
        private boolean validateCertificate;
        
        public CertificateConfig(String keyStorePath, String password) {
            this(keyStorePath, password, "pkcs12", true);
        }
        
        public CertificateConfig(String keyStorePath, String password, String keyStoreType, boolean validateCertificate) {
            this.keyStorePath = keyStorePath;
            this.password = password;
            this.keyStoreType = keyStoreType;
            this.validateCertificate = validateCertificate;
        }
        
        // Getters
        public String getKeyStorePath() { return keyStorePath; }
        public String getPassword() { return password; }
        public String getKeyStoreType() { return keyStoreType; }
        public boolean isValidateCertificate() { return validateCertificate; }
    }
    
    /**
     * Información del certificado
     */
    public static class CertificateInfo {
        private X509Certificate certificate;
        private String issuer;
        private String subject;
        private String serialNumber;
        private boolean isValid;
        private String validationMessage;
        
        public CertificateInfo(X509Certificate certificate, String issuer, String subject, 
                             String serialNumber, boolean isValid, String validationMessage) {
            this.certificate = certificate;
            this.issuer = issuer;
            this.subject = subject;
            this.serialNumber = serialNumber;
            this.isValid = isValid;
            this.validationMessage = validationMessage;
        }
        
        // Getters
        public X509Certificate getCertificate() { return certificate; }
        public String getIssuer() { return issuer; }
        public String getSubject() { return subject; }
        public String getSerialNumber() { return serialNumber; }
        public boolean isValid() { return isValid; }
        public String getValidationMessage() { return validationMessage; }
    }

    /**
     * NUEVA CLASE INTERNA para manejar archivos temporales
     */
    public static class TemporaryCertificateFile implements AutoCloseable {
        private final Path tempFile;
        private final KeyingDataProvider provider;

        public TemporaryCertificateFile(Path tempFile, KeyingDataProvider provider) {
            this.tempFile = tempFile;
            this.provider = provider;
        }

        public KeyingDataProvider getProvider() {
            return provider;
        }

        @Override
        public void close() {
            try {
                if (tempFile != null && Files.exists(tempFile)) {
                    Files.deleteIfExists(tempFile);
                    log.debug("Archivo temporal de certificado eliminado: {}", tempFile);
                }
            } catch (Exception e) {
                log.warn("No se pudo eliminar archivo temporal: {}", e.getMessage());
            }
        }
    }

    /**
     * MANTIENE LA MISMA FIRMA - Crea un KeyingDataProvider para XAdES
     * Ahora usa archivo temporal internamente
     */
    public KeyingDataProvider createKeyingDataProvider(CertificateConfig config) throws Exception {
        validateCertificateConfig(config);

        Path tempFile = null;
        try {
            // Descargar certificado de S3
            byte[] certificateData = s3FileService.downloadFileAsBytes(config.getKeyStorePath());

            // Crear archivo temporal
            tempFile = Files.createTempFile("cert-", ".p12");
            Files.write(tempFile, certificateData);

            log.debug("Certificado descargado a archivo temporal: {}", tempFile);

            // Usar el patrón Builder para crear FileSystemKeyStoreKeyingDataProvider
            FileSystemKeyStoreKeyingDataProvider provider = FileSystemKeyStoreKeyingDataProvider
                .builder(
                    config.getKeyStoreType(),
                    tempFile.toString(),
                    new FirstCertificateSelector()
                )
                .storePassword(new DirectPasswordProvider(config.getPassword()))
                .entryPassword(new DirectPasswordProvider(config.getPassword()))
                .fullChain(false)
                .build();

            log.info("KeyingDataProvider creado exitosamente desde archivo temporal");

            // IMPORTANTE: Registrar el archivo temporal para limpieza posterior
            registerTempFileForCleanup(tempFile);

            return provider;

        } catch (Exception e) {
            // Si hay error, limpiar el archivo temporal inmediatamente
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception deleteEx) {
                    log.warn("No se pudo eliminar archivo temporal tras error: {}", deleteEx.getMessage());
                }
            }

            log.error("Error creando KeyingDataProvider: {}", e.getMessage());
            throw new Exception("No se pudo crear el proveedor de certificados: " + e.getMessage(), e);
        }
    }

    /**
     * Registra archivos temporales para limpieza al finalizar la JVM
     */
    private void registerTempFileForCleanup(Path tempFile) {
        if (tempFile != null) {
            tempFile.toFile().deleteOnExit();
            log.debug("Archivo temporal registrado para limpieza al salir: {}", tempFile);
        }
    }

    /**
     * Valida la configuración del certificado - SIN CAMBIOS
     */
    public void validateCertificateConfig(CertificateConfig config) throws Exception {
        // ... [sin cambios - tal como está en tu código]
    }
}