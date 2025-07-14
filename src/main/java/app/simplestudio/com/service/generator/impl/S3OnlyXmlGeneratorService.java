// ==================== XmlGeneratorService 100% S3 ====================
package app.simplestudio.com.service.generator.impl;

import app.simplestudio.com.models.entity.Factura;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.service.generator.XmlGeneratorService;
import app.simplestudio.com.service.storage.S3FileService;
import app.simplestudio.com.mh.CCampoFactura;
import app.simplestudio.com.mh.IGeneraXml;
import app.simplestudio.com.mh.ISigner;
import app.simplestudio.com.service.mapper.FacturaToXmlMapper;
import app.simplestudio.com.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Files;

/**
 * Generador XML que usa SOLO S3 - Reemplaza XmlGeneratorServiceImpl
 */
@Service
public class S3OnlyXmlGeneratorService implements XmlGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(S3OnlyXmlGeneratorService.class);

    private final IGeneraXml generaXml;
    private final ISigner signer;
    private final FacturaToXmlMapper xmlMapper;
    private final S3FileService s3FileService;

    public S3OnlyXmlGeneratorService(
        IGeneraXml generaXml,
        ISigner signer,
        FacturaToXmlMapper xmlMapper,
        S3FileService s3FileService) {
        this.generaXml = generaXml;
        this.signer = signer;
        this.xmlMapper = xmlMapper;
        this.s3FileService = s3FileService;
        log.info("XmlGeneratorService configurado para usar SOLO S3");
    }

    @Override
    public String generarYFirmarXml(Factura factura, Emisor emisor) {
        log.info("🚀 Generando XML para clave: {} usando SOLO S3", factura.getClave());

        Path tempXmlSinFirmar = null;
        Path tempXmlFirmado = null;
        Path tempCertificado = null;

        try {
            // 1. Mapear Factura a CCampoFactura
            CCampoFactura campoFactura = xmlMapper.mapToXmlFormat(factura, emisor);

            // 2. Generar nombres
            String nombreXmlBase = factura.getClave() + "-factura";
            String nombreXmlFirmado = nombreXmlBase + "-sign.xml";

            // 3. Generar XML en memoria
            String xmlContent = generaXml.GeneraXml(campoFactura);
            log.debug("✅ XML generado en memoria para clave: {}", factura.getClave());

            // 4. Crear temporales para firmado
            tempXmlSinFirmar = Files.createTempFile("xml-", ".xml");
            Files.write(tempXmlSinFirmar, xmlContent.getBytes("UTF-8"));

            // 5. Descargar certificado desde S3
            String certificadoS3Key = s3FileService.generateCertificateKey(
                emisor.getIdentificacion(),
                emisor.getCertificado()
            );

            tempCertificado = s3FileService.downloadFileToTemp(certificadoS3Key, "cert.p12");
            if (tempCertificado == null) {
                throw new ValidationException("❌ Certificado no encontrado en S3: " + certificadoS3Key);
            }
            log.debug("✅ Certificado descargado desde S3");

            // 6. Firmar XML
            tempXmlFirmado = Files.createTempFile("xml-signed-", ".xml");
            signer.sign(tempCertificado, emisor.getPingApi(), tempXmlSinFirmar, tempXmlFirmado);
            log.debug("✅ XML firmado exitosamente");

            // 7. Subir ambos XMLs a S3
            String s3KeyXmlSinFirmar = s3FileService.generateXmlKey(emisor.getIdentificacion(), nombreXmlBase + ".xml");
            String s3KeyXmlFirmado = s3FileService.generateXmlKey(emisor.getIdentificacion(), nombreXmlFirmado);

            s3FileService.uploadFileFromPath(tempXmlSinFirmar, s3KeyXmlSinFirmar, "application/xml");
            s3FileService.uploadFileFromPath(tempXmlFirmado, s3KeyXmlFirmado, "application/xml");

            log.info("🎉 XMLs subidos exitosamente a S3 para clave: {}", factura.getClave());
            log.info("📄 XML sin firmar: {}", s3KeyXmlSinFirmar);
            log.info("🔐 XML firmado: {}", s3KeyXmlFirmado);

            return nombreXmlFirmado;

        } catch (Exception e) {
            log.error("❌ Error generando XML con S3 para clave {}: {}",
                factura.getClave(), e.getMessage(), e);
            throw new ValidationException("Error generando XML: " + e.getMessage());
        } finally {
            // 8. SIEMPRE limpiar temporales
            s3FileService.cleanupTempFiles(tempXmlSinFirmar, tempXmlFirmado, tempCertificado);
            log.debug("🧹 Archivos temporales limpiados");
        }
    }
}