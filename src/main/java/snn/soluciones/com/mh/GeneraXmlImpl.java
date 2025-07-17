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

      // Agregar número consecutivo
      addElement(doc, root, "NumeroConsecutivo", campoFactura.getConsecutivo());

      // Agregar fecha de emisión
      addElement(doc, root, "FechaEmision", campoFactura.getFechaEmision());

      // Agregar emisor
      agregarEmisor(doc, root, campoFactura);

      // Agregar receptor (si existe)
      if (campoFactura.getReceptorNombre() != null && !campoFactura.getReceptorNombre().isEmpty()) {
        agregarReceptor(doc, root, campoFactura);
      }

      // Agregar condición de venta
      addElement(doc, root, "CondicionVenta", campoFactura.getCondVenta());

      // Agregar plazo crédito
      if (campoFactura.getPlazoCredito() != null && !campoFactura.getPlazoCredito().isEmpty()) {
        addElement(doc, root, "PlazoCredito", campoFactura.getPlazoCredito());
      }

      // Agregar medio de pago
      addElement(doc, root, "MedioPago", campoFactura.getMedioPago());

      // Agregar detalle del servicio
      agregarDetalleServicio(doc, root, campoFactura);

      // Agregar resumen de factura con soporte para exoneración
      agregarResumenFactura(doc, root, campoFactura);

      // Agregar información de referencia si existe
      if (campoFactura.getReferencia() != null && !campoFactura.getReferencia().isEmpty()) {
        procesarInformacionReferencia(doc, root, campoFactura.getReferencia());
      }

      agregarOtrosConProveedor(doc, root, campoFactura);

      return documentToString(doc);

    } catch (Exception e) {
      log.error("Error generando XML: {}", e.getMessage(), e);
      throw new RuntimeException("Error generando XML: " + e.getMessage(), e);
    }
  }

  private void agregarOtrosConProveedor(Document doc, Element parent, CCampoFactura campoFactura) {
    Element otros = doc.createElement("Otros");
    parent.appendChild(otros);

    // Agregar OtrosTexto si existe
    if (campoFactura.getOtros() != null && !campoFactura.getOtros().isEmpty()) {
      addElement(doc, otros, "OtrosTexto", campoFactura.getOtros());
    }

    // Agregar información del proveedor
    Element otroContenido = doc.createElement("OtroContenido");
    otros.appendChild(otroContenido);

    Element contactoDesarrollador = doc.createElementNS("https://snnsoluciones.com", "ContactoDesarrollador");
    otroContenido.appendChild(contactoDesarrollador);

    Element proveedor = doc.createElement("ProveedorSistemaComprobantesElectronicos");
    contactoDesarrollador.appendChild(proveedor);

    addElement(doc, proveedor, "Nombre", "Andrés Mayorga Espinoza");

    Element identificacion = doc.createElement("Identificacion");
    proveedor.appendChild(identificacion);
    addElement(doc, identificacion, "Tipo", "01");
    addElement(doc, identificacion, "Numero", "114970286");

    addElement(doc, proveedor, "CorreoElectronico", "info@snnsoluciones.com");

    Element telefono = doc.createElement("Telefono");
    proveedor.appendChild(telefono);
    addElement(doc, telefono, "CodigoPais", "506");
    addElement(doc, telefono, "NumTelefono", "72010233");

    addElement(doc, proveedor, "SitioWeb", "www.snnsoluciones.com");
    addElement(doc, proveedor, "NombreEmpresa", "SNN Soluciones");
  }

  private Element crearElementoRaiz(Document doc, String tipoDocumento) {
    String namespace = "https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.3/";
    String rootElementName = "";

    switch (tipoDocumento) {
      case "01":
        rootElementName = "FacturaElectronica";
        namespace += "facturaElectronica";
        break;
      case "02":
        rootElementName = "NotaDebitoElectronica";
        namespace += "notaDebitoElectronica";
        break;
      case "03":
        rootElementName = "NotaCreditoElectronica";
        namespace += "notaCreditoElectronica";
        break;
      case "04":
        rootElementName = "TiqueteElectronico";
        namespace += "tiqueteElectronico";
        break;
      case "08":
        rootElementName = "FacturaElectronicaCompra";
        namespace += "facturaElectronicaCompra";
        break;
      case "09":
        rootElementName = "FacturaElectronicaExportacion";
        namespace += "facturaElectronicaExportacion";
        break;
      default:
        rootElementName = "FacturaElectronica";
        namespace += "facturaElectronica";
    }

    Element root = doc.createElementNS(namespace, rootElementName);
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

    if (campoFactura.getNombreComercial() != null && !campoFactura.getNombreComercial().isEmpty()) {
      addElement(doc, emisor, "NombreComercial", campoFactura.getNombreComercial());
    }

    if (campoFactura.getEmisorProv() != null || campoFactura.getEmisorCanton() != null ||
        campoFactura.getEmisorDistrito() != null || campoFactura.getEmisorBarrio() != null) {
      Element ubicacion = doc.createElement("Ubicacion");
      emisor.appendChild(ubicacion);
      addElement(doc, ubicacion, "Provincia", campoFactura.getEmisorProv());
      addElement(doc, ubicacion, "Canton", campoFactura.getEmisorCanton());
      addElement(doc, ubicacion, "Distrito", campoFactura.getEmisorDistrito());
      addElement(doc, ubicacion, "Barrio", campoFactura.getEmisorBarrio());
      addElement(doc, ubicacion, "OtrasSenas", campoFactura.getEmisorOtrasSenas());
    }

    if (campoFactura.getEmisorCodPaisTel() != null && campoFactura.getEmisorTel() != null) {
      Element telefono = doc.createElement("Telefono");
      emisor.appendChild(telefono);
      addElement(doc, telefono, "CodigoPais", campoFactura.getEmisorCodPaisTel());
      addElement(doc, telefono, "NumTelefono", campoFactura.getEmisorTel());
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

    if (campoFactura.getReceptorProvincia() != null || campoFactura.getReceptorCanton() != null ||
        campoFactura.getReceptorDistrito() != null || campoFactura.getReceptorBarrio() != null) {
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

          // Código CABYS
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

          // Impuestos con soporte para exoneración
          if (detalle.has("Impuesto") && !detalle.get("Impuesto").isNull()) {
            JsonNode impuestos = detalle.get("Impuesto");
            if (impuestos.isArray()) {
              for (JsonNode imp : impuestos) {
                Element impuesto = doc.createElement("Impuesto");
                lineaDetalle.appendChild(impuesto);

                addElement(doc, impuesto, "Codigo", imp.path("Codigo").asText());
                addElement(doc, impuesto, "CodigoTarifa", imp.path("CodigoTarifa").asText());
                addElement(doc, impuesto, "Tarifa", imp.path("Tarifa").asText());

                // Factor IVA (para IVA Bienes Usados)
                if (imp.has("FactorIVA")) {
                  addElement(doc, impuesto, "FactorIVA", imp.path("FactorIVA").asText());
                }

                addElement(doc, impuesto, "Monto", imp.path("Monto").asText());

                // Monto de exportación
                if (imp.has("MontoExportacion")) {
                  addElement(doc, impuesto, "MontoExportacion", imp.path("MontoExportacion").asText());
                }

                // NUEVO: Procesar exoneración si existe
                if (imp.has("Exoneracion") && !imp.get("Exoneracion").isNull()) {
                  Element exoneracion = doc.createElement("Exoneracion");
                  impuesto.appendChild(exoneracion);

                  JsonNode exon = imp.get("Exoneracion");
                  addElement(doc, exoneracion, "TipoDocumento", exon.path("TipoDocumento").asText());
                  addElement(doc, exoneracion, "NumeroDocumento", exon.path("NumeroDocumento").asText());
                  addElement(doc, exoneracion, "NombreInstitucion", exon.path("NombreInstitucion").asText());
                  addElement(doc, exoneracion, "FechaEmision", exon.path("FechaEmision").asText());
                  addElement(doc, exoneracion, "PorcentajeExoneracion", exon.path("PorcentajeExoneracion").asText());
                  addElement(doc, exoneracion, "MontoExoneracion", exon.path("MontoExoneracion").asText());
                }
              }
            }
          }

          // Impuesto neto (cuando hay exoneración)
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
      Element detalleServicio = doc.createElement("DetalleServicio");
      parent.appendChild(detalleServicio);
      agregarLineaDetalleBasica(doc, detalleServicio, campoFactura);
    }
  }

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
    Element resumenFactura = doc.createElement("ResumenFactura");
    parent.appendChild(resumenFactura);

    // Código de moneda y tipo de cambio
    if (campoFactura.getTipoCambio() != null || campoFactura.getCodMoneda() != null) {
      Element codigoTipoMoneda = doc.createElement("CodigoTipoMoneda");
      resumenFactura.appendChild(codigoTipoMoneda);
      addElement(doc, codigoTipoMoneda, "CodigoMoneda", campoFactura.getCodMoneda());
      addElement(doc, codigoTipoMoneda, "TipoCambio", campoFactura.getTipoCambio());
    }

    // NUEVO: Agregar campos para exoneración
    if (campoFactura.getTotalServGravados() != null) {
      addElement(doc, resumenFactura, "TotalServGravados", campoFactura.getTotalServGravados());
    }

    if (campoFactura.getTotalServExentos() != null) {
      addElement(doc, resumenFactura, "TotalServExentos", campoFactura.getTotalServExentos());
    }

    // NUEVO: Total servicios exonerados
    if (campoFactura.getTotalServExonerado() != null) {
      addElement(doc, resumenFactura, "TotalServExonerado", campoFactura.getTotalServExonerado());
    }

    if (campoFactura.getTotalMercGravadas() != null) {
      addElement(doc, resumenFactura, "TotalMercanciasGravadas", campoFactura.getTotalMercGravadas());
    }

    if (campoFactura.getTotalMercExentas() != null) {
      addElement(doc, resumenFactura, "TotalMercanciasExentas", campoFactura.getTotalMercExentas());
    }

    // NUEVO: Total mercancías exoneradas
    if (campoFactura.getTotalMercExonerada() != null) {
      addElement(doc, resumenFactura, "TotalMercExonerada", campoFactura.getTotalMercExonerada());
    }

    addElement(doc, resumenFactura, "TotalGravado", campoFactura.getTotalGravados());
    addElement(doc, resumenFactura, "TotalExento", campoFactura.getTotalExentos());

    // NUEVO: Total exonerado
    if (campoFactura.getTotalExonerado() != null) {
      addElement(doc, resumenFactura, "TotalExonerado", campoFactura.getTotalExonerado());
    }

    addElement(doc, resumenFactura, "TotalVenta", campoFactura.getTotalVentas());

    if (campoFactura.getTotalDescuentos() != null) {
      addElement(doc, resumenFactura, "TotalDescuentos", campoFactura.getTotalDescuentos());
    }

    addElement(doc, resumenFactura, "TotalVentaNeta", campoFactura.getTotalVentasNeta());

    if (campoFactura.getTotalImp() != null) {
      addElement(doc, resumenFactura, "TotalImpuesto", campoFactura.getTotalImp());
    }

    // IVA Devuelto
    if (campoFactura.getTotalIVADevuelto() != null) {
      addElement(doc, resumenFactura, "TotalIVADevuelto", campoFactura.getTotalIVADevuelto());
    }

    // Otros cargos
    if (campoFactura.getTotalOtrosCargos() != null) {
      addElement(doc, resumenFactura, "TotalOtrosCargos", campoFactura.getTotalOtrosCargos());
    }

    addElement(doc, resumenFactura, "TotalComprobante", campoFactura.getTotalComprobante());
  }

  private void procesarInformacionReferencia(Document doc, Element parent, String informacionReferenciaJson) {
    try {
      JsonNode referenciaArray = objectMapper.readTree(informacionReferenciaJson);

      if (referenciaArray.isArray()) {
        for (JsonNode ref : referenciaArray) {
          Element informacionReferencia = doc.createElement("InformacionReferencia");
          parent.appendChild(informacionReferencia);

          addElement(doc, informacionReferencia, "TipoDoc", ref.path("TipoDoc").asText());
          addElement(doc, informacionReferencia, "Numero", ref.path("Numero").asText());
          addElement(doc, informacionReferencia, "FechaEmision", ref.path("FechaEmision").asText());
          addElement(doc, informacionReferencia, "Codigo", ref.path("Codigo").asText());
          addElement(doc, informacionReferencia, "Razon", ref.path("Razon").asText());
        }
      }
    } catch (Exception e) {
      log.error("Error procesando referencias: {}", e.getMessage());
    }
  }

  private void addElement(Document doc, Element parent, String name, String value) {
    if (value != null && !value.isEmpty()) {
      Element element = doc.createElement(name);
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

      String xmlString = baos.toString("UTF-8");

      // Log del XML generado para debug
      if (log.isDebugEnabled()) {
        log.debug("XML generado completo: {}", xmlString);
      } else {
        log.info("XML generado - Tamaño: {} caracteres", xmlString.length());
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
    return GeneraXml(paramCCampoFactura, "");
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