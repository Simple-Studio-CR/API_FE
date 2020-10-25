package app.simplestudio.com.models.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "mensaje_receptor")
public class MensajeReceptor {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(length = 4)
  public String tipoDocumento;
  
  @Column(length = 50)
  public String clave;
  
  @Column(name = "clave_documento_emisor", length = 50)
  public String claveDocumentoEmisor;
  
  @Column(name = "numero_cedula_emisor")
  public String numeroCedulaEmisor;
  
  @Column(name = "fecha_emision_doc", length = 40)
  public String fechaEmisionDoc;
  
  @Column(length = 2)
  public String mensaje;
  
  @Column(name = "detalle_mensaje", length = 500)
  public String detalleMensaje;
  
  @Column(name = "codigo_actividad", length = 6)
  public String codigoActividad;
  
  @Column(name = "condicion_impuesto", length = 2)
  public String condicionImpuesto;
  
  @Column(name = "monto_total_impuesto_acreditar", length = 20)
  public String montoTotalImpuestoAcreditar;
  
  @Column(name = "monto_total_de_gasto_aplicable", length = 20)
  public String montoTotalDeGastoAplicable;
  
  @Column(name = "monto_total_impuesto", length = 20)
  public String montoTotalImpuesto;
  
  @Column(name = "total_factura", length = 20)
  public String totalFactura;
  
  @Column(name = "numero_cedula_receptor", length = 12)
  public String numeroCedulaReceptor;
  
  @Column(name = "numero_consecutivo_receptor", length = 20)
  public String numeroConsecutivoReceptor;
  
  public Long getId() {
    return this.id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public String getTipoDocumento() {
    return this.tipoDocumento;
  }
  
  public void setTipoDocumento(String tipoDocumento) {
    this.tipoDocumento = tipoDocumento;
  }
  
  public String getClave() {
    return this.clave;
  }
  
  public void setClave(String clave) {
    this.clave = clave;
  }
  
  public String getClaveDocumentoEmisor() {
    return this.claveDocumentoEmisor;
  }
  
  public void setClaveDocumentoEmisor(String claveDocumentoEmisor) {
    this.claveDocumentoEmisor = claveDocumentoEmisor;
  }
  
  public String getNumeroCedulaEmisor() {
    return this.numeroCedulaEmisor;
  }
  
  public void setNumeroCedulaEmisor(String numeroCedulaEmisor) {
    this.numeroCedulaEmisor = numeroCedulaEmisor;
  }
  
  public String getFechaEmisionDoc() {
    return this.fechaEmisionDoc;
  }
  
  public void setFechaEmisionDoc(String fechaEmisionDoc) {
    this.fechaEmisionDoc = fechaEmisionDoc;
  }
  
  public String getMensaje() {
    return this.mensaje;
  }
  
  public void setMensaje(String mensaje) {
    this.mensaje = mensaje;
  }
  
  public String getDetalleMensaje() {
    return this.detalleMensaje;
  }
  
  public void setDetalleMensaje(String detalleMensaje) {
    this.detalleMensaje = detalleMensaje;
  }
  
  public String getCodigoActividad() {
    return this.codigoActividad;
  }
  
  public void setCodigoActividad(String codigoActividad) {
    this.codigoActividad = codigoActividad;
  }
  
  public String getCondicionImpuesto() {
    return this.condicionImpuesto;
  }
  
  public void setCondicionImpuesto(String condicionImpuesto) {
    this.condicionImpuesto = condicionImpuesto;
  }
  
  public String getMontoTotalImpuestoAcreditar() {
    return this.montoTotalImpuestoAcreditar;
  }
  
  public void setMontoTotalImpuestoAcreditar(String montoTotalImpuestoAcreditar) {
    this.montoTotalImpuestoAcreditar = montoTotalImpuestoAcreditar;
  }
  
  public String getMontoTotalDeGastoAplicable() {
    return this.montoTotalDeGastoAplicable;
  }
  
  public void setMontoTotalDeGastoAplicable(String montoTotalDeGastoAplicable) {
    this.montoTotalDeGastoAplicable = montoTotalDeGastoAplicable;
  }
  
  public String getMontoTotalImpuesto() {
    return this.montoTotalImpuesto;
  }
  
  public void setMontoTotalImpuesto(String montoTotalImpuesto) {
    this.montoTotalImpuesto = montoTotalImpuesto;
  }
  
  public String getTotalFactura() {
    return this.totalFactura;
  }
  
  public void setTotalFactura(String totalFactura) {
    this.totalFactura = totalFactura;
  }
  
  public String getNumeroCedulaReceptor() {
    return this.numeroCedulaReceptor;
  }
  
  public void setNumeroCedulaReceptor(String numeroCedulaReceptor) {
    this.numeroCedulaReceptor = numeroCedulaReceptor;
  }
  
  public String getNumeroConsecutivoReceptor() {
    return this.numeroConsecutivoReceptor;
  }
  
  public void setNumeroConsecutivoReceptor(String numeroConsecutivoReceptor) {
    this.numeroConsecutivoReceptor = numeroConsecutivoReceptor;
  }
}

