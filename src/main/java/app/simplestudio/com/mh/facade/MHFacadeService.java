package app.simplestudio.com.mh.facade;

import app.simplestudio.com.mh.auth.MHAuthenticationService;
import app.simplestudio.com.mh.config.MHConfigurationProperties;
import app.simplestudio.com.mh.document.MHDocumentService;
import app.simplestudio.com.mh.http.MHHttpClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Facade principal para todas las operaciones con el Ministerio de Hacienda
 * Este servicio orquesta todas las demás clases y proporciona una interfaz simple y limpia
 */
@Service
public class MHFacadeService {
    
    private static final Logger log = LoggerFactory.getLogger(MHFacadeService.class);
    
    private final MHAuthenticationService authService;
    private final MHDocumentService documentService;
    private final MHConfigurationProperties config;
    private final MHHttpClientService httpClientService;
    
    public MHFacadeService(MHAuthenticationService authService,
                          MHDocumentService documentService,
                          MHConfigurationProperties config,
                          MHHttpClientService httpClientService) {
        this.authService = authService;
        this.documentService = documentService;
        this.config = config;
        this.httpClientService = httpClientService;
    }
    
    /**
     * Obtiene un token válido para un emisor en un ambiente específico
     * Maneja automáticamente renovación y regeneración según sea necesario
     * 
     * @param username Usuario del MH
     * @param password Contraseña del MH
     * @param ambiente "prod" o "stag"
     * @param emisorToken Identificador del emisor
     * @return Token de acceso válido
     */
    public String obtenerToken(String username, String password, String ambiente, String emisorToken) {
        try {
            return authService.obtenerTokenValido(username, password, ambiente, emisorToken);
        } catch (Exception e) {
            log.error("Error obteniendo token para emisor: {} en ambiente: {}", emisorToken, ambiente, e);
            throw new RuntimeException("No se pudo obtener token del MH", e);
        }
    }
    
    /**
     * Envía un documento electrónico al MH
     * Maneja automáticamente la lógica de primer envío vs reenvío
     * 
     * @param jsonDocument Documento en formato JSON
     * @param username Usuario del MH
     * @param password Contraseña del MH  
     * @param ambiente "prod" o "stag"
     * @param emisorToken Identificador del emisor
     * @param clave Clave única del documento
     * @return Respuesta del MH en formato JSON
     */
    public String enviarDocumento(String jsonDocument, String username, String password, 
                                 String ambiente, String emisorToken, String clave) {
        try {
            String endpoint = config.getEndpointForEnvironment(ambiente);
            
            log.info("Enviando documento - Clave: {}, Emisor: {}, Ambiente: {}", 
                    clave, emisorToken, ambiente);
            
            return documentService.enviarDocumento(endpoint, jsonDocument, username, 
                                                 password, ambiente, emisorToken, clave);
            
        } catch (Exception e) {
            log.error("Error enviando documento - Clave: {}, Emisor: {}", clave, emisorToken, e);
            throw new RuntimeException("Error enviando documento al MH", e);
        }
    }
    
    /**
     * Consulta el estado de un documento en el MH
     * Incluye la respuesta XML del MH si está disponible
     * 
     * @param clave Clave única del documento
     * @param username Usuario del MH
     * @param password Contraseña del MH
     * @param ambiente "prod" o "stag"
     * @param emisorToken Identificador del emisor
     * @return Estado del documento en formato JSON
     */
    public String consultarEstadoDocumento(String clave, String username, String password, 
                                          String ambiente, String emisorToken) {
        try {
            String endpoint = config.getEndpointForEnvironment(ambiente);
            
            log.debug("Consultando estado - Clave: {}, Emisor: {}, Ambiente: {}", 
                     clave, emisorToken, ambiente);
            
            return documentService.consultarEstadoDocumento(endpoint, clave, username, 
                                                          password, ambiente, emisorToken);
            
        } catch (Exception e) {
            log.error("Error consultando estado - Clave: {}, Emisor: {}", clave, emisorToken, e);
            throw new RuntimeException("Error consultando estado en MH", e);
        }
    }
    
    /**
     * Verifica si el MH recibió un documento específico
     * Método más liviano para verificación rápida de recepción
     * 
     * @param clave Clave única del documento
     * @param username Usuario del MH
     * @param password Contraseña del MH
     * @param ambiente "prod" o "stag"
     * @param emisorToken Identificador del emisor
     * @return "ok" si fue recibido, "error" si no
     */
    public String verificarRecepcionDocumento(String clave, String username, String password, 
                                             String ambiente, String emisorToken) {
        try {
            String endpoint = config.getEndpointForEnvironment(ambiente);
            
            return documentService.consultarSiMhRecibioDocumento(endpoint, clave, username, 
                                                               password, ambiente, emisorToken);
            
        } catch (Exception e) {
            log.error("Error verificando recepción - Clave: {}, Emisor: {}", clave, emisorToken, e);
            return "error";
        }
    }
    
