package app.simplestudio.com.mh.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mh")
public class MHConfigurationProperties {

    // URLs y endpoints
    private String endpointProd;
    private String endpointStag;
    private String tokenUrlProd;
    private String tokenUrlStag;

    // Credenciales
    private String clientIdProd;
    private String clientIdStag;

    // Timeouts en segundos
    private int connectionTimeout = 35;
    private int socketTimeout = 35;
    private int requestTimeout = 35;

    // Paths
    private String uploadPath;

    // SSL configuración
    private boolean sslVerificationEnabled = false;

    // Retry configuración
    private int maxRetryAttempts = 3;
    private long retryDelayMillis = 1000;

    // Constructors
    public MHConfigurationProperties() {}

    // Métodos de utilidad
    public String getEndpointForEnvironment(String ambiente) {
        return "prod".equals(ambiente) ? endpointProd : endpointStag;
    }

    public String getTokenUrlForEnvironment(String ambiente) {
        return "prod".equals(ambiente) ? tokenUrlProd : tokenUrlStag;
    }

    public String getClientIdForEnvironment(String ambiente) {
        return "prod".equals(ambiente) ? clientIdProd : clientIdStag;
    }

    public int getTimeoutInMillis() {
        return connectionTimeout * 1000;
    }

    // Getters y Setters
    public String getEndpointProd() {
        return endpointProd;
    }

    public void setEndpointProd(String endpointProd) {
        this.endpointProd = endpointProd;
    }

    public String getEndpointStag() {
        return endpointStag;
    }

    public void setEndpointStag(String endpointStag) {
        this.endpointStag = endpointStag;
    }

    public String getTokenUrlProd() {
        return tokenUrlProd;
    }

    public void setTokenUrlProd(String tokenUrlProd) {
        this.tokenUrlProd = tokenUrlProd;
    }

    public String getTokenUrlStag() {
        return tokenUrlStag;
    }

    public void setTokenUrlStag(String tokenUrlStag) {
        this.tokenUrlStag = tokenUrlStag;
    }

    public String getClientIdProd() {
        return clientIdProd;
    }

    public void setClientIdProd(String clientIdProd) {
        this.clientIdProd = clientIdProd;
    }

    public String getClientIdStag() {
        return clientIdStag;
    }

    public void setClientIdStag(String clientIdStag) {
        this.clientIdStag = clientIdStag;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public void setUploadPath(String uploadPath) {
        this.uploadPath = uploadPath;
    }

    public boolean isSslVerificationEnabled() {
        return sslVerificationEnabled;
    }

    public void setSslVerificationEnabled(boolean sslVerificationEnabled) {
        this.sslVerificationEnabled = sslVerificationEnabled;
    }

    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    public void setMaxRetryAttempts(int maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
    }

    public long getRetryDelayMillis() {
        return retryDelayMillis;
    }

    public void setRetryDelayMillis(long retryDelayMillis) {
        this.retryDelayMillis = retryDelayMillis;
    }
}