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

    public boolean directoryExists(String directoryPath) {
        if (directoryPath == null || directoryPath.trim().isEmpty()) {
            log.warn("Ruta de directorio es null o vacía");
            return false;
        }

        try {
            File directory = new File(directoryPath.trim());
            boolean exists = directory.exists() && directory.isDirectory();

            log.debug("Verificación directorio [{}]: {}", directoryPath, exists ? "Existe" : "No existe");
            return exists;

        } catch (Exception e) {
            log.error("Error verificando existencia del directorio [{}]: {}", directoryPath, e.getMessage());
            return false;
        }
    }
}