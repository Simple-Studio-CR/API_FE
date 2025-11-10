package app.simplestudio.com.mh.helpers;

import app.simplestudio.com.models.entity.Factura;
import app.simplestudio.com.models.entity.RepPago;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RepJsonBuilder {

  public String construirRepJson(Factura f) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");

    // Encabezado básico (sucursal/terminal/situacion reusables)
    sb.append("\"situacion\":\"").append(nvl(f.getSituacion())).append("\",");
    sb.append("\"sucursal\":\"").append(nvl(f.getSucursal())).append("\",");
    sb.append("\"terminal\":\"").append(nvl(f.getTerminal())).append("\",");

    // 4.4 header extra (por consistencia)
    sb.append("\"proveedorSistemas\":\"").append(esc(f.getProveedorSistemas())).append("\",");

    // Receptor (mínimo para REP)
    sb.append("\"receptorNombre\":\"").append(esc(f.getReceptorNombre())).append("\",");
    sb.append("\"receptorTipoIdentif\":\"").append(nvl(f.getReceptorTipoIdentif())).append("\",");
    sb.append("\"receptorNumIdentif\":\"").append(nvl(f.getReceptor_num_identif())).append("\",");

    // Referencia al documento pagado
    sb.append("\"referencia\":{");
    sb.append("\"tipoDocumento\":\"").append(nvl(f.getRepTipoDocReferencia())).append("\",");
    sb.append("\"numero\":\"").append(esc(f.getRepNumeroReferencia())).append("\",");
    sb.append("\"fechaEmision\":\"").append(esc(f.getRepFechaReferencia())).append("\"");
    sb.append("},");

    // Observaciones opcionales
    sb.append("\"observaciones\":\"").append(esc(f.getRepObservaciones())).append("\",");

    // Pagos
    sb.append("\"pagos\":[");
    int i = 0;
    if (f.getRepPagos() != null) {
      for (RepPago p : f.getRepPagos()) {
        if (p == null) continue;
        if (i++ > 0) sb.append(",");
        sb.append("{");
        sb.append("\"tipoMedioPago\":\"").append(nvl(p.getTipoMedioPago())).append("\"");
        if (nvl(p.getMontoPago()).length() > 0)
          sb.append(",\"montoPago\":\"").append(p.getMontoPago()).append("\"");
        if (nvl(p.getMoneda()).length() > 0)
          sb.append(",\"moneda\":\"").append(p.getMoneda()).append("\"");
        if (nvl(p.getNumeroTransaccion()).length() > 0)
          sb.append(",\"numeroTransaccion\":\"").append(esc(p.getNumeroTransaccion())).append("\"");
        if ("99".equals(p.getTipoMedioPago()) && nvl(p.getMedioPagoOtros()).length() > 0)
          sb.append(",\"medioPagoOtros\":\"").append(esc(p.getMedioPagoOtros())).append("\"");
        sb.append("}");
      }
    }
    sb.append("]");

    sb.append("}");
    log.info("REP JSON construido");
    return sb.toString();
  }

  private String nvl(String s){ return s == null ? "" : s; }
  private String esc(String s){ return s == null ? "" : StringEscapeUtils.escapeJson(s); }
}