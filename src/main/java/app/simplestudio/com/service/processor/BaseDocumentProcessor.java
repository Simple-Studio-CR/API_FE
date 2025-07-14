// ==================== Processor Base ====================
package app.simplestudio.com.service.processor;

import app.simplestudio.com.dto.FacturaRequestDTO;
import app.simplestudio.com.dto.DocumentoElectronicoResponse;
import app.simplestudio.com.service.builder.FacturaBuilder;
import app.simplestudio.com.service.generator.XmlGeneratorService;
import app.simplestudio.com.service.ConsecutivoService;
import app.simplestudio.com.service.IEmisorService;
import app.simplestudio.com.service.IFacturaService;
import app.simplestudio.com.service.IComprobantesElectronicosService;
import app.simplestudio.com.models.entity.Factura;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.models.entity.ComprobantesElectronicos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase base con lógica común para todos los processors
 */
public abstract class BaseDocumentProcessor implements DocumentProcessor {

    protected static final Logger log = LoggerFactory.getLogger(BaseDocumentProcessor.class);

    protected final IEmisorService emisorService;
    protected final IFacturaService facturaService;
    protected final IComprobantesElectronicosService comprobantesService;
    protected final ConsecutivoService consecutivoService;
    protected final FacturaBuilder facturaBuilder;
    protected final XmlGeneratorService xmlGeneratorService;

    protected BaseDocumentProcessor(IEmisorService emisorService,
        IFacturaService facturaService,
        IComprobantesElectronicosService comprobantesService,
        ConsecutivoService consecutivoService,
        FacturaBuilder facturaBuilder,
        XmlGeneratorService xmlGeneratorService) {
        this.emisorService = emisorService;
        this.facturaService = facturaService;
        this.comprobantesService = comprobantesService;
        this.consecutivoService = consecutivoService;
        this.facturaBuilder = facturaBuilder;
        this.xmlGeneratorService = xmlGeneratorService;
    }

    @Override
    public final DocumentoElectronicoResponse procesar(FacturaRequestDTO request) {
        try {
            log.info("Procesando documento {} para emisor {}",
                request.tipoDocumento(), request.emisor());

            // 1. Validar emisor
            Emisor emisor = validarEmisor(request);

            // 2. Generar consecutivo y clave
            String consecutivo = consecutivoService.generarConsecutivo(request, emisor);
            String clave = consecutivoService.generarClave(request, emisor, consecutivo);

            // 3. Procesamiento específico del tipo de documento
            procesarEspecifico(request, emisor, clave, consecutivo);

            // 4. Construir factura
            Factura factura = facturaBuilder.construir(request, emisor, clave, consecutivo);

            // 5. Generar y firmar XML
            String xmlFileName = xmlGeneratorService.generarYFirmarXml(factura, emisor);

            // 6. Guardar comprobante electrónico
            guardarComprobanteElectronico(request, emisor, clave, consecutivo, xmlFileName);

            // 7. Guardar factura
            facturaService.save(factura);

            log.info("Documento procesado exitosamente. Clave: {}", clave);

            return DocumentoElectronicoResponse.success(
                clave,
                consecutivo,
                request.fechaEmision(),
                xmlFileName
            );

        } catch (Exception e) {
            log.error("Error procesando documento: {}", e.getMessage(), e);
            throw new RuntimeException("Error procesando documento: " + e.getMessage(), e);
        }
    }

    /**
     * Procesamiento específico por tipo de documento
     * Implementado por cada processor concreto
     */
    protected abstract void procesarEspecifico(FacturaRequestDTO request,
        Emisor emisor,
        String clave,
        String consecutivo);

    private Emisor validarEmisor(FacturaRequestDTO request) {
        Emisor emisor = emisorService.findEmisorByIdentificacion(
            request.emisor(),
            request.tokenAccess()
        );

        if (emisor == null) {
            throw new IllegalArgumentException("El usuario o token no existe.");
        }

        return emisor;
    }

    private void guardarComprobanteElectronico(FacturaRequestDTO request,
        Emisor emisor,
        String clave,
        String consecutivo,
        String xmlFileName) {
        ComprobantesElectronicos comprobante = new ComprobantesElectronicos();
        comprobante.setEmisor(emisor);
        comprobante.setConsecutivo(Long.valueOf(consecutivo));
        comprobante.setTipoDocumento(request.tipoDocumento());
        comprobante.setIdentificacion(request.emisor());
        comprobante.setClave(clave);
        comprobante.setFechaEmision(request.fechaEmision());
        comprobante.setNameXml(xmlFileName);
        comprobante.setNameXmlSign(clave + "-factura-sign");
        comprobante.setAmbiente(emisor.getAmbiente());
        comprobante.setEmailDistribucion(request.receptorEmail());
        comprobante.setSucursal(request.sucursal());
        comprobante.setTerminal(request.terminal());

        comprobantesService.save(comprobante);
    }
}