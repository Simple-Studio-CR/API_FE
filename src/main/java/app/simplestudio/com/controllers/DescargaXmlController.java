package app.simplestudio.com.controllers;

import jakarta.annotation.Resource;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping({"/api-4.4"})
public class DescargaXmlController {
  @Value("${path.upload.files.api}")
  private String pathUploadFilesApi;

  @GetMapping({"get-xml/{identificacion}/{filename:.+}"})
  public ResponseEntity<?> getXmlEnviado(@PathVariable String identificacion, @PathVariable String filename) {
    UrlResource urlResource;
    String path = this.pathUploadFilesApi;
    Map<String, Object> response = new HashMap<>();
    try {
      urlResource = new UrlResource(Paths.get(path).resolve(identificacion + '/' + filename).toAbsolutePath().toUri());
      System.out.println("===================== " + urlResource);
      if (!urlResource.exists() || !urlResource.isReadable()) {
        response.put("resp", "401");
        response.put("error", "Documento no existe");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
      }
    } catch (MalformedURLException e) {
      response.put("resp", "401");
      response.put("error", e.getMessage());
      return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    return ResponseEntity.ok()
        .header("Content-Disposition", new String[] { "attachment; filename=\"" + urlResource.getFilename() + "\"" })
        .header("Content-Type", new String[] { "application/octet-stream" }) // LÍNEA AGREGADA
        .body(urlResource);
  }
}

