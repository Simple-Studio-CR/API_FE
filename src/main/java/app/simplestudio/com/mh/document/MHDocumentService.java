package app.simplestudio.com.mh.document;

import app.simplestudio.com.mh.auth.MHAuthenticationService;
import app.simplestudio.com.mh.config.MHConfigurationProperties;
import app.simplestudio.com.mh.http.MHHttpClientService;
import app.simplestudio.com.models.entity.ComprobantesElectronicos;
import app.simplestudio.com.service.IComprobantesElectronicosService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class MHDocumentService {
    
    private static final Logger log = LoggerFactory.getLogger(MHDocumentService.class);
    
    private final MHAuthenticationService authService;
    private final MHHttpClientService httpClient;
    private final MHConfigurationProperties config;
    private final IComprobantesElectronicosService comprobantesService;
    private final ObjectMapper objectMapper;
    
    public MHDocumentService(MHAuthenticationService authService,
                           MHHttpClientService httpClient,
                           MHConfigurationProperties config,
                           IComprobantesElectronicosService comprobantesService) {
        this.authService = authService;
        this.httpClient = httpClient;
        this.config = config;
        this.comprobantesService = comprobantesService;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Envía un documento al MH
     */
    public String enviarDocumento(String endpoint, String jsonDocument, String username, 
                                String password, String ambiente, String emisorToken, 
                                String clave) throws Exception {
        
        ComprobantesElectronicos ce = comprobantesService.findByClaveDocumento(clave);
        
        if (ce != null && ce.getResponseCodeSend() != null) {
            log.info("Documento ya enviado previamente, consultando estado: {}", clave);
            return consultarEstadoSiYaSeEnvio(endpoint, clave, username, password, 
                                            ambiente, emisorToken);
        }
        
        log.info("Enviando documento por primera vez: {}", clave);
        return enviarDocumentoPrimeraVez(endpoint, jsonDocument, username, password, 
                                       ambiente, emisorToken);
    }
    
    /**
     * Envía documento por primera vez
     */
    private String enviarDocumentoPrimeraVez(String endpoint, String jsonDocument, 
                                           String username, String password, String ambiente, 
                                           String emisorToken) throws Exception {
        
        String token = authService.obtenerTokenValido(username, password, ambiente, emisorToken);
        
        try {
            HttpResponse response = httpClient.executeJsonPost(endpoint, jsonDocument, token);
            int statusCode = httpClient.getStatusCode(response);
            String headers = httpClient.formatHeaders(response.getAllHeaders());
            
            return construirRespuestaEnvio(statusCode, headers);
            
        } catch (IOException e) {
            log.error("Error enviando documento al MH", e);
            throw new Exception("Error en comunicación con MH", e);
        }
    }
    
    /**
     * Consulta estado cuando el documento ya fue enviado
     */
    private String consultarEstadoSiYaSeEnvio(String endpoint, String clave, String username, 
                                            String password, String ambiente, String emisorToken) {
        try {
            String respuestaConsulta = consultarSiMhRecibioDocumento(endpoint, clave, username, 
                                                                   password, ambiente, emisorToken);
            
            if ("error".equalsIgnoreCase(respuestaConsulta)) {
                log.info("MH no tiene el documento, reenviando: {}", clave);
                // Aquí se podría implementar reenvío automático
                return construirRespuestaError("Documento no encontrado en MH");
            }
            
            return respuestaConsulta;
            
        } catch (Exception e) {
            log.error("Error consultando estado de documento: {}", clave, e);
            return construirRespuestaError("Error consultando estado");
        }
    }
    
    /**
     * Consulta el estado de un documento específico
     */
    public String consultarEstadoDocumento(String endpoint, String clave, String username, 
                                         String password, String ambiente, String emisorToken) {
        try {
            String token = authService.obtenerTokenValido(username, password, ambiente, emisorToken);
            String url = endpoint + clave;
            
            HttpResponse response = httpClient.executeGet(url, token);
            String responseBody = httpClient.extractResponseBody(response);
            int statusCode = httpClient.getStatusCode(response);
            
            return procesarRespuestaConsulta(responseBody, statusCode, clave);
            
        } catch (Exception e) {
            log.error("Error consultando estado de documento: {}", clave, e);
            return construirRespuestaError("Error en consulta: " + e.getMessage());
        }
    }
    
    /**
     * Consulta si el MH recibió un documento (método de verificación)
     */
    public String consultarSiMhRecibioDocumento(String endpoint, String clave, String username, 
                                               String password, String ambiente, String emisorToken) {
        try {
            String token = authService.obtenerTokenValido(username, password, ambiente, emisorToken);
            String url = endpoint + clave;
            
            long startTime = System.currentTimeMillis();
            HttpResponse response = httpClient.executeGet(url, token);
            long duration = System.currentTimeMillis() - startTime;
            
            String responseBody = httpClient.extractResponseBody(response);
            int statusCode = httpClient.getStatusCode(response);
            
            log.info("Consulta a MH completada en {}ms - Status: {}", duration, statusCode);
            
            return determinarEstadoRecepcion(responseBody, statusCode);
            
        } catch (Exception e) {
            log.error("Error verificando recepción en MH para clave: {}", clave, e);
            return "error";
        }
    }
    
    /**
     * Consulta estado de cualquier documento (método genérico)
     */
    public String consultarEstadoCualquierDocumento(String endpoint, String clave, 
                                                   String username, String password, 
                                                   String ambiente, String emisorToken) {
        try {
            String token = authService.obtenerTokenValido(username, password, ambiente, emisorToken);
            String url = endpoint + clave;
            
            long startTime = System.currentTimeMillis();
            HttpResponse response = httpClient.executeGet(url, token);
            long duration = System.currentTimeMillis() - startTime;
            
            String responseBody = httpClient.extractResponseBody(response);
            int statusCode = httpClient.getStatusCode(response);
            
            log.info("Consulta genérica completada en {}s - Status: {}", 
                    duration / 1000.0, statusCode);
            log.info("Headers: {}", httpClient.formatHeaders(response.getAllHeaders()));
            
            return validarRespuestaGenerica(responseBody);
            
        } catch (Exception e) {
            log.error("Error en consulta genérica para clave: {}", clave, e);
            return construirRespuestaError("Error en consulta");
        }
    }
    
    /**
     * Procesa respuesta de consulta y extrae XML si está presente
     */
    private String procesarRespuestaConsulta(String responseBody, int statusCode, String clave) {
        try {
            Map<String, Object> response = objectMapper.readValue(responseBody, 
                new TypeReference<Map<String, Object>>() {});
            
            String respuestaXml = (String) response.get("respuesta-xml");
            if (respuestaXml != null) {
                // Decodificar XML de respuesta
                String xmlDecodificado = new String(Base64.decodeBase64(respuestaXml), "UTF-8");
                guardarXmlRespuesta(clave, xmlDecodificado);
            }
            
            return construirRespuestaConsulta(response, statusCode);
            
        } catch (Exception e) {
            log.error("Error procesando respuesta de consulta", e);
            return construirRespuestaError("Error procesando respuesta");
        }
    }
    
    /**
     * Determina si el MH recibió el documento basado en la respuesta
     */
    private String determinarEstadoRecepcion(String responseBody, int statusCode) {
        try {
            objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            return "ok";
        } catch (Exception e) {
            return "error";
        }
    }
    
    /**
     * Valida respuesta genérica de consulta
     */
    private String validarRespuestaGenerica(String responseBody) {
        try {
            objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            return "ok";
        } catch (Exception e) {
            return "error";
        }
    }
    
    /**
     * Guarda XML de respuesta del MH
     */
    private void guardarXmlRespuesta(String clave, String xmlContent) {
        try {
            String fileName = clave + "-respuesta-mh.xml";
            Path filePath = Paths.get(config.getUploadPath(), fileName);
            FileUtils.writeStringToFile(filePath.toFile(), xmlContent, "UTF-8");
            log.debug("XML de respuesta guardado: {}", fileName);
        } catch (IOException e) {
            log.error("Error guardando XML de respuesta para clave: {}", clave, e);
        }
    }
    
    /**
     * Construye respuesta de envío
     */
    private String construirRespuestaEnvio(int statusCode, String headers) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("resp", "");
        respuesta.put("fecha", "");
        respuesta.put("ind-estado", "");
        respuesta.put("respuesta-xml", String.valueOf(statusCode));
        
        try {
            return objectMapper.writeValueAsString(respuesta);
        } catch (Exception e) {
            log.error("Error serializando respuesta", e);
            return "{\"error\":\"Error interno\"}";
        }
    }
    
    /**
     * Construye respuesta de consulta
     */
    private String construirRespuestaConsulta(Map<String, Object> response, int statusCode) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("resp", response.getOrDefault("ind-estado", ""));
        respuesta.put("fecha", response.getOrDefault("fecha", ""));
        respuesta.put("code", String.valueOf(statusCode));
        
        try {
            return objectMapper.writeValueAsString(respuesta);
        } catch (Exception e) {
            log.error("Error serializando respuesta de consulta", e);
            return "{\"error\":\"Error interno\"}";
        }
    }
    
    /**
     * Construye respuesta de error
     */
    private String construirRespuestaError(String mensaje) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("error", mensaje);
        respuesta.put("resp", "");
        respuesta.put("fecha", "");
        
        try {
            return objectMapper.writeValueAsString(respuesta);
        } catch (Exception e) {
            log.error("Error serializando respuesta de error", e);
            return "{\"error\":\"Error interno\"}";
        }
    }
}