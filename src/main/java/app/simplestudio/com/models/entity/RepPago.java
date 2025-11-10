package app.simplestudio.com.models.entity;

import jakarta.persistence.Transient;

public class RepPago {

  @Transient
  private String tipoMedioPago;      // "01"=Efectivo, "02"=Tarjeta, "03"=Cheque, "04"=Transferencia, "99"=Otros
  @Transient
  private String montoPago;          // String para mantener formato exacto (ej: "15000.00")
  @Transient
  private String moneda;             // Opcional (ej: "CRC"|"USD")
  @Transient
  private String numeroTransaccion;  // Opcional (autorización/trace)
  @Transient
  private String medioPagoOtros;     // Requerido si tipoMedioPago="99"

  public String getTipoMedioPago() { return tipoMedioPago; }
  public void setTipoMedioPago(String tipoMedioPago) { this.tipoMedioPago = tipoMedioPago; }

  public String getMontoPago() { return montoPago; }
  public void setMontoPago(String montoPago) { this.montoPago = montoPago; }

  public String getMoneda() { return moneda; }
  public void setMoneda(String moneda) { this.moneda = moneda; }

  public String getNumeroTransaccion() { return numeroTransaccion; }
  public void setNumeroTransaccion(String numeroTransaccion) { this.numeroTransaccion = numeroTransaccion; }

  public String getMedioPagoOtros() { return medioPagoOtros; }
  public void setMedioPagoOtros(String medioPagoOtros) { this.medioPagoOtros = medioPagoOtros; }
}