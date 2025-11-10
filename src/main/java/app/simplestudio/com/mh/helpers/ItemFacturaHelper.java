package app.simplestudio.com.mh.helpers;

import app.simplestudio.com.models.entity.*;
import app.simplestudio.com.models.entity.ItemSurtido.ImpuestoComponente;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Iterator;
import java.util.Map;

@Component
public class ItemFacturaHelper {

  @Autowired
  private ObjectMapper objectMapper;

  public void procesarItemsFactura(Factura factura, JsonNode detalleLineaNode,
      Map<String, Object> response) throws Exception {
    Iterator<JsonNode> elements = detalleLineaNode.elements();

    while (elements.hasNext()) {
      JsonNode k = elements.next();
      ItemFactura item = new ItemFactura();

      // Datos básicos del item
      item.setNumeroLinea(k.path("numeroLinea").asInt());
      item.setPartidaArancelaria(k.path("partidaArancelaria").asText());
      item.setCodigo(k.path("codigo").asText());

      // Procesar códigos comerciales
      procesarCodigosComerciales(item, k.path("codigoComercial"));

      // Datos del item
      item.setCantidad(k.path("cantidad").asDouble());
      item.setUnidadMedida(k.path("unidadMedida").asText());
      item.setUnidadMedidaComercial(k.path("unidadMedidaComercial").asText());
      item.setDetalle(k.path("detalle").asText());
      item.setPrecioUnitario(k.path("precioUnitario").asDouble());
      item.setMontoTotal(k.path("montoTotal").asDouble());
      item.setSubTotal(k.path("subTotal").asDouble());

      // Procesar descuentos
      procesarDescuentos(item, k.path("descuentos"));

      // Procesar impuestos
      procesarImpuestos(item, k.path("impuestos"), response);

      item.setImpuestoNeto(k.path("impuestoNeto").asDouble());
      item.setMontoTotalLinea(k.path("montoTotalLinea").asDouble());

      // --- 4.4: detalleSurtido (componentes del combo) ---
      JsonNode detalleSurtidoNode = k.path("detalleSurtido");
      if (detalleSurtidoNode != null && detalleSurtidoNode.isArray()) {
        List<ItemSurtido> comps = new ArrayList<>();
        for (JsonNode comp : detalleSurtidoNode) {
          ItemSurtido is = new ItemSurtido();
          is.setCabysComponente(comp.path("cabysComponente").asText(null));
          is.setDescripcionComponente(comp.path("descripcionComponente").asText(null));
          is.setCantidadComponente(comp.path("cantidadComponente").isNumber()
              ? comp.path("cantidadComponente").asDouble() : null);
          is.setPrecioUnitarioComponente(comp.path("precioUnitarioComponente").isNumber()
              ? comp.path("precioUnitarioComponente").asDouble() : null);

          // Descuento por componente (opcional)
          if (comp.has("montoDescuentoComponente")) {
            is.setMontoDescuentoComponente(
                comp.path("montoDescuentoComponente").isNumber()
                    ? comp.path("montoDescuentoComponente").asDouble() : null
            );
          }
          is.setCodigoDescuentoComponente(comp.path("codigoDescuentoComponente").asText(null));
          is.setCodigoDescuentoOtroComponente(
              comp.path("codigoDescuentoOtroComponente").asText(null));
          is.setNaturalezaDescuentoComponente(
              comp.path("naturalezaDescuentoComponente").asText(null));

          // Impuestos por componente (opcional)
          JsonNode impArr = comp.path("impuestosComponente");
          if (impArr != null && impArr.isArray()) {
            List<ImpuestoComponente> impList = new ArrayList<>();
            for (JsonNode imp : impArr) {
              ItemSurtido.ImpuestoComponente ic = new ItemSurtido.ImpuestoComponente();
              ic.setCodigo(imp.path("codigo").asText(null));
              ic.setTarifa(imp.path("tarifa").asText(null));
              ic.setCodigoTarifa(imp.path("codigoTarifa").asText(null));
              ic.setMonto(imp.path("monto").isNumber() ? imp.path("monto").asDouble() : null);
              impList.add(ic);
            }
            is.setImpuestosComponente(impList);
          }

          comps.add(is);
        }
        item.setSurtidoComponentes(comps);
      }

      factura.addItemFactura(item);
    }
  }