    /**
     * Consulta genérica para cualquier documento
     * Útil para verificaciones simples sin procesamiento de respuesta
     * 
     * @param clave Clave única del documento
     * @param username Usuario del MH
     * @param password Contraseña del MH
     * @param ambiente "prod" o "stag"
     * @param emisorToken Identificador del emisor
     * @return Estado simple de la consulta
     */
    public String consultarCualquierDocumento(String clave, String username, String password, 
                                             String ambiente, String emisorToken) {
        try {
            String endpoint = config.getEndpointForEnvironment(ambiente);
            
            return documentService.consultarEstadoCualquierDocumento(endpoint, clave, username, 
                                                                   password, ambiente, emisorToken);
            
        } catch (Exception e) {
            log.error("Error en consulta genérica - Clave: {}, Emisor: {}", clave, emisorToken, e);
            return "error";
        }
    }
    
    /**
     * Hace logout explícito de un token en el MH
     * Útil para limpiar tokens cuando se termina una sesión
     * 
     * @param username Usuario del MH
     * @param password Contraseña del MH
     * @param ambiente "prod" o "stag"
     * @param emisorToken Identificador del emisor
     */
    public void logout(String username, String password, String ambiente, String emisorToken) {
        try {
            log.info("Haciendo logout - Emisor: {}, Ambiente: {}", emisorToken, ambiente);
            authService.logout(username, password, ambiente, emisorToken);
        } catch (Exception e) {
            log.error("Error en logout - Emisor: {}, Ambiente: {}", emisorToken, ambiente, e);
            // No lanzamos excepción porque logout es best-effort
        }
    }
    
    /**
     * Valida configuración para un ambiente específico
     * Útil para verificar que la configuración está completa antes de usar
     * 
     * @param ambiente "prod" o "stag"
     * @return true si la configuración es válida
     */
    public boolean validarConfiguracion(String ambiente) {
        try {
            String endpoint = config.getEndpointForEnvironment(ambiente);
            String tokenUrl = config.getTokenUrlForEnvironment(ambiente);
            String clientId = config.getClientIdForEnvironment(ambiente);
            
            boolean valida = endpoint != null && !endpoint.trim().isEmpty() &&
                           tokenUrl != null && !tokenUrl.trim().isEmpty() &&
                           clientId != null && !clientId.trim().isEmpty();
            
            if (!valida) {
                log.warn("Configuración incompleta para ambiente: {}", ambiente);
            }
            
            return valida;
            
        } catch (Exception e) {
            log.error("Error validando configuración para ambiente: {}", ambiente, e);
            return false;
        }
    }
    
    /**
     * Obtiene información de configuración actual (para debugging)
     * No incluye datos sensibles como credenciales
     * 
     * @param ambiente "prod" o "stag"
     * @return Información de configuración
     */
    public String obtenerInfoConfiguracion(String ambiente) {
        try {
            StringBuilder info = new StringBuilder();
            info.append("Ambiente: ").append(ambiente).append("\n");
            info.append("Endpoint: ").append(config.getEndpointForEnvironment(ambiente)).append("\n");
            info.append("Token URL: ").append(config.getTokenUrlForEnvironment(ambiente)).append("\n");
            info.append("Timeout: ").append(config.getConnectionTimeout()).append("s\n");
            info.append("SSL Verification: ").append(config.isSslVerificationEnabled()).append("\n");
            info.append("Upload Path: ").append(config.getUploadPath()).append("\n");
            
            return info.toString();
            
        } catch (Exception e) {
            log.error("Error obteniendo info de configuración", e);
            return "Error obteniendo configuración";
        }
    }
    
    /**
     * Método de salud para verificar conectividad básica con el MH
     * No requiere autenticación, solo verifica conectividad
     * 
     * @param ambiente "prod" o "stag"
     * @return true si hay conectividad básica
     */
    public boolean verificarConectividad(String ambiente) {
        try {
            String endpoint = config.getEndpointForEnvironment(ambiente);
            if (endpoint == null || endpoint.trim().isEmpty()) {
                return false;
            }
            
            // Aquí se podría hacer un ping básico o health check
            // Por ahora solo validamos que la configuración existe
            return validarConfiguracion(ambiente);
            
        } catch (Exception e) {
            log.error("Error verificando conectividad para ambiente: {}", ambiente, e);
            return false;
        }
    }
}