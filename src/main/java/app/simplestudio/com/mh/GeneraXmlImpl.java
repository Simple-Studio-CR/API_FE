package app.simplestudio.com.mh;

import app.simplestudio.com.service.adapter.StorageAdapter;
import app.simplestudio.com.util.DocumentStructureUtil;
import app.simplestudio.com.util.FileManagerUtil;
import app.simplestudio.com.util.JsonToXmlConverterUtil;
import app.simplestudio.com.util.XmlBuilderUtil;
import app.simplestudio.com.util.XmlValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GeneraXmlImpl implements IGeneraXml {

  private final Logger log = LoggerFactory.getLogger(getClass());

  // ==================== SERVICIOS ORIGINALES ====================

  // ==================== NUEVOS UTILS ====================
  @Autowired
  private DocumentStructureUtil documentStructureUtil;

  @Autowired
  private XmlBuilderUtil xmlBuilderUtil;

  @Autowired
  private XmlValidationUtil xmlValidationUtil;

  @Autowired
  private JsonToXmlConverterUtil jsonToXmlConverterUtil;

  @Autowired
  private StorageAdapter storageAdapter;

  // ==================== MÉTODOS ORIGINALES MANTENIDOS ====================

  /**
   * FIRMA ORIGINAL MANTENIDA - Método principal de generación XML
   */
  public String GeneraXml(CCampoFactura c) {
    String tipoDocumento = c.getClave().substring(29, 31);
    log.info("Se está generando documento tipo: {}", documentStructureUtil.getDocumentDescription(tipoDocumento));

    if (documentStructureUtil.isMensajeReceptor(tipoDocumento)) {
      return GeneraXmlMr(c);
    } else {
      return generateDocumentXml(c, tipoDocumento);
    }
  }

  /**
   * FIRMA ORIGINAL MANTENIDA - Generación XML para documentos estándar
   */
  public String GeneraXmlDocumentos(CCampoFactura c) {
    String tipoDocumento = c.getClave().substring(29, 31);
    return buildDocumentContent(c, tipoDocumento);
  }

  /**
   * FIRMA ORIGINAL MANTENIDA - Generación XML para Mensaje Receptor
   */
  public String GeneraXmlMr(CCampoFactura mr) {
    String numeroCedulaEmisor = xmlValidationUtil.formatearNumeroCedula(mr.getNumeroCedulaEmisor());
    String numeroCedulaReceptor = xmlValidationUtil.formatearNumeroCedula(mr.getNumeroCedulaReceptor());

    return xmlBuilderUtil.createDocument()
        .addContent(documentStructureUtil.getDocumentHeader("05"))
        .addElement("Clave", mr.getClaveDocumentoEmisor())
        .addElement("NumeroCedulaEmisor", numeroCedulaEmisor)
        .addElement("FechaEmisionDoc", mr.getFechaEmisionDoc())
        .addElementWithEscape("Mensaje", mr.getMensaje())
        .addElementIf(xmlValidationUtil.isNotEmpty(mr.getMensaje()),
            "DetalleMensaje", xmlValidationUtil.procesarTexto(mr.getDetalleMensaje()))
        .addElementIf(xmlValidationUtil.isNotEmpty(mr.getMontoTotalImpuesto()),
            "MontoTotalImpuesto", mr.getMontoTotalImpuesto())
        .addElementIf(shouldIncludeCodigoActividad(mr),
            "CodigoActividad", mr.getCodigoActividad())
        .addElementIf(xmlValidationUtil.isNotEmpty(mr.getCondicionImpuesto()),
            "CondicionImpuesto", mr.getCondicionImpuesto())
        .addSection(buildMensajeReceptorConditionalFields(mr))
        .addElement("TotalFactura", mr.getTotalFactura())
        .addElement("NumeroCedulaReceptor", numeroCedulaReceptor)
        .addElement("NumeroConsecutivoReceptor", mr.getNumeroConsecutivoReceptor())
        .addContent(documentStructureUtil.getDocumentFooter("05"))
        .build();
  }

  /**
   * FIRMA ORIGINAL MANTENIDA - Generación de archivo XML
   */
  public void generateXml(String path, String datosXml, String name) throws Exception {
    String fullPath = path + name + ".xml";
    storageAdapter.saveToFile(fullPath, datosXml);
    log.info("Archivo XML creado con éxito: {}", fullPath);
  }

  /**
   * FIRMA ORIGINAL MANTENIDA - Procesamiento de texto
   */
  public String procesarTexto(String j) {
    return xmlValidationUtil.procesarTexto(j);
  }

  // ==================== MÉTODOS AUXILIARES REFACTORIZADOS ====================

  /**
   * Genera XML completo del documento con header y footer
   */
  private String generateDocumentXml(CCampoFactura c, String tipoDocumento) {
    return documentStructureUtil.getDocumentHeader(tipoDocumento) +
        buildDocumentContent(c, tipoDocumento) +
        documentStructureUtil.getDocumentFooter(tipoDocumento);
  }

  /**
   * Construye el contenido del documento XML
   */
  private String buildDocumentContent(CCampoFactura c, String tipoDocumento) {
    return xmlBuilderUtil.createDocument()
        .addSection(buildDocumentHeaderSection(c))
        .addSection(buildEmisorSection(c))
        .addSection(buildReceptorSection(c, tipoDocumento))
        .addSection(buildCondicionVentaSection(c))
        .addSection(jsonToXmlConverterUtil.convertDetalleFacturaToXml(c.getDetalleFactura(), tipoDocumento))
        .addSection(jsonToXmlConverterUtil.convertOtrosCargosToXml(c.getOtrosCargos()))
        .addSection(buildResumenFacturaSection(c, tipoDocumento))
        .addSection(jsonToXmlConverterUtil.convertReferenciasToXml(c.getReferencia()))
        .addSection(documentStructureUtil.buildOtrosSection(c.getOtros()))
        .build();
  }

  /**
   * Construye la sección de header del documento
   */
  private String buildDocumentHeaderSection(CCampoFactura c) {
    return xmlBuilderUtil.createDocument()
        .addElement("Clave", c.getClave())
        .addElement("CodigoActividad", xmlValidationUtil.formatearCodigoActividad(c.getCodigoActividad()))
        .addElement("NumeroConsecutivo", c.getConsecutivo())
        .addElement("FechaEmision", c.getFechaEmision())
        .build();
  }

  /**
   * Construye la sección del emisor
   */
  private String buildEmisorSection(CCampoFactura c) {
    XmlBuilderUtil builder = xmlBuilderUtil.createDocument()
        .openTag("Emisor")
        .addElementWithEscape("Nombre", c.getEmisorNombre())
        .addIdentificacionBlock(
            xmlValidationUtil.formatearTipoIdentificacion(c.getEmisorTipoIdentif()),
            c.getEmisorNumIdentif())
        .addElementIf(xmlValidationUtil.isNotEmpty(c.getNombreComercial()),
            "NombreComercial", xmlValidationUtil.procesarTexto(c.getNombreComercial()));

    // Ubicación del emisor
    if (hasCompleteUbicacion(c.getEmisorProv(), c.getEmisorCanton(), c.getEmisorDistrito(), c.getEmisorOtrasSenas())) {
      builder.addUbicacionBlock(
          c.getEmisorProv(),
          xmlValidationUtil.formatearCodigoUbicacion(c.getEmisorCanton()),
          xmlValidationUtil.formatearCodigoUbicacion(c.getEmisorDistrito()),
          xmlValidationUtil.isNotEmpty(c.getEmisorBarrio()) ?
              xmlValidationUtil.formatearCodigoUbicacion(c.getEmisorBarrio()) : null,
          c.getEmisorOtrasSenas());
    }

    // Teléfono y fax del emisor
    builder.addTelefonoBlock("Telefono", c.getEmisorCodPaisTel(), c.getEmisorTel())
        .addTelefonoBlock("Fax", c.getEmisorCodPaisFax(), c.getEmisorFax())
        .addElementWithEscape("CorreoElectronico", c.getEmisorEmail())
        .closeTag("Emisor");

    return builder.build();
  }

  /**
   * Construye la sección del receptor
   */
  private String buildReceptorSection(CCampoFactura c, String tipoDocumento) {
    if ("true".equals(c.getOmitirReceptor()) && xmlValidationUtil.isNotEmpty(c.getReceptorNombre())) {
      // Receptor mínimo (solo nombre)
      return xmlBuilderUtil.createDocument()
          .openTag("Receptor")
          .addElementWithEscape("Nombre", c.getReceptorNombre())
          .closeTag("Receptor")
          .build();
    } else if (!"true".equals(c.getOmitirReceptor())) {
      // Receptor completo
      return buildCompleteReceptorSection(c, tipoDocumento);
    }

    return "";
  }

  /**
   * Construye la sección completa del receptor
   */
  private String buildCompleteReceptorSection(CCampoFactura c, String tipoDocumento) {
    XmlBuilderUtil builder = xmlBuilderUtil.createDocument()
        .openTag("Receptor")
        .addElementWithEscape("Nombre", c.getReceptorNombre());

    // Identificación del receptor
    if (xmlValidationUtil.isIdentificacionExtranjera(c.getReceptorTipoIdentif())) {
      builder.addElement("IdentificacionExtranjero", c.getReceptorNumIdentif())
          .addElementIf(xmlValidationUtil.isNotEmpty(c.getReceptorOtrasSenas()),
              "OtrasSenasExtranjero", xmlValidationUtil.procesarTexto(c.getReceptorOtrasSenas()));
    } else {
      if (xmlValidationUtil.isNotEmpty(c.getReceptorTipoIdentif()) && xmlValidationUtil.isNotEmpty(c.getReceptorNumIdentif())) {
        builder.addIdentificacionBlock(
            xmlValidationUtil.formatearTipoIdentificacion(c.getReceptorTipoIdentif()),
            c.getReceptorNumIdentif());
      }

      // Ubicación del receptor (no para exportación)
      if (!documentStructureUtil.isFacturaExportacion(tipoDocumento)) {
        if (hasCompleteUbicacion(c.getReceptorProvincia(), c.getReceptorCanton(),
            c.getReceptorDistrito(), c.getReceptorOtrasSenas())) {
          builder.addUbicacionBlock(
              c.getReceptorProvincia(),
              xmlValidationUtil.formatearCodigoUbicacion(c.getReceptorCanton()),
              xmlValidationUtil.formatearCodigoUbicacion(c.getReceptorDistrito()),
              xmlValidationUtil.isNotEmpty(c.getReceptorBarrio()) ?
                  xmlValidationUtil.formatearCodigoUbicacion(c.getReceptorBarrio()) : null,
              c.getReceptorOtrasSenas());
        }
      }
    }

    // Teléfono, fax y email del receptor
    builder.addTelefonoBlock("Telefono", c.getReceptorCodPaisTel(), c.getReceptorTel())
        .addTelefonoBlock("Fax", c.getReceptorCodPaisFax(), c.getReceptorFax())
        .addElementIf(xmlValidationUtil.isNotEmpty(c.getReceptorEmail()),
            "CorreoElectronico", c.getReceptorEmail())
        .closeTag("Receptor");

    return builder.build();
  }

  /**
   * Construye la sección de condición de venta
   */
  private String buildCondicionVentaSection(CCampoFactura c) {
    return xmlBuilderUtil.createDocument()
        .addElement("CondicionVenta", xmlValidationUtil.formatearCondicionVenta(c.getCondVenta()))
        .addElement("PlazoCredito", c.getPlazoCredito())
        .addElementIf(xmlValidationUtil.isNotEmpty(c.getMedioPago()),
            "MedioPago", xmlValidationUtil.formatearMedioPago(c.getMedioPago()))
        .addElementIf(xmlValidationUtil.isNotEmpty(c.getMedioPago2()),
            "MedioPago", xmlValidationUtil.formatearMedioPago(c.getMedioPago2()))
        .addElementIf(xmlValidationUtil.isNotEmpty(c.getMedioPago3()),
            "MedioPago", xmlValidationUtil.formatearMedioPago(c.getMedioPago3()))
        .addElementIf(xmlValidationUtil.isNotEmpty(c.getMedioPago4()),
            "MedioPago", xmlValidationUtil.formatearMedioPago(c.getMedioPago4()))
        .build();
  }

  /**
   * Construye la sección de resumen de factura
   */
  private String buildResumenFacturaSection(CCampoFactura c, String tipoDocumento) {
    XmlBuilderUtil builder = xmlBuilderUtil.createDocument()
        .openTag("ResumenFactura");

    // Código de moneda
    if (xmlValidationUtil.isNotEmpty(c.getCodMoneda()) && xmlValidationUtil.isNotEmpty(c.getTipoCambio())) {
      builder.openTag("CodigoTipoMoneda")
          .addElement("CodigoMoneda", c.getCodMoneda())
          .addElement("TipoCambio", c.getTipoCambio())
          .closeTag("CodigoTipoMoneda");
    }

    builder.addElement("TotalServGravados", c.getTotalServGravados())
        .addElement("TotalServExentos", c.getTotalServExentos())
        .addElementIf(!documentStructureUtil.isFacturaExportacion(tipoDocumento) &&
                xmlValidationUtil.isNotEmpty(c.getTotalServExonerado()),
            "TotalServExonerado", c.getTotalServExonerado())
        .addElement("TotalMercanciasGravadas", c.getTotalMercGravadas())
        .addElement("TotalMercanciasExentas", c.getTotalMercExentas())
        .addElementIf(!documentStructureUtil.isFacturaExportacion(tipoDocumento) &&
                xmlValidationUtil.isNotEmpty(c.getTotalMercExonerada()),
            "TotalMercExonerada", c.getTotalMercExonerada())
        .addElement("TotalGravado", c.getTotalGravados())
        .addElement("TotalExento", c.getTotalExentos())
        .addElementIf(!documentStructureUtil.isFacturaExportacion(tipoDocumento) &&
                xmlValidationUtil.isNotEmpty(c.getTotalExonerado()),
            "TotalExonerado", c.getTotalExonerado())
        .addElement("TotalVenta", c.getTotalVentas())
        .addElement("TotalDescuentos", c.getTotalDescuentos())
        .addElement("TotalVentaNeta", c.getTotalVentasNeta())
        .addElement("TotalImpuesto", c.getTotalImp())
        .addElementIf(shouldIncludeIVADevuelto(c, tipoDocumento),
            "TotalIVADevuelto", c.getTotalIVADevuelto())
        .addElementIf(xmlValidationUtil.isPositiveAmount(c.getTotalOtrosCargos()),
            "TotalOtrosCargos", c.getTotalOtrosCargos())
        .addElement("TotalComprobante", c.getTotalComprobante())
        .closeTag("ResumenFactura");

    return builder.build();
  }

  /**
   * Construye campos condicionales para Mensaje Receptor
   */
  private String buildMensajeReceptorConditionalFields(CCampoFactura mr) {
    XmlBuilderUtil builder = xmlBuilderUtil.createDocument();

    String condicion = mr.getCondicionImpuesto();

    if (!"01".equals(condicion) && !"04".equals(condicion) && !"05".equals(condicion)) {
      if (!"03".equals(condicion) && xmlValidationUtil.isPositiveAmount(mr.getMontoTotalImpuestoAcreditar())) {
        builder.addElement("MontoTotalImpuestoAcreditar", mr.getMontoTotalImpuesto());
      }

      if (!"02".equals(condicion)) {
        if (xmlValidationUtil.isPositiveAmount(mr.getMontoTotalImpuestoAcreditar())) {
          builder.addElement("MontoTotalImpuestoAcreditar", mr.getMontoTotalImpuestoAcreditar());
        }
        if (xmlValidationUtil.isPositiveAmount(mr.getMontoTotalDeGastoAplicable())) {
          builder.addElement("MontoTotalDeGastoAplicable", mr.getMontoTotalDeGastoAplicable());
        }
      }
    }

    return builder.build();
  }

  // ==================== MÉTODOS DE VALIDACIÓN ====================

  /**
   * Verifica si tiene ubicación completa
   */
  private boolean hasCompleteUbicacion(String provincia, String canton, String distrito, String otrasSenas) {
    return xmlValidationUtil.isNotEmpty(provincia) &&
        xmlValidationUtil.isNotEmpty(canton) &&
        xmlValidationUtil.isNotEmpty(distrito) &&
        xmlValidationUtil.isNotEmpty(otrasSenas);
  }

  /**
   * Verifica si debe incluir código de actividad en MR
   */
  private boolean shouldIncludeCodigoActividad(CCampoFactura mr) {
    return xmlValidationUtil.isNotEmpty(mr.getCondicionImpuesto()) &&
        !"05".equals(mr.getCondicionImpuesto()) &&
        xmlValidationUtil.isNotEmpty(mr.getCodigoActividad());
  }

  /**
   * Verifica si debe incluir IVA devuelto
   */
  private boolean shouldIncludeIVADevuelto(CCampoFactura c, String tipoDocumento) {
    return xmlValidationUtil.requiresIVADevuelto(tipoDocumento) &&
        xmlValidationUtil.isNotEmpty(c.getTotalIVADevuelto()) &&
        xmlValidationUtil.isPositiveAmount(c.getTotalIVADevuelto());
  }
}