  private void procesarCodigosComerciales(ItemFactura item, JsonNode codigoComercialNode)
      throws Exception {
    String codigoComercial = codigoComercialNode.toString();
    if (codigoComercial != null && !codigoComercial.isEmpty() && !codigoComercial.equals("{}")) {
      JsonNode m = objectMapper.readTree(codigoComercial);
      Iterator<JsonNode> codigosComerciales = m.elements();
      int countCC = 0;

      while (codigosComerciales.hasNext() && countCC < 5) {
        JsonNode cc = codigosComerciales.next();
        countCC++;

        switch (countCC) {
          case 1:
            item.setCodigoComercialTipo(cc.path("tipo").asText());
            item.setCodigoComercialCodigo(cc.path("codigo").asText());
            break;
          case 2:
            item.setCodigoComercialTipo2(cc.path("tipo").asText());
            item.setCodigoComercialCodigo2(cc.path("codigo").asText());
            break;
          case 3:
            item.setCodigoComercialTipo3(cc.path("tipo").asText());
            item.setCodigoComercialCodigo3(cc.path("codigo").asText());
            break;
          case 4:
            item.setCodigoComercialTipo4(cc.path("tipo").asText());
            item.setCodigoComercialCodigo4(cc.path("codigo").asText());
            break;
          case 5:
            item.setCodigoComercialTipo5(cc.path("tipo").asText());
            item.setCodigoComercialCodigo5(cc.path("codigo").asText());
            break;
        }
      }
    }
  }

  private void procesarDescuentos(ItemFactura item, JsonNode descuentosNode) throws Exception {
    if (descuentosNode != null && descuentosNode.isArray() && !descuentosNode.isEmpty()) {
      Iterator<JsonNode> descuentos = descuentosNode.elements();
      int countCC = 0;

      while (descuentos.hasNext() && countCC < 5) {
        JsonNode dd = descuentos.next();
        countCC++;

        String cod = dd.path("codigoDescuento").asText(null);         // "01".."09","99"
        String codOtro = dd.path("codigoDescuentoOTRO").asText(null); // requerido si cod="99"
        String nat = dd.path("naturalezaDescuento").asText(null);
        double monto = dd.path("montoDescuento").asDouble(0.0);

        switch (countCC) {
          case 1:
            item.setMontoDescuento(monto);
            item.setNaturalezaDescuento(nat);
            item.setCodigoDescuento(cod);
            item.setCodigoDescuentoOtro(codOtro);
            break;
          case 2:
            item.setMontoDescuento2(monto);
            item.setNaturalezaDescuento2(nat);
            item.setCodigoDescuento2(cod);
            item.setCodigoDescuentoOtro2(codOtro);
            break;
          case 3:
            item.setMontoDescuento3(monto);
            item.setNaturalezaDescuento3(nat);
            item.setCodigoDescuento3(cod);
            item.setCodigoDescuentoOtro3(codOtro);
            break;
          case 4:
            item.setMontoDescuento4(monto);
            item.setNaturalezaDescuento4(nat);
            item.setCodigoDescuento4(cod);
            item.setCodigoDescuentoOtro4(codOtro);
            break;
          case 5:
            item.setMontoDescuento5(monto);
            item.setNaturalezaDescuento5(nat);
            item.setCodigoDescuento5(cod);
            item.setCodigoDescuentoOtro5(codOtro);
            break;
        }
      }

      // Validación suave (igual que la tuya)
      try {
        boolean faltanCodigos = false;
        if (item.getMontoDescuento()  != null && item.getMontoDescuento()  > 0 && (item.getCodigoDescuento()  == null || item.getCodigoDescuento().isEmpty()))   faltanCodigos = true;
        if (item.getMontoDescuento2() != null && item.getMontoDescuento2() > 0 && (item.getCodigoDescuento2() == null || item.getCodigoDescuento2().isEmpty()))   faltanCodigos = true;
        if (item.getMontoDescuento3() != null && item.getMontoDescuento3() > 0 && (item.getCodigoDescuento3() == null || item.getCodigoDescuento3().isEmpty()))   faltanCodigos = true;
        if (item.getMontoDescuento4() != null && item.getMontoDescuento4() > 0 && (item.getCodigoDescuento4() == null || item.getCodigoDescuento4().isEmpty()))   faltanCodigos = true;
        if (item.getMontoDescuento5() != null && item.getMontoDescuento5() > 0 && (item.getCodigoDescuento5() == null || item.getCodigoDescuento5().isEmpty()))   faltanCodigos = true;

        if (faltanCodigos) {
          System.out.println("[WARN] 4.4: montoDescuento>0 pero falta codigoDescuento en alguna entrada; se sigue para compat 4.3");
        }
      } catch (Exception ignore) {}
    }
  }

