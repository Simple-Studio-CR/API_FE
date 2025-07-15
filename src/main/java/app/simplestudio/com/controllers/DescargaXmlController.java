package app.simplestudio.com.controllers;

import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping({"/api-4.3"})
public class DescargaXmlController {
  @Value("${path.upload.files.api}")
  private String pathUploadFilesApi;
  
  @GetMapping({"get-xml/{identificacion}/{filename:.+}"})
  public ResponseEntity<?> getXmlEnviado(@PathVariable String identificacion, @PathVariable String filename) {
    UrlResource urlResource;
    Resource recurso = null;
    String path = this.pathUploadFilesApi;
    Map<String, Object> response = new HashMap<>();
    try {
      urlResource = new UrlResource(Paths.get(path, new String[0]).resolve(identificacion + '/' + filename).toAbsolutePath().toUri());
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
    return ((ResponseEntity.BodyBuilder)ResponseEntity.ok()
      .header("Content-Disposition", new String[] { "attachment; filename=\"" + urlResource.getFilename() + "\"" })).body(urlResource);
  }
}

