package app.simplestudio.com.mh.http;

import app.simplestudio.com.mh.config.MHConfigurationProperties;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class MHHttpClientService {
    
    private static final Logger log = LoggerFactory.getLogger(MHHttpClientService.class);
    private final MHConfigurationProperties config;
    
    public MHHttpClientService(MHConfigurationProperties config) {
        this.config = config;
    }
    
    /**
     * Crea un cliente HTTP configurado para conectarse al MH
     */
    public CloseableHttpClient createHttpClient() {
        try {
            HttpClientBuilder builder = HttpClients.custom();
            
            if (!config.isSslVerificationEnabled()) {
                SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, (certificate, authType) -> true)
                    .build();
                    
                builder.setSSLContext(sslContext)
                       .setSSLHostnameVerifier(new NoopHostnameVerifier());
            }
            
            return builder.build();
            
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            log.error("Error configurando SSL context para cliente HTTP", e);
            return HttpClientBuilder.create().build();
        }
    }
    
    /**
     * Crea configuración de request con timeouts
     */
    public RequestConfig createRequestConfig() {
        return RequestConfig.custom()
            .setConnectTimeout(config.getTimeoutInMillis())
            .setConnectionRequestTimeout(config.getTimeoutInMillis())
            .setSocketTimeout(config.getTimeoutInMillis())
            .build();
    }
    
    /**
     * Ejecuta un POST con form data
     */
    public HttpResponse executeFormPost(String url, List<NameValuePair> parameters, 
                                       String bearerToken) throws IOException {
        try (CloseableHttpClient client = createHttpClient()) {
            HttpPost request = new HttpPost(url);
            
            // Headers
            request.addHeader("content-type", "application/x-www-form-urlencoded");
            if (bearerToken != null) {
                request.addHeader("Authorization", "bearer " + bearerToken);
            }
            
            // Body
            request.setEntity(new UrlEncodedFormEntity(parameters));
            
            // Config
            request.setConfig(createRequestConfig());
            
            long startTime = System.currentTimeMillis();
            HttpResponse response = client.execute(request);
            long duration = System.currentTimeMillis() - startTime;
            
            log.debug("POST a {} completado en {}ms", url, duration);
            return response;
        }
    }
    
    /**
     * Ejecuta un POST con JSON
     */
    public HttpResponse executeJsonPost(String url, String jsonBody, 
                                       String bearerToken) throws IOException {
        try (CloseableHttpClient client = createHttpClient()) {
            HttpPost request = new HttpPost(url);
            
            // Headers
            request.addHeader("content-type", "application/javascript");
            if (bearerToken != null) {
                request.addHeader("Authorization", "bearer " + bearerToken);
            }
            
            // Body
            StringEntity params = new StringEntity(jsonBody);
            request.setEntity(params);
            
            // Config
            request.setConfig(createRequestConfig());
            
            long startTime = System.currentTimeMillis();
            HttpResponse response = client.execute(request);
            long duration = System.currentTimeMillis() - startTime;
            
            log.debug("POST JSON a {} completado en {}ms", url, duration);
            return response;
        }
    }
    
    /**
     * Ejecuta un GET
     */
    public HttpResponse executeGet(String url, String bearerToken) throws IOException {
        try (CloseableHttpClient client = createHttpClient()) {
            HttpGet request = new HttpGet(url);
            
            // Headers
            if (bearerToken != null) {
                request.addHeader("Authorization", "bearer " + bearerToken);
            }
            
            // Config
            request.setConfig(createRequestConfig());
            
            long startTime = System.currentTimeMillis();
            HttpResponse response = client.execute(request);
            long duration = System.currentTimeMillis() - startTime;
            
            log.debug("GET a {} completado en {}ms", url, duration);
            return response;
        }
    }
    
    /**
     * Extrae el contenido de una respuesta HTTP como String
     */
    public String extractResponseBody(HttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, "UTF-8");
    }
    
    /**
     * Convierte headers a string para logging
     */
    public String formatHeaders(Header[] headers) {
        StringBuilder sb = new StringBuilder();
        for (Header header : headers) {
            sb.append(header.getName())
              .append(": ")
              .append(header.getValue())
              .append("; ");
        }
        return sb.toString();
    }
    
    /**
     * Valida si una respuesta HTTP fue exitosa
     */
    public boolean isSuccessResponse(HttpResponse response) {
        int statusCode = response.getStatusLine().getStatusCode();
        return statusCode >= 200 && statusCode < 300;
    }
    
    /**
     * Obtiene el código de estado de la respuesta
     */
    public int getStatusCode(HttpResponse response) {
        return response.getStatusLine().getStatusCode();
    }
}