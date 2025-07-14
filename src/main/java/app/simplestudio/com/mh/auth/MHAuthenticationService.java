package app.simplestudio.com.mh.auth;

import app.simplestudio.com.mh.config.MHConfigurationProperties;
import app.simplestudio.com.mh.http.MHHttpClientService;
import app.simplestudio.com.models.entity.TokenControl;
import app.simplestudio.com.service.ITokenControlService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MHAuthenticationService {
    
    private static final Logger log = LoggerFactory.getLogger(MHAuthenticationService.class);
    
    private final ITokenControlService tokenControlService;
    private final MHHttpClientService httpClient;
    private final MHConfigurationProperties config;
    private final ObjectMapper objectMapper;
    
    public MHAuthenticationService(ITokenControlService tokenControlService,
                                 MHHttpClientService httpClient,
                                 MHConfigurationProperties config) {
        this.tokenControlService = tokenControlService;
        this.httpClient = httpClient;
        this.config = config;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Obtiene un token válido, renovándolo si es necesario
     */
    public String obtenerTokenValido(String username, String password, 
                                   String ambiente, String emisorToken) throws Exception {
        
        TokenControl tokenControl = tokenControlService.findByEmisor(emisorToken);
        
        if (tokenControl != null && esTokenValido(tokenControl)) {
            log.debug("Usando token existente para emisor: {}", emisorToken);
            return tokenControl.getAccessToken();
        }
        
        if (tokenControl != null && puedeRenovarToken(tokenControl)) {
            log.info("Renovando token para emisor: {}", emisorToken);
            return renovarToken(username, password, ambiente, emisorToken, 
                              tokenControl.getRefreshTokens());
        }
        
        log.info("Generando nuevo token para emisor: {}", emisorToken);
        return generarNuevoToken(username, password, ambiente, emisorToken);
    }
    
    /**
     * Genera un nuevo token desde cero
     */
    private String generarNuevoToken(String username, String password, 
                                   String ambiente, String emisorToken) throws Exception {
        
        String tokenUrl = config.getTokenUrlForEnvironment(ambiente) + "token";
        String clientId = config.getClientIdForEnvironment(ambiente);
        
        List<NameValuePair> parameters = crearParametrosNuevoToken(username, password, clientId);
        
        return ejecutarSolicitudToken(tokenUrl, parameters, emisorToken, "NUEVO");
    }
    
    /**
     * Renueva un token existente usando refresh token
     */
    private String renovarToken(String username, String password, String ambiente, 
                              String emisorToken, String refreshToken) throws Exception {
        
        String tokenUrl = config.getTokenUrlForEnvironment(ambiente) + "token";
        String clientId = config.getClientIdForEnvironment(ambiente);
        
        List<NameValuePair> parameters = crearParametrosRenovacion(username, password, 
                                                                  clientId, refreshToken);
        
        return ejecutarSolicitudToken(tokenUrl, parameters, emisorToken, "RENOVACION");
    }
    
    /**
     * Ejecuta la solicitud HTTP para obtener token
     */
    private String ejecutarSolicitudToken(String url, List<NameValuePair> parameters, 
                                        String emisorToken, String tipoAccion) throws Exception {
        
        long startTime = System.currentTimeMillis();
        
        try {
            HttpResponse response = httpClient.executeFormPost(url, parameters, null);
            String responseBody = httpClient.extractResponseBody(response);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Duración {} de token: {}ms", tipoAccion, duration);
            
            return procesarRespuestaToken(responseBody, emisorToken, tipoAccion);
            
        } catch (IOException e) {
            log.error("Error en solicitud de token para emisor: {}", emisorToken, e);
            throw new Exception("Error comunicándose con el MH para obtener token", e);
        }
    }
    
    /**
     * Procesa la respuesta del MH y guarda/actualiza el token
     */
    private String procesarRespuestaToken(String responseBody, String emisorToken, 
                                        String tipoAccion) throws Exception {
        
        try {
            Map<String, Object> response = objectMapper.readValue(responseBody, 
                new TypeReference<Map<String, Object>>() {});
            
            if (response.containsKey("error")) {
                manejarErrorToken(response, emisorToken);
                throw new Exception("Error del MH: " + response.get("error"));
            }
            
            String accessToken = (String) response.get("access_token");
            guardarToken(response, emisorToken, tipoAccion);
            
            return accessToken;
            
        } catch (IOException e) {
            log.error("Error parseando respuesta de token: {}", responseBody, e);
            throw new Exception("Respuesta inválida del MH", e);
        }
    }
    
    /**
     * Guarda o actualiza el token en base de datos
     */
    private void guardarToken(Map<String, Object> response, String emisorToken, 
                            String tipoAccion) {
        
        Long horaCreacion = System.currentTimeMillis() / 1000L / 60L;
        TokenControl tokenExistente = tokenControlService.findByEmisor(emisorToken);
        
        if (tokenExistente != null) {
            // Actualizar token existente
            tokenControlService.updateAccessToken(
                (String) response.get("access_token"),
                (String) response.get("expires_in"),
                horaCreacion,
                tokenExistente.getId()
            );
            log.debug("Token actualizado para emisor: {}", emisorToken);
        } else {
            // Crear nuevo token
            TokenControl nuevoToken = new TokenControl();
            nuevoToken.setEmisor(emisorToken);
            nuevoToken.setAccessToken((String) response.get("access_token"));
            nuevoToken.setExpiresIn((String) response.get("expires_in"));
            nuevoToken.setRefreshTokens((String) response.get("refresh_token"));
            nuevoToken.setRefreshExpiresIn((String) response.get("refresh_expires_in"));
            nuevoToken.setHoraCreacionToken(horaCreacion);
            nuevoToken.setHoraCreacionRefreshToken(horaCreacion);
            
            tokenControlService.save(nuevoToken);
            log.debug("Nuevo token creado para emisor: {}", emisorToken);
        }
    }
    
    /**
     * Maneja errores específicos del token
     */
    private void manejarErrorToken(Map<String, Object> response, String emisorToken) {
        String error = (String) response.get("error");
        
        if ("invalid_grant".equals(error)) {
            log.warn("Refresh token inválido para emisor: {}, eliminando token", emisorToken);
            tokenControlService.deleteTokenByEmisor(emisorToken);
        }
    }
    
    /**
     * Verifica si un token está vigente
     */
    private boolean esTokenValido(TokenControl token) {
        if (token == null || token.getAccessToken() == null) {
            return false;
        }
        
        long horaActual = System.currentTimeMillis() / 1000L / 60L;
        long horaExpiracion = token.getHoraCreacionToken() + 
                             Long.parseLong(token.getExpiresIn()) / 60L;
        
        // Renovar 5 minutos antes de expirar
        return horaActual < (horaExpiracion - 5);
    }
    
    /**
     * Verifica si se puede renovar el token con refresh token
     */
    private boolean puedeRenovarToken(TokenControl token) {
        if (token == null || token.getRefreshTokens() == null) {
            return false;
        }
        
        long horaActual = System.currentTimeMillis() / 1000L / 60L;
        long horaExpiracionRefresh = token.getHoraCreacionRefreshToken() + 
                                   Long.parseLong(token.getRefreshExpiresIn()) / 60L;
        
        return horaActual < horaExpiracionRefresh;
    }
    
    /**
     * Crea parámetros para solicitud de nuevo token
     */
    private List<NameValuePair> crearParametrosNuevoToken(String username, String password, 
                                                         String clientId) {
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("grant_type", "password"));
        parameters.add(new BasicNameValuePair("client_id", clientId));
        parameters.add(new BasicNameValuePair("client_secret", ""));
        parameters.add(new BasicNameValuePair("scope", ""));
        parameters.add(new BasicNameValuePair("username", username));
        parameters.add(new BasicNameValuePair("password", password));
        return parameters;
    }
    
    /**
     * Crea parámetros para renovación de token
     */
    private List<NameValuePair> crearParametrosRenovacion(String username, String password, 
                                                         String clientId, String refreshToken) {
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("grant_type", "refresh_token"));
        parameters.add(new BasicNameValuePair("refresh_token", refreshToken));
        parameters.add(new BasicNameValuePair("client_id", clientId));
        parameters.add(new BasicNameValuePair("client_secret", ""));
        parameters.add(new BasicNameValuePair("scope", ""));
        parameters.add(new BasicNameValuePair("username", username));
        parameters.add(new BasicNameValuePair("password", password));
        return parameters;
    }
    
    /**
     * Hace logout del token en el MH
     */
    public void logout(String username, String password, String ambiente, 
                      String emisorToken) {
        try {
            TokenControl token = tokenControlService.findByEmisor(emisorToken);
            if (token == null || token.getRefreshTokens() == null) {
                return;
            }
            
            String tokenUrl = config.getTokenUrlForEnvironment(ambiente) + "logout";
            String clientId = config.getClientIdForEnvironment(ambiente);
            
            List<NameValuePair> parameters = new ArrayList<>();
            parameters.add(new BasicNameValuePair("refresh_token", token.getRefreshTokens()));
            parameters.add(new BasicNameValuePair("client_id", clientId));
            parameters.add(new BasicNameValuePair("client_secret", ""));
            parameters.add(new BasicNameValuePair("scope", ""));
            parameters.add(new BasicNameValuePair("username", username));
            parameters.add(new BasicNameValuePair("password", password));
            
            HttpResponse response = httpClient.executeFormPost(tokenUrl, parameters, null);
            String responseBody = httpClient.extractResponseBody(response);
            
            log.info("Logout ejecutado para emisor: {} - Respuesta: {}", emisorToken, responseBody);
            
        } catch (Exception e) {
            log.error("Error en logout para emisor: {}", emisorToken, e);
        }
    }
}