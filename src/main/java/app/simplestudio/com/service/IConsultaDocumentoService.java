// src/main/java/app/simplestudio/com/service/IConsultaDocumentoService.java
package app.simplestudio.com.service;


import app.simplestudio.com.dto.ConsultaDocumentoRequest;
import app.simplestudio.com.dto.ConsultaDocumentoResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface IConsultaDocumentoService {

    /**
     * Consulta el estado de un comprobante cualquiera en Hacienda.
     */
    ConsultaDocumentoResponse consultarDocumento(ConsultaDocumentoRequest req)
        throws JsonProcessingException;

    /**
     * Consulta el estado de un comprobante externo (decodifica Base64 y extrae MensajeHacienda).
     */
    ConsultaDocumentoResponse consultarDocumentoExterno(ConsultaDocumentoRequest req)
        throws JsonProcessingException;
}