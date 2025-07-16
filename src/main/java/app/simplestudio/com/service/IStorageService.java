package app.simplestudio.com.service;

import java.io.InputStream;
import java.util.List;

public interface IStorageService {
    
    /**
     * Sube un archivo a S3
     * @param key - Ruta del archivo en S3 (ej: "XmlClientes/114970286/xml/signed/factura.xml")
     * @param inputStream - Contenido del archivo
     * @param contentType - Tipo de contenido (ej: "application/xml")
     * @return URL del archivo subido
     */
    String uploadFile(String key, InputStream inputStream, String contentType);
    
    /**
     * Sube un archivo a S3 desde contenido String
     * @param key - Ruta del archivo en S3
     * @param content - Contenido como String
     * @param contentType - Tipo de contenido
     * @return URL del archivo subido
     */
    String uploadFile(String key, String content, String contentType);
    
    /**
     * Descarga un archivo de S3
     * @param key - Ruta del archivo en S3
     * @return InputStream del archivo
     */
    InputStream downloadFile(String key);
    
    /**
     * Descarga un archivo de S3 como String
     * @param key - Ruta del archivo en S3
     * @return Contenido del archivo como String
     */
    String downloadFileAsString(String key);
    
    /**
     * Verifica si un archivo existe
     * @param key - Ruta del archivo en S3
     * @return true si existe, false si no
     */
    boolean fileExists(String key);
    
    /**
     * Elimina un archivo
     * @param key - Ruta del archivo en S3
     * @return true si se eliminó exitosamente
     */
    boolean deleteFile(String key);
    
    /**
     * Lista archivos en un prefijo/carpeta
     * @param prefix - Prefijo/carpeta (ej: "XmlClientes/114970286/xml/")
     * @return Lista de keys de archivos
     */
    List<String> listFiles(String prefix);
    
    /**
     * Construye la key completa para el storage
     * @param emisorId - ID del emisor
     * @param folder - Carpeta (xml, cert, reports)
     * @param subfolder - Subcarpeta (signed, responses)
     * @param filename - Nombre del archivo
     * @return Key completa para S3
     */
    String buildKey(String emisorId, String folder, String subfolder, String filename);
    
    /**
     * Construye la key completa para el storage (sin subcarpeta)
     * @param emisorId - ID del emisor  
     * @param folder - Carpeta
     * @param filename - Nombre del archivo
     * @return Key completa para S3
     */
    String buildKey(String emisorId, String folder, String filename);
}