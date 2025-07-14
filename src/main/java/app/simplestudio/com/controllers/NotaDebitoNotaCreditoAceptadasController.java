package app.simplestudio.com.controllers;

import app.simplestudio.com.dto.DocumentoElectronicoResponse;
import app.simplestudio.com.service.DocumentoElectronicoService;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// ==================== CONTROLADOR ESPECÍFICO PARA ND/NC ====================
@RestController
@RequestMapping("/api-4.3")
public class NotaDebitoNotaCreditoAceptadasController {

  private static final Logger log = LoggerFactory.getLogger(NotaDebitoNotaCreditoAceptadasController.class);

  // ✅ NUEVA ARQUITECTURA INTERNA
  private final DocumentoElectronicoService documentoElectronicoService;

  public NotaDebitoNotaCreditoAceptadasController(DocumentoElectronicoService documentoElectronicoService) {
    this.documentoElectronicoService = documentoElectronicoService;
  }

  // ✅ MISMO ENDPOINT QUE ANTES - Sin cambios para clientes
  @PostMapping(value = "/recepcion-nd-nc",
      consumes = "application/json; charset=utf-8",
      produces = "application/json; charset=utf-8")
  public ResponseEntity<Map<String, Object>> recepcionNdNc(@RequestBody String jsonRequest) {
    try {
      log.info("Procesando ND/NC aceptadas - Usando nueva arquitectura");

      // ✅ INTERNAMENTE usa la nueva arquitectura con validaciones específicas
      DocumentoElectronicoResponse response = documentoElectronicoService
          .procesarDocumento(jsonRequest, "ND_NC_ACEPTADAS");

      // ✅ CONVERTIR a formato legacy para mantener compatibilidad
      Map<String, Object> legacyResponse = convertirAFormatoLegacy(response);

      return ResponseEntity.ok(legacyResponse);

    } catch (Exception e) {
      log.error("Error en ND/NC: {}", e.getMessage(), e);
      return ResponseEntity.status(500)
          .body(Map.of(
              "response", 500,
              "msj", "Error interno: " + e.getMessage()
          ));
    }
  }

  // ✅ MÉTODO HELPER PARA MANTENER COMPATIBILIDAD
  private Map<String, Object> convertirAFormatoLegacy(DocumentoElectronicoResponse response) {
    // Mismo método que en RecepcionController - podrías extraer a una utilidad
    Map<String, Object> legacyMap = new HashMap<>();
    legacyMap.put("response", response.response());
    legacyMap.put("msj", response.msj());

    if (response.clave() != null) legacyMap.put("clave", response.clave());
    if (response.consecutivo() != null) legacyMap.put("consecutivo", response.consecutivo());
    if (response.fechaEmision() != null) legacyMap.put("fechaEmision", response.fechaEmision());
    if (response.fileXmlSign() != null) legacyMap.put("fileXmlSign", response.fileXmlSign());

    return legacyMap;
  }
}