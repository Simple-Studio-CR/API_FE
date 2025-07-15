package app.simplestudio.com.models.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "facturas_items_impuestos_exoneraciones")
public class ExoneracionImpuestoItemFactura {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "tipo_documento", length = 2)
  private String tipoDocumento;
  
  @Column(name = "numero_documento", length = 17)
  private String numeroDocumento;
  
  @Column(name = "nombre_institucion", length = 100)
  private String nombreInstitucion;
  
  @Column(name = "fecha_emision", length = 30)
  private String fechaEmision;
  
  @Column(name = "porcentaje_exoneracion", length = 3)
  private int porcentajeExoneracion;
  
  @Column(name = "monto_exoneracion", precision = 18, scale = 5)
  private Double montoExoneracion;
  
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
  
  public String getNumeroDocumento() {
    return this.numeroDocumento;
  }
  
  public void setNumeroDocumento(String numeroDocumento) {
    this.numeroDocumento = numeroDocumento;
  }
  
  public String getNombreInstitucion() {
    return this.nombreInstitucion;
  }
  
  public void setNombreInstitucion(String nombreInstitucion) {
    this.nombreInstitucion = nombreInstitucion;
  }
  
  public String getFechaEmision() {
    return this.fechaEmision;
  }
  
  public void setFechaEmision(String fechaEmision) {
    this.fechaEmision = fechaEmision;
  }
  
  public int getPorcentajeExoneracion() {
    return this.porcentajeExoneracion;
  }
  
  public void setPorcentajeExoneracion(int porcentajeExoneracion) {
    this.porcentajeExoneracion = porcentajeExoneracion;
  }
  
  public Double getMontoExoneracion() {
    return this.montoExoneracion;
  }
  
  public void setMontoExoneracion(Double montoExoneracion) {
    this.montoExoneracion = montoExoneracion;
  }
}

