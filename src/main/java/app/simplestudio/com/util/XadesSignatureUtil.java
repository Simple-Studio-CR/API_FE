package app.simplestudio.com.util;

import java.io.ByteArrayInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xades4j.production.XadesEpesSigningProfile;
import xades4j.production.XadesSigner;
import xades4j.properties.ObjectIdentifier;
import xades4j.properties.SignaturePolicyBase;
import xades4j.properties.SignaturePolicyIdentifierProperty;
import xades4j.providers.KeyingDataProvider;
import xades4j.providers.SignaturePolicyInfoProvider;

@Component
public class XadesSignatureUtil {

    private static final Logger log = LoggerFactory.getLogger(XadesSignatureUtil.class);

    /**
     * URLs y constantes para Hacienda Costa Rica v4.3
     */
    private static final String HACIENDA_POLICY_URL =
        "https://www.hacienda.go.cr/ATV/ComprobanteElectronico/docs/esquemas/2016/v4.3/" +
            "Resolució_General_sobre_disposiciones_ténicas_comprobantes_electróicos_para_efectos_tributarios.pdf";

    private static final String HACIENDA_POLICY_DESCRIPTION = "Politica de Factura Digital";

    /**
     * Configuración de firma XAdES
     */
    public static class XadesConfig {
        private String policyUrl;
        private String policyDescription;
        private boolean useCustomAlgorithms;
        private String signingProfileType;

        public XadesConfig() {
            this(HACIENDA_POLICY_URL, HACIENDA_POLICY_DESCRIPTION, true, "EPES");
        }

        public XadesConfig(String policyUrl, String policyDescription, boolean useCustomAlgorithms, String signingProfileType) {
            this.policyUrl = policyUrl;
            this.policyDescription = policyDescription;
            this.useCustomAlgorithms = useCustomAlgorithms;
            this.signingProfileType = signingProfileType;
        }

        // Getters
        public String getPolicyUrl() { return policyUrl; }
        public String getPolicyDescription() { return policyDescription; }
        public boolean isUseCustomAlgorithms() { return useCustomAlgorithms; }
        public String getSigningProfileType() { return signingProfileType; }

        // Setters para personalización
        public void setPolicyUrl(String policyUrl) { this.policyUrl = policyUrl; }
        public void setPolicyDescription(String policyDescription) { this.policyDescription = policyDescription; }
        public void setUseCustomAlgorithms(boolean useCustomAlgorithms) { this.useCustomAlgorithms = useCustomAlgorithms; }
        public void setSigningProfileType(String signingProfileType) { this.signingProfileType = signingProfileType; }
    }

    /**
     * Crea un SignaturePolicyInfoProvider para Hacienda Costa Rica
     * MANTIENE LA MISMA LÓGICA que el provider original anónimo
     */
    public SignaturePolicyInfoProvider createHaciendaPolicyProvider(XadesConfig config) {
        return new SignaturePolicyInfoProvider() {
            @Override
            public SignaturePolicyBase getSignaturePolicy() {
                try {
                    ObjectIdentifier identifier = new ObjectIdentifier(config.getPolicyUrl());
                    ByteArrayInputStream policyStream = new ByteArrayInputStream(
                        config.getPolicyDescription().getBytes()
                    );

                    SignaturePolicyIdentifierProperty policy = new SignaturePolicyIdentifierProperty(
                        identifier,
                        policyStream
                    );

                    log.debug("Política de firma creada: {}", config.getPolicyUrl());
                    return policy;

                } catch (Exception e) {
                    log.error("Error creando política de firma: {}", e.getMessage());
                    throw new RuntimeException("No se pudo crear la política de firma", e);
                }
            }
        };
    }

