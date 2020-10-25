package app.simplestudio.com.mh;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import app.simplestudio.com.mh.CCampoFactura;
import app.simplestudio.com.mh.FuncionesService;
import app.simplestudio.com.mh.IGeneraXml;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GeneraXmlImpl implements IGeneraXml {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Autowired
  private FuncionesService _funcionesService;
  
  public String GeneraXml(CCampoFactura c) {
    String resp = null;
    switch (c.getClave().substring(29, 31)) {
      case "01":
        this.log.info("Se esta generando una FE");
        resp = "<?xml version=\"1.0\" encoding=\"utf-8\"?><FacturaElectronica xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.3/facturaElectronica\">";
        resp = resp + GeneraXmlDocumentos(c);
        resp = resp + "</FacturaElectronica>";
        break;
      case "02":
        this.log.info("Se esta generando una ND");
        resp = "<?xml version=\"1.0\" encoding=\"utf-8\"?><NotaDebitoElectronica xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.3/notaDebitoElectronica\">";
        resp = resp + GeneraXmlDocumentos(c);
        resp = resp + "</NotaDebitoElectronica>";
        break;
      case "03":
        this.log.info("Se esta generando una NC");
        resp = "<?xml version=\"1.0\" encoding=\"utf-8\"?><NotaCreditoElectronica xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.3/notaCreditoElectronica\">";
        resp = resp + GeneraXmlDocumentos(c);
        resp = resp + "</NotaCreditoElectronica>";
        break;
      case "04":
        this.log.info("Se esta generando una TE");
        resp = "<?xml version=\"1.0\" encoding=\"utf-8\"?><TiqueteElectronico xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.3/tiqueteElectronico\">";
        resp = resp + GeneraXmlDocumentos(c);
        resp = resp + "</TiqueteElectronico>";
        break;
      case "05":
        this.log.info("Se esta generando un MR CCE");
        resp = GeneraXmlMr(c);
        break;
      case "06":
        this.log.info("Se esta generando un MR CPCE");
        resp = GeneraXmlMr(c);
        break;
      case "07":
        this.log.info("Se esta generando un MR RCE");
        resp = GeneraXmlMr(c);
        break;
      case "08":
        this.log.info("Se esta generando una Factura electrónica de compra");
        resp = "<?xml version=\"1.0\" encoding=\"utf-8\"?><FacturaElectronicaCompra xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.3/facturaElectronicaCompra\">";
        resp = resp + GeneraXmlDocumentos(c);
        resp = resp + "</FacturaElectronicaCompra>";
        break;
      case "09":
        this.log.info("Se esta generando una Factura electrónica de exportación");
        resp = "<?xml version=\"1.0\" encoding=\"utf-8\"?><FacturaElectronicaExportacion xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.3/facturaElectronicaExportacion\">";
        resp = resp + GeneraXmlDocumentos(c);
        resp = resp + "</FacturaElectronicaExportacion>";
        break;
    } 
    return resp;
  }
  
  public String GeneraXmlDocumentos(CCampoFactura c) {
    String xmlString = "";
    String tipoDoc = c.getClave().substring(29, 31);
    xmlString = xmlString + "<Clave>" + c.getClave() + "</Clave>";
    xmlString = xmlString + "<CodigoActividad>" + this._funcionesService.str_pad(c.getCodigoActividad(), 6, "0", "STR_PAD_LEFT") + "</CodigoActividad>";
    xmlString = xmlString + "<NumeroConsecutivo>" + c.getConsecutivo() + "</NumeroConsecutivo>";
    xmlString = xmlString + "<FechaEmision>" + c.getFechaEmision() + "</FechaEmision>";
    xmlString = xmlString + "<Emisor>";
    xmlString = xmlString + "<Nombre>" + procesarTexto(c.getEmisorNombre()) + "</Nombre>";
    xmlString = xmlString + "<Identificacion>";
    xmlString = xmlString + "<Tipo>" + this._funcionesService.str_pad(c.getEmisorTipoIdentif(), 2, "0", "STR_PAD_LEFT") + "</Tipo>";
    xmlString = xmlString + "<Numero>" + c.getEmisorNumIdentif() + "</Numero>";
    xmlString = xmlString + "</Identificacion>";
    if (c.getNombreComercial() != null)
      xmlString = xmlString + "<NombreComercial>" + procesarTexto(c.getNombreComercial()) + "</NombreComercial>"; 
    if (c.getEmisorProv() != null && !c.getEmisorProv().equals("") && c.getEmisorCanton() != null && 
      !c.getEmisorCanton().equals("") && c.getEmisorDistrito() != null && !c.getEmisorDistrito().equals("") && c
      .getEmisorOtrasSenas() != null && !c.getEmisorOtrasSenas().equals("")) {
      xmlString = xmlString + "<Ubicacion>";
      xmlString = xmlString + "<Provincia>" + c.getEmisorProv() + "</Provincia>";
      xmlString = xmlString + "<Canton>" + this._funcionesService.str_pad(c.getEmisorCanton(), 2, "0", "STR_PAD_LEFT") + "</Canton>";
      xmlString = xmlString + "<Distrito>" + this._funcionesService.str_pad(c.getEmisorDistrito(), 2, "0", "STR_PAD_LEFT") + "</Distrito>";
      if (c.getEmisorBarrio() != "")
        xmlString = xmlString + "<Barrio>" + this._funcionesService.str_pad(c.getEmisorBarrio(), 2, "0", "STR_PAD_LEFT") + "</Barrio>"; 
      xmlString = xmlString + "<OtrasSenas>" + c.getEmisorOtrasSenas() + "</OtrasSenas>";
      xmlString = xmlString + "</Ubicacion>";
    } 
    if (c.getEmisorCodPaisTel() != null && !c.getEmisorCodPaisTel().equals("") && c.getEmisorTel() != null && 
      !c.getEmisorTel().equals("")) {
      xmlString = xmlString + "<Telefono>";
      xmlString = xmlString + "<CodigoPais>" + c.getEmisorCodPaisTel() + "</CodigoPais>";
      xmlString = xmlString + "<NumTelefono>" + c.getEmisorTel() + "</NumTelefono>";
      xmlString = xmlString + "</Telefono>";
    } 
    if (c.getEmisorCodPaisFax() != null && !c.getEmisorCodPaisFax().equals("") && c.getEmisorFax() != null && 
      !c.getEmisorFax().equals("")) {
      xmlString = xmlString + "<Fax>";
      xmlString = xmlString + "<CodigoPais>" + c.getEmisorCodPaisFax() + "</CodigoPais>";
      xmlString = xmlString + "<NumTelefono>" + c.getEmisorFax() + "</NumTelefono>";
      xmlString = xmlString + "</Fax>";
    } 
    xmlString = xmlString + "<CorreoElectronico>" + procesarTexto(c.getEmisorEmail()) + "</CorreoElectronico>";
    xmlString = xmlString + "</Emisor>";
    if (c.getOmitirReceptor().equals("false")) {
      xmlString = xmlString + "<Receptor>";
      xmlString = xmlString + "<Nombre>" + procesarTexto(c.getReceptorNombre()) + "</Nombre>";
      if ((c.getReceptorTipoIdentif() != null && c.getReceptorTipoIdentif().equals("05")) || c.getReceptorTipoIdentif().equals("5")) {
        xmlString = xmlString + "<IdentificacionExtranjero>" + c.getReceptorNumIdentif() + "</IdentificacionExtranjero>";
        if (c.getReceptorOtrasSenas() != null && c.getReceptorOtrasSenas().length() > 0)
          xmlString = xmlString + "<OtrasSenasExtranjero>" + procesarTexto(c.getReceptorOtrasSenas()) + "</OtrasSenasExtranjero>"; 
      } else {
        if (c.getReceptorTipoIdentif() != null && !c.getReceptorTipoIdentif().equals("") && c
          .getReceptorNumIdentif() != null && !c.getReceptorNumIdentif().equals("")) {
          xmlString = xmlString + "<Identificacion>";
          xmlString = xmlString + "<Tipo>" + this._funcionesService.str_pad(c.getReceptorTipoIdentif(), 2, "0", "STR_PAD_LEFT") + "</Tipo>";
          xmlString = xmlString + "<Numero>" + c.getReceptorNumIdentif() + "</Numero>";
          xmlString = xmlString + "</Identificacion>";
        } 
        if (!tipoDoc.equals("09"))
          if (c.getReceptorProvincia() != null && !c.getReceptorProvincia().equals("") && c
            .getReceptorCanton() != null && !c.getReceptorCanton().equals("") && c
            .getReceptorDistrito() != null && !c.getReceptorDistrito().equals("") && c
            .getReceptorOtrasSenas() != null && !c.getReceptorOtrasSenas().equals("")) {
            xmlString = xmlString + "<Ubicacion>";
            xmlString = xmlString + "<Provincia>" + c.getReceptorProvincia() + "</Provincia>";
            xmlString = xmlString + "<Canton>" + this._funcionesService.str_pad(c.getReceptorCanton(), 2, "0", "STR_PAD_LEFT") + "</Canton>";
            xmlString = xmlString + "<Distrito>" + this._funcionesService.str_pad(c.getReceptorDistrito(), 2, "0", "STR_PAD_LEFT") + "</Distrito>";
            if (c.getReceptorBarrio() != null && !c.getReceptorBarrio().equals(""))
              xmlString = xmlString + "<Barrio>" + this._funcionesService.str_pad(c.getReceptorBarrio(), 2, "0", "STR_PAD_LEFT") + "</Barrio>"; 
            xmlString = xmlString + "<OtrasSenas>" + c.getReceptorOtrasSenas() + "</OtrasSenas>";
            xmlString = xmlString + "</Ubicacion>";
          }  
      } 
      if (c.getReceptorCodPaisTel() != null && !c.getReceptorCodPaisTel().equals("") && c.getReceptorTel() != null && 
        !c.getReceptorTel().equals("")) {
        xmlString = xmlString + "<Telefono>";
        xmlString = xmlString + "<CodigoPais>" + c.getReceptorCodPaisTel() + "</CodigoPais>";
        xmlString = xmlString + "<NumTelefono>" + c.getReceptorTel() + "</NumTelefono>";
        xmlString = xmlString + "</Telefono>";
      } 
      if (c.getReceptorCodPaisFax() != null && !c.getReceptorCodPaisFax().equals("") && c.getReceptorFax() != null && 
        !c.getReceptorFax().equals("")) {
        xmlString = xmlString + "<Fax>";
        xmlString = xmlString + "<CodigoPais>" + c.getReceptorCodPaisFax() + "</CodigoPais>";
        xmlString = xmlString + "<NumTelefono>" + c.getReceptorFax() + "</NumTelefono>";
        xmlString = xmlString + "</Fax>";
      } 
      if (c.getReceptorEmail() != null && !c.getReceptorEmail().equals(""))
        xmlString = xmlString + "<CorreoElectronico>" + c.getReceptorEmail() + "</CorreoElectronico>"; 
      xmlString = xmlString + "</Receptor>";
    } else if (c.getReceptorNombre() != null && c.getReceptorNombre().length() > 0) {
      xmlString = xmlString + "<Receptor>";
      xmlString = xmlString + "<Nombre>" + procesarTexto(c.getReceptorNombre()) + "</Nombre>";
      xmlString = xmlString + "</Receptor>";
    } 
    xmlString = xmlString + "<CondicionVenta>" + this._funcionesService.str_pad(c.getCondVenta(), 2, "0", "STR_PAD_LEFT") + "</CondicionVenta>";
    xmlString = xmlString + "<PlazoCredito>" + c.getPlazoCredito() + "</PlazoCredito>";
    ObjectMapper objectMapper = new ObjectMapper();
    if (c.getMedioPago() != null && c.getMedioPago().length() > 0)
      xmlString = xmlString + "<MedioPago>" + this._funcionesService.str_pad(c.getMedioPago(), 2, "0", "STR_PAD_LEFT") + "</MedioPago>"; 
    if (c.getMedioPago2() != null && c.getMedioPago2().length() > 0)
      xmlString = xmlString + "<MedioPago>" + this._funcionesService.str_pad(c.getMedioPago2(), 2, "0", "STR_PAD_LEFT") + "</MedioPago>"; 
    if (c.getMedioPago3() != null && c.getMedioPago3().length() > 0)
      xmlString = xmlString + "<MedioPago>" + this._funcionesService.str_pad(c.getMedioPago3(), 2, "0", "STR_PAD_LEFT") + "</MedioPago>"; 
    if (c.getMedioPago4() != null && c.getMedioPago4().length() > 0)
      xmlString = xmlString + "<MedioPago>" + this._funcionesService.str_pad(c.getMedioPago4(), 2, "0", "STR_PAD_LEFT") + "</MedioPago>"; 
    xmlString = xmlString + "<DetalleServicio>";
    String impuestosJson = "";
    String codigosComerciales = "";
    String descuentos = "";
    try {
      JsonNode rootNode = objectMapper.readTree(c.getDetalleFactura());
      Iterator<JsonNode> elementsDetalleFactura = rootNode.elements();
      this.log.info("detalle factura " + c.getDetalleFactura());
      while (elementsDetalleFactura.hasNext()) {
        JsonNode k = elementsDetalleFactura.next();
        xmlString = xmlString + "<LineaDetalle>";
        xmlString = xmlString + "<NumeroLinea>" + k.path("numeroLinea").asText() + "</NumeroLinea>";
        if (!tipoDoc.equals("01") && !tipoDoc.equals("07") && !tipoDoc.equals("04"))
          if (k.path("partidaArancelaria") != null && !k.path("partidaArancelaria").asText().equals(""))
            xmlString = xmlString + "<PartidaArancelaria>" + k.path("partidaArancelaria").asText() + "</PartidaArancelaria>";  
        xmlString = xmlString + "<Codigo>" + k.path("codigo").asText() + "</Codigo>";
        if (codigosComerciales != null && codigosComerciales.length() > 0) {
          codigosComerciales = k.path("codigoComercial").toString();
          try {
            rootNode = objectMapper.readTree(codigosComerciales);
            Iterator<JsonNode> elementsCodigoComercial = rootNode.elements();
            while (elementsCodigoComercial.hasNext()) {
              JsonNode cc = elementsCodigoComercial.next();
              xmlString = xmlString + "<CodigoComercial>";
              xmlString = xmlString + "<Tipo>" + this._funcionesService.str_pad(cc.path("tipo").asText(), 2, "0", "STR_PAD_LEFT") + "</Tipo>";
              xmlString = xmlString + "<Codigo>" + procesarTexto(cc.path("codigo").asText()) + "</Codigo>";
              xmlString = xmlString + "</CodigoComercial>";
            } 
          } catch (IOException e) {
            e.printStackTrace();
          } 
        } 
        xmlString = xmlString + "<Cantidad>" + k.path("cantidad").asText() + "</Cantidad>";
        xmlString = xmlString + "<UnidadMedida>" + k.path("unidadMedida").asText() + "</UnidadMedida>";
        if (k.path("unidadMedidaComercial").asText() != null && k.path("unidadMedidaComercial").asText().length() > 0)
          xmlString = xmlString + "<UnidadMedidaComercial>" + k.path("unidadMedidaComercial").asText() + "</UnidadMedidaComercial>"; 
        xmlString = xmlString + "<Detalle>" + procesarTexto(k.path("detalle").asText()) + "</Detalle>";
        xmlString = xmlString + "<PrecioUnitario>" + k.path("precioUnitario").asText() + "</PrecioUnitario>";
        xmlString = xmlString + "<MontoTotal>" + k.path("montoTotal").asText() + "</MontoTotal>";
        descuentos = k.path("descuentos").toString();
        if (descuentos != null && descuentos.length() > 0)
          try {
            rootNode = objectMapper.readTree(descuentos);
            Iterator<JsonNode> elementsMontoDescuento = rootNode.elements();
            while (elementsMontoDescuento.hasNext()) {
              JsonNode md = elementsMontoDescuento.next();
              if (md.get("montoDescuento").asDouble() > 0.0D) {
                xmlString = xmlString + "<Descuento>";
                xmlString = xmlString + "<MontoDescuento>" + this._funcionesService.str_pad(md.get("montoDescuento").asText(), 2, "0", "STR_PAD_LEFT") + "</MontoDescuento>";
                xmlString = xmlString + "<NaturalezaDescuento>" + procesarTexto(md.path("naturalezaDescuento").asText()) + "</NaturalezaDescuento>";
                xmlString = xmlString + "</Descuento>";
              } 
            } 
          } catch (IOException e) {
            e.printStackTrace();
          }  
        xmlString = xmlString + "<SubTotal>" + k.path("subTotal").asText() + "</SubTotal>";
        if (k.path("baseImponible").asText() != null && k.path("baseImponible").asText().length() > 0)
          xmlString = xmlString + "<BaseImponible>" + k.path("baseImponible").asText() + "</BaseImponible>"; 
        impuestosJson = k.path("impuestos").toString();
        if (impuestosJson != null && impuestosJson.length() > 0) {
          rootNode = objectMapper.readTree(impuestosJson);
          Iterator<JsonNode> impuestos = rootNode.elements();
          while (impuestos.hasNext()) {
            JsonNode j = impuestos.next();
            xmlString = xmlString + "<Impuesto>";
            xmlString = xmlString + "<Codigo>" + this._funcionesService.str_pad(j.path("codigo").asText(), 2, "0", "STR_PAD_LEFT") + "</Codigo>";
            xmlString = xmlString + "<CodigoTarifa>" + this._funcionesService.str_pad(j.path("codigoTarifa").asText(), 2, "0", "STR_PAD_LEFT") + "</CodigoTarifa>";
            xmlString = xmlString + "<Tarifa>" + j.path("tarifa").asText() + "</Tarifa>";
            if (j.path("factorIVA").asText() != null && j.path("factorIVA").asText().length() > 0)
              xmlString = xmlString + "<FactorIVA>" + j.path("factorIVA").asText() + "</FactorIVA>"; 
            xmlString = xmlString + "<Monto>" + j.path("monto").asText() + "</Monto>";
            if (!tipoDoc.equals("09"))
              if (j.path("exoneracion").path("tipoDocumento").asText() != null && j.path("exoneracion").path("tipoDocumento").asText().length() > 0) {
                xmlString = xmlString + "<Exoneracion>";
                xmlString = xmlString + "<TipoDocumento>" + this._funcionesService.str_pad(j.path("exoneracion").path("tipoDocumento").asText(), 2, "0", "STR_PAD_LEFT") + "</TipoDocumento>";
                xmlString = xmlString + "<NumeroDocumento>" + j.path("exoneracion").path("numeroDocumento").asText() + "</NumeroDocumento>";
                xmlString = xmlString + "<NombreInstitucion>" + procesarTexto(j.path("exoneracion").path("nombreInstitucion").asText()) + "</NombreInstitucion>";
                xmlString = xmlString + "<FechaEmision>" + j.path("exoneracion").path("fechaEmision").asText() + "</FechaEmision>";
                xmlString = xmlString + "<PorcentajeExoneracion>" + j.path("exoneracion").path("porcentajeExoneracion").asText() + "</PorcentajeExoneracion>";
                xmlString = xmlString + "<MontoExoneracion>" + j.path("exoneracion").path("montoExoneracion").asText() + "</MontoExoneracion>";
                xmlString = xmlString + "</Exoneracion>";
              }  
            xmlString = xmlString + "</Impuesto>";
          } 
        } 
        xmlString = xmlString + "<ImpuestoNeto>" + k.path("impuestoNeto").asText() + "</ImpuestoNeto>";
        xmlString = xmlString + "<MontoTotalLinea>" + k.path("montoTotalLinea").asText() + "</MontoTotalLinea>";
        xmlString = xmlString + "</LineaDetalle>";
      } 
    } catch (IOException e) {
      e.printStackTrace();
    } 
    xmlString = xmlString + "</DetalleServicio>";
    ObjectMapper objectMapperOtrosCargos = new ObjectMapper();
    try {
      if (c.getOtrosCargos() != null && c.getOtrosCargos().length() > 0) {
        JsonNode rootNodeOtrosCargos = objectMapperOtrosCargos.readTree(c.getOtrosCargos());
        Iterator<JsonNode> elementsOtrosCargos = rootNodeOtrosCargos.elements();
        while (elementsOtrosCargos.hasNext()) {
          JsonNode s = elementsOtrosCargos.next();
          xmlString = xmlString + "<OtrosCargos>";
          xmlString = xmlString + "<TipoDocumento>" + this._funcionesService.str_pad(s.path("tipoDocumento").asText(), 2, "0", "STR_PAD_LEFT") + "</TipoDocumento>";
          if (s.path("numeroIdentidadTercero") != null && s.path("numeroIdentidadTercero").asText().trim().length() > 0)
            xmlString = xmlString + "<NumeroIdentidadTercero>" + s.path("numeroIdentidadTercero").asText() + "</NumeroIdentidadTercero>"; 
          if (s.path("nombreTercero") != null && s.path("nombreTercero").asText().trim().length() > 0)
            xmlString = xmlString + "<NombreTercero>" + s.path("nombreTercero").asText() + "</NombreTercero>"; 
          xmlString = xmlString + "<Detalle>" + procesarTexto(s.path("detalle").asText()) + "</Detalle>";
          if (s.path("porcentaje") != null && s.path("porcentaje").asText().trim().length() > 0)
            xmlString = xmlString + "<Porcentaje>" + s.path("porcentaje").asText() + "</Porcentaje>"; 
          xmlString = xmlString + "<MontoCargo>" + s.path("montoCargo").asText() + "</MontoCargo>";
          xmlString = xmlString + "</OtrosCargos>";
        } 
      } 
    } catch (Exception e) {
      e.printStackTrace();
    } 
    xmlString = xmlString + "<ResumenFactura>";
    if (c.getCodMoneda() != null && c.getCodMoneda().length() > 0 && c.getTipoCambio() != null && c.getTipoCambio().length() > 0) {
      xmlString = xmlString + "<CodigoTipoMoneda>";
      xmlString = xmlString + "<CodigoMoneda>" + c.getCodMoneda() + "</CodigoMoneda>";
      xmlString = xmlString + "<TipoCambio>" + c.getTipoCambio() + "</TipoCambio>";
      xmlString = xmlString + "</CodigoTipoMoneda>";
    } 
    xmlString = xmlString + "<TotalServGravados>" + c.getTotalServGravados() + "</TotalServGravados>";
    xmlString = xmlString + "<TotalServExentos>" + c.getTotalServExentos() + "</TotalServExentos>";
    if (!tipoDoc.equals("09"))
      if (c.getTotalServExonerado() != null && !c.getTotalServExonerado().equals(""))
        xmlString = xmlString + "<TotalServExonerado>" + c.getTotalServExonerado() + "</TotalServExonerado>";  
    xmlString = xmlString + "<TotalMercanciasGravadas>" + c.getTotalMercGravadas() + "</TotalMercanciasGravadas>";
    xmlString = xmlString + "<TotalMercanciasExentas>" + c.getTotalMercExentas() + "</TotalMercanciasExentas>";
    if (!tipoDoc.equals("09"))
      if (c.getTotalMercExonerada() != null && !c.getTotalMercExonerada().equals(""))
        xmlString = xmlString + "<TotalMercExonerada>" + c.getTotalMercExonerada() + "</TotalMercExonerada>";  
    xmlString = xmlString + "<TotalGravado>" + c.getTotalGravados() + "</TotalGravado>";
    xmlString = xmlString + "<TotalExento>" + c.getTotalExentos() + "</TotalExento>";
    if (!tipoDoc.equals("09"))
      if (c.getTotalExonerado() != null && !c.getTotalExonerado().equals(""))
        xmlString = xmlString + "<TotalExonerado>" + c.getTotalExonerado() + "</TotalExonerado>";  
    xmlString = xmlString + "<TotalVenta>" + c.getTotalVentas() + "</TotalVenta>";
    xmlString = xmlString + "<TotalDescuentos>" + c.getTotalDescuentos() + "</TotalDescuentos>";
    xmlString = xmlString + "<TotalVentaNeta>" + c.getTotalVentasNeta() + "</TotalVentaNeta>";
    xmlString = xmlString + "<TotalImpuesto>" + c.getTotalImp() + "</TotalImpuesto>";
    if (!tipoDoc.equals("08") && !tipoDoc.equals("09"))
      if (c.getTotalIVADevuelto() != null && !c.getTotalIVADevuelto().equals("") && Double.parseDouble(c.getTotalIVADevuelto()) > 0.0D)
        xmlString = xmlString + "<TotalIVADevuelto>" + c.getTotalIVADevuelto() + "</TotalIVADevuelto>";  
    if (c.getTotalOtrosCargos() != null && Double.parseDouble(c.getTotalOtrosCargos()) > 0.0D && 
      c.getTotalOtrosCargos() != null && !c.getTotalOtrosCargos().equals(""))
      xmlString = xmlString + "<TotalOtrosCargos>" + c.getTotalOtrosCargos() + "</TotalOtrosCargos>"; 
    xmlString = xmlString + "<TotalComprobante>" + c.getTotalComprobante() + "</TotalComprobante>";
    xmlString = xmlString + "</ResumenFactura>";
    ObjectMapper objectMapperReferencias = new ObjectMapper();
    try {
      if (c.getReferencia() != null && c.getReferencia().length() > 0) {
        JsonNode rootNodeReferencias = objectMapperReferencias.readTree(c.getReferencia());
        Iterator<JsonNode> elementsReferencia = rootNodeReferencias.elements();
        while (elementsReferencia.hasNext()) {
          JsonNode s = elementsReferencia.next();
          xmlString = xmlString + "<InformacionReferencia>";
          xmlString = xmlString + "<TipoDoc>" + s.path("numero").asText().substring(29, 31) + "</TipoDoc>";
          xmlString = xmlString + "<Numero>" + s.path("numero").asText() + "</Numero>";
          xmlString = xmlString + "<FechaEmision>" + s.path("fechaEmision").asText() + "</FechaEmision>";
          xmlString = xmlString + "<Codigo>" + this._funcionesService.str_pad(s.path("codigo").asText(), 2, "0", "STR_PAD_LEFT") + "</Codigo>";
          xmlString = xmlString + "<Razon>" + procesarTexto(s.path("razon").asText()) + "</Razon>";
          xmlString = xmlString + "</InformacionReferencia>";
        } 
      } 
    } catch (Exception e) {
      e.printStackTrace();
    } 
    xmlString = xmlString + "<Otros>";
    if (c.getOtros() != null && c.getOtros().length() > 0)
      xmlString = xmlString + "<OtroTexto>" + procesarTexto(c.getOtros()) + "</OtroTexto>"; 
    xmlString = xmlString + "<OtroContenido>";
    xmlString = xmlString + "<ContactoDesarrollador xmlns=\"https://simplestudio.app\">";
    xmlString = xmlString + "<ProveedorSistemaComprobantesElectronicos>";
    xmlString = xmlString + "<Nombre>SimpleStudio</Nombre>";
    xmlString = xmlString + "<Identificacion>";
    xmlString = xmlString + "<Tipo>01</Tipo>";
    xmlString = xmlString + "<Numero>114970286</Numero>";
    xmlString = xmlString + "</Identificacion>";
    xmlString = xmlString + "<CorreoElectronico>hello@simplestudio.app</CorreoElectronico>";
    xmlString = xmlString + "</ProveedorSistemaComprobantesElectronicos>";
    xmlString = xmlString + "</ContactoDesarrollador>";
    xmlString = xmlString + "</OtroContenido>";
    xmlString = xmlString + "</Otros>";
    return xmlString;
  }
  
  public String GeneraXmlMr(CCampoFactura mr) {
    String xmlString = "";
    String numeroCedulaEmisor = this._funcionesService.str_pad(mr.getNumeroCedulaEmisor(), 12, "0", "STR_PAD_LEFT");
    String numeroCedulaReceptor = this._funcionesService.str_pad(mr.getNumeroCedulaReceptor(), 12, "0", "STR_PAD_LEFT");
    xmlString = xmlString + "<?xml version=\"1.0\" encoding=\"utf-8\"?><MensajeReceptor xmlns=\"https://cdn.comprobanteselectronicos.go.cr/xml-schemas/v4.3/mensajeReceptor\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
    xmlString = xmlString + "<Clave>" + mr.getClaveDocumentoEmisor() + "</Clave>";
    xmlString = xmlString + "<NumeroCedulaEmisor>" + numeroCedulaEmisor + "</NumeroCedulaEmisor>";
    xmlString = xmlString + "<FechaEmisionDoc>" + mr.getFechaEmisionDoc() + "</FechaEmisionDoc>";
    xmlString = xmlString + "<Mensaje>" + procesarTexto(mr.getMensaje()) + "</Mensaje>";
    if (mr.getMensaje() != null && mr.getMensaje().length() > 0)
      xmlString = xmlString + "<DetalleMensaje>" + procesarTexto(mr.getDetalleMensaje()) + "</DetalleMensaje>"; 
    if (mr.getMontoTotalImpuesto() != null && mr.getMontoTotalImpuesto().length() > 0)
      xmlString = xmlString + "<MontoTotalImpuesto>" + mr.getMontoTotalImpuesto() + "</MontoTotalImpuesto>"; 
    if (mr.getCondicionImpuesto() != null && mr.getCondicionImpuesto() != "05" && mr.getCodigoActividad() != null && mr.getCodigoActividad().length() > 0)
      xmlString = xmlString + "<CodigoActividad>" + mr.getCodigoActividad() + "</CodigoActividad>"; 
    if (mr.getCondicionImpuesto() != null && mr.getCondicionImpuesto().length() > 0)
      xmlString = xmlString + "<CondicionImpuesto>" + mr.getCondicionImpuesto() + "</CondicionImpuesto>"; 
    if ((mr.getCondicionImpuesto() != null && !mr.getCondicionImpuesto().equals("01")) || !mr.getCondicionImpuesto().equals("04") || !mr.getCondicionImpuesto().equals("05")) {
      if (mr.getCondicionImpuesto() != null && !mr.getCondicionImpuesto().equals("03") && 
        mr.getMontoTotalImpuestoAcreditar() != null && Double.parseDouble(mr.getMontoTotalImpuestoAcreditar()) > 0.0D)
        xmlString = xmlString + "<MontoTotalImpuestoAcreditar>" + mr.getMontoTotalImpuesto() + "</MontoTotalImpuestoAcreditar>"; 
      if (mr.getCondicionImpuesto() != null && !mr.getCondicionImpuesto().equals("02")) {
        if (mr.getMontoTotalImpuestoAcreditar() != null && Double.parseDouble(mr.getMontoTotalImpuestoAcreditar()) > 0.0D)
          xmlString = xmlString + "<MontoTotalImpuestoAcreditar>" + mr.getMontoTotalImpuestoAcreditar() + "</MontoTotalImpuestoAcreditar>"; 
        if (mr.getMontoTotalDeGastoAplicable() != null && Double.parseDouble(mr.getMontoTotalDeGastoAplicable()) > 0.0D)
          xmlString = xmlString + "<MontoTotalDeGastoAplicable>" + mr.getMontoTotalDeGastoAplicable() + "</MontoTotalDeGastoAplicable>"; 
      } 
    } 
    xmlString = xmlString + "<TotalFactura>" + mr.getTotalFactura() + "</TotalFactura>";
    xmlString = xmlString + "<NumeroCedulaReceptor>" + numeroCedulaReceptor + "</NumeroCedulaReceptor>";
    xmlString = xmlString + "<NumeroConsecutivoReceptor>" + mr.getNumeroConsecutivoReceptor() + "</NumeroConsecutivoReceptor>";
    xmlString = xmlString + "</MensajeReceptor>";
    return xmlString;
  }
  
  public void generateXml(String path, String datosXml, String name) throws Exception {
    BufferedWriter bw;
    File archivo = new File(path + name + ".xml");
    if (archivo.exists()) {
      bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8));
      bw.write(datosXml);
      System.out.println("Archivo creado con éxito");
    } else {
      bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8));
      bw.write(datosXml);
      System.out.println("Archivo creado con éxito");
    } 
    bw.close();
  }
  
  public String procesarNumeros(String j, String decimales) {
    NumberFormat formatter = new DecimalFormat(decimales);
    String r = "";
    r = (j != null && !j.equals("")) ? j : "0.00";
    r = formatter.format(Double.parseDouble(r));
    r = r.replaceAll(",", ".");
    return r;
  }
  
  public String procesarTexto(String j) {
    String r = "";
    r = StringEscapeUtils.escapeXml(j);
    return r;
  }
}

