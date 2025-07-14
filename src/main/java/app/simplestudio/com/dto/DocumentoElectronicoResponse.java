// ==================== Response DTO ====================
package app.simplestudio.com.dto;

/**
 * Response para el procesamiento de documentos electrónicos
 */
public record DocumentoElectronicoResponse(
    int response,
    String clave,
    String consecutivo,
    String fechaEmision,
    String fileXmlSign,
    String msj
) {
  // Factory methods para respuestas comunes
  public static DocumentoElectronicoResponse success(String clave, String consecutivo,
      String fechaEmision, String fileXmlSign) {
    return new DocumentoElectronicoResponse(200, clave, consecutivo, fechaEmision, fileXmlSign, null);
  }

  public static DocumentoElectronicoResponse error(int code, String mensaje) {
    return new DocumentoElectronicoResponse(code, null, null, null, null, mensaje);
  }
}