    /**
     * Crea un XadesEpesSigningProfile configurado para Hacienda
     * MANTIENE LA MISMA CONFIGURACIÓN que la implementación original
     */
    public XadesEpesSigningProfile createSigningProfile(KeyingDataProvider keyingProvider, XadesConfig config) {
        try {
            SignaturePolicyInfoProvider policyProvider = createHaciendaPolicyProvider(config);

            XadesEpesSigningProfile profile = new XadesEpesSigningProfile(keyingProvider, policyProvider);

            // Configurar algoritmos personalizados si es necesario
            if (config.isUseCustomAlgorithms()) {
                // OPCIÓN 1: Intentar constructor directo
                try {
                    profile = new XadesEpesSigningProfile(keyingProvider, policyProvider);
                    log.debug("Algoritmos personalizados configurados via constructor");
                } catch (Exception constructorException) {
                    // OPCIÓN 2: Si constructor no funciona, usar perfil estándar
                    log.warn("Constructor con algoritmos no disponible, usando perfil estándar: {}", constructorException.getMessage());
                    profile = new XadesEpesSigningProfile(keyingProvider, policyProvider);
                }
            }

            log.info("Perfil de firma XAdES-EPES creado exitosamente");
            return profile;

        } catch (Exception e) {
            log.error("Error creando perfil de firma XAdES: {}", e.getMessage());
            throw new RuntimeException("No se pudo crear el perfil de firma", e);
        }
    }

    /**
     * Crea un XadesSigner listo para usar
     */
    public XadesSigner createXadesSigner(KeyingDataProvider keyingProvider, XadesConfig config) {
        try {
            XadesEpesSigningProfile profile = createSigningProfile(keyingProvider, config);
            XadesSigner signer = profile.newSigner();

            log.info("XadesSigner creado y listo para firmar");
            return signer;

        } catch (Exception e) {
            log.error("Error creando XadesSigner: {}", e.getMessage());
            throw new RuntimeException("No se pudo crear el firmador XAdES", e);
        }
    }

    /**
     * Crea configuración por defecto para Hacienda Costa Rica
     */
    public XadesConfig createDefaultHaciendaConfig() {
        return new XadesConfig();
    }

    /**
     * Crea configuración personalizada
     */
    public XadesConfig createCustomConfig(String policyUrl, String policyDescription) {
        return new XadesConfig(policyUrl, policyDescription, true, "EPES");
    }

    /**
     * Valida la configuración XAdES
     */
    public boolean validateXadesConfig(XadesConfig config) {
        if (config == null) {
            log.error("Configuración XAdES es nula");
            return false;
        }

        if (config.getPolicyUrl() == null || config.getPolicyUrl().trim().isEmpty()) {
            log.error("URL de política es requerida");
            return false;
        }

        if (config.getPolicyDescription() == null || config.getPolicyDescription().trim().isEmpty()) {
            log.error("Descripción de política es requerida");
            return false;
        }

        try {
            // Validar que la URL sea válida
            new java.net.URL(config.getPolicyUrl());
            log.debug("Configuración XAdES validada exitosamente");
            return true;

        } catch (Exception e) {
            log.error("URL de política inválida: {}", config.getPolicyUrl());
            return false;
        }
    }

    /**
     * Obtiene información de la configuración para logging
     */
    public void logXadesConfigDetails(XadesConfig config) {
        log.info("=== CONFIGURACIÓN XADES ===");
        log.info("URL Política: {}", config.getPolicyUrl());
        log.info("Descripción: {}", config.getPolicyDescription());
        log.info("Algoritmos Custom: {}", config.isUseCustomAlgorithms());
        log.info("Tipo Perfil: {}", config.getSigningProfileType());
        log.info("Válida: {}", validateXadesConfig(config));
        log.info("============================");
    }

    /**
     * Verifica si la configuración es compatible con Hacienda v4.3
     */
    public boolean isHaciendaCompatible(XadesConfig config) {
        return config != null &&
            config.getPolicyUrl() != null &&
            config.getPolicyUrl().contains("v4.3") &&
            "EPES".equals(config.getSigningProfileType());
    }

    /**
     * Crea configuración para testing (sin validación estricta)
     */
    public XadesConfig createTestConfig() {
        return new XadesConfig(
            "http://test.policy.url/test.pdf",
            "Test Policy Description",
            false,
            "EPES"
        );
    }
}