  private void procesarImpuestos(ItemFactura item, JsonNode impuestosNode,
      Map<String, Object> response) throws Exception {
    String itemImpuestos = impuestosNode.toString();
    if (itemImpuestos != null && !itemImpuestos.isEmpty() && !itemImpuestos.equals("{}")) {
      JsonNode m = objectMapper.readTree(itemImpuestos);
      Iterator<JsonNode> impuestos = m.elements();

      while (impuestos.hasNext()) {
        ImpuestosItemFactura iif = new ImpuestosItemFactura();
        JsonNode imp = impuestos.next();

        iif.setCodigo(imp.path("codigo").asText());
        iif.setCodigoTarifa(imp.path("codigoTarifa").asText());
        iif.setFactorIva(imp.path("factorIva").asDouble());
        iif.setTarifa(imp.path("tarifa").asDouble());
        iif.setMonto(imp.path("monto").asDouble());
        iif.setMontoExportacion(imp.path("montoExportacion").asDouble());

        // Procesar exoneración si existe
        JsonNode exoneracionNode = imp.path("exoneracion");
        if (exoneracionNode != null && exoneracionNode.path("tipoDocumento").asText() != null
            && !exoneracionNode.path("tipoDocumento").asText().isEmpty()) {

          ExoneracionImpuestoItemFactura eiif = procesarExoneracion(exoneracionNode);
          if (eiif != null) {
            iif.addItemFacturaImpuestosExoneracion(eiif);
          }
        }

        item.addItemFacturaImpuestos(iif);
      }
    }
  }

  private ExoneracionImpuestoItemFactura procesarExoneracion(JsonNode exoneracionNode) {
    ExoneracionImpuestoItemFactura eiif = new ExoneracionImpuestoItemFactura();

    String tipoDocumento = exoneracionNode.path("tipoDocumento").asText("");
    String numeroDocumento = exoneracionNode.path("numeroDocumento").asText("");
    String nombreInstitucion = exoneracionNode.path("nombreInstitucion").asText("");
    String fechaEmision = exoneracionNode.path("fechaEmision").asText("");
    String montoExoneracion = exoneracionNode.path("montoExoneracion").asText("");
    String porcentajeExoneracion = exoneracionNode.path("porcentajeExoneracion").asText("");

    if (!tipoDocumento.isEmpty() && !numeroDocumento.isEmpty()
        && !nombreInstitucion.isEmpty() && !fechaEmision.isEmpty()
        && !montoExoneracion.isEmpty() && !porcentajeExoneracion.isEmpty()) {

      eiif.setTipoDocumento(tipoDocumento);
      eiif.setNumeroDocumento(numeroDocumento);
      eiif.setNombreInstitucion(nombreInstitucion);
      eiif.setFechaEmision(fechaEmision);
      try {
        eiif.setMontoExoneracion(Double.parseDouble(montoExoneracion));
        eiif.setPorcentajeExoneracion(Integer.parseInt(porcentajeExoneracion));
      } catch (NumberFormatException nfe) {
        return null; // datos numéricos inválidos
      }
      return eiif;
    }
    return null;
  }
}