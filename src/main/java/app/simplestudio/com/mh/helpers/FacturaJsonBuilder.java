package app.simplestudio.com.mh.helpers;

import app.simplestudio.com.models.entity.Factura;
import app.simplestudio.com.models.entity.ItemFactura;
import app.simplestudio.com.models.entity.ItemSurtido;
import app.simplestudio.com.models.entity.ImpuestosItemFactura;
import app.simplestudio.com.models.entity.ExoneracionImpuestoItemFactura;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FacturaJsonBuilder {

  public static class FacturaJsonResult {
    public final String detalleLinea;
    public final String facturaCompleta;

    public FacturaJsonResult(String detalleLinea, String facturaCompleta) {
      this.detalleLinea = detalleLinea;
      this.facturaCompleta = facturaCompleta;
    }
  }

  public FacturaJsonResult construirJsonDesdeFactura(Factura factura, String tipoDocumento) {
    StringBuilder detalleLineaBuilder = new StringBuilder();
    detalleLineaBuilder.append("{\"detalleLinea\":{");

    int i = 1;
    for (ItemFactura item : factura.getItems()) {
      if (i > 1) {
        detalleLineaBuilder.append(",");
      }
      detalleLineaBuilder.append("\"").append(i++).append("\":");
      construirItemJson(detalleLineaBuilder, item);
    }

    detalleLineaBuilder.append("}}");
    String detalleLinea = detalleLineaBuilder.toString();

    // Construir factura completa
    String facturaCompleta = construirFacturaCompletaJson(factura, tipoDocumento);

    return new FacturaJsonResult(detalleLinea, facturaCompleta);
  }

  private void construirItemJson(StringBuilder builder, ItemFactura item) {
    builder.append("{");
    builder.append("\"numeroLinea\":\"").append(item.getNumeroLinea()).append("\",");

    if (item.getPartidaArancelaria() != null && !item.getPartidaArancelaria().isEmpty()) {
      builder.append("\"partidaArancelaria\":\"")
          .append(procesarTexto(item.getPartidaArancelaria())).append("\",");
    }

    builder.append("\"codigo\":\"").append(item.getCodigo()).append("\",");

    // Códigos comerciales
    construirCodigosComerciales(builder, item);

    builder.append("\"cantidad\":\"").append(item.getCantidad()).append("\",");
    builder.append("\"unidadMedida\":\"").append(item.getUnidadMedida()).append("\",");
    builder.append("\"unidadMedidaComercial\":\"")
        .append(procesarTexto(item.getUnidadMedidaComercial())).append("\",");
    builder.append("\"detalle\":\"").append(procesarTexto(item.getDetalle())).append("\",");
    builder.append("\"precioUnitario\":\"").append(item.getPrecioUnitario()).append("\",");
    builder.append("\"montoTotal\":\"").append(item.getMontoTotal()).append("\",");

    // Descuentos (4.4 y compat 4.3)
    construirDescuentos(builder, item);

    // --- 4.4: detalleSurtido (si aplica) ---
    construirDetalleSurtido(builder, item);

    builder.append("\"subTotal\":\"").append(item.getSubTotal()).append("\",");

    // Impuestos
    construirImpuestos(builder, item);

    builder.append("\"impuestoNeto\":\"").append(item.getImpuestoNeto()).append("\",");
    builder.append("\"montoTotalLinea\":\"").append(item.getMontoTotalLinea()).append("\"");
    builder.append("}");
  }

  private void construirCodigosComerciales(StringBuilder builder, ItemFactura item) {
    builder.append("\"codigoComercial\":{");
    boolean hasComercial = false;

    if (item.getCodigoComercialTipo() != null && !item.getCodigoComercialTipo().isEmpty()) {
      builder.append("\"0\":{");
      builder.append("\"tipo\":\"").append(item.getCodigoComercialTipo()).append("\",");
      builder.append("\"codigo\":\"").append(procesarTexto(item.getCodigoComercialCodigo()))
          .append("\"");
      builder.append("}");
      hasComercial = true;
    }

    if (item.getCodigoComercialTipo2() != null && !item.getCodigoComercialTipo2().isEmpty()) {
      if (hasComercial) builder.append(",");
      builder.append("\"1\":{");
      builder.append("\"tipo\":\"").append(item.getCodigoComercialTipo2()).append("\",");
      builder.append("\"codigo\":\"").append(procesarTexto(item.getCodigoComercialCodigo2()))
          .append("\"");
      builder.append("}");
      hasComercial = true;
    }

    if (item.getCodigoComercialTipo3() != null && !item.getCodigoComercialTipo3().isEmpty()) {
      if (hasComercial) builder.append(",");
      builder.append("\"2\":{");
      builder.append("\"tipo\":\"").append(item.getCodigoComercialTipo3()).append("\",");
      builder.append("\"codigo\":\"").append(procesarTexto(item.getCodigoComercialCodigo3()))
          .append("\"");
      builder.append("}");
      hasComercial = true;
    }

    if (item.getCodigoComercialTipo4() != null && !item.getCodigoComercialTipo4().isEmpty()) {
      if (hasComercial) builder.append(",");
      builder.append("\"3\":{");
      builder.append("\"tipo\":\"").append(item.getCodigoComercialTipo4()).append("\",");
      builder.append("\"codigo\":\"").append(procesarTexto(item.getCodigoComercialCodigo4()))
          .append("\"");
      builder.append("}");
      hasComercial = true;
    }

    if (item.getCodigoComercialTipo5() != null && !item.getCodigoComercialTipo5().isEmpty()) {
      if (hasComercial) builder.append(",");
      builder.append("\"4\":{");
      builder.append("\"tipo\":\"").append(item.getCodigoComercialTipo5()).append("\",");
      builder.append("\"codigo\":\"").append(procesarTexto(item.getCodigoComercialCodigo5()))
          .append("\"");
      builder.append("}");
      hasComercial = true;
    }

    builder.append("},");
  }

  private void construirDescuentos(StringBuilder builder, ItemFactura item) {
    // "descuentos": { "0":{...}, "1":{...} } (solo si hay monto > 0)
    class D {
      final Double monto; final String cod; final String otro; final String nat;
      D(Double m, String c, String o, String n){ this.monto=m; this.cod=c; this.otro=o; this.nat=n; }
    }
    D[] ds = new D[] {
        new D(item.getMontoDescuento(),  item.getCodigoDescuento(),  item.getCodigoDescuentoOtro(),  item.getNaturalezaDescuento()),
        new D(item.getMontoDescuento2(), item.getCodigoDescuento2(), item.getCodigoDescuentoOtro2(), item.getNaturalezaDescuento2()),
        new D(item.getMontoDescuento3(), item.getCodigoDescuento3(), item.getCodigoDescuentoOtro3(), item.getNaturalezaDescuento3()),
        new D(item.getMontoDescuento4(), item.getCodigoDescuento4(), item.getCodigoDescuentoOtro4(), item.getNaturalezaDescuento4()),
        new D(item.getMontoDescuento5(), item.getCodigoDescuento5(), item.getCodigoDescuentoOtro5(), item.getNaturalezaDescuento5())
    };

    boolean hay = false;
    for (D d : ds) { if (d.monto != null && d.monto > 0.0) { hay = true; break; } }
    if (!hay) return;

    builder.append("\"descuentos\":{");
    int idxJson = 0;
    for (int i = 0; i < ds.length; i++) {
      D d = ds[i];
      if (d.monto == null || d.monto <= 0.0) continue;
      if (idxJson++ > 0) builder.append(",");

      builder.append("\"").append(i).append("\":{");
      builder.append("\"montoDescuento\":\"").append(d.monto).append("\"");

      if (d.cod != null && !d.cod.isEmpty()) {
        builder.append(",\"codigoDescuento\":\"").append(d.cod).append("\"");
        if ("99".equals(d.cod)) {
          if (d.otro != null && !d.otro.isEmpty())
            builder.append(",\"codigoDescuentoOTRO\":\"").append(StringEscapeUtils.escapeJson(d.otro)).append("\"");
          if (d.nat != null && !d.nat.isEmpty())
            builder.append(",\"naturalezaDescuento\":\"").append(StringEscapeUtils.escapeJson(d.nat)).append("\"");
        } else {
          if (d.nat != null && !d.nat.isEmpty())
            builder.append(",\"naturalezaDescuento\":\"").append(StringEscapeUtils.escapeJson(d.nat)).append("\"");
        }
      } else {
        if (d.nat != null && !d.nat.isEmpty())
          builder.append(",\"naturalezaDescuento\":\"").append(StringEscapeUtils.escapeJson(d.nat)).append("\"");
      }
      builder.append("}");
    }
    builder.append("},");
  }

  // --- NUEVO: detalleSurtido (componentes) 4.4 ---
  private void construirDetalleSurtido(StringBuilder builder, ItemFactura item) {
    if (item.getSurtidoComponentes() == null || item.getSurtidoComponentes().isEmpty()) return;

    builder.append("\"detalleSurtido\":[");
    int idx = 0;
    for (ItemSurtido is : item.getSurtidoComponentes()) {
      if (idx++ > 0) builder.append(",");
      builder.append("{");

      if (is.getCabysComponente() != null)
        builder.append("\"cabysComponente\":\"").append(is.getCabysComponente()).append("\",");
      if (is.getDescripcionComponente() != null)
        builder.append("\"descripcionComponente\":\"")
            .append(StringEscapeUtils.escapeJson(is.getDescripcionComponente())).append("\",");
      if (is.getCantidadComponente() != null)
        builder.append("\"cantidadComponente\":\"").append(is.getCantidadComponente()).append("\",");
      if (is.getPrecioUnitarioComponente() != null)
        builder.append("\"precioUnitarioComponente\":\"").append(is.getPrecioUnitarioComponente()).append("\",");

      // Descuento por componente (opcional)
      if (is.getMontoDescuentoComponente() != null && is.getMontoDescuentoComponente() > 0.0) {
        builder.append("\"montoDescuentoComponente\":\"").append(is.getMontoDescuentoComponente()).append("\"");
        if (is.getCodigoDescuentoComponente() != null && !is.getCodigoDescuentoComponente().isEmpty()) {
          builder.append(",\"codigoDescuentoComponente\":\"").append(is.getCodigoDescuentoComponente()).append("\"");
          if ("99".equals(is.getCodigoDescuentoComponente())) {
            if (is.getCodigoDescuentoOtroComponente() != null && !is.getCodigoDescuentoOtroComponente().isEmpty())
              builder.append(",\"codigoDescuentoOtroComponente\":\"")
                  .append(StringEscapeUtils.escapeJson(is.getCodigoDescuentoOtroComponente())).append("\"");
            if (is.getNaturalezaDescuentoComponente() != null && !is.getNaturalezaDescuentoComponente().isEmpty())
              builder.append(",\"naturalezaDescuentoComponente\":\"")
                  .append(StringEscapeUtils.escapeJson(is.getNaturalezaDescuentoComponente())).append("\"");
          } else {
            if (is.getNaturalezaDescuentoComponente() != null && !is.getNaturalezaDescuentoComponente().isEmpty())
              builder.append(",\"naturalezaDescuentoComponente\":\"")
                  .append(StringEscapeUtils.escapeJson(is.getNaturalezaDescuentoComponente())).append("\"");
          }
        } else {
          if (is.getNaturalezaDescuentoComponente() != null && !is.getNaturalezaDescuentoComponente().isEmpty())
            builder.append(",\"naturalezaDescuentoComponente\":\"")
                .append(StringEscapeUtils.escapeJson(is.getNaturalezaDescuentoComponente())).append("\"");
        }
        builder.append(",");
      }

      // Impuestos por componente (opcional)
      if (is.getImpuestosComponente() != null && !is.getImpuestosComponente().isEmpty()) {
        builder.append("\"impuestosComponente\":[");
        int qi = 0;
        for (ItemSurtido.ImpuestoComponente ic : is.getImpuestosComponente()) {
          if (qi++ > 0) builder.append(",");
          builder.append("{");
          if (ic.getCodigo() != null)
            builder.append("\"codigo\":\"").append(ic.getCodigo()).append("\",");
          if (ic.getTarifa() != null)
            builder.append("\"tarifa\":\"").append(ic.getTarifa()).append("\",");
          if (ic.getCodigoTarifa() != null)
            builder.append("\"codigoTarifa\":\"").append(ic.getCodigoTarifa()).append("\",");
          if (ic.getMonto() != null)
            builder.append("\"monto\":\"").append(ic.getMonto()).append("\"");
          if (builder.charAt(builder.length()-1) == ',') builder.setLength(builder.length()-1);
          builder.append("}");
        }
        builder.append("],");
      }

      if (builder.charAt(builder.length()-1) == ',') builder.setLength(builder.length()-1);
      builder.append("}");
    }
    builder.append("],");
  }

  private void construirImpuestos(StringBuilder builder, ItemFactura item) {
    builder.append("\"impuestos\":{");
    int q = 0;

    for (ImpuestosItemFactura impuesto : item.getImpuestosItemFactura()) {
      if (q > 0) builder.append(",");
      builder.append("\"").append(q).append("\":{");
      builder.append("\"codigo\":\"").append(impuesto.getCodigo()).append("\",");
      builder.append("\"tarifa\":\"").append(impuesto.getTarifa()).append("\",");
      builder.append("\"codigoTarifa\":\"").append(impuesto.getCodigoTarifa()).append("\",");
      builder.append("\"monto\":\"").append(impuesto.getMonto()).append("\"");

      // Exoneración si existe
      if (!impuesto.getExoneracionImpuestoItemFactura().isEmpty()) {
        ExoneracionImpuestoItemFactura exoneracion =
            impuesto.getExoneracionImpuestoItemFactura().get(0);
        builder.append(",\"exoneracion\":{");
        builder.append("\"tipoDocumento\":\"").append(exoneracion.getTipoDocumento()).append("\",");
        builder.append("\"numeroDocumento\":\"").append(exoneracion.getNumeroDocumento()).append("\",");
        builder.append("\"nombreInstitucion\":\"")
            .append(procesarTexto(exoneracion.getNombreInstitucion())).append("\",");
        builder.append("\"fechaEmision\":\"").append(exoneracion.getFechaEmision()).append("\",");
        builder.append("\"montoExoneracion\":\"").append(exoneracion.getMontoExoneracion()).append("\",");
        builder.append("\"porcentajeExoneracion\":\"").append(exoneracion.getPorcentajeExoneracion()).append("\"");
        builder.append("}");
      }

      builder.append("}");
      q++;
    }

    builder.append("},");
  }

  private String construirFacturaCompletaJson(Factura f, String tipoDocumento) {
    StringBuilder ftmp = new StringBuilder();
    ftmp.append("{");
    ftmp.append("\"situacion\":\"").append(f.getSituacion()).append("\",");
    ftmp.append("\"sucursal\":\"").append(f.getSucursal()).append("\",");
    ftmp.append("\"terminal\":\"").append(f.getTerminal()).append("\",");
    ftmp.append("\"omitirReceptor\":\"").append(procesarTexto(f.getOmitirReceptor())).append("\",");
    ftmp.append("\"receptorNombre\":\"").append(procesarTexto(f.getReceptorNombre())).append("\",");
    ftmp.append("\"receptorTipoIdentif\":\"").append(f.getReceptorTipoIdentif()).append("\",");
    ftmp.append("\"receptorNumIdentif\":\"").append(f.getReceptor_num_identif()).append("\",");

    // 4.4 header extras
    ftmp.append("\"proveedorSistemas\":\"")
        .append(procesarTexto(f.getProveedorSistemas() == null ? "" : f.getProveedorSistemas()))
        .append("\",");
    ftmp.append("\"codigoActividadReceptor\":\"")
        .append(f.getCodigoActividadReceptor() == null ? "" : f.getCodigoActividadReceptor())
        .append("\",");

    if (!tipoDocumento.equalsIgnoreCase("FEE")) {
      ftmp.append("\"receptorProvincia\":\"").append(f.getReceptorProvincia()).append("\",");
      ftmp.append("\"receptorCanton\":\"").append(f.getReceptorCanton()).append("\",");
      ftmp.append("\"receptorDistrito\":\"").append(f.getReceptorDistrito()).append("\",");
      ftmp.append("\"receptorBarrio\":\"").append(f.getReceptorBarrio()).append("\",");
      ftmp.append("\"receptorOtrasSenas\":\"").append(f.getReceptorOtrasSenas()).append("\",");
    }

    ftmp.append("\"receptorCodPaisTel\":\"").append(f.getReceptorCodPaisTel()).append("\",");
    ftmp.append("\"receptorTel\":\"").append(f.getReceptorTel()).append("\",");
    ftmp.append("\"receptorCodPaisFax\":\"").append(f.getReceptorCodPaisFax()).append("\",");
    ftmp.append("\"receptorFax\":\"").append(f.getReceptorFax()).append("\",");
    ftmp.append("\"receptorEmail\":\"").append(f.getReceptorEmail()).append("\",");
    ftmp.append("\"condVenta\":\"").append(f.getCondVenta()).append("\",");
    ftmp.append("\"plazoCredito\":\"").append(f.getPlazoCredito()).append("\",");

    // 4.4 medios de pago (array) o legacy 4.3
    if (f.getMedioPagoTotal1() != null || f.getMedioPagoTotal2() != null
        || f.getMedioPagoTotal3() != null || f.getMedioPagoTotal4() != null) {
      StringBuilder mp = new StringBuilder();
      mp.append("\"mediosPago\":[");
      int count = 0;

      String[] tipos = {f.getMedioPago(), f.getMedioPago2(), f.getMedioPago3(), f.getMedioPago4()};
      java.math.BigDecimal[] tots = {f.getMedioPagoTotal1(), f.getMedioPagoTotal2(),
          f.getMedioPagoTotal3(), f.getMedioPagoTotal4()};
      String[] otros = {f.getMedioPagoOtros1(), f.getMedioPagoOtros2(), f.getMedioPagoOtros3(),
          f.getMedioPagoOtros4()};

      for (int i2 = 0; i2 < 4; i2++) {
        if (tipos[i2] == null || tipos[i2].isEmpty()) continue;
        if (count++ > 0) mp.append(",");
        mp.append("{\"tipoMedioPago\":\"").append(tipos[i2]).append("\"");
        if (tots[i2] != null)
          mp.append(",\"totalMedioPago\":\"").append(tots[i2].toPlainString()).append("\"");
        if ("99".equals(tipos[i2]) && otros[i2] != null && !otros[i2].isEmpty())
          mp.append(",\"medioPagoOtros\":\"").append(StringEscapeUtils.escapeJson(otros[i2])).append("\"");
        mp.append("}");
      }
      mp.append("],");
      ftmp.append(mp.toString());
    } else {
      // Legacy 4.3
      ftmp.append("\"medioPago\":\"").append(f.getMedioPago()).append("\",");
      ftmp.append("\"medioPago2\":\"").append(f.getMedioPago2()).append("\",");
      ftmp.append("\"medioPago3\":\"").append(f.getMedioPago3()).append("\",");
      ftmp.append("\"medioPago4\":\"").append(f.getMedioPago4()).append("\",");
    }

    ftmp.append("\"codMoneda\":\"").append(f.getCodMoneda()).append("\",");
    ftmp.append("\"tipoCambio\":\"").append(f.getTipoCambio()).append("\",");

    // Totales
    ftmp.append("\"totalServGravados\":\"").append(f.getTotalServGravados()).append("\",");
    ftmp.append("\"totalServExentos\":\"").append(f.getTotalServExentos()).append("\",");
    if (!tipoDocumento.equalsIgnoreCase("FEE")) {
      ftmp.append("\"totalServExonerado\":\"").append(f.getTotalServExonerado()).append("\",");
    }
    ftmp.append("\"totalMercGravadas\":\"").append(f.getTotalMercGravadas()).append("\",");
    ftmp.append("\"totalMercExentas\":\"").append(f.getTotalMercExentas()).append("\",");
    if (!tipoDocumento.equalsIgnoreCase("FEE")) {
      ftmp.append("\"totalMercExonerada\":\"").append(f.getTotalMercExonerada()).append("\",");
    }
    ftmp.append("\"totalGravados\":\"").append(f.getTotalGravados()).append("\",");
    ftmp.append("\"totalExentos\":\"").append(f.getTotalExentos()).append("\",");
    if (!tipoDocumento.equalsIgnoreCase("FEE")) {
      ftmp.append("\"totalExonerado\":\"").append(f.getTotalExonerado()).append("\",");
    }
    ftmp.append("\"totalVentas\":\"").append(f.getTotalVentas()).append("\",");
    ftmp.append("\"totalDescuentos\":\"").append(f.getTotalDescuentos()).append("\",");
    ftmp.append("\"totalVentasNeta\":\"").append(f.getTotalVentaNeta()).append("\",");
    ftmp.append("\"totalImp\":\"").append(f.getTotalImp()).append("\",");
    if (!tipoDocumento.equalsIgnoreCase("FEE")) {
      ftmp.append("\"totalIVADevuelto\":\"").append(f.getTotalIVADevuelto()).append("\",");
    }
    ftmp.append("\"totalOtrosCargos\":\"").append(f.getTotalOtrosCargos()).append("\",");
    ftmp.append("\"totalComprobante\":\"").append(f.getTotalComprobante()).append("\",");
    ftmp.append("\"otros\":\"").append(procesarTexto(f.getOtros())).append("\",");
    ftmp.append("\"numeroFactura\":\"").append(f.getNumeroFactura()).append("\"");
    ftmp.append("}");

    log.info("obteniendo el número de factura: {}", f.getNumeroFactura());

    return ftmp.toString();
  }

  private String procesarTexto(String texto) {
    if (texto == null) return "";
    // Para JSON, es mejor escapar como JSON (no como Java)
    return StringEscapeUtils.escapeJson(texto);
  }
}