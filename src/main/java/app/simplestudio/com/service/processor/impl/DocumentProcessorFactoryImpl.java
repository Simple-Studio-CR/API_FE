// ==================== DocumentProcessorFactory Mejorado ====================
package app.simplestudio.com.service.processor.impl;

import app.simplestudio.com.service.processor.DocumentProcessor;
import app.simplestudio.com.service.processor.DocumentProcessorFactory;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DocumentProcessorFactoryImpl implements DocumentProcessorFactory {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DocumentProcessorFactoryImpl.class);

    private final Map<String, DocumentProcessor> processorMap;
    private final DocumentProcessor fallbackProcessor;

    public DocumentProcessorFactoryImpl(List<DocumentProcessor> processors) {
        // Crear mapa de processors específicos
        this.processorMap = processors.stream()
            .filter(processor -> !processor.getClass().getSimpleName().contains("Generic"))
            .collect(Collectors.toMap(
                this::determineKey,
                Function.identity()
            ));

        // Encontrar processor genérico como fallback
        this.fallbackProcessor = processors.stream()
            .filter(processor -> processor.getClass().getSimpleName().contains("Generic"))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No se encontró processor genérico"));
    }

    @Override
    public DocumentProcessor getProcessor(String tipoDocumento) {
        // Intentar encontrar processor específico primero
        DocumentProcessor specificProcessor = findSpecificProcessor(tipoDocumento);
        if (specificProcessor != null) {
            return specificProcessor;
        }

        // Si no existe processor específico, usar el genérico
        log.warn("Usando processor genérico para tipo de documento: {}", tipoDocumento);
        return fallbackProcessor;
    }

    private DocumentProcessor findSpecificProcessor(String tipoDocumento) {
        return processorMap.values().stream()
            .filter(processor -> processor.puedeProceser(tipoDocumento))
            .findFirst()
            .orElse(null);
    }

    private String determineKey(DocumentProcessor processor) {
        return processor.getClass().getSimpleName().toLowerCase();
    }
}