package app.simplestudio.com.controllers;

import app.simplestudio.com.dto.DocumentoElectronicoResponse;
import app.simplestudio.com.dto.FacturaRequestDTO;
import app.simplestudio.com.service.DocumentoElectronicoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// ==================== PREPARACIÓN PARA API 4.4 ====================
@RestController
@RequestMapping("/api-4.4")  // ✅ NUEVA VERSIÓN PARA HACIENDA 4.4
public class DocumentoElectronicoV44Controller {

  private final DocumentoElectronicoService documentoElectronicoService;

  public DocumentoElectronicoV44Controller(DocumentoElectronicoService documentoElectronicoService) {
    this.documentoElectronicoService = documentoElectronicoService;
  }

  // ✅ ENDPOINTS MODERNOS para nuevos clientes que quieran migrar
  @PostMapping("/documentos")
  public ResponseEntity<DocumentoElectronicoResponse> procesarDocumento(
      @RequestBody FacturaRequestDTO request) {

    DocumentoElectronicoResponse response = documentoElectronicoService.procesarDocumento(request);

    return response.response() == 200
        ? ResponseEntity.ok(response)
        : ResponseEntity.status(response.response()).body(response);
  }

  // ✅ ENDPOINT específico para diferentes tipos de documento en v4.4
  @PostMapping("/facturas")
  public ResponseEntity<DocumentoElectronicoResponse> procesarFactura(@RequestBody String jsonRequest) {
    return procesarDocumentoInterno(jsonRequest, "FACTURA_V44");
  }

  @PostMapping("/notas-debito-credito")
  public ResponseEntity<DocumentoElectronicoResponse> procesarNotasDebitoCredito(@RequestBody String jsonRequest) {
    return procesarDocumentoInterno(jsonRequest, "ND_NC_V44");
  }

  private ResponseEntity<DocumentoElectronicoResponse> procesarDocumentoInterno(String jsonRequest, String flujo) {
    DocumentoElectronicoResponse response = documentoElectronicoService.procesarDocumento(jsonRequest, flujo);
    return response.response() == 200
        ? ResponseEntity.ok(response)
        : ResponseEntity.status(response.response()).body(response);
  }
}