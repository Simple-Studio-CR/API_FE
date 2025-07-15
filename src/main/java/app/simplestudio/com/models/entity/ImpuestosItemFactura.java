package app.simplestudio.com.models.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "facturas_items_impuestos")
public class ImpuestosItemFactura {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
  @JoinColumn(name = "impuestos_item_factura_id")
  private List<ExoneracionImpuestoItemFactura> exoneracionImpuestoItemFactura = new ArrayList<>();
  
  @Column(length = 5)
  private String codigo;
  
  @Column(name = "codigo_tarifa", length = 2)
  private String codigoTarifa;
  
  @Column(precision = 4)
  private Double tarifa;
  
  @Column(name = "factor_iva", precision = 4)
  private Double factorIva;
  
  @Column(precision = 20)
  private Double monto;
  
  @Column(name = "impuesto_neto", precision = 20)
  private Double impuestoNeto;
  
  @Column(name = "monto_exportacion", precision = 20)
  private Double montoExportacion;
  
  public Long getId() {
    return this.id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public List<ExoneracionImpuestoItemFactura> getExoneracionImpuestoItemFactura() {
    return this.exoneracionImpuestoItemFactura;
  }
  
  public void setExoneracionImpuestoItemFactura(List<ExoneracionImpuestoItemFactura> exoneracionImpuestoItemFactura) {
    this.exoneracionImpuestoItemFactura = exoneracionImpuestoItemFactura;
  }
  
  public String getCodigo() {
    return this.codigo;
  }
  
  public void setCodigo(String codigo) {
    this.codigo = codigo;
  }
  
  public Double getTarifa() {
    return this.tarifa;
  }
  
  public void setTarifa(Double tarifa) {
    this.tarifa = tarifa;
  }
  
  public Double getMonto() {
    return this.monto;
  }
  
  public void setMonto(Double monto) {
    this.monto = monto;
  }
  
  public Double getImpuestoNeto() {
    return this.impuestoNeto;
  }
  
  public void setImpuestoNeto(Double impuestoNeto) {
    this.impuestoNeto = impuestoNeto;
  }
  
  public String getCodigoTarifa() {
    return this.codigoTarifa;
  }
  
  public void setCodigoTarifa(String codigoTarifa) {
    this.codigoTarifa = codigoTarifa;
  }
  
  public Double getFactorIva() {
    return this.factorIva;
  }
  
  public void setFactorIva(Double factorIva) {
    this.factorIva = factorIva;
  }
  
  public Double getMontoExportacion() {
    return this.montoExportacion;
  }
  
  public void setMontoExportacion(Double montoExportacion) {
    this.montoExportacion = montoExportacion;
  }
  
  public void addItemFacturaImpuestosExoneracion(ExoneracionImpuestoItemFactura item) {
    this.exoneracionImpuestoItemFactura.add(item);
  }
}

