package app.simplestudio.com.service.impl;

import app.simplestudio.com.config.StorageProperties;
import app.simplestudio.com.service.IStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class S3StorageServiceImpl implements IStorageService {
    
    private static final Logger log = LoggerFactory.getLogger(S3StorageServiceImpl.class);
    
    @Autowired
    private S3Client s3Client;
    
    @Autowired
    private StorageProperties storageProperties;
    
    @Override
    public String uploadFile(String key, InputStream inputStream, String contentType) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(storageProperties.getBucket())
                    .key(key)
                    .contentType(contentType)
                    .build();
            
            s3Client.putObject(putRequest, RequestBody.fromInputStream(inputStream, inputStream.available()));
            
            log.info("Archivo subido exitosamente a S3: {}", key);
            return buildUrl(key);
            
        } catch (Exception e) {
            log.error("Error subiendo archivo a S3: {}", key, e);
            throw new RuntimeException("Error subiendo archivo a S3: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String uploadFile(String key, String content, String contentType) {
        try (InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            return uploadFile(key, inputStream, contentType);
        } catch (IOException e) {
            log.error("Error convirtiendo string a InputStream para: {}", key, e);
            throw new RuntimeException("Error procesando contenido: " + e.getMessage(), e);
        }
    }
    
    @Override
    public InputStream downloadFile(String key) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(storageProperties.getBucket())
                    .key(key)
                    .build();
            
            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getRequest);
            log.info("Archivo descargado exitosamente de S3: {}", key);
            return s3Object;
            
        } catch (NoSuchKeyException e) {
            log.warn("Archivo no encontrado en S3: {}", key);
            return null;
        } catch (Exception e) {
            log.error("Error descargando archivo de S3: {}", key, e);
            throw new RuntimeException("Error descargando archivo de S3: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String downloadFileAsString(String key) {
        try (InputStream inputStream = downloadFile(key)) {
            if (inputStream == null) {
                return null;
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error leyendo archivo como string: {}", key, e);
            throw new RuntimeException("Error leyendo archivo: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean fileExists(String key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(storageProperties.getBucket())
                    .key(key)
                    .build();
            
            s3Client.headObject(headRequest);
            return true;
            
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Error verificando existencia de archivo: {}", key, e);
            return false;
        }
    }
    
    @Override
    public boolean deleteFile(String key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(storageProperties.getBucket())
                    .key(key)
                    .build();
            
            s3Client.deleteObject(deleteRequest);
            log.info("Archivo eliminado exitosamente de S3: {}", key);
            return true;
            
        } catch (Exception e) {
            log.error("Error eliminando archivo de S3: {}", key, e);
            return false;
        }
    }
    
    @Override
    public List<String> listFiles(String prefix) {
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(storageProperties.getBucket())
                    .prefix(prefix)
                    .build();
            
            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
            
            return listResponse.contents().stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error listando archivos en S3 con prefijo: {}", prefix, e);
            return List.of();
        }
    }

    @Override
    public String buildKey(String emisorId, String filename) {
        return String.format("%s/%s/%s",
                storageProperties.getPrefix(), emisorId, filename);
    }
    
    private String buildUrl(String key) {
        return String.format("%s/%s/%s", 
                storageProperties.getEndpoint(), storageProperties.getBucket(), key);
    }
}