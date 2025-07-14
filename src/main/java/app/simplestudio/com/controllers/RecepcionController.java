package app.simplestudio.com.controllers;// 1. MANTENER CONTROLADORES EXISTENTES COMO FACADE
// Los clientes siguen usando las mismas URLs y JSON, pero internamente usas la nueva arquitectura

import app.simplestudio.com.dto.DocumentoElectronicoResponse;
import app.simplestudio.com.service.DocumentoElectronicoService;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api-4.3")
public class RecepcionController {

  // ✅ NUEVA ARQUITECTURA INTERNA
  private final DocumentoElectronicoService documentoElectronicoService;

  public RecepcionController(DocumentoElectronicoService documentoElectronicoService) {
    this.documentoElectronicoService = documentoElectronicoService;
  }

  // ✅ MISMO ENDPOINT QUE ANTES - Sin cambios para clientes
  @PostMapping(value = "/recepcion",
      consumes = "application/json; charset=utf-8",
      produces = "application/json; charset=utf-8")
  public ResponseEntity<Map<String, Object>> recepcion(@RequestBody String jsonRequest) {
    try {
      log.info("Procesando recepción general - Usando nueva arquitectura");

      // ✅ INTERNAMENTE usa la nueva arquitectura
      DocumentoElectronicoResponse response = documentoElectronicoService
          .procesarDocumento(jsonRequest, "RECEPCION");

      // ✅ CONVERTIR a formato legacy para mantener compatibilidad
      Map<String, Object> legacyResponse = convertirAFormatoLegacy(response);

      return ResponseEntity.ok(legacyResponse);

    } catch (Exception e) {
      log.error("Error en recepción: {}", e.getMessage(), e);
      return ResponseEntity.status(500)
          .body(Map.of(
              "response", 500,
              "msj", "Error interno: " + e.getMessage()
          ));
    }
  }

  // ✅ MISMO ENDPOINT QUE ANTES - Para mensajes receptor
  @PostMapping(value = "/recepcion-mr",
      consumes = "application/json",
      produces = "application/json")
  public ResponseEntity<Map<String, Object>> recepcionMr(@RequestBody String jsonRequest) {
    try {
      log.info("Procesando mensaje receptor - Usando nueva arquitectura");

      // ✅ INTERNAMENTE usa la nueva arquitectura
      DocumentoElectronicoResponse response = documentoElectronicoService
          .procesarDocumento(jsonRequest, "MENSAJE_RECEPTOR");

      // ✅ CONVERTIR a formato legacy
      Map<String, Object> legacyResponse = convertirAFormatoLegacy(response);

      return ResponseEntity.ok(legacyResponse);

    } catch (Exception e) {
      log.error("Error en mensaje receptor: {}", e.getMessage(), e);
      return ResponseEntity.status(500)
          .body(Map.of(
              "response", 500,
              "msj", "Error interno: " + e.getMessage()
          ));
    }
  }

  // ✅ CONVERTIR response moderna a formato que esperan los clientes existentes
  private Map<String, Object> convertirAFormatoLegacy(DocumentoElectronicoResponse response) {
    Map<String, Object> legacyMap = new HashMap<>();
    legacyMap.put("response", response.response());
    legacyMap.put("msj", response.msj());

    if (response.clave() != null) {
      legacyMap.put("clave", response.clave());
    }
    if (response.consecutivo() != null) {
      legacyMap.put("consecutivo", response.consecutivo());
    }
    if (response.fechaEmision() != null) {
      legacyMap.put("fechaEmision", response.fechaEmision());
    }
    if (response.fileXmlSign() != null) {
      legacyMap.put("fileXmlSign", response.fileXmlSign());
    }

    return legacyMap;
  }
}