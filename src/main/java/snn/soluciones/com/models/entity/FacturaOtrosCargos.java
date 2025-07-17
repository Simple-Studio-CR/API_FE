package snn.soluciones.com.models.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "facturas_otros_cargos")
public class FacturaOtrosCargos {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "tipoDocumento", length = 2)
  private String tipoDocumento;
  
  @Column(name = "numero_identidad_tercero", length = 12)
  private String numeroIdentidadTercero;
  
  @Column(name = "nombre_tercero", length = 100)
  private String nombreTercero;
  
  @Column(length = 160)
  private String detalle;
  
  @Column(length = 11)
  private String porcentaje;
  
  @Column(name = "monto_cargo", length = 20)
  private String montoCargo;
  
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
  
  public String getNumeroIdentidadTercero() {
    return this.numeroIdentidadTercero;
  }
  
  public void setNumeroIdentidadTercero(String numeroIdentidadTercero) {
    this.numeroIdentidadTercero = numeroIdentidadTercero;
  }
  
  public String getNombreTercero() {
    return this.nombreTercero;
  }
  
  public void setNombreTercero(String nombreTercero) {
    this.nombreTercero = nombreTercero;
  }
  
  public String getDetalle() {
    return this.detalle;
  }
  
  public void setDetalle(String detalle) {
    this.detalle = detalle;
  }
  
  public String getPorcentaje() {
    return this.porcentaje;
  }
  
  public void setPorcentaje(String porcentaje) {
    this.porcentaje = porcentaje;
  }
  
  public String getMontoCargo() {
    return this.montoCargo;
  }
  
  public void setMontoCargo(String montoCargo) {
    this.montoCargo = montoCargo;
  }
}

