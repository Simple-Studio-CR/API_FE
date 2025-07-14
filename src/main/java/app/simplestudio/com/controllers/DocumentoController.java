// src/main/java/app/simplestudio/com/controllers/DocumentoController.java
package app.simplestudio.com.controllers;

import app.simplestudio.com.dto.ConsultaDocumentoRequest;
import app.simplestudio.com.dto.ConsultaDocumentoResponse;
import app.simplestudio.com.service.IConsultaDocumentoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api-4.3")
@RequiredArgsConstructor
public class DocumentoController {

    private final IConsultaDocumentoService consultaService;

    @PostMapping("/consultar-documentos")
    public ResponseEntity<ConsultaDocumentoResponse> consultar(@RequestBody @Valid ConsultaDocumentoRequest req)
        throws JsonProcessingException {
        var resp = consultaService.consultarDocumento(req);
        return ResponseEntity.status(resp.getResponse()).body(resp);
    }

    @PostMapping("/consultar-documentos-externos")
    public ResponseEntity<ConsultaDocumentoResponse> consultarExterno(@RequestBody @Valid ConsultaDocumentoRequest req)
        throws JsonProcessingException {
        var resp = consultaService.consultarDocumentoExterno(req);
        return ResponseEntity.status(resp.getResponse()).body(resp);
    }
}