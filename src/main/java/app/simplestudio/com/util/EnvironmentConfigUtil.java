package app.simplestudio.com.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentConfigUtil {
    
    @Value("${endpoint.prod}")
    private String endpointProd;
    
    @Value("${endpoint.stag}")
    private String endpointStag;
    
    @Value("${token.prod}")
    private String tokenProd;
    
    @Value("${token.stag}")
    private String tokenStag;
    
    /**
     * Configuración de ambiente para comunicación con MH
     */
    public static class EnvironmentConfig {
        private String endpoint;
        private String urlToken;
        private String clientId;
        
        public EnvironmentConfig(String endpoint, String urlToken, String clientId) {
            this.endpoint = endpoint;
            this.urlToken = urlToken;
            this.clientId = clientId;
        }
        
        // Getters
        public String getEndpoint() { return endpoint; }
        public String getUrlToken() { return urlToken; }
        public String getClientId() { return clientId; }
    }
    
    /**
     * Configura ambiente basado en el tipo (prod/stag)
     */
    public EnvironmentConfig configureEnvironment(String ambiente) {
        if ("prod".equals(ambiente)) {
            return new EnvironmentConfig(endpointProd, tokenProd, "api-prod");
        } else {
            return new EnvironmentConfig(endpointStag, tokenStag, "api-stag");
        }
    }
    
    /**
     * Verifica si es ambiente de producción
     */
    public boolean isProduction(String ambiente) {
        return "prod".equals(ambiente);
    }
}