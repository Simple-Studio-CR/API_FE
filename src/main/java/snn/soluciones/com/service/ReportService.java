package snn.soluciones.com.service;


import snn.soluciones.com.models.entity.ComprobantesElectronicos;
import snn.soluciones.com.models.entity.Emisor;
import snn.soluciones.com.service.storage.S3FileService;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JasperRunManager;

/**
 * Servicio para generar documentos PDF de comprobantes (facturas) usando JasperReports.
 */
@Service
public class ReportService {
    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final DataSource dataSource;
    private final IComprobantesElectronicosService comprobantesService;
    private final IEmisorService emisorService;
    private final S3FileService s3FileService;

    public ReportService(
            DataSource dataSource,
            IComprobantesElectronicosService comprobantesService,
            IEmisorService emisorService,
        S3FileService s3FileService) {
        this.dataSource = dataSource;
        this.comprobantesService = comprobantesService;
        this.emisorService = emisorService;
        this.s3FileService = s3FileService;
        log.info("ReportService initialized");
    }

    /**
     * Genera el PDF de una factura a partir de su clave.
     *
     * @param clave clave de 50 caracteres de la factura
     * @return arreglo de bytes con el contenido PDF
     * @throws IllegalArgumentException si la clave es inválida
     * @throws IllegalStateException    si no se encuentra el comprobante o emisor
     * @throws RuntimeException          si falla la generación del reporte
     */
    public byte[] generateFacturaPdf(String clave) {
        log.info("Generating PDF for factura, clave={}", clave);
        if (clave == null || clave.length() != 50) {
            log.error("Invalid clave length: {}", clave);
            throw new IllegalArgumentException("Clave inválida: " + clave);
        }

        // Buscar el comprobante y emisor
        ComprobantesElectronicos ce = comprobantesService.findByClave(clave);
        if (ce == null) {
            log.error("No ComprobantesElectronicos found for clave={}", clave);
            throw new IllegalStateException("Comprobante no encontrado: " + clave);
        }
        Emisor emisor = emisorService.findEmisorOnlyIdentificacion(ce.getIdentificacion());
        if (emisor == null) {
            log.error("No Emisor found for identificacion={}", ce.getIdentificacion());
            throw new IllegalStateException("Emisor no encontrado: " + ce.getIdentificacion());
        }

        // Preparar parámetros para JasperReports
        String tipoDoc = mapTipoDocumento(ce.getTipoDocumento());
        String baseUrl = Objects.requireNonNull(getClass().getResource("/")).toString();
        String logoUrl = s3FileService.getLogoUrl(emisor);

      Map<String, Object> params = new HashMap<>();
        params.put("BASE_URL", baseUrl);
        params.put("BASE_URL_LOGO", logoUrl);
        params.put("CLAVE_FACTURA", clave);
        params.put("TIPO_DOCUMENTO", tipoDoc);
        params.put("RESOLUCION", "Autorizada mediante resolución Nº DGT-R-033-2019 del 20/06/2019");
        params.put("NOTA_FACTURA", emisor.getNataFactura());
        params.put("URL_QR", clave);

        // Generar PDF con try-with-resources
        try (Connection conn = dataSource.getConnection();
             InputStream jasperStream = getClass().getResourceAsStream("/facturas.jasper")) {

            byte[] pdfBytes = JasperRunManager.runReportToPdf(jasperStream, params, conn);
            log.info("Generated PDF ({} bytes) for clave={}", pdfBytes.length, clave);
            return pdfBytes;
        } catch (Exception e) {
            log.error("Error generating PDF for clave={}", clave, e);
            throw new RuntimeException("Error generando PDF de factura: " + e.getMessage(), e);
        }
    }

    // Mapea códigos de documento a textos legibles
    private String mapTipoDocumento(String td) {
        switch (td) {
            case "FE":  return "Factura Electrónica";
            case "ND":  return "Nota de débito Electrónica";
            case "NC":  return "Nota de crédito Electrónica";
            case "TE":  return "Tiquete Electrónico";
            case "FEC": return "Factura Electrónica Compra";
            case "FEE": return "Factura Electrónica Exportación";
            default:    return td;
        }
    }
}