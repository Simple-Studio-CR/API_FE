// src/main/java/app/simplestudio/com/dto/ConsultarDocumentoResponse.java
package app.simplestudio.com.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConsultaDocumentoResponse {

    /**
     * Código de respuesta HTTP interno (200, 401, etc).
     */
    private int response;

    /**
     * Clave única del documento.
     */
    private String clave;

    /**
     * Fecha de emisión reportada por Hacienda.
     */
    private String fecha;

    /**
     * Indicador de estado (“procesando”, “aceptado”, “rechazado”, …).
     */
    private String indEstado;

    /**
     * Si está disponible, el XML de respuesta (o su mensaje extraído).
     */
    private String respuestaXml;
}