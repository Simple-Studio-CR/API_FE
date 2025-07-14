// ==================== Processor para Factura Electrónica de Compra (ACTUALIZADO) ====================
package app.simplestudio.com.service.processor.impl;

import app.simplestudio.com.dto.FacturaRequestDTO;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.service.ConsecutivoService;
import app.simplestudio.com.service.IComprobantesElectronicosService;
import app.simplestudio.com.service.IEmisorService;
import app.simplestudio.com.service.IFacturaService;
import app.simplestudio.com.service.builder.FacturaBuilder;
import app.simplestudio.com.service.generator.XmlGeneratorService;
import app.simplestudio.com.service.processor.BaseDocumentProcessor;
import app.simplestudio.com.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component("fecProcessor")
public class FacturaElectronicaCompraProcessor extends BaseDocumentProcessor {

    public FacturaElectronicaCompraProcessor(
        IEmisorService emisorService,
        IFacturaService facturaService,
        IComprobantesElectronicosService comprobantesService,
        ConsecutivoService consecutivoService,
        FacturaBuilder facturaBuilder,
        XmlGeneratorService xmlGeneratorService) {
        super(emisorService, facturaService, comprobantesService,
            consecutivoService, facturaBuilder, xmlGeneratorService);
    }

    @Override
    protected void procesarEspecifico(FacturaRequestDTO request,
        Emisor emisor,
        String clave,
        String consecutivo) {
        log.info("Procesamiento específico para Factura Electrónica de Compra");

        // 1. Validar que es para régimen simplificado o sector agropecuario
        validarUsoFEC(request);

        // 2. Validar que tenga referencia (automáticamente agregada en algunos casos)
        validarReferencias(request);

        // 3. Validar datos completos del proveedor (que viene como receptor)
        validarDatosProveedor(request);

        // 4. Validar ubicación completa (provincia, cantón, distrito obligatorios)
        validarUbicacionCompleta(request, emisor);
    }

    private void validarUsoFEC(FacturaRequestDTO request) {
        // Mensaje informativo sobre el uso correcto de FEC
        log.warn("FEC generada - Verificar que se use solo para Régimen Simplificado o Sector Agropecuario");

        // Aquí se podrían agregar validaciones específicas del emisor
        // Por ejemplo, verificar que el emisor esté autorizado para emitir FEC
    }

    private void validarReferencias(FacturaRequestDTO request) {
        // FEC generalmente requiere referencia a la factura del proveedor
        if (request.referencias() == null || request.referencias().isEmpty()) {
            log.warn("FEC sin referencias - Considerar agregar referencia a factura del proveedor");
        }
    }

    private void validarDatosProveedor(FacturaRequestDTO request) {
        // En FEC, el proveedor viene como "receptor" en el request
        if (request.receptorNombre() == null || request.receptorNombre().isEmpty()) {
            throw new ValidationException("El nombre del proveedor es requerido para FEC.");
        }

        if (request.receptorTipoIdentif() == null || request.receptorTipoIdentif().isEmpty()) {
            throw new ValidationException("El tipo de identificación del proveedor es requerido para FEC.");
        }

        if (request.receptorNumIdentif() == null || request.receptorNumIdentif().isEmpty()) {
            throw new ValidationException("La identificación del proveedor es requerida para FEC.");
        }
    }

    private void validarUbicacionCompleta(FacturaRequestDTO request, Emisor emisor) {
        // Para FEC, tanto el emisor como el receptor necesitan ubicación completa

        // Validar ubicación del emisor
        if (emisor.getProvincia() == null || emisor.getCanton() == null ||
            emisor.getDistrito() == null || emisor.getOtrasSenas() == null) {
            throw new ValidationException(
                "Campo provincia, cantón, distrito y otras señas del emisor son requeridos para la Factura de Compra."
            );
        }

        // Validar ubicación del proveedor (receptor en el request)
        if (request.receptorProvincia() == null || request.receptorProvincia().isEmpty()) {
            throw new ValidationException("Provincia del proveedor es requerida para FEC.");
        }

        if (request.receptorCanton() == null || request.receptorCanton().isEmpty()) {
            throw new ValidationException("Cantón del proveedor es requerido para FEC.");
        }

        if (request.receptorDistrito() == null || request.receptorDistrito().isEmpty()) {
            throw new ValidationException("Distrito del proveedor es requerido para FEC.");
        }

        if (request.receptorOtrasSenas() == null || request.receptorOtrasSenas().isEmpty()) {
            throw new ValidationException("Otras señas del proveedor son requeridas para FEC.");
        }
    }

    @Override
    public boolean puedeProceser(String tipoDocumento) {
        return "FEC".equals(tipoDocumento);
    }
}

