package app.simplestudio.com.util;

import app.simplestudio.com.mh.DirectPasswordProvider;
import app.simplestudio.com.mh.FirstCertificateSelector;
import java.io.File;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xades4j.providers.KeyingDataProvider;
import xades4j.providers.impl.FileSystemKeyStoreKeyingDataProvider;

@Component
public class CertificateManagerUtil {
    
    private static final Logger log = LoggerFactory.getLogger(CertificateManagerUtil.class);
    
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
     * Crea un KeyingDataProvider para XAdES con la configuración especificada
     * MANTIENE LA MISMA LÓGICA que FileSystemKeyStoreKeyingDataProvider original
     */
    public KeyingDataProvider createKeyingDataProvider(CertificateConfig config) throws Exception {
        validateCertificateConfig(config);
        
        try {
            FileSystemKeyStoreKeyingDataProvider provider = new FileSystemKeyStoreKeyingDataProvider(
                config.getKeyStoreType(),
                config.getKeyStorePath(),
                new FirstCertificateSelector(),
                new DirectPasswordProvider(config.getPassword()),
                new DirectPasswordProvider(config.getPassword()),
                false // No protection domain
            );
            
            log.info("KeyingDataProvider creado exitosamente para: {}", config.getKeyStorePath());
            return provider;
            
        } catch (Exception e) {
            log.error("Error creando KeyingDataProvider: {}", e.getMessage());
            throw new Exception("No se pudo crear el proveedor de certificados: " + e.getMessage(), e);
        }
    }
    
    /**
     * Valida la configuración del certificado
     */
    public void validateCertificateConfig(CertificateConfig config) throws Exception {
        if (config == null) {
            throw new IllegalArgumentException("La configuración del certificado no puede ser nula");
        }
        
        if (config.getKeyStorePath() == null || config.getKeyStorePath().trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta del keystore es requerida");
        }
        
        if (config.getPassword() == null || config.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña del certificado es requerida");
        }
        
        // Verificar que el archivo existe
        File keystoreFile = new File(config.getKeyStorePath());
        if (!keystoreFile.exists()) {
            throw new Exception("El archivo de certificado no existe: " + config.getKeyStorePath());
        }
        
        if (!keystoreFile.canRead()) {
            throw new Exception("No se puede leer el archivo de certificado: " + config.getKeyStorePath());
        }
        
        log.debug("Configuración del certificado validada exitosamente");
    }


}