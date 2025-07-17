package snn.soluciones.com.mh;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.io.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class GeneraXmlImpl implements IGeneraXml {

  private static final Logger log = LoggerFactory.getLogger(GeneraXmlImpl.class);
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String GeneraXml(CCampoFactura campoFactura, String tipoDocumento) {
    try {
      log.info("Iniciando generación de XML para clave: {}", campoFactura.getClave());

      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      dbFactory.setNamespaceAware(true);
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.newDocument();

      // Crear elemento raíz según tipo
      Element root = crearElementoRaiz(doc, tipoDocumento);
      doc.appendChild(root);

      // Agregar clave
      addElement(doc, root, "Clave", campoFactura.getClave());

      // Agregar código de actividad
      addElement(doc, root, "CodigoActividad", campoFactura.getCodigoActividad());

      // Agregar número consecutivo - CORREGIDO
      addElement(doc, root, "NumeroConsecutivo", campoFactura.getConsecutivo());

      // Agregar fecha de emisión
      addElement(doc, root, "FechaEmision", campoFactura.getFechaEmision());

      // Agregar emisor
      agregarEmisor(doc, root, campoFactura);

      // Agregar receptor (si existe)
      if (campoFactura.getReceptorNombre() != null && !campoFactura.getReceptorNombre().isEmpty()) {
        agregarReceptor(doc, root, campoFactura);
      }

      // Agregar condición de venta - CORREGIDO
      addElement(doc, root, "CondicionVenta", campoFactura.getCondVenta());

      // Agregar plazo de crédito si aplica
      if ("02".equals(campoFactura.getCondVenta())) {
        addElement(doc, root, "PlazoCredito", campoFactura.getPlazoCredito());
      }

      // Agregar medio de pago
      addElement(doc, root, "MedioPago", campoFactura.getMedioPago());

      // Agregar detalle del servicio
      agregarDetalleServicio(doc, root, campoFactura);

      // Agregar resumen de factura
      agregarResumenFactura(doc, root, campoFactura);

      // Agregar referencias (para NC y ND)
      if (campoFactura.getReferencia() != null && !campoFactura.getReferencia().isEmpty()) {
        agregarReferencias(doc, root, campoFactura);
      }

      // Agregar otros campos
      if (campoFactura.getOtros() != null && !campoFactura.getOtros().isEmpty()) {
        addElement(doc, root, "Otros", campoFactura.getOtros());
      }

      // Convertir documento a string
      String xmlString = documentToString(doc);

      // Verificación final del XML
      String rootElementName = root.getNodeName();
      if (!xmlString.contains("</" + rootElementName + ">")) {
        log.error("XML no contiene etiqueta de cierre para elemento raíz: {}", rootElementName);
        throw new RuntimeException("XML mal formado - falta etiqueta de cierre");
      }

      log.info("XML generado exitosamente: {} caracteres", xmlString.length());
      log.debug("Verificación - Inicio del XML: {}", xmlString.substring(0, Math.min(200, xmlString.length())));
      log.debug("Verificación - Fin del XML: {}", xmlString.substring(Math.max(0, xmlString.length() - 200)));

      return xmlString;

    } catch (Exception e) {
      log.error("Error generando XML: {}", e.getMessage(), e);
      throw new RuntimeException("Error generando XML: " + e.getMessage(), e);
    }
  }

  private Element crearElementoRaiz(Document doc, String tipoDocumento) {
    Element root;
    String namespace;
    String elementName;

    switch (tipoDocumento) {
      case "FE":
        namespace = "https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.3/facturaElectronica";
        elementName = "FacturaElectronica";
        break;
      case "NBE":
        namespace = "https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.3/notaDebitoElectronica";
        elementName = "NotaDebitoElectronica";
        break;
      case "NCE":
        namespace = "https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.3/notaCreditoElectronica";
        elementName = "NotaCreditoElectronica";
        break;
      case "TE":
        namespace = "https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.3/tiqueteElectronico";
        elementName = "TiqueteElectronico";
        break;
      case "FEC":
        namespace = "https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.3/facturaElectronicaCompra";
        elementName = "FacturaElectronicaCompra";
        break;
      case "FEE":
        namespace = "https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.3/facturaElectronicaExportacion";
        elementName = "FacturaElectronicaExportacion";
        break;
      default:
        namespace = "https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.3/facturaElectronica";
        elementName = "FacturaElectronica";
    }

    // Crear elemento raíz con namespace
    root = doc.createElementNS(namespace, elementName);

    // IMPORTANTE: No usar setAttributeNS para el namespace principal
    // El namespace ya está definido en createElementNS

    // Agregar solo los atributos xsi y xsd como atributos normales
    root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
    root.setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");

    return root;
  }

  private void agregarEmisor(Document doc, Element parent, CCampoFactura campoFactura) {
    Element emisor = doc.createElement("Emisor");
    parent.appendChild(emisor);

    addElement(doc, emisor, "Nombre", campoFactura.getEmisorNombre());

    Element identificacion = doc.createElement("Identificacion");
    emisor.appendChild(identificacion);
    addElement(doc, identificacion, "Tipo", campoFactura.getEmisorTipoIdentif());
    addElement(doc, identificacion, "Numero", campoFactura.getEmisorNumIdentif());

    // NombreComercial - CORREGIDO
    if (campoFactura.getNombreComercial() != null && !campoFactura.getNombreComercial().isEmpty()) {
      addElement(doc, emisor, "NombreComercial", campoFactura.getNombreComercial());
    }

    Element ubicacion = doc.createElement("Ubicacion");
    emisor.appendChild(ubicacion);
    addElement(doc, ubicacion, "Provincia", campoFactura.getEmisorProv());
    addElement(doc, ubicacion, "Canton", campoFactura.getEmisorCanton());
    addElement(doc, ubicacion, "Distrito", campoFactura.getEmisorDistrito());
    addElement(doc, ubicacion, "Barrio", campoFactura.getEmisorBarrio());
    addElement(doc, ubicacion, "OtrasSenas", campoFactura.getEmisorOtrasSenas());

    Element telefono = doc.createElement("Telefono");
    emisor.appendChild(telefono);
    addElement(doc, telefono, "CodigoPais", campoFactura.getEmisorCodPaisTel());
    addElement(doc, telefono, "NumTelefono", campoFactura.getEmisorTel());

    if (campoFactura.getEmisorFax() != null) {
      Element fax = doc.createElement("Fax");
      emisor.appendChild(fax);
      addElement(doc, fax, "CodigoPais", campoFactura.getEmisorCodPaisFax());
      addElement(doc, fax, "NumTelefono", campoFactura.getEmisorFax());
    }

    addElement(doc, emisor, "CorreoElectronico", campoFactura.getEmisorEmail());
  }

  private void agregarReceptor(Document doc, Element parent, CCampoFactura campoFactura) {
    Element receptor = doc.createElement("Receptor");
    parent.appendChild(receptor);

    addElement(doc, receptor, "Nombre", campoFactura.getReceptorNombre());

    if (campoFactura.getReceptorTipoIdentif() != null && campoFactura.getReceptorNumIdentif() != null) {
      Element identificacion = doc.createElement("Identificacion");
      receptor.appendChild(identificacion);
      addElement(doc, identificacion, "Tipo", campoFactura.getReceptorTipoIdentif());
      addElement(doc, identificacion, "Numero", campoFactura.getReceptorNumIdentif());
    }

    if (campoFactura.getReceptorProvincia() != null) {
      Element ubicacion = doc.createElement("Ubicacion");
      receptor.appendChild(ubicacion);
      addElement(doc, ubicacion, "Provincia", campoFactura.getReceptorProvincia());
      addElement(doc, ubicacion, "Canton", campoFactura.getReceptorCanton());
      addElement(doc, ubicacion, "Distrito", campoFactura.getReceptorDistrito());
      addElement(doc, ubicacion, "Barrio", campoFactura.getReceptorBarrio());
      addElement(doc, ubicacion, "OtrasSenas", campoFactura.getReceptorOtrasSenas());
    }

    if (campoFactura.getReceptorCodPaisTel() != null && campoFactura.getReceptorTel() != null) {
      Element telefono = doc.createElement("Telefono");
      receptor.appendChild(telefono);
      addElement(doc, telefono, "CodigoPais", campoFactura.getReceptorCodPaisTel());
      addElement(doc, telefono, "NumTelefono", campoFactura.getReceptorTel());
    }

    if (campoFactura.getReceptorEmail() != null && !campoFactura.getReceptorEmail().isEmpty()) {
      addElement(doc, receptor, "CorreoElectronico", campoFactura.getReceptorEmail());
    }
  }

  private void agregarDetalleServicio(Document doc, Element parent, CCampoFactura campoFactura) {
    try {
      Element detalleServicio = doc.createElement("DetalleServicio");
      parent.appendChild(detalleServicio);

      // Parsear el JSON del detalle
      if (campoFactura.getDetalleFactura() != null && !campoFactura.getDetalleFactura().isEmpty()) {
        JsonNode detalleArray;
        try {
          detalleArray = objectMapper.readTree(campoFactura.getDetalleFactura());
        } catch (Exception e) {
          log.warn("No se pudo parsear detalleFactura como JSON, intentando como string: {}", e.getMessage());
          // Si no es JSON válido, crear un detalle básico
          agregarLineaDetalleBasica(doc, detalleServicio, campoFactura);
          return;
        }

        // Verificar que sea un array y tenga elementos
        if (!detalleArray.isArray() || detalleArray.isEmpty()) {
          log.warn("detalleFactura no es un array o está vacío");
          agregarLineaDetalleBasica(doc, detalleServicio, campoFactura);
          return;
        }

        for (JsonNode detalle : detalleArray) {
          Element lineaDetalle = doc.createElement("LineaDetalle");
          detalleServicio.appendChild(lineaDetalle);

          addElement(doc, lineaDetalle, "NumeroLinea", detalle.path("NumeroLinea").asText());

          // Código comercial si existe
          if (detalle.has("CodigoComercial") && !detalle.get("CodigoComercial").isNull()) {
            Element codigoComercial = doc.createElement("CodigoComercial");
            lineaDetalle.appendChild(codigoComercial);
            addElement(doc, codigoComercial, "Tipo", detalle.path("CodigoComercial").path("Tipo").asText());
            addElement(doc, codigoComercial, "Codigo", detalle.path("CodigoComercial").path("Codigo").asText());
          }

          // Código
          if (detalle.has("Codigo")) {
            addElement(doc, lineaDetalle, "Codigo", detalle.path("Codigo").asText());
          }

          addElement(doc, lineaDetalle, "Cantidad", detalle.path("Cantidad").asText());
          addElement(doc, lineaDetalle, "UnidadMedida", detalle.path("UnidadMedida").asText());

          if (detalle.has("UnidadMedidaComercial")) {
            addElement(doc, lineaDetalle, "UnidadMedidaComercial", detalle.path("UnidadMedidaComercial").asText());
          }

          addElement(doc, lineaDetalle, "Detalle", detalle.path("Detalle").asText());
          addElement(doc, lineaDetalle, "PrecioUnitario", detalle.path("PrecioUnitario").asText());
          addElement(doc, lineaDetalle, "MontoTotal", detalle.path("MontoTotal").asText());

          // Descuentos
          if (detalle.has("Descuento") && !detalle.get("Descuento").isNull()) {
            JsonNode descuentos = detalle.get("Descuento");
            if (descuentos.isArray()) {
              for (JsonNode desc : descuentos) {
                Element descuento = doc.createElement("Descuento");
                lineaDetalle.appendChild(descuento);
                addElement(doc, descuento, "MontoDescuento", desc.path("MontoDescuento").asText());
                addElement(doc, descuento, "NaturalezaDescuento", desc.path("NaturalezaDescuento").asText());
              }
            }
          }

          addElement(doc, lineaDetalle, "SubTotal", detalle.path("SubTotal").asText());

          // Base imponible
          if (detalle.has("BaseImponible")) {
            addElement(doc, lineaDetalle, "BaseImponible", detalle.path("BaseImponible").asText());
          }

          // Impuestos
          if (detalle.has("Impuesto") && !detalle.get("Impuesto").isNull()) {
            JsonNode impuestos = detalle.get("Impuesto");
            if (impuestos.isArray()) {
              for (JsonNode imp : impuestos) {
                Element impuesto = doc.createElement("Impuesto");
                lineaDetalle.appendChild(impuesto);
                addElement(doc, impuesto, "Codigo", imp.path("Codigo").asText());
                addElement(doc, impuesto, "CodigoTarifa", imp.path("CodigoTarifa").asText());
                addElement(doc, impuesto, "Tarifa", imp.path("Tarifa").asText());
                addElement(doc, impuesto, "Monto", imp.path("Monto").asText());
              }
            }
          }

          // Impuesto neto
          if (detalle.has("ImpuestoNeto")) {
            addElement(doc, lineaDetalle, "ImpuestoNeto", detalle.path("ImpuestoNeto").asText());
          }

          addElement(doc, lineaDetalle, "MontoTotalLinea", detalle.path("MontoTotalLinea").asText());
        }
      } else {
        // Si no hay detalle, agregar una línea básica mínima requerida
        log.warn("No hay detalle de factura, agregando línea básica");
        agregarLineaDetalleBasica(doc, detalleServicio, campoFactura);
      }
    } catch (Exception e) {
      log.error("Error procesando detalle del servicio: {}", e.getMessage());
      // En caso de error, agregar línea básica para que el XML sea válido
      Element detalleServicio = doc.createElement("DetalleServicio");
      parent.appendChild(detalleServicio);
      agregarLineaDetalleBasica(doc, detalleServicio, campoFactura);
    }
  }

  /**
   * Agrega una línea de detalle básica cuando no hay detalle o hay error
   */
  private void agregarLineaDetalleBasica(Document doc, Element detalleServicio, CCampoFactura campoFactura) {
    Element lineaDetalle = doc.createElement("LineaDetalle");
    detalleServicio.appendChild(lineaDetalle);

    addElement(doc, lineaDetalle, "NumeroLinea", "1");
    addElement(doc, lineaDetalle, "Cantidad", "1");
    addElement(doc, lineaDetalle, "UnidadMedida", "Unid");
    addElement(doc, lineaDetalle, "Detalle", "Servicio");
    addElement(doc, lineaDetalle, "PrecioUnitario", campoFactura.getTotalVentasNeta() != null ? campoFactura.getTotalVentasNeta() : "0.00");
    addElement(doc, lineaDetalle, "MontoTotal", campoFactura.getTotalVentasNeta() != null ? campoFactura.getTotalVentasNeta() : "0.00");
    addElement(doc, lineaDetalle, "SubTotal", campoFactura.getTotalVentasNeta() != null ? campoFactura.getTotalVentasNeta() : "0.00");
    addElement(doc, lineaDetalle, "MontoTotalLinea", campoFactura.getTotalComprobante() != null ? campoFactura.getTotalComprobante() : "0.00");
  }

  private void agregarResumenFactura(Document doc, Element parent, CCampoFactura campoFactura) {
    Element resumen = doc.createElement("ResumenFactura");
    parent.appendChild(resumen);

    // Código de tipo de moneda - CORREGIDO
    if (campoFactura.getCodMoneda() != null) {
      Element codigoTipoMoneda = doc.createElement("CodigoTipoMoneda");
      resumen.appendChild(codigoTipoMoneda);
      addElement(doc, codigoTipoMoneda, "CodigoMoneda", campoFactura.getCodMoneda());
      addElement(doc, codigoTipoMoneda, "TipoCambio", campoFactura.getTipoCambio());
    }

    // Totales - TODOS CORREGIDOS
    addElement(doc, resumen, "TotalServGravados", campoFactura.getTotalServGravados());
    addElement(doc, resumen, "TotalServExentos", campoFactura.getTotalServExentos());
    addElement(doc, resumen, "TotalServExonerado", campoFactura.getTotalServExonerado());
    addElement(doc, resumen, "TotalMercanciasGravadas", campoFactura.getTotalMercGravadas());
    addElement(doc, resumen, "TotalMercanciasExentas", campoFactura.getTotalMercExentas());
    addElement(doc, resumen, "TotalMercExonerada", campoFactura.getTotalMercExonerada());
    addElement(doc, resumen, "TotalGravado", campoFactura.getTotalGravados());
    addElement(doc, resumen, "TotalExento", campoFactura.getTotalExentos());
    addElement(doc, resumen, "TotalExonerado", campoFactura.getTotalExonerado());
    addElement(doc, resumen, "TotalVenta", campoFactura.getTotalVentas());
    addElement(doc, resumen, "TotalDescuentos", campoFactura.getTotalDescuentos());
    addElement(doc, resumen, "TotalVentaNeta", campoFactura.getTotalVentasNeta());
    addElement(doc, resumen, "TotalImpuesto", campoFactura.getTotalImp());

    // Total IVA Devuelto (si aplica)
    if (campoFactura.getTotalIVADevuelto() != null && !campoFactura.getTotalIVADevuelto().equals("0.00000")) {
      addElement(doc, resumen, "TotalIVADevuelto", campoFactura.getTotalIVADevuelto());
    }

    // Total otros cargos (si aplica)
    if (campoFactura.getTotalOtrosCargos() != null && !campoFactura.getTotalOtrosCargos().equals("0.00000")) {
      addElement(doc, resumen, "TotalOtrosCargos", campoFactura.getTotalOtrosCargos());
    }

    addElement(doc, resumen, "TotalComprobante", campoFactura.getTotalComprobante());
  }

  private void agregarReferencias(Document doc, Element parent, CCampoFactura campoFactura) {
    try {
      if (campoFactura.getReferencia() != null && !campoFactura.getReferencia().isEmpty()) {
        JsonNode referencias = objectMapper.readTree(campoFactura.getReferencia());

        if (referencias.isArray()) {
          for (JsonNode ref : referencias) {
            Element informacionReferencia = doc.createElement("InformacionReferencia");
            parent.appendChild(informacionReferencia);

            addElement(doc, informacionReferencia, "TipoDoc", ref.path("TipoDoc").asText());
            addElement(doc, informacionReferencia, "Numero", ref.path("Numero").asText());
            addElement(doc, informacionReferencia, "FechaEmision", ref.path("FechaEmision").asText());
            addElement(doc, informacionReferencia, "Codigo", ref.path("Codigo").asText());
            addElement(doc, informacionReferencia, "Razon", ref.path("Razon").asText());
          }
        }
      }
    } catch (Exception e) {
      log.error("Error procesando referencias: {}", e.getMessage());
    }
  }

  private void addElement(Document doc, Element parent, String name, String value) {
    if (value != null && !value.isEmpty()) {
      Element element = doc.createElement(name);
      // No escapar manualmente - dejar que el DOM lo maneje
      element.appendChild(doc.createTextNode(value));
      parent.appendChild(element);
    }
  }

  private String documentToString(Document doc) throws Exception {
    try {
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();

      // Configuración para generar XML bien formado
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

      // Usar ByteArrayOutputStream para mejor control
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      StreamResult result = new StreamResult(baos);

      transformer.transform(new DOMSource(doc), result);

      // Convertir a string usando UTF-8
      String xmlString = baos.toString("UTF-8");

      // Limpiar cualquier carácter no deseado al final
      xmlString = xmlString.trim();

      // Verificar que termine correctamente
      if (!xmlString.endsWith(">")) {
        log.error("XML mal formado, no termina con >");
        throw new RuntimeException("XML generado está mal formado");
      }

      // Buscar el cierre del elemento raíz
      String rootElementName = doc.getDocumentElement().getNodeName();
      String closingTag = "</" + rootElementName + ">";
      int closingIndex = xmlString.lastIndexOf(closingTag);

      if (closingIndex == -1) {
        log.error("No se encontró etiqueta de cierre para: {}", rootElementName);
        throw new RuntimeException("XML mal formado - falta etiqueta de cierre");
      }

      // Asegurarse de que no haya contenido después del cierre
      int endIndex = closingIndex + closingTag.length();
      if (endIndex < xmlString.length()) {
        String extraContent = xmlString.substring(endIndex).trim();
        if (!extraContent.isEmpty()) {
          log.warn("Contenido extra después del cierre del XML: '{}'", extraContent);
          // Truncar el XML justo después del cierre
          xmlString = xmlString.substring(0, endIndex);
        }
      }

      // Log para debugging
      log.debug("XML generado - Longitud: {} caracteres", xmlString.length());
      if (log.isDebugEnabled()) {
        log.debug("Inicio del XML: {}",
            xmlString.length() > 200 ? xmlString.substring(0, 200) + "..." : xmlString);
        log.debug("Fin del XML: {}",
            xmlString.length() > 200 ? "..." + xmlString.substring(xmlString.length() - 200) : xmlString);
      }

      return xmlString;

    } catch (Exception e) {
      log.error("Error convirtiendo documento a string: {}", e.getMessage(), e);
      throw new Exception("Error generando XML string: " + e.getMessage(), e);
    }
  }

  @Override
  public String GeneraXmlDocumentos(CCampoFactura paramCCampoFactura) {
    // Implementación para otros tipos de documentos
    return GeneraXml(paramCCampoFactura, "" );
  }

  @Override
  public String GeneraXmlMr(CCampoFactura paramCCampoFactura) {
    // Implementación específica para Mensaje Receptor
    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      dbFactory.setNamespaceAware(true);
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.newDocument();

      // Crear elemento raíz para Mensaje Receptor
      Element root = doc.createElementNS("https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.3/mensajeReceptor", "MensajeReceptor");
      root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
      root.setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
      doc.appendChild(root);

      // Agregar elementos del mensaje receptor
      addElement(doc, root, "Clave", paramCCampoFactura.getClave());
      addElement(doc, root, "NumeroCedulaEmisor", paramCCampoFactura.getEmisorNumIdentif());
      addElement(doc, root, "FechaEmisionDoc", paramCCampoFactura.getFechaEmision());
      addElement(doc, root, "Mensaje", paramCCampoFactura.getMensaje());

      if (paramCCampoFactura.getDetalleMensaje() != null) {
        addElement(doc, root, "DetalleMensaje", paramCCampoFactura.getDetalleMensaje());
      }

      addElement(doc, root, "MontoTotalImpuesto", paramCCampoFactura.getTotalImp());
      addElement(doc, root, "TotalFactura", paramCCampoFactura.getTotalComprobante());

      // Agregar receptor
      agregarReceptor(doc, root, paramCCampoFactura);

      addElement(doc, root, "NumeroConsecutivoReceptor", paramCCampoFactura.getConsecutivo());

      return documentToString(doc);

    } catch (Exception e) {
      log.error("Error generando XML de Mensaje Receptor: {}", e.getMessage(), e);
      throw new RuntimeException("Error generando XML MR: " + e.getMessage(), e);
    }
  }

  @Override
  public void generateXml(String path, String xmlContent, String fileName) throws Exception {
    // Este método se usa para guardar el XML en el sistema de archivos
    // En tu caso, ya estás usando S3, así que este método podría quedar vacío
    // o podrías usarlo para guardar temporalmente si es necesario
    log.info("generateXml llamado - path: {}, fileName: {}", path, fileName);
  }
}