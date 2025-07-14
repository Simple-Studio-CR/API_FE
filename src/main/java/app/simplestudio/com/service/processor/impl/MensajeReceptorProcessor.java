// ==================== Processor para Mensajes de Receptor ====================
package app.simplestudio.com.service.processor.impl;

import app.simplestudio.com.dto.FacturaRequestDTO;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.models.entity.MensajeReceptor;
import app.simplestudio.com.service.ConsecutivoService;
import app.simplestudio.com.service.IComprobantesElectronicosService;
import app.simplestudio.com.service.IEmisorService;
import app.simplestudio.com.service.IFacturaService;
import app.simplestudio.com.service.IMensajeReceptorService;
import app.simplestudio.com.service.builder.FacturaBuilder;
import app.simplestudio.com.service.generator.XmlGeneratorService;
import app.simplestudio.com.service.processor.BaseDocumentProcessor;
import org.springframework.stereotype.Component;

@Component("mensajeReceptorProcessor")
public class MensajeReceptorProcessor extends BaseDocumentProcessor {

    private final IMensajeReceptorService mensajeReceptorService;

    public MensajeReceptorProcessor(
        IEmisorService emisorService,
        IFacturaService facturaService,
        IComprobantesElectronicosService comprobantesService,
        ConsecutivoService consecutivoService,
        FacturaBuilder facturaBuilder,
        XmlGeneratorService xmlGeneratorService,
        IMensajeReceptorService mensajeReceptorService) {
        super(emisorService, facturaService, comprobantesService,
            consecutivoService, facturaBuilder, xmlGeneratorService);
        this.mensajeReceptorService = mensajeReceptorService;
    }

    @Override
    protected void procesarEspecifico(FacturaRequestDTO request,
        Emisor emisor,
        String clave,
        String consecutivo) {
        log.info("Procesamiento específico para Mensaje Receptor tipo: {}", request.tipoDocumento());

        // Crear y guardar mensaje receptor
        MensajeReceptor mensajeReceptor = construirMensajeReceptor(request, emisor, clave, consecutivo);
        mensajeReceptorService.save(mensajeReceptor);
    }

    private MensajeReceptor construirMensajeReceptor(FacturaRequestDTO request, Emisor emisor, 
                                                   String clave, String consecutivo) {
        MensajeReceptor mr = new MensajeReceptor();
        
        mr.setClave(clave);
        mr.setTipoDocumento(request.tipoDocumento());
        mr.setNumeroCedulaEmisor(request.receptorTipoIdentif());
        mr.setFechaEmisionDoc(request.fechaEmisionCliente());
        
        // Mapear tipo de mensaje
        String mensaje = switch (request.tipoDocumento()) {
            case "CCE" -> "1";   // Aceptación total
            case "CPCE" -> "2";  // Aceptación parcial  
            case "RCE" -> "3";   // Rechazo total
            default -> throw new IllegalArgumentException("Tipo de mensaje no válido: " + request.tipoDocumento());
        };
        mr.setMensaje(mensaje);
        
        mr.setDetalleMensaje(request.detalleMensaje());
        mr.setCodigoActividad(request.codigoActividadEmisor());
        mr.setCondicionImpuesto(request.condicionImpuesto());
        mr.setMontoTotalImpuestoAcreditar(
            request.montoTotalImpuestoAcreditar() != null ? 
            request.montoTotalImpuestoAcreditar().toString() : "0.00");
        mr.setMontoTotalDeGastoAplicable(
            request.montoTotalDeGastoAplicable() != null ? 
            request.montoTotalDeGastoAplicable().toString() : "0.00");
        mr.setMontoTotalImpuesto(String.valueOf(request.montoTotalImpuesto()));
        mr.setTotalFactura(String.valueOf(request.totalFactura()));
        mr.setClaveDocumentoEmisor(request.claveDocumentoEmisor());
        mr.setNumeroCedulaReceptor(request.emisor());
        
        return mr;
    }

    @Override
    public boolean puedeProceser(String tipoDocumento) {
        return "CCE".equals(tipoDocumento) || 
               "CPCE".equals(tipoDocumento) || 
               "RCE".equals(tipoDocumento);
    }
}