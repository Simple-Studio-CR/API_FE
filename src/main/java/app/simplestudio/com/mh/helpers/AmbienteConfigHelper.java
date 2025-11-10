package app.simplestudio.com.mh.helpers;

import app.simplestudio.com.models.entity.Emisor;

/**
 * Helper para configurar el ambiente de los servicios de Hacienda
 */
public class AmbienteConfigHelper {
    
    public final String endpoint;
    public final String urlToken;
    public final String clientId;
    public final String username;
    public final String password;
    public final String certificado;
    public final String keyCertificado;
    
    public AmbienteConfigHelper(Emisor emisor, 
                               String endpointProd, 
                               String endpointStag,
                               String tokenProd, 
                               String tokenStag,
                               String pathUploadFilesApi) {
        
        boolean isProd = "prod".equals(emisor.getAmbiente());
        
        this.endpoint = isProd ? endpointProd : endpointStag;
        this.urlToken = isProd ? tokenProd : tokenStag;
        this.clientId = isProd ? "api-prod" : "api-stag";
        this.username = emisor.getUserApi();
        this.password = emisor.getPwApi();
        this.certificado = pathUploadFilesApi + "/" + emisor.getIdentificacion() + "/cert/" + emisor.getCertificado();
        this.keyCertificado = emisor.getPingApi();
    }
}