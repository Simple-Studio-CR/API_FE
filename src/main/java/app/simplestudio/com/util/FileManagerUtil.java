package app.simplestudio.com.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FileManagerUtil {
    
    private static final Logger log = LoggerFactory.getLogger(FileManagerUtil.class);
    
    /**
     * Guarda contenido en archivo
     */
    public void saveToFile(String filePath, String content) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, StandardCharsets.UTF_8))) {
            writer.write(content);
            writer.flush();
        }
    }
    
    /**
     * Lee contenido de archivo
     */
    public String readFromFile(String filePath) throws Exception {
        return FileUtils.readFileToString(new File(filePath), StandardCharsets.UTF_8);
    }
    
    /**
     * Convierte archivo a Base64
     */
    public String fileToBase64(String filePath) throws Exception {
        byte[] fileBytes = FileUtils.readFileToByteArray(new File(filePath));
        return Base64.encodeBase64String(fileBytes);
    }
    
    /**
     * Convierte Base64 a archivo
     */
    public void base64ToFile(String base64Content, String filePath) throws Exception {
        byte[] decodedBytes = Base64.decodeBase64(base64Content);
        FileUtils.writeByteArrayToFile(new File(filePath), decodedBytes);
    }
    
    /**
     * Verifica si el archivo existe
     */
    public boolean fileExists(String filePath) {
        return new File(filePath).exists();
    }
    
    /**
     * Crea directorio si no existe
     */
    public void createDirectoryIfNotExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                log.info("Directorio creado: {}", directoryPath);
            } else {
                log.warn("No se pudo crear el directorio: {}", directoryPath);
            }
        }
    }
    
    /**
     * Elimina archivo
     */
    public boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }
    
    /**
     * Obtiene el tamaño del archivo en bytes
     */
    public long getFileSize(String filePath) {
        File file = new File(filePath);
        return file.exists() ? file.length() : 0;
    }
}