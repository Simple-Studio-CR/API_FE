package snn.soluciones.com.controllers;

import snn.soluciones.com.service.ReportService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping({"/api-4.3"})
@Slf4j
public class ImpresionController {

  private final ReportService reportService;

  public ImpresionController(ReportService reportService) {
    this.reportService = reportService;
    log.info("ImpresionController initialized");
  }

  /**
   * Genera y retorna el PDF de la factura identificado por la clave.
   *
   * @param clave clave de 50 caracteres de la factura
   * @return ResponseEntity con el PDF como recurso descargable
   */
  @GetMapping("/imprimir-factura/{clave}")
  public ResponseEntity<?> imprimirFactura(@PathVariable String clave) {
    log.info("Received request to print factura with clave={}", clave);
    try {
      byte[] pdfBytes = reportService.generateFacturaPdf(clave);
      if (pdfBytes == null || pdfBytes.length == 0) {
        log.warn("Report generation returned empty PDF for clave={}", clave);
        return ResponseEntity.noContent().build();
      }

      ByteArrayResource resource = new ByteArrayResource(pdfBytes);
      String filename = clave + ".pdf";
      log.info("Serving PDF for clave={} ({} bytes)", clave, pdfBytes.length);

      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_PDF)
          .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
          .body(resource);
    } catch (IllegalArgumentException e) {
      log.error("Invalid request for clave={}: {}", clave, e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error generating PDF for clave={}", clave, e);
      return ResponseEntity.status(500).build();
    }
  }
}