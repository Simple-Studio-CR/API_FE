package app.simplestudio.com.util;

import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HttpClientUtil {
    
    private static final Logger log = LoggerFactory.getLogger(HttpClientUtil.class);
    private static final int DEFAULT_TIMEOUT = 35000; // 35 segundos
    
    /**
     * Crea un cliente HTTP con configuración SSL permisiva
     */
    public CloseableHttpClient createHttpClient() throws Exception {
        return createHttpClient(DEFAULT_TIMEOUT);
    }
    
    /**
     * Crea un cliente HTTP con timeout personalizado
     */
    public CloseableHttpClient createHttpClient(int timeoutSeconds) throws Exception {
        SSLContext sslContext = new SSLContextBuilder()
            .loadTrustMaterial(null, (certificate, authType) -> true)
            .build();
        
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(timeoutSeconds * 1000)
            .setConnectionRequestTimeout(timeoutSeconds * 1000)
            .setSocketTimeout(timeoutSeconds * 1000)
            .build();
        
        return HttpClientBuilder.create()
            .setSSLContext(sslContext)
            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
            .setDefaultRequestConfig(config)
            .build();
    }
    
    /**
     * Ejecuta POST para obtener token OAuth
     */
    public String executeTokenRequest(String url, String username, String password, String clientId) throws Exception {
        try (CloseableHttpClient client = createHttpClient()) {
            HttpPost post = new HttpPost(url + "token");
            
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("grant_type", "password"));
            params.add(new BasicNameValuePair("client_id", clientId));
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));
            
            post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            
            try (CloseableHttpResponse response = client.execute(post)) {
                return EntityUtils.toString(response.getEntity());
            }
        }
    }
    
    /**
     * Ejecuta POST para refresh token
     */
    public String executeRefreshTokenRequest(String url, String refreshToken, String clientId) throws Exception {
        try (CloseableHttpClient client = createHttpClient()) {
            HttpPost post = new HttpPost(url + "token");
            
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("grant_type", "refresh_token"));
            params.add(new BasicNameValuePair("client_id", clientId));
            params.add(new BasicNameValuePair("refresh_token", refreshToken));
            
            post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            
            try (CloseableHttpResponse response = client.execute(post)) {
                return EntityUtils.toString(response.getEntity());
            }
        }
    }
    
    /**
     * Ejecuta POST con JSON y Bearer token
     */
    public CloseableHttpResponse executePostWithBearer(String url, String jsonContent, String bearerToken) throws Exception {
        CloseableHttpClient client = createHttpClient();
        HttpPost post = new HttpPost(url);
        
        post.setHeader("Content-Type", "application/json");
        post.setHeader("Authorization", "Bearer " + bearerToken);
        post.setEntity(new StringEntity(jsonContent, "UTF-8"));
        
        return client.execute(post);
    }
    
    /**
     * Ejecuta GET con Bearer token
     */
    public CloseableHttpResponse executeGetWithBearer(String url, String bearerToken) throws Exception {
        CloseableHttpClient client = createHttpClient();
        HttpGet get = new HttpGet(url);
        
        get.setHeader("Authorization", "Bearer " + bearerToken);
        
        return client.execute(get);
    }
    
    /**
     * Extrae el contenido de respuesta como String
     */
    public String extractResponseContent(HttpResponse response) throws Exception {
        return EntityUtils.toString(response.getEntity());
    }
}