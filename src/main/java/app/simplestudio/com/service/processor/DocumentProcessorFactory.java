// ==================== DocumentProcessorFactory Interface ====================
package app.simplestudio.com.service.processor;

/**
 * Factory para obtener el processor adecuado según el tipo de documento
 */
public interface DocumentProcessorFactory {
    
    /**
     * Obtiene el processor específico para el tipo de documento
     * @param tipoDocumento Tipo de documento (FE, TE, NC, ND, etc.)
     * @return Processor específico o genérico si no existe uno específico
     */
    DocumentProcessor getProcessor(String tipoDocumento);
}